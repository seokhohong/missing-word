__author__ = 'SEOKHO'


import pickle
from collections import Counter

chunkCounter = Counter()

with open("C:/MissingWord/chunksPart1.txt", "r") as f:
    for index, line in enumerate(f):
        line = line.strip()
        if index % 10000 == 0:
            print(index, len(chunkCounter))
        if len(line) > 1:
            for chunk in line.split("\\"):
                chunkCounter[chunk] += 1

with open("C:/MissingWord/chunkCounter.pickle", "wb") as f:
    pickle.dump(chunkCounter, f)