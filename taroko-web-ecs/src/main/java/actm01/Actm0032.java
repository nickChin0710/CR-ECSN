/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-07-21  V1.00.04  Simon      1.取消本金類調整                           *
*                                 2.取消銷帳鍵值、借方科目                   *
* 112-12-18  V1.00.05  Simon      1.恢復本金類調整                           *
*                                 2.各科目調整設定獨立調整類別               *
*                                 3.新增減免分期付款利息調整類別             *
*                                 4.exclude act_acaj.process_flag='Y'        *
******************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actm0032 extends BaseEdit {
	CommString commString = new CommString();
	Actm0032Func func;

	String mPSeqno = "";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			saveFunc();
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
	
	@Override
	public void initPage() {
		if (empty(strAction)) {
			//initial get exchange_rate
			String lsSql = "select a.exchange_rate er840, b.exchange_rate er392 "
					+ "from ptr_curr_rate a, ptr_curr_rate b "
					+ "where a.curr_code = '840' and b.curr_code = '392' ";
			sqlSelect(lsSql);
			wp.colSet("exchange_rate_840", sqlStr("er840"));
			wp.colSet("exchange_rate_392", sqlStr("er392"));
		}
	}

	@Override
	public void queryFunc() throws Exception {
		// 設定queryRead() SQL條件
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		Object[] param = null;
		String lsSql = "";
		String acctkey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
    /*
		ls_sql  = "select p_seqno from act_acno where acct_key=? ";
		ls_sql += " and acno_p_seqno = p_seqno ";
		param = new Object[] { acctkey };
    */
    String accttype = wp.itemStr2("ex_acct_type");

		lsSql  = "select p_seqno from act_acno where acct_key=? and acct_type=? ";
		lsSql += " and acno_p_seqno = p_seqno ";
		param = new Object[] { acctkey,accttype };

		sqlSelect(lsSql, param);
		if (empty(sqlStr("p_seqno"))) {
			mPSeqno = "";
		} else {
			mPSeqno = sqlStr("p_seqno");
		}
		String strWhereA = "and act_debt.p_seqno= :p_seqno and act_debt.acct_code not in "
		                 + "('DP') and act_debt.dc_end_bal >= 0";
		setString("p_seqno", mPSeqno);
		String strWhereB = "and act_debt_hst.p_seqno= :p_seqno and act_debt_hst.acct_code not in "
		                 + "('DP') and act_debt_hst.dc_end_bal >= 0";
		setString("p_seqno", mPSeqno);

		if (empty(wp.itemStr2("ex_curr_code")) == false) {
			strWhereA += " and act_debt.curr_code = :curr_code ";
			setString("curr_code", wp.itemStr2("ex_curr_code"));
			strWhereB += " and act_debt_hst.curr_code = :curr_code ";
			setString("curr_code", wp.itemStr2("ex_curr_code"));
		}
		if (empty(wp.itemStr2("ex_s_yyymm")) == false) {
			strWhereA += " and act_debt.acct_month >= :acct_months ";
			setString("acct_months", wp.itemStr2("ex_s_yyymm"));
			strWhereB += " and act_debt_hst.acct_month >= :acct_months ";
			setString("acct_months", wp.itemStr2("ex_s_yyymm"));
		}
		if (empty(wp.itemStr2("ex_e_yyymm")) == false) {
			strWhereA += " and act_debt.acct_month <= :acct_monthe ";
			setString("acct_monthe", wp.itemStr2("ex_e_yyymm"));
			strWhereB += " and act_debt_hst.acct_month <= :acct_monthe ";
			setString("acct_monthe", wp.itemStr2("ex_e_yyymm"));
		}

		String acctCodeIn = "";
		if (eqIgno(wp.itemStr2("ex_acitem01"), "Y")) {
			acctCodeIn += ",'AF','LF','CF','PF','SF','CC'";
		}
		if (eqIgno(wp.itemStr2("ex_acitem02"), "Y")) {
			acctCodeIn += ",'RI','AI','CI'";
		}
		if (eqIgno(wp.itemStr2("ex_acitem03"), "Y")) {
			acctCodeIn += ",'PN'";
		}
		if (eqIgno(wp.itemStr2("ex_acitem04"), "Y")) {
			acctCodeIn += ",'BL','CA','IT','ID','AO','OT','CB','DB' ";
		}
	//if (eqIgno(wp.itemStr2("ex_acitem05"), "Y")) {
	//	acctCodeIn += ",'ID'";
	//}
		if (acctCodeIn.length() > 0) {
			strWhereA += " and act_debt.acct_code in (" + acctCodeIn.substring(1) + ") ";
			strWhereB += " and act_debt_hst.acct_code in (" + acctCodeIn.substring(1) + ") ";
		}

		wp.sqlCmd = " SELECT act_debt.reference_no, "
				+ "          act_debt.p_seqno, "
				+ "          act_debt.acct_type, "
				+ "          act_debt.post_date, "
				+ "          act_debt.acct_month, "
				+ "          act_debt.card_no, "
				+ "          act_debt.interest_date, "
				+ "          act_debt.beg_bal, "
				+ "          act_debt.end_bal, "
				+ "          act_debt.d_avail_bal, "
				+ "          act_debt.txn_code, "
				+ "          act_debt.acct_code, "
				+ "          'debt' as db_table, "
				+ "          act_debt.bill_type, "
				+ "          hex(act_debt.rowid) as rowid, "
				+ "          act_debt.interest_rs_date, "
				+ "          ptr_actcode.chi_short_name as acct_item_cname, "
				+ "          act_debt.curr_code, "
				+ "          'U' function_code,  "
				+ "          act_debt.dc_beg_bal, "
				+ "          act_debt.dc_end_bal, "
				+ "          act_debt.dc_d_avail_bal, 0 as dc_dr_amt, 0 as sv_dc_dr_amt,"
				+ " 0 as dc_cr_amt, act_debt.dc_end_bal as dc_bef_amt, act_debt.dc_end_bal as dc_aft_amt, "
				+ " act_debt.dc_d_avail_bal as dc_bef_d_amt, act_debt.dc_d_avail_bal as dc_aft_d_amt, "
				+ " 0 as dr_amt, 0 as sv_dr_amt, 0 as cr_amt, act_debt.end_bal as bef_amt, "
				+ " act_debt.end_bal as aft_amt, act_debt.d_avail_bal as bef_d_amt, "
				+ " act_debt.d_avail_bal as aft_d_amt, act_debt.beg_bal as orginal_amt, "
				+ " act_debt.dc_beg_bal as dc_orginal_amt, "
				+ "          '' as wk_table, "
				+ "          '2' as value_type, "
				+ "          '1' as adj_reason_code, "
			//+ "          '14817000' as debit_item, "
			//+ "          '' as c_debt_key, "
				+ "          '' as adj_comment, "
				+ "          '' as sv_aopt, "
				+ "          '' as sv_dopt, "
				+ "          '' as add_disable, "
				+ "          'disabled' as del_disable "
				+ " FROM act_debt, ptr_actcode "
				+ " where act_debt.acct_code = ptr_actcode.acct_code " 
				+ strWhereA
				+ " union "
				+ " SELECT act_debt_hst.reference_no, "
				+ "          act_debt_hst.p_seqno, "
				+ "          act_debt_hst.acct_type, "
				+ "          act_debt_hst.post_date, "
				+ "          act_debt_hst.acct_month, "
				+ "          act_debt_hst.card_no, "
				+ "          act_debt_hst.interest_date, "
				+ "          act_debt_hst.beg_bal, "
				+ "          act_debt_hst.end_bal, "
				+ "          act_debt_hst.d_avail_bal, "
				+ "          act_debt_hst.txn_code, "
				+ "          act_debt_hst.acct_code, "
				+ "          'debt_hst' as db_table, "
				+ "          act_debt_hst.bill_type, "
				+ "          hex(act_debt_hst.rowid) as rowid, "
				+ "          act_debt_hst.interest_rs_date, "
				+ "          ptr_actcode.chi_short_name as acct_item_cname, "
				+ "          act_debt_hst.curr_code, "
				+ "          'U' function_code , "
				+ "          act_debt_hst.dc_beg_bal, "
				+ "          act_debt_hst.dc_end_bal, "
				+ "          act_debt_hst.dc_d_avail_bal, 0 as dc_dr_amt, 0 as sv_dc_dr_amt,"
				+ " 0 as dc_cr_amt, act_debt_hst.dc_end_bal as dc_bef_amt, "
				+ " act_debt_hst.dc_end_bal as dc_aft_amt, act_debt_hst.dc_d_avail_bal as dc_bef_d_amt, "
				+ " act_debt_hst.dc_d_avail_bal as dc_aft_d_amt, 0 as dr_amt, 0 as sv_dr_amt, 0 as cr_amt, "
				+ " act_debt_hst.end_bal as bef_amt, act_debt_hst.end_bal as aft_amt, "
				+ " act_debt_hst.d_avail_bal as bef_d_amt, act_debt_hst.d_avail_bal as aft_d_amt, "
				+ " act_debt_hst.beg_bal as orginal_amt, act_debt_hst.dc_beg_bal as dc_orginal_amt, "
				+ "          '' as wk_table, "
				+ "          '2' as value_type, "
				+ "          '1' as adj_reason_code, "
			//+ "          '14817000' as debit_item, "
		  //+ "          '' as c_debt_key, "
				+ "          '' as adj_comment, "
				+ "          '' as sv_aopt, "
				+ "          '' as sv_dopt, "
				+ "          '' as add_disable, "
				+ "          'disabled' as del_disable "
				+ " FROM act_debt_hst, ptr_actcode "
				+ " where act_debt_hst.acct_code = ptr_actcode.acct_code "
				+ strWhereB
				+ " order by acct_month,post_date ";

		wp.pageCountSql = "select count(*) from ("
				+ " select hex(act_debt.rowid) from act_debt, ptr_actcode "
				+ " where act_debt.ACCT_CODE = ptr_actcode.ACCT_CODE " + strWhereA
				+ " union select hex(act_debt_hst.rowid) from act_debt_hst, ptr_actcode "
				+ " where act_debt_hst.ACCT_CODE = ptr_actcode.ACCT_CODE " + strWhereB + " )";

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		ofcQueryafter();
		listWkdata();
		wp.setPageValue();

	}

	void ofcQueryafter() throws Exception {
		String lsCname = "", lsCorpCname = "";
		double totDrAmt = 0, totDcDrAmt = 0;
		int drCnt = 0;

		// Get id_p_seqno, corp_p_seqno
		String lsSql = "select act_acno.id_p_seqno, crd_idno.chi_name id_cname, "
				+ "act_acno.corp_p_seqno, nvl(crd_corp.chi_name,'') corp_cname, "
				+ "act_acno.acct_type, act_acno.acct_key from act_acno "
				+ "left join crd_idno on crd_idno.id_p_seqno = act_acno.id_p_seqno "
				+ "left join crd_corp on crd_corp.corp_p_seqno = act_acno.corp_p_seqno "
				+ "where act_acno.acno_p_seqno = :p_seqno ";
		setString("p_seqno", mPSeqno);
		sqlSelect(lsSql);
		if (sqlRowNum >= 0) {
			lsCname = sqlStr("id_cname");
			lsCorpCname = sqlStr("corp_cname");
		}
		wp.colSet("ex_cname", lsCname);
		wp.colSet("ex_corp_cname", lsCorpCname);
    
    /*
    String[] aa_val = {sql_ss("acct_type")};
    wp.setInBuffer("ex_acct_type", aa_val);
    */
 
		// --Read act_acaj---------------------------------------------------------------
		// TAG2000:
//		for (int ii = 0; ii < wp.selectCnt; ii++) {
//			ls_sql = "select act_acaj.orginal_amt, act_acaj.aft_amt, act_acaj.aft_d_amt, act_acaj.acct_code, hex(act_acaj.rowid) as rowid, "
//					+ "'acaj' as db_table, act_acaj.curr_code, act_acaj.dc_orginal_amt, act_acaj.dc_aft_amt, act_acaj.dc_aft_d_amt, "
//					+ "act_acaj.dc_dr_amt, act_acaj.value_type, act_acaj.adj_reason_code, act_acaj.adj_comment, act_acaj.c_debt_key, act_acaj.debit_item, "
//					+ "ptr_actcode.chi_short_name from act_acaj "
//					+ "left join ptr_actcode on act_acaj.acct_code = ptr_actcode.acct_code "
//					+ "where reference_no = :reference_no ";
//			setString("reference_no", wp.col_ss(ii, "reference_no"));
//			sqlSelect(ls_sql);
//			if (sql_nrow > 0) {
//				wp.col_set(ii, "beg_bal", sql_ss("orginal_amt"));
//				wp.col_set(ii, "end_bal", sql_ss("aft_amt"));
//				wp.col_set(ii, "d_avail_bal", sql_ss("aft_d_amt"));
//				wp.col_set(ii, "acct_code", sql_ss("acct_code"));
//				wp.col_set(ii, "rowid", sql_ss("rowid"));
//				wp.col_set(ii, "db_table", sql_ss("db_table"));
//				wp.col_set(ii, "curr_code", sql_ss("curr_code"));
//				wp.col_set(ii, "dc_beg_bal", sql_ss("dc_orginal_amt"));
//				wp.col_set(ii, "dc_end_bal", sql_ss("dc_aft_amt"));
//				wp.col_set(ii, "dc_d_available_bal", sql_ss("dc_aft_d_amt"));
//				wp.col_set(ii, "acct_item_cname", sql_ss("chi_short_name"));
//				wp.col_set(ii, "wk_table", "已調整");
//				wp.col_set(ii, "del_disable", "");
//				wp.col_set(ii, "dc_dr_amt", sql_ss("dc_dr_amt"));
//				wp.col_set(ii, "value_type", sql_ss("value_type"));
//				wp.col_set(ii, "adj_reason_code", sql_ss("adj_reason_code"));
//				wp.col_set(ii, "adj_comment", sql_ss("adj_comment"));
//				wp.col_set(ii, "c_debt_key", sql_ss("c_debt_key"));
//				wp.col_set(ii, "debit_item", sql_ss("debit_item"));
//			}
//		}
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			lsSql = "select orginal_amt, aft_amt, aft_d_amt, dr_amt, hex(rowid) as rowid, "
					+ "'acaj' as db_table, dc_orginal_amt, dc_aft_amt, dc_aft_d_amt, "
					+ "adjust_type, "
					+ "dc_dr_amt, value_type, adj_comment, c_debt_key, debit_item "
					+ "from act_acaj where reference_no = :reference_no "
					+ " and process_flag != 'Y' ";
			setString("reference_no", wp.colStr(ii, "reference_no"));
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet(ii, "beg_bal", sqlStr("orginal_amt"));  //以下依此類推
				wp.colSet(ii, "end_bal", sqlStr("aft_amt"));  //依此類推
				wp.colSet(ii, "dc_beg_bal", sqlStr("dc_orginal_amt"));  //依此類推
				wp.colSet(ii, "dc_end_bal", sqlStr("dc_aft_amt"));  //依此類推
				wp.colSet(ii, "rowid", sqlStr("rowid"));
				wp.colSet(ii, "db_table", sqlStr("db_table"));
				wp.colSet(ii, "wk_table", "已調整");
				wp.colSet(ii, "del_disable", "");
		    if (commString.mid(sqlStr("adjust_type"), 0,2).equals("DR") )  {
          wp.colSet(ii, "del_disable", "disabled");
			  //alertErr("已被D檔Reversal,不可D檔處理, reference_no= " + mReferenceNo);
		    }
				
				wp.colSet(ii, "add_disable", "disabled");
				wp.colSet(ii, "sv_aopt", "Y");
				wp.colSet(ii, "aopt-Y", "checked");
				//以下為畫面會調整的欄位, 須做update
				wp.colSet(ii, "dr_amt", sqlStr("dr_amt"));
				wp.colSet(ii, "sv_dr_amt", sqlStr("dr_amt"));
				wp.colSet(ii, "aft_amt", sqlStr("aft_amt"));
				wp.colSet(ii, "aft_d_amt", sqlStr("aft_d_amt"));
				wp.colSet(ii, "dc_dr_amt", sqlStr("dc_dr_amt"));
				wp.colSet(ii, "sv_dc_dr_amt", sqlStr("dc_dr_amt"));
				wp.colSet(ii, "dc_aft_amt", sqlStr("dc_aft_amt"));
				wp.colSet(ii, "dc_aft_d_amt", sqlStr("dc_aft_d_amt"));
				wp.colSet(ii, "value_type", sqlStr("value_type"));
				wp.colSet(ii, "adj_comment", sqlStr("adj_comment"));
			//wp.colSet(ii, "c_debt_key", sqlStr("c_debt_key"));
			//wp.colSet(ii, "debit_item", sqlStr("debit_item"));
        drCnt++;
			  totDcDrAmt += sqlNum("dc_dr_amt");
			  totDrAmt += sqlNum("dr_amt");
			}
		}
		wp.colSet("click_tot_dr_cnt", drCnt+""); 
		wp.colSet("click_tot_dc_dr_amt", totDcDrAmt+""); 
		wp.colSet("click_tot_dr_amt", totDrAmt+""); 
	}

	void listWkdata() throws Exception {
		String ss = "";

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			ss = wp.colStr(ii, "curr_code");
			wp.colSet(ii, "ex_curr_name", getCurrName(ss));

			// ss =wp.col_ss(ii,"value_type");
			// wp.col_set(ii,"value_type-1", ss.equals("1")?"selected":"");
			// wp.col_set(ii,"value_type-2", ss.equals("2")?"selected":"");

/***
		  if ( eqIgno(wp.colStr(ii, "acct_code"),"CB") || 
		       eqIgno(wp.colStr(ii, "acct_code"),"DB")    ) 
		  { 
			  wp.colSet(ii, "debit_item", "55030700"); 
			  wp.colSet(ii, "debit_item_disabled", "disabled"); 
		  } else {
		  	wp.colSet(ii, "debit_item_disabled", ""); 
		  }
***/
		}
	}

	String getCurrName(String currcode) throws Exception {
		String rtn = "";
		String lsSql = "select wf_desc from ptr_sys_idtab where wf_type = 'DC_CURRENCY' and wf_id = :curr_code ";
		setString("curr_code", currcode);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			rtn = sqlStr("wf_desc");
		}
		return rtn;
	}

	@Override
	public void querySelect() throws Exception {

	}

	@Override
	public void dataRead() throws Exception {

	}

	@Override
	public void saveFunc() throws Exception {
		func = new Actm0032Func(wp);

//		String ls_sql = "", job1 = "", job2 = "";
//
//		ls_sql = "Select usr_deptno, gl_code" + "from   sec_user, ptr_classcode" + "where  usr_deptno = class_code"
//				+ "  and	 usr_id =:userid";
//
//		setString("userid", wp.loginUser);
//
//		sqlSelect(ls_sql);
//		if (sql_nrow > 0) {
//			job1 = sql_ss("usr_deptno");
//			job2 = empty(sql_ss("gl_code")) ? "0" : "0" + sql_ss("gl_code").substring(0, 1);
//		}
//
//		String[] aa_rowid = wp.item_buff("rowid");
//		String[] aa_opt = wp.item_buff("aopt");
//		String[] dd_opt = wp.item_buff("dopt");
//		String[] aa_reference_no = wp.item_buff("reference_no");
//		String[] aa_db_table = wp.item_buff("db_table");
//		String[] aa_dc_dr_amt = wp.item_buff("dc_dr_amt");
//		String[] aa_dc_bef_d_amt = wp.item_buff("dc_bef_d_amt");
//		String[] aa_dc_aft_d_amt = wp.item_buff("dc_aft_d_amt");
//		String[] aa_acct_code = wp.item_buff("acct_code");
//		String[] aa_value_type = wp.item_buff("value_type");
//		String[] aa_c_debt_key = wp.item_buff("c_debt_key");
//		String[] aa_debit_item = wp.item_buff("debit_item");

		ofcUpdate();
	}

	void ofcUpdate() throws Exception {
		int llOk = 0, llErr = 0;
		String lsSql = "", job1 = "", job2 = "";
		
		try {
			String[] aaDbTable = wp.itemBuff("db_table");
			String[] aaRowid = wp.itemBuff("rowid");
			String[] aaPSeqno = wp.itemBuff("p_seqno");
			String[] aaAcctType = wp.itemBuff("acct_type");
			String[] aaReferenceNo = wp.itemBuff("reference_no");
			String[] aaPostDate = wp.itemBuff("post_date");
			String[] aaOrginalAmt = wp.itemBuff("orginal_amt");
			String[] aaDrAmt = wp.itemBuff("dr_amt");
			String[] aaCrAmt = wp.itemBuff("cr_amt");
			String[] aaBefAmt = wp.itemBuff("bef_amt");
			String[] aaAftAmt = wp.itemBuff("aft_amt");
			String[] aaBefDAmt = wp.itemBuff("bef_d_amt");
			String[] aaAftDAmt = wp.itemBuff("aft_d_amt");
			String[] aaAcctCode = wp.itemBuff("acct_code");
			String[] aaFunctionCode = wp.itemBuff("function_code");
			String[] aaCardNo = wp.itemBuff("card_no");
			String[] aaInterestDate = wp.itemBuff("interest_date");
			String[] aaCurrCode = wp.itemBuff("curr_code");
			String[] aaDcOrginalAmt = wp.itemBuff("dc_orginal_amt");
			String[] aaDcCrAmt = wp.itemBuff("dc_cr_amt");
			String[] aaDcBefAmt = wp.itemBuff("dc_bef_amt");
			String[] aaDcAftAmt = wp.itemBuff("dc_aft_amt");
			String[] aaDcBefDAmt = wp.itemBuff("dc_bef_d_amt");
			String[] aaDcAftDAmt = wp.itemBuff("dc_aft_d_amt");

			String[] aaDcDrAmt = wp.itemBuff("dc_dr_amt");
			String[] aaValueType = wp.itemBuff("value_type");
			String[] aaAdjReasonCode = wp.itemBuff("adj_reason_code");
			String[] aaAdjComment = wp.itemBuff("adj_comment");
		//String[] aaCDebtKey = wp.itemBuff("c_debt_key");
		//String[] aaDebitItem = wp.itemBuff("debit_item");
			String[] aaBillType = wp.itemBuff("bill_type");

			String[] aaOpt = wp.itemBuff("aopt");
			String[] ddOpt = wp.itemBuff("dopt");
			
			Float dramt, befdamt, aftdamt;
			
  		int liCBDBCnt = 0;
			for (int ll = 0; ll < aaRowid.length; ll++) {
				if ( eqIgno(aaAcctCode[ll],"CB") || 
		         eqIgno(aaAcctCode[ll],"DB")    ) 
		    { 
			    liCBDBCnt++; 
		    }
			}			

    //aa_debit_item[ll] = "55030700"; 要注意搬到陣列裡的元素時，超過實際個數時會影響到之後的變數 
  		int rowcntaa = 0;
/***
	  	rowcntaa = aaRowid.length;
      if ( liCBDBCnt > 0) {
		    aaDebitItem = itemBuffReorg(rowcntaa, "acct_code","debit_item");
      } 
		
			for (int ll = 0; ll < aaRowid.length; ll++) {
				if ( eqIgno(aaAcctCode[ll],"CB") || 
		         eqIgno(aaAcctCode[ll],"DB")    ) 
		    { 
			    wp.colSet(ll, "debit_item", "55030700"); 
			    wp.colSet(ll, "debit_item_disabled", "disabled"); 
		    } else {
			  	wp.colSet(ll, "debit_item_disabled", ""); 
		    }
		  }			
***/
			//detail_control
			rowcntaa = 0;
			if (!(aaRowid == null) && !empty(aaRowid[0])) rowcntaa = aaRowid.length;
			wp.listCount[0] = rowcntaa;
			
//			ls_sql = "Select usr_deptno, gl_code" + "from   sec_user, ptr_classcode" + "where  usr_deptno = class_code"
//					+ "  and	 usr_id =:userid";
			lsSql = "Select usr_deptno, gl_code from sec_user, ptr_dept_code where usr_deptno = dept_code "
					+ " and usr_id = :userid ";
			setString("userid", wp.loginUser);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
			//job1 = sql_ss("usr_deptno");
			//job1 = sql_ss("usr_deptno").substring(0, 2);
    	  job1 = commString.mid(sqlStr("usr_deptno"), 0,2);
				job2 = empty(sqlStr("gl_code")) ? "0" : "0" + sqlStr("gl_code").substring(0, 1);
			}else {
				alertErr("無法取得使用者之 [起帳部門代碼]! ");
				return;
			}

			for (int ll = 0; ll < aaRowid.length; ll++) {
				if ((checkBoxOptOn(ll, aaOpt) == false) && (checkBoxOptOn(ll, ddOpt) == false)) {
					continue;
				}

				if (!checkBoxOptOn(ll, ddOpt)) {
					if (empty(aaReferenceNo[ll])) {
						alertErr("交易參考號 不可空白");
						return;
					}
					lsSql = "SELECT prb_status " + "    FROM rsk_problem " + "   WHERE reference_no = :referno "
							+ "	AND	reference_seq = 1 ";
					setString("referno", aaReferenceNo[ll]);
					sqlSelect(lsSql);
					if (sqlRowNum > 0) {
						String sStatus = empty(sqlStr("prb_status")) ? "" : sqlStr("prb_status");
						double li_status = 0 ; 
						li_status = commString.strToNum(sStatus);
						if (li_status <=80) {
							alertErr("交易參考號：" + aaReferenceNo[ll] + "此交易已列問交卻未結案, 不可D檔");
							return;
						} else {
							alertErr("交易參考號：" + aaReferenceNo[ll] + "此交易已列問交卻結案");
							return;
						}
					}
				}

//				ls_sql = "select confirm_flag from act_acaj where reference_no = :referno ";
				lsSql = "select apr_flag from act_acaj where reference_no = :referno "
				      + " and process_flag != 'Y' ";
				setString("referno", aaReferenceNo[ll]);
				sqlSelect(lsSql);
				if (sqlRowNum > 0) {
					if (sqlRowNum > 1) {
						alertErr("交易參考號：" + aaReferenceNo[ll] + "此帳務資料，被多次調整，請由[w_actm0030]作業處理");
						return;
					} else if (sqlRowNum == 1 && sqlStr("apr_flag").toUpperCase().equals("Y")) {
						alertErr("交易參考號：" + aaReferenceNo[ll] + "資料已覆核, 不可再調整異動");
						return;
					}
					if (!aaDbTable[ll].toUpperCase().equals("ACAJ")) {
						alertErr("交易參考號：" + aaReferenceNo[ll] + "帳務資料已調整未處理, 不可重覆調整");
						return;
					}
				} else {
					if (sqlRowNum == 0 && aaDbTable[ll].toUpperCase().equals("ACAJ")) {
						alertErr("交易參考號：" + aaReferenceNo[ll] + "資料已不存在, 請重新讀取");
						return;
					}
				}

				dramt = (float) (empty(aaDcDrAmt[ll]) ? 0.0 : Float.parseFloat(aaDcDrAmt[ll]));
				befdamt = (float) (empty(aaDcBefDAmt[ll]) ? 0.0 : Float.parseFloat(aaDcBefDAmt[ll]));
				aftdamt = (float) (empty(aaDcAftDAmt[ll]) ? 0.0 : Float.parseFloat(aaDcAftDAmt[ll]));
//				System.out.println("ofc_update S: ll="+ll+", dramt="+dramt+", befdamt="+befdamt+", aftdamt="+aftdamt);
				if(dramt < 0 && befdamt < 0 && aftdamt < 0) {
					alertErr("交易參考號：" + aaReferenceNo[ll] + "D檔金額 輸入錯誤");
					return;
				}

				if(dramt < 0 ) {
					alertErr("交易參考號：" + aaReferenceNo[ll] + "D檔金額 須大於 0");
					return;
				}
				if(dramt > befdamt) {
					alertErr("交易參考號：" + aaReferenceNo[ll] + "D檔金額不可大於目前可 D 數!");
					return;
				}
				
				String acctcode = empty(aaAcctCode[ll]) ? "" : aaAcctCode[ll];
				String valuetype = empty(aaValueType[ll]) ? "" : aaValueType[ll];
				
				if(acctcode.toUpperCase().equals("DB") ||
						acctcode.toUpperCase().equals("CB") ||
						acctcode.toUpperCase().equals("CC") ||
						acctcode.toUpperCase().equals("CI") ) {
					if(!valuetype.equals("2")) {
						alertErr("交易參考號：" + aaReferenceNo[ll] + "催呆戶之[重新起息日] 須為 [覆核日]!");
						return;
					}
				}
				
/***
				String debititem = empty(aaDebitItem[ll]) ? "" : aaDebitItem[ll];
				String debitkey = empty(aaCDebtKey[ll]) ? "" : aaCDebtKey[ll];
				
				lsSql = "select * from gen_acct_m where ac_no = :ls_item ";
				setString("ls_item", debititem);
				sqlSelect(lsSql);
				if(sqlRowNum <= 0) {
					alertErr("交易參考號：" + aaReferenceNo[ll] + "調整錯誤:借方科目 不存在");
					return;
				}else {
					if(debititem.indexOf("1751") == 0 && debitkey.length() != 20) {
						alertErr("交易參考號：" + aaReferenceNo[ll] + "調整錯誤:借方科目1751,一定要有銷帳鍵值[20碼]");
						return;
					}
				}
***/
			}

			for (int ll = 0; ll < aaRowid.length; ll++) {
				// -option-ON-
				if ((checkBoxOptOn(ll, aaOpt) == false) && (checkBoxOptOn(ll, ddOpt) == false)) {
					continue;
				}

				if (eqIgno(aaDbTable[ll], "acaj") && checkBoxOptOn(ll, ddOpt)) {
					// -delete detail-
					func.varsSet("rowid", aaRowid[ll]);
					// func.vars_set("mod_seqno", aa_mod_seqno[ll]);
					if (func.dbDelete() < 0) {
						alertErr(func.getMsg());
						sqlCommit(0);
						return;
					} else {
						llOk++;
					}
					continue;
				}

				// -update & insert-
				if (!eqIgno(aaDbTable[ll], "acaj")) {  // -insert-
				  dramt = (float) (empty(aaDcDrAmt[ll]) ? 0.0 : Float.parseFloat(aaDcDrAmt[ll]));
				  if (dramt != 0) {  
					  func.varsSet("p_seqno", aaPSeqno[ll]);
					  func.varsSet("acct_type", aaAcctType[ll]);
					  func.varsSet("reference_no", aaReferenceNo[ll]);
					  func.varsSet("post_date", aaPostDate[ll]);
					  func.varsSet("orginal_amt", aaOrginalAmt[ll]);
					  func.varsSet("dr_amt", aaDrAmt[ll]);
					  func.varsSet("cr_amt", aaCrAmt[ll]);
					  func.varsSet("bef_amt", aaBefAmt[ll]);
					  func.varsSet("aft_amt", aaAftAmt[ll]);
					  func.varsSet("bef_d_amt", aaBefDAmt[ll]);
					  func.varsSet("aft_d_amt", aaAftDAmt[ll]);
					  func.varsSet("acct_code", aaAcctCode[ll]);
					  func.varsSet("function_code", aaFunctionCode[ll]);
					  func.varsSet("card_no", aaCardNo[ll]);
					  func.varsSet("interest_date", aaInterestDate[ll]);
					  func.varsSet("curr_code", aaCurrCode[ll]);
					  func.varsSet("dc_orginal_amt", aaDcOrginalAmt[ll]);
					  func.varsSet("dc_cr_amt", aaDcCrAmt[ll]);
					  func.varsSet("dc_bef_amt", aaDcBefAmt[ll]);
					  func.varsSet("dc_aft_amt", aaDcAftAmt[ll]);
					  func.varsSet("dc_bef_d_amt", aaDcBefDAmt[ll]);
					  func.varsSet("dc_aft_d_amt", aaDcAftDAmt[ll]);
					  func.varsSet("dc_dr_amt", empty(aaDcDrAmt[ll]) ? "0" : aaDcDrAmt[ll]);
					  func.varsSet("value_type", aaValueType[ll]);
					  func.varsSet("adj_reason_code", aaAdjReasonCode[ll]);
					  func.varsSet("adj_comment", aaAdjComment[ll]);
					//func.varsSet("c_debt_key", aaCDebtKey[ll]);
					//func.varsSet("debit_item", aaDebitItem[ll]);
					//func.varsSet("adjust_type", "DE32");//改由 Actm0032Func.java依據acct_code產生調整類別
					  func.varsSet("job_code", job1);
					  func.varsSet("vouch_job_code", job2);
					  func.varsSet("bill_type", aaBillType[ll]);
					  if (func.dbInsert() == 1) {
						  llOk++;
					  } else {
						  llErr++;
					  }
					}
				} else {  // -update-
					func.varsSet("rowid", aaRowid[ll]);
					func.varsSet("dr_amt", aaDrAmt[ll]);
					func.varsSet("aft_amt", aaAftAmt[ll]);
					func.varsSet("aft_d_amt", aaAftDAmt[ll]);
					func.varsSet("dc_dr_amt", empty(aaDcDrAmt[ll]) ? "0" : aaDcDrAmt[ll]);
					func.varsSet("dc_aft_amt", aaDcAftAmt[ll]);
					func.varsSet("dc_aft_d_amt", aaDcAftDAmt[ll]);
					func.varsSet("value_type", aaValueType[ll]);
				//func.vars_set("adj_reason_code", aa_adj_reason_code[ll]);
					func.varsSet("adj_comment", aaAdjComment[ll]);
				//func.varsSet("c_debt_key", aaCDebtKey[ll]);
				//func.varsSet("debit_item", aaDebitItem[ll]);
					func.varsSet("job_code", job1);
					func.varsSet("vouch_job_code", job2);
					func.varsSet("bill_type", aaBillType[ll]);
					if (func.dbUpdate() == 1) {
						llOk++;
					} else {
						llErr++;
					}
				}
				// System.out.println("-OOO->" + ll +", aa_rpt_block:"+aa_rpt_block[ll] +",
				// aa_print_text:"+ aa_print_text[ll]+", ss_from_type:"+ss_from_type);
				// System.out.println("-GOG->" +" ll_ok=" + ll_ok + ", ll_err=" + ll_err);
			}
			// 有失敗rollback，無失敗commit
			alertMsg("存檔處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr);
			sqlCommit(llErr > 0 ? 0 : 1);
			if (llErr == 0) {
				queryFunc();
			}
		} catch (Exception ex) {
		}
	}

  public String[] itemBuffReorg(int serCnt, String fieldName1, String fieldName2) throws Exception {
	  String lsDebitItem = "";
   	int dataCnt = 0;

    String[] inputFieldDataA = new String[serCnt];//借用 "ser_num" 欄位個數設定陣列個數

		for (int ii = 0; ii < serCnt; ii++) {
      //ddd("actm0032_debit_item-ii: " + ii);
      //ddd("actm0032_debit_item[ii]: " + wp.col_ss(ii, fieldName2));
      //ddd("actm0032_acct_code-ii: " + ii);
      //ddd("actm0032_acct_code[ii]: " + wp.col_ss(ii, fieldName1));
		  if ( eqIgno(wp.colStr(ii, fieldName1),"CB") || 
		       eqIgno(wp.colStr(ii, fieldName1),"DB")    ) 
		  { 
			  lsDebitItem = "55030700";
		  } else {
		  	lsDebitItem = wp.colStr(dataCnt, fieldName2);
        dataCnt++;
		  }
		  inputFieldDataA[ii] = lsDebitItem;
      //ddd("actm0032_inputFieldData_a-ii: " + ii);
      //ddd("actm0032_inputFieldData_a[ii]: " + inputFieldData_a[ii]);
		}

	  return inputFieldDataA;
  }

	@Override
	public void initButton() {
		//沒有 detail 畫面
		//if (wp.respHtml.indexOf("_detl") > 0) {
		//	this.btnMode_aud();
		//}
	  String sKey = "1st-page";
    if (wp.respHtml.equals("actm0032"))  {
       wp.colSet("btnUpdate_disable","");
       this.btnModeAud(sKey);
    }

	}

	@Override
	public void dddwSelect() {
		try {
			// wp.initOption = "--";
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
		if (acctkey.trim().length() == 8)
			rtn += "000";
		if (acctkey.trim().length() == 10)
			rtn += "0";

		return rtn;
	}

}
