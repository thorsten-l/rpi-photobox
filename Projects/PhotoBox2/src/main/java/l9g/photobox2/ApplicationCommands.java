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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 *
 * @author th
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ApplicationCommands implements ApplicationRunner,
                                            GpioButtonListener
{
  private final GphotoWebApiService gphotoWebApiService;

  private final FrameGrabberService grabberService;

  private final ViewPortPanel viewport;

  private final DisplayMode displayMode;

  private final GraphicsDevice graphicsDevice;

  @Override
  public void run(ApplicationArguments args) throws Exception
  {
    log.info("{}", BuildProperties.toFormattedString());
    photobox();
  }

  public void photobox() throws Exception
  {
    log.info("Starting PhotoBox2");

    Thread shutdownHook = new Thread(() ->
    {
      shutdownPhotobox();
      System.out.println(AnsiTerm.BG_RED.toString()
        + AnsiTerm.FG_BOLD_WHITE + "\n\n\n    EXIT\n\n"
        + AnsiTerm.RESET + "\n");
    });
    shutdownHook.setDaemon(true);
    Runtime.getRuntime().addShutdownHook(shutdownHook);

    gpioTestMode = true;
    gpioTestFailed = true;

    shutterButtonHandler = GpioButtonHandler
      .createInstance("shutter", gpioShutterPin);
    shutterButtonHandler.addButtonPressedListener(this);
    buttonLed = new GpioOutput("led", gpioLedPin);
    buttonLed.setValue(true);

    System.out.print(AnsiTerm.BG_BLUE.toString()
      + AnsiTerm.FG_BOLD_BRIGHT_WHITE);
    System.out.println(
      "\n\n\n    *** GPIO Test ***\n    Bitte in den nächsten 5s"
      + "\n    den Auslöse-Taster drücken.\n");
    System.out.println(AnsiTerm.RESET + "\n");

    Thread.sleep(5000);

    if (gpioTestFailed)
    {
      log.error("GPIO Test (Taster) fehlgeschlagen!");
      System.exit(-1);
    }

    gpioTestMode = false;

    ///////////////////////////////////////////////////////////////////////////
    viewport.initialize(this);
    if (!gphotoWebApiService.checkCamera())
    {
      System.exit(0);
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
          System.exit(0);
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
              System.exit(0);
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

  public void shutdownPhotobox()
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
    //graphicsDevice.setFullScreenWindow(null);
    //System.exit(0);
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
