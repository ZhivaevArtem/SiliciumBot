if __name__ == '__main__':
    import sys
    import os
    sys.path.append(os.path.dirname(os.path.dirname(__file__)))
    from silicium_bot.constants import Constants
    from silicium_bot.bot import bot
    bot.run(Constants.discord_token)
