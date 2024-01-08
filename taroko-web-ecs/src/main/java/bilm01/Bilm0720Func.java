/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-23  V1.00.00  ryan       program initial                            *
* 107-02-09  V1.00.01  ryan       update                                     *
*109-04-24  V1.00.01  shiyuqi       updated for project coding standard      *   
* 109-11-19  V1.00.02  Ryan       移除畫面部分欄位與邏輯                                                                  *   
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Bilm0720Func extends FuncEdit {
  String kk1 = "", lsDesc = "";
  int ok = 0;

  public Bilm0720Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from bil_auto_tx where card_no = ? and reference_no = ? and authorization = ? ";
      Object[] param = new Object[] {wp.itemStr("kk_card_no"), wp.itemStr("reference_no"),
          wp.itemStr("authorization")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {

      // -other modify-
      sqlWhere =
          " where card_no = ? and reference_no = ? and authorization = ? and nvl(mod_seqno,0) = ?";

      Object[] param2 = new Object[] {wp.itemStr("card_no"), wp.itemStr("reference_no"),
          wp.itemStr("authorization2"), wp.modSeqno()};
      if (isOtherModify("bil_auto_tx", sqlWhere, param2)) {
        errmsg("請重新查詢");
        return;
      }
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_auto_tx");
    sp.ppstr("card_no", wp.itemStr("kk_card_no"));
    sp.ppstr("authorization", wp.itemStr("authorization"));
    sp.ppstr("mcht_no_parm", wp.itemStr("mcht_no_parm"));
    sp.ppnum("tot_term", wp.itemNum("tot_term"));
    sp.ppstr("mcht_no", wp.itemStr("mcht_no"));
    sp.ppnum("dest_amt", wp.itemNum("dest_amt"));
    sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
    sp.ppstr("mcht_chi_name", wp.itemStr("mcht_chi_name"));
    sp.ppstr("sale_emp_no", wp.itemStr("sale_emp_no"));
    sp.ppstr("action_desc", wp.itemStr("action_desc"));
    sp.ppstr("destination_amt_flag", wp.itemStr("destination_amt_flag"));
    sp.ppstr("destination_amt_parm", wp.itemStr("destination_amt_parm"));
    sp.ppstr("payment_rate_flag", wp.itemStr("payment_rate_flag"));
    sp.ppstr("payment_rate_term", wp.itemStr("payment_rate_term"));
    sp.ppstr("rc_rate_flag", wp.itemStr("rc_rate_flag"));
    sp.ppstr("rc_rate_parm", wp.itemStr("rc_rate_parm"));
    sp.ppstr("credit_amt_rate_parm", wp.itemStr("credit_amt_rate_parm"));
    sp.ppstr("mcc_code_flag", wp.itemStr("mcc_code_flag"));
    sp.ppstr("over_credit_amt_flag", wp.itemStr("over_credit_amt_flag"));
    sp.ppstr("block_reason_flag", wp.itemStr("block_reason_flag"));
    sp.ppstr("spec_status_flag", wp.itemStr("spec_status_flag"));
    sp.ppstr("mcht_flag", wp.itemStr("mcht_flag"));
    sp.ppstr("rc_rate", wp.itemStr("rc_rate"));
    sp.ppstr("credit_amt_rate", wp.itemStr("credit_amt_rate"));
    sp.ppstr("mcht_category", wp.itemStr("mcht_category"));
    sp.ppnum("line_of_credit_amt", wp.itemNum("line_of_credit_amt"));
    sp.ppnum("acct_jrnl_bal", wp.itemNum("acct_jrnl_bal"));
    sp.ppstr("spec_status", wp.itemStr("spec_status"));
    sp.ppstr("spec_del_date", wp.itemStr("spec_del_date"));
    sp.ppstr("trial_status", wp.itemStr("trial_status"));
    sp.ppstr("block_reason", wp.itemStr("block_reason"));
    sp.ppstr("block_reason2", wp.itemStr("block_reason2"));
    for (int i = 1; i <= 25; i++) {
      sp.ppstr("payment_rate" + i, wp.itemStr("payment_rate" + i));
    }
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", tx_date", ", to_char(sysdate,'YYYYMMDD')");
    sp.ppstr("apr_user_1", wp.loginUser);
    sp.addsql(", apr_date_1 ", ", to_char(sysdate,'YYYYMMDD')");
    sp.ppstr("reference_no", wp.itemStr("reference_no"));
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    return rc;
  }


  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("bil_auto_tx");
    sp.ppstr("authorization", wp.itemStr("authorization"));
    sp.ppstr("mcht_no_parm", wp.itemStr("mcht_no_parm"));
    sp.ppnum("tot_term", wp.itemNum("tot_term"));
    sp.ppstr("mcht_no", wp.itemStr("mcht_no"));
    sp.ppnum("dest_amt", wp.itemNum("dest_amt"));
    sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
    sp.ppstr("mcht_chi_name", wp.itemStr("mcht_chi_name"));
    sp.ppstr("sale_emp_no", wp.itemStr("sale_emp_no"));
    sp.ppstr("action_desc", wp.itemStr("action_desc"));
    sp.ppstr("destination_amt_flag", wp.itemStr("destination_amt_flag"));
    sp.ppstr("destination_amt_parm", wp.itemStr("destination_amt_parm"));
    sp.ppstr("payment_rate_flag", wp.itemStr("payment_rate_flag"));
    sp.ppstr("payment_rate_term", wp.itemStr("payment_rate_term"));
    sp.ppstr("rc_rate_flag", wp.itemStr("rc_rate_flag"));
    sp.ppstr("rc_rate_parm", wp.itemStr("rc_rate_parm"));
    sp.ppstr("credit_amt_rate_parm", wp.itemStr("credit_amt_rate_parm"));
    sp.ppstr("mcc_code_flag", wp.itemStr("mcc_code_flag"));
    sp.ppstr("over_credit_amt_flag", wp.itemStr("over_credit_amt_flag"));
    sp.ppstr("block_reason_flag", wp.itemStr("block_reason_flag"));
    sp.ppstr("spec_status_flag", wp.itemStr("spec_status_flag"));
    sp.ppstr("mcht_flag", wp.itemStr("mcht_flag"));
    sp.ppstr("rc_rate", wp.itemStr("rc_rate"));
    sp.ppstr("credit_amt_rate", wp.itemStr("credit_amt_rate"));
    sp.ppstr("mcht_category", wp.itemStr("mcht_category"));
    sp.ppnum("line_of_credit_amt", wp.itemNum("line_of_credit_amt"));
    sp.ppnum("acct_jrnl_bal", wp.itemNum("acct_jrnl_bal"));
    sp.ppstr("spec_status", wp.itemStr("spec_status"));
    sp.ppstr("spec_del_date", wp.itemStr("spec_del_date"));
    sp.ppstr("trial_status", wp.itemStr("trial_status"));
    sp.ppstr("block_reason", wp.itemStr("block_reason"));
    sp.ppstr("block_reason2", wp.itemStr("block_reason2"));
    for (int i = 1; i <= 25; i++) {
      sp.ppstr("payment_rate" + i, wp.itemStr("payment_rate" + i));
    }
    sp.ppstr("apr_user_1", wp.loginUser);
    sp.addsql(", apr_date_1 =to_char(sysdate,'YYYYMMDD')");
    sp.addsql(", mod_time =sysdate");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
    sp.sql2Where("where card_no=?", wp.itemStr("card_no"));
    sp.sql2Where("and reference_no=?", wp.itemStr("reference_no"));
    sp.sql2Where("and authorization=?", wp.itemStr("authorization2"));
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    msgOK();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete bil_auto_tx  where hex(rowid) = ? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {wp.itemStr("rowid"), wp.modSeqno()};

    rc = sqlExec(strSql, param);

    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
