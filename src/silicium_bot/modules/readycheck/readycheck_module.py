from discord.ext import commands

from discord.utils import get
import discord
from silicium_bot.store import Store
from silicium_bot.constants import Constants
from ..module_base import ModuleBase

class ReadyCheckModule(ModuleBase):
    def __init__(self, bot):
        super().__init__(bot)

    async def on_reaction_add(self, reaction: discord.Reaction, user: discord.User):
        await self._update_message(reaction.message)

    async def on_reaction_remove(self, reaction: discord.Reaction, user: discord.User):
        await self._update_message(reaction.message)

    async def on_message(self, message: discord.Message):
        if message.author == self.bot.user and len(message.embeds) > 0:
            embed: discord.Embed = message.embeds[0]
            if embed.title == Constants.readycheck_embed_title:
                await message.add_reaction(Constants.decline_emoji)
                await message.add_reaction(Constants.accept_emoji)
                await message.add_reaction(Constants.undecide_emoji)
                return True
        return False

    async def _update_message(self, message: discord.Message):
        if message.author != self.bot.user or len(message.embeds) == 0:
            return
        embed: discord.Embed = message.embeds[0]
        if embed.title != Constants.readycheck_embed_title:
            return
        reactions = message.reactions
        accept_reacts = [r for r in reactions if r.emoji == Constants.accept_emoji]
        decline_reacts = [r for r in reactions if r.emoji == Constants.decline_emoji]
        undecide_reacts = [r for r in reactions if r.emoji == Constants.undecide_emoji]
        accept_users: set[discord.User] = set()
        decline_users: set[discord.User] = set()
        undecide_users: set[discord.User] = set()
        for react in accept_reacts:
            async for user in react.users():
                if user != self.bot.user:
                    accept_users.add(user)
        for react in decline_reacts:
            async for user in react.users():
                if user != self.bot.user:
                    decline_users.add(user)
        for react in undecide_reacts:
            async for user in react.users():
                if user != self.bot.user:
                    undecide_users.add(user)
        embed.clear_fields()
        accept_value = '<empty>'
        decline_value = '<empty>'
        undecide_value = '<empty>'
        if len(accept_users) > 0:
            accept_value = '\n'.join([u.mention for u in list(accept_users)])
        if len(decline_users) > 0:
            decline_value = '\n'.join([u.mention for u in list(decline_users)])
        if len(decline_users) > 0:
            undecide_value = '\n'.join([u.mention for u in list(undecide_users)])
        embed.add_field(name='Ready', inline=False, value=accept_value)
        embed.add_field(name='Not ready', inline=False, value=decline_value)
        embed.add_field(name='A bit later', inline=False, value=undecide_value)
        await message.edit(embed=embed)

    @commands.command(aliases=['rc'])
    async def readycheck(self, ctx: commands.Context, *, text=''):
        embed = discord.Embed(description=text, title=Constants.readycheck_embed_title)
        embed.add_field(name='Ready', value='<empty>', inline=False)
        embed.add_field(name='Not ready', value='<empty>', inline=False)
        embed.add_field(name='A bit later', value='<empty>', inline=False)
        await ctx.send(embed=embed)

    # def create_embed(ready: str[])
