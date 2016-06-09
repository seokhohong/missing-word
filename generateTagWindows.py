__author__ = 'SEOKHO'


import os.path
from collections import Counter
import pickle
from textblob_aptagger import PerceptronTagger
from textblob import TextBlob
from compressWindows import WindowCompressor

aptagger = PerceptronTagger()

def makeWindow(values, index = 0, size = 0, begin = 0, end = 0, filler = ''):
    window = []
    if begin == 0 and end == 0:
        iterRange = range(index + int(-(size - 1) / 2), index + int((size - 1) / 2) + 1)
    else:
        iterRange = range(begin, end)
    for sub in iterRange:
        if sub < 0 or sub >= len(values):  # out of bounds
            window.append(filler)
        else:
            window.append(values[sub])
    return tuple(window)

def makeWindows(values, size = 7, filler = ''):
    windows = []
    for index in range(len(values) + int(size / 2) - 1):
        if size % 2 == 1:
            windows.append(makeWindow(values, index, size, filler = filler))
        else:
            windows.append(makeWindow(values, index, begin = index - int(size / 2), end = index + int(size / 2), filler = filler))
    return windows

def getCompleteTags(blob):
    tagIndex = 0
    allTags = []
    tags = blob.tags
    for tokenIndex, token in enumerate(blob.tokens):
        if tagIndex < len(tags) and token == tags[tagIndex][0]:
            allTags.append(tags[tagIndex][1])
            tagIndex += 1
        else:
            allTags.append(token)
    return allTags

def main():
    WIN_SIZE = 5
    compressor = WindowCompressor()
    with open("toLexicalize.pickle", "rb") as f:
        lexicalizedTags = pickle.load(f)

    tagWindows = Counter()
    for part in range(0, 4):
        hasDisplayed = False
        tagsFile = open("C:/MissingWord/train/cleanTagsPart"+str(part)+".txt", "r", encoding = "utf-8")
        tokensFile = open("C:/MissingWord/train/cleanTokensPart"+str(part)+".txt", "r", encoding = "utf-8")
        numLines = 0
        while True:
            numLines += 1
            if numLines % 10000 == 0:
                print(numLines, len(tagWindows))
            tagLine = tagsFile.readline()
            tokenLine = tokensFile.readline()

            if len(tagLine) > 0:
                tags = tagLine.strip().split("|")
                tokens = tokenLine.strip().split(" ")
                #tags = lexicalizeTags(tags, tokens, lexicalizedTags)
                windows = compressor.compressList(makeWindows(tags, size = WIN_SIZE))
                tagWindows.update(windows)
                if hasDisplayed == False:
                    print(windows)
                    hasDisplayed = True
            else:
                break

    with open("C:/MissingWord/comp"+str(WIN_SIZE)+".pickle", "wb") as f:
        pickle.dump(tagWindows, f)

if __name__ == "__main__":
    main()