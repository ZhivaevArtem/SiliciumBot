import os
import re
import sys
import traceback

import discord
from discord.ext import commands

from locallib import Config
from locallib import DatabaseAdapter
from locallib import LoopRequestsTask
from locallib import ShikiClient
from locallib import invoke_timeout
from discord_cogs import BotCog

bot = commands.Bot(command_prefix=";", help_command=None)

# region init

VERSION = "RELEASE 1.3.10"
ADMIN_ID = int(os.getenv("ADMIN_ID"))
GITHUB = 'https://github.com/thisUsernameIsAlredyTaken/ShikimoriDiscordBot'
try:
    global DB_ADAPTER
    global CFG
    global SHIKI_CLIENT
    global LOOP_REQUESTS
    DB_ADAPTER = DatabaseAdapter()
    CFG = Config(DB_ADAPTER, bot)
    SHIKI_CLIENT = ShikiClient(CFG)
    LOOP_REQUESTS = LoopRequestsTask(CFG, bot, SHIKI_CLIENT)
except Exception:
    print("Error occurred while init")
    print(traceback.format_exc())
    sys.exit()

# endregion init


@bot.event
async def on_ready():
    if CFG.status != discord.Status.online:
        await bot.change_presence(status=discord.Status.online)
    if CFG.activity.type != discord.ActivityType.unknown:
        await bot.change_presence(activity=CFG.activity)
    if CFG.prefix != bot.command_prefix:
        bot.command_prefix = CFG.prefix
    if CFG.is_worker_running:
        LOOP_REQUESTS.start()
    print(f'Bot ready. Prefix: {CFG.prefix}')


@bot.event
async def on_message(message: discord.Message):
    try:
        if message.author == bot.user:
            return
        # await message.channel.send(message.content)
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
        await bot.process_commands(message)
    except Exception:
        print(traceback.format_exc())


bot.add_cog(BotCog(bot=bot,
                   cfg=CFG,
                   loop=LOOP_REQUESTS,
                   admin_id=ADMIN_ID,
                   version=VERSION,
                   github=GITHUB))
