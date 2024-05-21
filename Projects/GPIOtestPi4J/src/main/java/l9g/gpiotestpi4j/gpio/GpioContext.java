package l9g.gpiotestpi4j.gpio;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import java.util.logging.Level;
import java.util.logging.Logger;
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
      log.error("sleep 2s interrupted", ex);
    }
  }
  
  public static Context getPi4JContext()
  {
    log.debug("getPi4JContext");
    return singleton.pi4j;
  }
}
