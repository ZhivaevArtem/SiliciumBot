package com.zhivaevartem.siliciumbot.discord.listener;

import com.zhivaevartem.siliciumbot.discord.listener.base.AbstractCommandListener;
import com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

/**
 * Contain all admin command handlers.
 */
@Component
public class AdminCommandListener extends AbstractCommandListener {
  /**
   * Change bot status (online, dnd, invisible, offline, idle).
   */
  @CommandHandler(aliases = {"bot status"})
  public void changeBotStatus(MessageReceivedEvent event, String newStatus) {
    OnlineStatus status = switch (newStatus.toLowerCase()) {
      case "online" -> OnlineStatus.ONLINE;
      case "dnd" -> OnlineStatus.DO_NOT_DISTURB;
      case "offline" -> OnlineStatus.OFFLINE;
      case "invisible" -> OnlineStatus.INVISIBLE;
      case "idle" -> OnlineStatus.IDLE;
      default -> null;
    };
    if (null != status) {
      event.getJDA().getPresence().setStatus(status);
    }
  }
}
