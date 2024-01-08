package ccam01;
/**
 * 2019-0925   JH    spec_status
 * 19-0611:   JH    p_seqno >>acno_pxxx
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * */

import ofcapp.BaseAction;

public class Ccam2120 extends BaseAction {
	String kkPSeqno = "", acctpSeqno = "", cardAcctIdx = "" , checkErrMsg ="";
	int lmPayAmt = 0, lmPayAmt2 = 0, lmAmt = 0;
	ccam01.Ccam2120Func func = null;
	busi.func.CcasFunc ooCcas = null;
	taroko.base.CommDate commDate = new taroko.base.CommDate();

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
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "S2")) {
			/* 動態查詢 */
			f5CalcAmt();
		} else if (eqIgno(wp.buttonCode, "S3")) {
			/* 會簽單 */
			dataRead0930();
		}
	}

	@Override
	public void dddwSelect() {
		try {
			if (eqIgno(wp.respHtml, "ccam2120_detl")) {
				wp.optionKey = wp.colStr(0, "adj_reason");
				dddwList("dddw_adj_reason", "cca_sys_parm3", "sys_key", "sys_data1", "where sys_id = 'ADJREASON'");
			}
		} catch (Exception ex) {
		}

		try {
			if (eqIgno(wp.respHtml, "ccam2120_detl")) {
				wp.optionKey = wp.colStr(0, "ex_risk_type");
				dddwList("dddw_risktype", "vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
			}
		} catch (Exception ex) {
		}

		String lsSql = "";
		try {
			if (eqIgno(wp.respHtml, "ccam2120_detl2")) {
				if (wp.colEmpty("id_no") == false) {
					if (wp.colEq("chg", "Y"))
						wp.colSet("card_no", "");
					lsSql = " select A.card_no as db_code , " + " A.card_no as db_desc "
							+ " from cca_card_base A , crd_card B "
							+ " where A.card_no =B.card_no and B.current_code='0' and B.sup_flag ='0' "
							+ " and A.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
							+ sqlCol(wp.colStr("id_no"), "id_no") + " ) " + " order by 2 Asc";
					wp.optionKey = wp.colStr("card_no");
					dddwList("dddw_card_no", lsSql);
				}
			}

		} catch (Exception ex) {
		}

		try {
			if (eqIgno(wp.respHtml, "ccam2120_detl2")) {
				wp.optionKey = wp.colStr("trial_action");
				ddlbList("dddw_trial_action", wp.colStr("trial_action"), "ecsfunc.DeCodeRsk.trial_action");
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

		String lsWhere = " where debit_flag <>'Y'";

		if (!empty(wp.itemStr("ex_card_no"))) {
			lsWhere += " and acno_p_seqno in (select acno_p_seqno from crd_card where 1=1 "
					+ sqlCol(wp.itemStr("ex_card_no"), "card_no") + ") ";
			// String sql1 = "select p_seqno as acctp_seqno "
			// +", acno_p_seqno"
			// + " from crd_card "
			// + " where card_no = ? ";
			// sqlSelect(sql1, new Object[] {
			// wp.item_ss("ex_card_no")
			// });
			//
			// if (sql_nrow <= 0) {
			// err_alert("卡號錯誤 or 查無資料");
			// return;
			// }
			// kk_p_seqno = sql_ss("acno_p_seqno");
			// acctp_seqno = sql_ss("acctp_seqno");
			// if (empty(kk_p_seqno) && empty(acctp_seqno)) {
			// err_alert("查無帳戶序號");
			// return;
			// }
			//
			// ls_where +=" and acno_p_seqno <>''"
			// +" and acno_p_seqno in (:p_seqno,:acno_p_seqno)";
			// ppp("p_seqno",acctp_seqno);
			// ppp("acno_p_seqno",kk_p_seqno);
		} else if (!wp.itemEmpty("ex_idno")) {
			if (wp.itemStr("ex_idno").length() == 10) {
				lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
						+ sqlCol(wp.itemStr("ex_idno"), "id_no") + ") ";
			} else if (wp.itemStr("ex_idno").length() == 8) {
				lsWhere += " and corp_p_seqno in (select corp_p_seqno from crd_corp where 1=1 "
						+ sqlCol(wp.itemStr("ex_idno"), "corp_no") + ") ";
			} else {
				alertErr2("身分證字號 為 8碼 or 10碼");
				return;
			}
		}

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = "" + " C.acno_p_seqno ," + " C.p_seqno as acctp_seqno," + " C.debit_flag ," + " C.acno_flag ,"
				+ " C.card_acct_idx ," + " C.block_status ,"
				+ " uf_spec_status(C.spec_status,C.spec_del_date) as spec_status," + " C.adj_quota ,"
				+ " C.adj_eff_start_date ," + " C.adj_eff_end_date ," + " C.adj_area ," + " C.tot_amt_month ,"
				+ " C.adj_inst_pct ," + " C.adj_remark ," + " uf_acno_key(C.acno_p_seqno) as acct_key , "
				+ " C.mod_user , " + " uf_corp_no(C.corp_p_seqno) as corp_no , "
				+ " uf_tt_ccas_parm3('ADJREASON',C.adj_reason) as tt_adj_reason,"
				// + " (select sys_data1 from cca_sys_parm3 where sys_key =C.adj_reason and
				// sys_id =
				// 'ADJREASON') as tt_adj_reason , "
				+ " to_char(C.mod_time,'yyyymmdd') as mod_date ";
		wp.daoTable = "cca_card_acct C";

		pageQuery();
		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}
		listWkData();
		wp.setPageValue();
		wp.setListCount(1);
	}

	void listWkData() {
		String sql1 = "select acct_type , " + " line_of_credit_amt ," + " corp_act_flag , "
				+ " uf_acno_name(acno_p_seqno) as chi_name " + " from act_acno " + " where acno_p_seqno = ? ";
		
		String sql2 = "select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wp.logSql = false;
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "acno_p_seqno") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "acct_type", sqlStr("acct_type"));
				wp.colSet(ii, "line_of_credit_amt", sqlStr("line_of_credit_amt"));
				wp.colSet(ii, "acno_name", sqlStr("chi_name"));
			}
			wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "-" + wp.colStr(ii, "acct_key"));
			wp.colSet(ii, "adj_eff_date", commString.strToYmd(wp.colStr(ii, "adj_eff_start_date")) + " -- "
					+ commString.strToYmd(wp.colStr(ii, "adj_eff_end_date")));
			
			if(wp.colEq(ii,"acct_type", "03") || wp.colEq(ii,"acct_type", "06")) {
				sqlSelect(sql2,new Object[] {wp.colStr(ii,"acno_p_seqno")});
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

		wp.selectSQL = "" + " card_acct_idx , " + " acno_flag , " + " acno_p_seqno, p_seqno as acctp_seqno,"
				+ " id_p_seqno , " + " uf_idno_id(id_p_seqno) as id_no , " + " adj_area , " + " adj_area as org_area ,"
				+ " adj_reason , " + " adj_reason as org_reason ," + " tot_amt_month , "
				+ " tot_amt_month as org_amt_mon ," + " adj_eff_start_date , "
				+ " adj_eff_start_date as ori_start_date , " + " adj_eff_end_date ,"
				+ " adj_eff_end_date as ori_end_date , " + " adj_inst_pct , " + " adj_inst_pct as ori_inst_pct , "
				+ " adj_remark , " + " adj_remark as org_remark ," + " spec_status , " + " adj_risk_flag ,"
				+ " mod_user," + " crt_user," + " crt_date," + " adj_date," + " debit_flag,"
				+ " to_char(mod_time,'yyyymmdd') as mod_date ," + " uf_idno_name(id_p_seqno) as chi_name ,"
				+ " 0 as comp_amt , " + " 0 as comp_inst_amt , " + " 0 as line_credit_amt , " + " block_reason1 , "
				+ " block_reason2 , " + " block_reason3 , " + " block_reason4 , " + " block_reason5 , "
				+ " block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as wk_block_reason ,"
				+ " uf_spec_status(spec_status,spec_del_date) as spec_status," + " hex(rowid) as rowid, mod_seqno , "
				+ " notice_flag , adj_sms_flag ";
		wp.daoTable = " cca_card_acct";
		wp.whereStr = " where 1=1 " + col(toNum(cardAcctIdx), "card_acct_idx",true);

		pageSelect();
		if (sqlRowNum <= 0) {
			alertErr("查無資料, key=" + cardAcctIdx);
			return;
		}

		dataReadAfter();
		selectCcaAdjParm();
		if (wp.colEmpty("adj_eff_start_date") && wp.colEmpty("adj_eff_end_date")) {
			wp.colSet("adj_sms_flag", "N");
			wp.colSet("notice_flag", "N");
		}		
	}

	void selectCcaAdjParm() throws Exception {
		wp.pageRows = 999;
		wp.sqlCmd = " select " + " card_acct_idx , " + " risk_type, card_note, risk_level,"
				+ " uf_tt_risk_type(risk_type) as tt_risk_type , " + " adj_month_amt , " // -月限額-
				+ " adj_day_amt, " // -次限額-
				+ " adj_day_cnt , " // -日限次-
				+ " adj_month_cnt ," // -月限次-
				+ " adj_eff_start_date as adj_date1 , " + " adj_eff_end_date as adj_date2 , "
				+ " spec_flag , "
				+ " decode(spec_flag,'Y','checked','') as opt2_on , "
				+ " '' as opt1_on "
				+ " from cca_adj_parm "
				+ " where 1=1 " + col(wp.colNum("card_acct_idx"), "card_acct_idx", true) + " order by risk_type";
		pageQuery();

		if (sqlNotFind()) {
			wp.colSet("IND_NUM", 0);
			selectOK();
//      String lsCardNote = getCardNote(wp.colStr("acno_p_seqno"));
//      wp.sqlCmd = " select " + " A.card_note , " + " A.area_type , " + " A.risk_type , "
//          + " A.risk_level , " + " A.lmt_amt_month_pct , " + " A.rsp_code_1 , "
//          + " A.lmt_cnt_month , " + " A.rsp_code_2 , " + " A.lmt_amt_time_pct , "
//          + " A.rsp_code_3 , " + " A.lmt_cnt_day , " + " A.rsp_code_4 , " + " A.add_tot_amt , "
//          + " uf_tt_risk_type(A.risk_type) as tt_risk_type " + " from cca_risk_consume_parm A "
//          + " where A.area_type ='T' " + " and A.card_note = ?" + " and A.risk_level =?"
//          + " order by A.risk_type";
//      setString2(1, lsCardNote);
//      setString(wp.colStr("class_code"));
//
//      pageQuery();
//      int llNrow = sqlRowNum;
//      double lmLineAmt = wp.colNum("line_credit_amt");
//      double lmTotAmt = lmLineAmt; // wp.col_num("tot_amt_month");
//      for (int ii = 0; ii < llNrow; ii++) {
//        if (pos("|J|R|G|C", wp.colStr(ii, "risk_type")) > 0) {
//          wp.colSetNum(ii, "adj_month_amt", lmLineAmt, 0);
//          wp.colSetNum(ii, "adj_day_amt", (lmLineAmt * wp.colNum(ii, "lmt_amt_time_pct") / 100),
//              0);
//        } else {
//          wp.colSetNum(ii, "adj_month_amt", lmTotAmt, 0);
//          wp.colSetNum(ii, "adj_day_amt", (lmTotAmt * wp.colNum(ii, "lmt_amt_time_pct") / 100),
//              0);
//        }
//        wp.colSet(ii, "adj_day_cnt", wp.colStr(ii, "lmt_cnt_day"));
//        wp.colSet(ii, "adj_month_cnt", wp.colStr(ii, "lmt_cnt_month"));
//        
//        if(wp.colEmpty("adj_eff_start_date")) {
//      	  wp.colSet(ii,"adj_date1",getSysDate());
//        } else {
//      	  wp.colSet(ii,"adj_date1",wp.colStr("adj_eff_start_date"));
//        }
//        
//        if(wp.colEmpty("adj_eff_end_date")) {
//      	  wp.colSet(ii,"adj_date2",commDate.dateAdd(getSysDate(), 0, 1, 0));
//        } else {
//      	  wp.colSet(ii,"adj_date2",wp.colStr("adj_eff_start_date"));
//        }
//        
//      }
		} else {
			wp.listCount[0] = sqlRowNum;
			wp.colSet("IND_NUM", sqlRowNum);
		}
//    wp.setListCount(0);
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

	void dataReadAfter() throws Exception {
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
		if (wp.colEmpty("adj_eff_start_date") && wp.colEmpty("adj_eff_end_date")) {
			wp.colSet("tot_amt_month", sqlStr("line_credit_amt"));
			wp.colSet("org_amt_mon", sqlStr("line_credit_amt"));
		}
		wp.colSet("line_credit_amt", sqlStr("line_credit_amt"));
		wp.colSet("class_code", sqlStr("class_code"));
		wp.colSet("risk_level", sqlStr("class_code"));
		wp.colSet("min_pay_bal", sqlStr("min_pay_bal"));
		wp.colSet("last_payment_date", sqlStr("last_payment_date"));
		if (wp.colNum("adj_inst_pct") == 0) {
			wp.colSet("adj_inst_pct", sqlStr("line_credit_amt"));
		}

		if (wp.colNum("min_pay_bal") > 0) {
			wp.colSet("min_color", "style='color:red;'");
		}

		String sql4 = " select " + " min_pay_bal , " + " last_payment_date " + " from act_acct "
				+ " where p_seqno = ? ";

		setString2(1, wp.colStr("acctp_seqno"));

		sqlSelect(sql4);
		if (sqlRowNum > 0) {
			wp.colSet("min_pay_bal", sqlStr("min_pay_bal"));
			wp.colSet("last_payment_date", sqlStr("last_payment_date"));
			if (wp.colNum("min_pay_bal") > 0) {
				wp.colSet("min_color", "style='color:red;'");
			}
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
			return;
		}
		wp.colSet("rela_flag", sqlStr("rela_flag"));
		wp.colSet("asset_balance", sqlStr("asset_balance"));
		wp.colSet("bond_amt", sqlStr("bond_amt"));
		// --Stage 1
		double lmAmt = selectPayAmtStage1(wp.colStr("acno_p_seqno"), wp.colStr("acct_type"));
		// --Stage 2
//    double lmAmt = wfPayAmt(wp.colStr("acctp_seqno"));
		wp.colSetNum("db_pay_amt", lmAmt, 0);

		String lsPseqno = wp.colStr("acno_p_seqno");
		String lsCardNote = getCardNote(lsPseqno);
		wp.colSet("card_note", lsCardNote);
		// --dddw
		wp.optionKey = wp.colStr(0, "ex_risk_type");
		dddwList("dddw_risk_type", "cca_risk_consume_parm", "risk_type", "risk_type||'.'||uf_tt_risk_type(risk_type)",
				"where area_type ='T' " + sqlCol(wp.colStr("card_note"), "card_note")
						+ sqlCol(wp.colStr("class_code"), "risk_level"));
		checkSupCard();
		
		if(wp.colEq("acct_type", "03") || wp.colEq("acct_type", "06")) {
			String sql5 = "";
			sql5 = " select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
			sqlSelect(sql5,new Object[] {wp.colStr("acno_p_seqno")});
			
			if(sqlRowNum > 0 ) {
				wp.colSet("card_no", sqlStr("card_no"));
			}
		}
		
	}

	double selectPayAmtStage1(String aAcnoPseqno, String aAcctType) {
		String sql1 = "";
		if (aAcctType.equals("90")) {
			sql1 = "select pay_amt from cca_card_acct where acno_p_seqno = ? and debit_flag = 'Y' ";
		} else {
			sql1 = "select pay_amt from cca_card_acct where acno_p_seqno = ? and debit_flag <> 'Y' ";
		}
		sqlSelect(sql1, new Object[] { aAcnoPseqno });
		if (sqlRowNum > 0) {
			return sqlNum("pay_amt");
		}
		return 0;
	}

	void checkSupCard() {
		String sql1 = " select " + " count(*) as db_cnt " + " from crd_card " + " where acno_p_seqno = ? "
				+ " and current_code = '0' " + " and sup_flag = '1' ";
		sqlSelect(sql1, new Object[] { wp.colStr("acno_p_seqno") });

		if (sqlNum("db_cnt") > 0) {
			wp.colSet("card_sup_cnt", sqlNum("db_cnt"));
		} else {
			wp.colSet("card_sup_cnt", "0");
		}
	}

	double wfPayAmt(String aPseqno) {
		if (empty(aPseqno))
			return 0;

		String sql1 = "select sum(nvl(a.pay_amt,0)) as lm_pay_amt " + " from  act_pay_detail a, act_pay_batch b "
				+ " where 1=1 " + " and a.p_seqno = ? " + " and a.batch_no = b.batch_no " + " and b.batch_tot_cnt > 0 "
				+ " and b.batch_no not like '%9001%'";
		sqlSelect(sql1, new Object[] { aPseqno });

		double lmPayAmt = sqlNum("lm_pay_amt");

		String sql2 = "select sum(nvl(txn_amt,0)) as lm_pay_amt2 " + " from act_pay_ibm " + " where 1=1 "
				+ " and p_seqno = ? " + " and nvl(proc_mark,'N') <> 'Y' " + " and nvl(error_code,'0') = '0'";
		sqlSelect(sql2, new Object[] { aPseqno });

		double lmPayAmt2 = sqlNum("lm_pay_amt2");

		return lmPayAmt + lmPayAmt2;
	}

	@Override
	public void saveFunc() throws Exception {
		// String[] aa_rt = wp.item_buff("risk_type");

		if (eqIgno(wp.respHtml, "ccam2120_detl2")) {
			rskm02.Rskm0930Func func2 = new rskm02.Rskm0930Func();
			func2.setConn(wp);

			rc = func2.dbSave(strAction);
			sqlCommit(rc);

			if (rc != 1) {
				errmsg(func2.getMsg());
			} else
				saveAfter(false);

		} else {
			wp.listCount[0] = wp.itemRows("risk_type");
			optNumKeep(wp.itemRows("risk_type"), "opt_del", "opt1_on");
			optNumKeep(wp.itemRows("risk_type"), "opt", "opt2_on");
			func = new ccam01.Ccam2120Func();
			func.setConn(wp);
			ooCcas = new busi.func.CcasFunc();
			ooCcas.setConn(wp);

			if (!isDelete()) {
				if (wp.itemEq("adj_reason", "02")) {
					if (wp.itemNum("tot_amt_month") > (wp.itemNum("line_credit_amt") + wp.itemNum("db_pay_amt"))) {
						alertErr2("臨調額度 不可大於 目前額度+未銷帳金額");
						return;
					}
				} else {
					if (wp.itemNum("tot_amt_month") > wp.itemNum("line_credit_amt")) {
						alertErr2("一般臨調放大%超過使用者權限");
						return;
					}
				}
			}

			if (this.isDelete() == false) {
				int ck = wfCheckPayAmt();
				if (ck == -2) {
					rc = -2;
					return;
				}
				
				if(checkSpecAmt()==false) {
					alertErr(checkErrMsg);
					return ;
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

			this.saveAfter(true);
			alertMsg("資料存檔完成");
		}

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

			if (checkBoxOptOn(ll, optDel))
				continue;
			if (checkBoxOptOn(ll, opt)) {
				func.varsSet("spec_flag", "Y");
			} else {
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
			if (wp.itemEq("adj_risk_flag", "Y")) {
				if (checkHighRisk(lsRiskType)) {
					func.varsSet("adj_month_amt", "0");
					func.varsSet("adj_month_cnt", "0");
					func.varsSet("adj_day_amt", "0");
					func.varsSet("adj_day_cnt", "0");
				}
			}

			if (func.insertDetlParm() == 1) {
				llOk++;
				// wp.col_set(ll,"ok_flag","V");
			} else {
				wp.colSet(ll, "ok_flag", "X");
			}
		}
		if (llOk > 0) {
			sqlCommit(1);
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

	/*
	 * int checkData() { String sql1 = "select sum(nvl(a.pay_amt,0)) as lm_pay_amt "
	 * + " from  act_pay_detail a, act_pay_batch b " + " where 1=1 " +
	 * " and a.p_seqno = ? " + " and a.batch_no = b.batch_no " +
	 * " and b.batch_tot_cnt > 0 " + " and b.batch_no not like '%9001%'";
	 * sqlSelect(sql1, new Object[] { wp.item_ss("p_seqno") });
	 * 
	 * lm_pay_amt = (int) sql_num("lm_pay_amt");
	 * 
	 * String sql2 = "select sum(nvl(txn_amt,0)) as lm_pay_amt2 " +
	 * " from act_pay_ibm " + " where 1=1 " + " and p_seqno = ? " +
	 * " and nvl(proc_mark,'N') <> 'Y' " + " and nvl(error_code,'0') = '0'";
	 * sqlSelect(sql2, new Object[] { wp.item_ss("p_seqno") });
	 * 
	 * lm_pay_amt2 = (int) sql_num("lm_pay_amt2");
	 * 
	 * lm_amt = (int) (wp.item_num("line_credit_amt") + lm_pay_amt + lm_pay_amt2);
	 * 
	 * if (wp.item_num("tot_amt_month") > lm_amt || wp.item_num("adj_inst_pct") >
	 * lm_amt) { return -1; } else { if (item_eq("conf_flag", "Y") == false) {
	 * wp.respMesg = "一般限額臨調 : " + wp.item_num("tot_amt_month") + "\n" + "分期限額臨調 : "
	 * + wp.item_num("adj_inst_pct") + "\n" + "繳款未消帳臨調 : " + wp.item_num("lm_amt") +
	 * "\n" + "是否存檔  ???"; // wp.col_set("conf_mesg","Y"); wp.col_set("conf_mesg",
	 * " || 1==1 "); return -2; } } return 1; }
	 */
	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		btnModeAud("XX");
		if (eqIgno(wp.respHtml, "ccam2120_detl2")) {
			this.btnAddOn(true);
			wp.colSet("tel_user", wp.loginUser);
			wp.colSet("tel_date", getSysDate());
			wp.colSet("tel_time", commDate.sysTime());
			wp.colSet("adj_date1", getSysDate());
			wp.colSet("reply_date", getSysDate());
			wp.colSet("sms_flag", "Y");
			if (wp.colEmpty("audit_remark")) {
				wp.colSet("audit_remark", "詳附件");
			}
		}

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

	void f5CalcAmt() {
		double lmAmtMonth = 0.0;
		double lmLineCreditAmt = 0.0;
		lmAmtMonth = wp.itemNum("tot_amt_month");
		lmLineCreditAmt = wp.itemNum("line_credit_amt");
		wp.listCount[0] = wp.itemRows("risk_type");

		wp.sqlCmd = "select risk_type," + " uf_tt_risk_type(risk_type) as tt_risk_type," + " lmt_amt_time_pct,"
				+ " lmt_cnt_day as adj_day_cnt," + " lmt_cnt_month as adj_month_cnt" + " from cca_risk_consume_parm"
				+ " where area_type ='T' " + " and card_note =? and risk_level =?" + " order by risk_type";

		setString2(1, wp.itemStr2("card_note"));
		setString(wp.itemStr2("risk_level"));

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
			// wp.col_set(ii, "adj_day_cnt", wp.col_ss(ii, "lmt_cnt_day"));
			// wp.col_set(ii, "adj_month_cnt", wp.col_ss(ii, "lmt_cnt_month"));

			wp.colSetNum(ii, "adj_month_amt", lmAmtMonth, 0);
			wp.colSetNum(ii, "adj_day_amt", (lmAmtMonth * wp.colNum(ii, "lmt_amt_time_pct") / 100), 0);

			if (wp.itemEmpty("adj_eff_start_date")) {
				wp.colSet(ii, "adj_date1", getSysDate());
			} else {
				wp.colSet(ii, "adj_date1", wp.itemStr("adj_eff_start_date"));
			}

			if (wp.itemEmpty("adj_eff_end_date")) {
				wp.colSet(ii, "adj_date2", commDate.dateAdd(getSysDate(), 0, 1, 0));
			} else {
				wp.colSet(ii, "adj_date2", wp.itemStr("adj_eff_end_date"));
			}
		}
	}

	int wfCheckPayAmt() throws Exception {
		double lmPayAmt = wfPayAmt(wp.itemStr2("acctp_seqno"));
		// -永調+繳款未銷帳-
		double lmAmt = wp.itemNum("line_credit_amt") + lmPayAmt;

		// -調升>(永調+繳款未銷帳)-
		if (wp.itemNum("tot_amt_month") > lmAmt || wp.itemNum("adj_inst_pct") > lmAmt) {
			if (ooCcas.limitUserAuth(wp.loginUser, wp.itemStr2("id_p_seqno"), wp.itemStr2("acctp_seqno"),
					wp.itemNum("tot_amt_month")) == -1) {
				return -1;
			}
			return 2; // haveto approve
		} else {
			if (itemEq("conf_flag", "Y") == false) {
				if (!wp.itemEq("adj_reason", "02"))
					return 1;
				String lsMesg = "一般限額臨調 : " + wp.itemNum("tot_amt_month") // +"\n"
						+ ", 分期限額臨調 : " + wp.itemNum("adj_inst_pct") // + "\n"
						+ ", 繳款未消帳臨調 : " + lmAmt // +"\n"
						+ "; 是否存檔 ???";
				// wp.col_set("conf_mesg", " || 1==1 ");
				wp.javascript("var resp =confirm('" + lsMesg + "');" + wp.newLine + "if (resp) {" + wp.newLine
						+ "  document.dataForm.conf_flag.value='Y';" + wp.newLine + "  top.submitControl('U');"
						+ wp.newLine + "}" + wp.newLine + "else {" + wp.newLine + "  alert('取消存檔!!!');" + wp.newLine
						+ "}");
				return -2; // user-confirm
			}
		}

		return 1;
	}

	void dataRead0930() {
		cardAcctIdx = wp.itemStr("data_k1");
		String lsIdPSeqno = "", lsLastSixMonth = "";
		double lmAcnoLmt = 0, lmCardLmt = 0;
		String sql1 = " select " + " chi_name , " + " company_name , " + " job_position , " + " id_p_seqno , "
				+ " uf_nvl(student,'N') as student , " + " nation , " + " uf_nvl(asset_value,0) as asset_value , "
				+ " id_no , " + " e_mail_addr " + " from crd_idno " + " where id_no = ? ";

		sqlSelect(sql1, new Object[] { cardAcctIdx });

		if (sqlRowNum <= 0) {
			errmsg("非本行卡友");
			return;
		}

		lsIdPSeqno = sqlStr("id_p_seqno");
		wp.colSet("id_p_seqno", lsIdPSeqno);
		wp.colSet("chi_name", sqlStr("chi_name"));
		wp.colSet("comp_name", sqlStr("company_name"));
		wp.colSet("comp_title", sqlStr("job_position"));
		wp.colSet("id_no", sqlStr("id_no"));
		wp.colSet("email_addr", sqlStr("e_mail_addr"));

		wp.colSet("card_cond_02", "N");
		if (!eqIgno(sqlStr("student"), "Y") && !eqIgno(sqlStr("nation"), "2")) {
			wp.colSet("card_cond_02", "Y");
		}

		// -- 無保證人 擔保品
		String sql2 = " select " + " count(*) as ll_cnt " + " from crd_rela " + " where id_p_seqno = ? "
				+ " and rela_type ='1' ";

		sqlSelect(sql2, new Object[] { lsIdPSeqno });

		wp.colSet("card_cond_03", "Y");

		if (sqlNum("ll_cnt") > 0 || sqlNum("asset_value") > 0) {
			wp.colSet("card_cond_03", "N");
		}

		if (sqlNum("ll_cnt") > 0) {
			wp.colSet("rela_flag", "Y");
		} else {
			wp.colSet("rela_flag", "N");
		}

		// --持卡時間
		lsLastSixMonth = commDate.dateAdd(this.getSysDate(), 0, -6, 0);
		String sql3 = " select " + " count(*) as ll_cnt3 " + " from crd_card " + " where 1=1 " + " and id_p_seqno = ? "
				+ " and issue_date < ? ";

		sqlSelect(sql3, new Object[] { lsIdPSeqno, lsLastSixMonth });

		wp.colSet("card_cond_04", "N");
		if (sqlNum("ll_cnt3") > 0) {
			wp.colSet("card_cond_04", "Y");
		}

		// --行員
		wp.colSet("card_cond_05", "N");
		String sql4 = " select " + " count(*) as ll_cnt4 " + " from crd_employee " + " where id = ? "
				+ " and status_id in ('1','7') ";

		sqlSelect(sql4, new Object[] { wp.itemStr("id_no") });

		if (sqlNum("ll_cnt4") <= 0) {
			String sql5 = " select " + " sum( "
					+ " case when payment_rate1 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
					+ " case when payment_rate2 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
					+ " case when payment_rate3 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
					+ " case when payment_rate4 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
					+ " case when payment_rate5 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
					+ " case when payment_rate6 in ('0A','0B','0C','0D','00') then 1 else 0 end " + " ) as li_0E "
					+ " from act_acno "
					+ " where acno_p_seqno in (select acno_p_seqno from crd_card where id_p_seqno = ? ) ";

			sqlSelect(sql5, new Object[] { lsIdPSeqno });

			if (sqlNum("li_0E") >= 3) {
				wp.colSet("card_cond_05", "Y");
			}

		} else {
			wp.colSet("card_cond_05", "Y");
		}

		// --無延滯記錄
		String sql6 = " select " + " sum( "
				+ " case when payment_rate1 not in ('0A','0C','00','0E') then 1 else 0 end + "
				+ " case when payment_rate2 not in ('0A','0C','00','0E') then 1 else 0 end + "
				+ " case when payment_rate3 not in ('0A','0C','00','0E') then 1 else 0 end + "
				+ " case when payment_rate4 not in ('0A','0C','00','0E') then 1 else 0 end + "
				+ " case when payment_rate5 not in ('0A','0C','00','0E') then 1 else 0 end + "
				+ " case when payment_rate6 not in ('0A','0C','00','0E') then 1 else 0 end " + " ) as ll_cnt6 "
				+ " from act_acno " + " where id_p_seqno = ? ";
		sqlSelect(sql6, new Object[] { lsIdPSeqno });

		wp.colSet("card_cond_06", "Y");

		if (sqlNum("ll_cnt6") > 0)
			wp.colSet("card_cond_06", "N");

		// --信用額度
		String sql7 = " select " + " sum(A.line_of_credit_amt) as lm_acno_lmt " + " from act_acno A "
				+ " where uf_nvl(A.corp_act_flag,'N') <> 'Y' " + " and A.id_p_seqno = ? "
				+ " and exists (select 1 from crd_card B where A.acct_type = B.acct_type "
				+ " and A.id_p_seqno = B.id_p_seqno and B.current_code = '0' ) ";

		sqlSelect(sql7, new Object[] { lsIdPSeqno });
		lmAcnoLmt = sqlNum("lm_acno_lmt");
		if (sqlRowNum <= 0)
			lmAcnoLmt = 0;

		String sql8 = " select " + " sum(line_of_credit_amt) as lm_card_lmt " + " from act_acno "
				+ " where acno_p_seqno in "
				+ " (select acno_p_seqno from crd_card where major_id_p_seqno = ? and current_code ='0' "
				+ " fetch first 1 rows only ) ";

		sqlSelect(sql8, new Object[] { lsIdPSeqno });
		lmCardLmt = sqlNum("lm_card_lmt");
		if (sqlRowNum <= 0)
			lmCardLmt = 0;

		wp.colSet("card_amt1", "" + (int) lmCardLmt);
		wp.colSet("acno_amt1", "" + (int) lmAcnoLmt);

		String sql9 = " select " + " count(*) as corp_cnt " + " from act_acno A "
				+ " where uf_nvl(A.corp_act_flag,'N') = 'Y' " + " and A.id_p_seqno = ? " + " and exists ( "
				+ " select 1 from crd_card B where A.acct_type = B.acct_type and A.id_p_seqno = B.id_p_seqno "
				+ " and current_code ='0' )  ";

		sqlSelect(sql9, new Object[] { lsIdPSeqno });

		if (sqlNum("corp_cnt") > 0) {
			wp.colSet("acno_amt1", "" + (int) lmCardLmt);
		}

		// -近12個月消費金額,近12個月消費金額:-
		wfCardConsume(lsIdPSeqno);

		// --期中覆審--
		String sql10 = " select " + " trial_date , " + " risk_group , " + " action_code " + " from rsk_trial_idno "
				+ " where id_p_seqno = ? ";

		sqlSelect(sql10, new Object[] { lsIdPSeqno });

		if (sqlRowNum > 0) {
			wp.colSet("trial_date", sqlStr("trial_date"));
			wp.colSet("risk_group", sqlStr("risk_group"));
			wp.colSet("trial_action", sqlStr("action_code"));
		}

		wp.colSet("chg", "Y");

		wp.colSet("user_no", wp.loginUser);
		userName();

	}

	void userName() {
		String sql1 = " select " + " usr_cname " + " from sec_user " + " where usr_id = ? ";

		sqlSelect(sql1, new Object[] { wp.loginUser });

		if (sqlRowNum > 0) {
			wp.colSet("user_name", sqlStr("usr_cname"));
		}

	}

	void wfCardConsume(String lsIdPSeqno) {
		if (empty(lsIdPSeqno))
			return;

		String lsDate = "", lsYm1 = "", lsYm2 = "";
		double lmLastAmt = 0, lmThisAmt = 0;

		lsDate = commString.mid(commDate.dateAdd(this.getSysDate(), -1, 0, 0), 0, 4);
		lsYm1 = lsDate + "01";
		lsYm2 = lsDate + "02";

		String sql1 = " select " + " sum("
				+ " uf_nvl(consume_bl_amt,0)+uf_nvl(consume_ca_amt,0)+uf_nvl(consume_it_amt,0)+"
				+ " uf_nvl(consume_ao_amt,0)+uf_nvl(consume_id_amt,0)+uf_nvl(consume_ot_amt,0)" + " ) - sum("
				+ " uf_nvl(sub_bl_amt,0)+uf_nvl(sub_ca_amt,0)+uf_nvl(sub_it_amt,0)+ "
				+ " uf_nvl(sub_ao_amt,0)+uf_nvl(sub_id_amt,0)+uf_nvl(sub_ot_amt,0) " + " ) as lm_last_amt "
				+ " from mkt_post_consume " + " where card_no in ( "
				+ " select card_no from crd_card where major_id_p_seqno = ? " + " ) "
				+ " and acct_month >= ? and acct_month <= ? ";

		sqlSelect(sql1, new Object[] { lsIdPSeqno, lsYm1, lsYm2 });
		lmLastAmt = sqlNum("lm_last_amt");
		if (eqIgno(sqlStr("lm_last_amt"), null)) {
			lmLastAmt = 0;
		}
		wp.colSet("lastyy_consum_amt", "" + lmLastAmt);

		// --近12個月消費
		lsDate = commDate.dateAdd(getSysDate(), 0, -1, 0);
		lsYm1 = commString.mid(commDate.dateAdd(lsDate, 0, -12, 0), 0, 6);
		lsDate = commString.mid(lsDate, 0, 6);

		String sql2 = " select " + " sum(uf_nvl(his_purchase_amt,0))+sum(uf_nvl(his_cash_amt,0)) as lm_amt2 "
				+ " from act_anal_sub "
				+ " where p_seqno in (select p_seqno from crd_card where major_id_p_seqno = ? ) "
				+ " and acct_month >= ? " + " and acct_month <= ? ";

		sqlSelect(sql2, new Object[] { lsIdPSeqno, lsYm1, lsDate });
		lmThisAmt = sqlNum("lm_amt2");
		if (eqIgno(sqlStr("lm_amt2"), null)) {
			lmThisAmt = 0;
		}

		wp.colSet("yy_consum_amt", "" + (int) lmThisAmt);

	}

}
