import asyncio
import traceback

import discord
from discord.ext import commands


def _claim(value, min, max):
    if value < min:
        return min
    if value > max:
        return max
    return value


def _send_sync_func(ctx, message):
    def wrapper():
        loop = asyncio.get_running_loop()
        if loop.is_running():
            loop.create_task(ctx.send(message, reference=ctx.message))
    return wrapper


class BotCog(commands.Cog):

    def __init__(self, bot: commands.Bot, cfg,
                 admin_id: int, loop, version: str, github: str):
        self._bot = bot
        self._CFG = cfg
        self._ADMIN_ID = admin_id
        self._LOOP = loop
        self._VERSION = version
        self._GITHUB = github

    def _raise_if_not_me(self, ctx: commands.Context):
        if ctx.author.id != self._ADMIN_ID:
            raise Exception(f"{ctx.message.content} ADMIN ONLY")

    @commands.group(invoke_without_command=True)
    async def bot(self, ctx: commands.Context):
        pass

    # bot prefix
    @bot.command()
    async def prefix(self, ctx: commands.Context, new_prefix=None):
        self._raise_if_not_me(ctx)
        if new_prefix is None:
            await ctx.send(f'Current prefix: "{self.bot.command_prefix}"',
                           reference=ctx.message)
        else:
            if self._CFG.prefix != new_prefix:
                self._bot.command_prefix = new_prefix
                self._CFG.prefix = new_prefix
            await ctx.send(f'New prefix: "{new_prefix}"',
                           reference=ctx.message)

    # bot status
    @bot.command()
    async def status(self, ctx: commands.Context, new_status: str):
        if new_status in ('online', 'invisible', 'idle', 'dnd'):
            stat = discord.Status(new_status)
            if self._CFG.status != stat:
                self._CFG.status = stat
                await self._bot.change_presence(status=stat)

    # bot activity
    @bot.command()
    async def activity(self, ctx: commands.Context,
                       activity_type: str, activity_text: str):
        type_map = {
            'playing':   discord.ActivityType.playing,
            'streaming': discord.ActivityType.streaming,
            'listening': discord.ActivityType.listening,
            'watching':  discord.ActivityType.watching,
        }
        if activity_type in type_map:
            new_activity = discord.Activity(name=activity_text,
                                            type=type_map[activity_type])
            if new_activity != self._CFG.activity:
                self._CFG.activity = new_activity
                await self._bot.change_presence(activity=new_activity)

    # config
    @commands.command()
    async def config(self, ctx: commands.Context):
        self._raise_if_not_me(ctx)
        await ctx.send(str(self._CFG), reference=ctx.message)

    @commands.group(invoke_without_command=True)
    async def calculator(self, ctx: commands.Context):
        pass

    # calculator timeout
    @calculator.command()
    async def timeout(self, ctx: commands.Context, seconds=None):
        self._raise_if_not_me(ctx)
        if seconds is None:
            text = f"Calculator timeout:" \
                   + f" {self._CFG.calculator_timeout} seconds"
            await ctx.send(text, reference=ctx.message)
        else:
            try:
                secs = _claim(float(seconds), 0.1, 5)
                if secs != self._CFG.calculator_timeout:
                    self._CFG.calculator_timeout = secs
                text = f"New calculator timeout:" \
                       + f" {self._CFG.calculator_timeout} seconds"
                await ctx.send(text, reference=ctx.message)
            except ValueError:
                pass

    @commands.group(invoke_without_command=True)
    async def shiki(self, ctx: commands.Context):
        pass

    # shiki status
    @shiki.command("status")
    async def shiki_status(self, ctx: commands.Context):
        self._raise_if_not_me(ctx)
        await ctx.send("Worker is running" if self._LOOP.is_running()
                       else "Worker is not running", reference=ctx.message)

    # shiki interval
    @shiki.command()
    async def interval(self, ctx: commands.Context, new_interval=None):
        self._raise_if_not_me(ctx)
        if new_interval is None:
            text = f"Shiki interval: {self._CFG.loop_requests_interval}" \
                   + " seconds"
            await ctx.send(text, reference=ctx.message)
            return
        try:
            secs = _claim(int(new_interval), 2, 3600)
            if secs != self._CFG.loop_requests_interval:
                self._CFG.loop_requests_interval = secs
                if self._LOOP.is_running():
                    self._LOOP.restart()
            text = f"New shiki interval: {secs} seconds"
            await ctx.send(text, reference=ctx.message)
        except ValueError:
            pass

    # shiki limit
    @shiki.command()
    async def limit(self, ctx: commands.Context, new_limit=None):
        self._raise_if_not_me(ctx)
        if new_limit is None:
            text = f"Shiki request limit: {self._CFG.history_request_limit}"
            await ctx.send(text, reference=ctx.message)
            return
        try:
            lim = _claim(int(new_limit), 2, 50)
            if lim != self._CFG.history_request_limit:
                self._CFG.history_request_limit = lim
            text = f"New shiki request limit: {lim}"
            await ctx.send(text, reference=ctx.message)
        except ValueError:
            pass

    # shiki start
    @shiki.command()
    async def start(self, ctx: commands.Context):
        self._raise_if_not_me(ctx)
        if self._LOOP.is_running():
            await ctx.send("Worker is already running", reference=ctx.message)
            return
        self._LOOP.start(_send_sync_func(ctx, "Worker started"))
        self._CFG.is_worker_running = True

    # shiki stop
    @shiki.command()
    async def stop(self, ctx: commands.Context):
        self._raise_if_not_me(ctx)
        if not self._LOOP.is_running():
            await ctx.send("Worker has already been stopped")
            return
        self._LOOP.stop(_send_sync_func(ctx, "Worker stopped"))
        self._CFG.is_worker_running = False

    # shiki restart
    @shiki.command()
    async def restart(self, ctx: commands.Context):
        self._raise_if_not_me(ctx)
        if not self._LOOP.is_running():
            await ctx.send("Worker is not running")
            return
        self._LOOP.restart(_send_sync_func(ctx, "Worker restarted"))

    # shiki users
    @shiki.group(invoke_without_command=True)
    async def users(self, ctx: commands.Context):
        text = ", ".join(self._CFG.usernames)
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
        to_add = [u for u in args if u not in self._CFG.usernames]
        if len(to_add) == 0:
            await ctx.send("There are no users to add")
            return
        self._CFG.add_users(to_add)
        text = ", ".join(to_add)
        await ctx.send(f"Users added: {text}", reference=ctx.message)

    # shiki users remove
    @users.command("remove")
    async def remove_users(self, ctx: commands.Context, *args):
        if len(args) == 0:
            await ctx.send("At least one username must be passed",
                           reference=ctx.message)
            return
        to_del = [u for u in args if u in self._CFG.usernames]
        if len(to_del) == 0:
            await ctx.send("There are no users to remove")
            return
        self._CFG.delete_users(to_del)
        text = ", ".join(to_del)
        if text:
            await ctx.send(f"Users removed: {text}", reference=ctx.message)
        else:
            await ctx.send("There are no users", reference=ctx.message)

    # shiki users truncate
    @users.command("truncate")
    async def truncate_users(self, ctx: commands.Context):
        self._raise_if_not_me(ctx)
        if len(self._CFG.usernames) > 0:
            self._CFG.truncate_users()
        await ctx.send("Users truncated", reference=ctx.message)

    # shiki usechannel
    @shiki.command()
    async def usechannel(self, ctx: commands.Context):
        self._raise_if_not_me(ctx)
        if ctx.channel.id != self._CFG.notification_channel.id:
            self._CFG.notification_channel = ctx.channel
        await ctx.send("This channel will be used for notifications",
                       reference=ctx.message)

    # jokes
    @commands.group(invoke_without_command=True)
    async def jokes(self, ctx: commands.Context):
        text = ", ".join([f"{j} -> {r}" for j, r in self._CFG.jokes.items()])
        if text:
            await ctx.send(text, reference=ctx.message)
        else:
            await ctx.send("There are no jokes")

    # jokes add
    @jokes.command("add")
    async def add_joke(self, ctx: commands.Context, message: str, react: str):
        if message not in self._CFG.jokes \
           or self._CFG.jokes[message] != react:
            self._CFG.add_joke(message, react)
        await ctx.send(f"Joke added: {message} -> {react}",
                       reference=ctx.message)

    # jokes remove
    @jokes.command("remove")
    async def remove_jokes(self, ctx: commands.Context, *args):
        if len(args) == 0:
            await ctx.send("At least one joke must be passed",
                           reference=ctx.message)
            return
        to_del = [j for j in args if j in self._CFG.jokes]
        if len(to_del) == 0:
            await ctx.send("There are no jokes to remove",
                           reference=ctx.message)
            return
        self._CFG.delete_jokes(to_del)
        text = ", ".join(to_del)
        await ctx.send(f"Jokes removed: {text}", reference=ctx.message)

    # jokes truncate
    @jokes.command("truncate")
    async def truncate_jokes(self, ctx: commands.Context):
        self._raise_if_not_me(ctx)
        if len(self._CFG.jokes) != 0:
            self._CFG.truncate_jokes()
        await ctx.send("Jokes truncated")

    # github
    @commands.command()
    async def github(self, ctx: commands.Context):
        await ctx.send(self._GITHUB, reference=ctx.message)

    # version
    @commands.command()
    async def version(self, ctx):
        await ctx.send(self._VERSION, reference=ctx.message)

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

    @commands.Cog.listener()
    async def on_command_error(self, ctx, error):
        ignored = (commands.CommandNotFound,
                   commands.MissingRequiredArgument)
        if type(error) in ignored:
            print('Ignored exception:')
            print(type(error))
            print(error)
            return
        try:
            raise error
        except Exception:
            print(traceback.format_exc())
