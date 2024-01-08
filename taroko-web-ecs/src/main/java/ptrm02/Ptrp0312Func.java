/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
*                                                                            *
******************************************************************************/
package ptrm02;

import busi.FuncProc;

public class Ptrp0312Func extends FuncProc {

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
    sqlWhere = " where acct_type =:acct_type " + " and class_code=:class_code";
    var2ParmStr("acct_type");
    var2ParmStr("class_code");
    log("sql_where" + sqlWhere);
    isOtherModify("ptr_class_code2", sqlWhere);


  }

  @Override
  public int dataProc() {
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    dbDeleteClassCode();
    dbDeleteDTL();

    strSql = "update ptr_class_code2 set" + " apr_user =:mod_user"
        + ", apr_date =to_char(sysdate,'yyyymmdd')" + ", apr_flag ='Y'" + ", mod_user =:mod_user"
        + ", mod_time =sysdate " + ", mod_pgm =:mod_pgm" + ", mod_seqno =mod_seqno+1"
        + " where acct_type =:acct_type" + " and class_code =:class_code" + " and apr_flag<>'Y'";
    var2ParmStr("mod_user");
    var2ParmStr("mod_pgm");
    var2ParmStr("acct_type");
    var2ParmStr("class_code");

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update PTR_CLASS_CODE2 error; " + this.getMsg());
    }
    updateDTL();
    return rc;
  }

  public int dbDeleteClassCode() {
    msgOK();

    strSql = "Delete ptr_class_code2" + " where acct_type =:acct_type"
        + " and class_code =:class_code" + " and apr_flag ='Y'";
    var2ParmStr("acct_type");
    var2ParmStr("class_code");
    sqlExec(strSql);
    return rc;
  }

  public int updateDTL() {

    strSql = "update PTR_CLASS_CODE_DTL set" + " apr_flag ='Y'" + " where acct_type =:acct_type"
        + " and class_code =:class_code" + " and apr_flag <>'Y'";
    var2ParmStr("acct_type");
    var2ParmStr("class_code");

    this.sqlExec(strSql);
    if (sqlRowNum == 0) {
      rc = 1;
    }
    return rc;
  }

  public int dbDeleteDTL() {
    msgOK();

    strSql = "Delete PTR_CLASS_CODE_DTL" + " where acct_type =:acct_type"
        + " and class_code =:class_code" + " and apr_flag ='Y'";
    var2ParmStr("acct_type");
    var2ParmStr("class_code");
    sqlExec(strSql);
    if (sqlRowNum == 0) {
      rc = 1;
    }
    return rc;

  }
}
