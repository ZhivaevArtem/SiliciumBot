import decimal

import psycopg2

from silicium_bot.constants import Constants
from silicium_bot.logger import database_logger as logger
from silicium_bot.store.database_adapter_base import DatabaseAdapterBase


class Column(object):
    def __init__(self, name, type, *, nullable=True, primary=False):
        self.name = name
        self.type = type
        self.primary = primary
        self.nullable = nullable
        if self.primary:
            self.nullable = False

    def create_script(self):
        db_types = {
            int: 'numeric(31)',
            str: 'character varying(255)',
            bool: 'boolean',
            float: 'real'
        }
        sql = f"{self.name} {db_types[self.type]}"
        if not self.nullable:
            sql += " NOT NULL"
        return sql


class Table(object):
    def __init__(self, name, columns):
        self.name = "silbot_" + name
        self.columns = columns

    def create_script(self):
        id_columns = [c.name for c in self.columns if c.primary]
        newline = ',\n'
        sql = f"""
CREATE TABLE IF NOT EXISTS {self.name} (
{newline.join([c.create_script() for c in self.columns])},
CONSTRAINT {self.name}_pkey PRIMARY KEY ({', '.join(id_columns)})
);
""".strip()
        return sql


class MultiValueTable(Table):
    ID_COLUMN = Column("value_id", str, primary=True)
    VAL_COLUMNS = {
        str: Column("str_value", str),
        int: Column("int_value", int),
        float: Column("float_value", float),
        bool: Column("bool_value", bool)
    }

    def __init__(self, name="", columns=[]):
        prefix = "store"
        if len(name) > 0:
            prefix += "_"
        mv_columns = [
            MultiValueTable.ID_COLUMN
        ] + list(MultiValueTable.VAL_COLUMNS.values())
        super().__init__(prefix + name, columns + mv_columns)


class DictTable(MultiValueTable):
    KEY_COLUMN_NAME = "dict_key"

    def __init__(self, type):
        key_column = Column(DictTable.KEY_COLUMN_NAME, type,
                            primary=True)
        super().__init__(type.__name__, [key_column])


ATOMIC_TABLE = MultiValueTable()
DICT_TABLES = {
    str: DictTable(str),
    int: DictTable(int),
    float: DictTable(float),
    bool: DictTable(bool)
}
TABLES = [ATOMIC_TABLE] + list(DICT_TABLES.values())


