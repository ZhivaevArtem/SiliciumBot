package com.zhivaevartem.siliciumbot.persistence.entity;

import static com.zhivaevartem.siliciumbot.constants.StringConstants.DEFAULT_BOT_COMMAND_PREFIX;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.NonNull;

/**
 * Guild scoped bot config.
 */
@Document
public class BotGuildConfig {
  @Id
  private String guildId;

  private String prefix = DEFAULT_BOT_COMMAND_PREFIX;

  private String notificationChannelId;

  public BotGuildConfig() { }

  public String getNotificationChannelId() {
    return notificationChannelId;
  }

  public void setNotificationChannelId(String notificationChannelId) {
    this.notificationChannelId = notificationChannelId;
  }

  public BotGuildConfig(String guildId) {
    this.guildId = guildId;
  }

  @NonNull
  public String getGuildId() {
    return this.guildId;
  }

  public void setGuildId(String guildId) {
    this.guildId = guildId;
  }

  public String getPrefix() {
    return this.prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
}
