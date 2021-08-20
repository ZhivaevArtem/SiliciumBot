import discord
from discord.ext import commands

from silicium_bot.globals import G
from silicium_bot.modules.module_base import ModuleBase


class InfoModule(ModuleBase):
    # github
    @commands.command()
    async def github(self, ctx: commands.Context):
        await ctx.send(G.GITHUB, reference=ctx.message)

    # version
    @commands.command()
    async def version(self, ctx):
        await ctx.send(G.VERSION, reference=ctx.message)

    # config
    @commands.command()
    async def config(self, ctx):
        self.raise_if_not_me(ctx)
        await ctx.send(f'{G.CFG}', reference=ctx.message)

    # help
    @commands.command()
    async def help(self, ctx):
        help_string = """
`bot activity [{listening|playing|streaming|watching} <text: str>]`
`bot status [{dnd|idle|invisible|online}]`
`jokes [add <trigger: str> <react: str>]`
`jokes [remove [triggers...: str]]`
`shiki users [{add|remove} [usernames...: str]]`
`help`
`version`
`github`
""".strip()
        admin_help_string = help_string + "\n" + """
Admin commands:
`bot prefix [prefix: str]`
`calculator timeout [seconds: float]`
`config`
`jokes [truncate]`
`shiki daemon [status]`
`shiki daemon [{start|stop|restart}]`
`shiki daemon interval [seconds: int]`
`shiki daemon limit [count: int]`
`shiki users [truncate]`
`shiki usechannel`
""".strip()
        try:
            self.raise_if_not_me(ctx)
            await ctx.send(admin_help_string, reference=ctx.message)
        except Exception:
            await ctx.send(help_string, reference=ctx.message)
