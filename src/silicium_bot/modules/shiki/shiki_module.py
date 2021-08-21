from discord.ext import commands

from silicium_bot.modules.shiki.loop_requests import LoopRequestsTask
from silicium_bot.modules.module_base import ModuleBase
from silicium_bot.globals import G


class ShikiModule(ModuleBase):
    def __init__(self):
        self.loop_requests_task = None

    async def on_ready(self):
        self.loop_requests_task = LoopRequestsTask()
        if G.CFG.is_worker_running:
            self.loop_requests_task.start()

    @commands.group(invoke_without_command=True)
    async def shiki(self, ctx: commands.Context):
        pass

    @shiki.group(invoke_without_command=True)
    async def request(self, ctx: commands.Context):
        pass

    # shiki request limit
    @request.command()
    async def limit(self, ctx: commands.Context, new_limit=None):
        self.raise_if_not_me(ctx)
        if new_limit is None:
            text = f"Shiki request limit: {G.CFG.history_request_limit}"
            await ctx.send(text, reference=ctx.message)
            return
        try:
            lim = self.claim(int(new_limit), 2, 50)
            if lim != G.CFG.history_request_limit:
                G.CFG.history_request_limit = lim
            text = f"New shiki request limit: {lim}"
            await ctx.send(text, reference=ctx.message)
        except ValueError:
            pass

    # shiki daemon
    @shiki.group(invoke_without_command=True)
    async def daemon(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        await ctx.send(
            "Daemon is running" if self.loop_requests_task.is_running()
            else "Daemon is not running", reference=ctx.message)

    # shiki daemon status
    @daemon.command()
    async def status(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        await ctx.send(
            "Daemon is running" if self.loop_requests_task.is_running()
            else "Daemon is not running", reference=ctx.message)

    # shiki daemon interval
    @daemon.command()
    async def interval(self, ctx: commands.Context, new_interval=None):
        self.raise_if_not_me(ctx)
        if new_interval is None:
            text = f"Shiki interval: {G.CFG.loop_requests_interval}" \
                   + " seconds"
            await ctx.send(text, reference=ctx.message)
            return
        try:
            secs = self.claim(int(new_interval), 2, 3600)
            if secs != G.CFG.loop_requests_interval:
                G.CFG.loop_requests_interval = secs
                if self.loop_requests_task.is_running():
                    self.loop_requests_task.restart()
            text = f"New shiki interval: {secs} seconds"
            await ctx.send(text, reference=ctx.message)
        except ValueError:
            pass

    # shiki daemon start
    @daemon.command()
    async def start(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if self.loop_requests_task.is_running():
            await ctx.send("Daemon is already running",
                           reference=ctx.message)
            return
        self.loop_requests_task.start(self.send_sync_func(ctx,
                                                          "Daemon started"))
        G.CFG.is_worker_running = True

    # shiki daemon stop
    @daemon.command()
    async def stop(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if not self.loop_requests_task.is_running():
            await ctx.send("Daemon has already been stopped")
            return
        self.loop_requests_task.stop(self.send_sync_func(ctx,
                                                         "Daemon stopped"))
        G.CFG.is_worker_running = False

    # shiki daemon restart
    @daemon.command()
    async def restart(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if not self.loop_requests_task.is_running():
            await ctx.send("Daemon is not running", reference=ctx.message)
            return
        self.loop_requests_task.restart(
            self.send_sync_func(ctx, "Daemon restarted"))

    # shiki users
    @shiki.group(invoke_without_command=True)
    async def users(self, ctx: commands.Context):
        text = ", ".join(G.CFG.usernames)
        if text:
            await ctx.send(text, reference=ctx.message)
        else:
            await ctx.send("There are no users", reference=ctx.message)

    # shiki users add
    @users.command()
    async def add(self, ctx: commands.Context, *args):
        if len(args) == 0:
            await ctx.send("At least one username must be passed",
                           reference=ctx.message)
            return
        to_add = [u for u in args if u not in G.CFG.usernames]
        to_add = list(set(to_add))
        if len(to_add) == 0:
            await ctx.send("There are no users to add", reference=ctx.message)
            return
        G.CFG.add_users(to_add)
        text = ", ".join(to_add)
        await ctx.send(f"Users added: {text}", reference=ctx.message)

    # shiki users remove
    @users.command()
    async def remove(self, ctx: commands.Context, *args):
        if len(args) == 0:
            await ctx.send("At least one username must be passed",
                           reference=ctx.message)
            return
        to_del = [u for u in args if u in G.CFG.usernames]
        to_del = list(set(to_del))
        if len(to_del) == 0:
            await ctx.send("There are no users to remove",
                           reference=ctx.message)
            return
        G.CFG.delete_users(to_del)
        text = ", ".join(to_del)
        if text:
            await ctx.send(f"Users removed: {text}", reference=ctx.message)
        else:
            await ctx.send("There are no users", reference=ctx.message)

    # shiki users truncate
    @users.command()
    async def truncate(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if len(G.CFG.usernames) > 0:
            G.CFG.truncate_users()
        await ctx.send("Users truncated", reference=ctx.message)

    # shiki usechannel
    @shiki.group(invoke_without_command=True)
    async def notifchannel(self, ctx: commands.Context):
        text = f"{G.CFG.notification_channel.guild}"
        text += f" > {G.CFG.notification_channel.category}"
        text += f" > {G.CFG.notification_channel.name}"
        await ctx.send(text, reference=ctx.message)

    @notifchannel.command()
    async def this(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if ctx.channel.id != G.CFG.notification_channel.id:
            G.CFG.notification_channel = ctx.channel
        await ctx.send("This channel will be used for notifications",
                       reference=ctx.message)
