/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.00  yanghan  修改了變量名稱和方法名稱*
* 109-12-25  V1.00.01  Justin        parameterize sql
******************************************************************************/
package dbbm01;

import busi.FuncAction;

public class Dbbm0020Func extends FuncAction {
  String rowid = "";

  @Override
  public void dataCheck() {
    rowid = wp.itemStr("rowid");

    if (wp.itemNum("fees_fix_amt") < 0) {
      errmsg("此數值需大於等於0");
      return;
    }

    if (wp.itemNum("fix_rate") < 0) {
      errmsg("此數值需大於等於0");
      return;
    }

    sqlWhere = " where nvl(mod_seqno,0) =? and hex(rowid) = ? " ;
    Object[] parms = new Object[] {wp.itemNum("mod_seqno"), rowid};
    if (this.isOtherModify("dbb_markup", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");

    if (wp.itemNum("fees_fix_amt") < 0 || wp.itemNum("fix_rate") < 0) {
      errmsg("數值需大於等於0");
      return rc;
    }

    strSql = " insert into dbb_markup (" + " fees_fix_amt , " + " fix_rate , " + " key_no , "
        + " mod_pgm , " + " mod_seqno , " + " mod_time , " + " mod_user " + " ) values ( " + " 0 , "
        + " 0 , " + " '' , " + " :mod_pgm ," + " 1 , " + " sysdate , " + " :mod_user " + " ) ";

    setString("mod_pgm", wp.modPgm());
    setString("mod_user", "system");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert dbb_markup error !");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update dbb_markup set " + " fees_fix_amt =:fees_fix_amt , "
        + " fix_rate =:fix_rate , " + " mod_user =:mod_user , " + " mod_pgm =:mod_pgm , "
        + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where nvl(mod_seqno,0) =:mod_seqno and rowid =x:rowId " ;

    item2ParmNum("fees_fix_amt");
    item2ParmNum("fix_rate");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    item2ParmNum("mod_seqno");
    setString("rowId",rowid);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update dbb_markup error ");
    }

    return rc;
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

}
