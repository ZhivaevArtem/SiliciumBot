import json
import requests
from os import path
from bs4 import BeautifulSoup


def _retrieve_html(url):
    headers = {'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'}
    return requests.get(url, headers=headers).content.decode('utf-8')

def _parse_shiki_logs(html):
    soup = BeautifulSoup(html, features="lxml")
    divs = soup.select('div.b-user_rate_log')
    result = []
    for div in divs:
        d = {
            "id": div.select('span a')[0].text,
            "action": div.select('span.action')[0].text,
            "title": div.select('span a')[1].text,
            "misc": json.loads(div.select('div.spoiler.target code')[0].text)
        }
        result.append(d)
    return result

def get_resource_path():
    resources_path = path.dirname(__file__)
    resources_path = path.dirname(resources_path)
    resources_path = path.dirname(resources_path)
    resources_path = path.join(resources_path, 'resources')
    return resources_path

def _load_fetched_ids():
    file_path = path.join(get_resource_path(), 'fetched_ids.json')
    try:
        with open(file_path, 'r') as f:
            return json.load(f)
    except Exception as e:
        print(e)
        return {}

def _store_fetched_ids(grouped_logs):
    file_path = path.join(get_resource_path(), 'fetched_ids.json')
    fetched = {}
    try:
        with open(file_path, 'r') as f:
            fetched = json.load(f)
            for username, user_logs in grouped_logs.items():
                if (not fetched[username]):
                    fetched[username] = []
                for user_log in user_logs:
                    if (user_log['id'] not in fetched[username]):
                        fetched[username].append(user_log['id'])
    except Exception as e:
        print(e)
        fetched = {}
        for username, user_logs in grouped_logs.items():
            fetched[username] = [i['id'] for i in user_logs]
    try:
        with open(file_path, 'w') as f:
            json.dump(fetched, f, indent=4)
    except Exception as e:
        print(e)

def _retrieve_logs_by_usernames(usernames):
    grouped_logs = {}
    for username in usernames:
        try:
            html = _retrieve_html(f"https://shikimori.one/{username}/history/logs")
            logs = _parse_shiki_logs(html)
            grouped_logs[username] = logs
        except Exception as e:
            print(e)
    return grouped_logs

def retrieve_new_logs_by_usernames(usernames):
    fetched_ids = _load_fetched_ids()
    grouped_logs = _retrieve_logs_by_usernames(usernames)
    _store_fetched_ids(grouped_logs)
    for username, user_logs in grouped_logs.items():
        if username in fetched_ids.keys():
            user_logs_to_delete_indexes = []
            for i in range(len(user_logs)):
                user_log = user_logs[i]
                if user_log['id'] in fetched_ids[username]:
                    user_logs_to_delete_indexes.append(i)
            i = 0
            for index in user_logs_to_delete_indexes:
                del grouped_logs[username][index - i]
                i += 1
    return grouped_logs
