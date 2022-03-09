package com.zhivaevartem.siliciumbot.util;

import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.springframework.data.util.Pair;

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
  public static String parseArguments(
      String command, @Nullable List<String> args, int argumentsCount) {
    if (null != args) {
      args.clear();
    }
    command = command.trim();
    // (?<=")(\\"|[^"])+(?=")|(\\"|[^\s"])+
    Pattern p = Pattern.compile("\"(\\\\\"|[^\"])+\"|(\\\\\"|[^\\s\"])+");
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
        .replaceAll("^\"|\"$", "");
      if (args != null) {
        args.add(s);
      }
      ++i;
    }
    return "";
  }

  public static String parseArguments(String command, List<String> args) {
    return StringUtils.parseArguments(command, args, -1);
  }

  public static String parseArguments(String command, int argumentsCount) {
    return StringUtils.parseArguments(command, null, argumentsCount);
  }

  private StringUtils() {}
}
