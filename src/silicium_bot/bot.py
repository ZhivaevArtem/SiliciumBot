import sys
import traceback

import discord
from discord.ext import commands

from silicium_bot.logger import exception_logger as logger
from silicium_bot.modules import cogs
from silicium_bot.store import Store

intents = discord.Intents.default()
intents.members = True
intents.reactions = True

bot = commands.Bot(command_prefix=";", help_command=None,
                   case_insensitive=True, intents=intents)


def log_traceback():
    logger.log(traceback.format_exc())

@bot.event
async def on_ready():
    try:
        Store.init(bot)
        for cog in list(bot.cogs.values()):
            await cog.on_ready()
    except Exception:
        log_traceback()
        logger.log("Exit...")
        sys.exit(-1)
    logger.log("Bot started...")


@bot.event
async def on_message(message: discord.Message):
    try:
        for cog in list(bot.cogs.values()):
            try:
                if await cog.on_message(message):
                    return
            except Exception:
                log_traceback()
        await bot.process_commands(message)
    except Exception:
        log_traceback()

@bot.event
async def on_reaction_add(reaction, user):
    try:
        for cog in list(bot.cogs.values()):
            try:
                await cog.on_reaction_add(reaction, user)
            except Exception:
                log_traceback()
    except Exception:
        log_traceback()

@bot.event
async def on_reaction_remove(reaction, user):
    try:
        for cog in list(bot.cogs.values()):
            try:
                await cog.on_reaction_remove(reaction, user)
            except Exception:
                log_traceback()
    except Exception:
        log_traceback()


for cog in cogs:
    bot.add_cog(cog(bot))
