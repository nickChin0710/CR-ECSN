/*****************************************************************************
* * MODIFICATION LOG * 
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-17   V1.00.01  Zuwei         updated for project coding standard      *
* 109-04-20   V1.00.01  Zuwei          code format                              *
* 109-09-03   V1.00.02  JustinWu    add educationCode, marriageCode, and billApplyFlagCode
* 109-09-16   V1.00.03  JustinWu    remove acctStatusCode, educationCode
* 109-12-31    V1.00.04 JustinWu    add getOEMPayCardTypeDesc and getBlockStatusDesc
* 110-01-08  V1.00.05  tanwei        修改意義不明確變量 
* 110-03-25    V1.00.06 JustinWu    return OEM Pay if wallet_id is empty         
* 110-10-07   V1.00.07  JustinWu    中止 -> 終止                             * 
* 111-06-16   V1.00.08  JustinWu    3.中止(實體卡停卡) => 3.中止(已停用)     *
* 111-11-17   V1.00.09  Sunny       調整帳單註記1-4定義     *
* 111-11-21   V1.00.09  ZuweiSu     add default Constructor                  *
* 111-11-21   V1.00.10  Sunny       調整caseConfFlag案件簽核文字：退回                  *
******************************************************************************/
package ecsfunc;

import taroko.com.TarokoCommon;

public class DeCodeCms extends DeCodeBase {
	
	public DeCodeCms() {
	}
	
	public DeCodeCms(TarokoCommon wp) {
		
		
	}

  public static String ppOppostReason(String strName) {
    // 'P1','Lost(L遺失)','P2','Stolen(S)','P3','Cancelled(CA)','P4','Not_Received(NR)'
    return commString.decode(strName, new String[] {"P1", "P2", "P3", "P4"}, new String[] {"Lost(L遺失)",
        "Stolen(S)", "Cancelled(CA)", "Not_Received(NR)"});

  }

  public static String caseProcResult(String strName) {
    // proc_result: 0.未處理 5.處理中 9.處理完成
    String[] cardVal = {"0", "5", "9"};
    String[] cardName = {"未處理", "處理中", "處理完成"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String caseConfFlag(String strName) {
    // case_conf_flag: V.需簽核 N.免簽核 Y.已簽核
    return commString.decode(strName, new String[] {"V", "N", "Y", "R"}, new String[] {"需簽核", "免簽核", "已簽核",
        "退回"});
  }

  /*
   * status_code: ; 0.正常; 1.暫停; 2.終止(人工); 3.終止(已停用); 4.重複取消; 5.終止(已過效期)
   */
  public static String tpanStatusCode(String strName) {
    return tpanStatusCode(strName, false);
  }

  public static String tpanStatusCode(String strName, boolean flag) {
    // 0.正常; 1.暫停; 2.終止(人工); 3.終止(已停用); 4.重複取消; 5.終止(已過效期)
    if (flag) {
      return ddlbOption(new String[] {"0", "1", "2", "3", "4", "5"}, new String[] {"正常", "暫停",
          "終止(人工)", "終止(已停用)", "重複取消", "終止(已過效期)"}, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, new String[] {"0", "1", "2", "3", "4", "5"}, new String[] {"正常", "暫停",
        "終止(人工)", "終止(已停用)", "重複取消", "終止(已過效期)"});
  }

  public static String tpanChangeCode(String strName) {
    return tpanChangeCode(strName, false);
  }

  public static String tpanChangeCode(String strName, boolean flag) {
    // 1.1.暫停; 2.2.暫停恢復; 3.3.終止(人工)
    if (flag) {
      return ddlbOption(new String[] {"1", "2", "3"}, new String[] {"暫停", "暫停恢復", "終止(人工)"}, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, new String[] {"1", "2", "3"}, new String[] {"暫停", "暫停恢復", "終止(人工)"});
  }
  
  public static String marriageCode(String marriage) {
	  switch(marriage) {
		case "1":
			return "已婚";
		case "2":
			return "未婚";
		default:
			return "";
		}
  }
  
  public static String billApplyFlagCode(String billApplyFlag) {
	  switch(billApplyFlag) {
		case "1":
			return "同戶籍";
		case "2":
			return "同居住";
		case "3":
			return "同公司";
		case "4":
			return "其他";
//		case "4":
//			return "同通訊(法人)";
//		case "5":
//			return "同公司(法人)";
		default:
			return "";
		}
  }

	public static String majorRelationCode(String majorRelation) {
		switch (majorRelation) {
		case "1":
			return "配偶";
		case "2":
			return "父母";
		case "3":
			return "子女";
		case "4":
			return "兄弟姊妹";
		case "5":
			return "法人";
		case "6":
			return "其他";
		case "7":
			return "配偶父母";
		default:
			return "";
		}

	}

	public String getWalletIdDesc(String walletId) {
		if (walletId == null) return ""; 
		switch (walletId) {
		case "101":
			return "WALLET REMOTE";
		case "102":
			return "NFC";
		case "103":
			return "Apple Pay";
		case "216":
			return "Android Pay";
		case "217":
			return "Samsung Pay";
		default:
			return "OEM Pay";
		}
		
	}

	public static String getBlockStatusDesc(String blockStatus) {
		switch (blockStatus) {
		case "11":
			return "人工禁超";
		case "12":
			return "系統禁超";
		case "21":
			return "人工解超";
		case "22":
			return "人工解超";
		default:
			return "無任何禁解超記錄";
		}

	}
}
