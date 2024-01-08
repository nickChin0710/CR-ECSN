package Rsk;
/**
 * 2020-0929  V1.00.00	 JH		 凍81,或無凍結, 不列特殊易
 * 2020-0410  V1.00.01	 JH		 不合格退貨
 * 2020-0117  V1.00.02   JH      U.bil_bill
 * 2020-0108  V1.00.03   JH      modify
 * 2019-0912  V1.00.04   JH      --rsk-ctrlseqno-log
 * 2019-0718  V1.00.05   JH      p_xxx >>acno_pxxx
 * 2018-1002  V1.00.06	 JH		 debug=false
 * 2017-1226  V1.00.07	 JH		 initial
 * 109-11-23  V1.00.08   tanwei  updated for project coding standard
*  109/12/30  V1.00.09  yanghan       修改了部分无意义的變量名稱          *
*  111/09/20  V1.00.10   Alex    rsk_type 4 不處理 , FIFC
*  111/09/22  V1.00.11   Alex    rsk_type 2、3 問交理由碼異動
 * */

import com.SqlParm;
import com.BaseBatch;

@SuppressWarnings("unused")
public class RskP510 extends BaseBatch {
private String progname = "帳單篩選列風管處理程式   111/09/20 V1.00.11";
//=============================================================================
hdata.BilBill hBill=new hdata.BilBill();
hdata.CrdCard hCard=new hdata.CrdCard();
hdata.CrdIdno hIdno=new hdata.CrdIdno();
hdata.CcaCardAcct hCcat=new hdata.CcaCardAcct();
hdata.BilNccc300Dtl hNc3d=new hdata.BilNccc300Dtl();

//=*****************************************************************************
private String hPlemCtrlSeqno = "";
private String hPlemPrbReasonCode = "";
private String hTardNewEndDate = "";
private String hTardAutoloadFlag = "";
private String hCgecPurchaseDate = "";
//-----------------------------------------------------------------------------
private int iiCountBlock81 = 0;
private String isCtrlSeqno = "";
private int ilCount = 0;
private String prblReasonCode="";
private int liDcPrbAmount=0;
private int iiCountBlock=0;
private String hLogPrbl1Mark="";
private String hLogOther3Mark="";

//=====================================================================
private SqlParm tPrbl=new com.SqlParm();
com.SqlParm tLog=new com.SqlParm();
//-------------------------------------------------------------
private int tidCardS1=-1;
private int tidTscCardS=-1;
private int tiBintype;

//=*****************************************************************************
public static void main(String[] args) {
	RskP510 proc = new RskP510();
//	proc.debug = false;
	proc.mainProcess(args);
	proc.systemExit(0);
}

// ---------------------------------------------------------------------------
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

		if (args.length > 0) {
			printf("Usage : rsk_p510 ");
			errExit(1);
		}

		dbConnect();

		selectBilBill();

		sqlCommit();
		endProgram();
}

