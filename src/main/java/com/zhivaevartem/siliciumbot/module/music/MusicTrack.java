package com.zhivaevartem.siliciumbot.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import java.util.concurrent.ExecutionException;

public class MusicTrack {
  private String url;

  public String getName() {
    return url;
  }

  public void play(AudioPlayerManager playerManager, AudioLoadResultHandler scheduler) {
    try {
      playerManager.loadItem(this.url, scheduler).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  public MusicTrack(String url) {
    this.url = url;
  }
}
