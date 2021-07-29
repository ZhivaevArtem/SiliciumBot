import re
from enum import Enum

import requests
from bs4 import BeautifulSoup


class ActionType(Enum):
    ADDED = 1
    MOVED = 2
    DELETED = 3
    SCORED = 4


class CollectionType(Enum):
    NONE = 0
    PLANNED = 1
    WATCHING = 2
    DROPPED = 3
    WATCHED = 4
    DELAYED = 5


class TitleType(Enum):
    ANIME = 1
    MANGA = 2


class ShikiLog(object):
    def __init__(self, data: dict, username: str):
        super().__init__()
        self.id: int = data['id']
        self.username = username
        self.data = data
        self.description: str = re.sub('</?\\w+>', '', data['description'])
        self.title: str = data['target']['name']
        self.russian_title: str = data['target']['russian']

    def get_message(self):
        return f"{self.username}: {self.description}"


class ShikiClient(object):
    def __init__(self):
        super().__init__()
        self._fetched_ids: dict[int, list[int]] = {}
        self._cached_ids: dict[int, list[int]] = {}
        self._headers = {
            'User-Agent': 'SiliciumBotChan/0.1.0 Discord' +
                          ' bot for me and my friends'
        }

    # region public

    def userid_to_username(self, user_id: int) -> str:
        url = f"https://shikimori.one/api/users/{user_id}"
        data = requests.get(url, headers=self._headers).json()
        return data['nickname']

    def username_to_userid(self, username: str) -> int:
        url = f"https://shikimori.one/{username}"
        html = requests.get(url, headers=self._headers).content.decode('utf-8')
        soup = BeautifulSoup(html, features="lxml")
        user_id = soup.select('div.profile-head')[0].get('data-user-id')
        return user_id

    def retrieve_user_logs(self, user_id: int) -> list[ShikiLog]:
        url = f"https://shikimori.one/api/users/{user_id}/history?limit=5"
        res = requests.get(url=url, headers=self._headers)
        logs = {d['id']: ShikiLog(d, self.userid_to_username(user_id))
                for d in res.json()}
        if user_id in self._cached_ids:
            for cached_id in self._cached_ids[user_id]:
                if cached_id in logs:
                    del logs[cached_id]
        else:
            self._cached_ids[user_id] = []
        self._cached_ids[user_id] += logs.keys()
        return [log for log_id, log in logs.items()]

    # endregion public
