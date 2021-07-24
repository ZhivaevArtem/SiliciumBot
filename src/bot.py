import asyncio
from asyncio import tasks
import json
import discord
import datetime
import time
from discord.enums import ChannelType, Status
from os import path, stat, environ
from shikilogsparser import retrieve_new_logs_by_usernames


client = discord.Client()

# region helper things
class Config(object):
    def __init__(self):
        self._usernames = []
        self._message_channel = None
        self._status = Status.online
        self._long_pooling_interval = 5 * 60
        self._prefix = ';'
        self._path = ''

    # region properties
    @property
    def usernames(self):
        return self._usernames

    @usernames.setter
    def usernames(self, value):
        self._usernames = value
        self.store()

    @property
    def message_channel(self):
        return self._message_channel

    @message_channel.setter
    def message_channel(self, value):
        self._message_channel = value
        self.store()

    @property
    def status(self):
        return self._status

    @status.setter
    def status(self, value):
        self._status = value
        self.store()

    @property
    def long_pooling_interval(self):
        return self._long_pooling_interval

    @long_pooling_interval.setter
    def long_pooling_interval(self, value):
        if value < 2:
            self._long_pooling_interval = 2
        else:
            self._long_pooling_interval = value
        self.store()

    @property
    def prefix(self):
        return self._prefix

    @prefix.setter
    def prefix(self, value):
        self._prefix = value
        self.store()
    # endregion properties

    def toJSON(self):
        return json.dumps({
            'usernames': self._usernames,
            'message_channel_id': self._message_channel and self._message_channel.id,
            'status': str(self._status),
            'long_pooling_interval': self._long_pooling_interval,
            'prefix': self._prefix
        }, indent=4)

    def fromJSON(self, jsonstr):
        global client
        d = json.loads(jsonstr)
        if d['usernames']:
            self._usernames = d['usernames']
        if d['message_channel_id']:
            self._message_channel = client.get_channel(d['message_channel_id'])
        if d['status']:
            self._status = Status(d['status'])
        if d['long_pooling_interval']:
            self._long_pooling_interval = d['long_pooling_interval']
        if d['prefix']:
            self._prefix = d['prefix']

    def load(self, path):
        self._path = path
        try:
            with open(path, 'r') as f:
                self.fromJSON(f.read())
        except:
            pass

    def store(self):
        try:
            with open(self._path, 'w') as f:
                f.write(self.toJSON())
        except:
            pass

    def __str__(self):
        return self.toJSON()

CFG = Config()

class ShikiWatcherTask(object):
    def __init__(self):
        self._is_running = False
        self._task = None

    async def _run(self):
        global CFG
        while self._is_running:
            print('while: ' + str(datetime.datetime.now()))
            grouped_logs = retrieve_new_logs_by_usernames(CFG.usernames)
            notification_message = parse_shiki_logs(grouped_logs)
            if notification_message:
                await CFG.message_channel.send(notification_message)
            for i in range(CFG.long_pooling_interval // 2):
                if not self._is_running:
                    return
                await asyncio.sleep(2)

    def is_running(self):
        return self._task is not None and not self._task.done()

    def start(self):
        global client
        if self.is_running():
            return
        self._is_running = True
        self._task = client.loop.create_task(self._run())

    def stop(self):
        self._is_running = False

    def restart(self):
        if self.is_running():
            self._task.add_done_callback(lambda e: self.start())
            self.stop()

SHIKI_WATCHER_TASK = ShikiWatcherTask()

def parse_shiki_logs(grouped_logs):
    result = ""
    for username, user_logs in grouped_logs.items():
        if user_logs:
            result += f"{username}: {user_logs}"
    return result
# endregion helper things


@client.event
async def on_ready():
    global CFG
    resources_path = path.dirname(path.dirname(__file__))
    resources_path = path.join(resources_path, 'resources')
    config_path = path.join(resources_path, 'cfg.json')
    CFG.load(config_path)
    await client.change_presence(status=CFG.status)
    print('ready')

@client.event
async def on_message(message):
    global CFG
    global SHIKI_WATCHER_TASK
    if message.content.startswith(CFG.prefix):
        args = message.content[len(CFG.prefix):].strip().split()

        if args[0] == 'isalive':
            await message.channel.send('im okay')

        elif args[0] == 'usechannel':
            CFG.message_channel = message.channel
            await message.channel.send('I will be using this channel')

        elif not CFG.message_channel:
            return

        elif args[0] == 'watcher':
            if len(args) == 1:
                await CFG.message_channel.send(
                    'running' if SHIKI_WATCHER_TASK.is_running() else 'stopped'
                )
            elif len(args) == 2:
                if args[1] == 'start':
                    SHIKI_WATCHER_TASK.start()
                elif args[1] == 'stop':
                    SHIKI_WATCHER_TASK.stop()

        elif args[0] == 'config':
            if len(args) == 1:
                await CFG.message_channel.send(str(CFG))
            if len(args) > 1:
                if args[1] == 'users' and len(args) == 2:
                    await CFG.message_channel.send(', '.join(CFG.usernames))
                elif args[1] == 'users' and len(args) > 2:
                    if args[2] == 'add':
                        for i in range(3, len(args)):
                            if args[i] not in CFG.usernames:
                                CFG.usernames.append(args[i])
                        CFG.store()
                    elif args[2] == 'clear':
                        CFG.usernames = []
                    elif args[2] == 'remove':
                        for i in range(3, len(args)):
                            if args[i] in CFG.usernames:
                                CFG.usernames.remove(args[i])
                        CFG.store()
                elif args[1] == 'status' and len(args) == 3:
                    try:
                        status = Status(args[2])
                        await client.change_presence(status=status)
                        CFG.status = status
                    except:
                        pass
                elif args[1] == 'interval' and len(args) == 3:
                    try:
                        interval = int(args[2])
                        CFG.long_pooling_interval = int(interval)
                        SHIKI_WATCHER_TASK.restart()
                    except:
                        pass
                elif args[1] == 'prefix' and len(args) == 3:
                    CFG.prefix = args[2]

TOKEN = environ.get('discord_bot_token')
print(f"TOKEN = '{TOKEN}'")
client.run(TOKEN)
