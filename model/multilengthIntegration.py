__author__ = 'SEOKHO'

from model.frameModel import FrameModel
from model.positionModel import PositionModelStructure
from model import util
from model import modelSystem
from model.nPosModel import NPositionModel
from model import bridge
from sklearn.metrics import classification_report
from model import gold
import numpy as np

class MultilengthIntegration:
    def __init__(self, lengthRange, numFeaturesPer):
        self.lengthRange = lengthRange
        self.maxLength = max(lengthRange)
        self.numFeaturesPer = numFeaturesPer

    def train(self, ratios, numData):

        print(len(rawFeatureBatches[2]), len(goldBatches[2]))
        frameModel = FrameModel(self.sentenceLength, ratios)
        frameModel.trainPredictiveModel(rawFeatureBatches[0], goldBatches[0])
        framePredictions1 = frameModel.predictNewData(rawFeatureBatches[1])

        posModel = PositionModelStructure(self.sentenceLength)
        posModel.trainPredictiveModel(rawFeatureBatches[1], goldBatches[1], framePredictions1)
        framePredictions2 = frameModel.predictNewData(rawFeatureBatches[2])
        posPredictions = posModel.predict(rawFeatureBatches[2], framePredictions2)

        nposModel = NPositionModel(self.sentenceLength)
        nposModel.trainPredictiveModel(rawFeatureBatches[2], goldBatches[2], posPredictions)

    def loadDataFor(self, sentenceLength, numData):
        rawFeatureBatches = util.cutInThree(bridge.loadMergedFeatures(sentenceLength, end = numData))
        for batch in rawFeatureBatches:
            self.addPadding(batch)
        goldBatches = util.cutInThree(gold.loadDefaultGolds(sentenceLength)[:numData])
        return rawFeatureBatches, goldBatches

    def addPadding(self, rawFeatures):
        for sentence in rawFeatures: #sentence is a list of frame features
            dim = len(sentence[0])
            for i in range(len(sentence), self.maxLength):
                sentence.append(np.zeros(dim))