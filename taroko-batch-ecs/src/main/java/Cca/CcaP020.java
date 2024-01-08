/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112-03-22   V1.00.01  Alex        initial                                  *
* 112-05-18   V1.00.02  Alex        開頭 delete cca_acct_balance_cal           *
* 112-05-26   V1.00.03  JH          效能加強                                                                                           *
* 112-09-25   V1.00.04  Alex        計算方式變更-超額使用專款專用部分還額
 * 2023-1113 V1.00.05   JH    cacu_amount<>N
*****************************************************************************/
package Cca;

import com.Parm2sql;

import java.util.Calendar;

public class CcaP020 extends com.BaseBatch {
	private final String progname = "帳戶可用餘額計算 2023-1113 V1.00.05";

//--boolean truncate
	boolean ibTruncate = false;
	com.DataSet dsAcno = new com.DataSet();
	com.DataSet dsCcaAcct = new com.DataSet();
	com.DataSet dsIdCorp = new com.DataSet();
	com.DataSet dsAcno2 = new com.DataSet();
	com.DataSet dsIdCorp2 = new com.DataSet();
//-HH--
	String hhAcnoPseqno = "";
	String hhIdPseqno = "";
	String hhCorpPseqno = "";
	String hhAcctType = "";
	String hhAcnoFlag = "";
	double hhAcctAmtBal = 0;
	double hhAcctCashBal = 0;
	double hhCorpAmtBal = 0;
	double hhCorpCashBal = 0;
	double hhIdcorpAmtBal = 0;
	double hhIdcorpCashBal = 0;
	String hhCorpFlag = "";
	
	String hhReturnDate = "";
	boolean lbReturn = false ;
	
	public static void main(String[] args) {
		CcaP020 proc = new CcaP020();
		// proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 2) {
			printf("Usage : Ccap020 [business_date,truncate flag]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		} else if (liArg == 2) {
			this.setBusiDate(args[0]);
			ibTruncate = eq(args[1], "Y");
		}

		printf("=====清除 cca_acct_balance_cal開始=====");
		if (ibTruncate)
			truncateCcaAcctBalanceCal();
		else
			deleteCcaAcctBalanceCal();
		
		getReturnDate();
		
		loadAcnoIdCorp();
		loadActAcno();
		loadCcaCardAcct();
		// -acct_type+corp_p_seqno VS acno_p_seqno-
		loadActAcno2();

		printf("=====計算 acno_flag [1,3] 帳戶可用餘額開始=====");
		processAcnoFlag1();
		printf("=====計算 acno_flag [2] 帳戶可用餘額開始=====");
		processAcnoFlag2();
//   printf("=====計算 acno_flag [3] 帳戶可用餘額開始=====");
//   processAcnoFlag3();
//   printf("=====計算 統編向下有03、06 帳戶可用餘額開始=====");
//   processCorp();
//   printf("=====計算 ID統編向下有03、06 帳戶可用餘額開始=====");
//   processId();

		procDsacno();
		sqlCommit();
		// --
		procDsidcorp();
		sqlCommit();

		dsAcno.dataClear();
		dsAcno2.dataClear();
		dsCcaAcct.dataClear();
		dsIdCorp.dataClear();

		endProgram();
	}

	void getReturnDate() throws Exception {
		String sql1 = "select wf_value3 from ptr_sys_parm where wf_parm = 'SYSPARM' and wf_key ='ROLLBACK_P2' ";
		sqlSelect(sql1);
		hhReturnDate = colSs("wf_value3");
		
		if(hhReturnDate.compareTo(hBusiDate) <0 || hhReturnDate.isEmpty())
			lbReturn = false;
		else
			lbReturn = true;		
	}
	
//==================
	void hh_Init() {
		hhAcnoPseqno = "";
		hhIdPseqno = "";
		hhCorpPseqno = "";
		hhAcctType = "";
		hhAcnoFlag = "";
		hhAcctAmtBal = 0;
		hhAcctCashBal = 0;
		hhCorpAmtBal = 0;
		hhCorpCashBal = 0;
		hhIdcorpAmtBal = 0;
		hhIdcorpCashBal = 0;
		hhCorpFlag = "";
	}

	void processAcnoFlag1() throws Exception {

		// -臨調額度---
		procCcaCardAcct();
		// --已授權未請款金額
		selectCcaAuthTxlog();
//   //--取得欠款金額、溢付款, 爭議款--
		selectActAcct();
		selectActAcctSum();
		// --分期金額
		selectBilContract();
		selectBilContract2();
		// --繳款--
		selectTotPaidAmt();
//   //--爭議款
//   selectRsk_problem();
		// --檢查ID向下有無公司戶
		// checkCorpFlag();
	}

