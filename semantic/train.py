__author__ = 'SEOKHO'

from gensim.models import Word2Vec
import makeFeatures
import random
import numpy as np
from sklearn.svm import SVC
from sklearn.ensemble import RandomForestClassifier
from sklearn.ensemble import AdaBoostClassifier
import sklearn
from sklearn.metrics import classification_report
import filelib

VECTOR_DIM = 100

def concatVectors(model, tokens):
    concat = []
    for token in tokens:
        if token in model:
            concat.extend(model[token])
        else:
            concat.extend([0] * VECTOR_DIM)
    return concat

def concatTokensAndTags(sentence, tagLine):
    tokens = sentence.split(" ")
    tags = tagLine.split("|")
    connected = []
    for i in range(len(tokens)):
        connected.append((tokens[i], tags[i]))
    return connected

def separate(connected):
    return [elem[0] for elem in connected], [elem[1] for elem in connected]



model = Word2Vec.load_word2vec_format("C:/MissingWord/corpusVec.bin", binary=True)

print(model.similarity('woman', 'womanly'))

print("Loaded vector space model")

data = []
numLine = 0
for lines in filelib.readSimul(["C:/MissingWord/train/cleanTokensPart2.txt", "C:/MissingWord/train/cleanTagsPart2.txt"]):
    numLine += 1
    if numLine % 10000 == 0:
        print(numLine)
    if numLine > 100000:
        break
    sentence = concatTokensAndTags(lines[0], lines[1])
    origWindows = makeFeatures.getNormalWindows(sentence, 1, ["", ""])
    for window in origWindows:
        tokens, tags = separate(window)
        if tags == ['MD', 'VB']:
            data.append((concatVectors(model, tokens), 0))
    modWindows = makeFeatures.getModWindows(sentence, 1, ["", ""])
    for window in modWindows:
        tokens, tags = separate(window)
        if tags == ['MD', 'VB']:
            data.append((concatVectors(model, tokens), 1))


#print([datum[0] for datum in data[:30]])

print(len(data), "data points")

random.shuffle(data)

cutoff = int(len(data) * 7 / 10)

trainFeatures = [datum[0] for datum in data[:cutoff]]
trainLabels = [datum[1] for datum in data[:cutoff]]

testFeatures = [datum[0] for datum in data[cutoff:]]
testLabels = [datum[1] for datum in data[cutoff:]]

trainFeatures = np.array(trainFeatures)
testFeatures = np.array(testFeatures)

clf = SVC(C=5, class_weight = 'auto')
#clf = RandomForestClassifier(n_estimators = 1000)
#clf = AdaBoostClassifier()
clf.fit(trainFeatures, trainLabels)
trainingPred = clf.predict(trainFeatures)
print(classification_report(trainLabels, trainingPred))
print(classification_report(testLabels, clf.predict(testFeatures)))