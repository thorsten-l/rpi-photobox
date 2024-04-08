package l9g.photobox;

//~--- non-JDK imports --------------------------------------------------------
import java.awt.Container;
import l9g.photobox.cv.BaseFrame;
import l9g.photobox.gphoto2.GPhoto2Exception;
import l9g.photobox.gphoto2.GPhoto2Handler;
import l9g.photobox.gpio.GpioButtonEvent;
import l9g.photobox.gpio.GpioButtonHandler;
import l9g.photobox.gpio.GpioButtonListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import java.util.Properties;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import l9g.photobox.gpio.GpioOutput;

/**
 * Class description
 *
 *
 * @version $version$, 18/08/19
 * @author Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
public class App implements GpioButtonListener
{

  static
  {
    String appHome = System.getProperty("app.home");
    if (appHome == null || appHome.length() == 0)
    {
      System.setProperty("app.home", ".");
    }
  }

  /**
   * Field description
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(
    App.class.getName());

  /**
   * Field description
   */
  private static BaseFrame baseFrame;

  /**
   * Field description
   */
  private static GpioButtonHandler shutterButtonHandler;

  /**
   * Field description
   */
  private static GpioButtonHandler yesButtonHandler;

  /**
   * Field description
   */
  private static GpioButtonHandler noButtonHandler;

  public static GpioOutput buttonLed;

  //~--- static initializers --------------------------------------------------
  static
  {
    try (PrintWriter out = new PrintWriter("timestamp.txt"))
    {
      out.println(new java.util.Date().toString());
    }
    catch (FileNotFoundException ex)
    {
      LOGGER.error("Could not write timestamp.txt", ex);
    }
  }

  //~--- methods --------------------------------------------------------------
  private static void setVsyncRequested(JFrame f, boolean b)
  {
    try
    {
      Class<?> tmpClass
        = Class.forName("com.sun.java.swing.SwingUtilities3");
      Method tmpMethod
        = tmpClass.
          getMethod("setVsyncRequested", Container.class, boolean.class);
      tmpMethod.invoke(tmpClass, f, Boolean.valueOf(b));
      LOGGER.info("VSync requested");
    }
    catch (Throwable ignore)
    {
      LOGGER.error("Warning: Error while requesting vsync: " + ignore);
    }
  }

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
    LOGGER.info("PhotoBox starting...");
    AppState.setState(AppState.STARTUP);

    Runtime.getRuntime().addShutdownHook(new Thread(() ->
    {
      AppState.setState(AppState.SHUTDOWN);
      LOGGER.info("PhotoBox exit.");

      try
      {
        GPhoto2Handler.close();
        Thread.sleep(5000);
      }
      catch (InterruptedException | GPhoto2Exception ex)
      {
        LOGGER.error("Error on sleep 5s", ex);
      }
    }));

    printBuildProperties();

    Config config = Config.getInstance();

    UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 48));
    UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.PLAIN, 48));

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    String localIp = "<unknown>";

    System.out.println("--- NetworkInterface ---");
    Enumeration<NetworkInterface> networkInterfaceEnumeration
      = NetworkInterface.getNetworkInterfaces();

    while (networkInterfaceEnumeration.hasMoreElements())
    {
      NetworkInterface networkInterface
        = networkInterfaceEnumeration.nextElement();

      String name = networkInterface.getName();

      if (name.startsWith("en")
        || name.startsWith("eth")
        || name.startsWith("wlan"))
      {
        System.out.print(networkInterface.getName());

        for (InterfaceAddress interfaceAddress : networkInterface.
          getInterfaceAddresses())
        {
          if (interfaceAddress.getAddress().isSiteLocalAddress())
          {
            System.out.print(" " + interfaceAddress.getAddress().
              getHostAddress());
            if (localIp.equals("<unknown>"))
            {
              localIp = interfaceAddress.getAddress().
                getHostAddress() + " (" + name + ")";
            }
            else
            {
              localIp += " ," + interfaceAddress.getAddress().
                getHostAddress() + " (" + name + ")";
            }
          }
        }
        System.out.println();
      }
    }

    int result = JOptionPane.showConfirmDialog(null,
      "Datum: " + dateFormat.format(new java.util.Date())
      + "\nUhrzeit: " + timeFormat.format(new java.util.Date())
      + "\nNetzwerk-Adresse: " + localIp
      + "\n\nDrucker verwenden?", "Drucker verwenden?",
      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

    if (result == JOptionPane.OK_OPTION)
    {
      config.setPrintingDisabled(false);
      JOptionPane.showMessageDialog(null,
        "Drucker wird verwendet.\nEinschalten nicht vergessen!");
    }
    else
    {
      config.setPrintingDisabled(true);
      UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 32));
      JOptionPane.showMessageDialog(null, "Drucker wird NICHT verwendet!");
    }

    System.out.println("\nprinting disabled = " + config.isPrintingDisabled());
    System.out.println("\n" + config.toString().replace(',', '\n') + "\n");

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

    buttonLed = new GpioOutput("led", config.getButtonLedGpioPin());
    buttonLed.setValue(true);

    if (config.isPrintingDisabled() == false)
    {
      if (Util.usbPrinterConnected() == false)
      {
        LOGGER.error("\n\n*** USB Printer not found ***\n");
        System.exit(2);
      }

      Util.resetPrintSystem();
    }

    File varDir = Config.getVarDirectory();
    varDir.mkdirs();
    GPhoto2Handler.connect();

    java.awt.EventQueue.invokeLater(
      () ->
    {
      baseFrame = new BaseFrame();

      GraphicsDevice device
        = GraphicsEnvironment.getLocalGraphicsEnvironment()
          .getScreenDevices()[0];

      baseFrame.setAlwaysOnTop(true);
      baseFrame.setAutoRequestFocus(true);

      Rectangle screenSize = device.getDefaultConfiguration().getBounds();
      Dimension appDimension = new Dimension(screenSize.width, screenSize.height);

      baseFrame.setPreferredSize(appDimension);
      baseFrame.setMinimumSize(appDimension);
      baseFrame.setVisible(true);
      device.setFullScreenWindow(baseFrame);
      AppState.setState(AppState.STANDBY);
      try
      {
        buttonLed.setValue(true);
      }
      catch (IOException ex)
      {
        java.util.logging.Logger.getLogger(App.class.getName()).
          log(Level.SEVERE, null, ex);
      }
      baseFrame.setVisible(true);
      setVsyncRequested(baseFrame, true);
    });
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
    LOGGER.debug("'{}' button pressed...", gbe.getName());

    int shutterPinNumber = Config.getInstance().getShutterButtonGpioPin();
    int yesPinNumber = Config.getInstance().getYesButtonGpioPin();
    int noPinNumber = Config.getInstance().getNoButtonGpioPin();

    int pressedPinNumber = gbe.getPinNumber();

    switch (AppState.getState())
    {
      case PRESHUTDOWN:
        if (pressedPinNumber == shutterPinNumber)
        {
          try
          {
            buttonLed.setValue(false);
          }
          catch (IOException ex)
          {
            LOGGER.error("Can't set LED");
          }
          baseFrame.dispose();
          System.exit(0);
        }

        break;

      case READY:
      case STANDBY:
        if ((pressedPinNumber == yesPinNumber)
          || (pressedPinNumber == noPinNumber))
        {
          try
          {
            if ((yesButtonHandler.getGpioPin().isLow())
              && (noButtonHandler.getGpioPin().isLow()))
            {
              LOGGER.info("Prepare shutdown...");
              buttonLed.setValue(true);
              AppState.setState(AppState.PRESHUTDOWN);
            }
          }
          catch (IOException ex)
          {
            LOGGER.error("Can't read gpio pins", ex);
          }
        }
        else if (pressedPinNumber == shutterPinNumber)
        {
          AppState.setState(AppState.STARTCOUNTDOWN);
        }

        break;

      case PRINTQUESTION:
        if (pressedPinNumber == noPinNumber)
        {
          // buttonLed.setBlink(false);
          AppState.setState(AppState.NOPRINT);
        }

        if (pressedPinNumber == yesPinNumber)
        {
          AppState.setState(AppState.YESPRINT);
          Util.printPicture();
        }

        break;

      case PRINTINGFAILED:

        if (pressedPinNumber == yesPinNumber)
        {
          AppState.setState(AppState.STANDBY);
          Util.resetPrintSystem();
        }

        break;

      default:
    }
  }
}
