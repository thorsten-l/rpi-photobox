package l9g.photobox.gpio;

/**
 * Interface description
 *
 *
 * @version        $version$, 18/08/19
 * @author         Dr. Thorsten Ludewig <t.ludewig@gmail.com>
 */
public interface GpioButtonListener
{

  /**
   * Method description
   *
   *
   * @param gbe
   */
  public void buttonPressed(GpioButtonEvent gbe);
}
