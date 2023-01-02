package com.zhivaevartem.siliciumbot.module.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.zhivaevartem.siliciumbot.core.service.MessageService;
import com.zhivaevartem.siliciumbot.module.music.youtube.YoutubeService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.voice.VoiceConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MusicService {
  @Autowired
  private MessageService messageService;

  @Autowired(required = false)
  private GatewayDiscordClient gateway;

  @Autowired
  private YoutubeService youtubeService;

  private final Map<String, GuildMusicState> states = new HashMap<>();

  @Scheduled(fixedDelayString = "3000")
  public void checkQueue() {
    synchronized (this.states) {
      for (String guildId : states.keySet()) {
        GuildMusicState state = this.states.get(guildId);
        AudioTrack playingTrack = state.getPlayer().getPlayingTrack();
        if (playingTrack == null) {
          MusicTrack track = state.pollNextTrack();
          if (track != null) {
            track.play(state.getPlayerManager(), state.getTrackScheduler());
          }
        }
      }
    }
  }

  private Mono<VoiceConnection> joinVoice(String guildId, MessageCreateEvent event) {
    if (!this.states.containsKey(guildId)) {
      GuildMusicState state = new GuildMusicState(guildId);
      this.states.put(guildId, state);
      return Mono.justOrEmpty(event.getMember())
        .flatMap(Member::getVoiceState)
        .flatMap(VoiceState::getChannel)
        .flatMap(channel -> channel.join(VoiceChannelJoinSpec.builder().provider(state.getProvider()).build()));
    }
    return Mono.empty();
  }

  public Mono<VoiceConnection> joinVoice(MessageCreateEvent event) {
    String guildId = this.messageService.getGuildId(event.getMessage());
    return this.joinVoice(guildId, event);
  }

  public Mono<List<MusicTrackResponse>> playTrack(MessageCreateEvent event, String query) {
    List<MusicTrack> tracks = this.parseTrack(query);
    if (tracks == null || tracks.isEmpty()) {
      return Mono.just(Collections.singletonList(new MusicTrackResponse(MusicTrackResponse.Status.ERROR, query)));
    }
    List<MusicTrackResponse> responses = tracks.stream().map(track -> {
      String guildId = this.messageService.getGuildId(event);
      if (!this.states.containsKey(guildId)) {
        this.joinVoice(guildId, event).block();
        this.states.get(guildId).addTrack(track);
        return new MusicTrackResponse(MusicTrackResponse.Status.PLAYING, track);
      } else {
        this.states.get(guildId).addTrack(track);
        if (this.isPlaying(guildId)) {
          return new MusicTrackResponse(MusicTrackResponse.Status.ADDED, track);
        } else {
          return new MusicTrackResponse(MusicTrackResponse.Status.PLAYING, track);
        }
      }
    }).collect(Collectors.toList());
    return Mono.just(responses);
  }

  public Mono<MusicTrackResponse> skip(MessageCreateEvent event) {
    String guildId = this.messageService.getGuildId(event);
    if (this.states.containsKey(guildId)) {
      GuildMusicState state = this.states.get(guildId);
      state.getPlayer().stopTrack();
      MusicTrack track = state.getCurrentTrack();
      if (track != null) {
        return Mono.just(new MusicTrackResponse(MusicTrackResponse.Status.SKIPPED, track));
      }
    }
    return Mono.empty();
  }

  public Mono<MusicTrack> getCurrent(MessageCreateEvent event) {
    String guildId = this.messageService.getGuildId(event);
    if (this.states.containsKey(guildId)) {
      GuildMusicState state = this.states.get(guildId);
      if (this.isPlaying(guildId)) {
        return Mono.just(state.getCurrentTrack());
      }
    }
    return Mono.just(new MusicTrack(""));
  }

  public Mono<Void> clear(MessageCreateEvent event) {
    String guildId = this.messageService.getGuildId(event);
    if (this.states.containsKey(guildId)) {
      GuildMusicState state = this.states.get(guildId);
      state.clearQueue();
    }
    return Mono.just("null").then();
  }

  public boolean isPlaying(String guildId) {
    if (this.states.containsKey(guildId)) {
      GuildMusicState state = this.states.get(guildId);
      return state.getPlayer().getPlayingTrack() != null;
    }
    return false;
  }

  public void onDisconnect(String guildId) {
    this.states.remove(guildId);
  }

  public Mono<Void> disconnect(String guildId) {
    this.states.remove(guildId);
    return this.gateway.getGuildById(Snowflake.of(guildId))
      .flatMap(Guild::getVoiceConnection)
      .flatMap(VoiceConnection::disconnect);
  }

  public Mono<Void> disconnect(MessageCreateEvent event) {
    String guildId = this.messageService.getGuildId(event);
    return this.disconnect(guildId);
  }

  public Mono<List<MusicTrack>> getQueue(MessageCreateEvent event) {
    String guildId = this.messageService.getGuildId(event);
    if (this.states.containsKey(guildId)) {
      GuildMusicState state = this.states.get(guildId);
      return Mono.just(state.getQueue());
    }
    return Mono.just(new ArrayList<>(0));
  }

  @Nullable
  private List<MusicTrack> parseTrack(String query) {
    if (query.startsWith("https://www.youtube.com/watch")) {
      if (query.contains("list=")) {
        List<String> videoIds = this.youtubeService.getPlaylistVideos(query);
        return videoIds.stream().map(id -> new MusicTrack("https://www.youtube.com/watch?v=" + id)).collect(Collectors.toList());
      } else {
        return Collections.singletonList(new MusicTrack(query));
      }
    } else {
      List<String> ids = this.youtubeService.searchVideos(query);
      if (ids.size() > 0)  {
        String id = ids.get(0);
        String url = "https://www.youtube.com/watch?v=" + id;
        return Collections.singletonList(new MusicTrack(url));
      }
    }
    return null;
  }
}
