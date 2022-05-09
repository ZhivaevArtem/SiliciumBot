package com.zhivaevartem.siliciumbot.discord.service;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MessageService {
  @Autowired(required = false)
  private GatewayDiscordClient gateway;

  public Mono<Message> replyMessage(Message message, MessageCreateSpec spec) {
    return message.getChannel().flatMap(
      channel -> channel.createMessage(spec.withMessageReference(message.getId())));
  }

  public Mono<Message> replyMessage(Message message, String content) {
    return this.replyMessage(message, MessageCreateSpec.create().withContent(content));
  }

  public Mono<Message> replyMessage(Message message, Iterable<EmbedCreateSpec> embeds) {
    return this.replyMessage(message, MessageCreateSpec.create().withEmbeds(embeds));
  }

  public Mono<Message> replyMessage(Message message, EmbedCreateSpec embed) {
    return this.replyMessage(message, List.of(embed));
  }

  public Mono<Message> sendMessage(String guildId, String messageChannelId, MessageCreateSpec spec) {
    return this.gateway.getGuildById(Snowflake.of(guildId))
      .flatMap(guild -> guild.getChannelById(Snowflake.of(messageChannelId)))
      .flatMap(channel -> (channel instanceof MessageChannel messageChannel)
        ? messageChannel.createMessage(spec)
        : Mono.empty());
  }

  public Mono<Message> sendMessage(String guildId, String messageChannelId, String content) {
    return this.sendMessage(guildId, messageChannelId, MessageCreateSpec.create().withContent(content));
  }

  public Mono<Message> sendMessage(String guildId, String messageChannelId, Iterable<EmbedCreateSpec> embeds) {
    return this.sendMessage(guildId, messageChannelId, MessageCreateSpec.create().withEmbeds(embeds));
  }

  public Mono<Message> sendMessage(String guildId, String messageChannelId, EmbedCreateSpec embed) {
    return this.sendMessage(guildId, messageChannelId, List.of(embed));
  }

  public String getMessageId(Message message) {
    return message.getId().asString();
  }

  public String getMessageChannelId(Message message) {
    return message.getChannel().block().getId().asString();
  }

  public String getGuildId(Message message) {
    return message.getGuild().block().getId().asString();
  }

  public GuildChannel getChannel(String guildId, String channelId) {
    return this.gateway.getGuildById(Snowflake.of(guildId)).block().getChannelById(Snowflake.of(channelId)).block();
  }
}
