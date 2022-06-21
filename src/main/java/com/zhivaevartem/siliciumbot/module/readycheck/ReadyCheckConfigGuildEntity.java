package com.zhivaevartem.siliciumbot.module.readycheck;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.core.persistence.guild.AbstractGuildEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Guild scoped ready check feature configuration.
 */
@Document
public class ReadyCheckConfigGuildEntity extends AbstractGuildEntity {
  /**
   * Ready check option. Includes emoji unicode and option name.
   */
  public static class ReadyCheckOption {
    public String emoji;
    public String name;

    public ReadyCheckOption() {}

    public ReadyCheckOption(String emoji, String name) {
      this.emoji = emoji;
      this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ReadyCheckOption rcOpt) {
        return this.emoji.equals(rcOpt.emoji) && this.name.equals(rcOpt.name);
      }
      return false;
    }

    @Override
    public int hashCode() {
      int hash = 1;
      hash = 31 * hash + this.emoji.hashCode();
      hash = 31 * hash + this.name.hashCode();
      return hash;
    }
  }

  private List<ReadyCheckOption> options = StringConstants.DEFAULT_READY_CHECK_OPTIONS;

  private String title = StringConstants.READY_CHECK_EMBED_TITLE;

  private String emptyValue = StringConstants.EMPTY_READY_CHECK_VALUE;

  public ReadyCheckConfigGuildEntity() {
    super(StringConstants.UNKNOWN_GUILD_ID);
  }

  public ReadyCheckConfigGuildEntity(String guildId) {
    super(guildId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ReadyCheckConfigGuildEntity cfg) {
      return Objects.equals(cfg.options, this.options)
          && Objects.equals(cfg.title, this.title)
          && Objects.equals(cfg.emptyValue, this.emptyValue);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = 31 * hash + this.emptyValue.hashCode();
    hash = 31 * hash + this.options.hashCode();
    hash = 31 * hash + this.title.hashCode();
    return hash;
  }

  public List<ReadyCheckOption> getOptions() {
    return new ArrayList<>(this.options);
  }

  public void setOptions(List<ReadyCheckOption> options) {
    this.options = options;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getEmptyValue() {
    return emptyValue;
  }

  public void setEmptyValue(String emptyValue) {
    this.emptyValue = emptyValue;
  }
}
