package com.zhivaevartem.siliciumbot.persistence.entity.base;

import org.springframework.data.annotation.Id;

/**
 * Abstract entity class for all guild scoped entities.
 * Extending this you <strong>MUST declare constructor with
 * single {@link java.lang.String} parameter</strong>.
 */
public abstract class AbstractGuildEntity {
  @Id
  protected String guildId;

  protected AbstractGuildEntity(String guildId) {
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