	void processAcnoFlag2() throws Exception {
//--已授權未請款金額
		selectCcaAuthTxlogCorp();
		// --取得欠款金額、溢付款, 爭議款--
		selectActAcctCorp();
		selectActAcctSumCorp();
		// --分期金額
		selectBilContractCorp();
		// --繳款
		selectTotPaidAmtCorp();
		// --爭議款
		// selectRskProblemCorp();
	}

	int convCorp2acnoPseqno(String kkPseqno) {
		if (empty(kkPseqno))
			return -1;
		if (dsAcno2.getKeyData(kkPseqno) <= 0)
			return -1;
		int rr = dsAcno2.getCurrRow();
		String lsPseqno = dsAcno2.colSs(rr, "acno_p_seqno");
		if (dsAcno.getKeyData(lsPseqno) <= 0)
			return -1;
		return dsAcno.getCurrRow();
	}

//-----
	void procDsacno() throws Exception {
		int llNrow = dsAcno.rowCount();

		int llCnt = 0;
		for (int ll = 0; ll < llNrow; ll++) {
			totalCnt++;
			llCnt++;
			dsAcno.listCurr(ll);
			hh_Init();

			hhAcnoPseqno = dsAcno.colSs("acno_p_seqno");
			hhIdPseqno = dsAcno.colSs("id_p_seqno");
			hhCorpPseqno = dsAcno.colSs("corp_p_seqno");
			hhAcctType = dsAcno.colSs("acct_type");
			hhAcnoFlag = dsAcno.colSs("acno_flag");
			hhAcctAmtBal = dsAcno.colNum("tot_amt_month");
			if (hhAcctAmtBal > 0) {
				hhAcctAmtBal += dsAcno.colNum("ccas_spec_amt");
			} else {
				// A.line_of_credit_amt , A.line_of_credit_amt_cash
				hhAcctAmtBal = dsAcno.colNum("line_of_credit_amt");
			}
			hhAcctCashBal = dsAcno.colNum("line_of_credit_amt_cash");
			hhAcctAmtBal += dsAcno.colNum("prepay_amt");
			// -總消費-
			// totConsume = authTxLogAmt + paidConsume + unpostInstFee + problemAmt ;
			// paidConsume += (acctJrnlBal - tlEndBalSpec);
			// unpostInstFee =colNum("inst_unpost");
			// problemAmt = colNum("dp_end_bal");

			double lmAuthAmt = dsAcno.colNum("auth_amt");
			double lmAuthCash = dsAcno.colNum("auth_cash");
			double lmAcctJrnlBal = dsAcno.colNum("acct_jrnl_bal");
			double lmEndBalSpec = dsAcno.colNum("end_bal_spec");
			double lmInstUnpost = dsAcno.colNum("inst_unpost_amt");
			double lmInstpost = dsAcno.colNum("inst_post_amt");
			double lmRskPrblAmt = dsAcno.colNum("rsk_prbl_amt");
			double lmTotPayAmt = dsAcno.colNum("tot_pay_amt");
			double lmTotSpecAmt = dsAcno.colNum("tot_spec_amt");
			double lmTotMaxAmt = dsAcno.colNum("max_spec_amt");
			double lmReturnUnitPrice = dsAcno.colNum("tl_unit_price");
			double lmReturnSpecAmt = 0;
			if(lbReturn == false)
				lmReturnUnitPrice = 0;
			
			if(lmInstpost > lmTotMaxAmt)
				lmReturnSpecAmt = (lmInstpost - lmTotMaxAmt);
			
			// --額度計算:canUseAmt = finalAcctAmt - totConsume - totSpecAmt;
			double lmCanUseAmt = hhAcctAmtBal + lmTotPayAmt - lmAuthAmt - (lmAcctJrnlBal - lmEndBalSpec) - lmInstUnpost
					- lmRskPrblAmt - lmTotSpecAmt + lmReturnUnitPrice + lmReturnSpecAmt;
			double lmCanUseCash = hhAcctCashBal - lmAuthCash;
			if (lmCanUseCash > lmCanUseAmt) {
				lmCanUseCash = lmCanUseAmt;
			}					
			
			hhAcctAmtBal = 0;
			hhAcctCashBal = 0;
			String lsKKpseqno = hhAcctType + "-" + hhCorpPseqno;
			// --檢查ID向下有無公司戶--
			if (eq(hhAcnoFlag, "1")) {
				if (dsIdCorp.getKeyData(hhIdPseqno) > 0) {
					hhCorpFlag = "Y";
				}
				hhAcctAmtBal = lmCanUseAmt;
				hhAcctCashBal = lmCanUseCash;
			} else if (eq(hhAcnoFlag, "2")) {
				if (dsAcno2.getKeyData(lsKKpseqno) > 0) {
					dsAcno2.colSet(dsAcno2.getCurrRow(), "can_use_amt", lmCanUseAmt);
					dsAcno2.colSet(dsAcno2.getCurrRow(), "can_use_cash", lmCanUseCash);
				}
				hhAcctAmtBal = lmCanUseAmt;
				hhAcctCashBal = lmCanUseCash;
				hhCorpAmtBal = lmCanUseAmt;
				hhCorpCashBal = lmCanUseCash;
			} else if (eq(hhAcnoFlag, "3")) {
				hhCorpFlag = "Y";
//         hh_idcorp_amt_bal =lm_canUseAmt;
//         hh_idcorp_cash_bal =lm_canUseCash;
				hhAcctAmtBal = lmCanUseAmt;
				hhAcctCashBal = lmCanUseCash;
				if (dsAcno2.getKeyData(lsKKpseqno) > 0) {

					hhCorpAmtBal = dsAcno2.colNum(dsAcno2.getCurrRow(), "can_use_amt");
					hhCorpCashBal = dsAcno2.colNum(dsAcno2.getCurrRow(), "can_use_cash");
					if (hhAcctAmtBal > hhCorpAmtBal) {
						hhAcctAmtBal = hhCorpAmtBal;
					}
					if (hhAcctCashBal > hhCorpCashBal)
						hhAcctCashBal = hhCorpCashBal;
				}
				// ---
				if (dsIdCorp.getKeyData(hhIdPseqno) > 0) {
					int kk = dsIdCorp.getCurrRow();
					double lmIdcorpAmt = dsIdCorp.colNum(kk, "id_corp_amt") + hhAcctAmtBal;
					double lmIdcorpCash = dsIdCorp.colNum(kk, "id_corp_cash") + hhAcctCashBal;
					dsIdCorp.colSet(kk, "id_corp_amt", lmIdcorpAmt);
					dsIdCorp.colSet(kk, "id_corp_cash", lmIdcorpCash);
				}
			}

			insertCcaAcctBalanceCal();
			// -TTT--
			if (dspProcRow(10000)) {
				sqlCommit(1);
			}
//      if (llCnt >=20000) {
//         break;
//      }
		}

		printf("acno1 insert row=[%s]", llCnt);
	}

//---------
	void procDsidcorp() throws Exception {
		int llNrow = dsIdCorp.rowCount();

		int llCnt = 0;
		for (int ll = 0; ll < llNrow; ll++) {
			llCnt++;
			dsIdCorp.listCurr(ll);
			hh_Init();

			hhIdPseqno = dsIdCorp.colSs(ll, "id_p_seqno");
			hhIdcorpAmtBal = dsIdCorp.colNum(ll, "id_corp_amt");
			hhIdcorpCashBal = dsIdCorp.colNum(ll, "id_corp_cash");
			updateCcaAcctBalanceCal();

		}
	}

//-----
	void procCcaCardAcct() throws Exception {
		int llCnt = 0;
		int llNrow = dsCcaAcct.rowCount();
		printf("proc cca_card_acct start: [%s]", llNrow);
		for (int kk = 0; kk < llNrow; kk++) {
			dsCcaAcct.listCurr(kk);
			String lsAcnoPseqno = dsCcaAcct.colSs("acno_p_seqno");
			if (dsAcno.getKeyData(lsAcnoPseqno) > 0) {
				llCnt++;
				int rr = dsAcno.getCurrRow();
				double lmTotAmtMonth = dsCcaAcct.colNum("tot_amt_month");
				dsAcno.colSet(rr, "tot_amt_month", lmTotAmtMonth);
				double liCcasIdx = dsCcaAcct.colNum("card_acct_idx");
				double lmTotSpecAmt = selectSpecAmt(liCcasIdx);
				double lmMaxSpecAmt = selectMaxSpecAmt(liCcasIdx);
				dsAcno.colSet(rr, "tot_spec_amt", lmTotSpecAmt);	//--已使用專款專用額度
				dsAcno.colSet(rr, "max_spec_amt", lmMaxSpecAmt);	//--最大專款專用額度
			}
		}
		printf("臨調額度 cnt=[%s,%s]", llNrow, llCnt);
	}

//---------
	void selectCcaAuthTxlog() throws Exception {
		int llCnt = 0, llOk = 0;
		sqlCmd = "select acno_p_seqno, sum(decode(cacu_cash,'Y',nt_amt,0)) as auth_cash "
				+ ", sum(decode(cacu_amount,'N',0,nt_amt)) as auth_amt "
          + " from cca_auth_txlog"
				+ " where mtch_flag not in ('Y','U') "
          + " and cacu_flag <> 'Y' " + " group by acno_p_seqno";
		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lsAcnoPseqno = colSs("acno_p_seqno");
			if (dsAcno.getKeyData(lsAcnoPseqno) <= 0)
				continue;

			llOk++;
			int rr = dsAcno.getCurrRow();
			dsAcno.colSet(rr, "auth_amt", colNum("auth_amt"));
			dsAcno.colSet(rr, "auth_cash", colNum("auth_cash"));
		}
		closeCursor();
		printf("acno1: auth_txlog.Count[%s,%s]", llCnt, llOk);
	}

	void selectCcaAuthTxlogCorp() throws Exception {
		int llCnt = 0, llOk = 0;
		sqlCmd = "SELECT acct_type||'-'||corp_p_seqno AS kk_corp_pseqno "
				+ ", sum(decode(cacu_cash, 'Y', nt_amt, 0)) AS auth_cash "
				+ ", sum(decode(cacu_amount,'N',0,nt_amt)) AS auth_amt" + " from cca_auth_txlog"
				+ " WHERE mtch_flag NOT IN ('Y', 'U')" + " AND cacu_flag <> 'Y'"
				+ " AND corp_p_seqno<>'' AND card_acct_idx >0" + " GROUP BY acct_type||'-'||corp_p_seqno";

		openCursor();
		while (fetchTable()) {
			llCnt++;
			String kkPseqno = colSs("kk_corp_pseqno");
			int rr = convCorp2acnoPseqno(kkPseqno);
			if (rr < 0)
				continue;
			llOk++;
			dsAcno.colSet(rr, "auth_amt", colNum("auth_amt"));
			dsAcno.colSet(rr, "auth_cash", colNum("auth_cash"));
		}
		closeCursor();
		printf("acno2: auth_txlog.Count[%s,%s]", llCnt, llOk);
	}

