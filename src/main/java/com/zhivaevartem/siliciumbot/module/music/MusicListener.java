package com.zhivaevartem.siliciumbot.module.music;

import com.zhivaevartem.siliciumbot.core.listener.AbstractEventListener;
import com.zhivaevartem.siliciumbot.core.listener.CommandHandler;
import com.zhivaevartem.siliciumbot.core.service.MessageService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.VoiceChannel;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MusicListener extends AbstractEventListener {
  @Autowired
  private MusicService musicService;

  @Autowired
  private GatewayDiscordClient gateway;

  @Autowired
  private MessageService messageService;

  @CommandHandler(aliases = {"play", "p"}, lastFreeArgument = true)
  public void play(MessageCreateEvent event, String query) {
    this.musicService.playTrack(event, query)
      .flatMap(musicTrackResponses -> {
        String message = String.join("\n",
          musicTrackResponses.stream().map(MusicTrackResponse::getMessage).collect(Collectors.toList()));
        return this.messageService.replyMessage(event.getMessage(), message);
      }).subscribe();
  }

  @CommandHandler(aliases = {"j", "join"})
  public void join(MessageCreateEvent event, String query) {
    this.musicService.joinVoice(event).block();
  }

  @CommandHandler(aliases = {"s", "skip"})
  public void skip(MessageCreateEvent event) {
    this.musicService.skip(event).flatMap(musicTrackResponse -> {
      return this.messageService.replyMessage(event.getMessage(), musicTrackResponse.getMessage());
    }).subscribe();
  }

  @CommandHandler(aliases = {"np", "nowplaying", "current"})
  public void getCurrent(MessageCreateEvent event) {
    this.musicService.getCurrent(event)
      .flatMap(track -> {
        String message = "Now playing: ";
        if (!track.getName().isEmpty()) {
          message += track.getName();
        } else {
          message = "Nothing is playing now";
        }
        return this.messageService.replyMessage(event.getMessage(), message);
      }).subscribe();
  }

  @CommandHandler(aliases = {"leave", "dc", "disconnect"})
  public void disconnect(MessageCreateEvent event) {
    this.musicService.disconnect(event).block();
  }

  @CommandHandler(aliases = {"q", "queue"})
  public void getQueue(MessageCreateEvent event) {
    this.musicService.getQueue(event)
      .flatMap(tracks -> {
        String message = "Queue is empty";
        if (!tracks.isEmpty()) {
          message = String.join("\n", tracks.stream().map(MusicTrack::getName).collect(Collectors.toList()));
        }
        return this.messageService.replyMessage(event.getMessage(), message);
      }).subscribe();
  }

  @CommandHandler(aliases = {"c", "clean", "clear"})
  public void clearQueue(MessageCreateEvent event) {
    this.musicService.clear(event).block();
    this.messageService.replyMessage(event, "Queue is empty").subscribe();
  }

  @Override
  public void onVoiceStateUpdateEvent(final VoiceStateUpdateEvent event) {
    if (event.isLeaveEvent()) {
      this.gateway.getSelfMember(event.getOld().get().getGuildId())
        .flatMap(member -> member.getVoiceState())
        .flatMap(state -> {
          if (state == null) {
            String guildId = event.getCurrent().getGuildId().asString();
            this.musicService.onDisconnect(guildId);
            return Mono.empty();
          }
          return state.getChannel();
        })
        .flatMap(channel -> channel.getVoiceStates().collectList())
        .flatMap(states -> states.size() == 1 ? Mono.just(states.get(0)) : Mono.empty())
        .flatMap(state -> state.getMember())
        .subscribe(member -> {
          if (member.getId().equals(this.gateway.getSelfId())) {
            this.musicService.disconnect(member.getGuildId().asString()).subscribe();
          }
        });
    }
  }
}
