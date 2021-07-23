import asyncio
from asyncio import tasks
import discord
import time
from discord.enums import Status
from os import path
from shikilogsparser import retrieve_new_logs_by_usernames


client = discord.Client()
usernames = []
resources_path = path.dirname(__file__)
resources_path = path.dirname(resources_path)
resources_path = path.join(resources_path, 'resources', 'usernames.txt')
with open(resources_path, 'r') as file:
    for line in file:
        line = line.strip('\r\n \t')
        if (line):
            usernames.append(line)

async def start_watch():
    global usernames
    global channel
    while True:
        print('while')
        grouped_logs = retrieve_new_logs_by_usernames(usernames)
        for username, user_logs in grouped_logs.items():
            if user_logs:
                print(grouped_logs)
                if channel:
                    await channel.send(str(grouped_logs))
                break
        time.sleep(5)

@client.event
async def on_ready():
    global client
    global usernames
    await client.change_presence(status=Status.invisible)
    print('start')

@client.event
async def on_message(message):
    global channel
    if message.content == ';isalive':
        await message.channel.send('im okay')
    if message.content == ';start':
        print(123)
        await message.channel.send('123')
        channel = message.channel
        client.loop.create_task(start_watch())
        asyncio.run(start_watch())
        await message.channel.send('321')


client.run('NDgyNDU1NDY0NTI1NjkyOTI4.W3-3qg.qU1Mitevhe0du50vY1Buo0tBCJU')


