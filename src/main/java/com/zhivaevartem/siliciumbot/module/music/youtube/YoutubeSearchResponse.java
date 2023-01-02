package com.zhivaevartem.siliciumbot.module.music.youtube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

public class YoutubeSearchResponse {
  public static class PageInfo {
    @JsonProperty("totalResults")
    private Integer totalResults;
    @JsonProperty("resultsPerPage")
    private Integer resultsPerPage;

    @JsonIgnore
    public Integer getTotalResults() {
      return totalResults;
    }

    @JsonIgnore
    public Integer getResultsPerPage() {
      return resultsPerPage;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      PageInfo pageInfo = (PageInfo) o;
      return Objects.equals(totalResults, pageInfo.totalResults) && Objects.equals(resultsPerPage, pageInfo.resultsPerPage);
    }

    @Override
    public int hashCode() {
      return Objects.hash(totalResults, resultsPerPage);
    }
  }

  public static class Item {
    public static class Id {
      @JsonProperty("kind")
      private String kind;
      @JsonProperty("videoId")
      private String videoId;

      @JsonIgnore
      public String getKind() {
        return kind;
      }

      @JsonIgnore
      public String getVideoId() {
        return videoId;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id = (Id) o;
        return Objects.equals(kind, id.kind) && Objects.equals(videoId, id.videoId);
      }

      @Override
      public int hashCode() {
        return Objects.hash(kind, videoId);
      }
    }

    @JsonProperty("kind")
    private String kind;
    @JsonProperty("etag")
    private String etag;
    @JsonProperty("id")
    private Id id;

    @JsonIgnore
    public String getKind() {
      return kind;
    }

    @JsonIgnore
    public String getEtag() {
      return etag;
    }

    @JsonIgnore
    public Id getId() {
      return id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Item item = (Item) o;
      return Objects.equals(kind, item.kind) && Objects.equals(etag, item.etag) && Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(kind, etag, id);
    }
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

  @JsonIgnore
  public String getKind() {
    return kind;
  }

  @JsonIgnore
  public String getEtag() {
    return etag;
  }

  @JsonIgnore
  public String getNextPageToken() {
    return nextPageToken;
  }

  @JsonIgnore
  public String getRegionCode() {
    return regionCode;
  }

  @JsonIgnore
  public PageInfo getPageInfo() {
    return pageInfo;
  }

  @JsonIgnore
  public Item[] getItems() {
    return items;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YoutubeSearchResponse that = (YoutubeSearchResponse) o;
    return Objects.equals(kind, that.kind) && Objects.equals(etag, that.etag) && Objects.equals(nextPageToken, that.nextPageToken) && Objects.equals(regionCode, that.regionCode) && Objects.equals(pageInfo, that.pageInfo) && Arrays.equals(items, that.items);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(kind, etag, nextPageToken, regionCode, pageInfo);
    result = 31 * result + Arrays.hashCode(items);
    return result;
  }
}
