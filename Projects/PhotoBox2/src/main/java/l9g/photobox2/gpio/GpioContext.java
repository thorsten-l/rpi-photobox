package l9g.photobox.gpio;

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
  }
 
  public static Context getPi4JContext()
  {
    log.debug("getPi4JContext");
    return singleton.pi4j;
  }
}
