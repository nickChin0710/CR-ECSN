/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.00  yanghan  修改了變量名稱和方法名稱*
* 109-12-25  V1.00.01   Justin      parameterize sql
******************************************************************************/
package dbbm01;

import busi.FuncAction;

public class Dbbm0100Func extends FuncAction {

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
    // TODO Auto-generated method stub
    return 0;
  }

  public int deleteDetl() {
    msgOK();

    strSql = "delete dbb_rsk_merchant where 1=1 and hex(rowid) = ? ";

    sqlExec(strSql, new Object[] {varsStr("rowid")});
    if (sqlRowNum <= 0) {
      errmsg("delete dbb_rsk_merchant error !");
    }
    return rc;
  }

  public int updateDetl() {
    msgOK();

    strSql = " update dbb_rsk_merchant set " + " mcht_no =:mcht_no , " + " mod_user =:mod_user , "
        + " mod_pgm =:mod_pgm , " + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where 1=1 and rowid =x:rowid" ;

    var2ParmStr("mcht_no");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("rowid", varsStr("rowid"));

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update dbb_rsk_merchant error ");
    }

    return rc;
  }

  public int insertDetl() {
    msgOK();

    strSql = " insert into dbb_rsk_merchant (" + " group_name , " + " mcht_no , " + " rsk_group , "
        + " mod_user , " + " mod_pgm , " + " mod_time , " + " mod_seqno " + " ) values ( "
        + " :group_name , " + " :mcht_no , " + " :rsk_group , " + " :mod_user , " + " :mod_pgm , "
        + " sysdate , " + " 1 " + " ) ";

    var2ParmStr("group_name");
    var2ParmStr("mcht_no");
    var2ParmStr("rsk_group");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert dbb_rsk_merchant error ");
    }

    return rc;
  }
}
