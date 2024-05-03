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
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan
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
    log.info("Starting PhotoBox2");
    log.info( "{}", BuildProperties.toFormattedString() );
    if (!System.getProperty("os.name").equalsIgnoreCase("Linux"))
    {
      log.error("PhotoBox2 is designed for Linux/RaspberryPi OS only.");
      System.exit(0);
    }
    System.setProperty("java.awt.headless", "false");
    AppState.setState(AppState.STARTUP);
    SpringApplication.run(PhotoBox2Application.class, args);
  }
}
