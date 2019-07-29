/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shutterbuttontest1;

import java.io.IOException;
import java.util.Scanner;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

/**
 *
 * @author th
 */
public class ShutterButtonListenTest
{

  static long debounceTimestamp = 0;
  
  public static void main(String[] args) throws IOException, InterruptedException
  {
    System.out.println("Shutter Button Listen Test 0.3");

    //J--
    GPIOPinConfig config = new GPIOPinConfig.Builder()
            .setControllerNumber(DeviceConfig.UNASSIGNED)
            .setPinNumber(5).setDirection(GPIOPinConfig.DIR_INPUT_ONLY)
            .setDriveMode(GPIOPinConfig.MODE_INPUT_PULL_UP)
            .setTrigger(GPIOPinConfig.TRIGGER_FALLING_EDGE)
            .build();
    //J++

    GPIOPin pin = DeviceManager.open(GPIOPin.class, config);

    pin.setInputListener(new PinListener()
    {
      @Override
      public void valueChanged(PinEvent pe)
      {
        boolean value = pe.getValue();
        
        if (( System.currentTimeMillis() - debounceTimestamp ) > 200 && value == false )
        {
          System.out.printf("%d ", System.currentTimeMillis());
          System.out.println(value);
          
          debounceTimestamp = System.currentTimeMillis();
        }
      }
    });

    new Scanner(System.in).next();
  }

}
