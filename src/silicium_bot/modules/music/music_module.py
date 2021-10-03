from discord.ext import commands, tasks
from queue import Queue
import re
import time

from vk_api.audio import VkAudio
from youtube_dl import YoutubeDL
import vk_api
import requests
import discord
from discord.utils import get
from discord import FFmpegPCMAudio
from discord import TextChannel
import traceback

from silicium_bot.store import Store
from silicium_bot.constants import Constants
from ..module_base import ModuleBase
        
guild = None

music_queue = Queue()
now_playing = None
vk_session = None
vk_audio = None

class MusicModule(ModuleBase):
    def __init__(self, bot):
        super().__init__(bot)

        players = {}

    def play_music(self, voice, url):
        global now_playing
        if 'youtube.com' in url:
            YDL_OPTIONS = {'format': 'bestaudio', 'noplaylist': 'False'}
            FFMPEG_OPTIONS = {
                'before_options': '-reconnect 1 -reconnect_streamed 1 -reconnect_delay_max 5', 'options': '-vn'}
            with YoutubeDL(YDL_OPTIONS) as ydl:
                info = ydl.extract_info(url, download=False)
            URL = info['url']
            voice.play(FFmpegPCMAudio(URL, **FFMPEG_OPTIONS))
            voice.is_playing()
            now_playing = url
        else: #vk
            audio = url
            voice.play(discord.FFmpegPCMAudio(audio['url']))
            voice.pause()
            time.sleep(3)
            voice.resume()
            now_playing = audio['artist'] + ' - ' + audio['title']


    @tasks.loop(seconds=2)
    async def queue_loop(self):
        voice = get(self.bot.voice_clients, guild=guild)
        if not voice:
            return
        if not music_queue.empty() and not voice.is_playing() and not voice.is_paused():
            track_url = music_queue.get_nowait()
            self.play_music(voice, track_url)

    # command for bot to join the channel of the user, if the bot has already joined and is in a different channel, it will move to the channel the user is in
    @commands.command()
    async def join(self, ctx, id=None):
        """Join to voice channel"""
        global guild
        guild = ctx.guild
        if id is None:
            channel = ctx.message.author.voice.channel
        else:
            channel = self.bot.get_channel(int(id))
        voice = get(self.bot.voice_clients, guild=ctx.guild)
        if voice and voice.is_connected():
            await voice.move_to(channel)
        else:
            voice = await channel.connect()
        if not self.queue_loop.is_running():
            self.queue_loop.start()


    @commands.command()
    async def say(self, ctx, *, text):
        """say smth"""
        await ctx.send(text)


    # command to play sound from a youtube URL
    @commands.command(aliases=["p"])
    async def play(self, ctx, *, request):
        """play music from youtube (url or search query)"""
        voice = get(self.bot.voice_clients, guild=ctx.guild)
        if not voice:
            await self.join(ctx)
            voice = get(self.bot.voice_clients, guild=ctx.guild)

        if request.startswith('http://') or request.startswith('https://'):
            url = request
        else:
            search_string = request.replace("&", "")
            response: requests.Response = requests.get(f"https://youtube.googleapis.com/youtube/v3/search?part=snippet&type=video&key={Constants.youtube_token}&q={search_string}&safeSearch=none&regionCode=RU&maxResults=1")
            if response.status_code // 100 != 2:
                await ctx.send("Not found")
                return
            data = response.json()
            try:
                video_id = data['items'][0]['id']['videoId']
            except Exception:
                await ctx.send("Not found")
                return
            url = f"https://www.youtube.com/watch?v={video_id}"
        if not voice.is_playing():
            try:
                self.play_music(voice, url)
            except Exception:
                await ctx.send("Not found")
                return
            await ctx.send('Bot is playing: ' + url)
        else:
            music_queue.put(url)
            await ctx.send("Added to queue: " + url)
            return

    # command to resume voice if it is paused
    @commands.command()
    async def resume(self, ctx):
        voice = get(self.bot.voice_clients, guild=ctx.guild)

        if not voice.is_playing():
            voice.resume()
            await ctx.send('Bot is resuming')

    # command to pause voice if it is playing
    @commands.command()
    async def pause(self, ctx):
        voice = get(self.bot.voice_clients, guild=ctx.guild)

        if voice and voice.is_playing():
            voice.pause()
            await ctx.send('Bot has been paused')

    @commands.command(aliases=["s", "sk", "skp"])
    async def skip(self, ctx):
        voice = get(self.bot.voice_clients, guild=ctx.guild)
        if voice and voice.is_playing():
            voice.stop()

    @commands.command(aliases=["q"])
    async def queue(self, ctx):
        l = []
        while not music_queue.empty():
            item = music_queue.get_nowait()
            l.append(item)
        for u in l:
            music_queue.put(u)
        if l:
            await ctx.send('\n'.join([i if 'youtube.com' in i else f"{i['artist']} - {i['title']}" for i in l]))
        else:
            await ctx.send("empty")

    @commands.command()
    async def clear(self, ctx):
        """Clear queue"""
        while not music_queue.empty():
            music_queue.get_nowait()

    @commands.command()
    async def phonk(self, ctx):
        """5 hours full of quality phonk"""
        await self.clear(ctx)
        await self.skip(ctx)
        await self.play(ctx, request="https://www.youtube.com/watch?v=yLsyTX5mNTk")
        await self.play(ctx, request="https://www.youtube.com/watch?v=cjAO7Y5WmwM")
        await self.play(ctx, request="https://www.youtube.com/watch?v=YxXTQg-vuJ4")
        await self.play(ctx, request="https://www.youtube.com/watch?v=vpkHAwhUIzY")
        await self.play(ctx, request="https://www.youtube.com/watch?v=leAJI9-sFug")

    @commands.command()
    async def mantra(self, ctx):
        """Most powerful mantra of Shiva"""
        await self.pidor(ctx)
        await self.play(ctx, request="https://www.youtube.com/watch?v=GBq_OgFf9YA")

    @commands.command()
    async def kish(self, ctx):
        """top 50 Korol + Joker tracks"""
        await self.pidor(ctx)
        await self.play(ctx, request="https://www.youtube.com/watch?v=0-C0lCPFTj8")

    @commands.command()
    async def reset(self, ctx):
        """Reset bot"""
        global players
        global guild
        global music_queue
        global vk_session
        global vk_audio
        global now_playing
        vk_session = None
        vk_audio = None
        now_playing = None
        try:
            voice = get(self.bot.voice_clients, guild=ctx.guild)
            if voice:
                voice.stop()
                await voice.disconnect()
        except Exception:
            pass
        players = {}
        guild = None
        music_queue = Queue()

    @commands.command()
    async def leave(self, ctx):
        """Leave from voice channel"""
        self.queue_loop.stop()
        voice = get(self.bot.voice_clients, guild=ctx.guild)
        while not music_queue.empty():
            music_queue.get_nowait()
        if voice and voice.is_playing():
            voice.stop()
        await voice.disconnect()

    @commands.command(aliases=['stop'])
    async def pidor(self, ctx):
        """= clear + skip"""
        await self.clear(ctx)
        await self.skip(ctx)


    async def force_playlist(self, ctx, urls):
        await self.pidor(ctx)
        for url in urls:
            await self.play(ctx, request=url)

    @commands.command()
    async def majula(self, ctx):
        await self.force_playlist(ctx, ["https://www.youtube.com/watch?v=z0nZAXyF2BU"])

    @commands.command(aliases=["pvk"])
    async def playvk(self, ctx, *, request):
        """Play music from vk (search query or id (format: <owner_id>_<audio_id>))"""
        voice = get(self.bot.voice_clients, guild=ctx.guild)
        if not voice:
            await self.join(ctx)
            voice = get(self.bot.voice_clients, guild=ctx.guild)
        global vk_session
        global vk_audio
        if not vk_session or not vk_audio:
            try:
                vk_session = vk_api.VkApi(Constants.vk_login, Constants.vk_password)
                vk_session.auth()
                vk_audio = VkAudio(vk_session)
            except Exception:
                print("vk login error")
                traceback.format_exc()                
        if re.match(r"-?[0-9]+_-?[0-9]+", request):
            ownerid, songid = request.split('_')
            aud = vk_audio.get_audio_by_id(ownerid, songid)
        else:
            audios = list(vk_audio.search(request, 10))
            aud = audios[0]
        # url = aud['url']

        if not voice.is_playing():
            try:
                self.play_music(voice, aud)
            except Exception:
                await ctx.send("Not found")
                return
            await ctx.send('Bot is playing: ' + aud['artist'] + ' - ' + aud['title'])
        else:
            music_queue.put(aud)
            await ctx.send("Added to queue: " + aud['artist'] + ' - ' + aud['title'])
            return


    @commands.command("now", aliases=["nowplaying", "np"])
    async def now_playing(self, ctx):
        """What song is playing right now"""
        global now_playing
        voice = get(self.bot.voice_clients, guild=ctx.guild)
        if voice:
            await ctx.send("Now playing: " + now_playing)
        else:
            await ctx.send("Silence...")

