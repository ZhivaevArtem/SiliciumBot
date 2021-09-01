import discord

from silicium_bot.globals import G
from silicium_bot.store.database_adapter_base import DatabaseAdapterBase
from silicium_bot.store.postgres_adapter import PostgresAdapter


class NotificationChannelStub(discord.TextChannel):
    def __init__(self):
        self.id = 0


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
        StoreField.db_adapter.save(self.key, self.serialize())


class Store(object):
    def __init__(self):
        self.calculator_timeout = StoreField(10, 'calctimeout')
        self.shiki_request_limit = StoreField(5, 'shikireqlimit')
        self.bot_activity_text = StoreField('', 'botactivtext')
        self.is_daemon_running = StoreField(False, 'daemonrun')
        self.jokes = StoreField({}, 'jokes')
        self.daemon_interval = StoreField(300, 'daemoninterval')
        self.bot_prefix = StoreField(';', 'botprefix')
        self.shiki_usernames = StoreField([], 'shikiusers')
        self.notification_channel = StoreField(
            NotificationChannelStub(), 'notifchannel', lambda v: v.id,
            lambda v: G.BOT.get_channel(v) or NotificationChannelStub()
        )
        self.bot_activity_type = StoreField(
            discord.Activity(name="", type=discord.ActivityType.unknown),
            'botactivtype', lambda v: int(v), lambda v: discord.ActivityType(v)
        )
        self.bot_status = StoreField(
            discord.Status.online, 'botstatus', lambda v: str(v),
            lambda v: discord.Status(v)
        )
        StoreField.db_adapter = PostgresAdapter()
        dic = StoreField.db_adapter.find_all()
        self._from_dict(dic)

    def _from_dict(self, dic: dict):
        fields_map = {}
        for field in StoreField.fields:
            fields_map[field.key] = field
        for key, val in dic.items():
            fields_map[key].deserialize(val)
