/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncAction;

public class Ccap5053Func extends FuncAction {

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
    if (delData() != 1)
      return rc;
    if (insertData() != 1)
      return rc;
    if (delDataTemp() != 1)
      return rc;
    return rc;
  }

  public int delData() {
    msgOK();
    strSql =
        "delete cca_risk_consume_parm where card_note =:card_note and risk_type =:risk_type and risk_level =:risk_level ";
    var2ParmStr("card_note");
    var2ParmStr("risk_type");
    var2ParmStr("risk_level");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cca_risk_consume_parm error ");
    } else {
      rc = 1;
    }

    return rc;
  }

  public int delDataTemp() {
    msgOK();
    strSql =
        "delete cca_risk_consume_parm_t where card_note =:card_note and risk_type =:risk_type and risk_level =:risk_level ";
    var2ParmStr("card_note");
    var2ParmStr("risk_type");
    var2ParmStr("risk_level");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cca_risk_consume_parm error ");
    } else {
      rc = 1;
    }

    return rc;
  }

  public int insertData() {
    msgOK();

    strSql = " insert into cca_risk_consume_parm ( " + " card_note , " + " risk_type , "
        + " risk_level , " + " area_type , " + " lmt_amt_month_pct , " + " lmt_amt_time_pct , "
        + " lmt_cnt_day , " + " lmt_cnt_month , " + " rsp_code_1 , " + " rsp_code_2 , "
        + " rsp_code_3 , " + " rsp_code_4 , " + " crt_date , " + " crt_user , " + " apr_date , "
        + " apr_user , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values ( " + " :card_note , " + " :risk_type , " + " :risk_level , " + " 'T' , "
        + " :lmt_amt_month_pct , " + " :lmt_amt_time_pct , " + " :lmt_cnt_day , "
        + " :lmt_cnt_month , " + " :rsp_code_1 , " + " :rsp_code_2 , " + " :rsp_code_3 , "
        + " :rsp_code_4 , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " + " :mod_user , " + " sysdate , "
        + " :mod_pgm , " + " 1 " + " ) ";

    var2ParmStr("card_note");
    var2ParmStr("risk_type");
    var2ParmStr("risk_level");
    var2ParmNum("lmt_amt_month_pct");
    var2ParmNum("lmt_amt_time_pct");
    var2ParmNum("lmt_cnt_day");
    var2ParmNum("lmt_cnt_month");
    var2ParmStr("rsp_code_1");
    var2ParmStr("rsp_code_2");
    var2ParmStr("rsp_code_3");
    var2ParmStr("rsp_code_4");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    var2ParmStr("mod_user");
    setString("mod_pgm", "ccam5053");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cca_risk_consume_parm error ");
    }

    return rc;
  }

}
