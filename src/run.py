from os import system
from shikilogsparser import retrieve_new_logs_by_usernames
from os import path


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
    grouped_logs = retrieve_new_logs_by_usernames(usernames)
    print(grouped_logs)
