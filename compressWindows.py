__author__ = 'SEOKHO'

import pickle
import math

class WindowCompressor:
    def __init__(self):
        with open("lexTags.pickle", "rb") as f:
            self.lexTags = pickle.load(f)

        self.lexMap = dict()
        for index, tag in enumerate(self.lexTags):
            self.lexMap[tag] = index

        self.numBits = len(self.lexTags).bit_length()
        self.bitMask = [0] * 7 # shouldn't need bigger windows
        self.maxLength = 0 #keep track of maximum decompression

    def compressList(self, windows):
        return [self.compress(window) for window in windows]

    def compress(self, window):
        compressed = 0
        for index, tag in enumerate(window):
            if tag not in self.lexMap:
                tag = 'UNK'
            compressed += self.lexMap[tag] << (index * self.numBits)
        return compressed

    def decompress(self, compressed):
        window = []
        numTags = math.ceil(compressed.bit_length() / self.numBits)
        if numTags > self.maxLength:
            self.maxLength = numTags
            self.makeBitMasks()
        for index in range(numTags):
            window.append(self.lexTags[int((compressed & self.bitMask[index]) >> (index * self.numBits))])
        return tuple(window)

    def makeBitMasks(self):
        for i in range(self.maxLength):
            self.bitMask[i] = ((1 << ((i + 1) * self.numBits)) - 1) - ((1 << (i * self.numBits)) - 1)

def main():
    compressor = WindowCompressor()
    compressed = compressor.compress(("DT", "NN"))
    print(compressed)
    print(compressor.decompress(compressed))

if __name__ == "__main__":
    main()