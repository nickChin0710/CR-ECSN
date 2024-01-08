/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/07/03  V1.00.01   machao                   Initial                       *
***************************************************************************/
package mktp01;

import taroko.com.TarokoCommon;

public class Mktp0930Func extends busi.FuncProc{
	private final String PROGNAME = "數位帳戶VD卡活動回饋參數覆核112/07/03 V1.00.01";
	  String approveTabName = "mkt_digacctvd_parm";
	  String controlTabName = "mkt_digacctvd_parm_t";

	  public Mktp0930Func(TarokoCommon wr) {
	    wp = wr;
	    this.conn = wp.getConn();
	  }

	  // ************************************************************************
	  @Override
	  public int querySelect() {
	    // TODO Auto-generated method
	    return 0;
	  }

	  // ************************************************************************
	  @Override
	  public int dataSelect() {
	    // TODO Auto-generated method stub
	    return 1;
	  }

	  // ************************************************************************
	  @Override
	  public void dataCheck() {}

	  // ************************************************************************
	  @Override
	  public int dataProc() {
	    return rc;
	  }

	  // ************************************************************************
	  public int dbInsertA4() {
	    rc = dbSelectS4();
	    if (rc != 1)
	      return rc;
	    strSql = " insert into  " + approveTabName + " (" + " active_code, " + " active_name, " + " active_date_s, "
	    	+ " active_date_e, " + " feedback_cycle, "  + " feedback_dd, "
	        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, "
	        + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,"
	        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
	        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

	    Object[] param = new Object[] {colStr("active_code"), colStr("active_name"),colStr("active_date_s"), colStr("active_date_e"),
	    		 colStr("feedback_cycle"), colStr("feedback_dd"), wp.loginUser,
		         colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser,
		         colStr("mod_seqno"), wp.modPgm()};

	    sqlExec(strSql, param);

	    return rc;
	  }

	  // ************************************************************************
	  public int dbSelectS4() {
	    String proc_tab_name = "";
	    proc_tab_name = controlTabName;
	    strSql = " select " + " active_code, " + " active_name, " + " active_date_s, " + " active_date_e, " + " feedback_cycle, " + " feedback_dd, "
		        + " apr_date, " + " apr_user, "
		        + " crt_date, " + " crt_user, "
		        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
	        + proc_tab_name + " where rowid = ? ";

	    Object[] param = new Object[] {wp.itemRowId("wprowid")};

	    sqlSelect(strSql, param);
	    rc = 1;
	    if (sqlRowNum <= 0) {
	        rc = 0;
	      errmsg(" 讀取 " + proc_tab_name + " 錯誤");
	    }

	    return rc;
	  }

	  // ************************************************************************
	  public int dbUpdateU4() {
	    rc = dbSelectS4();
	    if (rc != 1)
	      return rc;
	    String aprFlag = "Y";
	    strSql = "update " + approveTabName + " set " + "active_name = ?, " + " active_date_s = ?, " + " active_date_e = ?, "
		    + " feedback_cycle = ?, " + " feedback_dd = ?, " + "crt_user  = ?, "
	        + "crt_date  = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
	        + "mod_user  = ?, " + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
	        + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 "
	        + "and   active_code  = ? ";

	    Object[] param = new Object[] {colStr("active_name"), colStr("active_date_s"), colStr("active_date_e"),
	    	colStr("feedback_cycle"), colStr("feedback_dd"), colStr("crt_user"),
	        colStr("crt_date"), wp.loginUser, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
	        colStr("active_code")};

	    sqlExec(strSql, param);
	    if (sqlRowNum <= 0)
	      rc = 0;
	    else
	      rc = 1;

	    return rc;
	  }

	  // ************************************************************************
	  public int dbDeleteD4() {
	    rc = dbSelectS4();
	    if (rc != 1)
	      return rc;
	    strSql = "delete " + controlTabName + " " + "where 1 = 1 " + "and active_code = ? ";

	    Object[] param = new Object[] {colStr("active_code")};

	    sqlExec(strSql, param);
	    if (sqlRowNum <= 0)
	      rc = 0;
	    else
	      rc = 1;
	    if (rc != 1)
	      errmsg("刪除 " + controlTabName + " 錯誤");

	    return rc;
	  }


	  public int dbDelete() {
	    strSql = "delete " + controlTabName + " " + "where rowid = ?";

	    Object[] param = new Object[] {wp.itemRowId("wprowid")};

	    sqlExec(strSql, param);
	    if (sqlRowNum <= 0)
	      rc = 0;
	    else
	      rc = 1;
	    if (sqlRowNum <= 0)
	      errmsg("刪除 " + controlTabName + " 錯誤");

	    return rc;
	  }
	  // ************************************************************************
}
