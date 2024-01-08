/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-10-09  V1.00.02   Alex      修正預借現金額度不用考慮卡戶等級最高的預借現金額度,故點掉卡戶等級最高預借現金SQL        
* 112-08-30  V1.00.03   Ryan      modify 159                                    *
 * 2023-1113   V1.00.04 JH        cacu_amount <>'N'
 * 2023-1114 取消 where mtch_flag
******************************************************************************/
package ccam01;

import busi.FuncBase;

public class CcasLimitStageOne extends FuncBase {
	private taroko.base.CommDate commDate = new taroko.base.CommDate();

	private boolean debit = false;
	private String debitFlag = "N";
	private String acnoPSeqno = "", acctPSeqno = "";
	private String cardNo = "" , oriCardNo = "";
	private double cardAcctIdx = 0;
	private String corpPSeqno = "", acnoFlag = "", acctType = "";
	private int iiType = 1; // -1.一般,2.商務卡-
	private boolean comboIndicator = false;
	private String classCode = "";
	// --
	double lineCreditAmt = 0, totAmtMonth = 0;
	double lineCreditCash = 0, ccasLimitCash = 0;
	double paidConsume = 0; // , unpaid_consume_fee=0;
	double paidPrecash = 0; // unpaid_precash=0;
	double txNtAmt = 0, unpostInstFee = 0;
	double prePayAmt = 0, totUnpaidAmt = 0;
	double txLogAmt1 = 0, txLogCash1 = 0, txLogAmt2 = 0, txLogCash2 = 0;
	double totAmtConsume = 0, totAmtPrecash = 0, totReversalAmt = 0;
	double canUseLimit = 0, canUseCash = 0 , specCanUseLimit = 0;
	double cardLimit = 0, cardTotConsume = 0;
	double unpayAmt = 0; // --逾期未繳金額
	double specialAmt = 0; //--專款專用金額
	String amtDate = ""; //--已送AP4日期
	
	private void initData() {
		canUseLimit = 0;
		lineCreditAmt = 0;
		totAmtMonth = 0;
		lineCreditCash = 0;
		ccasLimitCash = 0;
		paidConsume = 0;
		// unpaid_consume_fee=0;
		paidPrecash = 0;
		// unpaid_precash=0;
		txNtAmt = 0;
		unpostInstFee = 0;
		prePayAmt = 0;
		totUnpaidAmt = 0;
		totAmtConsume = 0;
		totAmtPrecash = 0;
		canUseCash = 0;
		cardLimit = 0;
		cardTotConsume = 0;
		totReversalAmt = 0;
		specialAmt = 0;
		specCanUseLimit = 0;
		oriCardNo = "";
	}

	public int canUseLimit(String aCardno) {
		msgOK();
		initData();
		double lmAmt = 0;
		if (empty(aCardno))
			return 0;
		cardNo = nvl(aCardno);
		selectCcaCardBase();
		if (rc != 1)
			return 0;
		amtDate = getProcDate();
		if(amtDate.isEmpty()) {
			errmsg("查無系統參數: TXLOGAMT_DATE");
			return 0;
		}
		iiType = 1;
		// -已授權未請款:一般消費/預借現金-
		selectCcaAuthTxlog();
		// -已授權為請款:一般消費/預借現金- 補收檔時間差
		selectConsume(1);
		totAmtConsume = totAmtConsume + txLogAmt1 + txLogAmt2;
		totAmtPrecash = totAmtPrecash + txLogCash1 + txLogCash2;
		selectActAcno();
		if (rc != 1)
			return 0;
		// -已付款未銷帳-個人(payment末消)- cca_card_acct.pay_amt
		selectPayAmt();
		// -總消費金額-
		double lmAmt1 = totAmtConsume + paidConsume;
		double lmAmt2 = totUnpaidAmt;
		// -有臨調-
		if (totAmtMonth > 0) {
			lmAmt2 += totAmtMonth;
		} else
			lmAmt2 += lineCreditAmt;

		lmAmt2 = lmAmt2 - lmAmt1 - specialAmt;		
		canUseLimit = commString.numScale(lmAmt2, 0);
		specCanUseLimit = lmAmt2 + specialAmt;
		specCanUseLimit = commString.numScale(specCanUseLimit, 0);
		return 1;
	}
	
