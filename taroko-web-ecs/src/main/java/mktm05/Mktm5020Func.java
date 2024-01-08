/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package mktm05;

import busi.FuncAction;

public class Mktm5020Func extends FuncAction {
  String salesId = "", aprFlag = "";

  @Override
  public void dataCheck() {
    if (ibAdd) {
      salesId = wp.itemStr("kk_sales_id");
      aprFlag = "N";
    } else {
      salesId = wp.itemStr("sales_id");
      aprFlag = wp.itemStr("apr_flag");
    }

    if (empty(salesId)) {
      errmsg("DS業務代號: 不可空白");
      return;
    }



    if (wp.itemEmpty("hire_date")) {
      errmsg("到職日: 不可空白");
      return;
    }

    if (wp.itemEmpty("mangr_id")) {
      errmsg("DS業務主管代號: 不可空白");
      return;
    }

    if (ibAdd)
      return;

    sqlWhere = " where sales_id = ? " + " and apr_flag =? " + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {salesId, aprFlag, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("mkt_ds_sales", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into mkt_ds_sales ( " + " sales_id ,  " + " mangr_id , " + " sales_cname , "
        + " id_no , " + " hire_date , " + " leave_date , " + " apr_flag , " + " apr_date , "
        + " apr_user , " + " crt_date , " + " crt_user , " + " mod_user , " + " mod_pgm , "
        + " mod_time , " + " mod_seqno " + " ) values ( " + " :kk1 ,  " + " :mangr_id , "
        + " :sales_cname , " + " :id_no , " + " :hire_date , " + " :leave_date , " + " 'N' , "
        + " '' , " + " '' , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , "
        + " :mod_user , " + " :mod_pgm , " + " sysdate , " + " 1 " + " ) ";

    setString("kk1", salesId);
    item2ParmStr("mangr_id");
    item2ParmStr("sales_cname");
    item2ParmStr("id_no");
    item2ParmStr("hire_date");
    item2ParmStr("leave_date");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert mkt_ds_sales error ");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    deleteTemp();
    if (rc != 1)
      return rc;
    insertTemp();
    if (rc != 1)
      return rc;

    return rc;
  }

  int deleteTemp() {
    msgOK();

    strSql = "delete mkt_ds_sales where sales_id =:kk1 and apr_flag ='N' ";
    setString("kk1", salesId);

    sqlExec(strSql);

    if (sqlRowNum < 0) {
      return rc;
    } else
      rc = 1;

    return rc;
  }

  int insertTemp() {
    msgOK();

    strSql = " insert into mkt_ds_sales ( " + " sales_id ,  " + " mangr_id , " + " sales_cname , "
        + " id_no , " + " hire_date , " + " leave_date , " + " apr_flag , " + " apr_date , "
        + " apr_user , " + " crt_date , " + " crt_user , " + " mod_user , " + " mod_pgm , "
        + " mod_time , " + " mod_seqno " + " ) values ( " + " :kk1 ,  " + " :mangr_id , "
        + " :sales_cname , " + " :id_no , " + " :hire_date , " + " :leave_date , " + " 'N' , "
        + " '' , " + " '' , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , "
        + " :mod_user , " + " :mod_pgm , " + " sysdate , " + " 1 " + " ) ";

    setString("kk1", salesId);
    item2ParmStr("mangr_id");
    item2ParmStr("sales_cname");
    item2ParmStr("id_no");
    item2ParmStr("hire_date");
    item2ParmStr("leave_date");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert mkt_ds_sales error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete mkt_ds_sales where sales_id =:kk1 and apr_flag =:kk2 ";
    setString("kk1", salesId);
    setString("kk2", aprFlag);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete mkt_ds_sales error !");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    msgOK();

    if (varEq("apr_flag", "Y")) {
      errmsg("已覆核資料 , 不可再覆核");
      return rc;
    }
    procDelete();
    if (rc != 1)
      return rc;

    procUpdate();
    return rc;
  }

  int procDelete() {
    msgOK();

    strSql = "delete mkt_ds_sales where sales_id =:sales_id and apr_flag ='Y' ";

    var2ParmStr("sales_id");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete mkt_ds_sales error");
    } else
      rc = 1;

    return rc;
  }

  int procUpdate() {
    msgOK();

    strSql = " update mkt_ds_sales set " + " apr_flag = 'Y' , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user = :apr_user " + " where "
        + " sales_id =:sales_id " + " and apr_flag ='N' ";

    var2ParmStr("sales_id");
    var2ParmStr("apr_user");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update mkt_ds_sales error ");
    }

    return rc;
  }

}
