/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/02/28  V1.00.01   machao      Initial                              *
* 112/05/10  V1.00.02   Zuwei Su    程式調整：bug修訂                  *
* 112/05/11  V1.00.03   Zuwei Su    程式調整：覆核錯誤修訂                  *
* 112-06-06  V1.00.04   machao      活動群組增 ‘群組月累績最低消費金額’
***************************************************************************/
package mktp01;

import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0851Func extends busi.FuncProc {
  private final String PROGNAME = "行銷通路活動群組設定檔維護覆核112/02/28 V1.00.01";
  String approveTabName = "mkt_channelgp_parm";
  String controlTabName = "mkt_channelgp_parm_t";

  public Mktp0851Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " active_group_id, " + " active_group_desc, "
    		+ " sum_amt, "  + " feedback_type, " + " limit_amt, "
	        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, "
	        + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,"
	        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
	        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("active_group_id"), colStr("active_group_desc"), colStr("sum_amt"),
            colStr("feedback_type"), colStr("limit_amt"), wp.loginUser,
        colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser,
        colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String proc_tab_name = "";
    proc_tab_name = controlTabName;
    strSql = " select " + " active_group_id, " + " active_group_desc, " + " sum_amt, " 
            + " feedback_type, " + " limit_amt, " 
            + " apr_date, " + " apr_user, "
        + " crt_date, " + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + proc_tab_name + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    wp.colSet("sum_amt",wp.getValue("sum_amt"));
    if (sqlRowNum <= 0)
      errmsg(" 讀取 " + proc_tab_name + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String aprFlag = "Y";
    strSql = "update " + approveTabName + " set " + "active_group_desc = ?, "
    		+ " sum_amt = ?, " + " feedback_type = ?, " + " limit_amt = ?, " + "crt_user  = ?, "
	        + "crt_date  = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
	        + "mod_user  = ?, " + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
	        + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 "
	        + "and   active_group_id  = ? ";

    Object[] param = new Object[] {colStr("active_group_desc"),colStr("sum_amt"), colStr("feedback_type"), colStr("limit_amt"), colStr("crt_user"),
        colStr("crt_date"), wp.loginUser, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
        colStr("active_group_id")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and active_group_id = ? ";

    Object[] param = new Object[] {colStr("active_group_id")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + approveTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_channelgp_data " + "where 1 = 1 " + "and active_group_id  = ?  ";

    Object[] param = new Object[] {colStr("active_group_id"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_channelgp_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_channelgp_data_t " + "where 1 = 1 " + "and active_group_id  = ?  ";

    Object[] param = new Object[] {colStr("active_group_id"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_channelgp_data_T 錯誤");

    return 1;
  }
  
  public int dbDeleteP4TBndata() {
	    rc = dbSelectS4();
	    if (rc != 1)
	      return rc;
	    strSql = "delete mkt_channelgp_parm_t " + "where 1 = 1 " + "and active_group_id  = ?  ";

	    Object[] param = new Object[] {colStr("active_group_id"),};

	    sqlExec(strSql, param);
	    if (rc != 1)
	      errmsg("刪除 mkt_channelgp_parm_T 錯誤");

	    return 1;
	  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_channelgp_data " + "select * " + "from  mkt_channelgp_data_t "
        + "where 1 = 1 "  + "and active_group_id  = ?  ";

    Object[] param = new Object[] {colStr("active_group_id"),};

    sqlExec(strSql, param);

    return 1;
  }

  // ************************************************************************
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

}  // End of class
