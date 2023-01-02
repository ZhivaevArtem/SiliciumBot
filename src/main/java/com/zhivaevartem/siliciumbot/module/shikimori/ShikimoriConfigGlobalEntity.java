package com.zhivaevartem.siliciumbot.module.shikimori;

import com.zhivaevartem.siliciumbot.core.persistence.global.AbstractGlobalEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Shikimori globals.
 */
public class ShikimoriConfigGlobalEntity extends AbstractGlobalEntity {
  private List<String> guildsIds = new ArrayList<>();

  public List<String> getGuildsIds() {
    return guildsIds;
  }

  public void setGuildsIds(List<String> guildsIds) {
    this.guildsIds = guildsIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ShikimoriConfigGlobalEntity that = (ShikimoriConfigGlobalEntity) o;
    return Objects.equals(guildsIds, that.guildsIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guildsIds);
  }
}
