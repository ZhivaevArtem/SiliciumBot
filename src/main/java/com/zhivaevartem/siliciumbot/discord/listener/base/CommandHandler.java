package com.zhivaevartem.siliciumbot.discord.listener.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * Annotation for command handlers. All command handlers must be annotated.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHandler {
  /**
   * Possible commands to invoke the handler.
   */
  String[] aliases() default {};

  /**
   * If true remaining string will be passed in last argument
   * which must be {@link java.lang.String}.
   * Default: {@code false}.
   */
  boolean lastFreeArgument() default false;
}
