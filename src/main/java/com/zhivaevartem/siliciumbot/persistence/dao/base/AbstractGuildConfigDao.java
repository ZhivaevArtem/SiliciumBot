package com.zhivaevartem.siliciumbot.persistence.dao.base;

import com.zhivaevartem.siliciumbot.persistence.dto.base.AbstractGuildConfigDto;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Base interface for all {@link AbstractGuildConfigDto} objects.
 *
 * @param <E> Guild config DTO type.
 */
public interface AbstractGuildConfigDao<E extends AbstractGuildConfigDto>
    extends MongoRepository<E, String> {}
