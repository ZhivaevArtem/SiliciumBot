package com.zhivaevartem.siliciumbot.persistence.service;

import com.zhivaevartem.siliciumbot.persistence.dao.ReadyCheckGuildConfigRepository;
import com.zhivaevartem.siliciumbot.persistence.entity.ReadyCheckGuildConfig;
import com.zhivaevartem.siliciumbot.persistence.entity.ReadyCheckGuildConfig.ReadyCheckOption;
import com.zhivaevartem.siliciumbot.persistence.service.base.AbstractGuildService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provide access to
 * {@link com.zhivaevartem.siliciumbot.persistence.entity.ReadyCheckGuildConfig} entities.
 */
@Service
public class ReadyCheckGuildConfigService
    extends AbstractGuildService<ReadyCheckGuildConfig, ReadyCheckGuildConfigRepository> {
  @Autowired
  public ReadyCheckGuildConfigService(ReadyCheckGuildConfigRepository repository) {
    super(repository, ReadyCheckGuildConfig.class);
  }

  /**
   * Get title of ready check embed message in specified guild.
   *
   * @param guildId Guild id.
   * @return Title of ready check message.
   */
  public String getTitle(String guildId) {
    return this.getEntity(guildId).getTitle();
  }

  /**
   * Set title of ready check embed message for specified guild.
   *
   * @param guildId Guild id.
   * @param title Title of ready check message.
   */
  public void setTitle(String guildId, String title) {
    ReadyCheckGuildConfig cfg = this.getEntity(guildId);
    cfg.setTitle(title);
    this.updateEntity(cfg);
  }

  /**
   * Get text will be printed for options without any votes.
   *
   * @param guildId Guild id.
   * @return Text for options without any votes.
   */
  public String getEmptyValue(String guildId) {
    return this.getEntity(guildId).getEmptyValue();
  }

  /**
   * Set text for ready check option if no one has voted for this option in this guild.
   *
   * @param guildId Guild id.
   * @param emptyValue Text for options without any votes.
   */
  public void setEmptyValue(String guildId, String emptyValue) {
    ReadyCheckGuildConfig cfg = this.getEntity(guildId);
    cfg.setEmptyValue(emptyValue);
    this.updateEntity(cfg);
  }

  /**
   * Set ready check options of specified guild.
   * Ready check option includes emoji unicode and option name.
   *
   * @param guildId Guild id.
   * @return Ready check options.
   */
  public List<ReadyCheckOption> getOptions(String guildId) {
    return this.getEntity(guildId).getOptions();
  }

  /**
   * Set ready check options for specified guild.
   * Ready check option includes emoji unicode and option name.
   *
   * @param guildId Guild id.
   * @param options Ready check options.
   */
  public void setOptions(String guildId, List<ReadyCheckOption> options) {
    ReadyCheckGuildConfig cfg = this.getEntity(guildId);
    cfg.setOptions(options);
    this.updateEntity(cfg);
  }
}
