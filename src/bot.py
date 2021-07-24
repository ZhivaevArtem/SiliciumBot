import asyncio
from asyncio import tasks
import json
import discord
import time
from discord.enums import ChannelType, Status
from os import path, stat, environ
from shikilogsparser import retrieve_new_logs_by_usernames


client = discord.Client()

class Config(object):
    global client
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
shiki_watcher_task = None
is_shiki_watcher_running = False


async def start_shiki_watcher_async():
    global CFG
    global is_shiki_watcher_running
    if is_shiki_watcher_running: return
    is_shiki_watcher_running = True
    while is_shiki_watcher_running:
        print('while')
        grouped_logs = retrieve_new_logs_by_usernames(CFG.usernames)
        for username, user_logs in grouped_logs.items():
            if user_logs:
                print(grouped_logs)
                if message_channel:
                    await message_channel.send(str(grouped_logs))
                break
        await asyncio.sleep(5)

def stop_shiki_watcher():
    is_shiki_watcher_running = False

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
    global shiki_watcher_task
    if message.content.startswith(CFG.prefix):
        args = message.content[len(CFG.prefix):].strip().split()

        if args[0] == 'isalive':
            await message.channel.send('im okay')

        elif args[0] == 'usechannel':
            CFG.message_channel = message.channel
            await message.channel.send('I will be using this channel')

        elif not CFG.message_channel:
            return

        elif args[0] == 'config':
            if len(args) == 1:
                await CFG.message_channel.send(str(CFG))
            if len(args) > 1:
                if args[1] == 'usernames' and len(args) > 2:
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
                    except:
                        pass
                elif args[1] == 'prefix' and len(args) == 3:
                    CFG.prefix = args[2]

TOKEN = environ.get('discord_bot_token')
print(f"TOKEN = '{TOKEN}'")
client.run(TOKEN)
