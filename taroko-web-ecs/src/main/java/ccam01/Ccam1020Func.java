package ccam01;
/** 人工授權沖正處理 V.2019-0520.jh
 * 2019-0520:  jh    modify
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
 * */

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

import busi.FuncAction;
import taroko.com.TarokoParm;
import bank.AuthIntf.AuthData;
import bank.AuthIntf.AuthGate;
import bank.AuthIntf.AuthGateway;
import bank.AuthIntf.BicFormat;

public class Ccam1020Func extends FuncAction {
  String cardNo = "";
  AuthGate gate = null;
  busi.CommBusi commBusi = new busi.CommBusi();
  boolean debug = false;
  private String respCode = "", isoAuthNo = "";
  private String errRespCode = "", authMesgData = "";
  CcasWkVars oowk = new CcasWkVars();

  public void debugMode(boolean bb) {
    debug = bb;
  }

  @Override
  public void dataCheck() {
    cardNo = wp.itemStr2("card_no");
    if (empty(cardNo)) {
      errmsg("卡號 不可空白");
      return;
    }

    strSql = "select debit_flag " + " from cca_card_base" + " where card_no =?";
    setString2(1, cardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("輸入卡號錯誤");
      return;
    }
    if (colEq("debit_flag", "Y")) {
      errmsg("DEBIT卡不可做人工沖正!");
      return;
    }

    selectCrdard();
    if (rc != 1) {
      return;
    }

    // --OK--
    return;
  }

  void selectCrdard() {
    daoTid = "card.";
    strSql = "select new_end_date" + " from crd_card" + " where card_no =?";
    setString2(1, cardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("查無卡片資料, kk[%s]", cardNo);
      return;
    }
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    msgOK();
    dataCheck();
    if (rc != 1)
      return rc;

    selectAuthTxlog();
    if (rc != 1)
      return rc;

    gate = new AuthGate();

    wfCvtIso8583();
    if (rc != 1)
      return rc;
    wfSendTrans();
    if (rc != 1)
      return rc;
    wfRespMsg();

    // select_Resp_code();
    if (eqIgno(respCode, "00")) {
      wp.colSet("rc_mesg", "沖正交易完成 - " + oowk.apprCode); // +"; 授權號碼="+nvl(iso_auth_no,"???456"));
      wp.colSet("rc_mesg2", "授權號碼 - " + isoAuthNo);
      wp.colSet("rc_parm", "F");
      wp.colSet("reversal_flag", "Y");
      return rc;
    }

    errmsg("沖正交易失敗 - [%s]-[%s]", respCode, oowk.errMsg);

    return rc;
  }


  void wfRespMsg() {
    // if (eq(resp_code,"00")==false && empty(err_resp_code)) {
    // errmsg("error: resp_p39[%s], err_resp_code[%s]",resp_p39,err_resp_code);
    // return;
    // }

    strSql = "select resp_remark, nccc_p38, nccc_p39, resp_status" + " from cca_resp_code"
        + " where resp_code =?";
    setString2(1, errRespCode);
    daoTid = "resp.";
    sqlSelect(strSql);
    if (sqlRowNum == 1) {
      oowk.errMsg = colStr("resp.resp_remark");
      oowk.apprCode = colStr("resp.nccc_p38");
      oowk.isoRspCode = colStr("resp.nccc_p39");
      oowk.rspUnnormalFlag = colStr("resp.resp_status");
      // errmsg(err_resp_code+" "+oowk.err_msg);
    } else {
      // oowk.err_msg =col_ss("resp.resp_remark");
      oowk.apprCode = "00";
      oowk.isoRspCode = "96";
      oowk.rspUnnormalFlag = "N";
    }
  }

  // void select_Resp_code() {
  // String ls_appr_code ="00";
  // String ls_iso_resp_code ="96"; //系統內部有異常
  // String ls_unormal_flag ="N";
  //
  // is_sql ="select nccc_p38, nccc_p39, resp_status"
  // +" from cca_resp_code"
  // +" where resp_code =?";
  // ppp(1,resp_code);
  // sqlSelect(is_sql);
  // if (sql_nrow >0) {
  // ls_appr_code =col_ss("nccc_p38");
  // ls_iso_resp_code =col_ss("nccc_p39");
  // ls_unormal_flag =col_ss("resp_status");
  // }
  // if (eq_igno(ls_iso_resp_code,"00")==false) {
  // errmsg("沖正交易失敗 - [%s]-[%s]",resp_code,this.get_sysDate());
  // return;
  // }
  //
  // wp.col_set("rc_mesg","沖正交易完成 - "+resp_code+"; 授權號碼="+iso_auth_no);
  // wp.col_set("rc_parm","F");
  // }

