package ccam01;

import busi.func.OutgoingOppo;

/**卡片停用公用程式
 * 2019-0610:  JH    p_seqno >>acno_p_seqno
 * 2018-0718:	JH		bugfix
 * 2018-0710:	JH		附卡連動停用
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-04-20 V1.00.01  shiyuqi     處理停掛function
 * 109-06-16 V1.00.02  ryan             updated function dataCheckCcam2012()
 * 110-01-21 V1.00.03  ryan             ooOutgo.ibDebit to ooOutgo.isDebit
 * 111-02-23 V1.00.04  ryan             mark select mob_tpan_info
 * 111-06-08 V1.00.05  ryan             修改ccam2012Update
 * 111-12-14 V1.00.06 Ryan         票證可能有多筆，修改只讀有效起日最新的一筆 
 * 112-07-19 V1.00.06 Wilson         mark wfDelSpecial
 * */ 

public class Ccam2010Func extends busi.FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  double iiCardAcctIdx = 0;
  String isCardNo = "", isBinType = "", cardNo = "", kk2 = "";
  boolean ibDebit = false;
  boolean ibFile = false;
  // String is_outgo_area="";
  busi.SqlPrepare sp = null;
  String isNegReason = ""; // , is_external_reason="";
  String isFiscReason = "";
  // Cca_exception_log oexec=null;
  OutgoingOppo ooOutgo = null;

  // private String is_neg_cap;
  private String isNegDelDate;
  private String isVmjReason;
  private String isOutgoingAreaOri = "";
  private String isOutgoingArea = "";
  private String isVmjReasonOri;
  String[] isMOutgoArea = new String[6];
  String[] isMOutgoAreaOri = new String[6];


  @Override
  public int querySelect() {
    return 0;
  }

  public int outgoingQuery() {
    if (wp.colEq("bin_type", "A")) // || wp.col_eq("debit_flag","Y"))
      return 0;

    // if (empty(is_neg_reason) && empty(is_vmj_reason))
    // return 0;

    ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    ooOutgo.wpCallStatus("");

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = wp.colStr("card_no");
    ooOutgo.p2BinType = wp.colStr("bin_type");
    ooOutgo.oppoNegId("5");
    // wf_set_lbl_n();

    // rtniso = wf_outgoing(ls_date,ls_time,is_card_type)
    if (wp.colEq("bin_type", "M")) {
      // "開始傳送 ["+is_card_no+"] 至 Master...MCC102"
      ooOutgo.parmClear();
      ooOutgo.p1CardNo = wp.colStr("card_no");
      ooOutgo.p4Reason = wp.colStr("is_mast_reason");
      ooOutgo.p6VipAmt = "0";
      ooOutgo.oppoMasterReq2("5");
    } else if (wp.colEq("bin_type", "J")) {
      // "開始傳送 ["+is_card_no+"] 至 JCB......"
      ooOutgo.parmClear();
      ooOutgo.p1CardNo = wp.colStr("card_no");
      ooOutgo.p5DelDate = wp.colStr("is_opp_date_ori");
      ooOutgo.p4Reason = wp.colStr("is_jcb_reason");
      ooOutgo.p7Region = wp.colStr("is_outgoing_area_ori");
      ooOutgo.oppoJcbReq("5");
    } else if (wp.colEq("bin_type", "V")) {
      // "開始傳送 ["+is_card_no+"] 至 VISA......"
      if (empty(wp.colStr("is_outgoing_area_ori")) == false) {
        ooOutgo.parmClear();
        ooOutgo.p1CardNo = wp.colStr("card_no");
        ooOutgo.p4Reason = wp.colStr("is_vis_reason");
        ooOutgo.p5DelDate = wp.colStr("is_opp_date_ori");
        ooOutgo.p7Region = wp.colStr("is_outgoing_area_ori");
        ooOutgo.oppoVisaReq("5");
      } else {
        ooOutgo.parmClear();
        ooOutgo.p1CardNo = wp.colStr("card_no");
        ooOutgo.p4Reason = "41";
        ooOutgo.p5DelDate = wp.colStr("eff_date_end");
        ooOutgo.p7Region = wp.colStr("is_outgoing_area_ori");
        ooOutgo.oppoVisaReq("5");
      }
    }

    if (wp.colEq("bin_type", "V")) {
      String lsVmjReason = wp.colStr("vmj_reason_code");
      // if ( !eq_any(oo_outgo.vmj_income_reason,wp.col_ss("is_vis_reason"))) {
      if (!eqAny(lsVmjReason, wp.colStr("is_vis_reason"))) {
        errmsg("停掛原因不一致: " + ooOutgo.vmjIncomeReason + "-" + wp.colStr("is_vis_reason"));
      }
      String lsVmjRegion = wp.colStr("vmj_region");
      // if ( !eq_any(oo_outgo.vmj_income_region,wp.col_ss("is_outgoing_area_ori"))) {
      if (!eqAny(lsVmjRegion, wp.colStr("is_outgoing_area_ori"))) {
        errmsg(
            "VISA REGION不一致: " + ooOutgo.vmjIncomeRegion + '-' + wp.colStr("is_outgoing_area_ori"));
      }
    }
    // wf_set_lbl_m(); //Set OutGoing-ISO response to BOX
    return rc;
  }

  void checkExceptionArea() {
    /*
     * if (wp.item_empty("excep_flag")) return;
     */
    String lsDate = "";
    if (wp.itemEq("bin_type", "V")) {
      lsDate = wp.itemStr("vis_purg_date_1");
      isOutgoingArea =
          wp.itemStr("vis_area_1") + wp.itemStr("vis_area_2") + wp.itemStr("vis_area_2")
              + wp.itemStr("vis_area_4") + wp.itemStr("vis_area_5") + wp.itemStr("vis_area_6")
              + wp.itemStr("vis_area_7") + wp.itemStr("vis_area_8") + wp.itemStr("vis_area_9");
      if (empty(isOutgoingArea) == false && empty(lsDate)) {
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
      isOutgoingArea = wp.itemStr("jcb_area_1") + wp.itemStr("jcb_area_2")
          + wp.itemStr("jcb_area_3") + wp.itemStr("jcb_area_4") + wp.itemStr("jcb_area_5");
      lsDate = wp.itemStr("jcb_date1");
      if (empty(isOutgoingArea) == false && empty(lsDate)) {
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
          errmsg("MASTER對應地區之刪除日期不可空白");
          return;
        }
        if (commDate.sysComp(lsDate) > 0) {
          errmsg("MASTER刪除日期不可小於今日");
          return;
        }
      }
      return;
    }

  }

  @Override
  public void dataCheck() {
    ibDebit = wp.itemEq("debit_flag", "Y");
    isCardNo = wp.itemStr("card_no");
    isBinType = wp.itemStr("bin_type");

    selectCrdCard(isCardNo);
    if (rc != 1)
      return;

    if (wp.itemEmpty("rowid")) {
      if (!colEq("card.current_code", "0")) {
        errmsg("此卡已停掛, 不可新增!");
        return;
      }
      // --
      if (wp.itemEmpty("neg_del_date")) {
        wp.itemSet("neg_del_date", commDate.dateAdd(colStr("card.new_end_date"), 0, 0, 1));
      }
      if (wp.itemEmpty("renew_flag") && (wp.itemEq("oppo_type", "2") || wp.itemEq("oppo_type", "5")
          || wp.itemEq("oppo_type", "4"))) {
        errmsg("補發否,不可空白");
        return;
      }
      // -不可強停-
      // String ls_date=commDate.sysDate();
      if (wp.itemEq("oppo_type", "3")) {
        if (colEq("card.no_f_stop_flag", "Y")) {
          if (commDate.sysComp(colStr("card.no_f_stop_s_date")) >= 0
              && commDate.sysComp(colNvl("card.no_f_stop_e_date", "20991231")) <= 0) {
            errmsg("此卡戶為永不強停, 不可新增!");
            return;
          }
        }
      } 
    }



    // ls_date =wp.item_ss("neg_del_date");
    if (commDate.sysComp(wp.itemStr2("neg_del_date")) > 0) {
      errmsg("NEG刪除日期: 不可小於 系統日期");
      return;
    }

    // -VD公司戶-
    if (wp.itemEq("debit_flag", "Y") && wp.itemEmpty("corp_p_seqno") == false
        && wp.itemEmpty("id_p_seqno")) {
      if (wp.itemEq("renew_flag", "Y")) {
        errmsg("公司戶VISA金融卡不可補發");
        return;
      }
    }

    // --
    if (wp.itemEmpty("oppo_type") || wp.itemEmpty("oppo_reason")) {
      errmsg("停掛類別, 停掛原因: 不可空白!");
      return;
    }

    String lsNegDdate = wp.itemStr("neg_del_date");
    if (empty(lsNegDdate)) {
      lsNegDdate = commDate.dateAdd(wp.itemStr("new_end_date"), 0, 0, 1);
      wp.itemSet("neg_del_date", lsNegDdate);
    }
    isNegDelDate = wp.itemStr("neg_del_date");

    // --
    checkExceptionArea();
    if (rc != 1)
      return;
    
    // -set outgoing value-
    setOutgoingValue();
    if (rc != 1)
      return;

    // -outgoing-
    ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    //ooOutgo.isCallAutoAuth = false;
    ooOutgo.iscalltwmp = true;
    ooOutgo.isDebit = wp.itemEq("debit_flag", "Y");
    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = isBinType;
    ooOutgo.p4Reason = isNegReason;
    ooOutgo.p5DelDate = wp.itemStr2("neg_del_date");    
    ooOutgo.p6VipAmt = "0";
    ooOutgo.p7Region = isOutgoingArea;
    ooOutgo.p8NewEndDate = wp.itemStr("new_end_date");
	ooOutgo.p9CurrentCode = wp.itemStr("oppo_type");
	ooOutgo.p10OppostReason = wp.itemStr("oppo_reason");
	ooOutgo.electronicCode = wp.itemStr("electronic_code");
	String electronicCode = ooOutgo.electronicCode ; 
	ooOutgo.p11ElectronicCardno = selectCardNo(electronicCode,cardNo);
    //NCCC
    outgoingNccc();
    //是否送國際組織為N return
    if(wp.itemEq("tt_excep_flag", "N")){
        return;
    }
    // -outgoing-VMJ-

    if (!empty(isVmjReason) && !eqIgno(isBinType, "A")) {
      String lsAction = "1";
      outgoingUpdate(lsAction);

      // NCCC Reject-OutGoing RECORD Already Exist WHILE ADD
      if (eqIgno(ooOutgo.respCode, "N4") && eqIgno(isBinType, "V")) {
        this.outgoingUpdate("2");
      }
      // NCCC Reject-OutGoing RECORD Already Exist WHILE ADD
      if (eqIgno(ooOutgo.respCode, "04") && eqIgno(isBinType, "J")) {
        this.outgoingUpdate("2");
      }
      // wf_set_lbl_m(); //Set OutGoing-ISO response to BOX
    }
    // -OK-
    return;
  }

  void setOutgoingValue() {
    // -get outgo-reason-
    strSql = "select ncc_opp_type, neg_opp_reason as neg_reason,"
        + " vis_excep_code as visa_reason," + " mst_auth_code as mast_reason,"
        + " jcb_excp_code as jcb_reason, " 
        + " fisc_opp_code as fisc_reason "
        + " from cca_opp_type_reason" + " where 1=1 "
        + " and opp_status = ? " ;
//        + commSqlStr.col(wp.itemStr("oppo_reason"), "opp_status");
    setString(1,wp.itemStr("oppo_reason"));
    sqlSelect(strSql); 
    if (sqlRowNum <= 0) {
      errmsg("讀取NEG原因失敗!");
      return;
    }
    isNegReason = colStr("neg_reason");
    isFiscReason = colStr("fisc_reason");
    // ls_nccc_opp_type =col_ss("ncc_opp_type");

    if (eqIgno(isBinType, "V"))
      this.isVmjReason = colStr("visa_reason");
    else if (this.eqIgno(isBinType, "M"))
      isVmjReason = colStr("mast_reason");
    else if (this.eqIgno(isBinType, "J"))
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
      isOutgoingArea = wp.itemNvl("jcb_area_1", "0") + wp.itemNvl("jcb_area_2", "0")
          + wp.itemNvl("jcb_area_3", "0") + wp.itemNvl("jcb_area_4", "0")
          + wp.itemNvl("jcb_area_5", "0");
    } else if (eqIgno(isBinType, "V")) {
      isOutgoingArea = wp.itemNvl("vis_area_1", " ") + wp.itemNvl("vis_area_2", " ")
          + wp.itemNvl("vis_area_3", " ") + wp.itemNvl("vis_area_4", " ")
          + wp.itemNvl("vis_area_5", " ") + wp.itemNvl("vis_area_6", " ")
          + wp.itemNvl("vis_area_7", " ") + wp.itemNvl("vis_area_8", " ")
          + wp.itemNvl("vis_area_9", " ");
    } else if (eqIgno(isBinType, "M")) {
      isOutgoingArea = wp.itemNvl("mast_area_1", " ") + this.strMid(commDate.dateAdd(wp.itemStr("mast_date1"),0,0,1), 2, 6)
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

  // void wf_set_lbl_m() {
  // wp.col_set("vmj_resp_code",oo_outgo.resp_code);
  // wp.col_set("vmj_reason_code",oo_outgo.vmj_income_reason);
  // }
  // void wf_set_lbl_n() {
  // wp.col_set("neg_resp_code", oo_outgo.resp_code);
  // wp.col_set("neg_reason_code",oo_outgo.neg_income_reason);
  // }

  void selectCrdCard(String aCardNo) {
    daoTid = "card.";
    strSql = "select A.current_code, A.card_type,A.group_code, A.bin_type,"
        + " A.new_end_date, A.id_p_seqno, A.acno_p_seqno, A.corp_p_seqno, A.acct_type, uf_nvl(A.combo_indicator,'N') as combo_indicator, "
        + " B.no_f_stop_flag, B.no_f_stop_s_date, B.no_f_stop_e_date,"
        + " B.no_block_flag, B.no_block_s_date, B.no_block_e_date "
        + " from crd_card A join act_acno B on A.acno_p_seqno =B.acno_p_seqno"
        + " where A.card_no =?"; // +commSqlStr.col(is_card_no,"A.card_no");
    if (ibDebit) {
      strSql = "select A.current_code, A.card_type,A.group_code, A.bin_type,"
          + " A.new_end_date,A.id_p_seqno, A.p_seqno as acno_p_seqno, A.corp_p_seqno, A.acct_type, "
          + " A.acct_no," + " B.no_f_stop_flag, B.no_f_stop_s_date, B.no_f_stop_e_date,"
          + " B.no_block_flag, B.no_block_s_date, B.no_block_e_date "
          + " from dbc_card A join dba_acno B on A.p_seqno =B.p_seqno" + " where A.card_no =?"; // +commSqlStr.col(is_card_no,"A.card_no");
    }
    setString2(1, aCardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("卡號不存在; card_no=[%s]", isCardNo);
      return;
    }
//    if (!colStr("card.combo_indicator").equals("N")) {
//      errmsg("COMBO卡不可於此畫面停掛");
//      return;
//    }
//    if (ibDebit) {
//      errmsg("VD卡不可於此畫面停掛");
//      return;
//    }
    wp.itemSet("z.card_type", colStr("card.card_type"));
    wp.itemSet("z.new_end_date", colStr("card.new_end_date"));
    if (ibDebit && colEmpty("card.acct_no")) {
      errmsg("無金融帳號, 不可做停掛");
      return;
    }


    // -cca_card_base-
    strSql = "select card_acct_idx" + " from cca_card_base" + " where 1=1 and card_no = ? ";
//        + commSqlStr.col(isCardNo, "card_no");
    setString(1,isCardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("卡片資料不存在 [cca_card_base], kk=" + isCardNo);
      return;
    }
    iiCardAcctIdx = colNum("card_acct_idx");
  }

  void outgoingNccc() {
//    if (empty(isNegReason) || wp.itemEq("bin_type", "A") || ibDebit) {
//      return;
//    }
	  if (empty(isNegReason)){
		  return; 
	  }
    // st_helpmsg.text = "開始傳送["+is_card_no+"]至 NCCC (新增)......"
    
    ooOutgo.p1CardNo = isCardNo;
    ooOutgo.p4Reason = isNegReason;
    ooOutgo.fiscReason = isFiscReason;
    ooOutgo.p5DelDate = isNegDelDate;
    ooOutgo.p2BinType = wp.itemStr("bin_type");

    rc = ooOutgo.oppoNegId("1");
    if (rc != 1) {
      errmsg(ooOutgo.getMsg());
      return;
    }

    // NCCC Reject-NEG RECORD Already Exist WHILE ADD
    if (eqIgno(ooOutgo.respCode, "N4")) {
      rc = ooOutgo.oppoNegId("2");
    }
    // wf_set_lbl_n(); //Set NCCC-ISO response to BOX
    // ls_neg_rsp_code = gs_resp_code
  }

  void outgoingUpdate(String aAction) {

    String lsVisPurgDate1 = "";

    if (eqIgno(isBinType, "M")) {
      if (!empty(isOutgoingAreaOri) && empty(isOutgoingArea)) {
        outgoingDelete();
      }
      String lsFile = aAction;
      if (eqAny(aAction, "2"))
        lsFile = "1";
      ooOutgo.masterDate = isNegDelDate;
      if (empty(ooOutgo.masterDate))
        ooOutgo.masterDate = wp.itemStr("z.new_end_date");
      if (empty(isOutgoingArea)) {
        // "開始傳送 ["+is_card_no+"] 至 Master......MCC102"
        ooOutgo.parmClear();
        ooOutgo.p1CardNo = isCardNo;
        ooOutgo.p4Reason = isVmjReason;
        ooOutgo.p2BinType = wp.itemStr("bin_type");
        ooOutgo.p6VipAmt = "0";
        ooOutgo.oppoMasterReq2(lsFile);
      } else {
        ooOutgo.parmClear();
        ooOutgo.p1CardNo = isCardNo;
        ooOutgo.p4Reason = isVmjReason;
        ooOutgo.p2BinType = wp.itemStr("bin_type");
        ooOutgo.p6VipAmt = "0";
        // "開始傳送 ["+is_card_no+"] 至 Master......MCC102"
        ooOutgo.oppoMasterReq2(lsFile);
        // "開始傳送 ["+is_card_no+"] 至 Master......MCC103"
        ooOutgo.oppoMasterReq(lsFile, isMOutgoArea);
      }
    } else if (eqIgno(isBinType, "J")) {
      lsVisPurgDate1 = commDate.dateAdd(wp.itemStr("jcb_date1"),0,0,1);
      if (empty(lsVisPurgDate1))
         lsVisPurgDate1 = isNegDelDate;
      ooOutgo.parmClear();
      ooOutgo.p1CardNo = isCardNo;
      ooOutgo.p4Reason = isVmjReason;
      ooOutgo.p5DelDate = lsVisPurgDate1;
      ooOutgo.p7Region = isOutgoingArea;
      // "開始傳送 ["+is_card_no+"] 至 JCB......"
      ooOutgo.oppoJcbReq(aAction);
    } else if (eqIgno(isBinType, "V")) {
      lsVisPurgDate1 = commDate.dateAdd(wp.itemStr("vis_purg_date_1"),0,0,1);
      if (empty(lsVisPurgDate1))
        lsVisPurgDate1 = isNegDelDate;

      ooOutgo.parmClear();
      ooOutgo.p1CardNo = isCardNo;
      ooOutgo.p4Reason = isVmjReason;
      ooOutgo.p5DelDate = lsVisPurgDate1;
      ooOutgo.p7Region = isOutgoingArea;
      // "開始傳送 ["+is_card_no+"] 至 VISA......"
      ooOutgo.oppoVisaReq(aAction);
    }
  }

  void outgoingDelete() {
    if (eqIgno(isBinType, "M")) {
      if (empty(isOutgoingAreaOri)) {
        ooOutgo.parmClear();
        ooOutgo.p1CardNo = isCardNo;
        ooOutgo.p4Reason = isVmjReasonOri;
        ooOutgo.p6VipAmt = "0";
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC102"
        ooOutgo.oppoMasterReq2("3");
      } else {
        ooOutgo.parmClear();
        ooOutgo.p1CardNo = isCardNo;
        ooOutgo.p4Reason = isVmjReasonOri;
        ooOutgo.p6VipAmt = "0";
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC102"
        ooOutgo.oppoMasterReq2("3");
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC103"
        ooOutgo.oppoMasterReq("3", isMOutgoAreaOri);
      }
    } else if (eqIgno(isBinType, "J")) {
      ooOutgo.parmClear();
      ooOutgo.p1CardNo = isCardNo;
      ooOutgo.p4Reason = isVmjReasonOri;
      ooOutgo.p5DelDate = isNegDelDate;
      ooOutgo.p7Region = isOutgoingAreaOri;
      // "開始傳送 ["+is_card_no+"] 至 JCB......"
      ooOutgo.oppoJcbReq("0");
    } else if (eqIgno(isBinType, "J")) {
      ooOutgo.parmClear();
      ooOutgo.p1CardNo = isCardNo;
      ooOutgo.p4Reason = isVmjReasonOri;
      ooOutgo.p5DelDate = isNegDelDate;
      ooOutgo.p7Region = isOutgoingAreaOri;
      // 開始傳送 ["+is_card_no+"] 至 VISA......"
      ooOutgo.oppoVisaReq("3");
    }
  }

  private void outgoingIbm(String aFile) {
//    if (!ibDebit)
//      return;
//
//    if (ooOutgo == null) {
//      ooOutgo = new OutgoingOppo();
//      ooOutgo.setConn(wp);
//    }
//
//    ooOutgo.parmClear();
//    ooOutgo.p3BankAcctno = colStr("card.bank_actno");
//    ooOutgo.p1CardNo = isCardNo;
//    ooOutgo.p4Reason = isNegReason;
//    ooOutgo.p5DelDate = isNegDelDate;
//    ooOutgo.p2BinType = colStr("card.bin_type");
//
//    ooOutgo.oppoIbmNegfile(aFile);
//
//    if (rc != 1) {
//      errmsg("IBM_negfile: " + ooOutgo.getMsg());
//    }
  }

  @Override
  public int dbInsert() {
    // -cardNo-
    cardNo = wp.itemStr2("card_no");
    isCardNo = wp.itemStr2("card_no");
    ibFile = true;
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    updateCrdCard(isCardNo);
    if (rc != 1)
      return rc;
    insertCcaOpposition(isCardNo);
    if (rc != 1)
      return rc;
    insertOnbat2ecs(isCardNo);
    if (rc != 1)
      return rc;
    
    // -補發-

    // //-刪除附卡-
    // if (rc==1 && !wp.item_eq("oppo_type", "2")) {
    // stop_sup_card();
    // }
    // -debit-card-
    if (ibDebit) {
      outgoingIbm("1");
      wp.log(getMsg());
      rc = 1;
    }

    return rc;
  }

  public int stopSupCard() {
    isCardNo = wp.itemStr2("card_no");
    ibFile = false;
    if (empty(isCardNo)) {
      errmsg("附卡連動停用, 卡號不可空白");
      return rc;
    }
    boolean lbRun = false;
    // -VD卡: 無附卡-
    if (ibDebit)
      return 1;

    if (wp.itemEq("oppo_type", "1") || wp.itemEq("oppo_type", "3")) {
      lbRun = true;
    }
    if (lbRun == false)
      return 1;

    daoTid = "card.";
    strSql = "select card_no, current_code, card_type, group_code,"
        + " new_end_date,  id_p_seqno,  acno_p_seqno,  corp_p_seqno,  acct_type " + " from crd_card"
        + " where major_card_no =?" + " and current_code ='0' and sup_flag ='1'";
    setString2(1, isCardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0)
      return 1;

    int llNrow = sqlRowNum;
    // -一般附卡停用-
    for (int ll = 0; ll < llNrow; ll++) {
      String lsCardNo = colStr(ll, "card.card_no");
      updateCrdCard(lsCardNo);
      if (rc != 1) {
        errmsg("附卡連動停用失敗, kk=" + lsCardNo);
        return rc;
      }
      insertCcaOpposition(lsCardNo);
      if (rc != 1)
        return rc;
      insertOnbat2ecs(lsCardNo);
      if (rc != 1)
        return rc;
    }

    return rc;
  }

  void updateCrdCard(String aCardNo) {
    if (ibDebit == false) {
      strSql = "update crd_card set " + " current_code = :current_code, "
          + " oppost_date = to_char(sysdate,'yyyymmdd'), " + " oppost_reason = :oppo_reason, "
          + " lost_fee_code =:lost_fee_code, " + commSqlStr.setModxxx(modUser, modPgm)
          + " where card_no =:card_no"; 
    } else {
      strSql = "update dbc_card set " + " current_code = :current_code, "
          + " oppost_date = to_char(sysdate,'yyyymmdd'), " + " oppost_reason = :oppo_reason, "
          + " lost_fee_code =:lost_fee_code, " + commSqlStr.setModxxx(modUser, modPgm)
          + " where card_no =:card_no";
    }
    setString2("current_code", wp.itemStr2("oppo_type"));
    setString2("oppo_reason", wp.itemStr2("oppo_reason"));
    setString2("lost_fee_code", wp.itemStr2("lost_fee_code"));

    // --KK-
    setString2("card_no", aCardNo);
    sqlExec(strSql);
    if (sqlRowNum != 1) {
      errmsg("update CRD[DBC]_CARD error,kk[%s]; " + this.sqlErrtext, aCardNo);
      return;
    }

//    wfDelSpecial(aCardNo);
  }

//  void wfDelSpecial(String aCardNo) {
//
//    strSql = "select spec_flag from cca_card_base" + " where card_no =?"; // +commSqlStr.col(is_card_no,"card_no");
//    setString2(1, aCardNo);
//    sqlSelect(strSql);
//    if (sqlRowNum <= 0)
//      return;
//
//    // -無特指, 偽卡停用-
//    if (colNeq("spec_flag", "Y") || eqIgno(wp.itemStr2("oppo_type"), "6"))
//      return;
//
//    // --
//    strSql = "update cca_card_base set" + " spec_status =''" + ", spec_flag ='N'"
//        + ", spec_mst_vip_amt =0" + ", spec_del_date =''" + ", spec_remark =''" + ","
//        + commSqlStr.setModxxx(modUser, modPgm) + " where card_no =?";
//    setString2(1, aCardNo);
//    sqlExec(strSql);
//    if (sqlRowNum != 1) {
//      sqlErr("CCA_CARD_BASE.update");
//      return;
//    }
//
//    // ----------------
//    strSql = "delete cca_special_visa where card_no =?";
//    setString2(1, aCardNo);
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
//    setString2(1, aCardNo);
//    setString(isBinType);
//    setString(modPgm);
//    setString(modUser);
//    sqlExec(strSql);
//    if (this.sqlRowNum <= 0) {
//      sqlErr("cca_spec_his.ADD");
//      return;
//    }
//  }

  void setVmjArea() {
    /*
     * if (wp.item_empty("excep_flag")) { sp.ppss("vis_area_1",""); sp.ppss("vis_area_2","");
     * sp.ppss("vis_area_3",""); sp.ppss("vis_area_4",""); sp.ppss("vis_area_5","");
     * sp.ppss("vis_area_6",""); sp.ppss("vis_area_7",""); sp.ppss("vis_area_8","");
     * sp.ppss("vis_area_9",""); sp.ppss("vis_purg_date_1",""); sp.ppss("vis_purg_date_2","");
     * sp.ppss("vis_purg_date_3",""); sp.ppss("vis_purg_date_4",""); sp.ppss("vis_purg_date_5","");
     * sp.ppss("vis_purg_date_6",""); sp.ppss("vis_purg_date_7",""); sp.ppss("vis_purg_date_8","");
     * sp.ppss("vis_purg_date_9",""); return; }
     */
    /*
     * if (wp.item_eq("oppo_type","1")) { return; }
     */
    if (wp.itemEq("bin_type", "V")) {
      sp.ppstr("vis_area_1", wp.itemStr2("vis_area_1"));
      sp.ppstr("vis_area_2", wp.itemStr2("vis_area_2"));
      sp.ppstr("vis_area_3", wp.itemStr2("vis_area_3"));
      sp.ppstr("vis_area_4", wp.itemStr2("vis_area_4"));
      sp.ppstr("vis_area_5", wp.itemStr2("vis_area_5"));
      sp.ppstr("vis_area_6", wp.itemStr2("vis_area_6"));
      sp.ppstr("vis_area_7", wp.itemStr2("vis_area_7"));
      sp.ppstr("vis_area_8", wp.itemStr2("vis_area_8"));
      sp.ppstr("vis_area_9", wp.itemStr2("vis_area_9"));
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

  void insertCcaOpposition(String aCardNo) {
    String lsCardNo = aCardNo;

    this.strSql = "select count(*) as db_cnt" + " from cca_opposition " + " where card_no =?";
    sqlSelect(strSql, new Object[] {lsCardNo});
    boolean lbInsert = (colNum("db_cnt") <= 0);

    String lsNegRespCode = "";
    String lsVmjRespCode = "";
    if (ooOutgo != null) {
      lsNegRespCode = ooOutgo.negRespCode;
      lsVmjRespCode = ooOutgo.vmjRespCode;
    }

    sp = new busi.SqlPrepare();

    if (lbInsert) {
      sp.sql2Insert("cca_opposition");
      sp.ppstr("card_no", lsCardNo);
      sp.ppnum("card_acct_idx", iiCardAcctIdx);
      sp.ppstr("debit_flag", wp.itemStr2("debit_flag"));
      sp.ppstr("card_type", colStr("card.card_type"));
      sp.ppstr("bin_type", wp.itemStr2("bin_type"));
      sp.ppstr("group_code ", colStr("card.group_code"));
      sp.ppstr("from_type", "1");
      sp.ppstr("oppo_type", wp.itemStr2("oppo_type"));
      sp.ppstr("oppo_status", wp.itemStr2("oppo_reason"));
      sp.ppstr("oppo_user", modUser);
      sp.ppymd("oppo_date");
      sp.pptime("oppo_time");
      sp.ppstr("neg_del_date", wp.itemStr2("neg_del_date"));
      sp.ppstr("renew_flag", wp.itemStr("renew_flag"));
      // sp.ppss("renew_urgen ");
      sp.ppstr("cycle_credit", wp.itemStr2("cycle_credit"));
      sp.ppstr("opp_remark", wp.itemStr2("opp_remark"));
      sp.ppstr("mail_branch", wp.itemStr2("mail_branch"));
      sp.ppstr("lost_fee_flag", wp.itemStr2("lost_fee_code"));
      sp.ppstr2("excep_flag", wp.itemStr2("excep_flag"));
      sp.ppstr("except_proc_flag", "0");
      if (ibFile == true)
        setVmjArea();
      sp.ppstr("neg_resp_code", lsNegRespCode);
      sp.ppstr("visa_resp_code", lsVmjRespCode);
      sp.ppstr2("mst_reason_code", isNegReason);
      sp.ppstr2("vis_reason_code", isVmjReason);
      sp.ppstr2("fisc_reason_code", isFiscReason);
      // sp.ppss("mcas_neg_resp_code");
      sp.ppnum("curr_tot_tx_amt", 0);
      sp.ppnum("curr_tot_cash_amt", 0);
      sp.ppstr("bank_acct_no", wp.itemStr2("bank_acctno"));
      // sp.ppss("logic_del ");
      // sp.ppss("logic_del_date ");
      // sp.ppss("logic_del_time ");
      // sp.ppss("logic_del_user ");
      sp.ppymd("crt_date");
      sp.pptime("crt_time"); // ,"to_char(sysdate,'hh24miss')");
      sp.ppstr("crt_user", modUser);
      // --sp.ppss("in_main_flag","N");
      sp.ppdate("mod_time"); // ,"sysdate");
      sp.ppstr("mod_user", modUser);
      sp.ppstr("mod_pgm", modPgm);
      sp.ppnum("mod_seqno", 1);
    } else {
      sp.sql2Update("cca_opposition");
      sp.ppstr("from_type", "1");
      sp.ppstr("oppo_type", wp.itemStr2("oppo_type"));
      sp.ppstr("oppo_status  ", wp.itemStr2("oppo_reason"));
      sp.ppstr("oppo_user", modUser);
      sp.ppymd("oppo_date");
      sp.pptime("oppo_time");
      sp.ppstr("neg_del_date", wp.itemStr2("neg_del_date"));
      sp.ppstr("renew_flag", wp.itemStr("renew_flag"));
      sp.ppstr("cycle_credit ", wp.itemStr2("cycle_credit"));
      sp.ppstr("opp_remark", wp.itemStr2("opp_remark"));
      sp.ppstr("mail_branch", wp.itemStr2("mail_branch"));
      sp.ppstr("lost_fee_flag", wp.itemStr2("lost_fee_code"));
      sp.ppstr2("excep_flag", wp.itemStr2("excep_flag"));
      sp.ppstr2("except_proc_flag", "0");
      if (ibFile == true)
        setVmjArea();
      sp.ppstr("neg_resp_code", lsNegRespCode);
      sp.ppstr("visa_resp_code", lsVmjRespCode);
      sp.ppstr2("mst_reason_code", isNegReason);
      sp.ppstr2("vis_reason_code", isVmjReason);
      sp.ppstr2("fisc_reason_code", isFiscReason);
      // sp.ppss("mcas_neg_resp_code");
      sp.ppnum("curr_tot_tx_amt", 0);
      sp.ppnum("curr_tot_cash_amt", 0);
      sp.ppstr("bank_acct_no", wp.itemStr("bank_acctno"));
      sp.ppstr("logic_del", "");
      sp.ppstr("logic_del_date", "");
      sp.ppstr("logic_del_time", "");
      sp.ppstr("logic_del_user", "");
      sp.ppymd("chg_date");
      sp.pptime("chg_time");
      sp.ppstr("chg_user", modUser);
      sp.modxxx(modUser, modPgm);
      sp.sql2Where(" where card_no =?", lsCardNo);
    }

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      errmsg("insert CCA_OPPOSITION error; " + this.sqlErrtext);
    }
  }

  void insertOnbat2ecs(String aCardNo) {

    sp = new busi.SqlPrepare();
    sp.sql2Insert("onbat_2ecs");
    if (wp.itemEq("oppo_type", "3")) {
      sp.ppstr2("trans_type", "5"); // -強停-
    } else {
      sp.ppstr2("trans_type", "6");
    }
    sp.ppstr2("to_which", "1");
    sp.ppdate("dog");
    sp.ppstr2("proc_mode", "O");
    sp.ppstr2("card_no", aCardNo);
    sp.ppstr2("acct_type", colStr("card.acct_type"));
    sp.ppstr2("acno_p_seqno", colStr("card.acno_p_seqno"));
    sp.ppstr2("id_p_seqno", colStr("card.id_p_seqno"));
    sp.ppstr2("opp_type", wp.itemStr2("oppo_type"));
    sp.ppstr2("opp_reason", wp.itemStr2("oppo_reason"));
    sp.ppymd("opp_date");
    sp.ppstr2("is_renew", wp.itemNvl("renew_flag", "N"));
    sp.ppint2("curr_tot_lost_amt", 100);
    sp.ppstr2("mail_branch", wp.itemStr("mail_branch"));
    sp.ppstr2("lost_fee_flag", wp.itemStr2("lost_fee_code"));
    sp.ppstr2("debit_flag", wp.itemStr2("debit_flag"));

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
      sqlErr("insert ONBAT_2ECS error, kk=" + aCardNo);
    }

  }

  void insertCrdEmboss() {
    // -????-
  }

  @Override
	public int dbUpdate() {
		actionInit("U");
		ibDebit = wp.itemEq("debit_flag", "Y");
		isCardNo = wp.itemStr("card_no");
	    updateCrdCard(isCardNo);
	    if (rc != 1)
	      return rc;

		String sqlSelect = "select count(*) as cnt from cca_opposition where card_no = :card_no ";
		setString("card_no", isCardNo);
		sqlSelect(sqlSelect);
		int cnt = colInt("cnt");
		if (cnt > 0) {
			strSql = " update cca_opposition set ";
			strSql += " opp_remark = :opp_remark, ";
			strSql += " oppo_status = :oppo_status, ";
			strSql += " chg_date = to_char(sysdate,'yyyymmdd') , ";
			strSql += " chg_time = to_char(sysdate,'hh24miss') , ";
			strSql += " chg_user = :chg_user , ";
			strSql += " mod_time = sysdate , ";
			strSql += " mod_user = :mod_user , ";
			strSql += " mod_pgm = :mod_pgm , ";
			strSql += " mod_seqno = nvl(mod_seqno,0)+1 ";
			strSql += " where 1=1 and card_no = :card_no ";
			setString("chg_user", wp.loginUser);
			setString("opp_remark", wp.itemStr("opp_remark"));
			setString("oppo_status", wp.itemStr("oppo_reason"));
			setString("mod_user", wp.loginUser);
			setString("mod_pgm", wp.modPgm());
			setString("card_no", wp.itemStr("card_no"));
			sqlExec(strSql);
			if (sqlRowNum <= 0) {
				errmsg("update cca_cca_opposition error ");
				return rc;
			}
		} else {
			insertOppo();
		}
		
		
		
		return rc;
	}

  @Override
  public int dbDelete() {
    return 0;
  }

  @Override
  public int dataProc() {
    return 0;
  }

  public void selectCcaCardAcct() {
    if (wp.colEmpty("acno_p_seqno"))
      return;

    String sql1 = "select A.block_reason1||' '||A.block_reason2||' '||A.block_reason3"
        + "||' '||A.block_reason4||' '||A.block_reason5 as block_reason15"
        + ", A.spec_status as acno_spec_status" + ", C.spec_status as card_spec_status"
        + " from cca_card_acct A join cca_card_base C on A.card_acct_idx =C.card_acct_idx"
        + " where 1=1" + " and card_no =?";

    setString2(1, wp.colStr("card_no"));
    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      wp.colSet("block_reason15", colStr("block_reason15"));
      wp.colSet("acno_spec_status", colStr("acno_spec_status"));
      wp.colSet("card_spec_status", colStr("card_spec_status"));
    } else {
      wp.colSet("block_reason15", "");
      wp.colSet("acno_spec_status", "");
      wp.colSet("card_spec_status", "");
    }
  }

  public void cntSupCard() {
    if (wp.colEq("sup_flag", "1")) {
      return;
    }

    String sql1 = "select count(*) as ll_cnt from crd_card" + " where major_card_no =?"
        + " and current_code ='0' and sup_flag='1'";
    setString2(1, wp.colStr("card_no"));
    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      wp.colSet("sup_card_num", colStr("ll_cnt"));
    }
  }

  public void cntTpanCard() {
    double liTpanCnt = 0;
//    String sql1 = " select " + " count(*) as db_cnt1 " + " from mob_tpan_info "
//        + " where card_no =? " + " and tpan_status ='0' ";
//    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});

    String sql2 = " select " + " count(*) as db_cnt2 " + " from hce_card "
        + " where status_code ='0' " + " and card_no = ? ";
    sqlSelect(sql2, new Object[] {wp.colStr("card_no")});

//    liTpanCnt = colNum("db_cnt") + colNum("db_cnt2");
    liTpanCnt =  colNum("db_cnt2");
    if (liTpanCnt > 0) {
      wp.colSet("tpan_card_num", "" + liTpanCnt);
    } else {
      wp.colSet("tpan_card_num", "0");
    }

  }

  public void wfOutgoing() {
    if (wp.itemEq("debit_flag", "Y"))
      return;

    OutgoingOppo ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = wp.colStr("card_no");
    ooOutgo.p2BinType = wp.colStr("bin_type");
    ooOutgo.oppoNegId("5");

    if (wp.colEq("bin_type", "M")) {
      ooOutgo.p6VipAmt = "0";
      ooOutgo.oppoMasterReq2("5");
    } else if (wp.colEq("bin_type", "J")) {
      ooOutgo.oppoJcbReq("5");
    } else if (wp.colEq("bin_type", "V")) {
      ooOutgo.p5DelDate = wp.colStr("new_end_date");
      ooOutgo.p7Region = "0" + commString.space(8);
      ooOutgo.oppoVisaReq("5");
    }
    //
    // wp.col_set("neg_resp_code",oo_outgo.neg_resp_code);
    // wp.col_set("neg_reason_code",oo_outgo.neg_income_reason);
    // wp.col_set("vmj_resp_code",oo_outgo.vmj_resp_code);
    // wp.col_set("vmj_reason_code",oo_outgo.vmj_income_reason);
  }

  void dataCheckCcam2012() {

    String sql1 = " select " + " oppost_date " + " from crd_card " + " where card_no = ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("card_no")});
    String oppostDate = colStr("oppost_date");

    if (empty(oppostDate)) {
      errmsg("本卡尚未停掛,不可修改");
      return;
    }

  }

  public int ccam2012Update() {
    actionInit("U");
//    dataCheckCcam2012();
//    if (rc != 1)
//      return rc;
    dbUpdate();
    if (rc != 1) {
      return rc;
    }
    
    strSql = " update cca_opposition set " + " renew_flag =:renew_flag , "
        + " lost_fee_flag =:lost_fee_code , " + " mail_branch =:mail_branch , "
        + " mod_time = sysdate , " + " mod_user =:mod_user , " + " mod_pgm =:mod_pgm , "
        + " mod_seqno = nvl(mod_seqno,0)+1 " + " where card_no =:card_no ";
    item2ParmNvl("renew_flag", "N");
    item2ParmNvl("lost_fee_code", "N");
    item2ParmStr("mail_branch");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    item2ParmStr("card_no");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update cca_cca_opposition error");
      return rc;
    }
    
    String cardNo = wp.itemStr("card_no");
    if(wp.itemEq("renew_flag", "Y")){
    	daoTid = "card.";
    	String sqlSelect = "";
    	if(ibDebit) {
    		sqlSelect = "select acct_type,p_seqno,id_p_seqno from dbc_card where card_no = :cardNo ";
    	}else {
    		sqlSelect = "select acct_type,acno_p_seqno,id_p_seqno from crd_card where card_no = :cardNo ";
    	}
    	
    	setString("cardNo",cardNo);
    	sqlSelect(sqlSelect);
    	if (sqlRowNum <= 0) {
    	    errmsg("卡號不存在; card_no=[%s]", isCardNo);
    	    return rc;
    	}
        insertOnbat2ecs(cardNo);
        if (rc != 1)
          return rc;
    }

    return rc;
  }

  public void selectOpposition() {
    String sql1 = " select * from cca_opposition where card_no = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});

    wp.colSet("excep_flag", colStr("excep_flag"));

    if (wp.colEq("bin_type", "V")) {
      wp.colSet("vis_area_1", colStr("vis_area_1"));
      wp.colSet("vis_area_2", colStr("vis_area_2"));
      wp.colSet("vis_area_3", colStr("vis_area_3"));
      wp.colSet("vis_area_4", colStr("vis_area_4"));
      wp.colSet("vis_area_5", colStr("vis_area_5"));
      wp.colSet("vis_area_6", colStr("vis_area_6"));
      wp.colSet("vis_area_7", colStr("vis_area_7"));
      wp.colSet("vis_area_8", colStr("vis_area_8"));
      wp.colSet("vis_area_9", colStr("vis_area_9"));
      wp.colSet("vis_purg_date_1", colStr("vis_purg_date_1"));
    } else if (wp.colEq("bin_type", "M")) {
      wp.colSet("mast_area_1", colStr("vis_area_1"));
      wp.colSet("mast_area_2", colStr("vis_area_2"));
      wp.colSet("mast_area_3", colStr("vis_area_3"));
      wp.colSet("mast_area_4", colStr("vis_area_4"));
      wp.colSet("mast_area_5", colStr("vis_area_5"));
      wp.colSet("mast_area_6", colStr("vis_area_6"));
      wp.colSet("mast_date1", colStr("vis_purg_date_1"));
      wp.colSet("mast_date2", colStr("vis_purg_date_2"));
      wp.colSet("mast_date3", colStr("vis_purg_date_3"));
      wp.colSet("mast_date4", colStr("vis_purg_date_4"));
      wp.colSet("mast_date5", colStr("vis_purg_date_5"));
      wp.colSet("mast_date6", colStr("vis_purg_date_6"));
    } else if (wp.colEq("bin_type", "J")) {
      wp.colSet("jcb_area_1", colStr("vis_area_1"));
      wp.colSet("jcb_area_2", colStr("vis_area_2"));
      wp.colSet("jcb_area_3", colStr("vis_area_3"));
      wp.colSet("jcb_area_4", colStr("vis_area_4"));
      wp.colSet("jcb_area_5", colStr("vis_area_5"));
      wp.colSet("jcb_date1", colStr("vis_purg_date_1"));
    }

  }

  public int insertProhibit() {
    msgOK();
    sql2Insert("crd_prohibit");
    addsqlParm("?", "card_no", wp.itemStr2("card_no"));
    addsqlParm(", prohibit_remark", ", '偽卡禁用'");
    addsqlYmd(", apr_date");
    addsqlParm(",?", ", apr_user", modUser);
    addsqlYmd(", crt_date");
    addsqlParm(",?", ", crt_user", modUser);
    addsqlModXXX(modUser, modPgm);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("insert crd_prohibit error ");
    }

    return rc;
  }
  
