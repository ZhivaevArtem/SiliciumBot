import requests
from bs4 import BeautifulSoup
from discord.ext import commands

from silicium_bot.constants import Constants
from ..module_base import ModuleBase


class AnekModule(ModuleBase):
    @commands.command()
    async def anek(self, ctx: commands.Context):
        response = requests.get(Constants.anek_api)
        if response.status_code // 100 == 2:
            soup = BeautifulSoup(response.content, 'html.parser')
            tag = soup.find('section', class_='anek-view') \
                .find('article').find('p')
            print(soup.find('section', class_='anek-view').find('article')
                  .get_text())
            await ctx.send(tag.get_text(), reference=ctx.message)
