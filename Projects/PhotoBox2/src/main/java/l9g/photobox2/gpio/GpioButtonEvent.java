package l9g.photobox.gpio;

//~--- non-JDK imports --------------------------------------------------------
import com.pi4j.io.gpio.digital.DigitalInput;
import lombok.Getter;

/**
 *
 * @author Dr. Thorsten Ludewig <t.ludewig@gmail.com>
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
  GpioButtonEvent(String name, int pinNumber, DigitalInput gpioPin)
  {
    this.name = name;
    this.pinNumber = pinNumber;
    this.gpioPin = gpioPin;
  }

  //~--- fields ---------------------------------------------------------------
  @Getter
  private final DigitalInput gpioPin;

  @Getter
  private final String name;

  @Getter
  private final int pinNumber;
}
