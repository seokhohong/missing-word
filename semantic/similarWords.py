__author__ = 'SEOKHO'

import gensim
from gensim.models import Word2Vec
from model import util

def loadWordsFromCounter(file):
    words = []
    with open(file, "r") as f:
        for line in f:
            if len(line.strip()) == 0 or line.startswith("\\"):
                continue
            split = line.split("\\")
            if int(split[1]) >= 1000:
                words.append(split[0])
    return words

model = Word2Vec.load_word2vec_format("C:/MissingWord/corpusVec.bin", binary=True)

words = loadWordsFromCounter("C:/MissingWord/frequentWords.txt")

with open("C:/MissingWord/mostRelated.txt", "w") as f:
    for index, word in enumerate(words):
        if(index % 1000) == 0:
            print(index)
        try:
            if word in model:
                print(word)
                f.write(word+"@"+"\\".join([str(elem) for elem in util.flatten(model.most_similar(positive=[word], topn = 100))])+"\n")
        except UnicodeEncodeError:
            pass