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

public class Tscm2230Func extends FuncAction {

  @Override
  public void dataCheck() {
    if (eqIgno(wp.itemNvl("mcode_cond", "N"), "Y")) {
      if (wp.itemNum("payment_rate") < 0) {
        errmsg("MCode : 須大於0");
        return;
      }
      if (wp.itemNum("mcode_amt") < 0) {
        errmsg("MCode欠款本金 : 須大於0");
        return;
      }
    }

    if (eqIgno(wp.itemNvl("bkec_block_cond", "N"), "Y")) {
      if (empty(wp.itemStr("bkec_block_reason"))) {
        errmsg("[列黑名單凍結碼] 不可空白");
        return;
      }
    }

    if (eqIgno(wp.itemNvl("auto_block_cond", "N"), "Y")) {
      if (empty(wp.itemStr("auto_block_reason"))) {
        errmsg("[Auto load 凍結碼] 不可空白");
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
    strSql = "insert into rsk_comm_parm (" + " parm_type , " + " seq_no , " + " parm_desc , "
        + " card_lost_cond , " + " card_oppo_cond , " + " mcode_cond , " + " bkec_block_cond , "
        + " bkec_block_reason , " + " auto_block_cond , " + " auto_block_reason , "
        + " payment_rate , " + " mcode_amt , " + " apr_date , " + " apr_user , " + " mod_user , "
        + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values (" + " 'W_RSKM2230' , "
        + " '1' , " + " '悠遊卡黑名單參數' , " + " :card_lost_cond , " + " :card_oppo_cond , "
        + " :mcode_cond , " + " :bkec_block_cond , " + " :bkec_block_reason , "
        + " :auto_block_cond , " + " :auto_block_reason , " + " :payment_rate , " + " :mcode_amt , "
        + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " + " :mod_user , " + " sysdate , "
        + " :mod_pgm , " + " '1' " + " )";
    item2ParmNvl("card_lost_cond", "N");
    item2ParmNvl("card_oppo_cond", "N");
    item2ParmNvl("mcode_cond", "N");
    item2ParmNvl("bkec_block_cond", "N");
    item2ParmStr("bkec_block_reason");
    item2ParmNvl("auto_block_cond", "N");
    item2ParmStr("auto_block_reason");
    item2ParmStr("payment_rate");
    item2ParmNum("mcode_amt");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscm2230");

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
    strSql = "update rsk_comm_parm set " + " card_lost_cond =:card_lost_cond ,"
        + " card_oppo_cond =:card_oppo_cond ," + " mcode_cond =:mcode_cond ,"
        + " bkec_block_cond =:bkec_block_cond ," + " bkec_block_reason =:bkec_block_reason ,"
        + " auto_block_cond =:auto_block_cond ," + " auto_block_reason =:auto_block_reason ,"
        + " payment_rate =:payment_rate ," + " mcode_amt =:mcode_amt ,"
        + " apr_date =to_char(sysdate,'yyyymmdd') , " + " apr_user =:apr_user ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate , " + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where parm_type ='W_RSKM2230' "
        + " and seq_no = '1'";

    item2ParmNvl("card_lost_cond", "N");
    item2ParmNvl("card_oppo_cond", "N");
    item2ParmNvl("mcode_cond", "N");
    item2ParmNvl("bkec_block_cond", "N");
    item2ParmStr("bkec_block_reason");
    item2ParmNvl("auto_block_cond", "N");
    item2ParmStr("auto_block_reason");
    item2ParmStr("payment_rate");
    item2ParmNum("mcode_amt");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "tscm2230");

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert rsk_comm_parm error, " + getMsg());
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
