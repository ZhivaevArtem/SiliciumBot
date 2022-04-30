package com.zhivaevartem.siliciumbot.persistence.global.base;

import org.springframework.data.annotation.Id;

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
