if __name__ == '__main__':
    import os
    from bot import client
    client.run(os.getenv("DISCORD_BOT_TOKEN"))
