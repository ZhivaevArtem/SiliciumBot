package com.zhivaevartem.siliciumbot;

import com.zhivaevartem.siliciumbot.persistence.entities.BotConfigLocal;
import com.zhivaevartem.siliciumbot.persistence.services.BotConfigLocalService;

/**
 * Startup class.
 */
public class App {
  /**
   * Startup method.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    BotConfigLocalService service = new BotConfigLocalService();
    BotConfigLocal cfg = service.getBotConfigLocal("123");
    System.out.println(cfg.getGuildId());
    System.out.println(cfg.getPrefix());
  }
}
