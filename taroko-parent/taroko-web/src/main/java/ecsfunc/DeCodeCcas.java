/**
 * 2019-1209  V1.00.01  Alex  remove 0 正常

*  109-04-17  V1.00.01  Zuwei       updated for project coding standard     *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard                          *
 * 109-10-14  V1.00.02  Wilson     停掛類別刪除"凍結"                            *
 * 110-01-08  V1.00.03  tanwei        修改意義不明確變量                       
 * 111-01-20  V1.00.04  Justin     fix (Code Correctness: Hidden Method)    * 
 */
package ecsfunc;

public class DeCodeCcas extends DeCodeBase {

  public static String onusOpptype(String strName) {
    return onusOpptype(strName, false);
  }

  public static String onusOpptype(String strName, boolean flag) {
    String[] cardVal = {"1", "2", "3", "4", "5"};
    String[] cardName = {"一般停用", "掛失停用", "強制停用", "其他停用", "偽卡停用"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String onusOpptype(String strName, String falg) {
    String[] cardVal = {"1", "2", "3", "4", "5"};
    String[] cardName = {"一般停用", "掛失停用", "強制停用", "其他停用", "偽卡停用"};
    if (falg.length() > 0) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  // -NCCC_OPP_TYPE-
  public static String areaType(String strName) {
    return areaType(strName, false);
  }

  public static String areaType(String strName, boolean falg) {
    // **adj_area: 1.國外 2.國內 3.國內外
    String[] cardVal = {"1", "2", "3"};
    String[] cardName = {"國外", "國內", "國內外"};

    if (falg) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  // -NCCC_OPP_TYPE-
  public static String ncccOpptype(String strName) {
    return ncccOpptype(strName, false);
  }

  public static String ncccOpptype(String strName, boolean flag) {
    String[] cardVal = {"1", "2", "3", "4", "5"};
    String[] cardName = {"一般停用", "掛失停用", "強制停用", "其他停用", "偽卡停用"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }
  
  public static String onlineRedeem(String strName) {
		return deCode(strName,"A,分期(A),I,分期(I),E,分期(E),Z,分期(Z),0,紅利(0),1,紅利(1),2,紅利(2),3,紅利(3),4,紅利(4),5,紅利(5),6,紅利(6),7,紅利(7)");
	}
  
}
