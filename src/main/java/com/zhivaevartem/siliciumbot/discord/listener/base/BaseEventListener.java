package com.zhivaevartem.siliciumbot.discord.listener.base;

import com.zhivaevartem.siliciumbot.persistence.service.BotGuildConfigService;
import com.zhivaevartem.siliciumbot.util.StringUtils;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.InviteCreateEvent;
import discord4j.core.event.domain.InviteDeleteEvent;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.event.domain.UserUpdateEvent;
import discord4j.core.event.domain.VoiceServerUpdateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.WebhooksUpdateEvent;
import discord4j.core.event.domain.channel.CategoryCreateEvent;
import discord4j.core.event.domain.channel.CategoryDeleteEvent;
import discord4j.core.event.domain.channel.CategoryUpdateEvent;
import discord4j.core.event.domain.channel.NewsChannelCreateEvent;
import discord4j.core.event.domain.channel.NewsChannelDeleteEvent;
import discord4j.core.event.domain.channel.NewsChannelUpdateEvent;
import discord4j.core.event.domain.channel.PinsUpdateEvent;
import discord4j.core.event.domain.channel.StoreChannelCreateEvent;
import discord4j.core.event.domain.channel.StoreChannelDeleteEvent;
import discord4j.core.event.domain.channel.StoreChannelUpdateEvent;
import discord4j.core.event.domain.channel.TextChannelCreateEvent;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import discord4j.core.event.domain.channel.TextChannelUpdateEvent;
import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.core.event.domain.channel.VoiceChannelCreateEvent;
import discord4j.core.event.domain.channel.VoiceChannelDeleteEvent;
import discord4j.core.event.domain.channel.VoiceChannelUpdateEvent;
import discord4j.core.event.domain.guild.BanEvent;
import discord4j.core.event.domain.guild.EmojisUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.guild.GuildUpdateEvent;
import discord4j.core.event.domain.guild.IntegrationsUpdateEvent;
import discord4j.core.event.domain.guild.MemberChunkEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.guild.UnbanEvent;
import discord4j.core.event.domain.lifecycle.ConnectEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ReconnectEvent;
import discord4j.core.event.domain.lifecycle.ReconnectFailEvent;
import discord4j.core.event.domain.lifecycle.ReconnectStartEvent;
import discord4j.core.event.domain.lifecycle.ResumeEvent;
import discord4j.core.event.domain.message.MessageBulkDeleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveAllEvent;
import discord4j.core.event.domain.message.ReactionRemoveEmojiEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

/**
 * Base class for all listeners. Each listener must extend this.
 */
public abstract class BaseEventListener {
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

  @Autowired
  private BotGuildConfigService configService;

  private final Map<String, Handler> commandHandlers = new HashMap<>();

  private Object getArgument(Class<?> parameterType, @Nullable String rawArgument) {
    if (null == rawArgument) {
      if (int.class.equals(parameterType)) {
        return 0;
      } else if (double.class.equals(parameterType)) {
        return 0.0;
      } else if (Double.class.equals(parameterType)
          || Integer.class.equals(parameterType)
          || String.class.equals(parameterType)) {
        return null;
      }
    } else {
      if (int.class.equals(parameterType)) {
        try {
          return Integer.parseInt(rawArgument);
        } catch (NumberFormatException ex) {
          return 0;
        }
      } else if (double.class.equals(parameterType)) {
        try {
          return Double.parseDouble(rawArgument);
        } catch (NumberFormatException ex) {
          return 0.0;
        }
      } else if (String.class.equals(parameterType)) {
        return rawArgument;
      } else if (Double.class.equals(parameterType)) {
        try {
          return Double.valueOf(rawArgument);
        } catch (NumberFormatException ex) {
          return null;
        }
      } else if (Integer.class.equals(parameterType)) {
        try {
          return Integer.valueOf(rawArgument);
        } catch (NumberFormatException ex) {
          return null;
        }
      }
    }
    return null;
  }

