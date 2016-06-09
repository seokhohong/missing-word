__author__ = 'SEOKHO'

from model.bridge import getMergedFeatures
from model.bridge import appendLabels
from model.gold import loadGolds
from model.sentence import Sentence
from sklearn.ensemble import RandomForestClassifier
import random
import sklearn
from sklearn.metrics import classification_report
import pickle

class IterativeFilterModel:
    def __init__(self, sentenceLength, trainCorrectionThreshold = 0.45):
        self.sentenceLength = sentenceLength
        self.trainCorrectionThreshold = trainCorrectionThreshold
        self.tossedReintroduction = 0.2

    #does NOT edit the features passed in through editFeatures, instead returns the modified copy
    def computeSentenceModelProbabilities(self, trainFeatures, trainLabels, testFeatures, testLabels):
        clf = RandomForestClassifier(n_estimators = 100, n_jobs = 5)
        print("Fitting Model")
        clf.fit(trainFeatures, trainLabels)
        results = clf.predict(testFeatures)
        print(classification_report(results, testLabels))
        modifiedFeatures = testFeatures.copy()
        probs = clf.predict_proba(testFeatures)

        correctnessVector = []
        for i in range(len(results)):
            correctnessVector.append(1 if testLabels[i] == results[i] else 0)
            #use the class probability outputs as features for the correction model
            for index, elem in enumerate(probs[i]):
                modifiedFeatures[i][index] = elem

        return clf, correctnessVector, modifiedFeatures

    def trainSentenceModel(self, data):
        random.shuffle(data)

        cutoff = int(len(data) * 1 / 2)

        featureSet1 = [datum[0] for datum in data[:cutoff]]
        labelSet1 = [datum[1] for datum in data[:cutoff]]

        featureSet2 = [datum[0] for datum in data[cutoff:]]
        labelSet2 = [datum[1] for datum in data[cutoff:]]

        clf1, correctnessVector2, modFeatures2 = self.computeSentenceModelProbabilities(featureSet1, labelSet1, featureSet2, labelSet2)
        clf2, correctnessVector1, modFeatures1 = self.computeSentenceModelProbabilities(featureSet2, labelSet2, featureSet1, labelSet1)

        return clf1, modFeatures1 + modFeatures2, correctnessVector1 + correctnessVector2

    def trainCorrectionModel(self, correctionalFeatures, correctionalLabels, tossedDataBalancer):

        #add tossed data with label 0 so we don't get imbalanced training here
        for datum in tossedDataBalancer:
            correctionalFeatures.append(datum[0])
            correctionalLabels.append(0)

        data = [(correctionalFeatures[i], correctionalLabels[i]) for i in range(len(correctionalFeatures))]

        #vastly simplify training data
        for i, datum in enumerate(data):
            data[i] = (datum[0][:15] + datum[0][-30:], datum[1])

        imbalance = (sum([1 for datum in data if datum[1] == 1]) + 1) / (sum([1 for datum in data if datum[1] == 0]) + 1)

        random.shuffle(data)

        print("Imbalance", imbalance)


        cutoff = int(len(data) * 7/10)

        featureSet1 = [datum[0] for datum in data[:cutoff]]
        labelSet1 = [datum[1] for datum in data[:cutoff]]

        balance = []
        for label in labelSet1:
            balance.append(imbalance * 5 if label == 0 else 1)

        featureSet2 = [datum[0] for datum in data[cutoff:]]
        labelSet2 = [datum[1] for datum in data[cutoff:]]


        clf = RandomForestClassifier(n_estimators=500, n_jobs = 5)
        print("Fitting Correction Model")
        #clf.fit(featureSet1, labelSet1, balance)
        clf.fit(featureSet1, labelSet1)
        print(classification_report(clf.predict(featureSet1), labelSet1))
        print(classification_report(clf.predict(featureSet2), labelSet2))
        print("Correction Model Complete")
        return clf

    def cleanModelAFromData(self, data):
        for datum in data:
            for i in range(self.sentenceLength):
                datum[0][i] = 0

    def filterData(self, data, correctionClf, sentenceClf):
        print("Filtering...")

        probs = sentenceClf.predict_proba([datum[0] for datum in data])

        for i in range(len(probs)):
            #use the class probability outputs as features for the correction model
            for index, elem in enumerate(probs[i]):
                data[i][0][index] = elem

        predictions = correctionClf.predict_proba([datum[0] for datum in data])
        newData = [datum for index, datum in enumerate(data) if predictions[index][1] > self.trainCorrectionThreshold]
        tossedData = [datum for index, datum in enumerate(data) if predictions[index][1] <= self.trainCorrectionThreshold]

        print("Ratio of accepted data", str(float(len(newData)) / len(data)))

        self.cleanModelAFromData(newData)
        self.cleanModelAFromData(tossedData)

        return newData, tossedData

    #helps the correction classifier converge
    def addSomeTossedData(self, tossedData):
        return [datum for datum in tossedData if random.random() > self.tossedReintroduction]

    def classifyTossedData(self, tossedData, clf):
        if len(tossedData) > 0:
            predictionProbs = clf.predict_proba([datum[0] for datum in tossedData])
            #make correction features
            for i in range(len(predictionProbs)):
                #use the class probability outputs as features for the correction model
                for index, elem in enumerate(predictionProbs[i]):
                    tossedData[i][0][index] = elem

    def appendSentenceProbs(self, data):
        for i in range(len(data)):
            data[i] = ([0] * self.sentenceLength + data[i][0], data[i][1])

    def createData(self, output, numDataPoints):

        allFeatures = getMergedFeatures("C:/MissingWord/mergedFeatures"+str(self.sentenceLength)+".txt", numDataPoints)

        print("Loaded Features")
        golds = loadGolds("C:/MissingWord/"+str(self.sentenceLength)+"Gold.txt", numGolds = len(allFeatures), length = self.sentenceLength)

        print(len(allFeatures))

        sentences = []
        for i in range(len(allFeatures)):
            sentences.append(Sentence(golds[i], allFeatures[i]))

        appendLabels(str(self.sentenceLength)+"rf15.txt", sentences)
        appendLabels(str(self.sentenceLength)+"rf110.txt", sentences)

        data = [(sentence.getAllFeatures(), sentence.getGold().getRemovedIndex()) for sentence in sentences]

        with open(output, "wb") as f:
            pickle.dump(data, f)

        return data
    def loadData(self, fromFile):
        with open(fromFile, "rb") as f:
            data = pickle.load(f)

        print("Loaded Data", len(data))
        self.appendSentenceProbs(data)
        print("Initialized Data")
        return data

    def test(self):
        data = self.createData("smallData.txt", 1000)
        #with open("data.pickle", "rb") as f:
        #    data = pickle.load(f)

        testData = data[-300:]

        self.appendSentenceProbs(testData)

        for i in range(len(data)):
            data[i] = ([0] * self.sentenceLength + data[i][0], data[i][1])

        with open("C:/MissingWord/clf/sentence"+str(self.sentenceLength)+".clf", "rb") as f:
            sentenceClf = pickle.load(f)

        with open("C:/MissingWord/clf/correction"+str(self.sentenceLength)+".clf", "rb") as f:
            correctionClf = pickle.load(f)

        probs = sentenceClf.predict_proba([datum[0] for datum in testData])
        labels = [datum[1] for datum in testData]

        for i in range(len(testData)):
            for j in range(len(probs[i])):
                testData[i][0][j] = probs[i][j]

        correctionResults = correctionClf.predict([datum[0][self.sentenceLength:] + datum[0][-2 * self.sentenceLength:] for datum in testData])

        sentenceResults = sentenceClf.predict([datum[0] for datum in testData])

        passThrough = sum(correctionResults)

        print("Pass Through", passThrough)

        results = []
        retainedLabels = []

        for i in range(len(correctionResults)):
            if correctionResults[i] == 1:
                results.append(sentenceResults[i])
                retainedLabels.append(labels[i])

        print(classification_report(results, retainedLabels))

    def train(self, dataSize, numIters = 5):

        originalData = self.loadData()

        #start unfiltered
        data = originalData[:dataSize]
        tossedData = [] #filtered out

        for i in range(numIters):

            print("Beginning Iteration", i)
            sentenceClf, correctionFeatures, correctionLabels = self.trainSentenceModel(data)
            correctionClf = self.trainCorrectionModel(correctionFeatures, correctionLabels, self.addSomeTossedData(tossedData))
            self.classifyTossedData(tossedData, correctionClf)
            data, tossedData = self.filterData(originalData[:dataSize], correctionClf, sentenceClf)

            with open("C:/MissingWord/clf/sentence"+str(self.sentenceLength)+"i"+str(i)+".clf", "wb") as f:
                pickle.dump(sentenceClf, f)

            with open("C:/MissingWord/clf/correction"+str(self.sentenceLength)+"i"+str(i)+".clf", "wb") as f:
                pickle.dump(correctionClf, f)

if __name__ == "__main__":
    model = IterativeFilterModel(15)
    #model.train(1000, numIters = 1)
    model.test()