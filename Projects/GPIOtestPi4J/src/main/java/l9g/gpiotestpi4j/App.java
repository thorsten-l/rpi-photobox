package l9g.gpiotestpi4j;

//~--- non-JDK imports --------------------------------------------------------

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;

import java.util.Properties;
import l9g.gpiotestpi4j.gpio.GpioButtonEvent;
import l9g.gpiotestpi4j.gpio.GpioButtonHandler;
import l9g.gpiotestpi4j.gpio.GpioButtonListener;
import l9g.gpiotestpi4j.gpio.GpioOutput;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Slf4j
public class App implements GpioButtonListener
{
  private static GpioButtonHandler shutterButtonHandler;

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
    log.info("GPIOtestPI4J starting...");

    Config config = Config.getInstance();

    Runtime.getRuntime().addShutdownHook(new Thread(() ->
    {
      log.info("GPIOtestPI4J exiting in 5s.");

      try
      {
        Thread.sleep(5000);
      }
      catch (InterruptedException ex)
      {
        log.error("Error on sleep 5s", ex);
      }
    }));

    printBuildProperties();

    log.info("GPIOtest running...");
    log.debug("config={}", config.toString());

    App listener = new App();

    shutterButtonHandler = GpioButtonHandler.createInstance("shutter",
      config.getShutterButtonGpioPin());
    shutterButtonHandler.addButtonPressedListener(listener);

    log.info("GPIOtest running for 10s ...");

    GpioOutput led = new GpioOutput("led", config.getButtonLedGpioPin());
    led.setValue(true);
    Thread.sleep(10000);
    led.setValue(false);
    System.exit(0);
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
      log.error("Can not load build.properties file.", ex);
    }
  }

  @Override
  public void buttonPressed(GpioButtonEvent gbe)
  {
    log.info("'{}' button pressed.", gbe.getName());
  }
}
