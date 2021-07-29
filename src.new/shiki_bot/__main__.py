import os

import discord
import dotenv

from .locallib import BotWorkerTask
from .locallib import Config
from .locallib import DatabaseAdapter
from .locallib import ShikiClient

dotenv.load_dotenv()
client = discord.Client()

ADMIN_DISCORD_ID = os.getenv('ADMIN_DISCORD_ID')
DB_ADAPTER = DatabaseAdapter().connect()
SHIKI_CLIENT = ShikiClient()
CFG = Config(DB_ADAPTER, client).load()
BOT_WORKER = BotWorkerTask(CFG, client, SHIKI_CLIENT)


@client.event
async def on_ready():
    print('Bot ready')


@client.event
async def on_message(message: discord.Message):
    if message.author == client.user:
        return


client.run(os.getenv('DISCORD_BOT_TOKEN'))
