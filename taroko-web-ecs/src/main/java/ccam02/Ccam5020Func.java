
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.02  yanghan  修改了變量名稱和方法名稱							 *
* 109-06-01  V1.00.03  Alex     remove nccc_ftp_code 						 *
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5020Func extends FuncEdit {
  String mccCode = "";

  public Ccam5020Func(TarokoCommon wr) {
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
      mccCode = wp.itemStr("kk_mcc_code");
    } else {
      mccCode = wp.itemStr("mcc_code");
    }

    if (isEmpty(mccCode)) {
      errmsg("MCC CODE 不可空白");
      return;
    }

    if (wp.itemEmpty("risk_type")) {
      errmsg("風險分類:不可空白");
      return;
    }

    if (empty(wp.itemStr("mcc_remark"))) {
      errmsg("MCC說明 不可空白");
      return;
    }

    if (this.isAdd()) {
      return;
    }

    // -other modify-
    sqlWhere = " where mcc_code= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {mccCode, wp.itemNum("mod_seqno")};
    // ddd("sql-where="+sql_where);
    if (this.isOtherModify("CCA_MCC_RISK", sqlWhere,parms)) {
      return;
    }

    if (isEmpty(wp.itemStr("risk_factor"))) {
      wp.itemSet("risk_factor", "0");
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into CCA_MCC_RISK (" + " mcc_code, " // 1
        + " risk_type, " + " mcc_remark, " + " amount_rule, " 
        + " risk_factor, " + " crt_date, crt_user , " + " apr_date, apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,? "
        + ",to_char(sysdate,'yyyymmdd'),? " + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1"
        + " )";
    Object[] param = new Object[] {mccCode // 1
        , wp.itemStr("risk_type"), wp.itemStr("mcc_remark"), wp.itemStr("amount_rule"),
        wp.itemStr("risk_factor"), wp.loginUser,
        wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm")};
    this.log("kk1=" + mccCode);
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
    strSql = "update CCA_MCC_RISK set " + " risk_type =?, " + " mcc_remark =?, "
        + " amount_rule =?, " + " risk_factor =?, "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user = ? , "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + " where mcc_code= ? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {wp.itemStr("risk_type"), wp.itemStr("mcc_remark"),
        wp.itemStr("amount_rule"), wp.itemStr("risk_factor"),
        wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"),
        mccCode, wp.itemNum("mod_seqno")};
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
    strSql = "delete CCA_MCC_RISK " + " where mcc_code= ? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {mccCode, wp.itemNum("mod_seqno")};    
    rc = sqlExec(strSql,param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
