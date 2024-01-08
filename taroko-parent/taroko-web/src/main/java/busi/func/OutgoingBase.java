/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-12-24  V1.00.02  Justin        parameterize sql
******************************************************************************/
package busi.func;
/**
 * 2019-1225   JH    p1_xxx
 * 2019-0701   JH    IBM 只insert outgoing
 * 19-0417: JH    resp_data
 *  19-0311: JH    modify
 *  110-01-07  V1.00.05  tanwei        修改意義不明確變量                                                                          *
 *  110-01-21 V2.00.00  ryan           add isDebit
 *  110-10-15 V3.00.01  Wilson         斷線時authPortNo = colStr("wf_value2")   *   
 *  110-12-16 V3.00.02  Ryan           oempayReq 新增  token_requestor_id,account_number_ref 欄位 *   
 *  110-12-17 V3.00.03  Ryan           oempayReq account_number_ref --> t_c_identifier *   
 *  111-04-12 V4.00.04  Ryan           NCCC格式修正 , strNegReason = strFiscReason *   
 * */
/*
 * AutoAuth: return
    	String sL_IsoField39 //2;
    	String sL_IsoField38 //6);
    	String sL_IsoField73 //6);
    	String sL_IsoField92 //2);
    	String sL_IsoField120 //;
 * */
import busi.FuncBase;
import taroko.com.TarokoParm;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import bank.AuthIntf.AuthData;
import bank.AuthIntf.AuthGateway;

public class OutgoingBase extends FuncBase {
  private CcasIsoString ooiso = new CcasIsoString();
  AuthGateway authway = null;
  // <<<-------------
  public String p1CardNo = "";
  public String p2BinType = "";
  public String p3BankAcctno = "";
  public String p4Reason = "";
  public String p5DelDate = "";
  public String p6VipAmt = "0.00";
  public String p7Region = "";
  public String p8NewEndDate = "";
  public String p9CurrentCode = "";
  public String p10OppostReason = "";
  public String p11ElectronicCardno = "";
  public boolean isCallAutoAuth = true;
  public boolean isDebit = false;
  // >>>---------------

  private String authIp = ""; // "192.168.30.20";
  private String authPortNo = ""; // "6050";

  // private String _bitmap="";
  private String bit39 = "";
  private String bit73 = "";
  private String bit120 = "";

  public String masterDate = "";
  public String respCode = "";
  public String negRespCode = "";
  public String vmjRespCode = "";
  public String negIncomeReason = "";
  public String vmjIncomeReason = "";
  public String vmjIncomeRegion = "";

  // --
  public String strNegReason = "";
  public String strFiscReason = "";
  public String strVisaReason = "";
  public String strMastReason = "";
  public String strJcbReason = "";
  public String strSendIbm = "";
  public String strNegCapCode = "0";
  public String strCallStatus = "";
  String respData = "";
  
  public String tokenRequestorId = "";
  public String tCIdentifier = "";

  // public void p1_cardNo(String s1) {
  // p1_card_no =s1;
  // }
  // public void p2_bankActno(String s1) {
  // p3_bank_acctno =s1;
  // }
  // public void p3_binType(String s1) {
  // p2_bin_type =s1;
  // }
  // public void p4_delDate(String s1) {
  // p5_del_date =s1;
  // }
  // public void p5_reason(String s1) {
  // p4_reason =s1;
  // }
  // public void p6_vipAmt(String s1) {
  // p6_vip_amt =s1;
  // }
  // public void p7_region(String s1) {
  // p7_region =s1;
  // }


  public String isoMesgType() {
    return ooiso.mesgType();
    //return "0312";
  }

