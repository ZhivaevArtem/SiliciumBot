package com.zhivaevartem.siliciumbot.persistence.guild.base;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Base interface for all {@link AbstractGuildEntity} objects.
 *
 * @param <E> Guild config DTO type.
 */
public interface AbstractGuildEntityRepo<E extends AbstractGuildEntity>
    extends MongoRepository<E, String> {}
