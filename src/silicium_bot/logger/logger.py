class Logger(object):
    _max_prefix_len = 0

    def __init__(self, tool: str):
        self._prefix = f"[{tool}]: "
        Logger._max_prefix_len = max(Logger._max_prefix_len, len(self._prefix))

    def log(self, info):
        print(f"{self._prefix.ljust(Logger._max_prefix_len, ' ')}{info}")


class Loggers(enumerate):
    Postgres = Logger('PostgreSQL')
    ShikiDaemon = Logger('Daemon')
