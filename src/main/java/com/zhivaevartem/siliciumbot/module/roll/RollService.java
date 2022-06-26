package com.zhivaevartem.siliciumbot.module.roll;

import com.zhivaevartem.siliciumbot.util.RandomUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class RollService {
  public int generateRoll(@Nullable Integer from, @Nullable Integer to) {
    return RandomUtils.randomInt(from, to);
  }

  public String flip() {
    int r = RandomUtils.randomInt(1, 101);
    if (r < 50) {
      return "Орёл";
    }
    if (r > 50) {
      return "Решка";
    }
    return "Ребро";
  }
}
