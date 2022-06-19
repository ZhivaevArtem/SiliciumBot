package com.zhivaevartem.siliciumbot.module.music.youtube;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class YoutubeSearchResponse {
  @Data
  public static class PageInfo {
    @JsonProperty("totalResults")
    private Integer totalResults;
    @JsonProperty("resultsPerPage")
    private Integer resultsPerPage;
  }

  @Data
  public static class Item {
    @Data
    public static class Id {
      @JsonProperty("kind")
      private String kind;
      @JsonProperty("videoId")
      private String videoId;
    }

    @JsonProperty("kind")
    private String kind;
    @JsonProperty("etag")
    private String etag;
    @JsonProperty("id")
    private Id id;
  }

  @JsonProperty("kind")
  private String kind;
  @JsonProperty("etag")
  private String etag;
  @JsonProperty("nextPageToken")
  private String nextPageToken;
  @JsonProperty("regionCode")
  private String regionCode;
  @JsonProperty("pageInfo")
  private PageInfo pageInfo;
  @JsonProperty("items")
  private Item[] items;
}
