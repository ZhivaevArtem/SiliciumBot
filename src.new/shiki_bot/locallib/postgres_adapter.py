import psycopg2
import os


class PostgresAdapter(object):
    def __init__(self):
        super().__init__()
        self._pg_connection: psycopg2.connection

    # region private

    def _execute_sql(self, sql: str):
        cursor = None
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
        self._pg_connection = psycopg2.connect(url, sslmode="require")
        return self

    def insert_data_distinct(self, table: str, columns: list,
                             data: list) -> None:
        if not data:
            return
        sql = f"INSERT INTO {table}({', '.join(columns)}) VALUES\n"
        values_sets = []
        for d in data:
            values_set = "("
            values = []
            for val in d:
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
        for i in range(1, len(columns)):
            column = columns[i]
            set_eval = f"{column} = excluded.{column}"
            set_evals.append(set_eval)
        sql += ",\n".join(set_evals)
        sql += "\n;\n"
        self._execute_sql(sql)

    def fetch_data(self, table: str, columns: list) -> tuple:
        sql = f"SELECT {', '.join(columns)}\nFROM {table};\n"
        data = self._execute_sql(sql)
        return data

    def remove_data_by_ids(self, table: str, id_column: str, ids: list):
        sql = f"DELETE FROM {table}\n"
        ids_form = [f"'{s}'" for s in ids]
        sql += f"WHERE {id_column} IN ({', '.join(ids_form)});\n"
        self._execute_sql(sql)

    def update_table(self, table: str, columns: list[str], data: list[list]):
        self.truncate_tables([table])
        self.insert_data_distinct(table, columns, data)

    def truncate_table(self, table):
        sql = f"TRUNCATE TABLE {table};\n"
        self._execute_sql(sql)

    def fetch_table_names(self) -> list[str]:
        sql = "SELECT table_name FROM information_schema.tables\n"
        sql += "WHERE table_schema = 'public';\n"
        data = self._execute_sql(sql)
        return [d[0] for d in data]

    # endregion public
