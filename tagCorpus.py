__author__ = 'SEOKHO'

import sys
from textblob_aptagger import PerceptronTagger
from textblob import TextBlob
import generateTagWindows

aptagger = PerceptronTagger()

def main():
    with open("C:/MissingWord/train/corpusPart"+str(sys.argv[1])+".txt", "r", encoding = 'utf8') as f:
        with open("C:/MissingWord/train/tagsPart"+str(sys.argv[1])+".txt", "w", encoding = 'utf8') as tags:
            for i, line in enumerate(f):
                blob = TextBlob(line, pos_tagger = aptagger)
                tags.write('|'.join(generateTagWindows.getCompleteTags(blob))+"\n")

if __name__ == "__main__":
    main()