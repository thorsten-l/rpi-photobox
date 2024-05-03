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
