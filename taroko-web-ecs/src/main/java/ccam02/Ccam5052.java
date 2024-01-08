package ccam02;
/*
 * 卡戶等級風險類別限額參數維護 V.2018-0614.JH
 * 2018-0614:	JH		list-count
 * V00.0		JH		2017-0808: initial
 * V1.00.03  2020-0420   yanghan 修改了變量名稱和方法名稱
 * */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5052 extends BaseEdit {
Ccam5052Func func;
String cardNote = "", riskType="";

@Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R";
      reloadList();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      ccam5052Update();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
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
    }

    dddwSelect();
    initButton();
    if (wp.respHtml.indexOf("_detl") > 0) {
      wp.colSet("IND_NUM", "" + wp.listCount[0]);
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ccam5052")) {
        ddlbList("dddw_card_note", wp.itemStr("ex_card_note"), "ecsfunc.DeCodeCrd.card_note2");
      }
      if (wp.respHtml.indexOf("_detl") > 0) {
        ddlbList("dddw_card_note", wp.itemStr("kk_card_note"), "ecsfunc.DeCodeCrd.card_note2");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + " and A.area_type='T'" + " and B.card_note = A.card_note"
        + " and B.area_type = A.area_type" + sqlCol(wp.itemStr("ex_card_note"), "A.card_note")
        + sqlCol(wp.itemStr("ex_risk_type"), "B.risk_type");


    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " distinct " + "A.card_note, " + "A.oversea_cash_pct, " + "A.end_date, "
        + "A.open_chk, " + "A.mcht_chk," + "A.delinquent," + "A.oversea_chk," + "A.month_risk_chk,"
        + "A.day_risk_chk," + "A.mod_user," + "to_char(A.mod_time,'yyyymmdd') as mod_date,"
        + "B.risk_type";
    wp.daoTable = "cca_auth_parm A , CCA_RISK_CONSUME_PARM B";
    wp.whereOrder = " order by A.card_note, B.risk_type ";

    wp.pageCountSql = "select count(distinct A.card_note||B.risk_type) "
        + " from cca_auth_parm A , CCA_RISK_CONSUME_PARM B" + " " + wp.whereStr;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    cardNote = wp.itemStr("data_k1");
    riskType = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNote)) {
      cardNote = itemKk("card_note");
    }
    if (empty(riskType)) {
      riskType = itemKk("risk_type");
    }
    if (empty(cardNote) || empty(riskType)) {
      alertErr2("卡片等級, 風險類別: 不可空白");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno," + "card_note,   " + "open_chk, " + "mcht_chk, "
        + "delinquent," + "oversea_chk," + "month_risk_chk," + "day_risk_chk," + "end_date,"
        + "oversea_cash_pct," + "mod_user," + "to_char(mod_time,'yyyymmdd') as mod_date ";
    wp.daoTable = "cca_auth_parm ";
    wp.whereStr = " where 1=1 and area_type ='T'" + sqlCol(cardNote, "card_note");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNote);
      return;
    }

    wp.colSet("risk_type", riskType);

    detlRead1();
    detlRead2();
  }

  void detlRead1() throws Exception {
    // String ls_risk_type =kk2;
    wp.varRows = 9999;
    if (empty(riskType)) {
      riskType = itemKk("risk_type");
    }
    wp.selectSQL = "hex(rowid) as B_rowid, mod_seqno as B_mod_seqno," + "risk_level,   "
        + "lmt_amt_month_pct, " + "add_tot_amt, " + "rsp_code_1," + "lmt_cnt_month," + "rsp_code_2,"
        + "lmt_amt_time_pct," + "rsp_code_3," + "lmt_cnt_day," + "rsp_code_4," + "risk_type";
    wp.daoTable = "cca_risk_consume_parm";
    wp.whereStr = " where 1=1 and area_type ='T'" + sqlCol(cardNote, "card_note")
        + sqlCol(riskType, "risk_type");
    wp.whereOrder = " order by risk_level";
    pageSelect();
    if (sqlRowNum <= 0) {
      this.selectOK();
    }
    // wp.setListCount(1);
    wp.setListSernum(1, "", sqlRowNum);
  }

  void detlRead2() throws Exception {
    wp.varRows = 9999;
    wp.selectSQL = "risk_level as B_risk_level, " + "rsp_code as B_rsp_code, "
        + "tot_amt_pct as B_tot_amt_pct, " + "add_tot_amt as B_add_tot_amt, "
        + "inst_month_pct as B_inst_month_pct," + "max_inst_amt as B_max_inst_amt,"
        + "max_cash_amt as B_max_cash_amt";
    wp.daoTable = "CCA_RISK_LEVEL_PARM";
    wp.whereStr = " where 1=1 and area_type ='T'" + sqlCol(cardNote, "card_note");
    wp.whereOrder = " order by risk_level";

    pageSelect();
    if (sqlRowNum <= 0) {
      selectOK();
    }
    wp.setListSernum(2, "B_SER_NUM", sqlRowNum);
  }

  void reloadList() throws Exception {

    String lsRiskType = "", lsCardNote = "";

    if (wp.itemEmpty("rowid")) {
      lsRiskType = wp.itemStr("kk_risk_type");
      lsCardNote = wp.itemStr("kk_card_note");
    } else {
      lsRiskType = wp.itemStr("risk_type");
      lsCardNote = wp.itemStr("card_note");
    }

    wp.listCount[1] = wp.itemRows("B_risk_level");


    wp.varRows = 999;
    wp.sqlCmd = "";

    wp.sqlCmd = " select " + " risk_level , " + " rsp_code as rsp_code_1 , "
        + " rsp_code as rsp_code_2 , " + " rsp_code as rsp_code_3 , " + " rsp_code as rsp_code_4 , "
        + " tot_amt_pct as lmt_amt_month_pct  , " + " add_tot_amt " + " from cca_risk_level_parm "
        + " where 1=1 and area_type ='T' " + sqlCol(lsCardNote, "card_note")
        + " order by risk_level ";

    pageQuery();

    if (sqlRowNum <= 0) {
      errmsg("重置卡人等級失敗");
      return;
    }

    wp.setListCount(0);

    String sql1 = " select " + " lmt_amt_month_pct ," + " add_tot_amt ," + " rsp_code_1 ,"
        + " lmt_cnt_month ," + " rsp_code_2 ," + " lmt_amt_time_pct ," + " rsp_code_3 ,"
        + " lmt_cnt_day ," + " rsp_code_4 ," + " risk_type " + " from cca_risk_consume_parm "
        + " where 1=1 and area_type ='T' " + " and risk_type = ? " + " and card_note = ? "
        + " and risk_level = ? ";



    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {lsRiskType, lsCardNote, wp.colStr(ii, "risk_level")});

      if (sqlRowNum <= 0) {
        wp.colSet(ii, "lmt_cnt_month", "" + 0);
        wp.colSet(ii, "lmt_amt_time_pct", "" + 0);
        wp.colSet(ii, "lmt_cnt_day", "" + 0);
        wp.colSet(ii, "risk_type", "");
        continue;
      }

      wp.colSet(ii, "lmt_amt_month_pct", sqlStr("lmt_amt_month_pct"));
      wp.colSet(ii, "add_tot_amt", sqlStr("add_tot_amt"));
      wp.colSet(ii, "rsp_code_1", sqlStr("rsp_code_1"));
      wp.colSet(ii, "lmt_cnt_month", sqlStr("lmt_cnt_month"));
      wp.colSet(ii, "rsp_code_2", sqlStr("rsp_code_2"));
      wp.colSet(ii, "lmt_amt_time_pct", sqlStr("lmt_amt_time_pct"));
      wp.colSet(ii, "rsp_code_3", sqlStr("rsp_code_3"));
      wp.colSet(ii, "lmt_cnt_day", sqlStr("lmt_cnt_day"));
      wp.colSet(ii, "rsp_code_4", sqlStr("rsp_code_4"));
      wp.colSet(ii, "risk_type", sqlStr("risk_type"));

    }

    alertMsg("重新產生卡人等級成功");


  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ccam5052Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);

    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void ccam5052Update() throws Exception {
    int isOk = 0, isError = 0;
    int ii = 0;
    Ccam5052Func func3 = new Ccam5052Func();
    func3.setConn(wp);
    wp.log("k1=" + wp.itemStr("card_note") + ", k2=" + wp.itemStr("risk_type"));

    String[] aaRowid = wp.itemBuff("B_rowid");
    String[] aaOpt = wp.itemBuff("opt");
    String[] aaRl = wp.itemBuff("risk_level");
    String[] aaLamp = wp.itemBuff("lmt_amt_month_pct");
    String[] aaAta = wp.itemBuff("add_tot_amt");
    String[] aaRc1 = wp.itemBuff("rsp_code_1");
    String[] aaLcm = wp.itemBuff("lmt_cnt_month");
    String[] aaRc2 = wp.itemBuff("rsp_code_2");
    String[] aaLatp = wp.itemBuff("lmt_amt_time_pct");
    String[] aaRc3 = wp.itemBuff("rsp_code_3");
    String[] aaLcd = wp.itemBuff("lmt_cnt_day");
    String[] aaRc4 = wp.itemBuff("rsp_code_4");
    String[] aaOlddata = wp.itemBuff("old_data");

    wp.listCount[0] = wp.itemRows("B_rowid");
    wp.colSet("IND_NUM", "" + wp.itemRows("B_rowid"));
    func3.varModxxx(wp.loginUser, wp.modPgm(), "1");

    for (int ll = 0; ll < wp.itemRows("B_rowid"); ll++) {
      wp.colSet(ll, "ok_flag", "");

      // -option-ON: delete-
      if (checkBoxOptOn(ll, aaOpt)) {
        func3.varsSet("rowid", aaRowid[ll]);
        log("rowid:" + aaRowid[ll]);
        if (func3.dbDeleteDtl() != 1) {
          wp.colSet(ll, "ok_flag", "x");
          isError++;
        } else
          isOk++;
        continue;
      }
      // -no-update-
      String ssOld = aaLamp[ll] + "," + aaAta[ll] + "," + aaRc1[ll] + "," + aaLcm[ll] + ","
          + aaRc2[ll] + "," + aaLatp[ll] + "," + aaRc3[ll] + "," + aaLcd[ll] + "," + aaRc4[ll];

      if (eqAny(aaOlddata[ll], ssOld)) {
        continue;
      }

      func3.varsSet("card_note", wp.itemStr("card_note"));
      func3.varsSet("risk_type", wp.itemStr("risk_type"));
      func3.varsSet("risk_level", aaRl[ll]);
      func3.varsSet("lmt_amt_month_pct", aaLamp[ll]);
      func3.varsSet("add_tot_amt", aaAta[ll]);
      func3.varsSet("rsp_code_1", aaRc1[ll]);
      func3.varsSet("lmt_cnt_month", aaLcm[ll]);
      func3.varsSet("rsp_code_2", aaRc2[ll]);
      func3.varsSet("lmt_amt_time_pct", aaLatp[ll]);
      func3.varsSet("rsp_code_3", aaRc3[ll]);
      func3.varsSet("lmt_cnt_day", aaLcd[ll]);
      func3.varsSet("rsp_code_4", aaRc4[ll]);

      if (empty(aaRowid[ll]) == false) {
        func3.varsSet("rowid", aaRowid[ll]);
        rc = func3.dbDeleteDtl();
      }
      if (rc == 1) {
        rc = func3.dbInsertDtl();
      }

      if (rc == 1) {
        isOk++;
      } else {
        isError++;
        wp.colSet(ll, "ok_flag", "X");
      }
    }
    sqlCommit(rc);

    alertMsg("資料存檔處理完成; OK=" + isOk + ", ERR=" + isError);
    if (isError == 0) {
      dataRead();
    }
  }

}