  public void wpCallStatus(String aType) {
    if (wp == null)
      return;

    if (eq(aType, "NEG")) {
      wp.colSet("neg_call_status", strCallStatus);
      wp.colSet("neg_resp_code", this.respCode);
      wp.colSet("neg_reason_code", p4Reason);
      return;
    }
    if (eq(aType, "FISC")) {
        wp.colSet("fisc_call_status", strCallStatus);
        wp.colSet("fisc_resp_code", this.respCode);
        wp.colSet("fisc_reason_code", p4Reason);
        return;
    }
    if (eq(aType, "TWMP")) {
    	wp.colSet("twmp_call_status", strCallStatus);
        wp.colSet("twmp_resp_code", this.respCode);
        wp.colSet("twmp_reason_code", "");
       // wp.colSet("twmp_region", "");
        return;
    }
    if (eq(aType, "OEMPAY")) {
    	wp.colSet("oempay_call_status", strCallStatus);
        wp.colSet("oempay_resp_code", this.respCode);
        wp.colSet("oempay_reason_code", "");
       // wp.colSet("oempay_region", "");
        return;
    }
    if (eq(aType, "VISA")) {
      wp.colSet("vmj_call_status", strCallStatus);
      wp.colSet("vmj_resp_code", this.respCode);
      wp.colSet("vmj_reason_code", p4Reason);
      //wp.colSet("vmj_region", commString.mid(getBit120(), 41, 9));
      return;
    }
    if (eq(aType, "MASTER")) {
      wp.colSet("vmj_call_status", strCallStatus);
      wp.colSet("vmj_resp_code", this.respCode);
      wp.colSet("vmj_reason_code", p4Reason);
      //wp.colSet("vmj_region", "");
      return;
    }
    if (eq(aType, "MASTER2")) {
      wp.colSet("vmj_call_status", strCallStatus);
      wp.colSet("vmj_resp_code", this.respCode);
      wp.colSet("vmj_reason_code", p4Reason);
    //  wp.colSet("vmj_region", "");
      return;
    }
    if (eq(aType, "JCB")) {
      wp.colSet("vmj_call_status", strCallStatus);
      wp.colSet("vmj_resp_code", this.respCode);
      wp.colSet("vmj_reason_code", p4Reason);
     // wp.colSet("vmj_region", commString.mid(getBit120(), 37, 5));
      return;
    }
    if (eq(aType, "TSC")) {
        wp.colSet("tii_call_status", strCallStatus);
        wp.colSet("tii_resp_code", this.respCode);
        wp.colSet("tii_reason_code", "");
       // wp.colSet("tii_region", "");
        return;
     }
    if (eq(aType, "IPS")) {
    	wp.colSet("tii_call_status", strCallStatus);
        wp.colSet("tii_resp_code", this.respCode);
        wp.colSet("tii_reason_code", "");
      //  wp.colSet("tii_region", "");

        return;
    }
    if (eq(aType, "ICH")) {
    	wp.colSet("tii_call_status", strCallStatus);
        wp.colSet("tii_resp_code", this.respCode);
        wp.colSet("tii_reason_code", "");
       // wp.colSet("tii_region", "");
        return;
    }

    wp.colSet("neg_call_status", "");
    wp.colSet("neg_resp_code", "");
    wp.colSet("neg_reason_code", "");
    wp.colSet("fisc_call_status", "");
    wp.colSet("fisc_resp_code", "");
    wp.colSet("fisc_reason_code", "");
    wp.colSet("twmp_call_status", "");
    wp.colSet("twmp_resp_code", "");
    wp.colSet("twmp_reason_code", "");
    wp.colSet("vmj_call_status", "");
    wp.colSet("vmj_resp_code", "");
    wp.colSet("vmj_reason_code", "");
    wp.colSet("vmj_region", "");
    wp.colSet("tii_call_status", "");
    wp.colSet("tii_resp_code", "");
    wp.colSet("tii_reason_code", "");
    wp.colSet("tii_region", "");
  }

  public void parmClear() {
    p1CardNo = "";
    p3BankAcctno = "";
    // pp_file_code="";
    p4Reason = "";
    p5DelDate = "";
    p6VipAmt = "";
    p2BinType = "";
    p7Region = "";
  }

  public String getBitmap() {
    return ooiso.getIsoString();
  }

  public String getBit39() {
    return bit39;
  }

  public String getBit73() {
    return bit73;
  }

  public String getBit120() {
    return bit120;
  }


