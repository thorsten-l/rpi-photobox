/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package l9g.photobox.cv;

//~--- non-JDK imports --------------------------------------------------------
import l9g.photobox.AppState;
import l9g.photobox.Util;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import l9g.photobox.Config;

/**
 *
 * @author th
 */
public class MouseHandler extends MouseAdapter
{

  /**
   * Field description
   */
  private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(
    MouseHandler.class.getName());

  //~--- constructors ---------------------------------------------------------
  /**
   * Constructs ...
   *
   *
   * @param yesButtonRectangle
   * @param noButtonRectangle
   */
  public MouseHandler(Rectangle yesButtonRectangle, Rectangle noButtonRectangle)
  {
    this.yesButtonRectangle = yesButtonRectangle;
    this.noButtonRectangle = noButtonRectangle;
  }

  //~--- methods --------------------------------------------------------------
  @Override
  public void mouseClicked(MouseEvent e)
  {
    LOGGER.debug("Mouse clicked at ({}|{})", e.getX(), e.getY());

    boolean yesButtonClicked = yesButtonRectangle.contains(e.getX(), e.getY());
    boolean noButtonClicked = noButtonRectangle.contains(e.getX(), e.getY());

    if (yesButtonClicked)
    {
      LOGGER.debug("'Yes'-button clicked");
      yesButtonTimestamp = System.currentTimeMillis();
    }

    if (noButtonClicked)
    {
      LOGGER.debug("'No'-button clicked");
    }

    switch (AppState.getState())
    {
      case READY:
      case STANDBY:
        if (noButtonClicked
          && (System.currentTimeMillis() - yesButtonTimestamp) <= 1000)
        {
          LOGGER.info("Prepare shutdown...");
          AppState.setState(AppState.PRESHUTDOWN);
        }

        break;

      case PRINTQUESTION:
        if (noButtonClicked)
        {
          AppState.setState(AppState.NOPRINT);
        }

        if (yesButtonClicked)
        {
          if (Config.getInstance().isPrintingDisabled() == false)
          {
            AppState.setState(AppState.YESPRINT);
            Util.printPicture();
          }
          else
          {
            LOGGER.info("Printing disabled");
          }
        }

        break;

      case PRINTINGFAILED:

        if (yesButtonClicked)
        {
          AppState.setState(AppState.STANDBY);
        }

        break;

      default:
    }
  }

  //~--- fields ---------------------------------------------------------------
  /**
   * Field description
   */
  private final Rectangle noButtonRectangle;

  /**
   * Field description
   */
  private final Rectangle yesButtonRectangle;

  /**
   * Field description
   */
  private long yesButtonTimestamp;
}
