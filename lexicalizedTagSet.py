__author__ = 'SEOKHO'

from collections import Counter
import pickle

#computes the full list of lexicalized tags currently in use

allTags = Counter()

with open("C:/MissingWord/lex5.pickle", "rb") as f:
    lex5 = pickle.load(f)

for tagSet in lex5:
    allTags[tagSet[0]] +=1

tags = []
for key in allTags:
    if allTags[key] > 10:
        tags.append(key)

tags.append("UNK")

print(len(tags))

with open("lexTags.pickle", "wb") as f:
    pickle.dump(tags, f)