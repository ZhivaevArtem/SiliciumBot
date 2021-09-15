import sys

import discord
from discord.ext import commands

from silicium_bot.modules import cogs
from silicium_bot.store import Store

bot = commands.Bot(command_prefix=";", help_command=None,
                   case_insensitive=True)


# region init

try:
    Store.init(bot)
except Exception:
    sys.exit(-1)


# endregion init


@bot.event
async def on_ready():
    try:
        for cog in cogs:
            await cog.on_ready(bot)
    except Exception:
        sys.exit(-1)


@bot.event
async def on_message(message: discord.Message):
    try:
        if message.author == bot.user:
            return
        for cog in cogs:
            if await cog.on_message(message, bot):
                return
        await bot.process_commands(message)
    except Exception:
        pass


for cog in cogs:
    bot.add_cog(cog(bot))
