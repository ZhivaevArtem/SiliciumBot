package com.zhivaevartem.siliciumbot.module.shikimori;

import com.zhivaevartem.siliciumbot.core.persistence.global.AbstractGlobalEntity;
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
