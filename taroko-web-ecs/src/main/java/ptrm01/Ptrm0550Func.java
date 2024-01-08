/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
* 109-07-31  V1.00.02  yanghan       修改页面覆核栏位     *
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0550Func extends FuncEdit {
  String mKkMsgValue = "";

  public Ptrm0550Func(TarokoCommon wr) {
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
      mKkMsgValue = wp.itemStr("kk_msg_value");
    } else {
      mKkMsgValue = wp.itemStr("msg_value");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from crd_message where msg_type = 'BUS_CODE' and msg_value = ?";
      Object[] param = new Object[] {mKkMsgValue};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where msg_type = 'BUS_CODE' and msg_value = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkMsgValue, wp.modSeqno()};
      isOtherModify("crd_message", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into crd_message (" + " msg_type, " + " msg_value, " + " map_value, "
        + " msg, " + " crt_date, crt_user, " + " apr_date, apr_user, "
        + " mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " 'BUS_CODE'," + " ?, ?, ?,"
        + " to_char(sysdate,'yyyymmdd'), ?," + " to_char(sysdate,'yyyymmdd'), ?," + " sysdate,?,?,1"
        + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkMsgValue // 1
        , wp.itemStr("map_value"), wp.itemStr("msg"), wp.loginUser, wp.itemStr("approval_user"),
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

    strSql = "update crd_message set " + " map_value =?, " + " msg =?, "
        + " apr_date=to_char(sysdate,'yyyymmdd'), apr_user=?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =?, " + " mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("map_value"), wp.itemStr("msg"),
        wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"), mKkMsgValue, wp.modSeqno()};
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
    strSql = "delete crd_message " + sqlWhere;
    Object[] param = new Object[] {mKkMsgValue, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
