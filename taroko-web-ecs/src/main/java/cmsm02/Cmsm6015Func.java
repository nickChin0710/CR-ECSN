/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.01  shiyuqi       updated for project coding standard  *
* 109-07-23  V1.00.02  JustinWu   set attributes that will be taken to html
******************************************************************************/
package cmsm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Cmsm6015Func extends FuncEdit {
	private final String pgmName = "cmsm6015";
	public Cmsm6015Func(TarokoCommon wr) {		 
		wp = wr;
		this.conn = wp.getConn();
	}	
	//String kk1="",kk2="";
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

	}

	@Override
	public int dbInsert() {
		msgOK();

		strSql = "insert into CMS_CASETYPE ("
				+ " case_type, " // 1
				+ " case_id, "
				+ " case_desc,"
				+ " crt_date, "
				+ " crt_user, "
				+ " apr_flag, "
				+ " apr_date, "
				+ " apr_user, "
				+ " mod_time, "
				+ " mod_user, "
				+ " mod_pgm, "
				+ " mod_seqno"
				+ " ) values ("
				+ " :case_type, "
				+ " :case_id, "
				+ " :case_desc "
				+ ",:crt_date, "
				+ " :crt_user"
				+ ",'Y', "
				+ " :apr_date , "
				+ " :apr_user "
				+ ", timestamp_format( :mod_time ,'yyyymmddhh24miss') , "
				+ " :mod_user, "
				+ " :mod_pgm, "
				+ " 1"
				+ " )";
			// -set ?value-
		try {
			getSysDate();
			
			var2ParmStr("case_type");
			var2ParmStr("case_id");
			var2ParmStr("case_desc");
			setString("crt_date",sysDate);
			setString("crt_user",wp.loginUser);
			setString("apr_date", sysDate);
			var2ParmStr("apr_user");
			setString("mod_time", sysDate + sysTime);
			setString("mod_user",wp.loginUser);
			setString("mod_pgm",pgmName);
		}
		catch (Exception ex) {
			wp.expHandle("sqlParm",ex);
		}
		sqlExec(strSql);
		
		if(sqlDupl) {
			errmsg(String.format("此代碼類別[%s]、分類代碼[%s]", varsStr("case_type"), varsStr("case_id")));
		}else if (sqlRowNum <= 0) {
			errmsg(sqlErrtext);
		}else {
			int ll = varsInt("rowNumber");
			wp.colSet(ll , "mod_date", sysDate);
			wp.colSet(ll , "mod_time", sysTime);
			wp.colSet(ll , "mod_user", wp.loginUser);
			wp.colSet(ll,  "old_data", varsStr("case_id") + "," +varsStr("case_desc"));
			wp.colSet(ll,  "mod_seqno", 0);
		}

		return rc;
	}

	@Override
	public int dbUpdate() {
		msgOK();
		
		strSql = "update CMS_CASETYPE set "
				+ " case_type = :case_type,"
				+ " case_id = :case_id,"
				+ " case_desc = :case_desc,"
//				+ " crt_date= :crt_date,"
//				+ " crt_user= :crt_user,"
				+ " apr_flag= 'Y',"
				+ " apr_date= :apr_date ,"
				+ " apr_user= :apr_user,"
				+ " mod_user = :mod_user, "
				+ " mod_time= timestamp_format( :mod_time ,'yyyymmddhh24miss') , "
				+ " mod_pgm =:mod_pgm "
				+ ", mod_seqno =nvl(mod_seqno,0)+1 " 
				+ " where hex(rowid) =:rowid "
				+" and mod_seqno =:mod_seqno"
				;
//			Object[] param = new Object[] { 
//				wp.item_ss("risk_desc"), 
//				wp.item_ss("mod_user"),
//				wp.item_ss("mod_pgm") 
//			};
		
		        getSysDate();
		        
		
				var2ParmStr("case_type");
				var2ParmStr("case_id");
				var2ParmStr("case_desc");
//				var2ParmNvl("crt_date",this.getSysDate());
//				var2ParmNvl("crt_user",wp.loginUser);
				setString("apr_date", sysDate);
				var2ParmStr("apr_user");
				setString("mod_user",wp.loginUser);
				setString("mod_time",sysDate + sysTime);
				setString("mod_pgm",pgmName);
				setString("rowid",varsStr("rowid"));
				this.setDouble("mod_seqno",varsInt("mod_seqno"));
			
			rc = sqlExec(strSql);
			if (sqlRowNum <= 0) {
				errmsg(String.format("代碼類別[%s]、分類代碼[%s]已經存在", varsStr("case_type"), varsStr("case_id")));
			}else {
				int ll = varsInt("rowNumber");
				wp.colSet(ll , "mod_date", sysDate);
				wp.colSet(ll , "mod_time", sysTime);
				wp.colSet(ll , "mod_user", wp.loginUser);
				wp.colSet(ll,  "old_data", varsStr("case_id") + "," +varsStr("case_desc"));
				wp.colSet(ll,  "mod_seqno", varsInt("mod_seqno")+1);
			}
			return rc;
	
	}
	public int selectData(){
		strSql =" select mod_seqno from CMS_CASETYPE "
				+" where case_type= :case_type and case_id= :case_id ";
		var2ParmStr("case_type");
		var2ParmStr("case_id");
		sqlSelect(strSql);
		log("sql_nrow="+sqlRowNum);
		if (sqlRowNum <= 0) {
			dbInsert();
		} else {
			dbUpdate();
		}
		return rc;
	}
	@Override
	public int dbDelete() {
		msgOK();
		strSql ="Delete CMS_CASETYPE"
				+" where hex(rowid) =?"
				+" and mod_seqno =?"
				  ;
		sqlExec(strSql,new Object[]{
				varsStr("rowid"),
				varsNum("mod_seqno")
		});
		if (sqlRowNum<0) {
			errmsg("Delete CMS_CASETYPE err; "+getMsg());
		}else if(sqlRowNum == 0) {
			errmsg("資料已不存在請重新讀取資料");
		}
		else rc =1;
		
		return rc;
	}

}
