/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-20  V1.00.00  yash       program initial                            *
* 106-09-20  V1.00.00  Andy       program update                             *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package crdm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0007Func extends FuncEdit {
  String mKkMsgType = "";
  String mKkMsgValue = "";

  public Crdm0007Func(TarokoCommon wr) {
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
      mKkMsgType = wp.itemStr("kk_msg_type");
    } else {
      mKkMsgType = wp.itemStr("msg_type");
    }
    if (this.ibAdd) {
      mKkMsgValue = wp.itemStr("kk_msg_value");
    } else {
      mKkMsgValue = wp.itemStr("msg_value");
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from crd_message where msg_type = ? and msg_value =?";
      Object[] param = new Object[] {mKkMsgType, mKkMsgValue};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where msg_type = ? and msg_value =? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkMsgType, mKkMsgValue, wp.modSeqno()};
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
    // 取最大序號
    String lsSql = "", kkMsgId = "";
    lsSql = "select max(msg_id) as msg_id from crd_message where msg_id != 'EMPLOYEE'  ";
    sqlSelect(lsSql);
    kkMsgId = numToStr(colNum("msg_id") + 1, "00000000");

    strSql = "insert into crd_message (" + " msg_id " + ", msg_type " + ", msg_value "
        + ", map_value " + ", msg " + ", crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?, ?,?, ? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {kkMsgId, mKkMsgType // 1
        , mKkMsgValue, wp.itemStr("map_value"), wp.itemStr("msg"), wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm")};
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

    strSql = "update crd_message set " + " map_value =? " + " , msg =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;

    Object[] param = new Object[] {wp.itemStr("map_value"), wp.itemStr("msg"), wp.loginUser,
        wp.itemStr("mod_pgm"), mKkMsgType, mKkMsgValue, wp.modSeqno()};

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
    Object[] param = new Object[] {mKkMsgType, mKkMsgValue, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
