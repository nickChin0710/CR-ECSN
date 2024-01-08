/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  106/06/01  V1.00.00    Edson       program initial                        *
 *  109/11/23  V1.00.01   shiyuqi      updated for project coding standard    *
 *  109/11/30  V1.00.02    JeffKung    updated for TCB   rsk_type2,qr_flag    *
 *  111/06/07  V1.00.03    JeffKung    授權比對邏輯                                                        *
 *  111/09/22  V1.00.04    Justin      弱點修正                                                                *
 *  111/11/22  V1.00.05    JeffKung    bil_bill新增欄位settle_flag,platform_kind,ecs_cust_mcht_no 
 *                                     act_debt&bil_contract回寫授權記錄的spec_flag  
 *  112/03/02  V1.00.06    JeffKung    票證卡的授權比對                                     
 *  112/11/03  V1.00.07    JeffKung    列問交沒入帳不比對授權
 *****************************************************************************/

package Bil;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

//import bank.authbatch.main.AuthBatch060;
//import bank.authbatch.vo.Data060Vo;

/*當日入帳檔轉入帳單檔作業*/
public class BilA009 extends AccessDAO {
  private String progname = "當日入帳檔轉入帳單檔作業  112/11/03 V1.00.07 ";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;
  boolean debug = false;

  String hCallErrorDesc = "";
  int tmpInt = 0;
  String hTempUser = "";

  String prgmId = "BilA009";
  String prgmName = "當日入帳檔轉入帳單檔作業";
  int recordCnt = 0;
  int errCnt = 0;
  String errMsg = "";
  long hModSeqno = 0;
  String hModUser = "";
  String hModTime = "";
  String hModPgm = "";
  String iFileName = "";
  String iPostDate = "";
  String hCurpModPgm = "";
  String hCurpModTime = "";
  long hCurpModSeqno = 0;

  String hBusinssChiDate = "";
  String hVouchChiDate = "";
  String hSystemDate = "";
  String hSystemTime = "";
  String hSystemDateFull = "";
  String hApbtBatchNo = "";
  String hBusiBusinessDate = "";
  String hCurpReferenceNo = "";
  String hCurpSignFlag = "";
  String hCurpTerm = "";
  String hCurpTotalTerm = "";
  String hCurpAmtMccr = "";
  String hCurpAmtIccr = "";
  String hCurpPSeqno = "";
  String hCurpAcctType = "";
  String hCurpAcctKey = "";
  String hCurpItemOrderNormal = "";
  String hCurpItemOrderBackDate = "";
  String hCurpItemOrderRefund = "";
  String hCurpItemClassNormal = "";
  String hCurpItemClassBackDate = "";
  String hCurpItemClassRefund = "";
  String hBillAcctMonth = "";
  String hCurpStmtCycle = "";
  String hCurpBillType = "";
  String hCurpTransactionCode = "";
  double hCurpCashPayAmt = 0;
  String hCurpCardNo = "";
  String hCurpAcctCode = "";
  String hCurpAcctChiShortName = "";
  String hBillInterestDate = "";
  String hCurpPurchaseDate = "";
  String hCurpAcquireDate = "";
  String hCurpFilmNo = "";
  String hCurpMerchantNo = "";
  String hCardRegBankNo = "";
  String hCurpInstallmentKind = "";
  String hCurpPtrMerchantNo = "";
  String hCurpNewItFlag = "";
  String hCurpCurrCode = "";
  double hCurpDcAmount = 0;
  String hCurpContractNo = "";
  String hCurpContractSeqNo = "";
  int hApbtBatchTotCnt = 0;
  double hApbtBatchTotAmt = 0;
  String hCurpModUser = "";
  String hTempCurrCode = "";
  String hApdlSerialNo = "";
  String hCardIdPSeqno = "";
  String hCardMajorIdPSeqno = "";
  String hCardNewBegDate = "";
  String hCardNewEndDate = "";
  String hCardId = "";
  String hBillPostDate = "";
  String hCurpMajorCardNo = "";
  String hCurpAcquirerMemberId = "";
  double hCurpDestinationAmt = 0;
  String hCurpDestinationCurrency = "";
  double hCurpSourceAmt = 0;
  String hCurpSourceCurrency = "";
  String hCurpMerchantEngName = "";
  String hCurpMerchantCity = "";
  String hCurpMerchantCountry = "";
  String hCurpMerchantCategory = "";
  String hCurpMerchantZip = "";
  String hCurpMerchantState = "";
  String hCurpMerchantChiName = "";
  String hCurpAuthorization = "";
  String hCurpProcessDate = "";
  String hCurpBatchNo = "";
  double hCurpContractAmt = 0;
  String hCurpInterestMode = "";
  String hCurpAdvWkday = "";
  String hCurpDoubtType = "";
  String hCurpDuplicatedFlag = "";
  String hCurpGroupCode = "";
  String hCurpId = "";
  String hCurpTxConvtFlag = "";
  String hCurpAppTranCount = "";
  String hCurpInterestRskDate = "";
  int hBillInstallTotTerm = 0;
  int hBillInstallCurrTerm = 0;
  String hCurpTmpRequestFlag = "";
  String hCurpTmpServiceCode = "";
  String hCurpUsageCode = "";
  String hCurpReasonCode = "";
  String hCurpSettlementFlag = "";
  String hCurpElectronicTermInd = "";
  String hCurpPosTermCapability = "";
  String hCurpPosPinCapability = "";
  String hCurpPosEntryMode = "";
  String hCurpReimbursementAttr = "";
  double hCurpSettlementAmt = 0;
  String hCurpSecondConversionDate = "";
  String hCurpProdNo = "";
  String hCurpPromoteDept = "";
  String hCardGroupCode = "";
  String hCardIssueDate = "";
  String hCardCardType = "";
  String hCurpIssueDate = "";
  String hCurpCollectionMode = "";
  String hCurpExchangeRate = "";
  String hCurpExchangeDate = "";
  String hCurpOriginalNo = "";
  String hCurpTransactionSource = "";
  String hCurpFeesReferenceNo = "";
  String hCurpReferenceNoOriginal = "";
  String hCurpReferenceNoFeeF = "";
  String hCurpChar299 = "";
  String hCurpBinType = "";
  String hCurpCardSw = "";
  String hCurpValidFlag = "";
  String hCurpRskType = "";
  String hCurpRskRsn = "";
  String hCurpRskType2 = "";
  String hCurpAcctItem = "";
  String hCurpAcctEngShortName = "";
  String hCurpAcexterDesc = "";
  String hCurpCashAdvState = "";
  String hCurpSourceCode = "";
  String hCurpQueryType = "";
  String hCurpFloorLimit = "";
  String hCurpLimitEndDate = "";
  String hCurpChipConditionCode = "";
  String hCurpAuthResponseCode = "";
  String hCurpTransactionType = "";
  String hCurpTerminalVerResults = "";
  String hCurpIadResult = "";
  String hCurpCardSeqNum = "";
  String hCurpUnpredicNum = "";
  String hCurpAppIntPro = "";
  String hCurpCryptogram = "";
  String hCurpDerKeyIndex = "";
  String hCurpCryVerNum = "";
  String hCurpDataAuthCode = "";
  String hCurpCryInfoData = "";
  String hCurpTerminalCapPro = "";
  String hCurpLifeCycSupInd = "";
  String hCurpBanknetDate = "";
  String hCurpInterRateDes = "";
  String hCurpExpirDate = "";
  String hCurpTransactionAmtChar = "";
  String hCurpDac = "";
  String hCurpServiceCode = "";
  String hCurpInstallmentSource = "";
  String hCurpPaymentType = "";
  double hCurpCurrTxAmount = 0;
  String hCurpInstallTotTerm = "";
  double hCurpInstallFirstAmt = 0;
  double hCurpInstallPerAmt = 0;
  String hCurpInstallFee = "";
  String hCurpDeductBp = "";
  String hCurpIssueFee = "";
  double hCurpIncludeFeeAmt = 0;
  String hCurpUcaf = "";
  String hCurpEcInd = "";
  String hCurpIssueSR = "";
  String hCurpMergeFlag = "";
  String hCurpAcceFee = "";
  String hCurpAcceFeeInBc = "";
  String hCurpAddAcctType = "";
  double hCurpAddAmtType = 0;
  String hCurpAddCurcyCode = "";
  String hCurpAddAmtSign = "";
  double hCurpAddAmt = 0;
  String hCurpTerminalId = "";
  String hCurpBnetRefNum = "";
  String hCurpDe22 = "";
  String hCurpDcExchangeRate = "";
  String hCurpMcsNum = "";
  String hCurpMcsCnt = "";
  String hCurpTermType = "";
  String hCurpWalletIden = "";
  String hcurpaccepttermind = "";
  String hCurpMerchantZipTw = "";
  String hCurpMerchantType = "";
  String hCurpVCardNo = "";
  String hCurpSettlFlag = "";
  String hCurpEcsPlatformKind = ""; 
  String hCurpEcsCusMchtNo = "";
  String hCurpQrFlag = "";
  String hCurpModWs = "";
  String hTxlogSpecFlag = "";
  String hVouchCdKind = "";
  String hTAcNo = "";
  int hTSeqno = 0;
  String hTDbcr = "";
  String hTMemo3Kind = "";
  String hTMemo3Flag = "";
  String hTDrFlag = "";
  String hTCrFlag = "";
  String hRBillType = "";
  String hRMerchantNo = "";
  String hRMerchantName = "";
  String hRInstallmentKind = "";
  double hRTotRecord = 0;
  double hRTotAmount = 0;
  double hRTotPost = 0;
  double hRTotExchange = 0;
  double hRTotRedeemAmt = 0;
  double hRTotAmtP = 0;
  double hRTotAmtM = 0;
  String inputCurr = "";
  double hFeesFixAmt = 0;
  double hFeesPercent = 0;
  int hJrnlEnqSeqno = 0;
  String hCardCorpPSeqno = "";
  String hCardCorpNo = "";
  String hCardCorpNoCode = "";
  String hCardOriCardNo = "";
  double hAcurAcctJrnlBal = 0;
  double hAcurDcAcctJrnlBal = 0;
  String hPrintName = "";
  String hRptName = "";
  String[] hMTempAcNo = new String[250];
  String hCardAcctPSeqno = "";
  String hCardAcnoPSeqno = "";
  String hCurpRowid = "";
  double hAcctAcctJrnlBal = 0;
  String hAcctRowid = "";
  String hAcurRowid = "";
  long overRunPid = 0;
  String tempType = "";
  String hCurrCodeGl = "";
  double amtP = 0;
  double amtM = 0;
  double amtPF = 0;
  double amtMF = 0;
  String idxType = "";
  String hGcalBillType = "";
  String hGcalCurrCode = "";
  String hGcalCurrCodeGl = "";
  double hGcalPayAmtP = 0;
  double hGcalPayAmtM = 0;
  double hGcalPayAmtPF = 0;
  double hGcalPayAmtMF = 0;
  String tAcNo = "";
  String tSeq = "";
  double tAmt = 0;
  double totalAmt = 0;
  String idxCurr = "";
  String hGrskCurrCode = "";
  String hGrskCurrCodeGl = "";
  String hGrskAcNo = "";
  int hGrskSeqNo = 0;
  double hGrskAmt = 0;
  String hGrskReferenceNo = "";
  String hBityExterDesc = "";
  String hStmtInstFlag = "";
  String billType = "";
  String txnCode = "";
  String hBatchNoErr = "";
  String tempVouchType = "";
  int vouchCnt = 0;
  int nCycleCnt = 0;
  int ptrActcodeCnt = 0;
  int cntDc = 0;
  int totCnt = 0;
  double sumErrPDc = 0;
  double sumErrMDc = 0;
  int cntErrVouch = 0;
  int currIdx = 0;
  String sw11 = "";
  String sw23 = "";
  String dcFlag = "";

