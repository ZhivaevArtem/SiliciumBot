package com.zhivaevartem.siliciumbot;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.persistence.guild.entity.ReadyCheckConfigGuildEntity.ReadyCheckOption;
import com.zhivaevartem.siliciumbot.persistence.guild.service.ReadyCheckConfigGuildEntityService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class ReadyCheckGuildConfigServiceTest {
  @Autowired
  private ReadyCheckConfigGuildEntityService service;

  private final String guildId = "test_entity_" + UUID.randomUUID();

  @Test
  public void defaultTitleTest() {
    String title = this.service.getTitle(this.guildId);
    assertEquals(StringConstants.READY_CHECK_EMBED_TITLE, title);
  }

  @Test
  public void defaultEmptyValueTest() {
    String value = this.service.getEmptyValue(this.guildId);
    assertEquals(StringConstants.EMPTY_READY_CHECK_VALUE, value);
  }

  @Test
  public void defaultOptionsTest() {
    List<ReadyCheckOption> options = this.service.getOptions(this.guildId);
    List<ReadyCheckOption> expected = StringConstants.DEFAULT_READY_CHECK_OPTIONS;
    assertEquals(expected, options);
  }

  @Test
  public void setTitleTest() {
    String expected = "8YG7Hasdf346n565er7srnrTGB";
    this.service.setTitle(this.guildId, expected);
    String title = this.service.getTitle(this.guildId);
    assertEquals(expected, title);
  }

  @Test
  public void setEmptyValueTest() {
    String expected = "b56r56eb18vbf68ivb8 97g6";
    this.service.setEmptyValue(this.guildId, expected);
    String value = this.service.getEmptyValue(this.guildId);
    assertEquals(expected, value);
  }

  @Test void setOptionsTest() {
    List<ReadyCheckOption> expected = List.of(new ReadyCheckOption[] {
      new ReadyCheckOption("⬅️", "field 0"),
      new ReadyCheckOption("🗽", "field 1"),
      new ReadyCheckOption("🇧🇳", "field 2"),
      new ReadyCheckOption("✴️", "field 3"),
      new ReadyCheckOption("🌯", "field 4"),
      new ReadyCheckOption("🛺", "field 5")
    });
    this.service.setOptions(this.guildId, expected);
    List<ReadyCheckOption> options = this.service.getOptions(this.guildId);
    assertEquals(expected, options);
  }
}
