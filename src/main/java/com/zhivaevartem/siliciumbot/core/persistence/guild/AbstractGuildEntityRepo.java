package com.zhivaevartem.siliciumbot.core.persistence.guild;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Base interface for all {@link AbstractGuildEntity} objects.
 *
 * @param <E> Guild config DTO type.
 */
public interface AbstractGuildEntityRepo<E extends AbstractGuildEntity>
    extends MongoRepository<E, String> {}
