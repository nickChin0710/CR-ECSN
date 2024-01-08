/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-05-17  V1.00.00  yash       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package crdm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0025Func extends FuncEdit {
  String mKkCardNo = "";

  public Crdm0025Func(TarokoCommon wr) {
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
      mKkCardNo = wp.itemStr("kk_card_no");
    } else {
      mKkCardNo = wp.itemStr("card_no");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from crd_prohibit where card_no = ?";
      Object[] param = new Object[] {mKkCardNo};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where card_no = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkCardNo, wp.modSeqno()};
      isOtherModify("crd_prohibit", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into crd_prohibit (" + " card_no " + ", prohibit_remark "
        + ", crt_date, crt_user " + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?, ? " + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkCardNo // 1
        , wp.itemStr("prohibit_remark"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update crd_prohibit set " + " prohibit_remark =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("prohibit_remark"), wp.loginUser,
        wp.itemStr("mod_pgm"), mKkCardNo, wp.modSeqno()};
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
    strSql = "delete crd_prohibit " + sqlWhere;
    Object[] param = new Object[] {mKkCardNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
