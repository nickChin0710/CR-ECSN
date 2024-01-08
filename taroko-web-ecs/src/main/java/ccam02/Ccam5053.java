package ccam02;
/**
 * 2019-1212   Alex  update initButton
 * 2019-1127   JH    deleteData
 *  2018-0925:	JH		bugfix
 * 2018-0828:	JH		++user_AUTH
 * 2018-0827:	JH		modify
 * 2020-0420   yanghan 修改了變量名稱和方法名稱
 * */
import ofcapp.BaseAction;

public class Ccam5053 extends BaseAction {
  String lsWhere = "", cardNote = "", riskType = "";

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      insertRiskTypeP();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      // -存檔-
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteData();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      //
      strAction = "S";
      querySelect();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      // --異動列表
      strAction = "S2";
      dataRead2();
    } else if (eqIgno(wp.buttonCode, "S3")) {
      // --異動列表
      strAction = "S3";
      dataRead3();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -擷取新卡人等級-
      strAction = "C";
      procFunc();
    } else if (eqIgno(wp.buttonCode, "C1")) {
      // -主管覆核-
      addNewRiskType();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ccam5053")) {
//        ddlbList("dddw_card_note", wp.itemStr("ex_card_note"), "ecsfunc.DeCodeCrd.card_note2");
    	wp.optionKey = wp.colStr("ex_card_note");
    	dddwAddOption("*", "*_通用");  
        dddwList("dddw_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
        wp.optionKey = wp.colStr("ex_risk_type");
        dddwList("dddw_risk_type", "Vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    lsWhere =
        " where 1=1 " + " and area_type ='T' " + sqlCol(wp.itemStr("ex_card_note"), "card_note")
            + sqlCol(wp.itemStr2("ex_risk_type"), "risk_type");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.sqlCmd =
        " select card_note , risk_type , sum(apr_cnt) as li_apr_y , sum(unapr_cnt) as li_apr_n from "
            + " (select card_note , risk_type , count(*) as apr_cnt , 0 as unapr_cnt from cca_risk_consume_parm "
            + lsWhere + " group by card_note , risk_type " + " union all "
            + "  select card_note , risk_type , 0 as apr_cnt , count(*) as unapr_cnt from cca_risk_consume_parm_t "
            + lsWhere + " group by card_note , risk_type "
            + " ) group by card_note , risk_type order by card_note , risk_type ";
    pageQuery();
    if (this.sqlNotFind()) {
      this.selectOK();
    }
    wp.setListCount(1);
  }

  @Override
  public void querySelect() throws Exception {
    cardNote = wp.itemStr("data_k1");
    riskType = wp.itemStr("data_k2");

    wp.colSet("kk_card_note", cardNote);
    wp.colSet("kk_risk_type", riskType);
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    cardNote = wp.itemStr("ex_card_note");
    riskType = wp.itemStr("ex_risk_type");

    if (empty(cardNote) || empty(riskType)) {
      alertErr2("卡片等級, 風險消費: 不可空白");
      return;
    }

    wp.colSet("kk_card_note", cardNote);
    wp.colSet("kk_risk_type", riskType);
    wp.colSet("kk_tt_card_note", cardNoteCheck(cardNote));
    
    wp.sqlCmd = " select " + " oversea_cash_pct, " + " end_date , " + " open_chk , " + " mcht_chk ,"
        + " delinquent , " + " oversea_chk , " + " month_risk_chk , " + " day_risk_chk "
        + " from cca_auth_parm " + " where area_type ='T' " + sqlCol(cardNote, "card_note");

    pageSelect();

    if (sqlRowNum <= 0) {
      errmsg("此條件查無資料");
      return;
    }

    wp.pageRows = 999;

    wp.sqlCmd = " select " + " '' as star_sign , " + " risk_level , " + " lmt_amt_month_pct , "
        + " add_tot_amt , " + " rsp_code_1 , " + " lmt_cnt_month , " + " rsp_code_2 , "
        + " lmt_amt_time_pct , " + " rsp_code_3 , " + " lmt_cnt_day ," + " rsp_code_4 ,"
        + " hex(rowid) as rowid , " + " mod_user " + " from cca_risk_consume_parm " + " where 1=1 "
        + sqlCol(cardNote, "card_note") + sqlCol(riskType, "risk_type")
        + " and risk_level not in (select risk_level from cca_risk_consume_parm_t where 1=1 "
        + sqlCol(cardNote, "card_note") + sqlCol(riskType, "risk_type") + " ) " + " union all "
        + " select " + " '*' as star_sign , " + " risk_level , " + " lmt_amt_month_pct , "
        + " add_tot_amt , " + " rsp_code_1 , " + " lmt_cnt_month , " + " rsp_code_2 , "
        + " lmt_amt_time_pct , " + " rsp_code_3 , " + " lmt_cnt_day ," + " rsp_code_4 ,"
        + " hex(rowid) as rowid , " + " mod_user " + " from cca_risk_consume_parm_t "
        + " where 1=1 " + sqlCol(cardNote, "card_note") + sqlCol(riskType, "risk_type")
        + " order by risk_level ";

    pageQuery();
    if (sqlRowNum <= 0) {
      errmsg("此條件查無資料");
      return;
    }
    wp.setListCount(0);
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
  
  void dataRead2() {
    int ii = -1;
    String[] lsRiskLevel = wp.itemBuff("risk_level");
    String[] liLmtAmtMonthPct = wp.itemBuff("lmt_amt_month_pct");
    String[] lsRspCode1 = wp.itemBuff("rsp_code_1");
    String[] liLmtCntMonth = wp.itemBuff("lmt_cnt_month");
    String[] lsRspCode2 = wp.itemBuff("rsp_code_2");
    String[] liLmtAmtTimePct = wp.itemBuff("lmt_amt_time_pct");
    String[] lsRspCode3 = wp.itemBuff("rsp_code_3");
    String[] liLmtCntDay = wp.itemBuff("lmt_cnt_day");
    String[] lsRspCode4 = wp.itemBuff("rsp_code_4");
    String[] lsModUser = wp.itemBuff("mod_user");
    String[] opt = wp.itemBuff("opt");

    wp.colSet("kk_card_note", wp.itemStr("kk_card_note"));
    wp.colSet("kk_tt_card_note", cardNoteCheck(wp.itemStr("kk_card_note")));
    wp.colSet("kk_risk_type", wp.itemStr("kk_risk_type"));

    for (int zz = 0; zz < wp.itemRows("risk_level"); zz++) {
      if (checkBoxOptOn(zz, opt) == false)
        continue;
      ii++;
      wp.colSet(ii, "risk_level", lsRiskLevel[zz]);
      wp.colSet(ii, "lmt_amt_month_pct", liLmtAmtMonthPct[zz]);
      wp.colSet(ii, "rsp_code_1", lsRspCode1[zz]);
      wp.colSet(ii, "lmt_cnt_month", liLmtCntMonth[zz]);
      wp.colSet(ii, "rsp_code_2", lsRspCode2[zz]);
      wp.colSet(ii, "lmt_amt_time_pct", liLmtAmtTimePct[zz]);
      wp.colSet(ii, "rsp_code_3", lsRspCode3[zz]);
      wp.colSet(ii, "lmt_cnt_day", liLmtCntDay[zz]);
      wp.colSet(ii, "rsp_code_4", lsRspCode4[zz]);
      wp.colSet(ii, "mod_user", lsModUser[zz]);
      continue;
    }

    wp.listCount[0] = ii + 1;
  }

  void dataRead3() {
    int ii = -1;
    String[] lsStarSign = wp.itemBuff("star_sign");
    String[] lsRiskLevel = wp.itemBuff("risk_level");
    String[] liLmtAmtMonthPct = wp.itemBuff("lmt_amt_month_pct");
    String[] lsRspCode1 = wp.itemBuff("rsp_code_1");
    String[] liLmtCntMonth = wp.itemBuff("lmt_cnt_month");
    String[] lsRspCode2 = wp.itemBuff("rsp_code_2");
    String[] liLmtAmtTimePct = wp.itemBuff("lmt_amt_time_pct");
    String[] lsRspCode3 = wp.itemBuff("rsp_code_3");
    String[] liLmtCntDay = wp.itemBuff("lmt_cnt_day");
    String[] lsRspCode4 = wp.itemBuff("rsp_code_4");
    String[] lsModUser = wp.itemBuff("mod_user");
    String[] opt = wp.itemBuff("opt");

    wp.colSet("kk_card_note", wp.itemStr("kk_card_note"));
    wp.colSet("kk_tt_card_note", cardNoteCheck(wp.itemStr("kk_card_note")));
    wp.colSet("kk_risk_type", wp.itemStr("kk_risk_type"));

    for (int zz = 0; zz < wp.itemRows("risk_level"); zz++) {
      if (checkBoxOptOn(zz, opt) == false)
        continue;
      log("B:" + lsStarSign[zz]);
      if (eqIgno(lsStarSign[zz], "*"))
        continue;
      ii++;
      wp.colSet(ii, "risk_level", lsRiskLevel[zz]);
      wp.colSet(ii, "lmt_amt_month_pct", liLmtAmtMonthPct[zz]);
      wp.colSet(ii, "rsp_code_1", lsRspCode1[zz]);
      wp.colSet(ii, "lmt_cnt_month", liLmtCntMonth[zz]);
      wp.colSet(ii, "rsp_code_2", lsRspCode2[zz]);
      wp.colSet(ii, "lmt_amt_time_pct", liLmtAmtTimePct[zz]);
      wp.colSet(ii, "rsp_code_3", lsRspCode3[zz]);
      wp.colSet(ii, "lmt_cnt_day", liLmtCntDay[zz]);
      wp.colSet(ii, "rsp_code_4", lsRspCode4[zz]);
      wp.colSet(ii, "mod_user", lsModUser[zz]);
      continue;
    }

    wp.listCount[0] = ii + 1;
  }

  @Override
  public void saveFunc() throws Exception {
    int llErr = 0, llOk = 0, llEmpty = 0;
    ccam02.Ccam5053Func func = new ccam02.Ccam5053Func();
    func.setConn(wp);

    String[] opt = wp.itemBuff("opt");
    String[] lsRiskLevel = wp.itemBuff("risk_level");
    String[] liLmtAmtMonthPct = wp.itemBuff("lmt_amt_month_pct");
    String[] lsRspCode1 = wp.itemBuff("rsp_code_1");
    String[] liLmtCntMonth = wp.itemBuff("lmt_cnt_month");
    String[] lsRspCode2 = wp.itemBuff("rsp_code_2");
    String[] liLmtAmtTimePct = wp.itemBuff("lmt_amt_time_pct");
    String[] lsRspCode3 = wp.itemBuff("rsp_code_3");
    String[] liLmtCntDay = wp.itemBuff("lmt_cnt_day");
    String[] lsRspCode4 = wp.itemBuff("rsp_code_4");

    int rows = wp.itemRows("risk_level");
    wp.listCount[0] = rows;
    if (rows <= 0) {
      alertErr2("無資料不須 [存檔]");
      return;
    }

    // -data.check---
    for (int zz = 0; zz < opt.length; zz++) {
      // if(checkBox_opt_on(zz, aa_opt)) continue;
      // -取消修改-
      int rr = optToIndex(opt[zz]);
      if (rr >= 0)
        continue;

      wp.colSet(zz, "ok_flag", "!");
      if (commString.strToNum(liLmtAmtMonthPct[zz]) <= 0) {
        llEmpty++;
        continue;
      }
      if (commString.strToNum(liLmtCntMonth[zz]) <= 0) {
        llEmpty++;
        continue;
      }
      if (commString.strToNum(liLmtAmtTimePct[zz]) <= 0) {
        llEmpty++;
        continue;
      }
      if (commString.strToNum(liLmtCntDay[zz]) <= 0) {
        llEmpty++;
        continue;
      }

      if (empty(lsRspCode1[zz])) {
        llEmpty++;
        continue;
      }

      if (empty(lsRspCode2[zz])) {
        llEmpty++;
        continue;
      }

      if (empty(lsRspCode3[zz])) {
        llEmpty++;
        continue;
      }

      if (empty(lsRspCode4[zz])) {
        llEmpty++;
        continue;
      }
      wp.colSet(zz, "ok_flag", "");
    }

    if (llEmpty > 0) {
      alertErr2("回覆碼: 不可空白 ! or 數值需大於 0 ");
      return;
    }

    func.varsSet("card_note", wp.itemStr("kk_card_note"));
    func.varsSet("risk_type", wp.itemStr("kk_risk_type"));
    for (int ii = 0; ii < wp.itemRows("risk_level"); ii++) {
      func.varsSet("risk_level", lsRiskLevel[ii]);
      func.varsSet("lmt_amt_month_pct", liLmtAmtMonthPct[ii]);
      func.varsSet("add_tot_amt", wp.itemStr(ii, "add_tot_amt"));
      func.varsSet("rsp_code_1", lsRspCode1[ii]);
      func.varsSet("lmt_cnt_month", liLmtCntMonth[ii]);
      func.varsSet("rsp_code_2", lsRspCode2[ii]);
      func.varsSet("lmt_amt_time_pct", liLmtAmtTimePct[ii]);
      func.varsSet("rsp_code_3", lsRspCode3[ii]);
      func.varsSet("lmt_cnt_day", liLmtCntDay[ii]);
      func.varsSet("rsp_code_4", lsRspCode4[ii]);

      rc = 1;
      rc = func.deleteTemp();
      if (checkBoxOptOn(ii, opt)) {
        sqlCommit(rc);
        optOkflag(ii, rc);
        if (rc != 1)
          llErr++;
        continue;
      }

      rc = func.insertTemp();
      sqlCommit(rc);
      optOkflag(ii, rc);
      if (rc == -1)
        llErr++;
      else
        llOk++;
    }

    wp.respMesg = "存檔完成 成功:" + llOk + "筆 失敗:" + llErr + " 筆";

  }

  void deleteData() throws Exception {
    int isOk = 0, isError = 0;
    wp.listCount[0] = wp.itemRows("risk_level");
    String[] optArray = wp.itemBuff("opt");
    String[] lsRiskLevel = wp.itemBuff("risk_level");
    if (wp.listCount[0] == 0) {
      alertErr("無資料可刪除");
      return;
    }
    if (wp.itemEq("kk_card_note", "*") && wp.itemEq("kk_risk_type", "P")) {
      if (wp.listCount[0] > 2) {
        alertErr("卡片等級:通用 風險類別:P 不可刪除(超過2筆)!!");
        return;
      }
    }

    if (checkApproveZz() == false) {
      return;
    }

    ccam02.Ccam5053Func func = new ccam02.Ccam5053Func();
    func.setConn(wp);

    func.varsSet("card_note", wp.itemStr2("kk_card_note"));
    func.varsSet("risk_type", wp.itemStr2("kk_risk_type"));
    for (int ii = 0; ii < wp.itemRows("risk_level"); ii++) {
      func.varsSet("risk_level", lsRiskLevel[ii]);

      if (func.deleteData() == 1) {
        isOk++;
        sqlCommit(1);
        wp.colSet(ii, "ok_flag", "V");
        continue;
      } else {
        isError++;
        dbRollback();
        wp.colSet(ii, "ok_flag", "X");
        continue;
      }

    }

    alertMsg("刪除完成 , 成功:" + isOk + " 失敗:" + isError);

  }

  void addNewRiskType() {
    ccam02.Ccam5053Func func = new ccam02.Ccam5053Func();
    func.setConn(wp);
    wp.listCount[0] = wp.itemRows("risk_type");

    rc = func.procFuncAdd();
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else {
      wp.respMesg = "風險類別新增完成";
    }

  }

  @Override
  public void procFunc() throws Exception {
    // wp.listCount[0] = wp.item_rows("risk_level");

    String cardNote = wp.itemStr2("kk_card_note");
    String riskType = wp.itemStr2("kk_risk_type");
    wp.sqlCmd = " select distinct " + " risk_level , " + " 0 as lmt_amt_month_pct , "
        + " add_tot_amt as add_tot_amt , " + " '' as rsp_code_1 , " + " 0 as lmt_cnt_month , "
        + " '' as rsp_code_2 , " + " 0 as lmt_amt_time_pct , " + " '' as rsp_code_3 , "
        + " 0 as lmt_cnt_day , " + " '' as rsp_code_4 , " + " '' as mod_user "
        + " from cca_risk_level_parm where apr_date <>'' and area_type='T'"
        + sqlCol(cardNote, "card_note") + " and risk_level not in "
        + " (select risk_level from cca_risk_consume_parm where 1=1 "
        + sqlCol(cardNote, "card_note") + sqlCol(riskType, "risk_type") + " union all "
        + " select risk_level from cca_risk_consume_parm_t where 1=1 "
        + sqlCol(cardNote, "card_note") + sqlCol(riskType, "risk_type") + " ) ";

    pageQuery();

    wp.setListCount(0);
    if (sqlNotFind()) {
      alertMsg("查無新卡人等級[ccaM5051]");
      selectOK();
      return;
    }

    wp.respMesg = "重新產生卡人等級成功，請修改後存檔";
  }

  @Override
  public void initButton() {
    this.btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }
  
  public void insertRiskTypeP() throws Exception {
	  
	  //--檢核通用-P類是否存在
	  String sql1 = " select count(*) as db_cnt from (select card_note from cca_risk_consume_parm ";
	  sql1 += " where card_note = '*' and risk_type ='P' union all select card_note from cca_risk_consume_parm_t ";
	  sql1 += " where card_note = '*' and risk_type ='P' ) ";
	  
	  sqlSelect(sql1);
	  
	  double aa = 0.0;
	  aa = sqlNum("db_cnt");
	  
	  if(sqlRowNum<0 || sqlNum("db_cnt")>0) {
		  alertErr("通用-P類已存在,請讀取後異動 !");
		  return ;
	  }
	  
	  wp.colSet("kk_card_note", "*");
	  wp.colSet("kk_tt_card_note","通用");
	  wp.colSet("kk_risk_type","P");
	  
	  wp.sqlCmd = " select distinct " + " risk_level , " + " 0 as lmt_amt_month_pct , "
		        + " add_tot_amt as add_tot_amt , " + " '' as rsp_code_1 , " + " 0 as lmt_cnt_month , "
		        + " '' as rsp_code_2 , " + " 0 as lmt_amt_time_pct , " + " '' as rsp_code_3 , "
		        + " 0 as lmt_cnt_day , " + " '' as rsp_code_4 , " + " '' as mod_user "
		        + " from cca_risk_level_parm where apr_date <>'' and area_type='T'"
		        + " and card_note = '*' ";
	  
	  pageQuery();
	  	 
	  if(sqlNotFind()) {
		  alertErr("查無卡人等級資料,請至 Ccam5051 進行設定");
		  return ;
	  }
	  
	  wp.setListCount(0);	  
  }
  
}
