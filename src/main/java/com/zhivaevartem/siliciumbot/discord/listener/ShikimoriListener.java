package com.zhivaevartem.siliciumbot.discord.listener;

import com.zhivaevartem.siliciumbot.discord.listener.base.AbstractEventListener;
import com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler;
import com.zhivaevartem.siliciumbot.discord.service.ShikimoriService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Discord event listener for shikimori features.
 * Business logic: {@link ShikimoriService}
 */
@Component
public class ShikimoriListener extends AbstractEventListener {
  @Autowired
  private ShikimoriService service;

  @CommandHandler(aliases = "shiki logs")
  public void testUserLogs(MessageCreateEvent event, String username) {
    // TODO: remove method
    service.retrieveAllUserLogs(username);
  }

  // TODO: add listeners
}
