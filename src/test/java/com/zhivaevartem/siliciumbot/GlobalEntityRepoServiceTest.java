package com.zhivaevartem.siliciumbot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhivaevartem.siliciumbot.persistence.global.GlobalEntityService;
import com.zhivaevartem.siliciumbot.persistence.global.entity.ShikimoriGlobalEntity;
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
    ShikimoriGlobalEntity dto = new ShikimoriGlobalEntity();
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    service.saveEntity(dto);
  }

  @Test
  public void getShikimoriConfigTest() {
    ShikimoriGlobalEntity dto = new ShikimoriGlobalEntity();
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    dto.getGuilds().add("42532534534");
    service.saveEntity(dto);
    ShikimoriGlobalEntity entity = service.getEntity(ShikimoriGlobalEntity.class);
    assertEquals(dto, entity);
  }
}
