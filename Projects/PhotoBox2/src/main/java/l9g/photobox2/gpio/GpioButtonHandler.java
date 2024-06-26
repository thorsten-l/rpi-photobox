/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.photobox2.gpio;

//~--- non-JDK imports --------------------------------------------------------
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import com.pi4j.io.gpio.digital.PullResistance;
import java.io.Closeable;
import lombok.Getter;


//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;

import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;

/**
 * Class description
 *
 *
 * @version $version$, 18/08/19
 * @author Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Slf4j
public class GpioButtonHandler implements DigitalStateChangeListener
{

  /**
   * Field description
   */
  private final static long DEBOUNCING_TIME = 200;    // in ms
  
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
    log.debug("GpioButtonHandler({}, {})", name, pinNumber);
    this.name = name;
    this.pinNumber = pinNumber;

    DigitalInputConfigBuilder gpioConfig
      = DigitalInput.newConfigBuilder(GpioContext.getPi4JContext())
        .id("button_" + name)
        .name(name)
        .address(pinNumber)
        .pull(PullResistance.PULL_UP);

    gpioPin = GpioContext.getPi4JContext().create(gpioConfig);
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
    log.debug("Listening on pin " + gpioPin);
    GpioButtonHandler handler = new GpioButtonHandler(name, gpioPin);
    handler.gpioPin.addListener(handler);
    return handler;
  }

  /**
   * @param listener
   */
  public synchronized void addButtonPressedListener(GpioButtonListener listener)
  {
    listeners.add(listener);
  }

  /**
   * Method description
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
  public void onDigitalStateChange(DigitalStateChangeEvent dsce)
  {
    log.trace("onDigitalStateChange");
    long currentTimestamp = System.currentTimeMillis();

    if ((currentTimestamp - debounceTimestamp) > DEBOUNCING_TIME
      && (gpioPin.isLow()))
    {
      log.debug("GPIO pin {} button pressed", pinNumber);

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

  public void shutdown() throws IOException
  {
    GpioContext.getPi4JContext().shutdown();
  }

  //~--- fields ---------------------------------------------------------------
  /**
   * Field description
   */
  private long debounceTimestamp;

  /**
   * Field description
   */
  @Getter
  private final DigitalInput gpioPin;

  /**
   * Field description
   */
  private final HashSet<GpioButtonListener> listeners = new HashSet<>();

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
