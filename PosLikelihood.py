__author__ = 'SEOKHO'

import pickle
from collections import Counter
import generateTagWindows
from textblob import TextBlob
from textblob_aptagger import PerceptronTagger
import math
from WindowProb import WindowProb
import statistics
import numpy
import lexicalizedTagWindows

WIN_SIZE = 5
WIN_OFFSET = int((WIN_SIZE - 1) / 2)

aptagger = PerceptronTagger()

def removeWord(tokens):
    removed = []
    for i in range(1, len(tokens) - 1):
        removed.append([token for ind, token in enumerate(tokens) if ind != i])
    return removed

def modLikelihood(allLexTags, modProb4, windowProb5, tags, tagIndex):
    probs = []
    for alt in allLexTags:
        win4 = generateTagWindows.makeWindow(tags, begin = tagIndex - 2, end = tagIndex + 2)
        winProb4 = modProb4.of(win4)
        win5 = list(win4)
        win5.insert(2, alt)
        win5 = tuple(win5)
        winProb5 = windowProb5.of(win5)
        probs.append((winProb5 / winProb4 / (len(tags) - 1), alt))
    probs.sort(reverse=True)
    print (probs)
    return sum([prob[0] for prob in probs])

class SynReplacer:
    def __init__(self, winSize = 5, lex = False, compFile = False):
        self.lex = lex
        self.lexFilename = "lex" if lex else ""
        comp = "Comp" if lex else "comp"
        if not compFile:
            comp = ""
        self.winSize = winSize
        self.winOrig = WindowProb("C:/MissingWord/"+self.lexFilename+comp+str(self.winSize)+".pickle", compressed = compFile)

        with open("lexTags.pickle", "rb") as f:
            self.allLexTags = pickle.load(f) #inefficient to use a lexicalized set, but it will still work for unlexicalized models

        with open("toLexicalize.pickle", "rb") as f:
            self.toLexicalize = pickle.load(f)

        self.tagger = PerceptronTagger()

    def fix(self, tokens, index, withTags = False):
        blob = TextBlob(' '.join(tokens), pos_tagger = aptagger)
        tags = generateTagWindows.getCompleteTags(blob)
        if self.lex:
            tags = lexicalizedTagWindows.lexicalizeTags(tags, tokens, self.toLexicalize)
        return self.modLikelihood(tags, index, withTags = withTags)

    def modLikelihood(self, tags, tagIndex, withTags):
        probs = []
        counts = Counter()
        for alt in self.allLexTags:
            for offset in range(2, 3):
                win4 = generateTagWindows.makeWindow(tags, begin = tagIndex - WIN_SIZE + offset + 1, end = tagIndex + offset)
                win5 = list(win4)
                win5.insert(self.winSize - offset - 1, alt)
                counts[alt] += self.winOrig.count(win5)
        for alt in self.allLexTags:
            probs.append((counts[alt] / (sum(counts.values()) + 1), alt))
        probs.sort(reverse=True)
        if withTags:
            return probs
        return [prob[0] for prob in probs]

def main():
    synRepl = SynReplacer(lex = False)

    with open("C:/MissingWord/train/corpusPart0.txt", "r", encoding='utf8') as f:
        for i, line in enumerate(f):
            line = line.strip()
            if i > 0 :
                break
            line = "Japan has suspended of buffalo mozzarella from Italy , after reports that high levels of dioxin have been found in the cheese ."
            #line = "The cat screamed and ran into the house ."
            tokens = line.split()
            synRepl.fix(tokens, 3, withTags = True)
            for i in range(1, len(tokens) - 1):
                print(i, synRepl.fix(tokens, i, withTags = True))


if __name__ == "__main__":
    main()

