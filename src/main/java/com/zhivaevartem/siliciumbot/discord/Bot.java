package com.zhivaevartem.siliciumbot.discord;

import com.zhivaevartem.siliciumbot.discord.listener.base.AbstractCommandListener;
import com.zhivaevartem.siliciumbot.discord.listener.base.CommandHandler;
import java.lang.reflect.Method;
import java.util.List;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Bot bean. Used to start bot.
 */
@Component
public class Bot {
  @Value("${discord.bot.token}")
  private String token;

  @Autowired
  private List<? extends AbstractCommandListener> commandListeners;

  /**
   * Starts bot.
   *
   * @throws LoginException Thrown if invalid token passed.
   */
  public void start() throws LoginException {
    JDA jda = JDABuilder.createDefault(this.token).build();
    for (AbstractCommandListener commandListener : this.commandListeners) {
      for (Method method : commandListener.getClass().getMethods()) {
        if (method.isAnnotationPresent(CommandHandler.class)) {
          commandListener.registerCommandHandler(method);
        }
      }
      jda.addEventListener(commandListener);
    }
  }
}
