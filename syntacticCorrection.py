__author__ = 'SEOKHO'

from WindowProb import WindowProb
from textblob import TextBlob
from textblob_aptagger import PerceptronTagger
import generateTagWindows
import PosLikelihood
import lexicalizedTagWindows
import pickle
import random
import numpy as np
from sklearn.svm import SVC
from sklearn.ensemble import RandomForestClassifier
import sklearn
from sklearn.metrics import classification_report
import math
from PosLikelihood import SynReplacer

#syntactic correctional model

class SynCorrection:
    def __init__(self, winSize, lex = False):
        self.winSize = winSize
        self.lex = lex
        self.lexFilename = "Lex" if lex else ""
        self.winMod = WindowProb("C:/MissingWord/post"+self.lexFilename+"ModComp"+str(self.winSize)+".pickle", compressed = True)
        self.winOrig = WindowProb("C:/MissingWord/post"+self.lexFilename+"Comp"+str(self.winSize)+".pickle", compressed = True)

        with open("toLexicalize.pickle", "rb") as f:
            self.toLexicalize = pickle.load(f)

        self.aptagger = PerceptronTagger()

    def correct(self, tokens):
        blob = TextBlob(' '.join(tokens), pos_tagger = self.aptagger)
        completeTags = generateTagWindows.getCompleteTags(blob)
        if self.lex == True:
            completeTags = lexicalizedTagWindows.lexicalizeTags(completeTags, tokens, self.toLexicalize)
        #print(lexicalizedTags)
        windows = generateTagWindows.makeWindows(completeTags, size = self.winSize)
        probs = []
        confidence = []
        for window in windows:
            prob = (self.winMod.count(window) + 1) / ((self.winMod.count(window) + self.winOrig.count(window)) + 1)
            probs.append(prob)
            confidence.append(math.log(self.winMod.count(window) + self.winOrig.count(window) + 1))
            #if self.winSize == 5:
            #    print(self.winMod.count(window) + self.winOrig.count(window))
        return probs, confidence

def makeFeatures(synCor, synCorLex, synRepl, cutTokens, removedIndex):
    corrProbs, corrConfidence = synCor.correct(cutTokens)
    probWindows = generateTagWindows.makeWindows(corrProbs, size = 9, filler = 0.0)
    #corrConfWindows = generateTagWindows.makeWindows(corrConfidence, size = 3, filler = 0.0)
    window = list(probWindows[removedIndex])
    window.append(max(probWindows[removedIndex]) - max(corrProbs)) # difference between top probability in window and top probability in all possible locations
    #window.extend(corrConfWindows[removedIndex])
    lexCorrProbs, lexCorrConfidence = synCorLex.correct(cutTokens)
    lexCorrProbWindows = generateTagWindows.makeWindows(lexCorrProbs, size = 9, filler = 0.0)
    #lexCorrConfWindows = generateTagWindows.makeWindows(lexCorrConfidence, size = 3, filler = 0.0)
    window.extend(lexCorrProbWindows[removedIndex])
    #window.extend(lexCorrConfWindows[removedIndex])
    window.append(max(lexCorrProbWindows[removedIndex]) - max(lexCorrProbs))
    window.append(math.log(len(cutTokens)))
    replProbs = synRepl.fix(cutTokens, removedIndex)
    window.extend(replProbs[:5])
    return window

def testSentence():
    sentence = "Japan has suspended of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese ."
    synCor = SynCorrection(4, lex = True)
    print(synCor.correct(sentence.split(" ")))

def main():
    synCor = SynCorrection(5)
    synCorLex = SynCorrection(4, lex = True)
    synRep = SynReplacer(lex = True)
    posWindows = []
    negWindows = []
    with open("C:/MissingWord/train/corpusPart2.txt", "r") as f:
        for index, line in enumerate(f):
            line = line.strip()
            if len(line) > 1:
                tokens = line.split(" ")
                if len(tokens) > 3:
                    removed = random.randint(1, len(tokens) - 2)
                    cutTokens = tokens.copy()
                    del cutTokens[removed]
                    posWindows.append(makeFeatures(synCor, synCorLex, synRep, cutTokens, removed))
                    for i in range(3):
                        negWindowIndex = random.randint(1, len(tokens) - 2)
                        if abs(negWindowIndex - removed) > 0:
                            negWindows.append(makeFeatures(synCor, synCorLex, synRep, cutTokens, negWindowIndex))
            if index > 10000:
                break

    data = []

    for window in posWindows:
        data.append((window, 1))

    for window in negWindows:
        data.append((window, 0))

    random.shuffle(data)

    cutoff = int(len(data) * 7 / 10)

    trainFeatures = [datum[0] for datum in data[:cutoff]]
    trainLabels = [datum[1] for datum in data[:cutoff]]

    testFeatures = [datum[0] for datum in data[cutoff:]]
    testLabels = [datum[1] for datum in data[cutoff:]]

    for i in range(10):
        print(trainFeatures[i])

    trainFeatures = np.array(trainFeatures)
    testFeatures = np.array(testFeatures)

    clf = SVC(C=1)
    #clf = RandomForestClassifier(n_estimators = 1000)
    clf.fit(trainFeatures, trainLabels)
    trainingPred = clf.predict(trainFeatures)
    print(classification_report(trainLabels, trainingPred))
    print(classification_report(testLabels, clf.predict(testFeatures)))

    with open("synCorrTight.clf", "wb") as f:
        pickle.dump(clf, f)

if __name__ == "__main__":
    testSentence()