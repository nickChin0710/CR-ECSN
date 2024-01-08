/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi   修改無意義的命名                                                                          * 
******************************************************************************/
package ichm01;

import busi.FuncAction;

public class Ichm0030Func extends FuncAction {
  String parmType = "", seqNo = "";

  @Override
  public void dataCheck() {
    parmType = wp.itemStr2("parm_type");
    seqNo = wp.itemStr2("seq_no");

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into ich_00_parm ( " + " parm_type ," + " seq_no ," + " parm_desc ,"
        + " amt01_txt ," + " amt01 ," + " amt02_txt ," + " amt02 ," + " cnt01 ," + " cnt01_txt ,"
        + " cnt02 ," + " cnt02_txt ," + " send_date ," + " crt_date ," + " crt_user ,"
        + " apr_date ," + " apr_user ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
        + " mod_seqno " + " ) values ( " + " :parm_type ," + " :seq_no ," + " :parm_desc ,"
        + " :amt01_txt ," + " :amt01 ," + " :amt02_txt ," + " :amt02 ," + " :cnt01 ,"
        + " :cnt01_txt ," + " :cnt02 ," + " :cnt02_txt ," + " '' ,"
        + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :apr_user ," + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 " + " ) ";

    setString("parm_type", parmType);
    setString("seq_no", seqNo);
    item2ParmStr("parm_desc");
    item2ParmStr("amt01_txt");
    item2ParmNum("amt01");
    item2ParmStr("amt02_txt");
    item2ParmNum("amt02");
    item2ParmNum("cnt01");
    item2ParmStr("cnt01_txt");
    item2ParmNum("cnt02");
    item2ParmStr("cnt02_txt");
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

    strSql = "update ich_00_parm set " + " amt01 =:amt01 , " + " amt02 =:amt02 , "
        + " cnt01 =:cnt01 , " + " cnt02 =:cnt02 , " + " apr_date = to_char(sysdate,'yyyymmdd') , "
        + " apr_user =:apr_user , " + " mod_user =:mod_user , " + " mod_pgm =:mod_pgm , "
        + " mod_time = sysdate , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where parm_type =:parm_type " + " and seq_no =:seq_no ";

    item2ParmNum("amt01");
    item2ParmNum("amt02");
    item2ParmNum("cnt01");
    item2ParmNum("cnt02");
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
