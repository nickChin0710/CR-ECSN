package Rsk;
/** VD帳單篩選列風管處理程式
 * 
 * 2021-0328  V1.01.08   JeffKung  --VD請款不寫入
 * 2021-0324  V1.01.07   JeffKung  --rsk_type 8退貨不列入
 * 2020-0812  V1.01.01   JeffKung  --rsk_type篩選變更
 * 109-08-07  V1.01.02   yanghan       修改了變量名稱和方法名稱  
 * 2019-0912  V1.01.03   JH    --rsk_ctrlseqno_log
 * 2018-1002  V1.01.04	 JH		modify
 * 2017-1226  V1.01.05   JH		initial
 * 109-11-23  V1.01.06   tanwei  updated for project coding standard
 * */

import com.CommFunction;
import com.CommRoutine;
import com.SqlParm;
import com.BaseBatch;
import com.CommSqlStr;
import com.CommString;
import com.CommSqlStr;

public class RskP520 extends BaseBatch {
	private String progname = "VD帳單篩選列風管處理程式    111/03/28 V1.01.08 ";
	CommFunction comm = new CommFunction();
	CommRoutine comr = null;
//=============================================================================
	hdata.BilBill hBill = new hdata.BilBill();
	hdata.CcaCardAcct hCcat = new hdata.CcaCardAcct();

//=============================================================================
	private String hDccdDebitAcctNo = "";
	private String hOldRskType;
	private String hPlemPrbMark;
	private String hPlemPrbReasonCode;
	private String isCtrlSeqno = "";
	private String hCardMajorIdPSeqno = "";
	private String hCardCorpPSeqno;

//=============================================================================
	private SqlParm tPblm = new com.SqlParm();
	private com.SqlParm tLog = new com.SqlParm();

	public static void main(String[] args) {
		RskP520 proc = new RskP520();
//	proc.debug =true;
		proc.mainProcess(args);
		proc.systemExit(0);
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

//		this.check_Active=true
//		if (comm.isAppActive(javaProgram))
//			exitProgram(1);

		if (args.length > 0) {
			printf("Usage : RskP520 ");
			errExit(1);
		}

		dbConnect();

		//V1.01.08  VD請款不列入
		//selectDbbBill();

		sqlCommit();
		endProgram();
	}

