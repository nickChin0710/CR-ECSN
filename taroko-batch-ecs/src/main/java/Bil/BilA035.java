/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/23  V1.00.01   shiyuqi       updated for project coding standard   *
*  111/06/16  V1.00.02    Justin    弱點修正                                  *
*  112/05/30  V1.00.03    JeffKung  update for id_p_seqno                     *
*  112/11/07  V1.00.04    JeffKung  for已出帳單的分期及活動名稱                               
*  112/12/14  V1.00.05    JeffKung  轉檔時debt加總金額無法比對問題修正                    *
******************************************************************************/

package Bil;

import java.util.Locale;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡友來電自費分期轉合約檔處理*/
public class BilA035 extends AccessDAO {
    private String progname = "卡友來電自費分期轉合約檔處理  112/12/14  V1.00.05 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;

    String prgmId = "BilA035";
    String prgmName = "卡友來電自費分期轉合約檔處理";
    String errMsg = "";
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";

    String hBusinessDate = "";
    String hSystemDate = "";
    String hSystemTime = "";
    String hSystemDateF = "";
    String hMerchantNo = "";
    String hMerchantChiName = "";
    String hMerchantEngName = "";
    String hMerchantType = "";
    String hAutxMerchantNoParm = "";
    long hAutxTotTerm = 0;
    String hAutxAprDate1 = "";
    String hAutxAprUser1 = "";
    String hAutxSaleEmpNo = "";
    String hAutxActionDesc = "";
    String hBillRowid = "";
    String hPrintName = "";
    String hRptName = "";
    String tempX10 = "";
    String hContStmtCycle = "";
    String hContAcctType = "";
    String hInstallmentFlag = "";
    double hProdCltFeesFixAmt = 0;
    double hProdCltFeesMinAmt = 0;
    double hProdCltFeesMaxAmt = 0;
    String hContProductNo = "";
    String hContMerchantNo = "";
    String hContContractNo = "";
    String hContRealCardNo = "";
    String hContBackCardNo = "";
    String hContNewCardNo = "";
    String hContProductName = "";
    String hContMerchantChiName = "";
    String hContContractKind = "";
    String hContAllocateFlag = "";
    String hContCvv2 = "";
    double hContExchangeAmt = 0;
    long hContUnitPrice = 0;
    int hContQty = 0;
    double hContTotAmt = 0;
    long hContInstallTotTerm = 0;
    double hContRemdAmt = 0;
    String hContAutoDelvFlag = "";
    double hContFeesFixAmt = 0;
    String hContFirstPostDate = "";
    String hContAllPostFlag = "";
    String hContRefundFlag = "";
    String hContRefundConfirmFlag = "";
    String hContRefundConfirmDate = "";
    String hContConfirmDate = "";
    String hContConfirmFlag = "";
    String hContReceiveName = "";
    String hContReceiveTel = "";
    String hContReceiveTel1 = "";
    String hContVoucherHead = "";
    String hContUniformNo = "";
    String hContZipCode = "";
    String hContReceiveAddress = "";
    String hContDelvDate = "";
    String hContDelvConfirmFlag = "";
    String hContDelvConfirmDate = "";
    String hContRegisterNo = "";
    String hContAuthorization = "";
    String hContDelvBatchNo = "";
    String hContForcedPostFlag = "";
    String hContInstallBackTermFlag = "";
    String hContDevFlag20 = "";
    String hContPrtFlag21 = "";
    String hContCpsFlag = "";
    String hContLimitEndDate = "";
    String hContFeeFlag = "";
    String hContFilmNo = "";
    String hContReferenceNo = "";
    String hContPaymentType = "";
    double hContFirstRemdAmt = 0;
    double hContCltFeesAmt = 0;
    double hContCltUnitPrice = 0;
    long hContCltInstallTotTerm = 0;
    double hContCltRemdAmt = 0;
    double hContExtraFees = 0;
    long hContPostCycleDd = 0;
    long hContInstallCurrTerm = 0;
    long hContRefundQty = 0;
    long hContInstallBackTerm = 0;
    String hBillBatchNo = "";
    String hBillAcquirerMemberId = "";
    String hContMerchantEngName = "";
    String hBillPurchaseDate = "";
    String hContInstallmentKind = "";
    String hContSaleEmpNo = "";
    String hAutxPostFlag = "";
    String hAutxCloseFlag = "";
    String hAutxReferenceNo = "";
    String hAutxErrorDesc = "";
    String hAutxRowid = "";
    String hAcajCreateDate = "";
    String hAcajCreateTime = "";
    String hAcajPSeqno = "";
    String hAcajAcctType = "";
    String hAcajAdjustType = "";
    String hAcajReferenceNo = "";
    String hAcajPostDate = "";
    double hAcajOrginalAmt = 0;
    double hAcajDrAmt = 0;
    double hAcajCrAmt = 0;
    double hAcajBefAmt = 0;
    double hAcajAftAmt = 0;
    double hAcajBefDAmt = 0;
    double hAcajAftDAmt = 0;
    String hAcajAcctCode = "";
    String hAcajFunctionCode = "";
    String hAcajCardNo = "";
    String hAcajCashType = "";
    String hAcajValueType = "";
    String hAcajTransAcctType = "";
    String hAcajTransAcctKey = "";
    String hAcajInterestDate = "";
    String hAcajAdjReasonCode = "";
    String hAcajAdjComment = "";
    String hAcajCDebtKey = "";
    String hAcajDebitItem = "";
    String hAcajConfirmFlag = "";
    String hAcajConfirmDate = "";
    String hAcajJrnlDate = "";
    String hAcajJrnlTime = "";
    String hAcajPaymentType = "";
    String hAcajUpdateDate = "";
    String hAcajUpdateUser = "";
    String hAcajMerchantNo = "";
    String hAutxCreateUser = "";
    String hAcajModWs = "";
    String hAcajModSeqno = "";
    String hAcajModLog = "";
    String hDebtPSeqno = "";
    String hDebtAcctType = "";
    String hDebtAcctCode = "";
    String hDebtReferenceNo = "";
    String hDebtInterestDate = "";
    double hDebtBegBal = 0;
    double hDebtEndBal = 0;
    double hDebtDAvailableBal = 0;
    String hBillReferenceNo = "";
    String hBillPSeqno = "";
    String hBillAcnoPSeqno = "";
    String hBillIdPSeqno = "";
    String hBillBillType = "";
    String hBillTransactionCode = "";
    String hBillMerchantNo = "";
    String hBillAuthorization = "";
    String hBillFilmNo = "";
    String hBillMerchantEngName = "";
    String hBillAcctType = "";
    String hBillBilledFlag = "";
    String hBillInstallmentKind = "";
    String hBillAcctMonth = "";
    String hAutxCardNo = "";
    String hAutxMerchantNo = "";
    String hAutxAuthorization = "";
    String hAutxPurchaseDate = "";
    double hAutxDestinationAmt = 0;
    String hTempX08 = "";
    String hTempReferenceNo = "";
    String hAufeBillType = "";
    String hAufeTxCode = "";
    double hTempDestinationAmt = 0;
    String hPbtbCurrCode = "";
    String hBityExterDesc = "";
    String hCurpBatchNo = "";
    String hBityAcctCode = "";
    String hBityAcctItem = "";
    String hPcodEngShortName = "";
    String hPcodChiShortName = "";
    String hPcodItemOrderNormal = "";
    String hPcodItemOrderBackDate = "";
    String hPcodItemOrderRefund = "";
    String hBityEntryAcct = "";
    String hPcodItemClassNormal = "";
    String hPcodItemClassBackDate = "";
    String hPcodItemClassRefund = "";
    String hPcodAcctMethod = "";
    String hBityInterestMode = "";
    String hBityAdvWkday = "";
    String hBityCollectionMode = "";
    double hBityFeesFixAmt = 0;
    String hBityFeesPercent = "";
    String hBityFeesMin = "";
    String hBityFeesMax = "";
    String hBityFeesBillType = "";
    String hBityFeesTxnCode = "";
    String hBityBalanceState = "";
    String hBityCashAdvState = "";
    String hBityMerchFee = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoBlockStatus = "";
    String hAcnoBlockDate = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo = "";
    String hCardMajorCardNo = "";
    String hCardCurrentCode = "";
    String hCardOppostDate = "";
    String hCardIssueDate = "";
    String hCardPromoteDept = "";
    String hCardProdNo = "";
    String hCardGroupCode = "";
    String hCurpCardSw = "";
    String hCardAcctPSeqno = "";
    String hCardId = "";
    String hCardSourceCode = "";
    String hPcodQueryType = "";
    String hCardCardType = "";
    String hCardComboAcctNo = "";
    int hPostBatchSeq = 0;
    String hBiunConfFlag = "";
    String hBiunAuthFlag = "";
    String hContPurchaseDate = "";
    double hProdFeesFixAmt = 0;
    double hContFeesRate = 0;
    double hProdCltInterestRate = 0;
    double hProdExtraFees = 0;
    int hContAgainstNum = 0;
    int hContContractSeqNo = 0;
    int totalCnt = 0;
    int tempInt = 0;
    long tempLong = 0;
    double tempDouble = 0;
    int cntContract = 0;
    // **********************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : BilA035 callbatch_seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            // comcr.callbatch(0, 0, 0);
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            //檢查是否開啟debug mode
            for (int argi=0; argi < args.length ; argi++ ) {
          	  if (args[argi].equals("debug")) {
          		  debug = 1;
          	  }
            }

