package l9g.photobox.gpio;

//~--- non-JDK imports --------------------------------------------------------

import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.HashSet;

/**
 * Class description
 *
 *
 * @version        $version$, 18/08/19
 * @author         Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
public class GpioButtonHandler implements PinListener
{

  /** Field description */
  private final static long DEBOUNCING_TIME = 200;    // in ms

  /** Field description */
  private final static Logger LOGGER = LoggerFactory.getLogger(
    GpioButtonHandler.class.getName());

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param pinNumber
   *
   * @throws IOException
   */
  private GpioButtonHandler(String name, int pinNumber) throws IOException
  {
    this.name = name;
    this.pinNumber = pinNumber;

    //J--
    GPIOPinConfig config = new GPIOPinConfig.Builder()
            .setControllerNumber(0)
            .setPinNumber(pinNumber).setDirection(GPIOPinConfig.DIR_INPUT_ONLY)
            .setDriveMode(GPIOPinConfig.MODE_INPUT_PULL_UP)
            .setTrigger(GPIOPinConfig.TRIGGER_FALLING_EDGE)
            .setInitValue(true)
            .build();
    //J++

    gpioPin = DeviceManager.open(GPIOPin.class, config);
    debounceTimestamp = System.currentTimeMillis();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   * @param gpioPin
   *
   * @return
   *
   * @throws IOException
   */
  public static GpioButtonHandler createInstance(String name, int gpioPin)
    throws IOException
  {
    LOGGER.debug("Listening on pin " + gpioPin);

    GpioButtonHandler handler = new GpioButtonHandler(name, gpioPin);

    handler.gpioPin.setInputListener(handler);

    return handler;
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  public synchronized void addButtonPressedListener(GpioButtonListener listener)
  {
    listeners.add(listener);
  }

  /**
   * Method description
   *
   */
  public synchronized void removeAllButtonPressedListeners()
  {
    listeners.clear();
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  public synchronized void removeButtonPressedListener(
    GpioButtonListener listener)
  {
    listeners.remove(listener);
  }

  @Override
  public void valueChanged(PinEvent pe)
  {
    long currentTimestamp = System.currentTimeMillis();

    if ((currentTimestamp - debounceTimestamp) > DEBOUNCING_TIME
      && (pe.getValue() == false))
    {
      LOGGER.debug("GPIO pin {} button pressed", pinNumber);

      synchronized (this)
      {
        listeners.parallelStream().forEach(l -> l.buttonPressed(
          new GpioButtonEvent(name,
            pinNumber,
            gpioPin)));
      }

      debounceTimestamp = currentTimestamp;
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private long debounceTimestamp;

  /** Field description */
  @Getter
  private final GPIOPin gpioPin;

  /** Field description */
  private final HashSet<GpioButtonListener> listeners = new HashSet<>();

  /** Field description */
  @Getter
  private final String name;

  /** Field description */
  @Getter
  private final int pinNumber;
}
