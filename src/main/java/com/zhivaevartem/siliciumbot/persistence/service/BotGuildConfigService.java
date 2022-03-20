package com.zhivaevartem.siliciumbot.persistence.service;

import com.zhivaevartem.siliciumbot.persistence.dao.BotGuildConfigDao;
import com.zhivaevartem.siliciumbot.persistence.dto.BotGuildConfigDto;
import com.zhivaevartem.siliciumbot.persistence.service.base.AbstractGuildConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provide access to
 * {@link BotGuildConfigDto} entities.
 */
@Service
public class BotGuildConfigService
    extends AbstractGuildConfigService<BotGuildConfigDto, BotGuildConfigDao> {
  @Autowired
  public BotGuildConfigService(BotGuildConfigDao repository) {
    super(repository, BotGuildConfigDto.class);
  }

  /**
   * Get command prefix of specified guild.
   *
   * @param guildId Guild id.
   * @return Prefix.
   */
  public String getPrefix(String guildId) {
    return this.getDto(guildId).getPrefix();
  }

  /**
   * Set command prefix for specified guild.
   *
   * @param guildId Guild id.
   * @param prefix New prefix.
   */
  public void setPrefix(String guildId, String prefix) {
    BotGuildConfigDto cfg = this.getDto(guildId);
    cfg.setPrefix(prefix);
    this.updateDto(cfg);
  }
}
