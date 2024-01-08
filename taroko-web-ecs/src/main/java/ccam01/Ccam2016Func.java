package ccam01;
/**卡片停用維護　exc_general:
 * 2019-0610:  JH    p_seqno >>acno_p_xxx
   2018-1108:  JH    outgoing display
 * 2018-1018:	JH		bugfix
 * 2018-0710:	JH		附卡連動撤掛
 * 2018-0704:  正卡撤掛時附卡連動撤掛
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-06-16 V1.00.01  Ryan             updated for project coding standard
 * 110-01-21 V1.00.02  ryan             ooOutgo.ibDebit to ooOutgo.isDebit 
 * 110-01-30 V1.00.03  Justin          modify an error message
 * 110-04-08 V1.00.04  ryan          mark792~798
 * 111-06-28 V1.00.05  Justin           select tsc_vd_card: card_no -> vd_card_no
 * 111-06-30 V1.00.06  Justin           修正電子票證檢核條件
 * 112-07-19 V1.00.07  Wilson         mark wfDelSpecial
 * 112-08-25 V1.00.08  Wilson         不可撤掛條件排除reissue_status = 1
 * */
  
import busi.FuncAction;
import busi.func.OutgoingOppo;

public class Ccam2016Func extends FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  String cardNo = "";
  String isOutgoArea = "";
  busi.SqlPrepare sp = new busi.SqlPrepare();
  OutgoingOppo ooOutgo = null;

  private String isNegReason = "";
  private String isNegReasonOri = "";
  private String isBinType = "";
  private String isVmjReason = "";
  private String isVmjReasonOri = "";
  
  private String isFiscReason = "";
  private String isFiscReasonOri = "";
  // private String is_neg_cap="";
  private String isGoingArea = "";
  private String[] isMOutgoArea = new String[6];
  private String isGoingAreaOri = "";
  boolean ibDebit = false;
  boolean ibDelSup = false; // -連動取消附卡-

  private boolean ibChange = false;
  private String[] aaType = new String[2];
  private String[] aaReason = new String[2];

  private String[] isMOutgoAreaOri = new String[6];


  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  void checkExceptionArea() {
    /*
     * if (wp.item_empty("excep_flag")) return;
     */
    String lsDate = "";
    if (wp.itemEq("bin_type", "V")) {
      lsDate = wp.itemStr("vis_purg_date_1");
      isOutgoArea = wp.itemStr("vis_area_1") + wp.itemStr("vis_area_2") + wp.itemStr("vis_area_2")
          + wp.itemStr("vis_area_4") + wp.itemStr("vis_area_5") + wp.itemStr("vis_area_6")
          + wp.itemStr("vis_area_7") + wp.itemStr("vis_area_8") + wp.itemStr("vis_area_9");
      if (empty(isOutgoArea) == false && empty(lsDate)) {
        errmsg("VISA刪除日期: 不可空白");
        return;
      }
      if (!empty(lsDate) && commDate.sysComp(lsDate) > 0) {
        errmsg("VISA刪除日期不可小於今日");
        return;
      }

      return;
    }
    // -JCB-
    if (wp.itemEq("bin_type", "J")) {
      isOutgoArea = wp.itemStr("jcb_area_1") + wp.itemStr("jcb_area_2") + wp.itemStr("jcb_area_3")
          + wp.itemStr("jcb_area_4") + wp.itemStr("jcb_area_5");
      lsDate = wp.itemStr("jcb_date1");
      if (empty(isOutgoArea) == false && empty(lsDate)) {
        errmsg("JCB刪除日期不可空白");
        return;
      }
      if (empty(lsDate) == false && commDate.sysComp(lsDate) > 0) {
        errmsg("JCB刪除日期不可小於今日");
        return;
      }
      return;
    }
    // -MasterCard-
    if (wp.itemEq("bin_type", "M")) {
      for (int ii = 1; ii <= 6; ii++) {
        if (wp.itemEmpty("mast_area_" + ii))
          continue;
        lsDate = wp.itemStr("mast_date" + ii);
        if (empty(lsDate)) {
          errmsg("MASTER對應地區之刪除日期[%s]不可空白", ii);
          return;
        }
        if (commDate.sysComp(lsDate) > 0) {
          errmsg("MASTER刪除日期[%s]不可小於今日", ii);
          return;
        }
      }
      return;
    }

  }

  @Override
  public void dataCheck() {
    cardNo = wp.itemStr2("card_no");
    if (empty(cardNo)) {
      errmsg("[卡號]: 不可空白");
      return;
    }

    selectCcaOpposition(cardNo);
//    if (rc != 1)
//      return;

    selectCrdCard(cardNo);
    if (rc != 1)
      return;

    isBinType = colStr("card.bin_type");

    aaType[0] = colStr("oppo.oppo_type");
    aaReason[0] = colStr("oppo.oppo_status");
    aaType[1] = wp.itemStr2("oppo_type2");
    aaReason[1] = wp.itemStr2("oppo_reason2");

    if (ibDebit) {
      if (!empty(aaType[1]) || !empty(aaReason[1])) {
        errmsg("Debit卡: 不可變更[停掛類別,原因]");
        return;
      }
      if (!wp.itemEmpty("corp_p_seqno") && wp.itemEmpty("id_p_seqno")) {
        if (wp.itemEq("renew_flag", "Y")) {
          errmsg("公司戶VISA金融卡不可補發");
          return;
        }
      }
    } else {
      if (!empty(aaType[1]) && empty(aaReason[1])) {
        errmsg("(新)停掛原因: 不可空白!");
        return;
      }
      if (empty(aaType[1]) && !empty(aaReason[1])) {
        errmsg("(新)停掛類別: 不可空白!");
        return;
      }

      if (empty(aaType[1]) && empty(aaReason[1])) {
        aaType[1] = aaType[0];
        aaReason[1] = aaReason[0];
        wp.itemSet("excep_flag", wp.itemStr("db_excep_flag_0"));
        wp.itemSet("oppo_type2", aaType[1]);
        wp.itemSet("oppo_reason2", aaReason[1]);
      }

    }

    String lsNegDdate = wp.itemStr("neg_del_date");
    if (empty(lsNegDdate)) {
      lsNegDdate = commDate.dateAdd(wp.itemStr("new_end_date"), 0, 0, 1);
      wp.itemSet("neg_del_date", lsNegDdate);
    }

    // -不改原因存檔-
    ibChange = (!empty(aaType[1]) || !empty(aaReason[1]));
    if (ibChange == false) {
      return;
    }

    if (ibDebit && colEmpty("card.bank_acct_no")) {
      errmsg("無金融帳號, 不可做不掛檔修改");
      return;
    }

    checkExceptionArea();
    if (rc != 1)
      return; 

    setOutgoingValue(wp.itemStr("oppo_reason2"));
    if (rc != 1)
      return;



    // --outgoing--------------------------------------------------------
    String lsAction = "";
    if (eqIgno(isBinType, "M"))
      lsAction = "1";
    else if (eqIgno(isBinType, "J"))
      lsAction = "2";
    else if (eqIgno(isBinType, "V"))
      lsAction = "2";
    else
      return;

    if (empty(isNegReason) && empty(isVmjReason) && empty(isVmjReasonOri))
      return;
    //ooOutgo.isCallAutoAuth = false;
    ooOutgo.iscalltwmp = true;
    ooOutgo.isDebit = wp.itemEq("debit_flag", "Y");
    // -開始傳送["+is_card_no+"]至 NCCC (更新)......-
    if (!empty(isNegReason) && !ibDebit) {
      ooOutgo.parmClear();
      ooOutgo.p1CardNo = cardNo;
      ooOutgo.p2BinType = isBinType;
      ooOutgo.p4Reason = isNegReason;
      ooOutgo.fiscReason = isFiscReason;
      ooOutgo.p5DelDate = wp.itemStr2("neg_del_date");    
      ooOutgo.p6VipAmt = "0";
      ooOutgo.p7Region = isGoingArea;
      ooOutgo.p8NewEndDate = wp.itemStr("new_end_date");
      if (wp.respHtml.indexOf("ccam2016_detl") >= 0) {
    	  ooOutgo.p9CurrentCode = wp.itemStr("oppo_type2");
    	  ooOutgo.p10OppostReason = wp.itemStr("oppo_reason2");
  	  }else{
  		  ooOutgo.p9CurrentCode = wp.itemStr("oppo_type");
  		  ooOutgo.p10OppostReason = wp.itemStr("oppo_reason");
  	  }
      ooOutgo.electronicCode = wp.itemStr("electronic_code");
      String electronicCode = ooOutgo.electronicCode ; 
      ooOutgo.p11ElectronicCardno = selectCardNo(electronicCode, cardNo);
      rc = ooOutgo.oppoNegId("2");
      if (rc == 1 && eqIgno(ooOutgo.respCode, "N5")) {
        // NCCC Reject-NEG RECORD not Exist WHILE UPDATE
        rc = ooOutgo.oppoNegId("1");
      }
      // wf_set_lbl_n();
    }

    // -delete-----------------
    if (!empty(isVmjReasonOri) && empty(isVmjReason)) {
      wfOutgoingDelete();
      // wf_set_lbl_m();
    }
    // -Insert---------------------------
    if (empty(isVmjReasonOri) && !empty(isVmjReason)) {
      wfOutgoingUpdate("1");
      if (eqIgno(ooOutgo.respCode, "N4") && eqIgno(isBinType, "V")) {
        // NCCC Reject-OutGoing RECORD Already Exist WHILE ADD
        wfOutgoingUpdate("2");
      }
      if (eqIgno(ooOutgo.respCode, "04") && eqIgno(isBinType, "J")) {
        // NCCC Reject-OutGoing RECORD Already Exist WHILE ADD
        wfOutgoingUpdate("2");
      }
      // wf_set_lbl_m();
    }
    // -Update-----------------------
    if (notEmpty(isVmjReasonOri) && notEmpty(isVmjReason)) {
//      if (eqIgno(isBinType, "M"))
//        lsAction = "1";
//      else
      lsAction = "2";
      wfOutgoingUpdate(lsAction);
      if (eqIgno(ooOutgo.respCode, "N5") && eqIgno(isBinType, "V")) {
        // NCCC Reject-OutGoing RECORD not Exist WHILE UPDATE
        wfOutgoingUpdate("1");
      }
      if (eqIgno(ooOutgo.respCode, "25") && eqIgno(isBinType, "J")) {
        // NCCC Reject-OutGoing RECORD not Exist WHILE UPDATE
        wfOutgoingUpdate("1");
      }
      // wf_set_lbl_m();
    }

  }

  void setOutgoingValue(String oppoReason) {
    // -get outgo-reason-
    strSql = "select ncc_opp_type, neg_opp_reason as neg_reason,"
        + " vis_excep_code as visa_reason," + " mst_auth_code as mast_reason,"
        + " jcb_excp_code as jcb_reason, " 
        + " fisc_opp_code as fisc_reason "
        + " from cca_opp_type_reason" + " where 1=1"
//        + commSqlStr.col(oppoReason, "opp_status")
        + " and opp_status = ? "
        ;
    sqlSelect(strSql,new Object[] {oppoReason});
    if (sqlRowNum <= 0) {
      errmsg("讀取NEG原因失敗!");
      return;
    }
    isNegReason = colStr("neg_reason");
    isFiscReason = colStr("fisc_reason");

    if (eqIgno(isBinType, "V"))
      this.isVmjReason = colStr("visa_reason");
    else if (eqIgno(isBinType, "M"))
      isVmjReason = colStr("mast_reason");
    else if (eqIgno(isBinType, "J"))
      isVmjReason = colStr("jcb_reason");

    // --
    // if (!empty(is_neg_reason)) {
    // is_sql ="select sys_data2 as neg_cap from cca_sys_parm1"
    // +" where sys_id='NCCC'"+commSqlStr.col(is_neg_reason,"sys_key");
    // sqlSelect(is_sql);
    // if (sql_nrow<=0) {
    // errmsg("無法讀取NCCC原因碼 (cca_SYS_PARM1-NCCC).");
    // return;
    // }
    // is_neg_cap =col_nvl("neg_cap","0");
    // }

    if (eqIgno(isBinType, "J")) {
      isGoingArea = wp.itemNvl("jcb_area_1", "0") + wp.itemNvl("jcb_area_2", "0")
          + wp.itemNvl("jcb_area_3", "0") + wp.itemNvl("jcb_area_4", "0")
          + wp.itemNvl("jcb_area_5", "0");
    } else if (eqIgno(isBinType, "V")) {
      isGoingArea = wp.itemNvl("vis_area_1", " ") + wp.itemNvl("vis_area_2", " ")
          + wp.itemNvl("vis_area_3", " ") + wp.itemNvl("vis_area_4", " ")
          + wp.itemNvl("vis_area_5", " ") + wp.itemNvl("vis_area_6", " ")
          + wp.itemNvl("vis_area_7", " ") + wp.itemNvl("vis_area_8", " ")
          + wp.itemNvl("vis_area_9", " ");
    } else if (eqIgno(isBinType, "M")) {
      isGoingArea = wp.itemNvl("mast_area_1", " ") + this.strMid(wp.itemStr("mast_date1"), 2, 6)
          + wp.itemNvl("mast_area_2", " ") + this.strMid(wp.itemStr("mast_date2"), 2, 6)
          + wp.itemNvl("mast_area_3", " ") + this.strMid(wp.itemStr("mast_date3"), 2, 6)
          + wp.itemNvl("mast_area_4", " ") + this.strMid(wp.itemStr("mast_date4"), 2, 6)
          + wp.itemNvl("mast_area_5", " ") + this.strMid(wp.itemStr("mast_date5"), 2, 6)
          + wp.itemNvl("mast_area_6", " ") + this.strMid(wp.itemStr("mast_date6"), 2, 6);
      for (int ii = 0; ii < 6; ii++) {
        isMOutgoArea[ii] =
            wp.itemNvl("mast_area_" + ii, " ") + this.strMid(commDate.dateAdd(wp.itemStr("mast_date" + ii),0,0,1), 2, 6);
      }
    }
  }

  void wfOutgoingDelete() {
    if (empty(isVmjReasonOri))
      return;

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p4Reason = isVmjReasonOri;
    ooOutgo.p5DelDate = colStr("oppo.neg_del_date");
    ooOutgo.p2BinType = isBinType;
    ooOutgo.p7Region = isGoingAreaOri;
    ooOutgo.p6VipAmt = "0";
    if (eqIgno(isBinType, "M")) {
      if (empty(isGoingAreaOri)) {
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC102"
        ooOutgo.oppoMasterReq2("3");
      } else {
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC102"
        ooOutgo.oppoMasterReq2("3");
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC103"
        ooOutgo.oppoMasterReq("3", isMOutgoAreaOri);
      }
    } else if (eqIgno(isBinType, "J")) {
      // "開始傳送 ["+is_card_no+"] 至 JCB......"
      ooOutgo.oppoJcbReq("0");
    } else if (eqIgno(isBinType, "V")) {
      // "開始傳送 ["+is_card_no+"] 至 VISA......"
      ooOutgo.oppoVisaReq("3");
    }
    // wf_set_lbl_m();
  }

  void wfOutgoingUpdate(String aAction) {
    // ls_card_type = mid(as_card_type,1,1)
    // ls_vis_purg_date1 = dw_4.Object.vis_purg_date1[1]
    // IF ls_vis_purg_date1 = '' OR IsNull(ls_vis_purg_date1) THEN ls_vis_purg_date1 =
    // is_neg_del_date

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p4Reason = isVmjReason;
    ooOutgo.p2BinType = isBinType;
    ooOutgo.p7Region = isGoingArea;
    ooOutgo.p6VipAmt = "0";

    if (eqIgno(isBinType, "M")) {
      if (notEmpty(isGoingAreaOri) && empty(isGoingArea)) {
        wfOutgoingDelete();
        aAction = "1";
      }
//      if (eqIgno(aAction, "2"))
//        aAction = "1";
      ooOutgo.masterDate = wp.itemStr2("neg_del_date");
      if (empty(ooOutgo.masterDate))
        ooOutgo.masterDate = colStr("card.new_end_date");
      if (empty(isGoingArea)) {
        // "開始傳送 ["+is_card_no+"] 至 Master......MCC102"
        ooOutgo.oppoMasterReq2(aAction);
      } else {
        // "開始傳送 ["+is_card_no+"] 至 Master......MCC102"
        ooOutgo.oppoMasterReq2(aAction);
        if(empty(isGoingAreaOri)&&notEmpty(isGoingArea)){
        	aAction = "1";
        }
        // "開始傳送 ["+is_card_no+"] 至 Master......MCC103"
        ooOutgo.oppoMasterReq(aAction, this.isMOutgoArea);
      }
    } else if (eqIgno(isBinType, "J")) {
      
      String lsPurgDate = commDate.dateAdd(wp.itemStr2("jcb_date1"),0,0,1);
      if (empty(lsPurgDate)) {
        lsPurgDate = wp.itemStr2("neg_del_date");
      }
      ooOutgo.p5DelDate = lsPurgDate;
      // "開始傳送 ["+is_card_no+"] 至 JCB......"
      ooOutgo.oppoJcbReq(aAction);
    } else if (eqIgno(isBinType, "V")) {
      String lsPurgDate = commDate.dateAdd(wp.itemStr2("vis_purg_date_1"),0,0,1);
      if (empty(lsPurgDate))
        lsPurgDate = wp.itemStr2("neg_del_date");
      ooOutgo.p5DelDate = lsPurgDate;
      // "開始傳送 ["+is_card_no+"] 至 VISA......"
      ooOutgo.oppoVisaReq(aAction);
    }
    // wf_set_lbl_m();
  }

