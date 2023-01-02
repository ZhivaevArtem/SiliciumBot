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
    String message = "";
    switch (this.status) {
      case ADDED   : message = "Added to queue"; break;
      case PLAYING : message = "Playing"; break;
      case SKIPPED : message = "Skipped"; break;
      default      : message = "Unexpected error playing"; break;
    }
    message += ": " + (this.track != null
      ? this.track.getName()
      : this.name);
    return message;
  }
}
