package com.zhivaevartem.siliciumbot.core.persistence.global;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for {@link AbstractGlobalEntity} children.
 */
public interface GlobalEntityRepo extends MongoRepository<AbstractGlobalEntity, String> {}
