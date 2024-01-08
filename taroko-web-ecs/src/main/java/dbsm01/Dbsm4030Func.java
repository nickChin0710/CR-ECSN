/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-24  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package dbsm01;

import busi.FuncAction;

public class Dbsm4030Func extends FuncAction {

  @Override
  public void dataCheck() {
    String lsReferenceNo = "", lsTable = "", lsRowid = "", lsAcctCode = "";
    int ldcAmt = 0;
    lsReferenceNo = varsStr("reference_no");
    lsTable = varsStr("db_table");
    lsRowid = varsStr("rowid");

    if (eqIgno(lsTable, "acaj")) {
      if (checkAcajApprove(lsRowid) == false) {
        errmsg("此交易已主管覆核, 不可修改");
        return;
      }
      return;
    }

    if (eqIgno(lsTable, "debt") || eqIgno(lsTable, "debt_hst")) {
      // --check 是否other user已輸入---------------------------------------
      if (checkOtherUser(lsReferenceNo) == false) {
        errmsg("此交易已調整未處理, 現在不可調整");
        return;
      }

      // --已調整未處理------------------------------------------------------
      if (checkAcajProcess(lsReferenceNo) == false) {
        errmsg("此交易有調整未處理, 現在不可調整");
        return;
      }

      // --是否列問交-------------------------------------------------------
      if (checkPrbStatus(lsReferenceNo) == false) {
        errmsg("此交易已列問交卻未結案, 不可D檔");
        return;
      }

      ldcAmt = varsInt("db_damt");
      if (ldcAmt == 0) {
        errmsg("請輸入D檔金額");
        return;
      }

      if (ldcAmt > varsInt("d_avail_bal")) {
        errmsg("D檔金額 不可大於 可D數餘額");
        return;
      }

      // --利息 , 違約金--
      lsAcctCode = varsStr("acct_code");
      if (pos("|RI|AI|CI|PN|", lsAcctCode) > 0) {
        if (ldcAmt > 1000) {
          errmsg("D檔金額 不可大於 1000 元");
          return;
        }
      }

      if (eqIgno(lsAcctCode, "EF")) {
        if (empty(varsStr("adj_comment"))) {
          errmsg("費用 D檔 請輸入備註");
          return;
        }
      }
    }

    // --Select Data
    if (eqIgno(lsTable, "debt")) {
      selectDebt(lsRowid);
    } else if (eqIgno(lsTable, "debt_hst")) {
      selectDebtHst(lsRowid);
    }

  }

  boolean checkAcajApprove(String lsRowid) {
    String sql1 = " select " + " count(*) as db_cnt " + " from cms_acaj " + " where rowid =:rowid "
        + " and apr_date <> '' ";
    setRowId("rowid", lsRowid);
    sqlSelect(sql1);

    if (colNum("db_cnt") > 0)
      return false;

    return true;
  }

  boolean checkOtherUser(String lsReferenceNo) {
    String sql1 =
        " select " + " count(*) as db_cnt " + " from cms_acaj " + " where reference_no = ? "
            + " and uf_nvl(acct_post_flag,'N') ='N' " + " and uf_nvl(debit_flag,'N') ='Y' ";
    sqlSelect(sql1, new Object[] {lsReferenceNo});
    if (colNum("db_cnt") > 0)
      return false;
    return true;
  }

  boolean checkAcajProcess(String lsReferenceNo) {
    String sql1 = " select " + " count(*) as db_cnt " + " from act_acaj "
        + " where reference_no = ? " + " and nvl(process_flag,'N') <> 'Y' ";
    sqlSelect(sql1, new Object[] {lsReferenceNo});
    if (colNum("db_cnt") > 0)
      return false;
    return true;
  }

  boolean checkPrbStatus(String lsReferenceNo) {
    String sql1 = " select " + " nvl(prb_status,' ') as ls_status " + " from rsk_problem "
        + " where reference_no = ? " + " and reference_seq = 1 ";
    sqlSelect(sql1, new Object[] {lsReferenceNo});

    if (sqlRowNum > 0 && !eqIgno(colStr("ls_status"), "80"))
      return false;

    return true;
  }

