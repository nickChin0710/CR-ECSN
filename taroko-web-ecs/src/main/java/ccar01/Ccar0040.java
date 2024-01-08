package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*/

import ofcapp.BaseAction;

public class Ccar0040 extends BaseAction {

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
    String lsWhere = "", lsKey = "";

    if (chkStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2")) == false) {
      alertErr2("交易日期起迄錯誤");
      return;
    }

    lsWhere =
        " where 1=1 and iso_resp_code = '00' " + sqlCol(wp.itemStr2("ex_date1"), "tx_date", ">=")
            + sqlCol(wp.itemStr2("ex_date2"), "tx_date", "<=");

    if (wp.itemStr2("ex_idno").length() == 10) {
      lsKey = getIdPseqno();
      if (empty(lsKey)) {
        alertErr2("身分證ID:輸入錯誤");
        return;
      }
      lsWhere += sqlCol(lsKey, "id_p_seqno");
    } else if (wp.itemStr2("ex_idno").length() == 8) {
      lsKey = getCorpPseqno();
      if (empty(lsKey)) {
        alertErr2("公司統編:輸入錯誤");
        return;
      }
      lsWhere += sqlCol(lsKey, "corp_p_seqno");
    } else {
      alertErr2("身分證ID,公司統編輸入錯誤");
      return;
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  String getIdPseqno() {
    String sql1 = " select id_p_seqno from crd_idno where id_no = ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr2("ex_idno")});
    if (sqlRowNum > 0) {
      return sqlStr("id_p_seqno");
    }
    return "";
  }

  String getCorpPseqno() {
    String sql1 = " select corp_p_seqno from crd_corp where corp_no = ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr2("ex_idno")});
    if (sqlRowNum > 0) {
      return sqlStr("corp_p_seqno");
    }
    return "";
  }

  @Override
  public void queryRead() throws Exception {

    wp.selectSQL =
        " tx_date , risk_type , " + " count(*) as day_txn_cnt , " + " sum(nt_amt) as day_txn_amt ";

    wp.daoTable = "cca_auth_txlog";
    if (wp.itemStr2("ex_idno").length() == 10) {
      wp.selectSQL += " , id_p_seqno as ls_key ";
      wp.whereOrder =
          " group by id_p_seqno , tx_date , risk_type order by risk_type Asc , tx_date Asc ";
      wp.pageCountSql =
          " select count(*) from (select distinct id_p_seqno , tx_date , risk_type from cca_auth_txlog "
              + wp.queryWhere + ")";
    } else if (wp.itemStr2("ex_idno").length() == 8) {
      wp.selectSQL += " , corp_p_seqno as ls_key ";
      wp.whereOrder =
          " group by corp_p_seqno , tx_date , risk_type order by risk_type Asc , tx_date Asc ";
      wp.pageCountSql =
          " select count(*) from (select distinct corp_p_seqno , tx_date , risk_type from cca_auth_txlog "
              + wp.queryWhere + ")";
    }


    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);

    queryAfter();
  }

  void queryAfter() {
    int ilSelectCnt = 0;
    ilSelectCnt = wp.selectCnt;

    String sql1 = " select " + " count(*) as month_txn_cnt , " + " sum(nt_amt) as month_txn_amt "
        + " from cca_auth_txlog " + " where 1=1 " + " and tx_date like ? " + " and risk_type = ? "
        + " and iso_resp_code = '00' ";

    String sql2 = " select sys_data1 from cca_sys_parm1 where sys_id = 'RISK' and sys_key = ? ";

    if (wp.itemStr2("ex_idno").length() == 10) {
      sql1 += " and id_p_seqno = ? ";
    } else if (wp.itemStr2("ex_idno").length() == 8) {
      sql1 += " and corp_p_seqno = ? ";
    }

    for (int ii = 0; ii < ilSelectCnt; ii++) {
      String lsDate = "", lsDateLast = "";
      lsDate = commString.mid(wp.colStr(ii, "tx_date"), 0, 6) + "%";
      lsDateLast = commString.mid(wp.colStr(ii - 1, "tx_date"), 0, 6) + "%";

      if (ii != 0) {
        if (wp.colEq(ii, "risk_type", wp.colStr(ii - 1, "risk_type"))
            && eqIgno(lsDate, lsDateLast)) {
          wp.colSet(ii, "month_txn_cnt", wp.colStr(ii - 1, "month_txn_cnt"));
          wp.colSet(ii, "month_txn_amt", wp.colStr(ii - 1, "month_txn_amt"));
          wp.colSet(ii, "tt_risk_type", wp.colStr(ii - 1, "tt_risk_type"));
          continue;
        }
      }

      sqlSelect(sql1, new Object[] {lsDate, wp.colStr(ii, "risk_type"), wp.colStr(ii, "ls_key")});
      wp.colSet(ii, "month_txn_cnt", sqlStr("month_txn_cnt"));
      wp.colSet(ii, "month_txn_amt", sqlStr("month_txn_amt"));

      sqlSelect(sql2, new Object[] {wp.colStr(ii, "risk_type")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_risk_type", sqlStr("sys_data1"));
      }
    }
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
