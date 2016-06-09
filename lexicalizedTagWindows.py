__author__ = 'SEOKHO'


import os.path
from collections import Counter
import pickle
from textblob_aptagger import PerceptronTagger
from textblob import TextBlob
import generateTagWindows
from compressWindows import WindowCompressor

aptagger = PerceptronTagger()

def lexicalizeTags(tags, tokens, toLexicalize):
    tagIndex = 0
    allTags = []
    for tokenIndex, token in enumerate(tokens):
        if tagIndex < len(tags) and token != tags[tagIndex]:
            tag = tags[tagIndex]
            if tag in toLexicalize and token in toLexicalize[tag]: #OR token.lower() in toLexicalize[tag]
                tag = tag+"_"+token
            allTags.append(tag)
        else:
            allTags.append(token)
        tagIndex += 1
    return allTags

def main():
    WIN_SIZE = 6
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
                tags = lexicalizeTags(tags, tokens, lexicalizedTags)
                windows = compressor.compressList(generateTagWindows.makeWindows(tags, size = WIN_SIZE))
                tagWindows.update(windows)
                if hasDisplayed == False:
                    print(windows)
                    hasDisplayed = True
            else:
                break

    with open("lexComp"+str(WIN_SIZE)+".pickle", "wb") as f:
        pickle.dump(tagWindows, f)

if __name__ == "__main__":
    main()