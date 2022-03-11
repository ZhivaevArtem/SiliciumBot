package com.zhivaevartem.siliciumbot.discord.listener.base;

import com.zhivaevartem.siliciumbot.persistence.service.BotGuildConfigService;
import com.zhivaevartem.siliciumbot.util.StringUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for all command listeners.
 */
public abstract class AbstractCommandListener extends ListenerAdapter {
  private static class Handler {
    private Method method;
    private boolean lastArgumentFree;
    private String command;
    private int commandSize;

    private Handler(Method method, boolean lastArgumentFree, List<String> command) {
      this.method = method;
      this.lastArgumentFree = lastArgumentFree;
      this.command = StringUtils.prettifyString(String.join(" ", command));
      this.commandSize = command.size();
    }
  }

  private BotGuildConfigService botGuildConfigService;

  private final Map<String, Handler> commandHandlers = new HashMap<>();

  @Autowired
  private void setBotGuildConfigService(BotGuildConfigService botGuildConfigService) {
    this.botGuildConfigService = botGuildConfigService;
  }

  private String matchCommandHandler(String command) {
    List<String> args = new ArrayList<>();
    StringUtils.splitArguments(command, args);
    for (int i = 0, size = args.size(); i < size; i++) {
      String s = StringUtils.prettifyString(String.join(" ", args.subList(0, i + 1)));
      if (this.commandHandlers.containsKey(s)) {
        return s;
      }
    }
    return "";
  }

  private void invokeCommandHandler(String handlerKey, MessageReceivedEvent event, String command)
      throws InvocationTargetException, IllegalAccessException {
    if (this.commandHandlers.containsKey(handlerKey)) {
      Handler handler = this.commandHandlers.get(handlerKey);
      Method method = handler.method;
      int positionalParameterCount = method.getParameterCount()
          - (handler.lastArgumentFree ? 2 : 1);
      List<String> rawArguments = new ArrayList<>();
      String freeArgument = StringUtils.splitArguments(command, rawArguments,
          positionalParameterCount + handler.commandSize);
      rawArguments = rawArguments.subList(handler.commandSize, rawArguments.size());
      if (rawArguments.size() >= positionalParameterCount) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        for (int i = 0; i < positionalParameterCount; i++) {
          Class<?> parameterType = parameterTypes[i + 1];
          Object argument;
          String rawArgument = rawArguments.get(i);
          if (int.class.equals(parameterType) || Integer.class.equals(parameterType)) {
            argument = Integer.valueOf(rawArgument);
          } else if (double.class.equals(parameterType) || Double.class.equals(parameterType)) {
            argument = Double.valueOf(rawArgument);
          } else {
            argument = rawArgument;
          }
          arguments[i + 1] = argument;
        }
        arguments[0] = event;
        if (handler.lastArgumentFree) {
          arguments[arguments.length - 1] = freeArgument;
        }
        method.invoke(this, arguments);
      }
    }
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (!event.getAuthor().isBot()) {
      String prefix = this.botGuildConfigService.getPrefix(event.getGuild().getId());
      String command = event.getMessage().getContentRaw();
      if (command.startsWith(prefix)) {
        command = command.substring(prefix.length()).trim();
        try {
          this.invokeCommandHandler(this.matchCommandHandler(command), event, command);
        } catch (InvocationTargetException | IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Register new command handler. Method will be invoked when user send associated command.
   *
   * @param method Command handler should be annotated
   *               with {@link com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler}.
   *               Otherwise, method won't be registered.
   */
  public void registerCommandHandler(Method method) {
    CommandHandler annotation = method.getAnnotation(CommandHandler.class);
    if (null != annotation) {
      for (String alias : annotation.aliases()) {
        List<String> command = new ArrayList<>();
        StringUtils.splitArguments(alias, command);
        this.commandHandlers.put(StringUtils.prettifyString(alias),
          new Handler(method, annotation.lastFreeArgument(), command));
      }
    }
  }
}
