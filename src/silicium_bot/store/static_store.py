import os


class StaticStore(object):
    shiki_url = 'https://shikimori.one'
    shiki_api = f"{shiki_url}/api"
    version = '1.5.0'
    github = 'https://github.com/thisUsernameIsAlredyTaken/ShikimoriDiscordBot'
    my_discord_id = int(os.getenv("MY_USER_ID"))
    discord_token = os.getenv("DISCORD_BOT_TOKEN")
    database_url = os.getenv("DATABASE_URL")
