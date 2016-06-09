__author__ = 'SEOKHO'

import pickle
import syntacticCorrection
from PosLikelihood import SynReplacer
from syntacticCorrection import SynCorrection
import random
import numpy as np

with open("synCorrLoose.clf", "rb") as f:
    looseClf = pickle.load(f)

with open("synCorrTight.clf", "rb") as f:
    tightClf = pickle.load(f)

synCor = SynCorrection(5)
synCorLex = SynCorrection(4, lex = True)
synRep = SynReplacer(lex = True)


with open("C:/MissingWord/train/corpusPart3.txt", "r") as f:
    for index, line in enumerate(f):
        line = line.strip()
        if len(line) > 1:
            features = []
            labels = []
            tokens = line.split(" ")
            removed = random.randint(1, len(tokens) - 2)
            cutTokens = tokens.copy()
            del cutTokens[removed]
            for i in range(len(cutTokens)):
                features.append(syntacticCorrection.makeFeatures(synCor, synCorLex, synRep, cutTokens, i))
            labelSet = [0] * (len(cutTokens))
            labelSet[removed] = 1
            labels.append(labelSet)

            try:
                print(tokens)
                print(cutTokens)
                print(np.array(labelSet))
                print(looseClf.predict(features))
                print(tightClf.predict(features))
            except UnicodeDecodeError:
                pass
        if index > 100 :
            break

