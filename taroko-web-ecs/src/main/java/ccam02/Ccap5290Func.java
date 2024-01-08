/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncAction;

public class Ccap5290Func extends FuncAction {

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    deleteNotOnline();
    updateNotOnline();
    return rc;
  }

  public int deleteNotOnline() {

    strSql = "delete cca_mcht_notonline " + " where acq_id =:acq_id " + " and mcht_no =:mcht_no"
        + " and apr_flag='Y'";
    // ddd("del-sql="+is_sql);
    var2ParmStr("acq_id");
    var2ParmStr("mcht_no");

    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;
    return rc;
  }

  public int updateNotOnline() {
    strSql = "update cca_mcht_notonline set " + " apr_flag ='Y',"
        + " apr_date = to_char(sysdate,'yyyymmdd') ," + " apr_user =:apr_user "
        + " where acq_id =:acq_id " + " and mcht_no =:mcht_no " + " and apr_flag<>'Y'";

    var2ParmStr("acq_id");
    var2ParmStr("mcht_no");
    setString("apr_user", wp.loginUser);
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    } ;
    return rc;
  }

}
