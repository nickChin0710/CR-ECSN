/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/10  V1.00.00   Machao      Initial                              *
* 112/03/13  V1.00.01   Zuwei Su      修改已覆核資料已覆核资料未删除                              *
***************************************************************************/
package mktp02;

import taroko.com.TarokoCommon;

public class Mktp6210Func extends busi.FuncProc {
	String approveTabName = "mkt_chantype_parm";
	String controlTabName = "mkt_chantype_parm_t";

	public Mktp6210Func(TarokoCommon wr) {
	    wp = wr;
	    this.conn = wp.getConn();
	  }
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
	
	public int dbInsertA4() {
	    rc = dbSelectS4();
	    if (rc != 1)
	      return rc;
      strSql = " insert into  "
              + approveTabName
              + " ("
              + " channel_type_id, "
              + " channel_type_desc, "
              + " apr_date, "
              + " apr_user, "
              + " crt_date, "
              + " crt_user, "
              + " mod_time, "
              + " mod_user, "
              + " mod_seqno, "
              + " mod_pgm "
              + " ) values ("
              + "?,?,"
              + "to_char(sysdate,'yyyymmdd'),"
              + "?,"
              + "?,"
              + "?,"
              + " timestamp_format(?,'yyyymmddhh24miss'), "
              + "?,"
              + "?,"
              + " ?) ";

      Object[] param = new Object[] {
              colStr("channel_type_id"),
              colStr("channel_type_desc"),
              wp.loginUser,
              colStr("crt_date"),
              colStr("crt_user"),
              wp.sysDate + wp.sysTime,
              wp.loginUser,
              colStr("mod_seqno"),
              wp.modPgm()
      };

	    sqlExec(strSql, param);

	    return rc;
	  }
	
	 // ************************************************************************
	  public int dbUpdateU4() {
	    rc = dbSelectS4();
	    if (rc != 1)
	      return rc;
      strSql = "update "
              + approveTabName
              + " set "
              + "channel_type_desc = ?, "
              + "crt_user  = ?, "
              + "crt_date  = ?, "
              + "apr_user  = ?, "
              + "apr_date  = to_char(sysdate,'yyyymmdd'), "
              + "mod_user  = ?, "
              + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
              + "mod_pgm   = ?, "
              + "mod_seqno = nvl(mod_seqno,0)+1 "
              + "where 1     = 1 "
              + "and   channel_type_id  = ? ";

      Object[] param = new Object[] {
              colStr("channel_type_desc"),
              colStr("crt_user"),
              colStr("crt_date"),
              wp.loginUser,
              colStr("mod_user"),
              colStr("mod_time"),
              colStr("mod_pgm"),
              colStr("channel_type_id")
      };

	    sqlExec(strSql, param);
	    if (sqlRowNum <= 0)
	      rc = 0;
	    else
	      rc = 1;

	    return rc;
	  }

	  // ************************************************************************

	public int dbSelectS4() {
	    String proc_tab_name = "";
	    proc_tab_name = controlTabName;
	    strSql = " select " + " channel_type_id, " + " channel_type_desc, " + " aud_type, " + " apr_date, " + " apr_user, "
	        + " crt_date, " + " crt_user, "
	        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
	        + proc_tab_name + " where rowid = ? ";

	    Object[] param = new Object[] {wp.itemRowId("wprowid")};

	    sqlSelect(strSql, param);
	    if (sqlRowNum <= 0)
	      errmsg(" 讀取 " + proc_tab_name + " 錯誤");

	    return rc;
	  }
	
	 // ************************************************************************
	  public int dbInsertA4Bndata() {
	    rc = dbSelectS4();
	    if (rc != 1)
	      return rc;
	    strSql = "insert into mkt_chantype_data " + "select * " + "from  mkt_chantype_data_t "
	        + "where 1 = 1 "  + "and channel_type_id  = ?  ";

	    Object[] param = new Object[] {colStr("channel_type_id"),};

	    sqlExec(strSql, param);

	    return 1;
	  }
	  
	  // ************************************************************************
	  public int dbDeleteD4TBndata() {
	    rc = dbSelectS4();
	    if (rc != 1)
	      return rc;
	    strSql = "delete mkt_chantype_data_t " + "where 1 = 1 " + "and channel_type_id  = ?  ";

	    Object[] param = new Object[] {colStr("channel_type_id"),};

	    sqlExec(strSql, param);
	    if (rc != 1)
	      errmsg("刪除 mkt_chantype_data_t 錯誤");

	    return 1;
	  }

	  // ************************************************************************
	  public int dbDeleteD4Bndata() {
		    rc = dbSelectS4();
		    if (rc != 1)
		      return rc;
		    strSql = "delete mkt_chantype_data " + "where 1 = 1 " + "and channel_type_id  = ?  ";

		    Object[] param = new Object[] {colStr("channel_type_id"),};

		    sqlExec(strSql, param);
		    if (rc != 1)
		      errmsg("刪除 mkt_chantype_data 錯誤");

		    return 1;
		  }

		  // ************************************************************************
	  
//	  public int dbDeleteP4TBndata() {
//		    rc = dbSelectS4();
//		    if (rc != 1)
//		      return rc;
//		    strSql = "delete mkt_chantype_parm_t " + "where 1 = 1 " + "and channel_type_id  = ?  ";
//
//		    Object[] param = new Object[] {colStr("channel_type_id"),};
//
//		    sqlExec(strSql, param);
//		    if (rc != 1)
//		      errmsg("刪除 mkt_chantype_parm_t 錯誤");
//
//		    return 1;
//		  }

	  // ************************************************************************
	@Override
	public void dataCheck() {
		// TODO Auto-generated method stub
		
	}
	 // ************************************************************************
	  public int dbDeleteD4() {
	    rc = dbSelectS4();
	    if (rc != 1)
	      return rc;
	    strSql = "delete from " + approveTabName + " " + "where 1 = 1 " + "and channel_type_id = ? ";

	    Object[] param = new Object[] 
	    		{
	    		colStr("channel_type_id")
	    		};

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
	  public int dbDelete() {
          strSql = "delete " + controlTabName + " " + "where rowid = ?";

          Object[] param = new Object[] {
                  wp.itemRowId("wprowid")
          };

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

	@Override
	public int dataProc() {
		// TODO Auto-generated method stub
		return 0;
	}

}
