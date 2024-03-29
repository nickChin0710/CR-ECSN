/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0540Func extends FuncEdit {
  String mKkReissueReason = "";
  String mKkReissueCode = "";

  public Ptrm0540Func(TarokoCommon wr) {
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
      mKkReissueReason = wp.itemStr("kk_reissue_reason");
      mKkReissueCode = wp.itemStr("kk_reissue_code");
    } else {
      mKkReissueReason = wp.itemStr("reissue_reason");
      mKkReissueCode = wp.itemStr("reissue_code");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from ptr_reissue_code where reissue_reason = ? and reissue_code = ?";
      Object[] param = new Object[] {mKkReissueReason, mKkReissueCode};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where reissue_reason = ? and reissue_code = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkReissueReason, mKkReissueCode, wp.modSeqno()};
      isOtherModify("ptr_reissue_code", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_reissue_code (" + " reissue_reason, " + " reissue_code, "
        + " content, " + " crt_date, crt_user, " + " apr_date, apr_user, "
        + " mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?, ?, ?,"
        + " to_char(sysdate,'yyyymmdd'), ?," + " to_char(sysdate,'yyyymmdd'), ?," + " sysdate,?,?,1"
        + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkReissueReason // 1
        , mKkReissueCode, wp.itemStr("content"), wp.loginUser, wp.itemStr("apr_user"),
        wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update ptr_reissue_code set " + " content =?, "
        + " apr_date=to_char(sysdate,'yyyymmdd'), apr_user=?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =?, " + " mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("content"), wp.itemStr("apr_user"), wp.loginUser,
        wp.itemStr("mod_pgm"), mKkReissueReason, mKkReissueCode, wp.modSeqno()};
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
    strSql = "delete ptr_reissue_code " + sqlWhere;
    Object[] param = new Object[] {mKkReissueReason, mKkReissueCode, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
