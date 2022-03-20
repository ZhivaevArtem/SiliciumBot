package com.zhivaevartem.siliciumbot.discord.listener;

import com.zhivaevartem.siliciumbot.discord.listener.base.AbstractEventListener;
import com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler;
import com.zhivaevartem.siliciumbot.discord.service.ShikimoriService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShikimoriListener extends AbstractEventListener {
  @Autowired
  private ShikimoriService service;

  @CommandHandler(aliases = {"shiki users add"})
  public void addUser(MessageCreateEvent event, String username) {
    System.out.println("ShikimoriListener.addUser");
  }

  @CommandHandler(aliases = {"shiki users remove"})
  public void removeUser(MessageCreateEvent event, String username) {
    System.out.println("ShikimoriListener.removeUser");
  }

  @CommandHandler(aliases = {"shiki users truncate"})
  public void truncateUser(MessageCreateEvent event, String username) {
    System.out.println("ShikimoriListener.truncateUser");
  }

  @CommandHandler(aliases = {"shiki users"})
  public void showUsers(MessageCreateEvent event) {
    System.out.println("ShikimoriListener.showUsers");
  }
}
