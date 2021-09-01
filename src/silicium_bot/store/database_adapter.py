import traceback

import psycopg2

from silicium_bot.globals import G


class DatabaseAdapter(object):

    # region private

    def _execute_sql(self, sql: str):
        try:
            with self._pg_connection.cursor() as cursor:
                print(f'Executing SQL:\n{sql}')
                cursor.execute(sql)
                self._pg_connection.commit()
                if sql.startswith('SELECT'):
                    data = cursor.fetchall()
                    print(data)
                    return data
        except psycopg2.Error:
            if self._pg_connection is not None \
               and not self._pg_connection.closed:
                self._pg_connection.close()
            print(traceback.format_exc())
            self._connect()

    def _connect(self):
        print('Connecting to database')
        self._pg_connection = psycopg2.connect(G.DATABASE_URL)
        return self

    # region magic

    def __init__(self):
        self._pg_connection = None
        self._connect()

    # endregion magic

    # endregion private

    # region public

    def insert_data_distinct(self, table: str, columns: list[str],
                             data: list[list]) -> None:
        if not data:
            return
        sql = f"INSERT INTO {table}({', '.join(columns)}) VALUES\n"
        values_sets = []
        for row in data:
            values_set = "("
            values = []
            for val in row:
                if val is None:
                    values.append('NULL')
                elif type(val) == int:
                    values.append(str(val))
                elif type(val) == str:
                    s = val.replace("'", "''")
                    values.append(f"'{s}'")
                elif type(val) == bool:
                    values.append(str(val).lower())
                elif type(val) == float:
                    values.append(str(val))
            values_set += ", ".join(values) + ")"
            values_sets.append(values_set)
        sql += ",\n".join(values_sets)
        sql += f"\nON CONFLICT ({columns[0]}) DO UPDATE\n"
        sql += f"SET\n"
        set_evals = []
        for i in range(0, len(columns)):
            column = columns[i]
            set_eval = f"{column} = excluded.{column}"
            set_evals.append(set_eval)
        sql += ",\n".join(set_evals)
        sql += "\n;\n"
        self._execute_sql(sql)

    def fetch_data(self, table: str, columns: list[str]) -> list[list]:
        sql = f"SELECT {', '.join(columns)}\nFROM {table};\n"
        data = self._execute_sql(sql)
        return data

    def remove_data_by_ids(self, table: str, id_column: str, ids: list[str]):
        sql = f"DELETE FROM {table}\n"
        ids_form = [f"'{s}'" for s in ids]
        sql += f"WHERE {id_column} IN ({', '.join(ids_form)});\n"
        self._execute_sql(sql)

    def update_table(self, table: str, columns: list[str],
                     data: list[list]) -> None:
        self.truncate_table(table)
        self.insert_data_distinct(table, columns, data)

    def truncate_table(self, table: str):
        sql = f"TRUNCATE TABLE {table};\n"
        self._execute_sql(sql)

    def fetch_table_names(self) -> list[str]:
        sql = "SELECT table_name FROM information_schema.tables\n"
        sql += "WHERE table_schema = 'public';\n"
        data = self._execute_sql(sql)
        return [str(d[0]) for d in data]

    # endregion public

    def save(self, key: str, value):
        type_map = {
            int: 'int',
            float: 'float',
            bool: 'bool',
            str: 'str'
        }
        if type(value) == list:
            if len(value) == 0:
                return
            for v in value:
                val_type = type_map[type(v)]
                table = f"arr_{val_type}_{key}_botdata_"
                columns = ["val_"]
                data = [[d] for d in value]
                break
        elif type(value) == dict:
            if len(value) == 0:
                return
            for k, v in value.items():
                key_type = type_map[type(value)]
                val_type = type_map[type(value)]
                table = f"dic_{key_type}_{val_type}_{key}_botdata_"
                columns = ["key_", "val_"]
                data = [[k, v] for k, v in value.items()]
                break
        else:
            val_type = type_map[type(value)]
            table = f"botdata_"
            columns = ["key_", f"{val_type}_"]
            data = [key, value]

        self.insert_data_distinct(table, columns, data)

    def find_all(self):
        type_map = {
            'int': int,
            'str': str,
            'bool': bool,
            'float': float
        }
        obj = {}
        tables = self.fetch_table_names()
        for table in tables:
            if not table.endswith("botdata_"):
                continue
            if table == 'botdata_':
                columns = ['key_', 'int_val_', 'str_val_',
                           'bool_val_', 'float_val_']
                data = self.fetch_data(table, columns)
                for d in data:
                    if d[1] is not None:
                        obj[d[0]] = int(d[1])
                    elif d[2] is not None:
                        obj[d[0]] = str(d[2])
                    elif d[3] is not None:
                        obj[d[0]] = bool(d[3])
                    elif d[4] is not None:
                        obj[d[0]] = float(d[4])
            elif table.startswith('arr_'):
                # arr_<val type>_<prop name>_botdata_
                split = table.split('_')
                val_type = type_map[split[1]]
                prop_name = split[2]
                data = self.fetch_data(table, ["val_"])
                data_arr = [val_type(d[0]) for d in data]
                obj[prop_name] = data_arr
            elif table.startswith('dic_'):
                # dic_<key type>_<val type>_<prop name>_botdata_
                split = table.split('_')
                key_type = type_map[split[1]]
                val_type = type_map[split[2]]
                prop_name = split[3]
                data = self.fetch_data(table, ["key_", "val_"])
                data_dic = {key_type(d[0]): val_type(d[1]) for d in data}
                obj[prop_name] = data_dic
        return obj
