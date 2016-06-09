__author__ = 'SEOKHO'

from nltk.tokenize import SpaceTokenizer
from textblob_aptagger import PerceptronTagger
from textblob import TextBlob
import sys
import generateTagWindows

aptagger = PerceptronTagger()

def cleanTokens():
    for part in range(0, 4):
        hasDisplayed = False
        tagsFile = open("C:/MissingWord/train/tagsPart"+str(part)+".txt", "r", encoding = "utf-8")
        tokensFile = open("C:/MissingWord/train/corpusPart"+str(part)+".txt", "r", encoding = "utf-8")

        cleanTags = open("C:/MissingWord/train/cleanTagsPart"+str(part)+".txt", "w", encoding = "utf-8")
        cleanTokens = open("C:/MissingWord/train/cleanTokensPart"+str(part)+".txt", "w", encoding = "utf-8")

        numLines = 0
        while True:
            numLines += 1
            if numLines % 10000 == 0:
                print(numLines)
            tagLine = tagsFile.readline()
            tokenLine = tokensFile.readline()

            if len(tagLine) > 0:
                tags = tagLine.strip().split("|")
                tokens = tokenLine.strip().split(" ")

                blob = TextBlob(' '.join(tokens), pos_tagger = aptagger)
                #DOESN"T WORK
                tags = [tag[0] for tag in blob.tags]
                tokens = blob.tokens
                cleanTags.write("|".join(tags)+"\n")
                cleanTokens.write(" ".join(tokens)+"\n")
            else:
                break

        tagsFile.close()
        tokensFile.close()
        cleanTags.close()
        cleanTokens.close()

def cleanTags(whichPart):
    with open("C:/MissingWord/train/cleanTokensPart"+str(whichPart)+".txt", "r", encoding = "utf-8") as f:
        cleanTagsFile = open("C:/MissingWord/train/cleanTagsPart"+str(whichPart)+".txt", "w", encoding = "utf-8")
        numLines = 0
        for tokenLine in f:
            numLines += 1
            if numLines % 10000 == 0:
                print(numLines)
            if len(tokenLine)>0:
                blob = TextBlob(tokenLine.strip(), pos_tagger=aptagger)
                tags = generateTagWindows.getCompleteTags(blob)
                cleanTagsFile.write("|".join(tags)+"\n")
            else:
                break
    cleanTagsFile.close()

def cleanOrigTags(whichPart):
    with open("C:/MissingWord/train/corpusPart"+str(whichPart)+".txt", "r", encoding = "utf-8") as f:
        cleanTokensFile = open("C:/MissingWord/train/cleanTokensPart"+str(whichPart)+".txt", "w", encoding = "utf-8")
        cleanTagsFile = open("C:/MissingWord/train/cleanTagsPart"+str(whichPart)+".txt", "w", encoding = "utf-8")
        numLines = 0
        for tokenLine in f:
            numLines += 1
            if numLines % 10000 == 0:
                print(numLines)
            if len(tokenLine)>0:
                blob = TextBlob(tokenLine.strip(), pos_tagger=aptagger)
                tags = generateTagWindows.getCompleteTags(blob)
                cleanTagsFile.write("|".join(tags)+"\n")
                cleanTokensFile.write(" ".join(blob.tokens)+"\n")
            else:
                break
    cleanTagsFile.close()

if __name__ == "__main__":
    cleanOrigTags(sys.argv[1])