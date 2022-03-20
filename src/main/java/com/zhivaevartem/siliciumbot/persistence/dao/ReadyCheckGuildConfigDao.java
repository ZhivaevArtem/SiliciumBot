package com.zhivaevartem.siliciumbot.persistence.dao;

import com.zhivaevartem.siliciumbot.persistence.dao.base.AbstractGuildConfigDao;
import com.zhivaevartem.siliciumbot.persistence.dto.ReadyCheckGuildDto;
import org.springframework.stereotype.Repository;

/**
 * {@link ReadyCheckGuildDto} repository.
 */
@Repository
public interface ReadyCheckGuildConfigDao extends AbstractGuildConfigDao<ReadyCheckGuildDto> {}
