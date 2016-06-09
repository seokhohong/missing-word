__author__ = 'SEOKHO'

import model.bridge as bridge
import model.gold as gold
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report
from model.sentence import Sentence
from model import modelSystem
import pickle
import numpy as np

sentenceLength = 15
numData = 100000

class NPositionModel:
    def __init__(self, sentenceLength):
        self.sentenceLength = sentenceLength

    def predict(self, rawFeatures, posPredictions):
        sentenceFeatures = modelSystem.rawToSentenceFeatures(rawFeatures)
        features, toPredict = self.assembleData(posPredictions, sentenceFeatures)
        data = [datum[0] for datum in features]

        with open(modelSystem.nPosModelFile(self.sentenceLength), "rb") as f:
            clf = pickle.load(f)

        #only the ones worth classifying
        reducedPredictions = clf.predict(data)
        fullPredictions = [0] * len(sentenceFeatures)

        predictionIndex = 0
        for i in range(len(fullPredictions)):
            if i in toPredict:
                fullPredictions[i] = reducedPredictions[predictionIndex]
                predictionIndex += 1

        return fullPredictions

    def assembleData(self, posPredictions, sentenceFeatures, golds = None):

        toPredict = [] #non all-zero indices
        flattened = []
        '''
        for index, predictionSet in enumerate(posPredictions):
            flat = np.array([pos[0] for pos in predictionSet])
            for value in flat:
                if value < 0.5:
                    toPredict.append(index)
                    break
            flattened.append(flat)
        '''

        count = [0] * 15
        for index, predictionSet in enumerate(posPredictions):
            flat = np.array([pos[0] for pos in predictionSet])
            flattened.append(flat)
            binarized = [1 if value < 0.5 else 0 for value in flat]
            count[binarized.count(1)] += 1
            if(binarized.count(1) == 1 or binarized.count(1) == 2):
                toPredict.append(index)

        for i in range(len(count)):
            print(count[i])

        sentences = []
        for i in range(len(sentenceFeatures)):
            sentence = Sentence(golds[i] if golds is not None else None, sentenceFeatures[i])
            sentence.addAdditionalFeatures(flattened[i])
            sentences.append(sentence)

        data = []
        for i in toPredict:
            if i > len(sentences):
                break
            data.append((sentences[i].getAllFeatures(), sentences[i].getRemovedIndex()))

        return np.array(data), toPredict

    def trainPredictiveModel(self, rawFeatures, golds, posPredictions):
        sentenceFeatures = modelSystem.rawToSentenceFeatures(rawFeatures)

        data, _ = self.assembleData(posPredictions, sentenceFeatures, golds)

        cutoff = int(len(data) * 7 / 10)

        trainFeatures = [datum[0] for datum in data[:cutoff]]
        trainLabels = [datum[1] for datum in data[:cutoff]]

        testFeatures = [datum[0] for datum in data[cutoff:]]
        testLabels = [datum[1] for datum in data[cutoff:]]

        clf = RandomForestClassifier(n_estimators = 500, n_jobs = 7)
        clf.fit(trainFeatures, trainLabels)
        trainingPred = clf.predict(trainFeatures)
        print(classification_report(trainLabels, trainingPred))
        print(classification_report(testLabels, clf.predict(testFeatures)))

        with open(modelSystem.nPosModelFile(self.sentenceLength), "wb") as f:
            pickle.dump(clf, f)

    def test(self, predictions, predictionsMade, golds):
        print("Total Sentences "+str(len(predictions)))
        toVerifyPrediction = []
        toVerifyLabel = []
        for predictionIndex in predictionsMade:
            toVerifyPrediction.append(predictions[predictionIndex])
            toVerifyLabel.append(golds[predictionIndex].getRemovedIndex())

        print("Predicted "+str(len(toVerifyPrediction)))

        print(classification_report(toVerifyPrediction, toVerifyLabel))

if __name__ == "__main__":
    sentenceLength = 16
    model = NPositionModel(sentenceLength)
    predictions, predictionsMade = model.predict(bridge.loadMergedFeatures(sentenceLength, start = 100000, end = 110000))
    model.test(predictions, predictionsMade, gold.loadGolds("C:/MissingWord/"+str(sentenceLength)+"Gold.txt", numGolds = 110000)[100000:110000])