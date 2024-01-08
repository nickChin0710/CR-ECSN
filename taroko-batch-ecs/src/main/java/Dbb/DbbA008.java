/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/07/03  V1.00.01    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V1.00.02    shiyuqi   coding standard,						  *													
*  109/09/19  V1.00.03    JeffKung if matched auth, use auth_nt_amt to instead of dest_amt 
*  109/12/24  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
*  111/03/28  V1.00.05    JeffKung 請款一律入帳,問交記錄僅為參考        *
*  111/11/21  V1.00.06    JeffKung  帳務月份一律放下次結帳帳務月份    *
******************************************************************************/

package Dbb;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*當日入帳檔轉入帳單檔作業*/
public class DbbA008 extends AccessDAO {
  private final String progname = "當日入帳檔轉入帳單檔作業  111/11/21 V1.00.06";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  String hTempUser = "";

  final int DEBUG = 0;

  String hCallBatchSeqno = "";

  String hBusinssChiDate = "";
  String hVouchChiDate = "";
  String hBusiBusinessDate = "";
  String hSystemDate = "";
  String hSystemTime = "";
  String hSystemDateFull = "";
  String hDcurReferenceNo = "";
  String hDcurBillType = "";
  String hDcurTxnCode = "";
  String hDcurSignFlag = "";
  String hDcurCardNo = "";
  String hDcurFilmNo = "";
  String hDcurAcqMemberId = "";
  String hDcurPurchaseDate = "";
  double hDcurDestAmt = 0;
  String hDcurDestCurr = "";
  double hDcurSourceAmt = 0;
  String hDcurSourceCurr = "";
  String hDcurMchtEngName = "";
  String hDcurMchtCity = "";
  String hDcurMchtCountry = "";
  String hDcurMchtCategory = "";
  String hDcurTxSeq = "";
  String hDcurSettlFlag = "";
  String hDcurEcsPlatformKind = ""; 
  String hDcurEcsCusMchtNo = "";  
  String hDcurMchtZip = "";
  String hDcurMchtState = "";
  String hDcurTmpRequestFlag = "";
  String hDcurUsageCode = "";
  String hDcurReasonCode = "";
  String hDcurSettlementFlag = "";
  double hDcurSettlAmt = 0;
  String hDcurTmpServiceCode = "";
  String hDcurAuthCode = "";
  String hDcurPosTermCapability = "";
  String hDcurPosPinCapability = "";
  String hDcurPosEntryMode = "";
  String hDcurProcessDate = "";
  String hDcurReimbursementAttr = "";
  String hDcurEcInd = "";
  String hDcurSecondConversionDate = "";
  String hDcurMchtNo = "";
  String hDcurMchtChiName = "";
  String hDcurElectronicTermInd = "";
  String hDcurTransactionSource = "";
  String hDcurAcquireDate = "";
  String hDcurContractNo = "";
  String hDcurContractSeqNo = "";
  double hDcurContractAmt = 0;
  String hDcurOriginalNo = "";
  String hDcurBatchNo = "";
  String hDcurExchangeRate = "";
  String hDcurExchangeDate = "";
  String hDcurAcctCode = "";
  String hDcurAcctItem = "";
  String hDcurAcctEngShortName = "";
  String hDcurAcctChiShortName = "";
  String hDcurItemOrderNormal = "";
  String hDcurItemOrderBackDate = "";
  String hDcurItemOrderRefund = "";
  String hDcurItemClassNormal = "";
  String hDcurItemClassBackDate = "";
  String hDcurItemClassRefund = "";
  String hDcurInterestMode = "";
  String hDcurInterestRskDate = "";
  String hDcurAdvWkday = "";
  String hDcurCollectionMode = "";
  String hDcurCashAdvState = "";
  String hDbilPostDate = "";
  String hDcurValidFlag = "";
  String hDcurDoubtType = "";
  String hDcurDuplicatedFlag = "";
  String hDcurRskType = "";
  String hDcurAcctType = "";
  String hDcurStmtCycle = "";
  String hDcurMajorCardNo = "";
  String hDcurIssueDate = "";
  String hDcurPromoteDept = "";
  String hDcurProdNo = "";
  String hDcurGroupCode = "";
  String hDcurBinType = "";
  String hDcurPSeqno = "";
  String hDcurAcexterDesc = "";
  String hDcurSourceCode = "";
  String hDcurQueryType = "";
  String hDcurFloorLimit = "";
  String hDcurReferenceNoOriginal = "";
  String hDcurFeesReferenceNo = "";
  String hDcurReferenceNoFeeF = "";
  String hDcurTxConvtFlag = "";
  String hDcurLimitEndDate = "";
  String hDcurChipConditionCode = "";
  String hDcurAuthResponseCode = "";
  String hDcurTransactionType = "";
  String hDcurTerminalVerResults = "";
  String hDcurIadResult = "";
  String hDcurCardSeqNum = "";
  String hDcurUnpredicNum = "";
  String hDcurAppTranCount = "";
  String hDcurAppIntPro = "";
  String hDcurCryptogram = "";
  String hDcurDerKeyIndex = "";
  String hDcurCryVerNum = "";
  String hDcurDataAuthCode = "";
  String hDcurCryInfoData = "";
  String hDcurTerminalCapPro = "";
  String hDcurLifeCycSupInd = "";
  String hDcurBanknetDate = "";
  String hDcurInterRateDes = "";
  String hDcurExpirDate = "";
  String hDcurPaymentType = "";
  double hDcurCurrTxAmount = 0;
  String hDcurInstallTotTerm1 = "";
  double hDcurInstallFirstAmt = 0;
  double hDcurInstallPerAmt = 0;
  String hDcurInstallFee = "";
  String hDcurDeductBp = "";
  double hDcurCashPayAmt = 0;
  double hTtttOriAmt = 0;
  double hDcurAmtMccrNum = 0;
  double hDcurAmtIccrNum = 0;
  double hDcurIssueFee = 0;
  double hDcurIncludeFeeAmt = 0;
  String hDcurUcaf = "";
  String hDcurIssueSR = "";
  String hDcurRskTypeSpecial = "";
  String hDcurMcsNum = "";
  int hDcurMcsCnt = 0;
  String hDcurTermType = "";
  String hDcurWalletIden = "";
  String hDcurAcceptTermInd = "";
  String hDcurMchtZipTw = "";
  String hDcurMchtType = "";
  String hDcurRowid = "";
  double hReserveAmt = 0;
  String hTempType = "";
  String hTempReferenceNo = "";
  String hDbilAcctMonth = "";
  String hDbilInterestDate = "";
  String hCardRegBankNo = "";
  String hCardComboAcctNo = "";
  String hCardIdPSeqno = "";
  String hCardMajorIdPSeqno = "";
  String hCardOriCardNo = "";
  String hCardBankActno = "";
  String hDaajCreateDate = "";
  String hDaajCreateTime = "";
  String hDaajPSeqno = "";
  String hDaajAcctType = "";
  String hDaajAdjustType = "";
  String hDaajReferenceNo = "";
  String hDaajPostDate = "";
  double hDaajOrginalAmt = 0;
  double hDaajDrAmt = 0;
  double hDaajCrAmt = 0;
  double hDaajBefAmt = 0;
  double hDaajAftAmt = 0;
  double hDaajBefDAmt = 0;
  double hDaajAftDAmt = 0;
  String hDaajAcctCode = "";
  String hDaajFunctionCode = "";
  String hDaajCardNo = "";
  String hDaajCashType = "";
  String hDaajValueType = "";
  String hDaajTransAcctType = "";
  String hDaajTransAcctKey = "";
  String hDaajInterestDate = "";
  String hDaajAdjReasonCode = "";
  String hDaajAdjComment = "";
  String hDaajCDebtKey = "";
  String hDaajDebitItem = "";
  String hDaajConfirmFlag = "";
  String hDaajJrnlDate = "";
  String hDaajJrnlTime = "";
  String hDaajPaymentType = "";
  String hDaajUpdateDate = "";
  String hDaajUpdateUser = "";
  String hDaajProcessFlag = "";
  String hCardGroupCode = "";
  String hCardGpNo = "";
  String hVouchCdKind = "";
  String hTAcNo = "";
  int hTSeqno = 0;
  String hTDbcr = "";
  String hTMemo3Kind = "";
  String hTMemo3Flag = "";
  String hTDrFlag = "";
  String hTCrFlag = "";
  String hTtAcNo = "";
  String hRBillType = "";
  String hRMchtNo = "";
  String hRMchtName = "";
  double hRTotRecord = 0;
  double hRTotAmount = 0;
  double hRTotPost = 0;
  double hRTotExchange = 0;
  double hRTotAmtP = 0;
  double hRTotAmtM = 0;
  int hDajlEnqSeqno = 0;
  String hCardCorpPSeqno = "";
  String hDcurModUser = "";
  String hPrintName = "";
  String hRptName = "";
  String hWdayStmtCycle = "";
  String hWdayNextCloseDate = "";
  String hWdayNextAcctMonth = "";
  String hWdayNextLastpayDate = "";
  String hWdayThisAcctMonth = "";
  String[] hMTempAcNo = new String[250];
  String hPcodInterestMethod = "";
  String hDadtPSeqno = "";
  String hDadtAcctType = "";
  double hDadtBegBal = 0;
  double hDadtEndBal = 0;
  double hDadtDAvailBal = 0;
  String hDadtAcctCode = "";
  String hDadtInterestDate = "";
  String hDaajTxnCode = "";
  String hDaajMchtNo = "";
  String hDcurTotalTerm = "";
  String hDcurTerm = "";
  int hDbilInstallTotTerm = 0;
  int hDbilInstallCurrTerm = 0;

