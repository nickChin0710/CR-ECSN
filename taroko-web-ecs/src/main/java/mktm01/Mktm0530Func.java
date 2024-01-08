/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-01-10  V1.00.01  machao     新增功能：合庫金控子公司參數維護作業    
* 111-06-30  V1.00.02  machao     页面bug处理                  *                         *
******************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Mktm0530Func extends FuncEdit {
	private  String PROGNAME = "合庫金控子公司參數維護處理程式111-01-10 V1.00.01";
	  String kk1;
	  String control_tab_name = "mkt_office_m";

	public Mktm0530Func(TarokoCommon wr) {
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
//		if (this.ibAdd)
//	     {
//	      kk1 = wp.itemStr("ex_office_m_code");
//	      if (empty(kk1))
//	         {
//	          errmsg("機構代號 不可空白");
////	          wp.colSet("ex_office_m_code", "pink");
//	          return;
//	         }
//	     }
//	  else
//	     {
//	      kk1 = wp.itemStr("office_m_code");
//	     }
	if((this.ibAdd)|| (this.ibUpdate))
	{
		String kk1 = wp.itemStr("ex_office_m_code");
		String corpno = wp.itemStr("corp_no");
		if(empty(kk1)) {
			errmsg("機構代號 不可空白");
	        wp.colSet("ex_office_m_code_pink", "pink");
		}else {
			if (kk1.length()>0)
//		     {
//		      strSql = "select count(*) as qua "
//		             + "from " + control_tab_name
//		             + " where office_m_code = ? "
//		             ;
//		      Object[] param = new Object[] {kk1};
//		      sqlSelect(strSql,param);
//		      int qua =  Integer.parseInt(colStr("qua"));
//		      if (qua > 0)
//		         {
//		          errmsg("[機構代號] 不可重複("+control_tab_name+") ,請重新輸入!");
//		          return;
//		         }
//		     }
			if(wp.itemStr2("ex_office_m_code")==wp.getValue("office_m_code")) {
          	  errmsg("機構代號重覆!!請重新輸入"); 
          }
		}
		if(empty(corpno)) {
			errmsg("必輸欄位請輸入資料");
	        wp.colSet("corp_no_pink", "pink");
		}
	}
		
	  if (this.ibAdd)
	  {
		  strSql = "select count(*) as qua "
		             + "from " + control_tab_name
		             + " where corp_no = ? "
		             ;
		      Object[] param = new Object[] {wp.itemStr("corp_no")};
		      sqlSelect(strSql,param);
		      int qua =  Integer.parseInt(colStr("qua"));
		      if (qua > 0)
		         {
		          errmsg("統一編重覆!!,不能存檔");
		          return;
		         }
	  }


	  if ((this.ibAdd)||(this.ibUpdate))
	     {
	      if (!wp.itemEmpty("effc_date_beg")&&(!wp.itemEmpty("EFFC_DATE_END")))
	      if (wp.itemStr("effc_date_beg").compareTo(wp.itemStr("EFFC_DATE_END"))>0)
	         {
	          errmsg("統計年度起迄["+wp.itemStr("effc_date_beg")+"]>["+wp.itemStr("EFFC_DATE_END")+"] 起迄值錯誤!");
	          return;
	         }
	     }


	  if (this.isAdd()) return;
		
	}


	@Override
	public int dbInsert() {
		actionInit("A");
		  dataCheck();
		  if (rc!=1) return rc;


		  strSql= " insert into  " + control_tab_name+ " ("
		          + " office_m_code, "
		          + " office_m_name, "
		          + " corp_no,"
		          + " effc_date_beg, "
		          + " effc_date_end, "
		          + " apr_date, "
		          + " apr_user, "
		          + " crt_date, "
		          + " crt_user, "
		          + " mod_seqno, "
		          + " mod_time,mod_user,mod_pgm "
		          + " ) values ("
		          + "?,?,?,?,?,"
		          + "to_char(sysdate,'yyyymmdd'),"
		          + "?,"
		          + "to_char(sysdate,'yyyymmdd'),"
		          + "?,"
		          + "?,"
		          + "sysdate,?,?)";

		  Object[] param =new Object[]
		       {
		    	wp.itemStr("ex_office_m_code"),
		        wp.itemStr("office_m_name"),
		        wp.itemStr("corp_no"),
		        wp.itemStr("effc_date_beg"),
		        wp.itemStr("effc_date_end"),
		        wp.itemStr("zz_apr_user"),
		        wp.loginUser,
		        wp.modSeqno(),
		        wp.loginUser,
		        wp.modPgm()
		       };

		  sqlExec(strSql, param);
		  if (sqlRowNum <= 0) errmsg("新增 "+control_tab_name+" 錯誤");

		  return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		  dataCheck();
		  if (rc!=1) return rc;

		  strSql= "update " +control_tab_name + " set "
		         + "office_m_name = ?, "
				 + "corp_no = ?, "
		         + "effc_date_beg = ?, "
		         + "effc_date_end = ?, "
		         + "apr_user  = ?, "
		         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
		         + "mod_user  = ?, "
		         + "mod_seqno = nvl(mod_seqno,0)+1, "
		         + "mod_time  = sysdate, "
		         + "mod_pgm   = ? "
		         + "where rowid = ? "
		         + "and   mod_seqno = ? ";

		  Object[] param =new Object[]
		    {
		     wp.itemStr("office_m_name"),
		     wp.itemStr("corp_no"),
		     wp.itemStr("effc_date_beg"),
		     wp.itemStr("effc_date_end"),
		     wp.itemStr("zz_apr_user"),
		     wp.loginUser,
		     wp.itemStr("mod_pgm"),
		     wp.itemRowId("rowid"),
		     wp.itemNum("mod_seqno")
		    };

		  sqlExec(strSql, param);
		  if (sqlRowNum <= 0) errmsg("更新 "+ control_tab_name +" 錯誤");

		  return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		  dataCheck();
		  if (rc!=1)return rc;

		  strSql = "delete " +control_tab_name + " " 
		         + "where rowid = ?";

		  Object[] param =new Object[]
		    {
		     wp.itemRowId("rowid")
		    };

		  sqlExec(strSql, param);
		  if (sqlRowNum <= 0) rc=0;else rc=1;
		  if (sqlRowNum <= 0) errmsg("刪除 "+ control_tab_name +" 錯誤");

		  return rc;
	}

	

}
