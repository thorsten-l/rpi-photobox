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
package l9g.photobox2.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
@Service
@Getter
public class FsUtilService
{

  public String getUsbStickDirectory() throws IOException
  {
    String directory = null;
    log.debug("usb device = {}, prox mount = {}", usbDevice, procMounts);

    File fsProcMounts = new File(procMounts);

    if (fsProcMounts.exists() && fsProcMounts.canRead())
    {
      BufferedReader reader = new BufferedReader(new FileReader(fsProcMounts));
      String line;
      while ((line = reader.readLine()) != null)
      {
        if (line.startsWith(usbDevice + " "))
        {
          directory = line.split("\\s+")[1];
          break;
        }
      }
    }

    log.debug("directory = '{}'", directory);
    return directory;
  }

  public void copyToUsbStick(String sourceFile)
    throws IOException, InterruptedException
  {
    String destination = getUsbStickDirectory();
    log.debug("copy {} to {}", sourceFile, destination);

    if (sourceFile != null && destination != null)
    {
      String[] cmds =
      {
        cpCommand, sourceFile, destination
      };
      ProcessBuilder builder = new ProcessBuilder(cmds);

      File directory = new File(gphotoWebApiDirectory);
      directory.mkdirs();
      builder.directory(directory);

      builder.redirectErrorStream(true);
      builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      builder.redirectInput(ProcessBuilder.Redirect.INHERIT);

      Process copyProcess = builder.start();
      copyProcess.waitFor(5, TimeUnit.SECONDS);
      log.debug("copy {} done.", sourceFile);
    }
    else
    {
      log.debug("No USB-Stick Found!");
    }
  }

  @Value("${fsutil.usb-device}")
  private String usbDevice;

  @Value("${fsutil.proc-mounts}")
  private String procMounts;

  @Value("${fsutil.cp}")
  private String cpCommand;

  @Value("${gphoto-webapi.directory}")
  private String gphotoWebApiDirectory;
}
