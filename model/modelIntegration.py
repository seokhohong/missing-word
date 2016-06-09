__author__ = 'SEOKHO'

from model.frameModel import FrameModel
from model.positionModel import PositionModelStructure
from model import util
from model import modelSystem
from model.nPosModel import NPositionModel
from model import bridge
from sklearn.metrics import classification_report
from model import gold
import pickle

class IntegratedModel:
    def __init__(self, sentenceLength):
        self.sentenceLength = sentenceLength

    def train(self, ratios, numData):
        rawFeatureBatches = util.cutInThree(bridge.loadMergedFeatures(self.sentenceLength, end = numData))
        goldBatches = util.cutInThree(gold.loadDefaultGolds(self.sentenceLength)[:numData])
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

    def predict(self, rawFeatures, ratios):

        frameModel = FrameModel(self.sentenceLength, ratios)
        framePredictions = frameModel.predictNewData(rawFeatures)

        posModel = PositionModelStructure(self.sentenceLength)
        posPredictions = posModel.predict(rawFeatures, framePredictions)

        nposModel = NPositionModel(self.sentenceLength)
        predictions = nposModel.predict(rawFeatures, posPredictions)

        return predictions

    #full predictions (including 0's) and labels
    def printResults(self, predictions, labels):
        reducedPredictions = []
        reducedLabels = []
        for i in range(len(predictions)):
            if predictions[i] != 0:
                reducedPredictions.append(predictions[i])
                reducedLabels.append(labels[i])

        print(classification_report(reducedPredictions, reducedLabels))

def test(length):
    rawFeatures = bridge.loadMergedFeatures(length, 300000, 400000)
    integratedModel = IntegratedModel(length)
    predictions = integratedModel.predict(rawFeatures, [(int) (length / 2), length])
    integratedModel.printResults(predictions, labels = gold.loadDefaultGoldLabels(length)[300000:400000])

    with open("C:/MissingWord/export.txt", "w") as f:
        golds = gold.loadDefaultGolds(length)[300000:400000]
        print(golds[0])
        for i in range(len(predictions)):
            f.write(str(predictions[i])+" "+str(golds[i].getRemovedIndex())+"/"+golds[i].getSentence()+"\n")

def train():
    for i in range(15, 16):
        integratedModel = IntegratedModel(i)
        integratedModel.train([(int) (i / 2), i], 300000)

if __name__ == "__main__":
    #train()
    test(15)