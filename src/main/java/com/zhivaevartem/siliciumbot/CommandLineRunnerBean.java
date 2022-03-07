package com.zhivaevartem.siliciumbot;

import com.zhivaevartem.siliciumbot.persistence.entity.BotGuildConfig;
import com.zhivaevartem.siliciumbot.persistence.service.BotGuildConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Bean runs when it is contained within a {@link org.springframework.boot.SpringApplication}.
 */
@Profile("!test")
@Component
public class CommandLineRunnerBean implements CommandLineRunner {
  @Autowired
  private BotGuildConfigService botGuildConfigService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(String... args) throws Exception {
    System.out.println("Hello");
    String guildId = "asdf";
    BotGuildConfig cfg = this.botGuildConfigService.getBotGuildConfig(guildId);
    System.out.println(cfg.getPrefix());
    cfg.setPrefix("123");
    cfg.setNotificationChannelId("notificationChannelId");
    this.botGuildConfigService.saveBotGuildConfig(cfg);
    System.out.println("saved....");
  }
}
