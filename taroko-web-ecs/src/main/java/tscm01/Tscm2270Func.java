/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-04-17  V1.00.01  Alex       新增VD悠遊卡問題交易結案程式                                                         *
******************************************************************************/
package tscm01;

import busi.FuncAction;

public class Tscm2270Func extends FuncAction {

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
    msgOK();
    actionInit("U");
    strSql = "update tsc_dcpr_log set " + " close_reason = :close_reason ,"
        + " close_remark = :close_remark ,";

    if (!wp.itemEq("close_reason", "0")) {
      strSql += " close_user =:mod_user ," + " close_date =to_char(sysdate,'yyyymmdd') ,";
    } else {
      strSql += " close_user ='' ," + " close_date ='' ,";
    }

    strSql += " mod_user = :mod_user ," + " mod_time = sysdate ," + " mod_pgm = :mod_pgm ,"
        + " mod_seqno = nvl(mod_seqno,0)+1 " + " where 1=1 " + " and hex(rowid) =:rowid ";

    item2ParmStr("close_reason");
    item2ParmStr("close_remark");
    item2ParmStr("rowid");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscm2270");    
    rc = sqlExec(strSql);    
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }    
    return rc;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
