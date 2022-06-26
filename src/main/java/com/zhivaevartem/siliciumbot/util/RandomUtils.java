package com.zhivaevartem.siliciumbot.util;

import org.springframework.lang.Nullable;

public class RandomUtils {
  public static int randomInt(@Nullable Integer min, @Nullable Integer max) {
    if (min == null) {
      min = 1;
    }
    if (max == null) {
      max = 100;
    }
    max += 1;
    if (max < min) {
      int tmp = min;
      min = max;
      max = tmp;
    }
    return (int) ((Math.random() * (max - min)) + min);
  }
}