void selectBilBill() throws Exception {
	this.fetchExtend = "aa.";
	sqlCmd = "select A.*,"
			+ " uf_dc_curr(A.curr_code) as db_curr_code ," 
			+ " uf_dc_amt(A.curr_code,A.dest_amt,A.dc_dest_amt) as db_dc_amt ," 
			+ " uf_acno_key(A.p_seqno) as acct_key ," 
			+ " hex(A.rowid) as rowid "
			+ " from bil_bill A "
			+ " where 1=1 "
			+ " and A.rsk_type >='1' "
			+ " and A.rsk_type <='3' "
			+ " and rsk_post not in ('B','O') "
			;
	this.daoTable ="select_bil_bill";
	this.openCursor();

	while (fetchTable()) {
		hBill.initData();
		hNc3d.initData();
		
		totalCnt++;
		
		//--Move Data--
		hBill.referenceNo = colSs("aa.reference_no");
		hBill.referenceNoOri = colSs("aa.reference_no_original");
		hBill.pSeqno = colSs("aa.p_seqno");
		hBill.acctType = colSs("aa.acct_type");
		hBill.majorCardNo = colSs("aa.major_card_no");
		hBill.cardNo = colSs("aa.card_no");
		hBill.binType =colSs("aa.bin_type");
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
		hBill.mchtZip = colSs("aa.mcht_zip");
		hBill.mchtState = colSs("aa.mcht_state");
		hBill.mchtNo = colSs("aa.mcht_no");
		hBill.mchtChiName = colSs("aa.mcht_chi_name");
		hBill.authCode = colSs("aa.auth_code");
		hBill.billedDate = colSs("aa.billed_date");
		hBill.acctMonth = colSs("aa.acct_month");
		hBill.billedFlag = colSs("aa.billed_flag");
		hBill.cardType = colSs("aa.card_type");
		hBill.billType = colSs("aa.bill_type");
		hBill.cashAdvFee = colInt("aa.cash_adv_fee");
		hBill.processDate = colSs("aa.process_date");
		hBill.acquireDate = colSs("aa.acquire_date");
		hBill.purchaseDate = colSs("aa.purchase_date");
		hBill.postDate = colSs("aa.post_date");
		hBill.acexterDesc = colSs("aa.acexter_desc");
		hBill.batchNo = colSs("aa.batch_no");
		hBill.contractNo = colSs("aa.contract_no");
		hBill.installTotTerm = colInt("aa.install_tot_term");
		hBill.installCurrTerm = colInt("aa.install_curr_term");
		hBill.posEntryMode = colSs("aa.pos_entry_mode");
		hBill.settlAmt = colNum("aa.settl_amt");
		hBill.stmtCycle = colSs("aa.stmt_cycle");
		hBill.prodNo = colSs("aa.prod_no");
		hBill.groupCode = colSs("aa.group_code");
		hBill.promoteDept = colSs("aa.promote_dept");
		hBill.issueDate = colSs("aa.issue_date");
		hBill.collectionMode = colSs("aa.collection_mode");
		hBill.interestDate = colSs("aa.interest_date");
		hBill.feesReferenceNo = colSs("aa.fees_reference_no");
		hBill.referenceNoOri = colSs("aa.reference_no_original");
		hBill.validFlag = colSs("aa.valid_flag");
		hBill.rskType = colSs("aa.rsk_type");
		hBill.rskPost = colSs("aa.rsk_post");
		hBill.rskOrgCardno = colSs("aa.rsk_org_cardno");
		hBill.rskErrNr = colSs("aa.rsk_err_nr");
		hBill.currCode = colSs("aa.curr_code");
		hBill.dcDestAmt = colNum("aa.db_dc_amt");
		hBill.rowid = colSs("aa.rowid");
		hBill.paymentType = colSs("aa.payment_type");
		hBill.installTotTerm1 = 0;
		hBill.installFirstAmt = 0;
		hBill.installPerAmt = 0;
		hBill.installFee = 0;
		hBill.deductBp = 0;
		hBill.cashPayAmt = 0;
		hBill.vCardNo = colSs("aa.v_card_no");
		hBill.majorIdPSeqno = colSs("aa.major_id_p_seqno");
		hBill.idPSeqno = colSs("aa.id_p_seqno");
		hBill.signFlag = colSs("aa.sign_flag");
		hBill.rskType2 =colSs("aa.rsk_type2");
		
		//--
		selectBilNccc300Dtl();
		if (empty(hBill.binType)) {
			hBill.binType =getBinType(hBill.cardNo);
		}

		if (eqIgno(hBill.billType, "FIFC")) {
			updateBilBill2();
			sqlCommit();
			ddd("[%s].bill_type=FIFC, refer[%s]",totalCnt,hBill.referenceNo);
			continue;
		}

		if (strIN(hBill.paymentType, "1,2")) {
			hBill.deductBp = colInt("aa.deduct_bp");
			hBill.cashPayAmt = colNum("aa.cash_pay_amt");
		}

		if (strIN(hBill.paymentType, ",I,E")) {
			hBill.installTotTerm1 = colInt("aa.install_tot_term1");
			hBill.installFirstAmt = colInt("aa.install_first_amt");
			hBill.installPerAmt = colInt("aa.install_per_amt");
			hBill.installFee = colInt("aa.install_fee");
		}

		selectCrdCard();

		iiCountBlock =0;
		iiCountBlock81 =1;	//-只凍81-
//		凍結入帳不落問交		
//		if (eqIgno(hBill.rskType, "4")) {
//			selectCcaCardAcct();
//			if (iiCountBlock81>0 || iiCountBlock==0) {
//				//-只凍結81 OR 無凍結碼-
//				updateBilBill2();
//				ddd("[%s].block=81, refer[%s]",totalCnt,hBill.referenceNo);
//				continue;
//			}
//		}

		insertRskProblem();
		updateBilBill();
		ddd("[%s].OK, refer[%s]",totalCnt,hBill.referenceNo);

		sqlCommit();
	} // while
	//--
	this.closeCursor();
}

