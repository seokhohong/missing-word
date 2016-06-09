__author__ = 'SEOKHO'

from collections import Counter
import pickle

with open("C:/MissingWord/parseModCounter.pickle", "rb") as f:
    counter = pickle.load(f)

with open("C:/MissingWord/unrolledParseModCounter.txt", "w") as f:
    for key in counter:
        f.write(','.join(key)+"\\"+str(counter[key])+"\n")