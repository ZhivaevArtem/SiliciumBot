package com.zhivaevartem.siliciumbot.persistence.dto;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.persistence.dto.base.AbstractGuildConfigDto;
import java.util.List;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Guild scoped ready check feature configuration.
 */
@Document
public class ReadyCheckGuildDto extends AbstractGuildConfigDto {
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
  }

  private List<ReadyCheckOption> options = StringConstants.DEFAULT_READY_CHECK_OPTIONS;

  private String title = StringConstants.READY_CHECK_EMBED_TITLE;

  private String emptyValue = StringConstants.EMPTY_READY_CHECK_VALUE;

  public ReadyCheckGuildDto() {
    super(StringConstants.UNKNOWN_GUILD_ID);
  }

  public ReadyCheckGuildDto(String guildId) {
    super(guildId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ReadyCheckGuildDto cfg) {
      return Objects.equals(cfg.options, this.options)
        && Objects.equals(cfg.title, this.title)
        && Objects.equals(cfg.emptyValue, this.emptyValue);
    }
    return false;
  }

  public List<ReadyCheckOption> getOptions() {
    return options;
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