	String getProcDate() {		
		String sql1 = "select wf_desc from ptr_sys_parm where wf_parm = 'SYSPARM' and wf_key = 'TXLOGAMT_DATE'";
		sqlSelect(sql1);
		if(sqlRowNum>0) {
			return colStr("wf_desc");
		}		
		return "";
	}
	
	private void selectCcaCardBase() {
		strSql = "select A.acno_p_seqno, A.debit_flag, A.card_acct_idx"
				+ ", A.p_seqno as acct_p_seqno, A.corp_p_seqno, A.acct_type, A.acno_flag "
				+ ", A.CARD_ADJ_LIMIT, A.CARD_ADJ_DATE1, A.CARD_ADJ_DATE2" + ", B.INDIV_CRD_LMT, B.SON_CARD_FLAG , B.ori_card_no "
				+ " from cca_card_base A left join crd_card B on B.card_no=A.card_no" + " where A.card_no =?";
		setString2(1, cardNo);
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			errmsg("card no not exist[%s]", cardNo);
			return;
		}

		acnoPSeqno = colStr("acno_p_seqno");
		acctPSeqno = colNvl("acct_p_seqno", acnoPSeqno);
		debitFlag = colNvl("debit_flag", "N");
		debit = colEq("debit_flag", "Y");
		cardAcctIdx = colNum("card_acct_idx");
		corpPSeqno = colStr("corp_p_seqno");
		acnoFlag = colNvl("acno_flag", "1");
		acctType = colNvl("acct_type", "01");
		oriCardNo = colStr("ori_card_no");
		//--避免 in 空值
		if(oriCardNo.isEmpty())
			oriCardNo = "X";
		
		if (colEq("son_card_flag", "Y")) {
			cardLimit = colNum("indiv_crd_lmt");
			if (commDate.sysComp(colStr("card_adj_date1")) >= 0 && commDate.sysComp(colStr("card_adj_date2")) <= 0
					&& colNum("card_adj_limit") > 0) {
				cardLimit = colNum("card_adj_limit");
			}
			getCardConsume();
		}

