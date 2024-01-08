/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-11-28  V1.00.01  Alex        inesrt detl fix                           *
* 109-04-20  V1.00.02  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5051Func extends FuncEdit {
  String cardNote = "";

  public Ccam5051Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

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
    if (this.ibDelete) {
      cardNote = wp.itemStr("card_note");
    } else {
      cardNote = wp.itemStr("card_note");
      if (empty(cardNote))
        cardNote = wp.itemStr2("kk_card_note");
    }

    if (isEmpty(cardNote)) {
      errmsg("卡片等級 不可空白");
      return;
    }

    if (wp.itemEmpty("rowid"))
      return;

    sqlWhere = " where area_type ='T'" + " and card_note =?" + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {cardNote, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("cca_auth_parm", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into CCA_AUTH_PARM (" + " card_note, " // 1
        + " area_type, " + " end_date, " + " open_chk, " + " mcht_chk, " + " delinquent, "
        + " oversea_chk, " + " avg_consume_chk, " + " month_risk_chk, " + " day_risk_chk, "
        + " oversea_cash_pct, " + " crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " :card_note, 'T', :end_date, :open_chk "
        + ",:mcht_chk, :delinquent, :oversea_chk, :avg_consume_chk"
        + ",:month_risk_chk, :day_risk_chk, :oversea_cash_pct"
        + ",to_char(sysdate,'yyyymmdd'),:crt_user " + ",sysdate, :mod_user, :mod_pgm, 1" + " )";
    try {
      this.setString("card_note", cardNote);
      item2ParmStr("end_date");
      item2ParmNvl("open_chk", "0");
      item2ParmNvl("mcht_chk", "0");
      item2ParmNvl("delinquent", "0");
      item2ParmNvl("oversea_chk", "0");
      item2ParmNvl("avg_consume_chk", "0");
      item2ParmNvl("month_risk_chk", "0");
      item2ParmNvl("day_risk_chk", "0");
      item2ParmNum("oversea_cash_pct");
      item2ParmNum("add_tot_amt");
      setString("crt_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
    } catch (Exception ex) {
      wp.log("sqlParm", ex);
    }
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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

    // --有 rowid 就 update 沒有就 insert

    if (wp.itemEmpty("rowid")) {
      insertParm();
    } else {

    }

    return rc;

  }



  public int dataProcParm() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    // --有 rowid 就 update 沒有就 insert

    if (wp.itemEmpty("rowid")) {
      insertParm();
    } else {
      updateParm();
    }

    return rc;
  }

  void updateParm() {
    msgOK();

    strSql = "update CCA_AUTH_PARM set " + " end_date = :end_date, " + " open_chk = :open_chk, "
        + " mcht_chk = :mcht_chk, " + " delinquent = :delinquent, "
        + " oversea_chk = :oversea_chk, " + " avg_consume_chk = :avg_consume_chk, "
        + " month_risk_chk = :month_risk_chk, " + " day_risk_chk = :day_risk_chk, "
        + " oversea_cash_pct = :oversea_cash_pct, "
        + " mod_user = :mod_user, mod_time=sysdate, mod_pgm =:mod_pgm "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " + " where card_note =:kk " + " and area_type ='T'"
        + " and nvl(mod_seqno,0) =:mod_seqno ";;
    // Object[] param = new Object[] {
    // wp.item_ss("risk_desc"),
    // wp.item_ss("mod_user"),
    // wp.item_ss("mod_pgm")
    // };
    item2ParmStr("end_date");
    item2ParmNvl("open_chk", "0");
    item2ParmNvl("mcht_chk", "0");
    item2ParmNvl("delinquent", "0");
    item2ParmNvl("oversea_chk", "0");
    item2ParmNvl("avg_consume_chk", "0");
    item2ParmNvl("month_risk_chk", "0");
    item2ParmNvl("day_risk_chk", "0");
    item2ParmNum("oversea_cash_pct");
    setString("mod_user", wp.loginUser);
    item2ParmStr("mod_pgm");
    setString("kk", cardNote);
    item2ParmNum("mod_seqno");

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
  }

  void insertParm() {
    msgOK();

    strSql = "insert into CCA_AUTH_PARM (" + " card_note, " // 1
        + " area_type, " + " end_date, " + " open_chk, " + " mcht_chk, " + " delinquent, "
        + " oversea_chk, " + " avg_consume_chk, " + " month_risk_chk, " + " day_risk_chk, "
        + " oversea_cash_pct, " + " crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " :card_note, 'T', :end_date, :open_chk "
        + ",:mcht_chk, :delinquent, :oversea_chk, :avg_consume_chk"
        + ",:month_risk_chk, :day_risk_chk, :oversea_cash_pct"
        + ",to_char(sysdate,'yyyymmdd'),:crt_user " + ",sysdate, :mod_user, :mod_pgm, 1" + " )";

    this.setString("card_note", cardNote);
    item2ParmStr("end_date");
    item2ParmNvl("open_chk", "0");
    item2ParmNvl("mcht_chk", "0");
    item2ParmNvl("delinquent", "0");
    item2ParmNvl("oversea_chk", "0");
    item2ParmNvl("avg_consume_chk", "0");
    item2ParmNvl("month_risk_chk", "0");
    item2ParmNvl("day_risk_chk", "0");
    item2ParmNum("oversea_cash_pct");
    item2ParmNum("add_tot_amt");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());


    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete CCA_AUTH_PARM " + " where card_note =:kk1 " + " and area_type='T'"
        + " and nvl(mod_seqno,0) =:mod_seqno ";
    // ddd("del-sql="+is_sql);
    setString("kk1", cardNote);
    item2ParmNum("mod_seqno");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    dbDeleteAll();
    return rc;
  }

  void deleteParm() {
    msgOK();

    strSql = "delete CCA_AUTH_PARM " + " where card_note =:kk1 " + " and area_type='T'";
    // ddd("del-sql="+is_sql);
    setString("kk1", cardNote);

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete CCA_AUTH_PARM error");
    } else
      rc = 1;
  }

  public int dbDeleteDtlAll() {
    msgOK();
    strSql = "delete CCA_RISK_LEVEL_PARM where card_note = :kk1 and area_type ='T' ";
    setString("kk1", cardNote);
    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete CCA_RISK_LEVEL_PARM error ");
    } else {
      rc = 1;
    }

    return rc;
  }

  public int dbInsertDtl() {
    msgOK();

    strSql = "insert into CCA_RISK_LEVEL_PARM (" + " card_note, " // 1
        + " area_type, " + " risk_level, " + " tot_amt_pct," + " max_cash_amt, " + " rsp_code, "
        + " inst_month_pct, " + " max_inst_amt, " + " add_tot_amt, " + " apr_date, " + " apr_user, "
        + " mod_user, mod_time, mod_pgm,mod_seqno " + " ) values (" + " :card_note, " // 1
        + " 'T', " + " :risk_level, " + " :tot_amt_pct," + " :max_cash_amt, " + " :rsp_code, "
        + " :inst_month_pct, " + " :max_inst_amt, " + " :add_tot_amt, "
        + " to_char(sysdate,'yyyymmdd'), " + " :apr_user, " + " :mod_user, sysdate, :mod_pgm,1 "
        + " )";

    var2ParmStr("card_note");
    var2ParmStr("risk_level");
    var2ParmNum("tot_amt_pct");
    var2ParmNum("max_cash_amt");
    var2ParmStr("rsp_code");
    var2ParmNum("inst_month_pct");
    var2ParmNum("max_inst_amt");
    var2ParmNum("add_tot_amt");
    setString("apr_user", wp.loginUser);
    var2ParmStr("mod_user");
    var2ParmStr("mod_pgm");
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert CCA_RISK_LEVEL_PARM error; " + getMsg());
    }
    return rc;
  }

  public int dbDeleteDtl() {
    msgOK();
    strSql = "Delete CCA_RISK_LEVEL_PARM" + " where rowid =:rowid ";
    setRowId("rowid", varsStr("rowid"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("Delete RSK_CTFI_FRMAN err; " + getMsg());
      rc = -1;
    } else
      rc = 1;
    return rc;
  }

  public int dbDeleteAll() {
    msgOK();
    strSql = "delete CCA_RISK_LEVEL_PARM " + " where card_note =:kk1 " + " and area_type='T'";
    // ddd("del-sql="+is_sql);
    setString("kk1", cardNote);
    rc = sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(this.sqlErrtext);
    } else
      rc = 1;
    return rc;
  }
}