            hModPgm = javaProgram;

            selectBilAutoTx();

            showLogMessage("I", "", String.format("  總筆數=[%d],分期=[%d]\n", totalCnt, cntContract));
            // ================================================================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        sqlCmd = "select business_date, to_char(sysdate,'yyyymmdd') h_system_date, ";
        sqlCmd += "to_char(sysdate,'hh24miss') h_system_time, ";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");  //營業日未設定: abend
        }
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
            hSystemDate = getValue("h_system_date");
            hSystemTime = getValue("h_system_time");
            hSystemDateF = getValue("h_system_date_f");
        }

        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }

    /***********************************************************************/
    void selectBilAutoTx() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "mcht_no_parm,";
        sqlCmd += "mcht_no,";
        sqlCmd += "authorization,";
        sqlCmd += "purchase_date,";
        sqlCmd += "dest_amt,";
        sqlCmd += "reference_no,";
        sqlCmd += "tot_term,";
        sqlCmd += "crt_user,";
        sqlCmd += "apr_date_1,";
        sqlCmd += "apr_user_1,";
        sqlCmd += "post_flag,";
        sqlCmd += "close_flag,";
        sqlCmd += "sale_emp_no,";
        sqlCmd += "action_desc,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from bil_auto_tx ";
        sqlCmd += "where post_flag   = 'N' ";
        sqlCmd += "  and apr_date_1 != '' ";
        sqlCmd += "  and decode(close_flag  ,'','N',close_flag)   = 'N' ";
        sqlCmd += "  and decode(trial_status,'','3',trial_status) = '3' ";
        
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAutxCardNo = getValue("card_no");
            hAutxMerchantNoParm = getValue("mcht_no_parm");
            hAutxMerchantNo = getValue("mcht_no");
            hAutxAuthorization = getValue("authorization");
            hAutxPurchaseDate = getValue("purchase_date");
            hAutxDestinationAmt = getValueDouble("dest_amt");
            hAutxReferenceNo = getValue("reference_no");
            hAutxTotTerm = getValueLong("tot_term");
            hAutxCreateUser = getValue("crt_user");
            hAutxAprDate1 = getValue("apr_date_1");
            hAutxAprUser1 = getValue("apr_user_1");
            hAutxPostFlag = getValue("post_flag");
            hAutxCloseFlag = getValue("close_flag");
            hAutxSaleEmpNo = getValue("sale_emp_no");
            hAutxActionDesc = getValue("action_desc");
            hAutxRowid = getValue("rowid");

            totalCnt++;

            hAutxErrorDesc = "";

            selectBilAutoParm();
            
            if (hAutxErrorDesc.length() > 0) {
                updateBilAutoTx();
                continue;
            }

            selectBilBill();

            if(debug ==1) {
            	showLogMessage("I",""," STEP 1=["+ hAutxErrorDesc.length()+"]"+ hAutxErrorDesc);
            }

            if (hAutxErrorDesc.length() > 0) {
                updateBilAutoTx();
                continue;
            }

            hAutxReferenceNo = hBillReferenceNo;

            if(debug ==1) {
            	showLogMessage("I",""," STEP 2=["+ hAutxErrorDesc.length()+"]"+ hAutxErrorDesc);
            }

            if (selectActDebt() == false) {
            	
            	if (selectActDebtCombind() == false) {
            		hAutxErrorDesc = String.format("debt 無資料或交易D檔過[%s]", hBillReferenceNo);
                    updateBilAutoTx();
                    continue;
            	} 
                
            }

            if(debug ==1) {
            	showLogMessage("I",""," STEP 3=["+ hAutxErrorDesc.length()+"]"+ hAutxErrorDesc);
            }

            //如果本日已經有調整過,直接update
            if (selectActAcaj() == 0) {
            	if (hAcajAftAmt < 0) {
            		hAutxErrorDesc = String.format("餘額不足,不可申請[%s]", hBillReferenceNo);
                    updateBilAutoTx();
                    continue;
            	}
            	if (updateActAcaj()==-1) {
            		hAutxErrorDesc = String.format("系統問題acaj異動失敗[%s]", hBillReferenceNo);
                    updateBilAutoTx();
                    continue;
            	}
            	
            } else {
            	insertActAcaj();
            }
            
            
            insertBilContract();



            if(debug ==1) {
            	showLogMessage("I",""," STEP 4=["+ hAutxErrorDesc.length()+"]"+ hAutxErrorDesc);
            }
            
            hAutxPostFlag = "Y";
            hAutxCloseFlag = "Y";

            updateBilAutoTx();
            
            if(debug ==1) {
            	showLogMessage("I",""," STEP 5=["+ hAutxErrorDesc.length()+"]"+ hAutxErrorDesc);
            }
            
            if ("".equals(hBillRowid) == false) {
            	daoTable = "bil_bill";
                //updateSQL = "billed_flag = 'U'";
                updateSQL = "installment_kind = 'F' "; //簽單分期
                whereStr = "where rowid  = ? ";
                setRowId(1, hBillRowid);
                updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_bil_bill not found!", "", hCallBatchSeqno); 
                }
            }
            
        }

        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectBilAutoParm() throws Exception {
        hMerchantChiName = "";
        hMerchantEngName = "";
        hMerchantNo = "";
        hMerchantType = "";
        sqlCmd = "select mcht_no,";
        sqlCmd += "mcht_chi_name,";
        sqlCmd += "mcht_eng_name,";
        sqlCmd += "mcht_type ";
        sqlCmd += " from bil_merchant  ";
        sqlCmd += "where mcht_no = ?  ";
        setString(1, hAutxMerchantNoParm);
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	hAutxErrorDesc = "分期特店未設定";
            
            if(debug ==1) {
            	showLogMessage("I",""," 分期特店未設定="+ hAutxCardNo +","+ hAutxMerchantNoParm );
            }
            
            return;
        }
        
        if (recordCnt > 0) {
            hMerchantNo = getValue("mcht_no");
            hMerchantChiName = getValue("mcht_chi_name");
            hMerchantEngName = getValue("mcht_eng_name");
            hMerchantType = getValue("mcht_type");
        }
    }

    /***********************************************************************/
    void selectBilBill() throws Exception {
        hBillReferenceNo = "";
        hBillPSeqno = "";
        hBillAcnoPSeqno = "";
        hBillBillType = "";
        hBillTransactionCode = "";
        hBillMerchantNo = "";
        hBillAuthorization = "";
        hBillPurchaseDate = "";
        hBillFilmNo = "";
        hBillBatchNo = "";
        hBillMerchantEngName = "";
        hBillAcquirerMemberId = "";
        hBillAcctType = "";
        hBillBilledFlag = "";
        hBillInstallmentKind = "";
        hBillRowid = "";

        sqlCmd = "select reference_no,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "p_seqno,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "mcht_no,";
        sqlCmd += "auth_code,";
        sqlCmd += "purchase_date,";
        sqlCmd += "film_no,";
        sqlCmd += "batch_no,";
        sqlCmd += "mcht_eng_name,";
        sqlCmd += "acq_member_id,";
        sqlCmd += "acct_type,";
        sqlCmd += "decode(billed_flag,'','N',billed_flag) h_bill_billed_flag,";
        sqlCmd += "acct_month,";
        sqlCmd += "installment_kind,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from bil_bill  ";
        sqlCmd += "where card_no         = ?  ";
        sqlCmd += "  and decode(auth_code   ,'',' ',auth_code)        = ?  ";
        sqlCmd += "  and purchase_date   = ?  ";
        sqlCmd += "  and txn_code        = '05'  ";
        sqlCmd += "  and acct_code       = 'BL'  ";
        sqlCmd += "  and decode(cash_pay_amt,0,dest_amt,cash_pay_amt) = ?  ";
        sqlCmd += "  and rsk_type not in ('1','2','3')  ";

        setString(1, hAutxCardNo);
        setString(2, hAutxAuthorization.equals("") ? " " : hAutxAuthorization);
        setString(3, hAutxPurchaseDate);
        setDouble(4, hAutxDestinationAmt);
        
        //有鍵入特店代號時才比對
        if ("".equals(hAutxMerchantNo) == false) {
            sqlCmd += "  and mcht_no         = ?  ";
            setString(5, hAutxMerchantNo);
        }
        
        int recordCnt = selectTable();
        
        if (notFound.equals("Y")) {
            hAutxErrorDesc = "bill特店未請款";
            
            if(debug ==1) {
            	showLogMessage("I",""," bill特店未請款="+ hAutxCardNo +","+ hAutxMerchantNo +","+ hAutxAuthorization +","+ hAutxPurchaseDate +","+ hAutxDestinationAmt);
            }
            
            return;
        }
        
		if (recordCnt > 1) {
			hAutxErrorDesc = "bill多筆相同交易";
			hAutxCloseFlag = "Y";
			return;
		}

		hBillReferenceNo = getValue("reference_no");
		hBillAcnoPSeqno = getValue("acno_p_seqno");
		hBillIdPSeqno = getValue("id_p_seqno");
		hBillPSeqno = getValue("p_seqno");
		hBillBillType = getValue("bill_type");
		hBillTransactionCode = getValue("transaction_code");
		hBillMerchantNo = getValue("merchant_no");
		hBillAuthorization = getValue("authorization");
		hBillPurchaseDate = getValue("purchase_date");
		hBillFilmNo = getValue("film_no");
		hBillBatchNo = getValue("batch_no");
		hBillMerchantEngName = getValue("merchant_eng_name");
		hBillAcquirerMemberId = getValue("acquirer_member_id");
		hBillAcctType = getValue("acct_type");
		hBillBilledFlag = getValue("h_bill_billed_flag");
		hBillInstallmentKind = getValue("installment_kind");
		hBillAcctMonth = getValue("acct_month");
		hBillRowid = getValue("rowid");
		
		/*
		if ("U".equals(hBillBilledFlag)) {
			hAutxErrorDesc = "重覆申請分期";
			hAutxCloseFlag = "Y";
			return;
		}
		*/
		
		if ("".equals(hBillInstallmentKind)==false) {
			hAutxErrorDesc = "重覆申請分期";
			hAutxCloseFlag = "Y";
			return;
		}

    }

    /***********************************************************************/
    void updateBilAutoTx() throws Exception {
        daoTable   = "bil_auto_tx";
        updateSQL  = " post_flag    = ?,";
        updateSQL += " close_flag   = ?,";
        updateSQL += " post_date    = decode(cast(? as varchar(10)),'Y',";
        updateSQL += " cast(? as varchar(10)) , post_date),";
        updateSQL += " reference_no = ?,";
        updateSQL += " error_desc   = ?,";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ? ";
        whereStr = "where rowid   = ? ";
        setString(1, hAutxPostFlag);
        setString(2, hAutxCloseFlag);
        setString(3, hAutxPostFlag);
        setString(4, hSystemDate);
        setString(5, hAutxReferenceNo);
        setString(6, hAutxErrorDesc);
        setString(7, prgmId);
        setRowId(8, hAutxRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_bil_auto_tx not found!", "", "");  //用rowId更新原資料, 不應該會發生
        }
    }

    /***********************************************************************/
    boolean selectActDebt() throws Exception {
        hDebtPSeqno = "";
        hDebtAcctType = "";
        hDebtBegBal = 0;
        hDebtEndBal = 0;
        hDebtDAvailableBal = 0;
        hDebtAcctCode = "";
        hDebtReferenceNo = "";
        hDebtInterestDate = "";

        sqlCmd = "select acno_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "beg_bal,";
        sqlCmd += "end_bal,";
        sqlCmd += "d_avail_bal,";
        sqlCmd += "acct_code,";
        sqlCmd += "interest_date ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where reference_no = ?  ";
        sqlCmd += "  and d_avail_bal   = beg_bal ";
        setString(1, hBillReferenceNo);
        int recordCnt = selectTable();
        
        if (notFound.equals("Y")) {
            return selectActDebtHst();
        }

		hDebtPSeqno = getValue("acno_p_seqno");
		hDebtAcctType = getValue("acct_type");
		hDebtBegBal = getValueDouble("beg_bal");
		hDebtEndBal = getValueDouble("end_bal");
		hDebtDAvailableBal = getValueDouble("d_avail_bal");
		hDebtAcctCode = getValue("acct_code");
		hDebtReferenceNo = hBillReferenceNo;
		hDebtInterestDate = getValue("interest_date");

		return true;
	}

    /***********************************************************************/
    boolean selectActDebtHst() throws Exception {
        hDebtPSeqno = "";
        hDebtAcctType = "";
        hDebtBegBal = 0;
        hDebtEndBal = 0;
        hDebtDAvailableBal = 0;
        hDebtAcctCode = "";
        hDebtReferenceNo = "";
        hDebtInterestDate = "";

        sqlCmd = "select acno_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "beg_bal,";
        sqlCmd += "end_bal,";
        sqlCmd += "d_avail_bal,";
        sqlCmd += "acct_code,";
        sqlCmd += "reference_no,";
        sqlCmd += "interest_date ";
        sqlCmd += " from act_debt_hst  ";
        sqlCmd += "where reference_no = ?  ";
        sqlCmd += "and d_avail_bal = beg_bal ";
        setString(1, hBillReferenceNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return false;
        }

		hDebtPSeqno = getValue("acno_p_seqno");
		hDebtAcctType = getValue("acct_type");
		hDebtBegBal = getValueDouble("beg_bal");
		hDebtEndBal = getValueDouble("end_bal");
		hDebtDAvailableBal = getValueDouble("d_avail_bal");
		hDebtAcctCode = getValue("acct_code");
		hDebtReferenceNo = hBillReferenceNo;
		hDebtInterestDate = getValue("interest_date");

		return true;
	}
    
    /***********************************************************************/
    boolean selectActDebtCombind() throws Exception {
        hDebtPSeqno = "";
        hDebtAcctType = "";
        hDebtBegBal = 0;
        hDebtEndBal = 0;
        hDebtDAvailableBal = 0;
        hDebtAcctCode = "";
        hDebtReferenceNo = "";
        hDebtInterestDate = "";

        sqlCmd = "select acno_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "beg_bal,";
        sqlCmd += "end_bal,";
        sqlCmd += "d_avail_bal,";
        sqlCmd += "acct_code,";
        sqlCmd += "reference_no,";
        sqlCmd += "interest_date ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where p_seqno   = ? ";
        sqlCmd += "  and acct_month = ? ";
        sqlCmd += "  and acct_code = 'BL' ";
        sqlCmd += "  and end_bal   >= ? ";
        setString(1, hBillPSeqno);
        setString(2, hBillAcctMonth);
        setDouble(3, hAutxDestinationAmt);
        
        int recordCnt = selectTable();
        
        if (notFound.equals("Y")) {
            return false;
        }

		hDebtPSeqno = getValue("acno_p_seqno");
		hDebtAcctType = getValue("acct_type");
		hDebtBegBal = getValueDouble("beg_bal");
		hDebtEndBal = getValueDouble("end_bal");
		hDebtDAvailableBal = getValueDouble("d_avail_bal");
		hDebtAcctCode = getValue("acct_code");
		hDebtReferenceNo = getValue("reference_no");
		hDebtInterestDate = getValue("interest_date");

		return true;
	}

    /***********************************************************************/
    void insertBilContract() throws Exception {
        String hInstallmentFlag = "";


        initBilContract();

        sqlCmd = "select substr(to_char(bil_contractseq.nextval,'0000000000'),2,10) temp_x10 ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempX10 = getValue("temp_x10");
        }

