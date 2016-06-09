__author__ = 'SEOKHO'

from sklearn.utils.validation import assert_all_finite
import numpy as np
import math
from model import modelSystem
import os.path
import pickle

def getMergedFeatures(filename, numData = -1):
    allFeatures = []
    features = []
    with open(filename, "r") as f:
        for line in f:
            line = line.strip()
            if len(line) > 0:
                featureSet = np.array([float(elem.strip()) for elem in line.split(",")], dtype = np.float16)
                features.append(featureSet)
            else:
                allFeatures.append(features)
                features = []
            if len(allFeatures) == numData:
                break

    return np.array(allFeatures)

def loadMergedFeatures(sentenceLength, start = 0, end = -1):
    '''
    cacheFile = "C:/MissingWord/cache/"+str(sentenceLength)+"-"+str(start)+"-"+str(end)+".feats"
    if os.path.exists(cacheFile):
        with open(cacheFile, "rb") as f:
            return pickle.load(f)
    else:
    '''
    features = getMergedFeatures(modelSystem.frameFeaturesFile(sentenceLength), numData = end)[start:]
    #with open(cacheFile, "wb") as f:
    #    pickle.dump(features, f)
    return features

def getLabels(filename, allFeatures, length = -1):
    labels = []
    with open("C:/MissingWord/frameGold.txt", "r") as f:
        for index, line in enumerate(f):
            sentence = line.split("@")[0]
            if len(allFeatures[index]) != len(sentence.split(" ")):
                print("error")
            if length == -1 or len(sentence.split(" ")) == length:
                labelExtension = [0] * len(allFeatures[index])
                labelExtension[int(line.split("@")[1])] = 1
                labels.append(labelExtension)
    return labels

def parseSuperList(string):
    string = string[2:-2]
    return string.split("], [")

def parseArrayList(string):
    return string.split(", ")


def getLabels(filename):
    labels = []
    with open(filename, "r") as f:
        for line in f:
            chunks = parseSuperList(line.strip())
            labelSet = []
            for elem in chunks:
                labelSet.append(parseArrayList(elem)[0])
            labels.append(labelSet)
    return labels

def parseNestedList(filename):
    values = []
    with open(filename, "r") as f:
        for line in f:
            if len(line) > 0:
                line = line.strip()[1: -1]
                predictionSet = []
                for chunk in line.split("], ["):
                    chunk = chunk.replace("[", "")
                    chunk = chunk.replace("]", "")
                    elems = [float(e) for e in chunk.split(", ")]
                    predictionSet.append(elems)
                values.append(predictionSet)
    return values

def pickleNestedList(input, output):
    with open(output, "wb") as f:
        pickle.dump(parseNestedList(input), f)

def appendLabels(filename, sentences):
    labels = getLabels(filename)
    for i in range(len(sentences)):
        sentences[i].addAdditionalFeatures(labels[i])

#pickleNestedList(modelSystem.posModelIntermediateOutput(15).replace(".dat", ".txt"), modelSystem.posModelIntermediateOutput(15))