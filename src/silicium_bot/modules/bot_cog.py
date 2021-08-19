import discord
from discord.ext import commands

from .exception_cog import ExceptionCog
from .functions import *
from ..globals import G


class BotCog(ExceptionCog):

    @commands.group(invoke_without_command=True)
    async def bot(self, ctx: commands.Context):
        pass

    # bot prefix
    @bot.command()
    async def prefix(self, ctx: commands.Context, new_prefix=None):
        raise_if_not_me(ctx)
        if new_prefix is None:
            await ctx.send(f'Current prefix: "{self.bot.command_prefix}"',
                           reference=ctx.message)
        else:
            if G.CFG.prefix != new_prefix:
                G.BOT.command_prefix = new_prefix
                G.CFG.prefix = new_prefix
            await ctx.send(f'New prefix: "{new_prefix}"',
                           reference=ctx.message)

    # bot status
    @bot.command()
    async def status(self, ctx: commands.Context, new_status: str):
        if new_status in ('online', 'invisible', 'idle', 'dnd'):
            stat = discord.Status(new_status)
            if G.CFG.status != stat:
                G.CFG.status = stat
                await G.BOT.change_presence(status=stat)

    # bot activity
    @bot.command()
    async def activity(self, ctx: commands.Context,
                       activity_type: str, activity_text: str):
        type_map = {
            'playing':   discord.ActivityType.playing,
            'streaming': discord.ActivityType.streaming,
            'listening': discord.ActivityType.listening,
            'watching':  discord.ActivityType.watching,
        }
        if activity_type in type_map:
            new_activity = discord.Activity(name=activity_text,
                                            type=type_map[activity_type])
            if new_activity != G.CFG.activity:
                G.CFG.activity = new_activity
                await G.BOT.change_presence(activity=new_activity)
