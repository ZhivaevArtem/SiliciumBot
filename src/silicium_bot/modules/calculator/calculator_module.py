import re

from discord.ext import commands

from silicium_bot.modules.module_base import ModuleBase
from silicium_bot.store import Store


class CalculatorModule(ModuleBase):
    def __init__(self, bot):
        super().__init__(bot)

    async def on_message(self, message) -> bool:
        content = message.content.strip()
        if re.match(r'^.*[0-9]+.*$', content) \
           and re.match(r'^[0-9/*^\-+. \t\n()]+$', content) \
           and not re.match(r'^[\-+]*[ \t]*[0-9]*\.?[0-9]*$', content):
            content = content.replace('^', '**')
            n = self.invoke_timeout(eval, Store.calculator_timeout.value,
                                    content)
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
                   + f" {Store.calculator_timeout.value} seconds"
            await ctx.send(text, reference=ctx.message)
        else:
            try:
                secs = self.claim(float(seconds), 0.1, 5)
                if secs != Store.calculator_timeout.value:
                    Store.calculator_timeout.value = secs
                text = f"New calculator timeout:" \
                       + f" {Store.calculator_timeout.value} seconds"
                await ctx.send(text, reference=ctx.message)
            except ValueError:
                pass