class PostgresAdapter(DatabaseAdapterBase):
    def __init__(self):
        self._url = Constants.database_url
        self._init_db()

    def _init_db(self):
        logger.log("Initialization database")
        init_sqls = [table.create_script() for table in TABLES]
        self._execute_sql(*init_sqls)

    def _execute_sql(self, *args):
        data = []
        with psycopg2.connect(self._url) as conn:
            with conn.cursor() as cur:
                for sql in args:
                    logger.log("Executing sql:")
                    logger.log(sql)
                    cur.execute(sql)
                    try:
                        data.append(cur.fetchall())
                        logger.log("Result of sql execution:")
                        logger.log(data[-1])
                    except psycopg2.ProgrammingError:
                        data.append(None)
                conn.commit()
        return data

    def _to_sql_value(self, value):
        if type(value) is str:
            s = value.replace("'", "''")
            return f"'{s}'"
        elif value is None:
            return "NULL"
        elif type(value) in (str, int, float, bool):
            return f"{value}"
        raise Exception()

    def _from_sql_value(self, value):
        if type(value) is decimal.Decimal:
            return int(value)
        elif type(value) in (str, int, float, bool):
            return value
        raise Exception()

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

    def find_all(self):
        atomic_sql = f"""
SELECT {MultiValueTable.ID_COLUMN.name},
{MultiValueTable.VAL_COLUMNS[str].name},
{MultiValueTable.VAL_COLUMNS[int].name},
{MultiValueTable.VAL_COLUMNS[float].name},
{MultiValueTable.VAL_COLUMNS[bool].name}
FROM {ATOMIC_TABLE.name};
""".strip()

        def get_dict_select_sql(type):
            sql = f"""
SELECT {MultiValueTable.ID_COLUMN.name},
{MultiValueTable.VAL_COLUMNS[str].name},
{MultiValueTable.VAL_COLUMNS[int].name},
{MultiValueTable.VAL_COLUMNS[float].name},
{MultiValueTable.VAL_COLUMNS[bool].name},
{DictTable.KEY_COLUMN_NAME}
FROM {DICT_TABLES[type].name};
""".strip()
            return sql

        dict_str_sql = get_dict_select_sql(str)
        dict_int_sql = get_dict_select_sql(int)
        dict_float_sql = get_dict_select_sql(float)
        dict_bool_sql = get_dict_select_sql(bool)
        sql_data = self._execute_sql(atomic_sql, dict_str_sql, dict_int_sql,
                                     dict_float_sql, dict_bool_sql)
        dict_data = {
            str: sql_data[1],
            int: sql_data[2],
            float: sql_data[3],
            bool: sql_data[4]
        }
        obj = {}
        for d in sql_data[0]:
            id = d[0]
            value = None
            for i in range(1, len(d)):
                if d[i] is not None:
                    value = self._from_sql_value(d[i])
                    break
            obj[id] = value
        for t, data in dict_data.items():
            for d in data:
                id = d[0]
                key = t(d[-1])
                value = None
                for i in range(1, len(d) - 1):
                    if d[i] is not None:
                        value = self._from_sql_value(d[i])
                        break
                if id not in obj:
                    obj[id] = {}
                obj[id][key] = value
        res_obj = {}
        for obj_k, obj_v in obj.items():
            if type(obj_v) is dict \
               and len([v for v in obj_v.values() if v is not None]) == 0:
                res_obj[obj_k] = list(obj_v.keys())
            else:
                res_obj[obj_k] = obj_v
        return res_obj

    def patch(self, id, value):
        if type(value) in (str, int, float, bool):
            table = ATOMIC_TABLE.name
            columns = [
                MultiValueTable.ID_COLUMN.name,
                MultiValueTable.VAL_COLUMNS[str].name,
                MultiValueTable.VAL_COLUMNS[int].name,
                MultiValueTable.VAL_COLUMNS[float].name,
                MultiValueTable.VAL_COLUMNS[bool].name,
            ]
            conflict_columns = [MultiValueTable.ID_COLUMN.name]
            data = [[id, None, None, None, None]]
            if type(value) is str:
                data[0][1] = value
            elif type(value) is int:
                data[0][2] = value
            elif type(value) is float:
                data[0][3] = value
            elif type(value) is bool:
                data[0][4] = value
        elif type(value) is dict or type(value) is list:
            if len(value) == 0:
                self.remove(id)
                return
            key_type = None
            val_type = None
            if type(value) is dict:
                key_type = type(list(value.keys())[0])
                val_type = type(list(value.values())[0])
            elif type(value) is list:
                key_type = type(value[0])
            table = DICT_TABLES[key_type].name
            columns = [
                MultiValueTable.ID_COLUMN.name,
                MultiValueTable.VAL_COLUMNS[str].name,
                MultiValueTable.VAL_COLUMNS[int].name,
                MultiValueTable.VAL_COLUMNS[float].name,
                MultiValueTable.VAL_COLUMNS[bool].name,
                DictTable.KEY_COLUMN_NAME
            ]
            conflict_columns = [MultiValueTable.ID_COLUMN.name,
                                DictTable.KEY_COLUMN_NAME]
            data = [[id, None, None, None, None, k] for k in value]
            if type(value) is dict:
                i = None
                if val_type is str:
                    i = 1
                elif val_type is int:
                    i = 2
                elif val_type is float:
                    i = 3
                elif val_type is bool:
                    i = 4
                if i is not None:
                    for v in data:
                        v[i] = value[v[-1]]
        else:
            raise Exception()
        newline = ',\n'
        sql = f"""
INSERT INTO {table}({', '.join(columns)}) VALUES
{newline.join(['(' + ', '.join(
            [self._to_sql_value(f) for f in d]) + ')' for d in data])}
ON CONFLICT ({', '.join(conflict_columns)}) DO UPDATE SET
{newline.join([f"{c} = excluded.{c}" for c in columns])};
""".strip()
        self._execute_sql(sql)

    def remove(self, id, keys=None):
        if keys is None:
            sqls = [f"""
DELETE FROM {t.name}
WHERE {MultiValueTable.ID_COLUMN.name} = {self._to_sql_value(id)};
""".strip() for t in TABLES]
            self._execute_sql(*sqls)
        elif len(keys) > 0:
            table = DICT_TABLES[type(keys[0])]
            sql = f"""
DELETE FROM {table.name}
WHERE {MultiValueTable.ID_COLUMN.name} = {self._to_sql_value(id)}
AND {DictTable.KEY_COLUMN_NAME}
IN ({', '.join([self._to_sql_value(k) for k in keys])});
""".strip()
            self._execute_sql(sql)
