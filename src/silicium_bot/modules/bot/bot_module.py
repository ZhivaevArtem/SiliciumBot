import discord
from discord.ext import commands

from silicium_bot.store import Store
from ..module_base import ModuleBase


class BotModule(ModuleBase):
    def __init__(self, bot):
        super().__init__(bot)

    async def on_ready(self):
        if Store.bot_activity_type.value != discord.ActivityType.unknown:
            activity = discord.Activity(type=Store.bot_activity_type.value,
                                        name=Store.bot_activity_text.value)
            await self.bot.change_presence(activity=activity)
        elif Store.bot_status.value != discord.Status.online:
            await self.bot.change_presence(status=Store.bot_status.value)
        if self.bot.command_prefix != Store.bot_prefix.value:
            self.bot.command_prefix = Store.bot_prefix.value

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
            if Store.bot_prefix.value != new_prefix:
                self.bot.command_prefix = new_prefix
                Store.bot_prefix.value = new_prefix
            await ctx.send(f'New prefix: "{new_prefix}"',
                           reference=ctx.message)

    # bot status
    @bot.command()
    async def status(self, ctx: commands.Context, new_status=None):
        if new_status is None:
            await ctx.send(f"{Store.bot_status.value}", reference=ctx.message)
        status_map = {
            'online': discord.Status.online,
            'invisible': discord.Status.invisible,
            'idle': discord.Status.idle,
            'dnd': discord.Status.dnd
        }
        if new_status in status_map:
            status = status_map[new_status]
            if Store.bot_status.value != status:
                Store.bot_status.value = status
                Store.bot_activity_type.value = discord.ActivityType.unknown
                Store.bot_activity_text.value = ""
                await self.bot.change_presence(status=status)

    # bot activity
    @bot.group(invoke_without_command=True)
    async def activity(self, ctx: commands.Context,
                       activity_type=None, *, activity_text=None):
        if activity_type is None and activity_text is None:
            type_str_map = {
                discord.ActivityType.playing:   'playing',
                discord.ActivityType.listening: 'listening',
                discord.ActivityType.watching:  'watching'
            }
            if Store.bot_activity_type.value in type_str_map:
                text = f"{type_str_map[Store.bot_activity_type.value]}:" \
                       + f" {Store.bot_activity_text.value}"
            else:
                text = "There is no activity"
            await ctx.send(text, reference=ctx.message)
            return
        type_map = {
            'playing': discord.ActivityType.playing,
            'listening': discord.ActivityType.listening,
            'watching': discord.ActivityType.watching
        }
        if activity_type in type_map:
            update = 0 == 1
            if Store.bot_activity_type.value != type_map[activity_type]:
                Store.bot_activity_type.value = type_map[activity_type]
                update = True
            if Store.bot_activity_text.value != activity_text:
                Store.bot_activity_text.value = activity_text
                update = True
            if update:
                activity = discord.Activity(type=Store.bot_activity_type.value,
                                            name=Store.bot_activity_text.value)
                await self.bot.change_presence(activity=activity)
