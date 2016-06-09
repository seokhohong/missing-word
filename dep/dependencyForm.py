__author__ = 'SEOKHO'

from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report
from sklearn import cross_validation
import pickle

def readFeatures(filename):
    allFeatures = []
    with open(filename, "r") as f:
        for line in f:
            allFeatures.append([float(elem) for elem in line.split(",")])
    return allFeatures

def main():
    incorrectFeatures = readFeatures("C:/MissingWord/incorrectFeatures.txt")
    correctFeatures = readFeatures("C:/MissingWord/correctFeatures.txt")

    features = []
    features.extend(incorrectFeatures)
    features.extend(correctFeatures)

    labels = [0] * len(incorrectFeatures)
    labels.extend([1] * len(correctFeatures))

    trainFeatures, testFeatures, trainLabels, testLabels = cross_validation.train_test_split(features, labels, test_size = 0.3)

    clf = RandomForestClassifier(n_estimators=100)
    clf.fit(trainFeatures, trainLabels)

    print(classification_report(clf.predict(trainFeatures), trainLabels))
    print(classification_report(clf.predict(testFeatures), testLabels))

    with open("C:/MissingWord/dependencyForm.clf", "wb") as f:
        pickle.dump(clf, f)

def test():
    with open("C:/MissingWord/dependencyForm.clf", "rb") as f:
        clf = pickle.load(f)

    testFeatures = readFeatures("C:/MissingWord/testFeatures.txt")

    print(clf.predict(testFeatures))

if __name__ == "__main__":
    test()