  String[] aWdayStmtCycle = new String[250];
  String[] aWdayNextCloseDate = new String[250];
  String[] aWdayNextAcctMonth = new String[250];
  String[] aWdayNextLastpayDate = new String[250];
  String[] aWdayThisAcctMonth = new String[250];
  String[] aPcodAcctCode = new String[250];
  String[] aPcodInterestMethod = new String[250];

  int totalCount = 0;
  double totalAmt = 0;
  int ptrActcodeCnt = 0;
  int nCycleCnt = 0;

  // ***********************************************************

  public int mainProcess(String[] args) {
    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);
      // =====================================

      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
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
        int recCnt = selectTable();
        hTempUser = getValue("user_id");
      }
      
      if (hTempUser.length() == 0) {
        hTempUser = comc.commGetUserID();
      }

      selectPtrBusinday();

      selectPtrWorkday();
      selectPtrActcode();
      selectDbbCurpost();

      if (totalCount > 0)
        updateDbbCurpost();

      // ==============================================
      // 固定要做的

      comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCount + "]";
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

  /***********************************************************************/
  void selectPtrBusinday() throws Exception {
    hSystemDateFull = "";

    sqlCmd =
        "select substr(to_char(to_number(business_date)- 19110000,'0000000'),2,7) h_businss_chi_date,";
    sqlCmd += "substr(to_char(to_number(vouch_date)- 19110000,'0000000'),2,7) h_vouch_chi_date,";
    sqlCmd += "business_date,";
    sqlCmd += "to_char(sysdate,'yyyymmdd') h_system_date,";
    sqlCmd += "to_char(sysdate,'hh24miss') h_system_time,";
    sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_full ";
    sqlCmd += " from ptr_businday ";
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
    }
    if (recordCnt > 0) {
      hBusinssChiDate = getValue("h_businss_chi_date");
      hVouchChiDate = getValue("h_vouch_chi_date");
      hBusiBusinessDate = getValue("business_date");
      hSystemDate = getValue("h_system_date");
      hSystemTime = getValue("h_system_time");
      hSystemDateFull = getValue("h_system_date_full");
    }
  }

  /***********************************************************************/
  void selectDbbCurpost() throws Exception {

    sqlCmd = "select ";
    sqlCmd += "dbb_curpost.reference_no,";
    sqlCmd += "dbb_curpost.bill_type,";
    sqlCmd += "dbb_curpost.txn_code,";
    sqlCmd += "dbb_curpost.sign_flag,";
    sqlCmd += "dbb_curpost.card_no,";
    sqlCmd += "film_no,";
    sqlCmd += "acq_member_id,";
    sqlCmd += "purchase_date,";
    sqlCmd += "dest_amt ,";  
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
    sqlCmd += "settlement_flag,";
    sqlCmd += "settl_amt,";
    sqlCmd += "tmp_service_code,";
    sqlCmd += "auth_code,";
    sqlCmd += "pos_term_capability,";
    sqlCmd += "pos_pin_capability,";
    sqlCmd += "dbb_curpost.pos_entry_mode,";
    sqlCmd += "process_date,";
    sqlCmd += "reimbursement_attr,";
    sqlCmd += "dbb_curpost.ec_ind,";
    sqlCmd += "second_conversion_date,";
    sqlCmd += "dbb_curpost.mcht_no,";
    sqlCmd += "mcht_chi_name,";
    sqlCmd += "electronic_term_ind,";
    sqlCmd += "transaction_source,";
    sqlCmd += "acquire_date,";
    sqlCmd += "contract_no,";
    sqlCmd += "contract_seq_no,";
    sqlCmd += "contract_amt,";
    sqlCmd += "original_no,";
    sqlCmd += "term,";
    sqlCmd += "total_term,";
    sqlCmd += "dbb_curpost.batch_no,";
    sqlCmd += "exchange_rate,";
    sqlCmd += "exchange_date,";
    sqlCmd += "dbb_curpost.acct_code,";
    sqlCmd += "dbb_curpost.acct_item,";
    sqlCmd += "acct_eng_short_name,";
    sqlCmd += "acct_chi_short_name,";
    sqlCmd += "item_order_normal,";
    sqlCmd += "item_order_back_date,";
    sqlCmd += "item_order_refund,";
    sqlCmd += "item_class_normal,";
    sqlCmd += "item_class_back_date,";
    sqlCmd += "item_class_refund,";
    sqlCmd += "decode(ptr_billtype.interest_mode,'','1',ptr_billtype.interest_mode), ";
    sqlCmd += "ptr_billtype.adv_wkday,";
    sqlCmd += "collection_mode,";
    sqlCmd += "dbb_curpost.cash_adv_state,";
    sqlCmd += "decode(dbb_curpost.this_close_date,'', ?,dbb_curpost.this_close_date) h_dbil_post_date,";
    sqlCmd += "valid_flag,";
    sqlCmd += "doubt_type,";
    sqlCmd += "duplicated_flag,";
    sqlCmd += "rsk_type,";
    sqlCmd += "acct_type,";
    sqlCmd += "dbb_curpost.stmt_cycle,";
    sqlCmd += "major_card_no,";
    sqlCmd += "issue_date,";
    sqlCmd += "promote_dept,";
    sqlCmd += "prod_no,";
    sqlCmd += "group_code,";
    sqlCmd += "bin_type,";
    sqlCmd += "p_seqno,";
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
    sqlCmd += "payment_type,";
    sqlCmd += "curr_tx_amount,";
    sqlCmd += "nvl(install_tot_term1,0) h_dcur_install_tot_term1,";
    sqlCmd += "nvl(install_first_amt,0) h_dcur_install_first_amt,";
    sqlCmd += "nvl(install_per_amt,0) h_dcur_install_per_amt,";
    sqlCmd += "nvl(install_fee,0) h_dcur_install_fee,";
    sqlCmd += "nvl(deduct_bp,0) h_dcur_deduct_bp,";
    sqlCmd += "nvl(cash_pay_amt,0) h_dcur_cash_pay_amt,";
    sqlCmd += "decode(nvl(ori_amt ,0),0,dest_amt,ori_amt) hTttt_ori_amt,";
    sqlCmd += "amt_mccr_num,";
    sqlCmd += "amt_iccr_num,";
    sqlCmd += "dbb_curpost.issue_fee,";
    sqlCmd += "dbb_curpost.include_fee_amt,";
    sqlCmd += "dbb_curpost.ucaf,";
    sqlCmd += "issue_s_r,";
    sqlCmd += "rsk_type_special,";
    sqlCmd += "dbb_curpost.mcs_num,";
    sqlCmd += "dbb_curpost.mcs_cnt,";
    sqlCmd += "term_type,";
    sqlCmd += "wallet_iden,";
    sqlCmd += "accept_term_ind,";
    sqlCmd += "mcht_zip_tw,";
    sqlCmd += "mcht_type,";
    sqlCmd += "mcht_category,";
    sqlCmd += "dbb_curpost.auth_nt_amt,";
    sqlCmd += "dbb_curpost.tx_seq,";
    sqlCmd += "dbb_curpost.SETTL_FLAG,";  //V1.00.06增加欄位
    sqlCmd += "dbb_curpost.ECS_PLATFORM_KIND,";  //V1.00.06增加欄位
    sqlCmd += "dbb_curpost.ECS_CUS_MCHT_NO,";  //V1.00.06增加欄位
    sqlCmd += "dbb_curpost.rowid as rowid ";
    sqlCmd += "from dbb_curpost ";
    sqlCmd += " inner join bil_postcntl   on DBB_CURPOST.BATCH_NO = BIL_POSTCNTL.BATCH_NO ";
    sqlCmd += " left join bil_nccc300_dtl on DBB_CURPOST.REFERENCE_NO = BIL_NCCC300_DTL.REFERENCE_NO ";
    sqlCmd += " inner join PTR_BILLTYPE   on PTR_BILLTYPE.bill_type = DBB_CURPOST.bill_type and PTR_BILLTYPE.txn_code = DBB_CURPOST.txn_code ";
    sqlCmd += "where 1=1 ";
    sqlCmd += "and upper(decode(dbb_curpost.entry_acct,'','N',dbb_curpost.entry_acct)) = 'Y' ";
    sqlCmd += "and upper(decode(confirm_flag_p,'','N',confirm_flag_p))='Y' ";
    sqlCmd += "and ((decode(dbb_curpost.fees_state,'','N',dbb_curpost.fees_state)  in ('P','N') or ";
    sqlCmd += "        (decode(dbb_curpost.fees_state,'','N',dbb_curpost.fees_state)  in ('Y','y') and ";
    sqlCmd += "          decode(rsk_type,'' ,'N',rsk_type) not in ('N','4') ) ) and ";
    sqlCmd += "        (decode(dbb_curpost.cash_adv_state,'','N',dbb_curpost.cash_adv_state) in ('P','N') or ";
    sqlCmd += "        (decode(dbb_curpost.cash_adv_state,'','N',dbb_curpost.cash_adv_state) in ('Y','y') and ";
    sqlCmd += "          decode(rsk_type,'' ,'N',rsk_type) not in ('N','4') ) ) ) ";
    sqlCmd += "and decode(manual_upd_flag,'','N',manual_upd_flag) != 'Y' ";
    sqlCmd += "and decode(valid_flag,'','N',valid_flag)  != 'Y' ";
    sqlCmd += "and decode(curr_post_flag,'','N',curr_post_flag) in ('N','n') ";
    sqlCmd += "and ( (decode(format_chk_ok_flag,'','N',format_chk_ok_flag)  in ('N','n') ";
    sqlCmd += "          and decode(double_chk_ok_flag,'','N',double_chk_ok_flag) in ('N','n') ";
    sqlCmd += "          and decode(err_chk_ok_flag,'' ,'N',err_chk_ok_flag) in ('N','n') )  ";
    sqlCmd += "    or ((decode(double_chk_ok_flag,'','N',double_chk_ok_flag)  in ('Y','y') ";
    sqlCmd += "      or decode(err_chk_ok_flag,'','N',err_chk_ok_flag) in ('Y','y') ) ";
    sqlCmd += "      and rsk_type <>'' ) ) ";
    sqlCmd += "order by dbb_curpost.batch_no ";
    setString(1, hBusiBusinessDate);
    openCursor();
    while (fetchTable()) {
      hDcurReferenceNo = getValue("reference_no");
      hDcurBillType = getValue("bill_type");
      hDcurTxnCode = getValue("txn_code");
      hDcurSignFlag = getValue("sign_flag");
      hDcurCardNo = getValue("card_no");
      hDcurFilmNo = getValue("film_no");
      hDcurAcqMemberId = getValue("acq_member_id");
      hDcurPurchaseDate = getValue("purchase_date");
      hDcurDestAmt = getValueDouble("dest_amt");
      hDcurDestCurr = getValue("dest_curr");
      hDcurSourceAmt = getValueDouble("source_amt");
      hDcurSourceCurr = getValue("source_curr");
      hDcurMchtEngName = getValue("mcht_eng_name");
      hDcurMchtCity = getValue("mcht_city");
      hDcurMchtCountry = getValue("mcht_country");
      hDcurMchtCategory = getValue("mcht_category");
      hDcurMchtZip = getValue("mcht_zip");
      hDcurMchtState = getValue("mcht_state");
      hDcurTmpRequestFlag = getValue("tmp_request_flag");
      hDcurUsageCode = getValue("usage_code");
      hDcurReasonCode = getValue("reason_code");
      hDcurSettlementFlag = getValue("settlement_flag");
      hDcurSettlAmt = getValueDouble("settl_amt");
      hDcurTmpServiceCode = getValue("tmp_service_code");
      hDcurAuthCode = getValue("auth_code");
      hDcurPosTermCapability = getValue("pos_term_capability");
      hDcurPosPinCapability = getValue("pos_pin_capability");
      hDcurPosEntryMode = getValue("pos_entry_mode");
      hDcurProcessDate = getValue("process_date");
      hDcurReimbursementAttr = getValue("reimbursement_attr");
      hDcurEcInd = getValue("ec_ind");
      hDcurSecondConversionDate = getValue("second_conversion_date");
      hDcurMchtNo = getValue("mcht_no");
      hDcurMchtChiName = getValue("mcht_chi_name");
      hDcurElectronicTermInd = getValue("electronic_term_ind");
      hDcurTransactionSource = getValue("transaction_source");
      hDcurAcquireDate = getValue("acquire_date");
      hDcurContractNo = getValue("contract_no");
      hDcurContractSeqNo = getValue("contract_seq_no");
      hDcurContractAmt = getValueDouble("contract_amt");
      hDcurOriginalNo = getValue("original_no");
      hDcurTerm = getValue("term");
      hDcurTotalTerm = getValue("total_term");
      hDcurBatchNo = getValue("batch_no");
      hDcurExchangeRate = getValue("exchange_rate");
      hDcurExchangeDate = getValue("exchange_date");
      hDcurAcctCode = getValue("acct_code");
      hDcurAcctItem = getValue("acct_item");
      hDcurAcctEngShortName = getValue("acct_eng_short_name");
      hDcurAcctChiShortName = getValue("acct_chi_short_name");
      hDcurItemOrderNormal = getValue("item_order_normal");
      hDcurItemOrderBackDate = getValue("item_order_back_date");
      hDcurItemOrderRefund = getValue("item_order_refund");
      hDcurItemClassNormal = getValue("item_class_normal");
      hDcurItemClassBackDate = getValue("item_class_back_date");
      hDcurItemClassRefund = getValue("item_class_refund");
      hDcurInterestMode = getValue("interest_mode");
      hDcurAdvWkday = getValue("adv_wkday");
      hDcurCollectionMode = getValue("collection_mode");
      hDcurCashAdvState = getValue("cash_adv_state");
      hDbilPostDate = getValue("h_dbil_post_date");
      hDcurValidFlag = getValue("valid_flag");
      hDcurDoubtType = getValue("doubt_type");
      hDcurDuplicatedFlag = getValue("duplicated_flag");
      hDcurRskType = getValue("rsk_type");
      hDcurAcctType = getValue("acct_type");
      hDcurStmtCycle = getValue("stmt_cycle");
      hDcurMajorCardNo = getValue("major_card_no");
      hDcurIssueDate = getValue("issue_date");
      hDcurPromoteDept = getValue("promote_dept");
      hDcurProdNo = getValue("prod_no");
      hDcurGroupCode = getValue("group_code");
      hDcurBinType = getValue("bin_type");
      hDcurPSeqno = getValue("p_seqno");
      hDcurAcexterDesc = getValue("acexter_desc");
      hDcurSourceCode = getValue("source_code");
      hDcurQueryType = getValue("query_type");
      hDcurFloorLimit = getValue("floor_limit");
      hDcurReferenceNoOriginal = getValue("reference_no_original");
      hDcurFeesReferenceNo = getValue("fees_reference_no");
      hDcurReferenceNoFeeF = getValue("reference_no_fee_f");
      hDcurTxConvtFlag = getValue("tx_convt_flag");
      hDcurLimitEndDate = getValue("limit_end_date");
      hDcurChipConditionCode = getValue("chip_condition_code");
      hDcurAuthResponseCode = getValue("auth_response_code"); 
      hDcurTransactionType = getValue("transaction_type");
      hDcurTerminalVerResults = getValue("terminal_ver_results");
      hDcurIadResult = getValue("iad_result");
      hDcurCardSeqNum = getValue("card_seq_num");
      hDcurUnpredicNum = getValue("unpredic_num");
      hDcurAppTranCount = getValue("app_tran_count");
      hDcurAppIntPro = getValue("app_int_pro");
      hDcurCryptogram = getValue("cryptogram");
      hDcurDerKeyIndex = getValue("der_key_index");
      hDcurCryVerNum = getValue("cry_ver_num");
      hDcurDataAuthCode = getValue("data_auth_code");
      hDcurCryInfoData = getValue("cry_info_data");
      hDcurTerminalCapPro = getValue("terminal_cap_pro");
      hDcurLifeCycSupInd = getValue("life_cyc_sup_ind");
      hDcurBanknetDate = getValue("banknet_date");
      hDcurInterRateDes = getValue("inter_rate_des");
      hDcurExpirDate = getValue("expir_date");
      hDcurPaymentType = getValue("payment_type");
      hDcurCurrTxAmount = getValueDouble("curr_tx_amount");
      hDcurInstallTotTerm1 = getValue("h_dcur_install_tot_term1");
      hDcurInstallFirstAmt = getValueDouble("h_dcur_install_first_amt");
      hDcurInstallPerAmt = getValueDouble("h_dcur_install_per_amt");
      hDcurInstallFee = getValue("h_dcur_install_fee");
      hDcurDeductBp = getValue("h_dcur_deduct_bp");
      hDcurCashPayAmt = getValueDouble("h_dcur_cash_pay_amt");
      hTtttOriAmt = getValueDouble("hTttt_ori_amt");
      hDcurAmtMccrNum = getValueDouble("amt_mccr_num");
      hDcurAmtIccrNum = getValueDouble("amt_iccr_num");
      hDcurIssueFee = getValueDouble("issue_fee");
      hDcurIncludeFeeAmt = getValueDouble("include_fee_amt");
      hDcurUcaf = getValue("ucaf");
      hDcurIssueSR = getValue("issue_s_r");
      hDcurRskTypeSpecial = getValue("rsk_type_special");
      hDcurMcsNum = getValue("mcs_num");
      hDcurMcsCnt = getValueInt("mcs_cnt");
      hDcurTermType = getValue("term_type");
      hDcurWalletIden = getValue("wallet_iden");
      hDcurAcceptTermInd = getValue("accept_term_ind");
      hDcurMchtZipTw = getValue("mcht_zip_tw");
      hDcurMchtType = getValue("mcht_type");
      hDcurMchtCategory = getValue("mcht_category");
      hReserveAmt = getValueDouble("auth_nt_amt");
      //以授權的圈存金額寫入帳單
      if (hReserveAmt>0)  
      {
    	  hDcurDestAmt = hReserveAmt;
      }
      
      hDcurTxSeq = getValue("tx_seq");
      
      hDcurSettlFlag = getValue("settl_flag");
      hDcurEcsPlatformKind = getValue("ecs_platform_kind");
      hDcurEcsCusMchtNo = getValue("ecs_cus_mcht_no");
      
      hDcurRowid = getValue("rowid");

      totalCount = totalCount + 1;
      if (totalCount % 5000 == 0 || totalCount == 1) {
        showLogMessage("I", "", String.format("Process record=[%d]", totalCount));
      }

      if (hDcurInterestMode.length() < 1)
        hDcurInterestMode = "1";

      //showLogMessage("D", "", "888 select main=[" + totalCount + "]" + hDcurBatchNo);
      //showLogMessage("D", "", "    TYPE=[" + hDcurBillType + "]" + hDcurCardNo);

      searchPtrWorkday();

      hDbilInterestDate = "";
      switch (hDcurInterestMode.toCharArray()[0]) {
        case '1':
          hDbilInterestDate = hDcurPurchaseDate;
          break;
        case '2':
          String tmpStr = comcr.increaseDays(hDbilPostDate, comcr.str2int(hDcurAdvWkday));
          hDbilInterestDate = tmpStr;
          break;
        case '3':
          hDbilInterestDate = hWdayNextCloseDate;
          break;
        case '4':
          hDbilInterestDate = hWdayNextLastpayDate;
          break;
      }

      if (hDcurInterestRskDate.length() != 0)
        hDbilInterestDate = hDcurInterestRskDate;

      if (hDbilInterestDate.length() == 0) {
        searchPtrActcode();

        if (hPcodInterestMethod.equals("Y"))
          hDbilInterestDate = hDbilPostDate;
      }

      selectDbcCard();
      insertDbbBill();
      updateDbbCurpost1();

      procVouchType();
      commitDataBase();
    }
    closeCursor();
  }

  /***********************************************************************/
  void selectPtrWorkday() throws Exception {

    for (int i = 0; i < 250; i++) {
      aWdayStmtCycle[i] = "";
      aWdayNextCloseDate[i] = "";
      aWdayNextAcctMonth[i] = "";
      aWdayNextLastpayDate[i] = "";
      aWdayThisAcctMonth[i] = "";
    }

    sqlCmd = "select stmt_cycle,";
    sqlCmd += "next_close_date,";
    sqlCmd += "next_acct_month,";
    sqlCmd += "next_lastpay_date,";
    sqlCmd += "this_acct_month ";
    sqlCmd += " from ptr_workday ";
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      aWdayStmtCycle[i] = getValue("stmt_cycle", i);
      aWdayNextCloseDate[i] = getValue("next_close_date", i);
      aWdayNextAcctMonth[i] = getValue("next_acct_month", i);
      aWdayNextLastpayDate[i] = getValue("next_lastpay_date", i);
      aWdayThisAcctMonth[i] = getValue("this_acct_month", i);
    }
    nCycleCnt = recordCnt;
  }

  /***********************************************************************/
  void searchPtrWorkday() throws Exception {
    int inta;

    for (inta = 0; inta < nCycleCnt; inta++) {
      if (aWdayStmtCycle[inta].equals(hDcurStmtCycle))
        break;
    }

    hWdayNextCloseDate = aWdayNextCloseDate[inta];
    hWdayNextAcctMonth = aWdayNextAcctMonth[inta];
    hWdayNextLastpayDate = aWdayNextLastpayDate[inta];
    hWdayThisAcctMonth = aWdayThisAcctMonth[inta];
  }

  /***********************************************************************/
  void selectPtrActcode() throws Exception {

    for (int i = 0; i < 250; i++) {
      aPcodAcctCode[i] = "";
      aPcodInterestMethod[i] = "";
    }

    sqlCmd = "select interest_method ";
    sqlCmd += " from ptr_actcode ";
    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
      aPcodInterestMethod[i] = getValue("interest_method", i);
    }
    ptrActcodeCnt = recordCnt;
  }

  /***********************************************************************/
  void searchPtrActcode() throws Exception {
    int inta;

    for (inta = 0; inta < ptrActcodeCnt; inta++) {
      if (aPcodAcctCode[inta].equals(hDcurAcctCode))
        break;
    }

    hPcodInterestMethod = aPcodInterestMethod[inta];
  }

  /***********************************************************************/
  void selectDbcCard() throws Exception {
    hCardCorpPSeqno = "";
    hCardIdPSeqno = "";
    hCardMajorIdPSeqno = "";
    hCardRegBankNo = "";
    hCardGpNo = "";
    hCardGroupCode = "";
    hCardComboAcctNo = "";
    hCardBankActno = "";
    hCardOriCardNo = "";

    sqlCmd = "select reg_bank_no,";
    sqlCmd += "acct_no,";
    sqlCmd += "BANK_ACTNO,";
    sqlCmd += "group_code,";
    sqlCmd += "id_p_seqno,";
    sqlCmd += "major_id_p_seqno,";
    sqlCmd += "corp_p_seqno,";
    sqlCmd += "ori_card_no  ";
    sqlCmd += " from dbc_card  ";
    sqlCmd += "where card_no = ? ";
    setString(1, hDcurCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hCardRegBankNo = getValue("reg_bank_no");
      hCardComboAcctNo = getValue("acct_no");
      hCardBankActno = getValue("BANK_ACTNO");
      hCardGpNo = getValue("p_seqno");
      hCardGroupCode = getValue("group_code");
      hCardIdPSeqno = getValue("id_p_seqno");
      hCardMajorIdPSeqno = getValue("major_id_p_seqno");
      hCardCorpPSeqno = getValue("corp_p_seqno");
      hCardOriCardNo = getValue("ori_card_no");
    }

  }

  /***********************************************************************/
  void insertDbbBill() throws Exception {
    hDbilInstallTotTerm = comcr.str2int(hDcurTotalTerm);
    hDbilInstallCurrTerm = comcr.str2int(hDcurTerm);

    /*以參數的帳務月份置入
    if (hDcurBillType.substring(0, 2).equals("OS") && (hDcurTxnCode.equals("AI")
        || hDcurTxnCode.equals("BF") || hDcurTxnCode.equals("DF") || hDcurTxnCode.equals("IF"))) {
      hDbilAcctMonth = hWdayThisAcctMonth;
    } else {
      hDbilAcctMonth = hWdayNextAcctMonth;
    }
    hDbilAcctMonth = String.format("%6.6s", hDbilPostDate);
    */
    
    hDbilAcctMonth = hWdayNextAcctMonth;  

    setValue("reference_no", hDcurReferenceNo);
    setValue("p_seqno", hDcurPSeqno);
    setValue("acct_type", hDcurAcctType);
    setValue("major_card_no", hDcurMajorCardNo);
    setValue("gp_no", hCardGpNo);
    setValue("id_p_seqno", hCardIdPSeqno);
    setValue("major_id_p_seqno", hCardMajorIdPSeqno);
    setValue("card_no", hDcurCardNo);
    setValue("txn_code", hDcurTxnCode);
    setValue("sign_flag", hDcurSignFlag);
    setValue("film_no", hDcurFilmNo);
    setValue("acq_member_id", hDcurAcqMemberId);
    setValueDouble("dest_amt", hDcurDestAmt);
    setValue("dest_curr", hDcurDestCurr);
    setValueDouble("source_amt", hDcurSourceAmt);
    setValue("source_curr", hDcurSourceCurr);
    setValue("mcht_eng_name", hDcurMchtEngName);
    setValue("mcht_city", hDcurMchtCity);
    setValue("mcht_country", hDcurMchtCountry);
    setValue("mcht_category", hDcurMchtCategory);
    setValue("mcht_zip", hDcurMchtZip);
    setValue("mcht_state", hDcurMchtState);
    setValue("mcht_no", hDcurMchtNo);
    setValue("mcht_chi_name", hDcurMchtChiName);
    setValue("auth_code", hDcurAuthCode);
    setValue("acct_month", hDbilAcctMonth);
    setValue("bill_type", hDcurBillType);
    setValue("process_date", hDcurProcessDate);
    setValue("acquire_date", hDcurAcquireDate);
    setValue("purchase_date", hDcurPurchaseDate);
    setValue("post_date", hDbilPostDate);
    setValue("batch_no", hDcurBatchNo);
    setValue("contract_no", hDcurContractNo);
    setValue("contract_seq_no", hDcurContractSeqNo);
    setValueDouble("contract_amt", hDcurContractAmt);
    setValueInt("install_tot_term", hDbilInstallTotTerm);
    setValueInt("install_curr_term", hDbilInstallCurrTerm);
    setValue("usage_code", hDcurUsageCode);
    setValue("pos_entry_mode", hDcurPosEntryMode);
    setValueDouble("settl_amt", hDcurSettlAmt);
    setValue("stmt_cycle", hDcurStmtCycle);
    setValue("prod_no", hDcurProdNo);
    setValue("promote_dept", hDcurPromoteDept);
    setValue("group_code", hCardGroupCode);
    setValue("issue_date", hDcurIssueDate);
    setValue("collection_mode", hDcurCollectionMode);
    setValue("interest_date", hDbilInterestDate);
    setValue("exchange_rate", hDcurExchangeRate);
    setValue("exchange_date", hDcurExchangeDate);
    setValue("fees_reference_no", hDcurFeesReferenceNo);
    setValue("reference_no_original", hDcurReferenceNoOriginal);
    setValue("reference_no_fee_f", hDcurReferenceNoFeeF);
    setValue("bin_type", hDcurBinType);
    setValue("valid_flag", hDcurValidFlag);
    setValue("rsk_type", hDcurRskType);
    setValue("acct_code", hDcurAcctCode);
    setValue("acct_item", hDcurAcctItem);
    setValue("acct_eng_short_name", hDcurAcctEngShortName);
    setValue("acct_chi_short_name", hDcurAcctChiShortName);
    setValue("acexter_desc", hDcurAcexterDesc);
    setValue("item_order_normal", hDcurItemOrderNormal);
    setValue("item_order_back_date", hDcurItemOrderBackDate);
    setValue("item_order_refund", hDcurItemOrderRefund);
    setValue("item_class_normal", hDcurItemClassNormal);
    setValue("item_class_back_date", hDcurItemClassBackDate);
    setValue("item_class_refund", hDcurItemClassRefund);
    setValue("cash_adv_state", hDcurCashAdvState);
    setValue("source_code", hDcurSourceCode);
    setValue("query_type", hDcurQueryType);
    setValue("reg_bank_no", hCardRegBankNo);
    setValue("limit_end_date", hDcurLimitEndDate);
    setValue("expir_date", hDcurExpirDate);
    setValue("payment_type", hDcurPaymentType);
    setValueDouble("curr_tx_amount", hDcurCurrTxAmount);
    setValue("install_tot_term1", hDcurInstallTotTerm1);
    setValueDouble("install_first_amt", hDcurInstallFirstAmt);
    setValueDouble("install_per_amt", hDcurInstallPerAmt);
    setValue("install_fee", hDcurInstallFee);
    setValue("deduct_bp", hDcurDeductBp);
    setValueDouble("cash_pay_amt", hDcurCashPayAmt);
    setValue("ucaf", hDcurUcaf);
    setValue("ec_ind", hDcurEcInd);
    setValue("rsk_type_special", hDcurRskTypeSpecial);
    setValue("acct_date", hBusiBusinessDate);
    setValue("ori_card_no", hCardOriCardNo);
    setValue("mcht_zip_tw", hDcurMchtZipTw);
    setValue("mcht_type", hDcurMchtType);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    setValue("tx_seq", hDcurTxSeq);
    setValue("settl_flag", hDcurSettlFlag);
    setValue("ecs_platform_kind", hDcurEcsPlatformKind);
    setValue("ecs_cus_mcht_no", hDcurEcsCusMchtNo);

    daoTable = "dbb_bill";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_dbb_bill duplicate!", "", comcr.hCallBatchSeqno);
    }
  }

  /***********************************************************************/
  void updateDbbCurpost1() throws Exception {
    daoTable = "dbb_curpost";
    updateSQL = "curr_post_flag = 'T' ";
    whereStr = "where rowid    = ?   ";
    whereStr += "  and decode(curr_post_flag,'','N',curr_post_flag) != 'T' ";
    setRowId(1, hDcurRowid);
    updateTable();
    if (notFound.equals("Y")) {
    	comcr.errRtn("update_dbb_curpost not found!", "", comcr.hCallBatchSeqno);
    }
  }

  /***********************************************************************/
  void procVouchType() throws Exception {
    String tempX04 = "";

    if (hDcurRskType.equals("9") && !hDcurTxnCode.equals("26")) {
      return;
    }

    //showLogMessage("D", "", " proc_vouch=" + hDcurRskType + "," + hDcurDoubtType + ",C=" + hDcurTxConvtFlag);

    //V1.00.05
    if (hDcurDoubtType.length() > 0)  return;  //帳單請款格式有誤,不處理 
    
    if (hDcurRskType.length() > 0 && hDcurRskType.equals("1")) return;  //卡片資料不存在,不處理 
    
/* V1.00.05 點掉此段
    if ((hDcurDoubtType.length() == 0)
        && (hDcurRskType.length() == 0 || hDcurRskType.equals("4"))
        && (!hDcurDuplicatedFlag.equals("Y") && !hDcurDuplicatedFlag.equals("y"))) 
    {
*/
    
    tempX04 = String.format("%2.2s%2.2s", hDcurBillType, hDcurTxnCode);

    //showLogMessage("D", "", " 8860 x04  =" + tempX04 + ",txn=" + hDcurTxnCode + ",dest=" + hDcurDestAmt);
      
    //負向交易
    if ((hDcurDestAmt < 0) || (hDcurTxnCode.equals("06")) || (hDcurTxnCode.equals("25"))
    		|| (hDcurTxnCode.equals("27")) || (hDcurTxnCode.equals("28"))
       		|| (hDcurTxnCode.equals("29")) || (hDcurTxnCode.equals("66"))
       		|| (hDcurTxnCode.equals("85")) || (hDcurTxnCode.equals("87"))
       		|| (hDcurTxnCode.equals("20"))) {
    	  
    	  selectDbaDebt();
    	  insertDbaAcaj();
    	  
       //正向交易
    } else {

    	  insertDbaDebt();

    	  if (hDcurDestAmt != 0)
    		  insertDbaJrnl();
   	}
  }
 

  /***********************************************************************/
  void selectDbaDebt() throws Exception {
    hDadtPSeqno = "";
    hDadtAcctType = "";
    hDadtBegBal = 0;
    hDadtEndBal = 0;
    hDadtDAvailBal = 0;
    hDadtAcctCode = "";
    hDadtInterestDate = "";

    sqlCmd = "select p_seqno,";
    sqlCmd += "acct_type,";
    sqlCmd += "beg_bal,";
    sqlCmd += "end_bal,";
    sqlCmd += "d_avail_bal,";
    sqlCmd += "acct_code,";
    sqlCmd += "interest_date ";
    sqlCmd += " from dba_debt  ";
    sqlCmd += "where reference_no = ? ";
    setString(1, hDcurReferenceNoOriginal);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDadtPSeqno = getValue("p_seqno");
      hDadtAcctType = getValue("acct_type");
      hDadtBegBal = getValueDouble("beg_bal");
      hDadtEndBal = getValueDouble("end_bal");
      hDadtDAvailBal = getValueDouble("d_avail_bal");
      hDadtAcctCode = getValue("acct_code");
      hDadtInterestDate = getValue("interest_date");
    } else {
      selectDbaDebtHst();
    }
  }

  /***********************************************************************/
  void selectDbaDebtHst() throws Exception {
    hDadtPSeqno = "";
    hDadtAcctType = "";
    hDadtBegBal = 0;
    hDadtEndBal = 0;
    hDadtDAvailBal = 0;
    hDadtAcctCode = "";
    hDadtInterestDate = "";

    sqlCmd = "select p_seqno,";
    sqlCmd += "acct_type,";
    sqlCmd += "beg_bal,";
    sqlCmd += "end_bal,";
    sqlCmd += "d_avail_bal,";
    sqlCmd += "acct_code,";
    sqlCmd += "interest_date ";
    sqlCmd += " from dba_debt_hst  ";
    sqlCmd += "where reference_no = ? ";
    setString(1, hDcurReferenceNoOriginal);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDadtPSeqno = getValue("p_seqno");
      hDadtAcctType = getValue("acct_type");
      hDadtBegBal = getValueDouble("beg_bal");
      hDadtEndBal = getValueDouble("end_bal");
      hDadtDAvailBal = getValueDouble("d_avail_bal");
      hDadtAcctCode = getValue("acct_code");
      hDadtInterestDate = getValue("interest_date");
    }
  }

  /***********************************************************************/
  void insertDbaAcaj() throws Exception {

    hDaajReferenceNo = hDcurReferenceNo;
    hDaajPostDate = hBusiBusinessDate;
    if ("TSCC".equals(hDcurBillType)) {
    	hDaajAdjustType = comc.getSubString(hDcurMchtEngName,0,4);
    } else {
        hDaajAdjustType = "DE19";
    }
    hDaajCreateDate = hBusiBusinessDate;
    hDaajCreateTime = hSystemTime;
    hDaajUpdateDate = hBusiBusinessDate;
    hDaajUpdateUser = javaProgram;

    hDaajPSeqno = hDcurPSeqno;
    hDaajAcctType = hDcurAcctType;
    hDaajOrginalAmt = hDadtBegBal;
    hDaajDrAmt = hDcurDestAmt;
    hDaajBefAmt = hDadtEndBal;
    hDaajAftAmt = hDadtEndBal - hDcurDestAmt;
    hDaajBefDAmt = hDadtDAvailBal;
    hDaajAftDAmt = hDadtDAvailBal - hDcurDestAmt;
    hDaajAcctCode = hDcurAcctCode;
    hDaajInterestDate = hDbilInterestDate;
    hDaajFunctionCode = "U";
    hDaajCardNo = hDcurCardNo;
    hDaajTxnCode = hDcurTxnCode;
    hDaajMchtNo = hDcurMchtNo;
    hDaajValueType = "1";
    hDaajConfirmFlag = "Y";
    hDaajProcessFlag = "N";

    setValue("crt_date", hDaajCreateDate);
    setValue("crt_time", hDaajCreateTime);
    setValue("p_seqno", hDaajPSeqno);
    setValue("acct_type", hDaajAcctType);
    setValue("adjust_type", hDaajAdjustType);
    setValue("reference_no", hDaajReferenceNo);
    setValue("post_date", hDaajPostDate);
    setValueDouble("orginal_amt", hDaajOrginalAmt);
    setValueDouble("dr_amt", hDaajDrAmt);
    setValueDouble("cr_amt", hDaajCrAmt);
    setValueDouble("bef_amt", hDaajBefAmt);
    setValueDouble("aft_amt", hDaajAftAmt);
    setValueDouble("bef_d_amt", hDaajBefDAmt);
    setValueDouble("aft_d_amt", hDaajAftDAmt);
    setValue("acct_code", hDaajAcctCode);
    setValue("func_code", hDaajFunctionCode);
    setValue("card_no", hDaajCardNo);
    setValue("cashType", hDaajCashType);
    setValue("value_type", hDaajValueType);
    setValue("trans_acct_type", hDaajTransAcctType);
    setValue("trans_acct_key", hDaajTransAcctKey);
    setValue("interest_date", hDaajInterestDate);
    setValue("adj_reason_code", hDaajAdjReasonCode);
    setValue("adj_comment", hDaajAdjComment);
    setValue("c_debt_key", hDaajCDebtKey);
    setValue("debit_item", hDaajDebitItem);
    setValue("apr_flag", hDaajConfirmFlag);
    setValue("jrnl_date", hDaajJrnlDate);
    setValue("jrnl_time", hDaajJrnlTime);
    setValue("payment_type", hDaajPaymentType);
    setValue("chg_date", hDaajUpdateDate);
    setValue("chg_user", hDaajUpdateUser);
    setValue("acct_no", hCardComboAcctNo);
    setValue("purchase_date", hDcurPurchaseDate);
    setValue("from_code", "1");
    setValue("proc_flag", hDaajProcessFlag);
    setValue("txn_code", hDcurTxnCode);
    setValue("mcht_no", hDcurMchtNo);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    daoTable = "dba_acaj";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_dba_acaj duplicate!", "", comcr.hCallBatchSeqno);
    }
  }

  /***********************************************************************/
  void insertDbaDebt() throws Exception {
    double hReserveAmt = 0;
    String hTempReferenceNo = "";

    hTempReferenceNo = hDcurReferenceNo;
    if ((hDcurReferenceNoOriginal.length() > 0) && (!hDcurBillType.equals("FIFC"))) {
      hTempReferenceNo = hDcurReferenceNoOriginal;
    }

    hTempType = "";
    hReserveAmt = 0;
    sqlCmd = "select dest_amt,";
    sqlCmd += "rsk_type ";
    sqlCmd += " from dbb_ccas_dtl  ";
    sqlCmd += "where reference_no = ?  ";
    sqlCmd += "and rsk_type not in ('B','D') ";
    setString(1, hTempReferenceNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hReserveAmt = getValueDouble("dest_amt");
      hTempType = getValue("rsk_type");
    }

    setValue("reference_no", hDcurReferenceNo);
    setValue("p_seqno", hDcurPSeqno);
    setValue("acct_type", hDcurAcctType);
    setValue("item_post_date", hBusiBusinessDate);
    setValue("item_order_normal", hDcurItemOrderNormal);
    setValue("item_order_back_date", hDcurItemOrderBackDate);
    setValue("item_order_refund", hDcurItemOrderRefund);
    setValue("item_class_normal", hDcurItemClassNormal);
    setValue("item_class_back_date", hDcurItemClassBackDate);
    setValue("item_class_refund", hDcurItemClassRefund);
    setValue("acct_month", hDbilAcctMonth);
    setValue("stmt_cycle", hDcurStmtCycle);
    setValue("bill_type", hDcurBillType);
    setValue("txn_code", hDcurTxnCode);
    setValueDouble("beg_bal", hDcurCashPayAmt == 0 ? hDcurDestAmt : hDcurCashPayAmt);
    setValueDouble("end_bal", hDcurCashPayAmt == 0 ? hDcurDestAmt : hDcurCashPayAmt);
    setValueDouble("d_avail_bal", hDcurCashPayAmt == 0 ? hDcurDestAmt : hDcurCashPayAmt);
    setValue("card_no", hDcurCardNo);
    setValue("acct_code", hDcurAcctCode);
    setValue("interest_date", hDbilInterestDate);
    setValue("purchase_date", hDcurPurchaseDate);
    setValue("acquire_date", hDcurAcquireDate);
    setValue("film_no", hDcurFilmNo);
    setValue("mcht_no", hDcurMchtNo);
    setValue("prod_no", hCardRegBankNo);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    setValue("acct_no", hCardComboAcctNo);
    setValue("id_p_seqno", hCardIdPSeqno);
    setValueDouble("org_reserve_amt", hReserveAmt);
    setValueDouble("reserve_amt", hReserveAmt);
    setValue("bank_actno", hCardBankActno);
    setValue("mcc_code", hDcurMchtCategory);
    setValue("ucaf", hDcurUcaf);
    setValue("eci", hDcurEcInd);
    setValue("pos_entry", hDcurPosEntryMode);
    setValue("tx_seq", hDcurTxSeq);
    daoTable = "dba_debt";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_dba_debt duplicate!", "", comcr.hCallBatchSeqno);
    }
  }

  /***********************************************************************/
  void insertDbaJrnl() throws Exception {
    hDajlEnqSeqno++;
    setValue("crt_date", hSystemDate);
    setValue("crt_time", hSystemTime);
    setValueInt("enq_seqno", hDajlEnqSeqno);
    setValue("p_seqno", hDcurPSeqno);
    setValue("acct_type", hDcurAcctType);
     setValue("id_p_seqno", hCardIdPSeqno);
    setValue("corp_p_seqno", hCardCorpPSeqno);
    setValue("acct_date", hBusiBusinessDate);
    setValue("tran_class", "B");
    setValue("tran_type", hDcurBillType);
    setValue("item_ename", hDcurAcctCode);
    setValue("dr_cr", "C");
    setValueDouble("transaction_amt", hDcurCashPayAmt == 0 ? hDcurDestAmt : hDcurCashPayAmt);
    setValueDouble("jrnl_bal", hDcurCashPayAmt == 0 ? hDcurDestAmt : hDcurCashPayAmt);
    setValueDouble("item_bal", hDcurCashPayAmt == 0 ? hDcurDestAmt : hDcurCashPayAmt);
    setValueDouble("item_d_bal", hDcurCashPayAmt == 0 ? hDcurDestAmt : hDcurCashPayAmt);
    setValue("item_date", hBusiBusinessDate);
    setValue("interest_date", hDbilInterestDate);
    setValue("adj_reason_code", hDcurTxnCode);
    setValue("reference_no", hDcurReferenceNo);
    setValue("pay_id", hDcurCardNo);
    setValue("stmt_cycle", hDcurStmtCycle);
    setValue("update_user", hDcurModUser);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    setValue("acct_no", hCardComboAcctNo);
    setValue("card_no", hDcurCardNo);
    setValue("purchase_date", hDcurPurchaseDate);
    setValue("item_post_date", hBusiBusinessDate);
    daoTable = "dba_jrnl";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_dba_jrnl duplicate!", "", comcr.hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  void updateDbbCurpost() throws Exception {
    daoTable = "dbb_curpost";
    updateSQL = "curr_post_flag       = 'Y' ";
    whereStr = "where curr_post_flag = 'T' ";
    updateTable();
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    DbbA008 proc = new DbbA008();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
  /***********************************************************************/
}
