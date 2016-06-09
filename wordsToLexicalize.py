__author__ = 'SEOKHO'

#lexicalize a word if it maintains more than 5% probability mass for a given pos tag

import pickle

toLexicalize = dict()

with open("wordsByTag.pickle", "rb") as f:
    #allCounters[tag][word] = count of word under given tag
    allCounters = pickle.load(f)

allLexWords = []

for tag in allCounters:
    toLexicalize[tag] = set()
    for word, count in allCounters[tag].most_common()[:min(100, len(allCounters[tag].values()))]:
        totalCount = sum(allCounters[tag].values())
        ratio = count / totalCount
        if ratio > 0.1 and ratio < 1.0 and count > 1000: #importance
            toLexicalize[tag].add(word)
            allLexWords.append(word)
            print(word)

#print(allLexWords)

with open("toLexicalize.pickle", "wb") as f:
    pickle.dump(toLexicalize, f)