String getBinType(String aCardNo) throws Exception {
	if (tiBintype <=0) {
		sqlCmd ="select uf_bin_type(?) as xx_bin_type from "+commSqlStr.sqlDual;
		tiBintype =ppStmtCrt("ti_bintype","");
	}
	ppp(1,aCardNo);
	sqlSelect(tiBintype);
	if (sqlNrow >0) {
		return colSs("xx_bin_type");
	}
	return "";
}
//-****************************************************************************
void selectBilNccc300Dtl() throws Exception {
	daoTable = "bil_nccc300_dtl-S1";
	int tid =getTableId();
	if (tid <= 0) {
		sqlCmd = "select "
				+ " tmp_request_flag ,"
				+ " tmp_service_code ,"
				+ " usage_code ,"
				+ " reason_code ,"
				+ " settlement_flag ,"
				+ " electronic_term_ind ,"
				+ " pos_term_capability ,"
				+ " pos_pin_capability ,"
				+ " reimbursement_attr ,"
				+ " second_conversion_date ,"
				+ " exchange_rate ,"
				+ " exchange_date ,"
				+ " ec_ind ,"
				+ " original_no ,"
				+ " transaction_source ,"
				+ " floor_limit ,"
				+ " chip_condition_code ,"
				+ " auth_response_code ,"
				+ " transaction_type ,"
				+ " terminal_ver_results ,"
				+ " iad_result ,"
				+ " terminal_cap_pro "
				+ " from bil_nccc300_dtl "
				+ " where reference_no = ?";
		tid =ppStmtCrt();
	}
	setString(1, hBill.referenceNo);
	sqlSelect(tid);
	if (sqlNrow < 0) {
		printf("select bil_nccc300_dtl error, kk[%s]",hBill.referenceNo);
		errExit();
	}

	if (sqlNrow ==0)
		return;
	
	hNc3d.tmpRequestFlag = colSs("tmp_request_flag");
	hNc3d.tmpServiceCode = colSs("tmp_service_code");
	hNc3d.usageCode = colSs("usage_code");
	hNc3d.reasonCode = colSs("reason_code");
	hNc3d.settlementFlag = colSs("settlement_flag");
	hNc3d.electronicTermInd = colSs("electronic_term_ind");
	hNc3d.posTermCapability = colSs("pos_term_capability");
	hNc3d.posPinCapability = colInt("pos_pin_capability");
	hNc3d.reimbursementAttr = colSs("reimbursement_attr");
	hNc3d.secondConversionDate = colSs("second_conversion_date");
	hNc3d.exchangeRate = colSs("exchange_rate");
	hNc3d.exchangeDate = colSs("exchange_date");
	hNc3d.ecInd = colSs("ec_ind");
	hNc3d.originalNo = colSs("original_no");
	hNc3d.transactionSource = colSs("transaction_source");
	hNc3d.floorLimit = colSs("floor_limit");
	hNc3d.chipConditionCode = colSs("chip_condition_code");
	hNc3d.authResponseCode = colSs("auth_response_code");
	hNc3d.transactionType = colSs("transaction_type");
	hNc3d.terminalVerResults = colSs("terminal_ver_results");
	hNc3d.iadResult = colSs("iad_result");
	hNc3d.terminalCapPro = colSs("terminal_cap_pro");
}

