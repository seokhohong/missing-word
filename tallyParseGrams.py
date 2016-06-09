__author__ = 'SEOKHO'

from collections import Counter
import pickle

lex = False

def count(f, gramCounter):
    for index, line in enumerate(f):
            line = line.strip()
            if index % 100000 == 0:
                print(index, len(gramCounter))
            grams = line.split(",")
            if not lex:
                for index, gram in enumerate(grams):
                    if "/" in gram:
                        grams[index] = "|".join(gram.split("|")[:-1][1:])+"|"+gram.split("|")[-1].split("/")[1]
                    else:
                        #VM1
                        split = gram.split("|")
                        if(len(split) > 2):
                            grams[index] = split[0]+"|"+split[1];
            gramCounter.update(grams)

def main():

    gramCounter = Counter()
    with open("C:/MissingWord/parseGramsPart3.txt", "r") as f:
        count(f, gramCounter)
    with open("C:/MissingWord/parseModGramsPart3.txt", "r") as f:
        count(f, gramCounter)

    with open("gramLexiconVM1.txt", "wb") as f:
        pickle.dump(gramCounter, f)

if __name__ == "__main__":
    main()