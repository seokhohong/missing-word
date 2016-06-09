__author__ = 'SEOKHO'

from collections import Counter
import pickle
from textblob_aptagger import PerceptronTagger
from textblob import TextBlob
import generateTagWindows

aptagger = PerceptronTagger()

pos = Counter()

with open("C:/MissingWord/train/corpus.txt", "r", encoding='utf8') as f:
    for i, line in enumerate(f):
        blob = TextBlob(line, pos_tagger = aptagger)
        pos.update(generateTagWindows.getCompleteTags(blob))
        if i > 10000:
            break
        if i % 100 == 0:
            print(i, len(pos))

with open("posFrequencies.pickle", "wb") as f:
    pickle.dump(pos, f)