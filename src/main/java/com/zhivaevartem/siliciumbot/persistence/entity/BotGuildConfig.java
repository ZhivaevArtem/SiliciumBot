package com.zhivaevartem.siliciumbot.persistence.entity;

import static com.zhivaevartem.siliciumbot.constant.StringConstants.DEFAULT_BOT_COMMAND_PREFIX;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.persistence.entity.base.AbstractGuildEntity;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Guild scoped bot config.
 */
@Document
public class BotGuildConfig extends AbstractGuildEntity {
  private String prefix = DEFAULT_BOT_COMMAND_PREFIX;

  public BotGuildConfig() {
    super(StringConstants.UNKNOWN_GUILD_ID);
  }

  public BotGuildConfig(String guildId) {
    super(guildId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BotGuildConfig cfg) {
      return Objects.equals(cfg.prefix, this.prefix) && Objects.equals(cfg.guildId, this.guildId);
    }
    return false;
  }

  public String getPrefix() {
    return this.prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
}
