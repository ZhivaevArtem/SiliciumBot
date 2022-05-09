package com.zhivaevartem.siliciumbot.persistence.global.service;

import com.zhivaevartem.siliciumbot.persistence.global.base.GlobalEntityService;
import com.zhivaevartem.siliciumbot.persistence.global.entity.ShikimoriConfigGlobalEntity;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for manipulating {@link ShikimoriConfigGlobalEntity}
 * entity.
 */
@Service
public class ShikimoriConfigGlobalEntityService {
  @Autowired
  private GlobalEntityService service;

  private final Class<ShikimoriConfigGlobalEntity> entityClass = ShikimoriConfigGlobalEntity.class;

  public void setGuildsIds(List<String> guildsIds) {
    ShikimoriConfigGlobalEntity cfg = this.service.getEntity(this.entityClass);
    if (!guildsIds.equals(cfg.getGuildsIds())) {
      cfg.setGuildsIds(guildsIds);
      this.service.saveEntity(cfg);
    }
  }

  public List<String> getGuildsIds() {
    return this.service.getEntity(this.entityClass).getGuildsIds();
  }

  public void addGuildId(String guildId) {
    ShikimoriConfigGlobalEntity cfg = this.service.getEntity(this.entityClass);
    List<String> guildsIds = new LinkedList<>(cfg.getGuildsIds());
    if (!guildsIds.contains(guildId)) {
      guildsIds.add(guildId);
      cfg.setGuildsIds(guildsIds);
      this.service.saveEntity(cfg);
    }
  }

  public void removeGuildId(String guildId) {
    ShikimoriConfigGlobalEntity cfg = this.service.getEntity(this.entityClass);
    List<String> guildsIds = new LinkedList<>(cfg.getGuildsIds());
    if (guildsIds.removeIf(guildId::equals)) {
      cfg.setGuildsIds(guildsIds);
      this.service.saveEntity(cfg);
    }
  }
}
