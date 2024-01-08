package ccam01;
/** 臨時調整額度維護（依卡戶）
 * 2019-1210   Alex  add initButton
 * 2019-0823   JH    adj_limit_approve()
 * 2019-0820   JH    spec_status
 * 19-0611:    JH    p_seqno >>acno_pxxx
 * 18-0926:		JH		立即生效
 * 18-0810:		JH		簡訊
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-10-16 V1.00.02  tanwei           updated for project coding standard
 * 109-12-22 V1.00.03  Justin            concat sql -> parameterize sql
 * */

import ofcapp.BaseAction;

public class Ccam2050 extends BaseAction {
	ccam01.Ccam2050Func func = null;
	ofcapp.EcsApprove ooAppr = null;
	taroko.base.CommDate commdate = new taroko.base.CommDate();
	boolean aprModify = false;
	String cardAcctIdx = "";
	String isAprUser = "", isAprPawd = "";
	String checkErrMsg = "";
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
			// onchange:調整一般月限額至--
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

		wp.sqlCmd = "select C.acct_type," + " C.p_seqno ," + " C.debit_flag ," + " C.acno_flag ," + " C.card_acct_idx ,"
				+ " C.block_status ," + " uf_spec_status(C.spec_status,C.spec_del_date) as spec_status,"
				+ " C.adj_quota ," + " C.adj_eff_start_date ," + " C.adj_eff_end_date ," + " C.adj_area ,"
				+ " C.tot_amt_month ," + " C.adj_inst_pct ," + " C.adj_remark ,"
				+ " uf_acno_key2(C.acno_p_seqno,C.debit_flag) as acct_key , "
				+ " uf_corp_no(C.corp_p_seqno) as corp_no , " + " C.adj_reason , "
				+ "  (select sys_data1 from cca_sys_parm3 where sys_key =C.adj_reason and sys_id = 'ADJREASON') as tt_adj_reason , "
				+ " C.acno_p_seqno " + " from cca_card_acct C" + " where debit_flag <>'Y'";
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

	}

	void listWkdata(int aRow) {
		String sql1 = "select " + " line_of_credit_amt ," + " acno_flag , " + " uf_acno_name(acno_p_seqno) as chi_name "
				+ " from act_acno" + " where acno_p_seqno =?";
		String sql2 = "select 'Y' as unapr_flag" + " from cca_card_acct_T" + " where card_acct_idx =?"
				+ " and  mod_type ='ADJ-LIMIT'" + commSqlStr.rownum(1);
		
		String sql3 = "select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
		
		for (int ii = 0; ii < aRow; ii++) {
			setString2(1, wp.colStr(ii, "acno_p_seqno"));
			sqlSelect(sql1);
			if (sqlRowNum > 0) {
				wp.colSet(ii, "line_of_credit_amt", sqlStr("line_of_credit_amt"));
				wp.colSet(ii, "acno_name", sqlStr("chi_name"));
			}
			wp.colSet(ii, "adj_eff_date", commString.strToYmd(wp.colStr(ii, "adj_eff_start_date")) + " -- "
					+ commString.strToYmd(wp.colStr(ii, "adj_eff_end_date")));
			// --
			setDouble(1, wp.colNum(ii, "card_acct_idx"));
			sqlSelect(sql2);
			if (sqlRowNum > 0) {
				wp.colSet(ii, "unapr_flag", "Y");
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
		if (empty(cardAcctIdx) || commString.strToNum(cardAcctIdx) == 0) {
			alertErr2("授權帳戶號碼(card_acct_idx): 不可空白");
			return;
		}

		wp.daoTable = " cca_card_acct";
		wp.whereStr = " where 1=1 " + col(toNum(cardAcctIdx), "card_acct_idx",true);
		wp.selectSQL = "" + " card_acct_idx , " + " acno_flag , " + " acno_p_seqno , " + " id_p_seqno , "
				+ " adj_area , " + " adj_area as org_area ," + " adj_reason , " + " adj_reason as org_reason ,"
				+ " tot_amt_month , " + " tot_amt_month as org_amt_mon ," + " adj_eff_start_date , "
				+ " adj_eff_start_date as ori_start_date , " + " adj_eff_end_date ,"
				+ " adj_eff_end_date as ori_end_date , " + " adj_inst_pct , " + " adj_inst_pct as ori_inst_pct , "
				+ " adj_remark , " + " adj_remark as org_remark ," + " spec_status , " + " adj_risk_flag ,"
				+ " mod_user," + " crt_user," + " crt_date," + " adj_date," + " debit_flag,"
				+ " to_char(mod_time,'yyyymmdd') as mod_date ," + " uf_idno_name(id_p_seqno) as chi_name ,"
				+ " 0 as comp_amt , " + " 0 as comp_inst_amt , " + " 0 as line_credit_amt , " + " block_reason1 , "
				+ " block_reason2 , " + " block_reason3 , " + " block_reason4 , " + " block_reason5 , "
				+ " block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as wk_block_reason ,"
				+ " uf_spec_status(spec_status,spec_del_date) as spec_status," + " hex(rowid) as rowid, mod_seqno , "
				+ " notice_flag , " + " adj_sms_flag ";

		pageSelect();
		if (sqlRowNum <= 0) {
			alertErr("查無資料, key=" + cardAcctIdx);
			return;
		}

		dataReadAfter();

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
		
		// selectRiskLevelCardNote();
	}

	// void selectRiskLevelCardNote(){
	// String ls_card_note = "" , ls_risk_level = "";
	//
	// ls_card_note = get_cardNote(wp.col_ss("acno_p_seqno"));
	// wp.col_set("card_note",ls_card_note);
	// wp.col_set("risk_level",wp.col_ss("class_code"));
	// }

	void selectCcaAdjParm() throws Exception {
		wp.pageRows = 999;
		wp.sqlCmd = " select " + " card_acct_idx , " + " risk_type, card_note, risk_level,"
				+ " uf_tt_risk_type(risk_type) as tt_risk_type , " + " adj_month_amt , " // -月限額-
				+ " adj_day_amt, " // -次限額-
				+ " adj_day_cnt , " // -日限次-
				+ " adj_month_cnt , " // -月限次-
				+ " adj_eff_start_date as adj_date1 , " + " adj_eff_end_date as adj_date2 , "
				+ " spec_flag , "
				+ " decode(spec_flag,'Y','checked','') as opt2_on , "
				+ " '' as opt1_on "
				+ " from cca_adj_parm "
				+ " where 1=1 " + col(wp.colNum("card_acct_idx"), "card_acct_idx", true) + " order by risk_type";
		pageQuery();
		wp.listCount[0] = sqlRowNum;
		wp.colSet("IND_NUM", sqlRowNum);
		if (sqlNotFind()) {
			wp.colSet("IND_NUM", 0);
			selectOK();
//			selectCcaRiskConsumeParm();
		}
	}

	void selectCcaRiskConsumeParm() {
		String lsCardNote = wp.colStr("card_note");
		String sql1 = "select risk_type," + " uf_tt_risk_type(risk_type) as tt_risk_type," + " lmt_amt_time_pct,"
				+ " lmt_cnt_day as adj_day_cnt," + " lmt_cnt_month as adj_month_cnt" + " from cca_risk_consume_parm"
				+ " where area_type ='T' " + " and card_note =? and risk_level =?" + " order by risk_type";

		wp.sqlCmd = sql1;
		setString2(1, lsCardNote);
		setString(wp.colStr("class_code"));
		pageQuery();
		if (sqlNotFind()) {
			wp.sqlCmd = sql1;
			setString2(1, "*");
			setString(wp.colStr("class_code"));

			pageQuery();
		}

		int llNrow = sqlRowNum;
		double lmLineAmt = wp.colNum("line_credit_amt");
		double lmTotAmt = lmLineAmt; // wp.col_num("tot_amt_month");
		for (int ii = 0; ii < llNrow; ii++) {
			double lmAmt = (lmLineAmt * wp.colNum(ii, "lmt_amt_time_pct") / 100);
			if (pos("|J|R|G|C", wp.colStr(ii, "risk_type")) > 0) {
				wp.colSetNum(ii, "adj_month_amt", lmLineAmt, 0);
				wp.colSetNum(ii, "adj_day_amt", lmAmt, 0);
			} else {
				wp.colSetNum(ii, "adj_month_amt", lmTotAmt, 0);
				wp.colSetNum(ii, "adj_day_amt", lmAmt, 0);
			}
			// wp.col_set(ii, "adj_day_cnt", wp.col_ss(ii, "lmt_cnt_day"));
			// wp.col_set(ii, "adj_month_cnt", wp.col_ss(ii, "lmt_cnt_month"));

			if (wp.colEmpty("adj_eff_start_date")) {
				wp.colSet(ii, "adj_date1", getSysDate());
			} else {
				wp.colSet(ii, "adj_date1", wp.colStr("adj_eff_start_date"));
			}

			if (wp.colEmpty("adj_eff_end_date")) {
				wp.colSet(ii, "adj_date2", commdate.dateAdd(getSysDate(), 0, 1, 0));
			} else {
				wp.colSet(ii, "adj_date2", wp.colStr("adj_eff_start_date"));
			}

		}
		wp.listCount[0] = llNrow;
	}

	String getCardNote(String aPseqno) {
		String lsCardNote = "*";
		String lsSql = "select A.card_note, count(*) as cnt_parm"
				+ " from crd_card A join cca_risk_consume_parm B on B.card_note=A.card_note and B.apr_date<>''"
				+ " where A.current_code='0'" + " and A.acno_p_seqno =?" + " group by A.card_note"
				+ " order by decode(A.card_note,'C',9,'G',8,'P',7,'S',6,'I',5,10)";
		setString2(1, aPseqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			for (int ll = 0; ll < sqlRowNum; ll++) {
				if (sqlInt("cnt_parm") > 0) {
					lsCardNote = sqlStr("card_note");
					break;
				}
			}
		}
		if (empty(lsCardNote))
			lsCardNote = "*";
		// 通用--
		// wp.col_set("kk_card_note",ls_card_note);
		return lsCardNote;
	}

	void dataReadAfter() throws Exception {
		String sql0 = " select count(*) as db_cnt from crd_card where acno_p_seqno = ? and current_code='0' and sup_flag='1' ";
		sqlSelect(sql0, new Object[] { wp.colStr("acno_p_seqno") });
		if (sqlRowNum > 0) {
			wp.colSet("card_sup_cnt", sqlStr("db_cnt"));
		}

		String sql1 = "select acct_type, acct_key" + ", p_seqno as acct_p_seqno"
				+ ", line_of_credit_amt as line_credit_amt" + ", uf_acno_name(acno_p_seqno) as acno_name"
				+ ", class_code " + " from act_acno " + " where 1=1 " + " and acno_p_seqno = ? ";
		setString2(1, wp.colStr("acno_p_seqno"));
		sqlSelect(sql1);
		if (sqlRowNum <= 0) {
			alertErr2("查無帳戶資料(act_acno), kk=" + wp.colStr("acno_p_seqno"));
			return;
		}
		wp.colSet("acct_type", sqlStr("acct_type"));
		wp.colSet("acct_key", sqlStr("acct_key"));
		wp.colSet("acct_p_seqno", sqlStr("acct_p_seqno"));
		wp.colSet("line_credit_amt", sqlStr("line_credit_amt"));
		wp.colSet("class_code", sqlStr("class_code"));
		if (wp.colNum("adj_inst_pct") == 0) {
			wp.colSet("adj_inst_pct", sqlStr("line_credit_amt"));
		}

		// --2018-07-12
		String sql3 = " select " + " tot_amt_month " + " from cca_card_acct " + " where "
				+ " to_char(sysdate,'yyyymmdd') between adj_eff_start_date and adj_eff_end_date "
				+ col(cardAcctIdx, "card_acct_idx",true);

		sqlSelect(sql3);
		if (sqlNum("tot_amt_month") > sqlNum("line_credit_amt")) {
			wp.colSet("line_credit_amt_t", sqlStr("tot_amt_month"));
		} else {
			wp.colSet("line_credit_amt_t", sqlStr("line_credit_amt"));
		}

		// --
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

		// --
		double lmAmt = wfPayAmt(wp.colStr("acct_p_seqno"));
		wp.colSetNum("db_pay_amt", lmAmt, 0);

		// --
		String lsPseqno = wp.colStr("acno_p_seqno");
		String lsCardNote = getCardNote(lsPseqno);
		wp.colSet("card_note", lsCardNote);
		//--dddw
		if(wp.colEq("acno_flag","2") == false) {
			//--acno_flag 調整限額或專款專用沒有意義 所以封起來
			wp.optionKey = wp.colStr(0, "ex_risk_type");
			dddwList("dddw_risk_type", "cca_risk_consume_parm", "risk_type", "risk_type||'.'||uf_tt_risk_type(risk_type)", 
					"where area_type ='T' "+sqlCol(wp.colStr("card_note"), "card_note")+sqlCol(wp.colStr("class_code"), "risk_level"));
		}
		
		
		lsCardNote = null;
		
		if(wp.colEq("acct_type", "03") || wp.colEq("acct_type", "06")) {
			String sql4 = "";
			sql4 = " select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
			sqlSelect(sql4,new Object[] {wp.colStr("acno_p_seqno")});
			
			if(sqlRowNum > 0 ) {
				wp.colSet("card_no", sqlStr("card_no"));
			}
		}		
		
	}

	@Override
	public void saveFunc() throws Exception {
		boolean lsCheckAdj = false;
		Double ldCheckAmt = 0.0;
		// String[] aa_rt = wp.item_buff("risk_type");
		wp.listCount[0] = wp.itemRows("risk_type");
		optNumKeep(wp.itemRows("risk_type"),"opt_del","opt1_on");
		optNumKeep(wp.itemRows("risk_type"),"opt","opt2_on");
		isAprUser = wp.itemStr("approval_user");
		isAprPawd = wp.itemStr("approval_passwd");

		func = new ccam01.Ccam2050Func();
		func.setConn(wp);
		ooAppr = new ofcapp.EcsApprove(wp);

		if (!wp.itemEq("adj_eff_start_date", wp.itemStr("ori_start_date"))
				|| !wp.itemEq("adj_eff_end_date", wp.itemStr("ori_end_date"))
				|| wp.itemNum("tot_amt_month") > wp.itemNum("org_amt_mon")
				|| wp.itemNum("adj_inst_pct") > wp.itemNum("ori_inst_pct")) {
			lsCheckAdj = true;
			if (wp.itemNum("tot_amt_month") >= wp.itemNum("adj_inst_pct"))
				ldCheckAmt = wp.itemNum("tot_amt_month");
			else
				ldCheckAmt = wp.itemNum("adj_inst_pct");
		}

		if (this.isDelete() == false) {
			if(checkSpecAmt()==false) {
				alertErr(checkErrMsg);
				return ;
			}
			
			
			if (lsCheckAdj == true) {
				int ck = wfCheckPayAmt(wp.itemStr2("acct_p_seqno"));
				if (ck == 2) {
					// --戶特指 91 一定要甲級
					if (ooAppr.specApprove(wp.itemStr("approval_user"), wp.itemStr("spec_status")) != 1) {
						alertErr2(ooAppr.getMesg());
						return;
					}

					// -主管覆核-
					if (ooAppr.adjLimitApprove(wp.modPgm(), wp.itemStr("approval_user"), wp.itemStr("approval_passwd"),
							ldCheckAmt) != 1) {
						alertErr2(ooAppr.getMesg());
						return;
					}

				} else if (ck == -2) {
					rc = -2;
					wp.respMesg = "　";
					return;
				}
			}
		}

		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if (rc != 1) {
			errmsg(func.getMsg());
			return;
		}
		if (this.isDelete()) {
			dataRead(); // re-retrieve-
			alertMsg("臨調資料, 刪除完成");
			return;
		}

		detlUpdate();
		if (rc != 1)
			return;

		// -發送簡訊-
		procSmsAdj();
		if (rc != 1) {
			return;
		}
		this.saveAfter(true);
		wp.colSet("approval_user", "");
		wp.colSet("approval_passwd", "");
		alertMsg("資料存檔完成");
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
	
		
	
	void procSmsAdj() {
		// 臨調簡訊
		// 姓名,調額,起日(xx月xx日),迄日(xx月xx日)--
		// -只送姓名-
		if (wp.itemEq("adj_sms_flag", "Y") == false)
			return;
		if (wp.itemNum("tot_amt_month") == 0)
			return;

		busi.func.SmsMsgDetl func = new busi.func.SmsMsgDetl();
		func.setConn(wp);
		String lsMsgDesc = wp.itemStr2("chi_name");

		int liRc = func.ccaM2050Adj(wp.itemNum("card_acct_idx"), lsMsgDesc);
		if (liRc == 1) {
			sqlCommit(1);
			return;
		}
		alertErr2(func.getMsg());
	}

	void detlUpdate() {
		int llOk = 0;
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

			if (func.insertDetlParm() == 1) {
				llOk++;
				// wp.col_set(ll,"ok_flag","V");
			} else {
				wp.colSet(ll, "ok_flag", "X");
				errmsg("風險分類新增錯誤 !");
				continue ;
			}
		}
		
//		if (rc==1 && wp.itemEq("adj_risk_flag", "Y")) {			
//			String lsStartDate = wp.itemStr("adj_eff_start_date");
//			String lsEndDate = wp.itemStr("adj_eff_end_date");
//			String sql1 = "select sys_key from cca_sys_parm1 where sys_id = 'RISK' and sys_data3 = 'Y' ";
//			sqlSelect(sql1);
//			if(sqlRowNum<=0) {
//				rc=1;
//			}	else	{
//				int rowCnt = sqlRowNum ;
//				String lsTempRisk = "";
//				for(int ii=0;ii<rowCnt;ii++) {
//					lsTempRisk = sqlStr(ii,"sys_key");
//					if(checkRiskList(lsTempRisk)==false) continue;
//					func.varsSet("risk_type", lsTempRisk);
//					func.varsSet("adj_month_amt", "0");
//					func.varsSet("adj_month_cnt", "0");
//					func.varsSet("adj_day_amt", "0");
//					func.varsSet("adj_day_cnt", "0");
//					func.varsSet("adj_eff_start_date", lsStartDate);
//					func.varsSet("adj_eff_end_date", lsEndDate);
//					func.varsSet("spec_flag", "N");
//					if (func.insertDetlParm() == 1) {
//						llOk++;						
//					} else {						
//						errmsg("新增管制高風險分類錯誤 !");
//						continue ;
//					}
//				}								
//			}
//		}
		
		if (llOk > 0) {
			sqlCommit(1);
		}
	}
	
	boolean checkRiskList(String lsRisk) {
		String[] riskType = wp.itemBuff("risk_type");
		String[] optDel = wp.itemBuff("opt_del");
		for(int ll=0;ll<wp.itemRows("risk_type");ll++) {
			if(this.checkBoxOptOn(ll, optDel))	continue;
			if(lsRisk.equals(riskType[ll]))	return false;			
		}		
		return true;
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

	double wfPayAmt(String aPseqno) {
		if (empty(aPseqno))
			return 0;

		// String sql1 = "select sum(nvl(a.pay_amt,0)) as lm_pay_amt "
		// + " from act_pay_detail a, act_pay_batch b "
		// + " where 1=1 "
		// + " and a.p_seqno = ? "
		// + " and a.batch_no = b.batch_no "
		// + " and b.batch_tot_cnt > 0 "
		// + " and b.batch_no not like '%9001%'";
		// sqlSelect(sql1, new Object[] {
		// a_pseqno
		// });
		//
		// double lm_pay_amt = sql_num("lm_pay_amt");

		String sql2 = "select sum(nvl(txn_amt,0)) as lm_pay_amt2 " + " from act_pay_ibm " + " where p_seqno = ? "
				+ " and nvl(proc_mark,'N') <> 'Y' " + " and nvl(error_code,'0') = '0'";
		sqlSelect(sql2, new Object[] { aPseqno });

		double lmPayAmt2 = sqlNum("lm_pay_amt2");

		// return lm_pay_amt + lm_pay_amt2;
		return lmPayAmt2;
	}

	int wfCheckPayAmt(String aPseqno) throws Exception {
		double lmPayAmt = wp.itemNum("db_pay_amt"); // wf_pay_amt(a_pseqno);
		// -永調+繳款未銷帳-
		double lmAmt = wp.itemNum("line_credit_amt") + lmPayAmt;

		if (itemEq("conf_flag", "Y") == false && wp.itemEq("adj_reason", "02")) {
			String lsMesg = "一般限額臨調 : " + wp.itemNum("tot_amt_month") // +"\n"
					+ ", 分期限額臨調 : " + wp.itemNum("adj_inst_pct") // + "\n"
					+ ", 繳款未消帳臨調 : " + lmAmt // +"\n"
					+ "; 是否存檔 ???";
			wp.javascript("var resp =confirm('" + lsMesg + "');" + wp.newLine + "if (resp) {" + wp.newLine
					+ "  document.dataForm.conf_flag.value='Y';" + wp.newLine + "  top.submitControl('U');" + wp.newLine
					+ "}" + wp.newLine + "else {" + wp.newLine + "  alert('取消存檔!!!');" + wp.newLine + "}");
			wp.colSet("approval_user", isAprUser);
			wp.colSet("approval_passwd", isAprPawd);
			return -2; // user-confirm
		}

		// -調升>(永調+繳款未銷帳)-
		if (wp.itemNum("tot_amt_month") > lmAmt || wp.itemNum("adj_inst_pct") > lmAmt) {
			return 2; // haveto approve
		}

		aprModify = false;
		aprModify = (wp.itemNum("tot_amt_month") > wp.itemNum("line_credit_amt_t")
				|| !wp.itemEq("adj_eff_start_date", wp.itemStr("ori_start_date"))
				|| !wp.itemEq("adj_eff_end_date", wp.itemStr("ori_end_date")));

		return 1;
	}

	// private int check_payDetail() {
	// double lm_amt =wp.item_num("line_credit_amt")+wp.item_num("db_pay_amt");
	// if (lm_amt <wp.item_num("tot_amt_month")) {
	// errmsg("調升須主管覆核");
	// return -1;
	// }
	//
	// return 1;
	// }
	//
	// private double get_payDetail(String a_pseqno) {
	//
	// double lm_amt1=0, lm_amt2=0;
	// String ls_sql ="select sum(pay_amt) as xx_pay_amt1 "
	// +" from act_pay_detail"
	// +" where p_seqno =?"; //p_seqno =?";
	// ppp(a_pseqno);
	// sqlSelect(ls_sql);
	// if (sql_nrow >0) {
	// lm_amt1 =sql_num("xx_pay_amt1");
	// }
	//
	// ls_sql ="select sum(pay_amt) as xx_pay_amt2"
	// +" from act_debt_cancel"
	// +" where process_flag <>'Y'"+
	// " and p_seqno =?";
	// ppp(1,a_pseqno);
	// sqlSelect(ls_sql);
	// if (sql_nrow >0) {
	// lm_amt2 =sql_num("xx_pay_amt2");
	// }
	//
	// return lm_amt1 +lm_amt2;
	// }
	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		btnModeAud("XX");

	}

	@Override
	public void initPage() {

	}

	void f5CalcAmt() {
		// select_cca_risk_consume_parm();
		int llNrow = wp.itemRows("risk_type");
		if (llNrow == 0) {
			selectCcaRiskConsumeParm();
			return;
		}
		double lmAmtMonth = wp.itemNum("tot_amt_month");
		//double lmLineCreditAmt = wp.itemNum("line_credit_amt");

		wp.listCount[0] = wp.itemRows("risk_type");
		String lsCardNote = getCardNote(wp.itemStr2("acno_p_seqno"));

		String sql1 = "select risk_type," + " uf_tt_risk_type(risk_type) as tt_risk_type," + " lmt_amt_time_pct,"
				+ " lmt_cnt_day as adj_day_cnt," + " lmt_cnt_month as adj_month_cnt" + " from cca_risk_consume_parm"
				+ " where area_type ='T' " + " and card_note =? and risk_level =?" + " order by risk_type";

		// ppp(1, wp.sss("card_note"));
		setString2(1, lsCardNote);
		setString(wp.itemStr2("class_code"));

		wp.sqlCmd = sql1;
		pageQuery();
		wp.listCount[0] = sqlRowNum;

		for (int ii = 0; ii < llNrow; ii++) {
			double lmAmt = (lmAmtMonth * wp.colNum(ii, "lmt_amt_time_pct") / 100);

			wp.colSetNum(ii, "adj_month_amt", lmAmtMonth, 0);
			wp.colSetNum(ii, "adj_day_amt", lmAmt, 0);

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