// ****************************************************************************
void updateBilBill2() throws Exception {
	daoTable = "bil_bill-U2";
	int tid =getTableId();
	if (tid <= 0) {
		sqlCmd = " update bil_bill set "
				+ " rsk_post = 'B' ,"
				+ commSqlStr.setModXxx(hModUser, hModPgm)
				+ " where rowid = ?";
		tid =ppStmtCrt();
	}
	
	Object[] pp = new Object[] {
			commSqlStr.ss2rowid(hBill.rowid)
	};
	sqlExec(tid,pp);
	if (sqlNrow <= 0) {
		errmsg("update bil_bill2 error");
		this.errExit(0);
	}
}

// ****************************************************************************
void selectCrdCard() throws Exception {
	hCard.majorIdPSeqno ="";
	hCard.corpPSeqno ="";
	hCard.cardType ="";
	if(empty(hBill.majorCardNo)) {
		return;
	}
	
	if ( tidCardS1 <= 0) {
		daoTable = "crd_card-S";
		sqlCmd = "select major_id_p_seqno ,"
				+ " corp_p_seqno ,"
				+ " card_type "
				+ " from crd_card "
				+ " where card_no = ?";
		tidCardS1 =ppStmtCrt();
	}
	setString(1, hBill.majorCardNo);
	sqlSelect(tidCardS1);
	if (sqlNrow <0) {
		errmsg("select crd_card error, kk[%s]",hBill.majorCardNo);
		errExit(0);
	}
	if (sqlNrow==0)
		return;

	//find
	hCard.majorIdPSeqno = colSs("major_id_p_seqno");
	hCard.corpPSeqno = colSs("corp_p_seqno");
	hCard.cardType = colSs("card_type");
}

// ****************************************************************************
void selectCcaCardAcct() throws Exception {
	hCcat.blockReason1 = "";
	hCcat.blockReason2 = "";
	hCcat.blockReason3 = "";
	hCcat.blockReason4 = "";
	hCcat.blockReason5 = "";

	iiCountBlock =0;
	iiCountBlock81=1; //只凍結81
	
	daoTable = "card_acct-S";
	int tid =getTableId();
	if (tid <= 0) {
		sqlCmd = "select block_reason1 ,"
				+ " block_reason2 ,"
				+ " block_reason3 ,"
				+ " block_reason4 ,"
				+ " block_reason5 "
				+ " from cca_card_acct "
				+ " where p_seqno = ? "
				+ " and debit_flag='N' and acno_flag<>'Y' ";
		tid =ppStmtCrt();
	}
	setString(1, hBill.pSeqno);
	sqlSelect(tid);
	if (sqlNrow>0) {
		hCcat.blockReason1 = colSs("block_reason1");
		hCcat.blockReason2 = colSs("block_reason2");
		hCcat.blockReason3 = colSs("block_reason3");
		hCcat.blockReason4 = colSs("block_reason4");
		hCcat.blockReason5 = colSs("block_reason5");
	}
	//  in ('','  ','81','61','71','72','73','74','91')
	for(int ii=1; ii<=5; ii++) {
		String blockReason=colSs("block_reason"+ii);
		if (empty(blockReason)) continue;
		
		iiCountBlock++;
		if (strIN(blockReason,",81,61,71,72,73,74,91")==false) {
			iiCountBlock81 =0;
			break;
		}
	}

}