  void selectAuthTxlog() {
    strSql = "select card_no, auth_seqno" + ", A.proc_code" + ", A.online_redeem" + ", A.nt_amt"
        + ", A.mcc_code" + ", A.bank_country" + ", A.pos_mode" + ", A.cond_code" + ", A.ref_no"
        + ", A.auth_no" + ", A.mcht_no" + ", A.consume_country , A.ori_amt as tran_amt ,"
        + " substr(A.tx_date,5) as cap_date , A.stand_in as acq_id , A.tx_currency as currency , A.auth_seqno , "
        + " A.curr_nt_amt , A.tx_cvc2 "
        + " from cca_auth_txlog A"
        + " where A.rowid =?";
    this.setRowId2(1, wp.itemStr2("rowid"));
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("select cca_auth_txlog error; " + sqlErrtext);
      return;
    }
    wp.log("-->mcc-code=[%s]", colStr("mcc_code"));

    // -cca_auth_bitdata- Table 用途以變更 欄位部分改抓 cca_auth_txlog
//    strSql =
//        "select '' as xxx" + ", B.bit4_tran_amt as tran_amt" + ", B.bit15_setl_date as setl_date"
//            + ", B.bit17_cap_date as cap_date" + ", B.bit32_code as acq_id"
//            + ", B.bit35_track_ii as track_ii " + ", B.bit49_trans_cur_code as currency"
//            + ", B.bit95_repl_amt as repl_amt" + " from cca_auth_bitdata B" + " where B.card_no =?"
//            + " and B.auth_seqno =?" + " and B.bit37_ref_no =?";
//    this.setString2(1, colStr("card_no"));
//    setString(colStr("auth_seqno"));
//    setString(colStr("ref_no"));
//    sqlSelect(strSql);
//    if (sqlRowNum <= 0) {
//      wp.log("select cca_auth_bitdata N-find, kk[%s,%s]", colStr("card_no"), colStr("auth_seqno"));
//    }
//    wp.log("-->mcc-code=[%s]", colStr("mcc_code"));
  }

  void wfSendTrans() {
    respCode = "99";
    isoAuthNo = "";

    // _bit120 ="";
    String lsAuthIp = "", lsAuthPortNo = "";

    if (empty(lsAuthIp) || empty(lsAuthPortNo)) {
      strSql = "select wf_value, wf_value2" + ", wf_value3, wf_value4" + " from ptr_sys_parm"
          + " where wf_parm ='SYSPARM' and wf_key='CCASLINK'";
      sqlSelect(strSql);
      if (sqlRowNum <= 0) {
        sqlErr("ptr_sysparm.SYSPARM,CCASLINK");
        return;
      }
		String dbSwitch2Dr = TarokoParm.getInstance().getDbSwitch2Dr();
		if (wp.localHost() || "Y".equals(dbSwitch2Dr)) {
			lsAuthIp = "127.0.0.1";
			lsAuthPortNo = "15001";
		} else if("3".equals(dbSwitch2Dr) || "6".equals(dbSwitch2Dr)){
			lsAuthIp = colStr("wf_value3");
			lsAuthPortNo = colStr("wf_value4");
		} else {
			lsAuthIp = colStr("wf_value");
			lsAuthPortNo = colStr("wf_value2");
		}
    }
    if (empty(lsAuthIp) || empty(lsAuthPortNo)) {
      errmsg("自動授權[IP,Port-No]: 不可空白");
      return;
    }
    
    wp.commitOnly();
    
    BicFormat bic = new BicFormat(null, gate, null);
    bic.host2Iso();
    // if (bic.host2Iso()==false) {
    // return 1;
    // }

    AuthData authdata = new AuthData();
    authdata.setFullIsoCommand(gate.isoString.substring(2));
    AuthGateway authway=null;
    try {
      authway = new AuthGateway();
      String lsRespData = authway.startProcess(authdata, lsAuthIp, lsAuthPortNo);
      wp.log("AUTH[沖正]-resp-data:[%s]", lsRespData);
      respCode = lsRespData.substring(0, 2);
      isoAuthNo = commString.mid(lsRespData, 2, 6);
      errRespCode = commString.mid(lsRespData, 14, 2);
      authMesgData = commString.mid(lsRespData, 16);
      /*
       * String sL_IsoField39 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[39], 2); //BIT39_ADJ_CODE
       * String sL_IsoField38 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[38], 6); //BIT38_APPR_CODE
       * String sL_IsoField73 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[73], 6); //BIT73_ACT_DATE
       * String sL_IsoField92 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[92], 2); // String
       * sL_IsoField120 = G_Gate.isoField[120]; //BIT120_MESS_DATA
       */
    } catch (Exception ex) {
      errmsg("call_autoAuth error; " + ex.getMessage());      
    }finally {
		authway.releaseConnection();		
	}


    return;
  }

  void wfCvtIso8583() {
    String string = "" , tempStr = "";
    String lsTranAmt = "" , lsNtAmt = "";
    String lsCurrency4 = "" , lsCurrency6 = "" , lmConsumeAmt4 = "" , lmConsumeAmt5 = "" , lmConsumeAmt6 = "";
    double tempNt = 0.0 ;
//    if (colStr("currency").equals("901") == false) {
//    	double lmAmt = colNum("tran_amt");
//    	string = String.format("%.0f", lmAmt * 100);
//        lsTranAmt = commString.lpad(string, 12, "0");
////      lsTranAmt = colStr("tran_amt");
//    } else {
//      double lmAmt = colNum("nt_amt");
//      string = String.format("%.0f", lmAmt * 100);
//      lsTranAmt = commString.lpad(string, 12, "0");
//    }
//    tempNt = colNum("nt_amt");
//    tempStr = String.format("%.0f", tempNt * 100);
//    lsNtAmt = commString.lpad(tempStr, 12, "0"); 
    
    //--消費地金額
    tempNt = colNum("tran_amt");
    tempStr = String.format("%.0f", tempNt * 100);
    lmConsumeAmt4 = commString.lpad(tempStr, 12, "0");
    lsCurrency4 = colStr("currency");
    
    //--台幣
    tempNt = colNum("nt_amt");
    tempStr = String.format("%.0f", tempNt * 100);
    lmConsumeAmt5 = commString.lpad(tempStr, 12, "0");
    
    //--清算幣
    tempNt = colNum("curr_nt_amt");
    tempStr = String.format("%.0f", tempNt * 100);
    lmConsumeAmt6 = commString.lpad(tempStr, 12, "0");
    lsCurrency6 = colStr("tx_cvc2");
        
    String lsTrackIi = "";
    lsTrackIi = cardNo + "=" + commString.mid(colStr("card.new_end_date"), 2, 4);
    String lsCountry = commString.rpad(colNvl("consume_country", "TW"), 3);
    String lsMchtNo = colStr("mcht_no"); //
    String lsMccCode = colStr("mcc_code");    

    this.dateTime();
    // ------------------------------
    boolean lbNewNccc = true;
    if (lbNewNccc)
      gate.bicHead = "ISO026000076";
    else
      gate.bicHead = "ISO025000076";
    gate.mesgType = "0420"; // "0200";
    gate.isoField[2] = cardNo;
    gate.isoField[3] = colStr("proc_code");
    //----[4]消費地金額 [5] 台幣金額 [6]清算金額
    gate.isoField[4] = lmConsumeAmt4 ;
    gate.isoField[5] = lmConsumeAmt5 ;
    if(wp.itemEq("card_curr_code", "901") == false && "901".equals(lsCurrency6) == false) {
		gate.isoField[6] = lmConsumeAmt6;
	}
//    gate.isoField[4] = lsTranAmt;
//    gate.isoField[6] = lsNtAmt;
    gate.isoField[7] = commString.mid(this.sysDate, 4) + this.sysTime;
    SecureRandom random = null;
    try {
      random = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      // random = new Random(new Date().getTime());
      throw new RuntimeException("init SecureRandom failed.", e);
    }
    gate.isoField[11] = commString.numFormat(random.nextDouble() * 1000000, "000000");
    gate.isoField[12] = this.sysTime;
    gate.isoField[13] = strMid(sysDate, 4, 4);
    if (colEmpty("setl_date"))
      gate.isoField[15] = commString.mid(sysDate, 4, 4);
    else
      gate.isoField[15] = colStr("setl_date"); // bit15_setl_date
    gate.isoField[17] = colStr("cap_date"); // bit17_cap_date
    gate.isoField[18] = colStr("mcc_code");
    string = colNvl("bank_country", "158");
    gate.isoField[19] = commString.rpad(string, 3);
    gate.isoField[22] = colStr("pos_mode");
    gate.isoField[25] = colNvl("cond_code", "00");
    gate.isoField[26] = ""; // IsoRec.bit26_pin_len = "00"
    gate.isoField[27] = "R";
    gate.isoField[32] = colStr("acq_id"); // bit32_acq_id "493817";
    gate.isoField[35] = lsTrackIi;
    gate.isoField[37] = colStr("ref_no");
    gate.isoField[38] = colStr("auth_no");
    gate.isoField[39] = "";
    gate.isoField[41] = commString.rpad("ccam1020_" + modUser, 16);
    gate.isoField[42] = commString.rpad(lsMchtNo, 15);
    gate.isoField[43] = commString.rpad(commBusi.gs_BK_name, 22) + commString.rpad(commBusi.gs_BK_city, 13)
        + lsCountry + commString.mid(lsCountry, 0, 2);
    gate.isoField[48] = commString.rpad(lsMchtNo, 27);
    //----[49]消費地幣別 [50] 901  [51]清算幣別
    gate.isoField[49] = lsCurrency4;
  	gate.isoField[50] = "901";
  	if(wp.itemEq("card_curr_code", "901") == false && "901".equals(lsCurrency6) == false) {
  		gate.isoField[51] = lsCurrency6;
  	}
//    gate.isoField[49] = colStr("currency");
//    gate.isoField[50] = colStr("currency");
//    gate.isoField[51] = "901";
    
    gate.isoField[82] = colStr("auth_seqno");
//    gate.isoField[95] = lsReplAmt;
    gate.isoField[122] = wp.itemStr2("auth_remark");
    gate.isoField[127] = commString.rpad(modUser,10," ");
    
    for (int ii = 0; ii < gate.isoField.length; ii++) {
      if (!empty(gate.isoField[ii])) {
        wp.log("%s[%s]", ii, gate.isoField[ii]);
      }
    }
    

    return;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
