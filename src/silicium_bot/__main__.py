# TODO: test

if __name__ == '__main__':
    import sys
    import os
    sys.path.append(os.path.dirname(os.path.dirname(__file__)))
    from bot import bot
    from globals import G
    G.BOT = bot
    G.BOT.run(G.DISCORD_TOKEN)
