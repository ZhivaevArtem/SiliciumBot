package com.zhivaevartem.siliciumbot;

import com.zhivaevartem.siliciumbot.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles("test")
@SpringBootTest
public class StringUtilsTest {
  @Test
  public void parseArgumentsTest() {
    String command = "asdf \"asdf  \\\"  asd\" f\\\"asdf \"asdf\" s\\\\df";
    List<String> expected = List.of(new String[]{"asdf", "asdf  \"  asd", "f\"asdf", "asdf", "s\\df"});
    List<String> result = new ArrayList<>();
    StringUtils.parseArguments(command, result);
    assertEquals(expected, result);
  }

  @Test
  public void trimArgumentsTest() {
    String command = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    String expected = "asdf asd fasd f";
    String result = StringUtils.parseArguments(command, 3);
    assertEquals(expected, result);
  }

  @Test
  public void trimAndParseTest() {
    String command = "asdf \"asdf  \\\"  asd\" f\\\"asdf \"asdf\" s\\\\df";
    String expectedString = "f\\\"asdf \"asdf\" s\\\\df";
    List<String> expectedArgs = List.of(new String[] {"asdf", "asdf  \"  asd"});
    List<String> resultArgs = new ArrayList<>();
    String resultString = StringUtils.parseArguments(command, resultArgs, 2);
    assertEquals(expectedArgs, resultArgs);
    assertEquals(expectedString, resultString);
  }

  @Test
  public void trimMoreThanExist() {
    String command = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    String expectedString = "";
    List<String> expectedArgs = List.of(new String[]{"asd", "fasd", "fasdf asdf", "asdf", "asd", "fasd", "f"});
    List<String> resultArgs = new ArrayList<>();
    String resultString = StringUtils.parseArguments(command, resultArgs, 999);
    assertEquals(expectedString, resultString);
    assertEquals(expectedArgs, resultArgs);
  }

  @Test
  public void trimZeroArguments() {
    String command = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    String expectedString = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    List<String> expectedArgs = new ArrayList<>();
    List<String> resultArgs = new ArrayList<>();
    String resultString = StringUtils.parseArguments(command, resultArgs, 0);
    assertEquals(expectedString, resultString);
    assertEquals(expectedArgs, resultArgs);
  }

  @Test
  public void trimNegativeArguments() {
    String command = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    String expectedString = "";
    List<String> expectedArgs = List.of(new String[]{"asd", "fasd", "fasdf asdf", "asdf", "asd", "fasd", "f"});
    List<String> resultArgs = new ArrayList<>();
    String resultString = StringUtils.parseArguments(command, resultArgs, -999);
    assertEquals(expectedString, resultString);
    assertEquals(expectedArgs, resultArgs);
  }
}
