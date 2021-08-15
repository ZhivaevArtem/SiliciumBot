import re
import sys
import traceback

import discord
from discord.ext import commands

from globals import G
from locallib import invoke_timeout
import modules

bot = commands.Bot(command_prefix=";", help_command=None)

# region init

if not G:
    print("Error occurred while init")
    print(G.get_traceback())
    sys.exit()

# endregion init


@bot.event
async def on_ready():
    if G.CFG.status != discord.Status.online:
        await bot.change_presence(status=discord.Status.online)
    if G.CFG.activity.type != discord.ActivityType.unknown:
        await bot.change_presence(activity=G.CFG.activity)
    if G.CFG.prefix != bot.command_prefix:
        bot.command_prefix = G.CFG.prefix
    if G.CFG.is_worker_running:
        G.LOOP_REQUESTS.start()
    print(f'Bot ready. Prefix: {G.CFG.prefix}')


@bot.event
async def on_message(message: discord.Message):
    try:
        if message.author == bot.user:
            return
        # await message.channel.send(message.content)
        content = message.content.strip()
        # region jokes
        if content in G.CFG.jokes and len(G.CFG.jokes) > 0:
            await message.channel.send(G.CFG.jokes[content],
                                       reference=message)
            return
        # endregion jokes
        if re.match(r'^.*[0-9]+.*$', content) \
           and re.match(r'^[0-9/*\-+. \t\n()]+$', content) \
           and not re.match(r'^[\-+]*[ \t]*[0-9]*\.?[0-9]*$', content):
            n = invoke_timeout(eval, G.CFG.calculator_timeout, content)
            if type(n) in (int, float):
                await message.channel.send(n, reference=message)
            else:
                await message.channel.send("Очень сложно, давай-ка сам",
                                           reference=message)
            return
        await bot.process_commands(message)
    except Exception:
        print(traceback.format_exc())


for cog in modules.cogs:
    bot.add_cog(cog())