//---
	void selectActAcct() throws Exception {
		int llCnt = 0, llCntOk = 0;
		sqlCmd = "select p_seqno, sum(acct_jrnl_bal+end_bal_op) AS acct_jrnl_bal"
				+ ", sum(end_bal_op+end_bal_lk) AS pre_payamt" + " from act_acct" + " where 1=1 "
				+ " and (acct_jrnl_bal+end_bal_op+end_bal_op+end_bal_lk) >0" + " group by p_seqno";
		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lsPseqno = colSs("p_seqno");
			if (dsAcno.getKeyData(lsPseqno) <= 0)
				continue;

			llCntOk++;
			int rr = dsAcno.getCurrRow();
			dsAcno.colSet(rr, "acct_jrnl_bal", colNum("acct_jrnl_bal"));
			dsAcno.colSet(rr, "prepay_amt", colNum("pre_payamt"));
		}
		closeCursor();
		printf("acno1: act_acct.Count[%s,%s]", llCnt, llCntOk);
	}

//---------
	void selectActAcctCorp() throws Exception {
		int llCnt = 0, llCntOk = 0;
		sqlCmd = "SELECT B.acct_type||'-'||B.corp_p_seqno kk_corp_pseqno"
				+ ", sum(A.acct_jrnl_bal + A.end_bal_op) AS acct_jrnl_bal "
				+ ", sum(A.end_bal_op + A.end_bal_lk) AS pre_pay_amt" + " FROM act_acct A JOIN act_acno B"
				+ "   ON A.p_seqno=B.p_seqno AND B.corp_p_seqno<>'' " + " WHERE 1=1"
				+ " AND (A.acct_jrnl_bal+A.end_bal_op+A.end_bal_op+A.end_bal_lk) >0"
				+ " GROUP BY B.acct_type||'-'||B.corp_p_seqno";
		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lsPseqno = colSs("kk_corp_pseqno");
			int rr = convCorp2acnoPseqno(lsPseqno);
			if (rr < 0)
				continue;

			llCntOk++;
			dsAcno.colSet(rr, "acct_jrnl_bal", colNum("acct_jrnl_bal"));
			dsAcno.colSet(rr, "prepay_amt", colNum("pre_payamt"));
		}
		closeCursor();
		printf("acno2: act_acct.Count[%s,%s]", llCnt, llCntOk);
	}

