import sys

from discord.ext import commands

from silicium_bot.modules.module_base import ModuleBase


class DeprecatedModule(ModuleBase):
    @commands.command()
    async def kill(self, ctx: commands.Context):
        sys.exit(-2)
