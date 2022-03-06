package com.zhivaevartem.siliciumbot.persistence.service;

import com.zhivaevartem.siliciumbot.persistence.dao.BotGuildConfigRepository;
import com.zhivaevartem.siliciumbot.persistence.entity.BotGuildConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provide access to
 * {@link com.zhivaevartem.siliciumbot.persistence.entity.BotGuildConfig} entities.
 */
@Service
public class BotGuildConfigService {
  @Autowired
  private BotGuildConfigRepository repository;

  public BotGuildConfig getBotGuildConfig(String guildId) {
    return repository.findById(guildId).orElse(new BotGuildConfig(guildId));
  }

  public void saveBotGuildConfig(BotGuildConfig config) {
    repository.save(config);
  }
}
