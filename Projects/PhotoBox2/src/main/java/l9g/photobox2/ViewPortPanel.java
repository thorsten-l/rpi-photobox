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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import static l9g.photobox2.AppState.LOADPICTURE;
import static l9g.photobox2.AppState.PRINTINGFAILED;
import static l9g.photobox2.AppState.PRINTPICTURE;
import static l9g.photobox2.AppState.PRINTQUESTION;
import static l9g.photobox2.AppState.READY;
import static l9g.photobox2.AppState.STANDBY;
import static l9g.photobox2.AppState.STARTCOUNTDOWN;
import static l9g.photobox2.AppState.TAKEPICTURE;
import static l9g.photobox2.AppState.YESPRINT;
import l9g.photobox2.gphoto.CapturedImage;
import l9g.photobox2.gphoto.CapturedImageResponse;
import l9g.photobox2.service.GphotoWebApiService;
import l9g.photobox2.service.NeoPixelRingService;
import l9g.photobox2.service.PrinterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author th
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewPortPanel extends JPanel implements Runnable
{
  private static final long serialVersionUID = -1451968942041503775L;

  private final Java2DFrameConverter frameConverter = new Java2DFrameConverter();

  private final DisplayMode displayMode;

  private final FrameGrabberService grabberService;

  private final GphotoWebApiService gphotoService;

  private final PrinterService printerService;
  
  private final NeoPixelRingService neoPixelService;

  void initialize(ApplicationCommands applicationCommands)
  {
    log.debug("initialize");
    int width = displayMode.getWidth();
    int height = displayMode.getHeight();
    Dimension panelDimension = new Dimension(width, height);
    this.setPreferredSize(panelDimension);
    this.setMinimumSize(panelDimension);
    this.setBounds(0, 0, width, height);
    this.setBackground(Color.red);
    log.debug("viewport width={} , height={}", width, height);
    this.active = true;
    this.lastTimstamp = System.currentTimeMillis();
    this.videoPosX = (viewportWidth - grabberService.getWidth()) / 2;
    this.smallFont = new Font("Arial", Font.PLAIN, 12);
    this.countdownFont = new Font("Arial", Font.BOLD, countdownFontHeight);
    this.infoFont = new Font("Arial", Font.PLAIN, infoFontHeight);
    this.buttonWidth = viewportWidth / 2 - buttonGap;
    this.buttonPosY = viewportHeight - 1 - buttonHeight;
    this.counter = 5;

    Rectangle noButton = new Rectangle(
      0, buttonPosY, buttonWidth, buttonHeight);
    Rectangle yesButton = new Rectangle(
      viewportWidth / 2 + buttonGap - 1, buttonPosY, buttonWidth, buttonHeight);

    MouseHandler mouseHandler = new MouseHandler(
      noButton, yesButton, applicationCommands, printerService);

    addMouseListener(mouseHandler);
  }

  private void clearScreen(Color color)
  {
    g.setColor(color);
    g.fillRect(0, 0, this.getWidth(), this.getHeight());
  }

  private Thread captureWorker;

  private Thread captureMonitor;

  private boolean captureImageDownloadFinished;

  private void takePicture()
  {
    captureWorker = new Thread(() ->
    {
      captureImageDownloadFinished = false;
      try
      {
        if ((capturedImageResponse = gphotoService.captureImageDownload())
          != null)
        {
          AppState.setState(AppState.LOADPICTURE);
        }
        else
        {
          AppState.setState(AppState.ERROR, "Kamerafehler!");
        }
      }
      catch (Throwable t)
      {
        log.error("capture worker error or interrupted", t);
      }
      captureImageDownloadFinished = true;
      captureMonitor.interrupt();
    });
    captureWorker.setName("captureWorker");
    captureWorker.setDaemon(true);
    captureWorker.start();

    captureMonitor = new Thread(() ->
    {
      try
      {
        Thread.sleep(gphotoService.getGphotoWebApiCaptureTimeout());
      }
      catch (InterruptedException ex)
      {
        log.debug("capture monitor interrupted");
      }
      if (captureImageDownloadFinished == false)
      {
        AppState.setState(AppState.ERROR, "Kamerafehler! - Timeout");
        captureWorker.interrupt();
      }
    });
    captureMonitor.setName("captureMonitor");
    captureMonitor.setDaemon(true);
    captureMonitor.start();
  }

  void showMessage(boolean errorMessage, String message)
  {
    g.setFont(infoFont);
    g.setColor(Color.darkGray);
    g.drawString(message, 42, viewportHeight - buttonHeight - (buttonGap * 2)
      + 2);
    if (errorMessage)
    {
      g.fillRoundRect(0, 560, viewportWidth - 1, 100, buttonArc, buttonArc);
      g.setColor(Color.lightGray);
      g.drawRoundRect(0, 560, viewportWidth - 1, 100, buttonArc, buttonArc);
      g.setColor(Color.red);
    }
    else
    {
      g.setColor(Color.cyan);
    }
    g.drawString(message, 40, viewportHeight - buttonHeight - (buttonGap * 2));
  }

  void showMessage(String message)
  {
    showMessage(false, message);
  }

  void showNoButton(String label)
  {
    g.setFont(infoFont);
    FontRenderContext frc = g.getFontRenderContext();
    Rectangle2D labelBounds = g.getFont().getStringBounds(label, frc);
    LineMetrics lineMetrics = g.getFont().getLineMetrics(label, frc);

    int buttonPosX = 0;
    g.setColor(new Color(128, 0, 0));
    g.fillRoundRect(buttonPosX, buttonPosY, buttonWidth,
      buttonHeight, buttonArc, buttonArc);
    g.setColor(new Color(255, 0, 0));
    g.drawRoundRect(buttonPosX, buttonPosY, buttonWidth,
      buttonHeight, buttonArc, buttonArc);

    g.setColor(Color.white);
    g.drawString(label,
      buttonPosX + ((buttonWidth - (int) labelBounds.getWidth()) / 2),
      viewportHeight
      - ((buttonHeight - (int) labelBounds.getHeight()) / 2)
      - (int) lineMetrics.getDescent()
    );
  }

  void showYesButton(String label)
  {
    g.setFont(infoFont);
    FontRenderContext frc = g.getFontRenderContext();
    Rectangle2D labelBounds = g.getFont().getStringBounds(label, frc);
    LineMetrics lineMetrics = g.getFont().getLineMetrics(label, frc);

    int buttonPosX = viewportWidth / 2 + buttonGap - 1;

    g.setColor(new Color(0, 128, 0));
    g.fillRoundRect(buttonPosX, buttonPosY, buttonWidth,
      buttonHeight, buttonArc, buttonArc);
    g.setColor(new Color(0, 255, 0));
    g.drawRoundRect(buttonPosX, buttonPosY, buttonWidth,
      buttonHeight, buttonArc, buttonArc);

    g.setColor(Color.white);
    g.drawString(label,
      buttonPosX + ((buttonWidth - (int) labelBounds.getWidth()) / 2),
      viewportHeight
      - ((buttonHeight - (int) labelBounds.getHeight()) / 2)
      - (int) lineMetrics.getDescent()
    );
  }

  @Override
  public void paint(Graphics _g)
  {
    g = (Graphics2D) _g;

    switch (AppState.getState())
    {
      case STANDBY:
      {
        counter = 5;
        doOnce = true;
        clearScreen(Color.BLACK);
        AppState.setState(AppState.READY);
      }
      break;

      case TAKEPICTURE:
      {
        AppState.setState(AppState.TAKEPICTURE2);
        clearScreen(Color.WHITE);
        timestamp = System.currentTimeMillis();
        doOnce = true;
        takePicture();
      }
      break;

      case TAKEPICTURE2:
        long timediff = System.currentTimeMillis() - timestamp;
        if (timediff >= 1000)
        {
          if (doOnce)
          {
            doOnce = false;
            neoPixelService.off();
            clearScreen(Color.BLACK);
            g.setColor(Color.cyan);
            g.setFont(infoFont);
            g.drawString("Bitte warten ...", 100, 150);
          }

          double progress = (timediff - 1000) / maxProgressTime;

          g.setColor(Color.yellow);
          g.fillRoundRect(5, 200, (int) ((viewportWidth - 10) * progress), 100,
            buttonArc, buttonArc);
          g.setColor(Color.cyan);
          g.drawRoundRect(5, 200, viewportWidth - 10, 100, buttonArc, buttonArc);
        }
        break;

      case LOADPICTURE:
      {
        try
        {
          CapturedImage capturedImage = capturedImageResponse.getImages()[0];
          File imageDirectory = new File(capturedImage.getLocalFolder());
          snapshot = ImageIO.read(
            new File(imageDirectory, capturedImage.getInfo().getName()));
          doOnce = true;
          AppState.setState(AppState.PRINTQUESTION);
        }
        catch (IOException ex)
        {
          AppState.setState(AppState.ERROR, "Bilddaten!");
          log.error("loading image", ex);
        }
      }
      break;

      case PRESHUTDOWN:
      {
        if (doOnce)
        {
          doOnce = false;
          showMessage("Photobox beenden?");
          showNoButton("NEIN");
          showYesButton("JA");
        }
      }
      break;

      case PRINTQUESTION:
      {
        if (doOnce)
        {
          doOnce = false;
          clearScreen(Color.BLACK);
          g.drawImage(snapshot, videoPosX, 0, imageWidth, imageHeight, null);

          if (printerService.isEnabled())
          {
            showMessage("Bild drucken?");
            showNoButton("NEIN");
            showYesButton("JA");
          }
          else
          {
            showYesButton("WEITER");
          }
        }
      }
      break;

      case YESPRINT:
        log.debug("YESPRINT");
        doOnce = true;
        printingChecked = false;
        if (capturedImageResponse != null
          && capturedImageResponse.getImages() != null
          && capturedImageResponse.getImages()[0] != null)
        {
          log.debug("PRINT");
          if (printerService.print(
            capturedImageResponse.getImages()[0].getLocalFolder(),
            capturedImageResponse.getImages()[0].getInfo().getName()))
          {
            AppState.setState(AppState.PRINTPICTURE);
          }
          else
          {
            AppState.setState(AppState.PRINTINGFAILED);
          }
        }
        else
        {
          AppState.setState(AppState.PRINTINGFAILED);
        }
        break;

      case NOPRINT:
        AppState.setState(AppState.STANDBY);
        break;

      case PRINTPICTURE:
      {
        if (doOnce)
        {
          doOnce = false;
          clearScreen(Color.BLACK);
          g.drawImage(snapshot, videoPosX, 0, imageWidth, imageHeight, null);
          showMessage("Bild wird gedruckt...");
        }

        if ((System.currentTimeMillis() - AppState.getStateChangedTimestamp())
          >= 10000)
        {
          doOnce = true;
          if (printerService.checkPrinter())
          {
            AppState.setState(AppState.STANDBY);
          }
          else
          {
            AppState.setState(AppState.PRINTINGFAILED);
          }
        }
      }
      break;

      case PRINTINGFAILED:
      {
        if (doOnce)
        {
          doOnce = false;
          neoPixelService.red();
          clearScreen(Color.BLACK);
          g.drawImage(snapshot, videoPosX, 0, imageWidth, imageHeight, null);
          showMessage(true, printerService.getErrorMessage());
          showYesButton("WEITER");
        }
      }
      break;

      case ERROR:
      {
        // if (doOnce)
        {
          doOnce = false;
          clearScreen(Color.BLACK);
          showMessage(true, AppState.getMessage());
          showYesButton("WEITER");
        }
      }
      break;

      case STARTCOUNTDOWN:
        counter = 5;
        timestamp = System.currentTimeMillis();
        AppState.setState(AppState.COUNTDOWN);

      case READY:
      case COUNTDOWN:
      default:
      {
        doOnce = true;

        if (counter > 2)
        {
          clearScreen(Color.BLACK);

          if (videoImage != null)
          {
            g.drawImage(videoImage, videoPosX, 0, imageWidth, imageHeight, null);
          }

          if (showFpsEnabled)
          {
            g.setColor(Color.RED);
            g.drawRect(0, 0, viewportWidth - 1, viewportHeight - 1);
            g.setFont(smallFont);
            g.drawString(Long.toString(
              1000 / (System.currentTimeMillis() - lastTimstamp)),
              viewportWidth - 30, 30);
            lastTimstamp = System.currentTimeMillis();
          }
        }

        if (AppState.getState() == AppState.COUNTDOWN)
        {          
          if (counter <= 2)
          {
            clearScreen(Color.black);
            g.setColor(Color.white);
            int w2 = this.getWidth() / 2;
            int h3 = this.getHeight() / 3;
            int[] px =
            {
              w2, w2 - 200, w2 + 200
            };
            int[] py =
            {
              0, h3, h3
            };
            g.fillPolygon(px, py, 3);
            g.fillRect(w2 - 100, h3, 200, this.getHeight() - h3);

            g.setColor(Color.GREEN);
            g.setFont(infoFont);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
              RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawString("Schau", w2 + 200, 80);
            g.drawString("hoch!", w2 + 200, 160);
          }

          g.setFont(countdownFont);
          g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

          g.setColor(Color.DARK_GRAY);
          g.drawString(
            Integer.toString(counter), countdownPosX + 2, countdownPosY + 2);
          g.setColor(Color.RED);
          g.drawString(
            Integer.toString(counter), countdownPosX, countdownPosY);

          if (System.currentTimeMillis() - timestamp >= 1000)
          {
            if ( counter == 4 )
            {
              neoPixelService.run();
            }
            
            if ( counter == 2 )
            {
               neoPixelService.flash();
            }
            
            timestamp = System.currentTimeMillis();
            counter--;
            if (counter == 0)
            {
              clearScreen(Color.WHITE);
              AppState.setState(AppState.TAKEPICTURE);
            }
          }
        }
      }
    }
  }

  @Override
  public void run()
  {
    log.info("viewport running");
    Frame videoFrame;

    AppState.setState(AppState.STANDBY);

    while (active)
    {
      try
      {
        videoFrame = grabberService.grab();

        if (videoFrame != null && videoFrame.image != null
          && videoFrame.imageWidth == grabberService.getWidth()
          && videoFrame.imageHeight == grabberService.getHeight())
        {
          videoImage = frameConverter.convert(videoFrame);
          repaint();
        }
      }
      catch (Exception ex)
      {
        log.error("grabber", ex);
      }
    }
  }

  public void stop()
  {
    log.debug("stopping viewport thread");
    active = false;
  }

  // CONFIG ///////////////////////////////////////////////////////////////////
  @Value("${viewport.showfps-enabled}")
  private boolean showFpsEnabled;

  @Value("${viewport.width}")
  private int viewportWidth;

  @Value("${viewport.height}")
  private int viewportHeight;

  @Value("${viewport.max-progress-time}")
  private double maxProgressTime;

  @Value("${viewport.info.font-height}")
  private int infoFontHeight;

  @Value("${viewport.countdown.font-height}")
  private int countdownFontHeight;

  @Value("${viewport.countdown.x}")
  private int countdownPosX;

  @Value("${viewport.countdown.y}")
  private int countdownPosY;

  @Value("${viewport.image.width}")
  private int imageWidth;

  @Value("${viewport.image.height}")
  private int imageHeight;

  @Value("${viewport.button.gap}")
  private int buttonGap;

  @Value("${viewport.button.height}")
  private int buttonHeight;

  @Value("${viewport.button.arc}")
  private int buttonArc;

  /////////////////////////////////////////////////////////////////////////////
  private boolean printingChecked;

  private int buttonWidth;

  private int buttonPosY;

  private boolean doOnce;

  private BufferedImage snapshot;

  private CapturedImageResponse capturedImageResponse;

  private Font smallFont;

  private Font countdownFont;

  private Font infoFont;

  private long timestamp;

  private Graphics2D g;

  private int counter;

  private boolean active;

  private long lastTimstamp;

  private int videoPosX;

  private BufferedImage videoImage;
}
