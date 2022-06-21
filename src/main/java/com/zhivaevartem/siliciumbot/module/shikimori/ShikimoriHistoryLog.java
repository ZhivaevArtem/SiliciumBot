package com.zhivaevartem.siliciumbot.module.shikimori;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * History log model received from shikimori API:
 * <code>
 *  https://shikimori.one/api/users/{username}/history
 * </code>.
 */
@Getter
@EqualsAndHashCode
public class ShikimoriHistoryLog {
  /**
   * {@link ShikimoriHistoryLog} nested field model.
   */
  @Getter
  @EqualsAndHashCode
  public static class Target {
    /**
     * {@link ShikimoriHistoryLog} nested field model.
     */
    @Getter
    @EqualsAndHashCode
    public static class Image {
      @JsonProperty("original")
      private String original;

      @JsonProperty("preview")
      private String preview;

      @JsonProperty("x96")
      private String x96;

      @JsonProperty("x48")
      private String x48;
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
  }

  @JsonProperty("id")
  private Long id;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("description")
  private String description;

  @JsonProperty("target")
  private Target target;
}