//----------------
	void selectActAcctSum() throws Exception {
		int llCnt = 0, llCntOk = 0;
		sqlCmd = "select p_seqno" + ", sum(end_bal_spec) AS end_bal_spec"
				+ ", sum(decode(acct_code,'DP',(unbill_end_bal+billed_end_bal),0)) as rsk_prbl_amt"
				+ " from act_acct_sum " + " where 1=1 " + " and ( acct_code ='DP' OR end_bal_spec >0 )"
				+ " group by p_seqno";

		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lsPseqno = colSs("p_seqno");
			if (dsAcno.getKeyData(lsPseqno) <= 0)
				continue;

			llCntOk++;
			int rr = dsAcno.getCurrRow();
			dsAcno.colSet(rr, "end_bal_spec", colNum("end_bal_spec"));
			dsAcno.colSet(rr, "rsk_prbl_amt", colNum("rsk_prbl_amt"));
		}
		closeCursor();
		printf("acno1: act_acct_sum.Count[%s,%s]", llCnt, llCntOk);
	}

//----------------
	void selectActAcctSumCorp() throws Exception {
		int llCnt = 0, llCntOk = 0;
		sqlCmd = "select B.acct_type||'-'||B.corp_p_seqno AS kk_corp_pseqno" + ", sum(A.end_bal_spec) AS end_bal_spec "
				+ ", sum(decode(A.acct_code,'DP',(A.unbill_end_bal+A.billed_end_bal),0)) as rsk_prbl_amt "
				+ " from act_acct_sum A JOIN act_acno B" + "   ON A.p_seqno=B.p_seqno AND B.corp_p_seqno<>'' "
				+ " where 1=1" + " and ( A.acct_code ='DP' OR A.end_bal_spec >0 )"
				+ " group by B.acct_type||'-'||B.corp_p_seqno";
		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lscorpPseqno = colSs("kk_corp_pseqno");
			int rr = convCorp2acnoPseqno(lscorpPseqno);
			if (rr < 0)
				continue;

			llCntOk++;
			dsAcno.colSet(rr, "end_bal_spec", colNum("end_bal_spec"));
			dsAcno.colSet(rr, "rsk_prbl_amt", colNum("rsk_prbl_amt"));
		}
		closeCursor();
		printf("acno2: act_acct_sum.Count[%s,%s]", llCnt, llCntOk);
	}

