package com.zhivaevartem.siliciumbot.persistence.global.base;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface GlobalEntityRepo extends MongoRepository<AbstractGlobalEntity, String> {}
