/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard    
* 111-04-14  V1.00.01  machao     TSC畫面整合  *
******************************************************************************/
package tscm01;

import busi.FuncAction;

public class Tscm2250Func extends FuncAction {

  @Override
  public void dataCheck() {
    if (this.eqIgno(wp.itemNvl("bkec_block_cond", "N"), "Y")) {
      if (empty(wp.itemStr("bkec_block_reason"))) {
        errmsg("[排除-凍結碼] 不可空白");
        return;
      }
    }

    if (this.eqIgno(wp.itemNvl("auto_block_cond", "N"), "Y")) {
      if (empty(wp.itemStr("auto_block_reason"))) {
        errmsg("[排除-維護特指] 不可空白");
        return;
      }
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into rsk_comm_parm (" + " parm_type ," + " seq_no ," + " parm_desc ,"
        + " bkec_block_cond ," + " bkec_block_reason ," + " auto_block_cond ,"
        + " auto_block_reason ," + " apr_date ," + " apr_user ," + " mod_user ," + " mod_time ,"
        + " mod_pgm ," + " mod_seqno " + " ) values (" + " 'W_TSCM2250' , " + " '10' , "
        + " '悠遊卡拒絕代行參數' , " + " :bkec_block_cond , " + " :bkec_block_reason , "
        + " :auto_block_cond , " + " :auto_block_reason , " + " to_char(sysdate,'yyyymmdd') , "
        + " :apr_user , " + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " '1' " + " )";
    item2ParmNvl("bkec_block_cond", "N");
    item2ParmStr("bkec_block_reason");
    item2ParmNvl("auto_block_cond", "N");
    item2ParmStr("auto_block_reason");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscm2250");

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert rsk_comm_parm error, " + getMsg());
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
    strSql = "update rsk_comm_parm set " + " bkec_block_cond =:bkec_block_cond ,"
        + " bkec_block_reason =:bkec_block_reason ," + " auto_block_cond =:auto_block_cond ,"
        + " auto_block_reason =:auto_block_reason ," + " apr_date =to_char(sysdate,'yyyymmdd') , "
        + " apr_user =:apr_user ," + " mod_user =:mod_user ," + " mod_time =sysdate , "
        + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where parm_type ='W_TSCM2250' " + " and seq_no = '10'";

    item2ParmNvl("bkec_block_cond", "N");
    item2ParmStr("bkec_block_reason");
    item2ParmNvl("auto_block_cond", "N");
    item2ParmStr("auto_block_reason");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscm2250");

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Update rsk_comm_parm error, " + getMsg());
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
