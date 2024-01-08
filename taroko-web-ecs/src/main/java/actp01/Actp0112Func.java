/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 111-10-25  v1.00.00  Yang Bo    Sync code from mega                        *
 * 111-11-12  V1.00.01  Simon      1.Add column CURR_CHANGE_ACCOUT maintenance*
 *                                 2.update autopay_acct_no data into act_acct_curr*
 ******************************************************************************/
package actp01;

import busi.FuncAction;

public class Actp0112Func extends FuncAction {
	String isExecCheckFlag = "" , isExecCheckDate = "" , isIbmCheckFlag = "";
	String isIbmCheckDate = "" , isIbmReturnCode = "" ;
	@Override
	public void dataCheck() {
		if(checkActChkno()==false){
			errmsg("資料已不存在 或 資料已被修改  請重新整理");
			return ;
		}
		
	//if(var_eq("exec_check_flag","N")){
	  if(varEq("exec_check_flag","N") || empty(varsStr("exec_check_flag"))){
			isExecCheckFlag = "Y";
			isExecCheckDate = this.getSysDate();
			isIbmCheckFlag = varsStr("ibm_check_flag");
			isIbmCheckDate = varsStr("ibm_check_date");
			isIbmReturnCode = varsStr("ibm_return_code");
		}	else	{
			if(varEq("ibm_check_flag","Y")){
				errmsg("已執行 IBM 檢核, 不允許 取消 覆核作業 ");
				return ;
			}
			isExecCheckFlag = "N";
			isExecCheckDate = "";
			isIbmCheckFlag = "N";
			isIbmCheckDate = "";
			isIbmReturnCode = "";
		}
		
	}
	
	boolean checkActChkno() {
		String sql1 = " select "
						+ " count(*) as db_cnt "
						+ " from act_chkno "
						+ " where 1=1 "
					//+commSqlStr.whereRowid(varsStr("rowid"))
				    + " and hex(rowid) = ? "
						+ " and mod_seqno = ? "
						;
		sqlSelect(sql1,new Object[]{varsStr("rowid"),varsStr("mod_seqno")});
		
		if(sqlRowNum<=0 || colNum("db_cnt")==0)	return false;
		return true;
	}
	
	@Override
	public int dbInsert() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbUpdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbDelete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataProc() {
		rc = 1;
		dataCheck();
		if(rc!=1)	return rc;

		if(eqIgno(commString.mid(varsStr("autopay_acct_bank"), 0,3),"006")) {
    	strSql = " update act_acct_curr set "
    			 + " autopay_acct_bank =:autopay_acct_bank ,"
    			 + " autopay_acct_no =:autopay_acct_no ,"
    			 + " curr_change_accout =:exchange_acct_no ,"
    			 + " autopay_id =:autopay_id ,"
    			 + " autopay_id_code =:autopay_id_code ,"
    			 + " autopay_indicator =:autopay_indicator ,"
    			 + " autopay_dc_flag =:autopay_dc_flag ,"
    			 + " autopay_dc_indicator =:autopay_dc_indicator ,"
    			 + " mod_user =:mod_user ,"
    			 + " mod_time = sysdate ,"
    			 + " mod_pgm =:mod_pgm ,"
    			 + " mod_seqno = nvl(mod_seqno,0)+1 "
		  		 + " where p_seqno =:p_seqno "
			  	 + " and curr_code =:curr_code "
    			 ;
    	
		  var2ParmStr("autopay_acct_bank");
		  var2ParmStr("autopay_acct_no");
		  var2ParmStr("exchange_acct_no");
		  var2ParmStr("autopay_id");
		  var2ParmStr("autopay_id_code");
		  var2ParmStr("autopay_indicator");
		  var2ParmStr("autopay_dc_flag");
		  var2ParmStr("autopay_dc_indicator");
    	setString("mod_user",wp.loginUser);
    	setString("mod_pgm",wp.modPgm());				
		  var2ParmStr("p_seqno");
		  var2ParmStr("curr_code");
    	sqlExec(strSql);
    	if(sqlRowNum<=0){
    		errmsg("update act_acct_curr error ");
		    return rc;
     	}
  	//strSql = " delete act_chkno where 1=1 "+commSqlStr.whereRowid(varsStr("rowid"));
  		strSql = " delete act_chkno where 1=1 and hex(rowid) = ? ";
    	setString(varsStr("rowid"));				
  		sqlExec(strSql);
  		if(sqlRowNum<=0){
  			errmsg("delete act_chkno error !");
  		}
    } else {
    	strSql = " update act_chkno set "
    			 + " exec_check_flag =:exec_check_flag ,"
    			 + " exec_check_date =:exec_check_date ,"
    			 + " ibm_check_flag =:ibm_check_flag ,"
    			 + " ibm_check_date =:ibm_check_date ,"
    			 + " ibm_return_code =:ibm_return_code ,"
    			 + " mod_user =:mod_user ,"
    			 + " mod_time = sysdate ,"
    			 + " mod_pgm =:mod_pgm ,"
    			 + " mod_seqno = nvl(mod_seqno,0)+1 "
    			 + " where 1=1 "
    		 //+commSqlStr.whereRowid(varsStr("rowid"))
    			 + " and hex(rowid) = :hex_rowid "
    			 ;
    	
    	setString("exec_check_flag", isExecCheckFlag);
    	setString("exec_check_date", isExecCheckDate);
    	setString("ibm_check_flag", isIbmCheckFlag);
    	setString("ibm_check_date", isIbmCheckDate);
    	setString("ibm_return_code", isIbmReturnCode);
    	setString("mod_user",wp.loginUser);
    	setString("mod_pgm",wp.modPgm());				
		  setString("hex_rowid",varsStr("rowid"));
    	sqlExec(strSql);
    	if(sqlRowNum<=0){
    		errmsg("update act_chkno error ");
     	}
    }
		
		return rc;
	}

}
