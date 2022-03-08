package com.zhivaevartem.siliciumbot.discord.listener;

import com.zhivaevartem.siliciumbot.discord.listener.base.AbstractCommandListener;
import com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler;
import java.util.List;
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
  @CommandHandler(aliases = "bot status")
  public void changeBotStatus(MessageReceivedEvent event, List<String> args) {
    if (args.size() == 1) {
      OnlineStatus status = switch (args.get(0).toLowerCase()) {
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
}
