package com.zhivaevartem.siliciumbot.util;

public class NullUtils {
  public static boolean isEmpty(String str) {
    return str == null || str
      .replace(" ", "")
      .replace("\t", "")
      .replace("\n", "")
      .isEmpty();
  }
}