if(debug ==1) showLogMessage("I",""," insert_bil_contract=["+ tempX10 +"]");

        hContContractNo = tempX10;

        hContContractSeqNo = 1;
        hContRealCardNo = hAutxCardNo;

        hContProductNo = String.format("%02d", hAutxTotTerm);

        hContMerchantNo = hMerchantNo;
        hContAuthorization = hBillAuthorization;

        //20231107改放活動名稱
        //hContProductName = String.format("%02d", hAutxTotTerm);
        hContProductName = hAutxActionDesc;
        
        hProdFeesFixAmt = 0;
        hContFeesRate = 0;
        hContAgainstNum = 0;
        hProdCltFeesFixAmt = 0;
        hProdCltInterestRate = 0;

        hContAutoDelvFlag = "Y";
        hContAcctType = hBillAcctType;

        sqlCmd = "select stmt_cycle ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acct_type    = ?  ";
        sqlCmd += "  and acno_p_seqno = ? ";
        setString(1, hContAcctType);
        setString(2, hBillAcnoPSeqno);
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            errMsg = String.format("select act_acno error=[%s]", hBillAcnoPSeqno);
            comcr.errRtn(errMsg, "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hContStmtCycle = getValue("stmt_cycle");
        }

        hContContractKind = "1";
        hContConfirmDate = hSystemDate;
        hContPurchaseDate = hBillPurchaseDate;
        hContDelvDate = hSystemDate;
        hContDelvConfirmDate = hSystemDate;
        hContDelvConfirmFlag = "Y";
        hContConfirmFlag = "Y";
        hContFeeFlag = "F";
        hContFilmNo = hBillFilmNo;
        hContReferenceNo = hAutxReferenceNo;
        hContMerchantEngName = hMerchantEngName;
        hContMerchantChiName = hMerchantChiName;
        hContSaleEmpNo = hAutxSaleEmpNo;

        hContQty = 1;
        hContInstallTotTerm = hAutxTotTerm;
        tempLong = (long) (hAutxDestinationAmt / hAutxTotTerm);
        hContUnitPrice = tempLong;

        hContTotAmt = hAutxDestinationAmt;
        hContFirstRemdAmt = hContTotAmt - hContInstallTotTerm * hContUnitPrice;

        hContInstallmentKind = "";

        cntContract++;

        /* icbc : N ,nccc : C , cps :Y */
        if (hMerchantType.substring(0, 1).equals("0")) {
            hContCpsFlag = "Y";
        }
        if (hMerchantType.substring(0, 1).equals("1")) {
            hContCpsFlag = "N";
        }
        if (hMerchantType.substring(0, 1).equals("2")) {
            hContCpsFlag = "C";
        }

        hContCltInstallTotTerm = 1;
        hContCltRemdAmt = 0;
        hProdCltFeesFixAmt = 0;
        hProdCltInterestRate = 0;
        hProdExtraFees = 0;
        sqlCmd = "select installment_flag,";
        sqlCmd += "extra_fees,";
        sqlCmd += "clt_fees_fix_amt,";
        sqlCmd += "clt_interest_rate,";
        sqlCmd += "clt_fees_min_amt,";
        sqlCmd += "clt_fees_max_amt ";
        sqlCmd += " from bil_prod  ";
        sqlCmd += "where product_no = ?  ";
        sqlCmd += "  and mcht_no    = ? ";
        setString(1, hContProductNo);
        setString(2, hContMerchantNo);
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            errMsg = String.format("select bil_prod     error=[%s]", hContProductNo);
            comcr.errRtn(errMsg, "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hInstallmentFlag = getValue("installment_flag");
            hProdCltFeesFixAmt = getValueDouble("clt_fees_fix_amt");
            hProdCltInterestRate = getValueDouble("clt_interest_rate");
            hProdCltFeesMinAmt = getValueDouble("clt_fees_min_amt");
            hProdCltFeesMaxAmt = getValueDouble("clt_fees_max_amt");
            hProdExtraFees     = getValueDouble("extra_fees");
        }

        tempDouble = hAutxDestinationAmt * hProdCltInterestRate / 100 + hProdCltFeesFixAmt;
        tempLong = (long) (tempDouble + 0.5);
        hContCltFeesAmt = tempLong;

        if (hContCltFeesAmt > hProdCltFeesMaxAmt)
            hContCltFeesAmt = hProdCltFeesMaxAmt;
        if (hContCltFeesAmt < hProdCltFeesMinAmt)
            hContCltFeesAmt = hProdCltFeesMinAmt;

        if (comc.getSubString(hInstallmentFlag, 0,1).toUpperCase(Locale.TAIWAN).equals("Y")) {
            hContCltInstallTotTerm = hContInstallTotTerm;
            tempInt = (int) (hContCltFeesAmt / hContInstallTotTerm);
            hContCltUnitPrice = tempInt;
            hContCltRemdAmt = hContCltFeesAmt - (hContInstallTotTerm * tempInt);
        } else {
            hContCltInstallTotTerm = 1;
            hContCltUnitPrice = hContCltFeesAmt;
            hContCltRemdAmt = 0;
        }
        
        hContExtraFees = hProdExtraFees;

        setValue("CONTRACT_NO"        , hContContractNo);
        setValueLong("CONTRACT_SEQ_NO", hContContractSeqNo);
        setValue("CARD_NO"            , hContRealCardNo);
        setValue("BACK_CARD_NO" , hContBackCardNo);
        setValue("NEW_CARD_NO"  , hContNewCardNo);
        setValue("ACCT_TYPE"    , hContAcctType);
        setValue("p_seqno"      , hBillPSeqno);
        setValue("acno_p_seqno" , hBillAcnoPSeqno);
        setValue("id_p_seqno"   , hBillIdPSeqno);
        setValue("STMT_CYCLE"   , hContStmtCycle);
        setValue("PRODUCT_NO"   , hContProductNo);
        setValue("PRODUCT_NAME" , hContProductName);
        setValue("MCHT_NO"      , hContMerchantNo);
        setValue("MCHT_CHI_NAME", hContMerchantChiName);
        setValue("CONTRACT_KIND", hContContractKind);
        setValue("ALLOCATE_FLAG", hContAllocateFlag);
        setValue("CVV2"         , hContCvv2);
        setValueDouble("EXCHANGE_AMT"  , hContExchangeAmt);
        setValueInt("AGAINST_NUM"      , hContAgainstNum);
        setValueDouble("UNIT_PRICE"    , hContUnitPrice);
        setValueInt("QTY"              , hContQty);
        setValueDouble("TOT_AMT"       , hContTotAmt);
        setValueLong("INSTALL_TOT_TERM", hContInstallTotTerm);
        setValueDouble("REMD_AMT"      , hContRemdAmt);
        setValue("AUTO_DELV_FLAG"      , hContAutoDelvFlag);
        setValueDouble("FEES_FIX_AMT"  , hContFeesFixAmt);
        setValueDouble("FEES_RATE"     , hContFeesRate);
        setValueDouble("EXTRA_FEES"    , hContExtraFees);
        setValue("FIRST_POST_DATE"     , hContFirstPostDate);
        setValueLong("POST_CYCLE_DD"   , hContPostCycleDd);
        setValueLong("INSTALL_CURR_TERM", hContInstallCurrTerm);
        setValue("ALL_POST_FLAG"        , hContAllPostFlag);
        setValue("REFUND_FLAG"          , hContRefundFlag);
        setValueLong("REFUND_QTY"       , hContRefundQty);
        setValue("REFUND_APR_FLAG"      , hContRefundConfirmFlag);
        setValue("REFUND_APR_DATE"      , hContRefundConfirmDate);
        setValue("APR_DATE"             , hContConfirmDate);
        setValue("APR_FLAG"         , hContConfirmFlag);
        setValue("RECEIVE_NAME"     , hContReceiveName);
        setValue("RECEIVE_TEL"      , hContReceiveTel);
        setValue("RECEIVE_TEL1"     , hContReceiveTel);
        setValue("VOUCHER_HEAD"     , hContVoucherHead);
        setValue("UNIFORM_NO"       , hContUniformNo);
        setValue("ZIP_CODE"         , hContZipCode);
        setValue("RECEIVE_ADDRESS"  , hContReceiveAddress);
        setValue("DELV_DATE"        , hContDelvDate);
        setValue("DELV_CONFIRM_FLAG", hContDelvConfirmFlag);
        setValue("DELV_CONFIRM_DATE", hContDelvConfirmDate);
        setValue("REGISTER_NO"      , hContRegisterNo);
        setValue("AUTH_CODE"        , hContAuthorization);
        setValue("DELV_BATCH_NO"    , hContDelvBatchNo);
        setValue("FORCED_POST_FLAG"      , hContForcedPostFlag);
        setValueLong("INSTALL_BACK_TERM" , hContInstallBackTerm);
        setValue("INSTALL_BACK_TERM_FLAG", hContInstallBackTermFlag);
        setValue("DEV_FLAG_20"           , hContDevFlag20);
        setValue("PRT_FLAG_21"           , hContPrtFlag21);
        setValue("CPS_FLAG"              , hContCpsFlag);
        setValue("LIMIT_END_DATE"        , hContLimitEndDate);
        setValue("FEE_FLAG"              , hContFeeFlag);
        setValue("FILM_NO"               , hContFilmNo);
        setValue("REFERENCE_NO"          , hContReferenceNo);
        setValue("PAYMENT_TYPE"          , hContPaymentType);
        setValueDouble("FIRST_REMD_AMT"    , hContFirstRemdAmt);
        setValueDouble("CLT_FEES_AMT"      , hContCltFeesAmt);
        setValueDouble("CLT_UNIT_PRICE"    , hContCltUnitPrice);
        setValueLong("CLT_INSTALL_TOT_TERM", hContCltInstallTotTerm);
        setValueDouble("CLT_REMD_AMT"  , hContCltRemdAmt);
        setValue("BATCH_NO"            , hBillBatchNo);
        setValue("ACQUIRER_MEMBER_ID"  , hBillAcquirerMemberId);
        setValue("MCHT_ENG_NAME"       , hContMerchantEngName);
        setValue("PURCHASE_DATE"       , hBillPurchaseDate);
        setValue("INSTALLMENT_KIND"    , hContInstallmentKind);
        
        //分期入帳時間 (已出對帳單Y:當期) (DB--0:次期;1:當期)
        if ("B".equals(hBillBilledFlag)) {
        	setValue("first_post_kind", "1");
        } else {
        	setValue("first_post_kind", "");
        }
        
        setValue("SALE_EMP_NO"         , hContSaleEmpNo);
        setValue("mod_user"            , hModUser);
        setValue("mod_time"            , sysDate + sysTime);
        setValue("mod_pgm"             , prgmId);
        daoTable = "bil_contract";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_contract duplicate", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void initBilContract() throws Exception {
        hContContractNo = "";
        hContContractSeqNo = 0;
        hContRealCardNo = "";
        hContBackCardNo = "";
        hContNewCardNo = "";
        hContAcctType = "";
        // h_cont_acct_key = "";
        hContStmtCycle = "";
        hContProductNo = "";
        hContProductName = "";
        hContMerchantNo = "";
        hContMerchantChiName = "";
        hContContractKind = "";
        hContAllocateFlag = "";
        hContCvv2 = "";
        hContExchangeAmt = 0;
        hContAgainstNum = 0;
        hContUnitPrice = 0;
        hContQty = 0;
        hContTotAmt = 0;
        hContInstallTotTerm = 0;
        hContRemdAmt = 0;
        hContAutoDelvFlag = "";
        hContFeesFixAmt = 0;
        hContFeesRate = 0;
        hContExtraFees = 0;
        hContFirstPostDate = "";
        hContPostCycleDd = 0;
        hContInstallCurrTerm = 0;
        hContAllPostFlag = "N";
        hContRefundFlag = "";
        hContRefundQty = 0;
        hContRefundConfirmFlag = "";
        hContRefundConfirmDate = "";
        hContConfirmDate = "";
        hContConfirmFlag = "";
        hContReceiveName = "";
        hContReceiveTel = "";
        hContReceiveTel1 = "";
        hContVoucherHead = "";
        hContUniformNo = "";
        hContZipCode = "";
        hContReceiveAddress = "";
        hContDelvDate = "";
        hContDelvConfirmFlag = "";
        hContDelvConfirmDate = "";
        hContRegisterNo = "";
        hContAuthorization = "";
        hContDelvBatchNo = "";
        hContForcedPostFlag = "";
        hContInstallBackTerm = 0;
        hContInstallBackTermFlag = "";
        hContDevFlag20 = "";
        hContPrtFlag21 = "";
        hContLimitEndDate = "";
        hContCltFeesAmt = 0;
        hContCltUnitPrice = 0;
        hContCltInstallTotTerm = 0;
        hContCltRemdAmt = 0;
        hContLimitEndDate = "";
        hContSaleEmpNo = "";
    }
    
    int selectActAcaj() throws Exception {
    	
    	hAcajOrginalAmt = 0;
        hAcajDrAmt = 0;
        hAcajAftAmt = 0;
        hAcajAftDAmt = 0;
        
        sqlCmd = "select orginal_amt,";
        sqlCmd += "dr_amt,";
        sqlCmd += "aft_amt,";
        sqlCmd += "aft_d_amt ";
        sqlCmd += " from act_acaj  ";
        sqlCmd += "where reference_no  = ? ";
        sqlCmd += "  and process_flag  <> 'Y' ";  //非處理過的資料
        setString(1, hDebtReferenceNo);

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        
        hAcajOrginalAmt = hAutxDestinationAmt + getValueDouble("orginal_amt");
        hAcajDrAmt = hAutxDestinationAmt + getValueDouble("dr_amt");
        hAcajAftAmt = getValueDouble("aft_amt") - hAutxDestinationAmt;
        hAcajAftDAmt = getValueDouble("aft_d_amt") - hAutxDestinationAmt;
    	
    	return 0;
    }

    int updateActAcaj() throws Exception {

        daoTable  = "act_acaj";
        updateSQL = "orginal_amt= ? , dr_amt = ? , aft_amt = ? , aft_d_amt = ? "; 
        whereStr  = "where reference_no  = ?  ";
        whereStr += "  and process_flag  <> 'Y' ";  
        setDouble(1, hAcajOrginalAmt);
        setDouble(2, hAcajDrAmt);
        setDouble(3, hAcajAftAmt);
        setDouble(4, hAcajAftDAmt);
        setString(5, hDebtReferenceNo);
        updateTable();
        if (notFound.equals("Y")) {
            showLogMessage("E","","update_act_acaj not found! referenceNo=[" + hDebtReferenceNo + "]"); 
            return -1;
        }
    	
    	return 0;
    }
    
    /**********************************************************************/
    void insertActAcaj() throws Exception {

if(debug ==1) showLogMessage("I",""," insert_act_acaj=["+"14817000"+"]");

        hAcajCreateDate = "";
        hAcajCreateTime = "";
        hAcajPSeqno = "";
        hAcajAcctType = "";
        hAcajAdjustType = "";
        hAcajReferenceNo = "";
        hAcajPostDate = "";
        hAcajOrginalAmt = 0;
        hAcajDrAmt = 0;
        hAcajCrAmt = 0;
        hAcajBefAmt = 0;
        hAcajAftAmt = 0;
        hAcajBefDAmt = 0;
        hAcajAftDAmt = 0;
        hAcajAcctCode = "";
        hAcajFunctionCode = "";
        hAcajCardNo = "";
        hAcajCashType = "";
        hAcajValueType = "";
        hAcajTransAcctType = "";
        hAcajTransAcctKey = "";
        hAcajInterestDate = "";
        hAcajAdjReasonCode = "";
        hAcajAdjComment = "";
        hAcajCDebtKey = "";
        hAcajDebitItem = "";
        hAcajConfirmFlag = "";
        hAcajConfirmDate = "";
        hAcajJrnlDate = "";
        hAcajJrnlTime = "";
        hAcajPaymentType = "";
        hAcajUpdateDate = "";
        hAcajUpdateUser = "";
        hAcajMerchantNo = "";

        hAcajDebitItem = "";
        hAcajReferenceNo = hDebtReferenceNo;
        hAcajPostDate = hBusinessDate;
        hAcajAdjustType = "DE31";

        hAcajCreateDate = hAutxAprDate1;
        hAcajCreateTime = hSystemTime;
        hAcajUpdateDate = hAutxAprDate1;
        hAcajUpdateUser = hAutxAprUser1;

        hAcajPSeqno = hBillPSeqno;
        hAcajAcctType = hDebtAcctType;
        hAcajOrginalAmt = hAutxDestinationAmt;

        hAcajDrAmt = hAutxDestinationAmt;
        hAcajBefAmt = hDebtEndBal;
        hAcajAftAmt = hDebtEndBal - hAutxDestinationAmt;
        hAcajBefDAmt = hDebtDAvailableBal;
        hAcajAftDAmt = hDebtDAvailableBal - hAutxDestinationAmt;

        hAcajAcctCode = hDebtAcctCode;
        hAcajInterestDate = hDebtInterestDate;
        hAcajFunctionCode = "U";
        hAcajCardNo = hAutxCardNo;
        hAcajValueType = "1";
        hAcajConfirmFlag = "Y";
        hAcajConfirmDate = hSystemDate;
        hAcajMerchantNo = hMerchantNo;

        setValue("CRT_DATE", hAcajCreateDate);
        setValue("CRT_TIME", hAcajCreateTime);
        setValue("P_SEQNO" , hAcajPSeqno);
        setValue("ACCT_TYPE", hAcajAcctType);
        setValue("ADJUST_TYPE", hAcajAdjustType);
        setValue("REFERENCE_NO", hAcajReferenceNo);
        setValue("POST_DATE", hAcajPostDate);
        setValueDouble("ORGINAL_AMT", hAcajOrginalAmt);
        setValueDouble("DR_AMT", hAcajDrAmt);
        setValueDouble("CR_AMT", hAcajCrAmt);
        setValueDouble("BEF_AMT", hAcajBefAmt);
        setValueDouble("AFT_AMT", hAcajAftAmt);
        setValueDouble("BEF_D_AMT", hAcajBefDAmt);
        setValueDouble("AFT_D_AMT", hAcajAftDAmt);
        setValue("acct_code", hAcajAcctCode);
        setValue("FUNCTION_CODE", hAcajFunctionCode);
        setValue("CARD_NO", hAcajCardNo);
        setValue("CASH_TYPE", hAcajCashType);
        setValue("VALUE_TYPE", hAcajValueType);
        setValue("TRANS_ACCT_TYPE", hAcajTransAcctType);
        setValue("TRANS_ACCT_KEY", hAcajTransAcctKey);
        setValue("INTEREST_DATE", hAcajInterestDate);
        setValue("ADJ_REASON_CODE", hAcajAdjReasonCode);
        setValue("ADJ_COMMENT", hAcajAdjComment);
        setValue("C_DEBT_KEY" , hAcajCDebtKey);
        setValue("DEBIT_ITEM" , hAcajDebitItem);
        setValue("APR_FLAG"   , hAcajConfirmFlag);
        setValue("APR_DATE"   , hAcajConfirmDate);
        setValue("JRNL_DATE"  , hAcajJrnlDate);
        setValue("JRNL_TIME"  , hAcajJrnlTime);
        setValue("PAYMENT_TYPE", hAcajPaymentType);
        setValue("UPDATE_DATE" , hAcajUpdateDate);
        setValue("UPDATE_USER" , hAcajUpdateUser);
        setValue("MERCHANT_NO" , hAcajMerchantNo);
        setValue("mod_user"    , hModUser);
        setValue("mod_time"    , sysDate + sysTime);
        setValue("mod_pgm"     , prgmId);
        daoTable = "act_acaj";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acaj duplicate", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA035 proc = new BilA035();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
