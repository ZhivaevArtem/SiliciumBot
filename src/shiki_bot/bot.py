import re
import traceback

import discord

from locallib import Config
from locallib import DatabaseAdapter
from locallib import LoopRequestsTask
from locallib import ShikiClient
from locallib import invoke_timeout

client = discord.Client()

VERSION = "RELEASE 1.3.7"
DB_ADAPTER = DatabaseAdapter()
CFG = Config(DB_ADAPTER, client)
SHIKI_CLIENT = ShikiClient(CFG)
LOOP_REQUESTS = LoopRequestsTask(CFG, client, SHIKI_CLIENT)


@client.event
async def on_ready():
    global DB_ADAPTER
    global CFG
    global LOOP_REQUESTS
    try:
        DB_ADAPTER.connect()
        CFG.load()
        LOOP_REQUESTS.start()
        if CFG.status != discord.Status.online:
            await client.change_presence(status=discord.Status.online)
        if CFG.activity.type != discord.ActivityType.unknown:
            await client.change_presence(activity=CFG.activity)
    except Exception:
        print('Error during initialization')
        print(traceback.format_exc())
        await client.close()
        print('Bot not started')
    print('Bot ready')


@client.event
async def on_message(message: discord.Message):
    global CFG
    try:
        if message.author == client.user:
            return
        content = message.content.strip()
        # region jokes
        if content in CFG.jokes and len(CFG.jokes) > 0:
            await message.channel.send(CFG.jokes[content],
                                       reference=message)
            return
        # endregion jokes
        if re.match(r'^.*[0-9]+.*$', content) \
           and re.match(r'^[0-9/*\-+. \t\n()]+$', content) \
           and not re.match(r'^[\-+]*[ \t]*[0-9]*\.?[0-9]*$', content):
            n = invoke_timeout(eval, CFG.calculator_timeout, content)
            if type(n) in (int, float):
                await message.channel.send(n, reference=message)
            else:
                await message.channel.send("Очень сложно, давай-ка сам",
                                           reference=message)
            return
        if message.content.startswith(CFG.prefix):
            print(message.author, message.channel.id, message.content)
            args = message.content[len(CFG.prefix):].split()
            if args[0] == 'help':
                await command_help(message)
            elif args[0] == 'config':
                await command_config(message, args)
            elif args[0] == 'usechannel':
                await command_usechannel(message)
            elif args[0] == 'worker':
                await command_worker(message, args)
            elif args[0] == 'github':
                await command_github(message)
            elif args[0] == 'version':
                await command_version(message)
    except Exception:
        print(traceback.format_exc())


# region commands

async def command_worker(message: discord.Message, args: list[str]):
    global LOOP_REQUESTS
    if len(args) > 1:
        if args[1] == 'start':
            LOOP_REQUESTS.start()
        elif args[1] == 'stop':
            LOOP_REQUESTS.stop()
    await message.channel.send("Worker running" if LOOP_REQUESTS.is_running()
                               else "Worker stopped", reference=message)


async def command_version(message: discord.Message):
    global VERSION
    await message.channel.send(VERSION, reference=message)


async def command_help(message: discord.Message):
    response = """
`config`: show config
`config users <add/remove> <usernames>`: add or remove users
`config users clear`: truncate users
`config interval <time in seconds>`: interval between requests
`config limit <count>`: query limit to /api/users/{id}/history
`config prefix <prefix>`: command prefix
`config status <online/invisible/idle/dnd>`: set bot status
`config activity <playing/streaming/listening/watching> <text>`: set bot \
activity
`config activity clear`: remove bot activity
`config jokes add "<message>" "<reaction>"`: add joke
`config jokes remove <jokes>`: remove jokes (jokes must be quoted)
`config timeout <time in seconds>`: max time per single math operation
`config jokes clear`: remove all jokes
`usechannel`: use this channel for notifications
`worker`: get worker status
`worker <start/stop>`: start/stop worker
`github`: get link to the source code
`version`: get version
"""
    await message.channel.send(response, reference=message)


# TODO: remove some commands from ;config section
async def command_config(message: discord.Message, args: list[str]):
    global CFG
    global LOOP_REQUESTS
    if len(args) > 1:
        if args[1] == 'users' and len(args) > 2:
            if args[2] == 'add':
                CFG.add_users(args[3:])
            elif args[2] == 'clear':
                CFG.truncate_users()
            elif args[2] == 'remove':
                CFG.delete_users(args[3:])
        elif args[1] == 'jokes' and len(args) > 2:
            if args[2] == 'add' and len(args) > 4:
                substrings = message.content.split('"')
                if len(substrings) < 4:
                    return
                mess = substrings[1].strip()
                react = substrings[3].strip()
                if mess and react:
                    CFG.add_joke(mess, react)
            elif args[2] == 'clear':
                CFG.truncate_jokes()
            elif args[2] == 'remove':
                substrings = [s.strip() for s in message.content.split('"')]
                CFG.delete_jokes(substrings[1::2])
        elif args[1] == 'status' and len(args) == 3:
            try:
                status = discord.Status(args[2])
                if status != discord.Status.online:
                    CFG.activity = discord.Activity()
                await client.change_presence(status=status)
                CFG.status = status
            except ValueError:
                pass
        elif args[1] == 'interval' and len(args) == 3:
            try:
                interval = int(args[2])
                if interval < 2:
                    interval = 2
                elif interval > 3600:
                    interval = 3600
                if LOOP_REQUESTS.is_running():
                    LOOP_REQUESTS.restart()
                CFG.long_pooling_interval = interval
            except ValueError:
                pass
        elif args[1] == 'limit' and len(args) == 3:
            try:
                limit = int(args[2])
                if limit < 2:
                    limit = 2
                elif limit > 50:
                    limit = 50
                CFG.long_pooling_query_limit = limit
            except ValueError:
                pass
        elif args[1] == 'prefix' and len(args) == 3:
            CFG.prefix = args[2]
        elif args[1] == 'timeout' and len(args) == 3:
            try:
                timeout = float(args[2])
                if timeout > 10:
                    timeout = 10
                elif timeout < 0.1:
                    timeout = 0.1
                CFG.calculator_timeout = timeout
            except ValueError:
                pass
        elif args[1] == 'activity':
            if len(args) == 3:
                activity = discord.Activity()
                await client.change_presence(activity=activity)
                CFG.activity = activity
            elif len(args) > 3:
                activity_text = ' '.join(args[3:])
                activity_type = discord.ActivityType.unknown
                if args[2] == 'playing':
                    activity_type = discord.ActivityType.playing
                elif args[2] == 'streaming':
                    activity_type = discord.ActivityType.streaming
                elif args[2] == 'listening':
                    activity_type = discord.ActivityType.listening
                elif args[2] == 'watching':
                    activity_type = discord.ActivityType.watching
                activity = discord.Activity()
                if activity_type != discord.ActivityType.unknown:
                    activity = discord.Activity(name=activity_text,
                                                type=activity_type)
                    CFG.status = discord.Status.online
                await client.change_presence(activity=activity)
                CFG.activity = activity
    await message.channel.send(str(CFG), reference=message)


async def command_usechannel(message: discord.Message):
    await message.channel.send("Okay. This channel", reference=message)
    CFG.message_channel = message.channel


async def command_github(message: discord.Message):
    url = 'https://github.com/thisUsernameIsAlredyTaken/ShikimoriDiscordBot'
    await message.channel.send(url, reference=message)

# endregion commands
