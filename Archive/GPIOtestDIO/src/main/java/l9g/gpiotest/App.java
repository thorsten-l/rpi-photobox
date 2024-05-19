package l9g.gpiotest;

//~--- non-JDK imports --------------------------------------------------------

import l9g.gpiotest.gpio.GpioOutput;
import l9g.gpiotest.gpio.GpioButtonEvent;
import l9g.gpiotest.gpio.GpioButtonHandler;
import l9g.gpiotest.gpio.GpioButtonListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Properties;

/**
 * Class description
 *
 *
 * @version $version$, 18/08/19
 * @author Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
public class App implements GpioButtonListener
{

  /**
   * Field description
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(
    App.class.getName());

  /** Field description */
  private static GpioButtonHandler shutterButtonHandler;

  /** Field description */
  private static GpioButtonHandler yesButtonHandler;

  /** Field description */
  private static GpioButtonHandler noButtonHandler;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param args
   *
   * @throws IOException
   * @throws InterruptedException
   */
  public static void main(String[] args)
    throws IOException, InterruptedException
  {
    LOGGER.info("GPIOtest starting...");

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        LOGGER.info("GPIOtest exiting in 5s.");

        try
        {
          Thread.sleep(5000);
        }
        catch (InterruptedException ex)
        {
          LOGGER.error("Error on sleep 5s", ex);
        }
      }));

    printBuildProperties();

    Config config = Config.getInstance();

    System.out.println("\n" + config + "\n");

    App listener = new App();

    shutterButtonHandler = GpioButtonHandler.createInstance("shutter",
      config.getShutterButtonGpioPin());
    shutterButtonHandler.addButtonPressedListener(listener);

    yesButtonHandler = GpioButtonHandler.createInstance("yes",
      config.getYesButtonGpioPin());
    yesButtonHandler.addButtonPressedListener(listener);

    noButtonHandler = GpioButtonHandler.createInstance("no",
      config.getNoButtonGpioPin());
    noButtonHandler.addButtonPressedListener(listener);

    LOGGER.info("GPIOtest running...");
    
    GpioOutput led = new GpioOutput( "led", config.getButtonLedGpioPin() );
    led.setValue(true);
    Thread.sleep(10000);
    led.setValue(false);
  }

  /**
   * Method description
   *
   */
  private static void printBuildProperties()
  {
    Properties buildProperties = new Properties();

    try
    {
      buildProperties.load(App.class.getResourceAsStream("/build.properties"));
      System.out.println("\nProject:");
      System.out.println("  name = " + buildProperties.getProperty(
        "build.project.name"));
      System.out.println("  version = " + buildProperties.getProperty(
        "build.project.version"));
      System.out.println("  build time = " + buildProperties.getProperty(
        "build.timestamp") + " UTC");

      System.out.println("\nCompiler:");
      System.out.println("  java.version = " + buildProperties.getProperty(
        "build.java.version"));
      System.out.println("  java.vendor = " + buildProperties.getProperty(
        "build.java.vendor"));

      System.out.println("\nRuntime:");

      System.out.println("  java.runtime.version = " + System.getProperty(
        "java.runtime.version"));
      System.out.println("  java.runtime.name = " + System.getProperty(
        "java.runtime.name"));
      System.out.println("  java.vm.info = " + System.getProperty(
        "java.vm.info"));
      System.out.println("  java.vm.name = " + System.getProperty(
        "java.vm.name"));
      System.out.println();
    }
    catch (IOException ex)
    {
      LOGGER.error("Can not load build.properties file.", ex);
    }

  }

  @Override
  public void buttonPressed(GpioButtonEvent gbe)
  {
    LOGGER.debug("'{}' button pressed.", gbe.getName());
  }
}
