package com.zhivaevartem.siliciumbot.discord.service;

import com.zhivaevartem.siliciumbot.persistence.dto.ReadyCheckGuildDto.ReadyCheckOption;
import com.zhivaevartem.siliciumbot.persistence.service.ReadyCheckGuildConfigService;
import discord4j.core.object.Embed;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for {@link com.zhivaevartem.siliciumbot.discord.listener.ReadyCheckListener}.
 */
@Service
public class ReadyCheckService {
  private static class CachedMessage {
    private Message message;
    private Map<String, List<User>> reactions;
    private boolean shouldUpdate = false;

    public CachedMessage(Message message, Map<String, List<User>> reactions) {
      this.message = message;
      this.reactions = reactions;
    }
  }

  private boolean shouldUpdate = false;

  private final Map<String, Map<String, CachedMessage>> cachedMessages = new HashMap<>();

  @Autowired
  private ReadyCheckGuildConfigService service;

  @Scheduled(fixedDelay = 2500)
  private void updateEmbeds() {
    if (this.shouldUpdate) {
      synchronized (this.cachedMessages) {
        this.shouldUpdate = false;
        this.cachedMessages.forEach((guildId, messages) -> {
          messages.forEach((messageId, message) -> {
            if (message.shouldUpdate) {
              message.shouldUpdate = false;
              this.updateMessage(guildId, message);
            }
          });
        });
      }
    }
  }

  @Scheduled(cron = "0 0 3 * * *")
  private void cleanCache() {
    synchronized (this.cachedMessages) {
      this.cachedMessages.clear();
      this.shouldUpdate = false;
    }
  }

  private String joinUserMentions(List<User> users, String guildId) {
    return users.size() == 0
      ? this.service.getEmptyValue(guildId)
      : String.join("\n", users.stream().map(User::getMention).toList());
  }

  private void updateMessage(String guildId, CachedMessage cachedMessage) {
    String description = cachedMessage.message.getEmbeds().get(0).getDescription().orElse(null);
    Builder builder = EmbedCreateSpec.builder().title(this.service.getTitle(guildId));
    if (null != description) {
      builder.description(description);
    }
    for (ReadyCheckOption option : this.service.getOptions(guildId)) {
      List<User> reactors = new ArrayList<>();
      if (cachedMessage.reactions.containsKey(option.emoji)) {
        reactors = cachedMessage.reactions.get(option.emoji);
      }
      builder.addField(option.name, this.joinUserMentions(reactors, guildId), false);
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
          if (!this.cachedMessages.containsKey(guildId)) {
            this.cachedMessages.put(guildId, new HashMap<>());
          }
          Map<String, CachedMessage> cachedGuild = this.cachedMessages.get(guildId);
          if (!cachedGuild.containsKey(messageId)) {
            cachedGuild.put(messageId, new CachedMessage(message, new HashMap<>()));
          }
          CachedMessage cachedMessage = cachedGuild.get(messageId);
          ReadyCheckOption option = this.getReadyCheckOption(guildId, emoji);
          if (null != option) {
            if (!cachedMessage.reactions.containsKey(option.emoji)) {
              cachedMessage.reactions.put(option.emoji, new ArrayList<>());
            }
            List<User> reactors = cachedMessage.reactions.get(option.emoji);
            if (reactors.stream().filter(user -> user.equals(author)).toList().size() == 0) {
              reactors.add(author);
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
          if (this.cachedMessages.containsKey(guildId)) {
            Map<String, CachedMessage> cachedGuild = this.cachedMessages.get(guildId);
            if (cachedGuild.containsKey(messageId)) {
              CachedMessage cachedMessage = cachedGuild.get(messageId);
              if (emoji.asUnicodeEmoji().isPresent()) {
                String rawEmoji = emoji.asUnicodeEmoji().get().getRaw();
                if (cachedMessage.reactions.containsKey(rawEmoji)) {
                  List<User> reactors = cachedMessage.reactions.get(rawEmoji);
                  if (reactors.removeIf(user -> user.equals(author))) {
                    cachedMessage.shouldUpdate = true;
                    this.shouldUpdate = true;
                  }
                }
              }
            }
          }
        }
      }
    }
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
