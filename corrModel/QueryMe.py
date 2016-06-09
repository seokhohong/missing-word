__author__ = 'SEOKHO'

import pickle

def main():
    features = []
    with open("C:/MissingWord/corrScoring/queryFeatures.txt", "r") as f:
        for line in f:
            features.append([float(elem) for elem in line.split(",")])

    dir = 'C:/MissingWord/corrScoring/'

    modelList = []
    with open("C:/MissingWord/corrScoring/modelsList.txt", "r") as f:
        for line in f:
            modelList.append(line.strip())

    allPredictions = []

    specialFeatures = getSpecialFeatures(len(features))

    for regrName in modelList:
        featureSet = [f.copy() for f in features]
        for i in range(min(len(specialFeatures), len(features))):
            featureSet[i].extend(specialFeatures[i])

        with open(dir+regrName+".regr", "rb") as f:
            regr = pickle.load(f)
            print(regrName+" "+str(len(featureSet)))
            predictions = regr.predict(featureSet)
            allPredictions.append(predictions)

    with open(dir+"labels.txt", "w") as f:
        for prediction in allPredictions:
            f.write(' '.join([str(num) for num in prediction]))
            f.write("\n")

def getSpecialFeatures(length):
    modelList = []
    with open("C:/MissingWord/corrScoring/modelsList.txt", "r") as f:
        for line in f:
            modelList.append(line.strip())

    specialFeatures = []
    for i in range(length):
        specialFeatures.append([])

    for model in modelList:
        with open("C:/MissingWord/corrScoring/"+model+"QuerySpecialFeatures.txt", "r") as f:
            for index, line in enumerate(f):
                if index < len(specialFeatures):
                    specialFeatures[index].extend([float(elem) for elem in line.split(" ")])

    return specialFeatures

if __name__ == "__main__":
    main()