		return;
	}

	private void getCardConsume() {
		// -子卡餘額-
		if (cardLimit < 0)
			return;

		strSql = "select sum(decode(cacu_amount,'N',0,nt_amt)) as xx_card_consume"
          + " from cca_auth_txlog"
				+ " where ori_card_no in (?) "
//				+ " (select uf_nvl(ori_card_no,card_no) from crd_card where card_no = ?) " 
				+ " and tx_date like to_char(sysdate,'yyyymm')||'%'"
		// +" and mtch_flag <>'Y'"
		;
		setString2(1, oriCardNo);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			cardTotConsume = colNum("xx_card_consume");
		}
	}

	private void selectActAcno() {
		// 20. AuConsume_IBM_RECEIVE_AMT: 指撥額度-個人
		if (!debit) {
			strSql = "select line_of_credit_amt, line_of_credit_amt_cash " + ", combo_indicator, class_code"
					+ " from act_acno" + " where 1=1";
			if (iiType == 2) {
				strSql += " and corp_p_seqno =? and acct_type =? and acno_flag='2'";
				setString2(1, corpPSeqno);
				setString(acctType);
			} else {
				strSql += " and acno_p_seqno =?";
				setString2(1, acnoPSeqno);
			}

			sqlSelect(strSql);
			if (sqlRowNum < 1) {
				errmsg("act_acno N-find");
				return;
			}
			lineCreditAmt = colNum("line_of_credit_amt");
			lineCreditCash = colNum("line_of_credit_amt_cash");
			comboIndicator = colEq("combo_indicator", "Y");
			classCode = colStr("class_code");
		} else {
			strSql = "select month_amount" + " from cca_debit_parm" + " where bin_no in ('000000',?)"
					+ " order by bin_no desc";
			setString2(1, commString.left(cardNo, 6));
			sqlSelect(strSql);
			if (sqlRowNum > 0) {
				lineCreditAmt = colNum("month_amount");
			}
		}

		// -臨調--
		strSql = "select tot_amt_month" + ", ADJ_EFF_END_DATE, ADJ_EFF_START_DATE" + "," + commSqlStr.sysYYmd
				+ " as xx_sysdate" + " from cca_card_acct" + " where 1=1"; // card_acct_idx =?";
		if (iiType == 2) {
			strSql += " and corp_p_seqno =? and acct_type =? and acno_flag='2'";
			setString2(1, corpPSeqno);
			setString(acctType);
		} else {
			strSql += " and card_acct_idx =?";
			setDouble2(1, cardAcctIdx);
		}

		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			return;
		}

		String lsDate1 = colStr("adj_eff_start_date");
		String lsDate2 = colStr("adj_eff_end_date");
		String lsSysdate = colStr("xx_sysdate");
		// -臨調期間-
		if (commString.strComp(lsSysdate, lsDate1) < 0 || commString.strComp(lsSysdate, lsDate2) > 0) {
			return;
		}

		if (debit) {
			totAmtMonth = lineCreditAmt * colNum("tot_amt_month") / 100;
		} else {
			totAmtMonth = colNum("tot_amt_month");
		}
		
		//--計算專款專用額度
		calSpecialAmt();
	}

	private void selectCcaAuthTxlogForCorp() {
		// 已授權未請款金額/預借現金
		// --cca_card_acct.tot_amt_consume + 今日授權消費
		double ldTempAmt1 = 0.0, ldTempAmt2 = 0.0, ldTempCash1 = 0.0, ldTempCash2 = 0.0;
		strSql = "select sum(tot_amt_consume) as tot_amt_consume , sum(jrnl_bal) as jrnl_bal , ";
		strSql += " sum(total_cash_utilized) as total_cash_utilized , sum(unpay_amt) as unpay_amt , ";
		strSql += " sum(pay_amt) as pay_amt ";
		strSql += " from cca_card_acct where acno_p_seqno in (select acno_p_seqno from act_acno where corp_p_seqno = ? and acno_flag ='3')  ";
		setString(1, corpPSeqno);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			ldTempAmt1 = colNum("tot_amt_consume");
			paidConsume = colNum("jrnl_bal");
			paidPrecash = colNum("total_cash_utilized");
			unpayAmt = colNum("unpay_amt");
			totUnpaidAmt = colNum("pay_amt");
		} else {
			ldTempAmt1 = 0;
			paidConsume = 0;
			paidPrecash = 0;
			unpayAmt = 0;
			totUnpaidAmt = 0;
		}

		strSql = "select sum(decode(cacu_cash,'Y',nt_amt,0)) as xx_tot_precash"
				+ ", sum(decode(cacu_amount,'N',0,nt_amt)) as xx_tot_amt"
          + " from cca_auth_txlog"
				+ " where 1=1"  //mtch_flag not in ('Y','U')"
          + " and tx_date > ? ";

		setString(1,amtDate);
		strSql += " and card_no in (select card_no from crd_card where corp_p_seqno =?)";
		setString2(2, corpPSeqno);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			ldTempAmt2 = colNum("xx_tot_amt");
			ldTempCash2 = colNum("xx_tot_precash");
		} else {
			ldTempAmt2 = 0;
			ldTempCash2 = 0;
		}

		totAmtConsume = ldTempAmt1 + ldTempAmt2;
		totAmtPrecash = paidPrecash + ldTempCash2;

		// --沖正交易還額
		strSql = "select ori_auth_seqno from cca_auth_txlog where tx_date > ? and trans_type = '0420' "
				+ "and card_no in (select card_no from crd_card where corp_p_seqno =?) and ori_auth_seqno <> '' "
				+ "and reversal_flag ='Y' "
				;

