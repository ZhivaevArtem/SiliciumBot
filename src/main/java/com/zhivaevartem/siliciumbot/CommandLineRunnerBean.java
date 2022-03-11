package com.zhivaevartem.siliciumbot;

import com.zhivaevartem.siliciumbot.discord.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Bean runs when it is contained within a {@link org.springframework.boot.SpringApplication}.
 */
@Profile("!test")
@Component
public class CommandLineRunnerBean implements CommandLineRunner {
  @Autowired
  private Bot bot;

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(String... args) throws Exception {
    this.bot.start();
  }
}
