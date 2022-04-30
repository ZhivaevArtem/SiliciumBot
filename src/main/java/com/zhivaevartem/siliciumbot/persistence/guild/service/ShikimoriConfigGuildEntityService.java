package com.zhivaevartem.siliciumbot.persistence.guild.service;

import com.zhivaevartem.siliciumbot.persistence.guild.repo.ShikimoriConfigGuildEntityRepo;
import com.zhivaevartem.siliciumbot.persistence.guild.entity.ShikimoriConfigGuildEntity;
import com.zhivaevartem.siliciumbot.persistence.guild.base.AbstractGuildEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Service for manipulating {@link ShikimoriConfigGuildEntity}.
 */
@Service
public class ShikimoriConfigGuildEntityService
    extends AbstractGuildEntityService<ShikimoriConfigGuildEntity, ShikimoriConfigGuildEntityRepo> {
  @Autowired
  public ShikimoriConfigGuildEntityService(ShikimoriConfigGuildEntityRepo dao) {
    super(dao, ShikimoriConfigGuildEntity.class);
  }

  public void setNotificationChannelId(String guildId, String notificationChannelId) {
    ShikimoriConfigGuildEntity cfg = this.getEntity(guildId);
    if (!notificationChannelId.equals(cfg.getNotificationChannelId())) {
      cfg.setNotificationChannelId(notificationChannelId);
      this.saveEntity(cfg);
    }
  }

  public String getNotificationChannelId(String guildId) {
    return this.getEntity(guildId).getNotificationChannelId();
  }

  public void setUsernames(String guildId, List<String> usernames) {
    ShikimoriConfigGuildEntity cfg = this.getEntity(guildId);
    if (!usernames.equals(cfg.getUsernames())) {
      cfg.setUsernames(usernames);
      this.saveEntity(cfg);
    }
  }

  public List<String> getUsernames(String guildId) {
    return this.getEntity(guildId).getUsernames();
  }

  public void addUsername(String guildId, String username) {
    ShikimoriConfigGuildEntity cfg = this.getEntity(guildId);
    List<String> usernames = new LinkedList<>(cfg.getUsernames());
    if (!usernames.contains(username)) {
      usernames.add(username);
      cfg.setUsernames(usernames);
      this.saveEntity(cfg);
    }
  }

  public void removeUsername(String guildId, String username) {
    ShikimoriConfigGuildEntity cfg = this.getEntity(guildId);
    List<String> usernames = new LinkedList<>(cfg.getUsernames());
    if (usernames.removeIf(username::equals)) {
      cfg.setUsernames(usernames);
      this.saveEntity(cfg);
    }
  }
}
