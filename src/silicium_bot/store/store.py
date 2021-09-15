import os

import discord

from silicium_bot.store.database_adapter_base import DatabaseAdapterBase
from silicium_bot.store.postgres_adapter import PostgresAdapter


class NotificationChannelStub(object):
    def __init__(self):
        self.id = 0


class BotStub(object):
    def get_channel(self, id):
        return NotificationChannelStub()


class StoreField(object):
    db_adapter = DatabaseAdapterBase()
    fields = []

    def __init__(self, value, key, serialize=None, deserialize=None):
        self._value = value
        self._key = key
        self._serialize = serialize or (lambda val: val)
        self._deserialize = deserialize or (lambda db_val: db_val)
        StoreField.fields.append(self)

    def serialize(self):
        return self._serialize(self._value)

    def deserialize(self, value):
        self._value = self._deserialize(value)

    @property
    def key(self):
        return self._key

    @property
    def value(self):
        return self._value

    @value.setter
    def value(self, value):
        self._value = value
        StoreField.db_adapter.patch(self.key, self.serialize())


class ListStoreField(StoreField):
    def __init__(self, value, key):
        super().__init__(value, key)

    def append(self, values):
        for v in values:
            if v not in self._value:
                self._value.append(v)
        StoreField.db_adapter.patch(self.key, values)

    def remove(self, values):
        for v in values:
            if v in self._value:
                self._value.remove(v)
        StoreField.db_adapter.remove(self.key, values)


class DictStoreField(StoreField):
    def __init__(self, value, key):
        super().__init__(value, key)

    def append(self, values):
        for k, v in values.items():
            self._value[k] = v
        StoreField.db_adapter.patch(self.key, values)

    def remove(self, values):
        for k in values:
            if k in self._value:
                self._value.pop(k)
        StoreField.db_adapter.remove(self.key, values)


class Store(object):
    _bot = BotStub()

    calculator_timeout = StoreField(10, 'calctimeout')
    shiki_request_limit = StoreField(5, 'shikireqlimit')
    bot_activity_text = StoreField('', 'botactivtext')
    is_daemon_running = StoreField(False, 'daemonrun')
    jokes = DictStoreField({}, 'jokes')
    daemon_interval = StoreField(300, 'daemoninterval')
    bot_prefix = StoreField(';', 'botprefix')
    shiki_usernames = ListStoreField([], 'shikiusers')
    notification_channel = StoreField(
        NotificationChannelStub(), 'notifchannel', lambda v: v.id,
        lambda v: Store._bot.get_channel(v) or NotificationChannelStub()
    )
    bot_activity_type = StoreField(
        discord.ActivityType.unknown, 'botactivtype',
        lambda v: int(v), lambda v: discord.ActivityType(v)
    )
    bot_status = StoreField(
        discord.Status.online, 'botstatus', lambda v: str(v),
        lambda v: discord.Status(v)
    )

    @staticmethod
    def init(bot):
        Store.bot = bot
        StoreField.db_adapter = PostgresAdapter()
        dic = StoreField.db_adapter.find_all()
        fields_map = {f.key: f for f in StoreField.fields}
        for key, val in dic.items():
            fields_map[key].deserialize(val)
