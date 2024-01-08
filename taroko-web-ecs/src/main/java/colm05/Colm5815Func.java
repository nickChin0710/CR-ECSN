/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package colm05;

import busi.FuncAction;

public class Colm5815Func extends FuncAction {
  String acctType = "", validDate = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      acctType = wp.itemStr("kk_acct_type");
      validDate = wp.itemStr("kk_valid_date");
    } else {
      acctType = wp.itemStr("acct_type");
      validDate = wp.itemStr("valid_date");
    }
    
    if(isEmpty(acctType)) {
    	errmsg("帳戶類別: 不可空白");
    	return ;
    }
    
    if(isEmpty(validDate)) {
    	errmsg("生效日期: 不可空白");
    	return ;
    }
    
    if (!this.ibAdd) {
      if (eqIgno(wp.itemStr("apr_flag"), "Y")) {
        errmsg("已放行，不可修改及刪除，需先請主管維護[取消放行]後再修改。");
        return;
      }
    }

    if (this.ibDelete && !empty(wp.itemStr("exec_date"))) {
      errmsg("參數已執行過, 不可刪除");
      return;
    }

    if (this.ibAdd) {
      if (this.chkStrend(this.getSysDate(), validDate) == -1) {
        errmsg("生效日期需大於系統日期");
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

    strSql = " insert into ptr_blockparam ( " + " param_type ," + " acct_type ," + " valid_date ,"
        + " exec_mode ," + " apr_flag ," + " pause_flag ," + " exec_day ," + " exec_cycle_nday ,"
        + " exec_date ," + " n0_month ," + " n1_cycle ," + " mcode_value1 ," + " debt_amt1 ,"
        + " mcode_value2 ," + " debt_amt2 ," + " mcode_value3 ," + " debt_amt3 ," + " mod_user ,"
        + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ( " + " '2' , " + " :kk1 , "
        + " :kk2 , " + " '3' , " + " 'N' , " + " 'N' , " + " :exec_day ," + " :exec_cycle_nday ,"
        + " :exec_date ," + " :n0_month ," + " :n1_cycle ," + " :mcode_value1 ," + " :debt_amt1 ,"
        + " :mcode_value2 ," + " :debt_amt2 ," + " :mcode_value3 ," + " :debt_amt3 ,"
        + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 " + " )";
    setString("kk1", acctType);
    setString("kk2", validDate);
    item2ParmNum("exec_day");
    item2ParmNum("exec_cycle_nday");
    item2ParmStr("exec_date");
    item2ParmNum("n0_month");
    item2ParmNum("n1_cycle");
    item2ParmNum("mcode_value1");
    item2ParmNum("debt_amt1");
    item2ParmNum("mcode_value2");
    item2ParmNum("debt_amt2");
    item2ParmNum("mcode_value3");
    item2ParmNum("debt_amt3");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "colm5815");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert ptr_blockparm error !");
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

    strSql = " update ptr_blockparam set " + " exec_day =:exec_day ,"
        + " exec_cycle_nday =:exec_cycle_nday ," + " exec_date =:exec_date ,"
        + " n0_month =:n0_month ," + " n1_cycle =:n1_cycle ," + " mcode_value1 =:mcode_value1 ,"
        + " debt_amt1 =:debt_amt1 ," + " mcode_value2 =:mcode_value2 ," + " debt_amt2 =:debt_amt2 ,"
        + " mcode_value3 =:mcode_value3 ," + " debt_amt3 =:debt_amt3 ," + " mod_user =:mod_user ,"
        + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ," + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where 1=1 " + " and param_type = '2' " + " and exec_mode = '3' "
        + " and acct_type =:kk1 " + " and valid_date =:kk2 ";
    item2ParmNum("exec_day");
    item2ParmNum("exec_cycle_nday");
    item2ParmStr("exec_date");
    item2ParmNum("n0_month");
    item2ParmNum("n1_cycle");
    item2ParmNum("mcode_value1");
    item2ParmNum("debt_amt1");
    item2ParmNum("mcode_value2");
    item2ParmNum("debt_amt2");
    item2ParmNum("mcode_value3");
    item2ParmNum("debt_amt3");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "colm5812");
    setString("kk1", acctType);
    setString("kk2", validDate);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ptr_blockparam error !");
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

    strSql = " delete ptr_blockparam " + " where 1=1 " + " and param_type = '2' "
        + " and exec_mode = '3' " + " and acct_type =:kk1 " + " and valid_date =:kk2 ";

    setString("kk1", acctType);
    setString("kk2", validDate);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete ptr_blockparam error !");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
