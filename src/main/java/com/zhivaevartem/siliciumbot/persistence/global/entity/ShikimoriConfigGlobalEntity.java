package com.zhivaevartem.siliciumbot.persistence.global.entity;

import com.zhivaevartem.siliciumbot.persistence.global.base.AbstractGlobalEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * Shikimori globals.
 */
@EqualsAndHashCode
public class ShikimoriConfigGlobalEntity extends AbstractGlobalEntity {
  private List<String> guildsIds = new ArrayList<>();

  public List<String> getGuildsIds() {
    return guildsIds;
  }

  public void setGuildsIds(List<String> guildsIds) {
    this.guildsIds = guildsIds;
  }
}
