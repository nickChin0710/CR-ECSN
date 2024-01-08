/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/04/21  V1.00.01   machao      Initial                              *
* 112/05/05  V1.00.02   Zuwei Su      dbSelectS4 查詢有資料返回錯誤rc                              *
* 112/05/06  V1.00.03   Zuwei Su      點選1筆覆核後detail list消失不見     *
***************************************************************************/
package mktp01;

import taroko.com.TarokoCommon;

public class Mktp0900Func extends busi.FuncProc{
	private final String PROGNAME = "稅務活動回饋參數覆核112/04/21 V1.00.01 ";
	  String approveTabName = "mkt_tax_parm";
	  String controlTabName = "mkt_tax_parm_t";

	  public Mktp0900Func(TarokoCommon wr) {
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
	    strSql = " insert into  " + approveTabName + " (" + " active_code, " + " active_name, " + " active_type, "
	    	+ " purchase_date_s, " + " purchase_date_e, " + " feedback_all_totcnt, " + " feedback_emp_totcnt, " + " feedback_nonemp_totcnt, "
	    	+ " feedback_peremp_cnt, " + " feedback_pernonemp_cnt, " + " purchase_amt_s, " + " purchase_amt_e, " + " feedback_id_type, "
	    	+ " gift_type, " + " cal_def_date, "
	        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, "
	        + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
	        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
	        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

	    Object[] param = new Object[] {colStr("active_code"), colStr("active_name"), colStr("active_type"),
	    		 colStr("purchase_date_s"), colStr("purchase_date_e"), colNum("feedback_all_totcnt"),colNum("feedback_emp_totcnt"),
	    		 colNum("feedback_nonemp_totcnt"), colNum("feedback_peremp_cnt"), colNum("feedback_pernonemp_cnt"), colNum("purchase_amt_s"),
	    		 colNum("purchase_amt_e"), colStr("feedback_id_type"), colStr("gift_type"), colStr("cal_def_date"),wp.loginUser,
	        colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser,
	        colStr("mod_seqno"), wp.modPgm()};

	    sqlExec(strSql, param);

	    return rc;
	  }

	  // ************************************************************************
	  public int dbSelectS4() {
	    String proc_tab_name = "";
	    proc_tab_name = controlTabName;
	    strSql = " select " + " active_code, " + " active_name, "+ " active_type, "
		    	+ " purchase_date_s, " + " purchase_date_e, " + " feedback_all_totcnt, " + " feedback_emp_totcnt, " + " feedback_nonemp_totcnt, "
		    	+ " feedback_peremp_cnt, " + " feedback_pernonemp_cnt, " + " purchase_amt_s, " + " purchase_amt_e, " + " feedback_id_type, "
		    	+ " gift_type, " + " cal_def_date, " + " apr_date, " + " apr_user, "
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
	    strSql = "update " + approveTabName + " set " + "active_name = ?, " + "active_type = ?, "
	    	+ " purchase_date_s = ?, " + " purchase_date_e = ?, " + " feedback_all_totcnt = ?, " + " feedback_emp_totcnt = ?, "
	    	+ " feedback_nonemp_totcnt = ?, " + " feedback_peremp_cnt = ?, " + " feedback_pernonemp_cnt = ?, " 
	    	+ " purchase_amt_s = ?, " + " purchase_amt_e = ?, " + " feedback_id_type = ?, "
		    + " gift_type = ?, " + " cal_def_date = ?, " + "crt_user  = ?, "
	        + "crt_date  = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
	        + "mod_user  = ?, " + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
	        + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 "
	        + "and   active_code  = ? ";

	    Object[] param = new Object[] {colStr("active_name"), colStr("active_type"),
	    	colStr("purchase_date_s"), colStr("purchase_date_e"), colNum("feedback_all_totcnt"), colNum("feedback_emp_totcnt"),
	    	colNum("feedback_nonemp_totcnt"), colNum("feedback_peremp_cnt"), colNum("feedback_pernonemp_cnt"), colNum("purchase_amt_s"),
	    	colNum("purchase_amt_e"), colStr("feedback_id_type"), colStr("gift_type"), colStr("cal_def_date"), colStr("crt_user"),
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
