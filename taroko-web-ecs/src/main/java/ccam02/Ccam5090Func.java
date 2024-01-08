
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5090Func extends FuncEdit {
  String respCode = "";


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
      respCode = wp.itemStr("kk_resp_code");
    } else {
      respCode = wp.itemStr("resp_code");
    }
    if (empty(respCode)) {
      errmsg("回覆碼: 不可空白");
      return;
    }

    if (empty(wp.itemStr("resp_remark"))) {
      errmsg("說明: 不可空白");
      return;
    }
    // if (wp.item_empty("nccc_p38")) {
    // errmsg("P-38 回覆碼: 不可空白");
    //
    // }
    /*
     * if (wp.item_empty("nccc_p39")) { errmsg("P-39回覆碼: 不可空白"); return; }
     */
    if (this.isAdd()) {
      return;
    }

    sqlWhere = " where resp_code=? and nvl(mod_seqno,0) = ?";
    Object[] parms = new Object[] {respCode, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("CCA_RESP_CODE", sqlWhere,parms)) {
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

    strSql = "insert into CCA_resp_code (" + " resp_code, " // 1
        + " resp_status, " + " resp_remark, " + " nccc_p38, " + " nccc_p39, " + " visa_p39, "
        + " mast_p39, " + " jcb_p39, " + " abnorm_flag, " // 9
        + " crt_date, crt_user, " + " apr_date, apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,?,?,? " // 9
        + ",to_char(sysdate,'yyyymmdd'),? " + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1"
        + " )";
    Object[] param = new Object[] {respCode, wp.itemStr("resp_status"), wp.itemStr("resp_remark"),
        wp.itemStr("nccc_p38"), wp.itemStr("nccc_p39"), wp.itemStr("visa_p39"),
        wp.itemStr("mast_p39"), wp.itemStr("jcb_p39"), wp.itemStr("abnorm_flag"), wp.loginUser,
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm") // 9
    };

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
        "update CCA_resp_code set " + " resp_status =?, " + " resp_remark =?, " + " nccc_p38 =?, "
            + " nccc_p39 =?, " + " visa_p39 =?, " + " mast_p39 =?, " + " jcb_p39 =?, "
            + " abnorm_flag =?, " + " apr_date =to_char(sysdate,'yyyymmdd'), apr_user =?, "
            + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
            + " where resp_code=? and nvl(mod_seqno,0) = ?";
    Object[] param = new Object[] {wp.itemStr("resp_status"), wp.itemStr("resp_remark"),
        wp.itemStr("nccc_p38"), wp.itemStr("nccc_p39"), wp.itemStr("visa_p39"),
        wp.itemStr("mast_p39"), wp.itemStr("jcb_p39"), wp.itemStr("abnorm_flag"), wp.loginUser,
        wp.loginUser, wp.itemStr("mod_pgm"),respCode, wp.itemNum("mod_seqno")};
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
    strSql = "delete CCA_RESP_CODE where resp_code=? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {respCode, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql,param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