//------
	void selectBilContract() throws Exception {
		int llCnt = 0, llCntOk = 0;
		sqlCmd = "select acno_p_seqno " + ", sum((install_tot_term - install_curr_term) * unit_price"
				+ "+remd_amt +decode(install_curr_term,0,first_remd_amt+extra_fees,0)) as inst_unpost_amt"
				+ " from bil_contract" 
				+ " where 1=1 " 
				+ " and post_cycle_dd > 0 "
				+ " and install_tot_term <> install_curr_term" 
				+ " and spec_flag <> 'Y'" 
				+ " group by acno_p_seqno";

		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lsPseqno = colSs("acno_p_seqno");
			if (dsAcno.getKeyData(lsPseqno) <= 0)
				continue;

			llCntOk++;
			int rr = dsAcno.getCurrRow();
			dsAcno.colSet(rr, "inst_unpost_amt", colNum("inst_unpost_amt"));
		}
		closeCursor();
		printf("acno1: bil_contract.Count[%s,%s]", llCnt, llCntOk);
	}

//-------------
	void selectBilContract2() throws Exception {
		int llCnt = 0, llCntOk = 0;
		sqlCmd = " select acno_p_seqno " 
				+ ", sum(install_curr_term * unit_price) as inst_post_amt , sum(unit_price) as tl_unit_price "				
				+ " from bil_contract" 
				+ " where 1=1 " 
				+ " and install_tot_term <> install_curr_term" 
				+ " and post_cycle_dd >0"
				+ " and spec_flag = 'Y'" 
				+ " group by acno_p_seqno";

		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lsPseqno = colSs("acno_p_seqno");
			if (dsAcno.getKeyData(lsPseqno) <= 0)
				continue;

			llCntOk++;
			int rr = dsAcno.getCurrRow();
			dsAcno.colSet(rr, "inst_post_amt", colNum("inst_post_amt"));
			dsAcno.colSet(rr, "tl_unit_price", colNum("tl_unit_price"));
		}
		closeCursor();
		printf("acno2: bil_contract.Count[%s,%s]", llCnt, llCntOk);
	}

