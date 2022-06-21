package com.zhivaevartem.siliciumbot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhivaevartem.siliciumbot.core.persistence.global.GlobalEntityService;
import com.zhivaevartem.siliciumbot.module.shikimori.ShikimoriConfigGlobalEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class GlobalEntityRepoServiceTest {
  @Autowired
  private GlobalEntityService service;

  @Test
  public void canSaveShikimoriConfigTest() {
    ShikimoriConfigGlobalEntity dto = new ShikimoriConfigGlobalEntity();
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    service.saveEntity(dto);
  }

  @Test
  public void getShikimoriConfigTest() {
    ShikimoriConfigGlobalEntity dto = new ShikimoriConfigGlobalEntity();
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    dto.getGuildsIds().add("42532534534");
    service.saveEntity(dto);
    ShikimoriConfigGlobalEntity entity = service.getEntity(ShikimoriConfigGlobalEntity.class);
    assertEquals(dto, entity);
  }
}
