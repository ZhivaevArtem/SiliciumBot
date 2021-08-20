import discord
from discord.ext import commands

from silicium_bot.modules.module_base import ModuleBase
from silicium_bot.globals import G


class BotModule(ModuleBase):
    async def on_ready(self):
        if G.CFG.activity.type != discord.ActivityType.unknown:
            G.BOT.change_presence(activity=G.CFG.activity)
        elif G.CFG.status != discord.Status.online:
            G.BOT.change_presence(status=G.CFG.status)
        if G.BOT.command_prefix != G.CFG.prefix:
            G.BOT.command_prefix = G.CFG.prefix

    @commands.group(invoke_without_command=True)
    async def bot(self, ctx: commands.Context):
        pass

    # bot prefix
    @bot.command()
    async def prefix(self, ctx: commands.Context, new_prefix=None):
        self.raise_if_not_me(ctx)
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
        status_map = {
            'online': discord.Status.online,
            'invisible': discord.Status.invisible,
            'idle': discord.Status.idle,
            'dnd': discord.Status.dnd
        }
        if new_status in status_map:
            stat = status_map[new_status]
            if G.CFG.status != stat:
                G.CFG.status = stat
                G.CFG.activity = discord.Activity(
                    name="", type=discord.ActivityType.unknown)
                await G.BOT.change_presence(status=stat)

    # bot activity
    @bot.command()
    async def activity(self, ctx: commands.Context,
                       activity_type: str, *, activity_text: str):
        type_map = {
            'playing': discord.ActivityType.playing,
            'streaming': discord.ActivityType.streaming,
            'listening': discord.ActivityType.listening,
            'watching': discord.ActivityType.watching,
        }
        if activity_type in type_map:
            new_activity = discord.Activity(name=activity_text,
                                            type=type_map[activity_type])
            if new_activity != G.CFG.activity:
                G.CFG.activity = new_activity
                G.CFG.status = ""
                await G.BOT.change_presence(activity=new_activity)
