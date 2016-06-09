__author__ = 'SEOKHO'

from collections import Counter
import pickle

allCounters = dict()

for part in range(0, 4):
    tagsFile = open("C:/MissingWord/train/tagsPart"+str(part)+".txt", "r", encoding = "utf-8")
    tokensFile = open("C:/MissingWord/train/corpusPart"+str(part)+".txt", "r", encoding = "utf-8")
    numLines = 0
    while True:
        numLines += 1
        if numLines % 10000 == 0:
            print(numLines)
        tagLine = tagsFile.readline()
        tokenLine = tokensFile.readline()
        if len(tagLine) == 0:
            break
        tags = tagLine.strip().split("|")
        tokens = tokenLine.strip().split(" ")
        if len(tags) != len(tokens):
            continue
        for index, tag in enumerate(tags):
            if tag not in allCounters:
                allCounters[tag] = Counter()
            allCounters[tag][tokens[index]] += 1

for tag in allCounters:
    print(tag, allCounters[tag].most_common()[:10])

with open("wordsByTag.pickle", "wb") as f:
    pickle.dump(allCounters, f)