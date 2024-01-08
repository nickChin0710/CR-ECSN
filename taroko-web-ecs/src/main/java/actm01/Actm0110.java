/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial   
* 111-10-20  V1.00.03  Machao      sync from mega & updated for project coding standard*
* 112-04-28  V1.00.04  Simon      avoid sqlStrend() error                    * 
*****************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;

public class Actm0110 extends BaseEdit {
	String mAccttype = "";
	CommString commString = new CommString();
	String mAcctkey = "";
	String mCardno = "";
	String mCurrcode = "";
	String pPSeqno = "";
	Actm0110Func func;

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
		} else if (eqIgno(wp.buttonCode, "S2")) {
			/* itemchanged */
			// is_action = "S2";
			// wf_trans_ackey();
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
			// -check approve-
			if (!checkApprove(wp.itemStr2("zz_apr_user"), wp.itemStr2("zz_apr_passwd"))) {
				wp.respHtml = "TarokoErrorPDF";
				return;
			}
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
		wp.colSet("ex_date1", getSysDate());
		wp.colSet("post_date", getSysDate());
		wp.colSet("ex_curr_code", "901");
		String lsSql = "";
		lsSql = "select exchange_rate from PTR_CURR_RATE where 1=1 "
				+ "and curr_code ='901'";
		try {
			sqlSelect(lsSql);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wp.colSet("exchange_rate", sqlStr("exchange_rate"));
		
		if (wp.respHtml.indexOf("_add") > 0) {
			wp.colSet("trans_acct_type", "01");
		}
		

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

		// if (wp.item_ss("ex_acct_key").length() < 6) { // phopho add
		// err_alert("帳戶帳號至少要6碼");
		// return;
		// }
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
				+ "0 dc_dr_amt, "
				+ "uf_dc_amt(curr_code,dr_amt,   dc_dr_amt   ) dc_dr_amt,"
				+ "uf_dc_amt(curr_code,bef_amt, dc_bef_amt ) bef_amt, "
				+ "uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt) bef_d_amt, "
				+ "(uf_dc_amt(curr_code,bef_amt, dc_bef_amt  ) + uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt)) as wk_tot_bef_amt, "
				+ "a.crt_date, "
				+ "a.crt_time, "
				+ "substr(a.crt_date,1,4)||'/'||substr(a.crt_date,5,2)||'/'||substr(a.crt_date,7,2) as update_date, "
				+ "substr(a.crt_time,1,2)||':'||substr(a.crt_time,3,2)||':'||substr(a.crt_time,5,2) as update_time, "
				+ "a.post_date, "
				+ "a.trans_acct_type, "
				+ "a.trans_acct_key, "
				+ "a.trans_acct_type||'-'||a.trans_acct_key as wk_acctkey1, " 
				+ "a.adjust_type, "
				+ "a.apr_flag, "
				+ "uf_acno_name(a.p_seqno) db_cname, "
				+ "uf_nvl(a.curr_code,'901') as curr_code, "
			//+ "to_char(a.mod_time,'YYYY/MM/DD HH24:Mi:SS') db_mod_time ";
				+ "(a.crt_date||'-'||a.crt_time) crt_datetime ";

		wp.sqlCmd += "from act_acaj a, act_acno b ";

		wp.sqlCmd += "WHERE 1=1 "
				+ "and a.p_seqno = b.acno_p_seqno "
				+ "and a.adjust_type = 'OP03' "
				+ "and a.process_flag != 'Y' ";

		
		if(empty(lsAcctKey) == false) {
			wp.sqlCmd += " and b.acct_key like :lsAcctKey ";
			setString("lsAcctKey",lsAcctKey + "%");
		}
		
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
		String ss = "", ss2 = "";
		String lsPSeqno = "", lsAcctType = "", lsCurrCode = "", lsCrtDatetime = "",lsAprFlag="";
		String lsSql = "";
		//計算筆數,金額
		computeSum();
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wp.colSet(ii, "tt_which_table", "act_acaj" + "--" + wp.colStr(ii, "adjust_type"));
			lsPSeqno = wp.colStr(ii, "p_seqno");
			lsAcctType = wp.colStr(ii, "acct_type");
			lsCurrCode = wp.colStr(ii, "curr_code");
			lsCrtDatetime = wp.colStr(ii, "crt_datetime");
			lsAprFlag = wp.colStr(ii, "apr_flag");
			lsSql = "select  max(crt_date||'-'||crt_time) max_datetime from act_acaj "
					   + "where 1=1 "
				     + " and process_flag != 'Y' "
				     + " and adjust_type like 'OP%' ";
			lsSql += sqlCol(lsPSeqno, "p_seqno");
			lsSql += sqlCol(lsCurrCode, "curr_code");
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
			ss = wp.colStr(ii, "trans_acct_type");
			ss2 = wp.colStr(ii, "trans_acct_key");
			wp.colSet(ii, "db_trans_cname", wfTransAckey(ss, ss2));

			wp.colSet(ii, "tt_update_time", wp.colStr(ii, "update_date") + " " + wp.colStr(ii, "update_time"));

		}
	}

	// Mantis4007 修改停用
	// int wk_chk_act_acaj(int tx_ii, String tx_p_seqno, String tx_curr_code)
	// throws Exception {
	//
	// String sql_0 = " select "
	// + " uf_dc_amt(curr_code,dr_amt, dc_dr_amt ) dc_dr_amt, "
	// // + " uf_dc_amt(curr_code,bef_amt, dc_bef_amt ) bef_amt, "
	// // + " uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt) bef_d_amt, "
	// + " (uf_dc_amt(curr_code,bef_amt, dc_bef_amt ) +
	// uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt)) as wk_tot_bef_amt, "
	// + " crt_date, "
	// + " crt_time, "
	// + " adjust_type, "
	// + " post_date, "
	// + " trans_acct_type, "
	// + " trans_acct_key "
	// + " from act_acaj "
	// + " where p_seqno = ? "
	// + " and curr_code = ? "
	// + " and process_flag != 'Y' "
	// // + " and adjust_type = 'OP03' "
	// + " and adjust_type like 'OP%' "
	// + " order by crt_date desc,crt_time desc "
	// + " fetch first 1 row only ";
	//
	// sqlSelect(sql_0, new Object[] { tx_p_seqno, tx_curr_code });
	//
	// if (sql_nrow <= 0) {
	// return -1;
	// }
	//
	// if (sql_ss("adjust_type").equals("OP03")) {
	// wp.col_set(tx_ii, "dc_dr_amt", sql_ss("dc_dr_amt"));
	// wp.col_set(tx_ii, "post_date", sql_ss("post_date"));
	// wp.col_set(tx_ii, "trans_acct_type", sql_ss("trans_acct_type"));
	// wp.col_set(tx_ii, "trans_acct_key", sql_ss("trans_acct_key"));
	// } else {
	// wp.col_set(tx_ii, "dc_dr_amt", 0);
	// wp.col_set(tx_ii, "post_date", "");
	// wp.col_set(tx_ii, "trans_acct_type", "");
	// wp.col_set(tx_ii, "trans_acct_key", "");
	// }
	//
	// wp.col_set(tx_ii, "wk_tot_bef_amt", sql_ss("wk_tot_bef_amt"));
	// wp.col_set(tx_ii, "crt_date", sql_ss("crt_date"));
	// wp.col_set(tx_ii, "crt_time", sql_ss("crt_time"));
	// wp.col_set(tx_ii, "adjust_type", sql_ss("adjust_type"));
	// // wp.col_set(tx_ii,"trans_acct_type", sql_ss("trans_acct_type"));
	// // wp.col_set(tx_ii,"trans_acct_key", sql_ss("trans_acct_key"));
	//
	// return 0;
	// }
	void computeSum() throws Exception {
		// 計算總筆數,金額
		String lsSql="";
		lsSql = "select count(*) sum_ct,"
				+ "sum(uf_dc_amt(curr_code,dr_amt,dc_dr_amt)) sum_dc_dr_amt ,"
				+ "sum((uf_dc_amt(curr_code,bef_amt, dc_bef_amt  ) + uf_dc_amt(curr_code,bef_d_amt,dc_bef_d_amt))) sum_tot_bef_amt "
				+ "from act_acaj a,act_acno b "
				+ "where 1=1 "
				+ "and a.p_seqno = b.acno_p_seqno "
				+ "and a.process_flag != 'Y' ";
		if(strAction.equals("XLS")){
		//ls_sql += "and a.adjust_type like 'OP%' ";
			lsSql += "and a.adjust_type = 'OP03' ";
		} else {
			lsSql += "and a.adjust_type = 'OP03' ";
		}

		if (empty(wp.itemStr("ex_acct_key")) == false) {
			lsSql += "and b.acct_key like :ex_acct_key ";
			setString("ex_acct_key",wp.itemStr("ex_acct_key") + "%");
		}
		
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
			lsSql += " and (a.apr_flag = '' or a.apr_flag ='N') ";
			break;
		case "1":
			lsSql += " and a.apr_flag = 'Y' ";
			break;
		}
		sqlSelect(lsSql);
		if (sqlNum("sum_ct") > 0) {
			wp.colSet("sum_ct", sqlStr("sum_ct"));
			wp.colSet("sum_tot_bef_amt", sqlStr("sum_tot_bef_amt"));
			wp.colSet("sum_dc_dr_amt", sqlStr("sum_dc_dr_amt"));
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

		wp.selectSQL = " hex(act_acaj.rowid) as rowid, act_acaj.mod_seqno, "
				+ " act_acaj.p_seqno, "
				+ " act_acaj.acct_type, "
				+ " act_acno.acct_key, "
				+ " act_acaj.card_no, "
				+ " decode(act_acaj.curr_code,'','901',act_acaj.curr_code) as curr_code, "
				+ " act_acaj.dr_amt, "
				+ " act_acaj.cr_amt, "
				+ " act_acaj.bef_amt, "
				+ " act_acaj.aft_amt, "
				+ " act_acaj.bef_d_amt, "
				+ " act_acaj.aft_d_amt, "
				+ " act_acaj.post_date, "
				+ " act_acaj.trans_acct_type, "
				+ " act_acaj.trans_acct_key, "
				+ " act_acaj.adjust_type, "
				+ " act_acaj.apr_flag, "
				+ " uf_acno_name(act_acaj.p_seqno) as db_name, "
				+ " uf_dc_amt(act_acaj.curr_code,act_acaj.dr_amt,act_acaj.dc_dr_amt) as dc_dr_amt, "
				+ " uf_dc_amt(act_acaj.curr_code,act_acaj.dr_amt,act_acaj.dc_dr_amt) as keep_dc_dr_amt, "
				+ " uf_dc_amt(act_acaj.curr_code,act_acaj.cr_amt,act_acaj.dc_cr_amt) as dc_cr_amt, "
				+ " uf_dc_amt(act_acaj.curr_code,act_acaj.bef_amt,act_acaj.dc_bef_amt) as dc_bef_amt, "
				+ " uf_dc_amt(act_acaj.curr_code,act_acaj.aft_amt,act_acaj.dc_aft_amt) as dc_aft_amt, "
				+ " uf_dc_amt(act_acaj.curr_code,act_acaj.aft_amt,act_acaj.dc_aft_amt) as keep_dc_aft_amt, "
				+ " uf_dc_amt(act_acaj.curr_code,act_acaj.bef_d_amt,act_acaj.dc_bef_d_amt) as dc_bef_d_amt, "
				+ " uf_dc_amt(act_acaj.curr_code,act_acaj.aft_d_amt,act_acaj.dc_aft_d_amt) as dc_aft_d_amt ";

		wp.daoTable = " act_acaj, act_acno ";
		wp.whereStr = " where act_acaj.p_seqno = act_acno.acno_p_seqno ";
		wp.whereStr += "and act_acaj.adjust_type = 'OP03' ";
		wp.whereStr += "and act_acaj.process_flag != 'Y' ";
		wp.whereStr += "and act_acaj.p_seqno = :p_seqno ";
		wp.whereStr += "and act_acaj.curr_code = :curr_code "
				+ "and hex(act_acaj.rowid) =:rowid ";
		setString("p_seqno", pPSeqno);
		setString("curr_code", mCurrcode);
		setString("rowid", wp.itemStr2("data_k5"));

		pageSelect();

		String lsAqlA = "select count(*) as ll_cnt_a from act_acaj "
				+ "where p_seqno = :p_seqno and curr_code = :curr_code "
				+ "and substr(adjust_type,1,2) = 'OP' "
				+ "and process_flag != 'Y'   ";
		setString("p_seqno", pPSeqno);
		setString("curr_code", mCurrcode);
		sqlSelect(lsAqlA);
		wp.colSet("ex_dcount", sqlNum("ll_cnt_a"));

		String ss = wp.colStr("trans_acct_type");
		String ss2 = wp.colStr("trans_acct_key");
		wp.colSet("db_trans_cname", wfTransAckey(ss, ss2));

		wp.colSet("wk_tot_bef_amt", wp.colNum("dc_bef_amt") + wp.colNum("dc_bef_d_amt"));
		wp.colSet("wk_tot_aft_amt", wp.colNum("dc_aft_amt") + wp.colNum("dc_aft_d_amt"));
		//
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
		
		// 有異動權限者，讀出明細資料後，將鍵值存至 kp_ 並設定防止鍵值輸入屬性
		if (wp.autUpdate()) {
			if (wp.itemEq("adjust_type", "OP03") || (wp.colNum("dc_bef_amt") > 0)) {
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

		/***
		 * if (empty(m_accttype)) { m_accttype = item_kk("acct_type"); } if
		 * (empty(m_acctkey)) { m_acctkey =
		 * fillZeroAcctKey(item_kk("acct_key")); } if (empty(m_cardno)) {
		 * m_cardno = item_kk("card_no"); } //若 wp.item_ss("kk_card_no") 空值，則會抓
		 * wp.item_ss("card_no") if (empty(m_currcode)) { m_currcode =
		 * item_kk("curr_code"); }
		 ***/

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

	int ofValidation() throws Exception {
		// if (wfChkAcctCurr(p_p_seqno, m_currcode) < 0 ) return -1;
		//double lm_dr_amt1 = 0, lm_dr_amt2 = 0, lm_amt = 0, lm_amt2 = 0;
		double lmDcDrAmt = 0, lmDcBefAmt = 0, lmDcAftAmt = 0, lmTotBefAmt = 0, lmTotAftAmt = 0,
		       lmDrAmt = 0, lmBefAmt = 0, lmAftAmt=0;


		if (wp.itemEq("apr_flag", "Y") && wp.itemStr2("adjust_type").equals("OP03")) {
			alertErr2("已覆核不可以異動");
			return -1;
		}

		if (strAction.equals("D")) {
			return 1;
		}

		// m_accttype =wp.item_ss("kk_acct_type");
		// m_acctkey =wp.item_ss("kk_acct_key");
		// m_cardno =wp.item_ss("kk_card_no");
		// m_currcode =wp.item_ss("kk_curr_code");
		// if (wfGetPseqno()<0) return -1;

		lmDcDrAmt = wp.itemNum("dc_dr_amt");

		lmTotBefAmt = wp.itemNum("wk_tot_bef_amt");
		lmDcBefAmt = wp.itemNum("dc_bef_amt");
		lmBefAmt = wp.itemNum("bef_amt");

		log("m_currcode : "+mCurrcode);
		if(empty(mCurrcode)){
			mCurrcode = wp.itemStr2("kk_curr_code");
		}

	  if (lmDcBefAmt != 0) {
 	    double lbDrAmt = (lmDcDrAmt * lmBefAmt) / lmDcBefAmt;
		  long cvtLong = (long) Math.round(lbDrAmt + 0.0000001);
		  lmDrAmt = ((double) cvtLong);
		} 

		if (lmDcDrAmt <= 0 || lmDrAmt <= 0) {
			alertErr("轉入金額 須 >0");
			return -1;
		}

		lmTotAftAmt = lmTotBefAmt - lmDcDrAmt;
		lmDcAftAmt = lmDcBefAmt - lmDcDrAmt;
		lmAftAmt    = lmBefAmt    - lmDrAmt;

    lmTotAftAmt =  convAmtDp2r(lmTotAftAmt);
    lmDcAftAmt =  convAmtDp2r(lmDcAftAmt);
    lmAftAmt    =  convAmtDp2r(lmAftAmt);


		if (lmDcAftAmt < 0 || lmAftAmt < 0) {
				alertErr("轉入金額 不可大於溢付款餘額");
				return -1;
		}
			
		// func.vars_set("dc_aft_amt", Double.toString(lm_amt));
		// func.vars_set("dc_aft_d_amt",
		// Double.toString(wp.item_num("dc_bef_d_amt")));
		func.varsSet("dr_amt", Double.toString(lmDrAmt));

	  func.varsSet("dc_aft_d_amt", Double.toString(wp.itemNum("dc_bef_d_amt")));
	  func.varsSet("aft_d_amt", Double.toString(wp.itemNum("bef_d_amt")));
		func.varsSet("dc_aft_amt", Double.toString(lmDcAftAmt));
		func.varsSet("aft_amt", Double.toString(lmAftAmt));

		wp.colSet("wk_tot_aft_amt", lmTotAftAmt+"");
		wp.colSet("dc_aft_amt", lmDcAftAmt+"");

		if (wfTransAckey() != 1) {
			// alert_err("轉入帳戶帳號+結算幣別 不存在 ");
			return -1;
		}

		String sqlSelect = "select a.usr_deptno, b.gl_code "
				+ " from sec_user as a, ptr_dept_code as b "
				+ " where b.dept_code = a.usr_deptno "
				+ " and a.usr_id = :ls_mod_user ";
		setString("ls_mod_user", wp.loginUser);
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			alertErr("無法取得 使用者部門代碼, 起帳部門代碼 !!");
			return -1;
		}
		// String ls_deptno = sql_ss("usr_deptno").substring(0, 2);
		String lsDeptno = commString.mid(sqlStr("usr_deptno"), 0, 2);
		String lsGlcode = sqlStr("gl_code");

		func.varsSet("job_code", lsDeptno);
		func.varsSet("vouch_job_code", "0" + strMid(lsGlcode, 0, 1));
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

	String wfTransAckey(String asAcctType, String asAcctKey) throws Exception {
		String rtn = "";

		if (empty(asAcctType) || empty(asAcctKey))
			return rtn;

		String lsSql = " select uf_acno_name(p_seqno) as trans_cname from act_acno ";
		lsSql += " where acct_type = :acct_type and acct_key = :acct_key ";
		lsSql += " and acno_p_seqno = p_seqno ";
		setString("acct_type", asAcctType);
		setString("acct_key", asAcctKey);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			rtn = sqlStr("trans_cname");
		}

		return rtn;
	}

	@Override
	public void saveFunc() throws Exception {
		func = new Actm0110Func(wp);

		if ( strAction.equals("A") && empty(wp.itemStr2("p_seqno")) ) {
			alertErr2("請先讀取");
			return;
		}
		
		//ddd("Detl_a: wp.item_ss(kp_curr_code):" + wp.item_ss("kp_curr_code"));
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
	}

	@Override
	public void initButton() {
		if (wp.respHtml.equals("actm0110")) {
			this.btnModeAud();
		}

		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
			// rowid 有值時，新增鍵 off(disabled)，修改鍵、刪除鍵 on(若有權限 on,沒權限 off)
			// rowid 無值時，新增鍵 on(若有權限 on,沒權限 off)，修改鍵、刪除鍵 off(disabled)
			//if (!empty(wp.col_ss("rowid")) && !wp.col_ss("adjust_type").equals("OP03") && wp.aut_update()) {
			//	this.btnOn_aud(true, false, false); // rowid
			//										// 有值但最近一筆acaj非"OP03"時，新增鍵
			//										// on，修改鍵、刪除鍵 off(disabled)
			//}
			// dc_bef_bal 無值時，表示不能新增 act_acaj(轉出)，因此新增鍵 off(disabled)
			//if (wp.col_num("dc_bef_amt") == 0) {
			//	btnOn_add(false);
			//}
		}
	}

	@Override
	public void dddwSelect() {
		try {
			// wp.initOption = "--";
			//wp.optionKey = wp.item_ss("kk_acct_type");
			wp.optionKey = wp.colStr("kk_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.initOption = "--";
			if (wp.respHtml.indexOf("_add") > 0 || wp.respHtml.indexOf("_detl") > 0) {
				wp.optionKey = empty(wp.colStr("kk_curr_code")) ? "901" : wp.colStr("kk_curr_code");
			} else {
			//wp.optionKey = empty(wp.col_ss("ex_curr_code")) ? "901" : wp.col_ss("ex_curr_code");
				wp.optionKey = wp.colStr("ex_curr_code");
			}
			this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
		} catch (Exception ex) {
		}
	}

	int wfTransAckey() throws Exception {
		String lsActk1 = wp.itemStr2("trans_acct_type");
		String lsActk2 = wp.itemStr2("trans_acct_key");
		String lsCurrCode = "";
		if (empty(lsActk1) || empty(lsActk2)) {
			wp.colSet("db_trans_cname", "");
			return 1;
		}
		lsCurrCode = wp.itemStr2("curr_code");
		if (empty(wp.itemStr2("curr_code"))) {
			lsCurrCode = wp.itemStr2("kk_curr_code");
		}
		String sqlSelect = "select uf_acno_name(p_seqno) as db_trans_cname, autopay_acct_no from	act_acct_curr where 1=1 "
				+ " and p_seqno in ( select acno_p_seqno from act_acno where acct_key = :ls_actk2 and acct_type = :ls_actk1 ) "
				+ " and curr_code = :ls_curr_code ";
		setString("ls_actk1", lsActk1);
		setString("ls_actk2", lsActk2);
		setString("ls_curr_code", lsCurrCode);
		sqlSelect(sqlSelect);
		if (sqlRowNum == 0) {
			alertErr("轉入帳號  不存在 or 未申請此 雙幣幣別之卡片");
			return -1;
		}
		if (sqlRowNum < 0) {
			alertErr("select act_acct_curr error");
			return -1;
		}
		wp.colSet("db_trans_cname", sqlStr("db_trans_cname"));
		return 1;
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
			wp.reportId = "actm0110";
			/***
			 * String ss = "帳戶帳號: " + wp.item_ss("ex_acct_key");
			 * wp.col_set("cond_1", ss); String ss2 = "結算幣別: " +
			 * wp.item_ss("ex_curr_code"); wp.col_set("cond_2", ss2);
			 ***/

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
		//ddd("Detl_a: m_currcode:" + m_currcode);

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
		  	pPSeqno = sqlStr("p_seqno");
		  	mAccttype = sqlStr("acct_type");//之後會搬到 kp_acct_type：wp.col_set("kp_acct_type", m_accttype);
		    mAcctkey  = sqlStr("acct_key");
			  wp.colSet("p_seqno", pPSeqno);
		  } else {
			  wp.colSet("p_seqno", "");
		  	alertErr2("此卡號不存在!");
		  	return;
		  }
		}

		log("p_seqno :" + pPSeqno);
		// 讀取act_acaj SQL
		lsSql = "SELECT a.p_seqno, "
				+ "       a.acct_type, "
				+ "       b.acct_key, "
				+ "		  (a.acct_type||'-'||b.acct_key) as acct_type_key, "
				+ " 	  a.card_no, "
				+ "       (uf_dc_amt (curr_code, bef_amt, dc_bef_amt) + uf_dc_amt (curr_code, bef_d_amt, dc_bef_d_amt)) AS wk_tot_bef_amt, "
				+ "       a.crt_date, "
				+ "       a.crt_time, "
				+ "       a.adjust_type, "
				+ "       ('act_acaj' || '-' || a.adjust_type) tt_which_table, "
				+ "       a.post_date, "
				+ "       a.cash_type, "
				+ "       a.trans_acct_type, "
				+ "       a.trans_acct_key, "
				+ "       a.bef_amt, "
				+ "       a.aft_amt, "
				+ "       a.aft_d_amt, "
				+ "       uf_idno_id (b.id_p_seqno) id_no, "
				+ "       uf_nvl (curr_code, '901') AS curr_code, "
				+ " 	  uf_acno_name(a.p_seqno) as db_name, "
				+ " 	  uf_dc_amt(a.curr_code,a.dr_amt,a.dc_dr_amt) as dc_dr_amt, "
				+ " 	  uf_dc_amt(a.curr_code,a.cr_amt,a.dc_cr_amt) as dc_cr_amt, "
				+ " 	  uf_dc_amt(a.curr_code,a.bef_amt,a.dc_bef_amt) as dc_bef_amt, "
				+ " 	  uf_dc_amt(a.curr_code,a.aft_amt,a.dc_aft_amt) as dc_aft_amt, "
				+ " 	  uf_dc_amt(a.curr_code,a.bef_d_amt,a.dc_bef_d_amt) as dc_bef_d_amt, "
				+ " 	  uf_dc_amt(a.curr_code,a.aft_d_amt,a.dc_aft_d_amt) as dc_aft_d_amt, "
			//+ "       to_char (a.mod_time, 'YYYY/MM/DD HH24:Mi:SS') db_mod_time "
				+ "     (a.crt_date||'-'||a.crt_time) m_crt_datetime "
				+ " FROM act_acaj a, act_acno b "
				+ " WHERE 1 = 1 "
				+ " AND a.p_seqno = b.acno_p_seqno "
				+ " AND a.process_flag != 'Y' "
				+ " AND a.adjust_type LIKE 'OP%' ";

		lsSql += sqlCol(pPSeqno, "a.p_seqno");
		lsSql += sqlCol(mCurrcode, "a.curr_code");
		lsSql += " and (a.crt_date||'-'||a.crt_time) =  "
					 + " (select max(crt_date||'-'||crt_time) "
				   + " from act_acaj where process_flag != 'Y' and adjust_type like 'OP%' ";
		lsSql += sqlCol(pPSeqno, "p_seqno");
		lsSql += sqlCol(mCurrcode, "curr_code");
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
			// dc_end_bal_op==>dc_bef_amt
			wp.colSet("keep_dc_aft_amt", sqlStr("dc_end_bal_op"));
			wp.colSet("ex_dcount", "0");
			wp.colSet("dc_dr_amt", sqlStr("dc_end_bal_op"));// 新增時轉帳預設同剩餘溢付款金額
			wp.colSet("dc_cr_amt", "0");
			wp.colSet("dc_bef_amt", sqlStr("dc_end_bal_op")); // 外幣目前餘額
			wp.colSet("dc_aft_amt", sqlStr("dc_end_bal_op"));
			// hidden
			wp.colSet("p_seqno", pPSeqno);
			// end_bal_op==>bef_amt
			wp.colSet("bef_amt", sqlStr("end_bal_op")); // 目前餘額
			// end_bal_lk==>bef_d_amt
			wp.colSet("bef_d_amt", sqlStr("end_bal_lk")); // 目前可D數
			// dc_end_bal_lk==>dc_bef_d_amt
			wp.colSet("dc_bef_d_amt", sqlStr("dc_end_bal_lk")); // 外幣目前可D數
			wp.colSet("dc_aft_d_amt", sqlStr("dc_end_bal_lk"));
		} else {
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
			wp.colSet("dc_dr_amt", sqlStr("dc_aft_amt"));// 新增時轉帳預設同剩餘溢付款金額
			wp.colSet("dc_cr_amt", sqlStr("dc_cr_amt"));
			wp.colSet("dc_aft_amt", sqlStr("dc_aft_amt"));
			wp.colSet("keep_dc_aft_amt", sqlStr("dc_aft_amt"));
			wp.colSet("wk_tot_bef_amt", sqlNum("wk_tot_bef_amt") + "");
			wp.colSet("wk_tot_aft_amt", sqlNum("dc_aft_amt") + sqlNum("dc_aft_d_amt") + "");
			// hidden
			wp.colSet("p_seqno", pPSeqno);
			wp.colSet("bef_amt", sqlStr("aft_amt"));
			wp.colSet("bef_d_amt", sqlStr("aft_d_amt"));
			wp.colSet("dc_bef_d_amt", wp.colStr("dc_bef_d_amt"));
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
				+ "values ('actm0110', 'actm0110.xlsx', :crt_date, :crt_user, 'Y', :apr_date, :apr_user )";
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
		
}
