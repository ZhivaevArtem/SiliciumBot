package com.zhivaevartem.siliciumbot.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Custom properties in application.yml.
 */
@ConfigurationProperties("silicium")
@Configuration
@EnableConfigurationProperties(SiliciumProperties.class)
@Data
public class SiliciumProperties {
  private String discordToken = "";
  private int readyCheckUpdateInterval = 2500;
  private int shikimoriCheckInterval = 45000;
  private String youtubeToken = "";
}
