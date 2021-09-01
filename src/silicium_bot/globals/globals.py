import os


class Globals:
    VERSION = "1.4.0"
    DISCORD_TOKEN = os.getenv("DISCORD_BOT_TOKEN")
    DATABASE_URL = os.getenv("DATABASE_URL")
    MY_USER_ID = int(os.getenv("MY_USER_ID"))
    GITHUB = "https://github.com/thisUsernameIsAlredyTaken/ShikimoriDiscordBot"
    SHIKI_API = "https://shikimori.one"
    BOT = None
    DB_ADAPTER = None
    CFG = None
