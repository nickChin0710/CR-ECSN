/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          * 
* 112-04-17  V1.00.03  Alex       信用卡減值選項刪除                                                                            *
******************************************************************************/
package ecsfunc;

import taroko.base.CommString;

public class DeCodeTscc extends DeCodeBase {

  public static String currentCode(String strName) {
    return currentCode(strName, false);
  }

  public static String currentCode(String strName, boolean flag) {
    // 0.正常; 1.申停; 2.掛失; 3.強停; 4.其他停用; 5.偽卡; 6.毀損重製
    if (flag) {
      return ddlbOption(new String[] {"0", "1", "2", "3", "4", "5", "6"}, new String[] {"正常", "申停",
          "掛失", "強停", "其他停用", "偽卡", "毀損重製"}, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, new String[] {"0", "1", "2", "3", "4", "5", "6"}, new String[] {"正常",
        "申停", "掛失", "強停", "其他停用", "偽卡", "毀損重製"});
  }

  public static String closeReason(String strName) {
    return closeReason(strName, false);
  }

  public static String closeReason(String strName, boolean flag) {
    // close_reason: .未結案 1.信用卡加值 2.信用卡減值 3.現金加值 4.現金減值 ;
    if (flag) {
      return ddlbOption(new String[] {"1", "3", "4"}, new String[] {"信用卡加值", "現金加值",
          "現金減值"}, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, new String[] {"1", "3", "4"}, new String[] {"信用卡加值",
        "現金加值", "現金減值"});
  }


}
