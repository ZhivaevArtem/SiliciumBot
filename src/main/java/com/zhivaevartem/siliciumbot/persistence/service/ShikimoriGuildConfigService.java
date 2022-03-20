package com.zhivaevartem.siliciumbot.persistence.service;

import com.zhivaevartem.siliciumbot.persistence.dao.ShikimoriGuildConfigDao;
import com.zhivaevartem.siliciumbot.persistence.dto.ShikimoriGuildConfigDto;
import com.zhivaevartem.siliciumbot.persistence.service.base.AbstractGuildConfigService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShikimoriGuildConfigService
    extends AbstractGuildConfigService<ShikimoriGuildConfigDto, ShikimoriGuildConfigDao> {
  @Autowired
  public ShikimoriGuildConfigService(ShikimoriGuildConfigDao dao) {
    super(dao, ShikimoriGuildConfigDto.class);
  }

  public List<String> getUsernames(String guildId) {
    return this.getDto(guildId).getUsernames();
  }

  public void setUsernames(String guildId, List<String> usernames) {
    ShikimoriGuildConfigDto cfg = this.getDto(guildId);
    cfg.setUsernames(usernames);
    this.updateDto(cfg);
  }

  public String getChannelId(String guildId) {
    return this.getDto(guildId).getChannelId();
  }

  public void setChannelId(String guildId, String channelId) {
    ShikimoriGuildConfigDto cfg = this.getDto(guildId);
    cfg.setChannelId(guildId);
    this.updateDto(cfg);
  }
}
