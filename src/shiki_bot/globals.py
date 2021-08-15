import os
import traceback

from shiki_bot.locallib import Config
from shiki_bot.locallib import DatabaseAdapter
from shiki_bot.locallib import LoopRequestsTask
from shiki_bot.locallib import ShikiClient


class Globals(object):

    def __init__(self):
        pass

    def __bool__(self):
        return self._is_success

    def _init(self):
        super().__init__()
        self._is_success = False
        self._traceback = ''
        try:
            # region GLOBALS
            self.MY_ID = os.getenv("ADMIN_ID")
            self.TOKEN = os.getenv("DISCORD_BOT_TOKEN")
            self.DATABASE_URL = os.getenv("DATABASE_URL")
            self.VERSION = "RELEASE-1.3.10"
            self.GITHUB = 'https://github.com/thisUsernameIsAlredyTaken' \
                          + '/ShikimoriDiscordBot'
            self.DB_ADAPTER = DatabaseAdapter()
            self.CFG = Config()
            self.SHIKI_CLIENT = ShikiClient()
            self.LOOP_REQUEST_TASK = LoopRequestsTask()
            # endregion GLOBALS
            self._is_success = True
        except Exception:
            self._traceback = traceback.format_exc()

    def get_traceback(self):
        return self._traceback

    @property
    def BOT(self):
        return self._BOT

    @BOT.setter
    def BOT(self, value):
        self._BOT = value
        self._init()


# TODO: fix cyclic imports
G = Globals()
