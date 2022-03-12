package com.zhivaevartem.siliciumbot.discord.listener;

import com.zhivaevartem.siliciumbot.discord.listener.base.BaseEventListener;
import com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import org.springframework.stereotype.Component;

/**
 * Listener example.
 */
@Component
public class HelpListener extends BaseEventListener {
  @Override
  public void onReactionAddEvent(ReactionAddEvent event) {
    event.getChannel().subscribe(channel -> {
      channel.createMessage("onReactionAddEvent").subscribe();
    });
  }

  /**
   * Command listener example.
   */
  @CommandHandler(aliases = {"ping", "ping pong"}, lastFreeArgument = true)
  public void pingCommand(
      MessageCreateEvent event, String arg, int num, double real, String freeArg) {
    event.getMessage().getChannel().subscribe(channel -> {
      String newMessage = String.join("\n",
          "Arg: " + arg,
          "Num: " + num,
          "Real: " + real,
          "Free arg: " + freeArg
        );
      channel.createMessage(newMessage).subscribe(message -> {
        message.addReaction(ReactionEmoji.unicode("✅")).subscribe();
        message.addReaction(ReactionEmoji.unicode("❌")).subscribe();
        message.addReaction(ReactionEmoji.unicode("⌚")).subscribe();
      });
    });
  }
}
