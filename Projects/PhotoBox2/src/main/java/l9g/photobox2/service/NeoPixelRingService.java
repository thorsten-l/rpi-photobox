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

import com.fazecast.jSerialComm.SerialPort;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
@Service
public class NeoPixelRingService
{
  @PostConstruct
  public void initialize()
  {
    log.debug("initialize");

    serial = SerialPort.getCommPort("/dev/ttyUSB0");

    if (serial.openPort())
    {
      serial.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
      serial.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
      serial.setComPortParameters(
        115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);

      serialOutputStream = serial.getOutputStream();
      serialinputStream = serial.getInputStream();

      Thread reader = new Thread(() ->
      {
        System.out.println("Start input stream reader");
        int c;
        try
        {
          while ((c = serialinputStream.read()) >= 0)
          {
            System.out.write(c);
            System.out.flush();
          }
        }
        catch (IOException ex)
        {
          log.error("ERROR: " + ex.getMessage());
        }
        System.out.println("End input stream reader");
      });

      reader.setDaemon(true);
      reader.start();

      initialized = true;
      log.debug("successfully initialized");
    }
    else
    {
      System.out.println("ERROR: Not initialized");
      log.error("ERROR: Not initialized");
    }
  }

  private void sendCommand(String command)
  {
    log.debug("send command = {}", command);
    System.out.println("send command = " + command);
    if (initialized)
    {
      try
      {
        serialOutputStream.write('/');
        serialOutputStream.write(command.getBytes());
        serialOutputStream.write('\n');
        serialOutputStream.flush();
      }
      catch (IOException ex)
      {
        log.error("Send command", ex);
      }
    }
    else
    {
      System.out.println("Not initialized");
    }
  }

  public void state()
  {
    sendCommand("state");
  }

  public void run()
  {
    sendCommand("run");
  }

  public void flash()
  {
    sendCommand("flash");
  }

  public void off()
  {
    sendCommand("off");
  }

  public void red()
  {
    sendCommand("red");
  }

  public void green()
  {
    sendCommand("green");
  }

  public void blue()
  {
    sendCommand("blue");
  }

  public void close() throws IOException
  {
    System.out.println("close");
    serialOutputStream.close();
    serialinputStream.close();
    serial.closePort();
  }

  private SerialPort serial;

  private OutputStream serialOutputStream;

  private InputStream serialinputStream;

  private boolean initialized;

}
