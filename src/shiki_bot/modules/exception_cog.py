import traceback

from discord.ext import commands


class ExceptionCog(commands.Cog):

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
