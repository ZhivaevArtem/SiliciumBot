from discord.ext import commands

from silicium_bot.modules.module_base import ModuleBase
from silicium_bot.globals import G


class JokesModule(ModuleBase):
    async def on_message(self, message) -> bool:
        content = message.content.strip()
        if content in G.CFG.jokes and len(G.CFG.jokes) > 0:
            await message.channel.send(G.CFG.jokes[content],
                                       reference=message)
            return True
        return False

    # jokes
    @commands.group(invoke_without_command=True)
    async def jokes(self, ctx: commands.Context):
        text = ", ".join([f"{j} -> {r}" for j, r in G.CFG.jokes.items()])
        if text:
            await ctx.send(text, reference=ctx.message)
        else:
            await ctx.send("There are no jokes")

    # jokes add
    @jokes.command("add")
    async def add_joke(self, ctx: commands.Context, message: str, react: str):
        if message not in G.CFG.jokes \
           or G.CFG.jokes[message] != react:
            G.CFG.add_joke(message, react)
        await ctx.send(f"Joke added: {message} -> {react}",
                       reference=ctx.message)

    # jokes remove
    @jokes.command("remove")
    async def remove_jokes(self, ctx: commands.Context, *args):
        if len(args) == 0:
            await ctx.send("At least one joke must be passed",
                           reference=ctx.message)
            return
        to_del = [j for j in args if j in G.CFG.jokes]
        if len(to_del) == 0:
            await ctx.send("There are no jokes to remove",
                           reference=ctx.message)
            return
        G.CFG.delete_jokes(to_del)
        text = ", ".join(to_del)
        await ctx.send(f"Jokes removed: {text}", reference=ctx.message)

    # jokes truncate
    @jokes.command("truncate")
    async def truncate_jokes(self, ctx: commands.Context):
        self.raise_if_not_me(ctx)
        if len(G.CFG.jokes) != 0:
            G.CFG.truncate_jokes()
        await ctx.send("Jokes truncated")



