package com.zhivaevartem.siliciumbot.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Getter
public class ShikimoriJsonLog {
  @Nullable
  private List<Long> id;

  @Nullable
  private List<String> status;

  @Nullable
  private List<Integer> episodes;

  @Nullable
  private List<Integer> score;

  private String raw;

  public static ShikimoriJsonLog create(String rawJson) {
    JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
    ShikimoriJsonLog log = new ShikimoriJsonLog();
    log.setRaw(rawJson);
    if (json.has("id")) {
      final List<Long> idArray = new ArrayList<>(2);
      json.getAsJsonArray("id").forEach(elem -> idArray.add(elem.isJsonNull() ? null : elem.getAsLong()));
      log.setId(idArray);
    }
    if (json.has("status")) {
      final List<String> statusArray = new ArrayList<>(2);
      json.getAsJsonArray("status").forEach(elem -> statusArray.add(elem.isJsonNull() ? null : elem.getAsString()));
      log.setStatus(statusArray);
    }
    if (json.has("episodes")) {
      final List<Integer> episodesArray = new ArrayList<>(2);
      json.getAsJsonArray("episodes").forEach(elem -> episodesArray.add(elem.isJsonNull() ? null : elem.getAsInt()));
      log.setEpisodes(episodesArray);
    }
    if (json.has("score")) {
      final List<Integer> scoreArray = new ArrayList<>(2);
      json.getAsJsonArray("score").forEach(elem -> scoreArray.add(elem.isJsonNull() ? null : elem.getAsInt()));
      log.setScore(scoreArray);
    }
    return log;
  }

  @Override
  public String toString() {
    return raw;
  }
}
