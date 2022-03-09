package com.zhivaevartem.siliciumbot.discord.listener.base;

import com.mongodb.annotations.Immutable;
import com.zhivaevartem.siliciumbot.persistence.service.BotGuildConfigService;
import com.zhivaevartem.siliciumbot.util.StringUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for all command listeners.
 */
public abstract class AbstractCommandListener extends ListenerAdapter {
  private class Handler {
    private Method method;
    private int argumentsCount;
  }

  private BotGuildConfigService botGuildConfigService;

  private final Map<String, Handler> commandHandlers = new HashMap<>();

  @Autowired
  private void setBotGuildConfigService(BotGuildConfigService botGuildConfigService) {
    this.botGuildConfigService = botGuildConfigService;
  }

  private String matchCommand(List<String> args) {
    for (int i = 0, size = args.size(); i < size; i++) {
      String command = String.join(" ", args.subList(0, i + 1)).toLowerCase();
      if (this.commandHandlers.containsKey(command)) {
        args.subList(0, i + 1).clear();
        return command;
      }
    }
    return null;
  }

  private void invokeCommandHandler(String content, MessageReceivedEvent event) {
    List<String> args = new ArrayList<>();
    StringUtils.parseArguments(content, args);
    String command = this.matchCommand(args);
    if (null != command) {
      Handler handler = this.commandHandlers.get(command);
      try {
        handler.method.invoke(this, event, args);
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    if (!event.getAuthor().isBot()) {
      String prefix = this.botGuildConfigService.getPrefix(event.getGuild().getId());
      String content = event.getMessage().getContentDisplay();
      if (content.startsWith(prefix)) {
        content = content.substring(prefix.length());
        this.invokeCommandHandler(content, event);
      }
    }
  }

  /**
   * Register new command handler. Method will be invoked when user send command.
   *
   * @param command Associated command.
   * @param method Command handler.
   */
  public void registerCommandHandler(String command, Method method, int argumentsCount) {
    Handler handler = new Handler();
    handler.method = method;
    handler.argumentsCount = argumentsCount;
    this.commandHandlers.put(command, handler);
  }
}
