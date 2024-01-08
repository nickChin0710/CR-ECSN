package Bil;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112/11/16  V0.00.01     JeffKung  initial                                  *
*****************************************************************************/

import com.CommCrd;
import com.CommCrdRoutine;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.AccessDAO;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class BilN030 extends AccessDAO {
	private String PROGNAME = "處理稅款分期匯入檔案 112/11/16 V0.00.01";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate zzdate = new CommDate();
	private int iiFileNum = 0;

	String rptNameN030A = "合庫信用卡分期匯入處理明細表";
    String rptIdN030A = "CRDN030";
    int rptSeqN030A = 0;
    List<Map<String, Object>> lparN030A = new ArrayList<Map<String, Object>>();

    String buf = "";
    String szTmp = "";

    int totalCnt = 0;
    int indexCnt = 0;
    int pageCnt = 0;

    double totalADestAmtDR = 0;
    int totalACntDR = 0;
    double errorADestAmtDR = 0;
    int errorACntDR = 0;
    
    int lineCnt = 0;
	
	String modUser = "";
	
	// ******** inputFile來源 ********//
	String cardNo = "";
	String purchaseDate = "";
	String txnCode = "";
	long totAmt = 0;
	String authCode = "";
	int totTerm = 0;
	String mchtNo = "";
	String productNo = "";
	String txnSource = "";
	String procDate = "";
	String procCode = "";

	String hBusiBusinessDate = "";
	
	String hDebtPSeqno = "";
    String hDebtAcctType = "";
    String hDebtAcctCode = "";
    String hDebtInterestDate = "";
    double hDebtBegBal = 0;
    double hDebtEndBal = 0;
    double hDebtDAvailableBal = 0;

	public void mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			// =====================================
			if (args.length != 0) {
				comc.errExit("Usage : BilN020 ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();
			
			//上傳檔案
			String isFileNames = String.format("INST_UPLD.%8.8s", hBusiBusinessDate);
			readFile(isFileNames);
			
			showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);

			if (pageCnt > 0) {
                String ftpName = String.format("%s.%s_%s", rptIdN030A, sysDate, hBusiBusinessDate);
                String filename = String.format("%s/reports/%s.%s_%s", comc.getECSHOME(), rptIdN030A, sysDate, hBusiBusinessDate);
                comc.writeReport(filename, lparN030A);

                ftpMput(ftpName);
            }

			finalProcess();
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return;
		}

	}

	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = "select business_date ";
		sqlCmd += " from ptr_businday  ";
		sqlCmd += " fetch first 1 rows only ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
		}

	}

	//=============================================================================
	void readFile(String fileName) throws Exception {

		String readData = "";
		String outData = "";
		String tmpstr = "";
		int realCnt = 0;
		int fieldCnt = 0;
		
		procCode = "0000";

		String lsFile = String.format("%s/media/bil/%s", comc.getECSHOME(), fileName);
		showLogMessage("I", "", "file path = [" + lsFile + "]");
		iiFileNum = openInputText(lsFile,"MS950");
		if (iiFileNum == -1) {
			showLogMessage("I", "", String.format("無檔案可處理 [%s]", fileName));
			return;
		}

		while (true) {

			readData = readTextFile(iiFileNum).trim();
			
			procCode = "0000";
			outData = readData;

			if (endFile[iiFileNum].equals("Y"))
				break;
			
			totalCnt++;
			
			if (readData.length() < 10) {
				procCode = "E003"; // 資料格式錯誤
				continue;
			} else {
				realCnt++;
				byte[] bytes = readData.getBytes("MS950");
				cardNo = comc.subMS950String(bytes, 0, 16).trim();
				purchaseDate = comc.subMS950String(bytes, 16, 8).trim();
				txnCode = comc.subMS950String(bytes, 24, 2).trim();
				totAmt = Long.parseLong(comc.subMS950String(bytes, 26, 10).trim());
				authCode = comc.subMS950String(bytes, 40, 6).trim();
				totTerm = Integer.parseInt(comc.subMS950String(bytes, 49, 2).trim());
				productNo = comc.subMS950String(bytes, 49, 2).trim();
				mchtNo = comc.subMS950String(bytes, 51, 12).trim();
				procDate = comc.subMS950String(bytes, 66, 8).trim();
				txnSource = comc.subMS950String(bytes, 74, 8).trim();

				if (comc.isThisDateValid(purchaseDate, "yyyyMMdd")==false) {
					procCode = "E003";  //資料格式錯誤
		    	} else if (comc.isThisDateValid(procDate, "yyyyMMdd")==false) {
					procCode = "E003";  //資料格式錯誤
				} else if (totAmt == 0 || totTerm == 0) {
					procCode = "E003";  //資料格式錯誤
				} else if (chkCardNo(cardNo) == 1) {
					procCode = "E001"; // 卡號無效
				} else if (chkMchtProduct(mchtNo, productNo) == 1) {
					procCode = "E009"; // 分期商品未定義
				} else if ("".equals(chkBilBill(cardNo,authCode,purchaseDate,totAmt))==false) {
					procCode = "E002"; //無交易記錄
				}

				if (realCnt % 500 == 0) {
					showLogMessage("I", "", "Process Count :  " + realCnt);
				}

				if ("0000".equals(procCode)) {
					//處理成功
		            int rc = insertBilContract();
		            if (rc==1) {
		            	procCode = "S001";  //select actAcno notfound
		            } else if (rc==2) {
		            	procCode = "S002";  //bilContract duplicated
		            } else if (rc==0) {
		            	rc = insertActAcaj();
		            	if (rc==1) {
		            		procCode = "S003";  //actAcaj duplicated
		            	} else {
							daoTable = "bil_bill";
							updateSQL = "installment_kind = 'F' ";
							whereStr = "where rowid  = ? ";
							setRowId(1, getValue("bilbill.rowid"));
							updateTable();
							if (notFound.equals("Y")) {
								; // do nothing 不應該發生
							}
		            	}
		            }
				} 
				
				if (indexCnt == 0) {
	                printHeaderN030A();
	            }

	            if (indexCnt > 25) {
	            	//分頁控制
	                lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", "##PPP"));
	                printHeaderN030A();
	                indexCnt = 0;
	            }

	            printDetailN030A();

			}
			
		}

		if (indexCnt != 0) {
			printFooterN030A();
		}
		
		closeInputText(iiFileNum);
		showLogMessage("I", "", "檔案轉入 [" + realCnt + "] 筆");
		renameFile(fileName);
	}

	/**檢查卡號是否有效***/
	int chkCardNo(String cardNo) throws Exception {
		int result = 1;
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT card_no, rowid as rowid ");
		sb.append(" FROM crd_card a ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND card_no = ? ");
		sb.append(" AND current_code = '0' ");
		sqlCmd = sb.toString();
		setString(1, cardNo);
		
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			result=0;
		} 
		
		return result;

	}
	
	/**檢查分期產品是否有效***/
	int chkMchtProduct(String mchtNo, String productNo) throws Exception {
		int result = 1;
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT product_name, installment_flag, ");
		sb.append("        extra_fees, clt_fees_fix_amt, clt_interest_rate, ");
		sb.append("        clt_fees_min_amt, clt_fees_max_amt ");
		sb.append(" FROM bil_prod  ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND mcht_no = ? ");
		sb.append(" AND product_no = ? ");
		sb.append(" AND mcht_no like '10600000000%' ");
		sb.append(" AND mcht_no not in ('106000000005','106000000007') ");  //長循/帳單分期不適用
		sqlCmd = sb.toString();
		setString(1, mchtNo);
		setString(2, productNo);
		
		extendField = "bilprod.";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			result=0;
		} 
		
		return result;

	}
	
	/***********************************************************************/
    String chkBilBill(String cardNo, String authCode, String purchaseDate, long totAmt) throws Exception {
    	
    	String chkCode = "";

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
        sqlCmd += "installment_kind,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from bil_bill  ";
        sqlCmd += "where card_no          = ?  ";
        sqlCmd += "  and auth_code        = ?  ";
        sqlCmd += "  and purchase_date    = ?  ";
        sqlCmd += "  and txn_code         = '05'  ";
        sqlCmd += "  and acct_code        = 'BL'  ";
        sqlCmd += "  and installment_kind = ''  ";  //可申請分期 (有值表示已申請過分期)
        sqlCmd += "  and decode(cash_pay_amt,0,dest_amt,cash_pay_amt) = ?  ";
        sqlCmd += "  and rsk_type not in ('1','2','3')  ";

        setString(1, cardNo);
        setString(2, authCode);
        setString(3, purchaseDate);
        setDouble(4, ((double) totAmt));
        
        extendField = "bilbill.";
        int recordCnt = selectTable();
        
        if (notFound.equals("Y")) {
        	chkCode = "E002"; //無交易記錄可分期
        	return chkCode;
        }
        
        if (selectActDebt() == false) {
        	chkCode = "E002"; //無交易記錄可分期
        	return chkCode;
        }
        
        return chkCode;
		
    }

    /***********************************************************************/
    boolean selectActDebt() throws Exception {
        hDebtPSeqno = "";
        hDebtAcctType = "";
        hDebtBegBal = 0;
        hDebtEndBal = 0;
        hDebtDAvailableBal = 0;
        hDebtAcctCode = "";
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
        setString(1, getValue("bilbill.reference_no"));
        
        extendField = "actdebt.";
        int recordCnt = selectTable();
        
        if (notFound.equals("Y")) {
            return selectActDebtHst();
        }

		hDebtPSeqno = getValue("actdebt.acno_p_seqno");
		hDebtAcctType = getValue("actdebt.acct_type");
		hDebtBegBal = getValueDouble("actdebt.beg_bal");
		hDebtEndBal = getValueDouble("actdebt.end_bal");
		hDebtDAvailableBal = getValueDouble("actdebt.d_avail_bal");
		hDebtAcctCode = getValue("actdebt.acct_code");
		hDebtInterestDate = getValue("actdebt.interest_date");

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
        hDebtInterestDate = "";

        sqlCmd = "select acno_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "beg_bal,";
        sqlCmd += "end_bal,";
        sqlCmd += "d_avail_bal,";
        sqlCmd += "acct_code,";
        sqlCmd += "interest_date ";
        sqlCmd += " from act_debt_hst  ";
        sqlCmd += "where reference_no = ?  ";
        sqlCmd += "and d_avail_bal = beg_bal ";
        setString(1, getValue("bilbill.reference_no"));
        
        extendField = "actdebthst.";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return false;
        }

		hDebtPSeqno = getValue("actdebthst.acno_p_seqno");
		hDebtAcctType = getValue("actdebthst.acct_type");
		hDebtBegBal = getValueDouble("actdebthst.beg_bal");
		hDebtEndBal = getValueDouble("actdebthst.end_bal");
		hDebtDAvailableBal = getValueDouble("actdebthst.d_avail_bal");
		hDebtAcctCode = getValue("actdebthst.acct_code");
		hDebtInterestDate = getValue("actdebthst.interest_date");

		return true;
	}

    /**取得特店資訊***/
	int getMchtName() throws Exception {
		int result = 1;
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT mcht_chi_name, mcht_eng_name, mcht_type ");
		sb.append(" FROM bil_merchant  ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND mcht_no = ? ");
		sqlCmd = sb.toString();
		setString(1, mchtNo);
		
		extendField = "bilmerchant.";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			result=0;
		} 
		
		return result;

	}
    /***********************************************************************/
    int insertBilContract() throws Exception {
    	
    	int rc = 0;
    	String hContContractNo = "";
        int hContContractSeqNo = 0;
        String hContRealCardNo = "";
        String hContBackCardNo = "";
        String hContNewCardNo = "";
        String hContAcctType = "";
        String hContStmtCycle = "";
        String hContProductNo = "";
        String hContProductName = "";
        String hContMerchantNo = "";
        String hContMerchantChiName = "";
        String hContContractKind = "";
        String hContAllocateFlag = "";
        String hContCvv2 = "";
        double hContExchangeAmt = 0;
        int hContAgainstNum = 0;
        long hContUnitPrice = 0;
        int hContQty = 0;
        double hContTotAmt = 0;
        long hContInstallTotTerm = 0;
        double hContFirstRemdAmt = 0;
        double hContRemdAmt = 0;
        String hContAutoDelvFlag = "";
        String hContFeeFlag = "";
        double hContFeesFixAmt = 0;
        double hContFeesRate = 0;
        double hContExtraFees = 0;
        String hContFirstPostDate = "";
        long hContPostCycleDd = 0;
        long hContInstallCurrTerm = 0;
        String hContAllPostFlag = "N";
        String hContRefundFlag = "";
        long hContRefundQty = 0;
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
        long hContInstallBackTerm = 0;
        String hContInstallBackTermFlag = "";
        String hContDevFlag20 = "";
        String hContPrtFlag21 = "";
        String hContLimitEndDate = "";
        double hContCltFeesAmt = 0;
        double hContCltUnitPrice = 0;
        long hContCltInstallTotTerm = 0;
        double hContCltRemdAmt = 0;
        String hContPurchaseDate = "";
        String hContSaleEmpNo = "";
        String hContFilmNo = "";
        String hContReferenceNo = "";
        String hContMerchantEngName = "";
        String hContInstallmentKind = "";
        String hContCpsFlag = "N";
        String hContPaymentType = "E"; //費用外加
        
        String tempX10 = "";
        int    tempInt = 0;
        long   tempLong = 0;
        double tempDouble = 0;

        sqlCmd = "select substr(to_char(bil_contractseq.nextval,'0000000000'),2,10) temp_x10 ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempX10 = getValue("temp_x10");
        }

        hContContractNo = tempX10;
        hContContractSeqNo = 1;
        hContRealCardNo = cardNo;
        hContMerchantNo = mchtNo;
        hContProductNo = productNo;
        hContAuthorization = authCode;
        hContProductName = getValue("bilprod.product_name");
        hContFeesRate = 0;
        hContAgainstNum = 0;
        hContAutoDelvFlag = "Y";
        hContAcctType = getValue("bilbill.acct_type");

        sqlCmd = "select stmt_cycle ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acct_type    = ?  ";
        sqlCmd += "  and acno_p_seqno = ? ";
        setString(1, hContAcctType);
        setString(2, getValue("bilbill.acno_p_seqno"));
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	//回傳錯誤
        	rc = 1;
        	showLogMessage("E","",String.format("select_act_acno not found,cardNo=[%s]",cardNo));
        	return rc;
        }
        if (recordCnt > 0) {
            hContStmtCycle = getValue("stmt_cycle");
        }

        hContContractKind = "1";
        hContConfirmDate = hBusiBusinessDate;
        hContPurchaseDate = purchaseDate;
        hContDelvDate = hBusiBusinessDate;
        hContDelvConfirmDate = hBusiBusinessDate;
        hContDelvConfirmFlag = "Y";
        hContConfirmFlag = "Y";
        hContFeeFlag = "F";
        hContFilmNo = getValue("bilbill.film_no");
        hContReferenceNo = getValue("bilbill.reference_no");
        
        //取得特店資訊
        if (getMchtName()==0) {
        	hContMerchantEngName = getValue("bilmerchant.mcht_eng_name");
        	hContMerchantChiName = getValue("bilmerchant.mcht_chi_name");
        } else {
        	hContMerchantChiName = getValue("bilprod.product_name");
        }

        hContQty = 1;
        hContInstallTotTerm = totTerm;
        tempLong = (long) (totAmt / totTerm);
        hContUnitPrice = tempLong;

        hContTotAmt = totAmt;
        hContFirstRemdAmt = hContTotAmt - hContInstallTotTerm * hContUnitPrice;
        /* ECS特店 : N ,nccc收單 : C , onus收單 :Y */
        hContCpsFlag = "N";

        hContCltInstallTotTerm = 1;
        hContCltRemdAmt = 0;
        tempDouble = totAmt 
        		   * getValueDouble("bilprod.clt_interest_rate") 
        		   / 100 
        		   + getValueDouble("bilprod.clt_fees_fix_amt");
        tempLong = (long) (tempDouble + 0.5);
        hContCltFeesAmt = tempLong;

        if (hContCltFeesAmt > getValueDouble("bilprod.clt_fees_max_amt"))
            hContCltFeesAmt = getValueDouble("bilprod.clt_fees_max_amt");
        if (hContCltFeesAmt < getValueDouble("bilprod.clt_fees_min_amt"))
            hContCltFeesAmt = getValueDouble("bilprod.clt_fees_min_amt");

        if (getValue("bilprod.installment_flag").toUpperCase(Locale.TAIWAN).equals("Y")) {
            hContCltInstallTotTerm = hContInstallTotTerm;
            tempInt = (int) (hContCltFeesAmt / hContInstallTotTerm);
            hContCltUnitPrice = tempInt;
            hContCltRemdAmt = hContCltFeesAmt - (hContInstallTotTerm * tempInt);
        } else {
            hContCltInstallTotTerm = 1;
            hContCltUnitPrice = hContCltFeesAmt;
            hContCltRemdAmt = 0;
        }
        
        hContExtraFees = getValueDouble("bilprod.extra_fees");

        setValue("CONTRACT_NO"              , hContContractNo);
        setValueLong("CONTRACT_SEQ_NO"      , hContContractSeqNo);
        setValue("CARD_NO"                  , hContRealCardNo);
        setValue("BACK_CARD_NO"             , hContBackCardNo);
        setValue("NEW_CARD_NO"              , hContNewCardNo);
        setValue("ACCT_TYPE"                , hContAcctType);
        setValue("p_seqno"                  , getValue("bilbill.p_seqno"));
        setValue("acno_p_seqno"             , getValue("bilbill.acno_p_seqno"));
        setValue("id_p_seqno"               , getValue("bilbill.id_p_seqno"));
        setValue("STMT_CYCLE"               , hContStmtCycle);
        setValue("PRODUCT_NO"               , hContProductNo);
        setValue("PRODUCT_NAME"             , hContProductName);
        setValue("MCHT_NO"                  , hContMerchantNo);
        setValue("MCHT_CHI_NAME"            , hContMerchantChiName);
        setValue("CONTRACT_KIND"            , hContContractKind);
        setValue("ALLOCATE_FLAG"            , hContAllocateFlag);
        setValue("CVV2"                     , hContCvv2);
        setValueDouble("EXCHANGE_AMT"       , hContExchangeAmt);
        setValueInt("AGAINST_NUM"           , hContAgainstNum);
        setValueDouble("UNIT_PRICE"         , hContUnitPrice);
        setValueInt("QTY"                   , hContQty);
        setValueDouble("TOT_AMT"            , hContTotAmt);
        setValueLong("INSTALL_TOT_TERM"     , hContInstallTotTerm);
        setValueDouble("REMD_AMT"           , hContRemdAmt);
        setValue("AUTO_DELV_FLAG"           , hContAutoDelvFlag);
        setValueDouble("FEES_FIX_AMT"       , hContFeesFixAmt);
        setValueDouble("FEES_RATE"          , hContFeesRate);
        setValueDouble("EXTRA_FEES"         , hContExtraFees);
        setValue("FIRST_POST_DATE"          , hContFirstPostDate);
        setValueLong("POST_CYCLE_DD"        , hContPostCycleDd);
        setValueLong("INSTALL_CURR_TERM"    , hContInstallCurrTerm);
        setValue("ALL_POST_FLAG"            , hContAllPostFlag);
        setValue("REFUND_FLAG"              , hContRefundFlag);
        setValueLong("REFUND_QTY"           , hContRefundQty);
        setValue("REFUND_APR_FLAG"          , hContRefundConfirmFlag);
        setValue("REFUND_APR_DATE"          , hContRefundConfirmDate);
        setValue("APR_DATE"                 , hContConfirmDate);
        setValue("APR_FLAG"                 , hContConfirmFlag);
        setValue("RECEIVE_NAME"             , hContReceiveName);
        setValue("RECEIVE_TEL"              , hContReceiveTel);
        setValue("RECEIVE_TEL1"             , hContReceiveTel);
        setValue("VOUCHER_HEAD"             , hContVoucherHead);
        setValue("UNIFORM_NO"               , hContUniformNo);
        setValue("ZIP_CODE"                 , hContZipCode);
        setValue("RECEIVE_ADDRESS"          , hContReceiveAddress);
        setValue("DELV_DATE"                , hContDelvDate);
        setValue("DELV_CONFIRM_FLAG"        , hContDelvConfirmFlag);
        setValue("DELV_CONFIRM_DATE"        , hContDelvConfirmDate);
        setValue("REGISTER_NO"              , hContRegisterNo);
        setValue("AUTH_CODE"                , hContAuthorization);
        setValue("DELV_BATCH_NO"            , hContDelvBatchNo);
        setValue("FORCED_POST_FLAG"         , hContForcedPostFlag);
        setValueLong("INSTALL_BACK_TERM"    , hContInstallBackTerm);
        setValue("INSTALL_BACK_TERM_FLAG"   , hContInstallBackTermFlag);
        setValue("DEV_FLAG_20"              , hContDevFlag20);
        setValue("PRT_FLAG_21"              , hContPrtFlag21);
        setValue("CPS_FLAG"                 , hContCpsFlag);
        setValue("LIMIT_END_DATE"           , hContLimitEndDate);
        setValue("FEE_FLAG"                 , hContFeeFlag);
        setValue("FILM_NO"                  , hContFilmNo);
        setValue("REFERENCE_NO"             , hContReferenceNo);
        setValue("PAYMENT_TYPE"             , hContPaymentType);
        setValueDouble("FIRST_REMD_AMT"     , hContFirstRemdAmt);
        setValueDouble("CLT_FEES_AMT"       , hContCltFeesAmt);
        setValueDouble("CLT_UNIT_PRICE"     , hContCltUnitPrice);
        setValueLong("CLT_INSTALL_TOT_TERM" , hContCltInstallTotTerm);
        setValueDouble("CLT_REMD_AMT"       , hContCltRemdAmt);
        setValue("BATCH_NO"                 , getValue("bilbill.batch_no"));
        setValue("ACQUIRER_MEMBER_ID"       , getValue("bilbill.acq_member_id"));
        setValue("MCHT_ENG_NAME"            , hContMerchantEngName);
        setValue("PURCHASE_DATE"            , purchaseDate);
        setValue("INSTALLMENT_KIND"         , hContInstallmentKind);
        
        //分期入帳時間 (已出對帳單Y:當期) (DB--0:次期;1:當期)
        if ("B".equals(getValue("bilbill.h_bill_billed_flag"))) {
        	setValue("first_post_kind"      , "1");
        } else {
        	setValue("first_post_kind"      , "");
        }
        
        setValue("SALE_EMP_NO"              , hContSaleEmpNo);
        setValue("mod_user"                 , "system");
        setValue("mod_time"                 , sysDate + sysTime);
        setValue("mod_pgm"                  , javaProgram);
        daoTable = "bil_contract";
        insertTable();
        if (dupRecord.equals("Y")) {
        	//資料重覆回傳錯誤
        	rc = 2;
        	showLogMessage("E","",String.format("insert_bil_contract duplicate,cardNo=[%s]",cardNo));
        }
        
        return rc;
    }

    /**********************************************************************/
    int insertActAcaj() throws Exception {

    	int rc = 0;
        setValue("CRT_DATE", hBusiBusinessDate);
        setValue("CRT_TIME", sysTime);
        setValue("P_SEQNO" , getValue("bilbill.p_seqno"));
        setValue("ACCT_TYPE", getValue("bilbill.acct_type"));
        setValue("ADJUST_TYPE", "DE31");
        setValue("REFERENCE_NO", getValue("bilbill.reference_no"));
        setValue("POST_DATE", hBusiBusinessDate);
        setValueDouble("ORGINAL_AMT", totAmt);
        setValueDouble("DR_AMT", totAmt);
        setValueDouble("CR_AMT", 0);
        setValueDouble("BEF_AMT", hDebtEndBal);
        setValueDouble("AFT_AMT", hDebtEndBal - totAmt);
        setValueDouble("BEF_D_AMT", hDebtDAvailableBal);
        setValueDouble("AFT_D_AMT", hDebtDAvailableBal - totAmt);
        setValue("acct_code", hDebtAcctCode);
        setValue("FUNCTION_CODE", "U");
        setValue("CARD_NO", cardNo);
        setValue("CASH_TYPE", "");
        setValue("VALUE_TYPE", "1");
        setValue("TRANS_ACCT_TYPE", getValue("bilbill.acct_type"));
        setValue("TRANS_ACCT_KEY", "");
        setValue("INTEREST_DATE", hDebtInterestDate);
        setValue("ADJ_REASON_CODE", "");
        setValue("ADJ_COMMENT", "申請分期付款");
        setValue("C_DEBT_KEY" , "");
        setValue("DEBIT_ITEM" , "");
        setValue("APR_FLAG"   , "Y");
        setValue("APR_DATE"   , hBusiBusinessDate);
        setValue("JRNL_DATE"  , "");
        setValue("JRNL_TIME"  , "");
        setValue("PAYMENT_TYPE", "");
        setValue("UPDATE_DATE" , hBusiBusinessDate);
        setValue("UPDATE_USER" , "system");
        setValue("MERCHANT_NO" , mchtNo);
        setValue("mod_user"    , "system");
        setValue("mod_time"    , sysDate + sysTime);
        setValue("mod_pgm"     , javaProgram);
        daoTable = "act_acaj";
        insertTable();
        if (dupRecord.equals("Y")) {
        	rc = 1;
            showLogMessage("E","",String.format("insert_act_acaj duplicate,cardNo=[%s]",cardNo));
        }
        return rc;
    }

	
    /***********************************************************************/
    void printHeaderN030A() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdN030A, 1);
        buf = comcr.insertStrCenter(buf, rptNameN030A, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);

        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));

        buf = "";
        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "入帳日期", 1);
        buf = comcr.insertStr(buf, "卡    號", 12);
        buf = comcr.insertStr(buf, "消費日期", 30);
        buf = comcr.insertStr(buf, "交易碼", 41);
        buf = comcr.insertStr(buf, "分期總金額", 51);
        buf = comcr.insertStr(buf, "授權碼", 66);
        buf = comcr.insertStr(buf, "分期特店", 76);
        buf = comcr.insertStr(buf, "分期期數", 90);
        buf = comcr.insertStr(buf, "來源", 103);
        buf = comcr.insertStr(buf, "處理結果", 113);
        
        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));
    }

    /***********************************************************************/
    void printFooterN030A() {
    	buf = "";
        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));
        
    	buf = "";
    	lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "成功筆數:", 6);
    	szTmp = String.format("%5d", totalACntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalADestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);

    	lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "失敗筆數:", 6);
    	szTmp = String.format("%5d", errorACntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorADestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));
    	
    	buf = "";
        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));

        buf = "處理結果: 0000-成功 ; E001-卡號無效 ; E002-無交易記錄 ; E003-資料格式錯誤 ; E004-重覆分期 ; E009-分期商品未定義	; S001-S003系統問題 ";
        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));

        buf = "    來源: 22222222:一般  ; 22222223-25:台灣PAY/EPAY";
        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));

    }

    /***********************************************************************/
    void printDetailN030A() throws Exception {
        lineCnt++;
        indexCnt++;
        //card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code
        buf = "";
        buf = comcr.insertStr(buf, procDate, 1);
        buf = comcr.insertStr(buf, cardNo, 12);
        buf = comcr.insertStr(buf, purchaseDate, 30);
        buf = comcr.insertStr(buf, txnCode, 43);
        szTmp = comcr.commFormat("3z,3z,3#", totAmt);
        buf = comcr.insertStr(buf, szTmp, 51);
        buf = comcr.insertStr(buf, authCode, 66);
        buf = comcr.insertStr(buf, mchtNo, 76);
        buf = comcr.insertStr(buf, productNo, 93);
        buf = comcr.insertStr(buf, txnSource, 101);
        buf = comcr.insertStr(buf, procCode, 114);

		if ("0000".equals(procCode)) {
			totalADestAmtDR += totAmt;
			totalACntDR++;
		} else {
			errorADestAmtDR += totAmt;
			errorACntDR++;
		}

        lparN030A.add(comcr.putReport(rptIdN030A, rptNameN030A, sysDate, ++rptSeqN030A, "0", buf));

    }


    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "CREDITCARD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "CREDITCARD";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            showLogMessage("I","",String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode));
        }
        return (0);
    }

    //=============================================================================
	void renameFile(String fileName) throws Exception {
		String tmpstr1 = String.format("%s/media/bil/%s", comc.getECSHOME(), fileName);
		String tmpstr2 = String.format("%s/media/bil/backup/%s.%-8.8s", comc.getECSHOME(), fileName, sysDate);

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("E", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
	}

	public static void main(String[] args) throws Exception {
		BilN030 proc = new BilN030();
		proc.mainProcess(args);
		return;
	}
	// ************************************************************************

}
