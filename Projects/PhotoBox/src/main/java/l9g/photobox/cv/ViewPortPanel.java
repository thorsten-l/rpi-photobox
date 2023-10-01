package l9g.photobox.cv;

//~--- non-JDK imports --------------------------------------------------------
import java.awt.AWTException;
import l9g.photobox.AppState;
import l9g.photobox.Config;
import l9g.photobox.gphoto2.GPhoto2Exception;
import l9g.photobox.gphoto2.GPhoto2Handler;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import l9g.photobox.App;
import static l9g.photobox.AppState.ERROR;
import static l9g.photobox.AppState.STARTCOUNTDOWN;
import l9g.photobox.Util;

/**
 * Class description
 *
 *
 * @version $version$, 18/08/24
 * @author Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
public class ViewPortPanel extends JPanel implements Runnable
{

  /**
   * Field description
   */
  private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(
    ViewPortPanel.class.getName());

  private static final long serialVersionUID = 3249306173642427576L;

  //~--- constructors ---------------------------------------------------------
  private final Config config;

  /**
   * Constructs ...
   *
   *
   */
  public ViewPortPanel()
  {
    counter = 0;

    config = Config.getInstance();

    bigFontHeight = config.getBigFontHeight();

    bigFont = new Font("Arial", Font.BOLD, bigFontHeight);
    midFont = new Font("Arial", Font.BOLD, config.getMidFontHeight());

    messagePosX = config.getMessagePosX();
    messagePosY = config.getMessagePosY();
    countdownPosX = config.getCountdownPosX();
    countdownPosY = config.getCountdownPosY();
    viewWidth = config.getViewWidth();
    viewHeight = config.getViewHeight();
    
    try
    {
      Robot robot = new Robot();
      robot.mouseMove(viewWidth, viewHeight);
    }
    catch (AWTException ex)
    {
     LOGGER.warn("Robot for mouse movement does not work.");
    }
    
  }

  //~--- methods --------------------------------------------------------------
  @Override
  @SuppressWarnings("fallthrough")
  public void paintComponent(Graphics g)
  {
    Graphics2D g2d = (Graphics2D) g;

    if (yesBounds == null)
    {
      FontRenderContext frc = g2d.getFontRenderContext();

      g2d.setFont(midFont);
      yesBounds = g2d.getFont().getStringBounds(yesLabel, frc);

      LineMetrics lineMetrics = g2d.getFont().getLineMetrics(yesLabel, frc);

      noBounds = g2d.getFont().getStringBounds(noLabel, frc);

      int gap2 = 5;
      int buttonWidth = (viewWidth / 2) - gap2;
      int buttonHeight = yesBounds.getBounds().height * 2;

      LOGGER.debug("button height = {}", buttonHeight);

      int fontHeightPx = yesBounds.getBounds().height;

      yesButtonRectangle = new Rectangle(imageX, viewHeight - buttonHeight,
        buttonWidth, buttonHeight);
      noButtonRectangle = new Rectangle(imageX + buttonWidth + (2 * gap2),
        viewHeight - buttonHeight, buttonWidth, buttonHeight);

      yesLabelPoint = new Point(yesButtonRectangle.x + (yesButtonRectangle.width
        - yesBounds.getBounds().width) / 2, yesButtonRectangle.y
        + fontHeightPx - (int) lineMetrics.getDescent()
        + (yesButtonRectangle.height - fontHeightPx) / 2);
      noLabelPoint = new Point(noButtonRectangle.x + (noButtonRectangle.width
        - noBounds.getBounds().width) / 2, yesLabelPoint.y);

      addMouseListener(new MouseHandler(yesButtonRectangle, noButtonRectangle));
    }

    switch (AppState.getState())
    {
      case STANDBY:
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        App.buttonLed.setBlink(false);
        AppState.setState(AppState.READY);
        break;

      case PRINTINGFAILED:
        g2d.setFont(midFont);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.red);
        g2d.drawString("Drucker Fehler!", messagePosX, messagePosY - 80);
        g2d.drawString("Evtl. kein Papier - behoben?", messagePosX, messagePosY);

        g2d.setColor(Color.green);
        g2d.fillRect(yesButtonRectangle.x, yesButtonRectangle.y,
          yesButtonRectangle.width, yesButtonRectangle.height);

        g2d.setColor(Color.white);
        g2d.drawString(yesLabel, yesLabelPoint.x, yesLabelPoint.y);

        break;

      case PRINTPICTURE:
        image = GPhoto2Handler.getSnapshot();
        g2d.drawImage(image, imageX, imageY, viewWidth, viewHeight, this);
        g2d.setFont(midFont);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.black);
        g2d.drawString("Bild wird gedruckt...", messagePosX+2, messagePosY+2);
        g2d.setColor(Color.cyan);
        g2d.drawString("Bild wird gedruckt...", messagePosX, messagePosY);

        if ((System.currentTimeMillis() - AppState.getStateChangedTimestamp())
          >= 10000 && printingChecked == false)
        {
          printingChecked = true;
          if (Util.checkPrinting() == false)
          {
            g2d.setColor(Color.black);
            g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
            App.buttonLed.setBlink(false);
            AppState.setState(AppState.PRINTINGFAILED);
            Util.resetPrintSystem();
          }
        }

        if ((System.currentTimeMillis() - AppState.getStateChangedTimestamp())
          >= 20000)
        {
          AppState.setState(AppState.STANDBY);
        }

        break;

      case PRESHUTDOWN:
        g2d.setFont(midFont);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.black);
        g2d.drawString("Shutdown?", messagePosX + 2, messagePosY + 2);
        g2d.setColor(Color.cyan);
        g2d.drawString("Shutdown?", messagePosX, messagePosY);

        if ((System.currentTimeMillis() - AppState.getStateChangedTimestamp())
          >= 5000)
        {
          App.buttonLed.setBlink(false);
          AppState.setState(AppState.STANDBY);
        }

        break;

      case LOADPICTURE:
        g2d.setFont(midFont);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

        g2d.setColor(Color.gray);

        double w = (double) (System.currentTimeMillis()
          - startLoadingPictureTimestamp);

        w /= 20000.0;
        w *= viewWidth;

        g2d.fillRect(imageX, messagePosY - 120, (int) w, 32);

        g2d.drawString("Bitte warten...", messagePosX, messagePosY);

        break;

      case PRINTQUESTION:
        printingChecked = false;
        image = GPhoto2Handler.getSnapshot();
        g2d.drawImage(image, imageX, imageY, viewWidth, viewHeight, this);
        g2d.setFont(midFont);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (config.isPrintingDisabled() == false)
        {
          g2d.setColor(Color.black);
          g2d.drawString("Bild drucken?", messagePosX + 2, messagePosY + 2);
          g2d.setColor(Color.cyan);
          g2d.drawString("Bild drucken?", messagePosX, messagePosY);

          g2d.setColor(Color.green);
          g2d.fillRect(yesButtonRectangle.x, yesButtonRectangle.y,
            yesButtonRectangle.width, yesButtonRectangle.height);

          g2d.setColor(Color.red);
          g2d.fillRect(noButtonRectangle.x, noButtonRectangle.y,
            noButtonRectangle.width, noButtonRectangle.height);

          g2d.setColor(Color.white);
          g2d.drawString(yesLabel, yesLabelPoint.x, yesLabelPoint.y);
          g2d.drawString(noLabel, noLabelPoint.x, noLabelPoint.y);
        }
        else
        {
          g2d.setColor(Color.black);
          g2d.drawString("Vorschau", messagePosX + 2, messagePosY + 2);

          g2d.setColor(Color.cyan);
          g2d.drawString("Vorschau", messagePosX, messagePosY);

          g2d.setColor(Color.green);
          g2d.fillRect(noButtonRectangle.x, noButtonRectangle.y,
            noButtonRectangle.width, noButtonRectangle.height);

          g2d.setColor(Color.white);
          g2d.drawString("weiter", noLabelPoint.x - 20, noLabelPoint.y);
        }

        break;

      case TAKEPICTURE:
        printingChecked = false;
        AppState.setState(AppState.TAKEPICTURE2);
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (GPhoto2Handler.isReady())
        {
          Thread tpThread = new Thread(
            () ->
          {
            try
            {
              GPhoto2Handler.takePicture();
            }
            catch (GPhoto2Exception ex)
            {
              LOGGER.error("Can't take picture", ex);
              AppState.setState(AppState.ERROR, "Übertragungsfehler!");
            }
          });

          tpThread.setDaemon(true);
          tpThread.start();
        }
        else
        {
          LOGGER.warn("GPhoto2Handler not ready");
        }

        break;

      case TAKEPICTURE2:
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

        if ((System.currentTimeMillis() - counterTimestamp) >= 500)
        {
          g2d.setColor(Color.black);
          g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
          System.gc();
          startLoadingPictureTimestamp = System.currentTimeMillis();
          AppState.setState(AppState.LOADPICTURE);
        }

        break;

      case NOPRINT:
        AppState.setState(AppState.STANDBY);
        break;

      case YESPRINT:
        if (config.isPrintingDisabled() == false)
        {
          g2d.setColor(Color.black);
          g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
          AppState.setState(AppState.PRINTPICTURE);
        }
        else
        {
          AppState.setState(AppState.STANDBY);
        }
        break;
        
      case ERROR:
        g2d.setFont(midFont);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.red);
        g2d.drawString(AppState.getMessage(), messagePosX, 120);
        
        if ((System.currentTimeMillis()
          - AppState.getStateChangedTimestamp()) >= 5000)
        {
          AppState.setState(AppState.STANDBY);
        }
        break;
        
      case STARTCOUNTDOWN:
        App.buttonLed.setBlink(true);
        counter = 5;
        counterTimestamp = System.currentTimeMillis();
        AppState.setState(AppState.COUNTDOWN);
        
      default:
        image = (biGrabber != null) ? biGrabber.getImage() : null;

        if (image != null)
        {
          if (AppState.getState() != AppState.COUNTDOWN || counter > 2)
          {
            g2d.drawImage(image, imageX, imageY, viewWidth, viewHeight, this);
          }
          else
          {
            g2d.setColor(Color.black);
            g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
            g2d.setColor(Color.white);
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
            g2d.fillPolygon(px, py, 3);
            g2d.fillRect(w2 - 100, h3, 200, this.getHeight() - h3);

            g2d.setColor(Color.GREEN);
            g2d.setFont(midFont);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
              RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.drawString("Schau", w2 + 200, 80);
            g2d.drawString("hoch!", w2 + 200, 160);
          }

          if (AppState.getState() == AppState.COUNTDOWN)
          {
            g2d.setColor(Color.red);
            g2d.setFont(bigFont);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
              RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.drawString(Long.toString(counter), countdownPosX,
              countdownPosY);

            if ((System.currentTimeMillis() - counterTimestamp) >= 1000)
            {
              counter--;
              counterTimestamp = System.currentTimeMillis();

              if (counter == 0)
              {
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
    while (true)
    {
      try
      {
        paintImmediately(0, 0, panelWidth, panelHeight);
        Thread.sleep(100);
      }
      catch (InterruptedException ex)
      {
        LOGGER.error("ViewPort failed", ex);
        System.exit(-1);
      }
    }
  }

  /**
   * Method description
   *
   */
  public void startUpdate()
  {
    LOGGER.debug("Starting Update");

    updateViewThread = new Thread(this);
    updateViewThread.setDaemon(true);
    updateViewThread.start();

    biGrabber = new BufferedImageGrabber();
    biGrabber.start();

    panelWidth = this.getWidth();
    panelHeight = this.getHeight();

    imageX = (panelWidth - config.getViewWidth()) / 2;
    imageY = (panelHeight - config.getViewHeight()) / 2;
  }

  //~--- fields ---------------------------------------------------------------
  /**
   * Field description
   */
  private final Font bigFont;

  /**
   * Field description
   */
  private final int bigFontHeight;

  /**
   * Field description
   */
  private final int countdownPosX;

  /**
   * Field description
   */
  private final int countdownPosY;

  /**
   * Field description
   */
  private long counter;

  /**
   * Field description
   */
  private long counterTimestamp;

  private BufferedImageGrabber biGrabber;

  /**
   * Field description
   */
  private Thread updateViewThread;

  /**
   * Field description
   */
  private BufferedImage image;

  /**
   * Field description
   */
  private int imageX;

  /**
   * Field description
   */
  private int imageY;

  /**
   * Field description
   */
  private final int messagePosX;

  /**
   * Field description
   */
  private final int messagePosY;

  /**
   * Field description
   */
  private final Font midFont;

  /**
   * Field description
   */
  private Rectangle2D noBounds;

  /**
   * Field description
   */
  private Rectangle noButtonRectangle;

  /**
   * Field description
   */
  private final String noLabel = "NEIN";

  /**
   * Field description
   */
  private Point noLabelPoint;

  /**
   * Field description
   */
  private int panelHeight;

  /**
   * Field description
   */
  private int panelWidth;

  /**
   * Field description
   */
  private long startLoadingPictureTimestamp;

  /**
   * Field description
   */
  private final int viewHeight;

  /**
   * Field description
   */
  private final int viewWidth;

  /**
   * Field description
   */
  private Rectangle2D yesBounds;

  /**
   * Field description
   */
  private Rectangle yesButtonRectangle;

  /**
   * Field description
   */
  private final String yesLabel = "JA";

  /**
   * Field description
   */
  private Point yesLabelPoint;

  private boolean printingChecked;
}
