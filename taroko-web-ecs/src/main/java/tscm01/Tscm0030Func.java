/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-15  V1.00.00  yash       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 111-04-14  V1.00.02  machao     TSC畫面整合
******************************************************************************/

package tscm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Tscm0030Func extends FuncEdit {
  String mKkEmbossKind = "";

  public Tscm0030Func(TarokoCommon wr) {
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
      mKkEmbossKind = wp.itemStr("kk_emboss_kind");
    } else {
      mKkEmbossKind = wp.itemStr("emboss_kind");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from tsc_fee_parm where emboss_kind = ?";
      Object[] param = new Object[] {mKkEmbossKind};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where emboss_kind = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkEmbossKind, wp.modSeqno()};
      isOtherModify("tsc_fee_parm", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into tsc_fee_parm (" + " emboss_kind " + ", charge_fee " + ", expire_fee_flag "
        + ", days_to_tsc " + ", month_times " + ", use_times " + ", month_money " + ", use_money "
        + ", apr_user " + ", apr_date " + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?,?,?,?,?,?,?,?,?,to_char(sysdate,'yyyymmdd') " + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkEmbossKind // 1
        , wp.itemStr("charge_fee"), wp.itemStr("expire_fee_flag"), wp.itemStr("days_to_tsc"),
        wp.itemStr("month_times"), wp.itemStr("use_times"), wp.itemStr("month_money"),
        wp.itemStr("use_money"), wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update tsc_fee_parm set " + " charge_fee =? " + ", expire_fee_flag =? "
        + ", days_to_tsc =? " + ", month_times =? " + ", use_times =? " + ", month_money =? "
        + ", use_money =? " + ", apr_user =? " + ", apr_date = to_char(sysdate,'yyyymmdd') "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("charge_fee"), wp.itemStr("expire_fee_flag"),
        wp.itemStr("days_to_tsc"), wp.itemStr("month_times"), wp.itemStr("use_times"),
        wp.itemStr("month_money"), wp.itemStr("use_money"), wp.itemStr("approval_user"), wp.loginUser,
        wp.itemStr("mod_pgm"), mKkEmbossKind, wp.modSeqno()};
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
    strSql = "delete tsc_fee_parm " + sqlWhere;
    Object[] param = new Object[] {mKkEmbossKind, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
