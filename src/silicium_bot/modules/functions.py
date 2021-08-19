import asyncio

from .. import globals


def raise_if_not_me(ctx):
    if ctx.author.id != globals.G.MY_ID:
        raise Exception(f"{ctx.message.content} ADMIN ONLY!")


def send_sync_func(ctx, message):
    def wrapper():
        loop = asyncio.get_running_loop()
        if loop.is_running():
            loop.create_task(ctx.send(message, reference=ctx.message))
    return wrapper


def claim(value, min, max):
    if value < min:
        return min
    if value > max:
        return max
    return value
