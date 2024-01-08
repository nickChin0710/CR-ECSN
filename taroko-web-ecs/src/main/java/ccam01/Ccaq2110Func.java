package ccam01;
/**
 * 19-0611:    JH    p_xxx >>acno_p_xxx
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
* */
import busi.FuncAction;

public class Ccaq2110Func extends FuncAction {

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
    if (rc != 1) {
      return rc;
    }

    strSql = "update cca_card_acct set " + " auth_remark =:auth_remark ,"
        + " sms_cell_phone =:sms_cell_phone ," + " mod_user =:mod_user ," + " mod_time =sysdate ,"
        + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where card_acct_idx =:kk_ccas_idx ";
    item2ParmStr("auth_remark");
    item2ParmStr("sms_cell_phone");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccaq2110");
    setDouble2("kk_ccas_idx", wp.itemNum("card_acct_idx"));

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    }
    insertLog();
    return rc;
  }

  public int insertLog() {
    msgOK();
    strSql = "insert into cca_auth_remark_log (" + " card_acct_idx ," + " chg_date ,"
        + " chg_time ," + " user_deptno ," + " chg_user ," + " auth_remark " + " ) values ("
        + " :card_acct_idx ," + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ,"
        + " :user_deptno ," + " :chg_user ," + " :auth_remark " + " )";
    item2ParmStr("card_acct_idx");
    setString("chg_user", wp.loginUser);
    item2ParmStr("auth_remark");
    setString("user_deptno", userDeptNo());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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
