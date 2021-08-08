import asyncio
import datetime
import traceback
from asyncio.tasks import Task

import discord

from .config import Config
from .shiki_client import ShikiClient


class LoopRequestsTask(object):
    def __init__(self, config: Config, discord_client: discord.Client,
                 shiki_client: ShikiClient):
        super().__init__()
        self._config = config
        self._discord_client = discord_client
        self._shiki_client = shiki_client
        self._task: Task or None = None
        self._is_running = False
        self._restart_attempts = 0
        self._MAX_RESTART_ATTEMPTS = 5

    def start(self, callback=None):
        if not self.is_running():
            self._is_running = True
            self._task = self._discord_client.loop.create_task(self._run())
            self._restart_attempts = 0
            print('Worker started')
            if callback is not None:
                try:
                    callback()
                except Exception:
                    print(traceback.format_exc())

    def stop(self, callback=None):
        if self.is_running():
            if callback is not None:
                def cb():
                    try:
                        callback()
                    except Exception:
                        print(traceback.format_exc())
                self._task.add_done_callback(lambda e: cb())
            self._is_running = False
            print('Worker stopped')

    def restart(self, callback=None):
        if self.is_running():
            self._task.add_done_callback(lambda e: self.start())
            if callback is not None:
                def cb():
                    try:
                        callback()
                    except Exception:
                        print(traceback.format_exc())
                self._task.add_done_callback(lambda e: cb())
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
                    await self._config.notification_channel.send(response)
                for i in range(self._config.loop_requests_interval // 2):
                    if not self._is_running:
                        return
                    await asyncio.sleep(2)
        except Exception:
            print(traceback.format_exc())
            if self._restart_attempts < self._MAX_RESTART_ATTEMPTS:
                self._restart_attempts += 1
                self.restart()
            else:
                print(f"Cannot restart shiki worker (attempts: "
                      + f"{self._MAX_RESTART_ATTEMPTS})")
