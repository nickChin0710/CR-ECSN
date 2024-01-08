/**
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 110-01-08  V1.00.02  tanwei        修改意義不明確變量
 * 2023-0414    JH    rdStatus
 * 2023-0421    JH    ++rmReason
 * 2023-0428    JH    ++rdsPcard
 */
package ecsfunc;

public class DeCodeRdsm extends DeCodeBase {

  //-優惠別-
public static String rdsPcard(String strName) {
  return rdsPcard(strName, false);
}
public static String rdsPcard(String strName, boolean flag) {
  // 0.停用 1. 1.新增車號 2.2.變更車號 3.3.取消車號
  String[] dbCode = {"I", "P", "V", "L"};
  String[] dbText = {"50公里免費拖吊", "50公里免費拖吊+機停60天", "30公里免費拖吊", "租賃車"};
  if (flag) {
    return ddlbOption(dbCode, dbText, strName);
  }

  if (strName == null || strName.trim().length() == 0) {
    return "";
  }

  return commString.decode(strName, dbCode, dbText);
}
//==========
  public static String rdStatus(String strName) {
    return rdStatus(strName, false);
  }

  public static String rdStatus(String strName, boolean flag) {
    // 0.停用 1. 1.新增車號 2.2.變更車號 3.3.取消車號
    String[] cardVal = {"0", "1", "2", "3", "4"};
    String[] cardName = {"停用", "新增車號", "變更車號", "取消車號", "未啟用"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String rdStoprsn(String strName) {
    return rdStoprsn(strName, false);
  }

  public static String rdStoprsn(String strName, boolean flag) {
    //         <option value="1" ${rd_stoprsn-1} >&nbsp;1.到期不續購 &nbsp;</option>
    //         <option value="2" ${rd_stoprsn-2} >&nbsp;2.消費不足暫停服務 &nbsp;</option>
    //         <option value="3" ${rd_stoprsn-3} >&nbsp;3.卡片已為無效卡 &nbsp;</option>
    //         <option value="4" ${rd_stoprsn-4} >&nbsp;4.卡友來電要求停用 &nbsp;</option>
    //         <option value="5" ${rd_stoprsn-5} >&nbsp;5.金卡提升為白金卡 &nbsp;</option>
    //         <option value="6" ${rd_stoprsn-6} >&nbsp;6.未達年度續用標準 &nbsp;</option>
    //         <option value="7" ${rd_stoprsn-7} >&nbsp;7.無車號且非自動登錄卡 &nbsp;</option>
    //         <option value="8" ${rd_stoprsn-8} >&nbsp;8.更換卡號(停用此卡) &nbsp;</option>
    String[] colVal = {"1", "2", "3", "4", "5", "6", "7","8"};
    String[] colTxt =
        {"到期不續購", "消費不足暫停服務", "卡片已為無效卡", "卡友來電要求停用"
                , "金卡提升為白金卡", "未達年度續用標準", "無車號且非自動登錄卡","更換卡號(停用此卡)"
        };
    if (flag) {
      return ddlbOption(colVal, colTxt, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, colVal, colTxt);
  }

public static String rmReason(String strName) {
  return rmReason(strName, false);
}
public static String rmReason(String strName, boolean flag) {
  //'1','到期不續購','2','消費不足暫停服務','3','卡片已為無效卡','4','卡友來電要求停用'
  // ,'5','金卡提升為白金卡','6','未達年度續用標準','7','無車號且非自動登錄卡') as tt_rm_reason
  String[] colVal = {"1", "2", "3", "4", "5", "6", "7","8"};
  String[] colTxt =
          {"到期不續購", "消費不足暫停服務", "卡片已為無效卡", "卡友來電要求停用"
                  , "金卡提升為白金卡", "未達年度續用標準", "無車號且非自動登錄卡"
                  ,"更換卡號(停用此卡)"
          };
  if (flag) {
    return ddlbOption(colVal, colTxt, strName);
  }

  if (strName == null || strName.trim().length() == 0) {
    return "";
  }

  return commString.decode(strName, colVal, colTxt);
}

}