//		setString(1, getSysDate());
		setString(1,amtDate);
		setString(2, corpPSeqno);

		sqlSelect(strSql);

		int ilRowNum = 0;
		ilRowNum = sqlRowNum;
		if (ilRowNum <= 0)
			return;

		// --查詢原交易
		String oriSql = "select nt_amt , tx_date , trans_code from cca_auth_txlog where auth_seqno = ? ";
		for (int ll = 0; ll < ilRowNum; ll++) {
			sqlSelect(oriSql, new Object[] { colStr(ll, "ori_auth_seqno") });
			if (sqlRowNum <= 0)
				continue;
			if (amtDate.compareTo(colStr("tx_date"))<0)
				continue; // --沖正今天交易尚未累積到cca_consume.auth_txlog_amt_1故不用還額
			if (commString.strIn(colStr("trans_code"), "CA|CO|AC")) {
				totAmtPrecash = totAmtPrecash - colNum("nt_amt");
			}
			totAmtConsume = totAmtConsume - colNum("nt_amt");
		}

	}

	private void selectCcaAuthTxlog() {
		// 已授權未請款金額/預借現金
		// --cca_card_acct.tot_amt_consume + 今日授權消費
		double ldTempAmt1 = 0.0, ldTempAmt2 = 0.0, ldTempCash1 = 0.0, ldTempCash2 = 0.0;

		strSql = "select tot_amt_consume , jrnl_bal , total_cash_utilized , unpay_amt ";
		strSql += "from cca_card_acct where card_acct_idx = ? ";
		setDouble(1, cardAcctIdx);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			ldTempAmt1 = colNum("tot_amt_consume");
			paidConsume = colNum("jrnl_bal");
			paidPrecash = colNum("total_cash_utilized");
			unpayAmt = colNum("unpay_amt");
		} else {
			ldTempAmt1 = 0;
			paidConsume = 0;
			paidPrecash = 0;
			unpayAmt = 0;
		}

		strSql = "select sum(decode(cacu_cash,'Y',nt_amt,0)) as xx_tot_precash"
				+ ", sum(decode(cacu_amount,'N',0,nt_amt)) as xx_tot_amt"
          + " from cca_auth_txlog"
				+ " where 1=1" //mtch_flag not in ('Y','U')"
          + " and tx_date > ? and card_acct_idx =? ";
//		setString(1, getSysDate());
		setString(1,amtDate);
		setDouble2(2, cardAcctIdx);

		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			ldTempAmt2 = colNum("xx_tot_amt");
			ldTempCash2 = colNum("xx_tot_precash");
		} else {
			ldTempAmt2 = 0;
			ldTempCash2 = 0;
		}

		totAmtConsume = ldTempAmt1 + ldTempAmt2;
		totAmtPrecash = paidPrecash + ldTempCash2;

		// --沖正交易還額
//		strSql = "select ori_auth_seqno from cca_auth_txlog where tx_date > ? and trans_type = '0420' "
//				+ "and card_acct_idx =? and ori_auth_seqno <> '' and reversal_flag ='Y'  ";

//		setString(1, getSysDate());
//		setString(1,amtDate);
//		setDouble2(2, cardAcctIdx);
//
//		sqlSelect(strSql);

//		int ilRowNum = 0;
//		ilRowNum = sqlRowNum;
//		if (ilRowNum <= 0)
//			return;
//
//		// --查詢原交易
//		String oriSql = "select nt_amt , tx_date , trans_code from cca_auth_txlog where auth_seqno = ? ";
//		for (int ll = 0; ll < ilRowNum; ll++) {
//			sqlSelect(oriSql, new Object[] { colStr(ll, "ori_auth_seqno") });
//			if (sqlRowNum <= 0)
//				continue;
//			if (amtDate.compareTo(colStr("tx_date"))<0)
//				continue; // --沖正今天交易尚未累積到cca_consume.auth_txlog_amt_1故不用還額
//			if (commString.strIn(colStr("trans_code"), "CA|CO|AC")) {
//				totAmtPrecash = totAmtPrecash - colNum("nt_amt");
//			}
//			totAmtConsume = totAmtConsume - colNum("nt_amt");
//		}

	}

	private void selectPayAmt() {

		strSql = "select pay_amt from cca_card_acct where card_acct_idx = ? ";
		setDouble(1, cardAcctIdx);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			totUnpaidAmt = colNum("pay_amt");
		} else {
			totUnpaidAmt = 0;
		}

	}

	public void canUseCash() {
		msgOK();
		if (debit || empty(cardNo))
			return;

		double lmTmpPrecash = 0;
		lmTmpPrecash = paidPrecash;

		double lmCashRate = 0, lmAddTotAmt = 0;
		// -cca_risk_consume_parm-
		strSql = "select card_note, lmt_amt_month_pct, add_tot_amt" + " from cca_risk_consume_parm"
				+ " where 1=1 and area_type ='T'"
				+ " and card_note in (select card_note from crd_card where acno_p_seqno =? and current_code ='0'"
				+ " union select '*' from dual )" + " and risk_level =?" + " and risk_type ='C'"
				+ " order by decode(card_note,'*','zz',card_note)" + commSqlStr.rownum(1);
		setString2(1, acnoPSeqno);
		setString(classCode);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			lmCashRate = colNum("lmt_amt_month_pct");
			lmAddTotAmt = colNum("add_tot_amt");
		}
		double lma = 0;
		if (lmCashRate > 0) {
			lma = commString.numScale(lineCreditCash * (lmCashRate / 100), 0);
		}
		if (lmAddTotAmt > 0 && lma > lmAddTotAmt) {
			lma = lmAddTotAmt;
		}
		// --臨調額度(預現)--
		if (totAmtMonth > 0) {
			strSql = "select times_amt" + " from cca_adj_parm" + " where card_acct_idx =? and risk_type ='C'";
			setDouble2(1, cardAcctIdx);
			sqlSelect(strSql);
			if (sqlRowNum > 0) {
				lma = commString.numScale(lma * (colNum("times_amt") / 100), 0);
			}
//      if (sqlRowNum > 0 && colNum("times_amt") > 0) {
//        lm_a = commString.numScale(lm_a * (colNum("times_amt") / 100), 0);
//      }
		}

		// --取得卡戶等級預現總最高額度--
