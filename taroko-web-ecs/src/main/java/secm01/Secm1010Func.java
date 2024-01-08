/* 佈告欄維護 V.2018-0808
 * 2018-0808:	JH		bugfix
 109-04-20    shiyuqi       updated for project coding standard  
 * 
 * */
package secm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Secm1010Func extends FuncEdit {
  String buitDtime = "";

  public Secm1010Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
    modUser = wp.loginUser;
    modPgm = wp.modPgm();
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
    if (isUpdate() || isDelete()) {
      buitDtime = wp.itemStr("bult_dtime");
      if (empty(buitDtime)) {
        errmsg("建檔日期：不可空白");
        return;
      }
    }
    if (empty("bult_subject")) {
      errmsg("留言主題：不可空白");
      return;
    }
    if (empty("bult_mesg")) {
      errmsg("留言內容：不可空白");
      return;
    }
    if (empty("eff_date1")) {
      errmsg("有效期間：不可空白");
      return;
    }
    if (empty("eff_date2")) {
      errmsg("有效期間：不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }

    sqlWhere = " where 1=1 and bult_dtime = ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {buitDtime, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("SEC_BULLETIN", sqlWhere,parms)) {
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

    strSql = "insert into SEC_BULLETIN (" + " bult_subject, " + " bult_mesg, " + " eff_date1, "
        + " eff_date2, " + " bult_dtime, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user " + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?"
        + ",TO_CHAR(sysdate,'yymmddhh24miss')" + ",to_char(sysdate,'yyyymmdd'),? "
        + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1" + " )";
    Object[] param = new Object[] {wp.itemStr("bult_subject"), wp.itemStr("bult_mesg"),
        wp.itemStr("eff_date1"), wp.itemStr("eff_date2"), wp.loginUser, wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm")};
    // this.ddd("kk1="+kk1);
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
        "update SEC_BULLETIN set " + " bult_subject =?, " + " bult_mesg =?, " + " eff_date1 =?, "
            + " eff_date2 =?, " + " apr_user =?, " + " apr_date = to_char(sysdate,'yyyymmdd'), "
            + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
            + " where 1=1 and bult_dtime = ? and nvl(mod_seqno,0) = ?  ";
    Object[] param = new Object[] {wp.itemStr("bult_subject"), wp.itemStr("bult_mesg"),
        wp.itemStr("eff_date1"), wp.itemStr("eff_date2"), wp.loginUser,
        // wp.item_ss("apr_date"),
        wp.loginUser, wp.itemStr("mod_pgm"),buitDtime, wp.itemNum("mod_seqno")};
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
    strSql = "delete sec_BULLETIN where 1=1 and bult_dtime = ? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {buitDtime, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql,param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}
