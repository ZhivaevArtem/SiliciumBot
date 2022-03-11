package com.zhivaevartem.siliciumbot.persistence.entity;

import static com.zhivaevartem.siliciumbot.constant.StringConstants.DEFAULT_BOT_COMMAND_PREFIX;

import javax.annotation.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Guild scoped bot config.
 */
@Document
public class BotGuildConfig {
  @Id
  private String guildId;

  private String prefix = DEFAULT_BOT_COMMAND_PREFIX;

  @Nullable
  private String notificationChannelId;

  public BotGuildConfig() { }

  @Nullable
  public String getNotificationChannelId() {
    return notificationChannelId;
  }

  public void setNotificationChannelId(@Nullable String notificationChannelId) {
    this.notificationChannelId = notificationChannelId;
  }

  public BotGuildConfig(String guildId) {
    this.guildId = guildId;
  }

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
