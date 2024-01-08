/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  yanghan  修改了變量名稱和方法名稱*
* 110-01-13  V1.00.02  Justin        parameterize sql 
******************************************************************************/
package ccam02;

import ofcapp.BaseAction;

public class Ccaq5053 extends BaseAction {

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
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
      /* 動態查詢 */
      strAction = "C";
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ccaq5053")) {
    	wp.optionKey =wp.itemStr("ex_card_note");
    	dddwAddOption("*","*_通用");
		dddwList("dddw_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
        wp.optionKey = wp.itemStr2("ex_risk_level");
        this.dddwList("dddw_risk_level", "ptr_class_code2", "distinct class_code",
            "where 1=1 and apr_flag='Y'");
        wp.optionKey = wp.itemStr2("ex_risk_type");
        this.dddwList("dddw_risk_type", "vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 and area_type ='T' and apr_date <> '' "
        + sqlCol(wp.itemStr("ex_card_note"), "card_note")
        + sqlCol(wp.itemStr("ex_risk_type"), "risk_type");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

    String sql1 = "select uf_tt_risk_type(?) as tt_risk_type from dual";
    sqlSelect(sql1, new Object[] {wp.itemStr("ex_risk_type")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_risk_type", sqlStr("tt_risk_type"));
    }

    wp.selectSQL = " risk_level ," + " lmt_amt_month_pct , " + " add_tot_amt , " + " rsp_code_1 , "
        + " lmt_cnt_month , " + " rsp_code_2 , " + " lmt_amt_time_pct , " + " rsp_code_3 , "
        + " lmt_cnt_day ," + " rsp_code_4 ," + " hex(rowid) as rowid ";
    wp.daoTable = " cca_risk_consume_parm ";

    pageQuery();

    if (sqlRowNum <= 0) {
      errmsg("此條件查無資料");
      return;
    }
    wp.setListCount(0);

    wp.sqlCmd = " select " + " oversea_cash_pct, " + " end_date , " + " open_chk , " + " mcht_chk ,"
        + " delinquent , " + " oversea_chk , " + " month_risk_chk , " + " day_risk_chk "
        + " from cca_auth_parm " + " where area_type ='T' "
        + sqlCol(wp.itemStr("ex_card_note"), "card_note");

    pageSelect();

    if (sqlRowNum <= 0) {
      errmsg("此條件查無資料");
      return;
    }

    wp.colSet("kk_card_note", wp.itemStr("ex_card_note"));
    wp.colSet("kk_risk_type", wp.itemStr("ex_risk_type"));

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

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
