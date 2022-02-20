package com.zhivaevartem.siliciumbot;

import com.zhivaevartem.siliciumbot.listeners.SampleEventListener;
import com.zhivaevartem.siliciumbot.utils.ConfigUtils;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class App {
  public static void main(String[] args) throws LoginException {
    String token = ConfigUtils.getProp("token");
    JDABuilder builder = JDABuilder.createDefault(token);
    JDA jda = builder.build();
    jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
    jda.getPresence().setActivity(Activity.listening("Музыка мясокомбината"));

    jda.addEventListener(new SampleEventListener());
  }
}
