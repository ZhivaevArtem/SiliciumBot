# TODO: test

if __name__ == '__main__':
    import sys
    import os
    sys.path.append(os.path.dirname(os.path.dirname(__file__)))
    from bot import bot
    from silicium_bot.store import StaticStore
    bot.run(StaticStore.discord_token)
