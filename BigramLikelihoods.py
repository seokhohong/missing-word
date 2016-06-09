__author__ = 'SEOKHO'

import generateTagWindows
from collections import Counter
import pickle

counter = Counter()

for i in range(0, 4):
    with open("C:/MissingWord/train/cleanTokensPart"+str(i)+".txt", "r", encoding = 'utf-8') as f:
        for index, line in enumerate(f):
            if index % 100000 == 0:
                print(index, len(counter))
            counter.update(generateTagWindows.makeWindows(line.split(" "), size = 2))

with open("C:/MissingWord/bigramCounts.pickle", "wb") as f:
    pickle.dump(counter, f)
