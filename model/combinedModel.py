__author__ = 'SEOKHO'

from model.gold import loadGolds
from sklearn.metrics import classification_report
import model.bridge as bridge
import model.gold as gold

sentenceLength = 15

def parseSuperList(string):
    string = string[2:-2]
    return string.split("], [")

def parseArrayList(string):
    return string.split(", ")

frameLabels = []
with open("C:/MissingWord/frame/"+str(sentenceLength)+"-"+str(10)+"combined.txt", "r") as f:
    for line in f:
        chunks = parseSuperList(line.strip())
        labelSet = []
        for elem in chunks:
            labelSet.append(round(float(parseArrayList(elem)[1])))
        frameLabels.append(labelSet)

condensedFrameLabels = []
for label in frameLabels:
    if 1 in label:
        condensedFrameLabels.append(label.index(1))
    else:
        condensedFrameLabels.append(0)


golds = gold.loadGolds("C:/MissingWord/"+str(sentenceLength)+"gold.txt", numGolds = 100000)
goldLabels = [gold.getRemovedIndex() for gold in golds]

allPredictions = []
with open("C:/MissingWord/frame/15-10Combined.txt", "r") as f:
    for line in f:
        if len(line) > 0:
            line = line.replace("array(", "")
            line = line.replace(")", "")
            line = line.strip()[1: -1]
            predictionSet = []
            for elem in line.split("], ["):
                elem = elem.replace("]", "")
                elem = elem.replace("[", "")
                elems = [float(e) for e in elem.split(", ")]
                if len(elems) == 1 or elems[0] > 0.5:
                    predictionSet.append(0)
                else:
                    predictionSet.append(1)
            allPredictions.append(predictionSet)


#golds = loadGolds("C:/MissingWord/"+str(sentenceLength)+"Gold.txt", numGolds = 100000, length = sentenceLength)

count2s = 0
prediction = []
for predictionSet in allPredictions:
    if predictionSet.count(1) == 1:
        prediction.append(predictionSet.index(1))
    elif predictionSet.count(1) == 2:
        prediction.append(0)
        count2s += 1
    else:
        prediction.append(0)

print(count2s)

print(prediction)
print(condensedFrameLabels)
print(goldLabels)

filteredGold = []
filteredPrediction = []
prediction = condensedFrameLabels
whichis = []
for i in range(len(goldLabels)):
    if prediction[i] != 0:
        filteredGold.append(goldLabels[i])
        filteredPrediction.append(prediction[i])
        whichis.append(i)

print(classification_report(filteredGold, filteredPrediction))

with open("C:/MissingWord/worthPredicting.txt", "w") as f:
    for i in whichis:
        f.write(str(i)+"\n")