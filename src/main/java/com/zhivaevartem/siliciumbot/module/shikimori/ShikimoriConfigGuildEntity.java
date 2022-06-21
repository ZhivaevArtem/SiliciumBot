package com.zhivaevartem.siliciumbot.module.shikimori;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.core.persistence.guild.AbstractGuildEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Config for shikimori features.
 * DAO: {@link ShikimoriConfigGuildEntityRepo}.
 * Service: {@link ShikimoriService}.
 */
@Document
public class ShikimoriConfigGuildEntity extends AbstractGuildEntity {
  private List<String> usernames = new ArrayList<>();
  private String notificationChannelId = "";

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
          && this.notificationChannelId.equals(dto.notificationChannelId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = 31 * hash + usernames.hashCode();
    hash = 31 * hash + notificationChannelId.hashCode();
    return hash;
  }

  public List<String> getUsernames() {
    return usernames;
  }

  public void setUsernames(List<String> usernames) {
    this.usernames = usernames;
  }

  public String getNotificationChannelId() {
    return notificationChannelId;
  }

  public void setNotificationChannelId(String notificationChannelId) {
    this.notificationChannelId = notificationChannelId;
  }
}
