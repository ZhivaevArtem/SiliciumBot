# TODO: test

if __name__ == '__main__':
    import sys
    import os
    sys.path.append(os.path.dirname(os.path.dirname(__file__)))
    from silicium_bot.store import StaticStore
    from silicium_bot.bot import bot
    bot.run(StaticStore.discord_token)
