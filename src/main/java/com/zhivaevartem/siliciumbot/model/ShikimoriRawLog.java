package com.zhivaevartem.siliciumbot.model;

import lombok.*;
import org.jsoup.nodes.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@With(AccessLevel.PRIVATE)
@Getter
public class ShikimoriRawLog {
  private long id;
  private ShikimoriJsonLog json;
  private Date date;
  private Action action;
  private String title;
  private String titleId;
  private Type type;

  public enum Action {
    CHANGED,
    ADDED,
    REMOVED
  }

  public enum Type {
    MANGA,
    ANIME,
    RANOBE
  }

  public static ShikimoriRawLog create(Element element) throws Exception {
    return new ShikimoriRawLog()
      .withDate(ShikimoriRawLog.parseDate(element))
      .withId(ShikimoriRawLog.parseId(element))
      .withAction(ShikimoriRawLog.parseAction(element))
      .withJson(ShikimoriRawLog.parseJson(element))
      .withTitle(ShikimoriRawLog.parseTitle(element))
      .withTitleId(ShikimoriRawLog.parseTitleId(element))
      .withType(ShikimoriRawLog.parseType(element));
  }

  private static Date parseDate(Element element) throws ParseException {
    return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX").parse(element.select("time").attr("datetime"));
  }

  private static long parseId(Element element) throws Exception {
    Element aElem = element.select("span > a").first();
    if (aElem != null) {
      return Long.parseLong(aElem.text().substring(1));
    } else {
      throw new Exception("HTML parsing error: can't parse id.");
    }
  }

  private static Action parseAction(Element element) throws Exception {
    if (element.select("span.action.update").first() != null) {
      return Action.CHANGED;
    } else if (element.select("span.action.create").first() != null) {
      return Action.ADDED;
    } else if (element.select("span.action.destroy").first() != null) {
      return Action.REMOVED;
    } else {
      throw new Exception("HTML parsing error: can't parse action type.");
    }
  }

  private static ShikimoriJsonLog parseJson(Element element) throws Exception {
    Element code = element.select(".diff code").first();
    if (code != null) {
      return ShikimoriJsonLog.create(code.text());
    } else {
      throw new Exception("HTML parsing error: can't parse json.");
    }
  }

  private static String parseTitle(Element element) throws Exception {
    Element a = element.select("a.bubbled").first();
    if (a != null) {
      return a.text();
    } else {
      throw new Exception("HTML parsing error: can't parse title.");
    }
  }

  private static String parseTitleId(Element element) throws Exception {
    Element a = element.select("a.bubbled").first();
    if (a != null) {
      String href = a.attr("href");
      String[] split = href.substring(1).split("/");
      if (split.length == 2) {
        return split[1];
      }
    }
    throw new Exception("HTML parsing error: can't parse title id.");
  }

  private static Type parseType(Element element) throws Exception {
    Element a = element.select("a.bubbled").first();
    if (a != null) {
      String href = a.attr("href");
      String[] split = href.substring(1).split("/");
      if (split.length == 2) {
        switch (split[0]) {
          case "ranobe":
            return Type.RANOBE;
          case "animes":
            return Type.ANIME;
          case "mangas":
            return Type.MANGA;
        }
      }
    }
    throw new Exception("HTML parsing error: can't parse type.");
  }
}
