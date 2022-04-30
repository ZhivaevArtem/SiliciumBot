package com.zhivaevartem.siliciumbot.discord.service;

import com.zhivaevartem.siliciumbot.model.ShikimoriRawLog;
import com.zhivaevartem.siliciumbot.persistence.global.service.ShikimoriConfigGlobalEntityService;
import com.zhivaevartem.siliciumbot.persistence.guild.service.ShikimoriConfigGuildEntityService;
import discord4j.core.GatewayDiscordClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Shikimori features business logic.
 * Discord event listener: {@link com.zhivaevartem.siliciumbot.discord.listener.ShikimoriListener}
 */
@Service
public class ShikimoriService {
  @Autowired
  private ShikimoriConfigGuildEntityService guildService;

  @Autowired
  private ShikimoriConfigGlobalEntityService globalService;

  @Autowired
  private GatewayDiscordClient gateway;

  @Scheduled(fixedDelayString = "${silicium.shikimori-check-interval}")
  private void checkShikimoriScheduled() {

  }

  // TODO: make private
  public List<ShikimoriRawLog> retrieveAllUserLogs(String username) {
    try {
      Document doc = Jsoup.connect("https://shikimori.one/" + username + "/history/logs").get();
      Elements logElements = doc.select(".b-user_rate_log");
      List<ShikimoriRawLog> rawLogs = new ArrayList<>();
      for (Element logElement : logElements) {
        ShikimoriRawLog shikimoriRawLog = ShikimoriRawLog.create(logElement);
        rawLogs.add(shikimoriRawLog);
      }
      return rawLogs;
    } catch (Exception e) {
       e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<ShikimoriRawLog> retrieveNewUserLogs(String username) {
    // TODO: implement
    return this.retrieveAllUserLogs(username);
  }

  public void addUsername(String guildId, String username) {
    this.guildService.addUsername(guildId, username);
  }

  public void removeUsername(String guildId, String username) {
    this.guildService.removeUsername(guildId, username);
  }

  public void registerGuild(String guildId, String channelId) {
    this.guildService.setNotificationChannelId(guildId, channelId);
    this.globalService.addGuildId(guildId);
  }

  public void unregisterGuild(String guildId) {
    this.guildService.setNotificationChannelId(guildId, "");
    this.globalService.removeGuildId(guildId);
  }
}
