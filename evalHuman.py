__author__ = 'SEOKHO'

import random
from sklearn.metrics import classification_report

guessesList = []
actualList = []

#
#   TP 0                Guessed 0 but 1
#   Guessed 1 but 0     TP 1

with open("C:/MissingWord/train/corpusPart3.txt", "r") as f:
    for line in f:
        tokens = line.strip().split(" ")
        if len(tokens) >= 3:
            removedIndex = random.randint(1, len(tokens) - 2)
            numPossibleGuesses = len(range(1, len(tokens) - 2))
            tokens.pop(removedIndex)
            removedIndex = removedIndex - 1
            display = []
            for index, token in enumerate(tokens):
                display.append(token)
                if index < len(tokens) - 1:
                    display.append("("+str(index)+")")

            print(' '.join(tokens));
            print(' '.join(display))
            answer = int(input())

            print("Correct Answer "+str(removedIndex))
            if answer == -1:
                guessesList.extend([0] * numPossibleGuesses)
                actualList.extend([0] * (numPossibleGuesses - 1))
                actualList.append(1)
            elif answer == removedIndex:
                guessesList.extend([0] * (numPossibleGuesses - 1))
                guessesList.append(1)
                actualList.extend([0] * (numPossibleGuesses - 1))
                actualList.append(1)
            else:
                guessesList.append(1)
                guessesList.extend([0] * (numPossibleGuesses - 1))
                actualList.extend([0] * (numPossibleGuesses - 1))
                actualList.append(1)

            print(classification_report(actualList, guessesList))