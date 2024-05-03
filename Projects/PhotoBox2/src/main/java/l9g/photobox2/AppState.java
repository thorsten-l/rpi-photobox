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
package l9g.photobox2;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Enum description
 *
 */
@Slf4j
public enum AppState
{
  UNDEFINED, STARTUP, STANDBY, PRESHUTDOWN, SHUTDOWN, STARTCOUNTDOWN,
  COUNTDOWN, TAKEPICTURE, TAKEPICTURE2, LOADPICTURE, PRINTQUESTION,
  PRINTPICTURE, ERROR, NOPRINT, YESPRINT, PRINTINGFAILED, YESPF, READY;

  /**
   * Field description
   */
  @Getter
  private static AppState state = UNDEFINED;

  /**
   * Field description
   */
  @Getter
  private static long stateChangedTimestamp;

  @Getter
  private static String message;

  //~--- set methods ----------------------------------------------------------
  /**
   * Method description
   *
   *
   * @param newState
   */
  public static void setState(AppState newState)
  {
    setState(newState, "");
  }

  public static void setState(AppState newState, String newMessage)
  {
    if ( state == STARTUP )
    {
      log.info( "startup time = {}s", 
        ((double)System.currentTimeMillis()
          -PhotoBox2Application.START_TIMESTAMP)/1000.0);
    }
    state = newState;
    message = newMessage;
    stateChangedTimestamp = System.currentTimeMillis();
    log.info("Application state = {} ({})", state.toString(), newMessage);
  }
}
