__author__ = 'SEOKHO'

from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report
from sklearn import cross_validation
from sklearn import svm

def main():
    features = []
    with open("C:/MissingWord/improvementFeatures.txt", "r") as f:
        for line in f:
            features.append([float(elem) for elem in line.split(", ")])

    labels = []
    with open("C:/MissingWord/improvementLabels.txt", "r") as f:
        for line in f:
            labels.append(1 if int(line) == 5 else 0)

    trainFeatures, testFeatures, trainLabels, testLabels = cross_validation.train_test_split(features, labels, test_size = 0.3)

    clf = RandomForestClassifier(n_estimators=100)
    #clf = svm.SVC()
    clf.fit(trainFeatures, trainLabels)

    print(classification_report(clf.predict(trainFeatures), trainLabels))
    print(classification_report(clf.predict(testFeatures), testLabels))

if __name__ == "__main__":
    main()