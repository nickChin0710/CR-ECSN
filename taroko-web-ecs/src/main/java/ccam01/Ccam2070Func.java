package ccam01;
/** DEBIT卡臨時調整額度維護
 * 20-0102:    Alex  start date , end_date < sysdate can't update
 * 19-0611:    JH    p_seqno >>acno_p_xxx
 * V.2018-0505-JH
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * */
import busi.FuncAction;

public class Ccam2070Func extends FuncAction {
  String cardAcctIdx = "", lsStartDate = "", lsEndDate = "";

  @Override
  public void dataCheck() {
    cardAcctIdx = wp.itemStr("card_acct_idx");
    lsStartDate = wp.itemStr("adj_eff_date1");
    lsEndDate = wp.itemStr("adj_eff_date2");

    String lsOriStartDate = "", lsOriEndDate = "";

    lsOriStartDate = wp.itemStr("ori_eff_date1");
    lsOriEndDate = wp.itemStr("ori_eff_date2");

    if (wp.itemEmpty("adj_area")) {
      errmsg("適用地區別不可空白 !");
      return;
    }
    if (wp.itemEmpty("adj_reason")) {
      errmsg("調高原因不可空白 !");
      return;
    }
    if (wp.itemNum("tot_amt_month") <= 0) {
      errmsg("臨調放大比率不可小於等於零 !");
      return;
    }

    if (empty(lsStartDate) || empty(lsEndDate)) {
      errmsg("有效日期 : 不可空白");
      return;
    }

    if (this.chkStrend(lsStartDate, lsEndDate) == -1) {
      errmsg("有效日期  起迄 錯誤");
      return;
    }
    if (lsEndDate.compareTo(this.getSysDate()) < 0) {
      errmsg("有效日期(迄) 須大於等於 系統日期");
      return;
    }
    // if(wp.item_empty("chg")||!eq_igno(ls_start_date,ls_ori_start_date)||!eq_igno(ls_end_date,ls_ori_end_date)
    // ){
    // if (ls_start_date.compareTo(this.get_sysDate())<0
    // ||ls_end_date.compareTo(this.get_sysDate())<0) {
    // errmsg("有效日期 起迄 須大於等於 系統日期");
    // return;
    // }
    // }



    selectCcaCardAcct();
    if (rc != 1)
      return;
  }

  void selectCcaCardAcct() {
    strSql = "select mod_seqno" + " from cca_card_acct" + " where card_acct_idx =?";
    setDouble2(1, this.strToNum(cardAcctIdx));

    daoTid = "A.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      sqlErr("cca_card_acct.select");
      return;
    }

    if (colNum("A.mod_seqno") != wp.itemNum("mod_seqno")) {
      errmsg(errOtherModify);
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
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "update CCA_CARD_ACCT set " + "adj_eff_start_date=:adj_eff_date1,"
        + "adj_eff_end_date=:adj_eff_date2," + "adj_reason=:adj_reason," + "adj_remark=:adj_remark,"
        + "tot_amt_month=:tot_amt_month," + "adj_area=:adj_area," + "adj_quota='Y'," + "adj_date="
        + commSqlStr.sysYYmd + "," + "adj_time=" + commSqlStr.sysTime + "," + "adj_user=:adj_user,"
        + "apr_user=:apr_user," + commSqlStr.setModxxx(modUser, modPgm) + " where card_acct_idx=:cardAcctIdx";

    item2ParmStr("adj_eff_date1");
    item2ParmStr("adj_eff_date2");
    setString2("adj_area", wp.itemStr2("adj_area"));
    item2ParmStr("adj_reason");
    item2ParmStr("adj_remark");
    item2ParmNum("tot_amt_month");
    item2ParmStr("adj_remark");
    setString2("adj_user", modUser);
    setString2("apr_user", wp.itemStr2("apr_user"));

    setDouble2("cardAcctIdx", strToNum(cardAcctIdx));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      sqlErr("cca_card_acct.Update");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    cardAcctIdx = wp.itemStr2("card_act_idx");
    dataCheck();
    strSql = "update CCA_CARD_ACCT set " + "adj_eff_start_date=''," + "adj_area='',"
        + "adj_eff_end_date=''," + "adj_reason=''," + "adj_remark=''," + "tot_amt_month=0,"
        + "adj_quota='N'," + commSqlStr.setModxxx(modUser, modPgm) + " where card_acct_idx =:cardAcctIdx";

    setDouble2("cardAcctIdx", strToNum(cardAcctIdx));
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      sqlErr("cca_card_acct.Update(delete)");
    }
    dbDeleteAdjParm();

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int dbDeleteAdjParm() {
    msgOK();
    cardAcctIdx = wp.itemStr2("card_acct_idx");
    strSql = "Delete CCA_DEBIT_ADJ_PARM" + " where card_acct_idx =:cardAcctIdx";
    setDouble2("cardAcctIdx", strToNum(cardAcctIdx));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("Delete CCA_DEBIT_ADJ_PARM err; " + sqlErrtext);
    }

    return rc;
  }

  public int dbInsertAdjParm() {
    msgOK();

    cardAcctIdx = wp.itemStr2("card_acct_idx");

    strSql = "insert into CCA_DEBIT_ADJ_PARM (" + " card_acct_idx, " + " risk_type, "
        + " cnt_amt_pct, " + " day_amt_pct," + " day_cnt_pct," + " month_amt_pct,"
        + " month_cnt_pct," + " mod_user," + " mod_time" + " ) values (" + " :cardAcctIdx," + " :risk_type,"
        + " :cnt_amt_pct," + " :day_amt_pct," + " :day_cnt_pct," + " :month_amt_pct,"
        + " :month_cnt_pct, " + " :mod_user, " + " sysdate" + " )";
    // -set ?value-
    setDouble2("cardAcctIdx", strToNum(cardAcctIdx));
    var2ParmStr("risk_type");
    var2ParmNum("cnt_amt_pct");
    var2ParmNum("day_amt_pct");
    var2ParmNum("day_cnt_pct");
    var2ParmNum("month_amt_pct");
    var2ParmNum("month_cnt_pct");
    setString("mod_user", modUser);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      sqlErr("cca_debit_adj_parm.Add");
    }
    return rc;
  }

  // public int del_CCA_ADJ_PARM() {
  // msgOK();
  // cardAcctIdx = wp.item_ss("p_seqno");
  // is_sql = "Delete CCA_ADJ_PARM"
  // + " where p_seqno =:cardAcctIdx"
  // + " and debit_flag='Y'";
  // setString("cardAcctIdx", cardAcctIdx);
  // sqlExec(is_sql);
  // if (sql_nrow < 0) {
  // errmsg("Delete CCA_ADJ_PARM err; " + getMsg());
  // rc = -1;
  // }
  // else
  // rc = 1;
  //
  // return rc;
  // }

}
