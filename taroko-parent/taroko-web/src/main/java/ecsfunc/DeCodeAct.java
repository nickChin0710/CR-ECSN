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

public class DeCodeAct extends DeCodeBase {

  public static String acctStatus(String strName) {
    return acctStatus(strName, false);
  }

  public static String acctStatus(String strName, boolean flag) {
    // 1.1 - 正常 2.2 - 逾放 3.3 - 催收 4.4 - 呆帳 5.5 - 結清;
    String[] cardVal = {"1", "2", "3", "4", "5"};
    String[] cardName = {"正常", "逾放", "催收", "呆帳", "結清"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String acnoFlag(String strName) {
    return acnoFlag(strName, false);
  }

  public static String acnoFlag(String strName, boolean flag) {
    // 1.一般卡, 2.總繳公司戶, 3.商務卡個繳, Y.總繳個人;
    String[] cardVal = {"1", "2", "3", "Y"};
    String[] cardName = {"一般卡", "總繳公司戶", "商務卡個繳", "總繳個人"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

}
