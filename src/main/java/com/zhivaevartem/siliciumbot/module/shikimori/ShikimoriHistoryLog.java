package com.zhivaevartem.siliciumbot.module.shikimori;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * History log model received from shikimori API:
 * <code>
 *  https://shikimori.one/api/users/{username}/history
 * </code>.
 */
public class ShikimoriHistoryLog {
  /**
   * {@link ShikimoriHistoryLog} nested field model.
   */
  public static class Target {
    /**
     * {@link ShikimoriHistoryLog} nested field model.
     */
    public static class Image {
      @JsonProperty("original")
      private String original;

      @JsonProperty("preview")
      private String preview;

      @JsonProperty("x96")
      private String x96;

      @JsonProperty("x48")
      private String x48;

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(original, image.original) && Objects.equals(preview, image.preview) && Objects.equals(x96, image.x96) && Objects.equals(x48, image.x48);
      }

      @Override
      public int hashCode() {
        return Objects.hash(original, preview, x96, x48);
      }

      @JsonIgnore
      public String getOriginal() {
        return original;
      }

      @JsonIgnore
      public String getPreview() {
        return preview;
      }

      @JsonIgnore
      public String getX96() {
        return x96;
      }

      @JsonIgnore
      public String getX48() {
        return x48;
      }
    }

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("russian")
    private String russian;

    @JsonProperty("image")
    private Image image;

    @JsonProperty("url")
    private String url;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("score")
    private String score;

    @JsonProperty("status")
    private String status;

    @JsonProperty("episodes")
    private Integer episodes;

    @JsonProperty("episodes_aired")
    private Integer episodesAired;

    @JsonProperty("aired_on")
    private String airedOn;

    @JsonProperty("released_on")
    private String releasedOn;

    @JsonIgnore
    public Long getId() {
      return id;
    }

    @JsonIgnore
    public String getName() {
      return name;
    }

    @JsonIgnore
    public String getRussian() {
      return russian;
    }

    @JsonIgnore
    public Image getImage() {
      return image;
    }

    @JsonIgnore
    public String getUrl() {
      return url;
    }

    @JsonIgnore
    public String getKind() {
      return kind;
    }

    @JsonIgnore
    public String getScore() {
      return score;
    }

    @JsonIgnore
    public String getStatus() {
      return status;
    }

    @JsonIgnore
    public Integer getEpisodes() {
      return episodes;
    }

    @JsonIgnore
    public Integer getEpisodesAired() {
      return episodesAired;
    }

    @JsonIgnore
    public String getAiredOn() {
      return airedOn;
    }

    @JsonIgnore
    public String getReleasedOn() {
      return releasedOn;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Target target = (Target) o;
      return Objects.equals(id, target.id) && Objects.equals(name, target.name) && Objects.equals(russian, target.russian) && Objects.equals(image, target.image) && Objects.equals(url, target.url) && Objects.equals(kind, target.kind) && Objects.equals(score, target.score) && Objects.equals(status, target.status) && Objects.equals(episodes, target.episodes) && Objects.equals(episodesAired, target.episodesAired) && Objects.equals(airedOn, target.airedOn) && Objects.equals(releasedOn, target.releasedOn);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, name, russian, image, url, kind, score, status, episodes, episodesAired, airedOn, releasedOn);
    }
  }

  @JsonProperty("id")
  private Long id;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("description")
  private String description;

  @JsonProperty("target")
  private Target target;

  @JsonIgnore
  public Long getId() {
    return id;
  }

  @JsonIgnore
  public String getCreatedAt() {
    return createdAt;
  }

  @JsonIgnore
  public String getDescription() {
    return description;
  }

  @JsonIgnore
  public Target getTarget() {
    return target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ShikimoriHistoryLog that = (ShikimoriHistoryLog) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
