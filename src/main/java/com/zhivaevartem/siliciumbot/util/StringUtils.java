package com.zhivaevartem.siliciumbot.util;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.util.cli.CommandLineUtils;

/**
 * Utils to work with strings.
 */
public class StringUtils {
  /**
   * Split input string to arguments. Like it is shell command.
   *
   * @param command Input string.
   * @return Arguments.
   */
  public static List<String> parseArguments(String command) {
    try {
      String[] strings = CommandLineUtils.translateCommandline(command);
      return new ArrayList<>(List.of(strings));
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  private StringUtils() {}
}
