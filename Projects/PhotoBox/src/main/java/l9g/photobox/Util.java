package l9g.photobox;

//~--- non-JDK imports --------------------------------------------------------
import l9g.photobox.gphoto2.GPhoto2Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Map;
import static l9g.photobox.Util.createVarProcessBuilder;

/**
 * Class description
 *
 *
 * @version $version$, 18/08/26
 * @author Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
public class Util
{

  /**
   * Field description
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(
          Util.class.getName());

  //~--- constructors ---------------------------------------------------------
  /**
   * Constructs ...
   *
   */
  private Util()
  {
  }

  //~--- methods --------------------------------------------------------------
  
  public static void printCyan( String msg )
  {
    System.out.println("\033[1;36m" + msg + "\033[0m");
  }
  
  /**
   * Method description
   *
   *
   * @param command
   *
   * @return
   */
  public static ProcessBuilder createVarProcessBuilder(String command)
  {
    ProcessBuilder processBuilder = new ProcessBuilder(command.split(
            " ")).directory(Config.getVarDirectory());

    Map<String, String> environment = processBuilder.environment();

    environment.put("LANG", "C");
    environment.put("LANGUAGE", "C");
    environment.put("LC_ALL", "C");

    return processBuilder;
  }

  /**
   * Method description
   *
   */
  public static void printPicture()
  {
    Config config = Config.getInstance();

    ProcessBuilder processBuilder = createVarProcessBuilder(
            config.getPrintingCommand() + " " + config.getPrinterDeviceName() + " "
            + GPhoto2Handler.getImageFilename()).inheritIO();

    try
    {
      processBuilder.start();
    } catch (IOException ex)
    {
      LOGGER.error("Print snapshot.jpg failed", ex);
    }
  }

  /**
   * Method description
   *
   */
  public static void resetPrintSystem()
  {
    Config config = Config.getInstance();

    LOGGER.info("Resetting CUPS printing system.");

    try
    {
      ProcessBuilder processBuilder = createVarProcessBuilder(
              config.getPrinterResetCommand());

      Process process = processBuilder.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(
              process.getInputStream()));
      String line;

      while ((line = reader.readLine()) != null)
      {
        line = line.trim();
        printCyan( line );
      }
    } catch (IOException ex)
    {
      LOGGER.error(" Resetting CUPS print system failed! ", ex);
    }

    LOGGER.info("Resetting CUPS printing system ... done.");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static boolean usbPrinterConnected()
  {
    Config config = Config.getInstance();

    boolean printerConnected = false;

    LOGGER.info("Check USB printer connection.");

    try
    {
      ProcessBuilder processBuilder = createVarProcessBuilder(
              config.getPrinterCheckUsbCommand());

      Process process = processBuilder.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(
              process.getInputStream()));
      String line;

      while ((line = reader.readLine()) != null)
      {
        line = line.trim();

        if (line.endsWith(config.getPrinterCheckUsbExpect()))
        {
          printCyan( line );
          printerConnected = true;
        }
      }
    } catch (IOException ex)
    {
      LOGGER.error(" Check USB printer failed! ", ex);
    }

    return printerConnected;
  }


  public static boolean checkPrinting()
  {
    Config config = Config.getInstance();

    boolean successfullyPrinting = true;

    LOGGER.info("Check photo printing.");

    try
    {
      ProcessBuilder processBuilder = createVarProcessBuilder(
              config.getPrinterStatusCommand() + " " + config.getPrinterDeviceName());

      Process process = processBuilder.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(
              process.getInputStream()));
      String line;

      while ((line = reader.readLine()) != null)
      {
        line = line.trim();
        printCyan( line );
        if( line.indexOf("disable") > 0 || line.indexOf("error") > 0 )
        {
          successfullyPrinting = false;
        }
      }
    } catch (IOException ex)
    {
      LOGGER.error(" Check photo printing failed! ", ex);
    }

    LOGGER.info(( successfullyPrinting ) ? "Printing succeed." : "Printing FAILED." );
    return successfullyPrinting;
  }
}

