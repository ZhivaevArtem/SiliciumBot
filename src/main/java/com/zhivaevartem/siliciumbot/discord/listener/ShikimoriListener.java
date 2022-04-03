package com.zhivaevartem.siliciumbot.discord.listener;

import com.zhivaevartem.siliciumbot.discord.listener.base.AbstractEventListener;
import com.zhivaevartem.siliciumbot.discord.service.ShikimoriService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Discord event listener for shikimori features.
 * Business logic: {@link ShikimoriService}
 */
@Component
public class ShikimoriListener extends AbstractEventListener {
  @Autowired
  private ShikimoriService service;
}
