import asyncio
from asyncio.tasks import Task

import discord

from silicium_bot.logger import shiki_logger as logger
from silicium_bot.modules.shiki.shiki_client import ShikiClient
from silicium_bot.store import Store


class LoopRequestsTask(object):
    def __init__(self, bot):
        super().__init__()
        self._bot = bot
        self._task: Task or None = None
        self._is_running = False
        self._restart_attempts = 0
        self._shiki_client = ShikiClient()
        self._MAX_RESTART_ATTEMPTS = 5

    def start(self, callback=None):
        if not self.is_running():
            self._is_running = True
            self._task = self._bot.loop.create_task(self._run())
            self._restart_attempts = 0
            if callback is not None:
                try:
                    callback()
                except Exception:
                    pass

    def stop(self, callback=None):
        if self.is_running():
            if callback is not None:
                def cb():
                    try:
                        callback()
                    except Exception:
                        pass
                self._task.add_done_callback(lambda e: cb())
            self._is_running = False

    def restart(self, callback=None):
        if self.is_running():
            self._task.add_done_callback(lambda e: self.start())
            if callback is not None:
                def cb():
                    try:
                        callback()
                    except Exception:
                        pass
                self._task.add_done_callback(lambda e: cb())
            self.stop()

    def is_running(self) -> bool:
        return self._task is not None and not self._task.done()

    async def _run(self):
        try:
            while self._is_running:
                logger.log("Daemon iteration")
                response = ""
                for username in Store.shiki_usernames.value:
                    logs = self._shiki_client.retrieve_user_logs(username)
                    for log in logs:
                        message = log.get_embed_message() + "\n\n"
                        response += message
                if response:
                    logger.log(f"Found new logs:")
                    logger.log(response)
                    embed = discord.Embed(description=response)
                    await Store.notification_channel.value.send(embed=embed)
                for i in range(Store.daemon_interval.value // 2):
                    if not self._is_running:
                        return
                    await asyncio.sleep(2)
        except Exception:
            if self._restart_attempts < self._MAX_RESTART_ATTEMPTS:
                self._restart_attempts += 1
                self.restart()
            else:
                logger.log("Cannot restart loop request with"
                           + f" {self._MAX_RESTART_ATTEMPTS} attempts")