  boolean selectDebt(String lsRowid) {
    String sql1 = " select " + " * " + " from dba_debt " + " where rowid =:rowid ";
    setRowId("rowid", lsRowid);

    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return false;

    return true;
  }

  boolean selectDebtHst(String lsRowid) {
    String sql1 = " select " + " * " + " from dba_debt_hst " + " where rowid =:rowid ";
    setRowId("rowid", lsRowid);

    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return false;
    return true;
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int insertAcaj() {
    msgOK();
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into cms_acaj ( " + " reference_no ," + " p_seqno ," + " acct_type ,"
        + " item_post_date ," + " item_order_normal ," + " item_order_back_date ,"
        + " item_order_refund ," + " item_class_normal ," + " item_class_back_date ,"
        + " item_class_refund ," + " acct_month ," + " stmt_cycle ," + " bill_type ,"
        + " txn_code ," + " beg_bal ," + " end_bal ," + " d_avail_bal ," + " card_no ,"
        + " acct_code ," + " interest_date ," + " purchase_date ," + " acquire_date ,"
        + " film_no ," + " mcht_no ," + " prod_no ," + " dp_reference_no ," + " interest_rs_date ,"
        + " acct_no ," + " source_table , " + " adj_amt " + " ) values ( " + " :reference_no ,"
        + " :p_seqno ," + " :acct_type ," + " :item_post_date ," + " :item_order_normal ,"
        + " :item_order_back_date ," + " :item_order_refund ," + " :item_class_normal ,"
        + " :item_class_back_date ," + " :item_class_refund ," + " :acct_month ," + " :stmt_cycle ,"
        + " :bill_type ," + " :txn_code ," + " :beg_bal ," + " :end_bal ," + " :d_avail_bal ,"
        + " :card_no ," + " :acct_code ," + " :interest_date ," + " :purchase_date ,"
        + " :acquire_date ," + " :film_no ," + " :mcht_no ," + " :prod_no ," + " :dp_reference_no ,"
        + " :interest_rs_date ," + " :acct_no ," + " :source_table , " + " :adj_amt " + " ) ";

    setString("reference_no", colStr("reference_no"));
    setString("p_seqno", colStr("p_seqno"));
    setString("acct_type", colStr("acct_type"));
    setString("item_post_date", colStr("item_post_date"));
    setString("item_order_normal", colStr("item_order_normal"));
    setString("item_order_back_date", colStr("item_order_back_date"));
    setString("item_order_refund", colStr("item_order_refund"));
    setString("item_class_normal", colStr("item_class_normal"));
    setString("item_class_back_date", colStr("item_class_back_date"));
    setString("item_class_refund", colStr("item_class_refund"));
    setString("acct_month", colStr("acct_month"));
    setString("stmt_cycle", colStr("stmt_cycle"));
    setString("bill_type", colStr("bill_type"));
    setString("txn_code", colStr("txn_code"));
    setString("beg_bal", "" + colNum("beg_bal"));
    setString("end_bal", "" + colNum("end_bal"));
    setString("d_avail_bal", "" + colNum("d_avail_bal"));
    setString("card_no", colStr("card_no"));
    setString("acct_code", colStr("acct_code"));
    setString("interest_date", colStr("interest_date"));
    setString("purchase_date", colStr("purchase_date"));
    setString("acquire_date", colStr("acquire_date"));
    setString("film_no", colStr("film_no"));
    setString("mcht_no", colStr("mcht_no"));
    setString("prod_no", colStr("prod_no"));
    setString("dp_reference_no", colStr("dp_reference_no"));
    setString("interest_rs_date", colStr("interest_rs_date"));
    setString("acct_no", colStr("acct_no"));
    setString("source_table", varsStr("db_table"));
    setString("adj_amt", varsStr("db_damt"));

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cms_acaj error !");
    }

    return rc;
  }

  public int deleteAcaj() {
    msgOK();
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " delete cms_acaj where rowid =:rowid ";
    setRowId("rowid", varsStr("rowid"));

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete cms_acaj error !");
    }

    return rc;
  }

}
