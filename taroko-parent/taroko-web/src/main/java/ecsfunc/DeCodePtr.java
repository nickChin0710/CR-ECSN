/**
 * 2017/05/15   Alex    add curr_code
 *  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
 * 109-04-20  V1.00.01  Zuwei       code format                              *
 * 110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          * 
 * */
package ecsfunc;

import taroko.base.CommString;

public class DeCodePtr extends DeCodeBase {

  // -卡人等級對象-
  public static String classCodeType(String strName) {
    // 1.商務卡總繳, 9.一般卡友

    String[] cardVal = {"1", "2", "9"};
    String[] cardName = {"商務卡總繳", "VIP", "一般卡友"};
    if (strName == null || strName.trim().length() == 0) {
      return "";
    }
    return commString.decode(strName, cardVal, cardName);
  }

  public static String binType(String strName) {
    return binType(strName, false);
  }

  public static String binType(String strName, boolean flag) {
    String[] cardVal = {"V", "M", "J", "N"};
    String[] cardName = {"VISA", "MasterCard", "JCB", "聯合信用卡"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String currCode(String strName) {
    return currCode(strName, false);
  }

  public static String currCode(String strName, boolean flag) {
    String[] cardVal = {"901", "392", "840"};
    String[] cardName = {"台幣", "日幣", "美金"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String acctType(String strName) {
    // 1.本行嬴, 2.本行輸, 3.其他
    return commString.decode(strName, new String[] {"01", "02", "03", "05", "06"}, new String[] {"一般卡",
        "商務卡", "採購卡", "歡喜卡", "AE利多卡"});
  }

}
