package com.zhivaevartem.siliciumbot.discord.listener;

import com.zhivaevartem.siliciumbot.discord.listener.base.AbstractEventListener;
import com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler;
import com.zhivaevartem.siliciumbot.discord.service.ShikimoriService;
import com.zhivaevartem.siliciumbot.model.ShikimoriRawLog;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.MessageCreateSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
    // TODO: remove listener
    List<ShikimoriRawLog> newLogs = service.retrieveNewUserLogs(username);
    List<String> messages = newLogs.stream().map(log -> {
      String action = switch (log.getAction()) {
        case ADDED -> "added";
        case REMOVED -> "removed";
        case CHANGED -> "changed";
      };
      String title = log.getTitle();
      String type = switch (log.getType()) {
        case ANIME -> "anime";
        case MANGA -> "mange";
        case RANOBE -> "ranobe";
      };
      return username + ": " + action + ": " + type + ": " + title + ": " + log.getJson();
    }).toList();
    if (!messages.isEmpty()) {
      event.getMessage().getChannel().subscribe(channel -> {
        channel.createMessage(MessageCreateSpec.create()
          .withContent(String.join("\n", messages))
          .withMessageReference(event.getMessage().getId())).subscribe();
      });
    }
  }

  // TODO: add required listeners
}
