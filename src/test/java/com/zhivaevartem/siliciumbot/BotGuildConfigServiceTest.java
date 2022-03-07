package com.zhivaevartem.siliciumbot;

import static com.zhivaevartem.siliciumbot.constants.StringConstants.DEFAULT_BOT_COMMAND_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import com.zhivaevartem.siliciumbot.persistence.service.BotGuildConfigService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class BotGuildConfigServiceTest {
  @Autowired
  private BotGuildConfigService botGuildConfigService;

  private final String guildId = "test_entity_" + UUID.randomUUID();

  @Test
  public void defaultPrefixExistTest() {
    final String prefix = this.botGuildConfigService.getPrefix(this.guildId);

    assertEquals(DEFAULT_BOT_COMMAND_PREFIX, prefix);
  }

  @Test
  public void canChangePrefixTest() {
    final String oldPrefix = this.botGuildConfigService.getPrefix(this.guildId);
    final String newPrefix = "!!!!" + oldPrefix;

    this.botGuildConfigService.setPrefix(this.guildId, newPrefix);

    assertEquals(newPrefix, this.botGuildConfigService.getPrefix(this.guildId));
  }

  @Test
  public void canSetNotificationChannelIdNonNullTest() {
    final String notificationChannelId = "68bvr568778b89ny";

    this.botGuildConfigService.setNotificationChannelId(this.guildId, notificationChannelId);

    assertEquals(notificationChannelId, this.botGuildConfigService.getNotificationChannelId(this.guildId));
  }

  @Test
  public void canSetNotificationChannelIdNullTest() {
    this.botGuildConfigService.setNotificationChannelId(this.guildId, null);

    assertNull(this.botGuildConfigService.getNotificationChannelId(this.guildId));
  }
}