  private void invokeCommandHandler(String handlerKey, MessageCreateEvent event, String command)
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
      Class<?>[] parameterTypes = method.getParameterTypes();
      Object[] arguments = new Object[parameterTypes.length];
      for (int i = 0; i < positionalParameterCount; i++) {
        Class<?> parameterType = parameterTypes[i + 1];
        Object argument;
        if (i < rawArguments.size()) {
          argument = this.getArgument(parameterType, rawArguments.get(i));
        } else {
          argument = this.getArgument(parameterType, null);
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

  private void registerCommandHandler(Method method) {
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

  private void registerCommandHandlers(GatewayDiscordClient gateway) {
    List<Method> handlers = Arrays.stream(this.getClass().getMethods())
        .filter(method -> method.isAnnotationPresent(CommandHandler.class)).toList();
    for (Method handler : handlers) {
      this.registerCommandHandler(handler);
    }
    gateway.on(MessageCreateEvent.class).subscribe(event -> {
      event.getGuildId().ifPresent(guildId -> {
        String prefix = this.configService.getPrefix(guildId.asString());
        String command = event.getMessage().getContent();
        if (command.startsWith(prefix)) {
          command = command.substring(prefix.length()).trim();
          try {
            this.invokeCommandHandler(this.matchCommandHandler(command), event, command);
          } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      });
    });
  }

  private void registerEventHandlers(GatewayDiscordClient gateway) {
    List<Method> handlers = Arrays.stream(this.getClass().getMethods())
        .filter(method -> method.getName().startsWith("on")
        && method.getName().endsWith("Event")
        && method.getDeclaringClass().equals(this.getClass())).toList();
    for (Method handler : handlers) {
      switch (handler.getName()) {
        case "onReadyEvent" -> gateway.on(ReadyEvent.class)
          .subscribe(this::onReadyEvent);
        case "onResumeEvent" -> gateway.on(ResumeEvent.class)
          .subscribe(this::onResumeEvent);
        case "onMessageCreateEvent" -> gateway.on(MessageCreateEvent.class)
          .subscribe(this::onMessageCreateEvent);
        case "onMessageDeleteEvent" -> gateway.on(MessageDeleteEvent.class)
          .subscribe(this::onMessageDeleteEvent);
        case "onMessageUpdateEvent" -> gateway.on(MessageUpdateEvent.class)
          .subscribe(this::onMessageUpdateEvent);
        case "onMessageBulkDeleteEvent" -> gateway.on(MessageBulkDeleteEvent.class)
          .subscribe(this::onMessageBulkDeleteEvent);
        case "onReactionAddEvent" -> gateway.on(ReactionAddEvent.class)
          .subscribe(this::onReactionAddEvent);
        case "onReactionRemoveEvent" -> gateway.on(ReactionRemoveEvent.class)
          .subscribe(this::onReactionRemoveEvent);
        case "onReactionRemoveEmojiEvent" -> gateway.on(ReactionRemoveEmojiEvent.class)
          .subscribe(this::onReactionRemoveEmojiEvent);
        case "onReactionRemoveAllEvent" -> gateway.on(ReactionRemoveAllEvent.class)
          .subscribe(this::onReactionRemoveAllEvent);
        case "onGuildCreateEvent" -> gateway.on(GuildCreateEvent.class)
          .subscribe(this::onGuildCreateEvent);
        case "onGuildDeleteEvent" -> gateway.on(GuildDeleteEvent.class)
          .subscribe(this::onGuildDeleteEvent);
        case "onGuildUpdateEvent" -> gateway.on(GuildUpdateEvent.class)
          .subscribe(this::onGuildUpdateEvent);
        case "onMemberJoinEvent" -> gateway.on(MemberJoinEvent.class)
          .subscribe(this::onMemberJoinEvent);
        case "onMemberLeaveEvent" -> gateway.on(MemberLeaveEvent.class)
          .subscribe(this::onMemberLeaveEvent);
        case "onMemberUpdateEvent" -> gateway.on(MemberUpdateEvent.class)
          .subscribe(this::onMemberUpdateEvent);
        case "onMemberChunkEvent" -> gateway.on(MemberChunkEvent.class)
          .subscribe(this::onMemberChunkEvent);
        case "onEmojisUpdateEvent" -> gateway.on(EmojisUpdateEvent.class)
          .subscribe(this::onEmojisUpdateEvent);
        case "onBanEvent" -> gateway.on(BanEvent.class)
          .subscribe(this::onBanEvent);
        case "onUnbanEvent" -> gateway.on(UnbanEvent.class)
          .subscribe(this::onUnbanEvent);
        case "onIntegrationsUpdateEvent" -> gateway.on(IntegrationsUpdateEvent.class)
          .subscribe(this::onIntegrationsUpdateEvent);
        case "onWebhooksUpdateEvent" -> gateway.on(WebhooksUpdateEvent.class)
          .subscribe(this::onWebhooksUpdateEvent);
        case "onTextChannelCreateEvent" -> gateway.on(TextChannelCreateEvent.class)
          .subscribe(this::onTextChannelCreateEvent);
        case "onVoiceChannelCreateEvent" -> gateway.on(VoiceChannelCreateEvent.class)
          .subscribe(this::onVoiceChannelCreateEvent);
        case "onCategoryCreateEvent" -> gateway.on(CategoryCreateEvent.class)
          .subscribe(this::onCategoryCreateEvent);
        case "onNewsChannelCreateEvent" -> gateway.on(NewsChannelCreateEvent.class)
          .subscribe(this::onNewsChannelCreateEvent);
        case "onStoreChannelCreateEvent" -> gateway.on(StoreChannelCreateEvent.class)
          .subscribe(this::onStoreChannelCreateEvent);
        case "onTextChannelDeleteEvent" -> gateway.on(TextChannelDeleteEvent.class)
          .subscribe(this::onTextChannelDeleteEvent);
        case "onVoiceChannelDeleteEvent" -> gateway.on(VoiceChannelDeleteEvent.class)
          .subscribe(this::onVoiceChannelDeleteEvent);
        case "onCategoryDeleteEvent" -> gateway.on(CategoryDeleteEvent.class)
          .subscribe(this::onCategoryDeleteEvent);
        case "onNewsChannelDeleteEvent" -> gateway.on(NewsChannelDeleteEvent.class)
          .subscribe(this::onNewsChannelDeleteEvent);
        case "onStoreChannelDeleteEvent" -> gateway.on(StoreChannelDeleteEvent.class)
          .subscribe(this::onStoreChannelDeleteEvent);
        case "onTextChannelUpdateEvent" -> gateway.on(TextChannelUpdateEvent.class)
          .subscribe(this::onTextChannelUpdateEvent);
        case "onVoiceChannelUpdateEvent" -> gateway.on(VoiceChannelUpdateEvent.class)
          .subscribe(this::onVoiceChannelUpdateEvent);
        case "onCategoryUpdateEvent" -> gateway.on(CategoryUpdateEvent.class)
          .subscribe(this::onCategoryUpdateEvent);
        case "onNewsChannelUpdateEvent" -> gateway.on(NewsChannelUpdateEvent.class)
          .subscribe(this::onNewsChannelUpdateEvent);
        case "onStoreChannelUpdateEvent" -> gateway.on(StoreChannelUpdateEvent.class)
          .subscribe(this::onStoreChannelUpdateEvent);
        case "onTypingStartEvent" -> gateway.on(TypingStartEvent.class)
          .subscribe(this::onTypingStartEvent);
        case "onPinsUpdateEvent" -> gateway.on(PinsUpdateEvent.class)
          .subscribe(this::onPinsUpdateEvent);
        case "onRoleCreateEvent" -> gateway.on(RoleCreateEvent.class)
          .subscribe(this::onRoleCreateEvent);
        case "onRoleDeleteEvent" -> gateway.on(RoleDeleteEvent.class)
          .subscribe(this::onRoleDeleteEvent);
        case "onRoleUpdateEvent" -> gateway.on(RoleUpdateEvent.class)
          .subscribe(this::onRoleUpdateEvent);
        case "onInviteCreateEvent" -> gateway.on(InviteCreateEvent.class)
          .subscribe(this::onInviteCreateEvent);
        case "onInviteDeleteEvent" -> gateway.on(InviteDeleteEvent.class)
          .subscribe(this::onInviteDeleteEvent);
        case "onUserUpdateEvent" -> gateway.on(UserUpdateEvent.class)
          .subscribe(this::onUserUpdateEvent);
        case "onPresenceUpdateEvent" -> gateway.on(PresenceUpdateEvent.class)
          .subscribe(this::onPresenceUpdateEvent);
        case "onVoiceStateUpdateEvent" -> gateway.on(VoiceStateUpdateEvent.class)
          .subscribe(this::onVoiceStateUpdateEvent);
        case "onVoiceServerUpdateEvent" -> gateway.on(VoiceServerUpdateEvent.class)
          .subscribe(this::onVoiceServerUpdateEvent);
        case "onConnectEvent" -> gateway.on(ConnectEvent.class)
          .subscribe(this::onConnectEvent);
        case "onReconnectEvent" -> gateway.on(ReconnectEvent.class)
          .subscribe(this::onReconnectEvent);
        case "onDisconnectEvent" -> gateway.on(DisconnectEvent.class)
          .subscribe(this::onDisconnectEvent);
        case "onReconnectStartEvent" -> gateway.on(ReconnectStartEvent.class)
          .subscribe(this::onReconnectStartEvent);
        case "onReconnectFailEvent" -> gateway.on(ReconnectFailEvent.class)
          .subscribe(this::onReconnectFailEvent);
      }
    }
  }

  // region: events
  public void onReadyEvent(ReadyEvent event) {}

  public void onResumeEvent(ResumeEvent event) {}

  public void onMessageCreateEvent(MessageCreateEvent event) {}

  public void onMessageDeleteEvent(MessageDeleteEvent event) {}

  public void onMessageUpdateEvent(MessageUpdateEvent event) {}

  public void onMessageBulkDeleteEvent(MessageBulkDeleteEvent event) {}

  public void onReactionAddEvent(ReactionAddEvent event) {}

  public void onReactionRemoveEvent(ReactionRemoveEvent event) {}

  public void onReactionRemoveEmojiEvent(ReactionRemoveEmojiEvent event) {}

  public void onReactionRemoveAllEvent(ReactionRemoveAllEvent event) {}

  public void onGuildCreateEvent(GuildCreateEvent event) {}

  public void onGuildDeleteEvent(GuildDeleteEvent event) {}

  public void onGuildUpdateEvent(GuildUpdateEvent event) {}

  public void onMemberJoinEvent(MemberJoinEvent event) {}

  public void onMemberLeaveEvent(MemberLeaveEvent event) {}

  public void onMemberUpdateEvent(MemberUpdateEvent event) {}

  public void onMemberChunkEvent(MemberChunkEvent event) {}

  public void onEmojisUpdateEvent(EmojisUpdateEvent event) {}

  public void onBanEvent(BanEvent event) {}

  public void onUnbanEvent(UnbanEvent event) {}

  public void onIntegrationsUpdateEvent(IntegrationsUpdateEvent event) {}

  public void onWebhooksUpdateEvent(WebhooksUpdateEvent event) {}

  public void onTextChannelCreateEvent(TextChannelCreateEvent event) {}

  public void onVoiceChannelCreateEvent(VoiceChannelCreateEvent event) {}

  public void onCategoryCreateEvent(CategoryCreateEvent event) {}

  public void onNewsChannelCreateEvent(NewsChannelCreateEvent event) {}

  public void onStoreChannelCreateEvent(StoreChannelCreateEvent event) {}

  public void onTextChannelDeleteEvent(TextChannelDeleteEvent event) {}

  public void onVoiceChannelDeleteEvent(VoiceChannelDeleteEvent event) {}

  public void onCategoryDeleteEvent(CategoryDeleteEvent event) {}

  public void onNewsChannelDeleteEvent(NewsChannelDeleteEvent event) {}

  public void onStoreChannelDeleteEvent(StoreChannelDeleteEvent event) {}

  public void onTextChannelUpdateEvent(TextChannelUpdateEvent event) {}

  public void onVoiceChannelUpdateEvent(VoiceChannelUpdateEvent event) {}

  public void onCategoryUpdateEvent(CategoryUpdateEvent event) {}

  public void onNewsChannelUpdateEvent(NewsChannelUpdateEvent event) {}

  public void onStoreChannelUpdateEvent(StoreChannelUpdateEvent event) {}

  public void onTypingStartEvent(TypingStartEvent event) {}

  public void onPinsUpdateEvent(PinsUpdateEvent event) {}

  public void onRoleCreateEvent(RoleCreateEvent event) {}

  public void onRoleDeleteEvent(RoleDeleteEvent event) {}

  public void onRoleUpdateEvent(RoleUpdateEvent event) {}

  public void onInviteCreateEvent(InviteCreateEvent event) {}

  public void onInviteDeleteEvent(InviteDeleteEvent event) {}

  public void onUserUpdateEvent(UserUpdateEvent event) {}

  public void onPresenceUpdateEvent(PresenceUpdateEvent event) {}

  public void onVoiceStateUpdateEvent(VoiceStateUpdateEvent event) {}

  public void onVoiceServerUpdateEvent(VoiceServerUpdateEvent event) {}

  public void onConnectEvent(ConnectEvent event) {}

  public void onReconnectEvent(ReconnectEvent event) {}

  public void onDisconnectEvent(DisconnectEvent event) {}

  public void onReconnectStartEvent(ReconnectStartEvent event) {}

  public void onReconnectFailEvent(ReconnectFailEvent event) {}
  // endregion: events

  public final void register(GatewayDiscordClient gateway) {
    this.registerCommandHandlers(gateway);
    this.registerEventHandlers(gateway);
  }
}
