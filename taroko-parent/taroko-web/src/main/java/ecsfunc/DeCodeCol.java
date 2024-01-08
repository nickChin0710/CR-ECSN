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

import taroko.base.CommString;

public class DeCodeCol extends DeCodeBase {


  public static String liaxStatus(String strName) {
    return liaxStatus(strName, false);
  }

  public static String liaxStatus(String strName, boolean flag) {
    // LIAB-1.停催 LIAB-2.復催 LIAB-3.協商成功 LIAC-1.受理申請 LIAC-2.停催通知 LIAC-3.簽約完成 LIAC-4.結案/復催 LIAC-5.正常結案
    if (flag) {
      return ddlbOption(new String[] {"LIAB-1", "LIAB-2", "LIAB-3", "LIAC-1", "LIAC-2", "LIAC-3",
          "LIAC-4", "LIAC-5"}, new String[] {"停催", "復催", "協商成功", "受理申請", "停催通知", "簽約完成", "結案/復催",
          "正常結案"}, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, new String[] {"LIAB-1", "LIAB-2", "LIAB-3", "LIAC-1", "LIAC-2",
        "LIAC-3", "LIAC-4", "LIAC-5"}, new String[] {"停催", "復催", "協商成功", "受理申請", "停催通知", "簽約完成",
        "結案/復催", "正常結案"});
  }
  
  public static String cpbdueType(String strName) {
	    String[] cardVal = {"1", "2", "3"};
	    String[] cardName = {"公會協商", "個別協商", "前置調解"};

	    if (strName == null || strName.trim().length() == 0) {
	      return "";
	    }

	    return commString.decode(strName, cardVal, cardName);
	  }
  
  public static String cpbdueCurrType(String strName) {
	    String[] cardVal = {"0" ,"1", "2", "3" , "4" , "5" , "6"};
	    String[] cardName = {"取消", "受理", "停催", "協商成立" , "復催" , "毀諾" , "還清"};

	    if (strName == null || strName.trim().length() == 0) {
	      return "";
	    }

	    return commString.decode(strName, cardVal, cardName);
	  }
  
  public static String cpbdueAcctType(String strName) {
	    String[] cardVal = {"01" ,"03"};
	    String[] cardName = {"一般卡", "商務卡"};

	    if (strName == null || strName.trim().length() == 0) {
	      return "";
	    }

	    return commString.decode(strName, cardVal, cardName);
	  }
  
  public static String cpbdueAcctStatus(String strName) {
	    String[] cardVal = {"1" ,"2" , "3" , "4"};
	    String[] cardName = {"正常", "逾放" , "催收" , "呆帳"};

	    if (strName == null || strName.trim().length() == 0) {
	      return "";
	    }

	    return commString.decode(strName, cardVal, cardName);
	  }
}
