__author__ = 'SEOKHO'

from collections import Counter
import generateTagWindows
import pickle
import random
import lexicalizedTagWindows
from compressWindows import WindowCompressor

#models P(ABCXE | ABCE)

def main():
    WIN_SIZE = 4
    compressor = WindowCompressor()
    with open("toLexicalize.pickle", "rb") as f:
        lexicalizedTags = pickle.load(f)

    replWindows = Counter()
    for part in range(0, 4):
        hasDisplayed = False
        tagsFile = open("C:/MissingWord/train/tagsPart"+str(part)+".txt", "r", encoding = "utf-8")
        tokensFile = open("C:/MissingWord/train/corpusPart"+str(part)+".txt", "r", encoding = "utf-8")
        numLines = 0
        while True:
            numLines += 1
            if numLines % 10000 == 0:
                print(numLines, len(replWindows))
            tagLine = tagsFile.readline()
            tokenLine = tokensFile.readline()

            if len(tagLine) > 0:
                tags = tagLine.strip().split("|")
                if len(tags) >= 3:
                    tokens = tokenLine.strip().split(" ")
                    if len(tags) != len(tokens):
                        continue
                    tags = lexicalizedTagWindows.lexicalizeTags(tags, tokens, lexicalizedTags)
                    for poppedTagIndex in random.sample(range(1, len(tags) - 2), min(len(range(1, len(tags) - 2)), 5)):
                        cutTags = tags.copy()
                        poppedTag = cutTags.pop(poppedTagIndex)
                        modWindow = list(generateTagWindows.makeWindow(cutTags, begin = poppedTagIndex - 2, end = poppedTagIndex + 2, size = WIN_SIZE))
                        if len(tags) >= 3:
                            #window = (WIN_SIZE tags..., poppedTag)
                            modWindow.append(poppedTag)
                            modWindow = tuple(modWindow)
                            replWindows[compressor.compress(modWindow)] += 1
                        if hasDisplayed == False:
                            print(modWindow)
                            hasDisplayed = True
            else:
                break

    with open("C:/MissingWord/postReplLexMod"+str(WIN_SIZE)+".pickle", "wb") as f:
        pickle.dump(replWindows, f)

if __name__ == "__main__":
    main()