//111-12-14 V1.00.06 Ryan         票證可能有多筆，修改只讀有效起日最新的一筆 
  String selectCardNo(String electronicCode,String cardNo){
	  String sqlSelect = "";String wkCardno = "";
	  if(electronicCode.equals("01")){
		  sqlSelect = "select tsc_card_no as wk_electronic_card_no ";
	      sqlSelect	+= " from tsc_card where new_end_date > to_char(sysdate,'yyyymm') ";
	      sqlSelect	+= " and card_no = :card_no ";
	      if(ibDebit){
	    	  sqlSelect = "select tsc_card_no as wk_electronic_card_no ";
		      sqlSelect	+= " from tsc_vd_card where new_end_date > to_char(sysdate,'yyyymm') ";
		      sqlSelect	+= " and vd_card_no = :card_no ";
	      }
	      sqlSelect += " and current_code = '0' order by new_beg_date desc fetch first 1 rows only ";
		  setString("card_no",cardNo);
		  sqlSelect(sqlSelect);
		  if (sqlRowNum > 0) {
			  wkCardno = colStr("wk_electronic_card_no");
		  }	
	  }
	  
	  if(electronicCode.equals("02")){
		  sqlSelect = "select ips_card_no as wk_electronic_card_no "
			  	+ " from ips_card where new_end_date > to_char(sysdate,'yyyymm') ";
		  sqlSelect += " and current_code = '0' ";
		  sqlSelect	+= " and card_no = :card_no order by new_beg_date desc fetch first 1 rows only ";
		  setString("card_no",cardNo);
		  sqlSelect(sqlSelect);
		  if (sqlRowNum > 0) {
			  wkCardno = colStr("wk_electronic_card_no");
		  }	
	  }
	  
	  if(electronicCode.equals("03")){
		  sqlSelect = "select ich_card_no as wk_electronic_card_no "
			  	+ " from ich_card where new_end_date > to_char(sysdate,'yyyymm') ";
		  sqlSelect += " and current_code = '0' ";
		  sqlSelect	+= " and card_no = :card_no order by new_beg_date desc fetch first 1 rows only ";
		  setString("card_no",cardNo);
		  sqlSelect(sqlSelect);
		  if (sqlRowNum > 0) {
			  wkCardno = colStr("wk_electronic_card_no");
		  }	
	  }
	  return wkCardno;
  }
  
  void insertOppo(){
	  isBinType  =  wp.itemStr("bin_type");
		setOutgoingValue();
		if (rc != 1)
		    return;
		sqlSelect  = "select card_acct_idx" + " from cca_card_base" + " where 1=1 and card_no = ? ";
		setString(1,wp.itemStr("card_no"));
//	        + commSqlStr.col(wp.itemStr("card_no"), "card_no");
	    sqlSelect(sqlSelect);
	    iiCardAcctIdx = colNum("card_acct_idx");
		sp = new busi.SqlPrepare();
		sp.sql2Insert("cca_opposition");
		sp.ppstr("card_no", wp.itemStr("card_no"));
		sp.ppnum("card_acct_idx", iiCardAcctIdx);
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
		sp.ppstr("opp_remark", wp.itemStr2("opp_remark"));
		sp.ppstr("mail_branch", wp.itemStr2("mail_branch"));
		sp.ppstr("lost_fee_flag", wp.itemStr2("lost_fee_code"));
		sp.ppstr2("excep_flag", wp.itemStr2("excep_flag"));
		sp.ppstr("except_proc_flag", "0");
		sp.ppstr("neg_resp_code", "");
		sp.ppstr("visa_resp_code", "");
		sp.ppstr2("mst_reason_code", isNegReason);
		sp.ppstr2("vis_reason_code", isVmjReason);
		sp.ppstr2("fisc_reason_code", isFiscReason);
		sp.ppnum("curr_tot_tx_amt", 0);
		sp.ppnum("curr_tot_cash_amt", 0);
		sp.ppymd("crt_date");
		sp.pptime("crt_time");
		sp.ppstr("crt_user", modUser);
		sp.ppymd("chg_date");
		sp.pptime("chg_time");
		sp.ppstr("chg_user", modUser);
		sp.ppdate("mod_time");
		sp.ppstr("mod_user", modUser);
		sp.ppstr("mod_pgm", modPgm);
		sp.ppnum("mod_seqno", 1);
	    sqlExec(sp.sqlStmt(), sp.sqlParm());
	    if (sqlRowNum == 0) {
	      errmsg("insert CCA_OPPOSITION error; " + this.sqlErrtext);
	    }
  }
  
}
