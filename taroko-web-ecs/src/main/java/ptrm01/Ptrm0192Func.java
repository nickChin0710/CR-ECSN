/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-23  V1.00.01   Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0192Func extends FuncEdit {
  String groupCode = "";

  public Ptrm0192Func(TarokoCommon wr) {
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
    groupCode = varsStr("group_code");

    if (varsNum("bill_msg_seq") < 0) {
      errmsg("對帳單廣告１列印順序：不可小於 0 ");
      return;
    }

    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where  group_code= ? and nvl(mod_seqno,0) = ? ";
    log("sql-where=" + sqlWhere);
    if (this.isOtherModify("ptr_group_code", sqlWhere, new Object[] {groupCode, varsStr("mod_seqno")})) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "update ptr_group_code set " + " BILL_MSG_SEQ =?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {varsStr("BILL_MSG_SEQ"), wp.loginUser, wp.itemStr("mod_pgm"), groupCode, varsStr("mod_seqno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

}
