__author__ = 'SEOKHO'

from model.bridge import getMergedFeatures
import os

import pickle

def process(clfName, sentenceLength, allFeatures):
    with open("C:/MissingWord/clf/"+clfName+".clf", "rb") as f:
        clf = pickle.load(f)

    clf.set_params(n_jobs = -1)

    flatten = []
    for features in allFeatures:
        flatten.extend(features)

    flatPredictions = clf.predict_proba(flatten)

    with open(clfName+".txt", "w") as f:
        for prediction in chunks(flatPredictions, sentenceLength):
            f.write(str(prediction.tolist())+"\n")

def chunks(l, n):
    """ Yield successive n-sized chunks from l.
    """
    for i in range(0, len(l), n):
        yield l[i:i+n]

def main():
    sentenceLength = 15
    allFeatures = getMergedFeatures("C:/MissingWord/mergedFeatures15.txt", numData = 50000)
    process(str(sentenceLength)+"rf1-5", sentenceLength, allFeatures)
    process(str(sentenceLength)+"rf1-10", sentenceLength, allFeatures)

if __name__ == "__main__":
    main()