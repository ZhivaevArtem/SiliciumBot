import asyncio
import datetime
import traceback
from asyncio.tasks import Task

import discord

from .config import Config
from .shiki_client import ShikiClient


class BotWorkerTask(object):
    def __init__(self, config: Config, discord_client: discord.Client,
                 shiki_client: ShikiClient):
        super().__init__()
        self._config = config
        self._discord_client = discord_client
        self._shiki_client = shiki_client
        self._task: Task or None = None
        self._is_running = False

    def start(self):
        if not self.is_running():
            self._is_running = True
            self._task = self._discord_client.loop.create_task(self._run())
            print('Worker started')

    def stop(self):
        if self.is_running():
            self._is_running = False
            print('Worker stopped')

    def restart(self):
        if self.is_running():
            self._task.add_done_callback(lambda e: self.start())
            self.stop()
        else:
            self.start()

    def is_running(self) -> bool:
        return self._task is not None and not self._task.done()

    async def _run(self):
        try:
            while self._is_running:
                print(f'Worker: {datetime.datetime.now()}')
                response = ""
                for username in self._config.usernames:
                    logs = self._shiki_client.retrieve_user_logs(username)
                    for log in logs:
                        message = log.get_message() + "\n"
                        response += message
                        print(message)
                if response:
                    await self._config.message_channel.send(response)
                for i in range(self._config.long_pooling_interval // 2):
                    if not self._is_running:
                        return
                    await asyncio.sleep(2)
        except Exception as e:
            print(traceback.format_exc())
            self.restart()