// ****************************************************************************
void insertRskProblem() throws Exception {
	//bil_bill.rsk_tpye: 1>>E不合格帳單; 2,3>>Q問交; 
	if (eqIgno(hBill.rskType, "1")) {
		hBill.rskType = "E";
		if (eq(hBill.rskType2,"R1")) {
			hPlemPrbReasonCode ="R1";
		}
		else if (eq(hBill.rskType2,"R4")) {
			hPlemPrbReasonCode ="R4";
		}
		else if (eq(hBill.rskType2,"R3")) {
			if (eq(hBill.signFlag,"-"))
				hPlemPrbReasonCode ="R2";  //-退貨-
			else hPlemPrbReasonCode ="R3";  //-請款-
		}
		else {
			hPlemPrbReasonCode = "51";
		}
	}
//	else if (eqIgno(hBill.rskType,"4")) {
//		//--此帳戶有凍結碼且不只含81 --
//		if (iiCountBlock81==0) {
//			hBill.rskType = "Q";
//			//--reason_code 還沒改
//			hPlemPrbReasonCode = "54";
//		}
//		else return;
//	}
	else if (eqIgno(hBill.rskType, "3")) {
		//--rsk_type 3 改為監控表述
		hBill.rskType = "Q";
//		hPlemPrbReasonCode = "53";
		hPlemPrbReasonCode = "27";
	}
	else if (eqIgno(hBill.rskType, "2")) {
		//--rsk_type 2 增加授權碼
		hBill.rskType = "Q";
		if(hBill.authCode.isEmpty() || hBill.authCode.length() <6) {
			hPlemPrbReasonCode = "26";
		} else {
			hPlemPrbReasonCode = "52";
			if (eqIgno(hBill.billType, "TSCC")) {
				selectTscCard();
			}
		}		
	}
	
	getCrtlSeqno();

	if (tPrbl.pfidx <=0) {
		daoTable = "rsk_problem-A1";
		tPrbl.sqlFrom = "insert into rsk_problem ("
				+ " reference_no ,"
				+ " reference_seq ,"
				+ " bin_type ,"
				+ " ctrl_seqno ,"
				+ " prb_src_code ,"
				+ " prb_mark ,"
				+ " prb_status ,"
				+ " prb_reason_code ,"
				+ " prb_amount ,"
				+ " prb_print_flag , prb_fraud_rpt,"
				+ " add_date ,"
				+ " add_user ,"
				+ " add_apr_date ,"
				+ " add_apr_user ,"
				+ " major_id_p_seqno ,"
				+ " major_card_no ,"
				+ " corp_p_seqno ,"
				+ " card_no ,"
				+ " p_seqno ,"
				+ " acct_type ,"
				+ " txn_code ,"
				+ " film_no ,"
				+ " acq_member_id ,"
				+ " source_amt ,"
				+ " source_curr ,"
				+ " settl_amt ,"
				+ " dest_amt ,"
				+ " dest_curr ,"
				+ " mcht_eng_name ,"
				+ " mcht_city ,"
				+ " mcht_country ,"
				+ " mcht_category ,"
				+ " mcht_no ,"
				+ " mcht_chi_name ,"
				+ " auth_code ,"
				+ " acct_month ,"
				+ " bill_type ,"
				+ " cash_adv_fee ,"
				+ " purchase_date ,"
				+ " acquire_date ,"
				+ " process_date ,"
				+ " post_date ,"
				+ " stmt_cycle ,"
				+ " interest_date ,"
				+ " fees_reference_no ,"
				+ " debit_flag ,"
				+ " rsk_type ,"
				+ " payment_type,"
				+ " install_tot_term1 ,"
				+ " install_first_amt ,"
				+ " install_per_amt ,"
				+ " install_fee ,"
				+ " block_reason ,"
				+ " block_reason2 ,"
				+ " block_reason3 ,"
				+ " block_reason4 ,"
				+ " block_reason5 ,"
				+ " curr_code ,"
				+ " dc_dest_amt ,"
				+ " dc_prb_amount ,"
				+ " v_card_no ,"
				+ " sign_flag ,"
				+ " id_p_seqno ,"
				+ " contract_no ,"
				+ " mod_user ,"
				+ " mod_time ,"
				+ " mod_pgm ,"
				+ " mod_seqno "
				+ " ) values ("
				+ tPrbl.pmkk(0, ":reference_no ,")
				+ "0 ,"
				+ tPrbl.pmkk(":bin_type ,")
				+ tPrbl.pmkk(":ctrl_seqno ,")
				+ tPrbl.pmkk(":prb_src_code ,")
				+ tPrbl.pmkk(":prb_mark ,")
				+ "'30' ,"
				+ tPrbl.pmkk(":prb_reason_code ,")
				+ tPrbl.pmkk(":prb_amount ,")
				+ "'N', '0',"
				+ tPrbl.pmkk(":add_date ,")
				+ tPrbl.pmkk(":add_user ,")
				+ tPrbl.pmkk(":add_apr_date ,")
				+ tPrbl.pmkk(":add_apr_user ,")
				+ tPrbl.pmkk(":major_id_p_seqno ,")
				+ tPrbl.pmkk(":major_card_no ,")
				+ tPrbl.pmkk(":corp_p_seqno ,")
				+ tPrbl.pmkk(":card_no ,")
				+ tPrbl.pmkk(":p_seqno ,")
				+ tPrbl.pmkk(":acct_type ,")
				+ tPrbl.pmkk(":txn_code ,")
				+ tPrbl.pmkk(":film_no ,")
				+ tPrbl.pmkk(":acq_member_id ,")
				+ tPrbl.pmkk(":source_amt ,")
				+ tPrbl.pmkk(":source_curr ,")
				+ tPrbl.pmkk(":settl_amt ,")
				+ tPrbl.pmkk(":dest_amt ,")
				+ tPrbl.pmkk(":dest_curr ,")
				+ tPrbl.pmkk(":mcht_eng_name ,")
				+ tPrbl.pmkk(":mcht_city ,")
				+ tPrbl.pmkk(":mcht_country ,")
				+ tPrbl.pmkk(":mcht_category ,")
				+ tPrbl.pmkk(":mcht_no ,")
				+ tPrbl.pmkk(":mcht_chi_name ,")
				+ tPrbl.pmkk(":auth_code ,")
				+ tPrbl.pmkk(":acct_month ,")
				+ tPrbl.pmkk(":bill_type ,")
				+ tPrbl.pmkk(":cash_adv_fee ,")
				+ tPrbl.pmkk(":purchase_date ,")
				+ tPrbl.pmkk(":acquire_date ,")
				+ tPrbl.pmkk(":process_date ,")
				+ tPrbl.pmkk(":post_date ,")
				+ tPrbl.pmkk(":stmt_cycle ,")
				+ tPrbl.pmkk(":interest_date ,")
				+ tPrbl.pmkk(":fees_reference_no ,")
				+ " 'N' , "
				+ tPrbl.pmkk(":rsk_type ,")
				+ tPrbl.pmkk(":payment_type ,")
				+ tPrbl.pmkk(":install_tot_term1 ,")
				+ tPrbl.pmkk(":install_first_amt ,")
				+ tPrbl.pmkk(":install_per_amt ,")
				+ tPrbl.pmkk(":install_fee ,")
				+ tPrbl.pmkk(":block_reason ,")
				+ tPrbl.pmkk(":block_reason2 ,")
				+ tPrbl.pmkk(":block_reason3 ,")
				+ tPrbl.pmkk(":block_reason4 ,")
				+ tPrbl.pmkk(":block_reason5 ,")
				+ tPrbl.pmkk(":curr_code ,")
				+ tPrbl.pmkk(":dc_dest_amt ,")
				+ tPrbl.pmkk(":dc_prb_amount ,")
				+ tPrbl.pmkk(":v_card_no ,")
				+ tPrbl.pmkk(":sign_flag ,")
				+ tPrbl.pmkk(":id_p_seqno ,")
				+ tPrbl.pmkk(":contract_no ,")
				+ tPrbl.pmkk(":mod_user ,")
				+ " sysdate , "
				+ tPrbl.pmkk(":mod_pgm ,")
				+ " '1' "
				+ " )";
		sqlCmd =tPrbl.sqlFrom;
		tPrbl.pfidx =ppStmtCrt();
	}
	tPrbl.ppp("reference_no", hBill.referenceNo);
	tPrbl.ppp("bin_type", hBill.binType);
	tPrbl.ppp("ctrl_seqno", isCtrlSeqno);
	tPrbl.ppp("prb_src_code", "S" + hBill.rskType);	//-S.系統列問交-
	tPrbl.ppp("prb_mark", hBill.rskType);
	tPrbl.ppp("prb_reason_code", hPlemPrbReasonCode);
	tPrbl.ppp("prb_amount", hBill.destAmt);
	tPrbl.ppp("add_date", hBusiDate);
	tPrbl.ppp("add_user", hModUser);
	tPrbl.ppp("add_apr_date", hBusiDate);
	tPrbl.ppp("add_apr_user", hModUser);
	tPrbl.ppp("major_id_p_seqno", hBill.majorIdPSeqno);
	tPrbl.ppp("major_card_no", hBill.majorCardNo);
	tPrbl.ppp("corp_p_seqno", hCard.corpNo);
	tPrbl.ppp("card_no", hBill.cardNo);
	tPrbl.ppp("p_seqno", hBill.pSeqno);
	tPrbl.ppp("acct_type", hBill.acctType);
	tPrbl.ppp("txn_code", hBill.txnCode);
	tPrbl.ppp("film_no", hBill.filmNo);
	tPrbl.ppp("acq_member_id", hBill.acqMemberId);
	tPrbl.ppp("source_amt", hBill.sourceAmt);
	tPrbl.ppp("source_curr", hBill.sourceCurr);
	tPrbl.ppp("settl_amt", hBill.settlAmt);
	tPrbl.ppp("dest_amt", hBill.destAmt);
	tPrbl.ppp("dest_curr", hBill.destCurr);
	tPrbl.ppp("mcht_eng_name", hBill.mchtEngName);
	tPrbl.ppp("mcht_city", hBill.mchtCity);
	tPrbl.ppp("mcht_country", hBill.mchtCountry);
	tPrbl.ppp("mcht_category", hBill.mchtCategory);
	tPrbl.ppp("mcht_no", hBill.mchtNo);
	tPrbl.ppp("mcht_chi_name", hBill.mchtChiName);
	tPrbl.ppp("auth_code", hBill.authCode);
	tPrbl.ppp("acct_month", hBill.acctMonth);
	tPrbl.ppp("bill_type", hBill.billType);
	tPrbl.ppp("cash_adv_fee", hBill.cashAdvFee);
	tPrbl.ppp("purchase_date", hBill.purchaseDate);
	tPrbl.ppp("acquire_date", hBill.acquireDate);
	tPrbl.ppp("process_date", hBill.processDate);
	tPrbl.ppp("post_date", hBill.postDate);
	tPrbl.ppp("stmt_cycle", hBill.stmtCycle);
	tPrbl.ppp("interest_date", hBill.interestDate);
	tPrbl.ppp("fees_reference_no", hBill.feesReferenceNo);
	tPrbl.ppp("rsk_type", hBill.rskType);
	tPrbl.ppp("payment_type", hBill.paymentType);
	tPrbl.ppp("install_tot_term1", hBill.installTotTerm1);
	tPrbl.ppp("install_first_amt", hBill.installFirstAmt);
	tPrbl.ppp("install_per_amt", hBill.installPerAmt);
	tPrbl.ppp("install_fee", hBill.installFee);
	tPrbl.ppp("block_reason",  hCcat.blockReason1);
	tPrbl.ppp("block_reason2", hCcat.blockReason2);
	tPrbl.ppp("block_reason3", hCcat.blockReason3);
	tPrbl.ppp("block_reason4", hCcat.blockReason4);
	tPrbl.ppp("block_reason5", hCcat.blockReason5);
	tPrbl.ppp("curr_code", hBill.currCode);
	tPrbl.ppp("dc_dest_amt", hBill.dcDestAmt);
	tPrbl.ppp("dc_prb_amount", hBill.dcDestAmt);
	tPrbl.ppp("v_card_no", hBill.vCardNo);
	tPrbl.ppp("sign_flag", hBill.signFlag);
	tPrbl.ppp("id_p_seqno", hBill.idPSeqno);
	tPrbl.ppp("contract_no", hBill.contractNo);
	tPrbl.ppp("mod_user", hModUser);
	tPrbl.ppp("mod_pgm", hModPgm);
	
	Object[] pps = tPrbl.getConvParm();
//	ddd(t_prbl.sql_from, pps);
	sqlExec(tPrbl.pfidx, pps);
	if (sqlNrow <= 0) {
		errmsg("insert rsk_acnolog error");
		errExit(0);
	}

}

