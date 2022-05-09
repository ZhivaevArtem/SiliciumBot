package com.zhivaevartem.siliciumbot.discord.service;

import com.zhivaevartem.siliciumbot.constant.MarkdownConstants;
import com.zhivaevartem.siliciumbot.constant.RegularExpressionConstants;
import com.zhivaevartem.siliciumbot.constant.ShikimoriConstants;
import com.zhivaevartem.siliciumbot.model.ShikimoriHistoryLog;
import com.zhivaevartem.siliciumbot.persistence.global.service.ShikimoriConfigGlobalEntityService;
import com.zhivaevartem.siliciumbot.persistence.guild.service.ShikimoriConfigGuildEntityService;
import com.zhivaevartem.siliciumbot.util.StringUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

  @Autowired(required = false)
  private GatewayDiscordClient gateway;

  private final Map<String, List<ShikimoriHistoryLog>> logsCache = new HashMap<>();

  @Scheduled(fixedDelayString = "${silicium.shikimori-check-interval}")
  private void checkShikimoriScheduled() {
    List<String> guildsIds = this.globalService.getGuildsIds();
    Set<String> usernames = new HashSet<>();
    Map<String, List<ShikimoriHistoryLog>> userLogs = new HashMap<>();  // username -> logs
    Map<String, List<String>> guildsUsers = new HashMap<>();  // guildId -> usernames
    for (String guildId : guildsIds) {
      List<String> guildUsernames = this.guildService.getUsernames(guildId);
      usernames.addAll(guildUsernames);
      guildsUsers.put(guildId, guildUsernames);
    }
    for (String username : usernames) {
      userLogs.put(username, this.getNewUserLogs(username));
    }
    for (String guildId : guildsIds) {
      List<String> guildUsernames = guildsUsers.get(guildId);
      Map<String, List<ShikimoriHistoryLog>> subMap = userLogs.entrySet().stream()
          .filter(es -> guildUsernames.contains(es.getKey()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      List<EmbedCreateSpec> embeds = this.generateEmbeds(subMap);
      if (embeds != null) {
        String notificationChannelId = this.guildService.getNotificationChannelId(guildId);
        if (!notificationChannelId.isEmpty()) {
          this.gateway.getGuildById(Snowflake.of(guildId)).subscribe(guild -> {
            guild.getChannelById(Snowflake.of(notificationChannelId)).subscribe(channel -> {
              if (channel instanceof MessageChannel messageChannel) {
                messageChannel.createMessage(MessageCreateSpec.create().withEmbeds(embeds))
                    .subscribe();
              }
            });
          });
        }
      }
    }
  }

  @Nullable
  private List<EmbedCreateSpec> generateEmbeds(Map<String, List<ShikimoriHistoryLog>> usersLogs) {
    if (!usersLogs.isEmpty()) {
      List<EmbedCreateSpec> embeds = new ArrayList<>();
      for (String username : usersLogs.keySet()) {
        List<ShikimoriHistoryLog> logs = usersLogs.get(username);
        List<String> values = new ArrayList<>();
        values.add(MarkdownConstants.HYPERLINK
            .replace(MarkdownConstants.TEXT_TOKEN, username)
            .replace(MarkdownConstants.HREF_TOKEN, ShikimoriConstants.USER_PAGE_URL
              .replace(ShikimoriConstants.USERNAME_TOKEN, username)) + ":");
        for (ShikimoriHistoryLog log : logs) {
          if (log != null && log.getTarget() != null) {
            String titleUrl = MarkdownConstants.HYPERLINK
                .replace(MarkdownConstants.TEXT_TOKEN, log.getTarget().getRussian())
                .replace(MarkdownConstants.HREF_TOKEN,
                  ShikimoriConstants.ROOT_URL + log.getTarget().getUrl());
            values.add(titleUrl + ": " + log.getDescription()
                .replaceAll(RegularExpressionConstants.HTML_B_TAG, MarkdownConstants.BOLD));
          }
        }
        if (values.size() > 1) {
          EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
          builder.description(String.join("\n", values));
          builder.color(Color.of(StringUtils.colorFromString(username)));
          embeds.add(builder.build());
        }
      }
      if (!embeds.isEmpty()) {
        return embeds;
      }
    }
    return null;
  }

  private List<ShikimoriHistoryLog> getAllUserLogs(String username) {
    return this.getAllUserLogs(username, -1);
  }

  private List<ShikimoriHistoryLog> getAllUserLogs(String username, int limit) {
    RestTemplate restTemplate = new RestTemplate();
    String url = ShikimoriConstants.USER_HISTORY_API_URL
        .replace(ShikimoriConstants.USERNAME_TOKEN, username);
    if (limit > 0) {
      url += "?limit=" + limit;
    }
    ResponseEntity<ShikimoriHistoryLog[]> response = restTemplate
        .getForEntity(url, ShikimoriHistoryLog[].class);
    ShikimoriHistoryLog[] logs = response.getBody();
    if (logs == null) {
      return new ArrayList<>();
    } else {
      ArrayList<ShikimoriHistoryLog> logsList = new ArrayList<>(logs.length);
      logsList.addAll(Arrays.asList(logs));
      return logsList;
    }
  }

  private List<ShikimoriHistoryLog> getNewUserLogs(String username) {
    List<ShikimoriHistoryLog> allUserLogs = this.getAllUserLogs(username, 20);
    List<ShikimoriHistoryLog> lastUserLogs = allUserLogs.subList(0, 5);
    if (!this.logsCache.containsKey(username)) {
      this.logsCache.put(username, allUserLogs);
      return new ArrayList<>();
    } else {
      List<ShikimoriHistoryLog> cachedLogs = this.logsCache.get(username);
      List<ShikimoriHistoryLog> filteredLogs = lastUserLogs.stream()
          .filter(log -> !cachedLogs.contains(log)).toList();
      this.logsCache.put(username, allUserLogs);
      return filteredLogs;
    }
  }

  /**
   * Add shikimori user to watch list.
   */
  public void addUsername(String guildId, String username) {
    this.guildService.addUsername(guildId, username);
  }

  /**
   * Remove shikimori user from watch list.
   */
  public void removeUsername(String guildId, String username) {
    this.guildService.removeUsername(guildId, username);
  }

  /**
   * Register guild and text channel for receive notifications.
   */
  public void registerGuild(String guildId, String channelId) {
    this.guildService.setNotificationChannelId(guildId, channelId);
    this.globalService.addGuildId(guildId);
  }

  /**
   * Unregister guild and notification channel.
   */
  public void unregisterGuild(String guildId) {
    this.guildService.setNotificationChannelId(guildId, "");
    this.globalService.removeGuildId(guildId);
  }

  public List<String> getUsernames(String guildId) {
    return this.guildService.getUsernames(guildId);
  }

  public List<String> getRegisteredGuildsIds() {
    return this.globalService.getGuildsIds();
  }

  public String getNotificationChannelId(String guildId) {
    return this.guildService.getNotificationChannelId(guildId);
  }
}
