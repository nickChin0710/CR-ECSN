/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-20  V1.00.00  ryan       program initial                            *
*                                                                            *
******************************************************************************/

package crdp02;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdp2080Func extends FuncProc {
  //String kk1;
  //String kk2;
  //String kk3;

  public Crdp2080Func(TarokoCommon wr) {
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
    insertFunc();
    return rc;
  }

  public int insertFunc() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("crd_emboss_pp_tmp");
    sp.ppnum("mod_seqno", 1);
    sp.ppstr("batchno", varsStr("batchno"));
    sp.ppstr("recno", varsStr("recno"));
    sp.ppstr("emboss_source", varsStr("emboss_source"));
    sp.ppstr("card_type", varsStr("card_type"));
    sp.ppstr("unit_code", varsStr("unit_code"));
    sp.ppstr("card_item", varsStr("card_item"));
    sp.ppstr("change_reason", varsStr("change_reason"));
    sp.ppstr("pp_card_no", varsStr("pp_card_no"));
    sp.ppstr("old_card_no", varsStr("old_card_no"));
    sp.ppstr("id_no", varsStr("id_no"));
    sp.ppstr("id_no_code", varsStr("id_no_code"));
    sp.ppstr("group_code", varsStr("group_code"));
    sp.ppstr("source_code", varsStr("source_code"));
    sp.ppstr("eng_name", varsStr("eng_name"));
    sp.ppstr("valid_fm", varsStr("valid_fm"));
    sp.ppstr("valid_to", varsStr("valid_to"));
    sp.ppstr("old_beg_date", varsStr("old_beg_date"));
    sp.ppstr("old_end_date", varsStr("old_end_date"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate ");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int insertFunc2() {

    strSql =
        " insert into crd_card_pp_tmp_h  select * from crd_card_pp_tmp  where kind_type = '080' "
            + " and apr_date <> '' ";

    Object[] param = new Object[] {};
    rc = sqlExec(strSql, param);

    return rc;
  }

  public int updateFunc() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card_pp");
    sp.ppstr("expire_chg_flag", "");
    sp.ppstr("expire_chg_date", "");
    sp.ppstr("expire_reason", "");
    sp.ppstr("change_reason", "1");
    sp.ppstr("change_status", "1");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", change_date =to_char(sysdate,'yyyymmdd')", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
    sp.sql2Where(" where pp_card_no=?", varsStr("pp_card_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int updateFunc2() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card_pp");
    sp.ppstr("expire_chg_flag", varsStr("expire_chg_flag"));
    sp.ppstr("expire_reason", varsStr("expire_reason"));
    sp.ppstr("change_reason", "");
    sp.ppstr("change_date", "");
    sp.ppstr("change_status", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", expire_chg_date =to_char(sysdate,'yyyymmdd')", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
    sp.sql2Where(" where pp_card_no=?", varsStr("pp_card_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int updateFunc3() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card_pp");
    sp.ppstr("expire_chg_flag", "");
    sp.ppstr("expire_reason", "");
    sp.ppstr("change_reason", "");
    sp.ppstr("expire_chg_date", "");
    sp.ppstr("change_status", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
    sp.sql2Where(" where pp_card_no=?", varsStr("pp_card_no"));
    sp.sql2Where(" and mod_seqno=?", varsStr("mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  public int updateFunc4() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_card_pp_tmp");
    sp.ppstr("apr_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", apr_date =to_char(sysdate,'yyyymmdd')", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
    sp.sql2Where(" where pp_card_no=?", varsStr("pp_card_no"));
    sp.sql2Where(" and kind_type=?", varsStr("kind_type"));
    sp.sql2Where(" and mod_seqno=?", varsStr("tmp_mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

  public int dbDelete() {

    Object[] param = new Object[] {varsStr("pp_card_no"), varsStr("pp_card_no")};

    strSql = " delete crd_emboss_pp_tmp  where 1=1 and old_pp_card_no = ? "
        + " and  pp_card_no = ? and nccc_batchno = ''  ";
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

  public int dbDelete2() {

    Object[] param = new Object[] {varsStr("pp_card_no")};

    strSql = " delete crd_emboss_pp_tmp  where 1=1 and pp_card_no = ? "
        + " and  emboss_source = '4' and nccc_batchno = ''  ";
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

  public int dbDelete3() {

    Object[] param = new Object[] {varsStr("pp_card_no")};

    strSql = " delete crd_notchg  where 1=1 and pp_card_no = ? ";
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

  public int dbDelete4() {

    Object[] param = new Object[] {};

    strSql = " delete crd_card_pp_tmp  where 1=1 and kind_type = '080' and apr_date <> ''  ";
    rc = sqlExec(strSql, param);

    return rc;
  }
}
