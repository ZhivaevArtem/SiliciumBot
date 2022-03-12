package com.zhivaevartem.siliciumbot.constant;

/**
 * Regular expressions.
 */
public class RegularExpressionConstants {
  // "(\\"|[^"])*"|(\\"|[^\s"])+
  public static String SPLIT_ARGUMENTS = "\"(\\\\\"|[^\"])*\"|(\\\\\"|[^\\s\"])+";
  // ^"|"$
  public static String FIRST_AND_LAST_DOUBLE_QUOTES = "^\"|\"$";

  private RegularExpressionConstants() {}
}
