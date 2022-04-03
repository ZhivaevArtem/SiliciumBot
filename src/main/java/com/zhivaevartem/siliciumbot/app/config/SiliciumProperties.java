package com.zhivaevartem.siliciumbot.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Custom properties in application.yml.
 */
@ConfigurationProperties("silicium")
@Configuration
@EnableConfigurationProperties(SiliciumProperties.class)
public class SiliciumProperties {
  private String token;
  private int readycheckUpdateInterval;
  private int shikimoriCheckInterval;

  public int getShikimoriCheckInterval() {
    return shikimoriCheckInterval;
  }

  public void setShikimoriCheckInterval(int shikimoriCheckInterval) {
    this.shikimoriCheckInterval = shikimoriCheckInterval;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public int getReadycheckUpdateInterval() {
    return readycheckUpdateInterval;
  }

  public void setReadycheckUpdateInterval(int readycheckUpdateInterval) {
    this.readycheckUpdateInterval = readycheckUpdateInterval;
  }
}
