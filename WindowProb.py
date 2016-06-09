__author__ = 'SEOKHO'

import pickle
from compressWindows import WindowCompressor

class WindowProb:
    def __init__(self, filename, compressed = False):
        with open(filename, "rb") as f:
            self.counter = pickle.load(f)
        self.total = sum(self.counter.values())
        self.compressed = compressed
        self.compressor = WindowCompressor()

    #smoothed with +1
    def of(self, window):
        window = self.compress(window)
        return (self.counter[window] + 1) / self.total

    def count(self, window):
        window = self.compress(window)
        if not self.compressed and type(window) is list:
            window = tuple(window)
        return self.counter[window]

    def compress(self, window):
        if self.compressed:
            return self.compressor.compress(window)
        else:
            return window