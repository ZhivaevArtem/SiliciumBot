package com.zhivaevartem.siliciumbot.utils;

import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ConfigUtils {
  private static Configuration properties;
  private static final Configurations configurations = new Configurations();
  private static final String fileName = "application.properties";

  private static void loadProperties() {
    if (null == ConfigUtils.properties) {
      try {
        URL resource = ConfigUtils.class.getClassLoader().getResource(ConfigUtils.fileName);
        if (null != resource) {
          ConfigUtils.properties = ConfigUtils.configurations.properties(resource);
        }
      } catch (ConfigurationException e) {
        e.printStackTrace();
      }
    }
  }

  public static String getProp(String prop) {
    ConfigUtils.loadProperties();
    if (null != ConfigUtils.properties) {
      return ConfigUtils.properties.getString(prop);
    } else {
      return prop;
    }
  }
}
