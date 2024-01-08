/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-12-31  V1.00.00   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package cmsm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Cmsm6040Func extends FuncEdit {
  String caseId = "";

  public Cmsm6040Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

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
    if (this.ibAdd) {
      caseId = wp.itemStr("kk_case_id");
    } else {
      caseId = wp.itemStr("case_id");
    }

    if (empty(caseId)) {
      errmsg("案件類別代碼: 不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where 1=1 " + " and case_id =:kk1 " + " and case_type ='1'";
    setString("kk1", caseId);

    if (isOtherModify("CMS_CASETYPE_MSG", sqlWhere) == true) {
      errmsg("資料已被修改, 請重新讀取");
      log("sql:" + sqlWhere + "  kk:" + caseId);
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

    strSql = "insert into CMS_CASETYPE_MSG (" + " case_type, " // 1
        + " case_id, " + " param1, " + " param2, " + " param3, " + " param4, " + " param5, "
        + " param6, " + " param7, " + " param8, " + " param9, " + " param10, " + " param11, "
        + " param12, " + " param13, " + " param14, " + " param15, " + " apr_flag," + " apr_user,"
        + " apr_date" + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " '1', "
        + " :case_id, " + " :param1, " + " :param2, " + " :param3, " + " :param4, " + " :param5, "
        + " :param6, " + " :param7, " + " :param8, " + " :param9, " + " :param10, " + " :param11, "
        + " :param12, " + " :param13, " + " :param14, " + " :param15," + " 'Y'," + " :apr_user,"
        + " to_char(sysdate,'yyyymmdd') " + ",sysdate, :mod_user, :mod_pgm, 1" + " )";
    // -set ?value-
    try {
      this.setString("case_id", caseId);
      item2ParmStr("param1");
      item2ParmStr("param2");
      item2ParmStr("param3");
      item2ParmStr("param4");
      item2ParmStr("param5");
      item2ParmStr("param6");
      item2ParmStr("param7");
      item2ParmStr("param8");
      item2ParmStr("param9");
      item2ParmStr("param10");
      item2ParmStr("param11");
      item2ParmStr("param12");
      item2ParmStr("param13");
      item2ParmStr("param14");
      item2ParmStr("param15");
      item2ParmStr("apr_user", "approval_user");
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
    } catch (Exception ex) {
      wp.expHandle("sqlParm", ex);
    }
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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

    strSql = "update CMS_CASETYPE_MSG set " + " case_type = '1'," + " case_id = :case_id,"
        + " param1 = :param1," + " param2 = :param2," + " param3 = :param3," + " param4 = :param4,"
        + " param5 = :param5," + " param6 = :param6," + " param7 = :param7," + " param8 = :param8,"
        + " param9 = :param9," + " param10 = :param10," + " param11 = :param11,"
        + " param12 = :param12," + " param13 = :param13," + " param14 = :param14,"
        + " param15 = :param15," + " apr_flag = 'Y'," + " apr_date = to_char(sysdate,'yyyymmdd'),"
        + " apr_user = :apr_user," + " mod_user = :mod_user, mod_time=sysdate, mod_pgm =:mod_pgm "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " + " where 1=1 " + " and case_id=:kk1"
        + " and case_type='1'" + " and nvl(mod_seqno,0) =:mod_seqno ";;

    item2ParmStr("case_id");
    item2ParmStr("param1");
    item2ParmStr("param2");
    item2ParmStr("param3");
    item2ParmStr("param4");
    item2ParmStr("param5");
    item2ParmStr("param6");
    item2ParmStr("param7");
    item2ParmStr("param8");
    item2ParmStr("param9");
    item2ParmStr("param10");
    item2ParmStr("param11");
    item2ParmStr("param12");
    item2ParmStr("param13");
    item2ParmStr("param14");
    item2ParmStr("param15");
    item2ParmStr("apr_user", "approval_user");
    item2ParmStr("mod_pgm");
    setString("mod_user", wp.loginUser);
    setString("kk1", caseId);
    item2ParmNum("mod_seqno");

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    log("sql_nrow:" + sqlRowNum);
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete CMS_CASETYPE_MSG " + " where 1=1 " + " and case_id =:kk1"
        + " and case_type='1'" + " and nvl(mod_seqno,0) =:mod_seqno ";;
    setString("kk1", caseId);
    item2ParmNum("mod_seqno");

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
