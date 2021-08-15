from discord.ext import commands

from .exception_cog import ExceptionCog
from .functions import *
from ..globals import G


class CalculatorCog(ExceptionCog):

    @commands.group(invoke_without_command=True)
    async def calculator(self, ctx: commands.Context):
        pass

    # calculator timeout
    @calculator.command()
    async def timeout(self, ctx: commands.Context, seconds=None):
        raise_if_not_me(ctx)
        if seconds is None:
            text = f"Calculator timeout:" \
                   + f" {G.CFG.calculator_timeout} seconds"
            await ctx.send(text, reference=ctx.message)
        else:
            try:
                secs = claim(float(seconds), 0.1, 5)
                if secs != G.CFG.calculator_timeout:
                    G.CFG.calculator_timeout = secs
                text = f"New calculator timeout:" \
                       + f" {G.CFG.calculator_timeout} seconds"
                await ctx.send(text, reference=ctx.message)
            except ValueError:
                pass
