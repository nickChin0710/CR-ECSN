/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-10-20  v1.00.00  Zuwei Su   sync from mega, update coding standard                     *
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0150Func extends FuncEdit {
	String acctCode="";
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
		if(this.ibAdd){
			acctCode=wp.itemStr("kk_acct_code");
		}	else	{
			acctCode=wp.itemStr("acct_code");
		}		
		
		if(empty(acctCode)){
			errmsg("科目代號：不可空白");
			return;
		}
		if(wp.itemEmpty("item_order_normal")){
			errmsg("科目沖銷順序-正常:不可空白");
			return;
		}
		if(wp.itemEmpty("item_class_normal")){
			errmsg("科目沖銷類別-正常:不可空白");
			return;
		}
		if(wp.itemEmpty("item_order_refund")){
			errmsg("科目沖銷順序-退貨:不可空白");
			return;
		}
		if(wp.itemEmpty("item_class_refund")){
			errmsg("科目沖銷類別-退貨:不可空白");
			return;
		}
		if(wp.itemEmpty("item_order_back_date")){
			errmsg("科目沖銷類別-超前起息:不可空白");
			return;
		}
		if(wp.itemEmpty("item_class_back_date")){
			errmsg("科目沖銷順序-超前起息:不可空白");
			return;
		}
		
		if(this.isAdd()){
			return;
		}
		sqlWhere = " where 1=1"
				+ " and acct_code='"+ acctCode+"'"
				+ " and nvl(mod_seqno,0) =" + wp.modSeqno(); 
		log("sql-where="+sqlWhere);
		if (this.isOtherModify("ptr_actcode", sqlWhere)) {
			return;	
		}
	}


	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc!=1){
			return rc;
		}
		
		strSql = "insert into PTR_ACTCODE ("
				+ " acct_code, " // 1
				+ " chi_short_name, " 
				+ " chi_long_name, " 
				+ " eng_short_name, "
				+ " eng_long_name, "
				+ " item_order_normal, "
				+ " item_order_back_date, "
				+ " item_order_refund, "
				+ " item_class_normal, "
				+ " item_class_back_date, "
				+ " item_class_refund, "
				+ " interest_method, "
				+ " inter_rate_code, "
				+ " part_rev, "
				+ " revolve, "
				+ " acct_method, "
				+ " urge_1st, "
				+ " urge_2st, "
				+ " urge_3st, "
				+ " occupy, "
				+ " receivables, "
				+ " query_type, "
				+ " inter_rate_code2, "
				+ " acct_code_flag, "//24
				+ " crt_date, "
				+ " crt_user "
				+ ", mod_time, mod_user, mod_pgm, mod_seqno" 
				+ " ) values ("
				+ " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? "
				+ ",to_char(sysdate,'yyyymmdd'),? "
				+ ",sysdate,?,?,1"  
				+ " )";
		Object[] param = new Object[] { 
				acctCode
				, wp.itemStr("chi_short_name")
				, wp.itemStr("chi_long_name")
				, wp.itemStr("eng_short_name")
				, wp.itemStr("eng_long_name")
				, wp.itemStr("item_order_normal")
				, wp.itemStr("item_order_back_date")
				, wp.itemStr("item_order_refund")
				, wp.itemStr("item_class_normal")
				, wp.itemStr("item_class_back_date")
				, wp.itemStr("item_class_refund")
				, wp.itemStr("interest_method")
				, wp.itemStr("inter_rate_code")
				, wp.itemStr("part_rev")
				, wp.itemStr("revolve")
				, wp.itemStr("acct_method")
				, wp.itemStr("urge_1st")
				, wp.itemStr("urge_2st")
				, wp.itemStr("urge_3st")
				, wp.itemStr("occupy")
				, wp.itemStr("receivables")
				, wp.itemStr("query_type")
				, wp.itemStr("inter_rate_code2")
				, wp.itemStr("acct_code_flag")
				, wp.loginUser
				, wp.loginUser
				, wp.itemStr("mod_pgm")
			};
			this.log("kk1="+acctCode); 
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
		if (rc!=1){
			return rc;
		}
		strSql = "update PTR_ACTCODE set "
				+ " chi_short_name =?, "
				+ " chi_long_name =?, "
				+ " eng_short_name =?, "
				+ " eng_long_name =?, "
				+ " item_order_normal =?, "
				+ " item_order_back_date =?, "
				+ " item_order_refund =?, "
				+ " item_class_normal =?, "
				+ " item_class_back_date =?, "
				+ " item_class_refund =?, "
				+ " interest_method =?, "
				+ " inter_rate_code =?, "
				+ " part_rev =?, "
				+ " revolve =?, "
				+ " acct_method =?, "
				+ " urge_1st =?, "
				+ " urge_2st =?, "
				+ " urge_3st =?, "
				+ " occupy =?, "
				+ " receivables =?, "
				+ " query_type =?, "
				+ " inter_rate_code2 =?, "
				+ " acct_code_flag =?, "
				+ " mod_user =?, mod_time=sysdate, mod_pgm =? "
				+ ", mod_seqno =nvl(mod_seqno,0)+1 " 
				+ sqlWhere;
			Object[] param = new Object[] { 
				wp.itemStr("chi_short_name"), 
				wp.itemStr("chi_long_name"),
				wp.itemStr("eng_short_name"),
				wp.itemStr("eng_long_name"),
				wp.itemStr("item_order_normal"),
				wp.itemStr("item_order_back_date"),
				wp.itemStr("item_order_refund"),
				wp.itemStr("item_class_normal"),
				wp.itemStr("item_class_back_date"),
				wp.itemStr("item_class_refund"),
				wp.itemStr("interest_method"),
				wp.itemStr("inter_rate_code"),
				wp.itemStr("part_rev"),
				wp.itemStr("revolve"),
				wp.itemStr("acct_method"),
				wp.itemStr("urge_1st"),
				wp.itemStr("urge_2st"),
				wp.itemStr("urge_3st"),
				wp.itemStr("occupy"),
				wp.itemStr("receivables"),
				wp.itemStr("query_type"),
				wp.itemStr("inter_rate_code2"),
				wp.itemStr("acct_code_flag"),
				wp.loginUser,
				wp.itemStr("mod_pgm")
			};
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
		if (rc!=1){
			return rc;
		}
		strSql = "delete PTR_ACTCODE "
				+ sqlWhere;
			// ddd("del-sql="+strSql);
			rc = sqlExec(strSql);
			if (sqlRowNum <= 0) {
				errmsg(this.sqlErrtext);
			} 
		 
		return rc;
	}

}
