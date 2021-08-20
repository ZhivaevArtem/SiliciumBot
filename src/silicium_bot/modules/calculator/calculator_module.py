import re

from discord.ext import commands

from silicium_bot.modules.module_base import ModuleBase
from silicium_bot.globals import G


class CalculatorModule(ModuleBase):
    async def on_message(self, message) -> bool:
        content = message.content.strip()
        if re.match(r'^.*[0-9]+.*$', content) \
           and re.match(r'^[0-9/*\-+. \t\n()]+$', content) \
           and not re.match(r'^[\-+]*[ \t]*[0-9]*\.?[0-9]*$', content):
            n = self.invoke_timeout(eval, G.CFG.calculator_timeout, content)
            if type(n) in (int, float):
                await message.channel.send(n, reference=message)
            else:
                await message.channel.send("Очень сложно, давай-ка сам",
                                           reference=message)
            return True
        return False

    @commands.group(invoke_without_command=True)
    async def calculator(self, ctx: commands.Context):
        pass

    # calculator timeout
    @calculator.command()
    async def timeout(self, ctx: commands.Context, seconds=None):
        self.raise_if_not_me(ctx)
        if seconds is None:
            text = f"Calculator timeout:" \
                   + f" {G.CFG.calculator_timeout} seconds"
            await ctx.send(text, reference=ctx.message)
        else:
            try:
                secs = self.claim(float(seconds), 0.1, 5)
                if secs != G.CFG.calculator_timeout:
                    G.CFG.calculator_timeout = secs
                text = f"New calculator timeout:" \
                       + f" {G.CFG.calculator_timeout} seconds"
                await ctx.send(text, reference=ctx.message)
            except ValueError:
                pass

