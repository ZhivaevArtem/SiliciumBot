# TODO: test

if __name__ == '__main__':
    import os
    from bot import bot
    TOKEN = os.getenv("DISCORD_BOT_TOKEN")
    bot.run(TOKEN)
