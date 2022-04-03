package com.zhivaevartem.siliciumbot.persistence.guild.base;

import static com.zhivaevartem.siliciumbot.constant.NumberConstants.CACHE_LIFETIME_MINUTES;
import static com.zhivaevartem.siliciumbot.constant.NumberConstants.GUILD_CONFIG_CACHE_MAX_ENTITIES_COUNT;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

/**
 * Service for interacting with
 * {@link AbstractGuildEntity} entities.
 *
 * @param <E> Entity to interact with. Entity <b>MUST have constructor
 *            with single {@link String} parameter</b>.
 * @param <R> Spring repository type for given entity.
 */
public abstract class AbstractGuildEntityService<E extends AbstractGuildEntity,
    R extends AbstractGuildEntityRepo<E>> {
  private final Class<E> entityClass;

  private final R repo;

  private final Cache<String, E> cache = Caffeine.newBuilder()
      .expireAfterWrite(CACHE_LIFETIME_MINUTES, TimeUnit.MINUTES)
      .maximumSize(GUILD_CONFIG_CACHE_MAX_ENTITIES_COUNT)
      .build();

  public AbstractGuildEntityService(R repo, Class<E> entityClass) {
    this.repo = repo;
    this.entityClass = entityClass;
  }

  protected E getEntity(String guildId) {
    return this.cache.get(guildId, id -> {
      E entity;
      entity = this.repo.findById(id).orElseGet(() -> {
        try {
          return this.entityClass.getDeclaredConstructor(String.class).newInstance(id);
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      });
      assert null != entity;
      return entity;
    });
  }

  protected void saveDto(E entity) {
    this.cache.put(entity.getGuildId(), entity);
    this.repo.save(entity);
  }
}
