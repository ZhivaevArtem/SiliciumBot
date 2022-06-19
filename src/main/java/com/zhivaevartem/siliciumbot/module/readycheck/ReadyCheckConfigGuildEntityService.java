package com.zhivaevartem.siliciumbot.module.readycheck;

import com.zhivaevartem.siliciumbot.core.persistence.guild.AbstractGuildEntityService;
import com.zhivaevartem.siliciumbot.module.readycheck.ReadyCheckConfigGuildEntity.ReadyCheckOption;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provide access to
 * {@link ReadyCheckConfigGuildEntity} entities.
 */
@Service
public class ReadyCheckConfigGuildEntityService extends
    AbstractGuildEntityService<ReadyCheckConfigGuildEntity, ReadyCheckConfigGuildEntityRepo> {
  @Autowired
  public ReadyCheckConfigGuildEntityService(ReadyCheckConfigGuildEntityRepo repository) {
    super(repository, ReadyCheckConfigGuildEntity.class);
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
    ReadyCheckConfigGuildEntity cfg = this.getEntity(guildId);
    if (!title.equals(cfg.getTitle())) {
      cfg.setTitle(title);
      this.saveEntity(cfg);
    }
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
    ReadyCheckConfigGuildEntity cfg = this.getEntity(guildId);
    if (!emptyValue.equals(cfg.getEmptyValue())) {
      cfg.setEmptyValue(emptyValue);
      this.saveEntity(cfg);
    }
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
    ReadyCheckConfigGuildEntity cfg = this.getEntity(guildId);
    if (!options.equals(cfg.getOptions())) {
      cfg.setOptions(options);
      this.saveEntity(cfg);
    }
  }
}
