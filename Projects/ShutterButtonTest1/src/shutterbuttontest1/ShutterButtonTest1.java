/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shutterbuttontest1;

import java.io.IOException;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

/**
 *
 * @author th
 */
public class ShutterButtonTest1
{

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException, InterruptedException
  {
    System.out.println( "Shutter Button Test 1");
    
    /*
        //J--
    GPIOPinConfig config = new GPIOPinConfig.Builder()
            .setControllerNumber(DeviceConfig.UNASSIGNED)
            .setPinNumber(5).setDirection(GPIOPinConfig.DIR_OUTPUT_ONLY)
            .setDriveMode(GPIOPinConfig.MODE_OUTPUT_PUSH_PULL)
            .setInitValue(false)
            .build();
    //J++

    GPIOPin pin = DeviceManager.open(GPIOPin.class, config);

    for (int i = 0; i < 5; i++)
    {
      pin.setValue(true);
      System.out.println("GPIO pin is now HIGH");
      Thread.sleep(2000);
      pin.setValue(false);
      System.out.println("GPIO pin is now LOW");
      Thread.sleep(2000);
    }

    */
    
    
    //J--
    GPIOPinConfig config = new GPIOPinConfig.Builder()
            .setControllerNumber(DeviceConfig.UNASSIGNED)
            .setPinNumber(5).setDirection(GPIOPinConfig.DIR_INPUT_ONLY)
            .setDriveMode(GPIOPinConfig.MODE_INPUT_PULL_UP)
            .build();
    //J++

    GPIOPin pin = DeviceManager.open(GPIOPin.class, config);

    for (int i = 0; i < 5; i++)
    {
      System.out.println("GPIO pin 5 is " + pin.getValue() );
      Thread.sleep(2000);
    }
  }
  
}
