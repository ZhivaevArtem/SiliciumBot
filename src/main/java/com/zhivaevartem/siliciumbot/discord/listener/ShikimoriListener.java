package com.zhivaevartem.siliciumbot.discord.listener;

import com.zhivaevartem.siliciumbot.discord.listener.base.AbstractEventListener;
import com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler;
import com.zhivaevartem.siliciumbot.discord.service.MessageService;
import com.zhivaevartem.siliciumbot.discord.service.ShikimoriService;
import com.zhivaevartem.siliciumbot.util.NullUtils;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.GuildChannel;
import java.util.List;
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

  @Autowired
  private MessageService messageService;

  @CommandHandler(aliases = "shiki subscribe")
  public void registerNotificationChannelAndGuild(MessageCreateEvent event) {
    String guildId = event.getGuild().block().getId().asString();
    String channelId = event.getMessage().getChannel().block().getId().asString();
    this.service.registerGuild(guildId, channelId);
    this.messageService.replyMessage(event.getMessage(),
      "This channel will be used for shikimori notifications").subscribe();
  }

  @CommandHandler(aliases = "shiki unsubscribe")
  public void unregisterNotificationChannelAndGuild(MessageCreateEvent event) {
    String guildId = event.getGuild().block().getId().asString();
    this.service.unregisterGuild(guildId);
    this.messageService.replyMessage(event.getMessage(),
      "This guild unsubscribed from shikimori notifications").subscribe();
  }

  @CommandHandler(aliases = "shiki user add")
  public void addUsername(MessageCreateEvent event, String username) {
    if (NullUtils.isEmpty(username)) {
      String guildId = event.getGuild().block().getId().asString();
      this.service.addUsername(guildId, username);
      this.messageService.replyMessage(event.getMessage(),
        "User '" + username + "' added").subscribe();
    } else {
      this.messageService.replyMessage(event.getMessage(),
        "Specify user").subscribe();
    }
  }

  @CommandHandler(aliases = "shiki user remove")
  public void removeUsername(MessageCreateEvent event, String username) {
    if (NullUtils.isEmpty(username)) {
      String guildId = event.getGuild().block().getId().asString();
      this.service.removeUsername(guildId, username);
      this.messageService.replyMessage(event.getMessage(),
        "User '" + username + "' removed").subscribe();
    } else {
      this.messageService.replyMessage(event.getMessage(),
        "Specify user").subscribe();
    }
  }

  @CommandHandler(aliases = "shiki users")
  public void getListUsers(MessageCreateEvent event) {
    String guildId = event.getGuild().block().getId().asString();
    List<String> usernames = this.service.getUsernames(guildId);
    this.messageService.replyMessage(event.getMessage(), usernames.isEmpty()
      ? "User list is empty"
      : String.join(", ", usernames)).subscribe();
  }

  @CommandHandler(aliases = "shiki status")
  public void subscriptionStatus(MessageCreateEvent event) {
    String guildId = this.messageService.getGuildId(event.getMessage());
    List<String> guildIds = this.service.getRegisteredGuildsIds();
    if (guildIds.contains(guildId)) {
      GuildChannel channel = this.messageService.getChannel(guildId,
          this.service.getNotificationChannelId(guildId));
      String responseContent = "This guild subscribed to shikimori notifications.\n"
          + "Channel used for notifications: "
          + channel.getName() + "#" + channel.getId().asString();
      this.messageService.replyMessage(event.getMessage(), responseContent).subscribe();
    } else {
      this.messageService.replyMessage(event.getMessage(),
          "This guild is not subscribed to shikimori notifications").subscribe();
    }
  }
}