  public int selectCcaOppTypeReason(String aOppoReason) {
    strNegReason = "";
    strVisaReason = "";
    strMastReason = "";
    strJcbReason = "";
    strFiscReason = "";

    strSql =
        "select ncc_opp_type, neg_opp_reason as neg_reason," + " vis_excep_code as visa_reason,"
            + " mst_auth_code as mast_reason," + " jcb_excp_code as jcb_reason, "
            + " fisc_opp_code as fisc_reason "
            + " from cca_opp_type_reason" + " where opp_status =?"; // +commSqlStr .col(wp.item_ss("oppo_reason"),"opp_status")
    setString2(1, aOppoReason);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("讀取NEG原因失敗!");
      return rc;
    }

    strNegReason = colStr("neg_reason");
    strVisaReason = colStr("visa_reason");
    strMastReason = colStr("mast_reason");
    strJcbReason = colStr("jcb_reason");
    strFiscReason = colStr("fisc_reason");
    strNegReason = strFiscReason;
    return rc;
  }

  public void selectCcaSpecCode(String aSpec) {
    if (empty(aSpec))
      return;

    strSql =
        "select" + " neg_reason, visa_reason, mast_reason, jcb_reason, send_ibm"
            + " from  cca_spec_code" + " where spec_code =?";
    setString2(1, aSpec);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      // ib_outgoing =false;
      return;
    }

    strNegReason = colStr("neg_reason");
    strVisaReason = colStr("visa_reason");
    strMastReason = colStr("mast_reason");
    strJcbReason = colStr("jcb_reason");
    strFiscReason = colStr("neg_reason");
    strSendIbm = colNvl("send_ibm", "N");
    strNegCapCode = "0";

    strSql =
        "select sys_data2 as neg_cap_code" + " from cca_sys_parm1" + " where sys_id ='NCCC'"
            + " and sys_key =?" // neg_reason
    ;
    setString2(1, strNegReason);
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      strNegCapCode = colNvl("neg_cap_code", "0");
    }
  }

  // String a_bank_cardno, String a_card_type, String a_neg_reason,String a_del_date, String
  // a_file_code
  protected int ftpIbmNegfile(String aFile) {
//    msgOK();
//    String ls_neg_cap = getNegCapCode(p4Reason);
//    if (rc != 1)
//      return rc;
//
//    rc = ooiso.ibmNegfile(p3BankAcctno, p2BinType, p4Reason, ls_neg_cap, p5DelDate, a_file);
//    if (rc == -1) {
//      errmsg(ooiso.getMsg());
//      return rc;
//    }
//    // -jh:190701-
//    // call_autoAuth("ibm-neg");
    return rc;
  }

  // String a_card_no, String a_card_type, String a_reason_code
  // , String a_del_date, String a_file_code
	protected int ftpNegId(String aFile) {
		msgOK();

//		String lsCapCode = getNegCapCode(p4Reason);
//		if (rc != 1) {
//			return rc;
//		}

		rc = ooiso.negId(p1CardNo, p2BinType, p4Reason, p5DelDate, aFile, p6VipAmt ,p7Region);
		if (rc == -1) {
			strCallStatus = ooiso.getMsg();
			errmsg(ooiso.getMsg());
			return rc;
		}

		callAutoAuth("neg-id");
		respOkNegId(aFile);

		return rc;
	}
  
	protected int ftpFiscReq(String aFile) {
		msgOK();

//		String lsCapCode = getNegCapCode(p4Reason);
//		if (rc != 1) {
//			return rc;
//		}

		rc = ooiso.fiscReq(p1CardNo, p2BinType, p4Reason, p5DelDate, aFile, p6VipAmt,p7Region);
		if (rc == -1) {
			strCallStatus = ooiso.getMsg();
			errmsg(ooiso.getMsg());
			return rc;
		}

		callAutoAuth("fisc");
		respOkFiscReq(aFile);

		return rc;
	}

	protected int ftpTwmpReq(String aFile,String cardNo) {
		// String a_card_no,String a_purg_date,String a_file_code,String
		// a_reason,String a_area
		msgOK();
 
		rc = ooiso.twmpReq(cardNo, aFile, p4Reason , p9CurrentCode);
		if (rc == -1) {
			errmsg(ooiso.getMsg());
			return rc;
		}
		//改為CcaB002執行
//		callAutoAuth("twmp-req");
//		respOkTwmpReq(aFile);

		return rc;
	}
	
	protected int ftpOempayReq(String aFile,String oempayCardNo,String cardNo) {
		// String a_card_no,String a_purg_date,String a_file_code,String
		// a_reason,String a_area
		msgOK();
		getTokenId(oempayCardNo);
		rc = ooiso.oempayReq(cardNo, aFile, p4Reason ,p2BinType,p8NewEndDate,oempayCardNo,p9CurrentCode,tokenRequestorId,tCIdentifier);
		if (rc == -1) {
			errmsg(ooiso.getMsg());
			return rc;
		}

		callAutoAuth("oempay-req");
		respOkOempayReq(aFile);

		return rc;
	}
	
  public boolean respOkNegId(String aFile) {
    int liFile = commString.strToInt(aFile);
    strCallStatus = "NEG: 傳送成功";

    // IF lstr_neg_InCome.msgtype <> '0310' or lstr_neg_InCome.bit39_resp_code <> '00' THEN
    if (eqIgno(respCode, "00"))
      return true;
    // NCCC Reject-NEG RECORD NOT FOUND WHILE DELETE
    if (eqIgno(respCode, "25")) {
      return true;
    }
    // NCCC Reject-NEG RECORD Already Exist WHILE ADD
    if (eqIgno(respCode, "N4")) {
      if (liFile == 1)
        return true;

      strCallStatus += ", 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }
    // NCCC Reject-NEG RECORD NOT FOUND WHILE UPDATE
    if (eqIgno(respCode, "N5")) {
      if (liFile == 2 || liFile == 3)
        return true;

      strCallStatus += ", 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }
    // NCCC Reject-NEG RECORD NOT FOUND WHILE INQUIRE
    if (eqIgno(respCode, "N6")) {
      if (liFile == 2 || liFile == 3 || liFile == 5)
        return true;

      strCallStatus += ", 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }

    strCallStatus += ", 處理失敗(" + aFile + "-" + respCode + ")";
    return false;

  }

  public boolean respOkFiscReq(String aFile) {
	    int liFile = commString.strToInt(aFile);
	    strCallStatus = "FISC: 傳送成功";

	    // IF lstr_neg_InCome.msgtype <> '0310' or lstr_neg_InCome.bit39_resp_code <> '00' THEN
	    if (eqIgno(respCode, "00"))
	      return true;
	
	    if (eqIgno(respCode, "25")) {
	      return true;
	    }

	    if (eqIgno(respCode, "N4")) {
	      if (liFile == 1)
	        return true;

	      strCallStatus += ", 處理失敗(" + aFile + "-" + respCode + ")";
	      return false;
	    }

	    if (eqIgno(respCode, "N5")) {
	      if (liFile == 2 || liFile == 3)
	        return true;

	      strCallStatus += ", 處理失敗(" + aFile + "-" + respCode + ")";
	      return false;
	    }

	    if (eqIgno(respCode, "N6")) {
	      if (liFile == 2 || liFile == 3 || liFile == 5)
	        return true;

	      strCallStatus += ", 處理失敗(" + aFile + "-" + respCode + ")";
	      return false;
	    }

	    strCallStatus += ", 處理失敗(" + aFile + "-" + respCode + ")";
	    return false;
	  }
  
  public boolean respOkTwmpReq(String aFile) {
	    strCallStatus = "TWMP:傳送成功";

	    int liFile = commString.strToInt(aFile);
	    if (!eqIgno(respCode, "00") || eqIgno(isoMesgType(), "0302") == false) {

	      if (pos(",N4", respCode) > 0) {
	        if (liFile == 1)
	          return true;
	      } else if (pos(",N5", respCode) > 0) {
	        if (liFile == 2)
	          return true;
	      } else if (pos(",25,N6", respCode) > 0) {
	        if (liFile == 3)
	          return true;
	      } else {
	        strCallStatus = "TWMP:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
	        return false;
	      }
	    }

	    return true;
}	
  
  public boolean respOkOempayReq(String aFile) {
	    strCallStatus = "OEMPAY:傳送成功";

	    int liFile = commString.strToInt(aFile);
	    if (!eqIgno(respCode, "00") || eqIgno(isoMesgType(), "0302") == false) {

	      if (pos(",N4", respCode) > 0) {
	        if (liFile == 1)
	          return true;
	      } else if (pos(",N5", respCode) > 0) {
	        if (liFile == 2)
	          return true;
	      } else if (pos(",25,N6", respCode) > 0) {
	        if (liFile == 3)
	          return true;
	      } else {
	        strCallStatus = "OEMPAY:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
	        return false;
	      }
	    }

	    return true;
}	
  
  public boolean respOkJcbReq(String aFile) {

    int liFile = commString.strToInt(aFile);
    strCallStatus = "JCB:傳送成功";
    if (eqIgno(respCode, "00") || eqIgno(isoMesgType(), "0302") == false) {
      return true;
    }

    // RECORD ALREADY EXISTS WIHLE ADD
    if (eqIgno(respCode, "N4")) {
      if (liFile == 1)
        return true;

      strCallStatus = "JCB:傳送成功, 處理失敗(" + aFile + "-N4)";
      return false;
    }
    // RECORD NOT FOUNE WHILE MODIFY
    if (eqIgno(respCode, "N5")) {
      if (liFile == 2)
        return true;
      else {
        strCallStatus = "JCB:傳送成功, 處理失敗(2-N5)";
        return false;
      }
    }
    // RECORD NOT FOUND WHILE DELETE
    if (pos(",25,N6", respCode) > 0) {
      if (liFile == 3)
        return true;
      else {
        strCallStatus = "JCB:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
        return false;
      }
    }
    // -<>00-
    if (eqIgno(respCode, "00") == false) {
      strCallStatus = "JCB:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }

    return true;
  }

  public boolean respOkMasterReq(String aFile) {

    int liFile = commString.strToInt(aFile);
    strCallStatus = "Master:傳送成功";
    if (eqIgno(respCode, "00") || eqIgno(isoMesgType(), "0302") == false)
      return true;


    if (pos(",N4", respCode) > 0) {
      if (liFile == 1)
        return true;
    } else if (pos(",25,N6", respCode) > 0) {
      if (liFile == 3)
        return true;
    } else {
      strCallStatus = "Master:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }

    return true;
  }

  public boolean respOkMasterReq2(String aFile) {
 
    // ==========================================================================
    strCallStatus = "Master2:傳送成功";
    int liFile = commString.strToInt(aFile);
    if (eq(respCode, "99")) {
      strCallStatus = "Master2:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }

    if (eqIgno(respCode, "00") || eqIgno(isoMesgType(), "0302") == false || liFile == 5)
      return true;

    // NCCC Reject-NEG RECORD NOT FOUND WHILE DELETE
    if (pos(",25", respCode) > 0) {
      if (liFile == 3)
        return true;

      strCallStatus = "Master2:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }
    // NCCC Reject-NEG RECORD Already Exist WHILE ADD
    if (pos(",N4", respCode) > 0) {
      if (liFile == 1)
        return true;

      strCallStatus = "Master2:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }
    // NCCC Reject-NEG RECORD NOT FOUND WHILE UPDATE
    if (pos(",N5", respCode) > 0) {
      if (liFile == 2 || liFile == 3)
        return true;

      strCallStatus = "Master2:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }
    // NCCC Reject-NEG RECORD NOT FOUND WHILE INQUIRE
    if (pos(",N6", respCode) > 0) {
      if (liFile == 2 || liFile == 3 || liFile == 5)
        return true;

      strCallStatus = "Master2:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
      return false;
    }

    strCallStatus = "Master2:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
    return false;
  }

  public boolean respOkVisaReq(String aFile) {

    // =============================================================================
    strCallStatus = "Visa:傳送成功";

    int liFile = commString.strToInt(aFile);
    if (!eqIgno(respCode, "00") || eqIgno(isoMesgType(), "0302") == false) {

      if (pos(",N4", respCode) > 0) {
        if (liFile == 1)
          return true;
      } else if (pos(",N5", respCode) > 0) {
        if (liFile == 2)
          return true;
      } else if (pos(",25,N6", respCode) > 0) {
        if (liFile == 3)
          return true;
      } else {
        strCallStatus = "Visa:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
        return false;
      }
    }

    return true;
  }
  
  public boolean respOkTscReq(String aFile) {
	    strCallStatus = "Tsc:傳送成功";

	    int liFile = commString.strToInt(aFile);
	    if (!eqIgno(respCode, "00") || eqIgno(isoMesgType(), "0302") == false) {

	      if (pos(",N4", respCode) > 0) {
	        if (liFile == 1)
	          return true;
	      } else if (pos(",N5", respCode) > 0) {
	        if (liFile == 2)
	          return true;
	      } else if (pos(",25,N6", respCode) > 0) {
	        if (liFile == 3)
	          return true;
	      } else {
	        strCallStatus = "Tsc:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
	        return false;
	      }
	    }

	    return true;
  }

  public boolean respOkIpsReq(String aFile) {
	    strCallStatus = "Ips:傳送成功";

	    int liFile = commString.strToInt(aFile);
	    if (!eqIgno(respCode, "00") || eqIgno(isoMesgType(), "0302") == false) {

	      if (pos(",N4", respCode) > 0) {
	        if (liFile == 1)
	          return true;
	      } else if (pos(",N5", respCode) > 0) {
	        if (liFile == 2)
	          return true;
	      } else if (pos(",25,N6", respCode) > 0) {
	        if (liFile == 3)
	          return true;
	      } else {
	        strCallStatus = "Ips:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
	        return false;
	      }
	    }

	    return true;
  }

  public boolean respOkIchReq(String aFile) {
	    strCallStatus = "Ich:傳送成功";

	    int liFile = commString.strToInt(aFile);
	    if (!eqIgno(respCode, "00") || eqIgno(isoMesgType(), "0302") == false) {

	      if (pos(",N4", respCode) > 0) {
	        if (liFile == 1)
	          return true;
	      } else if (pos(",N5", respCode) > 0) {
	        if (liFile == 2)
	          return true;
	      } else if (pos(",25,N6", respCode) > 0) {
	        if (liFile == 3)
	          return true;
	      } else {
	        strCallStatus = "Ich:傳送成功, 處理失敗(" + aFile + "-" + respCode + ")";
	        return false;
	      }
	    }

	    return true;
  }	
  
  protected int ftpMasterReq(String aFile, String[] aRegnDate) {
    // String a_card_no,String a_card_type,String a_file_code
    // , String a_reason_code, String[] a_regn_date

    msgOK();
    rc = ooiso.masterReq(p1CardNo, p2BinType, aFile, p4Reason, aRegnDate);
    if (rc == -1) {
      errmsg(ooiso.getMsg());
      return rc;
    }

    callAutoAuth("master-req");
    respOkMasterReq(aFile);

    return rc;
  }

  protected int ftpMasterReq2(String aFile) {
    // String a_card_no,String a_file_code
    // , String a_vip_amt, String a_reason

    ooiso.setMasterDate(masterDate);
    rc = ooiso.masterReq2(p1CardNo, aFile, p6VipAmt, p4Reason);
    if (rc == -1) {
      errmsg(ooiso.getMsg());
      return rc;
    }

    callAutoAuth("master-req2");
    respOkMasterReq2(aFile);

    return rc;
  }

  protected int ftpJcbReq(String aFile) {
    // String a_card_no,String a_purg_date,String a_file_code,String a_reason,String a_region

    rc = ooiso.jcbReq(p1CardNo, p5DelDate, aFile, p4Reason, p7Region);
    if (rc == -1) {
      errmsg(ooiso.getMsg());
      return rc;
    }

    callAutoAuth("jcb-req");
    respOkJcbReq(aFile);

    return rc;
  }

  protected int ftpVisaReq(String aFile) {
    // String a_card_no,String a_purg_date,String a_file_code,String a_reason,String a_area
    msgOK();

    rc = ooiso.visaReq(p1CardNo, p5DelDate, aFile, p4Reason, p7Region);
    if (rc == -1) {
      errmsg(ooiso.getMsg());
      return rc;
    }

    callAutoAuth("visa-req");
    respOkVisaReq(aFile);

    return rc;
  }
  
  protected int ftpTscReq(String aFile) {
  // String a_card_no,String a_purg_date,String a_file_code,String a_reason,String a_area
	  msgOK();

	  rc = ooiso.tscReq(p11ElectronicCardno, aFile, p8NewEndDate,p5DelDate,isDebit);
	  if (rc == -1) {
		  errmsg(ooiso.getMsg());
		  return rc;
	  }

	  callAutoAuth("tsc-req");
	  respOkTscReq(aFile);

	  return rc;
  }
  
  protected int ftpIpsReq(String aFile) {
  // String a_card_no,String a_purg_date,String a_file_code,String a_reason,String a_area
	  msgOK();

	  rc = ooiso.ipsReq(p11ElectronicCardno, aFile);
	  if (rc == -1) {
		  errmsg(ooiso.getMsg());
		  return rc;
	  }

	  callAutoAuth("ips-req");
	  respOkIpsReq(aFile);

	  return rc;
  }
  
  protected int ftpIchReq(String aFile) {
  // String a_card_no,String a_purg_date,String a_file_code,String a_reason,String a_area
	  msgOK();

	  rc = ooiso.ichReq(p11ElectronicCardno, aFile, p8NewEndDate);
	  if (rc == -1) {
		  errmsg(ooiso.getMsg());
		  return rc;
	  }

	  callAutoAuth("ich-req");
	  respOkIchReq(aFile);

	  return rc;
  }

  void isoRespData(String strName) {
    /*
     * String sL_IsoField39 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[39], 2); String sL_IsoField38 =
     * HpeUtil.fillZeroOnLeft(G_Gate.isoField[38], 6); String sL_IsoField73 =
     * HpeUtil.fillZeroOnLeft(G_Gate.isoField[73], 6); String sL_IsoField92 =
     * HpeUtil.fillZeroOnLeft(G_Gate.isoField[92], 2); String sL_IsoField120 = G_Gate.isoField[120];
     */

    respCode = "";
    if (empty(strName)) {
      bit39 = "";
      bit73 = "";
      bit120 = "";
      return;
    }
    
	wp.showLog("D", "respCode:"+strName);
	
    bit39 = strMid(strName, 0, 2);
    // auth_no =ss_mid(s1,2,6);
    bit73 = strMid(strName, 8, 6); // -YYMMDD-
    bit120 = commString.mid(strName, 16);
    respCode = bit39;
    
	System.out.println("respCode:"+strName);
	
    if (empty(bit73) == false)
      bit73 = "20" + bit73;
  }

  int callAutoAuth(String aType) {
	  this.sqlCommit(1);
	  if(!isCallAutoAuth)
		  return 1;
	    String isNeg ;
		respCode = "XX";
		isoRespData("");
		if (aType.equals("neg-id") || aType.equals("fisc")) {
			strSql = "select wf_value, wf_value2, wf_value3, wf_value4 , wf_value6 as db_nocall" + " from ptr_sys_parm"
					+ " where wf_parm ='SYSPARM' and wf_key='NEG'";
			isNeg = "NEG";
		} else {
			strSql = "select wf_value, wf_value2, wf_value3, wf_value4 , wf_value6 as db_nocall" + " from ptr_sys_parm"
					+ " where wf_parm ='SYSPARM' and wf_key='OUTGOING'";
			isNeg = "FHM";
		}
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			sqlErr("ptr_sysparm.SYSPARM,OUTGOING");
			return -1;
		}

		authIp = colStr("wf_value");
		authPortNo = colStr("wf_value2");

		// TTTT--
		if (wp.localHost() || "Y".equals(TarokoParm.getInstance().getDbSwitch2Dr())) {
			authIp = "127.0.0.1";
			authPortNo = colStr("wf_value2");
		}
		
		if ("3".equals(TarokoParm.getInstance().getDbSwitch2Dr()) || "6".equals(TarokoParm.getInstance().getDbSwitch2Dr())) {
			authIp = colStr("wf_value3");
			authPortNo = colStr("wf_value4");
		}
		
		if (empty(authIp) || empty(authPortNo)) {
			errmsg("自動授權[IP,Port-No]: 不可空白");
			return rc;
		}
		if (colInt("db_nocall") == 1) {
			bit120 = commString.fill('X', 60);
		} else {
			AuthData authdata = new AuthData();
			Thread t = null;
			authdata.setFullIsoCommand(ooiso.getIsoString());
			System.out.println(aType + "_iso : " + ooiso.getIsoString());
			try {
				try {
				FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
					public Boolean call() throws Exception {
						authway=new AuthGateway();
						authway.isNeg = isNeg;
						respData = authway.startProcess(authdata, authIp, authPortNo);
						return true;
					}
				});
				t = new Thread(task);
				t.start();
				task.get(10000, TimeUnit.MILLISECONDS);
				} catch (Exception ex) {
					respData = "99999999999999";
					wp.showLog("D",t.getState()+"");
				}
				isoRespData(respData);
				wp.showLog("D", "OUTGO-resp-data:"+respData);
			} catch (Exception ex) {
				errmsg("call_auth error; " + ex.getMessage());
				return -1;
			}finally {
				authway.releaseConnection();
				wp.showLog("D", "authway releaseConn OK");
			}
		}

		if (eqIgno(aType, "neg-id")) {
			negRespCode = respCode;
			this.negIncomeReason = p4Reason;
		} else if (eqIgno(aType, "fisc")) {
			negRespCode = respCode;
			this.negIncomeReason = p4Reason;

		} else if (eqIgno(aType, "jcb-req")) {
			vmjRespCode = respCode;
			vmjIncomeReason = p4Reason;
		} else if (eqIgno(aType, "master-req")) {
			vmjRespCode = respCode;
			vmjIncomeReason = p4Reason;
		} else if (eqIgno(aType, "master-req2")) {
			vmjRespCode = respCode;
			vmjIncomeReason = p4Reason;
		} else if (eqIgno(aType, "visa-req")) {
			vmjRespCode = respCode;
			vmjIncomeReason = p4Reason;
		} else if (eqIgno(aType, "tsc-req")) {
			vmjRespCode = respCode;
			vmjIncomeReason = "";
		} else if (eqIgno(aType, "ips-req")) {
			vmjRespCode = respCode;
			vmjIncomeReason = "";
		} else if (eqIgno(aType, "ich-req")) {
			vmjRespCode = respCode;
			vmjIncomeReason = "";
		} else if (eqIgno(aType, "twmp-req")) {
			vmjRespCode = respCode;
			vmjIncomeReason = "";
		}else if (eqIgno(aType, "oempay-req")) {
			vmjRespCode = respCode;
			vmjIncomeReason = "";
		}

		return 1;
  }

  String getNegCapCode(String aReason) {
    if (empty(aReason)) {
      return "";
    }
    strSql =
        "select sys_data2 as neg_cap from cca_sys_parm1" + " where sys_id='NCCC' and sys_key = ? ";
    sqlSelect(strSql, new Object[] {aReason});
    if (sqlRowNum <= 0) {
      errmsg("無法讀取NCCC原因碼 (cca_SYS_PARM1-NCCC).");
      return "";
    }
    return colNvl("neg_cap", "0");
  }
  
	void getTokenId(String vCardNo) {
		tokenRequestorId = "";
		tCIdentifier = "";
		if (empty(vCardNo)) {
			return;
		}
		strSql = "select token_requestor_id,t_c_identifier from oempay_card " 
		+ " where v_card_no = ? ";
		sqlSelect(strSql, new Object[] { vCardNo });
		if (sqlRowNum > 0) {
			tokenRequestorId = colStr("token_requestor_id");
			tCIdentifier = colStr("t_c_identifier");
		}
	}

}
