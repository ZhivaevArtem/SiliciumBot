# TODO: test

if __name__ == '__main__':
    from bot import bot
    from globals import G
    G.BOT = bot
    if G:
        bot.run(G.TOKEN)
    else:
        print('Error while init')
        print(G.get_traceback())
