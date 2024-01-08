/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-07-05  V1.00.01  machao     新增功能：合庫金控子公司推廣單位維護處理程式              *                         *
* 111-12-19  V1.00.02  Zuwei Su   新增存檔,其中APR_FLAG=N, APR_DATE=空白、 APR_USER=空白             *                         *
******************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Mktm0540Func extends FuncEdit {
	private final String PROGNAME = "合庫金控子公司推廣單位維護處理程式111-12-19 V1.00.02";
	  String kk1;
	  String controlTabName = "mkt_office_d";
//	  String orgTabName = "mkt_office_m";
	public Mktm0540Func(TarokoCommon wr) {
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

	@Override
	public void dataCheck() {	
		String lsSql = "select office_m_code from mkt_office_m " + "where corp_no =:corp_no ";
		setString("corp_no", wp.colStr("ex_corp_no"));
		sqlSelect(lsSql);
		if((this.ibDelete)|| (this.ibUpdate)) {
			if(wp.itemStr("apr_flag").equals("Y")) {
				errmsg("資料己覆核,不可異動");
			}
		}
		if((this.ibAdd)|| (this.ibUpdate)) {
			if(wp.itemEmpty("ex_corp_no") || wp.itemEmpty("office_code")
					|| wp.itemEmpty("office_name") || wp.itemEmpty("brokerage_flag")) {
				wp.colSet("ex_office_m_code_pink", "pink");
				wp.colSet("ex_corp_no_pink", "pink");
				wp.colSet("office_name_pink", "pink");
				wp.colSet("brokerage_flag_pink", "pink");
				errmsg("必輸欄位請輸入資料");
			}
			if(wp.itemStr("office_code") == wp.itemStr("corp_no")) {
				errmsg("關係企業編號、推廣單位編號不可相同");
			}
			
			if(wp.itemStr2("office_code") == wp.getValue("office_code") ) {
				String code2 = wp.getValue("office_m_code");
				if (colStr("office_m_code") == wp.getValue("office_m_code")) {
					errmsg("資料重覆,請重新輸入");  
				}  
	          }
		}
		

	}

	@Override
	public int dbInsert() {
		actionInit("A");
		  dataCheck();
		  if (rc!=1) return rc;


		  strSql= " insert into  " + controlTabName+ " ("
				  + "office_m_code, "
		          + " corp_no, "
		          + " office_code, "
		          + " office_name,"
		          + " brokerage_flag,"
		          + " apr_flag, "
		          + " apr_date, "
		          + " apr_user, "
		          + " crt_date, "
		          + " crt_user, "
		          + " mod_seqno, "
		          + " mod_time,mod_user,mod_pgm "
		          + " ) values ("
		          + "?,?,?,?,?,'N',"
		          + "'',"
		          + "'',"
		          + "to_char(sysdate,'yyyymmdd'),"
		          + "?,"
		          + "?,"
		          + "sysdate,?,?)";

		  Object[] param =new Object[]
		       {
		    	colStr("office_m_code"),
		    	wp.itemStr("ex_corp_no"),
		        wp.itemStr("office_code"),
		        wp.itemStr("office_name"),
		        wp.itemStr("brokerage_flag"),
//		        wp.loginUser,
		        wp.loginUser,
		        wp.modSeqno(),
		        wp.loginUser,
		        wp.modPgm()
		       };

		  sqlExec(strSql, param);
		  if (sqlRowNum <= 0) errmsg("新增 "+controlTabName+" 錯誤");
		  
		  wp.colSet("office_m_code", colStr("office_m_code"));
		  wp.colSet("corp_no", wp.itemStr("ex_corp_no"));

		  return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		  dataCheck();
		  if (rc!=1) return rc;

		  strSql= "update " +controlTabName + " set "
				 + "office_m_code = ?, "
		         + "corp_no = ?, "
				 + "office_code = ?, "
		         + "office_name = ?, "
		         + "brokerage_flag = ?, "
		         + "apr_flag = ?,"
//		         + "apr_user  = ?, "
//		         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
		         + "mod_user  = ?, "
		         + "mod_seqno = nvl(mod_seqno,0)+1, "
		         + "mod_time  = sysdate, "
		         + "mod_pgm   = ? "
		         + "where rowid = ? "
		         + "and   mod_seqno = ? ";

		  Object[] param =new Object[]
		    {
		     colStr("office_m_code"),		
		     wp.itemStr("ex_corp_no"),
		     wp.itemStr("office_code"),
		     wp.itemStr("office_name"),
		     wp.itemStr("brokerage_flag"),
		     wp.itemStr("apr_flag"),
//		     wp.loginUser,
		     wp.loginUser,
		     wp.itemStr("mod_pgm"),
		     wp.itemRowId("rowid"),
		     wp.itemNum("mod_seqno")
		    };

		  sqlExec(strSql, param);
		  if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

		  return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		  dataCheck();
		  if (rc!=1)return rc;

		  strSql = "delete " +controlTabName + " " 
		         + "where rowid = ?";

		  Object[] param =new Object[]
		    {
		     wp.itemRowId("rowid")
		    };

		  sqlExec(strSql, param);
		  if (sqlRowNum <= 0) rc=0;else rc=1;
		  if (sqlRowNum <= 0) errmsg("刪除 "+ controlTabName +" 錯誤");

		  return rc;
	}

	

}
