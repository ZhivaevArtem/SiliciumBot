package com.zhivaevartem.siliciumbot;

import static com.zhivaevartem.siliciumbot.constant.StringConstants.DEFAULT_BOT_COMMAND_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhivaevartem.siliciumbot.persistence.guild.service.BotConfigGuildEntityService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class BotGuildConfigServiceTest {
  @Autowired
  private BotConfigGuildEntityService service;

  private final String guildId = "test_entity_" + UUID.randomUUID();

  @Test
  public void defaultPrefixExistTest() {
    String prefix = this.service.getPrefix(this.guildId);

    assertEquals(DEFAULT_BOT_COMMAND_PREFIX, prefix);
  }

  @Test
  public void canChangePrefixTest() {
    String oldPrefix = this.service.getPrefix(this.guildId);
    String newPrefix = "!!!!" + oldPrefix;

    this.service.setPrefix(this.guildId, newPrefix);

    assertEquals(newPrefix, this.service.getPrefix(this.guildId));
  }
}
