package com.zhivaevartem.siliciumbot.persistence.global.entity;

import com.zhivaevartem.siliciumbot.persistence.global.base.AbstractGlobalEntity;
import java.util.ArrayList;
import java.util.List;

public class ShikimoriConfigGlobalEntity extends AbstractGlobalEntity {
  private List<String> guildsIds = new ArrayList<>();

  public List<String> getGuildsIds() {
    return guildsIds;
  }

  public void setGuildsIds(List<String> guildsIds) {
    this.guildsIds = guildsIds;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ShikimoriConfigGlobalEntity entity) {
      return entity.guildsIds.equals(this.guildsIds);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = 31 * hash + guildsIds.hashCode();
    return hash;
  }
}
