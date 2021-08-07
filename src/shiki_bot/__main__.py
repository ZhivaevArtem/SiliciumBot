if __name__ == '__main__':
    import os
    from bot import client
    TOKEN = os.getenv("DISCORD_BOT_TOKEN")
    client.run(TOKEN)
