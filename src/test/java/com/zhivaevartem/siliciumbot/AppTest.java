package com.zhivaevartem.siliciumbot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class AppTest {
  @Test
  public void contextLoadsTest() {
    assertTrue(true);
  }
}
