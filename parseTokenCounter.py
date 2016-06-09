__author__ = 'SEOKHO'

from collections import Counter
import pickle

counter = Counter()
with open("C:/MissingWord/parseGramsPart3.txt", "r") as f:
    for index, line in enumerate(f):
        line = line.strip()
        grams = line.split(",")
        for i, gram in enumerate(grams):
            tags = gram.split("|")
            if '/' in tags[-1]:
                tags[-1] = tags[-1].split("/")[1]
            if len(tags) > 2:
                tags = tags[1:]
            grams[i] = '|'.join(tags)
        counter[tuple(grams)] += 1
        if index % 100000 == 0:
            print(index, len(counter))

with open("C:/MissingWord/parseCounter.pickle", "wb") as f:
    pickle.dump(counter, f)
