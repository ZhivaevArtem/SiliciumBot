from .logger import Logger

database_logger = Logger("postgres")
shiki_logger = Logger("shiki")
exception_logger = Logger("exception")
main_logger = Logger("")
