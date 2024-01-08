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

import ofcapp.BaseAction;

public class Actp0112 extends BaseAction {
	String lsAcctKey = "";
	@Override
	public void userAction() throws Exception {
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		}
		else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		}
		else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
		}
		else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			saveFunc();
		}
		else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
		}
		else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
		}
		else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		}
		else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		}
		else if (eqIgno(wp.buttonCode, "UPLOAD")) {
			procFunc();
		}
		else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}
		else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			procFunc();
		}
	}

	@Override
	public void dddwSelect() {
		try {
			if (eqIgno(wp.respHtml,"Actp0112")) {				
				wp.optionKey = wp.colStr(0, "ex_acct_type");
				dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
				wp.optionKey = wp.colStr(0, "ex_current_code");
				dddwList("dddw_current_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY'");
			}
			}
			catch(Exception ex){}

	}

	@Override
	public void queryFunc() throws Exception {
    lsAcctKey = wp.itemStr("ex_acct_key");
    
		if(!empty(lsAcctKey)){
			if(lsAcctKey.length()<8){
				errmsg("帳戶帳號至少輸入8碼");
				return;
			}
			lsAcctKey = commString.acctKey(lsAcctKey);
			if(lsAcctKey.length()!=11){
				errmsg("帳戶帳號輸入錯誤 !");
				return;
			}
		}
    
    /*
		if(wp.item_empty("ex_acct_key") && wp.item_empty("ex_user")) {

		 //errmsg("帳戶帳號和登入人員至少需輸入一項");
  		 err_alert("帳戶帳號和登入人員至少需輸入一項");
			 return;
		}
    */		
		
		String lsWhere = " where 1=1 and uf_nvl(curr_code,'901')<>'901'  "
							 + " and ad_mark <>'D' and from_mark in ('02','03') "
							 + " and verify_flag = 'Y' and verify_return_code = '00' "
							 +sqlCol(wp.itemStr("ex_acct_type"),"acct_type")							 
						 //+sqlCol(ls_acct_key,"uf_acno_key(p_seqno)")		 執行時間較久					 
							 +sqlCol(wp.itemStr("ex_verify"),"uf_nvl(exec_check_flag,'N')")
							 +sqlCol(wp.itemStr("ex_current_code"),"curr_code")
							 +sqlCol(wp.itemStr("ex_user"),"crt_user","like%")
							 ;
		
    
		if(!empty(lsAcctKey)) {
			lsWhere += " and p_seqno in "
					   +  " (select acno_p_seqno from act_acno where 1=1 "
					   +sqlCol(wp.itemStr("ex_acct_type"),"acct_type")
					   +sqlCol(lsAcctKey,"acct_key")
					   + 	" ) "
					   ;
		}
    
		
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = ""
						 + " acct_type ,"
						 + " uf_acno_key(p_seqno) as acct_key ,"
						 + " uf_idno_id(id_p_seqno) as id_no ,"
						 + " autopay_acct_bank ,"
						 + " autopay_acct_no ,"
						 + " curr_change_accout as exchange_acct_no ,"
						 + " valid_flag ,"
						 + " decode(valid_flag,'1','即時生效','2','Cycle生效') as tt_valid_flag , "
						 + " autopay_indicator ,"
						 + " decode(autopay_indicator,'1','TTL','2','MP','3','其他') as tt_autopay_indicator ,"
						 + " autopay_fix_amt ,"
						 + " autopay_rate ,"
						 + " autopay_acct_s_date ,"
						 + " autopay_acct_e_date ,"
						 + " from_mark ,"
						 + " decode(from_mark,'02','授權書-新申請','03','授權書-修改帳號') as tt_from_mark ,"
						 + " verify_flag ,"
						 + " autopay_id ,"
						 + " autopay_id_code ,"
						 + " crt_date ,"
						 + " p_seqno ,"
						 + " exec_check_flag ,"
						 + " decode(exec_check_flag,'','待放行','N','待放行','Y','解放行') as tt_exec_check_flag , "
						 + " verify_return_code ,"
						 + " decode(verify_return_code,'00','成功','01','失敗','99','免驗印') as tt_verify_return_code , "
						 + " id_p_seqno ,"
						 + " verify_date ,"
						 + " mod_time ,"
						 + " mod_user ,"
						 + " crt_user ,"
						 + " crt_time ,"
						 + " effc_flag ,"
						 + " stmt_cycle ,"
						 + " curr_code ,"
						 + " autopay_dc_flag ,"
						 + " autopay_dc_indicator ,"
						 + " decode(autopay_dc_indicator,'1','TTL','2','MP') as tt_autopay_dc_indicator ,"
						 + " mod_seqno ,"
						 + " uf_acno_name(p_seqno) as db_acno_name ,"
						 + " hex(rowid) as rowid , "
						 + " ibm_check_flag , "
						 + " ibm_check_date , "
						 + " ibm_return_code "
						 ;
		wp.daoTable = " act_chkno ";
		wp.whereOrder = " order by crt_date Asc,autopay_id Asc ";
		pageQuery();
		if(sqlNotFind()){
			alertErr("此條件查無資料");
			return ;
		}
		queryAfter();
		wp.setListCount(0);
		wp.setPageValue();
    apprDisabled("crt_user");

	}
	
	void queryAfter() throws Exception{
		String sql1 = " select "
						+ " wf_desc "
						+ " from ptr_sys_idtab "
						+ " where wf_id = ? "
						+ " and wf_type = 'DC_CURRENCY' "
						;
		String sql2 = " select "
				+ " bank_name "
				+ " from act_ach_bank "
				+ " where bank_no = ? "
				;
		for(int ii=0;ii<wp.selectCnt;ii++){
			sqlSelect(sql1,new Object[]{wp.colStr(ii,"curr_code")});
			if(sqlRowNum>0){
				wp.colSet(ii,"tt_curr_code",sqlStr("wf_desc"));
			}
			sqlSelect(sql2,new Object[]{wp.colStr(ii,"autopay_acct_bank")});
			if(sqlRowNum>0){
				wp.colSet(ii,"tt_autopay_acct_bank", sqlStr("bank_name"));
			}

			if(!wp.colEq(ii,"autopay_dc_flag", "Y")){
				wp.colSet(ii,"tt_autopay_dc_indicator", "");
			}	

		}
		
	}
	
	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void procFunc() throws Exception {
		int llOk = 0 , llErr = 0 , llAprUserErr = 0;
		String[] lsRowid = wp.itemBuff("rowid");
		String[] lsPSeqno = wp.itemBuff("p_seqno");
		String[] lsCurrCode = wp.itemBuff("curr_code");
		String[] lsAutoAcctBank = wp.itemBuff("autopay_acct_bank");
		String[] lsAutoAcctNo = wp.itemBuff("autopay_acct_no");
		String[] lsExchAcctNo = wp.itemBuff("exchange_acct_no");
		String[] lsAutoId = wp.itemBuff("autopay_id");
		String[] lsAutoIdCode = wp.itemBuff("autopay_id_code");
		String[] lsAutoIndicator = wp.itemBuff("autopay_indicator");
		String[] lsAutoDcFlag = wp.itemBuff("autopay_dc_flag");
		String[] lsAutoDcIndicator = wp.itemBuff("autopay_dc_indicator");
		String[] lsModSeqno = wp.itemBuff("mod_seqno");
		String[] lsExecCheckFlag = wp.itemBuff("exec_check_flag");
		String[] lsIbmCheckFlag = wp.itemBuff("ibm_check_flag");
		String[] lsIbmCheckDate = wp.itemBuff("ibm_check_date");
		String[] lsIbmReturnCode = wp.itemBuff("ibm_return_code");
		String[] lsCrtUser = wp.itemBuff("crt_user");
		String[] aaOpt = wp.itemBuff("opt");
		
		Actp0112Func func = new Actp0112Func();
		func.setConn(wp);
		
		wp.listCount[0] = wp.itemRows("rowid");
		for(int ii=0;ii<wp.itemRows("rowid");ii++){
			if(checkBoxOptOn(ii, aaOpt)==false)	continue;
			
      /*** 已改成 opt 欄位 protected
			if(wp.loginUser.equals(ls_crt_user[ii]) ) {
        ll_apr_user_err++;
				wp.colSet(ii,"ok_flag", "X");
				ll_err++;
				continue;
			}
      ***/ 
			
			func.varsSet("rowid", lsRowid[ii]);
			func.varsSet("p_seqno", lsPSeqno[ii]);
			func.varsSet("curr_code", lsCurrCode[ii]);
			func.varsSet("autopay_acct_bank", lsAutoAcctBank[ii]);
			func.varsSet("autopay_acct_no", lsAutoAcctNo[ii]);
			func.varsSet("exchange_acct_no", lsExchAcctNo[ii]);
			func.varsSet("autopay_id", lsAutoId[ii]);
			func.varsSet("autopay_id_code", lsAutoIdCode[ii]);
			func.varsSet("autopay_indicator", lsAutoIndicator[ii]);
			func.varsSet("autopay_dc_flag", lsAutoDcFlag[ii]);
			func.varsSet("autopay_dc_indicator", lsAutoDcIndicator[ii]);
			func.varsSet("exec_check_flag", lsExecCheckFlag[ii]);
			func.varsSet("mod_seqno", lsModSeqno[ii]);
			func.varsSet("exec_check_flag", lsExecCheckFlag[ii]);
			func.varsSet("ibm_check_flag", lsIbmCheckFlag[ii]);
			func.varsSet("ibm_check_date", lsIbmCheckDate[ii]);
			func.varsSet("ibm_return_code", lsIbmReturnCode[ii]);
			
			rc = func.dataProc();
			if(rc!=1){
				llErr++;
				wp.colSet(ii,"ok_flag", "X");
		  	sqlCommit(0);
				continue;
			}	else	{
				llOk++;
				wp.colSet(ii,"ok_flag", "V");
		  	sqlCommit(1);
				continue;
			}			
		}
		
    /*** 已改成 opt 欄位 protected
		if(ll_apr_user_err > 0){
  		alert_msg("覆核人員及經辦不能為同一人！",true);
		}	
    ***/ 

		//if(llOk>0){
		//	sqlCommit(1);
		//}
		
		alertMsg("執行完畢,成功:"+llOk+" 失敗:"+llErr);

	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
