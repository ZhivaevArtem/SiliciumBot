package com.zhivaevartem.siliciumbot.discord.listener;

import com.zhivaevartem.siliciumbot.constant.StringConstants;
import com.zhivaevartem.siliciumbot.discord.listener.base.BaseEventListener;
import com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler;
import com.zhivaevartem.siliciumbot.persistence.entity.ReadyCheckGuildConfig.ReadyCheckOption;
import com.zhivaevartem.siliciumbot.persistence.service.ReadyCheckGuildConfigService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveAllEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import discord4j.core.spec.MessageEditSpec;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Ready check listener.
 */
@Component
public class ReadyCheckListener extends BaseEventListener {
  private final Logger logger = LoggerFactory.getLogger(ReadyCheckListener.class);

  private Disposable updateMessageSubscription;

  @Autowired
  private ReadyCheckGuildConfigService readyCheckService;

  private EmbedCreateSpec buildEmbed(String guildId) {
    List<ReadyCheckOption> rcOptions = this.readyCheckService.getOptions(guildId);
    Builder builder = EmbedCreateSpec.builder();
    builder.title(StringConstants.READY_CHECK_EMBED_TITLE);
    for (ReadyCheckOption rcOption : rcOptions) {
      builder.addField(rcOption.name, StringConstants.EMPTY_READY_CHECK_VALUE, false);
    }
    return builder.build();
  }

  private void initReactions(Message message, String guildId) {
    List<ReadyCheckOption> rcOpts = this.readyCheckService.getOptions(guildId);
    for (ReadyCheckOption rcOpt : rcOpts) {
      message.addReaction(ReactionEmoji.unicode(rcOpt.emoji)).subscribe();
    }
  }

  private void initEmbed(MessageChannel channel, String guildId) {
    channel.createMessage(this.buildEmbed(guildId)).subscribe(message -> {
      this.initReactions(message, guildId);
    });
  }

  private void initEmbed(Message message, String guildId) {
    message.edit(MessageEditSpec.create().withEmbeds(this.buildEmbed(guildId))).subscribe(mes -> {
      this.initReactions(mes, guildId);
    });
  }

  private void updateEmbed(Message message, String guildId) {
    String uuid = UUID.randomUUID().toString();
    this.logger.info(uuid + " Message updating...");
    if (null != this.updateMessageSubscription && !this.updateMessageSubscription.isDisposed()) {
      this.updateMessageSubscription.dispose();
      this.logger.info(uuid + " Disposed!");
    }
    List<ReadyCheckOption> rcOpts = this.readyCheckService.getOptions(guildId);
    @SuppressWarnings("unchecked")
    Mono<List<User>>[] reactorsMonos = (Mono<List<User>>[]) new Mono[rcOpts.size()];
    for (int i = 0; i < rcOpts.size(); i++) {
      ReadyCheckOption rcOpt = rcOpts.get(i);
      Flux<User> reactorsFlux = message.getReactors(ReactionEmoji.unicode(rcOpt.emoji))
          .filter(reactor -> !reactor.isBot());
      Mono<List<User>> reactorsMono = reactorsFlux.collectList();
      reactorsMonos[i] = reactorsMono;
    }
    this.logger.info(uuid + " Collecting reactors...");
    this.updateMessageSubscription
      = Flux.concat(reactorsMonos)
        .collectList()
        .flatMap(reactorsLists -> {
          this.logger.info(uuid + " Reactors collected!");
          Builder builder = EmbedCreateSpec.builder()
              .title(StringConstants.READY_CHECK_EMBED_TITLE);
          for (int i = 0; i < reactorsLists.size(); i++) {
            ReadyCheckOption rcOpt = rcOpts.get(i);
            List<User> reactors = reactorsLists.get(i);
            String value = StringConstants.EMPTY_READY_CHECK_VALUE;
            if (reactors.size() > 0) {
              value = String.join("\n", reactors.stream().map(User::getMention).toList());
            }
            builder.addField(rcOpt.name, value, false);
          }
          this.logger.info(uuid + " Embed built!");
          return message.edit(MessageEditSpec.create().withEmbeds(builder.build()));
        }).subscribe(msg -> {
          this.logger.info(uuid + " Message updated!");
        });
  }

  /**
   * Ready check handler.
   */
  @CommandHandler(aliases = {"readycheck", "rc"}, lastFreeArgument = true)
  public void createReadyCheckVote(MessageCreateEvent event, String description) {
    event.getGuild().zipWith(event.getMessage().getChannel()).subscribe(guildAndChannel -> {
      String guildId = guildAndChannel.getT1().getId().asString();
      MessageChannel channel = guildAndChannel.getT2();
      this.initEmbed(channel, guildId);
    });
  }

  @Override
  public void onReactionAddEvent(ReactionAddEvent event) {
    event.getClient().getSelf().zipWith(event.getUser()).subscribe(selfWithUser -> {
      User self = selfWithUser.getT1();
      User user = selfWithUser.getT2();
      if (!self.equals(user)) {
        event.getGuild().zipWith(event.getMessage()).subscribe(guildAndMessage -> {
          String guildId = guildAndMessage.getT1().getId().asString();
          Message message = guildAndMessage.getT2();
          this.updateEmbed(message, guildId);
        });
      }
    });
  }

  @Override
  public void onReactionRemoveEvent(ReactionRemoveEvent event) {
    event.getClient().getSelf().zipWith(event.getUser()).subscribe(selfWithUser -> {
      User self = selfWithUser.getT1();
      User user = selfWithUser.getT2();
      if (self.equals(user)) {
        event.getMessage().subscribe(message -> {
          message.addReaction(event.getEmoji()).subscribe();
        });
      } else {
        event.getGuild().zipWith(event.getMessage()).subscribe(guildAndMessage -> {
          String guildId = guildAndMessage.getT1().getId().asString();
          Message message = guildAndMessage.getT2();
          this.updateEmbed(message, guildId);
        });
      }
    });
  }

  @Override
  public void onReactionRemoveAllEvent(ReactionRemoveAllEvent event) {
    event.getGuild().zipWith(event.getMessage()).subscribe(guildAndMessage -> {
      String guildId = guildAndMessage.getT1().getId().asString();
      Message message = guildAndMessage.getT2();
      this.initEmbed(message, guildId);
    });
  }
}
