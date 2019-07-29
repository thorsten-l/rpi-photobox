package l9g.photobox.gphoto2;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Class description
 *
 *
 * @version        $version$, 18/08/19
 * @author         Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
public class GPhoto2Exception extends IOException
{

  /**
   * Constructs ...
   *
   */
  public GPhoto2Exception() {}

  /**
   * Constructs ...
   *
   *
   * @param message
   */
  public GPhoto2Exception(String message)
  {
    super(message);
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   * @param ex
   */
  public GPhoto2Exception(String message, Exception ex)
  {
    super(message, ex);
  }
}
