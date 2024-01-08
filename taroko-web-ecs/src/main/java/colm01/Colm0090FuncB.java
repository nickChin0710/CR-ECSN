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

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm0090FuncB extends FuncEdit {
  String pSeqno;
  String transDate;
  String transType;

  public Colm0090FuncB(TarokoCommon wr) {
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
    return 0;
  }

  @Override
  public void dataCheck() {
    pSeqno = wp.itemStr("p_seqno");
    transDate = wp.itemStr("trans_date");
    transType = wp.itemStr("trans_type");

    // -other modify-
    sqlWhere =
        "where p_seqno = ? " + "and trans_date = ? " + "and trans_type = ? " + "and mod_seqno = ? ";
    Object[] param = new Object[] {pSeqno, transDate, transType, wp.modSeqno()};
    if (isOtherModify("col_bad_debt", sqlWhere, param)) {
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

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_bad_debt");
    sp.ppstr("paper_name", wp.itemStr("paper_name"));
    sp.ppstr("description", wp.itemStr("description"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where p_seqno=?", pSeqno);
    sp.sql2Where(" and trans_date=?", transDate);
    sp.sql2Where(" and trans_type=?", transType);
    sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum == 0) {
      rc = -1;
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    // No use..
    return rc;
  }

}
