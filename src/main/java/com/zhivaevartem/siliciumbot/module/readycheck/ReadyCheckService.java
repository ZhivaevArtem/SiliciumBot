package com.zhivaevartem.siliciumbot.module.readycheck;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zhivaevartem.siliciumbot.constant.NumberConstants;
import com.zhivaevartem.siliciumbot.module.readycheck.ReadyCheckConfigGuildEntity.ReadyCheckOption;
import discord4j.core.object.Embed;
import discord4j.core.object.Embed.Field;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import discord4j.core.spec.MessageEditSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for {@link ReadyCheckListener}.
 */
@Service
public class ReadyCheckService {
  private static class CachedMessage {
    private Message message;
    private Map<String, List<String>> reactions;
    private boolean shouldUpdate = false;

    public CachedMessage(Message message, Map<String, List<String>> reactions) {
      this.message = message;
      this.reactions = reactions;
    }
  }

  private boolean shouldUpdate = false;

  private final Cache<String, CachedMessage> cachedMessages = Caffeine.newBuilder()
      .expireAfterWrite(NumberConstants.READYCHECK_LIFETIME_MINUTES, TimeUnit.MINUTES)
      .build();

  @Autowired
  private ReadyCheckConfigGuildEntityService service;

  @Scheduled(fixedDelayString = "${silicium.readycheck-update-interval}")
  private void updateEmbeds() {
    if (this.shouldUpdate) {
      synchronized (this.cachedMessages) {
        this.shouldUpdate = false;
        this.cachedMessages.asMap().forEach((guildIdAndMessageId, cachedMessage) -> {
          String[] split = guildIdAndMessageId.split(":");
          String guildId = split[0];
          String messageId = split[1];
          if (cachedMessage.shouldUpdate) {
            cachedMessage.shouldUpdate = false;
            this.updateMessage(guildId, cachedMessage);
          }
        });
      }
    }
  }

  private String joinUserMentions(List<String> mentions, String guildId) {
    return mentions.size() == 0
      ? this.service.getEmptyValue(guildId)
      : String.join("\n", mentions);
  }

  private void updateMessage(String guildId, CachedMessage cachedMessage) {
    String description = cachedMessage.message.getEmbeds().get(0).getDescription().orElse(null);
    Builder builder = EmbedCreateSpec.builder().title(this.service.getTitle(guildId));
    if (null != description) {
      builder.description(description);
    }
    for (ReadyCheckOption option : this.service.getOptions(guildId)) {
      List<String> mentions = new ArrayList<>();
      if (cachedMessage.reactions.containsKey(option.emoji)) {
        mentions = cachedMessage.reactions.get(option.emoji);
      }
      builder.addField(option.name, this.joinUserMentions(mentions, guildId), false);
    }
    cachedMessage.message.edit(MessageEditSpec.create().withEmbeds(builder.build())).subscribe();
  }

  private EmbedCreateSpec createEmbed(String guildId, @Nullable String description) {
    Builder builder = EmbedCreateSpec.builder().title(this.service.getTitle(guildId));
    if (null != description) {
      builder.description(description);
    }
    String emptyValue = this.service.getEmptyValue(guildId);
    List<ReadyCheckOption> options = this.service.getOptions(guildId);
    for (ReadyCheckOption option : options) {
      builder.addField(option.name, emptyValue, false);
    }
    return builder.build();
  }

  private void initReactions(String guildId, Message message) {
    for (ReadyCheckOption option : this.service.getOptions(guildId)) {
      message.addReaction(ReactionEmoji.unicode(option.emoji)).subscribe();
    }
  }

  @Nullable
  private Embed getReadyCheckEmbed(String guildId, Message message, User botUser) {
    List<Embed> embeds = message.getEmbeds();
    if (botUser.equals(message.getAuthor().orElse(null)) && embeds.size() == 1) {
      Embed embed = embeds.get(0);
      if (this.service.getTitle(guildId).equals(embed.getTitle().orElse(null))) {
        return embed;
      }
    }
    return null;
  }

  @Nullable
  private ReadyCheckOption getReadyCheckOption(String guildId, ReactionEmoji emoji) {
    if (emoji.asUnicodeEmoji().isPresent()) {
      String rawEmoji = emoji.asUnicodeEmoji().get().getRaw();
      List<ReadyCheckOption> options = this.service.getOptions(guildId);
      for (ReadyCheckOption option : options) {
        if (option.emoji.equals(rawEmoji)) {
          return option;
        }
      }
    }
    return null;
  }

