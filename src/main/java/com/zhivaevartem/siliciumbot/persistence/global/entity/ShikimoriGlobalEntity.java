package com.zhivaevartem.siliciumbot.persistence.global.entity;

import com.zhivaevartem.siliciumbot.persistence.global.base.AbstractGlobalEntity;
import java.util.ArrayList;
import java.util.List;

public class ShikimoriGlobalEntity extends AbstractGlobalEntity {
  private List<String> guilds = new ArrayList<>();

  public List<String> getGuilds() {
    return guilds;
  }

  public void setGuilds(List<String> guilds) {
    this.guilds = guilds;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ShikimoriGlobalEntity dto) {
      return dto.guilds.equals(this.guilds);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = 31 * hash + guilds.hashCode();
    return hash;
  }
}
