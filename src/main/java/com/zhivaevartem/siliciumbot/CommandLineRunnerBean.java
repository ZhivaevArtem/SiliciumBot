package com.zhivaevartem.siliciumbot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Bean runs when it is contained within a {@link org.springframework.boot.SpringApplication}.
 */
@Profile("!test")
@Component
public class CommandLineRunnerBean implements CommandLineRunner {
  /**
   * {@inheritDoc}
   */
  @Override
  public void run(String... args) throws Exception {}
}
