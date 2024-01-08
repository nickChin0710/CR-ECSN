/**
 *  代碼說明-CRD V.2018-1221
*   2018-1221:  JH    current_code2
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-08-24  V1.00.02  Wilson      更換卡號停用 -> 其他停用                                                                    *
* 109-11-02  v1.00.03  Sunny       修改idnoFromMark定義，Apply(新進件)，Cnv(資料轉置)*
* 109-12-14  V1.00.04  Wilson      修改idnoFromMark定義，Bank(銀行主機)           *
* 109-12-31  V1.00.04  Justin        tsccCurrentCode  -> electronicCurrentCode and remove currentCode2
* 110-01-08  V1.00.05  tanwei        修改意義不明確變量                                                                          * 
* */

package ecsfunc;

public class DeCodeCrd extends DeCodeBase {

  public static String idnoSex(String strName) {
    if (strName == null || strName.trim().length() == 0) {
      return "";
    }
    String[] cardVal = {"1", "2"};
    String[] cardName = {"男", "女"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String debitFlag(String strName) {
    if (strName == null || strName.trim().length() == 0) {
      return "";
    }
    String[] cardVal = {"Y", "N"};
    String[] cardName = {"debit卡", "信用卡"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String idnoFromMark(String strName) {
    if (strName == null || strName.trim().length() == 0) {
      return "";
    }
    String[] cardVal = {"I", "M", "W", "A", "B", "N", "C", "J"};
    String[] cardName =
        {"IBM(網路銀行)", "ECS(人工修改)", "Web(信用卡網站)", "Apply(新進件)", "Bank(銀行主機)", "IBM(網銀後台管理系統)",
            "Cnv(資料轉置)","0CJ0(批次變更)"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String supFlag(String strName) {
    if (strName == null || strName.trim().length() == 0) {
      return "";
    }
    String[] cardVal = {"0", "1"};
    String[] cardName = {"正卡", "附卡"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String currentCode(String strName) {
    return currentCode(strName, false);
  }

  public static String currentCode(String strName, boolean flag) {
    // 0.正常, 1.申停, 2.掛失, 3.強制, 4.其他停用, 5.偽卡
    String[] cardVal = {"0", "1", "2", "3", "4", "5"};
    String[] cardName = {"正常", "申停", "掛失", "強制停用", "其他停用", "偽卡"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String cardNote(String strName) {
    return cardNote(strName, false);
  }

  public static String cardNote(String strName, boolean flag) {
    String[] cardVal = {"C", "G", "P", "S", "I", "*"};
    String[] cardCard = {"普卡", "金卡", "白金卡", "卓越卡", "頂級卡", "通用"};
    if (flag) {
      return ddlbOption(cardVal, cardCard, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardCard);
  }

  public static String cardNote2(String strName) {
    return cardNote2(strName, false);
  }

  public static String cardNote2(String strName, boolean flag) {
    String[] cardVal = {"*", "C", "G", "P", "S", "I"};
    String[] cardName = {"通用", "普卡", "金卡", "白金卡", "卓越卡", "頂級卡"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String electronicCurrentCode(String strName) {
    return electronicCurrentCode(strName, false);
  }

  public static String electronicCurrentCode(String strName, boolean flag) {
    // 0.正常, 1.申停, 2.掛失, 3.強制, 4.其他停用, 5.偽卡
    String[] cardVal = {"0", "1", "2", "3", "4", "5", "6", "7"};
    String[] cardName = {"正常", "申停", "掛失", "強制停用", "其他停用", "偽卡", "毀損停用", "續卡停用"};
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
