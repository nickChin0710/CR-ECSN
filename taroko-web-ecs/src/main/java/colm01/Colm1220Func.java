/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-22  V1.00.00  ryan       program initial                            *
* 109-05-06  V1.00.01  Aoyulan       updated for project coding standard     *
******************************************************************************/

package colm01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm1220Func extends FuncProc {

  public Colm1220Func(TarokoCommon wr) {
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

    // -other modify-
    sqlWhere = " where hex(rowid)=?  and mod_seqno = ?";

    Object[] param = new Object[] {varsStr("aa_rowid"), varsStr("aa_mod_seqno")};
    if (isOtherModify("col_lgd_jrnl", sqlWhere, param)) {
      errmsg("請重新查詢");
      return;
    }

  }

  @Override
  public int dataProc() {
    return rc;
  }

  public int insertFunc() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_lgd_jrnl");
    sp.ppstr("p_seqno", varsStr("aa_p_seqno"));
    sp.ppstr("jrnl_crt_date ", varsStr("aa_crt_date"));
    sp.ppstr("jrnl_crt_time", varsStr("aa_crt_time"));
    sp.ppstr("jrnl_seqno", varsStr("aa_jrnl_seqno"));
    sp.ppnum("enq_seqno", varsNum("aa_enq_seqno"));
    sp.ppstr("acct_type", varsStr("aa_acct_type"));
    sp.ppstr("id_corp_type", varsStr("aa_id_corp_type"));
    sp.ppstr("id_corp_p_seqno", varsStr("aa_id_corp_p_seqno"));
    sp.ppstr("id_corp_no", varsStr("aa_id_corp_no"));
    sp.ppstr("acct_date", varsStr("aa_acct_date"));
    sp.ppstr("tran_class", varsStr("aa_tran_class"));
    sp.ppstr("tran_type", varsStr("aa_tran_type"));
    sp.ppstr("interest_date", varsStr("aa_interest_date"));
    sp.ppnum("trans_amt", varsNum("aa_transaction_amt"));
    sp.ppstr("curr_code", varsStr("aa_curr_code"));
    sp.ppnum("dc_trans_amt", varsNum("aa_dc_transaction_amt"));
    sp.ppstr("lgd_coll_flag", varsStr("aa_db_coll_flag"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", mod_time ", ", sysdate ");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int updateFunc() {

    dataCheck();
    if (rc != 1) {
      return -1;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_lgd_jrnl");
    sp.ppstr("lgd_coll_flag", varsStr("aa_db_coll_flag"));
    sp.ppstr("apr_date", "");
    sp.ppstr("apr_user", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time =sysdate", ", mod_seqno =nvl(mod_seqno,0)+1");
    sp.sql2Where(" where hex(rowid)=?", varsStr("aa_rowid"));
    sp.sql2Where(" and mod_seqno=?", varsStr("aa_mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int deleteFunc() {

    dataCheck();
    if (rc != 1) {
      return -1;
    }
    Object[] param = new Object[] {varsStr("aa_rowid"), varsStr("aa_mod_seqno")};
    strSql = "delete col_lgd_jrnl where hex(rowid) = ? and mod_seqno = ?";
    rc = sqlExec(strSql, param);
    return rc;

  }

}
