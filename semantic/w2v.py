__author__ = 'SEOKHO'

from gensim.models import Word2Vec
import gensim

import os

class MySentences(object):

    def __iter__(self):
        numLine = 0
        for i in range(5):
            with open("D:/MissingWord/train/corpusPart"+str(i)+".txt", "r", encoding = 'utf-8') as f:
                for line in f:
                    numLine += 1
                    if numLine % 10000 == 0:
                        print(numLine)
                    yield line.split(" ")

sentences = MySentences()
model = Word2Vec(sentences, workers = 8, min_count = 100)
model.save_word2vec_format("C:/MissingWord/corpusVec.bin", binary = True)

print(model['train'])