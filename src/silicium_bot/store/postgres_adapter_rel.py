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
    VAL_COLNAME_FMT = '{}_value'
    NAME_COLNAME = 'name'

    def __init__(self, name, additional_columns=[]):
        super().__init__(name, [
            Column(MultiValueTable.NAME_COLNAME, DB_TYPES[str], length=31,
                   primary=True),
            Column(MultiValueTable.VAL_COLNAME_FMT.format(int.__name__),
                   DB_TYPES[int], length=31),
            Column(MultiValueTable.VAL_COLNAME_FMT.format(str.__name__),
                   DB_TYPES[str], length=255),
            Column(MultiValueTable.VAL_COLNAME_FMT.format(bool.__name__),
                   DB_TYPES[bool]),
            Column(MultiValueTable.VAL_COLNAME_FMT.format(float.__name__),
                   DB_TYPES[float])
        ])
        for ac in additional_columns:
            self.columns[ac.name] = ac


class DictTable(MultiValueTable):
    TABLE_NAME_FMT = 'dic_{}'
    KEY_COLNAME = 'key'

    def __init__(self, key_type):
        column = Column(DictTable.KEY_COLNAME,
                        DB_TYPES[key_type], primary=True)
        if key_type is int:
            column.length = 31
        elif key_type is str:
            column.length = 255
        super().__init__(DictTable.TABLE_NAME_FMT.format(key_type.__name__),
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
                    print("Execute sql:")
                    print(sql)
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
        if type(value) is list and len(value) > 0:
            for v in value:
                return type(v)
        elif type(value) is dict and len(value) > 0:
            for k, v in value.items():
                return type(k), type(v)
        elif type(value) in (int, float, str, bool):
            return type(value)

    def _insert_distinct_script(self, table, columns, data,
                                conflict_columns=None):
        if conflict_columns is None:
            conflict_columns = [columns[0]]
        values = [', '.join([self._to_sql_value(f) for f in d]) for d in data]
        values = ',\n'.join([f"({v})" for v in values])
        on_conflict = [f"{c} = excluded.{c}" for c in columns]
        on_conflict = ',\n'.join(on_conflict)
        sql = f"""
INSERT INTO {table}({', '.join(columns)}) VALUES
{values}
ON CONFLICT ({', '.join(conflict_columns)}) DO UPDATE SET
{on_conflict};
""".strip()
        return sql

    def _delete_by_key_script(self, name):
        return '\n'.join([f"""
DELETE FROM {table.name}
WHERE {MultiValueTable.NAME_COLNAME} = '{name}';
""" for table in TABLES])

    def _to_sql_value(self, value):
        if type(value) is str:
            s = value.replace("'", "''")
            return f"'{s}'"
        elif type(value) in (int, float, bool):
            return f"{value}"
        elif value is None:
            return "NULL"

    def _prepare_data(self, name, value):
        if type(value) is dict:
            key_type, val_type = self._get_type(value)
            table = DICT_TABLES[key_type].name
            columns = [MultiValueTable.NAME_COLNAME, DictTable.KEY_COLNAME,
                       MultiValueTable.VAL_COLNAME_FMT.format(int.__name__),
                       MultiValueTable.VAL_COLNAME_FMT.format(float.__name__),
                       MultiValueTable.VAL_COLNAME_FMT.format(str.__name__),
                       MultiValueTable.VAL_COLNAME_FMT.format(bool.__name__)]
            data = [[name, k, None, None, None, None] for k, v in value.items()]
            if val_type is int:
                for i in range(len(value)):
                    data[i][2] = value.items()[i][1]
            if val_type is float:
                for i in range(len(value)):
                    data[i][3] = value.items()[i][1]
            if val_type is str:
                for i in range(len(value)):
                    data[i][4] = value.items()[i][1]
            if val_type is bool:
                for i in range(len(value)):
                    data[i][5] = value.items()[i][1]
            data = [[name, k, v] for k, v in value.items()]
            conflict_columns = [MultiValueTable.NAME_COLNAME,
                                DictTable.KEY_COLNAME]
        elif type(value) is list:
            val_type = self._get_type(value)
            table = DICT_TABLES[val_type].name
            columns = [MultiValueTable.NAME_COLNAME, DictTable.KEY_COLNAME,
                       MultiValueTable.VAL_COLNAME_FMT.format(int.__name__),
                       MultiValueTable.VAL_COLNAME_FMT.format(float.__name__),
                       MultiValueTable.VAL_COLNAME_FMT.format(str.__name__),
                       MultiValueTable.VAL_COLNAME_FMT.format(bool.__name__)]
            data = [[name, v, None, None, None, None] for v in value]
            conflict_columns = [MultiValueTable.NAME_COLNAME,
                                DictTable.KEY_COLNAME]
        elif type(value) in (int, float, bool, str):
            table = ATOMIC_TABLE.name
            columns = [MultiValueTable.NAME_COLNAME,
                       MultiValueTable.VAL_COLNAME_FMT.format(int.__name__),
                       MultiValueTable.VAL_COLNAME_FMT.format(float.__name__),
                       MultiValueTable.VAL_COLNAME_FMT.format(str.__name__),
                       MultiValueTable.VAL_COLNAME_FMT.format(bool.__name__)]
            data = [[name, None, None, None, None]]
            if type(value) is int:
                data[0][1] = value
            elif type(value) is float:
                data[0][2] = value
            elif type(value) is str:
                data[0][3] = value
            elif type(value) is bool:
                data[0][4] = value
            conflict_columns = [MultiValueTable.NAME_COLNAME]
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
WHERE {MultiValueTable.NAME_COLNAME} = '{name}';
"""
            for t in DICT_TABLES.values():
                sql += f"""
DELETE FROM {t.name}
WHERE {MultiValueTable.NAME_COLNAME} = '{name}';
"""
        elif type(values) is list:
            val_type = self._get_type(values)
            sql = f"""
DELETE FROM {DICT_TABLES[val_type].name}
WHERE {MultiValueTable.NAME_COLNAME} = '{name}'
AND {DictTable.KEY_COLNAME}
IN ({', '.join([self._to_sql_value(v) for v in values])});
"""
        else:
            raise Exception(UNSUPPORTED_TYPE_MESSAGE)
        self._execute_sql(sql)

    def find_all(self):
        atomic_sql = f"""
SELECT {MultiValueTable.NAME_COLNAME}, \
{MultiValueTable.VAL_COLNAME_FMT.format(int.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(float.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(str.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(bool.__name__)}
FROM {ATOMIC_TABLE.name};
"""
        int_sql = f"""
SELECT {MultiValueTable.NAME_COLNAME}, \
{DictTable.KEY_COLNAME}, \
{MultiValueTable.VAL_COLNAME_FMT.format(int.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(float.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(str.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(bool.__name__)}
FROM {DICT_TABLES[int].name}
"""
        float_sql = f"""
SELECT {MultiValueTable.NAME_COLNAME}, \
{DictTable.KEY_COLNAME}, \
{MultiValueTable.VAL_COLNAME_FMT.format(int.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(float.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(str.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(bool.__name__)}
FROM {DICT_TABLES[float].name}
"""
        str_sql = f"""
SELECT {MultiValueTable.NAME_COLNAME}, \
{DictTable.KEY_COLNAME}, \
{MultiValueTable.VAL_COLNAME_FMT.format(int.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(float.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(str.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(bool.__name__)}
FROM {DICT_TABLES[str].name}
"""
        bool_sql = f"""
SELECT {MultiValueTable.NAME_COLNAME}, \
{DictTable.KEY_COLNAME}, \
{MultiValueTable.VAL_COLNAME_FMT.format(int.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(float.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(str.__name__)}, \
{MultiValueTable.VAL_COLNAME_FMT.format(bool.__name__)}
FROM {DICT_TABLES[bool].name}
"""
        atomic_data = self._execute_sql(atomic_sql)
        int_data = self._execute_sql(int_sql)
        float_data = self._execute_sql(float_sql)
        str_data = self._execute_sql(str_sql)
        bool_data = self._execute_sql(bool_sql)
        obj = {}
        for d in atomic_data:
            if d[1] is not None:
                obj[d[0]] = int(d[1])
            elif d[2] is not None:
                obj[d[0]] = float(d[2])
            elif d[3] is not None:
                obj[d[0]] = str(d[3])
            elif d[4] is not None:
                obj[d[0]] = bool(d[4])
        for d in int_data:
            key = int(d[1])
            val = None
            if d[2] is not None:
                val = int(d[2])
            elif d[3] is not None:
                val = float(d[3])
            elif d[4] is not None:
                val = str(d[4])
            elif d[5] is not None:
                val = bool(d[5])
            if d[0] not in obj:
                obj[d[0]] = [] if val is None else {}
            if val is None:
                obj[d[0]].append(key)
            else:
                obj[d[0]][key] = val
        for d in float_data:
            key = float(d[1])
            val = None
            if d[2] is not None:
                val = int(d[2])
            elif d[3] is not None:
                val = float(d[3])
            elif d[4] is not None:
                val = str(d[4])
            elif d[5] is not None:
                val = bool(d[5])
            if d[0] not in obj:
                obj[d[0]] = [] if val is None else {}
            if val is None:
                obj[d[0]].append(key)
            else:
                obj[d[0]][key] = val
        for d in str_data:
            key = str(d[1])
            val = None
            if d[2] is not None:
                val = int(d[2])
            elif d[3] is not None:
                val = float(d[3])
            elif d[4] is not None:
                val = str(d[4])
            elif d[5] is not None:
                val = bool(d[5])
            if d[0] not in obj:
                obj[d[0]] = [] if val is None else {}
            if val is None:
                obj[d[0]].append(key)
            else:
                obj[d[0]][key] = val
        for d in bool_data:
            key = bool(d[1])
            val = None
            if d[2] is not None:
                val = int(d[2])
            elif d[3] is not None:
                val = float(d[3])
            elif d[4] is not None:
                val = str(d[4])
            elif d[5] is not None:
                val = bool(d[5])
            if d[0] not in obj:
                obj[d[0]] = [] if val is None else {}
            if val is None:
                obj[d[0]].append(key)
            else:
                obj[d[0]][key] = val
        return obj


if __name__ == '__main__':
    # TODO: refactor, already tested
    for tab in TABLES:
        print(tab.create_script())
    adapter = PostgresAdapter()
    # adapter.save('timeout_arr', [1, 2, 3, 4, 5])
    # adapter.patch('timeout_arr', [1.2, 2.3, 4.5])
    # adapter.remove('timeout_arr')
    adapter.remove('timeout_arr', [3, 5])
    data = (adapter.find_all())
    print(data)