//  void wfDelSpecial() {
//
//    strSql = "select spec_flag from cca_card_base" + " where 1=1 and card_no = ? "; 
////    + commSqlStr.col(cardNo, "card_no");
//    sqlSelect(strSql,new Object[] {cardNo});
//    if (sqlRowNum <= 0)
//      return;
//    // -無特指, 偽卡停用-
//    if (colNeq("spec_flag", "Y") || eqIgno(aaType[1], "6"))
//      return;
//
//    // --
//    strSql = "update cca_card_base set" + " spec_status =''" + ", spec_flag ='N'"
//        + ", spec_mst_vip_amt =0" + ", spec_del_date =''" + ", spec_remark =''" + ","
//        + commSqlStr.setModxxx(modUser, modPgm) + " where card_no =?";
//    setString2(1, cardNo);
//    sqlExec(strSql);
//    if (sqlRowNum != 1) {
//      sqlErr("CCA_CARD_BASE.update");
//      return;
//    }
//
//    // ----------------
//    strSql = "delete cca_special_visa where card_no =?";
//    setString2(1, cardNo);
//    sqlExec(strSql);
//    if (sqlRowNum < 0) {
//      sqlErr("cca_special_visa.Delete");
//      return;
//    }
//
//    // -insert cca_spec_his-
//    strSql = "insert into cca_spec_his (" + "  log_date " + ", log_time" + ", card_no"
//        + ", bin_type" + ", from_type" + ", aud_code " + ", pgm_id " + ", log_user " + ") values ("
//        + "" + commSqlStr.sysYYmd + "," + commSqlStr.sysTime + ",?,?,'1','D'" // card_no
//        + ", ?, ? )"; // pgm_id
//    setString2(1, cardNo);
//    setString(isBinType);
//    setString(modPgm);
//    setString(modUser);
//    sqlExec(strSql);
//    if (this.sqlRowNum <= 0) {
//      sqlErr("cca_spec_his.ADD");
//      return;
//    }
//
//  }

  @Override
  public int dbInsert() {
    errmsg("未提供 [新增功能]");
    return rc;
  }

  void updateCrdCard(String aCardNo) {
    // -未修改原因-
    if (!ibChange)
      return;
    if (empty(aCardNo)) {
      errmsg("卡號: 不可空白");
      return;
    }

    if (ibDebit) {
      strSql = "update dbc_card set " + " current_code = :current_code"
          + ", oppost_reason = :oppo_reason, " + commSqlStr.setModxxx(modUser, modPgm)
          + " where card_no =:card_no";
    } else {
      strSql = "update crd_card set " + " current_code = :current_code"
          + ", oppost_reason = :oppo_reason, " + commSqlStr.setModxxx(modUser, modPgm)
          + " where card_no =:card_no";
    }

    setString2("current_code", wp.itemStr2("oppo_type2"));
    setString2("oppo_reason", wp.itemStr2("oppo_reason2"));
    setString2("card_no", aCardNo);
    sqlExec(strSql);
    if (sqlRowNum != 1) {
      errmsg("update CRD[DBC]_CARD error; " + this.sqlErrtext);
    }

  }

	void updateCcaOpposition(String aCardNo) {
		if (empty(aCardNo)) {
			errmsg("update_cca_opposition: 卡號不可空白");
			return;
		}

		String sqlSelect = "select count(*) as cnt from cca_opposition where card_no = :card_no ";
		setString("card_no", aCardNo);
		sqlSelect(sqlSelect);
		int cnt = colInt("cnt");

		sp = new busi.SqlPrepare();
		if (cnt > 0) {
			sp.sql2Update("cca_opposition", wp);
		} else {
			sp.sql2Insert("cca_opposition", wp);
			sp.ppstr("card_no", aCardNo);
			sp.ppymd("crt_date");
			sp.pptime("crt_time");
			sp.ppstr("crt_user", modUser);
			sp.ppdate("mod_time");
			sp.ppstr("mod_user", modUser);
			sp.ppstr("mod_pgm", modPgm);
			sp.ppnum("mod_seqno", 1);
		}
		sp.ppstr("from_type", "1");
		sp.ppstr("oppo_user", modUser);
		// sp.ppymd("oppo_date");
		// sp.pptime("oppo_time");
		sp.ppymd("chg_date");
		sp.pptime("chg_time");
		sp.ppstr("chg_user", modUser);
		sp.ppstr2("renew_flag", wp.itemNvl("renew_flag", "N"));
		sp.ppstr2("mail_branch", wp.itemStr2("mail_branch"));
		sp.ppstr2("opp_remark", wp.itemStr2("opp_remark"));
		// sp.ppss("lost_fee_flag",wp.item_ss("lost_fee_flag"));
		// -變更停掛原因-
		sp.ppstr2("oppo_type", wp.itemStr2("oppo_type2"));
		sp.ppstr2("oppo_status", wp.itemStr2("oppo_reason2"));
		sp.ppstr2("neg_del_date", wp.itemStr2("neg_del_date"));
		sp.ppstr2("excep_flag", wp.itemStr2("excep_flag"));
		sp.ppstr2("except_proc_flag", "1");
		setVmjArea();
		sp.ppstr("neg_resp_code", nvl(ooOutgo.negRespCode, ""));
		sp.ppstr("visa_resp_code", nvl(ooOutgo.vmjRespCode, ""));
		if (cnt > 0) {
			sp.modxxx(modUser, modPgm);
			sp.sql2Where(" where card_no =?", cardNo);
		}

		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum != 1) {
			errmsg("update CCA_OPPOSITION error; " + this.sqlErrtext);
		}
	}

  void setVmjArea() {
    if (wp.itemEmpty("excep_flag")) {
      sp.ppstr("vis_area_1", "");
      sp.ppstr("vis_area_2", "");
      sp.ppstr("vis_area_3", "");
      sp.ppstr("vis_area_4", "");
      sp.ppstr("vis_area_5", "");
      sp.ppstr("vis_area_6", "");
      sp.ppstr("vis_area_7", "");
      sp.ppstr("vis_area_8", "");
      sp.ppstr("vis_area_9", "");
      sp.ppstr("vis_purg_date_1", "");
      sp.ppstr("vis_purg_date_2", "");
      sp.ppstr("vis_purg_date_3", "");
      sp.ppstr("vis_purg_date_4", "");
      sp.ppstr("vis_purg_date_5", "");
      sp.ppstr("vis_purg_date_6", "");
      sp.ppstr("vis_purg_date_7", "");
      sp.ppstr("vis_purg_date_8", "");
      sp.ppstr("vis_purg_date_9", "");
      return;
    }

    if (wp.itemEq("bin_type", "V")) {
      sp.ppstr("vis_area_1", wp.itemStr("vis_area_1"));
      sp.ppstr("vis_area_2", wp.itemStr("vis_area_2"));
      sp.ppstr("vis_area_3", wp.itemStr("vis_area_3"));
      sp.ppstr("vis_area_4", wp.itemStr("vis_area_4"));
      sp.ppstr("vis_area_5", wp.itemStr("vis_area_5"));
      sp.ppstr("vis_area_6", wp.itemStr("vis_area_6"));
      sp.ppstr("vis_area_7", wp.itemStr("vis_area_7"));
      sp.ppstr("vis_area_8", wp.itemStr("vis_area_8"));
      sp.ppstr("vis_area_9", wp.itemStr("vis_area_9"));
      sp.ppstr("vis_purg_date_1", wp.itemStr2("vis_purg_date_1"));
      return;
    }
    if (wp.itemEq("bin_type", "M")) {
      sp.ppstr("vis_area_1", wp.itemStr("mast_area_1"));
      sp.ppstr("vis_area_2", wp.itemStr("mast_area_2"));
      sp.ppstr("vis_area_3", wp.itemStr("mast_area_3"));
      sp.ppstr("vis_area_4", wp.itemStr("mast_area_4"));
      sp.ppstr("vis_area_5", wp.itemStr("mast_area_5"));
      sp.ppstr("vis_area_6", wp.itemStr("mast_area_6"));
      sp.ppstr("vis_purg_date_1", wp.itemStr("mast_date1"));
      sp.ppstr("vis_purg_date_2", wp.itemStr("mast_date2"));
      sp.ppstr("vis_purg_date_3", wp.itemStr("mast_date3"));
      sp.ppstr("vis_purg_date_4", wp.itemStr("mast_date4"));
      sp.ppstr("vis_purg_date_5", wp.itemStr("mast_date5"));
      sp.ppstr("vis_purg_date_6", wp.itemStr("mast_date6"));
      return;
    }
    if (wp.itemEq("bin_type", "J")) {
      sp.ppstr("vis_area_1", wp.itemNvl("jcb_area_1", "0"));
      sp.ppstr("vis_area_2", wp.itemNvl("jcb_area_2", "0"));
      sp.ppstr("vis_area_3", wp.itemNvl("jcb_area_3", "0"));
      sp.ppstr("vis_area_4", wp.itemNvl("jcb_area_4", "0"));
      sp.ppstr("vis_area_5", wp.itemNvl("jcb_area_5", "0"));
      sp.ppstr("vis_purg_date_1", wp.itemStr("jcb_date1"));
      return;
    }
  }

  @Override
  public int dbUpdate() {
    ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
//    if (wp.itemNum("mod_seqno") != colNum("oppo.mod_seqno")) {
//      errmsg(errOtherModify);
//      return rc;
//    }

    updateCrdCard(cardNo);
    if (rc != 1)
      return rc;

    updateCcaOpposition(cardNo);
    if (rc != 1)
      return rc;

    // -掛失補發: crd_g004.Insert crd_emboss_tmp-
    insertOnbat2ecs(cardNo);
    // -補發-
    // if (wp.item_eq("renew_flag","Y")) {
    // insert_crd_emboss();
    // }

//    wfDelSpecial();
//    if (rc != 1)
//      return rc;

    if (ibDebit) {
//      // --
//      oo_outgo.parmClear();
//      oo_outgo.p3BankAcctno = colStr("card.bank_acct_no");
//      oo_outgo.p4Reason = is_neg_reason;
//      oo_outgo.p5DelDate = wp.itemStr2("neg_del_date");
//      oo_outgo.p2BinType = colStr("card.bin_type");
//      oo_outgo.oppoIbmNegfile("2");
    }
    return rc;
  }

  void selectCrdCard(String aCardNo) {
    if (ibDebit) {
      strSql =
          "select major_card_no , current_code, bin_type, acct_no as bank_acct_no"
              + ", new_end_date, card_type, acct_type, id_p_seqno, p_seqno as acno_p_seqno ,reissue_date ,oppost_date ,reissue_status "
              + " from dbc_card" + " where card_no =?";
      // +commSqlStr.col(cardNo,"card_no");
      setString2(1, aCardNo);
    } else {
      strSql =
          "select major_card_no , current_code, bin_type, combo_acct_no as bank_acct_no"
              + ", new_end_date, card_type, acct_type, id_p_seqno, acno_p_seqno ,reissue_date ,oppost_date ,reissue_status " 
        	  + " from crd_card" + " where card_no =?";
      // + commSqlStr.col(cardNo, "card_no");
      setString2(1, aCardNo);
    }
    daoTid = "card.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("卡片[信用卡/Debit卡] 資料不存在");
      return;
    }

    if (colEq("card.current_code", "0")) {
      errmsg("卡片為流通卡, 不須撤掛及不可變更[停掛類別]");
      return;
    }

    if (ibDebit && colEmpty("card.bank_acct_no")) {
      errmsg("Debit卡: 無金融帳號,不可做撤掛");
      return;
    }
    
    return;
  }

  void selectCcaOpposition(String lsCardNo) {
    strSql = "select * from cca_opposition" + " where 1=1 and card_no = ? ";
    daoTid = "oppo.";
    sqlSelect(strSql,new Object[] {lsCardNo});
    if (sqlRowNum > 0) {
//      sqlErr("cca_opposition.select error, kk[" + lsCardNo + "]");
			ibDebit = colEq("oppo.debit_flag", "Y");
			isNegReasonOri = colStr("oppo.mst_reason_code");
			isVmjReasonOri = colStr("oppo.vis_reason_code");
			isFiscReasonOri = colStr("oppo.fisc_reason_code");
			isBinType = colStr("oppo.bin_type");

		    isGoingAreaOri = "";
		    if (eqIgno(isBinType, "J")) {
		      isGoingAreaOri = colNvl("oppo.vis_area_1", "0") + colNvl("oppo.vis_area_2", "0")
		          + colNvl("oppo.vis_area_3", "0") + colNvl("oppo.vis_area_4", "0")
		          + colNvl("oppo.vis_area_5", "0");
		    } else if (eqIgno(isBinType, "V")) {
		      isGoingAreaOri = colNvl("oppo.vis_area_1", " ") + colNvl("oppo.vis_area_2", " ")
		          + colNvl("oppo.vis_area_3", " ") + colNvl("oppo.vis_area_4", " ")
		          + colNvl("oppo.vis_area_5", " ") + colNvl("oppo.vis_area_6", " ")
		          + colNvl("oppo.vis_area_7", " ") + colNvl("oppo.vis_area_8", " ")
		          + colNvl("oppo.vis_area_9", " ");
		    } else if (eqIgno(isBinType, "M")) {
		      isGoingAreaOri =
		          colNvl("oppo.vis_area_1", " ") + this.strMid(colStr("oppo.vis_purg_date_1"), 2, 6)
		              + colNvl("oppo.vis_area_2", " ") + this.strMid(colStr("oppo.vis_purg_date_2"), 2, 6)
		              + colNvl("oppo.vis_area_3", " ") + this.strMid(colStr("oppo.vis_purg_date_3"), 2, 6)
		              + colNvl("oppo.vis_area_4", " ") + this.strMid(colStr("oppo.vis_purg_date_4"), 2, 6)
		              + colNvl("oppo.vis_area_5", " ") + this.strMid(colStr("oppo.vis_purg_date_5"), 2, 6)
		              + colNvl("oppo.vis_area_6", " ") + this.strMid(colStr("oppo.vis_purg_date_6"), 2, 6);
		      int rr = 1;
		      for (int ii = 0; ii < 6; ii++) {
		        isMOutgoAreaOri[ii] = colNvl("oppo.vis_area_" + rr, " ")
		            + this.strMid(colStr("oppo.vis_purg_date_" + rr), 2, 6);
		        rr++;
		      }
		    }
    }else{
    	ibDebit = wp.itemEq("debit_flag", "Y");
    	isBinType = wp.itemStr("bin_type");
       	String sqlSelect = "select oppost_reason from crd_card where card_no = :card_no";
    	if(ibDebit) {
    		sqlSelect = "select oppost_reason from dbc_card where card_no = :card_no";
    	}
    	setString("card_no",wp.itemStr("card_no"));
    	sqlSelect(sqlSelect);
    	setOutgoingValue(colStr("oppost_reason"));
		isNegReasonOri = isNegReason;
		isVmjReasonOri = isVmjReason;
		isFiscReasonOri = isFiscReason;
    }

  }

  boolean checkSupCard(String lsMajorCardNo) {
    String sql1 = "";
    if (ibDebit) {
      sql1 = " select current_code as major_current_code from dbc_card where card_no = ? ";
    } else {
      sql1 = " select current_code as major_current_code from crd_card where card_no = ? ";
    }

    sqlSelect(sql1, new Object[] {lsMajorCardNo});

    if (!eqIgno(colStr("major_current_code"), "0"))
      return false;

    return true;
  }

  void dataCheckDelete(String lsCardNo) {
    if (empty(lsCardNo)) {
      errmsg("撤掛卡號: 不可空白");
      return;
    }
    selectCcaOpposition(lsCardNo);
    selectCrdCard(lsCardNo);
    if (rc != 1)
      return;

    if (wp.itemEq("sup_flag", "1")) {
      if (checkSupCard(colStr("card.major_card_no")) == false) {
        errmsg("正卡為無效卡 不可撤掛");
        return;
      }
    }

    if (colEq("oppo.logic_del", "Y")) {
      errmsg("卡片己撤掛, 不需再執行[撤掛]");
      return;
    }

//    if(!wp.iempty("electronic_code")){
//    	if(!wp.itemEq("electronic_code", "00")){
//            errmsg("票證之卡片不可撤掛");
//            return;
//        }
//    }

    String oppostDate = colStr("card.oppost_date");
    if(empty(oppostDate)){
    	oppostDate = getSysDate();
    }
    if(!empty(colStr("card.reissue_status"))&&!colEq("card.reissue_status", "1")&&chkStrend(oppostDate, colStr("card.reissue_date"))==1){
        errmsg("卡片已重製不可撤掛");
        return;
    }

    
//    if (rc != 1)
//      return;
    
    // --不可撤掛
    /*
     * 2018-10-24 發卡部要求 所有卡片皆可撤掛 is_sql="select count(*) as hce_cnt from hce_card"
     * +" where 1=1"+commSqlStr.col(ls_card_no,"card_no") +commSqlStr.rownum(11) ; sqlSelect(is_sql); if
     * (col_int("hce_cnt")>0) { errmsg("有申請行動支付, 不可撤掛"); return; }
     * 
     * is_sql = " select count(*) as tsc_cnt from tsc_card " + " where 1=1 "+commSqlStr.col(ls_card_no,
     * "card_no") ; sqlSelect(is_sql); if (col_int("tsc_cnt")>0) { errmsg("有悠遊卡, 不可撤掛"); return; }
     * 
     * is_sql = " select count(*) as ips_cnt from ips_card " + " where 1=1 "+commSqlStr.col(ls_card_no,
     * "card_no") ; sqlSelect(is_sql); if (col_int("ips_cnt")>0) { errmsg("有一卡通, 不可撤掛"); return; }
     * 
     * is_sql = " select count(*) as combo_cnt from crd_card " +
     * " where 1=1 and combo_indicator ='Y' "+commSqlStr.col(ls_card_no, "card_no") ; sqlSelect(is_sql);
     * if (col_int("combo_cnt")>0) { errmsg("有COMBO卡, 不可撤掛"); return; }
     */
    // --
    // -set outgoing value-
    // set_outgoing_value();
    // if (rc!=1)
    // return;

    // -outgoing-
    ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    //ooOutgo.isCallAutoAuth = false;
    //ooOutgo.iscalltwmp = true;
    ooOutgo.isDebit = wp.itemEq("debit_flag", "Y");
    ooOutgo.parmClear();
    ooOutgo.p1CardNo = lsCardNo;
    ooOutgo.p2BinType = isBinType;
    ooOutgo.p5DelDate = colStr("oppo.neg_del_date");
    ooOutgo.p7Region = isGoingAreaOri;
    ooOutgo.p8NewEndDate = wp.itemStr("new_end_date");
    if (wp.respHtml.indexOf("ccam2016_detl") >= 0) {
    	 ooOutgo.p9CurrentCode = wp.itemStr("oppo_type2");
    	 ooOutgo.p10OppostReason = wp.itemStr("oppo_reason2");
	  }else{
		  ooOutgo.p9CurrentCode = wp.itemStr("oppo_type");
		  ooOutgo.p10OppostReason = wp.itemStr("oppo_reason");
	  }
    ooOutgo.electronicCode = wp.itemStr("electronic_code");
    String electronicCode = ooOutgo.electronicCode ; 
    ooOutgo.p11ElectronicCardno = selectCardNo(electronicCode,cardNo);
    if (!empty(isNegReasonOri)) {
    	ooOutgo.p4Reason = isNegReasonOri;
    	ooOutgo.fiscReason = isFiscReasonOri;
        rc = ooOutgo.oppoNegId("3");
        if (rc != 1) {
          errmsg("Neg_id:" + ooOutgo.getMsg());
          return;
        }
        wp.colSet("neg_resp_code", ooOutgo.negRespCode);
    }
    //是否送國際組織為N return
    if(wp.itemEq("tt_excep_flag", "N")){
        return;
    }
    if (!empty(isVmjReasonOri)) {
    	ooOutgo.p4Reason = isVmjReasonOri;
      if (colEq("card.bin_type", "M")) {
        ooOutgo.p6VipAmt = "0";
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC102"
        rc = ooOutgo.oppoMasterReq2("3");
        if (rc != 1) {
          errmsg("Master_req2:" + ooOutgo.getMsg());
          return;
        }
        if (!empty(isGoingAreaOri)) {
          // "開始傳送 ["+is_card_no+"] 至 Master...MCC103"
          rc = ooOutgo.oppoMasterReq("3", isMOutgoAreaOri);
          if (rc != 1) {
            errmsg("Master_req:" + ooOutgo.getMsg());
            return;
          }
        }
      } else if (colEq("card.bin_type", "J")) {
        // "開始傳送 ["+is_card_no+"] 至 JCB......"
        rc = ooOutgo.oppoJcbReq("0");
        if (rc != 1) {
          errmsg("Jcb_req:" + ooOutgo.getMsg());
          return;
        }
      } else if (colEq("card.bin_type", "V")) {
        // "開始傳送 ["+is_card_no+"] 至 VISA......"
        rc = ooOutgo.oppoVisaReq("3");
        if (rc != 1) {
          errmsg("Visa_req:" + ooOutgo.getMsg());
          return;
        }
      }
      wp.colSet("vmj_resp_code", ooOutgo.vmjRespCode);
    }
    // --IBM-----------
//    if (ib_debit && !empty(is_neg_reason_ori)) {
//      oo_outgo.parmClear();
//      oo_outgo.p1CardNo = ls_card_no;
//      oo_outgo.p3BankAcctno = colStr("oppo.bank_acct_no");
//      oo_outgo.p4Reason = is_neg_reason_ori;
//      oo_outgo.p5DelDate = colStr("oppo.neg_del_date");
//      oo_outgo.p2BinType = is_bin_type;
//      // delete
//      rc = oo_outgo.oppoIbmNegfile("3");
//      if (rc != 1) {
//        errmsg("IBM:" + oo_outgo.getMsg());
//        return;
//      }
//      wp.colSet("neg_resp_code", oo_outgo.negRespCode);
//    }
  }

  @Override
  public int dbDelete() {
    ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    actionInit("D");
    cardNo = wp.itemStr2("card_no");
    if (empty(cardNo)) {
      errmsg("撤掛[卡號]: 不可空白");
      return rc;
    }
    dataCheckDelete(cardNo);
    if (rc != 1)
      return rc;

    // -update cca_opposition-
    updateCcaOppositionD(cardNo);
    if (rc != 1)
      return rc;
    // -update crd_card-
    if (!ibDebit) {
      updateCrdCardD(cardNo);
    } else {
      updateDbcCardD(cardNo);
    }
    if (rc != 1)
      return rc;
    // -撤掛: crd_g004.delete crd_emboss_tmp-
    // String ls_oppo_type =col_ss("oppo.oppo_type");
    insertOnbat2ecsD(cardNo);
    if (rc != 1)
      return rc;

    // //--Reason:Q1 附卡連動撤掛
    // delSupFlag(cardNo);



    return rc;
  }

  // --撤掛
	public int updateCcaOppositionD(String lsCardNo) {
		msgOK();

		String sqlSelect = "select count(*) as cnt from cca_opposition where card_no = :card_no ";
		setString("card_no", wp.itemStr("card_no"));
		sqlSelect(sqlSelect);
		int cnt = colInt("cnt");
		if (cnt > 0) {
			strSql = "update cca_opposition set" + " logic_del ='Y'" + ", logic_del_date =" + commSqlStr.sysYYmd
					+ ", logic_del_time =" + commSqlStr.sysTime + ", logic_del_user =:del_user"
					+ ", neg_resp_code =:neg_resp_code" + ", visa_resp_code =:vmj_resp_code" + ", chg_date ="
					+ commSqlStr.sysYYmd + ", chg_time =" + commSqlStr.sysTime + ", chg_user =:chg_user" + ", opp_remark =''"
					+ ", vis_area_1 ='' " + ", vis_area_2 ='' " + ", vis_area_3 ='' " + ", vis_area_4 ='' "
					+ ", vis_area_5 ='' " + ", vis_area_6 ='' " + ", vis_area_7 ='' " + ", vis_area_8 ='' "
					+ ", vis_area_9 =''" + ", vis_purg_date_1 ='' " + ", vis_purg_date_2 ='' "
					+ ", vis_purg_date_3 ='' " + ", vis_purg_date_4 ='' " + ", vis_purg_date_5 ='' "
					+ ", vis_purg_date_6 ='' " + ", vis_purg_date_7 ='' " + ", vis_purg_date_8 ='' "
					+ ", vis_purg_date_9 ='' " + ", renew_flag =''" + "," + commSqlStr.setModxxx(modUser, modPgm)
					+ " where 1=1 and card_no =:card_no ";
			setString2("del_user", modUser);
			setString2("neg_resp_code", ooOutgo.negRespCode);
			setString2("vmj_resp_code", ooOutgo.vmjRespCode);
			setString2("chg_user", modUser);			
			setString2("card_no",lsCardNo);
			sqlExec(strSql);
			if (sqlRowNum <= 0) {
				errmsg("cca_opposition.update error; kk[%s]" + this.sqlErrtext, lsCardNo);
			}
		}else{
			sp = new busi.SqlPrepare();
			sp.sql2Insert("cca_opposition");
			sp.ppstr("card_no", wp.itemStr("card_no"));
			sp.ppstr("logic_del", "Y");
			sp.ppstr("debit_flag", wp.itemStr2("debit_flag"));
			sp.ppstr("bin_type", wp.itemStr2("bin_type"));
			sp.ppstr("from_type", "1");
			sp.ppstr("oppo_type", wp.itemStr2("oppo_type"));
			sp.ppstr("oppo_status", wp.itemStr2("oppo_reason"));
			sp.ppstr("oppo_user", modUser);
			sp.ppymd("oppo_date");
			sp.pptime("oppo_time");
			sp.ppstr("neg_del_date", wp.itemStr2("neg_del_date"));
			sp.ppstr("renew_flag", wp.itemStr("renew_flag"));
			sp.ppstr("cycle_credit", wp.itemStr2("cycle_credit"));
			sp.ppstr("mail_branch", wp.itemStr2("mail_branch"));
			sp.ppstr("lost_fee_flag", wp.itemStr2("lost_fee_code"));
			sp.ppstr2("excep_flag", wp.itemStr2("excep_flag"));
			sp.ppstr("except_proc_flag", "0");
			sp.ppymd("logic_del_date");
			sp.pptime("logic_del_time");
			sp.ppstr("logic_del_user", modUser);
			sp.ppymd("chg_date");
			sp.pptime("chg_time");
			sp.ppstr("chg_user", modUser);
			sp.ppymd("crt_date");
			sp.pptime("crt_time");
			sp.ppstr("crt_user", modUser);
			sp.ppdate("mod_time");
			sp.ppstr("mod_user", modUser);
			sp.ppstr("mod_pgm", modPgm);
			sp.ppnum("mod_seqno", 1);
		    sqlExec(sp.sqlStmt(), sp.sqlParm());
		    if (sqlRowNum == 0) {
		      errmsg("insert CCA_OPPOSITION error; " + this.sqlErrtext);
		    }
		}
		
		return rc;
	}

  public int updateCrdCardD(String lsCardNo) {
    msgOK();
    strSql =
        "update crd_card set " + " current_code ='0'" + ", oppost_reason =''" + ", oppost_date ='' "
            + ", lost_fee_code = ''" + "," + commSqlStr.setModxxx(modUser, modPgm) + " where card_no =?"; // +commSqlStr.col(cardNo,"card_no");

    setString2(1, lsCardNo);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      sqlErr("crd_card." + lsCardNo);
      return rc;
    }

    return rc;
  }

  public int updateDbcCardD(String lsCardNo) {
    msgOK();
    strSql =
        "update dbc_card set " + " current_code ='0'" + ", oppost_reason =''" + ", oppost_date =''"
            + ", lost_fee_code =''" + "," + commSqlStr.setModxxx(modUser, modPgm) + " where card_no =?"; // +commSqlStr.col(cardNo,"card_no");
    setString2(1, lsCardNo);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      sqlErr("crd_card." + lsCardNo);
    }
    return rc;
  }

  public int insertOnbat2ecsD(String lsCardNo) {
    msgOK();
    String lsAcctType = colStr("card.acct_type");
    String lsIdPseqno = colStr("card.id_p_seqno");
    String lsPseqno = colStr("card.acno_p_seqno");

    strSql = "insert into onbat_2ecs (" + " trans_type, to_which, dog, proc_mode, proc_status"
        + ", card_no, opp_type,acct_type, id_p_seqno, acno_p_seqno" + " ) values (" + " ?, '1', "
        + commSqlStr.sysdate + ", 'N', 0" + ",?,'0',?,?,?" + " )";
    if (colEq("oppo.oppo_type", "3")) {
      setString2(1, "5");
    } else
      setString2(1, "6");
    setString2(2, lsCardNo);
    setString(lsAcctType);
    setString(lsIdPseqno);
    setString(lsPseqno);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("ECS on-line 寫檔失敗!" + sqlErrtext);
    }
    return rc;
  }

  public int delSupFlag(String aCardNo) {
    ibDelSup = true;

    msgOK();
    String lsCardNo = aCardNo;
    if (empty(lsCardNo)) {
      errmsg("附卡停用取消, 卡號不可空白");
      return rc;
    }

    // --確認附卡當日有無停掛
    String sql2 = " select " + " count(*) as db_cnt " + " from cca_opposition "
        + " where card_no = ? " + " and oppo_date =" + commSqlStr.sysYYmd;

    String sql1 = "select card_no " + " from crd_card " + " where 1=1 " + " and major_card_no = ? "
        + " and sup_flag = '1' " + " and current_code <> '0' " + " and oppost_reason = 'Q1' ";
    sqlSelect(sql1, new Object[] {lsCardNo});
    if (sqlRowNum <= 0) {
      return 1;
    }
    int llNrow = sqlRowNum;
    for (int ll = 0; ll < llNrow; ll++) {
      String lsCardNo2 = colStr(ll, "card_no");
      sqlSelect(sql2, new Object[] {lsCardNo2});
      if (colNum("db_cnt") == 0 || sqlRowNum <= 0)
        continue;

      dataCheckDelete(lsCardNo2);
      if (rc != 1)
        break;

      updateCcaOppositionD(lsCardNo2);
      if (rc != 1)
        break;

      if (ibDebit) {
        updateDbcCardD(lsCardNo2);
      } else {
        updateCrdCardD(lsCardNo2);
      }
      if (rc != 1)
        break;

      insertOnbat2ecsD(lsCardNo2);
      if (rc != 1)
        break;
    }

    return rc;
  }

  // --

  void insertOnbat2ecs(String aCardNo) {
    String lsAcctType = colStr("card.acct_type");
    String lsIdPseqno = colStr("card.id_p_seqno");
    String lsPseqno = colStr("card.acno_p_seqno");

    String lsTranType = "6";
    String lsOppoType = aaType[0];
    String lsOppoReason = aaReason[0];
    if (ibChange) {
      lsOppoType = aaType[1];
      lsOppoReason = aaReason[1];
    }
    if (eqIgno(lsOppoType, "3"))
      lsTranType = "5";

    sp.sql2Insert("onbat_2ecs");
    sp.ppstr2("trans_type", lsTranType);
    sp.ppstr2("to_which", "1");
    sp.ppdate("dog");
    sp.ppstr2("proc_mode", "O");
    sp.ppint("proc_status", 0);
    sp.ppstr2("acct_type", lsAcctType);
    sp.ppstr2("id_p_seqno", lsIdPseqno);
    sp.ppstr2("acno_p_seqno", lsPseqno);
    sp.ppstr2("card_no", aCardNo);
    sp.ppstr2("opp_type", lsOppoType);
    sp.ppstr2("opp_reason", lsOppoReason);
    sp.ppstr2("opp_date", wp.itemStr2("oppo_date"));
    sp.ppstr2("is_renew", wp.itemNvl("renew_flag", "N"));
    // sp.ppp("curr_tot_lost_amt", 0);
    sp.ppstr2("mail_branch", wp.itemStr2("mail_branch"));
    // sp.ppp("lost_fee_flag",wp.item_nvl("lost_fee_flag","N"));
    if (ibDebit)
      sp.ppstr2("debit_flag", "Y");
    else
      sp.ppstr2("debit_flag", "N");

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      sqlErr("ONBAT_2ECS.insert");
    }
    return;
  }

  String selectCardNo(String electronicCode,String cardNo){
	  String sqlSelect = "";
	  String wkCardno = "";
	  if(electronicCode.equals("01")){
	      if(ibDebit){
	    	  sqlSelect = "select tsc_card_no as wk_electronic_card_no ";
		      sqlSelect	+= " from tsc_vd_card where new_end_date > to_char(sysdate,'yyyymm') ";
		      sqlSelect	+= " and vd_card_no = :card_no ";
	      }else {
	    	  sqlSelect = "select tsc_card_no as wk_electronic_card_no ";
		      sqlSelect	+= " from tsc_card where new_end_date > to_char(sysdate,'yyyymm') ";
		      sqlSelect	+= " and card_no = :card_no ";
	      }
	      sqlSelect += " and current_code = '2' ";
		  setString("card_no",cardNo);
		  sqlSelect(sqlSelect);
		  if (sqlRowNum > 0) {
			  wkCardno = colStr("wk_electronic_card_no");
		  }	
	  }else if(electronicCode.equals("02")){
		  sqlSelect = "select ips_card_no as wk_electronic_card_no "
			  	+ " from ips_card where new_end_date > to_char(sysdate,'yyyymm') ";
	      sqlSelect += " and current_code = '2' ";
	      sqlSelect	+= " and card_no = :card_no ";
		  setString("card_no",cardNo);
		  sqlSelect(sqlSelect);
		  if (sqlRowNum > 0) {
			  wkCardno = colStr("wk_electronic_card_no");
		  }	
	  }else if(electronicCode.equals("03")){
		  sqlSelect = "select ich_card_no as wk_electronic_card_no "
			  	+ " from ich_card where new_end_date > to_char(sysdate,'yyyymm') ";
	      sqlSelect += " and current_code = '2' ";
	      sqlSelect	+= " and card_no = :card_no ";
		  setString("card_no",cardNo);
		  sqlSelect(sqlSelect);
		  if (sqlRowNum > 0) {
			  wkCardno = colStr("wk_electronic_card_no");
		  }	
	  }
	  return wkCardno;
  }
  
  // void set_outgoing_value(String a_oppo_reason) {
  // // -get outgo-reason-
  // is_sql = "select ncc_opp_type, neg_op4_reason as neg_reason,"
  // + " vis_excep_code as visa_reason,"
  // + " mst_auth_code as mast_reason,"
  // + " jcb_excp_code as jcb_reason"
  // + " from cca_opp_type_reason"
  // + " where 1=1"
  // + commSqlStr.col(a_oppo_reason, "opp_status");
  //
  // // String ls_nccc_opp_type="";
  // sqlSelect(is_sql);
  // if (sql_nrow <= 0) {
  // errmsg("讀取NEG原因失敗!");
  // return;
  // }
  // is_neg_reason = col_ss("neg_reason");
  // // ls_nccc_opp_type =col_ss("ncc_opp_type");
  //
  // if (eq_igno(is_bin_type, "V"))
  // this.is_neg_reason = col_ss("visa_reason");
  // else if (this.eq_igno(is_bin_type, "M"))
  // is_vmj_reason = col_ss("mast_reason");
  // else if (this.eq_igno(is_bin_type, "J"))
  // is_vmj_reason = col_ss("jcb_reason");
  //
  // // --
  // is_sql = "select sys_data2 as neg_cap from cca_sys_parm1"
  // + " where sys_id='NCCC'"
  // + commSqlStr.col(is_neg_reason, "sys_key");
  // sqlSelect(is_sql);
  // if (sql_nrow <= 0) {
  // errmsg("無法讀取NCCC原因碼 (cca_SYS_PARM1-NCCC).");
  // return;
  // }
  // is_neg_cap = col_nvl("neg_cap", "0");
  //
  // is_going_area_ori = "";
  // is_going_area = "";
  // if (eq_igno(is_bin_type, "J")) {
  // for (int ii = 1; ii <= 4; ii++) {
  // is_going_area_ori += col_nvl("oppo.vis_area_" + ii, " ");
  // }
  // if (isUpdate()) {
  // is_going_area = wp.item_nvl("jcb_area_1", "0")
  // + wp.item_nvl("jcb_area_2", "0")
  // + wp.item_nvl("jcb_area_3", "0")
  // + wp.item_nvl("jcb_area_4", "0")
  // + wp.item_nvl("jcb_area_5", "0");
  // }
  // }
  // else if (eq_igno(is_bin_type, "V")) {
  // for (int ii = 1; ii <= 9; ii++) {
  // is_going_area_ori += col_nvl("oppo.vis_area_" + ii, " ");
  // }
  // if (isUpdate()) {
  // for(int ii=1; ii<=9; ii++) {
  // is_going_area +=wp.item_nvl("vis_area_"+ii," ");
  // }
  // }
  // }
  // else if (eq_igno(is_bin_type, "M")) {
  // for (int ii = 1; ii <= 6; ii++) {
  // is_going_area_ori +=
  // col_nvl("oppo.vis_area_" + ii, " ") +
  // commString.rpad(this.ss_mid(col_ss("oppo.vis_purg_date_"+ii),2,6),6);
  // }
  // if (isUpdate()) {
  // for (int ii=0; ii<6; ii++) {
  // int nn=ii+1;
  // is_M_outgo_area[ii] =wp.item_nvl("mast_area_"+nn,"
  // ")+commString.rpad(ss_mid(wp.sss("mast_date"+nn),2,6),6);
  // }
  // }
  // }
  // }

  // void wf_set_lbl_m() {
  // wp.col_set("vmj_resp_code",oo_outgo.resp_code);
  // wp.col_set("vmj_reason_code",oo_outgo.vmj_income_reason);
  // }
  // void wf_set_lbl_n() {
  // wp.col_set("neg_resp_code", oo_outgo.resp_code);
  // wp.col_set("neg_reason_code",oo_outgo.neg_income_reason);
  // }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int deleteProhibit() {
    msgOK();

    strSql = "delete crd_prohibit where card_no =:card_no ";
    item2ParmStr("card_no");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete crd_prohibit error !");
    }

    return rc;
  }

}
