/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-11  V1.00.00  ryan       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package crdp01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdp0010Func extends FuncProc {
 // String kk1;
  //String kk2;
  //String kk3;

  public Crdp0010Func(TarokoCommon wr) {
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
    sqlWhere = " where wh_year = ? and card_item=? and place=? and nvl(mod_seqno,0) = ?";
    Object[] param = new Object[] {varsStr("ls_nextYY"), varsStr("ls_key2"), varsStr("ls_key3"),
        varsStr("mod_seqno")};
    if (isOtherModify("crd_warehouse", sqlWhere, param)) {

      return;
    }

  }

  @Override
  public int dataProc() {

    rc = insertFunc();

    return rc;
  }

  public int insertFunc() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("crd_warehouse");
    sp.ppstr("wh_year", varsStr("ls_nextYY"));
    sp.ppstr("card_item", varsStr("ls_key2"));
    sp.ppstr("place", varsStr("ls_key3"));
    sp.ppnum("pre_total", varsNum("ld_qty"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sp.addsql(", mod_time ", ", sysdate ");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (rc <= 0) {
    }
    return rc;
  }

  public int updateFunc() {
    dataCheck();
    if (rc != 1)
      return rc;
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_warehouse");
    sp.ppstr("wh_year", varsStr("ls_nextYY"));
    sp.ppstr("card_item", varsStr("ls_key2"));
    sp.ppstr("place", varsStr("ls_key3"));
    sp.ppnum("pre_total", varsNum("ld_qty"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time =sysdate", ", mod_seqno =nvl(mod_seqno,0)+1");
    sp.sql2Where(" where wh_year=?", varsStr("ls_nextYY"));
    sp.sql2Where(" and card_item=?", varsStr("ls_key2"));
    sp.sql2Where(" and place=?", varsStr("ls_key3"));
    sp.sql2Where(" and mod_seqno=?", varsStr("mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

}
