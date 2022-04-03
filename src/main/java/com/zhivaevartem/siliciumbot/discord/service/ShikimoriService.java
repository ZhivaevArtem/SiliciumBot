package com.zhivaevartem.siliciumbot.discord.service;

import com.zhivaevartem.siliciumbot.persistence.guild.service.ShikimoriConfigGuildEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Shikimori features business logic.
 * Discord event listener: {@link com.zhivaevartem.siliciumbot.discord.listener.ShikimoriListener}
 */
@Service
public class ShikimoriService {
  @Autowired
  private ShikimoriConfigGuildEntityService service;

  @Scheduled(fixedDelayString = "${silicium.shikimori-check-interval}")
  private void checkShikimori() {

  }
}
