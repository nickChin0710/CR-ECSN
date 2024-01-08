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

import busi.FuncAction;

public class Ptrp0480Func extends FuncAction {

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
    if (deleteVipCancel() == -1)
      return rc;
    if (deleteVipData() == -1)
      return rc;
    if (updataVipCancel() == -1)
      return rc;
    if (updataVipData() == -1)
      return rc;
    return rc;
  }

  public int deleteVipCancel() {
    msgOK();
    strSql = " delete ptr_vip_cancel where 1=1 and apr_flag ='Y' ";
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete ptr_vip_cancel error !");
    }
    return rc;
  }

  public int deleteVipData() {
    msgOK();
    strSql = " delete ptr_vip_data where 1=1 and table_name = 'PTR_VIP_CANCEL' and apr_flag ='Y' ";
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete ptr_vip_data error");
    }
    return rc;
  }

  public int updataVipCancel() {
    msgOK();
    strSql = " update ptr_vip_cancel set " + " apr_flag = 'Y' , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user =:apr_user , "
        + " mod_seqno = nvl(mod_seqno,0)+1 " + " where apr_flag = 'N' ";
    setString("apr_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ptr_vip_cancel error !");
    }
    return rc;
  }

  public int updataVipData() {
    msgOK();
    strSql = " update ptr_vip_data set " + " apr_flag = 'Y' " + " where "
        + " table_name = 'PTR_VIP_CANCEL' " + " and apr_flag = 'N' ";
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ptr_vip_data error !");
    }
    return rc;
  }

}
