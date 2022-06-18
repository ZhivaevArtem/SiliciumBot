package com.zhivaevartem.siliciumbot.core.persistence.guild;

import org.springframework.data.annotation.Id;

/**
 * Abstract entity class for all guild scoped entities.
 * Extending this you <b>MUST declare constructor with
 * single {@link String} parameter</b>.
 */
public abstract class AbstractGuildEntity {
  @Id
  protected String guildId;

  public AbstractGuildEntity(String guildId) {
    this.guildId = guildId;
  }

  public final String getGuildId() {
    return guildId;
  }

  public final void setGuildId(String guildId) {
    this.guildId = guildId;
  }

  public abstract boolean equals(Object obj);

  public abstract int hashCode();
}
