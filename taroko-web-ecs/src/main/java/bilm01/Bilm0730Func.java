/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-19  V1.00.00             program initial                            *
* 109-04-24  V1.00.01  shiyuqi       updated for project coding standard     *                                                                              *
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Bilm0730Func extends FuncEdit {
  String mKkTxDate = "";

  public Bilm0730Func(TarokoCommon wr) {
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
      mKkTxDate = wp.itemStr("kk_tx_date");
    } else {
      mKkTxDate = wp.itemStr("tx_date");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from bil_auto_tx where tx_date = ?";
      Object[] param = new Object[] {mKkTxDate};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where tx_date = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkTxDate, wp.modSeqno()};
      isOtherModify("bil_auto_tx", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into bil_auto_tx (" + " tx_date " + ", xxx " + ", crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?, ? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkTxDate // 1
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
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql =
        "update bil_auto_tx set " + " xxx =? " + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
            + " , mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("xxx"), wp.loginUser, wp.itemStr("mod_pgm"),
        mKkTxDate, wp.modSeqno()};
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
    strSql = "delete bil_auto_tx " + sqlWhere;
    Object[] param = new Object[] {mKkTxDate, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
