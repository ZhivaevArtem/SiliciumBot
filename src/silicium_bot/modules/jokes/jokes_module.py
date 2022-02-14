from discord.ext import commands

from silicium_bot.store import Store
from ..module_base import ModuleBase


class JokesModule(ModuleBase):
    def __init__(self, bot):
        super().__init__(bot)

    async def on_message(self, message) -> bool:
        if message.author == self.bot.user:
            return False
        content = message.content.strip()
        if content in Store.jokes.value and len(Store.jokes.value) > 0:
            await message.channel.send(Store.jokes.value[content],
                                       reference=message)
            return True
        return False

    # jokes
    @commands.group(invoke_without_command=True)
    async def jokes(self, ctx: commands.Context):
        text = "\n".join([f"{j} -> {r}" for j, r in Store.jokes.value.items()])
        if text:
            await ctx.send(text.replace("<@", "<@ "), reference=ctx.message)
        else:
            await ctx.send("There are no jokes", reference=ctx.message)

    # jokes add
    @jokes.command()
    async def add(self, ctx: commands.Context, message: str, react: str):
        if message not in Store.jokes.value \
           or Store.jokes.value[message] != react:
            Store.jokes.append({message: react})
        await ctx.send(f"Joke added: {message} -> {react}",
                       reference=ctx.message)

    # jokes remove
    @jokes.command()
    async def remove(self, ctx: commands.Context, *args):
        if len(args) == 0:
            await ctx.send("At least one joke must be passed",
                           reference=ctx.message)
            return
        to_del = [j for j in args if j in Store.jokes.value]
        to_del = list(set(to_del))
        if len(to_del) == 0:
            await ctx.send("There are no jokes to remove",
                           reference=ctx.message)
            return
        Store.jokes.remove(to_del)
        text = ", ".join(to_del)
        await ctx.send(f"Jokes removed: {text}", reference=ctx.message)

    # jokes truncate
    @jokes.command()
    async def truncate(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if len(Store.jokes.value) != 0:
            Store.jokes.value = {}
        await ctx.send("Jokes truncated", reference=ctx.message)
