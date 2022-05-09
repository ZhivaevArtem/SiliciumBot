package com.zhivaevartem.siliciumbot.persistence.guild.entity;

import static com.zhivaevartem.siliciumbot.constant.StringConstants.DEFAULT_BOT_COMMAND_PREFIX;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.persistence.guild.base.AbstractGuildEntity;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Guild scoped bot config.
 */
@Document
public class BotConfigGuildEntity extends AbstractGuildEntity {
  private String prefix = DEFAULT_BOT_COMMAND_PREFIX;

  public BotConfigGuildEntity() {
    super(StringConstants.UNKNOWN_GUILD_ID);
  }

  public BotConfigGuildEntity(String guildId) {
    super(guildId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BotConfigGuildEntity cfg) {
      return Objects.equals(cfg.prefix, this.prefix) && Objects.equals(cfg.guildId, this.guildId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.prefix.hashCode();
  }

  public String getPrefix() {
    return this.prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
}
