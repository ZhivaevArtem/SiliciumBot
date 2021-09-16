from discord.ext import commands

from silicium_bot.modules import ModuleBase
from silicium_bot.store import StaticStore


class InfoModule(ModuleBase):
    def __init__(self, bot):
        super().__init__(bot)

    # github
    @commands.command()
    async def github(self, ctx: commands.Context):
        await ctx.send(StaticStore.github, reference=ctx.message)

    # version
    @commands.command()
    async def version(self, ctx):
        await ctx.send(StaticStore.version, reference=ctx.message)

    # help
    @commands.command()
    async def help(self, ctx):
        help_string = """
`bot activity [{listening|playing|watching} <text: str>]`
`bot status [{dnd|idle|invisible|online}]`
`jokes [add <trigger: str> <react: str>]`
`jokes [remove [triggers...: str]]`
`shiki users [{add|remove} [usernames...: str]]`
`shiki notifchannel`
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
`shiki request limit [count: int]`
`shiki notifchannel this`
`shiki users [truncate]`
`shiki usechannel`
""".strip()
        try:
            self.raise_if_not_me(ctx)
            await ctx.send(admin_help_string, reference=ctx.message)
        except Exception:
            await ctx.send(help_string, reference=ctx.message)
