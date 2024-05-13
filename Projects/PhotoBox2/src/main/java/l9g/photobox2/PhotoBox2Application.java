/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.photobox2;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class PhotoBox2Application
{
  public final static double START_TIMESTAMP = System.currentTimeMillis();

  private final GraphicsDevice graphicsDevice
    = GraphicsEnvironment
      .getLocalGraphicsEnvironment()
      .getDefaultScreenDevice();

  @Bean
  GraphicsDevice graphicsDevice()
  {
    return graphicsDevice;
  }

  @Bean
  DisplayMode displayMode()
  {
    return graphicsDevice.getDisplayMode();
  }

  public static void main(String[] args) throws IOException
  {
    log.info("Starting PhotoBox2" + AnsiColor.RESET);
    System.out.println(AnsiColor.RESET + "\n");
    System.out.println(AnsiColor.BG_GREEN.code + AnsiColor.FG_BOLD_BRIGHT_WHITE);
    System.out.println("\n\n    Starting PhotoBox2\n");
    System.out.println(AnsiColor.RESET + "\n");

    if (!System.getProperty("os.name").equalsIgnoreCase("Linux"))
    {
      System.out.println(
        AnsiColor.BG_RED.code + AnsiColor.FG_BOLD_WHITE
        + "\n\n\n    PhotoBox2 is designed for Linux/RaspberryPi OS only.\n\n"
        + AnsiColor.RESET.toString() + "\n");

      if (!"true".equals(System.getProperty("spring.aot.processing")))
      {
        System.exit(0);
      }
    }

    System.setProperty("java.awt.headless", "false");
    AppState.setState(AppState.STARTUP);
    SpringApplication.run(PhotoBox2Application.class, args);
  }
}
