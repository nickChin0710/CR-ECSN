/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 2022-0329	 JH		nvl(curr_code,'901')                                       *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-04-28  V1.00.04  Simon      avoid sqlStrend() error                    * 
* 112-07-04  V1.00.05  Simon      1.TCB溢付款退款客製化                      * 
*                                 2.parms revised in checkApprove(x,x)       * 
* 112-08-14  V1.00.06  Simon      馬爺說取消excel產出之覆核                  * 
*****************************************************************************/

package actm01;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;

public class Actm0100 extends BaseEdit {
	final String MPROGNAME = "actm0100";
	CommString commString = new CommString();
	String mAccttype = "";
	String mAcctkey = "";
	String mCardno = "";
	String mCurrcode = "";
	String pPSeqno = "";
	Actm0100Func func;

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
		} else if (eqIgno(wp.buttonCode, "R1")) {
			// -資料讀取-
			strAction = "R1";
			dataRead1();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			strAction = "A";
			saveFunc();
			// insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			strAction = "U";
			saveFunc();
			// updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			strAction = "D";
			saveFunc();
			// deleteFunc();
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
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
			// -check approve-
		//if (!checkApprove(wp.itemStr2("zz_apr_user"), wp.itemStr2("zz_apr_passwd"))) {
/*** 馬爺說取消excel產出之覆核
			if (!checkApprove(wp.itemStr2("approval_user"), wp.itemStr2("approval_passwd"))) {
				wp.respHtml = "TarokoErrorPDF";
				return;
			}
***/
			strAction = "XLS";
			xlsPrint();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		wp.colSet("dc_dr_amt", "0");
		wp.colSet("dr_amt", "0");
		wp.colSet("wk_tot_bef_amt", "0");
		wp.colSet("wk_tot_aft_amt", "0");
		wp.colSet("dc_bef_amt", "0");
		wp.colSet("dc_aft_amt", "0");
		wp.colSet("sum_ct", "0");
		wp.colSet("sum_tot_bef_amt", "0");
		wp.colSet("sum_dc_dr_amt", "0");
	//wp.colSet("ex_date1", getSysDate());
		// 載入當前營業日
		String lsSql = "";
		lsSql = "select business_date from ptr_businday "
				  + "fetch first 1 rows only";
		try {
			sqlSelect(lsSql);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wp.colSet("ex_date1", sqlStr("business_date"));
	//if (wp.respHtml.indexOf("_detl") > 0) {
	//	wp.colSet("cash_type", "1");
	//}
		wp.colSet("ex_curr_code", "901");
		// 載入當前匯率
		lsSql = "select exchange_rate from PTR_CURR_RATE where 1=1 "
				+ "and curr_code ='901'";
		try {
			sqlSelect(lsSql);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wp.colSet("exchange_rate", sqlStr("exchange_rate"));
	}

	@Override
	public void queryFunc() throws Exception {
		// 判斷摘要代碼是否為空值
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		/***
		 * if (wp.item_ss("ex_acct_key").length() < 6) { //phopho add
		 * err_alert("帳戶帳號至少要6碼"); return; }
		 ***/

		String lsAcctKey = "";
		if (!empty(wp.itemStr2("ex_acct_key"))) {
			lsAcctKey = commString.acctKey(wp.itemStr2("ex_acct_key"));
			if (lsAcctKey.length() != 11) {
				alertErr2("帳戶帳號:輸入錯誤");
				return;
			}
		}

		wp.sqlCmd = "select hex(a.rowid) as rowid,"
				+ "a.p_seqno, "
			  + "a.acct_type, "
			  + "b.acct_key, "
				+ "a.acct_type||'-'||b.acct_key as wk_acctkey, " 
				+ "uf_acno_name(a.p_seqno) as cname, " 
				+ "uf_nvl(a.curr_code,'901') as curr_code,"
				+ "uf_dc_amt(curr_code,dr_amt,   dc_dr_amt   ) dc_dr_amt, "
				+ "(uf_dc_amt(curr_code,bef_amt, dc_bef_amt  ) + uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt)) as wk_tot_bef_amt, "
				+ "a.crt_date, "
				+ "a.crt_time, "
				+ "substr(a.crt_date,1,4)||'/'||substr(a.crt_date,5,2)||'/'||substr(a.crt_date,7,2) as update_date, "
				+ "substr(a.crt_time,1,2)||':'||substr(a.crt_time,3,2)||':'||substr(a.crt_time,5,2) as update_time, "
				+ "a.adjust_type, "
				+ "('act_acaj'||'-'||a.adjust_type) tt_which_table, "
				+ "a.post_date, "
				+ "a.cash_type, "
				+ "a.trans_acct_type, "
				+ "a.trans_acct_key, "
				+ "a.mcht_no as bank_no, "
				+ "a.adj_comment, "
				+ "a.apr_flag,"
				+ "uf_acno_name(a.p_seqno) db_cname, "
				+ "uf_idno_id(b.id_p_seqno) id_no, "
			//+ "to_char(a.mod_time,'YYYY/MM/DD HH24:Mi:SS') db_mod_time ";
				+ "(a.crt_date||'-'||a.crt_time) crt_datetime ";
		wp.sqlCmd += "from act_acaj a, act_acno b ";
		wp.sqlCmd += "WHERE 1=1 "
				// + "and a.adjust_type like 'OP%' ";
				+ "and a.p_seqno = b.acno_p_seqno "
				+ "and a.adjust_type = 'OP02' "
				+ "and a.process_flag != 'Y' ";
		wp.sqlCmd += sqlCol(lsAcctKey, "b.acct_key", "like%");
		if (empty(wp.itemStr2("ex_curr_code")) == false) {
			wp.sqlCmd += " and decode(curr_code,'','901', curr_code) = :ex_curr_code ";    
			setString("ex_curr_code", wp.itemStr2("ex_curr_code"));
		}
	//wp.sqlCmd += sqlStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2"), "a.crt_date");
		if (empty(wp.itemStr("ex_date1")) == false) {
			wp.sqlCmd += " and a.crt_date >= :ex_date1 ";    
			setString("ex_date1", wp.itemStr("ex_date1"));
		}
		if (empty(wp.itemStr("ex_date2")) == false) {
			wp.sqlCmd += " and a.crt_date <= :ex_date2 ";    
			setString("ex_date2", wp.itemStr("ex_date2"));
		}

		switch (wp.itemStr2("ex_kind")) {
		case "2":
			wp.sqlCmd += " and (a.apr_flag = '' or a.apr_flag ='N') ";
			break;
		case "1":
			wp.sqlCmd += " and a.apr_flag = 'Y' ";
			break;
		}
		wp.sqlCmd += " ORDER BY a.crt_date desc, a.crt_time desc,a.acct_type ASC, "
				+ " b.acct_key ASC ";
		// ddd("sqlCmd :"+wp.sqlCmd);
		wp.pageCountSql = "select count(*) from (";
		wp.pageCountSql += wp.sqlCmd;
		wp.pageCountSql += ")";
		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			wp.colSet("sum_ct", "0");
			wp.colSet("sum_tot_bef_amt", "0");
			wp.colSet("sum_dc_dr_amt", "0");
			return;
		}

		listWkdata();
		wp.setPageValue();
	}

	void listWkdata() throws Exception {
		String ss = "", ss1 = "", ss2 = "";
	//String[] cde = new String[] { "1", "2", "3", "4", "5" };
	//String[] txt = new String[] { "1.溢付提領", "2.開立即期支票(華銀支票號碼)", "3.匯入本行帳號", "4.匯入它行帳號", "5.CRS溢繳款超過4萬美金" };
		String[] cde = new String[] { "1", "2", "3", "4" };
		String[] txt = new String[] { "1.隔日轉入本行帳號", "2.當日轉入本行帳號", 
			"3.當日轉入他行帳號", "4.其他" };
		String lsPSeqno = "", lsAcctType = "", lsCurrCode = "", lsCrtDatetime = "";
		String lsSql = "",lsAprFlag="";
		// 計算總筆數,金額
		lsSql = "select count(*) sum_ct,"
				+ "sum(uf_dc_amt(curr_code,dr_amt,dc_dr_amt)) sum_dc_dr_amt ,"
				+ "sum((uf_dc_amt(curr_code,bef_amt, dc_bef_amt  ) + uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt))) sum_tot_bef_amt "
				+ "from act_acaj a,act_acno b "
				+ "where 1=1 "
				+ "and a.p_seqno = b.acno_p_seqno "
				+ "and a.process_flag != 'Y' "
				+ "and a.adjust_type = 'OP02' ";
		lsSql += sqlCol(wp.itemStr2("ex_acct_key"), "b.acct_key", "like%");
		if (empty(wp.itemStr2("ex_curr_code")) == false) {
			lsSql += " and decode(curr_code,'','901', curr_code) = :ex_curr_code ";
			setString("ex_curr_code", wp.itemStr2("ex_curr_code"));
		}
	//lsSql += sqlStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2"), "a.crt_date");
		if (empty(wp.itemStr("ex_date1")) == false) {
			lsSql += " and a.crt_date >= :ex_date1 ";    
			setString("ex_date1", wp.itemStr("ex_date1"));
		}
		if (empty(wp.itemStr("ex_date2")) == false) {
			lsSql += " and a.crt_date <= :ex_date2 ";    
			setString("ex_date2", wp.itemStr("ex_date2"));
		}

		switch (wp.itemStr2("ex_kind")) {
		case "2":
			lsSql += " and a.apr_flag in ('','N') ";
			break;
		case "1":
			lsSql += " and a.apr_flag = 'Y' ";
			break;
		}
		log("ls_sql :" + lsSql);
		sqlSelect(lsSql);
		if (sqlNum("sum_ct") > 0) {
			wp.colSet("sum_ct", sqlStr("sum_ct"));
			wp.colSet("sum_tot_bef_amt", sqlStr("sum_tot_bef_amt"));
			wp.colSet("sum_dc_dr_amt", sqlStr("sum_dc_dr_amt"));
		}

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			lsPSeqno = wp.colStr(ii, "p_seqno");
			lsAcctType = wp.colStr(ii, "acct_type");
			lsCurrCode = wp.colStr(ii, "curr_code");
			lsCrtDatetime = wp.colStr(ii, "crt_datetime");
			lsAprFlag = wp.colStr(ii, "apr_flag");
			lsSql = " select  max(crt_date||'-'||crt_time) max_datetime from act_acaj "
					   + " where 1=1 "
				     + " and process_flag != 'Y' "
				     + " and adjust_type like 'OP%' ";
			lsSql += sqlCol(lsPSeqno, "p_seqno");
			lsSql += sqlCol(lsCurrCode, "uf_nvl(curr_code,'901')");
			lsSql += sqlCol(lsAcctType, "acct_type");

			sqlSelect(lsSql);
			if (sqlStr("max_datetime").equals(lsCrtDatetime)) {
				if(lsAprFlag.equals("N") || empty(lsAprFlag)){
					wp.colSet(ii, "mod_type", "可異動");
				} else {
					wp.colSet(ii, "mod_type", "不可異動");
				}				
			} else {
				wp.colSet(ii, "mod_type", "不可異動");
			}
			
			wp.colSet(ii, "tt_which_table", "act_acaj" + "--" + wp.colStr(ii, "adjust_type"));
			ss = wp.colStr(ii, "cash_type");
			wp.colSet(ii, "tt_cash_type", commString.decode(ss, cde, txt));

			ss1 = wp.colStr(ii, "bank_no");
			ss2 = wp.colStr(ii, "trans_acct_key");
    //if (ss.equals("1") || ss.equals("2") || ss.equals("3")) {
      if ( Arrays.asList("1","2","3").contains(ss) ) {
	      wp.colSet(ii, "trans_acct_key", ss1+"-"+ss2);
      } else if (ss.equals("4")) {
	      wp.colSet(ii, "trans_acct_key", wp.colStr(ii,"adj_comment"));
		  }

			wp.colSet(ii, "tt_update_time", wp.colStr(ii, "update_date") + " " + wp.colStr(ii, "update_time"));

		}
	}

	@Override
	public void querySelect() throws Exception {
		mAccttype = wp.itemStr2("data_k1");
		mAcctkey = wp.itemStr2("data_k2");
		mCurrcode = wp.itemStr2("data_k3");
		String lsModType = wp.itemStr2("data_k4");
		wp.colSet("kk_acct_type", mAccttype);
		wp.colSet("kk_acct_key", mAcctkey);
		wp.colSet("kk_curr_code", mCurrcode);
		wp.colSet("mod_type", lsModType);
		if (!lsModType.equals("可異動")) {
			wp.colSet("set_btn", "disabled='disabled'");
		}
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {

	  if (wfGetPseqno() < 0)
		  return;

		wp.selectSQL = " hex(a.rowid) as rowid, a.mod_seqno, a.apr_flag ,"
				+ " a.p_seqno, "
				+ " a.acct_type, "
				+ " b.acct_key, "
				+ " a.card_no, "
				+ " uf_nvl(a.curr_code,'901') as curr_code, "
				+ " a.dr_amt, "
				+ " a.cr_amt, "
				+ " a.bef_amt, "
				+ " a.aft_amt, "
				+ " a.bef_d_amt, "
				+ " a.aft_d_amt, "
				+ " a.cash_type, "
				+ " a.post_date, "
				+ " a.trans_acct_type, "
				+ " a.trans_acct_key, "
				+ " a.mcht_no as bank_no, "
				+ " a.adj_comment, "
				+ " a.adjust_type, "
				+ " uf_acno_name(a.p_seqno) as db_name, "
				+ " uf_dc_amt(a.curr_code,a.dr_amt,a.dc_dr_amt) as dc_dr_amt, "
				+ " uf_dc_amt(a.curr_code,a.dr_amt,a.dc_dr_amt) as keep_dc_dr_amt, "
				+ " uf_dc_amt(a.curr_code,a.cr_amt,a.dc_cr_amt) as dc_cr_amt, "
				+ " uf_dc_amt(a.curr_code,a.bef_amt,a.dc_bef_amt) as dc_bef_amt, "
				+ " uf_dc_amt(a.curr_code,a.aft_amt,a.dc_aft_amt) as dc_aft_amt, "
				+ " uf_dc_amt(a.curr_code,a.aft_amt,a.dc_aft_amt) as keep_dc_aft_amt, "
				+ " uf_dc_amt(a.curr_code,a.bef_d_amt,a.dc_bef_d_amt) as dc_bef_d_amt, "
				+ " uf_dc_amt(a.curr_code,a.aft_d_amt,a.dc_aft_d_amt) as dc_aft_d_amt ";

		wp.daoTable = "act_acaj a, act_acno b";
		wp.whereStr = "where a.p_seqno = b.acno_p_seqno ";
		wp.whereStr += "and a.adjust_type = 'OP02' ";
		wp.whereStr += "and a.process_flag != 'Y' ";
		wp.whereStr += "and a.p_seqno = :p_seqno ";
		wp.whereStr += "and uf_nvl(a.curr_code,'901') = :curr_code "
				+ "and hex(a.rowid) =:rowid ";
		setString("p_seqno", pPSeqno);
		setString("curr_code", mCurrcode);
		setString("rowid", wp.itemStr2("data_k5"));		

		pageSelect();
		
		String lsSqlA = "select count(*) as ll_cnt_a from act_acaj "
				+ "where p_seqno = :p_seqno and uf_nvl(curr_code,'901') = :curr_code "
				+ "and substr(adjust_type,1,2) = 'OP' "
				+ "and process_flag != 'Y'   ";
		setString("p_seqno", pPSeqno);
		setString("curr_code", mCurrcode);
		sqlSelect(lsSqlA);
		wp.colSet("ex_dcount", sqlNum("ll_cnt_a"));

		wp.colSet("wk_tot_bef_amt", wp.colNum("dc_bef_amt") + wp.colNum("dc_bef_d_amt") + "");
		wp.colSet("wk_tot_aft_amt", wp.colNum("dc_aft_amt") + wp.colNum("dc_aft_d_amt") + "");
		//重新載入匯率
		String lsSql = "";
		lsSql = "select exchange_rate from PTR_CURR_RATE where 1=1 "
				+ "and curr_code =:curr_code";
		setString("curr_code", wp.colStr("curr_code"));
		sqlSelect(lsSql);
		wp.colSet("exchange_rate", sqlStr("exchange_rate"));
 	  double lbDrAmt = wp.colNum("dc_dr_amt") * sqlNum("exchange_rate");
		long cvtLong = (long) Math.round(lbDrAmt + 0.0000001);
		lbDrAmt = ((double) cvtLong);
		wp.colSet("new_dr_amt", lbDrAmt+"");

		dataReadAfter();
		
		// 有異動權限者，讀出明細資料後，將鍵值存至 kp_ 並設定防止鍵值輸入屬性
		if (wp.autUpdate()) {
			if (wp.itemEq("adjust_type", "OP02") || (wp.colNum("dc_bef_amt") > 0)) {
				wp.colSet("kp_acct_type", mAccttype);
				wp.colSet("kp_acct_key", mAcctkey);
				wp.colSet("kp_card_no", mCardno);
				wp.colSet("kp_curr_code", mCurrcode);
				wp.colSet("kk_acct_type_attr", "disabled");
				wp.colSet("kk_acct_key_attr", "disabled");
				wp.colSet("kk_card_no_attr", "disabled");
				wp.colSet("kk_curr_code_attr", "disabled");
			//btnOn_query(false);
			}
		}

	}

	int wfGetPseqno() throws Exception {
		String lsSql = "";

		if (empty(mAccttype)) {
			mAccttype = wp.itemStr2("kk_acct_type");
		}
		if (empty(mAcctkey)) {
			mAcctkey = fillZeroAcctKey(wp.itemStr2("kk_acct_key"));
		}
		if (empty(mCardno)) {
			mCardno = wp.itemStr2("kk_card_no");
		}
		if (empty(mCurrcode)) {
			mCurrcode = wp.itemStr2("kk_curr_code");
		}

		if (empty(mAcctkey) && empty(mCardno)) {
			alertErr2("帳號, 卡號不可均為空白");
			return -1;
		}

		if (!empty(mAcctkey) && !empty(mCardno)) {
			alertErr2("帳號與卡號不可同時輸入!!");
			return -1;
		}

		if (empty(mCurrcode)) {
			alertErr2("結算幣別不可為空白");
			return -1;
		}

		// 以acct_type, acct_key 優先查詢
		if (empty(mAcctkey) == false) {
			if (empty(mAccttype)) {
				alertErr2("請輸入帳號代碼");
				return -1;
			}
			lsSql = " select p_seqno from act_acno ";
			lsSql += " where acct_type = :acct_type and acct_key like :acct_key ";
			lsSql += " and acno_p_seqno = p_seqno ";
			setString("acct_type", mAccttype);
			setString("acct_key", mAcctkey + "%");
		} else {
			lsSql = " select p_seqno from act_acno ";
			lsSql += " where p_seqno in (select p_seqno from crd_card where card_no = :card_no) ";
			lsSql += " and acno_p_seqno = p_seqno ";
			setString("card_no", mCardno);
		}

		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			pPSeqno = sqlStr("p_seqno");
			wp.colSet("p_seqno", pPSeqno);
		} else {
			if (empty(mAcctkey) == false) {
				alertErr2("此帳號不存在!");
			} else {
				alertErr2("此卡號不存在!");
			}
			return -1;
		}
		return 0;
	}

	void dataReadAfter() throws Exception {

		String ss="";
		ss  = wp.colStr("cash_type");
	
    if (ss.equals("1")) {
	     wp.colSet("ctype1_acct_no", wp.colStr("trans_acct_key"));
    } else if (ss.equals("2")) {
	     wp.colSet("ctype2_acct_no", wp.colStr("trans_acct_key"));
    } else if (ss.equals("3")) {
	     wp.colSet("ctype3_bank_no", wp.colStr("bank_no"));
	     wp.colSet("ctype3_acct_no", wp.colStr("trans_acct_key"));
    } else if (ss.equals("4")) {
	     wp.colSet("ctype4_comment",  wp.colStr("adj_comment"));
		}
		
	}

	int ofValidation() throws Exception {
		// .......// todo
		// if (wfChkAcctCurr(p_p_seqno, m_currcode) < 0 ) return -1;
		double lmDcDrAmt = 0, lmDcBefAmt = 0, lmDcAftAmt = 0, lmTotBefAmt = 0, lmTotAftAmt = 0,
		       lmDrAmt = 0, lmBefAmt = 0, lmAftAmt=0;

		if (wp.itemEq("apr_flag", "Y") && wp.itemStr2("adjust_type").equals("OP02")) {
			alertErr2("已覆核不可以異動");
			return -1;
		}

		if (strAction.equals("D")) {
			return 1;
		}

		lmDcDrAmt = wp.itemNum("dc_dr_amt");

		lmTotBefAmt = wp.itemNum("wk_tot_bef_amt");
		lmDcBefAmt = wp.itemNum("dc_bef_amt");
		lmBefAmt = wp.itemNum("bef_amt");

	//ddd("m_currcode : "+m_currcode);
		if(empty(mCurrcode)){
			mCurrcode = wp.itemStr2("kk_curr_code");
		}

	  if (lmDcBefAmt != 0) {
 	    double lbDrAmt = (lmDcDrAmt * lmBefAmt) / lmDcBefAmt;
		  long cvtLong = (long) Math.round(lbDrAmt + 0.0000001);
		  lmDrAmt = ((double) cvtLong);
		} 

		if (lmDcDrAmt <= 0 || lmDrAmt <= 0) {
			alertErr("提領金額 須 >0");
			return -1;
		}

		lmTotAftAmt = lmTotBefAmt - lmDcDrAmt;
		lmDcAftAmt = lmDcBefAmt - lmDcDrAmt;
		lmAftAmt    = lmBefAmt    - lmDrAmt;

    lmTotAftAmt =  convAmtDp2r(lmTotAftAmt);
    lmDcAftAmt =  convAmtDp2r(lmDcAftAmt);
    lmAftAmt    =  convAmtDp2r(lmAftAmt);

		if (lmDcAftAmt < 0 || lmAftAmt < 0) {
				alertErr("提領金額 不可大於溢付款餘額");
				return -1;
		}
			
	//func.vars_set("dc_dr_amt", Double.toString(lm_dc_dr_amt));
		func.varsSet("dr_amt", Double.toString(lmDrAmt));
		log("A:func.vars_num('dr_amt') : "+func.varsNum("dr_amt"));

	  func.varsSet("dc_aft_d_amt", Double.toString(wp.itemNum("dc_bef_d_amt")));
	  func.varsSet("aft_d_amt", Double.toString(wp.itemNum("bef_d_amt")));
		func.varsSet("dc_aft_amt", Double.toString(lmDcAftAmt));
		func.varsSet("aft_amt", Double.toString(lmAftAmt));

		wp.colSet("wk_tot_aft_amt", lmTotAftAmt+"");
		wp.colSet("dc_aft_amt", lmDcAftAmt+"");

		if (!wp.itemStr2("cash_type").equals("1") &&
				!wp.itemStr2("cash_type").equals("2") &&
				!wp.itemStr2("cash_type").equals("3") &&
				!wp.itemStr2("cash_type").equals("4")) {
			alertErr("請選擇提領方式");
			return -1;
		}

		String ss="", ss1="", ss2="";
		ss  = wp.itemStr2("cash_type");
	
    if (ss.equals("1")) {
	  	wp.colSet("ctype2_acct_no", "");
	  	wp.colSet("ctype3_bank_no", "");
	  	wp.colSet("ctype3_acct_no", "");
	  	wp.colSet("ctype4_comment", "");
      ss1=wp.itemStr2("ctype1_acct_no");
	    if (empty(ss1)) {
	  		alertErr("請輸入本行帳號");
		  	return -1;
	    }
	    if (ss1.length() != 13) {
	  		alertErr("本行帳號長度不對");
		  	return -1;
	    }
  		func.varsSet("adj_comment", "");
  		func.varsSet("bank_no", "006");
  		func.varsSet("trans_acct_key", ss1);
   } else if (ss.equals("2")) {
	  	wp.colSet("ctype1_acct_no", "");
	  	wp.colSet("ctype3_bank_no", "");
	  	wp.colSet("ctype3_acct_no", "");
	  	wp.colSet("ctype4_comment", "");
      ss1=wp.itemStr2("ctype2_acct_no");
	    if (empty(ss1)) {
	  		alertErr("請輸入本行帳號");
		  	return -1;
	    }
	    if (ss1.length() != 13) {
	  		alertErr("本行帳號長度不對");
		  	return -1;
	    }
  		func.varsSet("adj_comment", "");
  		func.varsSet("bank_no", "006");
  		func.varsSet("trans_acct_key", ss1);
    } else if (ss.equals("3")) {
	  	wp.colSet("ctype1_acct_no", "");
	  	wp.colSet("ctype2_acct_no", "");
	  	wp.colSet("ctype4_comment", "");
      ss1=wp.itemStr2("ctype3_bank_no");
      ss2=wp.itemStr2("ctype3_acct_no");
	    if (empty(ss1)) {
	  		alertErr("請輸入他行銀行代碼");
		  	return -1;
	    }
	    if (ss1.equals("006")) {
	  		alertErr("請輸入他行銀行代碼");
		  	return -1;
	    }
	    if (chkBankNo(ss1) == -1) {
	  		alertErr("他行銀行代碼錯誤");
		  	return -1;
	    }
	    if (empty(ss2)) {
	  		alertErr("請輸入他行帳號");
		  	return -1;
	    }
  		func.varsSet("adj_comment", "");
  		func.varsSet("bank_no", ss1);
  		func.varsSet("trans_acct_key", ss2);
    } else if (ss.equals("4")) {
	  	wp.colSet("ctype1_acct_no", "");
	  	wp.colSet("ctype2_acct_no", "");
	  	wp.colSet("ctype3_bank_no", "");
	  	wp.colSet("ctype3_acct_no", "");
      ss1=wp.itemStr2("ctype4_comment");
	    if (empty(wp.itemStr2("ctype4_comment"))) {
	  		alertErr("請輸入其他說明");
		  	return -1;
	    }
  		func.varsSet("bank_no", "");
  		func.varsSet("trans_acct_key", "");
  		func.varsSet("adj_comment", ss1);
		}
		
		String sqlSelect = "select a.usr_deptno, b.gl_code "
				+ " from sec_user as a, ptr_dept_code as b "
				+ " where b.dept_code = a.usr_deptno "
				+ " and a.usr_id = :ls_mod_user ";
		setString("ls_mod_user", wp.loginUser);
		sqlSelect(sqlSelect);

/***
		if (sqlRowNum <= 0) {
			alertErr("無法取得 使用者部門代碼, 起帳部門代碼 !!");
			return -1;
		}
***/
		// String ls_deptno = sql_ss("usr_deptno").substring(0, 2);
		String lsDeptno = commString.mid(sqlStr("usr_deptno"), 0, 2);
		String lsGlcode = sqlStr("gl_code");
		func.varsSet("job_code", lsDeptno);
		func.varsSet("vouch_job_code", "0" + strMid(lsGlcode, 0, 1));

		return 1;
	}

	int chkBankNo(String txBankNo) throws Exception {
		int llCnt = 0;

		String lsSql = " select count(*) as ll_cnt from act_ach_bank ";
		lsSql += " where substr(bank_no,1,3) = :bankno ";
		setString("bankno", txBankNo);
		sqlSelect(lsSql);
		llCnt = (int) sqlNum("ll_cnt");
		if (llCnt <= 0) {
			return -1;
		}

		return 1;
	}

	int wfChkAcctCurr(String asPSeqno, String asCurrCode) throws Exception {
		long llCnt = 0;

		String lsSql = " select count(*) as ll_cnt from act_acct_curr ";
		lsSql += " where p_seqno = :p_seqno and curr_code = :curr_code ";
		setString("p_seqno", asPSeqno);
		setString("curr_code", asCurrCode);
		sqlSelect(lsSql);
		llCnt = (long) sqlNum("ll_cnt");
		if (llCnt <= 0) {
			return -1;
		}

		return 1;
	}

	@Override
	public void saveFunc() throws Exception {
		func = new Actm0100Func(wp);

		if ( strAction.equals("A") && empty(wp.itemStr2("p_seqno")) ) {
			alertErr2("請先讀取");
			return;
		}
		
		// 有異動權限者，檢核輸入明細資料前亦需先搬回鍵值並設定防止鍵值輸入屬性
		if (wp.autUpdate() && !strAction.equals("A") ) {
			wp.colSet("kk_acct_type", wp.itemStr2("kp_acct_type"));
			wp.colSet("kk_acct_key", wp.itemStr2("kp_acct_key"));
			wp.colSet("kk_card_no", wp.itemStr2("kp_card_no"));
			wp.colSet("kk_curr_code", wp.itemStr2("kp_curr_code"));
			wp.colSet("kk_acct_type_attr", "disabled");
			wp.colSet("kk_acct_key_attr", "disabled");
			wp.colSet("kk_card_no_attr", "disabled");
			wp.colSet("kk_curr_code_attr", "disabled");
		//btnOn_query(false);
		}

		if (ofValidation() < 0)
			return;

		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc != 1) {
			alertErr2(func.getMsg());
		}
		this.sqlCommit(rc);
		// 計算修改次數
		String lsSqlA = "select count(*) as ll_cnt_a from act_acaj "
				+ "where 1=1 "
				+ "and substr(adjust_type,1,2) = 'OP' "
				+ "and process_flag != 'Y'   ";
		lsSqlA += sqlCol(wp.itemStr2("p_seqno"), "p_seqno");
		lsSqlA += sqlCol(wp.itemStr2("curr_code"), "uf_nvl(curr_code,'901')");
		sqlSelect(lsSqlA);
		wp.colSet("ex_dcount", sqlNum("ll_cnt_a"));

		wp.colSet("p_seqno", "");

	}

	@Override
	public void initButton() {
		// this.btnMode_aud("XX");
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
		if (wp.respHtml.equals("actm0100")) {
		  this.btnModeAud();
		}
		//
		// if (wp.respHtml.indexOf("_detl") > 0) {
		// this.btnMode_aud();
		// // rowid 有值時，新增鍵 off(disabled)，修改鍵、刪除鍵 on(若有權限 on,沒權限 off)
		// // rowid 無值時，新增鍵 on(若有權限 on,沒權限 off)，修改鍵、刪除鍵 off(disabled)
		// if (!empty(wp.col_ss("rowid")) &&
		// !wp.col_ss("adjust_type").equals("OP02") && wp.aut_update()) {
		// this.btnOn_aud(true, false, false); // rowid
		// // 有值但最近一筆acaj非"OP02"時，新增鍵
		// // on，修改鍵、刪除鍵 off(disabled)
		// }
		// // dc_bef_bal 無值時，表示不能新增 act_acaj(提領)，因此新增鍵 off(disabled)
		// if (wp.col_num("dc_bef_amt") == 0) {
		// btnOn_add(false);
		// }
		// }
	}

	@Override
	public void dddwSelect() {
		try {
		//wp.initOption = "--";
		//wp.optionKey = empty(wp.item_ss("kk_acct_type")) ? "01" : wp.item_ss("kk_acct_type");
		//wp.optionKey = wp.item_ss("kk_acct_type");
			wp.optionKey = wp.colStr("kk_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.initOption = "--";
			if (wp.respHtml.indexOf("_add") > 0 || wp.respHtml.indexOf("_detl") > 0) {
				wp.optionKey = empty(wp.colStr("kk_curr_code")) ? "901" : wp.colStr("kk_curr_code");
				// wp.optionKey = wp.col_ss("kk_curr_code");
			} else {
			//wp.optionKey = empty(wp.col_ss("ex_curr_code")) ? "901" : wp.col_ss("ex_curr_code");
				wp.optionKey = wp.colStr("ex_curr_code");
			}
			this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
		} catch (Exception ex) {
		}
	}

	void xlsPrint() throws Exception {
		try {
		 // 寫入Log紀錄檔 
		  if (addApproveLog() != 1) {
			  wp.respHtml = "TarokoErrorPDF";
			  return;
		  }

			log("xlsFunction: started--------");
			// wp.reportId = m_progName;
			wp.reportId = "actm0100";
			// -cond-

			String ss = "";

			if (empty(wp.itemStr2("ex_acct_key")) == false) {
				ss += "帳戶帳號: " + wp.itemStr2("ex_acct_key");
			}
			ss += "  結算幣別: " + wp.itemStr2("ex_curr_code");
			ss += "  建檔日期起迄 : " + wp.itemStr2("ex_date1") + " - " + wp.itemStr2("ex_date2");
			wp.colSet("cond_1", ss);

			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "N";
			// xlsx.report_id ="rskr0020";
			xlsx.excelTemplate = wp.reportId + ".xlsx";
			wp.colSet("IdUser", wp.loginUser);
			// ====================================
			// -明細-
			xlsx.sheetName[0] = "明細";

	 	  wp.pageRows = 9999;
			queryFunc();
		//reportQuery();
			wp.setListCount(1);
			log("Detl: rowcnt:" + wp.listCount[0]);
			xlsx.processExcelSheet(wp);

			xlsx.outputExcel();
			xlsx = null;
			log("xlsFunction: ended-------------");

		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}
	}

	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length() == 8)
			rtn += "000";
		if (acctkey.trim().length() == 10)
			rtn += "0";

		return rtn;
	}

	public void dataRead1() throws Exception {
		String lsSql = "";
		mAccttype = wp.itemStr2("kk_acct_type");
		mAcctkey = wp.itemStr2("kk_acct_key");
		mCardno = wp.itemStr2("kk_card_no");
		mCurrcode = wp.itemStr2("kk_curr_code");

	  wp.colSet("cash_type", "");
	  wp.colSet("ctype1_acct_no", "");
	  wp.colSet("ctype2_acct_no", "");
	  wp.colSet("ctype3_bank_no", "");
	  wp.colSet("ctype3_acct_no", "");
	  wp.colSet("ctype4_comment", "");

		if (empty(mAcctkey) && empty(mCardno)) {
			alertErr2("帳號, 卡號不可均為空白");
			return;
		}

		// 取得p_seqno
		if (!empty(wp.itemStr2("kk_acct_key"))) {
	 	  mAccttype = wp.itemStr2("kk_acct_type");
			lsSql = " select p_seqno from act_acno ";
			lsSql += " where acct_type = :acct_type ";
			lsSql += " and acct_key like :acct_key ";
			lsSql += " and acno_p_seqno = p_seqno ";
			setString("acct_type", mAccttype);
			setString("acct_key", mAcctkey + "%");
		  sqlSelect(lsSql);
		  if (sqlRowNum > 0) {
		  	pPSeqno = sqlStr("p_seqno");
			  wp.colSet("p_seqno", pPSeqno);
			  wp.colSet("kk_acct_type", mAccttype);//dddw_select 會用到 wp.col_ss("kk_acct_type")
		  } else {
			  wp.colSet("p_seqno", "");
		  	alertErr2("此帳戶帳號不存在!");
		  	return;
		  }
		} else if (!empty(wp.itemStr2("kk_card_no"))) {
			lsSql = " select acct_type,acct_key,p_seqno from act_acno ";
			lsSql += " where p_seqno in (select p_seqno from crd_card where card_no = :card_no) ";
			lsSql += " and acno_p_seqno = p_seqno ";
			setString("card_no", mCardno);
		  sqlSelect(lsSql);
		  if (sqlRowNum > 0) {
		  	pPSeqno  = sqlStr("p_seqno");
		  	mAccttype = sqlStr("acct_type");//之後會搬到 kp_acct_type：wp.col_set("kp_acct_type", m_accttype);
		    mAcctkey  = sqlStr("acct_key");
			  wp.colSet("p_seqno", pPSeqno);
		  } else {
			  wp.colSet("p_seqno", "");
		  	alertErr2("此卡號不存在!");
		  	return;
		  }
		}

		// 讀取act_acaj SQL
		lsSql = "SELECT a.p_seqno, "
				+ "       a.acct_type, "
				+ "       b.acct_key, "
				+ "		  (a.acct_type||'-'||b.acct_key) as acct_type_key, "
				+ " 	  a.card_no, "
				+ "       (uf_dc_amt (curr_code, aft_amt, dc_aft_amt) + uf_dc_amt (curr_code, aft_d_amt, dc_aft_d_amt)) AS wk_tot_bef_amt, "
				+ "       a.crt_date, "
				+ "       a.crt_time, "
				+ "       a.adjust_type, "
				+ "       ('act_acaj' || '-' || a.adjust_type) tt_which_table, "
				+ "       a.post_date, "
			//+ "       a.cash_type, "
			//+ "       a.trans_acct_type, "
			//+ "       a.trans_acct_key, "
				+ "       a.bef_amt, "
				+ "       a.bef_d_amt, "
				+ "       a.aft_amt, "
				+ "       a.aft_d_amt, "
				+ "       uf_idno_id (b.id_p_seqno) id_no, "
				+ "       uf_nvl (curr_code, '901') AS curr_code, "
				+ " 	  uf_acno_name(a.p_seqno) as db_name, "
				+ " 	  uf_dc_amt(a.curr_code,a.dr_amt,a.dc_dr_amt) as dc_dr_amt, "
				+ " 	  uf_dc_amt(a.curr_code,a.cr_amt,a.dc_cr_amt) as dc_cr_amt, "
				+ " 	  uf_dc_amt(a.curr_code,a.bef_amt,a.dc_bef_amt) as dc_bef_amt, "		
				+ " 	  uf_dc_amt(a.curr_code,a.aft_amt,a.dc_aft_amt) as dc_aft_amt, "
				+ " 	  uf_dc_amt(a.curr_code,a.bef_d_amt,a.dc_bef_d_amt) as dc_bef_d_amt, "	//新增時取前一筆的aft_amt
				+ " 	  uf_dc_amt(a.curr_code,a.aft_d_amt,a.dc_aft_d_amt) as dc_aft_d_amt, "
			//+ "      to_char (a.mod_time, 'YYYY/MM/DD HH24:Mi:SS') db_mod_time "
				+ "     (a.crt_date||'-'||a.crt_time) m_crt_datetime "
				+ " FROM act_acaj a, act_acno b "
				+ " WHERE 1 = 1 "
				+ " AND a.p_seqno = b.acno_p_seqno "
				+ " AND a.process_flag != 'Y' "
				+ " AND a.adjust_type LIKE 'OP%' ";

		lsSql += sqlCol(pPSeqno, "a.p_seqno");
		lsSql += sqlCol(mCurrcode, "uf_nvl(a.curr_code,'901')");
		lsSql += " and (a.crt_date||'-'||a.crt_time) = "
				   + " (select max(crt_date||'-'||crt_time) "
				   + " from act_acaj where process_flag != 'Y' and adjust_type like 'OP%' ";
		lsSql += sqlCol(pPSeqno, "p_seqno");
		lsSql += sqlCol(mCurrcode, "uf_nvl(curr_code,'901')");
		lsSql += ")";
		log("sql : " + lsSql);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			// 讀取 act_acct_curr主檔資料
			lsSql = "select a.end_bal_op, "
					+ " a.dc_end_bal_op, "
					+ " a.end_bal_lk, "
					+ " a.dc_end_bal_lk, "
					+ " a.overpay_lock_sta_date str_date, "
					+ " a.overpay_lock_due_date due_date, "
					+ " b.acct_type, "
					+ " b.acct_key, "
					+ " (a.acct_type||'-'||b.acct_key) as acct_type_key, "
					+ " uf_acno_name(a.p_seqno) db_name "
					+ " from act_acct_curr a, act_acno b "
					+ " where a.p_seqno = b.acno_p_seqno ";
			lsSql += sqlCol(pPSeqno, "a.p_seqno");
			lsSql += sqlCol(mCurrcode, "a.curr_code");
			log("act_acct_curr sql : " + lsSql);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
			  wp.colSet("p_seqno", "");
				alertErr2("此帳戶未申請 雙幣幣別[" + mCurrcode + "] 之卡片");
				return;
			}
			// set col by act_acct_curr
			wp.colSet("acct_type_key", sqlStr("acct_type_key"));
			wp.colSet("curr_code", mCurrcode);
			wp.colSet("acct_type", mAccttype);
			wp.colSet("acct_key", mAcctkey);
			wp.colSet("card_no", mCardno);
			wp.colSet("kp_curr_code", mCurrcode);
			wp.colSet("kp_acct_type", mAccttype);
			wp.colSet("kp_acct_key", mAcctkey);
			wp.colSet("kp_card_no", mCardno);
			wp.colSet("db_name", sqlStr("db_name"));
			wp.colSet("wk_tot_bef_amt", numToStr(sqlNum("dc_end_bal_op") + sqlNum("dc_end_bal_lk"), "###.##"));
			wp.colSet("wk_tot_aft_amt", numToStr(sqlNum("dc_end_bal_op") + sqlNum("dc_end_bal_lk"), "###.##"));
			
			wp.colSet("keep_dc_aft_amt", sqlStr("dc_end_bal_op"));
			wp.colSet("ex_dcount", "0");
			wp.colSet("dc_dr_amt", sqlStr("dc_end_bal_op"));// 新增時提領預設同剩餘溢付款金額
			wp.colSet("dc_cr_amt", "0");
		//wp.colSet("cash_type", "1");// 新增輸入時預設提領方式為 "1:溢付提領"
			// dc_end_bal_op==>dc_bef_amt
			wp.colSet("dc_bef_amt", sqlStr("dc_end_bal_op"));// 外幣目前餘額
			wp.colSet("dc_aft_amt", sqlStr("dc_end_bal_op"));
			//hidden欄位			
			wp.colSet("p_seqno", pPSeqno);
			// end_bal_op==>bef_amt
			wp.colSet("bef_amt", sqlStr("end_bal_op"));// 目前餘額
			// end_bal_lk==>bef_d_amt
			wp.colSet("bef_d_amt", sqlStr("end_bal_lk"));// 目前可D數
			// dc_end_bal_lk==>dc_bef_d_amt
			wp.colSet("dc_bef_d_amt", sqlStr("dc_end_bal_lk"));// 外幣目前可D數
			wp.colSet("dc_aft_d_amt", sqlStr("dc_end_bal_lk"));
		} else {
//			if(sql_ss("adjust_type").equals("OP02")){
//				alert_err(m_acctkey+"已有提領資料不可再新增");
//				return;
//			}
			wp.colSet("acct_type_key", sqlStr("acct_type_key"));
			wp.colSet("curr_code", mCurrcode);
			wp.colSet("acct_type", mAccttype);
			wp.colSet("acct_key", mAcctkey);
			wp.colSet("card_no", mCardno);
			wp.colSet("kp_curr_code", mCurrcode);
			wp.colSet("kp_acct_type", mAccttype);
			wp.colSet("kp_acct_key", mAcctkey);
			wp.colSet("kp_card_no", mCardno);
			wp.colSet("db_name", sqlStr("db_name"));
			wp.colSet("dc_bef_amt", sqlStr("dc_aft_amt"));// 新增時要取前一筆資料dc_aft_amt值
			wp.colSet("dc_dr_amt", sqlStr("dc_aft_amt"));// 新增時提領預設同剩餘溢付款金額
			wp.colSet("dc_cr_amt", sqlStr("dc_cr_amt"));
			wp.colSet("dc_aft_amt", sqlStr("dc_aft_amt"));
			wp.colSet("keep_dc_aft_amt", sqlStr("dc_aft_amt"));
			wp.colSet("wk_tot_bef_amt", sqlNum("wk_tot_bef_amt") + "");
			wp.colSet("wk_tot_aft_amt", sqlNum("dc_aft_amt") + sqlNum("dc_aft_d_amt") + "");
		//wp.colSet("cash_type", "1");// 新增輸入時預設提領方式為 "1:溢付提領"
			// hidden
			wp.colSet("p_seqno", pPSeqno);
			wp.colSet("bef_amt", sqlStr("aft_amt"));// 新增時要取前一筆資料aft_amt值
			wp.colSet("bef_d_amt", sqlStr("aft_d_amt"));// 新增時要取前一筆資料aft_d_amt值
			wp.colSet("dc_bef_d_amt", sqlStr("dc_aft_d_amt"));// 新增時要取前一筆資料dc_aft_d_amt值
			wp.colSet("dc_aft_d_amt", "0");			
		}

		//重新載入匯率
		lsSql = "select exchange_rate from PTR_CURR_RATE where 1=1 "
				+ "and curr_code =:curr_code";
		setString("curr_code", mCurrcode);
		sqlSelect(lsSql);
		wp.colSet("exchange_rate", sqlStr("exchange_rate"));
 	  double lbDrAmt = wp.colNum("dc_dr_amt") * sqlNum("exchange_rate");
		long cvtLong = (long) Math.round(lbDrAmt + 0.0000001);
		lbDrAmt = ((double) cvtLong);
		wp.colSet("new_dr_amt", lbDrAmt+"");
		
		// 計算修改次數
		String lsSqlA = "select count(*) as ll_cnt_a from act_acaj "
				+ "where 1=1 "
				+ "and substr(adjust_type,1,2) = 'OP' "
				+ "and process_flag != 'Y'   ";
		lsSqlA += sqlCol(pPSeqno, "p_seqno");
		lsSqlA += sqlCol(mCurrcode, "curr_code");
		sqlSelect(lsSqlA);
		wp.colSet("ex_dcount", sqlNum("ll_cnt_a"));

	}


  /*** conv_amt_dp2r(x) 有以下兩點作用：
  1.校正微小誤差：double 變數運算後會發生 .99999999...的問題，例如 19.125, 
    實際會變成 19.1249999999999999...，所以執行 conv_amt_dp2r(x)變成 19.13
  2.四捨五入到小數以下第二位
  ***/
  public double  convAmtDp2r(double cvtAmt) throws Exception
  {
    long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.00001);
    double cvtDouble =  ((double) cvtLong) / 100;
    return cvtDouble;
  }

	int addApproveLog() throws Exception {
		String isSql = "INSERT INTO LOG_ONLINE_APPROVE "
				+ "(program_id, file_name, crt_date, crt_user, apr_flag, apr_date, apr_user) "
				+ "values ('actm0100', 'actm0100.xlsx', :crt_date, :crt_user, 'Y', :apr_date, :apr_user )";
		setString("crt_date", wp.sysDate+wp.sysTime);
		setString("crt_user", wp.loginUser);
		setString("apr_date", wp.sysDate);
		setString("apr_user", wp.itemStr2("zz_apr_user"));

		sqlExec(isSql);
		if (sqlRowNum <= 0) {
			alertErr("Log紀錄檔寫入失敗 !");
		  return -1;
		}
		return 1;
	}

	/***********************************************************************/
	public double BigDecimalAdd(double v1, double v2) {
	BigDecimal b1 = new BigDecimal(Double.toString(v1));
	BigDecimal b2 = new BigDecimal(Double.toString(v2));
	return b1.add(b2).doubleValue();
	}
	/***********************************************************************/
	public double BigDecimalSub(double v1, double v2) {
	BigDecimal b1 = new BigDecimal(Double.toString(v1));
	BigDecimal b2 = new BigDecimal(Double.toString(v2));
	return b1.subtract(b2).doubleValue();
	}
}
