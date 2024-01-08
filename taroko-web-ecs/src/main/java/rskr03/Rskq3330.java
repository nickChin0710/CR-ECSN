/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package rskr03;

import ofcapp.BaseAction;

public class Rskq3330 extends BaseAction {
  String cardNo = "", txDate = "", txTime = "", authNo = "", traceNo = "";

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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
    if (chkStrend(wp.itemStr("ex_txn_date1"), wp.itemStr("ex_txn_date2")) == false) {
      alertErr2("消費日期:起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_txn_date1"), "tx_date", ">=")
        + sqlCol(wp.itemStr("ex_txn_date2"), "tx_date", "<=")
        + sqlCol(wp.itemStr("ex_risk_score"), "risk_score", ">=")
        + sqlCol(wp.itemStr("ex_card_no"), "card_no");

    // --身份證ID
    if (wp.itemEmpty("ex_idno") == false) {
      lsWhere += " and card_no in "
          + " (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 "
          + sqlCol(wp.itemStr("ex_idno"), "B.id_no") + " union "
          + " select A.card_no from dbc_card A join dbc_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 "
          + sqlCol(wp.itemStr("ex_idno"), "B.id_no") + " ) ";
    }

    // --授權交易成功or失敗
    if (wp.itemEq("ex_auth", "Y")) {
      lsWhere += " and iso_resp_code = '00' ";
    } else if (wp.itemEq("ex_auth", "N")) {
      lsWhere += " and iso_resp_code <> '00' ";
    }

    // --處理狀態
    if (wp.itemEq("ex_status_code", "A") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_status_code"), "status_code");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " tx_date , " + " tx_time , " + " card_no , " + " risk_score , "
        + " tx_currency , " + " ori_amt , " + " nt_amt , " + " auth_status_code , " + " auth_no , "
        + " mcht_no , " + " mcht_name , " + " mcht_city , " + " mcht_country , " + " status_code , "
        + " decode(status_code,'0','未處理','5','處理中','9','處理完成') as tt_status_code , "
        + " mcht_city_name , " + " trace_no ";

    wp.daoTable = " rsk_factormaster ";

    if (wp.itemEq("ex_order", "0")) {
      wp.whereOrder = " order by risk_score Asc ";
    } else if (wp.itemEq("ex_order", "1")) {
      wp.whereOrder = " order by card_no Asc ";
    } else if (wp.itemEq("ex_order", "2")) {
      wp.whereOrder = " order by tx_date Desc , tx_time Desc ";
    }

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
    wp.setListCount(0);
  }

  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    txDate = wp.itemStr("data_k2");
    txTime = wp.itemStr("data_k3");
    authNo = wp.itemStr("data_k4");
    traceNo = wp.itemStr("data_k5");

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.selectSQL =
        " card_no , uf_card_name(card_no) as chi_name , tx_date , tx_time , risk_score , auth_status_code , "
            + " iso_resp_code , decode(iso_resp_code,'00','成功','失敗') as tt_iso_resp_code , "
            + " auth_no , trace_no , tx_currency , ori_amt , nt_amt , mcht_no , mcht_country , mcht_city , "
            + " mcht_city_name , mcht_name , proc_date , proc_time , content_result , status_code , "
            + " proc_result , problem_flag , substring(proc_desc,1,100) as proc_desc1 , "
            + " substring(proc_desc,101,100) as proc_desc2 , substring(proc_desc,201,100) as proc_desc3 ";
    wp.daoTable = " rsk_factormaster ";
    wp.whereStr = " where 1=1 " + sqlCol(cardNo, "card_no") + sqlCol(txDate, "tx_date")
        + sqlCol(txTime, "tx_time") + sqlCol(authNo, "auth_no") + sqlCol(traceNo, "trace_no");

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    dataReadAfter();

  }

  void dataReadAfter() {
    boolean lbDebit = false;
    // --判斷卡片是信用卡還是VD卡
    String sql1 = " select debit_flag from cca_card_base where card_no = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
    lbDebit = eqIgno(sqlStr("debit_flag"), "Y");
    String sql2 = "";
    if (lbDebit) {
      sql2 = " select sup_flag from dbc_card where card_no = ? ";
    } else {
      sql2 = " select sup_flag from crd_card where card_no = ? ";
    }

    sqlSelect(sql2, new Object[] {wp.colStr("card_no")});
    if (sqlRowNum > 0) {
      wp.colSet("sup_flag", sqlStr("sup_flag"));
    }

    if (wp.colEq("content_result", "00")) {
      wp.colSet("tt_content_result", "聯繫成功");
    } else if (wp.colEq("content_result", "01")) {
      wp.colSet("tt_content_result", "電話停用");
    } else if (wp.colEq("content_result", "02")) {
      wp.colSet("tt_content_result", "傳真機");
    } else if (wp.colEq("content_result", "03")) {
      wp.colSet("tt_content_result", "無人接聽");
    } else if (wp.colEq("content_result", "04")) {
      wp.colSet("tt_content_result", "無此人");
    } else if (wp.colEq("content_result", "05")) {
      wp.colSet("tt_content_result", "空號");
    } else if (wp.colEq("content_result", "06")) {
      wp.colSet("tt_content_result", "語音信箱");
    } else if (wp.colEq("content_result", "07")) {
      wp.colSet("tt_content_result", "關機");
    } else if (wp.colEq("content_result", "08")) {
      wp.colSet("tt_content_result", "電話中");
    } else if (wp.colEq("content_result", "09")) {
      wp.colSet("tt_content_result", "出國");
    } else if (wp.colEq("content_result", "10")) {
      wp.colSet("tt_content_result", "非本人使用");
    } else if (wp.colEq("content_result", "11")) {
      wp.colSet("tt_content_result", "其他");
    }

    if (wp.colEq("status_code", "0")) {
      wp.colSet("tt_status_code", "未處理");
    } else if (wp.colEq("status_code", "5")) {
      wp.colSet("tt_status_code", "處理中");
    } else if (wp.colEq("status_code", "9")) {
      wp.colSet("tt_status_code", "處理完成");
    }

    if (wp.colEq("proc_result", "01")) {
      wp.colSet("tt_proc_result", "無須後續處理");
    } else if (wp.colEq("proc_result", "02")) {
      wp.colSet("tt_proc_result", "疑義交易 列問交");
    } else if (wp.colEq("proc_result", "03")) {
      wp.colSet("tt_proc_result", "偽冒交易 列問交");
    } else if (wp.colEq("proc_result", "04")) {
      wp.colSet("tt_proc_result", "CALL BANK");
    } else if (wp.colEq("proc_result", "05")) {
      wp.colSet("tt_proc_result", "其他");
    } else if (wp.colEq("proc_result", "99")) {
      wp.colSet("tt_proc_result", "處理中");
    }


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
