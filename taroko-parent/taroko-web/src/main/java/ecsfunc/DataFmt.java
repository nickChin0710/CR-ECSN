/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          *   
******************************************************************************/
package ecsfunc;

public class DataFmt {
  protected static taroko.base.CommString commString = new taroko.base.CommString();

  public static String toNumber4(String strName) {
    // -4小數-
    return String.format("%,.4f", commString.strToNum(strName));
  }

  public static String toNumber2(String strName) {
    // -2小數-
    return String.format("%,.2f", commString.strToNum(strName));
  }

  public static String toNumber0(String strName) {
    // -2小數-
    return String.format("%,.0f", commString.strToNum(strName));
  }

}
