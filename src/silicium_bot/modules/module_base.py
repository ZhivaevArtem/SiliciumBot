import asyncio
import multiprocessing
import traceback

from discord.ext import commands

from silicium_bot.constants import Constants
from silicium_bot.logger import exception_logger as logger


def _wrapper(func, dic, *args, **kwargs):
    dic['result'] = func(*args, **kwargs)


class ModuleBase(commands.Cog):
    def __init__(self, bot: commands.Bot):
        self.bot = bot

    async def on_message(self, message) -> bool:
        return False

    async def on_ready(self):
        pass

    async def on_reaction_add(self, reaction, user):
        pass

    async def on_raw_reaction_remove(self, payload):
        pass

    async def on_reaction_remove(self, reaction, user):
        pass

    def raise_if_not_me(self, ctx):
        if Constants.my_discord_id != ctx.author.id:
            raise Exception(f"ADMIN ONLY: {ctx.author}: {ctx.message.content}")

    def invoke_timeout(self, func, seconds, *args, **kwargs):
        m = multiprocessing.Manager()
        dic = m.dict()
        dic['result'] = None
        p = multiprocessing.Process(target=_wrapper,
                                    args=(func, dic) + args, kwargs=kwargs)
        p.start()
        p.join(seconds)
        p.kill()
        return dic['result']

    def send_sync_func(self, ctx, message):
        def wrapper():
            loop = asyncio.get_running_loop()
            if loop.is_running():
                loop.create_task(ctx.send(message, reference=ctx.message))

        return wrapper

    def claim(self, value, min, max):
        if value < min:
            return min
        if value > max:
            return max
        return value

    @commands.Cog.listener()
    async def on_command_error(self, ctx, error):
        ignored = (commands.CommandNotFound,
                   commands.MissingRequiredArgument)
        if type(error) in ignored:
            return
        try:
            raise error
        except Exception:
            logger.log(traceback.format_exc())