//-------------
	void selectBilContractCorp() throws Exception {
		int llCnt = 0, llCntOk = 0;
		sqlCmd = "select B.acct_type||'-'||B.corp_p_seqno kk_corp_pseqno "
				+ ", sum((A.install_tot_term - A.install_curr_term) * A.unit_price "
				+ " +A.remd_amt +decode(A.install_curr_term,0,A.first_remd_amt+A.extra_fees,0)) as inst_unpost_amt"
				+ " from bil_contract A JOIN act_acno B ON A.p_seqno=B.p_seqno AND B.corp_p_seqno<>'' " + " where 1=1 "
				+ " and A.auth_code not in ('','N','REJECT','P','reject','LOAN')"
				+ " and A.install_tot_term <> A.install_curr_term" + " and ( "
				+ " (A.post_cycle_dd >0 or A.installment_kind ='F')"
				+ "   or (A.post_cycle_dd=0 AND A.DELV_CONFIRM_FLAG='Y' AND A.auth_code='DEBT')"
				+ " )  and A.spec_flag <> 'Y'" + " group by B.acct_type||'-'||B.corp_p_seqno";

		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lsPseqno = colSs("kk_corp_pseqno");
			int rr = convCorp2acnoPseqno(lsPseqno);
			if (rr < 0)
				continue;

			llCntOk++;
			dsAcno.colSet(rr, "inst_unpost_amt", colNum("inst_unpost_amt"));
		}
		closeCursor();
		printf("acno2: bil_contract.Count[%s,%s]", llCnt, llCntOk);
	}

//---------
	void selectTotPaidAmt() throws Exception {
		int llCnt = 0, llCntOk = 0;
		sqlCmd = "SELECT p_seqno, sum(pay_amt) AS pay_amt FROM (" + " select A.p_seqno, A.pay_amt as pay_amt "
				+ " from act_pay_detail A join act_pay_batch B on A.batch_no=B.batch_no" + " where B.batch_tot_cnt >0"
				+ " UNION " + " select p_seqno, pay_amt as pay_amt" + " from act_debt_cancel "
				+ " where process_flag <>'Y' " + " UNION " + " select p_seqno, txn_amt as pay_amt"
				+ " from act_pay_ibm " + " where 1=1"
				+ " and nvl(proc_mark,'') <>'Y' and nvl(error_code,'') in ('','0','N') "
				+ " and txn_source not in ('0101', '0102', '0103', '0502')" + " ) group by p_seqno";

		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lsPseqno = colSs("p_seqno");
			if (dsAcno.getKeyData(lsPseqno) <= 0)
				continue;

			llCntOk++;
			int rr = dsAcno.getCurrRow();
			dsAcno.colSet(rr, "tot_pay_amt", colNum("pay_amt"));
		}
		closeCursor();
		printf("acno1: act_pay_detail.Count[%s,%s]", llCnt, llCntOk);

	}

//---------
	void selectTotPaidAmtCorp() throws Exception {
		int llCnt = 0, llCntOk = 0;
		sqlCmd = "SELECT B.acct_type||'-'||B.corp_p_seqno AS kk_corp_pseqno" + " , sum(K.pay_amt) AS pay_amt FROM ("
				+ " select A.p_seqno, A.pay_amt as pay_amt"
				+ " from act_pay_detail A join act_pay_batch B on A.batch_no=B.batch_no" + " where B.batch_tot_cnt >0"
				+ " UNION " + " select p_seqno, pay_amt as pay_amt" + " from act_debt_cancel"
				+ " where process_flag <>'Y'" + " UNION " + " select p_seqno, txn_amt as pay_amt" + " from act_pay_ibm"
				+ " where 1=1" + " and nvl(proc_mark,'') <>'Y' and nvl(error_code,'') in ('','0','N')"
				+ " and txn_source not in ('0101', '0102', '0103', '0502')"
				+ " ) K JOIN act_acno B ON K.p_seqno=B.p_seqno AND B.corp_p_seqno<>''" + " WHERE 1=1"
				+ " group by B.acct_type||'-'||B.corp_p_seqno";

		openCursor();
		while (fetchTable()) {
			llCnt++;
			String lsPseqno = colSs("kk_corp_pseqno");
			int rr = convCorp2acnoPseqno(lsPseqno);
			if (rr < 0)
				continue;

			llCntOk++;
			dsAcno.colSet(rr, "tot_pay_amt", colNum("pay_amt"));
		}
		closeCursor();
		printf("acno2: act_pay_detail.Count[%s,%s]", llCnt, llCntOk);
	}

//--------
	int tiSpec = -1;
	double selectSpecAmt(double ai_cardAcctIdx) throws Exception {
		if (ai_cardAcctIdx <= 0)
			return 0.0;
		if (tiSpec <= 0) {
			sqlCmd = "select sum(A.nt_amt) as tot_spec_amt"
					+ " from cca_auth_txlog A JOIN cca_adj_parm B ON A.card_acct_idx=B.card_acct_idx"
					+ " where B.card_acct_idx =?" + " and B.spec_flag ='Y'" + " and A.tx_date >= B.adj_eff_start_date"
					+ " and A.tx_date <= B.adj_eff_end_date" + " and A.risk_type =B.risk_type "
					+ " and A.cacu_amount <>'N' ";  //in ('Y','M')";
			tiSpec = ppStmtCrt("ti-spec-S", "");
		}

		ppp(1, ai_cardAcctIdx);
		sqlSelect(tiSpec);
		if (sqlNrow <= 0)
			return 0;

		return colNum("tot_spec_amt");
	}
	
