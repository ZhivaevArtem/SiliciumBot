from os import system
from shikilogsparser import retrieve_new_logs_by_usernames
from os import path
import time
from datetime import datetime


if __name__ == "__main__":
    usernames = []
    resources_path = path.dirname(__file__)
    resources_path = path.dirname(resources_path)
    resources_path = path.join(resources_path, 'resources', 'usernames.txt')
    with open(resources_path, 'r') as file:
        for line in file:
            line = line.strip('\r\n \t')
            if (line):
                usernames.append(line)
    while True:
        grouped_logs = retrieve_new_logs_by_usernames(usernames)
        for username, user_logs in grouped_logs.items():
            if user_logs:
                print(datetime.now())
                print(grouped_logs)
                break
        time.sleep(5 * 60)
