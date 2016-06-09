__author__ = 'SEOKHO'

from collections import Counter
import generateTagWindows
import pickle
import random
import lexicalizedTagWindows
from compressWindows import WindowCompressor

#models P(ABC?E | ABCE)

def main():
    WIN_SIZE = 4
    LEX = True
    lexFilename = ''
    if LEX:
        lexFilename = 'lex'
    NUM_SAMPLES = 5 #will bias the confidence, shifting it up as NUM_SAMPLES goes up
    compressor = WindowCompressor()
    with open("toLexicalize.pickle", "rb") as f:
        lexicalizedTags = pickle.load(f)

    tagModWindows = Counter()
    tagWindows = Counter()
    for part in range(0, 4):
        hasDisplayed = False
        tagsFile = open("C:/MissingWord/train/tagsPart"+str(part)+".txt", "r", encoding = "utf-8")
        tokensFile = open("C:/MissingWord/train/corpusPart"+str(part)+".txt", "r", encoding = "utf-8")
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
                if len(tags) != len(tokens) or len(tags) < 3:
                    continue
                if LEX:
                    origTags = lexicalizedTagWindows.lexicalizeTags(tags, tokens, lexicalizedTags)
                else:
                    origTags = tags
                for poppedTagIndex in random.sample(range(1, len(tags) - 2), min(len(range(1, len(tags) - 2)), NUM_SAMPLES)):
                    tags = origTags.copy()
                    tags.pop(poppedTagIndex)
                    affectedRange = range(poppedTagIndex - 2, poppedTagIndex + 3)
                    windows = compressor.compressList(generateTagWindows.makeWindows(tags, size = WIN_SIZE))
                    tagModWindows.update([window for index, window in enumerate(windows) if index in affectedRange])
                    tagWindows.update([window for index, window in enumerate(windows) if index not in affectedRange])
                    if hasDisplayed == False:
                        print(windows)
                        hasDisplayed = True
            else:
                break

    if LEX:
        lexFilename = "Lex"

    with open("C:/MissingWord/post"+lexFilename+"Comp"+str(WIN_SIZE)+".pickle", "wb") as f:
        pickle.dump(tagWindows, f)

    with open("C:/MissingWord/post"+lexFilename+"ModComp"+str(WIN_SIZE)+".pickle", "wb") as f:
        pickle.dump(tagModWindows, f)

if __name__ == "__main__":
    main()