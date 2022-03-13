package com.zhivaevartem.siliciumbot.persistence.dao;

import com.zhivaevartem.siliciumbot.persistence.entity.ReadyCheckGuildConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * {@link com.zhivaevartem.siliciumbot.persistence.entity.ReadyCheckGuildConfig} repository.
 */
public interface ReadyCheckGuildConfigRepository
    extends MongoRepository<ReadyCheckGuildConfig, String> {}
