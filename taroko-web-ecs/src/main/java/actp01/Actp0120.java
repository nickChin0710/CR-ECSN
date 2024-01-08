/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* ???        V1.00.01  ???        program initial                            *
* 20200825   V1.00.02  Andy       update Mantis3993                          *
* 111-10-25  v1.00.03  Yang Bo    Sync code from mega                        *
* 112-07-04  V1.00.04  Simon      TCB溢付款退款客製化                        * 
*****************************************************************************/
package actp01;

import ofcapp.BaseAction;

public class Actp0120 extends BaseAction {
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
			if (eqIgno(wp.respHtml,"Actp0120")) {				
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
    
    /*
		if(wp.item_empty("ex_acct_key") && wp.item_empty("ex_user")) {

		 //errmsg("帳戶帳號和登入人員至少需輸入一項");
  		 err_alert("帳戶帳號和登入人員至少需輸入一項");
			 return;
		}
    */
		
		String lsWhere = " where adjust_type like 'OP%' and nvl(process_flag,'x')<>'Y' "
							 + sqlCol(wp.itemStr("ex_acct_type"),"acct_type")							 
						 //+ sqlCol(ls_acct_key,"uf_acno_key(p_seqno)")    執行時間較久							 
							 + sqlCol(wp.itemStr("ex_user"),"update_user","like%")
							 + sqlCol(wp.itemStr("ex_current_code"),"uf_nvl(curr_code,'901')")
							 ;
		
		if(wp.itemEq("ex_apr_flag", "N")){
			lsWhere += " and apr_flag <> 'Y'";
		}	else if(wp.itemEq("ex_apr_flag", "Y")){
			lsWhere += " and apr_flag = 'Y'";
		}
		
    
		if(!empty(lsAcctKey)){
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
		String lsSql = "select count(*) sum_ct, sum(uf_dc_amt(curr_code,dr_amt,dc_dr_amt)) sum_amt "
				+ "from act_acaj ";
		lsSql += lsWhere;
		log("ls_sql :"+lsSql);
		sqlSelect(lsSql);
		if(sqlNum("sum_ct") > 0){
			wp.colSet("sum_ct", sqlStr("sum_ct"));
			wp.colSet("sum_amt", sqlStr("sum_amt"));
		}

		
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = ""
						 + " p_seqno ,"
						 + " acct_type ,"
						 + " uf_acno_key(p_seqno) as acct_key ,"
						 + " nvl(curr_code,'901') curr_code ,"
						 + " uf_dc_amt(curr_code,dr_amt,dc_dr_amt) as dr_amt ,"
						 + " uf_dc_amt(curr_code,bef_amt,dc_bef_amt) as bef_amt ,"
						 + " uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt) as bef_d_amt ,"
						 + " uf_dc_amt(curr_code,cr_amt,dc_cr_amt) as cr_amt ," //2020/9/23 U-1701說：溢付款圈存作業已取消
						 + " uf_dc_amt(curr_code,aft_amt,dc_aft_amt) as aft_amt ,"
						 + " uf_dc_amt(curr_code,aft_d_amt,dc_aft_d_amt) as aft_d_amt ,"
						 + " cash_type ,"
						 + " post_date ,"
						 + " update_user ,"
						 + " update_date ,"
						 + " uf_nvl(apr_flag,'N') as apr_flag ,"
						 + " decode(apr_flag,'','待覆核','N','待覆核','Y','解覆核') as tt_apr_flag ,"
						 + " jrnl_date ,"
						 + " jrnl_time ,"
						 + " adjust_type ,"
						 + " crt_time ,"
						 + " trans_acct_key ,"
						 + " trans_acct_type ,"
	      		 + " mcht_no as bank_no, "
				     + " adj_comment, "
						 + " interest_date ," //2020/9/23 U-1701說：溢付款圈存作業已取消
						 + " crt_date ,"
						 + " lpad(' ',40) as db_trans_cname ,"
						 + " uf_acno_name(p_seqno) db_cname ,"
						 + " 'acaj ' db_table ,"
						 + " mod_seqno ,"
						 + " hex(rowid) as rowid , "
						 + " uf_dc_amt(curr_code,bef_amt,dc_bef_amt) + uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt) as wk_tot_bef_amt "						 
						 ;
		wp.daoTable = "act_acaj";
		pageQuery();
		if(this.sqlNotFind()){
			alertErr("此條件查無資料");
			return ;			
		}
		queryAfter();
		wp.setListCount(0);
		wp.setPageValue();
		apprDisabled("update_user");

	}
	
	void queryAfter() throws Exception {
		String sql1 = " select "
						+ " uf_acno_name(p_seqno) as ls_cname "
						+ " from act_acno "
						+ " where acct_type = ? "
						+ " and acct_key = ? "
						+ " and acno_p_seqno = p_seqno "
						;
		
		String ss1 = "", ss2 = "";
		for(int ii=0;ii<wp.selectCnt;ii++){
		//wp.col_set(ii,"wk_tans_acctno", wp.colStr(ii,"trans_acct_type")+"-"+wp.colStr(ii,"trans_acct_key"));

			if(wp.colEq(ii,"adjust_type", "OP02")) {
  			ss1 = wp.colStr(ii, "bank_no");
	  		ss2 = wp.colStr(ii, "trans_acct_key");
			  if(wp.colEq(ii,"cash_type", "1")){
			  	wp.colSet(ii,"tt_cash_type", "1.隔日轉入本行帳號");
 		      wp.colSet(ii,"wk_tans_acctno", ss1+"-"+ss2);
			  }	else if(wp.colEq(ii,"cash_type", "2")){
			  	wp.colSet(ii,"tt_cash_type", "2.當日轉入本行帳號");
 		      wp.colSet(ii,"wk_tans_acctno", ss1+"-"+ss2);
			  }	else if(wp.colEq(ii,"cash_type", "3")){
			  	wp.colSet(ii,"tt_cash_type", "3.當日轉入他行帳號");
 		      wp.colSet(ii,"wk_tans_acctno", ss1+"-"+ss2);
			  }	else if(wp.colEq(ii,"cash_type", "4")){
			  	wp.colSet(ii,"tt_cash_type", "4.其他");
 		      wp.colSet(ii,"wk_tans_acctno", wp.colStr(ii,"adj_comment"));
			  }
 		  //wp.colSet(ii,"wk_tans_acctno", wp.colStr(ii,"trans_acct_key"));
			} else if (wp.colEq(ii,"adjust_type", "OP03")) {
				wp.colSet(ii,"tt_cash_type", "溢付款轉出");
		    wp.colSet(ii,"wk_tans_acctno", wp.colStr(ii,"trans_acct_type")+"-"+wp.colStr(ii,"trans_acct_key"));
			}
			
			if(wp.colEq(ii,"adjust_type", "OP01")){
				wp.colSet(ii,"tt_adjust_type", "OP01_圈存"); //2020/9/23 U-1701說：溢付款圈存作業已取消
			}	else if(wp.colEq(ii,"adjust_type", "OP02")){
				wp.colSet(ii,"tt_adjust_type", "OP02_提領");
			}	else if(wp.colEq(ii,"adjust_type", "OP03")){
				wp.colSet(ii,"tt_adjust_type", "OP03_轉出");
			}
			
			if(empty(wp.colStr(ii,"trans_acct_type"))||empty(wp.colStr(ii,"trans_acct_key")))	continue;
			
			sqlSelect(sql1,new Object[]{wp.colStr(ii,"trans_acct_type"),wp.colStr(ii,"trans_acct_key")});
			if(sqlRowNum>0){
				wp.colSet(ii,"db_trans_cname", sqlStr("ls_cname"));
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
		int llOk = 0 , llErr = 0 ; 
		String lsAlreadyCk = "" , lsNotOnAll = "";
		String[] lsPSeqno = wp.itemBuff("p_seqno");
		String[] lsRowid = wp.itemBuff("rowid");
		String[] lsModSeqno = wp.itemBuff("mod_seqno");
		String[] lsAcctType = wp.itemBuff("acct_type");
		String[] lsAcctKey = wp.itemBuff("acct_key");
		String[] lsAprFlag = wp.itemBuff("apr_flag");
		String[] aaOpt = wp.itemBuff("opt");
		wp.listCount[0] = wp.itemRows("rowid");
		
		Actp0120Func func = new Actp0120Func();
		func.setConn(wp);
		
		for(int ii=0;ii<wp.itemRows("rowid");ii++){
			if(!checkBoxOptOn(ii, aaOpt))	continue;	
			wp.colSet(ii,"check", "checked");
			if(lsAlreadyCk.indexOf(lsPSeqno[ii])<0){
				lsAlreadyCk += lsPSeqno[ii];
				if(checkSamePseqnoOn(lsPSeqno[ii])==false){
					if(lsNotOnAll.length()<=0){
						lsNotOnAll += lsAcctType[ii]+"-"+lsAcctKey[ii];
					}	else	{
						lsNotOnAll += ","+lsAcctType[ii]+"-"+lsAcctKey[ii];
					}
					wp.colSet(ii,"ok_flag", "X");
					llErr++;
					continue;
				}
			}
			
			func.varsSet("rowid", lsRowid[ii]);
			func.varsSet("mod_seqno", lsModSeqno[ii]);
			func.varsSet("apr_flag", lsAprFlag[ii]);
			if(func.dataProc() == 1){
				wp.colSet(ii,"ok_flag", "V");
				llOk++;
				continue;
			}	else	{
				wp.colSet(ii,"ok_flag", "X");
				llErr++;
				continue;
			}
			
		}
		
		if(llOk>0){
			sqlCommit(1);
		}
		
		if(empty(lsNotOnAll)){
			alertMsg("執行完成, 成功:"+llOk+" 失敗:"+llErr);
		}	else	{
			alertMsg("執行完成, 成功:"+llOk+" 失敗:"+llErr+" 未勾選所有相同帳號調整帳號:"+lsNotOnAll);
		}
		
		
	}
	
	boolean checkSamePseqnoOn(String pSeqno){
		String[] lsPSeqno = wp.itemBuff("p_seqno");
		String[] lsRowid = wp.itemBuff("rowid");
		String[] aaOpt = wp.itemBuff("opt");
		
		for(int ii=0;ii<wp.itemRows("rowid");ii++){
			if(eqIgno(pSeqno,lsPSeqno[ii]) && !checkBoxOptOn(ii, aaOpt)){
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initPage() {
		wp.colSet("sum_ct", "0");
		wp.colSet("sum_amt", "0");

	}

}
