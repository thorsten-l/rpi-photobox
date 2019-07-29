package l9g.gpiotest.gpio;

//~--- non-JDK imports --------------------------------------------------------

import jdk.dio.gpio.GPIOPin;

import lombok.Getter;

/**
 * Class description
 *
 *
 * @version        $version$, 18/08/19
 * @author         Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
public class GpioButtonEvent
{

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param pinNumber
   * @param gpioPin
   */
  GpioButtonEvent(String name, int pinNumber, GPIOPin gpioPin)
  {
    this.name = name;
    this.pinNumber = pinNumber;
    this.gpioPin = gpioPin;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Getter
  private final GPIOPin gpioPin;

  /** Field description */
  @Getter
  private final String name;

  /** Field description */
  @Getter
  private final int pinNumber;
}
