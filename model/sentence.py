__author__ = 'SEOKHO'

import numpy as np

class Sentence:
    def __init__(self, gold, features):
        self.gold = gold
        self.allFeatures = features
    def addAdditionalFeatures(self, labels):
        self.allFeatures = np.concatenate((self.allFeatures, np.array(labels, dtype = np.float16)), axis = 0)
    def __str__(self):
        return str(self.gold.sentence())
    def __len__(self):
        return len(self.features)
    def clearFeatures(self):
        self.allFeatures = []
    def getAllFeatures(self):
        return self.allFeatures
    def getGold(self):
        return self.gold
    def getRemovedIndex(self):
        if self.gold is not None:
            return self.gold.getRemovedIndex()
        return 0
    def noGuess(self):
        return not 1 in self.labels