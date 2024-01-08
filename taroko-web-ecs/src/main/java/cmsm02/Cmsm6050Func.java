package cmsm02;

import busi.FuncProc;

public class Cmsm6050Func extends FuncProc {

  private static final String progName = "cmsm6050";

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
    sqlWhere = " where hex(rowid) =:rowid " + " and mod_seqno =:mod_seqno ";
    var2ParmStr("rowid");
    var2ParmNum("mod_seqno");
    log("rowid=" + varsStr("rowid") + " mod_seqno=" + varsNum("mod_seqno"));
    if (isOtherModify("CMS_CASEDETAIL", sqlWhere) == true) {
      errmsg("資料已被修改, 請重新讀取");
      return;
    }
  }

  @Override
  public int dataProc() {
    dataCheck();
    log("rc_1:" + rc);
    if (rc != 1) {
      return rc;
    }

    strSql = "update CMS_CASEDETAIL set" 
        + " proc_result ='5' " 
    	+ ", crt_user =:crt_user "
        + " , mod_time = sysdate" 
    	+ ", mod_user =:mod_user " 
        + ", mod_pgm =:mod_pgm "
        + ", mod_seqno =mod_seqno+1 " 
        + " where hex(rowid)=:rowid"
        + " and nvl(mod_seqno,0)=:mod_seqno";
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", progName);
    var2ParmStr("crt_user");
    var2ParmStr("rowid");
    var2ParmNum("mod_seqno");

    sqlExec(strSql);
    log("rc_2:" + rc);
    log("sql_nrow:" + sqlRowNum);
    if (sqlRowNum <= 0) {
      errmsg("update CMS_CASEDETAIL error; " + this.getMsg());
    }
    log("rc_3:" + rc);
    return rc;
  }

}
