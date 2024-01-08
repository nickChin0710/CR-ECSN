/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-03  V1.00.01  Alex       dataCheck Fixed                            *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/

package ptrm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0330Func extends FuncEdit {
  String mediaName = "";

  public Ptrm0330Func(TarokoCommon wr) {
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
      mediaName = wp.itemStr("kk_media_name");
    } else {
      mediaName = wp.itemStr("media_name");
    }

    if (empty(mediaName)) {
      errmsg("媒體代碼: 不可空白");
      return;
    }

    if (empty("wp.item_ss(from_path)")) {
      errmsg("來源目錄：不可空白");
      return;
    }
    if (empty("wp.item_ss(to_path)")) {
      errmsg("存放目錄：不可空白");
      return;
    }

    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where media_name= ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {mediaName, wp.itemNum("mod_seqno")};    
    if (this.isOtherModify("ptr_media", sqlWhere,parms)) {
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

    strSql = "insert into ptr_media (" + " media_name, " // 1
        + " from_path, " + " to_path, " + " external_name, " + " dest_cname, " + " media_type, "
        + " trans_type, " + " in_out, " + " cycle_time "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,?,?,? "
        + ",sysdate,?,?,1" + " )";
    Object[] param = new Object[] {mediaName, wp.itemStr("from_path"), wp.itemStr("to_path"),
        wp.itemStr("external_name"), wp.itemStr("dest_cname"), wp.itemStr("media_type"),
        wp.itemStr("trans_type"), wp.itemStr("in_out"), wp.itemStr("cycle_time"), wp.loginUser,
        wp.itemStr("mod_pgm")};
    this.log("kk1=" + mediaName);
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
    strSql = "update ptr_media set " + " from_path =?, " + " to_path =?, " + " external_name =?, "
        + " dest_cname =?, " + " media_type =?, " + " trans_type =?, " + " in_out =?, "
        + " cycle_time =?, " + " mod_user =?, mod_time=sysdate, mod_pgm =? "
        + ", mod_seqno =nvl(mod_seqno,0)+1 where media_name= ? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {wp.itemStr("from_path"), wp.itemStr("to_path"),
        wp.itemStr("external_name"), wp.itemStr("dest_cname"), wp.itemStr("media_type"),
        wp.itemStr("trans_type"), wp.itemStr("in_out"), wp.itemStr("cycle_time"),
        wp.itemStr("mod_user"), wp.itemStr("mod_pgm"),mediaName, wp.itemNum("mod_seqno")};
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
    strSql = "delete ptr_media where media_name= ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {mediaName, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql,parms);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}

