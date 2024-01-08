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

public class Ptrm0145Func extends FuncEdit {
	String acctType="";

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
			acctType=wp.itemStr("kk_acct_type");
		}	else	{
			acctType=wp.itemStr("acct_type");
		}
		
		
		if(empty("acct_type")){
			errmsg("帳戶類別：不可空白");
			return;
		}
		if(empty("pn_max_cnt")){
			errmsg("逾期手續費連續最多收取次數(含) ：不可空白");
			return;
		}
		
		if(this.isAdd()){
			if (this.sqlRowcount("ptr_actpenalty","where acct_type='"+acctType+"'")>0) {
				errmsg("acct_type is exist");
				return;
			}
			//--
			return;
		}
		//--
		sqlWhere = " where 1=1"
				+ " and acct_type='"+ acctType+"'"
				+ " and nvl(mod_seqno,0) =" + wp.modSeqno(); 
		log("sql-where="+sqlWhere);
		if (this.isOtherModify("ptr_actpenalty", sqlWhere)) {
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
		
		strSql = "insert into ptr_actpenalty ("
				+ " ACCT_TYPE, " // 1
				+ " METHOD, " 
				+ " FIX_PENALTY, " 
				+ " PERCENT_PENALTY, "
				+ " MAX_PENALTY, "
				+ " MIN_PENALTY, "
				+ " FIRST_MONTH, "
				+ " FIRST_PENALTY, "
				+ " SECOND_MONTH, "
				+ " SECOND_PENALTY, "
				+ " THIRD_MONTH, "
				+ " THIRD_PENALTY, "
				+ " PN_MAX_CNT, "
				+ " crt_date, "
				+ " crt_user,"
				+ " apr_date,"
				+ " apr_user "
				+ ", mod_time, mod_user, mod_pgm, mod_seqno" 
				+ " ) values ("
				+ " ?,?,?,?,?,?,?,?,?,?,?,?,? "
				+ ",to_char(sysdate,'yyyymmdd'),? "
				+ ",to_char(sysdate,'yyyymmdd'),? "
				+ ",sysdate,?,?,1"  
				+ " )";
		Object[] param = new Object[] { 
				acctType
				, wp.itemStr("METHOD")
				, wp.itemStr("FIX_PENALTY")
				, wp.itemNum("PERCENT_PENALTY")
				, wp.itemNum("MAX_PENALTY")
				, wp.itemNum("MIN_PENALTY")
				, wp.itemNum("FIRST_MONTH")
				, wp.itemNum("FIRST_PENALTY")
				, wp.itemNum("SECOND_MONTH")
				, wp.itemNum("SECOND_PENALTY")
				, wp.itemNum("THIRD_MONTH")
				, wp.itemNum("THIRD_PENALTY")
				, wp.itemNum("PN_MAX_CNT")
				, wp.loginUser
				, wp.itemStr("zz_apr_user")
				, wp.loginUser
				, wp.itemStr("mod_pgm")
			};
			this.log("kk1="+acctType); 
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
		strSql = "update ptr_actpenalty set "
				+ " METHOD =?, "
				+ " FIX_PENALTY =?, "
				+ " PERCENT_PENALTY =?, "
				+ " MAX_PENALTY =?, "
				+ " MIN_PENALTY =?, "
				+ " FIRST_MONTH =?, "
				+ " FIRST_PENALTY =?, "
				+ " SECOND_MONTH =?, "
				+ " SECOND_PENALTY =?, "
				+ " THIRD_MONTH =?, "
				+ " THIRD_PENALTY =?, "
				+ " PN_MAX_CNT =?, "
				+ " apr_date = to_char(sysdate,'yyyymmdd') , "
				+ " apr_user = ? ,"
				+ " mod_user =?, mod_time=sysdate, mod_pgm =? "
				+ ", mod_seqno =nvl(mod_seqno,0)+1 " 
				+ sqlWhere;
			Object[] param = new Object[] { 
				wp.itemStr("METHOD"), 
				wp.itemNum("FIX_PENALTY"), 
				wp.itemNum("PERCENT_PENALTY"),
				wp.itemNum("MAX_PENALTY"),
				wp.itemNum("MIN_PENALTY"),
				wp.itemNum("FIRST_MONTH"),
				wp.itemNum("FIRST_PENALTY"),
				wp.itemNum("SECOND_MONTH"),
				wp.itemNum("SECOND_PENALTY"),
				wp.itemNum("THIRD_MONTH"),
				wp.itemNum("THIRD_PENALTY"),
				wp.itemNum("PN_MAX_CNT"),
				wp.itemStr("zz_apr_user"),
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
		strSql = "delete ptr_actpenalty "
				+ sqlWhere;
			// ddd("del-sql="+strSql);
			rc = sqlExec(strSql);
			if (sqlRowNum <= 0) {
				errmsg(this.sqlErrtext);
			} 
		 
		return rc;
	}

}
