package l9g.photobox;

//~--- non-JDK imports --------------------------------------------------------

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum description
 *
 */
public enum AppState
{
  UNDEFINED, STARTUP, STANDBY, PRESHUTDOWN, SHUTDOWN, STARTCOUNTDOWN,
    COUNTDOWN, TAKEPICTURE, TAKEPICTURE2, LOADPICTURE, PRINTQUESTION,
    PRINTPICTURE, ERROR, NOPRINT, YESPRINT, PRINTINGFAILED, YESPF, READY;

  /** Field description */
  private final static Logger LOGGER = LoggerFactory.getLogger(
    AppState.class.getName());

  /** Field description */
  @Getter
  private static AppState state = UNDEFINED;

  /** Field description */
  @Getter
  private static long stateChangedTimestamp;

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param newState
   */
  public static void setState(AppState newState)
  {
    state = newState;
    stateChangedTimestamp = System.currentTimeMillis();
    LOGGER.info("Application state = {}", state.toString());
  }
}