//****************************************************************************
void selectTscCard() throws Exception {
	hTardNewEndDate = "";
	hTardAutoloadFlag = "";
	hCgecPurchaseDate = "";

	if (tidTscCardS <= 0) {
		sqlCmd = " select "
				+ " b.new_end_date , "
				+ " nvl(b.autoload_flag,'N') as autoload_flag , "
				+ " a.purchase_date "
				+ " from tsc_card b, tsc_cgec_all a "
				+ " where b.tsc_card_no = a.tsc_card_no "
				+ " and a.reference_no = ? ";
		daoTable = "tid_tsc_card_S";
		tidTscCardS =ppStmtCrt();
	}
	setString(1, hBill.referenceNo);
	sqlSelect(tidTscCardS);
	if (sqlNrow < 0) {
		printf("select tsc_card error !");
		exitProgram(1);
	}
	if (sqlNrow==0)
		return;

	hTardNewEndDate = colSs("new_end_date");
	hTardAutoloadFlag = colSs("autoload_flag");
	hCgecPurchaseDate = colSs("purchase_date");
	
	if (!eq(hTardAutoloadFlag,"Y")) {
		hPlemPrbReasonCode	="56";
	   return;
	}
	if (commString.ssComp(hCgecPurchaseDate, hTardNewEndDate)>0) {
		hPlemPrbReasonCode	="57";
	   return;
	}
}

