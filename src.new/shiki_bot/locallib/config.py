import json
import discord
from locallib import PostgresAdapter


class Config(object):
    def __init__(self):
        super().__init__()
        self._client: discord.Client = None
        self._db_adapter: PostgresAdapter = None
        # config itself
        self._userids = []
        self._message_channel: discord.TextChannel = None
        self._status = discord.Status.online
        self._long_pooling_interval = 600
        self._prefix = ';'
        self._activity_type = discord.ActivityType.unknown
        self._activity_text = ''

    # region public

    # region config methods

    # region properties

    @property
    def userids(self):
        return tuple(self._userids)

    @property
    def message_channel(self):
        return self._message_channel

    @message_channel.setter
    def message_channel(self, value):
        self._message_channel = value
        data = ['message_channel_id', str(value.id) if value else None]
        self._db_adapter.insert_distinct('config_', ['key_', 'str_val_'], data)

    @property
    def status(self):
        return self._status

    @status.setter
    def status(self, value):
        self._status = value
        data = ['status', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'str_val_'], data)

    @property
    def long_pooling_interval(self):
        return self._long_pooling_interval

    @long_pooling_interval.setter
    def long_pooling_interval(self, value):
        self._long_pooling_interval = value
        data = ['long_pooling_interval', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'int_val_'], data)

    @property
    def prefix(self):
        return self._prefix

    @prefix.setter
    def prefix(self, value):
        self._prefix = value
        data = ['prefix', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'str_val_'], data)

    @property
    def activity_type(self):
        return self._activity_type

    @activity_type.setter
    def activity_type(self, value):
        self._activity_type = value
        data = ['activity_type', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'int_val_'], data)

    @property
    def activity_text(self):
        return self._activity_text

    @activity_text.setter
    def activity_text(self, value):
        self._activity_text = value
        data = ['activity_text', value]
        self._db_adapter.insert_distinct('config_', ['key_', 'str_val_'], data)

    # endregion properties

    def add_users(self, userids):
        table = 'arr_userids_config_'
        columns = ['userids_']
        to_add_users = [u for u in userids if u not in self._userids]
        data = [[u] for u in to_add_users]
        self._userids += to_add_users
        self._db_adapter.insert_data_distinct(table, columns, data)

    def delete_users(self, userids):
        table = 'arr_userids_config_'
        column = 'userids_'
        for user in userids:
            if user in self._userids:
                self._userids.remove(user)
        data = userids
        self._db_adapter.remove_data_by_ids(table, column, data)

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

    def _store(self):
        obj = self._to_dict()
        data = []
        for key, val in obj.items():
            if type(val) == list or type(val) == tuple:
                self._db_adapter.truncate_tables([f"arr_{key}_config_"])
                self._db_adapter.insert_data_distinct(f"arr_{key}_config_",
                                                      [f"{key}_"],
                                                      [[str(v)] for v in val])
            elif val is None:
                data.append([key, None, None, None])
            elif type(val) == int:
                data.append([key, val, None, None])
            elif type(val) == str:
                data.append([key, None, val, None])
            elif type(val) == bool:
                data.append([key, None, None, val])
        columns = ["key_", "int_val_", "str_val_", "bool_val_"]
        self._db_adapter.insert_data_distinct("config_", columns, data)

    def __str__(self) -> str:
        return json.dumps(self._to_dict(), indent=4)

    def _to_dict(self) -> dict:
        return {
            'userids': self._userids,
            'message_channel_id': str(self._message_channel.id)
            if self._message_channel else None,
            'status': str(self._status),
            'long_pooling_interval': self._long_pooling_interval,
            'prefix': self._prefix,
            'activity_type': int(self._activity_type),
            'activity_text': self._activity_text
        }

    def _from_dict(self, obj):
        if obj['userids'] is not None:
            self._userids = obj['userids'][:]
        if obj['message_channel_id'] is not None:
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
