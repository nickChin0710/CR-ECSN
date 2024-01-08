/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-07  V1.00.00  ryan       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package crdm01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdm0080Func extends FuncProc {
  //String kk1;
  //double kk2;

  public Crdm0080Func(TarokoCommon wr) {
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
    sp.sql2Insert("crd_card_tmp");
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("card_no", varsStr("ls_card_no"));
    sp.ppstr("kind_type", "080");
    sp.ppnum("mod_seqno", 1);
    sp.ppstr("id_p_seqno", varsStr("ls_id_p_seqno"));
    sp.ppstr("corp_no", varsStr("ls_corp_no"));
    sp.ppstr("process_kind", varsStr("ls_process_kind"));
    sp.ppstr("expire_reason", varsStr("ls_expire_reason"));
    sp.ppstr("expire_chg_flag", varsStr("ls_expire_chg_flag"));
    sp.ppstr("expire_reason_old", varsStr("ls_expire_reason_old"));
    sp.ppstr("expire_chg_flag_old", varsStr("ls_expire_chg_flag_old"));
    sp.ppstr("expire_chg_date", varsStr("ls_expire_chg_date"));
    sp.ppstr("expire_chg_date_old", varsStr("ls_expire_chg_date_old"));
    sp.ppstr("cur_end_date", varsStr("ls_cur_end_date"));
    sp.ppstr("old_end_date", varsStr("ls_old_end_date"));
    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }


  public int dbDelete() {
    Object[] param = new Object[] {varsStr("ls_card_no")};
    strSql = "delete crd_notchg where card_no = ? ";
    rc = sqlExec(strSql, param);
    return rc;
  }

  public int dbDelete2() {
    Object[] param = new Object[] {varsStr("ls_card_no")};
    strSql = "delete crd_card_tmp where card_no = ? and kind_type = '080' ";
    rc = sqlExec(strSql, param);
    return rc;
  }

}
