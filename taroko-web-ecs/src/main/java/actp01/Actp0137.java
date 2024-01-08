/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 111-10-25  v1.00.00  Yang Bo    Sync code from mega                        *
 ******************************************************************************/
package actp01;

import ofcapp.BaseAction;

public class Actp0137 extends BaseAction {
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
			if (eqIgno(wp.respHtml,"Actp0137")) {				
				wp.optionKey = wp.colStr(0, "ex_acct_type");
				dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
				wp.optionKey = wp.colStr(0, "ex_curr_code");
				dddwList("dddw_current_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY'");
			}
			}
			catch(Exception ex){}


	}

	@Override
	public void queryFunc() throws Exception {
		lsAcctKey = wp.itemStr("ex_acct_key");
		if(!empty(lsAcctKey)) {
			if(lsAcctKey.length()<8) {
				errmsg("帳戶帳號至少輸入8碼");
				return;
			}
			lsAcctKey = commString.acctKey(lsAcctKey);
			if(lsAcctKey.length()!=11) {
				errmsg("帳戶帳號輸入錯誤 !");
				return;
			}
		}
		
		String lsWhere = " where 1=1 "
							 +sqlCol(wp.itemNvl("ex_acct_type","01"),"acct_type")							 
						 //+sqlCol(ls_acct_key,"uf_acno_key(p_seqno)")	   執行時間較久						 
							 +sqlCol(wp.itemStr("ex_user"),"crt_user")
							 +sqlCol(wp.itemStr("ex_apr_flag"),"uf_nvl(apr_flag,'N')")
							 +sqlCol(wp.itemStr("ex_curr_code"),"uf_nvl(curr_code,'901')")
							 ;

    
		if(!empty(lsAcctKey)){
			lsWhere += " and p_seqno in "
					   +  " (select acno_p_seqno from act_acno where 1=1 "
					   +sqlCol(wp.itemNvl("ex_acct_type","01"),"acct_type")
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
						 + " crt_date ,"
						 + " crt_time ,"
						 + " enq_seqno ,"
						 + " p_seqno ,"
						 + " acct_type ,"
						 + " uf_acno_key(p_seqno) as acct_key ,"
						 + " acct_date ,"
						 + " tran_type ,"
						 + " transaction_amt ,"
						 + " interest_date ,"
						 + " payment_rev_amt ,"
						 + " adj_reason_code ,"
						 + " adj_comment ,"
						 + " c_debt_key ,"
						 + " cr_item ,"
						 + " job_code ,"
						 + " vouch_job_code ,"
						 + " value_type ,"
						 + " this_reversal_amt ,"
						 + " jrnl_seqno ,"
						 + " crt_user ,"						 
						 + " apr_flag ,"
						 + " apr_user ,"
						 + " apr_date ,"
						 + " proc_mark ,"
						 + " mod_user ,"
						 + " mod_time ,"
						 + " mod_pgm ,"
						 + " mod_seqno ,"
						 + " hex(rowid) as rowid ,"
						 + " uf_acno_name(p_seqno) as db_cname ,"
						 + " nvl(curr_code,'901') as curr_code ,"
						 + " dc_transaction_amt as wk_trans_amt ,"
						 + " dc_transaction_amt "
						 ;
		wp.daoTable = " act_pay_rev ";
		pageQuery();
		if(sqlNotFind()){
			alertErr("此條件查無資料");
			return ;
		}
		wp.setListCount(0);
		wp.setPageValue();
    apprDisabled("crt_user");

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
		int llOk = 0 , llErr = 0 ;
		String[] lsRowid = wp.itemBuff("rowid");
		String[] lsModSeqno = wp.itemBuff("mod_seqno");
		String[] lsAprFlag = wp.itemBuff("apr_flag");
		String[] aaOpt = wp.itemBuff("opt");
		wp.listCount[0] = wp.itemRows("rowid");
		
		Actp0137Func func = new Actp0137Func();
		func.setConn(wp);
		
		for(int ii=0;ii<wp.itemRows("rowid");ii++){
			if(checkBoxOptOn(ii, aaOpt)==false)	continue;
			
			func.varsSet("rowid", lsRowid[ii]);
			func.varsSet("apr_flag", lsAprFlag[ii]);
			func.varsSet("mod_seqno", lsModSeqno[ii]);
			
			if(func.dataProc()==1){
				llOk++;
				wp.colSet(ii,"ok_flag", "V");
				continue;
			}	else	{
				llErr++;
				wp.colSet(ii,"ok_flag", "X");
				continue;
			}
		}
		
		if(llOk>0){
			sqlCommit(1);			
		}
		
		alertMsg("執行完成 , 成功:"+llOk+" 失敗:"+llErr);

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
