/**
 * 2023-0705  JH    ++deleteAllDetl()
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package cmsm03;

import busi.FuncAction;

public class Cmsm4240Func extends FuncAction {
  String mccGroup = "";

int deleteAllDetl() throws Exception  {
  msgOK();

  strSql = " delete cms_mcc_group where mcc_group =? ";
//  item2ParmStr("mcc_group");
  setParm(1,wp.colStr("mcc_group"));
  sqlExec(strSql);

  if (sqlRowNum < 0) {
    errmsg("delete cms_mcc_group error ");
  }

  return rc;
}

  @Override
  public void dataCheck() {
    if (ibAdd)
      mccGroup = wp.itemStr2("kk_mcc_group");
    else
      mccGroup = wp.itemStr2("mcc_group");

    if (empty(mccGroup)) {
      errmsg("MCC類別: 不可空白");
      return;
    }

    if (ibAdd)
      return;

    sqlWhere = " where wf_type = 'CMS-MCC-GROUP' " + " and wf_id = ?" + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {mccGroup, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("ptr_sys_idtab", sqlWhere, parms)) {
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

    strSql = " insert into ptr_sys_idtab ( " + " wf_type ," + " wf_id ," + " wf_desc ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ( "
        + " 'CMS-MCC-GROUP' ," + " :kk1 ," + " :wf_desc ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " ) ";

    setString("kk1", mccGroup);
    setString("wf_desc", wp.itemStr2("group_desc"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert ptr_sys_idtab error !");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " update ptr_sys_idtab set " + " wf_desc =:wf_desc , " + " mod_user =:mod_user , "
        + " mod_time = sysdate , " + " mod_pgm =:mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where wf_type = 'CMS-MCC-GROUP' " + " and wf_id =:wf_id "
        + " and nvl(mod_seqno,0) =:mod_seqno ";;

    setString("wf_desc", wp.itemStr2("group_desc"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("wf_id", mccGroup);
    item2ParmNum("mod_seqno");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update ptr_sys_idtab error !");
    }

    return rc;
  }

  public int insertDetl() {
    msgOK();

    strSql = " insert into cms_mcc_group ( " + " mcc_group ," + " mcc_code ," + " apr_date ,"
        + " apr_user ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno "
        + " ) values ( " + " :mcc_group ," + " :mcc_code ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :apr_user ," + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 " + " ) ";

    item2ParmStr("mcc_group");
    setString("mcc_code", wp.itemStr2("ex_mcc_code"));
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cms_mcc_group error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    // --先刪 cms_mcc_group (明細) 再刪 ptr_sys_idtab(主檔)

    strSql = " delete cms_mcc_group where mcc_group =:mcc_group ";
    setString("mcc_group", mccGroup);
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cms_mcc_group error ");
      return rc;
    } else
      rc = 1;

    strSql = " delete ptr_sys_idtab where wf_type = 'CMS-MCC-GROUP' and wf_id =:wf_id ";
    setString("wf_id", mccGroup);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete ptr_sys_idtab error ");
      return rc;
    }
    return rc;
  }

  public int deleteDetl() {
    msgOK();

    strSql = " delete cms_mcc_group where mcc_group =:mcc_group and mcc_code =:mcc_code ";
    item2ParmStr("mcc_group");
    var2ParmStr("mcc_code");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete cms_mcc_group error ");
    }
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }


}
