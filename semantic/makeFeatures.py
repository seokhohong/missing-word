__author__ = 'SEOKHO'


def getNormalWindows(tokens, windowSize, default = ""):
    windows = []
    for i in range(len(tokens) - 1):
        window = []
        for j in range(-windowSize, windowSize):
            window.append(getElem(tokens, i + j, default))
        windows.append(window)
    return windows

def getModWindows(tokens, windowSize, default = ""):
    windows = []
    for i in range(len(tokens) - 1):
        window = []
        for j in range(-windowSize, windowSize + 1):
            if j == 0:
                continue
            window.append(getElem(tokens, i + j, default))
        windows.append(window)
    return windows

def getElem(elems, index, default = ""):
    if index < 0 or index >= len(elems):
        return default
    return elems[index]