  String[] hMWdayStmtCycle = new String[250];
  String[] hMWdayNextCloseDate = new String[250];
  String[] hMWdayNextAcctMonth = new String[250];
  String[] hMWdayNextLastpayDate = new String[250];
  String[] hMWdayThisAcctMonth = new String[250];
  String[] hMPcodAcctCode = new String[250];
  String[] hMPcodInterestMethod = new String[250];

  double[] payAmtP = new double[37];
  double[] payAmtRi = new double[37];
  double[] payAmtM = new double[37];
  double[] payAmtPD = new double[37];
  double[] payAmtMD = new double[37];
  double[] payAmtPF = new double[37];
  double[] payAmtMF = new double[37];

  String[] currArray = new String[30];
  long[] batchArray = new long[30];
  long[] seqArray = new long[30];
  long[] cntArray = new long[30];
  double[] amtArray = new double[30];

  String hWdayNextCloseDate = "";
  String hWdayNextLastpayDate = "";
  String hPcodInterestMethod = "";
  String hRskBatchNo = "";
  String hWdayNextAcctMonth = "";
  String hWdayThisAcctMonth = "";
  String hTempBatchNo = "";
  String swPrint = "";
  String swDcr = "";
  double sumErrM = 0;
  double sumErrP = 0;
  String tempTypeIn = "";
  String hCur1CardNo = "";
  String hCur1Id = "";
  long tempLong = 0;
  long lTotalAmt = 0;
  int hSeqNo = 0;
  int countOSTX = 0;
  int countINTX = 0;
  int countOKTX = 0;
  long cntLai = 0;
  
  com.CommDate commDate = new com.CommDate();
  com.CommString commString = new com.CommString();
  com.CommSqlStr commSqlStr = new com.CommSqlStr();
  double dbCardLimit = 0.0;
  double txAmtLowRate = 0.0;
  double txAmtHighRate = 0.0;
  double txAmtDiffRate = 0.0;
  
  
  // *********************************************************

