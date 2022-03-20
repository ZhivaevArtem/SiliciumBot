package com.zhivaevartem.siliciumbot.discord.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring scheduling configuration.
 */
@Configuration
@Profile("!test")
@EnableScheduling
public class SchedulingConfiguration {}
