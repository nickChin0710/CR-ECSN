/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-23   V1.00.01  Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0270Func extends FuncEdit {
  String holiday = "";

  public Ptrm0270Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
      holiday = wp.itemStr("holiday");
    } else {
      holiday = varsStr("holiday");
    }

    if (empty(holiday)) {
      errmsg("例假日日期：不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where hex(rowid) = ? and nvl(mod_seqno,0) = ? " ;
    log("sql-where=" + sqlWhere);
    if (this.isOtherModify("ptr_holiday", sqlWhere, new Object[] {varsStr("rowid"), varsNum("mod_seqno")})) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into ptr_holiday (" + " holiday " // 1
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ? " + ",sysdate,?,?,1"
        + " )";
    Object[] param = new Object[] {holiday, wp.loginUser, wp.itemStr("mod_pgm")};
    this.log("kk1=" + holiday);
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
        "update ptr_holiday set " + " holiday =?, " + " mod_user =?, mod_time=sysdate, mod_pgm =? "
            + ", mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {holiday, wp.loginUser, wp.itemStr("mod_pgm"), varsStr("rowid"), varsNum("mod_seqno")};
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
    strSql = "delete ptr_holiday " + sqlWhere;
    // ddd("del-sql="+is_sql);
    rc = sqlExec(strSql, new Object[] {varsStr("rowid"), varsNum("mod_seqno")});
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
