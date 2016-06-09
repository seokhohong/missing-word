__author__ = 'SEOKHO'

def chunks(l, n):
    for i in range(0, len(l), n):
        yield l[i:i+n]

def cutInHalf(l):
    cut = int(len(l) / 2)
    return [l[:cut], l[cut:]]

def cutInThree(l):
    cut = int(len(l) / 3)
    return [l[:cut], l[cut:2 * cut], l[2 * cut:]]

def flatten(l):
    return [item for sublist in l for item in sublist]