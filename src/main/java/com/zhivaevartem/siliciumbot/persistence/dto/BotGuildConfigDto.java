package com.zhivaevartem.siliciumbot.persistence.dto;

import static com.zhivaevartem.siliciumbot.constant.StringConstants.DEFAULT_BOT_COMMAND_PREFIX;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.persistence.dto.base.AbstractGuildConfigDto;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Guild scoped bot config.
 */
@Document
public class BotGuildConfigDto extends AbstractGuildConfigDto {
  private String prefix = DEFAULT_BOT_COMMAND_PREFIX;

  public BotGuildConfigDto() {
    super(StringConstants.UNKNOWN_GUILD_ID);
  }

  public BotGuildConfigDto(String guildId) {
    super(guildId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BotGuildConfigDto cfg) {
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
