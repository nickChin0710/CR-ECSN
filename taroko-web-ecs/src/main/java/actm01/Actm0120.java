/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112/03/25  V1.00.04  Simon      add value_type="3" to update act_debt.interest_rs_date*  
* 112-07-21  V1.00.05  Simon      1.取消本金類調整                           *
*                                 2.取消銷帳鍵值、借方科目                   *
* 112-07-24  V1.00.06  Simon      fixed error for reading act_acaj           *
* 112-12-18  V1.00.05  Simon      1.恢復本金類調整                           *
*                                 2.各科目調整還原設定獨立調整類別           *
*                                 3.分期付款利息調整還原設定獨立調整類別     *
******************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actm0120 extends BaseEdit {
	Actm0120Func func;
	CommString commString = new CommString();
	String mPSeqno = "";
	String mRowid = "";
	String mReferenceNo = "";
	String mReversalFlag = "";
	String mTransactionAmt = "";
	String mDcTransactionAmt = "";

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
			updateFunc();
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

	private boolean getWhereStr() throws Exception {
		// 設定queryRead() SQL條件
		String lsPSeqno = getInitParm();

		if (lsPSeqno.equals("")) {
			alertErr2("無此帳戶帳號");
			return false;
		}
		mPSeqno = lsPSeqno;
				
//		wp.whereStr = " where act_jrnl.p_seqno=act_acno.p_seqno "
//					+ " and act_jrnl.tran_class = 'A' "
//					+ " and (act_jrnl.tran_type like 'DE%' or act_jrnl.tran_type = 'WAIN' or act_jrnl.tran_type = 'WAPE') "
//					+ " and act_jrnl.p_seqno = :p_seqno ";
//		setString("p_seqno", m_p_seqno);
//		
//		String strIn = "";
//		// --費用--
//		if (eq_igno(wp.item_ss("ex_acitem01"), "Y")) {
//			strIn += ",'DE09','DE10','DE11','DE12'";
//		}
//		// --利息--
//		if (eq_igno(wp.item_ss("ex_acitem02"), "Y")) {
//			strIn += ",'DE13','DE15','WAIN'";
//		}
//		// --違約金--
//		if (eq_igno(wp.item_ss("ex_acitem03"), "Y")) {
//			strIn += ",'DE14','WAPE'";
//		}
//		// --簽帳款--
//		if (eq_igno(wp.item_ss("ex_acitem04"), "Y")) {
//			strIn += ",'DE08'";
//		}
//		// --Model1--
//		if (eq_igno(wp.item_ss("ex_acitem05"), "Y")) {
//			strIn += ",'DE01','DE02','DE03'";
//		}
//		// --Model2--
//		if (eq_igno(wp.item_ss("ex_acitem06"), "Y")) {
//			strIn += ",'DE04','DE05','DE06'";
//		}
//		// --other--
//		if (eq_igno(wp.item_ss("ex_acitem07"), "Y")) {
//			strIn += ",'DE07'";
//		}
//		if (strIn.length() > 0) {
//			wp.whereStr += " and tran_type in (" + strIn.substring(1) + ") ";
//		}
//
//		if (empty(wp.item_ss("ex_curr_code")) == false) {
//			wp.whereStr += " and decode(curr_code,'','901', curr_code) = :ex_curr_code ";
//			setString("ex_curr_code", wp.item_ss("ex_curr_code"));
//		}
//		wp.whereStr += " fetch first 2000 rows only ";
//
//		//-page control-
//		wp.queryWhere = wp.whereStr;
		
		return true;
	}
	
	@Override
	public void queryFunc() throws Exception {
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		
		if (getWhereStr() == false) return;

		wp.selectSQL = " act_jrnl.crt_date, "   
					+ "act_jrnl.crt_time, "
					+ "act_jrnl.p_seqno, "
					+ "act_jrnl.acct_code, "
					+ "act_jrnl.acct_type, "
					+ "act_acno.acct_key, "
					+ "act_jrnl.acct_date, "
					+ "act_jrnl.tran_type, "
					+ "act_jrnl.item_date, "
					+ "act_jrnl.interest_date, "
					+ "act_jrnl.dr_cr, "
					+ "act_jrnl.transaction_amt, "
					+ "uf_dc_amt(curr_code,transaction_amt,dc_transaction_amt) dc_transaction_amt, "
					+ "uf_dc_amt(curr_code,item_bal,dc_item_bal) item_bal, "
					+ "uf_dc_amt(curr_code,item_d_bal,dc_item_d_bal) item_d_bal, "
					+ "act_jrnl.adj_reason_code, "
					+ "act_jrnl.adj_comment, "
					+ "act_jrnl.reversal_flag, "
					+ "act_jrnl.reference_no, "
					+ "act_jrnl.value_type, "
					+ "uf_acno_name(act_jrnl.p_seqno) as ex_cname, "
					+ "decode(act_jrnl.curr_code,'','901',act_jrnl.curr_code) curr_code, "
					+ "hex(act_jrnl.rowid) as rowid ";
		wp.daoTable = "act_jrnl, act_acno ";
		
		StringBuffer sb = new StringBuffer();
		sb.append(" and act_jrnl.p_seqno = '");
		sb.append(mPSeqno);
		sb.append("'");
		
		wp.whereStr = " where act_jrnl.p_seqno=act_acno.acno_p_seqno "
				+ " and act_jrnl.tran_class = 'A' "
				+ " and (act_jrnl.tran_type like 'DE%' or act_jrnl.tran_type = 'WAIN' or act_jrnl.tran_type = 'WAPE') "
				+ sb.toString();
//				+ " and act_jrnl.p_seqno = :p_seqno ";
//		setString("p_seqno", m_p_seqno);

		sb.delete(0, sb.length());
		// --費用--
		if (eqIgno(wp.itemStr2("ex_acitem01"), "Y")) {
			sb.append(",'DE09','DE19','DE29','DE39','DE10','DE20'");
		}
		// --利息--
		if (eqIgno(wp.itemStr2("ex_acitem02"), "Y")) {
			sb.append(",'DE13','WAIN','DE23','DE33','DE43'");
		}
		// --違約金--
		if (eqIgno(wp.itemStr2("ex_acitem03"), "Y")) {
			sb.append(",'DE14','WAPE'");
		}
		// --本金類--
		if (eqIgno(wp.itemStr2("ex_acitem04"), "Y")) {
			sb.append(",'DE01','DE02','DE03','DE04','DE05','DE06','DE07','DE08'");
		}
/***
		// --Model1--
		if (eqIgno(wp.itemStr2("ex_acitem05"), "Y")) {
			sb.append(",'DE01','DE02','DE03'");
		}
		// --Model2--
		if (eqIgno(wp.itemStr2("ex_acitem06"), "Y")) {
			sb.append(",'DE04','DE05','DE06'");
		}
		// --other--
		if (eqIgno(wp.itemStr2("ex_acitem07"), "Y")) {
			sb.append(",'DE07'");
		}
***/
		if (sb.length() > 0) {
			wp.whereStr += " and tran_type in (" + sb.toString().substring(1) + ") ";
		}
		
		if (empty(wp.itemStr2("ex_curr_code")) == false) {
//			wp.whereStr += " and decode(curr_code,'','901', curr_code) = :ex_curr_code ";
//			setString("ex_curr_code", wp.item_ss("ex_curr_code"));
			sb.delete(0, sb.length());
			sb.append(" and decode(curr_code,'','901', curr_code) = '");
			sb.append(wp.itemStr2("ex_curr_code"));
			sb.append("'");
			wp.whereStr +=  sb.toString();
		}
//		wp.whereStr += " fetch first 2000 rows only ";

		//-page control-
		wp.queryWhere = wp.whereStr;
		
//		wp.whereOrder = " ORDER BY act_jrnl.crt_date, act_jrnl.crt_time ASC ";
		
		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		listWkdata();
		wp.setPageValue();
	}
	
	void listWkdata() throws Exception {
		String ss = "";
		double dd=0;
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			ss =wp.colStr(ii,"acct_code");
			wp.colSet(ii,"tt_acct_code", wfGetAcctName(ss));
			
			ss =wp.colStr(ii,"dr_cr");
			dd =wp.colNum(ii,"dc_transaction_amt");
			if (eqIgno(ss.toUpperCase(),"D")) {
				dd  = 0 - dd;
			}
			
			if (!eqIgno(wp.colStr(ii,"curr_code"),"840")) {  //整數
    			wp.colSet(ii,"wk_transamt", commString.numFormat(dd,"#,##0"));
        		wp.colSet(ii,"item_bal", commString.numFormat(wp.colNum(ii,"item_bal"),"#,##0"));
        		wp.colSet(ii,"item_d_bal", commString.numFormat(wp.colNum(ii,"item_d_bal"),"#,##0"));
    		} else {  //小數兩位
    			wp.colSet(ii,"wk_transamt", commString.numFormat(dd,"#,##0.00"));
        		wp.colSet(ii,"item_bal", commString.numFormat(wp.colNum(ii,"item_bal"),"#,##0.00"));
        		wp.colSet(ii,"item_d_bal", commString.numFormat(wp.colNum(ii,"item_d_bal"),"#,##0.00"));
    		}
		}
    }
	
	String wfGetAcctName(String idcode) throws Exception {
		String rtn = "";
		rtn = idcode;
		String lsSql = "select chi_short_name from ptr_actcode "
					+ "where acct_code = :acct_code ";
		setString("acct_code", idcode);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) rtn += "_"+sqlStr("chi_short_name");

		return rtn;
	}

	private String getInitParm() throws Exception {
		String lsSql = "";
		lsSql = " select acct_type, acct_key, p_seqno, uf_acno_name(p_seqno) as acno_cname ";
		lsSql += " from act_acno ";
		lsSql += " where 1=1 ";
		lsSql += "and acct_type = :acct_type and acct_key = :acct_key ";
		lsSql += "and acno_p_seqno = p_seqno ";
		String acctkey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
		setString("acct_type", wp.itemStr2("ex_acct_type"));
		setString("acct_key",  acctkey);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("h_acct_type", sqlStr("acct_type"));
			wp.colSet("h_acct_key", sqlStr("acct_key"));
			wp.colSet("q_p_seqno", sqlStr("p_seqno"));
			wp.colSet("q_id_cname", sqlStr("acno_cname"));
			return sqlStr("p_seqno");
		}
		return "";
	}

	@Override
	public void querySelect() throws Exception {
		mRowid = wp.itemStr2("data_k1");
		mReferenceNo  = itemKk("data_k2");
		mReversalFlag  = itemKk("data_k3");
		mTransactionAmt  = itemKk("data_k4");
		mDcTransactionAmt  = itemKk("data_k5");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		
		//--check reversal--  //todo 暫時移至前端做
//		if this.item(row,'reversal_flag') = 'Y' then
//			li_ret = Messagebox("訊息","此筆資料已曾被註記reversal,請確認是否還要reversal !",question!,okcancel!)
//			if li_ret = 2 then //cancel
//				dw_query.object.reversal_flag[row] = 'Y'   //only used by w_actm120, indicate only enquiry
//			else
//				dw_query.object.reversal_flag[row] = 'N'   //only used by w_actm120, indicate add reversal amt 
//			end if
//		end if
		
		if (dataReadAcaj() == false) {
			if (dataReadDebt() == false) {
				if (dataReadDebtHst() == false) {
				//alert_err("查無資料, reference_no="+m_reference_no+", rowid= " + m_rowid);
		      alertMsg("無法取得 交易參考號 ! reference_no="+mReferenceNo);
				}
			}
		}
		
		wp.colSet("tt_acct_code", wfGetAcctName(wp.colStr("acct_code")));
		wp.colSet("reversal_flag", mReversalFlag);
		wp.colSet("jrnl_rowid", mRowid);
	}
	
	boolean dataReadAcaj() throws Exception {
		
		wp.selectSQL = " hex(rowid) as rowid, " +
				" acct_type, " +
				" uf_acno_key(p_seqno) as acct_key, " +
				" uf_acno_name(p_seqno) as acno_name, " +
				" p_seqno, " +
				" curr_code, " +
				" card_no, " +
				" reference_no, " +
    			" acct_code, " +
    			" function_code, " +
    			" post_date, " +
    			" interest_date, " +
    			" 'Y' as ex_dcount, " +
    			" apr_flag, " +
    			" process_flag, " +
    			" adjust_type, " +
    			" dc_orginal_amt, " +
    			" dc_dr_amt, " +
	      		" dc_cr_amt, " +
    			" dc_bef_amt, " +
	      		" dc_aft_amt, " +
	      		" dc_bef_d_amt, " +
	      		" dc_aft_d_amt, " +
	      		" orginal_amt, " +
	      		" dr_amt, " +
	      		" cr_amt, " +
	      		" bef_amt, " +
	      		" aft_amt, " +
	      		" bef_d_amt, " +
	      		" aft_d_amt, " +
	      		" value_type, " +
	      		" adj_reason_code, " +
	      		" adj_comment, " +
	      	//" c_debt_key, " +
	      	//" debit_item, " +
	      		" trans_acct_type, " +
	      		" trans_acct_key, " +
	      		" mod_seqno " ;
	      		
//	      		" cash_type, " +
//	      		" update_date, " +
//	      		" update_user, " +
//	      		" mod_user, " +
//	      		" mod_time, " +
//	      		" mod_pgm, " +
//	      		" mod_seqno, " +
//	      		" 'Y' as ex_dcount, " +
//	      		" lpad(' ',30,' ') as ex_cname, " +
//	      		" lpad(' ',40,' ') as ex_corp_cname, " +
//	      		" job_code, " +
//	      		" vouch_job_code, " +
      
		wp.daoTable = "act_acaj";
		wp.whereStr = "where reference_no = :reference_no "
		            + "and substr(adjust_type,1,2) = 'DR' ";
		setString("reference_no", mReferenceNo);

		wp.whereOrder=" ";

		pageSelect();
		if (sqlNotFind()) {
			return false;
		}
		//★ modified on 2019/07/09
		if (eqIgno(wp.colStr("process_flag"),"Y")) {  
			return false;
		}


		if (!eqIgno(wp.colStr("curr_code"),"840")) {  //整數
			wp.colSet("ex_dc_cr_amt", commString.numFormat(wp.colNum("dc_cr_amt"),"#,##0"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(wp.colNum("dc_aft_amt"),"#,##0"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(wp.colNum("dc_aft_d_amt"),"#,##0"));
			wp.colSet("ex_dc_orginal_amt", commString.numFormat(wp.colNum("dc_orginal_amt"),"#,##0"));
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0"));
		} else {  //小數兩位
			wp.colSet("ex_dc_cr_amt", commString.numFormat(wp.colNum("dc_cr_amt"),"#,##0.00"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(wp.colNum("dc_aft_amt"),"#,##0.00"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(wp.colNum("dc_aft_d_amt"),"#,##0.00"));
			wp.colSet("ex_dc_orginal_amt", commString.numFormat(wp.colNum("dc_orginal_amt"),"#,##0.00"));
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0.00"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0.00"));
		}
		
		return true;
	}
	
	boolean dataReadDebt() throws Exception {

		wp.selectSQL = " '' as rowid, " +
				" acct_type, " +
				" uf_acno_key(p_seqno) as acct_key, " +
				" uf_acno_name(p_seqno) as acno_name, " +
				" p_seqno, " +
				" curr_code, " +
				" card_no, " +
				" reference_no, " +
    			" acct_code, " +
    			" 'U' as function_code, " +
    			" post_date, " +
    			" interest_date, " +
    			" 'N' as ex_dcount, " +
    		//" apr_flag, " +
    			" bill_type, " +
    			" beg_bal as orginal_amt, " +
    			" 0 as dr_amt, " +
    			" 0 as cr_amt, " +
    			" end_bal as bef_amt, " +
    			" end_bal as aft_amt, " +
    			" d_avail_bal as bef_d_amt, " +
    			" d_avail_bal as aft_d_amt, " +
    			" uf_dc_amt(curr_code,beg_bal,dc_beg_bal) as dc_orginal_amt, " +
    			" 0 as dc_dr_amt, " +
    			" 0 as dc_cr_amt, " +
    			" uf_dc_amt(curr_code,end_bal,dc_end_bal) as dc_bef_amt, " +
    			" uf_dc_amt(curr_code,end_bal,dc_end_bal) as dc_aft_amt, " +
    			" uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as dc_bef_d_amt, " +
    			" uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as dc_aft_d_amt " ;
//    			" dc_beg_bal as dc_orginal_amt, " +
//    			" 0 as dc_dr_amt, " +
//    			" 0 as dc_cr_amt, " +
//    			" dc_end_bal as dc_bef_amt, " +
//    			" dc_end_bal as dc_aft_amt, " +
//    			" dc_d_avail_bal as dc_bef_d_amt, " +
//    			" dc_d_avail_bal as dc_aft_d_amt ";
		
		wp.daoTable = "act_debt";
		wp.whereStr = "where reference_no = :reference_no " ;
		setString("reference_no", mReferenceNo);

		wp.whereOrder=" ";

		pageSelect();
		if (sqlNotFind()) {
			return false;
		}

		wp.colSet("value_type", "3");
	//wp.col_set("adj_reason_code", "1");
		wp.colSet("adj_reason_code", "2");
  //wp.col_set("debit_item", "14817000");
	//wp.colSet("debit_item", "24817000");
		wp.colSet("pho_disable", "disabled style='background-color: lightgray;'");
		
		double dCrAmt, dAftAmt, dAftDAmt, dDcCrAmt, dDcAftAmt, dDcAftDAmt;
		dCrAmt = toNum(mTransactionAmt);
		dAftAmt = wp.colNum("aft_amt") + toNum(mTransactionAmt);
		dAftDAmt = wp.colNum("aft_d_amt") + toNum(mTransactionAmt);
		dDcCrAmt = toNum(mDcTransactionAmt);
		dDcAftAmt = wp.colNum("dc_aft_amt") + toNum(mDcTransactionAmt);
		dDcAftDAmt = wp.colNum("dc_aft_d_amt") + toNum(mDcTransactionAmt);
		wp.colSet("cr_amt", dCrAmt);
		wp.colSet("aft_amt", dAftAmt);
		wp.colSet("aft_d_amt", dAftDAmt);
		wp.colSet("dc_cr_amt", dDcCrAmt);
		wp.colSet("dc_aft_amt", dDcAftAmt);
		wp.colSet("dc_aft_d_amt", dDcAftDAmt);
		
		if (!eqIgno(wp.colStr("curr_code"),"840")) {  //整數
			wp.colSet("ex_dc_cr_amt", commString.numFormat(dDcCrAmt,"#,##0"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(dDcAftAmt,"#,##0"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(dDcAftDAmt,"#,##0"));
			wp.colSet("ex_dc_orginal_amt", commString.numFormat(wp.colNum("dc_orginal_amt"),"#,##0"));
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0"));
		} else {  //小數兩位
			wp.colSet("ex_dc_cr_amt", commString.numFormat(dDcCrAmt,"#,##0.00"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(dDcAftAmt,"#,##0.00"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(dDcAftDAmt,"#,##0.00"));
			wp.colSet("ex_dc_orginal_amt", commString.numFormat(wp.colNum("dc_orginal_amt"),"#,##0.00"));
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0.00"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0.00"));
		}
		
		return true;
	}
	
	boolean dataReadDebtHst() throws Exception {
		
		wp.selectSQL = " '' as rowid, " +
				" acct_type, " +
				" uf_acno_key(p_seqno) as acct_key, " +
				" uf_acno_name(p_seqno) as acno_name, " +
				" p_seqno, " +
				" curr_code, " +
				" card_no, " +
				" reference_no, " +
    			" acct_code, " +
    			" 'U' as function_code, " +
    			" post_date, " +
    			" interest_date, " +
    			" 'N' as ex_dcount, " +
    		//" apr_flag, " +
    			" bill_type, " +
    			" beg_bal as orginal_amt, " +
    			" 0 as dr_amt, " +
    			" 0 as cr_amt, " +
    			" end_bal as bef_amt, " +
    			" end_bal as aft_amt, " +
    			" d_avail_bal as bef_d_amt, " +
    			" d_avail_bal as aft_d_amt, " +
    			" uf_dc_amt(curr_code,beg_bal,dc_beg_bal) as dc_orginal_amt, " +
    			" 0 as dc_dr_amt, " +
    			" 0 as dc_cr_amt, " +
    			" uf_dc_amt(curr_code,end_bal,dc_end_bal) as dc_bef_amt, " +
    			" uf_dc_amt(curr_code,end_bal,dc_end_bal) as dc_aft_amt, " +
    			" uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as dc_bef_d_amt, " +
    			" uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as dc_aft_d_amt " ;
    			
//    			" dc_beg_bal as dc_orginal_amt, " +
//    			" 0 as dc_dr_amt, " +
//    			" 0 as dc_cr_amt, " +
//    			" dc_end_bal as dc_bef_amt, " +
//    			" dc_end_bal as dc_aft_amt, " +
//    			" dc_d_avail_bal as dc_bef_d_amt, " +
//    			" dc_d_avail_bal as dc_aft_d_amt ";
		
		wp.daoTable = "act_debt_hst";
		wp.whereStr = "where reference_no = :reference_no " ;
		setString("reference_no", mReferenceNo);

		wp.whereOrder=" ";

		pageSelect();
		if (sqlNotFind()) {
			return false;
		}

		wp.colSet("value_type", "3");
	//wp.col_set("adj_reason_code", "1");
		wp.colSet("adj_reason_code", "2");
	//wp.col_set("debit_item", "14817000");
	//wp.colSet("debit_item", "24817000");
		wp.colSet("pho_disable", "disabled style='background-color: lightgray;'");
		
		double dCrAmt, dAftAmt, dAftDAmt, dDcCrAmt, dDcAftAmt, dDcAftDAmt;
		dCrAmt = toNum(mTransactionAmt);
		dAftAmt = wp.colNum("aft_amt") + toNum(mTransactionAmt);
		dAftDAmt = wp.colNum("aft_d_amt") + toNum(mTransactionAmt);
		dDcCrAmt = toNum(mDcTransactionAmt);
		dDcAftAmt = wp.colNum("dc_aft_amt") + toNum(mDcTransactionAmt);
		dDcAftDAmt = wp.colNum("dc_aft_d_amt") + toNum(mDcTransactionAmt);
		wp.colSet("cr_amt", dCrAmt);
		wp.colSet("aft_amt", dAftAmt);
		wp.colSet("aft_d_amt", dAftDAmt);
		wp.colSet("dc_cr_amt", dDcCrAmt);
		wp.colSet("dc_aft_amt", dDcAftAmt);
		wp.colSet("dc_aft_d_amt", dDcAftDAmt);
		
		if (!eqIgno(wp.colStr("curr_code"),"840")) {  //整數
			wp.colSet("ex_dc_cr_amt", commString.numFormat(dDcCrAmt,"#,##0"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(dDcAftAmt,"#,##0"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(dDcAftDAmt,"#,##0"));
			wp.colSet("ex_dc_orginal_amt", commString.numFormat(wp.colNum("dc_orginal_amt"),"#,##0"));
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0"));
		} else {  //小數兩位
			wp.colSet("ex_dc_cr_amt", commString.numFormat(dDcCrAmt,"#,##0.00"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(dDcAftAmt,"#,##0.00"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(dDcAftDAmt,"#,##0.00"));
			wp.colSet("ex_dc_orginal_amt", commString.numFormat(wp.colNum("dc_orginal_amt"),"#,##0.00"));
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0.00"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0.00"));
		}
		
		return true;
	}
	
	int ofValidation() throws Exception {
    	double lmDrAmt1, lmDrAmt2;
    	double lmAmt=0, lmAmt2=0;
    	long llCnt;
    	String lsPSeqno="", lsCurrCode="";
    	String lsTransAcctType="", lsTransAcctKey="";
    	String lsDeptno, lsGlcode;
    	
    	if (eqIgno(wp.itemStr2("apr_flag"),"Y")){
			alertErr("此筆已放行不可再調整!!");
			return -1;
		}
    	
    	if (eqIgno(strAction,"D")) return 1;
		
    	lsPSeqno = wp.itemStr2("p_seqno");
    	lsCurrCode = wp.itemStr2("curr_code");    	
    	if (empty(lsPSeqno) || empty(lsCurrCode)) {
			alertErr("帳戶帳號, 結算幣別  不可空白");
			return -1;
		}
    	
    	if (empty(wp.itemStr2("post_date"))) {
			alertErr("Payment value date 不可空白");
			return -1;
		}
    	
    	String lsSql = "select count(*) as ll_cnt from act_acct_curr "
				+ "where p_seqno = :p_seqno and curr_code = :curr_code ";
    	setString("p_seqno", lsPSeqno);
    	setString("curr_code", lsCurrCode);
    	sqlSelect(lsSql);
    	llCnt = (long) sqlNum("ll_cnt");
    	if (llCnt<=0) {
    		alertErr("帳戶無結算幣別 ["+lsCurrCode+"] 資料");
			return -1;
    	}
    	
    	//phopho mark 金額無法修改, 這段檢核下去什麼都不能做了?
//    	lm_dr_amt1 = wp.item_num("dc_dr_amt");
//    	if (eq_igno(ls_curr_code,"901")) {
//    		lm_dr_amt2 = lm_dr_amt1;
//    		wp.col_set("dr_amt", num_2str(lm_dr_amt1,"###0"));
//    	} else {
//    		lm_dr_amt2 = wp.item_num("dr_amt");
//    	}
//    	func.vars_set("dr_amt", String.valueOf(lm_dr_amt2));
////    	if io_curr.f_chk_decimal(lm_dr_amt1)=-1 or &
////				io_curr.f_chk_decimal(lm_dr_amt2)=-1 then
////				f_errmsg("轉入金額  不可有小數")
////				return -1
////			end if
//    	if ((lm_dr_amt1==0 && lm_dr_amt2!=0) || (lm_dr_amt1!=0 && lm_dr_amt2==0)) {
//    		alert_err("轉入金額 [結算/台幣]  須同時為0 or 不為0");
//			return -1;
//    	}
//    	if ((lm_dr_amt1<=0 || lm_dr_amt2<=0) ) {
//    		alert_err("轉入金額  須 >0");
//			return -1;
//    	}
//    	
//    	lm_amt = wp.item_num("dc_bef_amt") - lm_dr_amt1;
//    	lm_dr_amt1 = wp.item_num("bef_amt") - lm_dr_amt2;
//    	if ((lm_amt<0 || lm_amt2<0) ) {
//    		alert_err("轉入金額  不可大於溢付款餘額 !");
//			return -1;
//    	}
//    	wp.col_set("dc_aft_amt", num_2str(lm_amt,"###0"));
//    	wp.col_set("dc_aft_d_amt", wp.item_ss("dc_bef_d_amt"));
//    	wp.col_set("aft_amt", num_2str(lm_amt2,"###0"));
//    	wp.col_set("aft_d_amt", wp.item_ss("bef_d_amt"));
//    	func.vars_set("dc_aft_amt", String.valueOf(lm_amt));
//    	func.vars_set("dc_aft_d_amt", wp.item_ss("dc_bef_d_amt"));
//    	func.vars_set("aft_amt", String.valueOf(lm_amt2));
//    	func.vars_set("aft_d_amt", wp.item_ss("bef_d_amt"));
    	
    	//todo trans_acct_type, trans_acct_key 從哪來?
//    	ls_trans_acct_type = wp.item_ss("trans_acct_type");
//    	ls_trans_acct_key  = wp.item_ss("trans_acct_key");
//    	if (empty(ls_trans_acct_type) || empty(ls_trans_acct_key)) {
//			alert_err("轉入帳號 不可空白");
//			return -1;
//		}
//    	
//    	ls_sql = "select count(*) as ll_cnt from act_acct_curr a, act_acno b "
//				+ "where a.p_seqno = b.p_seqno and a.curr_code = :curr_code "
//				+ "and b.acct_type = :acct_type and b.acct_key = :acct_key ";
//    	setString("curr_code", ls_curr_code);
//    	setString("acct_type", ls_trans_acct_type);
//    	setString("acct_key", ls_trans_acct_key);
//    	sqlSelect(ls_sql);
//    	ll_cnt = (long) sql_num("ll_cnt");
//    	if (ll_cnt<=0) {
//    		alert_err("轉入帳戶帳號+結算幣別  不存在 !!");
//			return -1;
//    	}

    	lsSql = "select a.usr_deptno, b.gl_code from sec_user a, ptr_dept_code b "
				+ "where b.dept_code = a.usr_deptno and a.usr_id = :usr_id ";
    	setString("usr_id", wp.loginUser);
    	sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
    		alertErr("無法取得 使用者部門代碼, 起帳部門代碼 !!");
			return -1;
    	}
	//ls_deptno = sql_ss("usr_deptno").substring(0, 2);
    lsDeptno = commString.mid(sqlStr("usr_deptno"), 0,2);
		lsGlcode = empty(sqlStr("gl_code"))? "0" : "0"+sqlStr("gl_code").substring(0,1) ;
		wp.colSet("job_code", lsDeptno);
		wp.colSet("vouch_job_code", lsGlcode);
    	func.varsSet("job_code", lsDeptno);
    	func.varsSet("vouch_job_code", lsGlcode);
    	
    	return 1;
    }

	@Override
	public void saveFunc() throws Exception {
		func = new Actm0120Func(wp);

		if (ofValidation() < 0 ) return;
		
		rc = func.dbSave(strAction);
		if (rc != 1) {
			alertErr2(func.getMsg());
			return;
		}
		this.sqlCommit(rc);
		
		if (strAction.equals("U")) {
			//update act_jrnl
			if (empty(wp.itemStr2("rowid"))) {  //新增時
				if (func.updateAcctJrnl("Y") < 0) {
					alertErr("流水帳務資料檔，更新失敗 ?!");
		        	sqlCommit(0);
					return;
				}
			}
			mReferenceNo  = wp.itemStr2("reference_no");
			dataRead();
		}
		
		if (strAction.equals("D")) {
			if (func.updateAcctJrnl("") < 0) {
				alertErr("流水帳務資料檔，更新失敗 ?!");
	        	sqlCommit(0);
				return;
			}
		}
	}

	@Override
	public void initButton() {

		String sKey = "2nd-page";
		if (wp.respHtml.indexOf("_detl") > 0) {
			 this.btnModeAud(sKey);
		}
		
	}

	@Override
	public void dddwSelect() {
		try {
			wp.optionKey = wp.itemStr2("ex_acct_type");
			dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_curr_code");
			dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
		} catch (Exception ex) {
		}
	}

	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}
}
