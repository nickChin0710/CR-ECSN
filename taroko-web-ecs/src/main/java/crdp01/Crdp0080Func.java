/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-14  V1.00.00  ryan       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 112-03-27  V1.00.02  Wilson     update crd_card add cancel_expire_chg_date *
******************************************************************************/

package crdp01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdp0080Func extends FuncProc {
  //String kk1;
  //String kk2;
  //String kk3;

  public Crdp0080Func(TarokoCommon wr) {
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

  @Override
  public int dataProc() {

    return rc;
  }

  public int insertFunc() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("crd_emboss_tmp");
    sp.ppstr("batchno", varsStr("ls_batchno"));
    sp.ppnum("recno", varsNum("li_recno"));
    sp.ppstr("emboss_source", "3");
    sp.ppstr("to_nccc_code", "Y");
    sp.ppstr("card_type", varsStr("card_type"));
    sp.ppstr("unit_code", varsStr("unit_code"));
    sp.ppstr("status_code", "1");
    sp.ppstr("nccc_type", "2");
    sp.ppstr("change_reason", "1");
    sp.ppstr("card_no", varsStr("aa_card_no"));
    sp.ppstr("old_card_no", varsStr("aa_card_no"));
    sp.ppstr("reg_bank_no", varsStr("reg_bank_no"));
    sp.ppstr("risk_bank_no", varsStr("ls_risk_bank_no"));
    sp.ppstr("reason_code", "");
    sp.ppstr("apply_id", varsStr("id_no"));
    sp.ppstr("apply_id_code", varsStr("id_no_code"));
    sp.ppstr("ic_flag", varsStr("ic_flag"));
    sp.ppstr("pm_id", varsStr("pm_id"));
    sp.ppstr("pm_id_code", varsStr("pm_id_code"));
    sp.ppstr("major_card_no", varsStr("major_card_no"));
    sp.ppstr("major_valid_fm", varsStr("ls_major_valid_fm"));
    sp.ppstr("major_valid_to", varsStr("ls_major_valid_to"));
    sp.ppstr("emboss_4th_data", varsStr("emboss_4th_data"));
    sp.ppstr("combo_indicator", varsStr("ls_combo_indicator"));
    sp.ppstr("group_code", varsStr("aa_group_code"));
    sp.ppstr("source_code", varsStr("source_code"));
    sp.ppstr("corp_no", varsStr("corp_no"));
    sp.ppstr("corp_no_code", varsStr("corp_no_code"));
    sp.ppstr("acct_type", varsStr("acct_type"));
    sp.ppstr("acct_key", varsStr("acct_key"));
    sp.ppstr("chi_name", varsStr("chi_name"));
    sp.ppstr("birthday", varsStr("birthday"));
    sp.ppstr("eng_name", varsStr("eng_name"));
    sp.ppstr("valid_fm", varsStr("valid_fm"));
    sp.ppstr("valid_to", varsStr("valid_to"));
    sp.ppstr("sup_flag", varsStr("sup_flag"));
    sp.ppnum("credit_lmt", varsNum("li_act_credit_amt"));
    sp.ppstr("old_beg_date", varsStr("old_beg_date"));
    sp.ppstr("old_end_date", varsStr("old_end_date"));
    sp.ppstr("force_flag", varsStr("force_flag"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");

    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;
  }

  public int insertFunc2() {

    strSql = " insert into crd_card_tmp_h " + " select * from crd_card_tmp "
        + " where kind_type = '080' " + " and apr_date <> ''  ";

    Object[] param = new Object[] {};
    rc = sqlExec(strSql, param);

    return rc;

  }

  public int updateFunc1() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card");
    sp.ppstr("expire_chg_flag", "");
    sp.ppstr("expire_chg_date", "");
    sp.ppstr("expire_reason", "");
    sp.ppstr("change_reason", "1");
    sp.addsql(", change_date =to_char(sysdate,'yyyymmdd')", "");
    sp.ppstr("change_status", "1");
    sp.addsql(", cancel_expire_chg_date=to_char(sysdate,'yyyymmdd')", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time =sysdate");
    sp.sql2Where(" where card_no=?", varsStr("aa_card_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("aa_crd_card_mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int updateFunc2() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card");
    sp.ppstr("expire_chg_flag", varsStr("temp_expire_chg_flag"));
    sp.ppstr("expire_reason", varsStr("ls_expire_reason"));
    sp.addsql(", expire_chg_date=to_char(sysdate,'yyyymmdd')", "");
    sp.ppstr("change_status", "");
    sp.ppstr("change_date", "");
    sp.ppstr("change_reason", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time =sysdate");
    sp.sql2Where(" where card_no=?", varsStr("ls_card_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("aa_crd_card_mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int updateFunc3() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card");
    sp.ppstr("expire_chg_flag", "");
    sp.ppstr("expire_reason", "");
    sp.ppstr("expire_chg_date", "");
    sp.ppstr("change_status", "");
    sp.ppstr("change_reason", "");
    sp.addsql(", cancel_expire_chg_date=to_char(sysdate,'yyyymmdd')", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time =sysdate");
    sp.sql2Where(" where card_no=?", varsStr("ls_card_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("aa_crd_card_mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int updateFunc4() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card_tmp");
    sp.ppstr("apr_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", apr_date =to_char(sysdate,'yyyymmdd')", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time =sysdate");
    sp.sql2Where(" where card_no=?", varsStr("aa_card_no"));
    sp.sql2Where(" and kind_type=?", varsStr("aa_kind_type"));
    sp.sql2Where(" and mod_seqno=?", varsStr("aa_crd_card_tmp_mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int dbDelete(String lsCardNo) {

    Object[] param = new Object[] {lsCardNo, lsCardNo};

    strSql = "delete crd_emboss_tmp where old_card_no = ? and card_no=? and nccc_batchno ='' ";
    rc = sqlExec(strSql, param);
    return rc;
  }

  public int dbDelete2() {

    Object[] param = new Object[] {varsStr("ls_card_no")};

    strSql = "delete crd_emboss_tmp where card_no = ? and emboss_source='4' and nccc_batchno ='' ";
    rc = sqlExec(strSql, param);
    return rc;
  }

  public int dbDelete3() {

    Object[] param = new Object[] {varsStr("aa_card_no")};

    strSql = " delete crd_notchg where card_no = ? ";
    rc = sqlExec(strSql, param);
    return rc;
  }

  public int dbDelete4() {

    Object[] param = new Object[] {};

    strSql = " delete crd_card_tmp where kind_type = '080'  and apr_date <>'' ";
    rc = sqlExec(strSql, param);
    return rc = 1;
  }

}
