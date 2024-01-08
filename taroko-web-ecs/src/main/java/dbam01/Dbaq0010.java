/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.00  yanghan  修改了變量名稱和方法名稱*
* 109-12-23  V1.00.01  Justin       parameterize sql
******************************************************************************/
package dbam01;

import ofcapp.BaseAction;

public class Dbaq0010 extends BaseAction {

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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_acct_key")) && empty(wp.itemStr("ex_card_no"))) {
      alertErr2("帳戶帳號, 卡號: 不可同時空白");
      return;
    }

    if (!empty(wp.itemStr("ex_acct_key"))) {
      if (wp.itemStr("ex_acct_key").length() < 8) {
        errmsg("帳戶帳號:至少 8 碼");
        return;
      }
      if (logQueryIdno("Y", wp.itemStr("ex_acct_key")) == false) {
        errmsg("無查詢權限 " + this.getMesg());
        return;
      }
    } else {
      if (logQueryIdno("Y", wp.itemStr("ex_card_no")) == false) {
        errmsg("無查詢權限 " + this.getMesg());
        return;
      }
    }

    String lsWhere = getWhereStr();
    setSqlParmNoClear(true);
    sumEndBal(lsWhere);
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

private String getWhereStr() {
	String ls_where = " where 1=1 and end_bal >0 ";

    if (!empty(wp.itemStr("ex_acct_key"))) {
      ls_where += " and p_seqno in (select p_seqno from dba_acno where acct_type ='90' "
          + sqlCol(wp.itemStr("ex_acct_key"), "acct_key", "like%") + ")";
    } else {
      ls_where += " and p_seqno in (select p_seqno from dbc_card where 1=1 "
          + sqlCol(wp.itemStr("ex_card_no"), "card_no") + ")";
    }
	return ls_where;
}

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + "uf_vd_idno_id(id_p_seqno) as db_idno," + "acct_type,"
        + "uf_vd_acno_key(p_seqno) as acct_key," + "acct_no," + "card_no," + "reference_no,"
        + "p_seqno," + "id_p_seqno," + "end_bal," + "acct_no as ex_acct_no,"
        + "uf_vd_acno_name(p_seqno) as ex_cname";
    wp.daoTable = "dba_debt";
    wp.whereOrder = " order by 1 Asc , 2 Asc , 3 Asc , 4 Asc , 5 Asc ";
    pageQuery();

    if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      wp.colSet("sum_end_bal", "" + 0);
    }
    wp.setListCount(0);
    wp.setPageValue();
  }

  public void sumEndBal(String lsWhere) {
    String sql1 = "select " + "sum(end_bal) as sum_end_bal " + "from dba_debt" + lsWhere;
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      wp.colSet("sum_end_bal", "" + 0);
      return;
    }

    wp.colSet("sum_end_bal", "" + sqlNum("sum_end_bal"));
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
