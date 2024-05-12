/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package l9g.photobox2;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static l9g.photobox2.AppState.PRINTINGFAILED;
import l9g.photobox2.service.PrinterService;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author th
 */
@Slf4j
public class MouseHandler extends MouseAdapter
{

  public MouseHandler(
    Rectangle noButtonRectangle, Rectangle yesButtonRectangle,
    ApplicationCommands applicationCommands,
    PrinterService printerService)
  {
    this.yesButtonRectangle = yesButtonRectangle;
    this.noButtonRectangle = noButtonRectangle;
    this.applicationCommands = applicationCommands;
    this.printerService = printerService;
  }

  //~--- methods --------------------------------------------------------------
  @Override
  public void mouseClicked(MouseEvent e)
  {
    log.debug("Mouse clicked at ({}|{})", e.getX(), e.getY());

    boolean yesButtonClicked = yesButtonRectangle.contains(e.getX(), e.getY());
    boolean noButtonClicked = noButtonRectangle.contains(e.getX(), e.getY());

    if (yesButtonClicked)
    {
      log.debug("'Yes'-button clicked");
      yesButtonTimestamp = System.currentTimeMillis();
    }

    if (noButtonClicked)
    {
      log.debug("'No'-button clicked");
    }

    switch (AppState.getState())
    {
      case READY:
      case STANDBY:
        if (noButtonClicked
          && (System.currentTimeMillis() - yesButtonTimestamp) <= 3000)
        {
          log.info("Prepare shutdown...");
          AppState.setState(AppState.PRESHUTDOWN);
        }
        break;

      case PRINTQUESTION:
        if (noButtonClicked)
        {
          AppState.setState(AppState.STANDBY);
        }
        if (yesButtonClicked)
        {
          if (printerService.isEnabled())
          {
            AppState.setState(AppState.YESPRINT);
          }
          else
          {
            AppState.setState(AppState.STANDBY);
          }
        }
        break;

      case PRINTINGFAILED:
        if (yesButtonClicked)
        {
          printerService.cancelAllPrintJobs();
          printerService.enablePrintQueue();
          if (printerService.checkPrinter())
          {
            AppState.setState(AppState.STANDBY);
          }
        }
        break;

      case ERROR:
        if (yesButtonClicked)
        {
          AppState.setState(AppState.STANDBY);
        }
        break;

      case PRESHUTDOWN:
        if (yesButtonClicked)
        {
          System.exit(0);
        }
        if (noButtonClicked)
        {
          AppState.setState(AppState.STANDBY);
        }
        break;

      default:
    }
  }

  private final ApplicationCommands applicationCommands;

  private final PrinterService printerService;

  private final Rectangle noButtonRectangle;

  private final Rectangle yesButtonRectangle;

  private long yesButtonTimestamp;
}
