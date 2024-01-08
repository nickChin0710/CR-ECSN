package ccam01;

/**
 * 19-0611:    JH    p_xxx >>acno_p_xxx
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
 * */
import ofcapp.BaseAction;

public class Ccaq2110 extends BaseAction {
	String acctKey = "", acctType = "" , amtDate = "";

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
			queryReadLog();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

	}

	@Override
	public void dddwSelect() {
		try {
			if (eqIgno(wp.respHtml, "ccaq2110")) {
				wp.optionKey = wp.colStr(0, "ex_acct_type");
				dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where card_indicator='2'");
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryRead() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataRead() throws Exception {
		if (empty(wp.itemStr("ex_corp_no"))) {
			alertErr2("公司統編 : 不可空白");
			return;
		}

		acctKey = wp.itemStr("ex_corp_no");
		acctType = wp.itemStr("ex_acct_type");

//    if (kk1.length() == 8) {
//      kk1 = kk1 + "000";
//    } else if (kk1.length() == 11) {
//      kk1 = kk1;
//    } else {
//      alertErr2("統編輸入錯誤");
//      return;
//    }

		wp.selectSQL = "" + " acct_key , "
				+ " bill_sending_zip||'-'||bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5 as bill_addr , "
				+ " line_of_credit_amt , " + " corp_p_seqno , " + " acno_p_seqno ," + " payment_rate1 ,"
				+ " payment_rate2 ," + " payment_rate3 ," + " payment_rate4 ," + " payment_rate5 ," + " payment_rate6 ,"
				+ " payment_rate7 ," + " payment_rate8 ," + " payment_rate9 ," + " payment_rate10 ,"
				+ " payment_rate11 ," + " payment_rate12 ," + " autopay_acct_bank ," + " autopay_acct_no ,"
				+ " int_rate_mcode ," + " acct_status ," + " line_of_credit_amt as tot_balance  ";
		wp.daoTable = " act_acno";
		wp.whereStr = " where 1=1 " + sqlCol(acctKey, "acct_key") + sqlCol(acctType, "acct_type");
		this.logSql();
		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, key=" + acctKey);
			return;
		}
		countValidCard();
		selectDataCorp();
		selectDataAct();
		selectDataAuthForStageOne();
		dataReadAfterStageOne();
//		dataReadAfter();

	}

	void selectDataCorp() {
		wp.selectSQL = "" + " chi_name , " + " corp_tel_zone1||'-'||corp_tel_no1||'-'||corp_tel_ext1 as tel_no_1 , "
				+ " corp_tel_zone2||'-'||corp_tel_no2||'-'||corp_tel_ext2 as tel_no_2 , " + " card_since ";
		wp.daoTable = " crd_corp";
		wp.whereStr = " where 1=1 " + sqlCol(wp.colStr("corp_p_seqno"), "corp_p_seqno");
		this.logSql();
		pageSelect();

	}

	public void countValidCard() {
		String sql1 = " select count(*) as relate_cnt " + " from crd_card"
				+ " where corp_p_seqno = ? and current_code = '0'";
		sqlSelect(sql1, new Object[] { wp.colStr("corp_p_seqno") });
		if (sqlRowNum <= 0)
			return;
		wp.colSet("relate_cnt", sqlStr("relate_cnt"));
	}

	void selectDataAct() {
		wp.selectSQL = "" + " sms_cell_phone , " + " organ_id , " + " auth_remark , "
				+ " sms_cell_phone as old_sms_phone ," + " auth_remark as old_auth_remark ," + " card_acct_idx ,"
				+ " 0 as wk_tot_consume," // " tot_amt_consume - tot_amt_precash as
											// wk_tot_consume ,"
				+ " 0 as tot_amt_precash ," + " 0 as tot_amt_consume ," + " adj_eff_start_date ,"
				+ " adj_eff_end_date ," + " tot_amt_month ";
		wp.daoTable = " cca_card_acct";
		wp.whereStr = " where 1=1 " + sqlCol(wp.colStr("acno_p_seqno"), "acno_p_seqno");

		pageSelect();
		if (sqlRowNum <= 0) {
			wp.notFound = "N";
			return;
		}		
//    selectDataAuth();
	}

	void selectDataAuthForStageOne() {
		String sql1 = "", sql2 = "" , sql3 = "" , sql4 = "";
		double ldTempAmt1 = 0, ldTempAmt2 = 0, ldTotalAmt = 0, ldJrnlBal = 0, ldPaidCash = 0, ldTempCash1 = 0,
				ldTempCash2 = 0 , ldPaidAmt = 0 , ldTxLogAmt1 = 0 , ldTxLogAmt2 = 0 , ldTxLogCash1 = 0, ldTxLogCash2 = 0;
		sql1 = " select  sum(tot_amt_consume) as tot_amt_consume , sum(jrnl_bal) as jrnl_bal ,  ";
		sql1 += " sum(total_cash_utilized) as total_cash_utilized , sum(pay_amt) as pay_amt from cca_card_acct ";
		sql1 += " where acno_p_seqno in (select acno_p_seqno from act_acno where corp_p_seqno = ? and acno_flag ='3') ";

		sqlSelect(sql1, new Object[] { wp.colStr("corp_p_seqno") });
		if (sqlRowNum > 0) {
			ldTempAmt1 = sqlNum("tot_amt_consume");
			ldJrnlBal = sqlNum("jrnl_bal");
			ldTempCash1 = sqlNum("total_cash_utilized");
			ldPaidAmt = sqlNum("pay_amt");
		} else {
			ldTempAmt1 = 0;
			ldJrnlBal = 0;
			ldTempCash1 = 0;
			ldPaidAmt = 0;
		}
		
		amtDate = getAmtDate();
		
		sql2 = " select sum(decode(cacu_cash,'Y',nt_amt,0)) as xx_tot_precash ";
		sql2 += " , sum(decode(cacu_amount,'Y',nt_amt,0)) as xx_tot_amt from cca_auth_txlog ";
		sql2 += " where mtch_flag not in ('Y','U') and tx_date > ? ";
		sql2 += " and card_no in (select card_no from crd_card where corp_p_seqno =?) ";

		sqlSelect(sql2, new Object[] { amtDate, wp.colStr("corp_p_seqno") });
		if (sqlRowNum > 0) {
			ldTempAmt2 = sqlNum("xx_tot_amt");
			ldTempCash2 = sqlNum("xx_tot_precash");
		} else {
			ldTempAmt2 = 0;
			ldTempCash2 = 0;
		}
		
		sql3 = "";
		sql3 += "select sum(auth_txlog_amt_1) as auth_txlog_amt_1 , ";
		sql3 += "sum(auth_txlog_amt_2) as auth_txlog_amt_2 , sum(auth_txlog_amt_cash_1) as auth_txlog_amt_cash_1 ,";
		sql3 += "sum(auth_txlog_amt_cash_2) as auth_txlog_amt_cash_2 from cca_consume ";
		sql3 += "where p_seqno in (select acno_p_seqno from act_acno where corp_p_seqno = ? and acno_flag ='3') ";
		
		sqlSelect(sql3, new Object[] { wp.colStr("corp_p_seqno") });
		
		if(sqlRowNum >0) {
			ldTxLogAmt1 = sqlNum("auth_txlog_amt_1"); ;
			ldTxLogAmt2 = sqlNum("auth_txlog_amt_2"); ; 
			ldTxLogCash1 = sqlNum("auth_txlog_amt_cash_1"); ;
			ldTxLogCash2 = sqlNum("auth_txlog_amt_cash_2");;
		}	else	{
			ldTxLogAmt1 = 0 ;
			ldTxLogAmt2 = 0 ; 
			ldTxLogCash1 = 0 ;
			ldTxLogCash2 = 0;
		}
		
		ldTotalAmt = ldTempAmt1 + ldTempAmt2 + ldTxLogAmt1 + ldTxLogAmt2;
		ldPaidCash = ldTempCash1 + ldTempCash2 + ldTxLogCash1 + ldTxLogCash2 ;
		
		//--沖正交易還額
		
		sql4 = "select ori_auth_seqno from cca_auth_txlog where tx_date > ? and trans_type = '0420' "
				+ "and card_no in (select card_no from crd_card where corp_p_seqno =?) and ori_auth_seqno <> '' "
				+ "and reversal_flag ='Y' "
				;
		
		sqlSelect(sql4, new Object[] { amtDate , wp.colStr("corp_p_seqno") });
				
		int ilRowNum = 0;
		ilRowNum = sqlRowNum;
		if (ilRowNum > 0) {
			String oriSql = "select nt_amt , tx_date , trans_code from cca_auth_txlog where auth_seqno = ? ";
			for (int ll = 0; ll < ilRowNum; ll++) {
				sqlSelect(oriSql, new Object[] { sqlStr(ll, "ori_auth_seqno") });
				if (sqlRowNum <= 0)
					continue;
				if (amtDate.compareTo(sqlStr("tx_date"))<0)
					continue; // --沖正今天交易尚未累積到cca_consume.auth_txlog_amt_1故不用還額
				if (commString.strIn(sqlStr("trans_code"), "CA|CO|AC")) {
					ldPaidCash = ldPaidCash - sqlNum("nt_amt");
				}
				ldTotalAmt = ldTotalAmt - sqlNum("nt_amt");
			}
		}
		
		//--						
		if(ldJrnlBal>0) {
			wp.colSet("bill_low_pay_amt", ldJrnlBal);
			wp.colSet("pre_pay_amt", 0);
		}	else	{
			wp.colSet("bill_low_pay_amt", 0);
			wp.colSet("pre_pay_amt", ldJrnlBal*-1);
		}
		
		wp.colSet("tot_unpaid_amt",ldPaidAmt);
		wp.colSet("wk_tot_consume", ldTotalAmt);
		wp.colSet("tot_amt_precash", ldPaidCash);
		wp.colSet("tot_amt_consume", ldTotalAmt);
	}

	void selectDataAuth() {

		wp.selectSQL = "" + " max_consume_date ," + " max_consume_amt ," + " max_precash_date ," + " max_precash_amt ,"
				+ " tot_due ," + " tot_limit_amt ," + " tot_precash_amt ," + " consume_1 ," + " consume_2 ,"
				+ " consume_3 ," + " consume_4 ," + " consume_5 ," + " consume_6 ," + " bill_low_limit ,"
				+ " paid_annual_fee ," + " unpaid_annual_fee ," + " paid_srv_fee ," + " unpaid_srv_fee ,"
				+ " paid_law_fee ," + " unpaid_law_fee ," + " paid_punish_fee ," + " paid_cycle ,"
				+ " paid_interest_fee ," + " unpaid_interest_fee ," + " paid_consume_fee ," + " unpaid_consume_fee ,"
				+ " paid_precash ," + " unpaid_precash ," + " unpost_inst_fee ," + " bill_low_pay_amt ," + " m1_amt ,"
				+ " tot_unpaid_amt ," + " argue_amt ," + " pre_pay_amt ,"
				+ " paid_consume_fee+unpaid_consume_fee+paid_precash+unpaid_precash+unpost_inst_fee as id_tot_unpaid_amt ,"
				+ " pay_date ," + " payment_due_date ," + " pay_settle_date , " + " pay_latest_amt ";
		wp.daoTable = " cca_consume";
		wp.whereStr = " where 1=1 " + sqlCol(wp.colStr("card_acct_idx"), "card_acct_idx");
		this.logSql();
		pageSelect();
		wp.notFound = "N";
	}
	
	void dataReadAfterStageOne() {
		if (empty(wp.colStr("adj_eff_start_date")) || empty(wp.colStr("adj_eff_end_date"))) {
			wp.colSet("tot_adj_balance", "0");
		} else {
			if (getSysDate().compareTo(wp.colStr("adj_eff_start_date")) >= 0
					&& wp.colStr("adj_eff_end_date").compareTo(getSysDate()) >= 0) {
				if (wp.colNum(0, "tot_amt_month") > 0) {
					wp.colSet("tot_adj_balance", "" + wp.colNum(0, "tot_amt_month"));
				}
			} else {
				wp.colSet("tot_adj_balance", "0");
			}
		}
		
		int liCanuseAmt = 0;
		if (wp.colNum("tot_adj_balance") != 0) {
			liCanuseAmt = (int) Math.round(wp.colNum(0, "tot_adj_balance") + wp.colNum("pre_pay_amt") + wp.colNum("tot_unpaid_amt") - wp.colNum("bill_low_pay_amt") - wp.colNum("tot_amt_consume"));
			wp.colSet("balance_color", "style='color:red'");
		} else {
			liCanuseAmt = (int) Math.round(
					wp.colNum(0, "tot_balance") + wp.colNum("pre_pay_amt") + wp.colNum("tot_unpaid_amt") - wp.colNum("bill_low_pay_amt") - wp.colNum("tot_amt_consume"));
		}
		wp.colSet("wk_canuse_amt", "" + liCanuseAmt);

		busi.func.CcsAccount ooac = new busi.func.CcsAccount();
		ooac.setConn(wp);
		ooac.procData(wp.colStr("acno_p_seqno"));
		wp.colSet("last_pay_date", ooac.payDate);
		wp.colSet("pay_settle_date", ooac.paySettleDate);
		wp.colSet("last_pay_amt", ooac.payLastestAmt);
		
	}
	
	void dataReadAfter() {

		if (empty(wp.colStr("adj_eff_start_date")) || empty(wp.colStr("adj_eff_end_date"))) {
			wp.colSet("tot_adj_balance", "0");
		} else {
			if (getSysDate().compareTo(wp.colStr("adj_eff_start_date")) >= 0
					&& wp.colStr("adj_eff_end_date").compareTo(getSysDate()) >= 0) {
				if (wp.colNum(0, "tot_amt_month") > 0) {
					wp.colSet("tot_adj_balance", "" + wp.colNum(0, "tot_amt_month"));
				}
			} else {
				wp.colSet("tot_adj_balance", "0");
			}
		}

		int liCanuseAmt = 0;
		if (wp.colNum("tot_adj_balance") != 0) {
			liCanuseAmt = (int) Math.round(wp.colNum(0, "tot_adj_balance") - wp.colNum(0, "id_tot_unpaid_amt")
					- wp.colNum(0, "tot_amt_consume"));
			wp.colSet("balance_color", "style='color:red'");
		} else {
			liCanuseAmt = (int) Math.round(
					wp.colNum(0, "tot_balance") - wp.colNum(0, "id_tot_unpaid_amt") - wp.colNum(0, "tot_amt_consume"));
		}
		wp.colSet("wk_canuse_amt", "" + liCanuseAmt);

		busi.func.CcsAccount ooac = new busi.func.CcsAccount();
		ooac.setConn(wp);
		ooac.procData(wp.colStr("acno_p_seqno"));
		wp.colSet("last_pay_date", ooac.payDate);
		wp.colSet("pay_settle_date", ooac.paySettleDate);
		wp.colSet("last_pay_amt", ooac.payLastestAmt);

	}

	void queryReadLog() throws Exception {
		acctKey = wp.itemStr("data_k1");
		wp.sqlCmd = " select * " + " from cca_auth_remark_log " + " where 1=1 " + sqlCol(acctKey, "card_acct_idx")
				+ " order by chg_date Desc , chg_time Desc ";

		pageQuery();

		if (sqlNotFind()) {
			alertErr2("查無備註記錄");
			return;
		}

		wp.setListCount(0);

	}

	@Override
	public void saveFunc() throws Exception {
		if (eqIgno(wp.itemStr("auth_remark"), wp.itemStr("old_auth_remark"))
				&& eqIgno(wp.itemStr("sms_cell_phone"), wp.itemStr("old_sms_phone"))) {
			errmsg("資料未修改, 不須存檔");
			return;
		}
		Ccaq2110Func func = new Ccaq2110Func();
		func.setConn(wp);
		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if (rc != 1) {
			errmsg(func.getMsg());
		} else
			this.saveAfter(false);
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		btnModeAud(wp.colStr("card_acct_idx"));
	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}
	
	String getAmtDate() {		
		String sql1 = "select wf_desc from ptr_sys_parm where wf_parm = 'SYSPARM' and wf_key = 'TXLOGAMT_DATE'";
		sqlSelect(sql1);
		if(sqlRowNum>0) {
			return sqlStr("wf_desc");
		}		
		return "";
	}
	
}
