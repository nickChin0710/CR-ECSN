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

public class Ccam8020Func extends FuncAction {
  String sysId = "", sysKey = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      sysId = wp.itemStr("kk_sys_id");
      sysKey = wp.itemStr("kk_sys_key");
    } else {
      sysId = wp.itemStr("sys_id");
      sysKey = wp.itemStr("sys_key");
    }

    if (empty(sysId)) {
      errmsg("參數類別 : 不可空白");
      return;
    }

    if (empty(sysKey)) {
      errmsg("參數代碼 : 不可空白");
      return;
    }

    if (empty(wp.itemStr("sys_data1"))) {
      errmsg("參數說明一 : 不可空白");
      return;
    }

    if (this.ibAdd) {
      return;
    }

    sqlWhere = " where 1=1 and sys_id = ? and sys_key = ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {sysId, sysKey,wp.itemNum("mod_seqno")};
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

    strSql = "insert into CCA_SYS_PARM2 (" + " sys_id , " + " sys_key , " + " sys_data1 , "
        + " sys_data2 , " + " sys_data3 , " + " apr_date , " + " apr_user , " + " crt_date , "
        + " crt_user , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values (" + " :kk1 , " + " :kk2 , " + " :sys_data1 , " + " :sys_data2 , "
        + " :sys_data3 , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " :mod_user , " + " sysdate , "
        + " :mod_pgm , " + " '1' " + " )";
    setString("kk1", sysId);
    setString("kk2", sysKey);
    item2ParmStr("sys_data1");
    item2ParmStr("sys_data2");
    item2ParmStr("sys_data3");
    setString("apr_user", wp.loginUser);
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam8020");
    wp.log(strSql);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(getMsg());
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

    strSql = "update CCA_SYS_PARM2 set " + " sys_data1 =:sys_data1 ," + " sys_data2 =:sys_data2 ,"
        + " sys_data3 =:sys_data3 ," + " apr_date =to_char(sysdate,'yyyymmdd') ,"
        + " apr_user =:apr_user ," + " mod_user =:mod_user ," + " mod_time =sysdate ,"
        + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 " + " where 1=1 "
        + " and sys_id =:kk1 " + " and sys_key =:kk2" + " and nvl(mod_seqno,0)=:mod_seqno";

    setString("kk1", sysId);
    setString("kk2", sysKey);
    item2ParmStr("sys_data1");
    item2ParmStr("sys_data2");
    item2ParmStr("sys_data3");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam8020");
    item2ParmNum("mod_seqno");
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

    strSql = "delete CCA_SYS_PARM2 " + " where 1=1 " + " and sys_id =:kk1 " + " and sys_key =:kk2"
        + " and nvl(mod_seqno,0)=:mod_seqno";

    setString("kk1", sysId);
    setString("kk2", sysKey);
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
