/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-31  V1.00.00  yash       program initial                            *
* 109-04-23  V1.00.01  shiyuqi       updated for project coding standard     * 
* ******************************************************************************/

package bilp01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Bilp0010Func extends FuncEdit {
  String mKkBatchNo = "";

  public Bilp0010Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
      mKkBatchNo = wp.itemStr("kk_batch_no");
    } else {
      mKkBatchNo = wp.itemStr("batch_no");
    }
    if (this.isAdd()) {

      return;
    }

    // -other modify-
    sqlWhere = " where batch_no = ?  and nvl(mod_seqno,0) = ?";
    Object[] param = new Object[] {mKkBatchNo, wp.modSeqno()};
    isOtherModify("bil_postcntl", sqlWhere, param);

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into bil_postcntl (" + " batch_no " + ", xxx " + ", crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?, ? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkBatchNo // 1
        , wp.itemStr("xxx"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    msgOK();
    if (rc != 1) {
      return rc;
    }

    strSql = "update bil_postcntl set " + " confirm_flag_p =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + " where batch_no = ? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {"Y"// confirm_flag
        , wp.loginUser, wp.itemStr("mod_pgm"), varsStr("aa_batch_no"), varsStr("aa_mod_seqno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete bil_postcntl " + sqlWhere;
    Object[] param = new Object[] {mKkBatchNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
