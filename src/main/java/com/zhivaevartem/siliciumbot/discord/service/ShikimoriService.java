package com.zhivaevartem.siliciumbot.discord.service;

import com.zhivaevartem.siliciumbot.model.ShikimoriRawLog;
import com.zhivaevartem.siliciumbot.persistence.global.service.ShikimoriConfigGlobalEntityService;
import com.zhivaevartem.siliciumbot.persistence.guild.service.ShikimoriConfigGuildEntityService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Embed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  private final Map<String, List<Long>> logsCache = new HashMap<>();

  @Scheduled(fixedDelayString = "${silicium.shikimori-check-interval}")
  private void checkShikimoriScheduled() {

  }

  // TODO: make private
  private List<ShikimoriRawLog> retrieveAllUserLogs(String username) {
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
      return new ArrayList<>();
    }
  }

  public List<Embed> generateMessage(String username, List<ShikimoriRawLog> logs) {
    // TODO: implement
    return new ArrayList<>();
  }

  // TODO: make private
  public List<ShikimoriRawLog> retrieveNewUserLogs(String username) {
    List<ShikimoriRawLog> allLogs = this.retrieveAllUserLogs(username);
    if (this.logsCache.containsKey(username)) {
      List<Long> cachedIds = this.logsCache.get(username);
      List<ShikimoriRawLog> newLogs = allLogs.stream().filter(log -> !cachedIds.contains(log.getId())).toList();
      cachedIds.addAll(newLogs.stream().map(ShikimoriRawLog::getId).toList());
      return newLogs;
    } else {
      this.logsCache.put(username, allLogs.stream().map(ShikimoriRawLog::getId).collect(Collectors.toList()));
      return new ArrayList<>();
    }
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
