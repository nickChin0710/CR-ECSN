/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
*  110-01-08  V1.00.02  tanwei     修改意義不明確變量                                                                         *   
******************************************************************************/
package ofcapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import taroko.base.CommString;

@SuppressWarnings({"unchecked", "deprecation"})
public class BaseClass extends taroko.base.BaseSQL {

  public String getSysdate() {
    String dateStr = "";
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    dateStr = form1.format(currDate);

    return dateStr.substring(0, 8);
  }

  public boolean notEmpty(String strName) {
    return (!empty(strName));
  }

  public boolean empty(String strName) {
    return commString.empty(strName);
  }

  public String nvl(String strName, String colName) {
    return commString.nvl(strName, colName);
  }

  public boolean isNumber(String strName) {
    return commString.isNumber(strName);
  }

  public String nvl(String strName) {
    return commString.nvl(strName);
  }

  // -data-Conert-------------

  public String intToStr(int num1) {
    return commString.intToStr(num1);
  }

  public String strMid(String strName, int indx1, int len1) {
    return commString.mid(strName, indx1, len1);
  }

  public String numToStr(double num1, String fmt1) {
    if (empty(fmt1)) {
      return commString.numFormat(num1, "#,##0");
    }
    return commString.numFormat(num1, fmt1);
  }

  public double toNum(String strName) {
    return commString.strToNum(strName);
  }

  public boolean eqIgno(String strName, String colName) {
    return commString.eqIgno(strName, colName);
  }

  public boolean eqAny(String strName, String colName) {
    return commString.eqIgno(strName, colName);
  }

  public boolean neIgno(String strName, String colName) {
    return !eqIgno(strName, colName);
  }

  public boolean neAny(String strName, String colName) {
    return !eqAny(strName, colName);
  }

  public int pos(String strName, String colName) {
    return commString.pos(strName, colName);
  }

  public int posAny(String strName, String colName) {
    return commString.posAny(strName, colName);
  }

  public void println(String strName) {
    System.out.println("-JJJ->" + this.getClass().getSimpleName() + ":" + strName);
  }

}
