package com.zhivaevartem.siliciumbot.module.music;

import org.springframework.lang.Nullable;

public class MusicTrackResponse {
  public enum Status {
    ADDED,
    PLAYING,
    SKIPPED,
    ERROR
  }

  private Status status;

  @Nullable
  private MusicTrack track;

  @Nullable
  private String name;

  public MusicTrackResponse(Status status, MusicTrack track) {
    this.status = status;
    this.track = track;
  }

  public MusicTrackResponse(Status status, String name) {
    this.status = status;
    this.name = name;
  }

  public String getMessage() {
    return switch (this.status) {
      case ADDED   -> "Added to queue";
      case PLAYING -> "Playing";
      case SKIPPED -> "Skipped";
      case ERROR   -> "Unexpected error playing";
    } + ": " + (this.track != null
      ? this.track.getName()
      : this.name);
  }
}
