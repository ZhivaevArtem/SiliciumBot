import requests
from bs4 import BeautifulSoup
from discord.ext import commands

from silicium_bot.constants import Constants
from ..module_base import ModuleBase


class AnekModule(ModuleBase):
    @commands.command()
    async def anek(self, ctx: commands.Context, anek_id=None):
        if anek_id is None:
            url = Constants.anek_api
        else:
            url = f"https://baneks.ru/{anek_id}"
        response = requests.get(url)
        if response.status_code // 100 == 2:
            soup = BeautifulSoup(response.text, 'html.parser')
            tag = soup.find('section', class_='anek-view') \
                .find('article')
            print(tag.get_text())
            await ctx.send(tag.get_text(), reference=ctx.message)
        else:
            await ctx.send("Not found", reference=ctx.message)
