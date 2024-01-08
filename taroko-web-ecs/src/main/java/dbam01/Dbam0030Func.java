/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-08-31  V1.00.00  phopho     program initial                            *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/

package dbam01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Dbam0030Func extends FuncEdit {
  String rowid = "";
  String table = "";
  byte[] rowidBite = null;

  public Dbam0030Func(TarokoCommon wr) {
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
    rowidBite = wp.itemRowId("rowid");

    // -other modify-
    sqlWhere = "where rowid = ? " + "and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {rowidBite, wp.modSeqno()};
    isOtherModify("dba_acaj", sqlWhere, param);
  }

  @Override
  public int dbInsert() {
    actionInit("A");

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("dba_acaj");
    sp.ppnum("dr_amt", wp.itemNum("dr_amt"));
    sp.ppnum("aft_amt", wp.itemNum("aft_amt"));
    sp.ppnum("aft_d_amt", wp.itemNum("aft_d_amt"));

    sp.ppstr("p_seqno", wp.itemStr("p_seqno"));
    sp.ppstr("acct_type", wp.itemStr("acct_type"));
    sp.ppstr("acct_no", wp.itemStr("acct_no"));
    sp.ppstr("adjust_type", varsStr("adjust_type"));
    sp.ppstr("reference_no", wp.itemStr("reference_no"));
    sp.ppstr("post_date", wp.itemStr("post_date"));
    sp.ppnum("orginal_amt", wp.itemNum("orginal_amt"));
    sp.ppnum("cr_amt", wp.itemNum("cr_amt"));
    sp.ppnum("bef_amt", wp.itemNum("bef_amt"));
    sp.ppnum("bef_d_amt", wp.itemNum("bef_d_amt"));
    sp.ppstr("acct_code", wp.itemStr("acct_code"));
    sp.ppstr("func_code", wp.itemStr("func_code"));
    sp.ppstr("card_no", wp.itemStr("card_no"));
    sp.ppstr("item_post_date", wp.itemStr("item_post_date"));
    sp.ppstr("purchase_date", wp.itemStr("purchase_date"));
    sp.ppstr("value_type", varsStr("value_type"));
    sp.ppstr("adj_comment", wp.itemStr("adj_comment"));
    sp.ppstr("c_debt_key", wp.itemStr("c_debt_key"));
    sp.ppstr("debit_item", wp.itemStr("debit_item"));
    sp.ppstr("proc_flag", "N");
    sp.ppstr("apr_flag", "N");
    sp.ppstr("job_code", varsStr("job_code"));
    sp.ppstr("vouch_job_code", varsStr("vouch_job_code"));
    sp.ppstr("txn_code", wp.itemStr("txn_code"));
    sp.ppstr("chg_date", wp.sysDate);
    sp.ppstr("chg_user", wp.loginUser);
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_seqno", "1");

    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    // kk_rowid = wp.item_ss("rowid");
    table = wp.itemStr("db_table");

    if (eqIgno(table, "debt")) {
      rc = dbInsert();
    } else {
      actionInit("U");
      dataCheck();
      if (rc != 1)
        return rc;

      busi.SqlPrepare sp = new SqlPrepare();
      sp.sql2Update("dba_acaj");
      sp.ppnum("dr_amt", wp.itemNum("dr_amt"));
      sp.ppnum("aft_amt", wp.itemNum("aft_amt"));
      sp.ppnum("aft_d_amt", wp.itemNum("aft_d_amt"));
      sp.ppstr("adj_comment", wp.itemStr("adj_comment"));
      sp.ppstr("c_debt_key", wp.itemStr("c_debt_key"));
      sp.ppstr("debit_item", wp.itemStr("debit_item"));
      sp.ppstr("job_code", varsStr("job_code"));
      sp.ppstr("vouch_job_code", varsStr("vouch_job_code"));
      sp.ppstr("chg_date", wp.sysDate);
      sp.ppstr("chg_user", wp.loginUser);
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.addsql(", mod_time = sysdate ", "");
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
      sp.sql2Where(" where rowid = ?", rowidBite);
      sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
      rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum == 0) {
        rc = -1;
      }
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete dba_acaj " + sqlWhere;
    Object[] param = new Object[] {rowidBite, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
