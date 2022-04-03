package com.zhivaevartem.siliciumbot.persistence.guild.service;

import com.zhivaevartem.siliciumbot.persistence.guild.repo.ShikimoriConfigGuildEntityRepo;
import com.zhivaevartem.siliciumbot.persistence.guild.entity.ShikimoriConfigGuildEntity;
import com.zhivaevartem.siliciumbot.persistence.guild.base.AbstractGuildEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for manipulating {@link ShikimoriConfigGuildEntity}.
 */
@Service
public class ShikimoriConfigGuildEntityService
    extends AbstractGuildEntityService<ShikimoriConfigGuildEntity, ShikimoriConfigGuildEntityRepo> {
  @Autowired
  public ShikimoriConfigGuildEntityService(ShikimoriConfigGuildEntityRepo dao) {
    super(dao, ShikimoriConfigGuildEntity.class);
  }
}
