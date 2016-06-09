__author__ = 'SEOKHO'

from model import modelSystem

class Gold:
    def __init__(self, sentence, removedIndex, removed):
        self.sentence = sentence
        self.removedIndex = removedIndex
        self.removed = removed

    def getSentence(self):
        return self.sentence

    def length(self):
        return len(self.sentence.split(" "))

    def getLabels(self):
        labels = [0] * self.length()
        labels[self.removedIndex] = 1
        return labels

    def getRemovedIndex(self):
        return self.removedIndex

    def __str__(self):
        return self.sentence

def loadDefaultGolds(sentenceLength):
    return loadGolds(modelSystem.goldsFile(sentenceLength))

def loadGolds(file, numGolds = -1, length = -1):
    golds = []
    with open(file, "r") as f:
        for line in f:
            if len(line) > 0:
                split = line.split("@")
                gold = Gold(split[0], int(split[1]), split[2])
                if length == -1 or gold.length() == length:
                    golds.append(gold)
            if len(golds) == numGolds:
                break
    return golds

def loadFrameGolds(file):
    golds = []
    with open(file, "r") as f:
        for line in f:
            if len(line) > 0:
                split = line.split("@")
                gold = Gold(split[0], int(split[1]), split[2])
                golds.extend(gold.getLabels())
    return golds

def loadDefaultGoldLabels(sentenceLength, numGolds = -1):
    golds = loadDefaultGolds(sentenceLength)
    if numGolds != -1:
        golds = golds[:numGolds]
    return [gold.getRemovedIndex() for gold in golds]

def toFrameGolds(golds, sentenceLength):
    frameGolds = []
    for gold in golds:
        expanded = [0] * sentenceLength
        expanded[gold.getRemovedIndex()] = 1
        frameGolds.extend(expanded)
    return frameGolds

if __name__ == "__main__":
    print(len(loadGolds("C:/MissingWord/15Gold.txt")))