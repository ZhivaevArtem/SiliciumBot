package com.zhivaevartem.siliciumbot.persistence.service.base;

import com.zhivaevartem.siliciumbot.persistence.entity.base.AbstractGuildEntity;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Service for interacting with
 * {@link com.zhivaevartem.siliciumbot.persistence.entity.base.AbstractGuildEntity} entities.
 *
 * @param <E> Entity to interact with. Entity <strong>MUST have constructor
 *                 with single {@link java.lang.String} parameter</strong>.
 * @param <R> Spring repository type for given entity.
 */
public abstract class AbstractGuildService<E extends AbstractGuildEntity,
    R extends MongoRepository<E, String>> {
  private Class<E> entityClass;

  protected R repository;

  protected final Map<String, E> cache = new HashMap<>();

  protected AbstractGuildService(R repository, Class<E> entityClass) {
    this.repository = repository;
    this.entityClass = entityClass;
  }

  protected E getEntity(String guildId) {
    if (this.cache.containsKey(guildId)) {
      return this.cache.get(guildId);
    }
    E entity;
    entity = this.repository.findById(guildId).orElseGet(() -> {
      try {
        return this.entityClass.getDeclaredConstructor(String.class).newInstance(guildId);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
    assert null != entity;
    return entity;
  }

  protected void saveEntity(E entity) {
    this.cache.put(entity.getGuildId(), entity);
    this.repository.save(entity);
  }

  protected void updateEntity(E entity) {
    E existing = this.getEntity(entity.getGuildId());
    if (!entity.equals(existing)) {
      this.saveEntity(entity);
    }
  }
}
