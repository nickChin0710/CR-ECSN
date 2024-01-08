/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/

package ichm01;

import busi.FuncAction;

public class Ichm0040Func extends FuncAction {
  String parmType = "", seqNo = "";

  @Override
  public void dataCheck() {
    parmType = wp.itemStr2("parm_type");
    seqNo = wp.itemStr2("seq_no");

    if (ibAdd || ibUpdate) {
      if (wp.itemEq("mcode_cond", "Y")) {
        if (!wp.itemEmpty("payment_rate") && isNumber(wp.itemStr("payment_rate")) == false) {
          errmsg("Mcode 須為數字");
          return;
        }
        if (wp.itemNum("mcode_amt") <= 0) {
          errmsg("欠款本金 須大於 0");
          return;
        }
      }

      if (wp.itemEq("block_cond", "Y") && wp.itemEmpty("block_reason")) {
        errmsg("[列黑名單凍結碼] 不可空白");
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
        + " mcode_cond , " + " payment_rate , " + " mcode_amt , " + " block_cond , "
        + " block_reason , " + " crt_date ," + " crt_user ," + " apr_date ," + " apr_user ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ( "
        + " :parm_type ," + " :seq_no ," + " :parm_desc ," + " :mcode_cond , " + " :payment_rate , "
        + " :mcode_amt , " + " :block_cond , " + " :block_reason , "
        + " to_char(sysdate,'yyyymmdd') ," + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :apr_user ," + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 " + " ) ";

    setString("parm_type", parmType);
    setString("seq_no", seqNo);
    item2ParmStr("parm_desc");
    item2ParmNvl("mcode_cond", "N");
    item2ParmStr("payment_rate");
    item2ParmNum("mcode_amt");
    item2ParmNvl("block_cond", "N");
    item2ParmStr("block_reason");
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

    strSql = "update ich_00_parm set " + " mcode_cond =:mcode_cond , "
        + " payment_rate =:payment_rate , " + " mcode_amt =:mcode_amt , "
        + " block_cond =:block_cond , " + " block_reason =:block_reason , "
        + " apr_date =to_char(sysdate,'yyyymmdd') , " + " apr_user =:apr_user , "
        + " mod_user =:mod_user , " + " mod_pgm =:mod_pgm , " + " mod_time = sysdate , "
        + " mod_seqno = nvl(mod_seqno,0)+1 " + " where parm_type =:parm_type "
        + " and seq_no =:seq_no ";

    item2ParmNvl("mcode_cond", "N");
    item2ParmStr("payment_rate");
    item2ParmNum("mcode_amt");
    item2ParmNvl("block_cond", "N");
    item2ParmStr("block_reason");
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
