import datetime


class Logger(object):
    print_time = True
    loggers = []
    prefix_len = 0

    def __init__(self, name):
        self._name = name
        Logger.loggers.append(self)
        if len(name) > Logger.prefix_len:
            Logger.prefix_len = len(name)

    def log(self, message):
        print(f"{datetime.datetime.now()}"
              + f"[{self._name.rjust(Logger.prefix_len)}]: {message}")
