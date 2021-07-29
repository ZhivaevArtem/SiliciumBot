import json

import discord

from datbase_adapter import DatabaseAdapter


class TextChannelStub(discord.channel.TextChannel):
    def __init__(self):
        self.id = 0

    async def send(self, *args, **kwargs):
        pass


class Config(object):
    def __init__(self, db_adapter: DatabaseAdapter, client: discord.Client):
        super().__init__()
        self._client = client
        self._db_adapter = db_adapter
        # config itself
        self._usernames = []
        self._message_channel: discord.TextChannel = TextChannelStub()
        self._status = discord.Status.online
        self._long_pooling_interval = 600
        self._prefix = ';'
        self._activity_type = discord.ActivityType.unknown
        self._activity_text = ''

    # region public

    # region config methods

    # region properties

    @property
    def usernames(self) -> list[str]:
        return self._usernames

    @property
    def message_channel(self) -> discord.TextChannel:
        return self._message_channel

    @message_channel.setter
    def message_channel(self, value: discord.TextChannel):
        self._message_channel = value
        data = ['message_channel_id', str(value.id)]
        self._db_adapter.insert_data_distinct('config_',
                                              ['key_', 'str_val_'],
                                              [data])

    @property
    def status(self) -> str:
        return self._status

    @status.setter
    def status(self, value: str):
        self._status = value
        data = ['status', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'str_val_'], data)

    @property
    def long_pooling_interval(self) -> int:
        return self._long_pooling_interval

    @long_pooling_interval.setter
    def long_pooling_interval(self, value: int):
        self._long_pooling_interval = value
        data = ['long_pooling_interval', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'int_val_'], data)

    @property
    def prefix(self) -> str:
        return self._prefix

    @prefix.setter
    def prefix(self, value: str):
        self._prefix = value
        data = ['prefix', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'str_val_'], data)

    @property
    def activity_type(self) -> discord.ActivityType:
        return self._activity_type

    @activity_type.setter
    def activity_type(self, value: discord.ActivityType):
        self._activity_type = value
        data = ['activity_type', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'int_val_'], data)

    @property
    def activity_text(self) -> str:
        return self._activity_text

    @activity_text.setter
    def activity_text(self, value: str):
        self._activity_text = value
        data = ['activity_text', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'str_val_'], data)

    # endregion properties

    def add_users(self, usernames: list[str]):
        table = 'arr_usernames_config_'
        columns = ['usernames_']
        to_add_users = [u for u in usernames if u not in self._usernames]
        if len(to_add_users) == 0:
            return
        data = [[u] for u in to_add_users]
        self._usernames += to_add_users
        self._db_adapter.insert_data_distinct(table, columns, data)

    def delete_users(self, usernames: list[str]):
        table = 'arr_usernames_config_'
        column = 'usernames_'
        for user in usernames:
            if user in self._usernames:
                self._usernames.remove(user)
        self._db_adapter.remove_data_by_ids(table, column, usernames)

    def truncate_users(self):
        self._db_adapter.truncate_table('arr_usernames_config_')

    # endregion config methods

    def load(self, db_adapter=None):
        if db_adapter is not None:
            self._db_adapter = db_adapter
        obj = {}
        tables = [t for t in self._db_adapter.fetch_table_names()
                  if t.endswith('config_')]
        for table in tables:
            if table == 'config_':
                columns = ['key_', 'int_val_', 'str_val_', 'bool_val_']
                data = self._db_adapter.fetch_data(columns)
                for d in data:
                    if d[1] is not None:
                        obj[d[0]] = d[1]
                    elif d[2] is not None:
                        obj[d[0]] = d[2]
                    elif d[3] is not None:
                        obj[d[0]] = d[3]
            if table.startswith('arr_'):
                column = table[len('arr_'): len(table) - len('_config_')]
                data = self._db_adapter.fetch_data(table, [column])
                data_arr = [d[0] for d in data]
                obj[column] = data_arr[:]
        self._from_dict(obj)

    # endregion public

    # region private

    def __str__(self) -> str:
        return json.dumps(self._to_dict(), indent=4)

    def _to_dict(self) -> dict:
        return {
            'usernames': self._usernames,
            'message_channel_id': str(self._message_channel.id)
            if self._message_channel else None,
            'status': str(self._status),
            'long_pooling_interval': self._long_pooling_interval,
            'prefix': self._prefix,
            'activity_type': int(self._activity_type),
            'activity_text': self._activity_text
        }

    def _from_dict(self, obj):
        if obj['usernames'] is not None:
            self._usernames = obj['usernames'][:]
        if obj['message_channel_id'] is not None:
            mcid = obj['message_channel_id']
            if mcid == 0:
                self._message_channel = TextChannelStub()
            else:
                self._message_channel = self._client.get_channel(
                    int(obj['message_channel_id']))
        if obj['status'] is not None:
            self._status = discord.Status(obj['status'])
        if obj['long_pooling_interval'] is not None:
            self._long_pooling_interval = obj['long_pooling_interval']
        if obj['prefix'] is not None:
            self._prefix = obj['prefix']
        if obj['activity_type'] is not None:
            self._activity_type = obj['activity_type']
        if obj['activity_text'] is not None:
            self._activity_text = obj['activity_text']

    # endregion private
