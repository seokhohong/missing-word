__author__ = 'SEOKHO'

from collections import Counter
import pickle
from textblob_aptagger import PerceptronTagger
from textblob import TextBlob
import generateTagWindows

aptagger = PerceptronTagger()

pos = Counter()

outputFiles = []

for i in range(5):
    outputFiles.append(open("C:/MissingWord/train/corpusPart"+str(i)+".txt", "w", encoding='utf8'))

with open("C:/MissingWord/train/corpus.txt", "r", encoding='utf8') as f:
    for i, line in enumerate(f):
        outputFiles[i % 5].write(line+"\n")
        if i % 1000000 == 0:
            print(i)

for file in outputFiles:
    file.close()