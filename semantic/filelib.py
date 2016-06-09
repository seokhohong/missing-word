__author__ = 'SEOKHO'

def readSimul(files, encoding = 'utf8'):
    handles = [open(file, "r", encoding = encoding) for file in files]
    while True:
        lines = []
        try:
            for file in handles:
                while True:
                    line = file.readline()
                    if len(line) == 0:
                        return
                    line = line.strip()
                    if len(line) > 0:
                        break
                lines.append(line)
        except UnicodeDecodeError:
            continue
        if len(lines[0]) > 0:
            yield lines
        else:
            break
    for handle in handles:
        handle.close()