//------
	int tiAdjParm = -1;
	double selectMaxSpecAmt(double ai_cardAcctIdx) throws Exception {
		if (ai_cardAcctIdx <= 0)
			return 0.0;
		if (tiAdjParm <= 0) {
			sqlCmd = "select sum(adj_month_amt) as max_spec_amt "
					+ " from cca_adj_parm "
					+ " where card_acct_idx =? and spec_flag ='Y' "
					;
			tiAdjParm = ppStmtCrt("ti-adjParm-S", "");
		}

		ppp(1, ai_cardAcctIdx);
		sqlSelect(tiAdjParm);
		if (sqlNrow <= 0)
			return 0;

		return colNum("max_spec_amt");
	}
	
	
//------
	void loadActAcno() throws Exception {
		sqlCmd = " select A.acno_p_seqno , A.id_p_seqno , A.corp_p_seqno" + ", A.acct_type , A.acno_flag"
				+ ", A.line_of_credit_amt , A.line_of_credit_amt_cash " + " from act_acno A"
				+ " where A.acno_flag in ('1','2','3')" + " order by A.acno_flag, A.acno_p_seqno";

		long llTime0 = Calendar.getInstance().getTime().getTime();
		sqlQuery(dsAcno, "", null);
		dsAcno.loadKeyData("acno_p_seqno");
		long llTime1 = Calendar.getInstance().getTime().getTime();
		long llTime = llTime1 - llTime0;
		printf(" load act_acno, row[%s], kk[%s], time[%s]", dsAcno.rowCount(), dsAcno.keyCount(), llTime);
	}

//--------
	void loadActAcno2() throws Exception {
		sqlCmd = " select acct_type||'-'||corp_p_seqno as kk_corp_pseqno" + ", acno_p_seqno" + " from act_acno"
				+ " where acno_flag ='2'" + " order by 1";

		long llTime0 = Calendar.getInstance().getTime().getTime();
		sqlQuery(dsAcno2, "", null);
		dsAcno2.loadKeyData("kk_corp_pseqno");
		long llTime1 = Calendar.getInstance().getTime().getTime();
		long llTime = llTime1 - llTime0;
		printf(" load act_acno2, row[%s], kk[%s], time[%s]", dsAcno2.rowCount(), dsAcno2.keyCount(), llTime);
	}

//-----
	void loadCcaCardAcct() throws Exception {
		sqlCmd = " select card_acct_idx, acno_p_seqno, p_seqno, tot_amt_month" + " from cca_card_acct" + " where 1=1 "
				+ " and ? between adj_eff_start_date and adj_eff_end_date"
//   +" and adj_eff_start_date <=?" //>= ?"  //>=hBusiDate
//   +" and adj_eff_end_date >= ?"
		;

		long llTime0 = Calendar.getInstance().getTime().getTime();
//   sqlQuery(dsCcaAcct, "", new Object[]{"20990101","20000101"});
		sqlQuery(dsCcaAcct, "", new Object[] { hBusiDate });
		dsCcaAcct.loadKeyData("card_acct_idx");
		long llTime1 = Calendar.getInstance().getTime().getTime();
		long llTime = llTime1 - llTime0;
		printf(" load cca_card_acct, row[%s], kk[%s], time[%s]", dsCcaAcct.rowCount(), dsCcaAcct.keyCount(), llTime);
	}

//-------
	void loadAcnoIdCorp() throws Exception {
		sqlCmd = "select id_p_seqno, count(*) as db_cnt " + " from act_acno " + " where 1=1"
				+ " AND id_p_seqno <>'' and corp_p_seqno <>''" + " GROUP BY id_p_seqno";

		long llTime0 = Calendar.getInstance().getTime().getTime();
		sqlQuery(dsIdCorp, "", null);
		dsIdCorp.loadKeyData("id_p_seqno");
		long llTime1 = Calendar.getInstance().getTime().getTime();
		long llTime = llTime1 - llTime0;
		printf(" load acno_id_corp, row[%s], kk[%s], time[%s]", dsIdCorp.rowCount(), dsIdCorp.keyCount(), llTime);
	}

