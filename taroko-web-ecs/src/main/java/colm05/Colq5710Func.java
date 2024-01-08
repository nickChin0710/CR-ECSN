package colm05;
/** 19-0617:   JH    p_xxx >>acno_p_xxx
 *  V.2018-0605.JH
 *  109-05-06  V1.00.02  Tanwei       updated for project coding standard
 * */
import busi.FuncQuery;

public class Colq5710Func extends FuncQuery {

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    msgOK();
    dataCheck();
    if (rc == -1) {
      return rc;
    }

    String lsAcctType = varsStr("ex_acct_type");
    String lsStopDate1 = wp.itemStr2("ex_stop_date1");
    String lsStopDate2 = wp.itemNvl("ex_stop_date2", "99991231");

    double lmCnt = 0;
    double lmBal = 0;

    // -排除MAX(stop_date2)-
    if (eqIgno(lsStopDate2, "99991231") == false && !empty(lsStopDate2)) {
      strSql = "select count(*) as db_cnt" + ", nvl(sum(nvl(acct_jrnl_bal),0) as db_bal"
          + " from act_acct" + " where p_seqno in (" + " select acno_p_seqno from rsk_acnolog"
          + " where log_date between :ex_date1 and :ex_date2 and log_type='2' and kind_flag='A' and log_not_reason=''"
          + " and acct_type =:ex_acct_type"
          + " and acno_p_seqno in ( select acno_p_seqno from rsk_acnolog where log_date >:ex_date3"
          + " and log_type='2' and kind_flag='A' and log_not_reason=''"
          + " and acct_type =:ex_acct_type2 ) )";

      setString2("ex_date1", lsStopDate2);
      setString2("ex_date2", lsStopDate2);
      setString2("ex_acct_type", lsAcctType);
      setString2("ex_date3", lsStopDate2);
      setString2("ex_acct_type2", lsAcctType);

      this.sqlSelect(strSql);
      if (sqlRowNum > 0) {
        lmCnt += colNum("db_cnt");
        lmBal += colNum("db_bal");
      }
    }

    // -排除 act_acno.stop_status<>'Y'-
    strSql = "select count(*) as db_cnt2, " + " nvl(sum(acct_jrnl_bal),0) as db_bal2 "
        + " from act_acct " + " where p_seqno in ( "
        + " select A.p_seqno from act_acno B, rsk_acnolog A " + " where B.p_seqno = A.acno_p_seqno "
        + " and   A.acct_type =:ex_acct_type "
        + " and   A.log_date between :ex_date1 and :ex_date2 "
        + " and   A.log_type ='2' and A.kind_flag ='A' " + " and   A.log_not_reason ='' "
        + " and   B.stop_status <>'Y' " + " )";

    setString2("ex_acct_type", lsAcctType);
    setString2("ex_date1", lsStopDate1);
    setString2("ex_date2", lsStopDate2);

    this.sqlSelect(strSql);
    if (sqlRowNum > 0) {
      lmCnt += colNum("db_cnt2");
      lmBal += colNum("db_bal2");
    }
    this.varsSet("db_cnt", "" + lmCnt);
    this.varsSet("db_bal", "" + lmBal);

    return 1;
  }

  @Override
  public void dataCheck() {
    if (varEmpty("ex_acct_type")) {
      errmsg("帳戶類別: 不可空白");
      return;
    }
  }

}
