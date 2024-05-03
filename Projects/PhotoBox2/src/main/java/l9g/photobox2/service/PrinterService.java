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

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Service
@Getter
public class PrinterService
{
  private final String runCommand(String[] cmds)
  {
    StringBuilder result = new StringBuilder();
    try
    {
      ProcessBuilder builder = new ProcessBuilder(cmds);
      Map<String, String> environment = builder.environment();
      environment.put("LANG", "C");
      environment.put("LANGUAGE", "C");
      environment.put("LC_ALL", "C");

      builder.redirectErrorStream(true);
      Process process = builder.start();
      try (BufferedReader reader = process.inputReader())
      {
        String line;
        boolean nextLine = false;
        while ((line = reader.readLine()) != null)
        {
          if (nextLine)
          {
            result.append("\n");
          }
          result.append(line.trim());
          nextLine = true;
        }
      }
      process.waitFor();
      log.debug("{} exits with code: {}", cmds[0], process.exitValue());
    }
    catch (Exception ex)
    {
      log.error("Printer command failed ", ex);
    }
    return result.toString();
  }

  @PostConstruct
  private void initialize()
  {
    log.debug("initialize");
    String findPrinter = runCommand(new String[]
    {
      printerCommandFind
    });
    log.debug("Printer=" + "\n----------------------------------------" + "\n{}"
      + "\n----------------------------------------",
      findPrinter);
    int index = findPrinter.indexOf("PRINTER_NAME=");
    if (index > 0)
    {
      enabled = true;
      deviceName = findPrinter.substring(index + 13).trim();
      log.info("printer device name = '{}'", deviceName);
    }
    else
    {
      log.warn("No printer found.");
    }
  }

  public boolean print(String localFolder, String fileName)
  {
    boolean success = false;
    log.debug("local folder = {}, picture file name = {}",
      localFolder, fileName);

    File imageFile = new File(new File(localFolder), fileName);

    if (imageFile.exists() && imageFile.canRead())
    {
      String commandLine = MessageFormat.format(printerCommandPrint, deviceName,
        imageFile.getAbsolutePath());
      log.debug("print command = {}", commandLine);
      runCommand(commandLine.split("\\s"));
      success = true;
    }
    else
    {
      errorMessage = "Bilddatei existiert nicht.";
      log.error(errorMessage);
    }
    return success;
  }

  public boolean checkPrinter()
  {
    boolean success = false;

    String commandLine = MessageFormat.format(printerCommandStatus, deviceName);
    log.debug("print status command = {}", commandLine);

    String result = runCommand(commandLine.split("\\s"));

    log.debug("print status result = {}", result);

    int index = result.indexOf("Printer error:");
    if (index > 0)
    {
      errorMessage = result.substring(index);
    }
    else
    {
      success = true;
    }

    return success;
  }
  
  
  public void cancelAllPrintJobs()
  {
    log.debug("cancel all print jobs command = {}", printerCommandCancel);
    String result = runCommand(printerCommandCancel.split("\\s"));
    log.debug("cancel all print jobs result = {}", result);
  }
  
  public void enablePrintQueue()
  {
    String commandLine = MessageFormat.format(printerCommandEnable, deviceName);
    log.debug("print enable queue command = {}", commandLine);
    String result = runCommand(commandLine.split("\\s"));
    log.debug("print enable queue result = {}", result);
  }
  

  @Value("${printer.command.find}")
  private String printerCommandFind;

  @Value("${printer.command.print}")
  private String printerCommandPrint;

  @Value("${printer.command.status}")
  private String printerCommandStatus;

  @Value("${printer.command.cancel}")
  private String printerCommandCancel;

  @Value("${printer.command.enable}")
  private String printerCommandEnable;

  private boolean enabled;

  private String deviceName;

  private String errorMessage;
}

/*

lp -d Canon-CP910-1 var/IMG_9900.JPG

Drucker Canon-CP910-1 druckt jetzt Canon-CP910-1-4.  Aktiviert seit So 28 Apr 2024 19:44:16 CEST
	Printing page 1, 100%
th@photobox-pi4:~ $ lpstat -p Canon-CP910-1
Drucker Canon-CP910-1 druckt jetzt Canon-CP910-1-4.  Aktiviert seit So 28 Apr 2024 19:44:16 CEST
	Printer state: Feeding Paper
th@photobox-pi4:~ $ lpstat -p Canon-CP910-1
Drucker Canon-CP910-1 druckt jetzt Canon-CP910-1-4.  Aktiviert seit So 28 Apr 2024 19:44:16 CEST
	Printer state: Feeding Paper
th@photobox-pi4:~ $ lpstat -p Canon-CP910-1
Drucker Canon-CP910-1 druckt jetzt Canon-CP910-1-4.  Aktiviert seit So 28 Apr 2024 19:44:16 CEST
	Printer state: Feeding Paper
th@photobox-pi4:~ $ lpstat -p Canon-CP910-1
Drucker Canon-CP910-1 ist deaktiviert seit So 28 Apr 2024 19:44:23 CEST -
	Printer error: No Paper (03)
th@photobox-pi4:~ $ lpstat -p Canon-CP910-1
Drucker Canon-CP910-1 ist deaktiviert seit So 28 Apr 2024 19:44:23 CEST -
	Printer error: No Paper (03)
th@photobox-pi4:~ $ lpstat -p Canon-CP910-1
Drucker Canon-CP910-1 ist deaktiviert seit So 28 Apr 2024 19:44:23 CEST -
	Printer error: No Paper (03)


th@photobox-pi4:~ $ cancel -a -x
th@photobox-pi4:~ $ lpstat -p Canon-CP910-1
Drucker Canon-CP910-1 ist deaktiviert seit So 28 Apr 2024 19:44:23 CEST -
	Printer error: No Paper (03)
th@photobox-pi4:~ $ sudo lpadmin -p Canon-CP910-1  -E
th@photobox-pi4:~ $ lpstat -p Canon-CP910-1
Drucker Canon-CP910-1 ist im Leerlauf.  Aktiviert seit So 28 Apr 2024 19:46:25 CEST

 */
