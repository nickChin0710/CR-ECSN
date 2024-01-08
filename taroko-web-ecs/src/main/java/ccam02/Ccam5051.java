/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-18  V1.00.03  Alex        button fix                                *
* 108-12-10  V1.00.02  Alex        initButton										  *
* 108-11-28  V1.00.01  Alex        queryRead Code 1 -> Y                     *
* 109-04-20  V1.00.04  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;
import ecsfunc.DeCodeCrd;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5051 extends BaseEdit {
Ccam5051Func func;
String cardNote = "";

@Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
      wp.colSet("IND_NUM", "" + wp.selectCnt);
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    }
    /*
     * else if (eq_igno(wp.buttonCode, "R2")) { // -資料讀取- is_action = "R"; reloadList(); }
     */
    else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      strAction = "A";
      ccam5051Insert();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      strAction = "U";
      ccam5051Update();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      /* 清畫面 */
      strAction = "C";
      procFunc();
    }
    dddwSelect();
    initButton();
    if (wp.respHtml.indexOf("_detl") > 0) {
      wp.colSet("IND_NUM", "" + wp.listCount[0]);
    }
  }

  @Override
  public void initPage() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      wp.colSet("IND_NUM", "0");
    }

  }

  @Override
  public void dddwSelect() {
    if (wp.respHtml.equalsIgnoreCase("ccam5051")) {
      try {
    	wp.optionKey = wp.colStr("ex_card_note");
      	dddwAddOption("*", "*_通用");  
        dddwList("ddlb_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
      } catch (Exception ex) {
      }
    }
    if (wp.respHtml.indexOf("_detl") > 0) {
      try {
    	  wp.optionKey = wp.colStr("kk_card_note");
      	  dddwAddOption("*", "*_通用");  
          dddwList("ddlb_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");        
      } catch (Exception ex) {
      }
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1 and area_type='T'" + sqlCol(wp.itemStr("ex_card_note"), "card_note");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "card_note, " + "oversea_cash_pct, " + "end_date, "
        + "decode(open_chk,'1','Y','0','N') as open_chk, "
        + "decode(mcht_chk,'1','Y','0','N') as mcht_chk, "
        + "decode(delinquent,'1','Y','0','N') as delinquent, "
        + "decode(oversea_chk,'1','Y','0','N') as oversea_chk, " + "month_risk_chk,"
        + "day_risk_chk," + "mod_user," + "to_char(mod_time,'yyyymmdd') as mod_date";
    wp.daoTable = "cca_auth_parm";
    // if (empty(wp.whereStr)) {
    // wp.whereStr = " ORDER BY 1";
    // }
    wp.whereOrder = " order by card_note ";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // --
    for (int ll = 0; ll < wp.selectCnt; ll++) {
      String cardNote = cardNoteCheck(wp.colStr(ll, "card_note"));
      wp.colSet(ll, "tt_card_note", cardNote);
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    cardNote = wp.itemStr("data_k1");
    dataRead();
  }

  String cardNoteCheck(String cardNote) {
    // --
    if (eqAny(cardNote, "*")) {
      return "通用";
    }
    
    String sql1 = " select wf_desc from ptr_sys_idtab where wf_type = 'CARD_NOTE' and wf_id = ? ";
    sqlSelect(sql1,new Object[]{cardNote});
    if(sqlRowNum>0) {
    	return sqlStr("wf_desc");
    }
        
    return "";
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNote)) {
      cardNote = itemKk("card_note");
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno," + "card_note,   " + "oversea_cash_pct, "
        + "end_date, " + "open_chk," + "mcht_chk," + "delinquent," + "oversea_chk,"
        + "month_risk_chk," + "day_risk_chk," + " crt_date, crt_user, " + "mod_user,"
        + "to_char(mod_time,'yyyymmdd') as mod_date";
    wp.daoTable = "cca_auth_parm";
    wp.whereStr = " where 1=1 and area_type ='T'" + sqlCol(cardNote, "card_note");

    // this.sql_ddd();

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNote);
      return;
    }
    String cardNote = cardNoteCheck(wp.colStr("card_note"));
    wp.colSet("tt_card_note", cardNote);

    detlRead();
  }

  void detlRead() throws Exception {
    wp.selectSQL =
        "hex(rowid) as B_rowid, mod_seqno as B_mod_seqno," + "risk_level,   " + "tot_amt_pct, "
            + "max_cash_amt, " + "rsp_code," + "inst_month_pct," + "max_inst_amt," + "add_tot_amt";
    wp.daoTable = "CCA_RISK_LEVEL_PARM";
    wp.whereStr = " where 1=1 and area_type ='T'" + sqlCol(cardNote, "card_note");
    wp.whereOrder = " order by risk_level";

    // wp.pageRows =9999;
    // pageQuery();
    // wp.setListCount(1);
    pageSelect();
    wp.setListSernum(0, "", sqlRowNum);
    if (sqlRowNum <= 0) {
      this.selectOK();
      wp.colSet("ind_num", "0");
      return;
    }

    wp.colSet("ind_num", sqlRowNum);
    // wp.ddd("call-log="+wp.selectCnt+", sqlnrow="+this.sql_nrow);
  }

  void detlRead2() throws Exception {
    wp.selectSQL = "" + "risk_level,   " + "tot_amt_pct, " + "max_cash_amt, " + "rsp_code,"
        + "inst_month_pct," + "max_inst_amt," + "add_tot_amt";
    wp.daoTable = "CCA_RISK_LEVEL_PARM";
    wp.whereStr = " where 1=1 and area_type ='T' and card_note ='*' ";
    wp.whereOrder = " order by risk_level";

    pageSelect();
    wp.setListSernum(0, "", sqlRowNum);
    if (sqlRowNum <= 0)
      this.selectOK();
  }

  @Override
  public void saveFunc() throws Exception {

    wp.listCount[0] = wp.itemRows("risk_level");
    if (this.isDelete() && wp.itemEq("card_note", "*")) {
      errmsg("卡片等級：通用 不可全部刪除!");
      return;
    }

    if (checkApproveZz() == false) {
      return;
    }

    func = new ccam02.Ccam5051Func(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    if (this.isDelete() && rc == 1)
      wp.listCount[0] = 0;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud("XX");
      if (wp.autUpdate()) {
        if (wp.colEmpty("card_note"))
          this.btnOnAud(true, false, false);
        else
          this.btnOnAud(false, true, true);
      }
    }
  }

  /*
   * void reloadList() throws Exception{ wp.pageRows = 999; wp.sqlCmd = " select " +
   * " distinct class_code as risk_level " + " from ptr_class_code2 " + " where 1=1 " ;
   * 
   * pageQuery();
   * 
   * if(sql_nrow<=0){ errmsg("重新產生卡人等級 失敗 !"); return; }
   * 
   * wp.setListCount(0); wp.dddSql_log = false; String sql1 = " select " + " * " +
   * " from cca_risk_level_parm " + " where area_type ='T' " + " and risk_level = ? " +
   * " and card_note = ? " ;
   * 
   * String sql2 = " select " + " * " + " from cca_risk_level_parm " + " where area_type ='T' " +
   * " and risk_level = ? " + " and card_note ='*' " ;
   * 
   * String ls_card_note = ""; if(wp.item_empty("rowid")){ ls_card_note =
   * wp.item_ss("kk_card_note"); } else { ls_card_note = wp.item_ss("card_note"); }
   * 
   * for(int ii=0 ; ii<wp.selectCnt;ii++){ sqlSelect(sql1, new
   * Object[]{wp.col_ss(ii,"risk_level"),ls_card_note}); if(sql_nrow<=0){ sqlSelect(sql2,new
   * Object[]{wp.col_ss(ii,"risk_level")}); if(sql_nrow<=0){ wp.col_set(ii,"rsp_code", "");
   * wp.col_set(ii,"tot_amt_pct", ""+0); wp.col_set(ii,"add_tot_amt", ""+0);
   * wp.col_set(ii,"inst_month_pct", ""+0); wp.col_set(ii,"max_inst_amt", ""+0);
   * wp.col_set(ii,"max_cash_amt", ""+0); continue; } wp.col_set(ii,"rsp_code", sql_ss("rsp_code"));
   * wp.col_set(ii,"tot_amt_pct", sql_ss("tot_amt_pct")); wp.col_set(ii,"add_tot_amt",
   * sql_ss("add_tot_amt")); wp.col_set(ii,"inst_month_pct", sql_ss("inst_month_pct"));
   * wp.col_set(ii,"max_inst_amt", sql_ss("max_inst_amt")); wp.col_set(ii,"max_cash_amt",
   * sql_ss("max_cash_amt")); continue; }
   * 
   * wp.col_set(ii,"rsp_code", sql_ss("rsp_code")); wp.col_set(ii,"tot_amt_pct",
   * sql_ss("tot_amt_pct")); wp.col_set(ii,"add_tot_amt", sql_ss("add_tot_amt"));
   * wp.col_set(ii,"inst_month_pct", sql_ss("inst_month_pct")); wp.col_set(ii,"max_inst_amt",
   * sql_ss("max_inst_amt")); wp.col_set(ii,"max_cash_amt", sql_ss("max_cash_amt"));
   * 
   * }
   * 
   * alert_msg("重新產生卡人等級成功"); }
   */
  void procFunc() throws Exception {
    if (wp.itemEmpty("kk_card_note")) {
      alertErr2("卡片等級: 不可空白");
      return;
    }
    // --確認產生的卡人等級是否已存在
    String sql1 =
        "select count(*) as db_cnt from cca_auth_parm where 1=1 and area_type ='T' and card_note =? ";
    sqlSelect(sql1, new Object[] {wp.itemStr2("kk_card_note")});

    if (commString.strToNum(sqlStr("db_cnt")) > 0) {
      alertErr2("卡片等級已存在,不可產生參數表");
      return;
    }

    wp.selectSQL = "card_note,   " + "oversea_cash_pct, " + "end_date, " + "open_chk," + "mcht_chk,"
        + "delinquent," + "oversea_chk," + "month_risk_chk," + "day_risk_chk,"
        + " crt_date, crt_user, " + "mod_user," + "to_char(mod_time,'yyyymmdd') as mod_date";
    wp.daoTable = "cca_auth_parm";
    wp.whereStr = " where 1=1 and area_type ='T' and card_note ='*'";

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNote);
      return;
    }
    wp.colSet("card_note", wp.itemStr("kk_card_note"));
    String cardNote = cardNoteCheck(wp.colStr("card_note"));
    wp.colSet("tt_card_note", cardNote);

    detlRead2();
    wp.respMesg = "參數表產生完成";
  }
  
  void ccam5051Insert() throws Exception {	  
	  int isOk = 0, isError = 0, isEmpty = 0;
	  ccam02.Ccam5051Func func3 = new ccam02.Ccam5051Func(wp);
	  wp.listCount[0] = wp.itemRows("risk_level");
	  wp.colSet("IND_NUM", "" + wp.itemRows("risk_level"));
	  
	  if(checkApproveZz()==false)	return ;
	  
	  String lsCardNote = "";
	  lsCardNote = wp.itemStr("kk_card_note");
	  if(empty(lsCardNote))	{
		  alertErr("卡片等級: 不可空白");
		  return ;
	  }
	  
	  if(lsCardNote.equals("*")==false) {
		  alertErr("僅卡片等級:通用 可以使用新增，其餘請產生參數表後存檔");
		  return ;
	  }
	  
	 if(checkCcaAuthParm(lsCardNote)) {
		 alertErr("卡片等級:通用  資料已存在");
		 return ;
	 }
	 
	 func3.dbInsert();
	 
	 if(rc<=0) {
		 alertErr(func.getMsg());
		 dbRollback();
		 return;
	 }
	 
	 //--卡人等級
	 
	 String[] optArray = wp.itemBuff("opt");
	 String[] riskLevelArray = wp.itemBuff("risk_level");
	 String[] rspCodeArray = wp.itemBuff("rsp_code");
	 String[] totAmtPctArray = wp.itemBuff("tot_amt_pct");
	 String[] addTotAmtArray = wp.itemBuff("add_tot_amt");
	 String[] instMonthPctArray = wp.itemBuff("inst_month_pct");
	 String[] maxInstAmtArray = wp.itemBuff("max_inst_amt");
	 String[] maxCashAmtArray = wp.itemBuff("max_cash_amt");
	 
	 for(int ii=0;ii<wp.itemRows("risk_level");ii++) {
	      if ((checkBoxOptOn(ii, optArray)))
	          continue;
	      if (empty(rspCodeArray[ii])) {
	          isEmpty++;
	          wp.colSet(ii, "ok_flag", "!");
	          continue;
	      }

	      if (commString.strToNum(totAmtPctArray[ii]) <= 0) {
	          isEmpty++;
	          wp.colSet(ii, "ok_flag", "!");
	          continue;
	      }

	      if (commString.strToNum(instMonthPctArray[ii]) <= 0) {
	          isEmpty++;
	          wp.colSet(ii, "ok_flag", "!");
	          continue;
	      }

	      if (commString.strToNum(maxInstAmtArray[ii]) <= 0) {
	          isEmpty++;
	          wp.colSet(ii, "ok_flag", "!");
	          continue;
	      }

	      if (commString.strToNum(maxCashAmtArray[ii]) <= 0) {
	          isEmpty++;
	          wp.colSet(ii, "ok_flag", "!");
	          continue;
	      }
	 }
	 
	 if (isEmpty > 0) {		
	     alertErr2("回覆碼: 不可空白 ! or 數值需大於 0 ");
	     return;
	 }

	 if (func3.dbDeleteDtlAll() <= 0) {
	     errmsg(func.getMsg());
	     return;
	 }

	 func3.varModxxx(wp.loginUser, "Ccam5051", "1");

	 for (int ll = 0; ll < wp.itemRows("risk_level"); ll++) {
	      wp.colSet(ll, "ok_flag", "");
	      if (checkBoxOptOn(ll, optArray))
	        continue;

	      func3.varsSet("card_note", lsCardNote);
	      func3.varsSet("risk_level", riskLevelArray[ll]);
	      func3.varsSet("rsp_code", rspCodeArray[ll]);
	      func3.varsSet("tot_amt_pct", totAmtPctArray[ll]);
	      func3.varsSet("add_tot_amt", addTotAmtArray[ll]);
	      func3.varsSet("inst_month_pct", instMonthPctArray[ll]);
	      func3.varsSet("max_inst_amt", maxInstAmtArray[ll]);
	      func3.varsSet("max_cash_amt", maxCashAmtArray[ll]);

	      if (func3.dbInsertDtl() == 1) {
	        isOk++;
	        wp.colSet(ll, "ok_flag", "V");
	        sqlCommit(1);
	        continue;
	      } else {
	        isError++;
	        wp.colSet(ll, "ok_flag", "X");
	        dbRollback();
	        continue;
	      }
	    }

	    alertMsg("資料存檔處理完成; OK=" + isOk + ", ERR=" + isError);
	    if (isError == 0) {
	      dataRead();
	    }
	  
  }
  
  boolean checkCcaAuthParm(String cardNote) {
	  
	  String sql1 = "select count(*) as db_cnt from cca_auth_parm where card_note = ? and area_type = 'T' ";
	  
	  sqlSelect(sql1,new Object[] {cardNote});
	  
	  if(sqlRowNum<=0 || sqlNum("db_cnt")<=0)	return false;
	  	  
	  return true ;
  }
  
  void ccam5051Update() throws Exception {
    int isOk = 0, isError = 0, isEmpty = 0;

    ccam02.Ccam5051Func func3 = new ccam02.Ccam5051Func(wp);
    // func3.dataProcParm();

    String[] optArray = wp.itemBuff("opt");
    String[] riskLevelArray = wp.itemBuff("risk_level");
    String[] rspCodeArray = wp.itemBuff("rsp_code");
    String[] totAmtPctArray = wp.itemBuff("tot_amt_pct");
    String[] addTotAmtArray = wp.itemBuff("add_tot_amt");
    String[] instMonthPctArray = wp.itemBuff("inst_month_pct");
    String[] maxInstAmtArray = wp.itemBuff("max_inst_amt");
    String[] maxCashAmtArray = wp.itemBuff("max_cash_amt");

    wp.listCount[0] = wp.itemRows("risk_level");
    wp.colSet("IND_NUM", "" + wp.itemRows("risk_level"));

    String lsCardNote = "";
    lsCardNote = wp.itemStr("card_note");
    if (empty(lsCardNote))
      lsCardNote = wp.itemStr("kk_card_note");
    if (empty(lsCardNote)) {
      alertErr2("卡片等級不可空白");
      return;
    }

    if (isUpdate() && wp.itemRows("risk_level") == 0) {
      alertErr2("卡人等級不可空白 ");
      return;
    }
    if (checkApproveZz() == false) {
      return;
    }

    func3.dataProcParm();
    if (rc != 1) {
      alertErr2(func3.getMsg());
      return;
    }

    for (int zz = 0; zz < wp.itemRows("risk_level"); zz++) {
      if ((checkBoxOptOn(zz, optArray)))
        continue;
      if (empty(rspCodeArray[zz])) {
        isEmpty++;
        wp.colSet(zz, "ok_flag", "!");
        continue;
      }

      if (commString.strToNum(totAmtPctArray[zz]) <= 0) {
        isEmpty++;
        wp.colSet(zz, "ok_flag", "!");
        continue;
      }

      if (commString.strToNum(instMonthPctArray[zz]) <= 0) {
        isEmpty++;
        wp.colSet(zz, "ok_flag", "!");
        continue;
      }

      if (commString.strToNum(maxInstAmtArray[zz]) <= 0) {
        isEmpty++;
        wp.colSet(zz, "ok_flag", "!");
        continue;
      }

      if (commString.strToNum(maxCashAmtArray[zz]) <= 0) {
        isEmpty++;
        wp.colSet(zz, "ok_flag", "!");
        continue;
      }

    }

    if (isEmpty > 0) {
      alertErr2("回覆碼: 不可空白 ! or 數值需大於 0 ");
      return;
    }


    if (func3.dbDeleteDtlAll() <= 0) {
      errmsg(func.getMsg());
      return;
    }

    func3.varModxxx(wp.loginUser, "Ccam5051", "1");
    for (int ll = 0; ll < wp.itemRows("risk_level"); ll++) {
      wp.colSet(ll, "ok_flag", "");
      if (checkBoxOptOn(ll, optArray))
        continue;

      func3.varsSet("card_note", lsCardNote);
      func3.varsSet("risk_level", riskLevelArray[ll]);
      func3.varsSet("rsp_code", rspCodeArray[ll]);
      func3.varsSet("tot_amt_pct", totAmtPctArray[ll]);
      func3.varsSet("add_tot_amt", addTotAmtArray[ll]);
      func3.varsSet("inst_month_pct", instMonthPctArray[ll]);
      func3.varsSet("max_inst_amt", maxInstAmtArray[ll]);
      func3.varsSet("max_cash_amt", maxCashAmtArray[ll]);

      if (func3.dbInsertDtl() == 1) {
        isOk++;
        wp.colSet(ll, "ok_flag", "V");
        sqlCommit(1);
        continue;
      } else {
        isError++;
        wp.colSet(ll, "ok_flag", "X");
        dbRollback();
        continue;
      }
    }

    alertMsg("資料存檔處理完成; OK=" + isOk + ", ERR=" + isError);
    if (isError == 0) {
      dataRead();
    }
  }


}
