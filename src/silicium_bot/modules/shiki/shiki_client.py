import re

import requests

from silicium_bot.store import StaticStore, Store


class ShikiLog(object):
    def __init__(self, data: dict, username: str):
        super().__init__()
        self.id: int = data['id']
        self.username = username
        self.data = data
        self.url = f"{StaticStore.shiki_url}/{data['target']['url']}"
        self.description: str = re.sub('</?\\w+>', '', data['description'])
        self.title: str = data['target']['name']
        self.russian_title: str = data['target']['russian']

    def get_message(self):
        message = f"{self.username}: {self.description}:"
        message += f" {self.russian_title} / {self.title}"
        return message

    def get_embed_message(self):
        url = f"{StaticStore.shiki_url}/{self.username}"
        message = f"[{self.username}]({url}): {self.description}:"
        message += f" [{self.russian_title} / {self.title}]({self.url})"
        return message


class ShikiClient(object):
    def __init__(self):
        super().__init__()
        self._cached_ids: dict[str, list[int]] = {}
        self._headers = {
            'User-Agent': f'SiliciumBotChan/{StaticStore.version} Discord' +
                          ' bot for me and my friends'
        }

    def retrieve_user_logs(self, username: str) -> list[ShikiLog]:
        limit = Store.shiki_request_limit.value
        url = f"{StaticStore.shiki_api}/users/{username}" \
              + f"/history?limit={limit}"
        res = requests.get(url=url, headers=self._headers)
        if not res.ok:
            pass
        logs = {d['id']: ShikiLog(d, username) for d in res.json()}
        if username in self._cached_ids:
            for cached_id in self._cached_ids[username]:
                if cached_id in logs:
                    del logs[cached_id]
            self._cached_ids[username] += logs.keys()
            return [log for log_id, log in logs.items()]
        else:
            self._cached_ids[username] = []
            self._cached_ids[username] += logs.keys()
            return []
