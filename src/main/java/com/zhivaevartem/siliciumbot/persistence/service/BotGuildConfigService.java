package com.zhivaevartem.siliciumbot.persistence.service;

import com.zhivaevartem.siliciumbot.persistence.dao.BotGuildConfigRepository;
import com.zhivaevartem.siliciumbot.persistence.entity.BotGuildConfig;
import com.zhivaevartem.siliciumbot.persistence.service.base.AbstractGuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provide access to
 * {@link com.zhivaevartem.siliciumbot.persistence.entity.BotGuildConfig} entities.
 */
@Service
public class BotGuildConfigService
    extends AbstractGuildService<BotGuildConfig, BotGuildConfigRepository> {
  @Autowired
  public BotGuildConfigService(BotGuildConfigRepository repository) {
    super(repository, BotGuildConfig.class);
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
    BotGuildConfig cfg = this.getEntity(guildId);
    cfg.setPrefix(prefix);
    this.updateEntity(cfg);
  }
}
