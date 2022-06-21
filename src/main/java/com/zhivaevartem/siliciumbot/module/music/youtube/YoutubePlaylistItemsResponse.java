package com.zhivaevartem.siliciumbot.module.music.youtube;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class YoutubePlaylistItemsResponse {
  @Data
  public static class Item {
    @Data
    public static class ContentDetails {
      @JsonProperty("videoId")
      private String videoId;
      @JsonProperty("videoPublishedAt")
      private String videoPublishedAt;
    }

    @JsonProperty("kind")
    private String kind;
    @JsonProperty("etag")
    private String etag;
    @JsonProperty("id")
    private String id;
    @JsonProperty("contentDetails")
    private ContentDetails contentDetails;
  }

  @JsonProperty("kind")
  private String kind;
  @JsonProperty("etag")
  private String etag;
  @JsonProperty("nextPageToken")
  private String nextPageToken;
  @JsonProperty("items")
  private Item[] items;
}
