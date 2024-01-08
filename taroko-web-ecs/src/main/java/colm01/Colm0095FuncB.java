/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Aoyulan       updated for project coding standard   *
******************************************************************************/
package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colm0095FuncB extends FuncEdit {
  String seqno;
  String transDate;
  String transType;

  public Colm0095FuncB(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    return 0;
  }

  @Override
  public int dataSelect() {
    return 0;
  }

  @Override
  public void dataCheck() {
    seqno = wp.itemStr("p_seqno");
    transDate = wp.itemStr("trans_date");
    transType = wp.itemStr("trans_type");

    // -other modify-
    sqlWhere =
        "where p_seqno = ? " + "and trans_date = ? " + "and trans_type = ? " + "and mod_seqno = ? ";
    Object[] param = new Object[] {seqno, transDate, transType, wp.modSeqno()};
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
    if (rc != 1) {
      return rc;
    }

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("col_bad_debt");
    sp.ppstr("paper_name", wp.itemStr("paper_name")); // 憑証名稱
    sp.ppstr("description", wp.itemStr("description")); // 備註
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where p_seqno=?", seqno);
    sp.sql2Where(" and trans_date=?", transDate); // 原始轉催收日期
    sp.sql2Where(" and trans_type=?", transType); // trans_type
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
