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
/* 代碼說明[風管]
   2019-0506:  JH    ++ compl_times
 * 2018-1008:	JH		modify
 * 2018-04xx:	JH		modify
 * */

public class DeCodeRsk extends DeCodeBase {

  public static String acnoLogType(String strName) {
    return acnoLogType(strName, false);
  }

  public static String acnoLogType(String strName, boolean flag) {
    // 1.額度調整 2.強停 3.凍結 4.解凍, 5.授信不良戶, 6.特殊指示, A.授權卡人等級, B.授權Mcode
    if (flag) {
      return ddlbOption(new String[] {"1", "2", "3", "4", "5", "6", "A", "B"}, new String[] {
          "額度調整", "強停", "凍結", "解凍", "授信不良戶", "卡特指", "授權卡人等級", "授權Mcode"}, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, new String[] {"1", "2", "3", "4", "5", "6", "A", "B"}, new String[] {
        "額度調整", "強停", "凍結", "解凍", "授信不良戶", "卡特指", "授權卡人等級", "授權Mcode"});
  }

  public static String logReason(String strName) {
    String[] cardVal = {"A", "U", "D"};
    String[] cardName = {"新增", "修改", "刪除"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String logNotReason(String strName) {
    String[] cardVal = {"B1", "B2", "T1", "T4", "T2"};
    String[] cardName = {"永不凍結戶", "分期還款戶", "永不解凍戶", "同ID無法解凍", "其他帳戶未解凍"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String emendType(String strName) {
    return emendType(strName, false);
  }

  public static String emendType(String strName, boolean flag) {
    // 1.個人額度 2.商務卡公司額度 3.商務卡個人額度 4.子卡額度 5.預借現金額度
    if (flag) {
      return ddlbOption(new String[] {"1", "2", "3", "4", "5"}, new String[] {"個人額度", "商務卡公司額度",
          "商務卡個人額度", "子卡額度", "預借現金額度"}, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, new String[] {"1", "2", "3", "4", "5"}, new String[] {"個人額度",
        "商務卡公司額度", "商務卡個人額度", "子卡額度", "預借現金額度"});
  }

  public static String prbMark(String strName) {
    // prb_mark: Q.問交 S.特交 E.不合格
    String[] cardVal = {"Q", "S", "E"};
    String[] cardName = {"問交", "特交", "不合格"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String prbSrcCode(String strName) {
    return commString.decode(strName, new String[] {"SE", "SS", "SQ", "RQ", "CQ"}, new String[] {"系統列不合格",
        "系統列特交", "系統列問交", "風管列問交", "客服列問交"});
  }

  public static String prbStatus(String strName) {
    return commString.decode(strName, new String[] {"10", "30", "40", "50", "60", "80", "83", "85"},
        new String[] {"新增待覆核", "新增已覆核", "修改待覆核", "修改已覆核", "結案待覆核", "結案已覆核", "二次結案待覆核", "二次結案覆核"});
  }

  // -調單------------------------
  public static String reptType(String strName) {
    String[] cardVal = {"1", "2", "4"};
    String[] cardName = {"影本", "正本", "微縮影"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String reptStatus(String strName) {
    return commString.decode(strName, new String[] {"10", "30", "60", "80", "85"}, new String[] {"新增待覆核",
        "新增已覆核", "結案待覆核", "人工結案", "系統結案"});
  }

  public static String reptProcResult(String strName) {
    return commString.decode(strName, new String[] {"1", "2", "3"}, new String[] {"已回單", "無此單據", "未回單"});
  }

  // -------------------------------------------------
  // -扣款-
  public static String chgbChgStage(String strName) {
    // *chg_stage: 1.1.第一次扣款 2.2.再提示 3.3.第二次扣款 4.4.預備仲裁 5.5.仲裁;
    return commString.decode(strName, new String[] {"1", "2", "3", "4", "5"}, new String[] {"一扣", "再提示",
        "二扣", "預備仲裁", "仲裁"});
  }

  public static String chgbSubStage(String strName) {
    /*
     * sub_stage: 10.新增待覆核 30.新增已覆核 40.修改待覆核 50.修改已覆核 60.結案待覆核 80.結案已覆核 ;
     */
    String[] cardVal = new String[] {"10", "30", "40", "50", "60", "80"};
    String[] cardName = {"新增待覆核 ", "新增已覆核", "修改待覆核", "修改已覆核", "結案待覆核", "結案已覆核"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String chgbFinalClose(String strName) {
    /* final_close: C.人工結案 S.系統結案 ; */
    return commString.decode(strName, new String[] {"C", "S"}, new String[] {"人工結案", "系統結案"});

  }

  public static String arbitStatus(String strName) {
    /*
     * sub_stage: 10.新增待覆核 30.新增已覆核 40.修改待覆核 50.修改已覆核 60.結案待覆核 80.結案已覆核 ;
     */
    String[] cardVal = new String[] {"10", "30", "40", "50", "60", "80"};
    String[] cardName = {"新增待覆核 ", "新增已覆核", "仲裁待覆核", "仲裁已覆核", "結案待覆核", "結案已覆核"};
    return commString.decode(strName, cardVal, cardName);
  }
  
  public static String arbitPreResult(String strName) {
		return arbitPreResult(strName,false);
  }
  
  public static String arbitPreResult(String strName,boolean ddlb) {
	  String[] aaCode = new String[]{"11","12","13","21","22","23","24"};
	  String[] aaText = new String[]{"成功-收單行退款","商店退貨","持卡人自付","失敗-收單行拒絕","本行負擔","持卡人要求續做爭議","其他"};
		if (ddlb) {
			return ddlbOption(aaCode,aaText,strName);
		}

		if (strName==null || strName.trim().length()==0) {
			return "";
		}
		return commString.decode(strName,aaCode,aaText);
  }
  
  public static String arbitCloResult(String strName) {
		return arbitCloResult(strName,false);
	}
	public static String arbitCloResult(String strName, boolean ddlb) {		
		//1.本行嬴, 2.本行輸, 3.其他
		String[] aaCode=new String[]{"1","2","3"};
		String[] aaText=new String[]{"本行嬴","本行輸","其他"};
		if (ddlb) {
			return ddlbOption(aaCode,aaText,strName);
		}

		if (strName==null || strName.trim().length()==0) {
			return "";
		}
		return commString.decode(strName,aaCode,aaText);
	}
  
  public static String arbitStage(String strName) {
    return commString.decode(strName, new String[] {"1", "2"}, new String[] {"預備仲裁", "仲裁"});
  }

  public static String arbitResult(String strName) {
    // 1.本行嬴, 2.本行輸, 3.其他
    return commString.decode(strName, new String[] {"1", "2", "3"}, new String[] {"本行嬴", "本行輸", "其他"});
  }

  public static String complTimes(String strName) {
    // 1.預備依從, 2.依從權
    return commString.decode(strName, new String[] {"1", "2"}, new String[] {"預備依從", "依從權"});
  }

  public static String complStatus(String strName) {
    /*
     * sub_stage: 10.新增待覆核 30.新增已覆核 40.修改待覆核 50.修改已覆核 60.結案待覆核 80.結案已覆核 ;
     */
    String[] cardVal = new String[] {"10", "30", "80"};
    String[] cardName = {"新增待覆核 ", "新增已覆核", "結案"};
    return commString.decode(strName, cardVal, cardName);
  }

  public static String complPreResult(String strName) {
    /* 1.收單行接受,2.持卡人自付3.其他 */
    return complPreResult(strName,false);
  }

  public static String complPreResult(String strName, boolean ddlb) {
	//11.成功-收單行退款;12.商店退貨;13.持卡人自付;21.失敗-收單行拒絕;22.本行負擔;23.持卡人要求續做爭議
	//24.其他
	String[] aaCode=new String[]{"11","12","13","21","22","23","24"};
	String[] aaText=new String[]{"成功-收單行退款","商店退貨","持卡人自付","失敗-收單行拒絕","本行負擔","持卡人要求續做爭議","其他"};
	if (ddlb) {
		return ddlbOption(aaCode,aaText,strName);
	}
	if (strName==null || strName.trim().length()==0) {
		return "";
	}
		return commString.decode(strName,aaCode,aaText);
	}
  
  public static String complCloResult(String strName) {
    /* 1.收單行接受,2.持卡人自付3.其他 */
    return complCloResult(strName,false);
  }
    
  public static String complCloResult(String strName, boolean ddlb) {
	// 1.收單行接受,2.持卡人自付, 3.其他          --
	//		,new String[]{"收單行接受","持卡人自付","其他"});
	//"本行嬴","本行輸","其他"
	String[] aaCode=new String[]{"1","2","3"};
	String[] aaText=new String[]{"本行嬴","本行輸","其他"};
	if (ddlb) {
		return ddlbOption(aaCode,aaText,strName);
	}

	if (strName==null || strName.trim().length()==0) {
		return "";
	}
	return commString.decode(strName,aaCode,aaText);
  }
  
  // ------------------------------------------------------------
  // -雜費-=================
  public static String miscStatus(String strName) {
    // **misc_status: 10.新增待覆核 30.新增已覆核 40.修改待覆核
    // 50.修改已覆核 60.結案待覆核 80.結案已覆核 ;
    String[] cardVal = {"10", "30", "40", "50", "60", "80"};
    String[] cardName = {"新增待覆核", "新增已覆核", "修改待覆核", "修改已覆核", "結案待覆核", "結案已覆核"};

    return commString.decode(strName, cardVal, cardName);
  }

  // ---------------------------------------------------------
  public static String badFromType(String strName) {
    String[] cardVal = {"1", "2", "3", "4", "5"};
    String[] cardName = {"支票拒往", "他行強停-NCCC", "他行強停-JCIC", "人工匯入", "人工登錄"};

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String trialAction(String strName) {
    return trialAction(strName, false);
  }

  public static String trialAction(String strName, boolean flag) {
    String[] cardVal = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
    String[] cardName =
        {"原額用卡", "調降額度-未降足額度者凍結", "調降額度-未降足額度者維護特指", "調整額度", "調降額度-卡戶凍結(個繳)", "調降額度-維護特指",
            "卡戶凍結[4] [5]", "卡片維護特指", "額度內用卡"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String trialDataFrom(String strName) {
    String[] cardVal = {"1", "2"};
    String[] cardName = {"人工", "檔案匯入"};

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String blockParamType(String strName) {
    // String[] cde = {"1","2","3","4"};
    // String[] txt = {"一般卡", "商務卡總繳"
    // , "一般卡","商務卡總繳"
    // };
    String[] cardVal = {"1", "2", "3", "4"};
    String[] cardName = {"一般卡凍結", "一般卡解凍", "商務卡凍結", "商務卡解凍"};

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  public static String blockExecMode(String strName) {
    return blockExecMode(strName, false);
  }

  public static String blockExecMode(String strName, boolean flag) {
    String[] cardVal = {"1", "2", "3", "9"};
    String[] cardName = {"每月固定一天", "CYCLE後N日", "每天", "暫不執行"};
    if (flag) {
      return ddlbOption(cardVal, cardName, strName);
    }

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

  // -系統代碼說明-
  public static String rskIddesc(String strName) {
    StringBuilder cardVal = new StringBuilder("");
    StringBuilder cardName = new StringBuilder("");

    cardVal.append(",PRB_STATUS,PRB_SRC_CODE,PRBE_REASON_CODE,DBPE_REASON_CODE");
    cardName.append(",問交狀態,問交來源,不合格理由,不合格理由(DB)");
    cardVal.append(",PRBQ_REASON_CODE");
    cardName.append(",問交理由");
    cardVal.append(",DBPQ_REASON_CODE");
    cardName.append(",問交理由(DB)");
    cardVal.append(",PRBE_CLO_RESULT");
    cardName.append(",不合格結果");
    cardVal.append(",DBPE_CLO_RESULT");
    cardName.append(",不合格結果(DB)");
    cardVal.append(",PRBQ_CLO_RESULT");
    cardName.append(",問交結果");
    cardVal.append(",DBPQ_CLO_RESULT");
    cardName.append(",問交結果(DB)");
    cardVal.append(",PRBS_CLO_RESULT");
    cardName.append(",特交結果");
    cardVal.append(",CHGBACK_CLO_RESULT");
    cardName.append(",扣款結果");
    cardVal.append(",RECEIPT_REASON_CODE");
    cardName.append(",調單結果");
    cardVal.append(",RSK_STATUS_DESC");
    cardName.append(",風管狀態說明");
    cardVal.append(",TONCCC");
    cardName.append(",送NCCC");
    cardVal.append(",TRIAL_REASON");
    cardName.append(",期中覆審原因");
    cardVal.append(",TRIAL_ASIG_REASON");
    cardName.append(",覆審名單指定原因");
    cardVal.append(",BKCHECK_REFUND");
    cardName.append(",自行退票理由碼");
    cardVal.append(",BKCREDIT_MDFLAG");
    cardName.append(",自行異常通報代碼");

    return commString.decode(strName, cardVal.toString(), cardName.toString());
  }

  // --依從權-------------------------
  public static String complStage(String strName) {
    return commString.decode(strName, new String[] {"1", "2"}, new String[] {"預備依從", "依從權"});
  }

  public static String comlCloResult(String strName) {
    return commString.decode(strName, new String[] {"1", "2", "3"}, new String[] {"收單行接受", "持卡人自付", "其他"});
  }

  public static String acctStatus(String strName) {
    String[] cardVal = {"1", "2", "3", "4"};
    String[] cardName = {"正常", "逾期", "催收", "呆帳"};
    return commString.decode(strName, cardVal, cardName);
  }

}
