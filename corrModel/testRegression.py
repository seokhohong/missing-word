__author__ = 'SEOKHO'

from sklearn import svm
from sklearn.metrics import classification_report
from sklearn.ensemble import RandomForestRegressor
import numpy as np
from sklearn import linear_model
from sklearn.utils.validation import assert_all_finite
import pickle

def main(name, num, useSpecial = False):

    labels = []
    with open("C:/MissingWord/corrScoring/"+name+"Labels.txt", "r") as f:
        for line in f:
            labels.append(float(line))

    features = []
    with open("C:/MissingWord/corrScoring/1000features.txt", "r") as f:
        for line in f:
            features.append([float(elem) for elem in line.split(",")])

    specialFeatures = getSpecialFeatures(len(features))

    if useSpecial:
        for i in range(min(len(specialFeatures), len(features))):
            features[i].extend(specialFeatures[i])

    features = features[:num]
    labels = labels[:num]

    for i in range(len(features)):
        if len(features[i]) != len(features[0]):
            print(i)
        try:
            assert_all_finite(features[i])
        except:
            print(i)

    cutoff = int(len(features) * 7 / 10)

    trainFeatures = features[:cutoff]
    testFeatures = features[cutoff:]

    trainLabels = labels[:cutoff]
    testLabels = labels[cutoff:]

    #regr = svm.SVR(C=1)
    regr = RandomForestRegressor(n_estimators = 300, n_jobs = 7)
    #regr = linear_model.LinearRegression()

    regr.fit(trainFeatures, trainLabels)

    print("Train Residual sum of squares: %.2f"% np.mean((regr.predict(trainFeatures) - trainLabels) ** 2))
    print("Test Residual sum of squares: %.2f"% np.mean((regr.predict(testFeatures) - testLabels) ** 2))

    print('Variance score: %.2f' % regr.score(testFeatures, testLabels))

    with open("C:/MissingWord/corrScoring/"+name+".regr", "wb") as f:
        pickle.dump(regr, f)

def getSpecialFeatures(length):
    modelList = []
    with open("C:/MissingWord/corrScoring/modelsList.txt", "r") as f:
        for line in f:
            modelList.append(line.strip())

    specialFeatures = []
    for i in range(length):
        specialFeatures.append([])

    for model in modelList:
        with open("C:/MissingWord/corrScoring/"+model+"trainSpecialFeatures.txt", "r") as f:
            for index, line in enumerate(f):
                specialFeatures[index].extend([float(elem) for elem in line.split(" ")])

    return specialFeatures

if __name__ == "__main__":

    main("trigram0", 1000, True)
    main("trigram-1", 1000, True)
    main("trigram-2", 1000, True)
    main("trigram", 1000, True)
    main("bigram0Lower", 1000, True)
    main("bigram-1Lower", 1000, True)
    main("bigram0", 1000, True)
    main("bigram-1", 1000, True)

    main("det", 1000, True)
    main("cop", 1000, True)
    main("conj", 1000, True)
    main("prep", 1000, True)
    main("cc", 1000, True)
    main("dobj", 1000, True)
    main("prep", 1000, True)
    main("pobj", 1000, True)
    main("prep_", 1000, True)

    main("nsubj", 1000, True)
    main("nn", 1000, True)
    main("svo", 1000, True)
    #main("amod", 300, True)
    #main("num", 300, True)

    main("ntag51", 1000, True)
    main("ntag53", 1000, True)
    main("ntag54", 1000, True)
    main("bidir112", 1000, True)

    #main("globalbigram", 1000, True)
