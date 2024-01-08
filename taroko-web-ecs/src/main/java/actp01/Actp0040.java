/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-05  V1.00.00  OrisChang  program initial                            *
* 107-08-21  V1.00.01  Alex       savefunc , dddw , query                    *
* 111-10-24  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/

package actp01;
	
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actp0040 extends BaseEdit {
	String pPSeqno = "";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
//			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void queryFunc() throws Exception {
		
		//if(wp.item_empty("ex_acct_key")==false){
     /***
			p_p_seqno = getInitParm();
			
			if(empty(p_p_seqno)) {
				alert_err("此帳戶帳號無待覆核資料！");
				return;
			}
     ***/

		String lsWhere = "";
		String lsAcctkey = "";
		lsAcctkey = fillZeroAcctKey(wp.itemStr("ex_acct_key"));
		
		lsWhere = " where 1=1 "
				     + sqlCol(wp.itemStr("ex_acct_type"),"acct_type")				 
				     + sqlCol(wp.itemStr("ex_curr_code"),"curr_code")
		         + " and act_modtype='03' "
				     + " and p_seqno in "
					   +  " (select acno_p_seqno from act_acno where 1=1 "
					   +sqlCol(wp.itemStr("ex_acct_type"),"acct_type")
					   +sqlCol(lsAcctkey,"acct_key")
					   + 	" ) "
				     ;

		wp.whereStr = lsWhere;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {

		// -page control-
			
		wp.pageControl();

		// select columns
		wp.selectSQL =  " hex(rowid) as rowid, acct_type, uf_acno_key(p_seqno) as acct_key , curr_code, acct_data, uf_acno_name(p_seqno) as chi_name, p_seqno, update_user " ;
		// table name
		wp.daoTable = "ACT_MODDATA_TMP";
		// where sql
		/***
		wp.whereStr = " where 1=1 and act_modtype='03' "
						+sql_col(p_p_seqno,"p_seqno")
						+sql_col(wp.item_ss("ex_curr_code"),"curr_code")
						;						
		***/
		// order column
	//wp.whereOrder = "";
    wp.whereOrder=" order by mod_time ";

		pageQuery();
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.setListCount(1);

		wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
		wp.setPageValue();
		
		ProcModDataTmp();
    apprDisabled("update_user");


	}

	private String getInitParm() throws Exception {		
		String lsSql = "";
		String acctkey = "";
		acctkey = fillZeroAcctKey(wp.itemStr("ex_acct_key"));
		
		lsSql = " select p_seqno,acct_type, uf_acno_key(p_seqno) as acct_key, curr_code , acct_data, uf_acno_name(p_seqno) as chi_name ";
		lsSql += " from ACT_MODDATA_TMP ";
		lsSql += " where 1=1 "
				 + sqlCol(wp.itemStr("ex_acct_type"),"acct_type")				 
				 + sqlCol(wp.itemStr("ex_curr_code"),"curr_code")
				 + " and p_seqno in (select p_seqno from act_acno where acct_type = ? and acct_key = ? ) "
		     + " and act_modtype='03' "
				 ;
//		ls_sql += " where acct_type=? and acct_key = ? and curr_code = ? and act_modtype='03'";
//
//		param = new Object[] { wp.item_ss("ex_acct_type"), acctkey, wp.item_ss("ex_curr_code") };
		
		sqlSelect(lsSql , new Object[]{wp.itemStr("ex_acct_type"),acctkey});
		if (sqlRowNum<=0) {
			return "";
		}else {		
			return sqlStr("p_seqno");
		}

	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		// ProcModDataTmp();
	}

	@Override
	public void saveFunc() throws Exception {
		int llOk= 0 , llErr = 0, llAprUserErr = 0;
		Actp0040Func func = new Actp0040Func(wp);
		String[] opt = wp.itemBuff("opt");
		String[] liAutoPayBal = wp.itemBuff("dc_autopay_bal_a");
		String[] lsPSeqno = wp.itemBuff("p_seqno");
		String[] lsAcctType = wp.itemBuff("acct_type");
		String[] lsAcctKey = wp.itemBuff("acct_key");
		String[] lsCurrCode = wp.itemBuff("curr_code");
		String[] lsUpdateUser = wp.itemBuff("update_user");

    /***  
    wp.ddd("-->Actp0040-dsp01","");
    wp.ddd("--:opt[0][%s]",opt[0]);
    wp.ddd("--:opt[1][%s]",opt[1]);
    wp.ddd("--:opt[2][%s]",opt[2]);
    wp.ddd("--:ls_p_seqno[0][%s]",ls_p_seqno[0]);
    wp.ddd("--:ls_p_seqno[1][%s]",ls_p_seqno[1]);
    wp.ddd("--:ls_p_seqno[2][%s]",ls_p_seqno[2]);
    ***/  
		
		wp.listCount[0] = wp.itemRows("p_seqno");		
  //wp.ddd("-->Actp0040-dsp02","");
  //wp.ddd("--:item_rows(p_seqno)[%s]",wp.listCount[0]);
		
		int rr = 0;
		for (int ll = 0; ll < opt.length; ll++) {
			// -option-ON-
			rr = (int) optToIndex(opt[ll]);
			if (rr<0) {
				continue;
			}			
				
    /***  
      wp.ddd("-->Actp0040-dsp03","");
      wp.ddd("--:[rr][%s]",rr);
      wp.ddd("--:ls_p_seqno[rr][%s]",ls_p_seqno[rr]);
    ***/  
		
			func.varsSet("dc_autopay_bal", liAutoPayBal[rr]);
			func.varsSet("p_seqno", lsPSeqno[rr]);
			func.varsSet("acct_type", lsAcctType[rr]);
			func.varsSet("acct_key", lsAcctKey[rr]);
			func.varsSet("curr_code", lsCurrCode[rr]);
				
      /*** 改成 opt 欄位 protected
			if(wp.loginUser.equals(ls_update_user[rr]) ) {
			//errmsg("覆核人員及經辦不能為同一人！");
        ll_apr_user_err++;
				wp.col_set(rr,"ok_flag", "X");
				ll_err++;
				continue;
			}
      ***/ 
			
			if(func.updateData()==-1){
				wp.colSet(rr,"ok_flag", "X");
				llErr++;
				dbRollback();
				continue;
			}
							
			if(func.deleteData()==1){
				wp.colSet(rr, "ok_flag","V");
				llOk++;
				sqlCommit(1);
				continue;
			}	else	{
				wp.colSet(rr,"ok_flag", "X");
				llErr++;
				dbRollback();
				continue;
			}							
		}
		
    /*** 改成 opt 欄位 protected
		if(ll_apr_user_err > 0){
  		alert_msg("覆核人員及經辦不能為同一人！",true);
		}	
    ***/ 

		alertMsg("資料處理完成 , 成功:"+llOk+" 失敗:"+llErr);
		
		
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	@Override
	public void dddwSelect() {
		try {

//			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
						
			wp.optionKey = wp.itemStr("ex_curr_code");
			this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");

		} catch (Exception ex) {
		}
	}

	void ProcModDataTmp() throws Exception {
		int rowCt = wp.selectCnt;
		String strTmpData = "";

		

		for (int ii = 0; ii < rowCt; ii++) {
			strTmpData = wp.colStr(ii, "acct_data");
			String[] tmpCols = strTmpData.equals("") || strTmpData == null ? null : strTmpData.split("@");
			wp.colSet(ii, "stmt_cycle", tmpCols[0]);
			wp.colSet(ii, "this_acct_month", tmpCols[1]);
			
			String sStatus = "";
			if (Integer.parseInt(tmpCols[2]) == 1)
				sStatus = "1-正常";
			if (Integer.parseInt(tmpCols[2]) == 2)
				sStatus = "2-逾放";
			if (Integer.parseInt(tmpCols[2]) == 3)
				sStatus = "3-催收";
			if (Integer.parseInt(tmpCols[2]) == 4)
				sStatus = "4-呆帳";
			
			wp.colSet(ii, "acct_status", sStatus);
			
			wp.colSet(ii, "dc_autopay_beg_amt", tmpCols[3]);
			wp.colSet(ii, "dc_min_pay_bal", tmpCols[4]);
			wp.colSet(ii, "dc_autopay_bal", tmpCols[9]);
			wp.colSet(ii, "dc_acct_jrnl_bal", tmpCols[5]);

			wp.colSet(ii, "dc_autopay_beg_amt_a", tmpCols[3]);
			wp.colSet(ii, "dc_min_pay_bal_a", tmpCols[4]);
			wp.colSet(ii, "dc_autopay_bal_a", tmpCols[6]);
			wp.colSet(ii, "dc_acct_jrnl_bal_a", tmpCols[5]);
		}
	}
	
	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}
}
