/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Aoyulan       updated for project coding standard   *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package colm01;

import busi.FuncProc;

import taroko.com.TarokoCommon;

public class Colm0130Func extends FuncProc {
  String referenceNo, optcode;

  public Colm0130Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    referenceNo = wp.itemStr("reference_no");
    optcode = wp.itemStr("optcode");
    // kk1 = vars_ss("reference_no");
    log(this.actionCode + ", kk1=" + wp.itemStr("reference_no") + ", mod_seqno=" + wp.modSeqno());

    // -other modify-
    sqlWhere = " where reference_no = :reference_no ";
    // + " and nvl(mod_seqno,0) =" + var_modseqno();
    // + " and nvl(mod_seqno,0) =" + wp.mod_seqno();
    setString("reference_no", referenceNo);
    // 1.轉 才check duplicate 2.刪 不理他
    // if ("1".equals(kk2)){
    // other_modify("col_debt_t", sql_where); //other_modify都不會過??
    // }
  }

  @Override
  public int dataProc() {
    dataCheck();
    log(this.actionCode + ", rc=" + rc);
    if ("1".equals(optcode)) {
      rc = insertFunc();
    } else {
      rc = deleteFunc();
    }
    return rc;
  }

  int insertFunc() {
    if (rc != 1)
      return rc;
    /*
     * is_sql = "insert into col_debt_t (" + "select " + "reference_no, " + "p_seqno, " +
     * "acct_type, " + ":chi_name , " + "post_date, " + "acct_month, " + "bill_type, " +
     * "txn_code, " + "beg_bal, " + "end_bal, " + "d_avail_bal, " + "acct_code, " +
     * "interest_date, " + ":acct_status , " + "'' apr_date, " + "'' apr_user, " + "'1' aud_code, "
     * + "to_char(sysdate,'yyyymmdd') crt_date, " + ":crt_user , " + ":mod_user , " +
     * "sysdate mod_time, " + ":mod_pgm , " + "0 mod_seqno " + "from act_debt " +
     * "where reference_no = :reference_no " + ") "; setString("chi_name", wp.item_ss("chi_name"));
     * setString("acct_status", wp.item_ss("acct_status")); setString("crt_user",
     * wp.item_ss("mod_user")); setString("mod_user", wp.item_ss("mod_user")); setString("mod_pgm",
     * wp.item_ss("mod_pgm")); setString("reference_no", kk1); this.ddd("insertFunc()="+is_sql); rc
     * = sqlExec(is_sql); if (sql_nrow <= 0) { errmsg(this.sql_errtext); } return rc;
     */
    /*
     * is_sql = "insert into col_debt_t (" + "select " + "reference_no, " + "p_seqno, " +
     * "acct_type, " + "? , " + "post_date, " + "acct_month, " + "bill_type, " + "txn_code, " +
     * "beg_bal, " + "end_bal, " + "d_avail_bal, " + "acct_code, " + "interest_date, " + "? , " +
     * "'' apr_date, " + "'' apr_user, " + "'1' aud_code, " +
     * "to_char(sysdate,'yyyymmdd') crt_date, " + "? , " //crt_user + "? , " + "sysdate mod_time, "
     * + "? , " + "0 mod_seqno " + "from act_debt " + "where reference_no = ? " + ") "; //-set
     * ?value- Object[] param = new Object[] { wp.item_ss("chi_name") , wp.item_ss("acct_status") ,
     * wp.item_ss("mod_user") , wp.item_ss("mod_user") , wp.item_ss("mod_pgm") , kk1 };
     * 
     * //this.ddd("kk1="+kk1); sqlExec(is_sql, param); if (sql_nrow <= 0) { errmsg(sql_errtext); }
     * 
     * return rc;
     */

    strSql = "insert into col_debt_t (" + "reference_no, " + "p_seqno, " + "acct_type, "
        + "chi_name , " + "post_date, " + "acct_month, " + "bill_type, " + "txn_code, "
        + "beg_bal, " + "end_bal, " + "d_avail_bal, " + "acct_code, " + "interest_date, "
        + "acct_status , " + "apr_date, " + "apr_user, " + "aud_code, " + "crt_date, "
        + "crt_user , " + "mod_user , " + "mod_time, " + "mod_pgm , " + "mod_seqno " + " ) values ("
        + ":reference_no, " + ":p_seqno, " + ":acct_type, " + ":chi_name , " + ":post_date, "
        + ":acct_month, " + ":bill_type, " + ":txn_code, " + ":beg_bal, " + ":end_bal, "
        + ":d_avail_bal, " + ":acct_code, " + ":interest_date, " + ":acct_status, " + "'', "
        + "'', " + "'1', " + "to_char(sysdate,'yyyymmdd'), " + ":crt_user , " + ":mod_user , "
        + "sysdate, " + ":mod_pgm, " + "0 " + ") ";
    setString("reference_no", referenceNo);
    setString("p_seqno", wp.itemStr("p_seqno"));
    setString("acct_type", wp.itemStr("acct_type"));
    setString("chi_name", wp.itemStr("chi_name"));
    setString("post_date", wp.itemStr("post_date"));
    setString("acct_month", wp.itemStr("acct_month"));
    setString("bill_type", wp.itemStr("bill_type"));
    setString("txn_code", wp.itemStr("txn_code"));
    setString("beg_bal", wp.itemStr("beg_bal"));
    setString("end_bal", wp.itemStr("end_bal"));
    setString("d_avail_bal", wp.itemStr("d_avail_bal"));
    setString("acct_code", wp.itemStr("acct_code"));
    setString("interest_date", wp.itemStr("interest_date"));
    setString("acct_status", wp.itemStr("acct_status"));
    setString("crt_user", wp.itemStr("mod_user"));
    setString("mod_user", wp.itemStr("mod_user"));
    setString("mod_pgm", wp.itemStr("mod_pgm"));
    this.log("insertFunc()=" + strSql);
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  int deleteFunc() {
    if (rc != 1)
      return rc;

    strSql = "delete col_debt_t " + sqlWhere;
    this.log("deleteFunc()=" + strSql);
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
