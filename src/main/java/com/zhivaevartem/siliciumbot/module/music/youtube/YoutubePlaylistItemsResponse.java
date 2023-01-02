package com.zhivaevartem.siliciumbot.module.music.youtube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

public class YoutubePlaylistItemsResponse {
  public static class Item {
    public static class ContentDetails {
      @JsonProperty("videoId")
      private String videoId;
      @JsonProperty("videoPublishedAt")
      private String videoPublishedAt;

      @JsonIgnore
      public String getVideoId() {
        return videoId;
      }

      @JsonIgnore
      public String getVideoPublishedAt() {
        return videoPublishedAt;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentDetails that = (ContentDetails) o;
        return Objects.equals(videoId, that.videoId) && Objects.equals(videoPublishedAt, that.videoPublishedAt);
      }

      @Override
      public int hashCode() {
        return Objects.hash(videoId, videoPublishedAt);
      }
    }

    @JsonProperty("kind")
    private String kind;
    @JsonProperty("etag")
    private String etag;
    @JsonProperty("id")
    private String id;
    @JsonProperty("contentDetails")
    private ContentDetails contentDetails;

    @JsonIgnore
    public String getKind() {
      return kind;
    }

    @JsonIgnore
    public String getEtag() {
      return etag;
    }

    @JsonIgnore
    public String getId() {
      return id;
    }

    @JsonIgnore
    public ContentDetails getContentDetails() {
      return contentDetails;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Item item = (Item) o;
      return Objects.equals(kind, item.kind) && Objects.equals(etag, item.etag) && Objects.equals(id, item.id) && Objects.equals(contentDetails, item.contentDetails);
    }

    @Override
    public int hashCode() {
      return Objects.hash(kind, etag, id, contentDetails);
    }
  }

  @JsonProperty("kind")
  private String kind;
  @JsonProperty("etag")
  private String etag;
  @JsonProperty("nextPageToken")
  private String nextPageToken;
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
  public Item[] getItems() {
    return items;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    YoutubePlaylistItemsResponse that = (YoutubePlaylistItemsResponse) o;
    return Objects.equals(kind, that.kind) && Objects.equals(etag, that.etag) && Objects.equals(nextPageToken, that.nextPageToken) && Arrays.equals(items, that.items);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(kind, etag, nextPageToken);
    result = 31 * result + Arrays.hashCode(items);
    return result;
  }
}
