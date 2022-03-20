package com.zhivaevartem.siliciumbot.persistence.service.base;

import com.zhivaevartem.siliciumbot.persistence.dao.base.AbstractGuildConfigDao;
import com.zhivaevartem.siliciumbot.persistence.dto.base.AbstractGuildConfigDto;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for interacting with
 * {@link AbstractGuildConfigDto} entities.
 *
 * @param <E> Entity to interact with. Entity <b>MUST have constructor
 *            with single {@link String} parameter</b>.
 * @param <R> Spring repository type for given entity.
 */
public abstract class AbstractGuildConfigService<E extends AbstractGuildConfigDto,
    R extends AbstractGuildConfigDao<E>> {
  private Class<E> dtoClass;

  protected R dao;

  protected final Map<String, E> cache = new HashMap<>();

  protected AbstractGuildConfigService(R dao, Class<E> dtoClass) {
    this.dao = dao;
    this.dtoClass = dtoClass;
  }

  protected E getDto(String guildId) {
    if (this.cache.containsKey(guildId)) {
      return this.cache.get(guildId);
    }
    E dto;
    dto = this.dao.findById(guildId).orElseGet(() -> {
      try {
        return this.dtoClass.getDeclaredConstructor(String.class).newInstance(guildId);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
    assert null != dto;
    return dto;
  }

  protected void saveDto(E dto) {
    this.cache.put(dto.getGuildId(), dto);
    this.dao.save(dto);
  }

  protected void updateDto(E dto) {
    E existing = this.getDto(dto.getGuildId());
    if (!dto.equals(existing)) {
      this.saveDto(dto);
    }
  }
}
