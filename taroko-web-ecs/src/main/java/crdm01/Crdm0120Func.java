/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-29  V1.00.00  ryan       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/

package crdm01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdm0120Func extends FuncProc {
  String batchno;
  double recno;

  public Crdm0120Func(TarokoCommon wr) {
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

  }

  int dataCheck1() {
    batchno = varsStr("batchno");
    recno = varsNum("recno");
    String lsSql = "select count(*) as tot_cnt from crd_emboss_tmp where batchno = ?and recno=?";
    Object[] param = new Object[] {batchno, recno};
    sqlSelect(lsSql, param);
    if (colNum("tot_cnt") > 0) {
      return -1;
    }
    return 1;
  }

  @Override
  public int dataProc() {
    return rc;
  }

  public int insertFunc() {

    busi.SqlPrepare sp = new SqlPrepare();
    if (dataCheck1() <= 0) {
      return -1;
    }
    sp.sql2Insert("crd_emboss_tmp");
    sp.ppstr("batchno", varsStr("batchno"));
    sp.ppnum("recno", varsNum("recno"));
    sp.ppstr("emboss_source", "4");
    sp.ppstr("to_nccc_code", "Y");
    sp.ppstr("card_type", varsStr("card_type"));
    sp.ppstr("unit_code", varsStr("unit_code"));
    sp.ppstr("reg_bank_no", varsStr("reg_bank_no"));
    sp.ppstr("risk_bank_no", varsStr("risk_bank_no"));
    sp.ppstr("card_no", varsStr("card_no"));
    sp.ppstr("old_card_no", varsStr("old_card_no"));
    sp.ppstr("change_reason", varsStr("aa_db_change_reason"));
    sp.ppstr("status_code", "1");
    sp.ppstr("nccc_type", "2");
    sp.ppstr("reason_code", "");
    sp.ppstr("apply_id", varsStr("apply_id"));
    sp.ppstr("apply_id_code", varsStr("apply_id_code"));
    sp.ppstr("ic_flag", varsStr("ic_flag"));
    sp.ppstr("pm_id", varsStr("pm_id"));
    sp.ppstr("pm_id_code", varsStr("pm_id_code"));
    sp.ppstr("major_card_no", varsStr("major_card_no"));
    sp.ppstr("major_valid_fm", varsStr("major_valid_fm"));
    sp.ppstr("major_valid_to", varsStr("major_valid_to"));
    sp.ppstr("emboss_4th_data", varsStr("emboss_4th_data"));
    sp.ppstr("group_code", varsStr("group_code"));
    sp.ppstr("source_code", varsStr("source_code"));
    sp.ppstr("corp_no", varsStr("corp_no"));
    sp.ppstr("corp_no_code", varsStr("corp_no_code"));
    sp.ppstr("acct_type", varsStr("acct_type"));
    sp.ppstr("acct_key", varsStr("acct_key"));
    sp.ppstr("chi_name", varsStr("chi_name"));
    sp.ppstr("eng_name", varsStr("eng_name"));
    sp.ppstr("birthday", varsStr("birthday"));
    sp.ppstr("credit_lmt", varsStr("credit_lmt"));
    sp.ppstr("old_beg_date", varsStr("old_beg_date"));
    sp.ppstr("old_end_date", varsStr("old_end_date"));
    sp.ppstr("force_flag", varsStr("force_flag"));
    sp.ppstr("valid_fm", varsStr("valid_fm"));
    sp.ppstr("valid_to", varsStr("valid_to"));
    sp.ppstr("sup_flag", varsStr("sup_flag"));
    sp.ppstr("crt_user", wp.loginUser); // 20190212 by Andy
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }

  public int updateFunc1() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card");
    sp.ppstr("expire_chg_flag", "");
    sp.ppstr("expire_chg_date", "");
    sp.ppstr("change_reason", varsStr("aa_db_change_reason"));
    sp.ppstr("change_status", "1");
    sp.addsql(", change_date = to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", mod_time =sysdate", ", mod_seqno =nvl(mod_seqno,0)+1");
    sp.sql2Where(" where card_no=?", varsStr("card_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

  public int updateFunc2() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card");
    sp.ppstr("expire_chg_flag", "");
    sp.ppstr("expire_chg_date", "");
    sp.ppstr("change_reason", "");
    sp.ppstr("change_status", "");
    sp.ppstr("change_date", "");
    sp.ppstr("expire_reason", "");
    sp.addsql(", mod_time =sysdate", ", mod_seqno =nvl(mod_seqno,0)+1");
    sp.sql2Where(" where card_no=?", varsStr("card_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

  public int updateFunc3() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card");
    sp.ppstr("expire_chg_flag", "1");
    sp.ppstr("change_reason", "");
    sp.ppstr("change_status", "");
    sp.ppstr("change_date", "");
    sp.addsql(", expire_chg_date = to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", mod_time =sysdate", ", mod_seqno =nvl(mod_seqno,0)+1");
    sp.sql2Where(" where card_no=?", varsStr("card_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

  public int dbDelete() {
    Object[] param = new Object[] {varsStr("old_card_no"), varsStr("card_no")};
    strSql = "delete crd_emboss_tmp where old_card_no = ? and card_no=? and nccc_batchno ='' ";
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

  public int dbDelete2() {
    Object[] param = new Object[] {varsStr("old_card_no")};
    strSql = "delete crd_emboss_tmp where old_card_no = ? ";
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }
}
