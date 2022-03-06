package com.zhivaevartem.siliciumbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Startup class.
 */
@SpringBootApplication
public class App {
  /**
   * Startup method.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