//****************************************************************************
void getCrtlSeqno() throws Exception {
	daoTable = "";
	sqlCmd = "select uf_rsk_ctrlseqno() as is_ctrl_seqno from dual";
	sqlSelect();
	isCtrlSeqno = colSs("is_ctrl_seqno");
}

private int tiN300U=-1;
//==========================================================
void updateBilBill() throws Exception {
	daoTable = "bil_bill-U";
	int tid =getTableId();
	if (tid < 0) {
		sqlCmd = " update bil_bill set "
				+ " rsk_post ='B' ,"
				+ " rsk_ctrl_seqno = ? ,"
//				+ " rsk_problem1_mark =? ,"
				+commSqlStr.setModXxx(hModUser, hModPgm)
				+ " where rowid = ? ";
		tid =ppStmtCrt();
	}
	Object[] pp = new Object[] {
			isCtrlSeqno, 
//			hBill.rskType+"30",
			commSqlStr.ss2rowid(hBill.rowid)
	};
	sqlExec(tid,pp);
	if (sqlNrow <= 0) {
		errmsg("update bil_bill error; kk[%s]",hBill.referenceNo);
		errExit(0);
	}

	//-update nccc300_dtl-
   if (tiN300U < 0) {
      sqlCmd = " update bil_nccc300_dtl set "
         + " rsk_ctrl_seqno ='Y' ,"
         +" mod_time = sysdate "
         +", mod_pgm =?"
         + " where reference_no = ? ";
      tiN300U =ppStmtCrt("ti-n300_U","");
   }
   ppp(1,hModPgm);
   ppp(hBill.referenceNo);
   sqlExec(tiN300U);
   if (sqlNrow <= 0) {
      printf("update bil_nccc300_dtl error; kk[%s]",hBill.referenceNo);
      //err_exit(0);
   }
}


//-class-end-
}
