import datetime
from bs4 import BeautifulSoup
import requests
from enum import Enum
from locallib import PostgresAdapter


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
    def __init__(self, data):
        super().__init__()
        self.id = 0
        self.action_type: ActionType
        self.collection_type: CollectionType
        self.title_type: TitleType
        self.title = ""
        self.scores = (None, None)
        self.description = ""


class ShikiClient(object):
    def __init__(self, db_adapter: PostgresAdapter):
        super().__init__()
        self._db_adapter = db_adapter
        self._fetched_ids = None
        self._headers = {
            'User-Agent': 'SiliciumBotChan/' +
            '0.1.0 Discord bot for me and my friends'
        }

    def _fetch_fetched_ids(self):
        data = self._db_adapter.fetch_data('fetched_ids_',
                                           ['user_id_', 'log_id_'])
        fetched_ids: dict[str, list] = {}
        for row in data:
            user_id = int(row[0])
            log_id = int(row[1])
            if user_id not in fetched_ids:
                fetched_ids[user_id] = []
            fetched_ids[user_id].append(log_id)
        return fetched_ids

    def _store_fetched_ids(self, fetched_ids: dict):
        if len(fetched_ids) == 0:
            return
        data = []
        for user_id, user_logs in fetched_ids.items():
            data.append([user_id, user_logs['id']])
            if user_id not in self._fetched_ids:
                self._fetched_ids = []
            for log in user_logs:
                if log['id'] not in self._fetched_ids[user_id]:
                    self._fetched_ids[user_id].append(log['id'])
        self._db_adapter.insert_data_distinct('fetched_ids_',
                                              ['user_id_', 'log_id_'], data)

    def username_to_userid(self, username) -> int:
        url = f"https://shikimori.one/{username}"
        html = requests.get(url, headers=self._headers).content.decode('utf-8')
        soup = BeautifulSoup(html, features="lxml")
        id = soup.select('div.profile-head')[0].get('data-user-id')
        return id

    def retrieve_user_logs(self, user_id: int) -> list[ShikiLog]:
        if self._fetched_ids is None:
            self._fetched_ids = self._fetch_fetched_ids()
        url = f"https://shikimori.one/api/users/{user_id}/history?limit=5"
        res = requests.get(url=url, headers=self._headers)
        data = {d['id']: d for d in res.json()}
        if user_id in self._fetched_ids:
            for id in self._fetched_ids[user_id]:
                if id in data:
                    del data[id]
        self._store_fetched_ids(data)
        return data
