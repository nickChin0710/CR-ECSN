/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-12-16  V1.00.00  Ryan       program initial                            *
******************************************************************************/

package cycm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Cycm0200Func extends FuncEdit {


  public Cycm0200Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();

    if (rc != 1) {
      return rc;
    }
    strSql = "insert into cyc_rcrate_parm ("
     + " run_month "
     + ", run_day"
     + ", run_day2"
     + ", run_day3"
     + ", use_rcmonth"
     + ", acct_type_flag"
     + ", exclude_acct_type"
     + ", penalty_month"
     + ", penalty_month2"
     + ", crt_date, crt_user "
     + ", apr_date, apr_user "
     + ", mod_time, mod_user, mod_pgm, mod_seqno"
     + " ) values ("
     + " ?, ?, ?, ?, ? ,?"
     + ", to_char(sysdate,'yyyymmdd'), ? "
     + ", to_char(sysdate,'yyyymmdd'), ?"
     + ", sysdate,?,?,1"
     + " )";
     //-set ?value-
     Object[] param = new Object[] {
		dropMonth(), 
		wp.itemStr("run_day"), 
		wp.itemStr("run_day2"), 
		wp.itemStr("run_day3"), 
		strToInt(wp.itemStr("use_rcmonth")), 
		wp.itemStr("acct_type_flag"),
		wp.itemStr("exclude_acct_type"),
		wp.itemStr("penalty_month"), 
		wp.itemStr("penalty_month2"), 
		wp.loginUser,
		wp.itemStr("approval_user"), 
		wp.loginUser,
		wp.itemStr("mod_pgm") 
	};

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
    strSql = "update cyc_rcrate_parm set " 
    	+ " run_month=? "
        + " ,run_day =? "
        + " ,run_day2 =? "
        + " ,run_day3 =? "
        + " ,use_rcmonth =? "
        + " ,acct_type_flag =? "
        + " ,exclude_acct_type=?  "
        + " ,penalty_month =? "
        + " ,penalty_month2=? "
        + " ,apr_date=to_char(sysdate,'yyyymmdd'), apr_user=? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 ";
    Object[] param = new Object[] {
    	dropMonth()
    	, wp.itemStr("run_day")
     	, wp.itemStr("run_day2")
     	, wp.itemStr("run_day3")
    	, strToInt(wp.itemStr("use_rcmonth")) 
    	, wp.itemStr("acct_type_flag")
    	, wp.itemStr("exclude_acct_type")
    	, wp.itemStr("penalty_month")
    	, wp.itemStr("penalty_month2")
        , wp.itemStr("approval_user")
        , wp.loginUser
        , wp.itemStr("mod_pgm")};

    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    return rc;
  }


  private String dropMonth() {
    String buf = empty(wp.itemStr("mm1")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm2")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm3")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm4")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm5")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm6")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm7")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm8")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm9")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm10")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm11")) ? "N" : "Y";
    buf += empty(wp.itemStr("mm12")) ? "N" : "Y";


    return buf;
  }
}
