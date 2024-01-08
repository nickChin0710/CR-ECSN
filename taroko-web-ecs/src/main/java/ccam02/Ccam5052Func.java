
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  yanghan  修改了變量名稱和方法名稱*
* 109-12-25   V1.00.02 Justin       parameterize sql
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5052Func extends FuncEdit {
  String cardNote = "", riskType = "";

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      cardNote = wp.itemStr("kk_card_note");
      riskType = wp.itemStr("kk_risk_type");
    } else {
      cardNote = wp.itemStr("card_note");
      riskType = wp.itemStr("risk_type");
    }

    if (isEmpty(cardNote)) {
      errmsg("卡片等級 不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }

    sqlWhere = " where area_type ='T'" + " and card_note =?" + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {cardNote, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("cca_auth_parm", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }
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
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;
    delDetail();
    if (rc != 1)
      return rc;
    delParm();

    return rc;
  }

  public int delDetail() {
    msgOK();

    strSql = " delete cca_auth_parm where area_type = 'T' and card_note =:kk1 ";
    setString("kk1", cardNote);
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete cca_auth_parm error ! ");
    }

    return rc;
  }

  public int delParm() {
    msgOK();

    strSql =
        " delete cca_risk_consume_parm where area_type ='T' and card_note =:kk1 and risk_type =:kk2 ";
    setString("kk1", cardNote);
    setString("kk2", riskType);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete cca_risk_consume_parm error ! ");
    }

    return rc;
  }

  public int dbInsertDtl() {
    msgOK();
    wp.log("kk1,kk2=" + varsStr("card_note") + ", type=" + varsStr("risk_type"));

    strSql = "insert into CCA_RISK_CONSUME_PARM (" + " card_note, " // 1
        + " area_type, " + " risk_type, " + " risk_level," + " lmt_amt_month_pct, "
        + " rsp_code_1, " + " lmt_cnt_month, " + " rsp_code_2, " + " lmt_amt_time_pct, "
        + " rsp_code_3, " + " lmt_cnt_day, " + " rsp_code_4, " + " add_tot_amt, "
        // +" apr_date, "
        // +" apr_user, "
        + " mod_user, mod_time, mod_pgm,mod_seqno " + " ) values (" + " :card_note, " // 1
        + " 'T', " + " :risk_type, " + " :risk_level," + " :lmt_amt_month_pct, " + " :rsp_code_1, "
        + " :lmt_cnt_month, " + " :rsp_code_2, " + " :lmt_amt_time_pct, " + " :rsp_code_3, "
        + " :lmt_cnt_day, " + " :rsp_code_4, " + " :add_tot_amt, "
        // +" sysdate, "
        // +" :apr_user, "
        + " :mod_user, sysdate, :mod_pgm,1 " + " )";

    var2ParmStr("card_note");
    var2ParmStr("risk_type");
    var2ParmStr("risk_level");
    var2ParmNum("lmt_amt_month_pct");
    var2ParmStr("rsp_code_1");
    var2ParmNum("lmt_cnt_month");
    var2ParmStr("rsp_code_2");
    var2ParmNum("lmt_amt_time_pct");
    var2ParmStr("rsp_code_3");
    var2ParmNum("lmt_cnt_day");
    var2ParmStr("rsp_code_4");
    var2ParmNum("add_tot_amt");
    // setString("apr_user",wp.loginUser);
    var2ParmStr("mod_user");
    var2ParmStr("mod_pgm");
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert CCA_RISK_CONSUME_PARM error; " + getMsg());
    }
    return rc;
  }

  public int dbDeleteDtl() {
    msgOK();
    strSql = "Delete CCA_RISK_CONSUME_PARM" + " where hex(rowid) = ? ";
    sqlExec(strSql, new Object[] {this.varsStr("rowid")});
    if (sqlRowNum < 0) {
      errmsg("Delete CCA_RISK_CONSUME_PARM err; " + getMsg());
      rc = -1;
    } else
      rc = 1;
    return rc;
  }

}
