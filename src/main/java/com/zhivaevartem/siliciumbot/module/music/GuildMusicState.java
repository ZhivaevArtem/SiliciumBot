package com.zhivaevartem.siliciumbot.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.voice.AudioProvider;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

public class GuildMusicState {
  private String guildId;

  private Queue<MusicTrack> queue = new ConcurrentLinkedQueue<>();

  private AudioLoadResultHandler trackScheduler;

  private AudioPlayerManager playerManager;

  private AudioPlayer player;

  private AudioProvider provider;

  private MusicTrack currentTrack;

  public GuildMusicState(String guildId) {
    this.guildId = guildId;
    this.playerManager = new DefaultAudioPlayerManager();
    this.playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
    AudioSourceManagers.registerRemoteSources(playerManager);
    this.player = this.playerManager.createPlayer();
    this.provider = new LavaPlayerAudioProvider(this.player);
    this.trackScheduler = new TrackScheduler(this.player);
  }

  public void addTrack(MusicTrack track) {
    this.queue.add(track);
  }

  public List<MusicTrack> getQueue() {
    return this.queue.stream().collect(Collectors.toList());
  }

  @Nullable
  public MusicTrack pollNextTrack() {
    if (this.queue.size() > 0) {
      this.currentTrack = this.queue.poll();
      return this.currentTrack;
    }
    return null;
  }

  public void clearQueue() {
    this.queue.clear();
  }

  public AudioLoadResultHandler getTrackScheduler() {
    return trackScheduler;
  }

  public AudioPlayerManager getPlayerManager() {
    return playerManager;
  }

  public AudioPlayer getPlayer() {
    return player;
  }

  public AudioProvider getProvider() {
    return provider;
  }

  public MusicTrack getCurrentTrack() {
    return currentTrack;
  }
}
