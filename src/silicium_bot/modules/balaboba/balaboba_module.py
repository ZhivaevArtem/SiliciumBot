import json

import aiohttp

from ..module_base import ModuleBase
from ...constants import Constants


class BalabobaModule(ModuleBase):
    async def on_message(self, message) -> bool:
        if message.author == self.bot.user:
            return False
        if message.content.startswith(self.bot.command_prefix):
            return False
        if message.mentions[0].id != self.bot.user.id:
            return False
        query_text = message.clean_content.strip()
        if not query_text.startswith('@'):
            return False
        query_text = query_text[1 + len(self.bot.user.display_name):].strip()
        if not query_text:
            return False
        headers = Constants.request_headers
        headers['Content-Type'] = 'application/json'
        body = json.dumps({
            'query': query_text,
            'intro': 0,
            'filter': 1,
        }).encode('utf8')
        async with aiohttp.ClientSession() as session:
            async with session.post(Constants.balaboba_api, data=body,
                                    headers=headers) as res:
                res_obj = await res.json(encoding='utf8')
                answer_text = res_obj['text']
                if not answer_text:
                    return False
                await message.channel.send(answer_text, reference=message)
        return True
