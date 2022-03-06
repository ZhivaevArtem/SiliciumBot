package com.zhivaevartem.siliciumbot.persistence.dao;

import com.zhivaevartem.siliciumbot.persistence.entity.BotGuildConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * {@link com.zhivaevartem.siliciumbot.persistence.entity.BotGuildConfig} repository.
 */
public interface BotGuildConfigRepository extends MongoRepository<BotGuildConfig, String> { }
