package com.zhivaevartem.siliciumbot.module.shikimori;

import com.zhivaevartem.siliciumbot.constant.MarkdownConstants;
import com.zhivaevartem.siliciumbot.constant.RegularExpressionConstants;
import com.zhivaevartem.siliciumbot.constant.ShikimoriConstants;
import com.zhivaevartem.siliciumbot.util.StringUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Shikimori features business logic.
 * Discord event listener: {@link ShikimoriListener}
 */
@Service
public class ShikimoriService {
  @Autowired
  private ShikimoriConfigGuildEntityService guildService;

  @Autowired
  private ShikimoriConfigGlobalEntityService globalService;

  @Autowired(required = false)
  private GatewayDiscordClient gateway;

  @Value("${silicium.version}")
  private String buildVersion;

  private Iterator<Map.Entry<String, Set<String>>> userIterator;

  private final Map<String, List<ShikimoriHistoryLog>> logsCache = new HashMap<>();

  private final Logger logger = LoggerFactory.getLogger(ShikimoriService.class);

  @Scheduled(fixedRateString = "${silicium.shikimori-check-interval}")
  private void checkShikimoriScheduled() {
    if (userIterator == null || !userIterator.hasNext()) {
      List<String> guildsIds = this.globalService.getGuildsIds();
      Map<String, Set<String>> userGuilds = new HashMap<>();  // username => guildIds
      for (String guildId : guildsIds) {
        List<String> guildUsernames = this.guildService.getUsernames(guildId);
        guildUsernames.forEach(username -> {
          if (!userGuilds.containsKey(username)) {
            userGuilds.put(username, new HashSet<>());
          }
          userGuilds.get(username).add(guildId);
        });
      }
      this.userIterator = userGuilds.entrySet().iterator();
    }
    Map.Entry<String, Set<String>> user = this.userIterator.next();
    String username = user.getKey();
    this.logger.info("Check for user: " + username);
    Set<String> guildIds = user.getValue();
    List<ShikimoriHistoryLog> logs = this.getNewUserLogs(username);
    this.sendNotifications(guildIds, new HashMap<>() {
      {
        put(username, logs);
      }
    });
  }

  private void sendNotifications(Collection<String> guildIds, Map<String, List<ShikimoriHistoryLog>> usersLogs) {
    List<EmbedCreateSpec> embeds = this.generateEmbeds(usersLogs);
    if (embeds != null && embeds.size() > 0) {
      guildIds.forEach(guildId -> {
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
      });
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
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "SiliciumBotChan/" + this.buildVersion + " Discord bot for me and my friends");
    HttpEntity<Object> request = new HttpEntity<>(headers);
    String url = ShikimoriConstants.USER_HISTORY_API_URL
        .replace(ShikimoriConstants.USERNAME_TOKEN, username);
    if (limit > 0) {
      url += "?limit=" + limit;
    }
    ResponseEntity<ShikimoriHistoryLog[]> response;
    try {
      response = restTemplate.exchange(url, HttpMethod.GET, request, ShikimoriHistoryLog[].class);
    } catch (HttpClientErrorException e) {
      e.printStackTrace();
      return new ArrayList<>(0);
    }
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
