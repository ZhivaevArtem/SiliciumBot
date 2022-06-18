package com.zhivaevartem.siliciumbot.core.persistence.global;

import static com.zhivaevartem.siliciumbot.constant.NumberConstants.CACHE_GLOBAL_CONFIG_MAX_SIZE;
import static com.zhivaevartem.siliciumbot.constant.NumberConstants.CACHE_LIFETIME_MINUTES;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for manipulating with {@link AbstractGlobalEntity} children.
 */
@Service
public class GlobalEntityService {
  @Autowired
  private GlobalEntityRepo repo;

  private final Cache<String, AbstractGlobalEntity> cache = Caffeine.newBuilder()
      .expireAfterWrite(CACHE_LIFETIME_MINUTES, TimeUnit.MINUTES)
      .maximumSize(CACHE_GLOBAL_CONFIG_MAX_SIZE)
      .build();

  /**
   * Get global entity by class.
   */
  public <E extends AbstractGlobalEntity> E getEntity(Class<E> clazz) {
    String className = clazz.getName();
    AbstractGlobalEntity ret = this.cache.get(className, id -> {
      AbstractGlobalEntity entity;
      entity = this.repo.findById(id).orElseGet(() -> {
        try {
          return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      });
      assert null != entity;
      return entity;
    });
    if (clazz.isInstance(ret)) {
      return clazz.cast(ret);
    }
    assert false;
    return null;
  }

  /**
   * Save global entity.
   */
  public <E extends AbstractGlobalEntity> void saveEntity(E entity) {
    this.cache.put(entity.getClass().getName(), entity);
    this.repo.save(entity);
  }
}
