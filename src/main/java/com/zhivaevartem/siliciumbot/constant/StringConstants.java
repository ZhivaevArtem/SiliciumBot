package com.zhivaevartem.siliciumbot.constant;

import com.zhivaevartem.siliciumbot.module.readycheck.ReadyCheckConfigGuildEntity.ReadyCheckOption;

import java.util.Arrays;
import java.util.List;

/**
 * String constants.
 */
public class StringConstants {
  public static final String DEFAULT_BOT_COMMAND_PREFIX = "D";

  public static final List<ReadyCheckOption> DEFAULT_READY_CHECK_OPTIONS
      = Arrays.asList(
        new ReadyCheckOption("✅", "Ready"),
        new ReadyCheckOption("⌚", "Later"),
        new ReadyCheckOption("❌", "Not ready")
      );

  public static final String EMPTY_READY_CHECK_VALUE = "*No votes...*";

  public static final String READY_CHECK_EMBED_TITLE = "==== Ready check ====";

  public static final String UNKNOWN_GUILD_ID = "<UNKNOWN GUILD ID>";

  private StringConstants() {}
}
