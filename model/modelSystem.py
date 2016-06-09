__author__ = 'SEOKHO'

import os.path
from model import util
import numpy as np

def frameFeaturesFile(sentenceLength):
    return "C:/MissingWord/merged/"+str(sentenceLength)+".txt"

def frameGoldsFile(sentenceLength):
    return "C:/MissingWord/"+str(sentenceLength)+"gold.txt"

def frameModelFile(sentenceLength, ratio, batchTrained):
    return "C:/MissingWord/frame/"+str(sentenceLength)+"rf"+str(ratio)+"-"+str(batchTrained)+".clf"

def frameModelOutput(sentenceLength, ratio, batch):
    return frameModelFile(sentenceLength, ratio, batch).replace(".clf", ".txt")

def frameModelCombinedOutput(sentenceLength, ratio):
    return "C:/MissingWord/frame/"+str(sentenceLength)+"-"+str(ratio)+"combined.txt"

def frameModelPredictor(sentenceLength, ratio):
    return "C:/MissingWord/frame/"+str(sentenceLength)+"rf"+str(ratio)+".clf"

def frameModelPredictionOutput(sentenceLength, ratio):
    return frameModelPredictor(sentenceLength, ratio).replace(".clf", ".txt")

def posModelIntermediateOutput(sentenceLength):
    return "C:/MissingWord/pos/"+str(sentenceLength)+"combinedPosition.dat"

def posModelPredictionFile(sentenceLength, i):
    return "C:/MissingWord/pos/"+str(sentenceLength)+"/"+str(i)+".clf"

def posModelPredictionOutput(sentenceLength):
    return "C:/MissingWord/pos/"+str(sentenceLength)+"prediction.dat"

def nPosModelFile(sentenceLength):
    return "C:/MissingWord/pos/"+str(sentenceLength)+"/npos.clf"

def goldsFile(sentenceLength):
    return "C:/MissingWord/gold/"+str(sentenceLength)+".txt"

def rawToSentenceFeatures(allFeatures):
    return np.array([util.flatten(featureSet) for featureSet in allFeatures])

def frameModelTrees():
    return 500