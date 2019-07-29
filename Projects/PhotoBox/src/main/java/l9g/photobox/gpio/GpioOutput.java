package l9g.photobox.gpio;

//~--- non-JDK imports --------------------------------------------------------
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

import lombok.Getter;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author th
 */
public class GpioOutput
{

  private final static Logger LOGGER = LoggerFactory.getLogger(
          GpioOutput.class.getName());

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param pinNumber
   *
   * @throws IOException
   */
  public GpioOutput(String name, int pinNumber) throws IOException
  {
    this.name = name;
    this.pinNumber = pinNumber;

    //J--
    GPIOPinConfig config = new GPIOPinConfig.Builder()
            .setControllerNumber(0)
            .setPinNumber(pinNumber).setDirection(GPIOPinConfig.DIR_OUTPUT_ONLY)
            .setInitValue(false)
            .build();
    //J++

    gpioPin = DeviceManager.open(GPIOPin.class, config);
    timer = null;
  }

  public void setBlink(boolean doBlink)
  {
    LOGGER.info( "Do Blink {}", doBlink);
    if (doBlink)
    {
      if ( timer == null )
      {
        timer = new BlinkTimer();
        timer.start();
      }
    } 
    else
    {
      if ( timer != null )
      {
        timer.stopWorking();
        timer = null;
        setValue(false);
      }
    }
  }

  public void setValue(boolean value)
  {
    //LOGGER.info( "SetValue {}", value );
    lastState = value;
    try
    {
      gpioPin.setValue(value);
    } catch (IOException ex)
    {
      LOGGER.error("Setting output pin({}) value to ({}).", pinNumber, value, ex);
    }
  }

  public boolean getValue()
  {
    boolean value = false;
    try
    {
      value = gpioPin.getValue();
    } catch (IOException ex)
    {
      LOGGER.error("Getting output pin({}) value.", pinNumber, ex);
    }
    return value;
  }

  //~--- fields ---------------------------------------------------------------
  /**
   * Field description
   */
  @Getter
  private final GPIOPin gpioPin;

  /**
   * Field description
   */
  @Getter
  private final String name;

  /**
   * Field description
   */
  @Getter
  private final int pinNumber;

  boolean lastState;
  
  private BlinkTimer timer;
  
  class BlinkTimer extends Thread
  {
    private boolean bornToBeAlive;
    
    public BlinkTimer()
    {
      setDaemon(true);
      bornToBeAlive = true;
    }
    
    public void stopWorking()
    {
      bornToBeAlive = false;
    }
    
    @Override
    public void run()
    {
      while( bornToBeAlive )
      {
        try
        {
          setValue(lastState ? false : true);
          Thread.sleep(1000);
        } catch (InterruptedException ex)
        {
          LOGGER.error( "blink thread running...",ex);
        }
      }
    }
  }
}

