from discord.ext import commands

from ..module_base import ModuleBase
from ...globals import G


class InfoModule(ModuleBase):
    # github
    @commands.command()
    async def github(self, ctx: commands.Context):
        await ctx.send(G.GITHUB, reference=ctx.message)

    # version
    @commands.command()
    async def version(self, ctx):
        await ctx.send(G.VERSION, reference=ctx.message)

    # help
    @commands.command()
    async def help(self, ctx):
        help_string = """
`bot activity {listening|playing|streaming|watching} <text: str>`
`bot status {dnd|idle|invisible|online}`
`jokes`
`jokes add <trigger: str> <react: str>`
`jokes remove [triggers...]`
`shiki users [{add|remove} [usernames...: str]]`
`help`
`version`
`github`
Admin commands:
`bot prefix [prefix: str]`
`calculator timeout [seconds: float]`
`config`
`jokes truncate`
`shiki [{start|stop|restart}]`
`shiki interval [seconds: int]`
`shiki limit [count: int]`
`shiki users truncate`
`shiki usechannel`
    """.strip()
        await ctx.send(help_string, reference=ctx.message)

