package ccam01;
/** 臨時調整額度維護（批次）
 * 19-1210:    Alex  fix initButton
 * 19-0611:    JH    p_seqno >>acno_p_xxx
 * 18-0926:		JH		需主管覆核
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-12-22  V1.00.01  Justin concat sql -> parameterize sql
 * */

import ofcapp.BaseAction;

public class Ccam2052 extends BaseAction {
	ccam01.Ccam2052Func func = null;
	taroko.base.CommDate commdate = new taroko.base.CommDate();
	String kkPSeqno = "", cardAcctIdx = "" , checkErrMsg = "";
	boolean ibTemp = false;
	double cardAcctIdxDou = 0;
	
	@Override
	public void userAction() throws Exception {
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
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
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "S2")) {
			/* 動態查詢 */
			f5CalcAmt();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}
	}

	@Override
	public void dddwSelect() {
		try {
			if (wp.respHtml.indexOf("_detl") > 0) {
				wp.optionKey = wp.colStr(0, "adj_reason");
				dddwList("dddw_adj_reason", "cca_sys_parm3", "sys_key", "sys_data1", "where sys_id = 'ADJREASON'");
				if(wp.colEq("acno_flag", "2"))
					return ;
				wp.optionKey = wp.colStr(0, "ex_risk_type");
				dddwList("dddw_risk_type", "cca_risk_consume_parm", "risk_type", "risk_type||'.'||uf_tt_risk_type(risk_type)", 
						"where area_type ='T' "+sqlCol(wp.colStr("card_note"), "card_note")+sqlCol(wp.colStr("class_code"), "risk_level"));
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {
		if (empty(wp.itemStr("ex_idno")) && empty(wp.itemStr("ex_card_no"))) {
			alertErr2("卡號  身分證字號 : 不可同時空白 !");
			return;
		}

		String lsIdno = wp.itemStr("ex_idno");
		if (!empty(lsIdno)) {
			if (lsIdno.length() != 8 && lsIdno.length() != 10) {
				alertErr2("身分證字號 為 8碼 or 10碼");
				return;
			}
		}
		// wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		// wp.pageControl();

		wp.sqlCmd = "select C.acct_type," + " '' as unapr_flag," + " C.acno_p_seqno ," + " C.debit_flag ,"
				+ " C.acno_flag ," + " C.card_acct_idx ," + " C.block_status ," + " C.spec_status ," + " C.adj_quota ,"
				+ " C.adj_eff_start_date ," + " C.adj_eff_end_date ," + " C.adj_area ," + " C.tot_amt_month ,"
				+ " C.adj_inst_pct ," + " C.adj_remark ," + " uf_acno_key2(C.p_seqno,C.debit_flag) as acct_key , "
				+ " uf_corp_no(C.corp_p_seqno) as corp_no , " + " C.adj_reason , "
				+ "  (select sys_data1 from cca_sys_parm3 where sys_key =C.adj_reason and sys_id = 'ADJREASON') as tt_adj_reason "
				+ " from cca_card_acct C" + " where debit_flag <>'Y'";
		if (wp.itemEmpty("ex_card_no") == false) {
			wp.sqlCmd += " and C.acno_p_seqno in (select acno_p_seqno from crd_card where card_no =:kk_card_no)";
		}
		String lsAcctKey = wp.itemStr2("ex_idno");
		if (lsAcctKey.length() == 8) {
			wp.sqlCmd += " and C.corp_p_seqno in (select corp_p_seqno from crd_corp where corp_no = :kk_idno)";
		} else if (lsAcctKey.length() == 10) {
			wp.sqlCmd += " and C.id_p_seqno in (select id_p_seqno from crd_idno where id_no like :kk_idno)";
		}
		setString2("kk_card_no", wp.itemStr2("ex_card_no"));
		setString2("kk_idno", lsAcctKey);

		pageQuery();
		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr2("此條件查無資料");
			return;
		}
		listWkdata(sqlRowNum);

		// wp.setPageValue();
	}

	void listWkdata(int aRow) {
		String sql1 = "select line_of_credit_amt ," + " acno_flag , " + " uf_acno_name(p_seqno) as acno_name"
				+ " from act_acno" + " where acno_p_seqno =?"; // +commSqlStr.col(wp.col_ss(ii,"p_seqno"),"p_seqno");
		String sql2 = " select " + " adj_eff_start_date , " + " adj_eff_end_date , " + " adj_area , "
				+ " tot_amt_month , " + " adj_inst_pct , " + " adj_remark , " + " adj_quota , " + " adj_reason , "
				+ " (select sys_data1 from cca_sys_parm3 where sys_key =adj_reason and sys_id = 'ADJREASON') as tt_adj_reason "
				+ " from cca_card_acct_t " + " where mod_type ='ADJ-LIMIT' " + " and card_acct_idx =?";
		String sql3 = "select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
		
		
		for (int ii = 0; ii < aRow; ii++) {
			setString2(1, wp.colStr(ii, "acno_p_seqno"));
			sqlSelect(sql1);

			if (sqlRowNum > 0) {
				wp.colSet(ii, "line_of_credit_amt", sqlStr("line_of_credit_amt"));
				wp.colSet(ii, "acno_name", sqlStr("acno_name"));
			}
			wp.colSet(ii, "adj_eff_date", commString.strToYmd(wp.colStr(ii, "adj_eff_start_date")) + " -- "
					+ commString.strToYmd(wp.colStr(ii, "adj_eff_end_date")));

			// select.cca_card_acct_T
			setDouble(1, wp.colNum(ii, "card_acct_idx"));
			sqlSelect(sql2);
			if (sqlRowNum > 0) {
				wp.colSet(ii, "unapr_flag", "Y");
				wp.colSet(ii, "adj_eff_start_date", sqlStr("adj_eff_start_date"));
				wp.colSet(ii, "adj_eff_end_date", sqlStr("adj_eff_end_date"));
				wp.colSet(ii, "adj_area", sqlStr("adj_area"));
				wp.colSet(ii, "tot_amt_month", sqlStr("tot_amt_month"));
				wp.colSet(ii, "adj_inst_pct", sqlStr("adj_inst_pct"));
				wp.colSet(ii, "adj_remark", sqlStr("adj_remark"));
				wp.colSet(ii, "adj_eff_date", commString.strToYmd(sqlStr("adj_eff_start_date")) + " -- "
						+ commString.strToYmd(sqlStr("adj_eff_end_date")));
				wp.colSet(ii, "adj_quota", sqlStr("adj_quota"));
				wp.colSet(ii, "adj_reason", sqlStr("adj_reason"));
				wp.colSet(ii, "tt_adj_reason", sqlStr("tt_adj_reason"));
			}
			
			if(wp.colEq(ii,"acct_type", "03") || wp.colEq(ii,"acct_type", "06")) {
				sqlSelect(sql3,new Object[] {wp.colStr(ii,"acno_p_seqno")});
				if (sqlRowNum > 0) {
					wp.colSet(ii, "card_no", sqlStr("card_no"));
				}
			}
		}
	}

	@Override
	public void querySelect() throws Exception {
		cardAcctIdx = wp.itemStr("data_k1");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if (empty(cardAcctIdx)) {
			cardAcctIdx = wp.itemStr("card_acct_idx");
		}
		if (empty(cardAcctIdx)) {
			alertErr2("授權帳戶號碼(card_acct_idx): 不可空白");
			return;
		}

		wp.daoTable = " cca_card_acct";
		wp.whereStr = " where 1=1 " + col(toNum(cardAcctIdx), "card_acct_idx",true);
		wp.selectSQL = "" + " card_acct_idx , " + " acno_flag , " + " acno_p_seqno , "
				+ " uf_acct_pseqno(acno_p_seqno) as acct_p_seqno," + " id_p_seqno , " + " adj_area , "
				+ " adj_area as org_area ," + " adj_reason , " + " adj_reason as org_reason ," + " tot_amt_month , "
				+ " tot_amt_month as org_amt_mon ," + " adj_eff_start_date , "
				+ " adj_eff_start_date as ori_start_date , " + " adj_eff_end_date ,"
				+ " adj_eff_end_date as ori_end_date , " + " adj_inst_pct , " + " adj_inst_pct as ori_inst_pct , "
				+ " adj_remark , " + " adj_remark as org_remark ," + " spec_status , " + " adj_risk_flag ,"
				+ " mod_user," + " crt_user," + " crt_date," + " adj_date," + " debit_flag,"
				+ " to_char(mod_time,'yyyymmdd') as mod_date ," + " uf_idno_name(id_p_seqno) as chi_name ,"
				+ " 0 as comp_amt , " + " 0 as comp_inst_amt , " + " 0 as line_credit_amt , " + " block_reason1 , "
				+ " block_reason2 , " + " block_reason3 , " + " block_reason4 , " + " block_reason5 , "
				+ " block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as wk_block_reason ,"
				+ " spec_status ," + " hex(rowid) as rowid, mod_seqno , " + " notice_flag , " + " adj_sms_flag";

		pageSelect();
		if (sqlRowNum <= 0) {
			alertErr("查無資料, key=" + cardAcctIdx);
			return;
		}

		cardAcctIdxDou = wp.colNum("card_acct_idx");
		dataReadAfter();
		selectTempData();
		if (ibTemp) {
			selectAdjParmT();
			wp.colSet("xx_temp", "Y");
			selectRiskLevelCardNote();
			alertMsg("此筆尚未覆核.........");
			return;
		}	else	{
			wp.colSet("xx_temp", "");
		}

		// -no-Temp---
		selectCcaAdjParm();
		if (wp.colEmpty("adj_eff_start_date") && wp.colEmpty("adj_eff_end_date")) {
			if(wp.colEq("acno_flag", "2")) {
				wp.colSet("adj_sms_flag", "N");
				wp.colSet("notice_flag", "N");
			}	else	{
				wp.colSet("adj_sms_flag", "Y");
				wp.colSet("notice_flag", "N");	
			}
		}
		
		if(wp.colEq("acno_flag", "2")) {
			wp.colSet("noOnButton", "disabled");
		}	else	{
			wp.colSet("noOnButton", "");
		}
		
		selectRiskLevelCardNote();
	}

	void selectRiskLevelCardNote() throws Exception {
		String lsCardNote = "", lsRiskLevel = "";		
		lsCardNote = getCardNote(wp.colStr("acno_p_seqno"));
		wp.colSet("card_note", lsCardNote);
		wp.colSet("risk_level", wp.colStr("class_code"));
		
		if(wp.colEq("acno_flag", "2")) 
			return ;
		
		wp.optionKey = wp.colStr(0, "ex_risk_type");
		dddwList("dddw_risk_type", "cca_risk_consume_parm", "risk_type", "risk_type||'.'||uf_tt_risk_type(risk_type)", 
				"where area_type ='T' "+sqlCol(wp.colStr("card_note"), "card_note")+sqlCol(wp.colStr("class_code"), "risk_level"));
	}

	void dataReadAfter() {

		String sql0 = " select count(*) as db_cnt from crd_card where acno_p_seqno = ? and current_code='0' and sup_flag='1' ";
		sqlSelect(sql0, new Object[] { wp.colStr("acno_p_seqno") });
		if (sqlRowNum > 0) {
			wp.colSet("card_sup_cnt", sqlStr("db_cnt"));
		}

		String sql1 = "select acct_type , " + " acct_key ," + " line_of_credit_amt as line_credit_amt , "
				+ " uf_acno_name(acno_p_seqno) as acno_name , " + " class_code " + " from act_acno " + " where 1=1 "
				+ " and acno_p_seqno = ? ";
		setString2(1, wp.colStr("acno_p_seqno"));
		sqlSelect(sql1);
		if (sqlRowNum <= 0) {
			alertErr2("查無帳戶資料(act_acno), kk=" + wp.colStr("acno_p_seqno"));
			return;
		}
		wp.colSet("acct_type", sqlStr("acct_type"));
		wp.colSet("acct_key", sqlStr("acct_key"));
		wp.colSet("line_credit_amt", sqlStr("line_credit_amt"));
		wp.colSet("class_code", sqlStr("class_code"));
		if (wp.colNum("adj_inst_pct") == 0) {
			wp.colSet("adj_inst_pct", sqlStr("line_credit_amt"));
		}

		// -關係戶-
		String sql2 = "select A.fh_flag as rela_flag , " + " A.non_asset_balance as asset_balance , "
				+ " B.asset_value as bond_amt " + " from crd_idno B left join crd_correlate A  "
				+ " on A.correlate_id =B.id_no " + " where B.id_p_seqno =? " + " order by A.crt_date desc "
				+ commSqlStr.rownum(1);
		sqlSelect(sql2, new Object[] { wp.colStr("id_p_seqno") });
		if (sqlRowNum <= 0) {
			wp.colSet("rela_flag", "");
			wp.colSet("asset_balance", "");
			wp.colSet("bond_amt", "");			
		}	else	{
			wp.colSet("rela_flag", sqlStr("rela_flag"));
			wp.colSet("asset_balance", sqlStr("asset_balance"));
			wp.colSet("bond_amt", sqlStr("bond_amt"));
		}
		
		
		if(wp.colEq("acct_type", "03") || wp.colEq("acct_type", "06")) {
			String sql4 = "";
			sql4 = " select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
			sqlSelect(sql4,new Object[] {wp.colStr("acno_p_seqno")});
			
			if(sqlRowNum > 0 ) {
				wp.colSet("card_no", sqlStr("card_no"));
			}
		}		
		
	}

	// --read temp
	void selectTempData() throws Exception {
		ibTemp = false;
		String sql1 = " select " + " adj_eff_start_date , " + " adj_eff_end_date , " + " adj_reason , "
				+ " adj_remark , " + " adj_area , " + " adj_inst_pct , " + " tot_amt_month , " + " notice_flag , "
				+ " adj_risk_flag , " + " adj_sms_flag " + " from cca_card_acct_t " + " where mod_type ='ADJ-LIMIT' "
				+ " and card_acct_idx =?";
		setDouble(1, cardAcctIdxDou);
		sqlSelect(sql1);
		if (sqlRowNum <= 0) {
			return;
		}

		ibTemp = true;
		wp.colSet("adj_eff_start_date", sqlStr("adj_eff_start_date"));
		wp.colSet("adj_eff_end_date", sqlStr("adj_eff_end_date"));
		wp.colSet("adj_reason", sqlStr("adj_reason"));
		wp.colSet("adj_remark", sqlStr("adj_remark"));
		wp.colSet("adj_area", sqlStr("adj_area"));
		wp.colSet("adj_inst_pct", sqlStr("adj_inst_pct"));
		wp.colSet("tot_amt_month", sqlStr("tot_amt_month"));
		wp.colSet("notice_flag", sqlStr("notice_flag"));
		wp.colSet("adj_risk_flag", sqlStr("adj_risk_flag"));
		wp.colSet("adj_sms_flag", sqlStr("adj_sms_flag"));
	}

	void selectAdjParmT() {
		wp.pageRows = 999;
		wp.sqlCmd = "select " + " risk_type, card_note, risk_level," + " uf_tt_risk_type(risk_type) as tt_risk_type , "
				+ " adj_month_amt , " + " adj_day_amt , " + " adj_day_cnt , " + " adj_month_cnt , "
				+ " adj_eff_start_date as adj_date1 , " + " adj_eff_end_date as adj_date2 , "
				+ " decode(spec_flag,'Y','checked','') as opt2_on , '' as opt1_on "				
				+ " from cca_adj_parm_t "
				+ " where 1=1 " + " and card_acct_idx = ? " + " order by risk_type";
		setDouble(1, cardAcctIdxDou);
		pageQuery();
		wp.listCount[0] = sqlRowNum;
		if (sqlRowNum <= 0) {
			wp.colSet("IND_NUM", "0");
			this.selectOK();
		}	else	{
			wp.colSet("IND_NUM", sqlRowNum);
		}
	}

	void selectCcaAdjParm() throws Exception {
		wp.pageRows = 999;
		sqlRowNum = 0;
		wp.colSet("IND_NUM", "0");
		if (!wp.colEmpty("adj_eff_start_date") || !wp.colEmpty("add_eff_end_date")) {
			wp.sqlCmd = " select " + " risk_type, card_note, risk_level,"
					+ " uf_tt_risk_type(risk_type) as tt_risk_type , " + " adj_month_amt , " // -月限額-
					+ " adj_day_amt, " // -次限額-
					+ " adj_day_cnt , " // -日限次-
					+ " adj_month_cnt ," // -月限次-
					+ " adj_eff_start_date as adj_date1 , " + " adj_eff_end_date as adj_date2 , "
					+ " spec_flag , decode(spec_flag,'Y','checked','') as opt2_on , '' as opt1_on "
					+ " from cca_adj_parm "
					+ " where 1=1 " + col(cardAcctIdx, "card_acct_idx", true) + " order by risk_type";
			pageQuery();
			wp.listCount[0] = sqlRowNum;
			if (sqlRowNum > 0) {
				wp.colSet("IND_NUM", sqlRowNum);
				return;			
			}	else	{
				selectOK();
				wp.colSet("IND_NUM", 0);
			}
		}

//		// -no-find-
//		String lsCardNote = getCardNote(wp.colStr("acno_p_seqno"));
//		wp.sqlCmd = " select " + " A.* , " + " uf_tt_risk_type(A.risk_type) as tt_risk_type "
//				+ " from cca_risk_consume_parm A " + " where A.area_type ='T' " + " and A.card_note = ?"
//				+ " and A.risk_level =?"
//				// + " and apr_date <>''"
//				// +sql_col(wp.col_ss("class_code"),"A.risk_level")
//				+ " order by A.risk_type";
//		setString2(1, lsCardNote);
//		setString(wp.colStr("class_code"));
//
//		pageQuery();
//		wp.listCount[0] = sqlRowNum;
//		int llNrow = sqlRowNum;
//		double lmLineAmt = wp.colNum("line_credit_amt");
//		double lmTotAmt = lmLineAmt;
//		// double lm_tot_amt = wp.col_num("tot_amt_month");
//		if (lmTotAmt == 0) {
//			lmTotAmt = lmLineAmt;
//		}
//		for (int ii = 0; ii < llNrow; ii++) {
//			if (pos("|J|R|G|C", wp.colStr(ii, "risk_type")) > 0) {
//				wp.colSetNum(ii, "adj_month_amt", lmLineAmt, 0);
//				wp.colSetNum(ii, "adj_day_amt", (lmLineAmt * wp.colNum(ii, "lmt_amt_time_pct") / 100), 0);
//			} else {
//				wp.colSetNum(ii, "adj_month_amt", lmTotAmt, 0);
//				wp.colSetNum(ii, "adj_day_amt", (lmTotAmt * wp.colNum(ii, "lmt_amt_time_pct") / 100), 0);
//			}
//			wp.colSet(ii, "adj_day_cnt", wp.colStr(ii, "lmt_cnt_day"));
//			wp.colSet(ii, "adj_month_cnt", wp.colStr(ii, "lmt_cnt_month"));
//
//			if (wp.colEmpty("adj_eff_start_date")) {
//				wp.colSet(ii, "adj_date1", getSysDate());
//			} else {
//				wp.colSet(ii, "adj_date1", wp.colStr("adj_eff_start_date"));
//			}
//
//			if (wp.colEmpty("adj_eff_end_date")) {
//				wp.colSet(ii, "adj_date2", commdate.dateAdd(getSysDate(), 0, 1, 0));
//			} else {
//				wp.colSet(ii, "adj_date2", wp.colStr("adj_eff_start_date"));
//			}
//
//		}

	}

	String getCardNote(String aPseqno) {
		String lsSql = "select A.card_note, count(*) as cnt_parm"
				+ " from crd_card A join cca_risk_consume_parm B on B.card_note=A.card_note and B.apr_date<>''"
				+ " where A.current_code='0'" + " and A.acno_p_seqno =?" + " group by A.card_note"
				+ " order by decode(A.card_note,'C',9,'G',8,'P',7,'S',6,'I',5,10)";
		setString2(1, aPseqno);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0)
			return "*";
		for (int ll = 0; ll < sqlRowNum; ll++) {
			if (sqlInt("cnt_parm") > 0)
				return sqlStr("card_note");
		}
		// 通用--
		return "*";
	}

	@Override
	public void saveFunc() throws Exception {
		wp.listCount[0] = wp.itemRows("risk_type");
		optNumKeep(wp.itemRows("risk_type"),"opt_del","opt1_on");
		optNumKeep(wp.itemRows("risk_type"),"opt","opt2_on");
		func = new ccam01.Ccam2052Func();
		func.setConn(wp);
		
		
		if (this.isDelete() == false) {
			if(checkSpecAmt()==false) {
				alertErr(checkErrMsg);
				return ;
			}
		}		
		
		rc = func.dbSave(strAction);
		if (rc != 1) {
			sqlCommit(-1);
			errmsg(func.getMsg());
			return;
		}

		// -刪除---
		if (this.isDelete()) {
			sqlCommit(1);
			this.dataRead();
			alertMsg("臨調資料, 刪除完成");
			return;
		}

		detlUpdate();
		sqlCommit(rc);
		if (rc != 1)
			return;

		this.saveAfter(true);
		alertMsg("存檔完成，資料待覆核");
	}
	
	boolean checkSpecAmt() {
		Double lmTotMonth = 0.0 , lmTotSpec = 0.0 , lmNowAmt = 0.0;
		int listCnt = wp.itemRows("risk_type");
		if(listCnt ==0)	return true;
		String[] riskType = wp.itemBuff("risk_type");
		String[] specAmt = wp.itemBuff("adj_month_amt");
		String[] specDayAmt = wp.itemBuff("adj_day_amt");
		String[] dayCnt = wp.itemBuff("adj_day_cnt");
		String[] monthCnt = wp.itemBuff("adj_month_cnt");
		String[] optDel = wp.itemBuff("opt_del");
		String[] opt = wp.itemBuff("opt");
		lmTotMonth = wp.itemNum("tot_amt_month");
		lmNowAmt = wp.itemNum("line_credit_amt");
		
		//--確認高風險交易
		if(wp.itemEq("adj_risk_flag", "Y")) {
			for(int ll=0;ll<listCnt;ll++) {
				if(checkBoxOptOn(ll, optDel))	continue;	
				if(checkHighRisk(riskType[ll])) {
					checkErrMsg = "風險分類 : "+riskType[ll]+" 屬於高風險交易 , 不可調整 ! ";
					return false ;
				}
			}
		}
				
		for(int ii=0;ii<listCnt;ii++) {
			if(checkBoxOptOn(ii, optDel))	continue;					
			//--確認日限額、月限額
			if(commString.strToNum(specDayAmt[ii]) > commString.strToNum(specAmt[ii])) {
				checkErrMsg = "調整風險分類日限額不可超過月限額";
				return false ;
			}
			if(checkBoxOptOn(ii, opt)) {
				//--專款專用額度
				lmTotSpec += commString.strToNum(specAmt[ii]);
				if(commString.strToNum(dayCnt[ii]) <= 0) {
					checkErrMsg = "專款專用項目日限次: 不可小於等於 0 ! ";
					return false ;
				}				
				if(commString.strToNum(monthCnt[ii]) <= 0) {
					checkErrMsg = "專款專用項目月限次: 不可小於等於 0 ! ";
					return false ;
				}				
			} else {
				//--非專款專用額度不可超過信用額度
				if(commString.strToNum(specAmt[ii]) > lmNowAmt) {
					checkErrMsg = "風險分類:"+riskType[ii]+" 臨調月限額超過目前額度 !";
					return false ;
				}
			}			
		}
		
		//--沒有專款專用時 , 不用檢核專款專用總額度加上目前額度須超過臨調額度
		if(lmTotSpec ==0)
			return true;
		
		lmTotSpec += lmNowAmt ;
		if(lmTotSpec > lmTotMonth) {
			checkErrMsg = "專款專用總額度加上目前額度超過臨調額度 !";
			return false ;
		}
		
		return true ;
	}
	
	void detlUpdate() {
		int llOk = 0, llErr = 0;
		String[] optDel = wp.itemBuff("opt_del"); 
		String[] opt = wp.itemBuff("opt");
		int llNrow = wp.itemRows("risk_type");
		for (int ll = 0; ll < llNrow; ll++) {
			wp.colSet(ll, "ok_flag", "");
			String lsRiskType = wp.itemStr(ll, "risk_type");
			if (empty(lsRiskType)) {
				continue;
			}
			
			if(checkBoxOptOn(ll, optDel))	continue ;
			if(checkBoxOptOn(ll,opt)) {
				func.varsSet("spec_flag", "Y");
			}	else	{
				func.varsSet("spec_flag", "N");
			}
			
			func.varsSet("risk_type", lsRiskType);
			func.varsSet("adj_month_amt", wp.itemStr(ll, "adj_month_amt"));
			func.varsSet("adj_month_cnt", wp.itemStr(ll, "adj_month_cnt"));
			func.varsSet("adj_day_amt", wp.itemStr(ll, "adj_day_amt"));
			func.varsSet("adj_day_cnt", wp.itemStr(ll, "adj_day_cnt"));
			func.varsSet("adj_eff_start_date", wp.itemStr(ll, "adj_date1"));
			func.varsSet("adj_eff_end_date", wp.itemStr(ll, "adj_date2"));
			// 管制高風險交易 flag
//			if (wp.itemEq("adj_risk_flag", "Y")) {
//				if (checkHighRisk(lsRiskType)) {
//					func.varsSet("adj_month_amt", "0");
//					func.varsSet("adj_month_cnt", "0");
//					func.varsSet("adj_day_amt", "0");
//					func.varsSet("adj_day_cnt", "0");
//				}
//			}

			if (func.insertAdjParmT(ll) != 1) {
				llErr++;
				wp.colSet(ll, "ok_flag", "X");
			}
		}
		if (llErr > 0) {
			errmsg("消費風險類: 存檔失敗, 失敗筆數[%s]", llErr);
		}
	}

	boolean checkHighRisk(String lsRiskType) {

		String sql1 = "select sys_data3 from cca_sys_parm1 where sys_id = 'RISK' and sys_key = ? ";
		sqlSelect(sql1, new Object[] { lsRiskType });

		if (sqlRowNum <= 0)
			return false;

		if (sqlStr("sys_data3").equals("Y"))
			return true;

		return false;
	}

	int dataCheck() throws Exception {
		String sql1 = "select sum(nvl(a.pay_amt,0)) as lm_pay_amt " + " from  act_pay_detail a, act_pay_batch b "
				+ " where 1=1 " + " and a.p_seqno = ? " + " and a.batch_no = b.batch_no " + " and b.batch_tot_cnt > 0 "
				+ " and b.batch_no not like '%9001%'";
		sqlSelect(sql1, new Object[] { wp.itemStr("acct_p_seqno") });

		double lmPayAmt = sqlNum("lm_pay_amt");

		String sql2 = "select sum(nvl(txn_amt,0)) as lm_pay_amt2 " + " from act_pay_ibm " + " where 1=1 "
				+ " and p_seqno = ? " + " and nvl(proc_mark,'N') <> 'Y' " + " and nvl(error_code,'0') = '0'";
		sqlSelect(sql2, new Object[] { wp.itemStr("acct_p_seqno") });

		double lmPayAmt2 = sqlNum("lm_pay_amt2");
		double lmAmt = wp.itemNum("line_credit_amt") + lmPayAmt + lmPayAmt2;

		// -調升-
		if (wp.itemNum("tot_amt_month") > lmAmt || wp.itemNum("adj_inst_pct") > lmAmt) {
			busi.func.CcasFunc cs = new busi.func.CcasFunc();
			if (cs.limitUserAuth(wp.loginUser, wp.itemStr2("id_p_seqno"), wp.itemStr2("acno_p_seqno"),
					wp.itemNum("tot_amt_month")) == -1) {
				return -1;
			}
		} else {
			if (itemEq("conf_flag", "Y") == false) {
				String lsMesg = "一般限額臨調 : " + wp.itemNum("tot_amt_month") // +"\n"
						+ ", 分期限額臨調 : " + wp.itemNum("adj_inst_pct") // + "\n"
						+ ", 繳款未消帳臨調 : " + lmAmt // +"\n"
						+ "; 是否存檔 ???";
				// wp.col_set("conf_mesg", " || 1==1 ");
				wp.javascript("var resp =confirm('" + lsMesg + "');" + wp.newLine + "if (resp) {" + wp.newLine
						+ "  document.dataForm.conf_flag.value='Y';" + wp.newLine + "  top.submitControl('U');"
						+ wp.newLine + "}" + wp.newLine + "else {" + wp.newLine + "  alert('取消存檔!!!');" + wp.newLine
						+ "}");
				/*
				 * if (1==2 ${conf_mesg}) { var resp = confirm("繳款未消帳臨調 , 是否存檔"); if ( resp ==
				 * true ) { document.dataForm.conf_flag.value="Y"; top.submitControl('U'); }
				 * else { document.dataForm.conf_flag.value="N"; } }
				 */
				return -2;
			}
		}
		return 1;
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud("XX");
			if (wp.autUpdate() == false)
				return;
			if (wp.colEq("xx_temp", "Y")) {
				this.btnDeleteOn(true);
			} else {
				this.btnDeleteOn(false);
			}

		}

	}

	@Override
	public void initPage() {

	}

	void f5CalcAmt() {
		double lmAmtMonth = 0.0;
		double lmLineCreditAmt = 0.0;
		lmAmtMonth = wp.itemNum("tot_amt_month");
		lmLineCreditAmt = wp.itemNum("line_credit_amt");

		wp.sqlCmd = "select risk_type," + " uf_tt_risk_type(risk_type) as tt_risk_type," + " lmt_amt_time_pct,"
				+ " lmt_cnt_day as adj_day_cnt," + " lmt_cnt_month as adj_month_cnt" + " from cca_risk_consume_parm"
				+ " where area_type ='T' " + " and card_note =? and risk_level =?" + " order by risk_type";

		setString2(1, wp.itemStr2("card_note"));
		setString(wp.itemStr2("risk_level"));

		this.selectNoLimit();
		pageQuery();
		wp.listCount[0] = sqlRowNum;

		int llNrow = sqlRowNum;
		for (int ii = 0; ii < llNrow; ii++) {
//      if (pos("|J|R|G|C", wp.colStr(ii, "risk_type")) > 0) {
//        wp.colSetNum(ii, "adj_month_amt", lm_line_credit_amt, 0);
//        wp.colSetNum(ii, "adj_day_amt",
//            (lm_line_credit_amt * wp.colNum(ii, "lmt_amt_time_pct") / 100), 0);
//      } else {
//        wp.colSetNum(ii, "adj_month_amt", lm_amt_month, 0);
//        wp.colSetNum(ii, "adj_day_amt", (lm_amt_month * wp.colNum(ii, "lmt_amt_time_pct") / 100),
//            0);
//      }

			wp.colSetNum(ii, "adj_month_amt", lmAmtMonth, 0);
			wp.colSetNum(ii, "adj_day_amt", (lmAmtMonth * wp.colNum(ii, "lmt_amt_time_pct") / 100), 0);

			if (wp.itemEmpty("adj_eff_start_date")) {
				wp.colSet(ii, "adj_date1", getSysDate());
			} else {
				wp.colSet(ii, "adj_date1", wp.itemStr("adj_eff_start_date"));
			}

			if (wp.itemEmpty("adj_eff_end_date")) {
				wp.colSet(ii, "adj_date2", commdate.dateAdd(getSysDate(), 0, 1, 0));
			} else {
				wp.colSet(ii, "adj_date2", wp.itemStr("adj_eff_end_date"));
			}

		}
	}

}
