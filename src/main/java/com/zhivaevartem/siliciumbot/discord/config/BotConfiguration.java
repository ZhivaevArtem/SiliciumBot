package com.zhivaevartem.siliciumbot.discord.config;

import com.zhivaevartem.siliciumbot.constant.ExitCodeConstants;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration creates {@link GatewayDiscordClient} bean.
 */
@Profile("!test")
@Configuration
public class BotConfiguration {
  @Value("${discord.bot.token}")
  private String token;

  /**
   * Discord4j gateway bean. It wraps discord API.
   */
  @Bean
  public GatewayDiscordClient gateway() {
    DiscordClient client = DiscordClient.create(this.token);
    GatewayDiscordClient gateway = client.login().block();
    if (null == gateway) {
      System.exit(ExitCodeConstants.COULD_NOT_START_BOT);
    }
    new Thread(() -> gateway.onDisconnect().block()).start();  // Do not block main thread
    return gateway;
  }
}