import os


def get_log_print_time(var_name):
    var: str = os.getenv(var_name)
    if var is not None:
        return var.strip().lower() == "true"
    return True


class Constants(object):
    shiki_url = 'https://shikimori.one'
    shiki_api = f"{shiki_url}/api"
    version = '1.6.0'
    github = 'https://github.com/thisUsernameIsAlredyTaken/ShikimoriDiscordBot'
    my_discord_id = int(os.getenv("MY_USER_ID") or "-1")
    discord_token = os.getenv("DISCORD_BOT_TOKEN")
    database_url = os.getenv("DATABASE_URL")
    log_print_time = get_log_print_time("LOG_PRINT_TIME")
    vk_login = os.getenv('LOGIN')
    vk_password = os.getenv('PASSWORD')
    youtube_token = os.getenv('YTTOKEN')

