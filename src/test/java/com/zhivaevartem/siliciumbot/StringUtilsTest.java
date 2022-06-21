package com.zhivaevartem.siliciumbot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhivaevartem.siliciumbot.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class StringUtilsTest {
  @Test
  public void parseArgumentsTest() {
    String command = "asdf \"asdf  \\\"  asd\" f\\\"asdf \"asdf\" s\\\\df";
    List<String> expected = List.of(new String[]{"asdf", "asdf  \"  asd", "f\"asdf", "asdf", "s\\df"});
    List<String> result = new ArrayList<>();
    StringUtils.splitArguments(command, result);
    assertEquals(expected, result);
  }

  @Test
  public void trimArgumentsTest() {
    String command = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    String expected = "asdf asd fasd f";
    String result = StringUtils.splitArguments(command, 3);
    assertEquals(expected, result);
  }

  @Test
  public void trimAndParseTest() {
    String command = "asdf \"asdf  \\\"  asd\" f\\\"asdf \"asdf\" s\\\\df";
    String expectedString = "f\\\"asdf \"asdf\" s\\\\df";
    List<String> expectedArgs = List.of(new String[] {"asdf", "asdf  \"  asd"});
    List<String> resultArgs = new ArrayList<>();
    String resultString = StringUtils.splitArguments(command, resultArgs, 2);
    assertEquals(expectedArgs, resultArgs);
    assertEquals(expectedString, resultString);
  }

  @Test
  public void trimMoreThanExist() {
    String command = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    String expectedString = "";
    List<String> expectedArgs = List.of(new String[]{"asd", "fasd", "fasdf asdf", "asdf", "asd", "fasd", "f"});
    List<String> resultArgs = new ArrayList<>();
    String resultString = StringUtils.splitArguments(command, resultArgs, 999);
    assertEquals(expectedString, resultString);
    assertEquals(expectedArgs, resultArgs);
  }

  @Test
  public void trimZeroArguments() {
    String command = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    String expectedString = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    List<String> expectedArgs = new ArrayList<>();
    List<String> resultArgs = new ArrayList<>();
    String resultString = StringUtils.splitArguments(command, resultArgs, 0);
    assertEquals(expectedString, resultString);
    assertEquals(expectedArgs, resultArgs);
  }

  @Test
  public void trimNegativeArguments() {
    String command = "asd fasd \"fasdf asdf\" asdf asd fasd f";
    String expectedString = "";
    List<String> expectedArgs = List.of(new String[]{"asd", "fasd", "fasdf asdf", "asdf", "asd", "fasd", "f"});
    List<String> resultArgs = new ArrayList<>();
    String resultString = StringUtils.splitArguments(command, resultArgs, -999);
    assertEquals(expectedString, resultString);
    assertEquals(expectedArgs, resultArgs);
  }

  @Test
  public void prettifyStringTest() {
    String command = "    \t\t     SampLE   \t\t \t  TeXT  \t   \t\t\t ";
    String expected = "sample text";
    String result = StringUtils.prettifyString(command);
    assertEquals(expected, result);
  }

  @Test
  public void parseEmptyQuotes() {
    String command = "     \"\"    asdf     \"\"    ";
    List<String> expected = List.of(new String[]{"", "asdf", ""});
    List<String> result = new ArrayList<>();
    StringUtils.splitArguments(command, result);
    assertEquals(expected, result);
  }

  @Test
  public void capitalizeWithSymbols() {
    String str = "gSJHDFGhjgD@$%$%JHSFGSHJD@#%KGFh5456&^5467$@462sd";
    String expected = "Gsjhdfghjgd@$%$%jhsfgshjd@#%kgfh5456&^5467$@462sd";
    String result = StringUtils.capitalize(str);
    assertEquals(expected, result);
  }

  @Test
  public void parseQueryParams() {
    String url = "https://www.youtube.com/watch?v=OQGdrezi0Y4&list=RDOQGdrezi0Y4&start_radio=1";
    Map<String, String> expected = new HashMap<>() {{
      put("v", "OQGdrezi0Y4");
      put("list", "RDOQGdrezi0Y4");
      put("start_radio", "1");
    }};
    Map<String, String> result = StringUtils.parseQueryParams(url);
    assertEquals(expected, result);
  }
}
