/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-05  V1.00.01  Alex       add initButton                             *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                           *
******************************************************************************/
package dbsm01;

import ofcapp.BaseAction;

public class Dbsm4030 extends BaseAction {
  String isWhere = "";

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

  public int queryBefore() throws Exception {

    String lsKey = "";
    lsKey = commString.acctKey(wp.itemStr2("ex_acct_key"));

    String sql1 = " select " + " p_seqno , " + " acct_no as ex_acct_no , "
        + " uf_vd_acno_name(p_seqno) as ex_cname " + " from dba_acno " + " where acct_key =? "
        + " and acct_type = ? ";

    sqlSelect(sql1, new Object[] {lsKey, wp.itemStr("ex_acct_type")});

    if (sqlRowNum <= 0)
      return -1;

    wp.colSet("p_seqno", sqlStr("p_seqno"));
    wp.colSet("ex_acct_no", sqlStr("ex_acct_no"));
    wp.colSet("ex_cname", sqlStr("ex_cname"));

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.itemStr("ex_acct_key").length() < 8) {
      alertErr2("帳戶帳號至少8碼 !");
      return;
    }

    String lsKey = "";
    lsKey = commString.acctKey(wp.itemStr2("ex_acct_key"));
    if (lsKey.length() != 11) {
      alertErr2("帳戶帳號輸入錯誤 !");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_mon1"), wp.itemStr("ex_mon2")) == false) {
      alertErr2("關帳年月: 起迄錯誤");
      return;
    }

    if (queryBefore() < 0) {
      alertErr2("此條件查無資料 !");
      return;
    }

    isWhere = " where 1=1 " + sqlCol(sqlStr("p_seqno"), "p_seqno")
        + sqlCol(wp.itemStr("ex_mon1"), "acct_month", ">=")
        + sqlCol(wp.itemStr("ex_mon2"), "acct_month", "<=");

    if (eqIgno(wp.itemStr("ex_acitem01"), "1")) {
      isWhere += " and acct_code in ('AF','LF','CF','PF','SF','CC') ";
    } else {
      isWhere += " and acct_code <> 'DP' ";
    }


    wp.setQueryMode();
    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.sqlCmd = " select " + " reference_no ," + " p_seqno ," + " acct_type ,"
        + " uf_vd_acno_key(p_seqno) as acct_key ," + " item_post_date ," + " acct_month ,"
        + " card_no ," + " interest_date ," + " beg_bal ," + " end_bal ," + " d_avail_bal ,"
        + " txn_code ," + " acct_code ," + " bill_type ," + " hex(rowid ) as rowid ,"
        + " interest_rs_date ," + " 0 as db_d_amt ," + " '' as db_adj_comment ,"
        + " 'debt' as db_table ," + " acct_no " + " from dba_debt " + isWhere + " union "
        + " select " + " reference_no ," + " p_seqno ," + " acct_type ,"
        + " uf_vd_acno_key(p_seqno) as acct_key ," + " item_post_date ," + " acct_month ,"
        + " card_no ," + " interest_date ," + " beg_bal ," + " end_bal ," + " d_avail_bal ,"
        + " txn_code ," + " acct_code ," + " bill_type ," + " hex(rowid ) as rowid ,"
        + " interest_rs_date ," + " 0 as db_d_amt ," + " '' as db_adj_comment ,"
        + " 'debt_hst' as db_table ," + " acct_no " + " from dba_debt_hst " + isWhere
        + " order by item_post_date Asc ";

    this.pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    queryAfter();
    wp.setListCount(0);
  }

  void queryAfter() throws Exception {
    wp.logSql = false;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (selectAcaj(wp.colStr(ii, "reference_no")) == false) {
        wp.colSet(ii, "checkD", "disabled");
        wp.colSet(ii, "check", "");
        continue;
      }

      wp.colSet(ii, "beg_bal", sqlStr("beg_bal"));
      wp.colSet(ii, "end_bal", sqlStr("end_bal"));
      wp.colSet(ii, "d_avail_bal", sqlStr("d_avail_bal"));
      wp.colSet(ii, "db_d_amt", sqlStr("adj_amt"));
      wp.colSet(ii, "adj_comment", sqlStr("adj_remark"));
      wp.colSet(ii, "rowid", sqlStr("rowid"));
      wp.colSet(ii, "db_table", "acaj");
      wp.colSet(ii, "check", "disabled");
      wp.colSet(ii, "checkD", "");

    }
  }

  boolean selectAcaj(String lsReferenceNo) {
    String sql1 = " select " + " A.* , " + " hex(rowid) as rowid " + " from cms_acaj A "
        + " where A.reference_no = ? " + " and A.acct_post_flag <> 'Y' ";

    sqlSelect(sql1, new Object[] {lsReferenceNo});

    if (sqlRowNum <= 0)
      return false;
    return true;
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
    Dbsm4030Func func = new Dbsm4030Func();
    func.setConn(wp);
    int llOk = 0, llErr = 0, llCnt = 0;
    String[] lsOpt = wp.itemBuff("opt");
    String[] lsOptDel = wp.itemBuff("opt_del");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] lsTable = wp.itemBuff("db_table");
    String[] lsReferenceNo = wp.itemBuff("reference_no");
    String[] lsAcctCode = wp.itemBuff("acct_code");
    String[] lsDbAmt = wp.itemBuff("db_d_amt");
    String[] lsDAvailBal = wp.itemBuff("d_avail_bal");

    wp.listCount[0] = lsRowid.length;

    for (int rr = 0; rr < lsRowid.length; rr++) {
      if (!checkBoxOptOn(rr, lsOpt) && !checkBoxOptOn(rr, lsOptDel))
        continue;
      llCnt++;
      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("db_table", lsTable[rr]);
      func.varsSet("reference_no", lsReferenceNo[rr]);
      func.varsSet("acct_code", lsAcctCode[rr]);
      func.varsSet("db_damt", lsDbAmt[rr]);
      func.varsSet("d_avail_bal", lsDAvailBal[rr]);

      if (checkBoxOptOn(rr, lsOptDel) && eqIgno(lsTable[rr], "acaj")) {
        if (func.deleteAcaj() == 1) {
          llOk++;
          sqlCommit(1);
          wp.colSet(rr, "ok_flag", "V");
          continue;
        } else {
          llErr++;
          dbRollback();
          wp.colSet(rr, "ok_flag", "X");

          continue;
        }
      } else if (checkBoxOptOn(rr, lsOpt)
          && (eqIgno(lsTable[rr], "debt") || eqIgno(lsTable[rr], "debt_hst"))) {
        if (func.insertAcaj() == 1) {
          llOk++;
          sqlCommit(1);
          wp.colSet(rr, "ok_flag", "V");
          continue;
        } else {
          llErr++;
          dbRollback();
          wp.colSet(rr, "ok_flag", "X");
          continue;
        }
      }

    }

    if (llCnt == 0) {
      alertErr2("請選擇處理資料 !");
      return;
    } else {
      alertMsg("資料處理完成 , 成功 :" + llOk + " 失敗 :" + llErr);
    }

  }

  @Override
  public void initButton() {
    btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
