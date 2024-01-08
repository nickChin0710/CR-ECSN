package Tmp;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112/02/16  V0.00.01     JeffKung  initial                                  *
*****************************************************************************/

import com.CommCrd;
import com.CommCrdRoutine;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.AccessDAO;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class TmpB010 extends AccessDAO {
	private String PROGNAME = "處理分期特店參數 112/02/16 V0.00.01";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate zzdate = new CommDate();

	int    totalCnt = 0;
	String modUser = "";
	// ******** nccc來源 ********//
	String npMchtNoM = "";
	String npMchtNo = "";
	String npMccCode = "";
	String npStartDate = "";
	String npTotTerm = "";
	String npEndDate = "";
	String npFeeCat = "";
	Double npInterestRate = 0.0;
	String npBinNoStart = "";
	String npBinNoEnd = "";
	String npMchtChiName = "";
	String npMchtRegiName = "";
	String npCorpNo = "";
	String npBizAddr = "";
	String npProcessRC = "";
	String npStmtInstFlag = "";
	String npLoanFlag = "";
	String npTransFlag = "";
	String npInstallmentDelay = ""; 
	
	int    npTotalTerm = 0;
	String npProductName = "";
	int    npExtraFees = 0;
	double npTransRate = 0.0;
	double npFeesRate = 0.0;
	String npDestAmt = "";


	String hBusiBusinessDate = "";

	public void mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			// =====================================
			if (args.length != 0) {
				comc.errExit("Usage : TmpB010 ", "");
			}

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();

			showLogMessage("I", "", "=========================================");
			showLogMessage("I", "", "刪除分期特店資料...");
			deleteBilMerchant();
			commitDataBase();
			showLogMessage("I", "", "=========================================");
			showLogMessage("I", "", "處理分期特店資料...");
			processBilMerchant_001();
			processBilMerchant_002();
			processBilMerchant_003();
			processBilMerchant_004();
			processBilMerchant_005();
			processBilMerchant_006();
			processBilMerchant_007();
			processBilMerchant_008();
			processBilMerchant_009();
			showLogMessage("I", "", "=========================================");
			showLogMessage("I","","處理 ["+totalCnt+"] 筆");
			
			processPtrAssignInstallment();
			
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
	void processBilMerchant_001() throws Exception {

		totalCnt++;
		npMchtNo = "106000000001";
		npCorpNo = "99498865";
		npStmtInstFlag = "N"; // 對帳單分期註記 (Y/N)
		npMccCode = "6012";
		npMchtChiName = "單筆分期 (來電輕鬆分)";
		npLoanFlag = "N"; // 貸款特店旗標
		npTransFlag = "N"; // 轉換機制送JCIC旗標
		npInstallmentDelay = "N"; // 分期入帳時間 Y:當期;N:次期

		insertBilMerchant();
		
		//期數資料
		npTotalTerm = 6;
		npProductName = "來電分期輕鬆購";
		npExtraFees = 100;
		npTransRate = 6;
		npFeesRate = 6.35;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
		
		npTotalTerm = 12;
		npProductName = "來電分期輕鬆購";
		npExtraFees = 200;
		npTransRate = 8;
		npFeesRate = 8.38;
		insertBilProd();
		
		npDestAmt = "6000";
		insertBilAutoParm();
		
	}
	
	void processBilMerchant_002() throws Exception {

		totalCnt++;
		npMchtNo = "106000000002";
		npCorpNo = "99498865";
		npStmtInstFlag = "N"; // 對帳單分期註記 (Y/N)
		npMccCode = "9311";
		npMchtChiName = "綜所稅分期";
		npLoanFlag = "N"; // 貸款特店旗標
		npTransFlag = "N"; // 轉換機制送JCIC旗標
		npInstallmentDelay = "N"; // 分期入帳時間 Y:當期;N:次期

		insertBilMerchant();
		
		//期數資料
		npTotalTerm = 3;
		npProductName = "綜所稅分期";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate  = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();

		npTotalTerm = 6;
		npProductName = "綜所稅分期";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate  = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();

		npTotalTerm = 9;
		npProductName = "綜所稅分期";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate  = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();

		npTotalTerm = 12;
		npProductName = "綜所稅分期";
		npExtraFees = 0;
		npTransRate = 5;
		npFeesRate  = 5;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
	}

	void processBilMerchant_003() throws Exception {
		
		totalCnt++;
		npMchtNo = "106000000003";
		npCorpNo = "99498865";
		npStmtInstFlag = "N"; // 對帳單分期註記 (Y/N)
		npMccCode = "9399";
		npMchtChiName = "學雜費分期";
		npLoanFlag = "N"; // 貸款特店旗標
		npTransFlag = "N"; // 轉換機制送JCIC旗標
		npInstallmentDelay = "N"; // 分期入帳時間 Y:當期;N:次期

		insertBilMerchant();
		
		//期數資料
		npTotalTerm = 3;
		npProductName = "學費輕鬆繳";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate  = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();

		npTotalTerm = 6;
		npProductName = "學費輕鬆繳";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate  = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();

		npTotalTerm = 12;
		npProductName = "學費輕鬆繳";
		npExtraFees = 0;
		npTransRate = 5;
		npFeesRate  = 5;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
	}

	void processBilMerchant_004() throws Exception {
		
		totalCnt++;
		npMchtNo = "106000000004";
		npCorpNo = "99498865";
		npStmtInstFlag = "N"; // 對帳單分期註記 (Y/N)
		npMccCode = "9311";
		npMchtChiName = "稅費規費分期";
		npLoanFlag = "N"; // 貸款特店旗標
		npTransFlag = "N"; // 轉換機制送JCIC旗標
		npInstallmentDelay = "N"; // 分期入帳時間 Y:當期;N:次期

		insertBilMerchant();
		
		//期數資料
		npTotalTerm = 3;
		npProductName = "稅費輕鬆繳";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate  = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
		
		npTotalTerm = 5;
		npProductName = "稅費輕鬆繳";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate  = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
		
		npTotalTerm = 6;
		npProductName = "稅費輕鬆繳";
		npExtraFees = 0;
		npTransRate = 3;
		npFeesRate  = 3;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
		
		npTotalTerm = 12;
		npProductName = "稅費輕鬆繳";
		npExtraFees = 0;
		npTransRate = 5;
		npFeesRate  = 5;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
	}

	void processBilMerchant_005() throws Exception {
		
		totalCnt++;
		npMchtNo = "106000000005";
		npCorpNo = "99498865";
		npStmtInstFlag = "N"; // 對帳單分期註記 (Y/N)
		npMccCode = "6012";
		npMchtChiName = "分期償還 (長循轉分期)";
		npLoanFlag = "Y"; // 貸款特店旗標
		npTransFlag = "Y"; // 轉換機制送JCIC旗標
		npInstallmentDelay = "Y"; // 分期入帳時間 Y:當期;N:次期

		insertBilMerchant();
		
		//期數資料
		npTotalTerm = 6;
		npProductName = "長循轉分期";
		npExtraFees = 0;
		npTransRate = 10;
		npFeesRate  = 10;
		insertBilProd();

		npTotalTerm = 12;
		npProductName = "長循轉分期";
		npExtraFees = 0;
		npTransRate = 10;
		npFeesRate  = 10;
		insertBilProd();

		npTotalTerm = 18;
		npProductName = "長循轉分期";
		npExtraFees = 0;
		npTransRate = 10;
		npFeesRate  = 10;
		insertBilProd();

		npTotalTerm = 24;
		npProductName = "長循轉分期";
		npExtraFees = 0;
		npTransRate = 10;
		npFeesRate  = 10;
		insertBilProd();
		
		npTotalTerm = 30;
		npProductName = "長循轉分期";
		npExtraFees = 0;
		npTransRate = 10;
		npFeesRate  = 10;
		insertBilProd();
		
		npTotalTerm = 36;
		npProductName = "長循轉分期";
		npExtraFees = 0;
		npTransRate = 10;
		npFeesRate  = 10;
		insertBilProd();
	}

	void processBilMerchant_006() throws Exception {

		totalCnt++;
		npMchtNo = "106000000006";
		npCorpNo = "99498865";
		npStmtInstFlag = "N"; // 對帳單分期註記 (Y/N)
		npMccCode = "4812";
		npMchtChiName = "旅遊分期";
		npLoanFlag = "N"; // 貸款特店旗標
		npTransFlag = "N"; // 轉換機制送JCIC旗標
		npInstallmentDelay = "Y"; // 分期入帳時間 Y:當期;N:次期

		insertBilMerchant();
		
		//期數資料
		npTotalTerm = 3;
		npProductName = "旅遊分期";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
		
		npTotalTerm = 6;
		npProductName = "旅遊分期";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
		
		npTotalTerm = 12;
		npProductName = "旅遊分期";
		npExtraFees = 200;
		npTransRate = 6.66;
		npFeesRate = 6.66;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
	}

	void processBilMerchant_007() throws Exception {
		
		totalCnt++;
		npMchtNo = "106000000007";
		npCorpNo = "99498865";
		npStmtInstFlag = "Y"; // 對帳單分期註記 (Y/N)
		npMccCode = "4812";
		npMchtChiName = "帳單分期";
		npLoanFlag = "Y"; // 貸款特店旗標
		npTransFlag = "N"; // 轉換機制送JCIC旗標
		npInstallmentDelay = "Y"; // 分期入帳時間 Y:當期;N:次期

		insertBilMerchant();
		
		//期數資料
		npTotalTerm = 6;
		npProductName = "帳單分期";
		npExtraFees = 0;
		npTransRate = 10;
		npFeesRate  = 10;
		insertBilProd();

		npTotalTerm = 12;
		npProductName = "帳單分期";
		npExtraFees = 0;
		npTransRate = 11;
		npFeesRate  = 11;
		insertBilProd();

		npTotalTerm = 18;
		npProductName = "帳單分期";
		npExtraFees = 0;
		npTransRate = 13;
		npFeesRate  = 13;
		insertBilProd();

		npTotalTerm = 24;
		npProductName = "帳單分期";
		npExtraFees = 0;
		npTransRate = 14;
		npFeesRate  = 14;
		insertBilProd();
	}

	void processBilMerchant_008() throws Exception {
		
		totalCnt++;
		npMchtNo = "106000000008";
		npCorpNo = "99498865";
		npStmtInstFlag = "N"; // 對帳單分期註記 (Y/N)
		npMccCode = "4812";
		npMchtChiName = "零利率分期(特店分期)";
		npLoanFlag = "N"; // 貸款特店旗標
		npTransFlag = "N"; // 轉換機制送JCIC旗標
		npInstallmentDelay = "Y"; // 分期入帳時間 Y:當期;N:次期

		insertBilMerchant();
		
		//期數資料
		npTotalTerm = 5;
		npProductName = "指定特店０利率";
		npExtraFees = 0;
		npTransRate = 0;
		npFeesRate  = 0;
		insertBilProd();
		
		npDestAmt = "3000";
		insertBilAutoParm();
	}

	void processBilMerchant_009() throws Exception {

		totalCnt++;
		npMchtNo = "106000000009";
		npCorpNo = "99498865";
		npStmtInstFlag = "N"; // 對帳單分期註記 (Y/N)
		npMccCode = "4812";
		npMchtChiName = "想分就分";
		npLoanFlag = "N"; // 貸款特店旗標
		npTransFlag = "N"; // 轉換機制送JCIC旗標
		npInstallmentDelay = "N"; // 分期入帳時間 Y:當期;N:次期

		insertBilMerchant();
		
		//期數資料
		npTotalTerm = 6;
		npProductName = "想分就分";
		npExtraFees = 0;
		npTransRate = 6.66;
		npFeesRate = 6.66;
		insertBilProd();
		
		npDestAmt = "20000";
		insertBilAutoParm();

		npTotalTerm = 12;
		npProductName = "想分就分";
		npExtraFees = 0;
		npTransRate = 6.66;
		npFeesRate = 6.66;
		insertBilProd();
		
		npDestAmt = "20000";
		insertBilAutoParm();
	}
	
	void processPtrAssignInstallment() throws Exception {

		daoTable = "PTR_ASSIGN_INSTALLMENT";
		setValueInt("SEQ_NO", 1);
		setValue("START_DATE", "20220101");
		setValue("END_DATE", "20991231");
		setValue("RESERVE_TYPE", "3");
		setValueDouble("AMT_FROM", 3000);
		setValueDouble("AMT_TO", 999999999);
		setValue("MCHT_NO","106000000003");
		setValue("DENY_MX", "2");
		setValue("INSURANCE_12_TERM_FLAG", "");  
		setValue("TWD_LIMIT_FLAG", "N");
		setValue("CRT_USER", "system");
		setValue("CRT_DATE", sysDate);
		setValue("APR_USER", "system");
		setValue("APR_DATE", sysDate);
		setValue("MOD_USER", "system");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		setValueInt("MOD_SEQNO", 0);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert ptr_assign_installment ERROR : mcht_no=[106000000003]");
		}
		
		daoTable = "PTR_ASSIGN_INSTALLMENT";
		setValueInt("SEQ_NO", 2);
		setValue("START_DATE", "20220101");
		setValue("END_DATE", "20991231");
		setValue("RESERVE_TYPE", "2");
		setValueDouble("AMT_FROM", 3000);
		setValueDouble("AMT_TO", 999999999);
		setValue("MCHT_NO","106000000004");
		setValue("DENY_MX", "2");
		setValue("INSURANCE_12_TERM_FLAG", "");  
		setValue("TWD_LIMIT_FLAG", "N");
		setValue("CRT_USER", "system");
		setValue("CRT_DATE", sysDate);
		setValue("APR_USER", "system");
		setValue("APR_DATE", sysDate);
		setValue("MOD_USER", "system");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		setValueInt("MOD_SEQNO", 0);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert ptr_assign_installment ERROR : mcht_no=[106000000004]");
		}
	}

	//=============================================================================
	void insertBilProd() throws Exception {
		
		daoTable = "BIL_PROD";
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("PRODUCT_NAME", npProductName);
		setValue("MCHT_NO", npMchtNo);
		//setValueDouble("UNIT_PRICE",(int) 10000/npTotalTerm );
		//setValueDouble("TOT_AMT",10000 );
		
		setValueDouble("UNIT_PRICE", 0.0);
		setValueDouble("TOT_AMT", 0.0);
		setValueInt("TOT_TERM", npTotalTerm);
		setValueDouble("REMD_AMT", 0.0);
		setValueDouble("EXTRA_FEES", npExtraFees);
		setValueDouble("FEES_FIX_AMT", 0.0);
		setValueDouble("FEES_MIN_AMT", 0.0);
		setValueDouble("FEES_MAX_AMT", 99999);
		setValueDouble("INTEREST_RATE", 0.0);
		setValueDouble("INTEREST_MIN_RATE", 0.0);
		setValueDouble("INTEREST_MAX_RATE", 0.0);
		setValue("AUTO_DELV_FLAG","Y");
		setValue("AUTO_PRINT_FLAG","N");
		setValueInt("AGAINST_NUM", 0);
		setValue("CONFIRM_FLAG", "Y");
		setValueDouble("CLT_FEES_FIX_AMT", 0.0);
		setValueDouble("CLT_INTEREST_RATE", 0.0);
		setValueDouble("CLT_FEES_MIN_AMT", 0.0);
		setValueDouble("CLT_FEES_MAX_AMT", 99999);
		setValue("INSTALLMENT_FLAG", "N");
		setValue("REFUND_FLAG", "N");
		setValue("DTL_FLAG", "");
		setValueDouble("YEAR_FEES_RATE", npFeesRate);
		setValueDouble("TRANS_RATE", npTransRate);
		setValue("MOD_USER", "system");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		setValueInt("MOD_SEQNO", 0);

		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Prod ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
			return;
		}

		insertBilProdDtl();

	}

	void insertBilProdDtl() throws Exception {
		// insert 2筆 DTL_VALUE = '01' & '03'
		daoTable = "BIL_PROD_DTL";
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("MCHT_NO", npMchtNo);
		setValue("DTL_KIND", "ACCT-TYPE");
		setValue("DTL_VALUE", "01");
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Prod_Dtl(1) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}

		//商務卡皆不適用
		/*
		daoTable = "BIL_PROD_DTL";
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("MCHT_NO", npMchtNo);
		setValue("DTL_KIND", "ACCT-TYPE");
		setValue("DTL_VALUE", "03");
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Prod_Dtl(3) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}
		*/
	}
	
	//=============================================================================
	void insertBilAutoParm() throws Exception {
		
		daoTable = "BIL_AUTO_PARM";
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("MCHT_NO", npMchtNo);
		setValue("ACTION_DESC", npProductName);
		setValue("EFFC_DATE_B", "20220101");
		setValue("EFFC_DATE_E", "20991231");
		setValue("DESTINATION_AMT_FLAG", "Y");
		setValue("DESTINATION_AMT", npDestAmt);
		setValue("PAYMENT_RATE_FLAG", "Y");
		setValue("PAYMENT_RATE", "1");
		setValue("CURR_PD_RATING_FLAG", "N");
		setValue("ACTION_CODE_FLAG", "N");
		setValue("RISK_GROUP_FLAG", "N");
		setValue("RC_RATE_FLAG", "N");
		setValue("MCC_CODE_FLAG", "N");
		setValue("MCHT_FLAG", "N");
		setValue("OVER_CREDIT_AMT_FLAG", "Y");
		setValue("BLOCK_REASON_FLAG", "N");
		setValue("SPEC_STATUS_FLAG", "N");
		setValue("RECHECK_FLAG", "N");
		setValue("REACTION_CODE_FLAG", "N");
		setValue("RERISK_GROUP_FLAG", "N");
		setValue("RERC_USE_FLAG", "N");
		setValue("RERC_RATE_FLAG", "N");
		setValue("MOD_USER", "system");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		setValueInt("MOD_SEQNO", 0);

		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Auto_Parm ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
			return;
		}

		insertBilAutoParmData();

	}

	void insertBilAutoParmData() throws Exception {

		daoTable = "BIL_AUTO_PARM_DATA";
		setValue("MCHT_NO", npMchtNo);
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("DATA_TYPE", "01");
		setValue("DATA_CODE", "NULL");
		setValue("DATA_CODE2", "Y");
		setValue("TYPE_DESC", "指定繳款評等");
		setValue("APR_FLAG", "Y");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Auto_Parm_Data(1) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}
		
		daoTable = "BIL_AUTO_PARM_DATA";
		setValue("MCHT_NO", npMchtNo);
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("DATA_TYPE", "01");
		setValue("DATA_CODE", "0A");
		setValue("DATA_CODE2", "N");
		setValue("TYPE_DESC", "指定繳款評等");
		setValue("APR_FLAG", "Y");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Auto_Parm_Data(1) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}
		
		daoTable = "BIL_AUTO_PARM_DATA";
		setValue("MCHT_NO", npMchtNo);
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("DATA_TYPE", "01");
		setValue("DATA_CODE", "0B");
		setValue("DATA_CODE2", "N");
		setValue("TYPE_DESC", "指定繳款評等");
		setValue("APR_FLAG", "Y");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Auto_Parm_Data(1) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}
		
		daoTable = "BIL_AUTO_PARM_DATA";
		setValue("MCHT_NO", npMchtNo);
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("DATA_TYPE", "01");
		setValue("DATA_CODE", "0C");
		setValue("DATA_CODE2", "N");
		setValue("TYPE_DESC", "指定繳款評等");
		setValue("APR_FLAG", "Y");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Auto_Parm_Data(1) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}
		
		daoTable = "BIL_AUTO_PARM_DATA";
		setValue("MCHT_NO", npMchtNo);
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("DATA_TYPE", "01");
		setValue("DATA_CODE", "0D");
		setValue("DATA_CODE2", "N");
		setValue("TYPE_DESC", "指定繳款評等");
		setValue("APR_FLAG", "Y");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Auto_Parm_Data(1) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}
		
		daoTable = "BIL_AUTO_PARM_DATA";
		setValue("MCHT_NO", npMchtNo);
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("DATA_TYPE", "01");
		setValue("DATA_CODE", "0E");
		setValue("DATA_CODE2", "N");
		setValue("TYPE_DESC", "指定繳款評等");
		setValue("APR_FLAG", "Y");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Auto_Parm_Data(1) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}
		
		daoTable = "BIL_AUTO_PARM_DATA";
		setValue("MCHT_NO", npMchtNo);
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("DATA_TYPE", "01");
		setValue("DATA_CODE", "01");
		setValue("DATA_CODE2", "Y");
		setValue("TYPE_DESC", "指定繳款評等");
		setValue("APR_FLAG", "Y");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Auto_Parm_Data(1) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}
		
		daoTable = "BIL_AUTO_PARM_DATA";
		setValue("MCHT_NO", npMchtNo);
		setValue("PRODUCT_NO", String.format("%02d", npTotalTerm));
		setValue("DATA_TYPE", "01");
		setValue("DATA_CODE", "02");
		setValue("DATA_CODE2", "Y");
		setValue("TYPE_DESC", "指定繳款評等");
		setValue("APR_FLAG", "Y");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Auto_Parm_Data(1) ERROR : mcht_no,tot_term=[" + npMchtNo +","+npTotalTerm+ "]");
		}

	}

	void insertBilMerchant() throws Exception {
		
		daoTable = "BIL_MERCHANT";
		setValue("mcht_no", npMchtNo);
		setValue("uniform_no", npCorpNo);
		setValue("mcht_status", "1");
		setValue("mcc_code", npMccCode);
		setValue("mcht_property", "M");
		setValue("mcht_type", "0"); //自行特店
		setValue("stmt_inst_flag", npStmtInstFlag);
		setValue("installment_delay", "Y");  //分期入帳時間 Y:當期;N:次期
		setValue("loan_flag", npLoanFlag);
		setValue("trans_flag", npTransFlag);
		setValue("installment_delay", npInstallmentDelay);
		setValue("mcht_chi_name", npMchtChiName);
		setValueInt("mp_rate", 100);
		setValue("mcht_city","TAIPEI");
		setValue("mcht_country","TW");
		setValue("mcht_state","TW");
		setValue("contract_head","N");
		setValue("tx_type","2");  //1.一般 2.分期 3.郵購 4.網路
		setValue("CRT_USER", "system");
		setValue("CRT_DATE", sysDate);
		setValue("APR_USER", "system");
		setValue("APR_DATE", sysDate);
		setValue("MOD_USER", "system");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		setValueInt("MOD_SEQNO", 0);
		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("E", "", "Insert Bil_Merchant ERROR : mcht_no=[" + npMchtNo + "]");
		}
	}

	//************************************************************************
	void deleteBilMerchant() throws Exception {
		int procCnt = 0;
		
		daoTable = "bil_merchant a";
		whereStr = "WHERE mcht_no like '10600000%' "; 

		procCnt = deleteTable();
		
		daoTable = "bil_prod a";
		whereStr = "WHERE mcht_no like '10600000%' "; 

		procCnt = deleteTable();
		
		daoTable = "bil_prod_dtl a";
		whereStr = "WHERE mcht_no like '10600000%' "; 

		procCnt = deleteTable();
		
		daoTable = "bil_auto_parm a";
		whereStr = "WHERE mcht_no like '10600000%' "; 

		procCnt = deleteTable();
		
		daoTable = "bil_auto_parm_data a";
		whereStr = "WHERE mcht_no like '10600000%' "; 

		procCnt = deleteTable();
		
		daoTable = "ptr_assign_installment a";
		whereStr = "WHERE mcht_no like '10600000%' "; 

		procCnt = deleteTable();

		return;
	}

	public static void main(String[] args) throws Exception {
		TmpB010 proc = new TmpB010();
		proc.mainProcess(args);
		return;
	}
	// ************************************************************************

}
