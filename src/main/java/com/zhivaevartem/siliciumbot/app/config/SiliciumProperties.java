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
  private String discordToken = "";
  private int readyCheckUpdateInterval = 2500;
  private String youtubeToken = "";
  private int shikimoriBetweenRequests = 3000;
  private String version = "";

  public String getDiscordToken() {
    return discordToken;
  }

  public int getReadyCheckUpdateInterval() {
    return readyCheckUpdateInterval;
  }

  public String getYoutubeToken() {
    return youtubeToken;
  }

  public int getShikimoriBetweenRequests() {
    return shikimoriBetweenRequests;
  }

  public String getVersion() {
    return version;
  }

  public void setDiscordToken(String discordToken) {
    this.discordToken = discordToken;
  }

  public void setReadyCheckUpdateInterval(int readyCheckUpdateInterval) {
    this.readyCheckUpdateInterval = readyCheckUpdateInterval;
  }

  public void setYoutubeToken(String youtubeToken) {
    this.youtubeToken = youtubeToken;
  }

  public void setShikimoriBetweenRequests(int shikimoriBetweenRequests) {
    this.shikimoriBetweenRequests = shikimoriBetweenRequests;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
