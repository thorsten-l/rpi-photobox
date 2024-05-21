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

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
public class GpioContext
{
  private final Context pi4j;

  private static final GpioContext singleton = new GpioContext();

  private GpioContext()
  {
    log.debug("GpioContext");
    pi4j = Pi4J.newAutoContext();
    log.debug("wait 2s");
    try
    {
      Thread.sleep(2000);
    }
    catch (InterruptedException ex)
    {
      log.error("init 2s interrupted", ex);
    }
  }

  public static Context getPi4JContext()
  {
    log.debug("getPi4JContext");
    return singleton.pi4j;
  }
}
