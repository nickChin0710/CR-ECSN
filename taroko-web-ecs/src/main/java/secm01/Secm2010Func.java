/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
* 109-12-24  V1.00.02  Justin         parameterize sql
******************************************************************************/
package secm01;

import busi.FuncAction;

public class Secm2010Func extends FuncAction {
  String alLevel = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      alLevel = wp.itemStr("kk_al_level");
    } else {
      alLevel = wp.itemStr("al_level");
    }

    if (empty(alLevel)) {
      errmsg("額度層級:不可空白");
      return;
    }

    if (wp.itemNum("al_amt") == 0 && wp.itemNum("al_amt02") == 0) {
      errmsg("個人及公司額度不可全部為 0");
      return;
    }

    if (this.ibAdd) {
      return;
    }

    sqlWhere = " where al_level= ? and nvl(mod_seqno,0) = ? " ;
    log("sql-where=" + sqlWhere);
    if (this.isOtherModify("sec_amtlimit", sqlWhere, new Object[] {alLevel, wp.modSeqno()})) {
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

    strSql = "insert into sec_amtlimit (" + " al_level , " + " al_amt , " + " al_amt02 , "
        + " al_amt03 , " + " al_desc , " + " crt_date , " + " crt_user , " + " apr_flag , "
        + " apr_date , " + " apr_user , " + " mod_user , " + " mod_time , " + " mod_pgm , "
        + " mod_seqno " + " ) values (" + " :kk1 , " + " :al_amt , " + " :al_amt02 , "
        + " :al_amt03 , " + " :al_desc , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , "
        + " 'Y' , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " + " :mod_user , "
        + " sysdate , " + " :mod_pgm , " + " '1' " + " )";
    // -set ?value-
    setString("kk1", alLevel);
    item2ParmNum("al_amt");
    item2ParmNum("al_amt02");
    item2ParmNum("al_amt03");
    item2ParmStr("al_desc");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "secm2010");
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

    strSql = "update sec_amtlimit set " + " al_amt =:al_amt ," + " al_amt02 =:al_amt02 ,"
        + " al_amt03 =:al_amt03 ," + " al_desc =:al_desc ," + " apr_flag ='Y' ,"
        + " apr_date =to_char(sysdate,'yyyymmdd') ," + " apr_user =:apr_user ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where 1=1 " + " and al_level =:kk1 "
        + " and nvl(mod_seqno,0)=:mod_seqno"// 14
    ;
    item2ParmNum("al_amt");
    item2ParmNum("al_amt02");
    item2ParmNum("al_amt03");
    item2ParmStr("al_desc");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "secm2010");
    setString("kk1", alLevel);
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
    strSql = "delete sec_amtlimit " + " where 1=1 " + " and al_level =:kk1 "
        + " and nvl(mod_seqno,0)=:mod_seqno";

    setString("kk1", alLevel);
    item2ParmNum("mod_seqno");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
