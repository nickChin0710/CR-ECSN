package cmsm02;

import busi.FuncProc;

public class Cmsp6030Func extends FuncProc {

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
    sqlWhere = " where hex(rowid) =:rowid " + " and nvl(mod_seqno,0) =:mod_seqno ";
    var2ParmStr("rowid");
    var2ParmNum("mod_seqno");
    isOtherModify("CMS_CASEDETAIL", sqlWhere);
  }

  @Override
  public int dataProc() {
    updateDetail();
    updateMaster();
    log("rc_2:" + rc);
    return rc;
  }

  public int updateDetail() {
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "update CMS_CASEDETAIL set" + "  apr_flag ='Y' " + ", apr_user =:apr_user"
        + ", apr_date = to_char(sysdate,'yyyymmdd')"
        + ", finish_date = decode(proc_result,'9',to_char(sysdate,'yyyymmdd'),finish_date)"
        + ", mod_time = sysdate" + ", mod_user =:mod_user " + ", mod_pgm =:mod_pgm "
        + ", mod_seqno =mod_seqno+1 " + " where hex(rowid)=:rowid";
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsp6030");
    var2ParmStr("rowid");
    setString("apr_user", wp.loginUser);
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update CMS_CASEDETAIL error; " + this.getMsg());
    }
    return rc;
  }

  public int updateMaster() {
    strSql = "select min(proc_result) as proc_result" + " from cms_casedetail "
        + " where case_date=? " + " and case_seqno=?";
    this.sqlSelect(strSql, new Object[] {varsStr("case_date"), varsStr("case_seqno")});
    if (sqlRowNum <= 0)
      return 0;
    if (colEq("proc_result", "9") == false)
      return 1;
    log("case_date: " + varsStr("case_date") + " case_seqno: " + varsStr("case_seqno"));
    strSql = "update CMS_CASEMASTER set" + "  case_result ='9' "
        + ", finish_date = to_char(sysdate,'yyyymmdd')" + ", mod_time = sysdate"
        + ", mod_user =:mod_user " + ", mod_pgm =:mod_pgm " + ", mod_seqno =mod_seqno+1 "
        + " where case_date=:case_date " + " and case_seqno=:case_seqno";
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsp6030");
    var2ParmStr("case_date");
    var2ParmStr("case_seqno");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update CMS_CASEMASTER error; " + this.getMsg());
    }
    return rc;
  }
}
