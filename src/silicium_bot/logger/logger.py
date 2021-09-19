import datetime

from silicium_bot.constants import Constants


class Logger(object):
    loggers = []
    prefix_len = 0

    def __init__(self, name):
        self._name = name
        Logger.loggers.append(self)
        if len(name) > Logger.prefix_len:
            Logger.prefix_len = len(name)

    def log(self, message):
        strings = str(message).splitlines()
        for s in strings:
            if Constants.log_print_time:
                print(f"{datetime.datetime.now()}"
                      + f"[{self._name.rjust(Logger.prefix_len)}]: {s}")
            else:
                print(f"[{self._name.rjust(Logger.prefix_len)}]: {s}")
