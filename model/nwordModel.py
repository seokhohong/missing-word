__author__ = 'SEOKHO'

from model.bridge import getMergedFeatures
from model.bridge import appendLabels
import model.gold
from model.sentence import Sentence
from sklearn.svm import SVC
from sklearn.ensemble import RandomForestClassifier
from sklearn.ensemble import AdaBoostClassifier
import sklearn
from sklearn.metrics import classification_report
import random
import pickle

sentenceLength = 15

def getFeaturesOfLength(allFeatures, length):
    ofLength = []
    for feature in allFeatures:
        if len(feature) == length:
            thisFeatureSet = []
            for elem in feature:
                thisFeatureSet.extend(elem)
            ofLength.append(thisFeatureSet)
    return ofLength

allFeatures = getMergedFeatures("C:/MissingWord/mergedFeatures15.txt")

golds = model.gold.loadGolds("C:/MissingWord/15Gold.txt", sentenceLength)

golds = golds[:len(allFeatures)]

print(len(allFeatures))

sentences = []
for i in range(len(allFeatures)):
    sentences.append(Sentence(golds[i], allFeatures[i]))

appendLabels("15rf15.txt", sentences)
appendLabels("15rf110.txt", sentences)

sentences = [sentence for sentence in sentences if len(sentence) == sentenceLength]

data = [(sentence.getAllFeatures(), sentence.getGold().getRemovedIndex()) for sentence in sentences]

random.shuffle(data)

cutoff = int(len(data) * 7 / 10)

trainFeatures = [datum[0] for datum in data[:cutoff]]
trainLabels = [datum[1] for datum in data[:cutoff]]

testFeatures = [datum[0] for datum in data[cutoff:]]
testLabels = [datum[1] for datum in data[cutoff:]]

# clf = SVC(C=1, class_weight = 'auto')
clf = RandomForestClassifier(n_estimators=1000, n_jobs = 5)
#clf = AdaBoostClassifier()
clf.fit(trainFeatures, trainLabels)
trainingPred = clf.predict(trainFeatures)
print(classification_report(trainLabels, trainingPred))
print(classification_report(testLabels, clf.predict(testFeatures)))

with open("C:/MissingWord/clf/"+str(sentenceLength)+".clf") as f:
    pickle.dump(clf, f)