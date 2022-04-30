package com.zhivaevartem.siliciumbot.persistence.guild.service;

import com.zhivaevartem.siliciumbot.persistence.guild.repo.BotConfigGuildEntityRepo;
import com.zhivaevartem.siliciumbot.persistence.guild.entity.BotConfigGuildEntity;
import com.zhivaevartem.siliciumbot.persistence.guild.base.AbstractGuildEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provide access to
 * {@link BotConfigGuildEntity} entities.
 */
@Service
public class BotConfigGuildEntityService
    extends AbstractGuildEntityService<BotConfigGuildEntity, BotConfigGuildEntityRepo> {
  @Autowired
  public BotConfigGuildEntityService(BotConfigGuildEntityRepo repository) {
    super(repository, BotConfigGuildEntity.class);
  }

  /**
   * Get command prefix of specified guild.
   *
   * @param guildId Guild id.
   * @return Prefix.
   */
  public String getPrefix(String guildId) {
    return this.getEntity(guildId).getPrefix();
  }

  /**
   * Set command prefix for specified guild.
   *
   * @param guildId Guild id.
   * @param prefix New prefix.
   */
  public void setPrefix(String guildId, String prefix) {
    BotConfigGuildEntity cfg = this.getEntity(guildId);
    if (!prefix.equals(cfg.getPrefix())) {
      cfg.setPrefix(prefix);
      this.saveEntity(cfg);
    }
  }
}
