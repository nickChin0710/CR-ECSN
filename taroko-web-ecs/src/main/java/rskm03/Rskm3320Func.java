/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm03;

import busi.FuncAction;

public class Rskm3320Func extends FuncAction {

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
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    if (wp.itemEmpty("rowid"))
      insertParm();
    else
      updateParm();

    return rc;
  }

  void insertParm() {
    msgOK();
    strSql = " insert into rsk_factor_parm ( " + " mcc_code_flag , " + " pos_flag , "
        + " country_flag , " + " black_mcht_flag , " + " black_card_flag , "
        + " card_risk_factor , " + " repeat_txn_flag , " + " repeat_factor , " + " in_vip_flag , "
        + " vip_factor , " + " amt_base_flag , " + " txn_amt_base , " + " crt_date , "
        + " crt_user , " + " apr_date , " + " apr_user , " + " mod_user , " + " mod_time , "
        + " mod_pgm , " + " mod_seqno " + " ) values ( " + " :mcc_code_flag , " + " :pos_flag , "
        + " :country_flag , " + " :black_mcht_flag , " + " :black_card_flag , "
        + " :card_risk_factor , " + " :repeat_txn_flag , " + " :repeat_factor , "
        + " :in_vip_flag , " + " :vip_factor , " + " :amt_base_flag , " + " :txn_amt_base , "
        + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , "
        + " :apr_user , " + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";

    item2ParmNvl("mcc_code_flag", "N");
    item2ParmNvl("pos_flag", "N");
    item2ParmNvl("country_flag", "N");
    item2ParmNvl("black_mcht_flag", "N");
    item2ParmNvl("black_card_flag", "N");
    item2ParmNum("card_risk_factor");
    item2ParmNvl("repeat_txn_flag", "N");
    item2ParmNum("repeat_factor");
    item2ParmNvl("in_vip_flag", "N");
    item2ParmNum("vip_factor");
    item2ParmNvl("amt_base_flag", "N");
    item2ParmNum("txn_amt_base");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert rsk_factor_parm error !");
    }
  }

  void updateParm() {
    msgOK();
    strSql = " update rsk_factor_parm set " + " mcc_code_flag =:mcc_code_flag , "
        + " pos_flag =:pos_flag , " + " country_flag =:country_flag , "
        + " black_mcht_flag =:black_mcht_flag , " + " black_card_flag =:black_card_flag , "
        + " card_risk_factor =:card_risk_factor , " + " repeat_txn_flag =:repeat_txn_flag , "
        + " repeat_factor =:repeat_factor , " + " in_vip_flag =:in_vip_flag , "
        + " vip_factor =:vip_factor , " + " amt_base_flag =:amt_base_flag , "
        + " txn_amt_base =:txn_amt_base , " + " apr_date =to_char(sysdate,'yyyymmdd') , "
        + " apr_user =:apr_user , " + " mod_user =:mod_user , " + " mod_time = sysdate , "
        + " mod_pgm =:mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where 1=1 and mod_seqno =:mod_seqno ";

    item2ParmNvl("mcc_code_flag", "N");
    item2ParmNvl("pos_flag", "N");
    item2ParmNvl("country_flag", "N");
    item2ParmNvl("black_mcht_flag", "N");
    item2ParmNvl("black_card_flag", "N");
    item2ParmNum("card_risk_factor");
    item2ParmNvl("repeat_txn_flag", "N");
    item2ParmNum("repeat_factor");
    item2ParmNvl("in_vip_flag", "N");
    item2ParmNum("vip_factor");
    item2ParmNvl("amt_base_flag", "N");
    item2ParmNum("txn_amt_base");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    item2ParmNum("mod_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_factor_parm error ");
    }

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " delete rsk_factor_parm where 1=1 ";

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete rsk_factor_parm error ");
    }


    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
