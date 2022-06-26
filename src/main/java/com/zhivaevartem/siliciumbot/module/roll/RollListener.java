package com.zhivaevartem.siliciumbot.module.roll;

import com.zhivaevartem.siliciumbot.core.listener.AbstractEventListener;
import com.zhivaevartem.siliciumbot.core.listener.CommandHandler;
import com.zhivaevartem.siliciumbot.core.service.MessageService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RollListener extends AbstractEventListener {
  @Autowired
  private RollService rollService;

  @Autowired
  private MessageService messageService;

  @CommandHandler(aliases = {"roll"})
  public void roll(MessageCreateEvent event, Integer from, Integer to) {
    int num = this.rollService.generateRoll(from, to);
    this.messageService.replyMessage(event, String.valueOf(num)).subscribe();
  }

  @CommandHandler(aliases = {"flip"})
  public void flip(MessageCreateEvent event) {
    this.messageService.replyMessage(event, this.rollService.flip()).subscribe();
  }
}
