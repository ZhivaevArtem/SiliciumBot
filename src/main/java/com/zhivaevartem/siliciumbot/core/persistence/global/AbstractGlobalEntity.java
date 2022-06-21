package com.zhivaevartem.siliciumbot.core.persistence.global;

import org.springframework.data.annotation.Id;

/**
 * Abstract class for those entities which not belong to
 * any discord guilds.
 */
public abstract class AbstractGlobalEntity {
  @Id
  private String className = this.getClass().getName();

  public void setClassName(String className) {
    this.className = className;
  }

  public String getClassName() {
    return this.className;
  }

  public abstract boolean equals(Object obj);

  public abstract int hashCode();
}
