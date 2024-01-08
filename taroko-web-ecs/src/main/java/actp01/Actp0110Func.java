/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 111-10-25  v1.00.00  Yang Bo    Sync code from mega                        *
 * 111-11-14  V1.00.01  Simon      1.cancel autopay_indicator='3'             *
 *                                 2.update autopay_acct_no data into act_acno & act_acct_curr for bank 006*
 * 112-12-26  V1.00.02  Ryan       add AUTOPAY_ACCT_BANK <> 0060567 update act_acno,act_acct_curr
 *                                
 ******************************************************************************/
package actp01;

import busi.FuncAction;

public class Actp0110Func extends FuncAction {
	String isExecCheckFlag = "" , isExecCheckDate = "" , isIbmCheckFlag = "";
	String isIbmCheckDate = "" , isIbmReturnCode = "" ;
	@Override
	public void dataCheck() {
		if(checkActChkno()==false){
			errmsg("欲(取消)覆核資料 已不存在");
			return ;
		}
		
		if(selectActAcno()==false){
			errmsg("select act_acno  Error");
			return ;
		}
		
		if(varEq("exec_check_flag", "N") || empty(varsStr("exec_check_flag"))){
			//--確認--
		//if(eq_igno(zzstr.mid(varsStr("autopay_acct_bank"), 0,3),"017")||eq_igno(zzstr.mid(varsStr("autopay_acct_bank"), 0,3),"700")){
			if(eqIgno(commString.mid(varsStr("autopay_acct_bank"), 0,3),"006")) {
				if(varEq("verify_flag","N")){
					errmsg(" 請先執行驗印作業");
					return ;
				}
			}
			isExecCheckFlag = "Y";
			isExecCheckDate = getSysDate();
			if(!eqIgno(commString.mid(varsStr("autopay_acct_bank"), 0,3),"006")){
				isIbmCheckFlag = "Y";
				isIbmCheckDate = getSysDate();
				isIbmReturnCode = "0";				
			} else {
				isIbmCheckFlag = "";
				isIbmCheckDate = "";
				isIbmReturnCode = "";				
			}

			if(eqIgno(commString.mid(varsStr("autopay_acct_bank"), 0,3),"006")||eqIgno(commString.mid(varsStr("autopay_acct_bank"), 0,3),"700")){
				deleteActChkno();
				if(rc!=1)	return ;
			}
		}	else	{
			//--取消--
			if(eqIgno(commString.mid(varsStr("autopay_acct_bank"), 0,3),"017")){
				if(varEq("ibm_check_flag","Y")){
					errmsg("已執行 IBM 檢核, 不允許 取消 覆核作業 ");
					return ;
				}
			}
				
			if(varEq("ach_check_flag","Y")){
				errmsg("已產生ACH驗印檔, 不允許 取消 覆核作業 ");
				return ;
			}
			isExecCheckFlag = "N";
			isExecCheckDate = "";
			isIbmCheckFlag = "N";
			isIbmCheckDate = "";
			isIbmReturnCode = "";
			
			deleteActAchDtl();
			if(rc!=1)	return ;
		}
		
	}
		
	boolean checkActChkno() {
		String sql1 = " select "
						+ " count(*) as db_cnt "
						+ " from act_chkno "
						+ " where 1=1 "
					//+commSqlStr.whereRowid(varsStr("rowid"))
					  + " and hex(rowid) = :hex_rowid "
						;
		setString("hex_rowid",varsStr("rowid"));
		sqlSelect(sql1);
		
		if(sqlRowNum<=0 || colNum("db_cnt")==0)	return false;
		return true;
	}
	
	boolean selectActAcno() {
		String sql1 = " select "
						+ " p_seqno as ls_p_seqno , "
						+ " id_p_seqno as ls_id_p_seqno , "
						+ " uf_corp_no(corp_p_seqno) as ls_corp_no , "
						+ " vip_code as ls_vip_code , "
						+ " stmt_cycle as ls_stmt_cycle "
						+ " from act_acno "
						+ " where acct_type = ? "
						+ " and acct_key = ? "
						+ " and acno_p_seqno = p_seqno "
						;
		
		sqlSelect(sql1,new Object[]{varsStr("acct_type"),varsStr("acct_key")});
		if(sqlRowNum<=0)	return false;
		return true;
	}
	
