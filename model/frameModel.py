__author__ = 'SEOKHO'

import filelib
import random
from sklearn.svm import SVC
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report
import model.bridge as bridge
import model.gold as gold
from model import modelSystem
from model.util import chunks
from model import util
import numpy as np

import pickle

class FrameModel:
    def __init__(self, sentenceLength, ratios):
        self.sentenceLength = sentenceLength
        self.ratios = ratios

    def makeBatch(self, frameFeatures, goldLabels, ratio, output):
        data = []
        for i in range(len(frameFeatures)):
            data.append((frameFeatures[i], goldLabels[i]))

        onesData = [datum for datum in data if datum[1] == 1]

        #onesData = onesData[:30000]

        zerosData = [datum for datum in data if datum[1] == 0]

        random.shuffle(zerosData)

        zerosData = zerosData[:len(onesData) * ratio]

        data = onesData + zerosData

        random.shuffle(data)

        cutoff = int(len(data) * 7 / 10)

        trainFeatures = [datum[0] for datum in data[:cutoff]]
        trainLabels = [datum[1] for datum in data[:cutoff]]

        testFeatures = [datum[0] for datum in data[cutoff:]]
        testLabels = [datum[1] for datum in data[cutoff:]]

        print("Training Frame Model")
        clf = RandomForestClassifier(n_estimators = modelSystem.frameModelTrees(), n_jobs = 7)
        clf.fit(trainFeatures, trainLabels)
        trainingPred = clf.predict(trainFeatures)
        print(classification_report(trainLabels, trainingPred))
        print(classification_report(testLabels, clf.predict(testFeatures)))

        with open(output, "wb") as f:
            pickle.dump(clf, f)

    def predict(self, clfName, rawFeatures):
        frameFeatures = makeFrameFeatures(rawFeatures)
        with open(clfName, "rb") as f:
            clf = pickle.load(f)

        flatPredictions = np.array([tup[0] for tup in clf.predict_proba(frameFeatures)]) #so we get likelihood of 0

        chunkedPredictions = []
        for prediction in chunks(flatPredictions, self.sentenceLength):
            chunkedPredictions.append(prediction)

        return chunkedPredictions
    '''
    def trainIntermediateModels(self, rawFeatures):
        frameFeatures = util.flatten(rawFeatures)
        golds = gold.toFrameGolds(gold.loadDefaultGolds(self.sentenceLength), self.sentenceLength)

        print(len(frameFeatures), len(golds))

        cut = int(len(frameFeatures) / 2)
        batchFeatures = [frameFeatures[:cut], frameFeatures[cut:]]

        for ratio in self.ratios:
            self.makeBatch(batchFeatures[0], golds[:cut], ratio, modelSystem.frameModelFile(self.sentenceLength, ratio, 0))
            self.makeBatch(batchFeatures[1], golds[cut:], ratio, modelSystem.frameModelFile(self.sentenceLength, ratio, 1))

        intermediateBatchData = []
        for ratio in self.ratios:
            forRatio = {}
            for batch in range(2):
                predictionOnBatch = abs(1 - batch)
                forRatio[predictionOnBatch] = self.predict(modelSystem.frameModelFile(self.sentenceLength, ratio, batch), batchFeatures[predictionOnBatch])
            combined = []
            for batch in range(2):
                combined.extend(forRatio[batch])
            intermediateBatchData.append(combined)

        for ratio in range(len(self.ratios)):
            print(len(intermediateBatchData[ratio]))
        return intermediateBatchData
    '''
    def trainPredictiveModel(self, rawFeatures, golds):
        goldFrameLabels = gold.toFrameGolds(golds, self.sentenceLength)
        frameFeatures = makeFrameFeatures(rawFeatures)
        #frameFeatures = util.flatten(rawFeatures)

        for ratio in self.ratios:
            self.makeBatch(frameFeatures, goldFrameLabels, ratio, modelSystem.frameModelPredictor(self.sentenceLength, ratio))

    #per sentence, this method returns a ndarray [sentenceLength * len(ratio)]
    def predictNewData(self, rawFeatures):
        #frameFeatures = util.flatten(rawFeatures)
        ratioCombinedPredictions = None
        for ratio in self.ratios:
            chunkedPredictions = self.predict(modelSystem.frameModelPredictor(self.sentenceLength, ratio), rawFeatures)
            if ratioCombinedPredictions is None:
                ratioCombinedPredictions = chunkedPredictions
            else:
                for index, predictionSet in enumerate(ratioCombinedPredictions):
                    ratioCombinedPredictions[index] = np.concatenate((predictionSet, chunkedPredictions[index]), axis = 0)
        return ratioCombinedPredictions

    def outputResults(self, ratio, chunkedPredictions):
        with open(modelSystem.frameModelPredictionOutput(self.sentenceLength, ratio), "wb") as f:
            pickle.dump(chunkedPredictions, f)

    def examineFeatures(self):
        with open(modelSystem.frameModelPredictor(self.sentenceLength, 5), "rb") as f:
            clf = pickle.load(f)
        print(clf.feature_importances_)

def makeFrameFeatures(rawFeatures):
    allFrameFeatures = []
    for sentenceSet in rawFeatures:
        dim = len(sentenceSet[0])
        paddedFlattened = [[0] * dim] + list(sentenceSet) + [[0] * dim]
        for i in range(1, len(paddedFlattened) - 1):
            allFrameFeatures.append(np.concatenate([paddedFlattened[i - 1], paddedFlattened[i], paddedFlattened[i + 1]]))
    return allFrameFeatures

if __name__ == "__main__":
    frameModel = FrameModel(15, [5, 10])
    #frameModel.trainIntermediateModels()
    frameModel.examineFeatures()

    '''
    frameModel.trainPredictiveModel(100000)
    allFeatures = bridge.loadMergedFeatures(15, start = 100000, end = 110000)
    frameFeatures = util.flatten(allFeatures)
    frameModel.predictNewData(frameFeatures)
        '''