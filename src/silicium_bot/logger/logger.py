import datetime

from silicium_bot.store import StaticStore


class Logger(object):
    loggers = []
    prefix_len = 0

    def __init__(self, name):
        self._name = name
        Logger.loggers.append(self)
        if len(name) > Logger.prefix_len:
            Logger.prefix_len = len(name)

    def log(self, message):
        if StaticStore.log_print_time:
            print(f"{datetime.datetime.now()}"
                  + f"[{self._name.rjust(Logger.prefix_len)}]: {message}")
        else:
            print(f"[{self._name.rjust(Logger.prefix_len)}]: {message}")
