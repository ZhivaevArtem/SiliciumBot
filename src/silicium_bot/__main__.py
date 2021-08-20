# TODO: test

if __name__ == '__main__':
    from bot import bot
    from globals import G
    G.BOT = bot
    G.BOT.run(G.DISCORD_TOKEN)
