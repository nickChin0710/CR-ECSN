/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.01  YangFang   updated for project coding standard        *
mangr_id  *
******************************************************************************/
package mktm05;

import busi.FuncAction;

public class Mktm5010Func extends FuncAction {
  String mangrId = "", aprFlag = "";

  @Override
  public void dataCheck() {

    if (ibAdd) {
      mangrId = wp.itemStr("kk_mangr_id");
      aprFlag = "N";
    } else {
      mangrId = wp.itemStr("mangr_id");
      aprFlag = wp.itemStr("apr_flag");
    }

    if (empty(mangrId)) {
      errmsg("DS業務主管代號: 不可空白");
      return;
    }



    if (ibAdd)
      return;

    sqlWhere = " where mangr_id = ? " + " and apr_flag =? " + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {mangrId, aprFlag, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("mkt_ds_mangr", sqlWhere, parms)) {
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

    strSql = " insert into mkt_ds_mangr ( " + " mangr_id , " + " mangr_cname , " + " id_no , "
        + " apr_flag , " + " apr_date , " + " apr_user , " + " crt_date , " + " crt_user , "
        + " mod_user , " + " mod_pgm , " + " mod_time , " + " mod_seqno " + " ) values ( "
        + " :kk1 , " + " :mangr_cname , " + " :id_no , " + " 'N' , " + " '' , " + " '' , "
        + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " :mod_user , " + " :mod_pgm , "
        + " sysdate , " + " 1 " + " ) ";

    setString("kk1", mangrId);
    item2ParmStr("mangr_cname");
    item2ParmStr("id_no");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert mkt_ds_mangr error ");
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

    strSql = "delete mkt_ds_mangr where mangr_id =:kk1 and apr_flag ='N' ";
    setString("kk1", mangrId);

    sqlExec(strSql);

    if (sqlRowNum < 0) {
      return rc;
    } else
      rc = 1;

    return rc;
  }

  int insertTemp() {
    msgOK();

    strSql = " insert into mkt_ds_mangr ( " + " mangr_id , " + " mangr_cname , " + " id_no , "
        + " apr_flag , " + " apr_date , " + " apr_user , " + " crt_date , " + " crt_user , "
        + " mod_user , " + " mod_pgm , " + " mod_time , " + " mod_seqno " + " ) values ( "
        + " :kk1 , " + " :mangr_cname , " + " :id_no , " + " 'N' , " + " '' , " + " '' , "
        + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " :mod_user , " + " :mod_pgm , "
        + " sysdate , " + " 1 " + " ) ";

    setString("kk1", mangrId);
    item2ParmStr("mangr_cname");
    item2ParmStr("id_no");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert mkt_ds_mangr error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete mkt_ds_mangr where mangr_id =:kk1 and apr_flag =:kk2 ";
    setString("kk1", mangrId);
    setString("kk2", aprFlag);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete mkt_ds_mangr error !");
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

    strSql = "delete mkt_ds_mangr where mangr_id =:mangr_id and apr_flag ='Y' ";

    var2ParmStr("mangr_id");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete mkt_ds_mangr error");
    } else
      rc = 1;

    return rc;
  }

  int procUpdate() {
    msgOK();

    strSql = " update mkt_ds_mangr set " + " apr_flag = 'Y' , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user = :apr_user " + " where "
        + " mangr_id =:mangr_id " + " and apr_flag ='N' ";

    var2ParmStr("apr_user");
    var2ParmStr("mangr_id");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update mkt_ds_mangr error ");
    }

    return rc;
  }

}
