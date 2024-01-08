/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *                                                                               *
******************************************************************************/

package ichm01;


import busi.FuncAction;

public class Ichm0060Func extends FuncAction {
  String parmType = "", seqNo = "";

  @Override
  public void dataCheck() {
    parmType = wp.itemStr2("parm_type");
    seqNo = wp.itemStr2("seq_no");

    if (ibAdd || ibUpdate) {
      if (wp.itemEq("block_cond", "Y") && wp.itemEmpty("block_reason")) {
        errmsg("[排除-凍結碼] 不可空白");
        return;
      }

      if (wp.itemEq("spec_cond", "Y") && wp.itemEmpty("spec_status")) {
        errmsg("[排除-維護特指] 不可空白");
        return;
      }

    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into ich_00_parm ( " + " parm_type ," + " seq_no ," + " parm_desc ,"
        + " block_cond , " + " block_reason , " + " spec_cond , " + " spec_status , "
        + " crt_date ," + " crt_user ," + " apr_date ," + " apr_user ," + " mod_user ,"
        + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ( " + " :parm_type ,"
        + " :seq_no ," + " :parm_desc ," + " :block_cond , " + " :block_reason , "
        + " :spec_cond , " + " :spec_status , " + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ,"
        + " to_char(sysdate,'yyyymmdd') ," + " :apr_user ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " ) ";

    setString("parm_type", parmType);
    setString("seq_no", seqNo);
    item2ParmStr("parm_desc");
    item2ParmNum("mcode_amt");
    item2ParmNvl("block_cond", "N");
    item2ParmStr("block_reason");
    item2ParmNvl("spec_cond", "N");
    item2ParmStr("spec_status");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr2("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert ich_00_parm error ");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update ich_00_parm set " + " block_cond =:block_cond , "
        + " block_reason =:block_reason , " + " spec_cond =:spec_cond , "
        + " spec_status =:spec_status , " + " apr_date =to_char(sysdate,'yyyymmdd') , "
        + " apr_user =:apr_user , " + " mod_user =:mod_user , " + " mod_pgm =:mod_pgm , "
        + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where parm_type =:parm_type " + " and seq_no =:seq_no ";

    item2ParmNvl("block_cond", "N");
    item2ParmStr("block_reason");
    item2ParmNvl("spec_cond", "N");
    item2ParmStr("spec_status");
    setString("apr_user", wp.itemStr2("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("parm_type", parmType);
    setString("seq_no", seqNo);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update ich_00_parm error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete ich_00_parm where parm_type=:parm_type and seq_no =:seq_no";

    setString("parm_type", parmType);
    setString("seq_no", seqNo);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete ich_00_parm error ");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
