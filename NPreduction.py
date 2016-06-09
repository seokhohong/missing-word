__author__ = 'SEOKHO'

import filelib
from textblob import TextBlob
from textblob_aptagger import PerceptronTagger
import generateTagWindows

aptagger = PerceptronTagger()

def parseChunk(chunk):
    split = chunk.split("|")
    return split[0], split[1:]

export = open("C:/MissingWord/npReducedPart1.txt", "w", encoding = 'utf8')

index = 0
for tokenLine, chunkLine in filelib.readSimul(["C:/MissingWord/train/cleanTokensPart1.txt", "C:/MissingWord/chunksPart1.txt"]):
    tokens = tokenLine.split(" ")
    generateTagWindows.getCompleteTags(TextBlob(tokenLine, pos_tagger = aptagger))
    chunks = chunkLine.split("\\")
    #print(tokenLine, len(tokens))
    #print(chunkLine, len(chunks))
    tags = []
    for chunk in chunks:
        tags.extend(parseChunk(chunk)[1])
    if len(tokens) <= 1 or len(tokens) != len(tags):
        continue

    tokenIndex = 0
    reduced = []
    for chunk in chunks:
        phrase, tags = parseChunk(chunk)
        if phrase != "NP":
            for i in range(len(tags)):
                tags[i] = tags[i] + "_" + tokens[i + tokenIndex]
            reduced.extend(tags)
        else:
            reduced.append("NP")
        tokenIndex += len(tags)

    export.write('|'.join(reduced)+'\n')

    if index % 1000 == 0:
        print(index)
    index += 1
export.close()