  public int mainProcess(String[] args) {
    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);
      // =====================================
      if (args.length > 1) {
        comc.errExit("Usage : BilA009 callbatch_seqno", "");
      }

      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      for (int argi=0; argi < args.length ; argi++ ) {
    	  if (args[argi].equalsIgnoreCase("debug")) {
    		  debug=true;
    	  }
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

      String checkHome = comc.getECSHOME();
      if (comcr.hCallBatchSeqno.length() > 6) {
        if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6)
            .equals(comc.getSubString(checkHome, 0, 6))) {
          comcr.hCallBatchSeqno = "no-call";
        }
      }

      comcr.hCallRProgramCode = javaProgram;
      hTempUser = "";
      if (comcr.hCallBatchSeqno.length() == 20) {
        comcr.callbatch(0, 0, 1);
        selectSQL = " user_id ";
        daoTable = "ptr_callbatch";
        whereStr = "WHERE batch_seqno   = ?  ";

        setString(1, comcr.hCallBatchSeqno);
        if (selectTable() > 0)
          hTempUser = getValue("user_id");
      }

      selectPtrBusinday();
      showLogMessage("I", "", "Process_date = " + hBusiBusinessDate);

      sw11 = "Y";
      sw23 = "Y";

      initParameter();  

      comcr.hGsvhModWs = "BIL_A009R0";
      hCurpModUser = "BATCH";

      
      loadPtrWorkday();
      loadPtrActcode();
      // 取授權比對參數
      getAuthParm();

      hApbtBatchTotCnt = 0;
      hApbtBatchTotAmt = 0;
      cntErrVouch = 0;
      selectBilCurpost();
      
      if (debug)
    	  showLogMessage("I", "", "  end idx=[" + currIdx + "]" + ",Rsk=" + cntErrVouch);
      
      if (currIdx > 0) {
        insertActPayBatch();
      }

      comcr.hGsvhCurr = "00";

      if (debug)
    	  showLogMessage("I", "", "  8888 end dc=[" + cntDc + "]");

      String temstrCom =
          String.format("%s/reports/BIL_A009R0_%s", comc.getECSHOME(), hSystemDateFull);
      temstrCom = Normalizer.normalize(temstrCom, java.text.Normalizer.Form.NFKD);
      //comc.writeReport(temstrCom, comcr.lpar);

      // ==============================================
      // 固定要做的
      comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
      showLogMessage("I", "", comcr.hCallErrorDesc);

      if (comcr.hCallBatchSeqno.length() == 20)
        comcr.callbatch(1, 0, 1); // 1: 結束

      finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
    }
  }

  // **********************************************************************
  private void initParameter() {
    for (int int1 = 1; int1 <= 36; int1++) {
      payAmtP[int1 - 1] = 0;
      payAmtRi[int1 - 1] = 0;
      payAmtM[int1 - 1] = 0;
      payAmtPD[int1 - 1] = 0;
      payAmtMD[int1 - 1] = 0;
      payAmtPF[int1 - 1] = 0;
      payAmtMF[int1 - 1] = 0;
    }

    for (int int1 = 0; int1 < 30; int1++) {
      currArray[int1] = "";
      batchArray[int1] = 0;
      seqArray[int1] = 0;
      cntArray[int1] = 0;
      amtArray[int1] = 0;
    }
  }

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hSystemDateFull = "";

		extendField = "ptrbusinday.";
		daoTable = "ptr_businday";
		sqlCmd = "select substr(to_char(to_number(business_date)- 19110000,'0000000'),2,7) h_businss_chi_date,";
		sqlCmd += "substr(to_char(to_number(vouch_date)- 19110000,'0000000'),2,7) h_vouch_chi_date,";
		sqlCmd += "business_date,";
		sqlCmd += "to_char(sysdate,'yyyymmdd') h_system_date,";
		sqlCmd += "to_char(sysdate,'hh24miss') h_system_time,";
		sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_full ";
		sqlCmd += "from ptr_businday ";
		recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusinssChiDate = getValue("ptrbusinday.h_businss_chi_date");
			hVouchChiDate = getValue("ptrbusinday.h_vouch_chi_date");
			hBusiBusinessDate = getValue("ptrbusinday.business_date");
			hSystemDate = getValue("ptrbusinday.h_system_date");
			hSystemTime = getValue("ptrbusinday.h_system_time");
			hSystemDateFull = getValue("ptrbusinday.h_system_date_full");
		}

	}


  /**********************************************************************/
  void selectPtrActcode() throws Exception {

    for (int int1 = 0; int1 < 250; int1++) {
      hMPcodAcctCode[int1] = "";
      hMPcodInterestMethod[int1] = "";
    }

    sqlCmd = "select acct_code,";
    sqlCmd += "interest_method ";
    sqlCmd += "  from ptr_actcode ";
    recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      hMPcodAcctCode[i] = getValue("acct_code", i);
      hMPcodInterestMethod[i] = getValue("interest_method", i);
    }

    ptrActcodeCnt = recordCnt;
  }

	/***********************************************************************/
	void selectBilCurpost() throws Exception {

		//AuthBatch060 l060 = new AuthBatch060();
		//l060.initProg(getConnection());

		daoTable = "bil_curpost";
		sqlCmd = "select ";
		sqlCmd += "a.reference_no,";
		sqlCmd += "a.bill_type,";
		sqlCmd += "a.txn_code,";
		sqlCmd += "a.sign_flag, ";
		sqlCmd += "a.card_no,";
		sqlCmd += "uf_hi_cardno(a.card_no) h_cur1_card_no,";
		sqlCmd += "film_no,";
		sqlCmd += "acq_member_id,";
		sqlCmd += "purchase_date,";
		sqlCmd += "dest_amt,";
		sqlCmd += "a.issue_fee,";
		sqlCmd += "a.include_fee_amt,";
		sqlCmd += "a.ucaf,";
		sqlCmd += "a.ec_ind,";
		sqlCmd += "issue_s_r,";
		sqlCmd += "dest_curr,";
		sqlCmd += "source_amt,";
		sqlCmd += "source_curr,";
		sqlCmd += "mcht_eng_name,";
		sqlCmd += "mcht_city,";
		sqlCmd += "mcht_country,";
		sqlCmd += "mcht_category,";
		sqlCmd += "mcht_zip,";
		sqlCmd += "mcht_state,";
		sqlCmd += "tmp_request_flag,";
		sqlCmd += "usage_code,";
		sqlCmd += "reason_code,";
		sqlCmd += "settl_amt,";
		sqlCmd += "tmp_service_code,";
		sqlCmd += "auth_code,";
		sqlCmd += "pos_term_capability,";
		sqlCmd += "pos_pin_capability,";
		sqlCmd += "a.pos_entry_mode,";
		sqlCmd += "process_date,";
		sqlCmd += "reimbursement_attr,";
		sqlCmd += "second_conversion_date,";
		sqlCmd += "a.mcht_no,";
		sqlCmd += "mcht_chi_name,";
		sqlCmd += "a.electronic_term_ind,";
		sqlCmd += "transaction_source,";
		sqlCmd += "acquire_date,";
		sqlCmd += "contract_no,";
		sqlCmd += "contract_seq_no,";
		sqlCmd += "contract_amt,";
		sqlCmd += "original_no,";
		sqlCmd += "term,";
		sqlCmd += "total_term,";
		sqlCmd += "a.batch_no,";
		sqlCmd += "exchange_rate,";
		sqlCmd += "exchange_date,";
		sqlCmd += "a.acct_code,";
		sqlCmd += "a.acct_item,";
		sqlCmd += "a.acct_eng_short_name,";
		sqlCmd += "a.acct_chi_short_name,";
		sqlCmd += "a.item_order_normal,";
		sqlCmd += "a.item_order_back_date,";
		sqlCmd += "a.item_order_refund,";
		sqlCmd += "a.item_class_normal,";
		sqlCmd += "a.item_class_back_date,";
		sqlCmd += "a.item_class_refund,";
		sqlCmd += "e.interest_mode,"; // 起息日選項
		sqlCmd += "e.adv_wkday,"; // n days
		sqlCmd += "a.collection_mode,";
		sqlCmd += "a.cash_adv_state,";
		sqlCmd += "decode(a.this_close_date,'',cast(? as varchar(10)),a.this_close_date) h_bill_post_date,";
		sqlCmd += "valid_flag,";
		sqlCmd += "doubt_type,";
		sqlCmd += "duplicated_flag,";
		sqlCmd += "rsk_rsn, ";
		sqlCmd += "rsk_type,";
		// sqlCmd += "rsk_type2, ";
		sqlCmd += "acct_type,";
		sqlCmd += "a.stmt_cycle,";
		sqlCmd += "major_card_no,";
		sqlCmd += "issue_date,";
		sqlCmd += "promote_dept,";
		sqlCmd += "prod_no,";
		sqlCmd += "group_code,";
		sqlCmd += "bin_type,";
		sqlCmd += "acno_p_seqno,";
		sqlCmd += "nvl(uf_idno_id(id_p_seqno),'') as id,";
		sqlCmd += "uf_hi_idno(uf_idno_id(id_p_seqno)) h_cur1_id,";
		sqlCmd += "acexter_desc,";
		sqlCmd += "source_code,";
		sqlCmd += "query_type,";
		sqlCmd += "floor_limit,";
		sqlCmd += "reference_no_original,";
		sqlCmd += "fees_reference_no,";
		sqlCmd += "reference_no_fee_f,";
		sqlCmd += "tx_convt_flag,";
		sqlCmd += "limit_end_date,";
		sqlCmd += "chip_condition_code,";
		sqlCmd += "auth_response_code,";
		sqlCmd += "transaction_type,";
		sqlCmd += "terminal_ver_results,";
		sqlCmd += "iad_result,";
		sqlCmd += "card_seq_num,";
		sqlCmd += "unpredic_num,";
		sqlCmd += "app_tran_count,";
		sqlCmd += "app_int_pro,";
		sqlCmd += "cryptogram,";
		sqlCmd += "der_key_index,";
		sqlCmd += "cry_ver_num,";
		sqlCmd += "data_auth_code,";
		sqlCmd += "cry_info_data,";
		sqlCmd += "terminal_cap_pro,";
		sqlCmd += "life_cyc_sup_ind,";
		sqlCmd += "banknet_date,";
		sqlCmd += "inter_rate_des,";
		sqlCmd += "expir_date,";
		sqlCmd += "transaction_amt_char,";
		sqlCmd += "dac,";
		sqlCmd += "service_code,";
		sqlCmd += "a.amt_mccr,";
		sqlCmd += "a.amt_iccr,";
		sqlCmd += "installment_source,";
		sqlCmd += "payment_type,";
		sqlCmd += "curr_tx_amount,";
		sqlCmd += "install_tot_term  h_curp_install_tot_term,";
		sqlCmd += "install_first_amt h_curp_install_first_amt,";
		sqlCmd += "install_per_amt   h_curp_install_per_amt,";
		sqlCmd += "install_fee       h_curp_install_fee,";
		sqlCmd += "deduct_bp         h_curp_deduct_bp,";
		sqlCmd += "cash_pay_amt      h_curp_cash_pay_amt,";
		sqlCmd += "merge_flag,";
		sqlCmd += "installment_kind,";
		sqlCmd += "ptr_merchant_no,";
		sqlCmd += "acce_fee,";
		sqlCmd += "acce_fee_in_bc,";
		sqlCmd += "add_acct_type,";
		sqlCmd += "add_amt_type,";
		sqlCmd += "add_curcy_code,";
		sqlCmd += "add_amt_sign,";
		sqlCmd += "add_amt,";
		sqlCmd += "a.terminal_id,";
		sqlCmd += "bnet_ref_num,";
		sqlCmd += "de22,";
		sqlCmd += "decode(a.curr_code,'','901','TWD','901',a.curr_code) h_curp_curr_code,";
		sqlCmd += "dc_amount,";
		sqlCmd += "dc_exchange_rate,";
		sqlCmd += "curr_code_gl,";
		sqlCmd += "a.mcs_num,";
		sqlCmd += "a.mcs_cnt,";
		sqlCmd += "term_type,";
		sqlCmd += "wallet_iden,";
		sqlCmd += "accept_term_ind,";
		sqlCmd += "mcht_zip_tw,";
		sqlCmd += "mcht_type,";
		sqlCmd += "a.v_card_no,";  
		sqlCmd += "a.settl_flag,";          //hCurpSettlFlag
		sqlCmd += "a.ecs_platform_kind,";   //hCurpEcsPlatformKind
		sqlCmd += "a.ecs_cus_mcht_no,";     //hCurpEcsCusMchtNo
		// sqlCmd += "a.qr_flag,";
		sqlCmd += "a.rowid  as rowid ";
		sqlCmd += " from bil_curpost a  ";
		sqlCmd += "inner join bil_postcntl    n on n.batch_no     = a.batch_no ";
		sqlCmd += "inner join ptr_billtype    e on ";
		sqlCmd += "         e.bill_type  = a.bill_type and e.txn_code = a.txn_code ";
		sqlCmd += "inner join ptr_currcode    c on ";
		sqlCmd += "         c.curr_code  = decode(a.curr_code,'','901','TWD','901',a.curr_code) ";
		sqlCmd += " left join bil_nccc300_dtl d on d.reference_no = a.reference_no ";
		sqlCmd += "where 1=1 ";
		sqlCmd += "  and decode(a.entry_acct   ,'','N',a.entry_acct )   in ('Y','y') "; // 是否入帳
		sqlCmd += "  and decode(confirm_flag_p ,'','N',confirm_flag_p ) in ('Y','y') "; // 須覆核註記
		sqlCmd += "  and decode(contract_flag  ,'','N',contract_flag )  in ('P','p','N','n') "; // 分期查核旗標
		sqlCmd += "  and decode(manual_upd_flag,'','N',manual_upd_flag) != 'Y' "; // 人工更改旗標
		sqlCmd += "  and decode(valid_flag     ,'','N',valid_flag )     != 'Y' "; // Y:需取授權 W:送至授權 E:授權有誤 P:授權無誤
		sqlCmd += "  and decode(curr_post_flag ,'','N',curr_post_flag)  in ('N','n') "; // 當日入帳旗標
		sqlCmd += "  and decode(format_chk_ok_flag ,'','N',format_chk_ok_flag) in ('N','n') "; // 格式查核旗標
		sqlCmd += "  and decode(double_chk_ok_flag ,'','N',double_chk_ok_flag) in ('N','n') "; // 重覆查核旗標
		sqlCmd += "  and decode(err_chk_ok_flag    ,'','N',err_chk_ok_flag )   in ('N','n') "; // 疑異查核旗標
		
		/*手續費計算註記不參考
		sqlCmd += "  and ((decode(a.fees_state ,'','N',a.fees_state)    in ('P','N') or "; // 手續費收取
		sqlCmd += "       (decode(a.fees_state ,'','N',a.fees_state)    in ('Y','y') and ";
		sqlCmd += "        decode(rsk_type     ,'','N',rsk_type )   not in ('N','4'))) ";
		sqlCmd += "   and (decode(a.cash_adv_state,'','N',a.cash_adv_state) in ('P','N') or "; // 預借現金手續費
		sqlCmd += "       (decode(a.cash_adv_state,'','N',a.cash_adv_state) in ('Y','y') and ";
		sqlCmd += "        decode(rsk_type,'','N',rsk_type )        not in ('N','4')))) ";
		*/
		
		sqlCmd += "order by a.batch_no ";
		setString(1, hBusiBusinessDate);

		openCursor();

		totCnt = 0;
		while (fetchTable()) {
			hCurpReferenceNo = getValue("reference_no");
			hCurpBillType = getValue("bill_type");
			hCurpTransactionCode = getValue("txn_code");
			hCurpSignFlag = getValue("sign_flag");
			hCurpBinType = getValue("bin_type");
			hCurpCardNo = getValue("card_no");
			hCur1CardNo = getValue("h_cur1_card_no");
			hCurpFilmNo = getValue("film_no");
			hCurpAcquirerMemberId = getValue("acq_member_id");
			hCurpPurchaseDate = getValue("purchase_date");
			hCurpDestinationAmt = getValueDouble("dest_amt");
			hCurpIssueFee = getValue("issue_fee");
			hCurpIncludeFeeAmt = getValueDouble("include_fee_amt");
			hCurpUcaf = getValue("ucaf");
			hCurpEcInd = getValue("ec_ind");
			hCurpIssueSR = getValue("issue_s_r");
			hCurpDestinationCurrency = getValue("dest_curr");
			hCurpSourceAmt = getValueDouble("source_amt");
			hCurpSourceCurrency = getValue("source_curr");
			hCurpMerchantEngName = getValue("mcht_eng_name");
			hCurpMerchantCity = getValue("mcht_city");
			hCurpMerchantCountry = getValue("mcht_country");
			hCurpMerchantCategory = getValue("mcht_category");
			hCurpMerchantZip = getValue("mcht_zip");
			hCurpMerchantState = getValue("mcht_state");
			hCurpTmpRequestFlag = getValue("tmp_request_flag");
			hCurpUsageCode = getValue("usage_code");
			hCurpReasonCode = getValue("reason_code");
			hCurpSettlementAmt = getValueDouble("settl_amt");
			hCurpTmpServiceCode = getValue("tmp_service_code");
			hCurpAuthorization = getValue("auth_code");
			hCurpPosTermCapability = getValue("pos_term_capability");
			hCurpPosPinCapability = getValue("pos_pin_capability");
			hCurpPosEntryMode = getValue("pos_entry_mode");
			hCurpProcessDate = getValue("process_date");
			hCurpReimbursementAttr = getValue("reimbursement_attr");
			hCurpSecondConversionDate = getValue("second_conversion_date");
			hCurpMerchantNo = getValue("mcht_no");
			hCurpMerchantChiName = getValue("mcht_chi_name");
			hCurpElectronicTermInd = getValue("electronic_term_ind");
			hCurpTransactionSource = getValue("transaction_source");
			hCurpAcquireDate = getValue("acquire_date");
			hCurpContractNo = getValue("contract_no");
			hCurpContractSeqNo = getValue("contract_seq_no");
			hCurpContractAmt = getValueDouble("contract_amt");
			hCurpOriginalNo = getValue("original_no");
			hCurpTerm = getValue("term");
			hCurpTotalTerm = getValue("total_term");
			hCurpBatchNo = getValue("batch_no");
			hCurpExchangeRate = getValue("exchange_rate");
			hCurpExchangeDate = getValue("exchange_date");
			hCurpAcctCode = getValue("acct_code");
			hCurpAcctItem = getValue("acct_item");
			hCurpAcctEngShortName = getValue("acct_eng_short_name");
			hCurpAcctChiShortName = getValue("acct_chi_short_name");
			hCurpItemOrderNormal = getValue("item_order_normal");
			hCurpItemOrderBackDate = getValue("item_order_back_date");
			hCurpItemOrderRefund = getValue("item_order_refund");
			hCurpItemClassNormal = getValue("item_class_normal");
			hCurpItemClassBackDate = getValue("item_class_back_date");
			hCurpItemClassRefund = getValue("item_class_refund");
			hCurpInterestMode = getValue("interest_mode");
			hCurpAdvWkday = getValue("adv_wkday");
			hCurpCollectionMode = getValue("collection_mode");
			hCurpCashAdvState = getValue("cash_adv_state");
			hBillPostDate = getValue("h_bill_post_date");
			hCurpValidFlag = getValue("valid_flag");
			hCurpDoubtType = getValue("doubt_type");
			hCurpDuplicatedFlag = getValue("duplicated_flag");
			hCurpRskRsn = getValue("rsk_rsn");
			hCurpRskType = getValue("rsk_type");
			// hCurpRskType2 = getValue("rsk_type2");
			hCurpAcctType = getValue("acct_type");
			hCurpStmtCycle = getValue("stmt_cycle");
			hCurpMajorCardNo = getValue("major_card_no");
			hCurpIssueDate = getValue("issue_date");
			hCurpPromoteDept = getValue("promote_dept");
			hCurpProdNo = getValue("prod_no");
			hCurpGroupCode = getValue("group_code");
			hCurpPSeqno = getValue("acno_p_seqno");
			hCurpId = getValue("id");
			hCur1Id = getValue("h_cur1_id");
			hCurpAcexterDesc = getValue("acexter_desc");
			hCurpSourceCode = getValue("source_code");
			hCurpQueryType = getValue("query_type");
			hCurpFloorLimit = getValue("floor_limit");
			hCurpReferenceNoOriginal = getValue("reference_no_original");
			hCurpFeesReferenceNo = getValue("fees_reference_no");
			hCurpReferenceNoFeeF = getValue("reference_no_fee_f");
			hCurpTxConvtFlag = getValue("tx_convt_flag");
			hCurpLimitEndDate = getValue("limit_end_date");
			hCurpChipConditionCode = getValue("chip_condition_code");
			hCurpAuthResponseCode = getValue("auth_response_code");
			hCurpTransactionType = getValue("transaction_type");
			hCurpTerminalVerResults = getValue("terminal_ver_results");
			hCurpIadResult = getValue("iad_result");
			hCurpCardSeqNum = getValue("card_seq_num");
			hCurpUnpredicNum = getValue("unpredic_num");
			hCurpAppTranCount = getValue("app_tran_count");
			hCurpAppIntPro = getValue("app_int_pro");
			hCurpCryptogram = getValue("cryptogram");
			hCurpDerKeyIndex = getValue("der_key_index");
			hCurpCryVerNum = getValue("cry_ver_num");
			hCurpDataAuthCode = getValue("data_auth_code");
			hCurpCryInfoData = getValue("cry_info_data");
			hCurpTerminalCapPro = getValue("terminal_cap_pro");
			hCurpLifeCycSupInd = getValue("life_cyc_sup_ind");
			hCurpBanknetDate = getValue("banknet_date");
			hCurpInterRateDes = getValue("inter_rate_des");
			hCurpExpirDate = getValue("expir_date");
			hCurpTransactionAmtChar = getValue("transaction_amt_char");
			hCurpDac = getValue("dac");
			hCurpServiceCode = getValue("service_code");
			hCurpAmtMccr = getValue("amt_mccr");
			hCurpAmtIccr = getValue("amt_iccr");
			hCurpInstallmentSource = getValue("installment_source");
			hCurpPaymentType = getValue("payment_type");
			hCurpCurrTxAmount = getValueDouble("curr_tx_amount");
			hCurpInstallTotTerm = getValue("h_curp_install_tot_term");
			hCurpInstallFirstAmt = getValueDouble("h_curp_install_first_amt");
			hCurpInstallPerAmt = getValueDouble("h_curp_install_per_amt");
			hCurpInstallFee = getValue("h_curp_install_fee");
			hCurpDeductBp = getValue("h_curp_deduct_bp");
			hCurpCashPayAmt = getValueDouble("h_curp_cash_pay_amt");
			hCurpMergeFlag = getValue("merge_flag");
			hCurpInstallmentKind = getValue("installment_kind");
			hCurpPtrMerchantNo = getValue("ptr_mcht_no");
			hCurpAcceFee = getValue("acce_fee");
			hCurpAcceFeeInBc = getValue("acce_fee_in_bc");
			hCurpAddAcctType = getValue("add_acct_type");
			hCurpAddAmtType = getValueDouble("add_amt_type");
			hCurpAddCurcyCode = getValue("add_curcy_code");
			hCurpAddAmtSign = getValue("add_amt_sign");
			hCurpAddAmt = getValueDouble("add_amt");
			hCurpTerminalId = getValue("terminal_id");
			hCurpBnetRefNum = getValue("bnet_ref_num");
			hCurpDe22 = getValue("de22");
			hCurpCurrCode = getValue("h_curp_curr_code");
			hCurpDcAmount = getValueDouble("dc_amount");
			hCurpDcExchangeRate = getValue("dc_exchange_rate");
			hCurrCodeGl = getValue("curr_code_gl");
			hCurpMcsNum = getValue("mcs_num");
			hCurpMcsCnt = getValue("mcs_cnt");
			hCurpTermType = getValue("term_type");
			hCurpWalletIden = getValue("wallet_iden");
			hcurpaccepttermind = getValue("accept_term_ind");
			hCurpMerchantZipTw = getValue("mcht_zip_tw");
			hCurpMerchantType = getValue("mcht_type");
			hCurpVCardNo = getValue("v_card_no");
			hCurpSettlFlag = getValue("settl_flag");
			hCurpEcsPlatformKind = getValue("ecs_platform_kind");
			hCurpEcsCusMchtNo = getValue("ecs_cus_mcht_no");
			// hCurpQrFlag = getValue("qr_flag");
			hCurpRowid = getValue("rowid");

			hCurpNewItFlag = "";
			if (hCurpContractNo.length() > 1) {
				sqlCmd = "select new_it_flag ";
				sqlCmd += "  from bil_contract  ";
				sqlCmd += " where contract_no     = ? ";
				sqlCmd += "   and contract_seq_no = 1 ";
				setString(1, hCurpContractNo);
				int tmpInt = selectTable();
				if (tmpInt > 0) {
					hCurpNewItFlag = getValue("new_it_flag");
				}
			}
			totCnt++;
			if (totCnt % 5000 == 0 || totCnt == 1) {
				showLogMessage("I", "", String.format("Process record=[%d]", totCnt));
			}

			if (debug) {
				showLogMessage("I", "", "888 card_no=[" + hCurpCardNo + "]" + totCnt + "," + hCurpTransactionCode + ","
						+ hCurrCodeGl + ",curr_term=" + hCurpTerm);
				showLogMessage("I", "", " REF_NO=[" + hCurpReferenceNo + "]" + hCurpDestinationAmt);
			}

			dcFlag = "N";
			if (hCurpCurrCode.equals("901") == false) {
				dcFlag = "Y";
				cntDc++;
			}

			//除了紅利折抵交易應該會傳入cashPayAmt, 其他交易要搬入destinationAmt
			String[] valueArray = { "1", "2" };
			if (!Arrays.asList(valueArray).contains(hCurpPaymentType)) {
				hCurpCashPayAmt = hCurpDestinationAmt;
			}

			setValue("ptrworkday.stmt_cycle", getValue("stmt_cycle"));
			int cnt1 = getLoadData("ptrworkday.stmt_cycle");
			if (cnt1 > 0) {
				hWdayNextCloseDate = getValue("ptrworkday.next_close_date");
				hWdayNextAcctMonth = getValue("ptrworkday.next_acct_month");
				hWdayNextLastpayDate = getValue("ptrworkday.next_lastpay_date");
				hWdayThisAcctMonth = getValue("ptrworkday.this_acct_month");
			} else {
				; // should be error not found stmt_cycle parm.
			}

			hBillInterestDate = "";
			switch (comcr.str2int(hCurpInterestMode.length() > 0 ? hCurpInterestMode : "")) {
			case 1:
				hBillInterestDate = hCurpPurchaseDate;
				break;
			case 2:
				hBillInterestDate = comcr.increaseDays(hBillPostDate, comcr.str2int(hCurpAdvWkday));
				break;
			case 3:
				hBillInterestDate = hWdayNextCloseDate;
				break;
			case 4:
				hBillInterestDate = hWdayNextLastpayDate;
				break;
			}

			/* 沒有用到的欄位 20210823 */
			if (hCurpInterestRskDate.length() != 0)
				hBillInterestDate = hCurpInterestRskDate;

			if (hBillInterestDate.length() == 0) {
				setValue("ptractcode.acct_code", getValue("acct_code"));
				int cnt2 = getLoadData("ptractcode.acct_code");
				if (cnt2 > 0) {
					hPcodInterestMethod = getValue("ptractcode.interest_method");
					if (hPcodInterestMethod.equals("Y"))
						hBillInterestDate = hBillPostDate;
				}
			}

			selectCrdCard();

			hCurpPSeqno = hCardAcctPSeqno;

			/*授權比對 */
			
			/* 1.分期交易只有第1期需要拿分期總金額去做授權比對
			 *  2.非分期交易才走正常的比對邏輯
			 *  3....
			*/
		
			hTxlogSpecFlag = "";
			
			int chkAuthFlag = procAuthCheckCondition();
			if (chkAuthFlag == 0) {
				
				//第一次 授權比對
				int cva = 0;
				if (hCurpBillType.equals("TSCC") || hCurpBillType.equals("IPSS") || hCurpBillType.equals("ICAH")) {
					cva = checkAuthAddValue();
				} else {
					cva = checkAuth();	
				}
		      
				// 第二次 授權比對(第一次比對不成功才執行)
				if (cva == 1) {
					cva = checkAuth2();
				}
		      
				// 第三次 授權比對(第二次比對不成功才執行)
				if (cva == 1) {
					cva = checkAuth3();
				}
			}
			
			if (debug)
				showLogMessage("I", "", " 888 update_cca_rtn END =" + tmpInt);

			insertBilBill();
			updateBilCurpost1();

			if (hCurpRskType.equals("1") || hCurpRskType.equals("2") || hCurpRskType.equals("3")) {
				if (!hCurpBillType.equals("FIFC")) {
					cntErrVouch++;
					if (cntErrVouch == 1) {
						hRskBatchNo = hCurpBatchNo;
					}
				}
			}
			procVouchType();

			commitDataBase();
		}
		closeCursor();
		return;
	}
	/**********************************************************************/
	void selectCrdCard() throws Exception {

		hCardOriCardNo = "";
		hCardNewBegDate = "";
		hCardNewEndDate = "";
		
		extendField="crdCard.";
		String thisExtendField = extendField;
		daoTable = "crd_card";
		sqlCmd = "select reg_bank_no,";
		sqlCmd += "decode(ori_issue_date,'',issue_date,ori_issue_date) as issue_date, ";
		sqlCmd += "group_code,";
		sqlCmd += "card_type ,";
		sqlCmd += "major_id_p_seqno,";
		sqlCmd += "id_p_seqno,";
		sqlCmd += "corp_p_seqno,";
		sqlCmd += "corp_no,";
		sqlCmd += "corp_no_code,";
		sqlCmd += "ori_card_no, ";
		sqlCmd += "new_beg_date,";
		sqlCmd += "new_end_date,";
		sqlCmd += "p_seqno, ";
		sqlCmd += "acno_p_seqno ";
		sqlCmd += "from crd_card  ";
		sqlCmd += "where card_no = ? ";
		setString(1, hCurpCardNo);
		int tmpInt = selectTable();
		if (tmpInt > 0) {
			hCardRegBankNo = getValue(thisExtendField+"reg_bank_no");
			hCardIssueDate = getValue(thisExtendField+"issue_date");
			hCardGroupCode = getValue(thisExtendField+"group_code");
			hCardCardType = getValue(thisExtendField+"card_type");
			hCardIdPSeqno = getValue(thisExtendField+"id_p_seqno");
			hCardMajorIdPSeqno = getValue(thisExtendField+"major_id_p_seqno");
			hCardCorpPSeqno = getValue(thisExtendField+"corp_p_seqno");
			hCardCorpNo = getValue(thisExtendField+"corp_no");
			hCardCorpNoCode = getValue(thisExtendField+"corp_no_code");
			hCardOriCardNo = getValue(thisExtendField+"ori_card_no");
			hCardNewBegDate = getValue(thisExtendField+"new_beg_date");
			hCardNewEndDate = getValue(thisExtendField+"new_end_date");
			hCardAcctPSeqno = getValue(thisExtendField+"p_seqno");
			hCardAcnoPSeqno = getValue(thisExtendField+"acno_p_seqno");
		}
	}

  /***********************************************************************
   * old version for reference
   * 
   * *********************************************************************/
	
