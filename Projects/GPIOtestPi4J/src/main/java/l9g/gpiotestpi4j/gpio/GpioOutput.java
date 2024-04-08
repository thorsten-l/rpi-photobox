package l9g.gpiotestpi4j.gpio;

import com.pi4j.io.gpio.digital.DigitalOutput;
import lombok.Getter;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author th
 */
@Slf4j
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
    gpioPin = GpioContext.getPi4JContext().digitalOutput().create(pinNumber);
  }

  public void setValue(boolean value) throws IOException
  {
    log.debug("setValue({})", value);
    gpioPin.setState(value);
  }

  public boolean getValue() throws IOException
  {
    return gpioPin.isHigh();
  }

  //~--- fields ---------------------------------------------------------------
 
  @Getter
  private final DigitalOutput gpioPin;

  @Getter
  private final String name;

  @Getter
  private final int pinNumber;
}
