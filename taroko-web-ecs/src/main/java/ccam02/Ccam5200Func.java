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

public class Ccam5200Func extends FuncEdit {

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
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    strSql = "update CCA_sys_parm2 set " + " sys_data1 = :sys_data1, " + " sys_data2 = :sys_data2, "
        + " sys_data3 = :sys_data3," + " apr_date  = to_char(sysdate,'yyyymmdd'),"
        + " apr_user  = :apr_user, " + " mod_user = :mod_user," + " mod_time = sysdate,"
        + " mod_pgm =:mod_pgm," + " mod_seqno =nvl(mod_seqno,0)+1 " + " where sys_id ='MPARM' "
        + " and sys_key ='M1' " + " and mod_seqno=:mod_seqno_1 ";;

    item2ParmStr("sys_data1", "m1_mcode");
    item2ParmStr("sys_data2", "m1_unpay_amt");
    item2ParmStr("sys_data3", "m1_resp_code");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5200");
    item2ParmStr("mod_seqno_1");

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    strSql = "update CCA_sys_parm2 set " + " sys_data1 = :sys_data1, " + " sys_data2 = :sys_data2, "
        + " sys_data3 = :sys_data3," + " apr_date  = to_char(sysdate,'yyyymmdd'),"
        + " apr_user  = :apr_user, " + " mod_user = :mod_user," + " mod_time = sysdate,"
        + " mod_pgm =:mod_pgm," + " mod_seqno =nvl(mod_seqno,0)+1 " + " where sys_id ='MPARM' "
        + " and sys_key ='M2' " + " and mod_seqno=:mod_seqno_2 ";;

    item2ParmStr("sys_data1", "m1_mcode");
    item2ParmStr("sys_data2", "m2_unpay_amt");
    item2ParmStr("sys_data3", "m2_resp_code");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5200");
    item2ParmStr("mod_seqno_2");

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

}
