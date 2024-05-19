package l9g.gpiotest.gpio;

//~--- non-JDK imports --------------------------------------------------------
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

import lombok.Getter;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;

/**
 *
 * @author th
 */
public class GpioOutput
{

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
  }

  public void setValue(boolean value) throws IOException
  {
    gpioPin.setValue(value);
  }

  public boolean getValue() throws IOException
  {
    return gpioPin.getValue();
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
}
