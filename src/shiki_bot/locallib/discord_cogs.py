import asyncio

import discord
from discord.ext import commands


def _claim(value, min, max):
    if value < min:
        return min
    if value > max:
        return max
    return value


class BotCog(commands.Cog):

    def __init__(self, bot: commands.Bot, cfg, admin_id, loop, version):
        self._bot = bot
        self._CFG = cfg
        self._ADMIN_ID = admin_id
        self._LOOP = loop
        self._VERSION = version

    def _is_me(self, ctx: commands.Context):
        return self._ADMIN_ID == ctx.author.id

    @commands.group()
    async def bot(self, ctx: commands.Context):
        pass

    @bot.command()
    async def prefix(self, ctx: commands.Context, new_prefix=None):
        """Change command prefix (not available)"""
        if not self._is_me(ctx):
            return
        if new_prefix is None:
            await ctx.send(self.bot.command_prefix, reference=ctx.message)
        elif self._is_me(ctx):
            if self._bot.command_prefix == new_prefix:
                return
            self._bot.command_prefix = new_prefix
            await ctx.send(f"Prefix changed: {new_prefix}",
                           reference=ctx.message)
            self._CFG.prefix = new_prefix

    @bot.command()
    async def status(self, ctx: commands.Context, new_status):
        """Change bot status"""
        if new_status in ['online', 'invisible', 'idle', 'dnd']:
            stat = discord.Status(new_status)
            if self._CFG.status == stat:
                return
            await self._bot.change_presence(status=stat)
            self._CFG.status = stat

    @bot.command()
    async def activity(self, ctx: commands.Context,
                       activity_type, activity_text):
        """Change bot activity"""
        type_map = {
            'playing':   discord.ActivityType.playing,
            'streaming': discord.ActivityType.streaming,
            'listening': discord.ActivityType.listening,
            'watching':  discord.ActivityType.watching,
        }
        if activity_type in type_map:
            new_activity = discord.Activity(name=activity_text,
                                            type=type_map[activity_type])
            if new_activity == self._CFG.activity:
                return
            await self._bot.change_presence(activity=new_activity)
            self._CFG.activity = new_activity

    @commands.command()
    async def config(self, ctx: commands.Context):
        """Show config (not available)"""
        if not self._is_me(ctx):
            return
        await ctx.send(str(self._CFG), reference=ctx.message)

    @commands.group()
    async def calculator(self, ctx: commands.Context):
        pass

    @calculator.command()
    async def timeout(self, ctx: commands.Context, seconds):
        """Change max time for every math expression (not available)"""
        if not self._is_me(ctx):
            return
        try:
            secs = float(seconds)
            if secs == self._CFG.calculator_timeout:
                return
            self._CFG.calculator_timeout = secs
        except ValueError:
            pass

    @commands.group(invoke_without_command=True)
    async def shiki(self, ctx: commands.Context):
        """Get worker status (not available)"""
        if not self._is_me(ctx):
            return
        await ctx.send("Worker is running" if self._LOOP.is_running() else
                       "Worker is not running", reference=ctx.message)

    @shiki.command()
    async def interval(self, ctx: commands.Context, new_interval=None):
        """Set worker interval (not available)"""
        if not self._is_me(ctx):
            return
        if new_interval is None:
            await ctx.send(f"Interval: {self._CFG.loop_requests_interval} "
                           + "seconds",
                           reference=ctx.message)
            return
        try:
            secs = _claim(int(new_interval), 2, 3600)
            if secs != self._CFG.loop_requests_interval:
                self._CFG.loop_requests_interval = secs
            if self._LOOP.is_running():
                if secs != self._CFG.loop_requests_interval:
                    self._LOOP.restart(lambda: asyncio.get_running_loop()
                                       .create_task(
                        ctx.send(
                            f"Worker restarted with interval: {secs} seconds",
                            reference=ctx.message)))
                else:
                    await ctx.send(
                        f"Worker restarted with interval: {secs} seconds",
                        reference=ctx.message)
            else:
                await ctx.send(f"Interval: {secs} seconds",
                               reference=ctx.message)
        except ValueError:
            pass

    @shiki.command()
    async def limit(self, ctx: commands.Context, new_limit):
        """Set query limit for /api/users/:id/history endpoint"""
        if not self._is_me(ctx):
            return
        try:
            lim = _claim(int(new_limit), 2, 50)
            if lim == self._CFG.history_request_limit:
                return
            self._CFG.history_request_limit = lim
        except ValueError:
            pass

    @shiki.command()
    async def start(self, ctx):
        """Start worker"""
        if not self._is_me(ctx):
            return
        if self._LOOP.is_running():
            return
        self._LOOP.start(lambda: asyncio.get_running_loop().create_task(
            ctx.send("Worker started", reference=ctx.message)))
        self._CFG.is_worker_running = True

    @shiki.command()
    async def stop(self, ctx):
        """Stop worker"""
        if not self._is_me(ctx):
            return
        if not self._LOOP.is_running():
            return
        self._LOOP.stop(lambda: asyncio.get_running_loop().create_task(
            ctx.send("Worker stopped", reference=ctx.message)))
        self._CFG.is_worker_running = False

    @shiki.command()
    async def restart(self, ctx):
        """Restart worker"""
        if not self._is_me(ctx):
            return
        if not self._LOOP.is_running():
            return
        self._LOOP.restart(lambda: asyncio.get_running_loop().create_task(
            ctx.send("Worker restarted", reference=ctx.message)))

    @shiki.group(invoke_without_command=True)
    async def users(self, ctx: commands.Context):
        """Show target users"""
        mes = ", ".join(self._CFG.usernames)
        if mes:
            await ctx.send(mes, reference=ctx.message)
        else:
            await ctx.send("User list is empty", reference=ctx.message)

    @users.command("add")
    async def add_users(self, ctx: commands.Context, *args):
        """Add target users"""
        if len(args) > 0:
            self._CFG.add_users(args)
        await ctx.send(", ".join(self._CFG.usernames), reference=ctx.message)

    @users.command("remove")
    async def remove_users(self, ctx: commands.Context, *args):
        """Remove target users"""
        if len(args) > 0:
            self._CFG.remove_users(args)
        await ctx.send(", ".join(self._CFG.usernames), reference=ctx.message)

    @users.command("clean")
    async def truncate_users(self, ctx: commands.Context):
        """Remove all target users (not available)"""
        if not self._is_me(ctx):
            return
        if len(self._CFG.usernames) == 0:
            return
        self._CFG.truncate_users()
        await ctx.send("User list is empty", reference=ctx.message)

    @users.command()
    async def usechannel(self, ctx: commands.Context):
        """Use this message channel for notifications (not available)"""
        if not self._is_me(ctx):
            return
        if ctx.channel.id == self._CFG.notification_channel.id:
            return
        self._CFG.notification_channel = ctx.channel

    @commands.group()
    async def jokes(self, ctx):
        """Show jokes"""
        mes = ", ".join([f"{j} -> {r}" for j, r in self._CFG.jokes.items()])
        if mes:
            await ctx.send(mes, reference=ctx.message)
        else:
            await ctx.send("Jokes list is empty")

    @jokes.command("add")
    async def add_joke(self, ctx, message, react):
        """Add new joke"""
        self._CFG.add_joke(message, react)
        mes = ", ".join([f"{j} -> {r}" for j, r in self._CFG.jokes.items()])
        if mes:
            await ctx.send(mes, reference=ctx.message)
        else:
            await ctx.send("Jokes list is empty")

    @jokes.command("remove")
    async def remove_jokes(self, ctx, *args):
        """Remove jokes"""
        self._CFG.delete_jokes(args)
        mes = ", ".join([f"{j} -> {r}" for j, r in self._CFG.jokes.items()])
        if mes:
            await ctx.send(mes, reference=ctx.message)
        else:
            await ctx.send("Jokes list is empty")

    @jokes.command("clean")
    async def clean_jokes(self, ctx):
        """Remove all jokes (not available)"""
        if not self._is_me(ctx):
            return
        if len(self._CFG.jokes) == 0:
            return
        self._CFG.truncate_jokes()
        await ctx.send("Jokes list is empty")

    @commands.command()
    async def github(self, ctx):
        """Show link to the github repository"""
        url = 'https://github.com/thisUsernameIsAlredyTaken/' \
              + 'ShikimoriDiscordBot'
        await ctx.send(url, reference=ctx.message)

    @commands.command()
    async def version(self, ctx):
        """Show bot version"""
        await ctx.send(self._VERSION, reference=ctx.message)
