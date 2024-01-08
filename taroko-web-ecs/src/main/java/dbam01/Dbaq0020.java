/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package dbam01;

import ofcapp.BaseAction;

public class Dbaq0020 extends BaseAction {

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

    if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      errmsg("交易日期 起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 ";

    if (!empty(wp.itemStr("ex_acct_key"))) {
      lsWhere += " and A.p_seqno in (select p_seqno from dba_acno where acct_type ='90' "
          + sqlCol(wp.itemStr("ex_acct_key"), "acct_key", "like%") + ")";
    } else {
      lsWhere += " and A.p_seqno in (select p_seqno from dbc_card where 1=1 "
          + sqlCol(wp.itemStr("ex_card_no"), "card_no") + ")";
    }

    lsWhere += sqlCol(wp.itemStr("ex_date1"), "A.acct_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "A.acct_date", "<=");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " A.* , " + " A.acct_no as ex_acct_no,"
        + " uf_vd_acno_name(A.p_seqno) as ex_cname,"
        + " decode(tran_class,'B','入帳','D','扣款','C','D檔') as tt_tran_class , "
        + " (select chi_short_name from ptr_actcode where acct_code =A.acct_code) as tt_acct_code ";
    wp.daoTable = "dba_jrnl A ";
    wp.whereOrder =
        " order by A.item_post_date Asc , A.reference_no Asc , A.acct_date Asc , A.tran_class Asc ";
    pageQuery();

    if (this.sqlNotFind()) {
      alertErr2("DeBit流水帳務檔中無此帳戶資料");
      return;
    }

    wp.setListCount(1);
    queryAfter();
  }

  void queryAfter() throws Exception {
    int rr = -1;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "C"))
        continue;
      if (!eqIgno(commString.mid(wp.colStr(ii, "tran_type"), 0, 2), "DE")
          && !eqIgno(commString.mid(wp.colStr(ii, "tran_type"), 0, 2), "DP")
    	  && !eqIgno(commString.mid(wp.colStr(ii, "tran_type"), 0, 2), "DR"))
        continue;

      rr++;
      wp.colSet(rr, "A2_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A2_acct_code", wp.colStr(ii, "acct_code"));
      wp.colSet(rr, "A2_tt_acct_code", wp.colStr(ii, "tt_acct_code"));
      wp.colSet(rr, "A2_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A2_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A2_item_bal", wp.colStr(ii, "item_bal"));
      wp.colSet(rr, "A2_item_d_bal", wp.colStr(ii, "item_d_bal"));
      wp.colSet(rr, "A2_reference_no", wp.colStr(ii, "reference_no"));
      wp.colSet(rr, "A2_item_date", wp.colStr(ii, "item_date"));
      wp.colSet(rr, "A2_debit_item", wp.colStr(ii, "debit_item"));
      wp.colSet(rr, "A2_c_debt_key", wp.colStr(ii, "c_debt_key"));
      wp.colSet(rr, "A2_update_user", wp.colStr(ii, "update_user"));
      wp.colSet(rr, "A2_confirm_user", wp.colStr(ii, "confirm_user"));
      wp.colSet(rr, "A2_item_post_date", wp.colStr(ii, "item_post_date"));

    }

    wp.listCount[1] = (rr+1);
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
