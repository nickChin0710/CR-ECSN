/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名
* 110-10-21  V1.00.03   machao        SQL Injection修改                                                                            * 
******************************************************************************/
package colm05;

import busi.FuncEdit;

public class Colm5710Func extends FuncEdit {
  String paramType = "1", acctType = "", validDate = "";

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
      acctType = wp.itemStr("kk_acct_type");
      validDate = wp.itemStr("kk_valid_date");
    } else {
      acctType = wp.itemStr("acct_type");
      validDate = wp.itemStr("valid_date");
    }

    if(empty(acctType)) {
    	errmsg("帳戶類別: 不可空白");
    	return ;
    }
    
    if(empty(validDate)) {
    	errmsg("生效日期: 不可空白");
    	return ;
    }
    
    if(this.ibAdd) {    	
    	if (this.chkStrend(this.getSysDate(), validDate) == -1) {    		
            errmsg("生效日期不可小於系統日 !");
            return;
        }
    	return ;
    }    	
         
    if (isDelete() && wp.itemEmpty("exec_date") == false) {
      errmsg("參數曾執行過, 不可刪除");
      return;
    }

    if (eqIgno(wp.itemStr("apr_flag"), "Y")) {
      errmsg("已放行，不可修改及刪除，需先請主管維護[取消放行]後再修改。");
      return;
    }

//  sqlWhere = " where param_type='" + paramType + "'" + " and acct_type='" + acctType + "'"
//  + " and valid_date='" + validDate + "'" + " and nvl(mod_seqno,0) =" + wp.modSeqno();
//

    sqlWhere = "where param_type = ? and acct_Type = ? and valid_date = ? and nvl(mod_seqno,0) = ? ";
    
    Object[] parms = new Object[] {paramType, acctType,validDate, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("ptr_stopparam", sqlWhere, parms)) {
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

    strSql = "insert into ptr_stopparam (" + " param_type, " // 1
        + " acct_type, " + " valid_date, " + " exec_mode, " + " exec_day, " + " exec_cycle_nday, "
        + " exec_date, " + " n0_month, " + " n1_cycle, " + " mcode_value, " + " debt_amt, "
        + " non_af, " + " non_ri, " + " non_pn, " + " non_pf, " + " non_lf, " + " apr_flag, "
        + " pause_flag " // 18
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?,?,?,'2',?,?,?,?,?,?,?,?,?,?,?,?,'N','N' " + ",sysdate,?,?,1" + " )";
    Object[] param = new Object[] {paramType, acctType, validDate, wp.itemNum("exec_day"),
        wp.itemNum("exec_cycle_nday"), wp.itemStr("exec_date"), wp.itemNum("n0_month"),
        wp.itemNum("n1_cycle"), wp.itemNum("mcode_value"), wp.itemNum("debt_amt"),
        wp.itemStr("non_af"), wp.itemStr("non_ri"), wp.itemStr("non_pn"), wp.itemStr("non_pf"),
        wp.itemStr("non_lf"), wp.loginUser, wp.itemStr("mod_pgm")};
    this.log("kk1=" + paramType);
    sqlExec(strSql, param);
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
    strSql = "update ptr_stopparam set " + " exec_mode ='2', " + " exec_day =?, "
        + " exec_cycle_nday =?, " + " exec_date =?, " + " n0_month =?, " + " n1_cycle =?, "
        + " mcode_value =?, " + " debt_amt =?, " + " non_af =?, " + " non_ri =?, " + " non_pn =?, "
        + " non_pf =?, " + " non_lf =?, " + " mod_user =?, mod_time=sysdate, mod_pgm =? "
        + ", mod_seqno =nvl(mod_seqno,0)+1 where param_type = ? and acct_Type = ? and valid_date = ? and nvl(mod_seqno,0) = ?  " ;
    log("sql-where=" + sqlWhere);
    Object[] param = new Object[] {wp.itemNum("exec_day"), wp.itemNum("exec_cycle_nday"),
        wp.itemStr("exec_date"), wp.itemNum("n0_month"), wp.itemNum("n1_cycle"),
        wp.itemNum("mcode_value"), wp.itemStr("debt_amt"), wp.itemStr("non_af"),
        wp.itemStr("non_ri"), wp.itemStr("non_pn"), wp.itemStr("non_pf"), wp.itemStr("non_lf"),
        wp.loginUser, wp.itemStr("mod_pgm"),paramType, acctType,validDate, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql, param);
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
    strSql = "delete ptr_stopparam where param_type = ? and acct_Type = ? and valid_date = ? and nvl(mod_seqno,0) = ?  ";
    Object[] param = new Object[] {paramType, acctType,validDate, wp.itemNum("mod_seqno")};
    log("del-sql=" + strSql);
    rc = sqlExec(strSql,param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }
}
