__author__ = 'SEOKHO'

import os.path
import pickle
from model.bridge import getMergedFeatures
from model.bridge import appendLabels
from model.gold import loadGolds

class DataSet:
    def __init__(self, sentenceLength, numData):
        self.sentenceLength = sentenceLength
        self.numData = numData
        filename = "C:/MissingWord/data/"+str(sentenceLength)+"-"+str(numData)+".dat"
        if os.path.exists(filename):
            with open(filename, "rb") as f:
                self.data = pickle.load(f)

        else:
            self.makeData()

    def makeData(self):
        allFeatures = getMergedFeatures("C:/MissingWord/mergedFeatures"+str(self.sentenceLength)+".txt", self.numData)
        golds = loadGolds("C:/MissingWord/"+str(self.sentenceLength)+"Gold.txt", numGolds = len(allFeatures), length = self.sentenceLength)
