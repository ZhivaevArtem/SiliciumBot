package com.zhivaevartem.siliciumbot.persistence.services;

import com.zhivaevartem.siliciumbot.persistence.entities.BotConfigLocal;
import javax.persistence.EntityManager;

/**
 * Service for interaction with bot guild scoped configuration entities.
 */
public class BotConfigLocalService extends ServiceBase {
  /**
   * Get bot guild scoped configuration.
   *
   * @param guildId Guild id.
   * @return Bot guild scoped configuration.
   */
  public BotConfigLocal getBotConfigLocal(String guildId) {
    EntityManager em = this.startTransaction();
    BotConfigLocal cfg = em.find(BotConfigLocal.class, guildId);
    if (null == cfg) {
      cfg = new BotConfigLocal(guildId);
      em.persist(cfg);
    }
    this.endTransaction();
    return cfg;
  }
}
