package com.zhivaevartem.siliciumbot.core.bot;

import com.zhivaevartem.siliciumbot.core.listener.AbstractEventListener;
import com.zhivaevartem.siliciumbot.core.listener.CommandHandler;
import com.zhivaevartem.siliciumbot.core.service.MessageService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BotListener extends AbstractEventListener {
  @Autowired
  private MessageService messageService;

  @CommandHandler(aliases = {"prefix"})
  public void changePrefix(MessageCreateEvent event, String newPrefix) {
    String guildId = this.messageService.getGuildId(event);
    if (newPrefix != null && !newPrefix.isEmpty()) {
      this.configService.setPrefix(guildId, newPrefix);
      this.messageService.replyMessage(event, newPrefix).subscribe();
    } else {
      this.messageService.replyMessage(event, this.configService.getPrefix(guildId)).subscribe();
    }
  }
}
