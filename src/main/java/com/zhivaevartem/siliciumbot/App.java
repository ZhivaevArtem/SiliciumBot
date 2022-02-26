package com.zhivaevartem.siliciumbot;

import com.zhivaevartem.siliciumbot.utils.ConfigUtils;

/**
 * Startup class.
 */
public class App {
  /**
   * Startup method.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    String token = ConfigUtils.getProp("token");
    String connectionString = ConfigUtils.getProp("mongodb.connection_string");
  }
}
