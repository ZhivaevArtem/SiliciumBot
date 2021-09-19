import sys

from discord.ext import commands

from ..module_base import ModuleBase


class DeprecatedModule(ModuleBase):
    def __init__(self, bot):
        super().__init__(bot)

    @commands.command()
    async def kill(self, ctx: commands.Context):
        sys.exit(-2)

    @commands.command()
    async def throw(self, ctx: commands.Context, text=""):
        raise Exception(text)
