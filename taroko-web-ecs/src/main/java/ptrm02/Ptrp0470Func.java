/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-12  V1.00.01  Alex       bug fix                                    *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/

package ptrm02;

import busi.FuncAction;

public class Ptrp0470Func extends FuncAction {

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
    if (deleteVipCode() == -1)
      return rc;
    if (deleteVipData() == -1)
      return rc;
    if (updataVipCode() == -1)
      return rc;
    if (updataVipData() == -1)
      return rc;
    return rc;
  }

  public int deleteVipCode() {
    msgOK();
    strSql = " delete ptr_vip_code where seq_no =:seq_no and apr_flag = 'Y' ";
    var2ParmStr("seq_no");
    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete ptr_vip_code error !");
    } else
      rc = 1;

    return rc;
  }

  public int deleteVipData() {
    msgOK();
    strSql = " delete ptr_vip_data where seq_no=:seq_no and apr_flag = 'Y' ";
    var2ParmStr("seq_no");
    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete ptr_vip_data error !");
    } else
      rc = 1;

    return rc;
  }

  public int updataVipCode() {
    msgOK();
    strSql = " update ptr_vip_code set " + " apr_flag = 'Y' , " + " apr_user =:apr_user , "
        + " apr_date = to_char(sysdate,'yyyymmdd') ," + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where seq_no =:seq_no " + " and apr_flag = 'N' ";

    setString("apr_user", wp.loginUser);
    var2ParmStr("seq_no");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ptr_vip_code error !");
    }

    return rc;
  }

  public int updataVipData() {
    msgOK();
    strSql = " update ptr_vip_data set " + " apr_flag = 'Y'  " + " where seq_no =:seq_no "
        + " and apr_flag = 'N' ";

    var2ParmStr("seq_no");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("update ptr_vip_data error !");
    } else
      rc = 1;

    return rc;
  }

}
