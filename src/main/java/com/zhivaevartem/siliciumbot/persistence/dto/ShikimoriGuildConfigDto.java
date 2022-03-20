package com.zhivaevartem.siliciumbot.persistence.dto;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.persistence.dto.base.AbstractGuildConfigDto;
import java.util.ArrayList;
import java.util.List;

public class ShikimoriGuildConfigDto extends AbstractGuildConfigDto {
  private List<String> usernames = new ArrayList<>();

  private String channelId = "";

  protected ShikimoriGuildConfigDto(String guildId) {
    super(guildId);
  }

  protected ShikimoriGuildConfigDto() {
    super(StringConstants.UNKNOWN_GUILD_ID);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ShikimoriGuildConfigDto cfg) {
      return this.channelId.equals(cfg.channelId) && this.usernames.equals(cfg.usernames);
    }
    return false;
  }

  public List<String> getUsernames() {
    return usernames;
  }

  public void setUsernames(List<String> usernames) {
    this.usernames = usernames;
  }

  public String getChannelId() {
    return channelId;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }
}
