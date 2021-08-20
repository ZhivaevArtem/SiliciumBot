import sys
import traceback

import discord
from discord.ext import commands

from silicium_bot.configuration import Config
from silicium_bot.database_adapter import DatabaseAdapter
from silicium_bot.globals import G
from silicium_bot.modules import cogs

bot = commands.Bot(command_prefix=";", help_command=None,
                   case_insensitive=True)


# region init

try:
    G.BOT = bot
    G.DB_ADAPTER = DatabaseAdapter()
except Exception:
    print("Failed to init")
    print(traceback.format_exc())
    sys.exit(-1)


# endregion init


@bot.event
async def on_ready():
    try:
        G.CFG = Config()
        for cog in cogs:
            await cog.on_ready()
        print(f"Bot ready. Prefix: {bot.command_prefix}")
        print(f"Config:")
        print(f"{G.CFG}")
    except Exception:
        print("Failed to start")
        print(traceback.format_exc())
        sys.exit(-1)


@bot.event
async def on_message(message: discord.Message):
    try:
        if message.author == bot.user:
            return
        for cog in cogs:
            if await cog.on_message(message):
                return
        await bot.process_commands(message)
    except Exception:
        print(traceback.format_exc())


for cog in cogs:
    bot.add_cog(cog)