//  int updateCcaRtn(AuthBatch060 l060) throws Exception {
//    if (hCurpAuthorization.length() < 6)
//      return (0);
//
//    /** get loan_flag on bil_merchant **/
//    sqlCmd = " select loan_flag from bil_merchant where mcht_no = ? ";
//    setString(1, hCurpMerchantNo);
//    selectTable();
//    String loanFlag = "";
//    loanFlag = getValue("loan_flag");
//
//    /** get temp_flag **/
//    sqlCmd = " select '1' temp_flag from  tsc_cgec_all  ";
//    sqlCmd += " where reference_no = ? ";
//    sqlCmd += "   and decode(online_mark,'','0',online_mark) = '1' ";
//    setString(1, hCurpReferenceNo);
//    selectTable();
//    String tempFlag = "";
//    tempFlag = getValue("temp_flag");
//
//    if ((hCurpBillType.equals("I2") == false || hCurpBillType.equals("I1") == false
//        || hCurpBillType.equals("HQ") == false || hCurpBillType.equals("IN") == false
//        || hCurpBillType.equals("OS") == false || hCurpBillType.equals("OK") == false
//        || hCurpBillType.equals("OI") == false || hCurpBillType.equals("CO") == false
//        || hCurpBillType.equals("TS") == false)
//        || (hCurpBillType.equals("INBO") && hCurpTransactionCode.equals("05"))
//        || (hCurpBillType.equals("OICU") && hCurpTransactionCode.equals("PO")
//            && comcr.str2int(hCurpTerm) == 1 && comcr.str2int(hCurpTotalTerm) > 0
//            && (loanFlag.equals("N") || loanFlag.equals("")))
//        || (hCurpBillType.equals("OICU") && hCurpTransactionCode.equals("IN")
//            && comcr.str2int(hCurpTerm) == 1 && comcr.str2int(hCurpTotalTerm) > 0
//            && (hCurpMergeFlag.equals("") ? "N" : hCurpMergeFlag).equals("Y") == false
//            && (loanFlag.equals("N") || loanFlag.equals("")))
//        || (hCurpBillType.equals("TSCC") && tempFlag.equals("1"))) {
//      if ((hCurpAuthorization.equals("") ? "N" : hCurpAuthorization).equals("VLP111") == false
//          || (hCurpAuthorization.equals("") ? "N" : hCurpAuthorization).equals("JCB111") == false
//          || (hCurpAuthorization.equals("") ? "N" : hCurpAuthorization).equals("Y1") == false) {
//        if (hCurpRskType.equals("") || hCurpRskType.equals("4")) {// todo AuthBatch_060
//          String hOnbaTransCode = "";
//          if ((hCurpTransactionCode.equals("06")) || (hCurpTransactionCode.equals("25"))
//              || (hCurpTransactionCode.equals("27")) || (hCurpTransactionCode.equals("28"))
//              || (hCurpTransactionCode.equals("29")) || (hCurpTransactionCode.equals("66"))
//              || (hCurpTransactionCode.equals("85")) || (hCurpTransactionCode.equals("87"))
//              || (hCurpTransactionCode.equals("20"))) {
//            hOnbaTransCode = "51";
//          } else {
//            hOnbaTransCode = "01";
//          }
//
//          if (comcr.str2int(hOnbaTransCode) > 50)
//            return (0);// TransCode > 50 continue
//          int hOnbaTransAmt = hCurpDestinationAmt == 0 ? 1 : (int) hCurpDestinationAmt;
//
//          // 卡號,auth_no , date ,amt
//
//          boolean bLResult = false;
//          Data060Vo lData060Vo = new Data060Vo();
//          lData060Vo.setCardNo(hCurpCardNo);
//          lData060Vo.setAuthNo(hCurpAuthorization);
//          lData060Vo.setTransAmt(hOnbaTransAmt);
//          lData060Vo.setTransDate(hCurpPurchaseDate);
//          lData060Vo.setCardAcctId("");
//          lData060Vo.setDebitFlag("N");
//          lData060Vo.setMccCode(hCurpMerchantCategory);
//          lData060Vo.setRefeNo(hCurpReferenceNo);
//          lData060Vo.setTransCode(hOnbaTransCode);
//          
//          if (debug)
//        	  showLogMessage("I", "",
//                "  777 card= " + hCurpCardNo + "," + hCurpAuthorization + "," + hOnbaTransAmt
//                    + "," + hCurpPurchaseDate + "," + hCurpMerchantCategory + "," + hCurpReferenceNo
//                    + "," + hOnbaTransCode);
//
//          int nLResult = l060.startProcess(lData060Vo); // 0,1,-1
//
//          if (nLResult != 0) {
//            System.out.println(lData060Vo.getCardNo() + ":" + "發生 error...[" + nLResult + "]");
//            insertOnbat("0");
//          } else {
//            insertOnbat("9");
//            
//            if (debug)
//            	System.out.println(lData060Vo.getCardNo() + ":" + "程式正常處理完成...");
//          }
//
//          lData060Vo = null;
//        }
//      }
//    }
//    return (0);
//  }
//

  /***********************************************************************/
  void insertBilBill() throws Exception {
    
	  if (debug)
		  showLogMessage("I", "", "  insert_bil_bill=[ " + 1 + "]");

    hBillInstallTotTerm = comcr.str2int(hCurpTotalTerm);
    hBillInstallCurrTerm = comcr.str2int(hCurpTerm);

    if (hCurpBillType.substring(0, 2).equals("OS")
        && (hCurpTransactionCode.equals("AI") || hCurpTransactionCode.equals("BF")
            || hCurpTransactionCode.equals("DF") || hCurpTransactionCode.equals("IF"))) {
      hBillAcctMonth = hWdayThisAcctMonth;
    } else {
      hBillAcctMonth = hWdayNextAcctMonth;
    }

    if (dcFlag.equals("N") && hCurpDcAmount == 0) {
      hCurpDcAmount = hCurpDestinationAmt;
    }

    setValue("reference_no", hCurpReferenceNo);
    setValue("p_seqno", hCardAcctPSeqno);
    setValue("acno_p_seqno", hCardAcnoPSeqno);
    setValue("acct_type", hCurpAcctType);
    setValue("major_card_no", hCurpMajorCardNo);
    setValue("major_id_p_seqno", hCardMajorIdPSeqno);
    setValue("id_p_seqno", hCardIdPSeqno);
    setValue("card_no", hCurpCardNo);
    setValue("txn_code", hCurpTransactionCode);
    setValue("sign_flag", hCurpSignFlag);
    setValue("film_no", hCurpFilmNo);
    setValue("acq_member_id", hCurpAcquirerMemberId);
    setValueDouble("dest_amt", hCurpDestinationAmt);
    setValue("dest_curr", hCurpDestinationCurrency);
    setValueDouble("source_amt", hCurpSourceAmt);
    setValue("source_curr", hCurpSourceCurrency);
    setValue("mcht_eng_name", hCurpMerchantEngName);
    setValue("mcht_city", hCurpMerchantCity);
    setValue("mcht_country", hCurpMerchantCountry);
    setValue("mcht_category", hCurpMerchantCategory);
    setValue("mcht_zip", hCurpMerchantZip);
    setValue("mcht_state", hCurpMerchantState);
    setValue("mcht_no", hCurpMerchantNo);
    setValue("mcht_chi_name", hCurpMerchantChiName);
    setValue("auth_code", hCurpAuthorization);
    setValue("acct_month", hBillAcctMonth);
    setValue("bill_type", hCurpBillType);
    setValue("process_date", hCurpProcessDate);
    setValue("acquire_date", hCurpAcquireDate);
    setValue("purchase_date", hCurpPurchaseDate);
    setValue("post_date", hBillPostDate);
    setValue("batch_no", hCurpBatchNo);
    setValue("contract_no", hCurpContractNo);
    setValue("contract_seq_no", hCurpContractSeqNo);
    setValueDouble("contract_amt", hCurpContractAmt);
    setValueInt("install_tot_term", hBillInstallTotTerm);
    setValueInt("install_curr_term", hBillInstallCurrTerm);
    setValue("pos_entry_mode", hCurpPosEntryMode);
    setValueDouble("settl_amt", hCurpSettlementAmt);
    setValue("stmt_cycle", hCurpStmtCycle);
    setValue("prod_no", hCurpProdNo);
    setValue("promote_dept", hCurpPromoteDept);
    setValue("group_code", hCardGroupCode);
    setValue("card_type", hCardCardType);
    setValue("issue_date", hCurpIssueDate);
    setValue("issue_date", hCardIssueDate);
    setValue("collection_mode", hCurpCollectionMode);
    setValue("interest_date", hBillInterestDate);
    setValue("exchange_rate", hCurpExchangeRate);
    setValue("fees_reference_no", hCurpFeesReferenceNo);
    setValue("reference_no_original", hCurpReferenceNoOriginal);
    setValue("reference_no_fee_f", hCurpReferenceNoFeeF);
    setValue("bin_type", hCurpBinType);
    setValue("valid_flag", hCurpValidFlag);
    setValue("rsk_type", hCurpRskType);
    //setValue("rsk_type2", hCurpRskType2);
    setValue("rsk_other1_mark", hCurpRskRsn);
    setValue("acct_code", hCurpAcctCode);
    setValue("acct_item", hCurpAcctItem);
    setValue("acct_eng_short_name", hCurpAcctEngShortName);
    setValue("acct_chi_short_name", hCurpAcctChiShortName);
    setValue("acexter_desc", hCurpAcexterDesc);
    setValue("item_order_normal", hCurpItemOrderNormal);
    setValue("item_order_back_date", hCurpItemOrderBackDate);
    setValue("item_order_refund", hCurpItemOrderRefund);
    setValue("item_class_normal", hCurpItemClassNormal);
    setValue("item_class_back_date", hCurpItemClassBackDate);
    setValue("item_class_refund", hCurpItemClassRefund);
    setValue("source_code", hCurpSourceCode);
    setValue("reg_bank_no", hCardRegBankNo);
    setValue("limit_end_date", hCurpLimitEndDate);
    setValue("expir_date", hCurpExpirDate);
    setValue("installment_source", hCurpInstallmentSource);
    setValue("payment_type", hCurpPaymentType);
    setValueDouble("curr_tx_amount", hCurpCurrTxAmount);
    setValue("install_tot_term1", hCurpInstallTotTerm);
    setValueDouble("install_first_amt", hCurpInstallFirstAmt);
    setValueDouble("install_per_amt", hCurpInstallPerAmt);
    setValue("install_fee", hCurpInstallFee);
    setValue("deduct_bp", hCurpDeductBp);
    setValueDouble("cash_pay_amt", hCurpCashPayAmt);
    setValue("amt_mccr", hCurpAmtMccr);
    setValue("amt_iccr", hCurpAmtIccr);
    setValue("issue_fee", hCurpIssueFee);
    setValue("issue_date", hCardIssueDate);
    setValueDouble("include_fee_amt", hCurpIncludeFeeAmt);
    setValue("merge_flag", hCurpMergeFlag);
    setValue("installment_kind", hCurpInstallmentKind);
    setValue("ptr_merchant_no", hCurpPtrMerchantNo);
    setValue("curr_code", hCurpCurrCode);
    setValueDouble("dc_dest_amt", hCurpDcAmount);
    setValue("dc_exchange_rate", hCurpDcExchangeRate);
    setValue("mcht_zip_tw", hCurpMerchantZipTw);
    setValue("mcht_type", hCurpMerchantType);
    setValue("v_card_no", hCurpVCardNo);
    setValue("ori_card_no", hCardOriCardNo);
    setValue("ucaf", hCurpUcaf);
    setValue("ec_ind", hCurpEcInd);
    setValue("electronic_term_ind", hCurpElectronicTermInd);
    setValue("terminal_id", hCurpTerminalId);
    setValue("mcs_num", hCurpMcsNum);
    setValue("mcs_cnt", hCurpMcsCnt);
    //setValue("qr_flag", hCurpQrFlag);
    setValue("acct_date", hBusiBusinessDate);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", prgmId);
    setValue("settl_flag", hCurpSettlFlag);
    setValue("ecs_platform_kind", hCurpEcsPlatformKind);
    setValue("ecs_cus_mcht_no", hCurpEcsCusMchtNo);
    daoTable = "bil_bill";
    insertTable();

    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_bil_bill duplicate", hCurpReferenceNo, comcr.hCallBatchSeqno);
    }
  }

	/***********************************************************************/
	void updateBilCurpost1() throws Exception {

		if (debug)
			showLogMessage("I", "", " 888 update curpost=[" + hBusiBusinessDate + "]");

		daoTable = "bil_curpost";
		updateSQL = "curr_post_flag = 'Y', ";
		updateSQL += "rsk_type = ? , ";
		updateSQL += "ccas_date      = ?    ";
		whereStr = "where rowid    = ?    ";
		setString(1, hCurpRskType);
		setString(2, hBusiBusinessDate);
		setRowId(3, hCurpRowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_bil_curpost not found!", "", hCurpReferenceNo);
		}

	}
	
  /*************************************************************************/
  void procVouchType() throws Exception {
    
	if (debug)
      showLogMessage("I", "", "  888 procVouchType rskType=[" + hCurpRskType + "]" + hCurpBillType );

    if ((hCurpDoubtType.length() == 0)
        && (hCurpRskType.length() == 0 || (hCurpRskType.equals("1") == false
            && hCurpRskType.equals("2") == false && hCurpRskType.equals("3") == false)) ) {
    	if ((hCurpDestinationAmt < 0) || hCurpSignFlag.equals("-")) {
    		hApbtBatchTotCnt++;
    		hApbtBatchTotAmt = hApbtBatchTotAmt + Math.abs(hCurpCashPayAmt);

    		if (hCurpCurrCode.equals("901")) {
    			hCurpDcAmount = hCurpCashPayAmt;
    		}
    	  
    		chkCurrCode();

    		insertActPayDetail(1);
    	} else {
    		if (hCurpCurrCode.equals("901")) {
    			hCurpDcAmount = hCurpCashPayAmt;
    		}
       
    		//首期入帳更新spec_flag
    		if (comcr.str2int(hCurpTerm) == 1) {
    			updateBilContract();
    		}
    		
			insertActDebt();
			selectActAcct();
			insertActJrnl();
			updateActAcct();
    	}
    }
    
  }

  /**********************************************************************/
  void chkCurrCode() throws Exception {

    for (int int1 = 1; int1 <= currIdx; int1++) {
      if (currArray[int1].equals(hCurpCurrCode)) {
        cntArray[int1]++;
        amtArray[int1] = amtArray[int1] + hCurpDcAmount;
        return;
      }
    }
    currIdx++;
    selectActPayBatch();
    currArray[currIdx] = hCurpCurrCode;
    batchArray[currIdx] = comcr.str2long(hApbtBatchNo) + currIdx;
    cntArray[currIdx] = 1;
    amtArray[currIdx] = hCurpDcAmount;

  }

  /**********************************************************************/
  void selectActPayBatch() throws Exception {
    hApbtBatchNo = "";
    sqlCmd =
        "select ?||'9007'||substr(to_char(to_number(decode(substr(max(batch_no),13,4),null,'0000',substr(max(batch_no),13,4)))+ 1,'0000'),2,4) h_apbt_batch_no ";
    sqlCmd += "from act_pay_batch  ";
    sqlCmd += "where batch_no like ?||'9007%'";
    setString(1, hBusiBusinessDate);
    setString(2, hBusiBusinessDate);
    int tmpInt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_act_pay_batch not found!", "", comcr.hCallBatchSeqno);
    }
    if (tmpInt > 0) {
      hApbtBatchNo = getValue("h_apbt_batch_no");
    }
    
    if (debug)
    	showLogMessage("I", "", "  act_pay_batch =[" + hApbtBatchNo + "]");
  }

  /***********************************************************************/
  void insertActPayDetail(int idx) throws Exception {

    searchCurrCode();

    /*
      showLogMessage("D", "", " insert act_pay_detail=[" + hApbtBatchNo + "]" + hApdlSerialNo);
    */

    if (idx == 1) {
      setValue("payment_type", "REFU");
      setValueDouble("pay_amt", Math.abs(hCurpCashPayAmt));
    } else {
      setValue("payment_type", "OTHR");
      setValueDouble("pay_amt", hCurpContractAmt);
    }

    setValue("batch_no", hApbtBatchNo);
    setValue("serial_no", hApdlSerialNo);
    setValue("p_seqno", hCurpPSeqno);
    setValue("acno_p_seqno", hCardAcnoPSeqno);
    setValue("acct_type", hCurpAcctType);
    setValue("id_p_seqno", hCardIdPSeqno);
    setValue("pay_card_no", hCurpCardNo);
    setValue("pay_date", hBillInterestDate.length() == 0 ? hBillPostDate : hBillInterestDate);
    setValue("curr_code", hCurpCurrCode);
    setValueDouble("dc_pay_amt", hCurpDcAmount);
    setValue("crt_user", "BIlA009");
    setValue("crt_date", hSystemDate);
    setValue("crt_time", hSystemTime);
    setValue("mod_user", hCurpReferenceNo);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", prgmId);
    daoTable = "act_pay_detail";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_act_pay_detail duplicate", hApbtBatchNo + "," + hApdlSerialNo,
          comcr.hCallBatchSeqno);
    }
  }

  /***********************************************************************/
  void searchCurrCode() {
    String tmpstr = "";

    if (debug)
    	showLogMessage("I", "", "  search curr idx=[" + currIdx + "]");
    
    for (int int1 = 1; int1 <= currIdx; int1++) {
      if (currArray[int1].equals(hCurpCurrCode)) {
        hApbtBatchNo = String.format("%14d", batchArray[int1]);
        seqArray[int1]++;
        tmpstr = String.format("%05d", seqArray[int1]);
        hApdlSerialNo = tmpstr;
        return;
      }
    }
  }

  /***********************************************************************/
  void insertActDebt() throws Exception {
    
	if (debug)
		showLogMessage("I", "", "  insert_act_debt=[" + 1 + "]");

    setValue("REFERENCE_NO", hCurpReferenceNo);
    setValue("p_seqno", hCurpPSeqno);
    setValue("acno_p_seqno", hCardAcnoPSeqno);
    setValue("acct_type", hCurpAcctType);
    setValue("post_date", hBusiBusinessDate);
    setValue("item_order_normal", hCurpItemOrderNormal);
    setValue("item_order_back_date", hCurpItemOrderBackDate);
    setValue("item_order_refund", hCurpItemOrderRefund);
    setValue("item_class_normal", hCurpItemClassNormal);
    setValue("item_class_back_date", hCurpItemClassBackDate);
    setValue("item_class_refund", hCurpItemClassRefund);
    setValue("acct_month", hBillAcctMonth);
    setValue("stmt_cycle", hCurpStmtCycle);
    setValue("bill_type", hCurpBillType);
    setValue("txn_code", hCurpTransactionCode);
    setValueDouble("beg_bal", hCurpCashPayAmt);
    setValueDouble("end_bal", hCurpCashPayAmt);
    setValueDouble("d_avail_bal", hCurpCashPayAmt);
    setValueDouble("can_by_fund_bal", hCurpCashPayAmt);
    setValue("card_no", hCurpCardNo);
    setValue("acct_code", hCurpAcctCode);
    setValue("interest_date", hBillInterestDate);
    setValue("purchase_date", hCurpPurchaseDate);
    setValue("merchant_no", hCurpMerchantNo);
    setValue("installment_kind", hCurpInstallmentKind);
    setValue("ptr_merchant_no", hCurpPtrMerchantNo);
    setValue("new_it_flag", hCurpNewItFlag);
    setValue("curr_code", hCurpCurrCode);
    setValueDouble("dc_beg_bal", hCurpDcAmount);
    setValueDouble("dc_end_bal", hCurpDcAmount);
    setValueDouble("dc_d_avail_bal", hCurpDcAmount);
    setValueDouble("dc_can_by_fund_bal", hCurpDcAmount);
    setValue("contract_no", hCurpContractNo);
    setValue("contract_seq_no", hCurpContractSeqNo);
    setValue("crt_time", hCurpBatchNo);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", prgmId);
    setValue("spec_flag",hTxlogSpecFlag);
    daoTable = "act_debt";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_act_debt duplicate", hCurpReferenceNo, comcr.hCallBatchSeqno);
    }
  }

  /**********************************************************************/
  void selectActAcct() throws Exception {
    hAcctAcctJrnlBal = 0;
    hAcctRowid = "";

    sqlCmd = "select acct_jrnl_bal as h_acct_acct_jrnl_bal,";
    sqlCmd += "rowid as rowid ";
    sqlCmd += " from act_acct  ";
    sqlCmd += "where p_seqno = ? ";
    setString(1, hCurpPSeqno);
    int tmpInt = selectTable();
    if (tmpInt > 0) {
      hAcctAcctJrnlBal = getValueDouble("h_acct_acct_jrnl_bal");
      hAcctRowid = getValue("rowid");
    }

    hAcurAcctJrnlBal = 0;
    hAcurDcAcctJrnlBal = 0;
    hAcurRowid = "";
    sqlCmd = "select acct_jrnl_bal as h_acur_acct_jrnl_bal,";
    sqlCmd += "dc_acct_jrnl_bal     as h_acur_dc_acct_jrnl_bal,";
    sqlCmd += "rowid  as rowid ";
    sqlCmd += " from act_acct_curr  ";
    sqlCmd += "where p_seqno   = ?  ";
    sqlCmd += "  and curr_code = ? ";
    setString(1, hCurpPSeqno);
    setString(2, hCurpCurrCode);
    tmpInt = selectTable();
    if (tmpInt > 0) {
      hAcurAcctJrnlBal = getValueDouble("h_acur_acct_jrnl_bal");
      hAcurDcAcctJrnlBal = getValueDouble("h_acur_dc_acct_jrnl_bal");
      hAcurRowid = getValue("rowid");
    }

  }

  /***********************************************************************/
  void insertActJrnl() throws Exception {
    
	if (debug)
		showLogMessage("I", "", "  insert_act_jrnl=[" + 1 + "]");

    hJrnlEnqSeqno++;
    setValue("crt_date", hSystemDate);
    setValue("crt_time", hSystemTime);
    setValueInt("enq_seqno", hJrnlEnqSeqno);
    setValue("p_seqno", hCurpPSeqno);
    setValue("acct_type", hCurpAcctType);
    setValue("id_p_seqno", hCardIdPSeqno);
    setValue("corp_p_seqno", hCardCorpPSeqno);
    setValue("corp_no", hCardCorpNo);
    setValue("corp_no_code", hCardCorpNoCode);
    setValue("acct_date", hBusiBusinessDate);
    setValue("tran_class", "B");
    setValue("tran_type", hCurpBillType);
    setValue("acct_code", hCurpAcctCode);
    setValue("dr_cr", "C");
    setValueDouble("transaction_amt", hCurpCashPayAmt);
    setValueDouble("jrnl_bal", hAcurAcctJrnlBal + hCurpCashPayAmt);
    setValueDouble("item_bal", hCurpCashPayAmt);
    setValueDouble("item_d_bal", hCurpCashPayAmt);
    setValueDouble("can_by_fund_bal", hCurpCashPayAmt);
    setValue("item_date", hBusiBusinessDate);
    setValue("interest_date", hBillInterestDate);
    setValue("adj_reason_code", hCurpTransactionCode);
    setValue("reference_no", hCurpReferenceNo);
    setValue("pay_id", hCurpCardNo);
    setValue("stmt_cycle", hCurpStmtCycle);
    setValue("mod_user", hCurpModUser);
    setValue("curr_code", hCurpCurrCode);
    setValueDouble("dc_transaction_amt", hCurpDcAmount);
    setValueDouble("dc_item_bal", hCurpDcAmount);
    setValueDouble("dc_item_d_bal", hCurpDcAmount);
    setValueDouble("dc_can_by_fund_bal", hCurpDcAmount);
    setValueDouble("dc_jrnl_bal", hAcurDcAcctJrnlBal + hCurpDcAmount);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", prgmId);
    daoTable = "act_jrnl";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_act_jrnl duplicate", "", hCurpReferenceNo);
    }
  }

  /**********************************************************************/
  void updateActAcct() throws Exception {
    
	if (debug)
		showLogMessage("I", "", "  update_act_acct =[" + "]");

    daoTable = "act_acct";
    updateSQL = " acct_jrnl_bal     = acct_jrnl_bal + ?,";
    updateSQL += " mod_time          = sysdate";
    whereStr = "where rowid        = ? ";
    setDouble(1, hCurpCashPayAmt);
    setRowId(2, hAcctRowid);
    updateTable();

    if (notFound.equals("Y")) {
    }

    if (debug)
    	showLogMessage("I", "", "  update act_acct_curr ");

    daoTable = "act_acct_curr";
    updateSQL = " dc_acct_jrnl_bal = dc_acct_jrnl_bal+? ,";
    updateSQL += " acct_jrnl_bal    = acct_jrnl_bal+? ,";
    updateSQL += " mod_time         = sysdate";
    whereStr = "where rowid       = ? ";
    setDouble(1, hCurpDcAmount);
    setDouble(2, hCurpCashPayAmt);
    setRowId(3, hAcurRowid);
    updateTable();

    if (notFound.equals("Y")) {
    }

  }

  /*********************************************************************
  * 保留授權比對到的spec_flag回寫bil_contract
  **********************************************************************/
  void updateBilContract() throws Exception {

    daoTable = "bil_contract";
    updateSQL = " spec_flag     = ?,";
    updateSQL += " mod_time          = sysdate";
    whereStr  = " where contract_no     = ? ";
    whereStr += "   and contract_seq_no = 1 ";
	setString(1, hTxlogSpecFlag);
    setString(2, hCurpContractNo);
    updateTable();

    if (notFound.equals("Y")) {
    }

  }
  /***********************************************************************/
  void selectPtrBilltype(String billType, String txnCode) throws Exception {
    hBityExterDesc = "";
    sqlCmd = "select exter_desc ";
    sqlCmd += "  from ptr_billtype  ";
    sqlCmd += " where bill_type = ?  ";
    sqlCmd += "   and txn_code  = ? ";
    setString(1, billType);
    setString(2, txnCode);
    int tmpInt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_billtype not found!", "", comcr.hCallBatchSeqno);
    }
    if (tmpInt > 0) {
      hBityExterDesc = getValue("exter_desc");
    }
  }

  /***********************************************************************/
  void selectBilMerchant(String iMerchantNo) throws Exception {
    hStmtInstFlag = "";
    sqlCmd = "select stmt_inst_flag ";
    sqlCmd += "  from bil_merchant  ";
    sqlCmd += " where mcht_no = ?  ";
    setString(1, iMerchantNo);
    int tmpInt = selectTable();
    if (notFound.equals("Y")) {
    }

    if (tmpInt > 0) {
      hStmtInstFlag = getValue("stmt_inst_flag");
    }
    
    if (debug)
    	showLogMessage("I", "", "  chk mcht=" + iMerchantNo + ",inst=" + hStmtInstFlag);
  }

  /***********************************************************************/
  void insertActPayBatch() throws Exception {
    
	if (debug)
		showLogMessage("I", "", "  insert_act_pay_batch=[" + currIdx + "]");
	  
    for (int int1 = 1; int1 <= currIdx; int1++) {
      hTempCurrCode = currArray[int1];
      hApbtBatchNo = String.format("%14d", batchArray[int1]);
      hApbtBatchTotCnt = (int) cntArray[int1];
      hApbtBatchTotAmt = amtArray[int1];

      setValue("batch_no", hApbtBatchNo);
      setValueInt("batch_tot_cnt", hApbtBatchTotCnt);
      setValueDouble("batch_tot_amt", hApbtBatchTotAmt);
      setValue("crt_user", hCurpModUser);
      setValue("crt_date", hBusiBusinessDate);
      setValue("crt_time", hSystemTime);
      setValue("trial_user", hCurpModUser);
      setValue("trial_date", hBusiBusinessDate);
      setValue("trial_time", hSystemTime);
      setValue("confirm_user", hCurpModUser);
      setValue("confirm_date", hBusiBusinessDate);
      setValue("confirm_time", hSystemTime);
      setValue("curr_code", hTempCurrCode);
      setValueDouble("dc_pay_amt", hApbtBatchTotAmt);
      setValue("mod_time", sysDate + sysTime);
      setValue("mod_pgm", prgmId);
      daoTable = "act_pay_batch";
      insertTable();
      if (dupRecord.equals("Y")) {
        comcr.errRtn("insert_act_pay_batch duplicate", "", comcr.hCallBatchSeqno);
      }
    }
  }

  /**********************************************************************/
  void insertOnbat(String status) throws Exception {
    String hOnbaTransType = "11";
    int hOnbaToWhich = 2;
    String hOnbaProcMode = "B";
    String hOnbaProcStatus = status;
    String hOnbaCardNo = hCurpCardNo;
    String hOnbaTransDate = hCurpPurchaseDate;
    String hOnbaAuthNo = hCurpAuthorization;
    double hOnbaTransAmt = hCurpDestinationAmt;
    if (hCurpDestinationAmt == 0) {
      hOnbaTransAmt = 1;
    }
    String hOnbaCardValidFrom = hCardNewBegDate;
    String hOnbaCardValidTo = hCardNewEndDate;
    String hOnbaContractNo = hCurpContractNo;
    String hOnbaProcDate = hSystemDate;
    String hOnbaTransCode = "01";
    if ((hCurpTransactionCode.equals("06")) || (hCurpTransactionCode.equals("25"))
        || (hCurpTransactionCode.equals("27")) || (hCurpTransactionCode.equals("28"))
        || (hCurpTransactionCode.equals("29")) || (hCurpTransactionCode.equals("66"))
        || (hCurpTransactionCode.equals("85")) || (hCurpTransactionCode.equals("87"))
        || (hCurpTransactionCode.equals("20"))) {
      hOnbaTransCode = "51";
    }

    String hOnbaRefeNo = hCurpReferenceNo;
    String hOnbaMccCode = hCurpMerchantCategory;

    setValue("trans_type", hOnbaTransType);
    setValueInt("to_which", hOnbaToWhich);
    setValue("dog", sysDate + sysTime);
    setValue("proc_mode", hOnbaProcMode);
    setValue("card_no", hOnbaCardNo);
    setValue("trans_date", hOnbaTransDate);
    setValue("mcc_code", hOnbaMccCode);
    setValue("trans_date", hOnbaTransDate);
    setValueDouble("trans_amt", hOnbaTransAmt);
    setValue("mcc_code", hOnbaMccCode);
    setValue("card_valid_from", hOnbaCardValidFrom);
    setValue("card_valid_to", hOnbaCardValidTo);
    setValue("proc_date", hOnbaProcDate);
    setValue("auth_no", hOnbaAuthNo);
    setValue("trans_code", hOnbaTransCode);
    setValue("refe_no", hOnbaRefeNo);
    setValue("contract_no", hOnbaContractNo);
    setValue("proc_status", hOnbaProcStatus);

    daoTable = "onbat_2ccas";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_onbat_2ccas duplicate", hOnbaRefeNo, comcr.hCallBatchSeqno);
    }
  }
  
	// ************************************************************************
	void loadPtrWorkday() throws Exception {
		extendField = "ptrworkday.";
		selectSQL = "stmt_cycle,next_close_date,next_acct_month,next_lastpay_date,this_acct_month ";
		daoTable = "ptr_workday";
		whereStr = "where 1=1 order by stmt_cycle ";

		int n = loadTable();
		setLoadData("ptrworkday.stmt_cycle");
		showLogMessage("I", "", "Load ptrWorkday Count: [" + n + "]");
	}

	// ************************************************************************
	void loadPtrActcode() throws Exception {
		extendField = "ptractcode.";
		selectSQL = "acct_code,interest_method ";
		daoTable = "ptr_actcode";
		whereStr = "where 1=1 order by acct_code ";

		int n = loadTable();
		setLoadData("ptractcode.acct_code");
		showLogMessage("I", "", "Load ptrActcode Count: [" + n + "]");
	}

	void getAuthParm() throws Exception {
		String sysId = "REPORT", sysKey = "";

		// --授權碼為000001或00000Y之比對限額
		sysKey = "AMT1";
		sqlCmd = "select sys_data1 as dbCardLimit from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
		setString(1, sysId);
		setString(2, sysKey);

		int recordCnt = selectTable();

		if (recordCnt > 0) {
			dbCardLimit = getValueDouble("dbCardLimit");
		} else {
			dbCardLimit = 20000;
		}

		// --金額下限百分比
		sysKey = "L_LIMIT";
		sqlCmd = "select sys_data1 as txAmtLowRate from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
		setString(1, sysId);
		setString(2, sysKey);

		recordCnt = selectTable();

		if (recordCnt > 0) {
			txAmtLowRate = getValueDouble("txAmtLowRate");
		} else {
			txAmtLowRate = 100;
		}

		// --金額上限百分比
		sysKey = "U_LIMIT";
		sqlCmd = "select sys_data1 as txAmtHighRate from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
		setString(1, sysId);
		setString(2, sysKey);

		recordCnt = selectTable();

		if (recordCnt > 0) {
			txAmtHighRate = getValueDouble("txAmtHighRate");
		} else {
			txAmtHighRate = 100;
		}

		// --授權碼不正確帳單比對上下限百分比
		sysKey = "RATE1";
		sqlCmd = "select sys_data1 as txAmtDiffRate from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
		setString(1, sysId);
		setString(2, sysKey);

		recordCnt = selectTable();

		if (recordCnt > 0) {
			txAmtDiffRate = getValueDouble("txAmtDiffRate");
		} else {
			txAmtDiffRate = 1;
		}
	}

	  /**********************************************************************
	   * 判斷是否需要執行授權比對 
	   * *********************************************************************/
	int procAuthCheckCondition() throws Exception {
		int chkAuthFlag = 0;

		// 費用交易不比對
		if ("FIFC".equals(hCurpBillType)) {
			chkAuthFlag = 1; // 不比對
			return chkAuthFlag;
		}
		
		//線上加檔或系統產生的交易不比對
		if ("OKOL".equals(hCurpBillType) || "OSSG".equals(hCurpBillType)) {
			chkAuthFlag = 1; // 不比對
			return chkAuthFlag;
		}

		// 正向交易才比對
		if ("+".equals(hCurpSignFlag)) {
			;
		} else {
			chkAuthFlag = 1; // 不比對
			return chkAuthFlag;
		}
		
		//列問交不比對(20231103)
		if (hCurpRskType.equals("1") == true
	        || hCurpRskType.equals("2") == true 
	        || hCurpRskType.equals("3") == true) {
			chkAuthFlag = 1; // 不比對
			return chkAuthFlag;
		}

		// 分期付款
		if (hCurpBillType.equals("OICU") && hCurpTransactionCode.equals("IN")) {

			// 第一期的交易才比對
			if (comcr.str2int(hCurpTerm) == 1 ) {
				; // 要比對
			} else {
				chkAuthFlag = 1; // 不比對
			}
		}
		
		// 票證的加值交易才比對
		if (hCurpBillType.equals("TSCC")) {
			if (hCurpMerchantNo.equals("EASY8003")) {
				;
			} else {
				chkAuthFlag = 1; // 不比對
			}
		}
		
		if (hCurpBillType.equals("IPSS")) {
			if (hCurpMerchantNo.equals("IPSS8003")) {
				;
			} else {
				chkAuthFlag = 1; // 不比對
			}
		}

		if (hCurpBillType.equals("ICAH")) {
			if (hCurpMerchantNo.equals("ICAH8003")) {
				;
			} else {
				chkAuthFlag = 1; // 不比對
			}
		}

		return chkAuthFlag;

	}
	
	/**********************************************************************
	 * 票證自動加值的授權比對, 只比一次
	 * *******************************************************************/
	  int checkAuthAddValue() throws Exception {
	    String hRowid = "";

	    //cacu_flag:專款專用註記
	    sqlCmd =   " select tx_seq , nt_amt, vd_lock_nt_amt, cacu_flag as spec_flag ,hex(rowid) as rowid from cca_auth_txlog where card_no = ? ";
	    sqlCmd += " and tx_date = ? and abs(nt_amt) = ? ";
	    sqlCmd += " and cacu_amount = 'Y' and mtch_flag not in ('U','Y') and nt_amt > 0  "; // 排除已比對到的

	    sqlCmd += commSqlStr.rownum(1);

	    setString(1, hCurpCardNo);
	    setString(2, hCurpPurchaseDate);
	    setDouble(3, hCurpDestinationAmt);

	    int r = selectTable();

	    // 授權記錄檔比不到
	    if (r <= 0) {
	    	hCurpRskType = "7";
	    	return 0;
	    }

	    hRowid = getValue("rowid");
	    hTxlogSpecFlag = getValue("spec_flag");

	    updateCcaAuthTxlog(hRowid);
	    
	    return 0;
	  }
	  
	  /***********************************************************************/
	  int checkAuth() throws Exception {
	    String lsHighDate = "", lsLowDate = "";
	    Double ldHighAmt = 0.0, ldLowAmt = 0.0;
	    String hRowid = "";

	    /*國內交易沒有模糊比對 */
	    if (hCurpMerchantCountry.length() >= 2 && hCurpMerchantCountry.substring(0,2).equalsIgnoreCase("TW")) {
	    	lsHighDate = hCurpPurchaseDate;
	    	lsLowDate = hCurpPurchaseDate;
	    	//分期付款
			if	(hCurpBillType.equals("OICU") && hCurpTransactionCode.equals("IN") ) {
				ldHighAmt = hCurpContractAmt;
		    	ldLowAmt = hCurpContractAmt;
			} else {
				ldHighAmt = hCurpDestinationAmt;
		    	ldLowAmt = hCurpDestinationAmt;
			}
	    } else {
	    	lsHighDate = hCurpPurchaseDate;
	    	lsLowDate = hCurpPurchaseDate;
	        //分期付款
			if	(hCurpBillType.equals("OICU") && hCurpTransactionCode.equals("IN") ) {
				ldHighAmt = Math.abs(hCurpContractAmt) * (1 + txAmtHighRate / 100);
		        ldLowAmt = Math.abs(hCurpContractAmt) * (1 - txAmtLowRate / 100);
			} else {
		        ldHighAmt = Math.abs(hCurpDestinationAmt) * (1 + txAmtHighRate / 100);
		        ldLowAmt = Math.abs(hCurpDestinationAmt) * (1 - txAmtLowRate / 100);
	    	}
	    }
	    

	    //cacu_flag:專款專用註記
	    sqlCmd =   " select tx_seq , nt_amt, vd_lock_nt_amt, cacu_flag as spec_flag ,hex(rowid) as rowid from cca_auth_txlog where card_no = ? and auth_no = ? ";
	    sqlCmd += " and tx_date >= ? and tx_date <= ? and abs(nt_amt) >= ? and abs(nt_amt) <= ? ";
	    sqlCmd += " and cacu_amount = 'Y' and mtch_flag not in ('U','Y') and nt_amt > 0  "; // 排除已比對到的

	    sqlCmd += commSqlStr.rownum(1);

	    setString(1, hCurpCardNo);
	    setString(2, hCurpAuthorization);
	    setString(3, lsLowDate);
	    setString(4, lsHighDate);
	    setDouble(5, ldLowAmt);
	    setDouble(6, ldHighAmt);

	    int r = selectTable();

	    // 授權記錄檔比不到
	    if (r <= 0) {
	      // do something 下一個比對
	      return 1;
	    }

	    hRowid = getValue("rowid");
	    hTxlogSpecFlag = getValue("spec_flag");

	    updateCcaAuthTxlog(hRowid);
	    
	    return 0;
	  }

	  /***********************************************************************
	   *第二次授權比對 :	授權日期與請款檔的<交易日期不一致>, 如住宿, 網路購物, <金額相符> 及 <授權碼相符>
	   * 
	   */
	  int checkAuth2() throws Exception {
	    String lsHighDate = "", lsLowDate = "";
	    Double ldHighAmt = 0.0, ldLowAmt = 0.0;
	    String hRowid = "";

	    lsHighDate = hCurpPurchaseDate;
	    lsLowDate = hCurpPurchaseDate;
	    
	    if	(hCurpBillType.equals("OICU") && hCurpTransactionCode.equals("IN") ) {
			ldHighAmt = hCurpContractAmt;
	    	ldLowAmt = hCurpContractAmt;
		} else {
			ldHighAmt = hCurpDestinationAmt;
		    ldLowAmt = hCurpDestinationAmt;
		}
	    
	    sqlCmd =
	        " select tx_seq , nt_amt, vd_lock_nt_amt, cacu_flag as spec_flag ,hex(rowid) as rowid from cca_auth_txlog where card_no = ? and auth_no = ? ";
	    sqlCmd += " and abs(nt_amt) >= ? and abs(nt_amt) <= ? ";
	    sqlCmd += " and cacu_amount = 'Y' and mtch_flag not in ('U','Y') and nt_amt > 0 ";

	    sqlCmd += commSqlStr.rownum(1);

	    setString(1, hCurpCardNo);
	    setString(2, hCurpAuthorization);
	    setDouble(3, ldLowAmt);
	    setDouble(4, ldHighAmt);

	    int r = selectTable();

	    // 授權記錄檔比不到
	    if (r <= 0) {
	      return 1;
	    }

	    hRowid = getValue("rowid");
	    hTxlogSpecFlag = getValue("spec_flag");

	    updateCcaAuthTxlog(hRowid);
	    
	    return 0;
	  }

	  /***********************************************************************
	   *第三次授權比對 :	(1)	自助加油交易: (mcc=5542),  <授權碼相符>
	   *                (2) 請款交易與授權交易的金額不同 <授權碼相符>
	   */
	  int checkAuth3() throws Exception {
	    String hRowid = "";

	    sqlCmd =   " select ref_no,tx_date,tx_seq , nt_amt, vd_lock_nt_amt, cacu_flag as spec_flag ,hex(rowid) as rowid from cca_auth_txlog where card_no = ? and auth_no = ? ";
	    if ("5542".equals(hCurpMerchantCategory)) {
	    	sqlCmd += " and (vd_lock_nt_amt = 1500 or vd_lock_nt_amt = 1 ) and  ( nt_amt = 1 or nt_amt =1500 ) ";
	    }
	    sqlCmd += " and cacu_amount = 'Y' and mtch_flag not in ('U','Y') and nt_amt > 0  ";
	    sqlCmd += commSqlStr.rownum(1);

	    setString(1, hCurpCardNo);
	    setString(2, hCurpAuthorization);

	    int r = selectTable();

	    // 授權記錄檔比不到
	    if (r <= 0) {
	    	hCurpRskType = "7";
	    	return 1;
	    }

	    showLogMessage("I", "", "  checkVdAuth3= [" +hCurpCardNo + "],[" + hCurpAuthorization + "],["+hCurpMerchantCategory+ "],["+getValueDouble("vd_lock_nt_amt") + "]");
	    
	    hRowid = getValue("rowid");
	    hTxlogSpecFlag = getValue("spec_flag");

	   	updateCcaAuthTxlog(hRowid);
	     
	    return 0;
	    
	  }	  
	  void updateCcaAuthTxlog(String lsRowid) throws Exception {

	    daoTable = "cca_auth_txlog";
	    updateSQL = "mtch_flag = 'Y' , ";
	    updateSQL += "mtch_date = ? ,";
	    updateSQL += "cacu_amount = 'M', ";   
	    updateSQL += "mod_user = ? , ";
	    updateSQL += "mod_pgm = ? , ";
	    updateSQL += "mod_time = sysdate ";
	    whereStr = "where rowid = ? ";
	    setString(1, hBusiBusinessDate);
	    setString(2, "system");
	    setString(3, prgmId);
	    setRowId(4, lsRowid);
	    updateTable();
	  }
	  
	/*
G_Ps4AuthTxLog (2,
===================================================================

select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,
NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,
NVL(REF_NO,' ') as AuthTxLogRefNo,NVL(NT_AMT,0) as AuthTxLogNtAmt, CARD_NO as AuthTxLogCardNo, 
AUTH_NO as AuthTxLogAuthNo, NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,
NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag, 
NVL(TRACE_NO,'') as AuthTxLogTraceNo, NVL(TX_TIME,'') as AuthTxLogTxTime from CCA_AUTH_TXLOG  
WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND TX_DATE= ? and NT_AMT<= ? AND mtch_flag not in ('U','Y')  
FETCH FIRST ? ROWS ONLY

授權碼為000001或00000Y之比對限額   $25000




G_Ps4AuthTxLog2 : (31,
===================================
select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,
CARD_NO as AuthTxLogCardNo, AUTH_NO as AuthTxLogAuthNo, TX_TIME as AuthTxLogTxTime, 
TRACE_NO as AuthTxLogTraceNo, NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,
NVL(REF_NO,' ') as AuthTxLogRefNo,NVL(NT_AMT,0) as AuthTxLogNtAmt,NVL(RISK_TYPE,'*') as AuthTxLogRiskType,
NVL(PROC_CODE,'*') as AuthTxLogProcCode,NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,
NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag from CCA_AUTH_TXLOG  
WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND TX_DATE= ? and NT_AMT= ? and AUTH_NO= ? AND mtch_flag not in ('U','Y')   
FETCH FIRST 1 ROWS ONLY


**完全比對 卡號/交易日/授權碼/金額


G_Ps4AuthTxLog3 : (32
==============================================================================================================
select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,
NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,
NVL(REF_NO,' ') as AuthTxLogRefNo,CARD_NO as AuthTxLogCardNo, AUTH_NO as AuthTxLogAuthNo, TRACE_NO as AuthTxLogTraceNo, 
TX_TIME as AuthTxLogTxTime, NVL(NT_AMT,0) as AuthTxLogNtAmt,NVL(RISK_TYPE,'*') as AuthTxLogRiskType,
NVL(PROC_CODE,'*') as AuthTxLogProcCode,NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,
NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag from CCA_AUTH_TXLOG  
WHERE CARD_NO= ? AND CACU_AMOUNT= ? and AUTH_NO= ? AND (NT_AMT>= ? AND NT_AMT<=? ) AND mtch_flag not in ('U','Y')   
FETCH FIRST ? ROWS ONLY 


**模糊比對 卡號/交易日/授權碼/金額
授權碼相符帳單比對下限百分比  80％ 
授權碼相符帳單比對上限百分比 130％


G_Ps4AuthTxLog4 (41
=================================================================================================================
select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,
NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,CARD_NO as AuthTxLogCardNo, 
AUTH_NO as AuthTxLogAuthNo,NVL(TRACE_NO,'') as AuthTxLogTraceNo, NVL(TX_TIME,'') as AuthTxLogTxTime, 
NVL(REF_NO,' ') as AuthTxLogRefNo,NVL(NT_AMT,0) as AuthTxLogNtAmt,NVL(RISK_TYPE,'*') as AuthTxLogRiskType,
NVL(PROC_CODE,'*') as AuthTxLogProcCode,NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,
NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag from CCA_AUTH_TXLOG  
WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND NT_AMT= ? AND mtch_flag not in ('U','Y')  
FETCH FIRST ? ROWS ONLY

**完全比對 卡號/金額


G_Ps4AuthTxLog5 (42
=================================================================================================================
select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,
NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,CARD_NO as AuthTxLogCardNo, 
AUTH_NO as AuthTxLogAuthNo,NVL(TRACE_NO,'') as AuthTxLogTraceNo, NVL(TX_TIME,'') as AuthTxLogTxTime, 
NVL(REF_NO,' ') as AuthTxLogRefNo,NVL(NT_AMT,0) as AuthTxLogNtAmt,NVL(RISK_TYPE,'*') as AuthTxLogRiskType,
NVL(PROC_CODE,'*') as AuthTxLogProcCode,NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,
NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag from CCA_AUTH_TXLOG  
WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND (NT_AMT>= ? AND NT_AMT<= ? ) AND mtch_flag not in ('U','Y')   
FETCH FIRST ? ROWS ONLY 


**模糊比對 卡號/金額
授權碼不正確帳單比對上下限百分比 8％ 	 
	 
	 
	 */
	
  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    BilA009 proc = new BilA009();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
  /***********************************************************************/
}
