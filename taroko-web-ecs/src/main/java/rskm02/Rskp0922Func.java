/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-1217  V1.00.01  Alex  bug fix
* 109-04-27  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-23   V1.00.03  Justin        parameterize sql
******************************************************************************/
package rskm02;

import busi.FuncAction;

public class Rskp0922Func extends FuncAction {
  //String kk1 = "";

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
    msgOK();

    updateCrdCard();
    if (rc != 1)
      return rc;
    updateCcaCardBase();
    if (rc != 1)
      return rc;
    if (eqIgno(varsStr("sms_flag"), "Y")) {
      insertSmsMsgDtl();
      if (rc != 1)
        return rc;
    }
    updateRskAcnoLog();

    return rc;
  }

  void updateCrdCard() {
    // --永調
    msgOK();
    strSql = " update crd_card set " + " son_card_flag =:son_card_flag , "
        + " indiv_crd_lmt =:indiv_crd_lmt , " + " indiv_inst_lmt =:indiv_inst_lmt , "
        + " mod_user =:mod_user ," + " mod_pgm =:mod_pgm , " + " mod_time = sysdate , "
        + " mod_seqno = nvl(mod_seqno,0)+1 " + " where card_no =:card_no ";

    var2ParmNvl("son_card_flag", "N");
    var2ParmNum("indiv_crd_lmt", "aft_loc_amt");
    var2ParmNum("indiv_inst_lmt", "aft_loc_cash");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskp0922");
    var2ParmStr("card_no");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update crd_card error ");
    }
  }

  void updateCcaCardBase() {
    msgOK();
    strSql = " update cca_card_base set " + " card_adj_limit =:card_adj_limit , "
        + " card_adj_date1 =:card_adj_date1 , " + " card_adj_date2 =:card_adj_date2 , "
        + " adj_chg_user =:adj_chg_user " + " where card_no =:card_no ";

    var2ParmNum("card_adj_limit");
    var2ParmStr("card_adj_date1");
    var2ParmStr("card_adj_date2");
    if (varsNum("card_adj_limit") == 0) {
      setString("adj_chg_user", "");
    } else {
      setString("adj_chg_user", varsStr("mod_user"));
    }

    var2ParmStr("card_no");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update cca_card_base error ");
    }
  }

  void insertSmsMsgDtl() {
    busi.func.SmsMsgDetl ooSms = new busi.func.SmsMsgDetl();
    ooSms.setConn(wp);
    rc = ooSms.rskP0922(varsStr("acno_p_seqno"), varsStr("major_id_p_seqno"), "RSKM0922-1");
  }

  void updateRskAcnoLog() {
    msgOK();

    strSql = " update rsk_acnolog set " + " apr_flag ='Y' , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " 
    		+ " apr_user =:apr_user " 
    		+ " where 1=1 and rowid =:rowid ";
    setString("apr_user", wp.loginUser);
    setRowId("rowid",varsStr("rowid"));

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_acnolog error ");
    }
  }

}
