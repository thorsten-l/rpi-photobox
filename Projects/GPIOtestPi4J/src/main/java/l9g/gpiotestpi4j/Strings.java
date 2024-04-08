package l9g.gpiotestpi4j;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class Strings
{
  private Strings(){}
  
  public static boolean isNullOrEmpty(String s)
  {
    return s == null || s.isEmpty();
  }
}
