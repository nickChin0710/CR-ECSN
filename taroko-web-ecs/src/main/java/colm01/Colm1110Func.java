/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109-05-06  V1.00.00  Aoyulan       updated for project coding standard     *
******************************************************************************/
package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm1110Func extends FuncEdit {
  String kkOptname;
  byte[] kkk = null;

  public Colm1110Func(TarokoCommon wr) {
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
    String kkTable = "";
    kkOptname = wp.itemStr("optname");
    kkk = wp.itemRowId("rowid");

    // -other modify-
    sqlWhere = "where rowid = ? " + "and mod_seqno = ? ";
    Object[] param = new Object[] {kkk, wp.modSeqno()};
    kkTable = "aopt".equals(kkOptname) ? "col_liac_debt" : "col_liac_debt_ch";
    if (isOtherModify(kkTable, sqlWhere, param)) {
      return;
    }

    return;
  }

  @Override
  public int dbInsert() {
    // No use..
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    if ("aopt".equals(kkOptname))
      rc = updateFuncA();
    else
      rc = updateFuncB();

    return rc;
  }

  @Override
  public int dbDelete() {
    // No use..
    return rc;
  }

  int updateFuncA() {
    String lsProcFlag;
    lsProcFlag = varsStr("proc_flag");

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liac_debt");
    if (eqIgno(lsProcFlag, "2A") == false) {
      sp.ppnum("in_end_bal_new", wp.itemNum("in_end_bal_new"));
      sp.ppnum("out_end_bal_new", wp.itemNum("out_end_bal_new"));
      sp.ppnum("lastest_pay_amt_new", wp.itemNum("lastest_pay_amt_new"));
      sp.ppstr("not_send_flag", wp.itemStr("not_send_flag").equals("Y") ? "Y" : "N");
      sp.ppstr("no_calc_flag", wp.itemStr("no_calc_flag").equals("Y") ? "Y" : "N");
      sp.ppstr("no_include_flag", wp.itemStr("no_include_flag").equals("Y") ? "Y" : "N");
      sp.ppstr("paper_report_flag", wp.itemStr("paper_report_flag").equals("Y") ? "Y" : "N");
      sp.ppnum("out_capital_new", wp.itemNum("out_capital_new"));
      sp.ppnum("out_interest_new", wp.itemNum("out_interest_new"));
      sp.ppnum("out_fee_new", wp.itemNum("out_fee_new"));
      sp.ppnum("out_pn_new", wp.itemNum("out_pn_new"));
      sp.ppstr("proc_flag", "R");
      // sp.ppss("apr_flag", "N"); //??
      // sp.ppss("apr_date", "");
      // sp.ppss("apr_user", "");
    }
    sp.ppstr("debt_remark", wp.itemStr("debt_remark"));
    sp.addsql(", proc_date = to_char(sysdate,'yyyymmdd') ", "");
    sp.ppstr("apr_flag", "N");
    sp.ppstr("apr_date", "");
    sp.ppstr("apr_user", "");
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", crt_date = to_char(sysdate,'yyyymmdd') ", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where rowid = ?", kkk);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  int updateFuncB() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_liac_debt_ch");
    sp.ppstr("agree_flag", wp.itemStr("agree_flag"));
    sp.ppnum("receipt_amt", wp.itemNum("receipt_amt"));
    sp.ppnum("card_debt", wp.itemNum("card_debt"));
    sp.ppnum("cash_card_debt", wp.itemNum("cash_card_debt"));
    sp.ppstr("rela_consent_flag", wp.itemStr("rela_consent_flag"));
    sp.ppstr("remark", wp.itemStr("remark"));
    sp.ppstr("proc_flag", "0");
    sp.addsql(", proc_date = to_char(sysdate,'yyyymmdd') ", "");
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", crt_date = to_char(sysdate,'yyyymmdd') ", "");
    sp.ppstr("apr_flag", "N");
    sp.ppstr("apr_date", "");
    sp.ppstr("apr_user", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where rowid = ?", kkk);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

}
