__author__ = 'SEOKHO'

from collections import Counter
import generateTagWindows
import pickle
import random
import lexicalizedTagWindows
from compressWindows import WindowCompressor

def main():
    WIN_SIZE = 5
    compressor = WindowCompressor()
    with open("toLexicalize.pickle", "rb") as f:
        lexicalizedTags = pickle.load(f)

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
                tags = lexicalizedTagWindows.lexicalizeTags(tags, tokens, lexicalizedTags)
                if len(tags) >= 3:
                    tags.pop(random.randint(1, len(tags) - 2))
                windows = compressor.compressList(generateTagWindows.makeWindows(tags, size = WIN_SIZE))
                tagWindows.update(windows)
                if hasDisplayed == False:
                    print(windows)
                    hasDisplayed = True
            else:
                break
    with open("C:/MissingWord/modLexComp"+str(WIN_SIZE)+".pickle", "wb") as f:
        pickle.dump(tagWindows, f)

if __name__ == "__main__":
    main()