__author__ = 'SEOKHO'

from model.bridge import getMergedFeatures
from model.bridge import appendLabels
from model.gold import loadGolds
from model import gold
from model.sentence import Sentence
from sklearn import cross_validation
from sklearn.ensemble import RandomForestClassifier
from model import modelSystem
from model import util
import random
import sklearn
import model.bridge as bridge
from sklearn.metrics import classification_report
import pickle
import numpy as np

CV = True

class PositionModelStructure:
    def __init__(self, sentenceLength):
        self.sentenceLength = sentenceLength

    def trainIntermediateModels(self, numData, frameBatchData):
        rawFeatures = bridge.loadMergedFeatures(self.sentenceLength, end = numData)
        sentenceFeatures = modelSystem.rawToSentenceFeatures(rawFeatures)
        golds = gold.loadDefaultGolds(self.sentenceLength)

        sentences = []
        for i in range(len(sentenceFeatures)):
            sentences.append(Sentence(golds[i], sentenceFeatures[i]))

        for ratio in range(len(frameBatchData)):
            for index, additionalLabels in enumerate(frameBatchData[ratio]):
                sentences[index].addAdditionalFeatures(additionalLabels)

        batchSentences = util.cutInHalf(sentences)

        predictionsPerBatch = []
        for ratio in range(len(frameBatchData)):
            predictionsPerBatch.append([0])

        for batch in range(2):
            batchPredictions = []
            for i in range(len(batchSentences[0])):
                batchPredictions.append([0] * self.sentenceLength)
            for pos in range(self.sentenceLength):
                model = PositionModel(self.sentenceLength, pos)
                model.train(batchSentences[batch])
                predictions = model.predict([sentence.getAllFeatures() for sentence in batchSentences[abs(1 - batch)]])
                for i in range(len(predictions)):
                    batchPredictions[i][pos] = predictions[i]

            predictionsPerBatch[abs(1 - batch)] = batchPredictions

        allPredictions = predictionsPerBatch[0] + predictionsPerBatch[1]

        return allPredictions
        #with open(modelSystem.posModelIntermediateOutput(self.sentenceLength), "wb") as f:
        #    pickle.dump(allPredictions, f)

    def trainPredictiveModel(self, rawFeatures, golds, frameBatchData):
        sentenceFeatures = modelSystem.rawToSentenceFeatures(rawFeatures)

        sentences = []
        for i in range(len(sentenceFeatures)):
            sentences.append(Sentence(golds[i], sentenceFeatures[i]))

        if frameBatchData is not None:
            for index, additionalLabels in enumerate(frameBatchData):
                sentences[index].addAdditionalFeatures(additionalLabels)

        for i in range(self.sentenceLength):
            posModel = PositionModel(self.sentenceLength, i)
            posModel.train(np.array(sentences))
            posModel.export(modelSystem.posModelPredictionFile(self.sentenceLength, i))

    #combined prediction
    def predict(self, rawFeatures, labelsToAppend):
        sentenceFeatures = modelSystem.rawToSentenceFeatures(rawFeatures)
        predictionMatrix = []
        predictionMatrix.append([[1, 0]] * len(sentenceFeatures)) #for 0th column

        sentences = []
        for i in range(len(sentenceFeatures)):
            sentence = Sentence(None, sentenceFeatures[i])
            if labelsToAppend is not None:
                sentence.addAdditionalFeatures(labelsToAppend[i])
            sentences.append(sentence)

        for i in range(1, self.sentenceLength):
            clf = self.loadPositionModel(i)
            prediction = clf.predict_proba([sentence.getAllFeatures() for sentence in sentences])
            for a in range(len(prediction)):
                if type(prediction[a]) == int:
                    prediction[a] = [prediction[a], 1 - prediction[a]]
            predictionMatrix.append(prediction)

        transposed = []

        for i in range(len(predictionMatrix[0])):
            predictionSet = []
            for j in range(len(predictionMatrix)):
                predictionSet.append(predictionMatrix[j][i])
            transposed.append(predictionSet)

        return np.array(transposed)

    def loadPositionModel(self, index):
        with open(modelSystem.posModelPredictionFile(self.sentenceLength, index), "rb") as f:
            return pickle.load(f)

    def validatePrediction(self, golds):
        with open(modelSystem.posModelPredictionOutput(self.sentenceLength), "rb") as f:
            predictions = pickle.load(f)

        for i, prediction in enumerate(predictions):
            print(golds[i].getRemovedIndex(), prediction)
            if i > 10:
                break


class PositionModel:
    def __init__(self, sentenceLength, index):
        self.sentenceLength = sentenceLength
        self.index = index

    def train(self, sentences):
        onesData = [(sentence.getAllFeatures(), 1) for sentence in sentences if sentence.getGold().getRemovedIndex() == self.index]
        zerosData = [(sentence.getAllFeatures(), 0) for sentence in sentences if sentence.getGold().getRemovedIndex() != self.index]

        print(len(onesData), len(zerosData))

        data = onesData + zerosData
        random.shuffle(data)

        if CV:
            trainFeatures, testFeatures, trainLabels, testLabels = cross_validation.train_test_split([datum[0] for datum in data], [datum[1] for datum in data], test_size = 0.1)
            print("Training PositionModel "+str(self.index))
            self.clf = RandomForestClassifier(n_estimators = 500, n_jobs = 7)
            self.clf.fit(trainFeatures, trainLabels)
            print(classification_report(self.clf.predict(trainFeatures), trainLabels))
            print(classification_report(self.clf.predict(testFeatures), testLabels))

    def predict(self, features):
        return self.clf.predict_proba(features)

    def chunks(l, n):
        for i in range(0, len(l), n):
            yield l[i:i+n]

    def export(self, filename):
        if self.clf is not None:
            with open(filename, "wb") as f:
                pickle.dump(self.clf, f)

if __name__ == "__main__":
    #output(15)
    pms = PositionModelStructure(15, [5, 10])
    '''
    pms.trainIntermediateModels()
'''

    #pms.trainPredictiveModel()


    frameFeatures = bridge.loadMergedFeatures(15, 100000, 110000)
    sentenceFeatures = modelSystem.rawToSentenceFeatures(frameFeatures)
    pms.predict(sentenceFeatures)

    pms.validatePrediction(gold.loadGolds("C:/MissingWord/"+str(15)+"Gold.txt", numGolds = 110000)[100000:110000])
