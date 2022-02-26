package com.zhivaevartem.siliciumbot.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Bot guild scoped config.
 */
@Entity
public class BotConfigLocal {
  @Id
  private String guildId;

  private String prefix;

  public BotConfigLocal() { }

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
