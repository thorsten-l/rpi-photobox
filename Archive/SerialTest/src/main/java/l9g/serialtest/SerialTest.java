package l9g.serialtest;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class SerialTest
{

  public static void main(String[] args) throws Throwable
  {
    SerialPort serial = SerialPort.getCommPort("/dev/ttyUSB0");
    serial.openPort();
    serial.setBaudRate(74880);
    serial.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
    Thread reader = new Thread(()->{
      System.out.println("Start input stream reader");
      InputStream is = serial.getInputStream();
      int c;
      try
      {
        while((c=is.read())>=0)        
        {
          System.out.write(c);
          System.out.flush();
        }
      }
      catch (IOException ex)
      {
        Logger.getLogger(SerialTest.class.getName()).log(Level.SEVERE, null, ex);
      }
    });
    reader.setDaemon(true);
    reader.start();
    Thread.sleep(1000);
    OutputStream os = serial.getOutputStream();
    PrintStream out = new PrintStream(os,true,"ASCII");
    out.println("/state");
    Thread.sleep(1000);    
  }
}
