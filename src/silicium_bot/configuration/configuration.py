import json

import discord

from silicium_bot.globals import G


class TextChannelStub(discord.channel.TextChannel):
    def __init__(self):
        self.id = 0

    async def send(self, *args, **kwargs):
        pass


class Config(object):

    # region private

    def _load(self):
        obj = {}
        tables = G.DB_ADAPTER.fetch_table_names()
        for table in tables:
            if table == 'config_':
                columns = ['key_', 'int_val_', 'str_val_',
                           'bool_val_', 'float_val_']
                data = G.DB_ADAPTER.fetch_data(table, columns)
                for d in data:
                    for i in range(1, len(d)):
                        if d[i] is not None:
                            obj[d[0]] = d[i]
            elif table.startswith('arr_'):
                column = table[len('arr_'): len(table) - len('_config_')]
                data = G.DB_ADAPTER.fetch_data(table, [f"{column}_"])
                data_arr = [d[0] for d in data]
                obj[column] = data_arr
            elif table.startswith('dic_'):
                columns = ['key_', 'val_']
                data = G.DB_ADAPTER.fetch_data(table, columns)
                data_dic = {d[0]: d[1] for d in data}
                prop_name = table[len('dic_'): len(table) - len('_config_')]
                obj[prop_name] = data_dic
        self._from_dict(obj)
        return self

    def _to_dict(self) -> dict:
        return {
            'activity_text': self._activity.name,
            'activity_type': int(self._activity.type),
            'calculator_timeout': self._calculator_timeout,
            'history_request_limit': int(self._history_request_limit),
            'is_worker_running': self._is_worker_running,
            'jokes': self._jokes,
            'loop_requests_interval': int(self._loop_requests_interval),
            'notification_channel_id': int(self._notification_channel.id),
            'prefix': self._prefix,
            'status': str(self._status),
            'usernames': self._usernames
        }

    def _from_dict(self, obj):
        if 'activity_text' in obj and obj['activity_text'] is not None \
           and 'activity_type' in obj and obj['activity_type'] is not None:
            activity_text = obj['activity_text']
            activity_type = int(obj['activity_type'])
            try:
                activity_type = discord.ActivityType(activity_type)
                self._activity = discord.Activity(name=activity_text,
                                                  type=activity_type)
            except ValueError:
                self._activity = discord.Activity()
        if 'calculator_timeout' in obj \
           and obj['calculator_timeout'] is not None:
            self._calculator_timeout = obj['calculator_timeout']
        if 'history_request_limit' in obj \
           and obj['history_request_limit'] is not None:
            self._history_request_limit \
                = int(obj['history_request_limit'])
        if 'is_worker_running' in obj and obj['is_worker_running'] is not None:
            self._is_worker_running = obj['is_worker_running']
        if 'jokes' in obj and obj['jokes'] is not None:
            self._jokes = obj['jokes']
        if 'loop_requests_interval' in obj \
           and obj['loop_requests_interval'] is not None:
            self._loop_requests_interval = int(obj['loop_requests_interval'])
        if 'notification_channel_id' in obj \
           and obj['notification_channel_id'] is not None:
            mcid = int(obj['notification_channel_id'])
            if mcid == 0:
                self._notification_channel = TextChannelStub()
            else:
                self._notification_channel = G.BOT.get_channel(mcid)
            if self._notification_channel is None:
                self._notification_channel = TextChannelStub()
        if 'prefix' in obj and obj['prefix'] is not None:
            self._prefix = obj['prefix']
        if 'status' in obj and obj['status'] is not None:
            self._status = discord.Status(obj['status'])
        if 'usernames' in obj and obj['usernames'] is not None:
            self._usernames = obj['usernames'][:]

    # region magic

    def __init__(self):
        super().__init__()
        self._activity = discord.Activity()
        self._calculator_timeout = 0.5
        self._history_request_limit = 5
        self._is_worker_running = True
        self._jokes: dict[str, str] = {}
        self._loop_requests_interval = 600
        self._notification_channel: discord.TextChannel = TextChannelStub()
        self._prefix = ';'
        self._status = discord.Status.online
        self._usernames = []
        self._load()

    def __str__(self) -> str:
        d = self._to_dict()
        s = json.dumps(d, indent=4, ensure_ascii=False).encode('utf-8')
        return s.decode('utf-8').replace("@", "@ ")

    # endregion magic

    # endregion private

    # region public

    # region properties

    @property
    def activity(self) -> discord.Activity:
        return self._activity

    @activity.setter
    def activity(self, value: discord.Activity):
        self._activity = value
        activity_type = int(value.type)
        activity_text = value.name
        if activity_type == discord.ActivityType.unknown:
            activity_text = ''
        data = [['activity_text', None, activity_text],
                ['activity_type', int(activity_type), None]]
        G.DB_ADAPTER.insert_data_distinct('config_',
                                          ['key_', 'int_val_', 'str_val_'],
                                          data)

    @property
    def calculator_timeout(self) -> float:
        return self._calculator_timeout

    @calculator_timeout.setter
    def calculator_timeout(self, value: float):
        self._calculator_timeout = value
        data = [['calculator_timeout', value]]
        G.DB_ADAPTER.insert_data_distinct('config_',
                                          ['key_', 'float_val_'], data)

    @property
    def history_request_limit(self) -> int:
        return self._history_request_limit

    @history_request_limit.setter
    def history_request_limit(self, value: int):
        self._history_request_limit = value
        data = [['history_request_limit', value]]
        G.DB_ADAPTER.insert_data_distinct('config_',
                                          ['key_', 'int_val_'], data)

    @property
    def is_worker_running(self) -> bool:
        return self._is_worker_running

    @is_worker_running.setter
    def is_worker_running(self, value: bool):
        if self._is_worker_running == value:
            return
        self._is_worker_running = value
        data = [['is_worker_running', value]]
        G.DB_ADAPTER.insert_data_distinct('config_',
                                          ['key_', 'bool_val_'], data)

    @property
    def jokes(self) -> dict[str, str]:
        return self._jokes

    @property
    def loop_requests_interval(self) -> int:
        return self._loop_requests_interval

    @loop_requests_interval.setter
    def loop_requests_interval(self, value: int):
        self._loop_requests_interval = value
        data = [['loop_requests_interval', value]]
        G.DB_ADAPTER.insert_data_distinct('config_',
                                          ['key_', 'int_val_'], data)

    @property
    def notification_channel(self) -> discord.TextChannel:
        return self._notification_channel

    @notification_channel.setter
    def notification_channel(self, value: discord.TextChannel):
        self._notification_channel = value
        data = [['notification_channel_id', value.id]]
        G.DB_ADAPTER.insert_data_distinct('config_',
                                          ['key_', 'int_val_'], data)

    @property
    def prefix(self) -> str:
        return self._prefix

    @prefix.setter
    def prefix(self, value: str):
        self._prefix = value
        data = [['prefix', value]]
        G.DB_ADAPTER.insert_data_distinct('config_',
                                          ['key_', 'str_val_'], data)

    @property
    def status(self) -> str:
        return self._status

    @status.setter
    def status(self, value: str):
        self._status = value
        data = [['status', str(value)]]
        G.DB_ADAPTER.insert_data_distinct('config_',
                                          ['key_', 'str_val_'], data)

    @property
    def usernames(self) -> list[str]:
        return self._usernames

    # endregion properties

    def add_joke(self, message: str, react: str):
        table = 'dic_jokes_config_'
        columns = ['key_', 'val_']
        data = [[message, react]]
        self._jokes[message] = react
        G.DB_ADAPTER.insert_data_distinct(table, columns, data)

    def delete_jokes(self, jokes: list[str]):
        table = 'dic_jokes_config_'
        column = 'key_'
        for joke in jokes:
            if joke in self._jokes:
                del self._jokes[joke]
        G.DB_ADAPTER.remove_data_by_ids(table, column, jokes)

    def truncate_jokes(self):
        self._jokes = {}
        G.DB_ADAPTER.truncate_table('dic_jokes_config_')

    def add_users(self, usernames: list[str]):
        table = 'arr_usernames_config_'
        columns = ['usernames_']
        to_add_users = [u for u in usernames if u not in self._usernames]
        data = [[u] for u in to_add_users]
        self._usernames += to_add_users
        G.DB_ADAPTER.insert_data_distinct(table, columns, data)

    def delete_users(self, usernames: list[str]):
        table = 'arr_usernames_config_'
        column = 'usernames_'
        for user in usernames:
            if user in self._usernames:
                self._usernames.remove(user)
        G.DB_ADAPTER.remove_data_by_ids(table, column, usernames)

    def truncate_users(self):
        self._usernames = []
        G.DB_ADAPTER.truncate_table('arr_usernames_config_')

    # endregion public
