/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import ofcapp.BaseAction;

public class Ccap5053 extends BaseAction {
  String cardCote = "", riskType = "", modUser = "";

  @Override
  public void userAction() throws Exception {
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
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ccap5053")) {
    	  wp.optionKey =wp.itemStr("ex_card_note");
    	  dddwAddOption("*","*_通用");
		  dddwList("dddw_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = "";

    lsWhere = " where 1=1 and area_type ='T' " + sqlCol(wp.itemStr("ex_card_note"), "card_note")
        + sqlCol(wp.itemStr2("ex_risk_type"), "risk_type");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.selectSQL =
        " card_note , " + " risk_type , " + " count(*) as li_unapr_cnt , " + " mod_user ";
    wp.daoTable = " cca_risk_consume_parm_t ";
    wp.whereOrder = " group by card_note , risk_type , mod_user order by risk_type ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
  }

  @Override
  public void querySelect() throws Exception {
    cardCote = wp.itemStr("data_k1");
    riskType = wp.itemStr("data_k2");
    modUser = wp.itemStr("data_k3");

    wp.colSet("kk_card_note", cardCote);
    wp.colSet("kk_risk_type", riskType);
    wp.colSet("kk_mod_user", modUser);
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.pageRows = 999;
    if (empty(cardCote))
      cardCote = wp.itemStr("kk_card_note");
    if (empty(riskType))
      riskType = wp.itemStr("kk_risk_type");
    if (empty(modUser))
      modUser = wp.itemStr("kk_mod_user");

    wp.selectSQL =
        "" + " risk_level , " + " lmt_amt_month_pct , " + " add_tot_amt , " + " rsp_code_1 , "
            + " lmt_cnt_month , " + " rsp_code_2 , " + " lmt_amt_time_pct , " + " rsp_code_3 , "
            + " lmt_cnt_day ," + " rsp_code_4 ," + " hex(rowid) as rowid , " + " mod_user ";
    wp.daoTable = " cca_risk_consume_parm_t ";
    wp.whereStr = " where 1=1 " + sqlCol(cardCote, "card_note") + sqlCol(riskType, "risk_type")
        + sqlCol(modUser, "mod_user");

    pageQuery();
    if (sqlNotFind()) {
      errmsg("此條件查無資料");
      return;
    }

    wp.setListCount(0);

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int count = 0, isOk = 0, isError = 0;
    String[] riskLevel = wp.itemBuff("risk_level");
    String[] lmtAmtMonthPct = wp.itemBuff("lmt_amt_month_pct");
    String[] rspCode1 = wp.itemBuff("rsp_code_1");
    String[] lmtCntMonth = wp.itemBuff("lmt_cnt_month");
    String[] rspCode2 = wp.itemBuff("rsp_code_2");
    String[] lmtAmtTimePct = wp.itemBuff("lmt_amt_time_pct");
    String[] rspCode3 = wp.itemBuff("rsp_code_3");
    String[] lmtCntDay = wp.itemBuff("lmt_cnt_day");
    String[] rspCode4 = wp.itemBuff("rsp_code_4");
    String[] modUser = wp.itemBuff("mod_user");
    String[] opt = wp.itemBuff("opt");

    wp.listCount[0] = wp.itemRows("risk_level");

    ccam02.Ccap5053Func func = new ccam02.Ccap5053Func();
    func.setConn(wp);

    func.varsSet("card_note", wp.itemStr("kk_card_note"));
    func.varsSet("risk_type", wp.itemStr("kk_risk_type"));

    for (int ii = 0; ii < wp.itemRows("risk_level"); ii++) {
      if (checkBoxOptOn(ii, opt) == false)
        continue;
      count++;

      func.varsSet("risk_level", riskLevel[ii]);
      func.varsSet("lmt_amt_month_pct", lmtAmtMonthPct[ii]);
      func.varsSet("rsp_code_1", rspCode1[ii]);
      func.varsSet("lmt_cnt_month", lmtCntMonth[ii]);
      func.varsSet("rsp_code_2", rspCode2[ii]);
      func.varsSet("lmt_amt_time_pct", lmtAmtTimePct[ii]);
      func.varsSet("rsp_code_3", rspCode3[ii]);
      func.varsSet("lmt_cnt_day", lmtCntDay[ii]);
      func.varsSet("rsp_code_4", rspCode4[ii]);
      func.varsSet("mod_user", modUser[ii]);

      if (func.dataProc() != 1) {
        isError++;
        this.dbRollback();
        wp.colSet(ii, "ok_flag", "X");
        continue;
      } else {
        isOk++;
        sqlCommit(1);
        wp.colSet(ii, "ok_flag", "V");
        continue;
      }
    }

    if (count == 0) {
      alertErr2("請勾選要覆核的資料");
      return;
    }

    wp.respMesg = "覆核完成 成功:" + isOk + " 失敗:" + isError;

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
