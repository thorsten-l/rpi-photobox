package l9g.photobox.cv;

//~--- non-JDK imports --------------------------------------------------------
import l9g.photobox.Config;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------
import java.awt.image.BufferedImage;

/**
 *
 * @author th
 */
public class BufferedImageGrabber extends Thread
{

  /**
   * Field description
   */
  private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(
          BufferedImageGrabber.class.getName());

  /**
   * Field description
   */
  private final static Java2DFrameConverter CONVERTER
          = new Java2DFrameConverter();

  //~--- methods --------------------------------------------------------------
  @Override
  public void run()
  {
    while (true)
    {
      try
      {
        BufferedImage bi = CONVERTER.convert(grabber.grab());

        synchronized (this)
        {
          image = bi;
        }
      } catch (FrameGrabber.Exception ex)
      {
        LOGGER.error("grabbing image not working", ex);
      }

      try
      {
        Thread.sleep(10);
      } catch (InterruptedException ex)
      {
        LOGGER.error("ViewPort failed", ex);
      }
    }
  }

  @Override
  public void start()
  {
    setDaemon(true);
    setPriority(MAX_PRIORITY);
    setName("BufferedImageGrabber");

    LOGGER.info("Starting Grabber");

    grabber = new OpenCVFrameGrabber(Config.getInstance()
            .getVideoDeviceIndex());


    Config config = Config.getInstance();
    grabber.setImageWidth(config.getCaptureWidth());
    grabber.setImageHeight(config.getCaptureHeight());
    grabber.setFormat("YUYV");

    try
    {
      grabber.start();
      super.start();
    } catch (FrameGrabber.Exception ex)
    {
      LOGGER.error("Grabber NOT started.", ex);
    }
  }

  //~--- get methods ----------------------------------------------------------
  /**
   * Method description
   *
   *
   * @return
   */
  public synchronized BufferedImage getImage()
  {
    return image;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Field description
   */
  private OpenCVFrameGrabber grabber;

  /**
   * Field description
   */
  private BufferedImage image;
}

