import json
import requests
from bs4 import BeautifulSoup

URL = "https://shikimori.one/Rougline/history/logs"

def retrieve_html(url):
    headers = {'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'}
    try:
        return requests.get(url, headers=headers).content.decode('utf-8')
    except:
        return ""

def parse_html_for_logs(html):
    soup = BeautifulSoup(html, features="lxml")
    divs = soup.select('div.b-user_rate_log')
    result = []
    for div in divs:
        result.append({
            "id": div.select('span a')[0].text,
            "action": div.select('span.action')[0].text,
            "title": div.select('span a')[1].text,
            "misc": json.loads(div.select('div.spoiler.target code')[0].text)
        })
    return result


if __name__ == '__main__':
    html = retrieve_html(URL)
    for i in parse_html_for_logs(html):
        print(i)
