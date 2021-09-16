import sys
import traceback

import discord
from discord.ext import commands

from silicium_bot.logger import exception_logger as logger
from silicium_bot.modules import cogs
from silicium_bot.store import Store

bot = commands.Bot(command_prefix=";", help_command=None,
                   case_insensitive=True)


def log_traceback():
    logger.log(traceback.format_exc())


try:
    Store.init(bot)
except Exception:
    log_traceback()
    sys.exit(-1)


@bot.event
async def on_ready():
    try:
        for cog in cogs:
            await cog.on_ready(bot)
    except Exception:
        log_traceback()
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
        log_traceback()


for cog in cogs:
    bot.add_cog(cog(bot))
