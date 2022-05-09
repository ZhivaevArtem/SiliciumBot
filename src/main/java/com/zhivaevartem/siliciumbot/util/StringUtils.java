package com.zhivaevartem.siliciumbot.util;

import com.zhivaevartem.siliciumbot.constant.RegularExpressionConstants;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.lang.Nullable;

/**
 * Utils to work with strings.
 */
public class StringUtils {
  /**
   * Split input string to arguments. Like it is shell command.
   *
   * @param command Input string will be parsed.
   * @param args Arguments, output parameter.
   * @param argumentsCount Amount of arguments will be parsed.
   *                       Remaining string will be returned.
   *
   * @return Remaining string after first N arguments was parsed.
   */
  public static String splitArguments(
      String command, @Nullable List<String> args, int argumentsCount) {
    if (null != args) {
      args.clear();
    }
    command = command.trim();
    Pattern p = Pattern.compile(RegularExpressionConstants.SPLIT_ARGUMENTS);
    Matcher m = p.matcher(command);
    int i = 0;
    while (m.find()) {
      if (i >= argumentsCount && argumentsCount >= 0) {
        return command.substring(m.start());
      }
      String s = m.group();
      s = s
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replaceAll(RegularExpressionConstants.FIRST_AND_LAST_DOUBLE_QUOTES, "");
      if (args != null) {
        args.add(s);
      }
      ++i;
    }
    return "";
  }

  /**
   * Split input string to arguments. Like it is shell command.
   *
   * @param command Input string will be parsed.
   * @param args Arguments, output parameter.
   */
  public static void splitArguments(String command, List<String> args) {
    StringUtils.splitArguments(command, args, -1);
  }

  /**
   * Split input string to arguments. Like it is shell command.
   *
   * @param command Input string will be parsed.
   * @param argumentsCount Amount of arguments will be parsed.
   *                       Remaining string will be returned.
   *
   * @return Remaining string after first N arguments was parsed.
   */
  public static String splitArguments(String command, int argumentsCount) {
    return StringUtils.splitArguments(command, null, argumentsCount);
  }

  /**
   * Make string lowercase and replace multiple space characters with single space.
   *
   * @param rawString String to prettify.
   * @return Prettified string.
   */
  public static String prettifyString(String rawString) {
    return rawString.replaceAll("\\s+", " ").toLowerCase().trim();
  }

  public static String capitalize(String str) {
    return String.valueOf(str.toCharArray()[0]).toUpperCase() + str.substring(1).toLowerCase();
  }

  public static int colorFromString(String str) {
    return str.hashCode() % (256 * 256 * 256);
  }

  private StringUtils() {}
}