	void selectDbbBill() throws Exception {

		daoTable = "select_dbb_bill";
		this.fetchExtend = "AA.";
		sqlCmd = "select A.*,"
				+ " hex(A.rowid) as rowid " + " from dbb_bill A"
				+ " where 1=1 "
				+ " and A.rsk_type >='1' "
				+ " and A.rsk_type <='7' "    //V1.01.07  rsk_type 8不列入
				+ " and A.rsk_post not in ('B','O') ";
		this.openCursor();

		while (fetchTable()) {
			totalCnt++;
			hBill.initData();

			// ==========================================
			hBill.referenceNo = colSs("aa.reference_no");
			hBill.referenceNoOri = colSs("aa.reference_no_original");
			hBill.pSeqno = colSs("aa.p_seqno");
			hBill.acctType = colSs("aa.acct_type");
			hBill.binType = colSs("aa.bin_type");
			hBill.majorCardNo = colSs("aa.major_card_no");
			hBill.cardNo = colSs("aa.card_no");
			hBill.txnCode = colSs("aa.txn_code");
			hBill.filmNo = colSs("aa.film_no");
			hBill.acqMemberId = colSs("aa.acq_member_id");
			hBill.destAmt = colNum("aa.dest_amt");
			hBill.destCurr = colSs("aa.dest_curr");
			hBill.sourceAmt = colNum("aa.source_amt");
			hBill.sourceCurr = colSs("aa.source_curr");
			hBill.mchtEngName = colSs("aa.mcht_eng_name");
			hBill.mchtCity = colSs("aa.mcht_city");
			hBill.mchtCountry = colSs("aa.mcht_country");
			hBill.mchtCategory = colSs("aa.mcht_category");
			hBill.mchtNo = colSs("aa.mcht_no");
			hBill.mchtChiName = colSs("aa.mcht_chi_name");
			hBill.authCode = colSs("aa.auth_code");
			hBill.acctMonth = colSs("aa.acct_month");
			hBill.billType = colSs("aa.bill_type");
			hBill.cashAdvFee = colNum("aa.cash_adv_fee");
			hBill.processDate = colSs("aa.process_date");
			hBill.acquireDate = colSs("aa.acquire_date");
			hBill.purchaseDate = colSs("aa.purchase_date");
			hBill.postDate = colSs("aa.post_date");
			hBill.contractNo = colSs("aa.contract_no");
			hBill.installTotTerm = colInt("aa.install_tot_term");
			hBill.stmtCycle = colSs("aa.stmt_cycle");
			hBill.interestDate = colSs("aa.interest_date");
			hBill.feesReferenceNo = colSs("aa.fees_reference_no");
			hBill.referenceNoOri = colSs("aa.reference_no_original");
			hBill.rskType = colSs("aa.rsk_type");
			hBill.rowid = colSs("aa.rowid");
			hBill.currTxAmount = colNum("aa.curr_tx_amount");
			hBill.installFirstAmt = colInt("aa.install_first_amt");
			hBill.installPerAmt = colInt("aa.install_per_amt");
			hBill.installFee = colInt("aa.install_fee");
			hBill.signFlag = colSs("aa.sign_flag");
			hBill.idPSeqno = colSs("aa.id_p_seqno");
			hBill.settlAmt = colNum("aa.settl_amt");

			// *******************************
			selectDbcCard();
			hOldRskType = hBill.rskType;
			if (eqIgno(hBill.rskType, "1")) {
				hPlemPrbMark = "E";
			} else {
				hPlemPrbMark = "Q";
			}

			if (eqIgno(hBill.rskType, "1")) {
				hPlemPrbReasonCode = "1D";
				insertRskProblem();
				updateDbbBill();
			} else  {
				hPlemPrbReasonCode = "2D";
				insertRskProblem();
				updateDbbBill();
			} 

			sqlCommit();
		}
		this.closeCursor();
	}


//=****************************************************************************
	void selectDbcCard() throws Exception {
		/* Get Card Data */
		daoTable = "dbc_card-S1";
		int tid = getTableId();
		if (tid <= 0) {
			sqlCmd = "select A.major_id_p_seqno ," + " A.corp_p_seqno ," + " A.bin_type ," + " A.acct_no ,"
					+ " B.block_reason1 ," + " B.block_reason2 ," + " B.block_reason3 ," + " B.block_reason4 ,"
					+ " B.block_reason5 " + " from dbc_card A join cca_card_acct B"
					+ "       on A.p_seqno =B.p_seqno and B.debit_flag ='Y'  " + " where A.card_no =?";
			tid = this.ppStmtCrt();
		}
		ppp(1, hBill.majorCardNo);
		sqlSelect(tid);
		if (sqlNrow < 0) {
			errmsg("select_dbc_card-S1 error; kk[%s]", hBill.majorCardNo);
			return;
		}
		if (sqlNrow == 0) {
			hCardMajorIdPSeqno = "";
			hCardCorpPSeqno = "";
			hDccdDebitAcctNo = "";
			hCcat.blockReason1 = "";
			hCcat.blockReason2 = "";
			hCcat.blockReason3 = "";
			hCcat.blockReason4 = "";
			hCcat.blockReason5 = "";
			return;
		}

		hCardMajorIdPSeqno = colSs("major_id_p_seqno");
		hCardCorpPSeqno = colSs("corp_p_seqno");
		hDccdDebitAcctNo = colSs("acct_no");
		hCcat.blockReason1 = colSs("block_reason1");
		hCcat.blockReason2 = colSs("block_reason2");
		hCcat.blockReason3 = colSs("block_reason3");
		hCcat.blockReason4 = colSs("block_reason4");
		hCcat.blockReason5 = colSs("block_reason5");
	}

//=****************************************************************************
	void insertRskProblem() throws Exception {
		isCtrlSeqno = "";
		daoTable = "insert_rsk_problem-S1";
		int tid = getTableId();
		if (tid <= 0) {
			sqlCmd = "select uf_rsk_ctrlseqno() as is_ctrl_seqno from dual";
			tid = ppStmtCrt();
		}
		sqlSelect(tid);
		if (sqlNrow <= 0) {
			errmsg("無法取得 ctrl_seqno");
			errExit(0);
		}
		isCtrlSeqno = colSs("is_ctrl_seqno");

		daoTable = "rsk_problem-A";
		if (tPblm.pfidx <= 0) {
			tPblm.sqlFrom = "insert into rsk_problem (" + " reference_no ," + " reference_seq ," + " bin_type ,"
					+ " ctrl_seqno ," + " prb_src_code ," + " prb_mark ," + " prb_fraud_rpt," + " prb_status ,"
					+ " prb_reason_code ," + " prb_amount ," + " add_date ," + " add_user ," + " add_apr_date ,"
					+ " add_apr_user ," + " major_id_p_seqno ," + " major_card_no ," + " corp_p_seqno ," + " card_no ,"
					+ " p_seqno ," + " acct_type ," + " txn_code ," + " film_no ," + " acq_member_id ,"
					+ " source_amt ," + " source_curr ," + " settl_amt ," + " dest_amt ," + " dest_curr ,"
					+ " mcht_eng_name ," + " mcht_city ," + " mcht_country ," + " mcht_category ," + " mcht_no ,"
					+ " mcht_chi_name ," + " auth_code ," + " acct_month ," + " bill_type ," + " cash_adv_fee ,"
					+ " purchase_date ," + " acquire_date ," + " process_date ," + " post_date ," + " stmt_cycle ,"
					+ " interest_date ," + " fees_reference_no ," + " debit_flag ," + " rsk_type ," + " debit_acct_no, "
					+ " curr_tx_amount ," + " install_tot_term1 ," + " install_first_amt ," + " install_per_amt ,"
					+ " install_fee ," + " block_reason ," + " block_reason2 ," + " block_reason3 ,"
					+ " block_reason4 ," + " block_reason5 ," + " curr_code ," + " dc_dest_amt ," + " dc_prb_amount ,"
					+ " sign_flag ," + " id_p_seqno ," + " contract_no ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
					+ " mod_seqno " + " ) values (" + tPblm.pmkk(0, ":reference_no ,") + " 1 ,"
					+ tPblm.pmkk(":bin_type ,") + tPblm.pmkk(":ctrl_seqno ,") + tPblm.pmkk(":prb_src_code ,")
					+ tPblm.pmkk(":prb_mark ,") + " '0', '30' ," + tPblm.pmkk(":prb_reason_code ,")
					+ tPblm.pmkk(":prb_amount ,") + tPblm.pmkk(":add_date ,") + tPblm.pmkk(":add_user ,")
					+ tPblm.pmkk(":add_apr_date ,") + tPblm.pmkk(":add_apr_user ,")
					+ tPblm.pmkk(":major_id_p_seqno ,") + tPblm.pmkk(":major_card_no ,")
					+ tPblm.pmkk(":corp_p_seqno ,")// --
					+ tPblm.pmkk(":card_no ,") + tPblm.pmkk(":p_seqno ,") + tPblm.pmkk(":acct_type ,")
					+ tPblm.pmkk(":txn_code ,") + tPblm.pmkk(":film_no ,") + tPblm.pmkk(":acq_member_id ,")
					+ tPblm.pmkk(":source_amt ,") + tPblm.pmkk(":source_curr ,") + tPblm.pmkk(":settl_amt ,")
					+ tPblm.pmkk(":dest_amt ,") + tPblm.pmkk(":dest_curr ,") + tPblm.pmkk(":mcht_eng_name ,")
					+ tPblm.pmkk(":mcht_city ,") + tPblm.pmkk(":mcht_country ,") + tPblm.pmkk(":mcht_category ,")
					+ tPblm.pmkk(":mcht_no ,") + tPblm.pmkk(":mcht_chi_name ,") + tPblm.pmkk(":auth_code ,")
					+ tPblm.pmkk(":acct_month ,") + tPblm.pmkk(":bill_type ,") + tPblm.pmkk(":cash_adv_fee ,")
					+ tPblm.pmkk(":purchase_date ,") + tPblm.pmkk(":acquire_date ,") + tPblm.pmkk(":process_date ,")
					+ tPblm.pmkk(":post_date ,") + tPblm.pmkk(":stmt_cycle ,") + tPblm.pmkk(":interest_date ,")
					+ tPblm.pmkk(":fees_reference_no ,") + " 'Y' ," + tPblm.pmkk(":rsk_type ,")
					+ tPblm.pmkk(":debit_acct_no ,") + tPblm.pmkk(":curr_tx_amount ,")
					+ tPblm.pmkk(":install_tot_term1 ,") + tPblm.pmkk(":install_first_amt ,")
					+ tPblm.pmkk(":install_per_amt ,") + tPblm.pmkk(":install_fee ,") // --
					+ tPblm.pmkk(":block_reason ,") + tPblm.pmkk(":block_reason2 ,") + tPblm.pmkk(":block_reason3 ,")
					+ tPblm.pmkk(":block_reason4 ,") + tPblm.pmkk(":block_reason5 ,") + tPblm.pmkk(":curr_code ,")
					+ tPblm.pmkk(":dc_dest_amt ,") + tPblm.pmkk(":dc_prb_amount ,") + tPblm.pmkk(":sign_flag ,")
					+ tPblm.pmkk(":id_p_seqno ,") + tPblm.pmkk(":contract_no ,") + tPblm.pmkk(":mod_user ,")
					+ " sysdate ," + tPblm.pmkk(":mod_pgm ,") + " 1 " + " )";
			sqlCmd = tPblm.sqlFrom;
			tPblm.pfidx = ppStmtCrt();
		}
		tPblm.ppp("reference_no", hBill.referenceNo);
		tPblm.ppp("bin_type", hBill.binType);
		tPblm.ppp("ctrl_seqno", isCtrlSeqno);
		tPblm.ppp("prb_src_code", "S" + hPlemPrbMark);
		tPblm.ppp("prb_mark", hPlemPrbMark);
		tPblm.ppp("prb_reason_code", hPlemPrbReasonCode);
		tPblm.ppp("prb_amount", hBill.destAmt);
		tPblm.ppp("add_date", hBusiDate);
		tPblm.ppp("add_user", hModUser);
		tPblm.ppp("add_apr_date", hBusiDate);
		tPblm.ppp("add_apr_user", hModUser);
		tPblm.ppp("major_id_p_seqno", hCardMajorIdPSeqno);
		tPblm.ppp("major_card_no", hBill.majorCardNo);
		tPblm.ppp("corp_p_seqno", hCardCorpPSeqno);
		tPblm.ppp("card_no", hBill.cardNo);
		tPblm.ppp("p_seqno", hBill.pSeqno);
		tPblm.ppp("acct_type", hBill.acctType);
		tPblm.ppp("txn_code", hBill.txnCode);
		tPblm.ppp("film_no", hBill.filmNo);
		tPblm.ppp("acq_member_id", hBill.acqMemberId);
		tPblm.ppp("source_amt", hBill.sourceAmt);
		tPblm.ppp("source_curr", hBill.sourceCurr);
		tPblm.ppp("settl_amt", hBill.settlAmt);
		tPblm.ppp("dest_amt", hBill.destAmt);
		tPblm.ppp("dest_curr", hBill.destCurr);
		tPblm.ppp("mcht_eng_name", hBill.mchtEngName);
		tPblm.ppp("mcht_city", hBill.mchtCity);
		tPblm.ppp("mcht_country", hBill.mchtCountry);
		tPblm.ppp("mcht_category", hBill.mchtCategory);
		tPblm.ppp("mcht_no", hBill.mchtNo);
		tPblm.ppp("mcht_chi_name", hBill.mchtChiName);
		tPblm.ppp("auth_code", hBill.authCode);
		tPblm.ppp("acct_month", hBill.acctMonth);
		tPblm.ppp("bill_type", hBill.billType);
		tPblm.parmNum("cash_adv_fee", hBill.cashAdvFee);
		tPblm.ppp("purchase_date", hBill.purchaseDate);
		tPblm.ppp("acquire_date", hBill.acquireDate);
		tPblm.ppp("process_date", hBill.processDate);
		tPblm.ppp("post_date", hBill.postDate);
		tPblm.ppp("stmt_cycle", hBill.stmtCycle);
		tPblm.ppp("interest_date", hBill.interestDate);
		tPblm.ppp("fees_reference_no", hBill.feesReferenceNo);
		tPblm.ppp("rsk_type", hBill.rskType);
		tPblm.ppp("debit_acct_no", hDccdDebitAcctNo);
		tPblm.ppp("curr_tx_amount", hBill.currTxAmount);
		tPblm.ppp("install_tot_term1", hBill.installTotTerm);
		tPblm.ppp("install_first_amt", hBill.installFirstAmt);
		tPblm.ppp("install_per_amt", hBill.installPerAmt);
		tPblm.ppp("install_fee", hBill.installFee);
		tPblm.ppp("block_reason", hCcat.blockReason1);
		tPblm.ppp("block_reason2", hCcat.blockReason2);
		tPblm.ppp("block_reason3", hCcat.blockReason3);
		tPblm.ppp("block_reason4", hCcat.blockReason4);
		tPblm.ppp("block_reason5", hCcat.blockReason5);
		tPblm.ppp("curr_code", hBill.destCurr);
		tPblm.ppp("dc_dest_amt", hBill.destAmt);
		tPblm.ppp("dc_prb_amount", hBill.destAmt);
		tPblm.ppp("sign_flag", hBill.signFlag);
		tPblm.ppp("id_p_seqno", hBill.idPSeqno);
		tPblm.ppp("contract_no", hBill.contractNo);
		tPblm.ppp("mod_user", hModUser);
		tPblm.ppp("mod_pgm", hModPgm);

		Object[] pps = tPblm.getConvParm();
		ddd(tPblm.sqlFrom, pps);
		sqlExec(tPblm.pfidx, pps);
		if (sqlNrow <= 0) {
			errmsg("insert rsk_problem error");
			errExit(0);
		}
	}

//=****************************************************************************
	void updateDbbBill() throws Exception {
		int tid = getTableId("dbb_bill-U");
		if (tid <= 0) {
			sqlCmd = " update dbb_bill set " + " rsk_post = 'B' ," + " rsk_ctrl_seqno = ?, "
			// + " rsk_problem1_mark = ?||'30' ,"
					+ commSqlStr.setModXxx(hModUser, hModPgm) + " where rowid = ?";
			tid = ppStmtCrt();
		}
		ppp(1, isCtrlSeqno);
		setRowId(2, hBill.rowid);
		sqlExec(tid);
		if (sqlNrow <= 0) {
			printf("update dbb_bill error");
			errExit(0);
		}

	}

//EEE
}
