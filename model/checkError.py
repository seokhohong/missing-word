__author__ = 'SEOKHO'


import numpy as np
import pickle
from sklearn.utils import validation

with open("error.np", "rb") as f:
    X = pickle.load(f)

for array in X:
    try:
        validation._assert_all_finite(array)
    except ValueError:
        print(array)