	public int deleteActChkno() {
		msgOK();
		strSql = " delete act_chkno "
				 + " where p_seqno =:p_seqno "
				 + " and exec_check_flag = 'Y' "
				 + " and ad_mark <>'D' "
				 + " and curr_code in ('','901') "
				 + " and substr(autopay_acct_bank,1,3) in ('017','700') "
				 ;
		setString("p_seqno",colStr("ls_p_seqno"));
		sqlExec(strSql);
		//if(sqlRowNum<0){
		//	errmsg("delete ACT_CHKNO error");			
		//}	else rc =1;
		return rc;
	}
	
	public int deleteActAchDtl() {
		msgOK();
		strSql = " delete act_ach_dtl "
				 + " where p_seqno =:p_seqno "
				 + " and rtn_date = '' "
				 + " and mod_time in "
				 + " (select max(mod_time) from act_ach_dtl where p_seqno=:p_seqno and rtn_date ='') "
				 ;
		
		setString("p_seqno",colStr("ls_p_seqno"));
		setString("p_seqno",colStr("ls_p_seqno"));
		sqlExec(strSql);
		//if(sqlRowNum<0){
		//	errmsg("delete act_ach_dtl Error ");			
		//}	else rc =1;				
		return rc;
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
		String sql1 = " select "
						+ " business_date "
						+ " from ptr_businday "
						+ " where 1=1 "
						;
		
		sqlSelect(sql1);
		String lsBusDate = colStr("business_date");
		
		if(eqIgno(commString.mid(varsStr("autopay_acct_bank"), 0,3),"006")) {
    	strSql = " update act_acct_curr set "
    			 + " autopay_acct_bank =:autopay_acct_bank ,"
    			 + " autopay_acct_no =:autopay_acct_no ,"
    			 + " autopay_id =:autopay_id ,"
    			 + " autopay_id_code =:autopay_id_code ,"
    			 + " autopay_indicator =:autopay_indicator ,"
    			 + " mod_user =:mod_user ,"
    			 + " mod_time = sysdate ,"
    			 + " mod_pgm =:mod_pgm ,"
    			 + " mod_seqno = nvl(mod_seqno,0)+1 "
		  		 + " where p_seqno =:p_seqno "
			  	 + " and curr_code = '901' "
    			 ;
    	
		  var2ParmStr("autopay_acct_bank");
		  var2ParmStr("autopay_acct_no");
		  var2ParmStr("autopay_id");
		  var2ParmStr("autopay_id_code");
		  var2ParmStr("autopay_indicator");
    	setString("mod_user",wp.loginUser);
    	setString("mod_pgm",wp.modPgm());				
		  var2ParmStr("p_seqno");
    	sqlExec(strSql);
    	if(sqlRowNum<=0){
    		errmsg("update act_acct_curr error ");
		    return rc;
     	}

    	strSql = " update act_acno set "
    			 + " autopay_acct_bank =:autopay_acct_bank ,"
    			 + " autopay_acct_no =:autopay_acct_no ,"
    			 + " autopay_acct_s_date =:autopay_acct_s_date ,"
    			 + " autopay_acct_e_date ='' ,"
    			 + " autopay_id =:autopay_id ,"
    			 + " autopay_id_code =:autopay_id_code ,"
    			 + " autopay_indicator =:autopay_indicator ,"
    			 + " mod_user =:mod_user ,"
    			 + " mod_time = sysdate ,"
    			 + " mod_pgm =:mod_pgm ,"
    			 + " mod_seqno = nvl(mod_seqno,0)+1 "
		  		 + " where acno_p_seqno =:p_seqno "
    			 ;
    	
		  var2ParmStr("autopay_acct_bank");
		  var2ParmStr("autopay_acct_no");
    	setString("autopay_acct_s_date",lsBusDate);
		  var2ParmStr("autopay_id");
		  var2ParmStr("autopay_id_code");
		  var2ParmStr("autopay_indicator");
    	setString("mod_user",wp.loginUser);
    	setString("mod_pgm",wp.modPgm());				
		  var2ParmStr("p_seqno");
    	sqlExec(strSql);
    	if(sqlRowNum<=0){
    		errmsg("update act_acno error ");
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
  			return rc;
  		}
  		
  		updateActAcno();
  		if(rc<=0){
  			errmsg("updateActAcno error ");
  			return rc;
  		}
  		
  		updateActAcctCurr();
  		if(rc<=0){
  			errmsg("updateActAcctCurr error ");
  			return rc;
  		}
  	}
		
		return rc;
	}

