from discord.ext import commands

from silicium_bot.modules.shiki.loop_requests import LoopRequestsTask
from silicium_bot.store import Store
from ..module_base import ModuleBase


class ShikiModule(ModuleBase):
    def __init__(self, bot):
        super().__init__(bot)
        self.loop_requests_task = None
        self.shiki_client = None

    async def on_ready(self):
        self.loop_requests_task = LoopRequestsTask(self.bot)
        self.shiki_client = self.loop_requests_task.shiki_client
        if Store.is_daemon_running.value:
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
            text = f"Shiki request limit: {Store.shiki_request_limit.value}"
            await ctx.send(text, reference=ctx.message)
            return
        try:
            lim = self.claim(int(new_limit), 2, 50)
            if lim != Store.shiki_request_limit.value:
                Store.shiki_request_limit.value = lim
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

    @shiki.group(invoke_without_command=True)
    async def cache(self, ctx: commands.Context):
        pass

    # shiki cache truncate
    @cache.command("truncate")
    async def cache_truncate(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        self.shiki_client.clear_cache()

    @cache.command("size")
    async def cache_size(self, ctx: commands.Context):
        in_bytes = self.shiki_client.cache_size()
        in_kbytes = in_bytes // 1024
        await ctx.send(f"{in_kbytes} KB", reference=ctx.message)

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
            text = f"Shiki interval: {Store.daemon_interval.value}" \
                   + " seconds"
            await ctx.send(text, reference=ctx.message)
            return
        try:
            secs = self.claim(int(new_interval), 2, 3600)
            if secs != Store.daemon_interval.value:
                Store.daemon_interval.value = secs
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
        Store.is_daemon_running.value = True

    # shiki daemon stop
    @daemon.command()
    async def stop(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if not self.loop_requests_task.is_running():
            await ctx.send("Daemon has already been stopped")
            return
        self.loop_requests_task.stop(self.send_sync_func(ctx,
                                                         "Daemon stopped"))
        Store.is_daemon_running.value = False

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
        text = ", ".join(Store.shiki_usernames.value)
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
        to_add = [u for u in args if u not in Store.shiki_usernames.value]
        to_add = list(set(to_add))
        if len(to_add) == 0:
            await ctx.send("There are no users to add", reference=ctx.message)
            return
        Store.shiki_usernames.append(to_add)
        text = ", ".join(to_add)
        await ctx.send(f"Users added: {text}", reference=ctx.message)

    # shiki users remove
    @users.command()
    async def remove(self, ctx: commands.Context, *args):
        if len(args) == 0:
            await ctx.send("At least one username must be passed",
                           reference=ctx.message)
            return
        to_del = [u for u in args if u in Store.shiki_usernames.value]
        to_del = list(set(to_del))
        if len(to_del) == 0:
            await ctx.send("There are no users to remove",
                           reference=ctx.message)
            return
        Store.shiki_usernames.remove(to_del)
        text = ", ".join(to_del)
        if text:
            await ctx.send(f"Users removed: {text}", reference=ctx.message)
        else:
            await ctx.send("There are no users", reference=ctx.message)

    # shiki users truncate
    @users.command("truncate")
    async def users_truncate(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if len(Store.shiki_usernames.value) > 0:
            Store.shiki_usernames.value = []
        await ctx.send("Users truncated", reference=ctx.message)

    # shiki usechannel
    @shiki.group(invoke_without_command=True)
    async def notifchannel(self, ctx: commands.Context):
        text = f"{Store.notification_channel.value.guild}"
        text += f" > {Store.notification_channel.value.category}"
        text += f" > {Store.notification_channel.value.name}"
        await ctx.send(text, reference=ctx.message)

    @notifchannel.command()
    async def this(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if ctx.channel.id != Store.notification_channel.value.id:
            Store.notification_channel.value = ctx.channel
        await ctx.send("This channel will be used for notifications",
                       reference=ctx.message)
