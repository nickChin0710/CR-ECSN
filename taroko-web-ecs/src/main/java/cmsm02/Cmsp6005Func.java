package cmsm02;

import busi.FuncProc;

public class Cmsp6005Func extends FuncProc {

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
    sqlWhere = " where case_type =:case_type " + " and case_id =:case_id " + " and apr_flag='N'";
    var2ParmStr("case_type");
    var2ParmStr("case_id");

    isOtherModify("CMS_CASETYPE", sqlWhere);

  }

  @Override
  public int dataProc() {
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    dbDelete();
    strSql =
        "update CMS_CASETYPE set" + " apr_flag ='Y' " + ", apr_date = to_char(sysdate,'yyyymmdd') "
            + ", apr_user =:apr_user" + " , mod_time = sysdate" + ", mod_user =:mod_user "
            + ", mod_pgm =:mod_pgm " + ", mod_seqno =mod_seqno+1 " + " where case_type=:case_type"
            + " and case_id=:case_id" + " and apr_flag='N'";
    setString("mod_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_pgm", "cmsp6005");
    var2ParmStr("case_type");
    var2ParmStr("case_id");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update CMS_CASETYPE error; " + this.getMsg());
    }

    return rc;
  }

  public int dbDelete() {

    strSql = "delete CMS_CASETYPE " + " where case_type =:case_type " + " and case_id =:case_id"
        + " and apr_flag ='Y'";

    var2ParmStr("case_type");
    var2ParmStr("case_id");
    sqlExec(strSql);
    if (rc < 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }
}