  /**
   * Create new embed and send it.
   *
   * @param description Embed description.
   */
  public void startReadyCheck(String guildId, MessageChannel channel,
      Message message, @Nullable String description) {
    synchronized (this.cachedMessages) {
      channel.createMessage(this.createEmbed(guildId, description))
          .withMessageReference(message.getId()).subscribe(msg -> {
            this.initReactions(guildId, msg);
          });
    }
  }

  /**
   * Add user vote.
   *
   * @param author The user who voted.
   */
  public void addVote(String guildId, ReactionEmoji emoji, Message message,
      User botUser, User author) {
    synchronized (this.cachedMessages) {
      if (!Objects.equals(botUser, author) && message.getAuthor().isPresent()
          && message.getAuthor().get().equals(botUser)) {
        Embed embed = this.getReadyCheckEmbed(guildId, message, botUser);
        if (null != embed) {
          String messageId = message.getId().asString();
          String guildIdAndMessageId = guildId + ":" + messageId;
          CachedMessage cachedMessage = this.cachedMessages.getIfPresent(guildIdAndMessageId);
          if (null == cachedMessage) {
            cachedMessage = this.embedToCachedMessage(guildId, message, embed);
            this.cachedMessages.put(guildIdAndMessageId, cachedMessage);
          }
          ReadyCheckOption option = this.getReadyCheckOption(guildId, emoji);
          if (null != option) {
            if (!cachedMessage.reactions.containsKey(option.emoji)) {
              cachedMessage.reactions.put(option.emoji, new ArrayList<>());
            }
            List<String> mentions = cachedMessage.reactions.get(option.emoji);
            String authorMention = author.getMention();
            if (mentions.stream().filter(user -> user.equals(authorMention)).toList().size() == 0) {
              mentions.add(authorMention);
              cachedMessage.shouldUpdate = true;
              this.shouldUpdate = true;
            }
          }
        }
      }
    }
  }

  /**
   * Remove user vote.
   *
   * @param author The user who took his vote.
   */
  public void removeVote(String guildId, ReactionEmoji emoji, Message message,
      User botUser, User author) {
    synchronized (this.cachedMessages) {
      if (!Objects.equals(botUser, author) && message.getAuthor().isPresent()
          && message.getAuthor().get().equals(botUser)) {
        Embed embed = this.getReadyCheckEmbed(guildId, message, botUser);
        if (null != embed) {
          String messageId = message.getId().asString();
          String guildIdAndMessageId = guildId + ":" + messageId;
          CachedMessage cachedMessage = this.cachedMessages.getIfPresent(guildIdAndMessageId);
          if (null == cachedMessage) {
            cachedMessage = this.embedToCachedMessage(guildId, message, embed);
            this.cachedMessages.put(guildIdAndMessageId, cachedMessage);
          }
          if (emoji.asUnicodeEmoji().isPresent()) {
            String rawEmoji = emoji.asUnicodeEmoji().get().getRaw();
            if (cachedMessage.reactions.containsKey(rawEmoji)) {
              List<String> mentions = cachedMessage.reactions.get(rawEmoji);
              String authorMention = author.getMention();
              if (mentions.removeIf(user -> user.equals(authorMention))) {
                cachedMessage.shouldUpdate = true;
                this.shouldUpdate = true;
              }
            }
          }
        }
      }
    }
  }

  private CachedMessage embedToCachedMessage(String guildId, Message message, Embed embed) {
    List<ReadyCheckOption> options = this.service.getOptions(guildId);
    List<String> optionNames = options.stream().map(opt -> opt.name).toList();
    List<Field> fields = embed.getFields();
    Map<String, List<String>> reactions = new HashMap<>();
    for (int i = 0; i < fields.size(); i++) {
      Field field = fields.get(i);
      if (optionNames.contains(field.getName())) {
        ReadyCheckOption option = options.get(i);
        if (!reactions.containsKey(option.emoji)) {
          reactions.put(option.emoji, new ArrayList<>());
        }
        List<String> mentions = reactions.get(option.emoji);
        String value = field.getValue();
        String[] rawMentions = value.split("\n");
        if (rawMentions[0].startsWith("<") && rawMentions[0].endsWith(">")) {
          mentions.addAll(List.of(rawMentions));
        }
      }
    }
    return new CachedMessage(message, reactions);
  }

  /**
   * Remove all votes and restart ready check.
   */
  public void restartReadyCheck(String guildId, Message message, User botUser) {
    synchronized (this.cachedMessages) {
      Embed embed = this.getReadyCheckEmbed(guildId, message, botUser);
      if (embed != null) {
        String description = embed.getDescription().orElse(null);
        message.edit(MessageEditSpec.create().withEmbeds(this.createEmbed(guildId, description)))
            .subscribe(msg -> {
              this.initReactions(guildId, msg);
            });
      }
    }
  }
}
