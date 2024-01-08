/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-03-01  V1.00.00  ryan       program initial                            *
******************************************************************************/

package crdp01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdp0054Func extends FuncProc {
  public Crdp0054Func(TarokoCommon wr) {
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
    // TODO Auto-generated method stub

  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int updateFunc() {
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_emap_tmp");
    sp.ppstr("apr_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.addsql(", apr_date =to_char(sysdate,'yyyymmdd')", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time =sysdate");
    sp.sql2Where(" where recno=?", varsStr("recno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

}
