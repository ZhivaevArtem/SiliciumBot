package com.zhivaevartem.siliciumbot.persistence.service;

import com.zhivaevartem.siliciumbot.persistence.dao.BotGuildConfigRepository;
import com.zhivaevartem.siliciumbot.persistence.entity.BotGuildConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
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

  private final Map<String, BotGuildConfig> cache = new HashMap<>();

  private BotGuildConfig getBotGuildConfig(String guildId) {
    if (cache.containsKey(guildId)) {
      return cache.get(guildId);
    }
    BotGuildConfig cfg = this.repository.findById(guildId).orElse(new BotGuildConfig(guildId));
    this.cache.put(cfg.getGuildId(), cfg);
    return cfg;
  }

  private void saveBotGuildConfig(BotGuildConfig botGuildConfig) {
    this.cache.put(botGuildConfig.getGuildId(), botGuildConfig);
    this.repository.save(botGuildConfig);
  }

  /**
   * Get prefix for specified guild.
   *
   * @param guildId Guild id.
   * @return Prefix.
   */
  public String getPrefix(String guildId) {
    return this.getBotGuildConfig(guildId).getPrefix();
  }

  /**
   * Set prefix for specified guild.
   *
   * @param guildId Guild id.
   * @param prefix New prefix.
   */
  public void setPrefix(String guildId, String prefix) {
    BotGuildConfig cfg = this.getBotGuildConfig(guildId);
    if (!prefix.equals(cfg.getPrefix())) {
      cfg.setPrefix(prefix);
      this.saveBotGuildConfig(cfg);
    }
  }

  /**
   * Get notification channel id of specified guild.
   *
   * @param guildId Guild id.
   * @return Notification channel id.
   */
  @Nullable
  public String getNotificationChannelId(String guildId) {
    return this.getBotGuildConfig(guildId).getNotificationChannelId();
  }

  /**
   * Set notification channel for specified guild.
   *
   * @param guildId Guild id.
   * @param notificationChannelId Id of channel notifications will be sent to.
   */
  public void setNotificationChannelId(String guildId, @Nullable String notificationChannelId) {
    BotGuildConfig cfg = this.getBotGuildConfig(guildId);
    if (!Objects.equals(notificationChannelId, cfg.getNotificationChannelId())) {
      cfg.setNotificationChannelId(notificationChannelId);
    }
  }
}
