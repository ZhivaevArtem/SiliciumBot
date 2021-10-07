import requests
from bs4 import BeautifulSoup
from discord.ext import commands

from silicium_bot.constants import Constants
from ..module_base import ModuleBase


class AnekModule(ModuleBase):
    @commands.command()
    async def anek(self, ctx: commands.Context):
        response = requests.get(Constants.anek_api,
                                headers=Constants.request_headers)
        if response.status_code // 100 == 2:
            soup = BeautifulSoup(response.content, 'html.parser')
            tag = soup.find('div', class_='text')
            await ctx.send(tag.get_text(), reference=ctx.message)
