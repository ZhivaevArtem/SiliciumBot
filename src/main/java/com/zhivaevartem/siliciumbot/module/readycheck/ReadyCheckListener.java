package com.zhivaevartem.siliciumbot.module.readycheck;

import com.zhivaevartem.siliciumbot.core.listener.AbstractEventListener;
import com.zhivaevartem.siliciumbot.core.listener.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveAllEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;

/**
 * Ready check listener.
 */
@Component
public class ReadyCheckListener extends AbstractEventListener {
  @Autowired
  private ReadyCheckService service;

  /**
   * Command to start ready check.
   *
   * @param description Embed description.
   */
  @CommandHandler(aliases = {"rc", "readycheck"}, lastFreeArgument = true)
  public void startReadyCheck(MessageCreateEvent event, @Nullable String description) {
    Tuple2<Guild, MessageChannel> guildAndChannel = event.getGuild()
        .zipWith(event.getMessage().getChannel())
        .block();
    if (null != guildAndChannel) {
      String guildId = guildAndChannel.getT1().getId().asString();
      MessageChannel channel = guildAndChannel.getT2();
      this.service.startReadyCheck(guildId, channel, event.getMessage(), description);
    }
  }

  @Override
  public void onReactionAddEvent(ReactionAddEvent event) {
    Tuple2<Tuple2<Tuple2<Guild, Message>, User>, User> guildAndMessageAndBotUserAndAuthor
        = event.getGuild()
          .zipWith(event.getMessage())
          .zipWith(event.getClient().getSelf())
          .zipWith(event.getUser())
          .block();
    if (null != guildAndMessageAndBotUserAndAuthor) {
      String guildId = guildAndMessageAndBotUserAndAuthor.getT1().getT1().getT1().getId()
          .asString();
      Message message = guildAndMessageAndBotUserAndAuthor.getT1().getT1().getT2();
      User botUser = guildAndMessageAndBotUserAndAuthor.getT1().getT2();
      User author = guildAndMessageAndBotUserAndAuthor.getT2();
      this.service.addVote(guildId, event.getEmoji(), message, botUser, author);
    }
  }

  @Override
  public void onReactionRemoveEvent(ReactionRemoveEvent event) {
    Tuple2<Tuple2<Tuple2<Guild, Message>, User>, User> guildAndMessageAndBotUserAndAuthor
        = event.getGuild()
          .zipWith(event.getMessage())
          .zipWith(event.getClient().getSelf())
          .zipWith(event.getUser())
          .block();
    if (null != guildAndMessageAndBotUserAndAuthor) {
      String guildId = guildAndMessageAndBotUserAndAuthor.getT1().getT1().getT1().getId()
          .asString();
      Message message = guildAndMessageAndBotUserAndAuthor.getT1().getT1().getT2();
      User botUser = guildAndMessageAndBotUserAndAuthor.getT1().getT2();
      User author = guildAndMessageAndBotUserAndAuthor.getT2();
      this.service.removeVote(guildId, event.getEmoji(), message, botUser, author);
    }
  }

  @Override
  public void onReactionRemoveAllEvent(ReactionRemoveAllEvent event) {
    Tuple2<Tuple2<Guild, Message>, User> guildAndMessageAndBotUser = event.getGuild()
        .zipWith(event.getMessage())
        .zipWith(event.getClient().getSelf())
        .block();
    if (null != guildAndMessageAndBotUser) {
      String guildId = guildAndMessageAndBotUser.getT1().getT1().getId().asString();
      Message message = guildAndMessageAndBotUser.getT1().getT2();
      User botUser = guildAndMessageAndBotUser.getT2();
      this.service.restartReadyCheck(guildId, message, botUser);
    }
  }
}
