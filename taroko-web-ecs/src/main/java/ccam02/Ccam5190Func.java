
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
import taroko.com.TarokoCommon;

public class Ccam5190Func extends FuncEdit {
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

    return 0;
  }

  @Override
  public int dbUpdate() {
    /*--amt1--*/
    strSql = "update cca_sys_parm1 set" + " sys_data1 =?, " + " mod_user =?, "
        + " mod_time = sysdate, " + " mod_pgm =?, " + " mod_seqno =nvl(mod_seqno,0)+1"
        + " where sys_id ='REPORT'" + " and sys_key =?";
    Object[] param = new Object[] {wp.itemStr("amt1"), wp.loginUser, wp.modPgm(), "AMT1"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.AMT1 error; " + this.sqlErrtext);
      return rc;
    }
    /*--L_limit--*/
    // is_sql =
    // "update cca_sys_parm1 set"
    // + " sys_data1 =?, "
    // + " mod_user =?, "
    // + " mod_time = sysdate, "
    // + " mod_pgm =?, "
    // + " mod_seqno =nvl(mod_seqno,0)+1"
    // + " where sys_id ='REPORT'"
    // + " and sys_key =?";
    param = new Object[] {wp.itemStr("l_limit"), wp.loginUser, wp.modPgm(), "L_LIMIT"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.L_LIMIT error; " + this.sqlErrtext);
      return rc;
    }
    /*--U_limit--*/
    param = new Object[] {wp.itemStr("u_limit"), wp.loginUser, wp.modPgm(), "U_LIMIT"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.U_LIMIT error; " + this.sqlErrtext);
      return rc;
    }

    /*--rate1--*/
    // is_sql =
    // "update cca_sys_parm1 set"
    // + " sys_data1 =?, "
    // + " mod_user =?, "
    // + " mod_time = sysdate, "
    // + " mod_pgm =?, "
    // + " mod_seqno =nvl(mod_seqno,0)+1"
    // + " where sys_id ='REPORT'"
    // + " and sys_key =?";
    param = new Object[] {wp.itemStr("rate1"), wp.loginUser, wp.modPgm(), "RATE1"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.rate1 error; " + this.sqlErrtext);
      return rc;
    }
    /*--logic_day--*/
    // is_sql =
    // "update cca_sys_parm1 set"
    // + " sys_data1 =?, "
    // + " mod_user =?, "
    // + " mod_time = sysdate, "
    // + " mod_pgm =?, "
    // + " mod_seqno =nvl(mod_seqno,0)+1"
    // + " where sys_id ='REPORT'"
    // + " and sys_key =?";
    param = new Object[] {wp.itemStr("logic_day"), wp.loginUser, wp.modPgm(), "LOGIC_DAY"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.logic_day error; " + this.sqlErrtext);
      return rc;
    }
    /*--physical--*/
    // is_sql =
    // "update cca_sys_parm1 set"
    // + " sys_data1 =?, "
    // + " mod_user =?, "
    // + " mod_time = sysdate, "
    // + " mod_pgm =?, "
    // + " mod_seqno =nvl(mod_seqno,0)+1"
    // + " where sys_id ='REPORT'"
    // + " and sys_key =?";
    param = new Object[] {wp.itemStr("physical"), wp.loginUser, wp.modPgm(), "PHYSICAL"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.physical error; " + this.sqlErrtext);
      return rc;
    }
    /*--db_log--*/
    // is_sql =
    // "update cca_sys_parm1 set"
    // + " sys_data1 =?, "
    // + " mod_user =?, "
    // + " mod_time = sysdate, "
    // + " mod_pgm =?, "
    // + " mod_seqno =nvl(mod_seqno,0)+1"
    // + " where sys_id ='REPORT'"
    // + " and sys_key =?";
    param = new Object[] {wp.itemStr("db_log"), wp.loginUser, wp.modPgm(), "DB_LOG"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.db_log error; " + this.sqlErrtext);
      return rc;
    }
    
    //--VD_L_LIMIT
    param = new Object[] {wp.itemStr("vd_l_limit"), wp.loginUser, wp.modPgm(), "VD_L_LIMIT"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.db_log error; " + this.sqlErrtext);
      return rc;
    }
    
    //--VD_U_LIMIT
    param = new Object[] {wp.itemStr("vd_u_limit"), wp.loginUser, wp.modPgm(), "VD_U_LIMIT"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.db_log error; " + this.sqlErrtext);
      return rc;
    }
    
    //--VD_U_DAY
    param = new Object[] {wp.itemStr("vd_u_day"), wp.loginUser, wp.modPgm(), "VD_U_DAY"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.db_log error; " + this.sqlErrtext);
      return rc;
    }
    
    //--VD_U_DAY
    param = new Object[] {wp.itemStr("vd_l_day"), wp.loginUser, wp.modPgm(), "VD_L_DAY"};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("update CCA_SYS_PARM1.db_log error; " + this.sqlErrtext);
      return rc;
    }
    
    
    return rc;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

}
