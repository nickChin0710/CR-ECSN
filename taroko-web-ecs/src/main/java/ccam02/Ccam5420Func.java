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

public class Ccam5420Func extends FuncAction {
  String binType = "", eciVal = "", ucafVal = "", sysKey = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      binType = wp.itemStr("kk_bin_type");
      eciVal = wp.itemStr("kk_eci_val");
      ucafVal = wp.itemStr("kk_ucaf_val");
    } else {
      binType = wp.itemStr("bin_type");
      eciVal = wp.itemStr("eci_val");
      ucafVal = wp.itemStr("ucaf_val");
    }
    if (empty(binType)) {
      this.errmsg("國際組織:不可空白");
      return;
    }

    if (empty(eciVal) && empty(ucafVal)) {
      errmsg("ECI , UCAF 不可同時空白");
      return;
    }

    sysKey = binType + "-" + eciVal + "-" + ucafVal;

    if (this.ibAdd) {
      return;
    }

    sqlWhere = " where 1=1 and sys_id ='3D-WEB' and sys_key= ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {sysKey, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("cca_sys_parm2", sqlWhere,parms)) {
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql =
        "insert into cca_sys_parm2 (" + " sys_id ," + " sys_key ," + " sys_data1 ," + " sys_data2 ,"
            + " sys_data3 ," + " crt_date ," + " crt_user ," + " apr_date ," + " apr_user ,"
            + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ("
            + " '3D-WEB' ," + " :sys_key ," + " :sys_data1 ," + " :sys_data2 ," + " :sys_data3 ,"
            + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ,"
            + " :apr_user ," + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " '1' " + " )";

    setString("sys_key", sysKey);
    item2ParmStr("sys_data1", "tx_desc");
    setString("sys_data2", eciVal);
    setString("sys_data3", ucafVal);
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5420");
    sqlExec(strSql);
    wp.log(strSql);
    if (sqlRowNum <= 0) {
      errmsg("資料已存在不可新增");
      return rc;
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

    strSql = "update cca_sys_parm2 set " + " sys_data1=:sys_data1 ,"
        + " apr_date=to_char(sysdate,'yyyymmdd') ," + " apr_user=:apr_user ,"
        + " mod_user=:mod_user ," + " mod_time=sysdate ," + " mod_pgm=:mod_pgm ,"
        + " mod_seqno=nvl(mod_seqno,0)+1 " + " where 1=1 " + " and sys_id ='3D-WEB'"
        + " and sys_key=:sys_key" + " and nvl(mod_seqno,0)=:mod_seqno";

    item2ParmStr("sys_data1", "tx_desc");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5420");
    item2ParmNum("mod_seqno");
    setString("sys_key", sysKey);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(getMsg());
      return rc;
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

    strSql = "delete cca_sys_parm2 " + " where 1=1 " + " and sys_id ='3D-WEB'"
        + " and sys_key=:sys_key" + " and nvl(mod_seqno,0)=:mod_seqno";

    setString("sys_key", sysKey);
    item2ParmNum("mod_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(getMsg());
      return rc;
    }
    return rc;

  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
