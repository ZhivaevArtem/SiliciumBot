package com.zhivaevartem.siliciumbot.discord;

import com.zhivaevartem.siliciumbot.constant.ExitCodeConstants;
import com.zhivaevartem.siliciumbot.discord.listener.base.BaseEventListener;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Bot bean. Used to start bot.
 */
@Component
public class Bot {
  @Value("${discord.bot.token}")
  private String token;

  @Autowired
  private List<BaseEventListener> listeners;

  private void initGateway(GatewayDiscordClient gateway) {
    for (BaseEventListener listener : this.listeners) {
      listener.register(gateway);
    }
    gateway.onDisconnect().block();
  }

  /**
   * Starts bot.
   */
  public void start() {
    final String token = this.token;
    final DiscordClient client = DiscordClient.create(token);
    final GatewayDiscordClient gateway = client.login().block();

    if (null != gateway) {
      this.initGateway(gateway);
    } else {
      System.exit(ExitCodeConstants.COULD_NOT_START_BOT);
    }
  }
}
