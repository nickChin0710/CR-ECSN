/*
 * 2019-1213  V1.00.01  Alex  fix dataCheck
 * 2020-0420  V1.00.01 yanghan 修改了變量名稱和方法名稱
 */

package ccam02;

import busi.FuncAction;


public class Ccam5270Func extends FuncAction {
  String cardNote = "", entryModeType = "", web3dFlag = "", riskType = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      cardNote = wp.itemStr("kk_card_note");
      entryModeType = wp.itemStr("kk_entry_mode_type");
      web3dFlag = wp.itemStr("kk_web3d_flag");
      riskType = wp.itemStr("kk_risk_type");
    } else {
      cardNote = wp.itemStr("card_note");
      entryModeType = wp.itemStr("entry_mode_type");
      web3dFlag = wp.itemStr("web3d_flag");
      riskType = wp.itemStr("risk_type");
    }

    if (empty(cardNote)) {
      errmsg("卡片等級: 不可空白");
      return;
    }

    if (empty(entryModeType)) {
      errmsg("entry Mode類別: 不可空白");
      return;
    }

    if (empty(web3dFlag)) {
      errmsg("交易類別: 不可空白");
      return;
    }

    if (empty(riskType)) {
      errmsg("風險類別: 不可空白");
      return;
    }

    if (isDelete())
      return;

    if (wp.itemEmpty("msg_id1")) {
      errmsg("簡訊代碼:不可空白");
      return;
    }

    if (wp.itemEmpty("tx_amt")) {
      errmsg("單筆金額門檻:不可空白");
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

    strSql = "insert into cca_auth_sms (" + " card_note , " // 1
        + " entry_mode_type , " + " web3d_flag , " + " risk_type , " + " cond1_yn , " // 5
        + " tx_amt , " + " cond1_mcc , " + " cond1_mcht , " + " cond1_risk , " + " msg_id1 , "
        + " use_flag , " + " crt_date , " // 15
        + " crt_user , " + " apr_date , " + " apr_user , " + " mod_user , " + " mod_time , " // 20
        + " mod_pgm , " + " mod_seqno " // 22
        + " ) values (" + " :card_note , " // 1
        + " :entry_mode_type , " + " :web3d_flag , " + " :risk_type , " + " 'Y' , " // 5
        + " :tx_amt , " + " 'N' , " + " 'N' , " + " 'N' , " + " :msg_id1 , " + " :use_flag , "
        + " to_char(sysdate,'yyyymmdd') , " // 15
        + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " + " :mod_user , "
        + " sysdate , " // 20
        + " :mod_pgm , " + " '1' " // 22
        + " )";

    setString("card_note", cardNote);
    setString("entry_mode_type", entryModeType);
    setString("web3d_flag", web3dFlag);
    setString("risk_type", riskType);
    item2ParmNum("tx_amt");
    item2ParmStr("msg_id1");
    item2ParmNvl("use_flag", "N");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5270");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
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

    strSql = " update cca_auth_sms set " + " tx_amt=:tx_amt , " + " msg_id1=:msg_id1 , "
        + " use_flag =:use_flag , " + " apr_date=to_char(sysdate,'yyyymmdd') , "
        + " apr_user=:apr_user , " + " mod_user=:mod_user , " + " mod_time=sysdate , "
        + " mod_pgm=:mod_pgm , " + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
        + " and entry_mode_type=:entry_mode_type " + " and web3d_flag=:web3d_flag "
        + " and risk_type=:risk_type";
    item2ParmNum("tx_amt");
    item2ParmStr("msg_id1");
    item2ParmNvl("use_flag", "N");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5270");
    setString("card_note", cardNote);
    setString("entry_mode_type", entryModeType);
    setString("web3d_flag", web3dFlag);
    setString("risk_type", riskType);
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    msgOK();
    strSql = "Delete cca_auth_sms" + " where card_note =:card_note "
        + " and entry_mode_type =:entry_mode_type " + " and web3d_flag =:web3d_flag "
        + " and risk_type =:risk_type "

    ;
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Delete parmdtl err; " + getMsg());
      rc = -1;
    } else
      rc = 1;

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int dbDeleteDetl() {
    msgOK();
    strSql = "Delete cca_auth_smsdetl" + " where card_note =:card_note "
        + " and entry_mode_type =:entry_mode_type " + " and web3d_flag =:web3d_flag "
        + " and risk_type =:risk_type " + " and data_type =:data_type";
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");
    setString("data_type", wp.itemStr("data_type1"));
    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("Delete parmdtl err; " + getMsg());
      rc = -1;
    } else
      rc = 1;

    return rc;
  }

  boolean checkMcc() {

    String sql1 =
        " select " + " count(*) as db_cnt " + " from cca_mcc_risk " + " where mcc_code = ? ";

    sqlSelect(sql1, new Object[] {varsStr("data_code1")});

    if (colNum("db_cnt") <= 0)
      return false;

    return true;
  }

  public int dbInsertDetl() {
    msgOK();

    if (eqIgno(wp.respHtml, "ccam5270_mcc1")) {
      if (checkMcc() == false) {
        errmsg("Mcc Code 不存在");
        return rc;
      }
    }

    strSql = "insert into cca_auth_smsdetl (" + " card_note , " + " entry_mode_type , "
        + " web3d_flag , " + " risk_type , " + " data_type , " + " apr_flag , " + " data_code1 , "
        + " data_code2 , " + " data_code3 , "
        + " mod_time , " + " mod_pgm " + " ) values (" + " :card_note , " + " :entry_mode_type , "
        + " :web3d_flag , " + " :risk_type , " + " :data_type , " + " 'Y' , " + " :data_code1 , "
        + " :data_code2 , " + " :data_code3 , "
        + " sysdate , " + " :mod_pgm " + " )";
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");
    var2ParmStr("data_type");
    var2ParmStr("data_code1");
    var2ParmStr("data_code2");
    var2ParmStr("data_code3");
    setString("mod_pgm", "ccam5270");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  boolean checkDtlMcc1() {
    String sql1 =
        "select count(*) as db_cnt " + " from cca_auth_smsdetl " + " where card_note =:card_note "
            + " and entry_mode_type =:entry_mode_type " + " and web3d_flag =:web3d_flag "
            + " and risk_type =:risk_type " + " and data_type = 'MCC1' ";
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");
    sqlSelect(sql1);
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  public int updateMcc1() {
    if (checkDtlMcc1()) {
      strSql = " update cca_auth_sms set " + " cond1_mcc='Y' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
          + " and entry_mode_type=:entry_mode_type " + " and web3d_flag=:web3d_flag "
          + " and risk_type=:risk_type";

    } else {
      strSql = " update cca_auth_sms set " + " cond1_mcc='N' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
          + " and entry_mode_type=:entry_mode_type " + " and web3d_flag=:web3d_flag "
          + " and risk_type=:risk_type";
    }
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5270");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  boolean checkDtlMcht1() {
    String sql1 =
        "select count(*) as db_cnt " + " from cca_auth_smsdetl " + " where card_note =:card_note "
            + " and entry_mode_type =:entry_mode_type " + " and web3d_flag =:web3d_flag "
            + " and risk_type =:risk_type " + " and data_type = 'MCHT1' ";
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");
    sqlSelect(sql1);
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  public int updateMcht1() {
    if (checkDtlMcht1()) {
      strSql = " update cca_auth_sms set " + " cond1_mcht='Y' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
          + " and entry_mode_type=:entry_mode_type " + " and web3d_flag=:web3d_flag "
          + " and risk_type=:risk_type";

    } else {
      strSql = " update cca_auth_sms set " + " cond1_mcht='N' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
          + " and entry_mode_type=:entry_mode_type " + " and web3d_flag=:web3d_flag "
          + " and risk_type=:risk_type";
    }
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5270");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  boolean checkDtlRisk1() {
    String sql1 =
        "select count(*) as db_cnt " + " from cca_auth_smsdetl " + " where card_note =:card_note "
            + " and entry_mode_type =:entry_mode_type " + " and web3d_flag =:web3d_flag "
            + " and risk_type =:risk_type " + " and data_type = 'RISK1' ";
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");
    sqlSelect(sql1);
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  public int updateRisk1() {
    if (checkDtlRisk1()) {
      strSql = " update cca_auth_sms set " + " cond1_risk='Y' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
          + " and entry_mode_type=:entry_mode_type " + " and web3d_flag=:web3d_flag "
          + " and risk_type=:risk_type";

    } else {
      strSql = " update cca_auth_sms set " + " cond1_risk='N' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
          + " and entry_mode_type=:entry_mode_type " + " and web3d_flag=:web3d_flag "
          + " and risk_type=:risk_type";
    }
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5270");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