	int updateActAcno() {
		strSql = " UPDATE ACT_ACNO SET " ;
		strSql += " AUTOPAY_INDICATOR =:AUTOPAY_INDICATOR ,";
		strSql += " AUTOPAY_ID =:AUTOPAY_ID ,";
		strSql += " AUTOPAY_ID_CODE =:AUTOPAY_ID_CODE ,";
		strSql += " AUTOPAY_ACCT_BANK =:AUTOPAY_ACCT_BANK ,";
		strSql += " AUTOPAY_ACCT_NO =:AUTOPAY_ACCT_NO ,";
		strSql += " AUTOPAY_ACCT_S_DATE =:AUTOPAY_ACCT_S_DATE ,";
		strSql += " AUTOPAY_ACCT_E_DATE =:AUTOPAY_ACCT_E_DATE ,";
		strSql += " mod_user =:mod_user ,"; 
		strSql += " mod_time = sysdate ,";
		strSql += " mod_pgm =:mod_pgm ,";
		strSql += " mod_seqno = nvl(mod_seqno,0)+1 ";
		strSql += " where acno_p_seqno =:p_seqno ";

		var2ParmStr("AUTOPAY_INDICATOR");
		var2ParmStr("AUTOPAY_ID");
		var2ParmStr("AUTOPAY_ID_CODE");
		var2ParmStr("AUTOPAY_ACCT_BANK");
		var2ParmStr("AUTOPAY_ACCT_NO");
		var2ParmStr("AUTOPAY_ACCT_S_DATE");
		var2ParmStr("AUTOPAY_ACCT_E_DATE");
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", wp.modPgm());
		var2ParmStr("p_seqno");
		rc = sqlExec(strSql);
		return rc;
	}
	
	int updateActAcctCurr() {
		strSql = " UPDATE ACT_ACCT_CURR SET " ;
		strSql += " AUTOPAY_INDICATOR =:AUTOPAY_INDICATOR ,";
		strSql += " AUTOPAY_ID =:AUTOPAY_ID ,";
		strSql += " AUTOPAY_ID_CODE =:AUTOPAY_ID_CODE ,";
		strSql += " AUTOPAY_ACCT_BANK =:AUTOPAY_ACCT_BANK ,";
		strSql += " AUTOPAY_ACCT_NO =:AUTOPAY_ACCT_NO ,";
		strSql += " mod_user =:mod_user ,"; 
		strSql += " mod_time = sysdate ,";
		strSql += " mod_pgm =:mod_pgm ,";
		strSql += " mod_seqno = nvl(mod_seqno,0)+1 ";
		strSql += " where p_seqno =:p_seqno ";
		strSql += " and curr_code = '901' ";
		var2ParmStr("AUTOPAY_INDICATOR");
		var2ParmStr("AUTOPAY_ID");
		var2ParmStr("AUTOPAY_ID_CODE");
		var2ParmStr("AUTOPAY_ACCT_BANK");
		var2ParmStr("AUTOPAY_ACCT_NO");
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", wp.modPgm());
		var2ParmStr("p_seqno");
		rc = sqlExec(strSql);
		return rc;
	}
}
