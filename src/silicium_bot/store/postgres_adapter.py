import json
import os
import sys

import psycopg2

from silicium_bot.store.database_adapter_base import DatabaseAdapterBase


RECONNECT_MAX_ATTEMPTS = 5
STORE_TABLE = 'json_data_'
STORE_KEY = 'key_'
STORE_VALUE = 'value_'


class PostgresAdapter(DatabaseAdapterBase):
    def __init__(self):
        self._url = os.getenv("DATABASE_URL")
        self._init_and_validate_database()

    # region private
    def _execute_sql(self, sql, *args):
        data = None
        with psycopg2.connect(self._url) as conn:
            with conn.cursor() as cur:
                cur.execute(sql)
                for extra_sql in args:
                    cur.execute(extra_sql)
                conn.commit()
                try:
                    data = conn.fetchall()
                except psycopg2.ProgrammingError:
                    pass
        return data

    def _init_and_validate_database(self):
        init_sql = f"""
CREATE TABLE IF NOT EXISTS {STORE_TABLE}
(
    {STORE_KEY} character varying(31) NOT NULL,
    {STORE_VALUE} jsonb NOT NULL,
    CONSTRAINT json_data__pkey PRIMARY KEY ({STORE_KEY})
);
""".strip()
        patch_procedure_sql = f"""

""".strip()
        remove_procedure_sql = f"""

""".strip()
        validate_sql = f"""
SELECT column_name, data_type, is_nullable,
       character_maximum_length, is_updatable
FROM information_schema.columns
WHERE table_name = '{STORE_TABLE}';
""".strip()
        data = self._execute_sql(init_sql, validate_sql)
        assert_message = 'Failed to validate database'
        for d in data:
            if d[0] == 'data_':
                assert d[1] == 'jsonb',             assert_message
                assert d[2] == 'NO',                assert_message
                assert d[3] is None,                assert_message
                assert d[4] == 'YES',               assert_message
            elif d[0] == 'key_':
                assert d[1] == 'character varying', assert_message
                assert d[2] == 'NO',                assert_message
                assert d[3] >= 31,                  assert_message
                assert d[4] == 'YES',               assert_message

    def _to_json(self, value):
        return json.dumps(value).replace(r"\'", "''")

    def save(self, key, value):
        sql = f"""
INSERT INTO {STORE_TABLE}({STORE_KEY}, {STORE_VALUE}) VALUES
({key}, '{self._to_json(value)}');
""".strip()
        self._execute_sql(sql)

    def patch(self, key, values):
        super().patch(key, values)

    def remove(self, key, values):
        super().remove(key, values)

    def find_all(self):
        super().find_all()
    # endregion private


