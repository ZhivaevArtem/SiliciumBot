from discord.ext import commands

from .exception_cog import ExceptionCog
from .functions import *
from ..globals import G


class ShikiCog(ExceptionCog):

    @commands.group(invoke_without_command=True)
    async def shiki(self, ctx: commands.Context):
        pass

    # shiki status
    @shiki.command("status")
    async def shiki_status(self, ctx: commands.Context):
        raise_if_not_me(ctx)
        await ctx.send("Worker is running" if G.LOOP_REQUEST_TASK.is_running()
                       else "Worker is not running", reference=ctx.message)

    # shiki interval
    @shiki.command()
    async def interval(self, ctx: commands.Context, new_interval=None):
        raise_if_not_me(ctx)
        if new_interval is None:
            text = f"Shiki interval: {G.CFG.loop_requests_interval}" \
                   + " seconds"
            await ctx.send(text, reference=ctx.message)
            return
        try:
            secs = claim(int(new_interval), 2, 3600)
            if secs != G.CFG.loop_requests_interval:
                G.CFG.loop_requests_interval = secs
                if G.LOOP_REQUEST_TASK.is_running():
                    G.LOOP_REQUEST_TASK.restart()
            text = f"New shiki interval: {secs} seconds"
            await ctx.send(text, reference=ctx.message)
        except ValueError:
            pass

    # shiki limit
    @shiki.command()
    async def limit(self, ctx: commands.Context, new_limit=None):
        raise_if_not_me(ctx)
        if new_limit is None:
            text = f"Shiki request limit: {G.CFG.history_request_limit}"
            await ctx.send(text, reference=ctx.message)
            return
        try:
            lim = claim(int(new_limit), 2, 50)
            if lim != G.CFG.history_request_limit:
                G.CFG.history_request_limit = lim
            text = f"New shiki request limit: {lim}"
            await ctx.send(text, reference=ctx.message)
        except ValueError:
            pass

    # shiki start
    @shiki.command()
    async def start(self, ctx: commands.Context):
        raise_if_not_me(ctx)
        if G.LOOP_REQUEST_TASK.is_running():
            await ctx.send("Worker is already running",
                           reference=ctx.message)
            return
        G.LOOP_REQUEST_TASK.start(send_sync_func(ctx, "Worker started"))
        G.CFG.is_worker_running = True

    # shiki stop
    @shiki.command()
    async def stop(self, ctx: commands.Context):
        raise_if_not_me(ctx)
        if not G.LOOP_REQUEST_TASK.is_running():
            await ctx.send("Worker has already been stopped")
            return
        G.LOOP_REQUEST_TASK.stop(send_sync_func(ctx, "Worker stopped"))
        G.CFG.is_worker_running = False

    # shiki restart
    @shiki.command()
    async def restart(self, ctx: commands.Context):
        raise_if_not_me(ctx)
        if not G.LOOP_REQUEST_TASK.is_running():
            await ctx.send("Worker is not running")
            return
        G.LOOP_REQUEST_TASK.restart(send_sync_func(ctx, "Worker restarted"))

    # shiki users
    @shiki.group(invoke_without_command=True)
    async def users(self, ctx: commands.Context):
        text = ", ".join(G.CFG.usernames)
        if text:
            await ctx.send(text, reference=ctx.message)
        else:
            await ctx.send("There are no users", reference=ctx.message)

    # shiki users add
    @users.command("add")
    async def add_users(self, ctx: commands.Context, *args):
        if len(args) == 0:
            await ctx.send("At least one username must be passed",
                           reference=ctx.message)
            return
        to_add = [u for u in args if u not in G.CFG.usernames]
        if len(to_add) == 0:
            await ctx.send("There are no users to add")
            return
        G.CFG.add_users(to_add)
        text = ", ".join(to_add)
        await ctx.send(f"Users added: {text}", reference=ctx.message)

    # shiki users remove
    @users.command("remove")
    async def remove_users(self, ctx: commands.Context, *args):
        if len(args) == 0:
            await ctx.send("At least one username must be passed",
                           reference=ctx.message)
            return
        to_del = [u for u in args if u in G.CFG.usernames]
        if len(to_del) == 0:
            await ctx.send("There are no users to remove")
            return
        G.CFG.delete_users(to_del)
        text = ", ".join(to_del)
        if text:
            await ctx.send(f"Users removed: {text}", reference=ctx.message)
        else:
            await ctx.send("There are no users", reference=ctx.message)

    # shiki users truncate
    @users.command("truncate")
    async def truncate_users(self, ctx: commands.Context):
        raise_if_not_me(ctx)
        if len(G.CFG.usernames) > 0:
            G.CFG.truncate_users()
        await ctx.send("Users truncated", reference=ctx.message)

    # shiki usechannel
    @shiki.command()
    async def usechannel(self, ctx: commands.Context):
        raise_if_not_me(ctx)
        if ctx.channel.id != G.CFG.notification_channel.id:
            G.CFG.notification_channel = ctx.channel
        await ctx.send("This channel will be used for notifications",
                       reference=ctx.message)
