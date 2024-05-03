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
