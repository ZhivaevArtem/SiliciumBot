import os

import psycopg2


class DatabaseAdapter(object):
    def __init__(self):
        self._pg_connection = None

    # region private

    def _execute_sql(self, sql: str):
        with self._pg_connection.cursor() as cursor:
            print(f'Executing SQL:\n{sql}')
            cursor.execute(sql)
            self._pg_connection.commit()
            if sql.startswith('SELECT'):
                return cursor.fetchall()

    # endregion private

    # region public

    def connect(self):
        """Use DATABASE_URL environment variable to connect"""
        url = os.getenv('DATABASE_URL')
        print('Connecting to database')
        self._pg_connection = psycopg2.connect(url)
        return self

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
                    values.append(f"'{val}'")
                elif type(val) == bool:
                    values.append('true' if val else 'false')
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
