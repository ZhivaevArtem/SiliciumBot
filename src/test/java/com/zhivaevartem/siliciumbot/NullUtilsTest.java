package com.zhivaevartem.siliciumbot;

import com.zhivaevartem.siliciumbot.util.NullUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class NullUtilsTest {
  @Test
  public void emptyStringIsEmptyTest() {
    assertTrue(NullUtils.isEmpty(""));
  }

  @Test
  public void nullStringIsEmptyTest() {
    assertTrue(NullUtils.isEmpty(null));
  }

  @Test
  public void nonEmptyStringIsEmptyTest() {
    assertFalse(NullUtils.isEmpty("asdf asdf sdf sdf sdf sd"));
  }

  @Test
  public void whitespaceStringIsEmptyTest() {
    assertTrue(NullUtils.isEmpty(" "));
  }

  @Test
  public void tabStringIsEmptyTest() {
    assertTrue(NullUtils.isEmpty("\t"));
  }

  @Test
  public void newLineStringIsEmptyTest() {
    assertTrue(NullUtils.isEmpty("\n"));
  }

  @Test
  public void mixWhitespaceCharsIsEmptyTest() {
    assertTrue(NullUtils.isEmpty(" \t\n"));
  }
}