//==================
	com.Parm2sql ttAacctBal = null;

	void insertCcaAcctBalanceCal() throws Exception {
		if (ttAacctBal == null) {
			ttAacctBal = new com.Parm2sql();
			ttAacctBal.insert("cca_acct_balance_cal");
		}

		ttAacctBal.aaa("cal_date", hBusiDate); // VARCHAR (8,0) 計算日期
		ttAacctBal.aaa("acno_p_seqno", hhAcnoPseqno); // VARCHAR (10,0) 帳戶流水號
		ttAacctBal.aaa("id_p_seqno", hhIdPseqno); // VARCHAR (10,0) 卡人流水號
		ttAacctBal.aaa("corp_p_seqno", hhCorpPseqno); // VARCHAR (10,0) 公司流水號
		ttAacctBal.aaa("acct_type", hhAcctType); // VARCHAR (2,0) 帳戶類別
		ttAacctBal.aaa("acno_flag", hhAcnoFlag); // VARCHAR (1,0) 商務卡總個繳詳細
		ttAacctBal.aaa("acct_amt_balance", hhAcctAmtBal); // DECIMAL (10,0) 帳戶可用餘額
		ttAacctBal.aaa("acct_cash_balance", hhAcctCashBal); // DECIMAL (10,0) 帳戶預借現金可用餘額
		ttAacctBal.aaa("corp_amt_balance", hhCorpAmtBal); // DECIMAL (10,0) 帳戶可用餘額 BY CORP
		ttAacctBal.aaa("corp_cash_balance", hhCorpCashBal); // DECIMAL (10,0) 帳戶預借現金可用餘額 BY CORP
		ttAacctBal.aaa("id_corp_amt_balance", hhIdcorpAmtBal); // DECIMAL (10,0) ID下公司戶的帳戶可用餘額
		ttAacctBal.aaa("id_corp_cash_balance", hhIdcorpCashBal); // DECIMAL (10,0) ID下帳戶預借現金可用餘額
		ttAacctBal.aaa("corp_flag", hhCorpFlag); // VARCHAR (1,0) ID下有公司帳戶
		ttAacctBal.aaa("crt_date", sysDate);
		ttAacctBal.aaa("crt_time", sysTime);
		ttAacctBal.aaaModxxx(hModUser, hModPgm);

		if (ttAacctBal.ti <= 0) {
			ttAacctBal.ti = ppStmtCrt("tt-A-acctbal", ttAacctBal.getSql());
		}

		sqlExec(ttAacctBal.ti, ttAacctBal.getParms());
		if (sqlNrow <= 0) {
			sqlerr("insert cca_acct_balance_cal error");
			errExit(1);
		}
	}

//---------
	com.Parm2sql ttUacctBal = null;

	void updateCcaAcctBalanceCal() throws Exception {
		if (ttUacctBal == null) {
			ttUacctBal = new Parm2sql();
			ttUacctBal.update("cca_acct_balance_cal");
		}
		ttUacctBal.aaa("id_corp_amt_balance", hhIdcorpAmtBal);
		ttUacctBal.aaa("id_corp_cash_balance", hhIdcorpCashBal);
		ttUacctBal.aaaWhere(" where id_p_seqno =?", hhIdPseqno);
		ttUacctBal.aaaWhere(" and corp_p_seqno <>''", "");

		if (ttUacctBal.ti <= 0) {
			ttUacctBal.ti = ppStmtCrt("tt-U-acctbal", ttUacctBal.getSql());
		}

		sqlExec(ttUacctBal.ti, ttUacctBal.getParms());
		if (sqlNrow < 0) {
			sqlerr("update cca_acct_balance_cal error, kk[%s]", hhIdPseqno);
		}
	}

//--------
	void truncateCcaAcctBalanceCal() throws Exception {
		commitDataBase();
		String sql1 = "truncate cca_acct_balance_cal immediate";
		executeSqlCommand(sql1);
		commitDataBase();
		printf("truncate cca_acct_balance_cal complete");
	}

//--
	void deleteCcaAcctBalanceCal() throws Exception {
		int deleteCnt = 0;
		String sql1 = "select count(*) as db_cnt from cca_acct_balance_cal where 1=1 ";
		while (true) {
			sqlSelect(sql1);
			if (colNum("db_cnt") > 0) {
				daoTable = "cca_acct_balance_cal";
				whereStr = "where 1=1 fetch first 5000 rows only ";
				deleteTable();
				sqlCommit(1);
				deleteCnt += 5000;
				if (deleteCnt % 100000 == 0)
					showLogMessage("I", "", "已清除 [" + deleteCnt + "] 筆");
				continue;
			} else
				break;
		}
		showLogMessage("I", "", "清除 cca_acct_balance_cal 結束");
	}

}
