package com.zhivaevartem.siliciumbot.persistence.dto.base;

import org.springframework.data.annotation.Id;

/**
 * Abstract entity class for all guild scoped entities.
 * Extending this you <b>MUST declare constructor with
 * single {@link String} parameter</b>.
 */
public abstract class AbstractGuildConfigDto {
  @Id
  protected String guildId;

  protected AbstractGuildConfigDto(String guildId) {
    this.guildId = guildId;
  }

  public final String getGuildId() {
    return guildId;
  }

  public final void setGuildId(String guildId) {
    this.guildId = guildId;
  }

  public abstract boolean equals(Object obj);
}
