/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-29  V1.00.00  Andy       program initial                            *
* 109-04-27  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package mktm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Mktm0720Func extends FuncEdit {
  String mKkMchtNo = "";
  String mKkMchtGroupId = "";
  String mKkMchtGroupIdNew = "";

  public Mktm0720Func(TarokoCommon wr) {
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
    mKkMchtNo = varsStr("aa_mcht_no");
    mKkMchtGroupId = varsStr("aa_mcht_group_id");
    mKkMchtGroupIdNew = varsStr("aa_mcht_group_id_new");
    // if (this.isAdd())
    // {
    // }
    // else
    // {
    // //-other modify-
    // sql_where = " where hex(rowid) = ? and nvl(mod_seqno,0) = ?";
    // Object[] param = new Object[] { m_kk_rowid,m_kk_mod_seqno };
    // other_modify("bil_othexp", sql_where, param);
    // }
  }

  @Override
  public int dbInsert() {
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_mcht_group_t");
    sp.ppstr("mcht_no", mKkMchtNo);
    sp.ppstr("mcht_group_id", mKkMchtGroupId);
    sp.ppstr("mcht_group_id_new", mKkMchtGroupIdNew);
    sp.ppstr("crt_date", getSysDate());
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppstr("mod_seqno", "1");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return -1;
    }
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
    sp.sql2Update("bil_mcht_group_t");
    sp.ppstr("mcht_group_id", mKkMchtGroupIdNew);
    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where mcht_no=?", mKkMchtNo);
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    return rc;
  }

}
