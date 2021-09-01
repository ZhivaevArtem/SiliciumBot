import os

import psycopg2

from silicium_bot.store.database_adapter_base import DatabaseAdapterBase

DB_PREFIX = 'silbot_'
DB_TYPES = {
    int: 'numeric',
    str: 'character varying',
    bool: 'boolean',
    float: 'real'
}
UNSUPPORTED_TYPE_MESSAGE = 'Unsupported data type'


class Sequence(object):
    def __init__(self, name, *, max_value=9223372036854775807,
                 increment=1, start=1, min_value=1):
        self.name = DB_PREFIX + name
        self.max_value = max_value
        self.min_value = min_value
        self.increment = increment
        self.start = start

    def create_script(self):
        sql = f"""
CREATE SEQUENCE IF NOT EXISTS {self.name}
INCREMENT {self.increment}
START {self.start}
MAXVALUE {self.max_value};
""".strip()
        return sql


class Column(object):
    def __init__(self, name, type, *, length=None,
                 nullable=True, sequence=None, primary=False):
        self.name = name
        self.type = type
        self.length = length
        self.nullable = nullable
        self.sequence = sequence
        self.primary = primary
        if primary:
            self.nullable = False

    def create_script(self):
        sql = f"{self.name} {self.type}" \
              + ("" if self.length is None else f"({self.length})") \
              + ("" if self.nullable else " NOT NULL") \
              + ("" if self.sequence is None
                 else f" DEFAULT nextval('{self.sequence.name}'::regclass)")
        return sql


class Table(object):
    def __init__(self, name, columns):
        self.name = DB_PREFIX + name
        self.columns = {c.name: c for c in columns}

    def create_script(self):
        sequences = [c.sequence.create_script()
                     for c in self.columns.values() if c.sequence is not None]
        sequences = '\n'.join(sequences)
        fields = [c.create_script() for c in self.columns.values()]
        primaries = [c.name for c in self.columns.values() if c.primary]
        primaries = ', '.join(primaries)
        fields.append(
            f"CONSTRAINT {self.name}_pkey PRIMARY KEY ({primaries})")
        fields = ',\n'.join(fields)
        sql = f"""
{sequences}
CREATE TABLE IF NOT EXISTS {self.name} (
{fields}
);
""".strip()
        return sql


class MultiValueTable(Table):
    COL_FMT = '{}_value'
    VAL_NAME_FMT = 'name'

    def __init__(self, name, additional_columns=[]):
        super().__init__(name, [
            Column(MultiValueTable.VAL_NAME_FMT, DB_TYPES[str], length=31,
                   primary=True),
            Column(MultiValueTable.COL_FMT.format(int.__name__),
                   DB_TYPES[int], length=31),
            Column(MultiValueTable.COL_FMT.format(str.__name__),
                   DB_TYPES[str], length=255),
            Column(MultiValueTable.COL_FMT.format(bool.__name__),
                   DB_TYPES[bool]),
            Column(MultiValueTable.COL_FMT.format(float.__name__),
                   DB_TYPES[float])
        ])
        for ac in additional_columns:
            self.columns[ac.name] = ac


class DictTable(MultiValueTable):
    NAME_FMT = 'dic_{}'
    KEY_COL_NAME = 'key'

    def __init__(self, key_type):
        column = Column(DictTable.KEY_COL_NAME,
                        DB_TYPES[key_type], primary=True)
        if key_type is int:
            column.length = 31
        elif key_type is str:
            column.length = 255
        super().__init__(DictTable.NAME_FMT.format(key_type.__name__),
                         [column])


ATOMIC_TABLE = MultiValueTable('store')
DICT_INT_TABLE = DictTable(int)
DICT_FLOAT_TABLE = DictTable(float)
DICT_BOOL_TABLE = DictTable(bool)
DICT_STR_TABLE = DictTable(str)
TABLES = [ATOMIC_TABLE, DICT_INT_TABLE, DICT_FLOAT_TABLE,
          DICT_BOOL_TABLE, DICT_STR_TABLE]
DICT_TABLES = {
    int: DICT_INT_TABLE,
    str: DICT_STR_TABLE,
    float: DICT_FLOAT_TABLE,
    bool: DICT_BOOL_TABLE
}


