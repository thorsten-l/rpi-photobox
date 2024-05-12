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

import l9g.photobox2.service.FrameGrabberService;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import l9g.photobox2.gpio.GpioButtonEvent;
import l9g.photobox2.gpio.GpioButtonHandler;
import l9g.photobox2.gpio.GpioButtonListener;
import l9g.photobox2.gpio.GpioOutput;
import l9g.photobox2.service.GphotoWebApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.command.annotation.Command;

/**
 *
 * @author th
 */
@Command(group = "Application")
@Slf4j
@RequiredArgsConstructor
public class ApplicationCommands implements GpioButtonListener
{
  private final GphotoWebApiService gphotoWebApiService;

  private final FrameGrabberService grabberService;

  private final ViewPortPanel viewport;

  private final DisplayMode displayMode;

  private final GraphicsDevice graphicsDevice;

  @Command(description = "start photobox2 application")
  public void photobox() throws Exception
  {
    log.info("Starting PhotoBox2");

    gpioTestMode = true;
    gpioTestFailed = true;

    shutterButtonHandler = GpioButtonHandler
      .createInstance("shutter", gpioShutterPin);
    shutterButtonHandler.addButtonPressedListener(this);
    buttonLed = new GpioOutput("led", gpioLedPin);
    buttonLed.setValue(true);

    System.out.println("\n\n#################################################");
    System.out.println("#################################################");
    
    System.out.println(
      "\nGPIO Test\nBitte in den nächsten 5s\nden Auslöse-Taster drücken.\n");
    System.out.println("#################################################");
    System.out.println("#################################################");

    Thread.sleep(5000);

    if (gpioTestFailed)
    {
      log.error("GPIO Test (Taster) fehlgeschlagen!");
      exitPhotobox();
    }

    gpioTestMode = false;

    ///////////////////////////////////////////////////////////////////////////
    viewport.initialize(this);
    if (!gphotoWebApiService.checkCamera())
    {
      exitPhotobox();
    }
    grabberService.start();

    log.debug("setup canvas");

    java.awt.EventQueue.invokeLater(()
      ->
    {
      JFrame window = new JFrame();
      window.setAlwaysOnTop(true);
      window.setAutoRequestFocus(true);
      window.setSize(displayMode.getWidth(), displayMode.getHeight());
      window.setLayout(null);
      window.add(viewport);
      window.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          System.out.println("exit application");
          exitPhotobox();
        }
      });

      window.addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyPressed(KeyEvent e)
        {
          log.debug("key code = {}", e.getKeyCode());

          switch (e.getKeyCode())
          {
            case KeyEvent.VK_Q:
            case KeyEvent.VK_ESCAPE:
              exitPhotobox();
              break;

            case KeyEvent.VK_SPACE:
              if (AppState.getState() == AppState.READY)
              {
                AppState.setState(AppState.STARTCOUNTDOWN);
              }
              break;

            case KeyEvent.VK_Y:
              if (AppState.getState() == AppState.PRINTQUESTION)
              {
                AppState.setState(AppState.YESPRINT);
              }
              break;

            case KeyEvent.VK_N:
              if (AppState.getState() == AppState.PRINTQUESTION)
              {
                AppState.setState(AppState.NOPRINT);
              }
              break;

            default:
          }
        }
      });

      graphicsDevice.setFullScreenWindow(window);
      viewportRunner = new Thread((Runnable) viewport, "viewport");
      viewportRunner.setDaemon(true);
      viewportRunner.start();
    });
  }

  public void exitPhotobox()
  {
    log.info("exit photobox2");
    try
    {
      log.debug("shutdown button handler ...");
      shutterButtonHandler.shutdown();
      log.debug("stopping viewport ...");
      viewport.stop();
      if (viewportRunner != null)
      {
        viewportRunner.join(2000);
      }
      log.debug("stopping grabber ...");
      grabberService.stop();
      log.debug("stopping gphoto-webapi ...");
      gphotoWebApiService.stop();
      log.debug("all services shutdown!");
    }
    catch (Exception ex)
    {
      log.error("stop grabber", ex);
    }
    log.debug("exit - reset full screen window");
    // graphicsDevice.setFullScreenWindow(null);
    System.exit(0);
  }

  @Override
  public void buttonPressed(GpioButtonEvent gbe)
  {
    if (gbe.getPinNumber() == gpioShutterPin)
    {
      log.debug("{} button pressed", gbe.getName());

      if (gpioTestMode)
      {
        gpioTestFailed = false;
      }
      else
      {
        if (AppState.getState() == AppState.READY)
        {
          AppState.setState(AppState.STARTCOUNTDOWN);
        }
      }
    }
  }

  private boolean gpioTestMode;

  private boolean gpioTestFailed;

  @Value("${gpio.pin.led}")
  private int gpioLedPin;

  @Value("${gpio.pin.shutter}")
  private int gpioShutterPin;

  private Thread viewportRunner;

  private GpioButtonHandler shutterButtonHandler;

  public GpioOutput buttonLed;
}
