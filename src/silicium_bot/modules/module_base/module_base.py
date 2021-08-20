import asyncio
import multiprocessing
import traceback

from discord.ext import commands

from silicium_bot.globals import G


class ModuleBase(commands.Cog):
    async def on_message(self, message) -> bool:
        return False

    async def on_ready(self):
        pass

    def raise_if_not_me(self, ctx):
        if G.MY_USER_ID != ctx.author.id:
            raise Exception(f"ADMIN ONLY: {ctx.author}: {ctx.message.content}")

    def invoke_timeout(self, func, seconds, *args, **kwargs):
        m = multiprocessing.Manager()
        dic = m.dict()
        dic['result'] = None

        def wrapper(func, dic, *args, **kwargs):
            dic['result'] = func(*args, **kwargs)

        p = multiprocessing.Process(target=wrapper,
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
            print(f'Ignored exception caused by {ctx.author}')
            print(type(error))
            print(error)
            return
        try:
            raise error
        except Exception:
            print(traceback.format_exc())
