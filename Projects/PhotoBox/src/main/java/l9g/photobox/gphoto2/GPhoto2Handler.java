package l9g.photobox.gphoto2;

//~--- non-JDK imports --------------------------------------------------------
import l9g.photobox.AppState;
import l9g.photobox.Config;
import l9g.photobox.Util;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;

/**
 * Class description
 *
 *
 * @version $version$, 18/08/19
 * @author Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
public class GPhoto2Handler
{

  /**
   * Field description
   */
  private final static Config CONFIG = Config.getInstance();

  /**
   * Field description
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(
    GPhoto2Handler.class.getName());

  /**
   * Field description
   */
  private final static GPhoto2Handler SINGLETON = new GPhoto2Handler();

  /**
   * Field description
   */
  private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
    "YYYYMMdd-HHmmss");

  /**
   * Field description
   */
  private static long imageCounter;

  /**
   * Field description
   */
  @Getter
  private static BufferedImage snapshot;

  /**
   * Field description
   */
  @Getter
  private static String imageFilename;

  //~--- constructors ---------------------------------------------------------
  /**
   * Constructs ...
   *
   */
  private GPhoto2Handler()
  {
  }

  //~--- methods --------------------------------------------------------------
  /**
   * Method description
   *
   *
   * @throws GPhoto2Exception
   */
  public static void close() throws GPhoto2Exception
  {
    try
    {
      LOGGER.debug("Closing connection to camera.");
      if (SINGLETON != null)
      {
        SINGLETON.destroy();
      }
    }
    catch (IOException | InterruptedException ex)
    {
      throw new GPhoto2Exception("Error closing camera connection.", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @throws GPhoto2Exception
   */
  public static void connect() throws GPhoto2Exception
  {
    try
    {
      LOGGER.debug("Connecting camera via gphoto2 command : {}",
        CONFIG.getGphoto2Command());
      SINGLETON.initialize();
    }
    catch (IOException | InterruptedException ex)
    {
      throw new GPhoto2Exception("Can't initialize camera connection. ("
        + SINGLETON.errorMessage + ")", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @throws GPhoto2Exception
   */
  public static void takePicture() throws GPhoto2Exception
  {
    LOGGER.debug("taking picture");

    try
    {
      SINGLETON.captureImage();
    }
    catch (Throwable ex)
    {
      throw new GPhoto2Exception("Error taking picture.", ex);
    }

    LOGGER.debug("taking picture - done");
  }

  //~--- get methods ----------------------------------------------------------
  /**
   * Method description
   *
   *
   * @return
   */
  public static boolean isReady()
  {
    boolean ready;

    synchronized (SINGLETON)
    {
      ready = SINGLETON.gphoto2ready;
    }

    return ready;
  }

  //~--- methods --------------------------------------------------------------
  /**
   * Method description
   *
   *
   * @return
   */
  private static String createImageFilename()
  {
    imageFilename = DATE_FORMAT.format(new java.util.Date());
    imageFilename = Config.getAbsoluteVarPath(String.format("IMG%s-%04d.jpg",
      imageFilename,
      imageCounter++));
    LOGGER.debug("imageFilename = {}", imageFilename);

    return imageFilename;
  }

  /**
   * Method description
   *
   *
   *
   * @throws IOException
   * @throws InterruptedException
   */
  private void captureImage() throws InterruptedException, IOException
  {
    waitForGphoto2Ready();
    gphoto2ready = false;
    gphoto2error = false;
    gp2out.println(CONFIG.getCaptureImageCommand());
    gp2out.flush();
    waitForGphoto2Ready();

    if (gphoto2error == true)
    {
      LOGGER.error("Taking picture failed.");
      AppState.setState(AppState.ERROR, "Kamerafehler!");
    }
    else
    {
      File snapshotFile = Config.getVarFile("snapshot.jpg");
      createImageFilename();
      File imageFile = new File(imageFilename);

      if (doHorizontalFlip)
      {
        LOGGER.info("Flip horrizontally snapshot image : {}", imageFile.
          getAbsolutePath());
        flipHorizontally(snapshotFile, imageFile);
      }
      else
      {
        snapshotFile.renameTo(imageFile);
        snapshot = ImageIO.read(imageFile);
      }

      LOGGER.debug("snapshot image width={} height={}",
        snapshot.getWidth(), snapshot.getHeight());
      LOGGER.info("Load snapshot image : {}", imageFile.getAbsolutePath());
      AppState.setState(AppState.PRINTQUESTION);
    }
  }

  private void flipHorizontally(File inputFile, File outputFile) throws
    IOException
  {
    BufferedImage inputImage = ImageIO.read(inputFile);
    int width = inputImage.getWidth();
    int height = inputImage.getHeight();
    snapshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++)
    {
      for (int x = 0; x < width; x++)
      {
        snapshot.setRGB(width - 1 - x, y, inputImage.getRGB(x, y));
      }
    }
    ImageIO.write(snapshot, "jpg", outputFile);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws InterruptedException
   */
  private void destroy() throws IOException, InterruptedException
  {
    if (gp2out != null)
    {
      gp2out.println("exit");
      gp2out.close();
      Thread.sleep(1000);
      gphoto2Process.destroy();
    }
  }

  /**
   * Method description
   *
   *
   *
   * @return
   *
   * @throws IOException
   * @throws InterruptedException
   */
  private boolean initialize() throws IOException, InterruptedException
  {
    imageCounter = 1l;

    ProcessBuilder processBuilder = Util.createVarProcessBuilder(
      CONFIG.getGphoto2Command());

    gphoto2Process = processBuilder.start();
    gp2in = new InputStreamReader(gphoto2Process.getInputStream());
    gp2err = new InputStreamReader(gphoto2Process.getErrorStream());
    gp2out = new PrintStream(gphoto2Process.getOutputStream());

    gphoto2ready = false;
    gphoto2error = false;
    errorMessage = null;

    inputHandlerThread = new Thread(
      () ->
    {
      try
      {
        char[] buffer = new char[1];
        char c;
        StringBuilder line = new StringBuilder();

        while ((gp2in.read(buffer)) > 0)
        {
          c = buffer[0];

          if ((c == '\n') || (c == '\r') || (c == '>'))
          {
            if (c == '>')
            {
              line.append('>');
            }

            System.out.println("\033[1;36m" + line.toString() + "\033[0m");

            if ((c == '>') && line.toString().startsWith("gphoto2:"))
            {
              if (LOGGER.isDebugEnabled())
              {
                System.out.println();
                System.out.flush();
                LOGGER.debug("gphoto2 prompt found");
              }

              synchronized (SINGLETON)
              {
                SINGLETON.gphoto2ready = true;
                SINGLETON.notifyAll();
              }
            }

            line = new StringBuilder();
          }
          else
          {
            line.append(c);
          }
        }
      }
      catch (Throwable ex)
      {
        LOGGER.error("Error reading gphoto2 input stream.", ex);
        System.exit(-3);
      }
    });

    inputHandlerThread.setDaemon(true);
    inputHandlerThread.start();

    /////
    errorHandlerThread = new Thread(
      () ->
    {
      try
      {
        char[] buffer = new char[1];
        char c;
        StringBuilder line = new StringBuilder();

        while ((gp2err.read(buffer)) > 0)
        {
          c = buffer[0];

          if ((c == '\n') || (c == '\r'))
          {
            System.out.println("\033[1;31m" + line.toString() + "\033[0m");

            if (line.toString().startsWith("*** Error")
              || line.toString().startsWith("ERROR"))
            {
              synchronized (SINGLETON)
              {
                SINGLETON.gphoto2ready = false;
                SINGLETON.gphoto2error = true;
                SINGLETON.errorMessage = line.toString();
                SINGLETON.notifyAll();
              }
            }

            line = new StringBuilder();
          }
          else
          {
            line.append(c);
          }
        }
      }
      catch (IOException ex)
      {
        LOGGER.error("Error reading gphoto2 error stream.", ex);
        System.exit(-2);
      }
    });

    errorHandlerThread.setDaemon(true);
    errorHandlerThread.start();

    synchronized (this)
    {
      wait(CONFIG.getTimeout());
    }

    LOGGER.debug("gphoto2 ready = {}", gphoto2ready);

    if (gphoto2ready == false)
    {
      throw new IOException("Unable to initialize camera connection");
    }

    return gphoto2ready;
  }

  /**
   * Method description
   *
   *
   * @throws InterruptedException
   */
  private synchronized void waitForGphoto2Ready() throws InterruptedException
  {
    while (gphoto2ready == false)
    {
      wait();
    }
  }

  //~--- fields ---------------------------------------------------------------
  /**
   * Field description
   */
  private Thread errorHandlerThread;

  /**
   * Field description
   */
  private String errorMessage;

  /**
   * Field description
   */
  private InputStreamReader gp2err;

  /**
   * Field description
   */
  private InputStreamReader gp2in;

  /**
   * Field description
   */
  private PrintStream gp2out;

  /**
   * Field description
   */
  private Process gphoto2Process;

  /**
   * Field description
   */
  private boolean gphoto2error;

  /**
   * Field description
   */
  private boolean gphoto2ready;

  private boolean doHorizontalFlip = false;

  /**
   * Field description
   */
  private Thread inputHandlerThread;
}
