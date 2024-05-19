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
    setState( newState, "" );
  }
  
  public static void setState(AppState newState, String newMessage)
  {
    state = newState;
    message = newMessage;
    stateChangedTimestamp = System.currentTimeMillis();
    LOGGER.info("Application state = {} ({})", state.toString(), newMessage);
  }
}
