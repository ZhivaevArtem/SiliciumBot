import sys

from discord.ext import commands

from silicium_bot.modules import ModuleBase


class DeprecatedModule(ModuleBase):
    def __init__(self, bot):
        super().__init__(bot)

    @commands.command()
    async def kill(self, ctx: commands.Context):
        sys.exit(-2)