class PostgresAdapter(DatabaseAdapterBase):
    def __init__(self):
        self._url = os.getenv("DATABASE_URL")
        self._init_db()

    # region private
    def _execute_sql(self, *args):
        data = None
        with psycopg2.connect(self._url) as conn:
            with conn.cursor() as cur:
                for sql in args:
                    cur.execute(sql)
                conn.commit()
                try:
                    data = cur.fetchall()
                except psycopg2.ProgrammingError:
                    pass
        return data

    def _init_db(self):
        init_sqls = [table.create_script() for table in TABLES]
        self._execute_sql(*init_sqls)

    def _get_type(self, value):
        if value is list and len(value) > 0:
            for v in value:
                return type(v)
        elif value is dict and len(value) > 0:
            for k, v in value.items():
                return type(k), type(v)
        else:
            return type(value)

    def _insert_distinct_script(self, table, columns, data,
                                conflict_columns=None):
        if conflict_columns is None:
            conflict_columns = [columns[0]]
        values = [', '.join([self._to_sql_value(f) for f in d]) for d in data]
        values = ',\n'.join([f"({v})" for v in values])
        on_conflict = [f"{c.name} = excluded.{c.name}" for c in columns]
        on_conflict = ',\n'.join(on_conflict)
        sql = f"""
INSERT INTO {table}({', '.join(columns)}) VALUES
{values}
ON CONFLICT {', '.join(conflict_columns)} DO UPDATE SET
{on_conflict};
""".strip()
        return sql

    def _delete_by_key_script(self, name):
        return '\n'.join([f"""
DELETE FROM {table.name}
WHERE {MultiValueTable.VAL_NAME_FMT} = {name};
""" for table in TABLES])

    def _to_sql_value(self, value):
        if value is str:
            s = value.replace("'", "''")
            return f"'{value.s}'"
        elif value in (int, float, bool):
            return f"{value}"
        elif value is None:
            return "NULL"

    def _prepare_data(self, name, value):
        if type(value) is dict:
            key_type, val_type = self._get_type(value)
            table = DictTable.NAME_FMT.format(key_type.__name__)
            columns = [MultiValueTable.VAL_NAME_FMT, DictTable.KEY_COL_NAME,
                       DictTable.COL_FMT.format(val_type.__name__)]
            data = [[self._to_sql_value(name), self._to_sql_value(k),
                     self._to_sql_value(v)] for k, v in value.items()]
            conflict_columns = [MultiValueTable.VAL_NAME_FMT,
                                DictTable.KEY_COL_NAME]
        elif type(value) is list:
            val_type = self._get_type(value)
            table = DictTable.NAME_FMT.format(val_type.__name__)
            columns = [MultiValueTable.VAL_NAME_FMT, DictTable.KEY_COL_NAME]
            data = [[self._to_sql_value(name),
                     self._to_sql_value(v)] for v in value]
            conflict_columns = [MultiValueTable.VAL_NAME_FMT,
                                DictTable.KEY_COL_NAME]
        elif type(value) in (int, float, bool):
            val_type = self._get_type(value)
            table = ATOMIC_TABLE.name
            columns = [MultiValueTable.VAL_NAME_FMT,
                       MultiValueTable.COL_FMT.format(val_type.__name__)]
            data = [[self._to_sql_value(name), self._to_sql_value(value)]]
            conflict_columns = [MultiValueTable.VAL_NAME_FMT]
        else:
            raise Exception(UNSUPPORTED_TYPE_MESSAGE)
        return table, columns, data, conflict_columns
    # endregion private

    def save(self, name, value):
        table, columns, data, conflict_columns = self._prepare_data(name,
                                                                    value)
        delete_sql = self._delete_by_key_script(name)
        insert_sql = self._insert_distinct_script(
            table, columns, data, conflict_columns)
        self._execute_sql(delete_sql, insert_sql)

    def patch(self, name, value):
        table, columns, data, conflict_columns = self._prepare_data(name,
                                                                    value)
        insert_sql = self._insert_distinct_script(
            table, columns, data, conflict_columns)
        self._execute_sql(insert_sql)

    def remove(self, name, values=None):
        if values is None:
            sql = f"""
DELETE FROM {ATOMIC_TABLE.name}
WHERE {MultiValueTable.VAL_NAME_FMT} = {name};
"""
        elif type(values) is list:
            val_type = self._get_type(values)
            sql = f"""
DELETE FROM {DICT_TABLES[val_type].name}
WHERE {MultiValueTable.VAL_NAME_FMT} = {name}
AND {DictTable.KEY_COL_NAME}
IN ({', '.join([self._to_sql_value(v) for v in values])});
"""
        else:
            raise Exception(UNSUPPORTED_TYPE_MESSAGE)
        self._execute_sql(sql)

    def find_all(self):
        pass


if __name__ == '__main__':
    for tab in TABLES:
        print(tab.create_script())
    # adapter = PostgresAdapter()
