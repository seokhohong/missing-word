
import scipy.sparse as sparse
import numpy as np
import os
import pickle

TOP_DIR = "data"
WINDOW = 2

def extractAttr(attr, line):
    if not attr in line:
        return None
    valueIndex = line.index(attr)
    if valueIndex != -1:
        valueIndex += len(attr)
        return line[valueIndex: line.index("\"", valueIndex)]
    return None

def extractValue(line):
    return extractAttr("value=\"", line)

def extractName(line):
    return extractAttr("name=\"", line)

def processFile(f):
    tokens = []
    with open(f, "r") as heppleFile:
        data = {}
        for line in heppleFile:
            name = extractName(line)
            if name:
                data[name] = extractValue(line)
            if "base" in data and "msd" in data and data["base"].isalpha():
                tokens.append(data["base"]+"_"+data["msd"])
                data = {}
    return tokens

def processTokens(matrix, tokens, wordIndices, wordFrequencies):
    for centerIndex in range(len(tokens)):
        if tokens[centerIndex] in wordIndices:
            centerToken = tokens[centerIndex]
            for i in range(- WINDOW, WINDOW + 1):
                if i == 0:
                    continue
                if centerIndex + i >= 0 and centerIndex + i < len(tokens):
                    if tokens[centerIndex + i] in wordIndices:
                        matrix[wordIndices[centerToken], wordIndices[tokens[centerIndex + i]]] += 1.0

def getElem(list, index, exception = " "):
    if index < 0 or index > len(list) - 1:
        return exception
    return list[index]

def lookup(dictionary, token):
    if token not in dictionary:
        return len(dictionary)
    return dictionary[token]

def read(filename, matrix, wordIndices, wordDescriptors):
    with open(filename, "r", encoding = "utf-8") as f:
        for line in f:
            tokens = line.split(" ")
            for index, token in enumerate(tokens):
                for subIndex in range(-WINDOW, WINDOW):
                    if subIndex != 0:
                        sideToken = getElem(tokens, index + subIndex)
                    if sideToken in wordDescriptors:
                        print(lookup(wordIndices, token), wordDescriptors[sideToken])
                        matrix[lookup(wordIndices, token), wordDescriptors[sideToken]] += 1

def loadWordData(filename):
    wordIndices = {}
    index = 0
    with open(filename, "r") as f:
        for line in f:
            wordIndices[line] = index
            index += 1
    return wordIndices

def main():
    #[Before, After]
    wordIndices = loadWordData("C:/MissingWord/100Words.txt")
    wordDescriptors = loadWordData("C:/MissingWord/10000Words.txt")
    print("Loaded Word Indices")
    matrix = sparse.dok_matrix((len(wordIndices) + 1, len(wordDescriptors) + 1), dtype=np.float32)

    for a in range(0, 5):
        read("C:/MissingWord/train/cleanTokensPart"+str(a)+".txt", matrix, wordIndices, wordDescriptors)

    with open("wordword2DokMatrix.pickle", "wb") as f:
        pickle.dump(matrix, f)

if __name__ == "__main__":
    main()
