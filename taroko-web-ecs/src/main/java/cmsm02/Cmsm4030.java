/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-07-27  V1.00.08  JustinWu   change CMS_PROC_DEPT into PTR_DEPT_CODE
*  111-11-08  V1.00.01  Machao     頁面bug調整
******************************************************************************/

package cmsm02;
import java.util.HashMap;

/** 客服經辦D檔調整作業
 * 2019-1209:  Alex  keep opt
 * 2019-1205:  Alex  add initButton
 * 2019-1127:  Alex  order by , check apr , comment queryRead
 * 2019-1022:  Ru    移除線上覆核
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 *  V.2018-0904.jh
 * 109-04-27 shiyuqi       updated for project coding standard     *  
 * 109-05-29 JustinWu   remove checkApproveZz()
 * 109-07-17 JustinWu   備註的下拉式選單改從資料庫中取得
 * */
import ofcapp.BaseAction;

public class Cmsm4030 extends BaseAction {
	String lsIn="" , isErrorDesc = "";
	int iiCntDebt=0, iiCntDebthis=0,nrow=0;
	String lsWhere ="";
	int liMax=0;
	double tlDAmt =0.0;
	@Override
	public void userAction() throws Exception {
		wp.pgmVersion("V.19-1127");
		
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
		else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		}
		else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
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
			if (eqIgno(wp.respHtml,"cmsm4030")) {
				
				wp.optionKey = wp.colStr(0, "ex_acct_type");
				dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
				
				wp.optionKey = wp.colStr(0, "ex_curr_code");
				dddwList("dddw_dc_curr_code_tw", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id desc");
				
				wp.optionKey = wp.colStr(0, "ex_adj_dept");
				dddwList("dddw_proc_dept", "PTR_DEPT_CODE", "dept_code", "dept_name", "where 1=1");
				
//				wp.optionKey = "";
//				dddwList("dddw_adj_comment", "PTR_SYS_IDTAB", "WF_ID", "WF_ID||'.'||WF_DESC", " WHERE WF_TYPE='CMSM4030' ");
				
				setOptionAdjCommentList();
			}
		}
			catch(Exception ex){}
		
	}
	
	private void setOptionAdjCommentList() throws Exception {
		if (wp.itemNum("recordCnt") == 0) 
			return;
		
		String sql1 = " select  wf_desc as optionVal, "
				+ " wf_id||'.'||wf_desc as optionDesc "
				+ " from ptr_sys_idtab "
				+ " where wf_type = 'CMSM4030' ";
		
		sqlSelect(sql1);
		
		if(sqlRowNum <=0){
			throw new Exception("ptr_sys_idtab 沒有建立wf_type = 'CMSM4030'的值");
		}

		StringBuilder sb = new StringBuilder();
		
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for (int i = 0; i < sqlRowNum; i++) {
			sb.append("<option value='").append(sqlStr( i, "optionVal")).append("' %s >")
			.append(sqlStr( i, "optionDesc")).append("</option>");
			map.put(sqlStr( i, "optionVal"), i+1);
		}
		
		String optionStrTemplate = sb.toString();
		
		for (int i = 0; i < wp.itemNum("recordCnt"); i++) {
			String tempStr = "";
			String adjComment = wp.colStr( i, "adj_comment");
			Integer tempInt = map.get(adjComment);
			
			if (tempInt == null)
				tempStr = putSelectedInOption(0,sqlRowNum, optionStrTemplate);
			else
				tempStr = putSelectedInOption(tempInt,sqlRowNum, optionStrTemplate);
			
			wp.colSet( i, "dddw_adj_comment", tempStr);
		}
		
	}

	private String putSelectedInOption(int tagetOption, int theNumOfOption, String optionStrTemplate) {
		if (tagetOption == 0)
			optionStrTemplate = optionStrTemplate.replace("%s", "");
		else
			for (int i = 1; i <= theNumOfOption; i++) {
				if (tagetOption == i) {
					optionStrTemplate = optionStrTemplate.replaceFirst("%s", "selected");
				} else {
					optionStrTemplate = optionStrTemplate.replaceFirst("%s", "");
				}
			}

		return optionStrTemplate;
	}

	void queryBefore(String lsAcctKey){		
		//**ex_p_seqno

		String sql1 ="select p_seqno, uf_acno_name(p_seqno) as ex_acno_name"
            +", acno_p_seqno"
				+", payment_rate1, payment_rate2, payment_rate3"
				+", payment_rate4, payment_rate5, payment_rate6"
				+", payment_rate7, payment_rate8, payment_rate9"
				+", payment_rate10, payment_rate11, payment_rate12"
				+" from act_acno"
				+" where acct_key =? and acct_type =?"
				;
		setString2(1,lsAcctKey);
		setString(wp.itemStr2("ex_acct_type"));
		sqlSelect(sql1);
		if (sqlRowNum <=0) {
			alertErr2("帳戶帳號: 輸入錯誤");
			return;
		}
		wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
		wp.colSet("ex_acno_nam", sqlStr("ex_acno_name"));
		wp.colSet("ex_adj_check1", sqlStr("payment_rate1"));
		//**ex_adj_check2
		liMax=commString.strToInt(sqlStr("payment_rate2"));   //(int)sql_num("payment_rate2");
		for(int ii=2 ;ii<=12 ;ii++){
			int liRate =commString.strToInt(sqlStr("payment_rate"+ii));
			if(liRate>liMax){
				liMax = liRate;
			}
		}		
		if(liMax ==0){
			wp.colSet("ex_adj_check2", "繳款正常");
		}	else	{
			wp.colSet("ex_adj_check2", "0"+liMax);
		}
		
		//**ex_adj_check3
		String sql5 = " select count(*) as db_cnt from act_debt "
				+ " where p_seqno = ? "
				+ " and acct_code ='PN' "
				+ " and post_date >= to_char(add_months(sysdate,-12),'yyyymmdd') "
				+ " and end_bal = '0' "
				+ " and D_avail_bal = '0' ";
		sqlSelect(sql5,new Object[]{wp.colStr("ex_p_seqno")});
		wp.colSet("ex_adj_check3", sqlStr("db_cnt"));
		
		//**ex_adj_check4
		String sql6 = " select count(*) as ll_cnt from act_debt "
				+ " where p_seqno = ? "
				+ " and acct_code ='RI' "
				+ " and post_date >= to_char(add_months(sysdate,-12),'yyyymmdd') "
				+ " and end_bal = '0' "
				+ " and D_avail_bal = '0' ";
		sqlSelect(sql6,new Object[]{wp.colStr("ex_p_seqno")});
		wp.colSet("ex_adj_check4", sqlStr("ll_cnt"));
		
		//--ex_pn_dd
		String sql7 = "select max(crt_date) as ex_pn_dd from act_acaj_hst where p_seqno = ? and acct_code ='PN' ";
		sqlSelect(sql7,new Object[]{wp.colStr("ex_p_seqno")});
		if(sqlRowNum>0)	wp.colSet("ex_pn_dd", sqlStr("ex_pn_dd"));
	}
	
	@Override
	public void queryFunc() throws Exception {
		
		String lsAcctKey = "";
		lsAcctKey = wp.itemStr("ex_acct_key");
		if(lsAcctKey.length()==10){
			lsAcctKey += "0";
		}	else if (lsAcctKey.length()==8){
			lsAcctKey += "000";
		}
		if (lsAcctKey.length() !=11){
			alertErr2("帳戶帳號: 輸入錯誤[長度=8,10,11]");
			return;
		}
		
		queryBefore(lsAcctKey);
		if(rc!=1)	return ;
		
		boolean lbItem01 =wp.itemEq("ex_acitem01","1");
		boolean lbItem02 =wp.itemEq("ex_acitem02","1");
		boolean lbItem03 =wp.itemEq("ex_acitem03","1");
		boolean lbItem04 =wp.itemEq("ex_acitem04","1");
		lsWhere =" where 1=1 " 
				  + " and p_seqno in (select p_seqno from act_acno "
				  + " where acct_type ='"+wp.itemStr("ex_acct_type")+"' and acct_key ='"+lsAcctKey+"') ";
		if( !lbItem01 && !lbItem02 && !lbItem03 && !lbItem04 ){
			alertErr2("請選擇費用類項目 ! ");
			return;
		}
		if(lbItem01){
			lsIn +=",'AF','LF','CF','PF','SF','CC','PN'";
		}
		if(lbItem02){
			lsIn +=",'RI','AI','CI'";
		}
		if(lbItem03){
			lsIn +=",'PN'";
		}
		if(lbItem04){
			lsIn +=",'AF'";
		}
		String currCode = wp.itemStr("ex_curr_code");
		lsWhere +=" and acct_code in (''"+lsIn+")";
		lsWhere += sqlCol(currCode,"nvl(curr_code,'901')");	
		setString(currCode);
		lsWhere += sqlCol(wp.itemStr("ex_yymm1"),"acct_month",">=")
					+sqlCol(wp.itemStr("ex_yymm2"),"acct_month","<=");
		
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		
		wp.sqlCmd = " select "
				+ " reference_no ,"
				+ " p_seqno ,"
				+ " acct_type ,"
				+ " post_date ,"
				+ " acct_month ,"
				+ " card_no ,"
				+ " interest_date ,"
				+ " uf_dc_amt(curr_code,beg_bal,dc_beg_bal) as beg_bal ,"
				+ " uf_dc_amt(curr_code,end_bal,dc_end_bal) as end_bal ,"
				+ " uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as d_avail_bal ,"
				+ " txn_code ,"
				+ " acct_code ,"
				+ " bill_type ,"
				+ " interest_rs_date ,"
				+ " beg_bal as tw_beg_bal ,"
				+ " end_bal as tw_end_bal ,"
				+ " d_avail_bal as tw_d_avail_bal ,"
				+ " uf_nvl(curr_code,'901') as curr_code ,"
				+ " 0 as db_d_amt ,"
				+ " '' as adj_comment ,"
				+ " '' as adj_check_memo ,"
				+ " 0 as tw_d_amt ,"
				+" uf_tt_acct_code(acct_code) as tt_acct_code,"
				+ " hex(rowid) as rowid ,"
				+ " 'debt' as db_table, "
				+ " '' as show_opt , "
				+" 'disabled' as show_optD "
				+ " from act_debt "
				+ lsWhere 
				+ " union " 
				+" select "
				+ " reference_no ,"
				+ " p_seqno ,"
				+ " acct_type ,"
				+ " post_date ,"
				+ " acct_month ,"
				+ " card_no ,"
				+ " interest_date ,"
				+ " uf_dc_amt(curr_code,beg_bal,dc_beg_bal) as beg_bal,"
				+ " uf_dc_amt(curr_code,end_bal,dc_end_bal) as end_bal,"
				+ " uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as d_avail_bal,"
				+ " txn_code ,"
				+ " acct_code ,"
				+ " bill_type ,"
				+ " interest_rs_date ,"
				+ " beg_bal as tw_beg_bal,"
				+ " end_bal as tw_end_bal,"
				+ " d_avail_bal as tw_d_avail_bal,"
				+ " uf_nvl(curr_code,'901') as curr_code,"
				+ " 0 as db_d_amt,"
				+ " '' as adj_comment,"
				+ " '' as adj_check_memo,"
				+ " 0 as tw_d_amt,"
				+" uf_tt_acct_code(acct_code) as tt_acct_code,"
				+" hex(rowid) as rowid,"
				+ " 'debt_hst' as db_table,"
				+ " '' as show_opt ,"
				+ " 'disabled' as show_optD "
				+ " from act_debt_hst "
				+lsWhere 
				+ " order by post_date Asc "
				;
		
		pageQuery();
		if (sqlRowNum <= 0) {
			wp.colSet("tl_db_d_amt_392", "0");
			wp.colSet("tl_db_d_amt_840", "0");
			wp.colSet("tl_db_d_amt_901", "0");
			alertErr2("此條件查無資料");
			return ;
		}
		
		wp.setListCount(0);
		selectCmsAcaj();
		queryAfter();
	}	
	
	void selectCmsAcaj(){
		int recordCnt = 0;
		for(int ii=0 ; ii<wp.selectCnt;ii++){
			recordCnt++;
			
			String sql1 = " select  "
					+ " uf_dc_amt(curr_code,beg_bal,dc_beg_bal) as beg_bal,"
					+ " uf_dc_amt(curr_code,end_bal,dc_end_bal) as end_bal,"
					+ " uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as d_avail_bal,"
					+ " beg_bal as tw_beg_bal,"
					+ " end_bal as tw_end_bal,"
					+ " d_avail_bal as tw_d_avail_bal ,"
					+ " uf_nvl(curr_code,'901') as curr_code,"
					+ " adj_remark as adj_comment,"
					+ " adj_amt as db_d_amt ,"
					+" hex(rowid) as acaj_rowid"
					+ " from cms_acaj "
					+ " where reference_no = ? "
					+ " and nvl(acct_post_flag,'N')<>'Y' ";
			
			sqlSelect(sql1,new Object[]{wp.colStr(ii,"reference_no")});
			
			if(sqlRowNum <=0){
				continue;
			}

			wp.colSet(ii,"show_opt", "disabled");
			wp.colSet(ii,"show_optD", "");
			
			wp.colSet(ii,"db_d_amt", sqlStr("db_d_amt"));
			wp.colSet(ii,"adj_comment", sqlStr("adj_comment"));
			wp.colSet(ii,"curr_code", sqlStr("curr_code"));
			wp.colSet(ii,"beg_bal", sqlStr("beg_bal"));
			wp.colSet(ii,"end_bal", sqlStr("end_bal"));
			wp.colSet(ii,"d_avail_bal", sqlStr("d_avail_bal"));
			wp.colSet(ii,"tw_beg_bal", sqlStr("tw_beg_bal"));
			wp.colSet(ii,"tw_end_bal", sqlStr("tw_end_bal"));
			wp.colSet(ii,"tw_d_avail_bal", sqlStr("tw_d_avail_bal"));
			wp.colSet(ii,"rowid", sqlStr("acaj_rowid"));
			wp.colSet(ii,"db_table", "acaj");

			//-tw_d_amt-
			double lmTwAmt =sqlNum("db_d_amt");  //adj_amt
			if (sqlNum("beg_bal")>0) {
				lmTwAmt = (lmTwAmt * sqlNum("tw_beg_bal")) / sqlNum("beg_bal");
			}
			wp.colSet(ii, "tw_d_amt",commString.numScale(lmTwAmt,0));
		}
		wp.itemSet("recordCnt", Integer.toString(recordCnt));
	}
	
	void queryAfter(){
		tlDAmt =0.0;
		String lsCheckMemo = "";
		lsCheckMemo  = " 最近一期繳評:"+wp.colStr("ex_adj_check1");
		lsCheckMemo += " 最近一年最差繳評(不含最近一期):"+wp.colStr("ex_adj_check2");
		lsCheckMemo += " 最近一年刪除PN次數:"+wp.colStr("ex_adj_check3");
		lsCheckMemo += " 最近一年刪除RI次數:"+wp.colStr("ex_adj_check4");
		for (int ii=0; ii<wp.selectCnt;ii++){
			wp.colSet(ii,"adj_check_memo", lsCheckMemo);
			tlDAmt += wp.colNum(ii,"db_d_amt");
		}
		wp.colSet("tl_d_amt", ""+tlDAmt);
		wp.colSet("tl_db_d_amt", "0");	
		if(wp.colEq("ex_curr_code", "392")){
			wp.colSet("tl_curr_code", "日幣");
		}	else if(wp.colEq("ex_curr_code", "840")){
			wp.colSet("tl_curr_code", "美金");
		}	else if(wp.colEq("ex_curr_code", "901")){
			wp.colSet("tl_curr_code", "台幣");
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
		int llCnt =0, llOk=0, llErr=0;
		String lsErrCnt = "";
		cmsm02.Cmsm4030Func func = new cmsm02.Cmsm4030Func();
		func.setConn(wp);
		String [] lsReferenceNo = wp.itemBuff("reference_no");
		String [] lsOpt = wp.itemBuff("opt");
		String [] lsSerNum = wp.itemBuff("ser_num");
		String [] lsOptDel = wp.itemBuff("opt_del");
		String [] lsDbTable = wp.itemBuff("db_table");
		
		wp.itemSet("recordCnt", Integer.toString(lsReferenceNo.length));
		wp.listCount[0] = wp.itemRows("reference_no");
		keepOptOn();
		if(commString.strToInt(wp.itemStr("ex_adj_check1"))>=01){
			alertErr2("最近一期繳評>=01 不可刪除");
			return ;
		}
		
//取消0D0C刪利息需小組長覆核 @20200715 sunny mark
//		if(optToIndex(lsOpt[0])>=0){
//			if(commString.pos("|0C|0D", wp.itemStr2("ex_adj_check1"))>0 && checkRI()==true){
////				if(checkApproveZz()==false)	return ;
//			}
//取消最近一年刪除PN次數>1， 最近一年刪除RI次數>1，最近一年最差繳評(不含最近一期)>01			
//			if(wp.itemNum("ex_adj_check3")>0 || wp.itemNum("ex_adj_check4")>0 || wp.itemNum("ex_adj_check2")>0){
////				if(checkApproveZz()==false)	return ;
//			}
//		}	
		

		isErrorDesc = "";		
		int liRc=0;
		for(int rr =0 ; rr < wp.itemRows("reference_no") ;rr++){
			if (!checkBoxOptOn(rr,lsOpt) &&!checkBoxOptOn(rr,lsOptDel) ) {
				continue;
			}
			llCnt++;
//	取消以下處理 @20200715 sunny mark		
//			if(commString.pos("|0C|0D", wp.itemStr2("ex_adj_check1"))>0 && wp.itemEq(rr,"acct_code", "RI")){
//				func.varsSet("adj_comment", "洽小組長覆核");			
//				wp.colSet(rr,"adj_comment", "洽小組長覆核");
//			}	else	func.varsSet("adj_comment", wp.itemStr(rr,"adj_comment"));
//			
//			if(wp.itemNum("ex_adj_check3")>0 || wp.itemNum("ex_adj_check4")>0 || wp.itemNum("ex_adj_check2")>0){
//				func.varsSet("adj_comment", "洽小組長覆核");
//				wp.colSet(rr,"adj_comment", "洽小組長覆核");
//			}	else	func.varsSet("adj_comment", wp.itemStr(rr,"adj_comment"));
			
			func.varsSet("db_table", wp.itemStr(rr,"db_table"));
			func.varsSet("rowid", wp.itemStr(rr,"rowid"));
			func.varsSet("reference_no",wp.itemStr(rr,"reference_no"));
			func.varsSet("card_no", wp.itemStr(rr,"card_no"));
			func.varsSet("post_date", wp.itemStr(rr,"post_date"));
			func.varsSet("curr_code", wp.itemStr(rr,"curr_code"));
			func.varsSet("beg_bal", wp.itemStr(rr,"beg_bal"));
			func.varsSet("end_bal", wp.itemStr(rr,"end_bal"));
			func.varsSet("d_avail_bal", wp.itemStr(rr,"d_avail_bal"));
			func.varsSet("db_damt", wp.itemStr(rr,"db_d_amt"));
			func.varsSet("acct_code", wp.itemStr(rr,"acct_code"));
			
			func.varsSet("adj_comment", wp.itemStr(rr,"adj_comment"));  //Justin2020/07/17
			wp.colSet(rr, "adj_comment", wp.itemStr(rr,"adj_comment"));  //Justin2020/07/17
			
			func.varsSet("adj_check_memo", wp.itemStr(rr,"adj_check_memo"));
			func.varsSet("tw_beg_bal", wp.itemStr(rr,"tw_beg_bal"));
			func.varsSet("tw_end_bal", wp.itemStr(rr,"tw_end_bal"));
			func.varsSet("tw_d_avail_bal", wp.itemStr(rr,"tw_d_avail_bal"));
			func.varsSet("tw_d_amt",wp.itemStr(rr,"tw_d_amt"));
						
			//-delete for ACAJ----------
			if(checkBoxOptOn(rr,lsOptDel) && eqIgno(lsDbTable[rr],"acaj") ){
				if(checkCancel(rr)==false){
					wp.colSet(rr,"ok_flag", "X");
					llErr++;
					lsErrCnt = lsSerNum[rr];
					break;
				}
				liRc =func.dbDelete();
			}
			
			//-add for ACAJ-------------------------------------
			if(checkBoxOptOn(rr,lsOpt) && (pos("|debt,debt_hst",lsDbTable[rr])>0)){
				if(checkDelete(rr)==false){
					wp.colSet(rr,"ok_flag", "X");
					llErr++;
					lsErrCnt = lsSerNum[rr];
					break;
				}
				liRc =func.dbInsert();
			}
						
			if (liRc==1) {
				wp.colSet(rr,"ok_flag", "V");
				llOk++;				
			} 
			else {
				wp.colSet(rr,"ok_flag", "X");
				llErr++;
				lsErrCnt = lsSerNum[rr];
				isErrorDesc = func.getMsg() ;
				break;
			}
			
		}
		if(llCnt==0){
			alertErr2("請點選D檔資料");
			return ;
		}
		
		if(llErr>0){
			alertErr2("第 "+lsErrCnt+" 筆 錯誤 , 錯誤原因:"+isErrorDesc);
			return ;
		}
		
		alertMsg("存檔完成; OK="+llOk+", ERR="+llErr);		
	}				
	
	boolean checkRI(){
		
		String [] lsOpt = wp.itemBuff("opt");
		String [] lsAcctCode = wp.itemBuff("acct_code");
		int ilRows = wp.itemRows("acct_code");
		
		for(int ii=0;ii<ilRows;ii++){
			if(this.checkBoxOptOn(ii, lsOpt)==false)	continue;
			if(eqIgno(lsAcctCode[ii],"RI"))	return true ;
		}
		
		return false ;
	}
	
	boolean checkCancel(int xx){		
		String[] lsRowid = wp.itemBuff("rowid");
		
		String sql1 = " select count(*) as db_cnt1 "
						+ " from cms_acaj where apr_date <> '' "+commSqlStr.whereRowid(lsRowid[xx]);
		
		sqlSelect(sql1);
		if(sqlNum("db_cnt1")>0)	return false;
		
		return true;
	}
	
	boolean checkDelete(int xx){
		
		String[] lsReferenceNo = wp.itemBuff("reference_no");
		String[] lsDbDAmt = wp.itemBuff("db_d_amt");
		String[] lsCurrCode = wp.itemBuff("curr_code");
		String[] lsDAvailBal = wp.itemBuff("d_avail_bal");
		String[] lsAcctCode = wp.itemBuff("acct_code");
		String[] lsTwBegBal = wp.itemBuff("tw_beg_bal");
		String[] lsBegBal = wp.itemBuff("beg_bal");
		String[] lsAdjComment =wp.itemBuff("adj_comment");
		String[] lsCardNo = wp.itemBuff("card_no");
		
		double ldDbDAmt = 0.0 , ldDAvailBal = 0.0 , ldRwBegBal = 0.0 , ldBegBal = 0.0;
		ldDbDAmt = commString.strToNum(lsDbDAmt[xx]);
		ldDAvailBal = commString.strToNum(lsDAvailBal[xx]);
		ldRwBegBal = commString.strToNum(lsTwBegBal[xx]);
		ldBegBal = commString.strToNum(lsBegBal[xx]);
		//--是否有其他User輸入
		String sql1 = " select count(*) as db_cnt1 from cms_acaj "
						+ " where reference_no = ? and uf_nvl(acct_post_flag,'N') ='N' "
						+ " and uf_nvl(debit_flag,'N') = 'N' ";
		
		sqlSelect(sql1,new Object[]{lsReferenceNo[xx]});
		
		if(sqlNum("db_cnt1")>0){
			isErrorDesc = "此交易已調整未處理, 現在不可調整";
			return false ;
		}
		
		//--已調整未處理
		String sql2 = " select count(*) as db_cnt2 from act_acaj "
						+ " where reference_no = ? and uf_nvl(process_flag,'N') <> 'Y' "
						;
		
		sqlSelect(sql2,new Object[]{lsReferenceNo[xx]});
		
		if(sqlNum("db_cnt2")>0){
			isErrorDesc = "此交易有調整未處理, 現在不可調整";
			return false ;									
		}
		
		//--是否列問交
		String sql3 = " select prb_status from rsk_problem where reference_no = ? ";
		sqlSelect(sql3,new Object[]{lsReferenceNo[xx]});
		if(sqlRowNum>0 && eqIgno(sqlStr("prb_status"),"80")){
			isErrorDesc = "此交易已列問交卻未結案, 不可D檔";
			return false;
		}
		
		//--D檔金額
		if(ldDbDAmt <=0){
			isErrorDesc = "請輸入D檔金額, 須大於 0";
			return false ;
		}
		
		if(eqIgno(lsCurrCode[xx],"901")){
			if(ldDbDAmt%1!=0){
				isErrorDesc = "D檔金額, 台幣須為整數";
				return false ;
			}
		}
		
		//--可D數
		if(ldDbDAmt > ldDAvailBal){
			isErrorDesc = "D檔金額 不可大於 可D數餘額";
			return false ;
		}
		
		//--利息、違約金
		if(commString.pos("|RI|AI|CI|PN", lsAcctCode[xx])>0){
			ldDbDAmt = ldDbDAmt * ldRwBegBal / ldBegBal ;
			if(ldDbDAmt>1000){
				isErrorDesc = "D檔金額 ("+ldDbDAmt+") 不可大於 (台幣)1000 元";
				return false ;
			}
		}

// 改為備註為必填，故此段處理不需要 @20200715 sunny mark	
//		if(eqIgno(lsAcctCode[xx],"EF")){
//			if(empty(lsAdjComment[xx])){
//				isErrorDesc = "費用 D檔 請輸入備註";
//				return false ;
//			}
//		}
		
//// @add 20200715 sunny 判斷備註要有值		
//		if(empty(lsAdjComment[xx])){
//		isErrorDesc = "請輸入備註";
//		return false ;
//	}
		
		//--年費
		if(eqIgno(lsAcctCode[xx],"AF")){
			String lsGroupCode = "" , lsCurrentCode = "" , lsCardNote = "";
			String sql4 = " select A.group_code , A.current_code , B.card_note "
							+ " from crd_card A join ptr_card_type B on A.card_type = B.card_type "
							+ " where A.card_no = ? "
							+ " and A.current_code='0' "; 
			//2023.02.03 sunny add 有效卡始能出現
			
			sqlSelect(sql4,new Object[]{lsCardNo[xx]});
			if(sqlRowNum<=0){
				isErrorDesc = "卡號不存在";
				return false;
			}
// 評估目前合庫不需要此判斷 @20200715 sunny mark			
//			lsGroupCode = sqlStr("group_code");
//			lsCurrentCode = sqlStr("current_code");
//			lsCardNote = sqlStr("card_note");
//			
//			if(eqIgno(lsCardNote,"I") && eqIgno(lsCurrentCode,"0")){
//				isErrorDesc = "頂級卡: 有效卡年費不可刪除";
//				return false ;
//			}
			
//			if(commString.pos("|5015|5016", lsGroupCode)>0 && eqIgno(lsCurrentCode,"0")){
//				if(eqIgno(lsAdjComment[xx],"以紅利14400點折抵")==false){
//					isErrorDesc = "PLUS商旅卡: 需紅利點數扣除14400點才可刪除年費1800元";
//					return false ;
//				}
//				
//				if(ldDbDAmt>1800){
//					isErrorDesc = "PLUS商旅卡:紅利折抵年費只能1800以下";
//					return false ;
//				}				
//			}									
		}				
		return true;
	}
	
	@Override
	public void initButton() {
		btnModeAud("XX");

	}

	@Override
	public void initPage() {
		if(eqIgno(wp.respHtml, "cmsm4030")){
//		    selectUserBankNo(); //讀取登入者的分行代碼
			wp.colSet("ex_adj_dept", "A401"); // 調整預設值為A401
			wp.colSet("acitem01_checked", "checked");
			wp.colSet("acitem02_checked", "checked");
			wp.colSet("acitem03_checked", "checked");
			wp.colSet("acitem04_checked", "checked");
		}

	}
	
	void keepOptOn(){
		int liRows = wp.itemRows("reference_no");
		String[] opt1 = wp.itemBuff("opt");
		String[] opt2 = wp.itemBuff("opt_del");
		for(int ii=0;ii<liRows;ii++){
			if(checkBoxOptOn(ii, opt1))	{
				wp.colSet(ii,"opt_on", "checked");
				continue;
			}
			
			if(checkBoxOptOn(ii, opt2)) {
				wp.colSet(ii,"opt_on2", "checked");
				continue;
			}
			
		}
		
	}

}