//--    2022/10/09  預借可餘不考慮max_cash_amt-		
//		strSql = "select max_cash_amt" + " from cca_risk_level_parm" + " where area_type ='T'"
//				+ " and risk_level =? and risk_type ='C'"
//				+ " and card_note in (select card_note from crd_card where acno_p_seqno =? and current_code='0'"
//				+ " union select '*' from dual ) " + " order by decode(card_note,'*','zz',card_note)" + commSqlStr.rownum(1);
//		setString2(1, classCode);
//		setString(acnoPSeqno);
//		sqlSelect(strSql);
//		if (sqlRowNum > 0 && colNum("max_cash_amt") > 0) {
//			if (colNum("max_cash_amt") < lma)
//				lma = colNum("max_cash_amt");
//		}

		// 若預現總額度 > 臨調後總額度, 則預現總額度 = 臨調後總額度
		// IF lm_a > ld_consume_quota THEN lm_a = ld_consume_quota
		if (totAmtMonth > 0) {
			if (lma > totAmtMonth)
				lma = totAmtMonth;
		} else if (lma > lineCreditAmt) {
			lma = lineCreditAmt;
		}
		ccasLimitCash = lma;

		// --可用預借現金額度 - 當日使用預借現金額度 - 本月累積使用預借現金
		lma = lma - totAmtPrecash ;//- lmTmpPrecash;
		if (lma > 0) {
			if (lma > canUseLimit)
				canUseCash = canUseLimit;
			else
				canUseCash = commString.numScale(lma, 0);
		} else {
			canUseCash = commString.numScale(lma, 0);
		}
	}

	public int corpCanuseLimit(String aCardno) {
		msgOK();
		initData();
		double lmAmt = 0;
		if (empty(aCardno))
			return 0;
		cardNo = nvl(aCardno);
		selectCcaCardBase();
		if (rc != 1)
			return 0;
		if (eq(acnoFlag, "1")) {
			return 0;
		}
		amtDate = getProcDate();
		if(amtDate.isEmpty()) {
			errmsg("查無系統參數: TXLOGAMT_DATE");
			return 0;
		}
		iiType = 2;
		// -已授權未請款:一般消費/預借現金-
		selectCcaAuthTxlogForCorp();
		// -已授權未請款:一般消費/預借現金- 補收檔時間差
		selectConsume(2);
		totAmtConsume = totAmtConsume + txLogAmt1 + txLogAmt2;
		totAmtPrecash = totAmtPrecash + txLogCash1 + txLogCash2;
		selectActAcno();
		if (rc != 1)
			return 0;

		// -總消費金額-
		double lmAmt1 = totAmtConsume + paidConsume;

		double lmAmt2 = totUnpaidAmt;
		// -有臨調-
		if (totAmtMonth > 0) {
			lmAmt2 += totAmtMonth;
		} else
			lmAmt2 += lineCreditAmt;

		lmAmt2 = lmAmt2 - lmAmt1 - specialAmt;		
		canUseLimit = commString.numScale(lmAmt2, 0);
		return 1;
	}

	private void selectConsume(int aType) {
		// -- 1:個人 , 2:公司
		if (aType == 1) {
			strSql = "";
			strSql += "select auth_txlog_amt_1 , auth_txlog_amt_2 , auth_txlog_amt_cash_1 , auth_txlog_amt_cash_2 ";
			strSql += "from cca_consume where card_acct_idx = ? ";
			setDouble(1, cardAcctIdx);
			sqlSelect(strSql);
			if (sqlRowNum > 0) {
				txLogAmt1 = colNum("auth_txlog_amt_1");
				txLogAmt2 = colNum("auth_txlog_amt_2");
				txLogCash1 = colNum("auth_txlog_amt_cash_1");
				txLogCash2 = colNum("auth_txlog_amt_cash_2");
			} else {
				txLogAmt1 = 0;
				txLogAmt2 = 0;
				txLogCash1 = 0;
				txLogCash2 = 0;
			}
		} else if (aType == 2) {
			strSql = "";
			strSql += "select sum(auth_txlog_amt_1) as auth_txlog_amt_1 , ";
			strSql += "sum(auth_txlog_amt_2) as auth_txlog_amt_2 , sum(auth_txlog_amt_cash_1) as auth_txlog_amt_cash_1 ,";
			strSql += "sum(auth_txlog_amt_cash_2) as auth_txlog_amt_cash_2 from cca_consume ";
			strSql += "where p_seqno in (select acno_p_seqno from act_acno where corp_p_seqno = ? and acno_flag ='3') ";
			setString(1, corpPSeqno);
			sqlSelect(strSql);
			if (sqlRowNum > 0) {
				txLogAmt1 = colNum("auth_txlog_amt_1");
				txLogAmt2 = colNum("auth_txlog_amt_2");
				txLogCash1 = colNum("auth_txlog_amt_cash_1");
				txLogCash2 = colNum("auth_txlog_amt_cash_2");
			} else {
				txLogAmt1 = 0;
				txLogAmt2 = 0;
				txLogCash1 = 0;
				txLogCash2 = 0;
			}
		}
	}
	
	private void calSpecialAmt() {
		String riskType = "" , date1 = "" , date2 = "";
		double maxMonthAmt = 0 , useAmt = 0 ;
		int txnCnt = 0;
		
		String sql1 = "select risk_type , adj_month_amt , adj_eff_start_date , adj_eff_end_date from cca_adj_parm where card_acct_idx = ? and spec_flag ='Y' ";
		String sql2 = "select sum(nt_amt) as tl_spec_amt from cca_auth_txlog where card_acct_idx = ? and tx_date >= ? and tx_date <= ? and risk_type = ? and cacu_amount <> 'N' ";
		String sql3 = "select count(*) as db_txn_cnt from cca_auth_txlog where card_acct_idx = ? and tx_date >= ? and tx_date <= ? and risk_type = ? and cacu_amount <> 'N' ";
		sqlSelect(sql1,new Object[] {cardAcctIdx});
		
		int cnt = sqlRowNum;
		
		for(int ii=0 ;ii < cnt ; ii++) {
			txnCnt = 0;
			riskType = colStr(ii,"risk_type");
			maxMonthAmt = colNum(ii,"adj_month_amt");
			date1 = colStr(ii,"adj_eff_start_date");
			date2 = colStr(ii,"adj_eff_end_date");
			sqlSelect(sql2,new Object[] {cardAcctIdx,date1,date2,riskType});
			if(sqlRowNum >0) {
				useAmt =colNum("tl_spec_amt");
			}
			sqlSelect(sql3,new Object[] {cardAcctIdx,date1,date2,riskType});
			if(sqlRowNum >0) {
				txnCnt = colInt("db_txn_cnt");
			}						
			
			//--有消費不扣專款專用總額度 , 沒消費扣專款專用總額度
			if(txnCnt <= 0) {
				specialAmt += maxMonthAmt;
			}	else	{
				if(useAmt < maxMonthAmt) {
					specialAmt += (maxMonthAmt - useAmt);
				}
			}
			
		}
		
	}
	
}
