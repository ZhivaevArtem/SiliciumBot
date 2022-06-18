package com.zhivaevartem.siliciumbot.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.voice.AudioProvider;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GuildMusicState {
  private String guildId;

  private Queue<MusicTrack> queue = new ConcurrentLinkedQueue<>();

  @Getter
  private AudioLoadResultHandler trackScheduler;

  @Getter
  private AudioPlayerManager playerManager;

  @Getter
  private AudioPlayer player;

  @Getter
  private AudioProvider provider;

  @Getter
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
    return this.queue.stream().toList();
  }

  @Nullable
  public MusicTrack pollNextTrack() {
    if (this.queue.size() > 0) {
      this.currentTrack = this.queue.poll();
      return this.currentTrack;
    }
    return null;
  }
}
