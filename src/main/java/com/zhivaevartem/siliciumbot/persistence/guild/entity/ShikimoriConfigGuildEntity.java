package com.zhivaevartem.siliciumbot.persistence.guild.entity;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.persistence.guild.repo.ShikimoriConfigGuildEntityRepo;
import com.zhivaevartem.siliciumbot.persistence.guild.base.AbstractGuildEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Config for shikimori features.
 * DAO: {@link ShikimoriConfigGuildEntityRepo}.
 * Service: {@link com.zhivaevartem.siliciumbot.discord.service.ShikimoriService}.
 */
@Document
public class ShikimoriConfigGuildEntity extends AbstractGuildEntity {
  private List<String> usernames = new ArrayList<>();
  private String notificationChannel = "";

  public ShikimoriConfigGuildEntity() {
    super(StringConstants.UNKNOWN_GUILD_ID);
  }

  public ShikimoriConfigGuildEntity(String guildId) {
    super(guildId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ShikimoriConfigGuildEntity dto) {
      return this.usernames.equals(dto.usernames)
        && this.notificationChannel.equals(dto.notificationChannel);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = 31 * hash + usernames.hashCode();
    hash = 31 * hash + notificationChannel.hashCode();
    return hash;
  }

  public List<String> getUsernames() {
    return usernames;
  }

  public void setUsernames(List<String> usernames) {
    this.usernames = usernames;
  }

  public String getNotificationChannel() {
    return notificationChannel;
  }

  public void setNotificationChannel(String notificationChannel) {
    this.notificationChannel = notificationChannel;
  }
}
