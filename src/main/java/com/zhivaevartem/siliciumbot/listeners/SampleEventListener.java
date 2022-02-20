package com.zhivaevartem.siliciumbot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SampleEventListener extends ListenerAdapter {

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    if (!event.getAuthor().isBot()) {
      event
          .getChannel()
          .sendMessage(event.getMessage().getContentDisplay())
          .reference(event.getMessage())
          .queue();
    }
  }
}
