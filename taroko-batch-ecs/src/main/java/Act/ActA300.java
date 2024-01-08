/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 112-04-14  V1.00.00     Ryan     initial                                            *
* 112-05-09  V1.00.01     Ryan     UPDATE ACT_REPAY_CREDITLIMIT.APR_FLAG = 'T'        *
* 112-06-20  V1.00.02     Ryan     add calCorpAmount()                                *
* 112-07-06  V1.00.03     Ryan     modify                                             *
* 112-07-18  V1.00.04     Ryan     insertActPayBatch新增CONFIRM_USER,CONFIRM_DATE,CONFIRM_TIME *   
* 112-11-07  V1.00.05     Ryan     ACT_PAY_ERROR2 -->ACT_PAY_ERROR *                        
* 112-11-28  V1.00.06     Ryan     調整selectActRepayCreditlimit2                   
* 112-12-11  V1.00.07     Ryan     回復insert ACT_PAY_BATCH ,讀到有值且 =’N’ 時才執行  (UPDATE CCA_CARD_ACCT(PAY_AMT) , CCA_CONSUME (tot_unpaid_amt)為0)*                   
 **************************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommDate;


public class ActA300 extends AccessDAO {
	private final String progname = "每天接收還額檔轉繳款銷帳作業 112/12/11 V.00.07";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString comStr = new CommString();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	String modUser = "";
	String modPgm = "";
	String modTime = "";
	int totalCnt1 = 0;
	int totalCnt2 = 0;
	int totalCalCnt = 0;
	private String hBatchNo = "";
	private String hBatchNoTmp = "";
	private String hSerialNo = "";
	private String hCurrCode = "";
	private String hPayCardNo = "";
	private String hSign = "";
	private double hPayAmt = 0;
	private double hDcPayAmt = 0;
	private String hPayDate = "";
	private String hPayTime = "";
	private String hPaymentType2 = "";
	private String hPaymentType = "";
	private String hUniteMark = "";
	private String hDefBranch = "";
	private String hPayBranch = "";
	private String hAcctType = "";
	private String hIdPSeqno = "";
	private String hPSeqno = "";
	private String hAcnoPSeqno = "";
	private String keyFileNo = "";
	private String keySerialNo = "";
	
	String calAcctType = "";
	String calCorpPSeqno = "";
	double calUnpayAmt = 0;
	double calTotAmtConsume = 0;
	double calJrnlBal = 0 ;
	double calTotalCashUtilized = 0 ;
	double calPayAmt = 0;
	double calCardAcctIdx = 0;
	double consumeTotAmt = 0;
	double consumeTxLogAmt2 = 0;
	double consumeTxLogAmtCash2 = 0;
	double consumeUnpaidPrecash = 0;
	double consumeTotUnpaidAmt = 0;
	double consumeUnpaidConsumeFee = 0;
	double consumePaidConsumeFee = 0;
	double consumePrePayAmt = 0;
	
	private String hBeforeBusDate = "";
	private String hBusDate = "";
	boolean corpContinue = false;
	int maxSerialNo = 1;
	private String wfValue = "";

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
			showLogMessage("I", "","-->connect DB: " + getDBalias()[0]);
			
			selectPtrBusinday();
			if(args.length == 1) {
				if(!commDate.isDate(args[0])) {
					comc.errExit("參數日期格式輸入錯誤", "");
				}
				hBusDate = args[0];
				hBeforeBusDate = commDate.dateAdd(hBusDate, 0, 0, -1);
				showLogMessage("I", "", String.format("參數日期 = [%s]", hBusDate));
				showLogMessage("I", "", String.format("參數日期前一日 = [%s]", hBeforeBusDate));
			}else {
				showLogMessage("I", "", String.format("無輸入參數,取營業日 = [%s]", hBusDate));
				showLogMessage("I", "", String.format("無輸入參數,取營業日前一日 = [%s]", hBeforeBusDate));
			}

			selectPtrSysParm();
			showLogMessage("I", "", String.format("取得參數 wf_Value = [%s]", wfValue));
			
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			modPgm = javaProgram;
			modTime = sysDate + sysTime;
			comr = new CommRoutine(getDBconnect(), getDBalias());
			
			int loadCnt = loadActRepayCreditlimit();
			showLogMessage("I", "", String.format("loadActRepayCreditlimit ,筆數 = %s", loadCnt));
			getBatchNo();
			if(loadCnt > 0)
				selectActRepayCreditlimit1();
			showLogMessage("I", "", String.format("繳款方式含(冲正參數) ,筆數 = %s", totalCnt1));
			selectActRepayCreditlimit2();
			if(totalCnt2 > 0) {
				selectActPayDetail();
//				calCorpAmount();
			}			
			commitDataBase();
			showLogMessage("I", "", String.format("程式處理 ,筆數 = %s", totalCnt2));
			// ==============================================
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
	
	
	int loadActRepayCreditlimit() throws Exception {
		extendField = "loadact.";
		sqlCmd = "select FILE_NO,PAY_CARD_NO,PAY_DATE,PAYMENT_TYPE2,PAY_AMT,SERIAL_NO from ACT_REPAY_CREDITLIMIT ";
		sqlCmd += " where PAYMENT_TYPE2 in ('03','08','09','11','12','13','14','Q1') AND DATA_FROM = 'B' ";
		sqlCmd += " AND LEFT(FILE_NO,8) in (?,?) ";
		sqlCmd += " AND APR_FLAG = 'Y' ";
		setString(1,hBeforeBusDate);
		setString(2,hBusDate);
		int n = loadTable();
		setLoadData("loadact.FILE_NO,loadact.PAY_CARD_NO,loadact.PAY_DATE,loadact.PAYMENT_TYPE2,loadact.PAY_AMT");
		return n;
	}
	
	
	void selectActRepayCreditlimit1() throws Exception {
		fetchExtend = "CREDITLIMIT1.";
		sqlCmd = " SELECT  ";
		sqlCmd += " '901' AS CURR_CODE ,PAY_CARD_NO ,SIGN ,PAY_AMT ,PAY_AMT AS DC_PAY_AMT ,PAY_DATE ,PAY_TIME ";
		sqlCmd += " ,PAYMENT_TYPE2 ,PAYMENT_TYPE ,UNITE_MARK ,DEF_BRANCH ,PAY_BRANCH ,ACCT_TYPE ";
		sqlCmd += " ,ID_P_SEQNO ,P_SEQNO ,ACNO_P_SEQNO ,FILE_NO ,SERIAL_NO ,ABS(PAY_AMT) AS ABS_PAY_AMT ";
		sqlCmd += " FROM ACT_REPAY_CREDITLIMIT ";
		sqlCmd += " WHERE LEFT(FILE_NO,8) in (?,?) ";
		sqlCmd += " AND DATA_FROM = 'B' ";
		sqlCmd += " AND IS_PASS = 'Y' ";
		sqlCmd += " AND IS_REPAY = 'Y' ";
		sqlCmd += " AND APR_FLAG = 'Y' ";
		sqlCmd += " AND PAYMENT_TYPE2 in ('04', '10', '15', '16', '17', 'E2') ";
		sqlCmd += " AND PAY_AMT < 0 ";
		sqlCmd += " ORDER BY FILE_NO, SERIAL_NO ";
		setString(1,hBeforeBusDate);
		setString(2,hBusDate);
		this.openCursor();

		while (fetchTable()) {
			initData();
			hCurrCode = getValue("CREDITLIMIT1.CURR_CODE");
			hPayCardNo = getValue("CREDITLIMIT1.PAY_CARD_NO");
			hSign = getValue("CREDITLIMIT1.SIGN");
			hPayAmt = getValueDouble("CREDITLIMIT1.PAY_AMT");
			hDcPayAmt = getValueDouble("CREDITLIMIT1.DC_PAY_AMT");
			hPayDate = getValue("CREDITLIMIT1.PAY_DATE");
			hPayTime = getValue("CREDITLIMIT1.PAY_TIME");
			hPaymentType2 = getValue("CREDITLIMIT1.PAYMENT_TYPE2");
			hPaymentType = getValue("CREDITLIMIT1.PAYMENT_TYPE");
			hUniteMark = getValue("CREDITLIMIT1.UNITE_MARK");
			hDefBranch = getValue("CREDITLIMIT1.DEF_BRANCH");
			hPayBranch = getValue("CREDITLIMIT1.PAY_BRANCH");
			hAcctType = getValue("CREDITLIMIT1.ACCT_TYPE");
			hIdPSeqno = getValue("CREDITLIMIT1.ID_P_SEQNO");
			hPSeqno = getValue("CREDITLIMIT1.P_SEQNO");
			hAcnoPSeqno = getValue("CREDITLIMIT1.ACNO_P_SEQNO");
			keyFileNo = getValue("CREDITLIMIT1.FILE_NO");
			keySerialNo = getValue("CREDITLIMIT1.SERIAL_NO");
			double absPayAmt = getValueDouble("CREDITLIMIT1.ABS_PAY_AMT");
			setValue("loadact.FILE_NO",keyFileNo);
			setValue("loadact.PAY_CARD_NO",hPayCardNo);
			setValue("loadact.PAY_DATE",hPayDate);
			setValue("loadact.PAYMENT_TYPE2",comStr.decode(hPaymentType2,",04,10,15,16,17,E2",",03,09,12,13,14,E1"));
			setValue("loadact.PAY_AMT",String.format("%.2f", absPayAmt));
			if(getLoadData("loadact.FILE_NO,loadact.PAY_CARD_NO,loadact.PAY_DATE,loadact.PAYMENT_TYPE2,loadact.PAY_AMT") > 0) {
				insertActPayError();
				updateActRepayCreditlimit(keyFileNo,keySerialNo);
				updateActRepayCreditlimit(getValue("loadact.FILE_NO"),getValue("loadact.SERIAL_NO"));
				totalCnt1 ++;
			}
		}
		this.closeCursor();
	}
	
	void selectActRepayCreditlimit2() throws Exception {
		fetchExtend = "CREDITLIMIT2.";
		sqlCmd = " SELECT  ";
		sqlCmd += " '901' AS CURR_CODE ,PAY_CARD_NO ,SIGN ,PAY_AMT ,PAY_AMT AS DC_PAY_AMT ,PAY_DATE ,PAY_TIME ";
		sqlCmd += " ,PAYMENT_TYPE2 ,PAYMENT_TYPE ,UNITE_MARK ,DEF_BRANCH ,PAY_BRANCH ,ACCT_TYPE ";
		sqlCmd += " ,ID_P_SEQNO ,P_SEQNO ,ACNO_P_SEQNO ,FILE_NO ,SERIAL_NO ,IS_PASS ";
		sqlCmd += " FROM ACT_REPAY_CREDITLIMIT ";
		sqlCmd += " WHERE LEFT(FILE_NO,8) in (?,?) ";
		sqlCmd += " AND DATA_FROM = 'B' ";
//		sqlCmd += " AND IS_PASS = 'Y' ";
//		sqlCmd += " AND IS_REPAY = 'Y' ";
		sqlCmd += " AND APR_FLAG = 'Y' ";
		sqlCmd += " AND PAYMENT_TYPE2 NOT IN ('02','22','71','F1','P3','A1','N1','H1','E1') ";
		sqlCmd += " ORDER BY FILE_NO, SERIAL_NO ";
		setString(1,hBeforeBusDate);
		setString(2,hBusDate);
		this.openCursor();

		while (fetchTable()) {
			initData();
			hCurrCode = getValue("CREDITLIMIT2.CURR_CODE");
			hPayCardNo = getValue("CREDITLIMIT2.PAY_CARD_NO");
			hSign = getValue("CREDITLIMIT2.SIGN");
			hPayAmt = getValueDouble("CREDITLIMIT2.PAY_AMT");
			hDcPayAmt = getValueDouble("CREDITLIMIT2.DC_PAY_AMT");
			hPayDate = getValue("CREDITLIMIT2.PAY_DATE");
			hPayTime = getValue("CREDITLIMIT2.PAY_TIME");
			hPaymentType2 = getValue("CREDITLIMIT2.PAYMENT_TYPE2");
			hPaymentType = getValue("CREDITLIMIT2.PAYMENT_TYPE");
			hUniteMark = getValue("CREDITLIMIT2.UNITE_MARK");
			hDefBranch = getValue("CREDITLIMIT2.DEF_BRANCH");
			hPayBranch = getValue("CREDITLIMIT2.PAY_BRANCH");
			hAcctType = getValue("CREDITLIMIT2.ACCT_TYPE");
			hIdPSeqno = getValue("CREDITLIMIT2.ID_P_SEQNO");
			hPSeqno = getValue("CREDITLIMIT2.P_SEQNO");
			hAcnoPSeqno = getValue("CREDITLIMIT2.ACNO_P_SEQNO");
			keyFileNo = getValue("CREDITLIMIT2.FILE_NO");
			keySerialNo = getValue("CREDITLIMIT2.SERIAL_NO");
			String isPass = getValue("CREDITLIMIT2.IS_PASS");
			
			if("Y".equals(isPass) && !"-".equals(hSign) && hPayAmt >= 0) {
				insertActPayDetail();
			}else {
				insertActPayError();
			}
			updateActRepayCreditlimit(keyFileNo,keySerialNo);
			if("N".equals(wfValue)) {
				updateCcaCardAcct();
				updateCcaConsume();
			}
			totalCnt2 ++;
		}
		this.closeCursor();
	}
	
    void selectActPayDetail() throws Exception {
    	extendField = "DETAIL_SUM.";
    	sqlCmd = " SELECT COUNT(*) as BATCH_TOT_CNT,"
				   + "SUM(PAY_AMT) as BATCH_TOT_AMT "
				   + "FROM ACT_PAY_DETAIL "
				   + "WHERE BATCH_NO = ? ";
    	setString(1,hBatchNo);
		int detailCursor = selectTable();
		for(int i = 0; i<detailCursor ;i++) {
			int batchTotCnt = getValueInt("DETAIL_SUM.BATCH_TOT_CNT",i);
			double batchTotAmt = getValueDouble("DETAIL_SUM.BATCH_TOT_AMT",i);
			insertActPayBatch(hBatchNo,batchTotCnt,batchTotAmt);
		}
    }
	
    String getIdNo(String idPSeqno) throws Exception {
    	extendField = "idno.";
    	sqlCmd = " select id_no from crd_idno where id_p_seqno = ?";
    	setString(1,idPSeqno);
    	selectTable();
    	return getValue("idno.id_no");
    }
    
	void insertActPayError() throws Exception{
		String idNo = getIdNo(hIdPSeqno);
		hSerialNo = String.format("%07d", maxSerialNo++);
	   	extendField = "ERROR.";
		setValue("ERROR.BATCH_NO", hBatchNo);
		setValue("ERROR.SERIAL_NO", hSerialNo);
		setValue("ERROR.CURR_CODE", hCurrCode);
		setValue("ERROR.PAY_CARD_NO", hPayCardNo);
		setValueDouble("ERROR.PAY_AMT", hPayAmt);
		setValueDouble("ERROR.DC_PAY_AMT", hDcPayAmt);
		setValue("ERROR.PAY_DATE", hPayDate);
		setValue("ERROR.PAY_TIME", hPayTime);
		setValue("ERROR.PAYMENT_TYPE2", hPaymentType2);
		setValue("ERROR.PAYMENT_TYPE", hPaymentType);
		setValue("ERROR.UNITE_MARK", hUniteMark);
		setValue("ERROR.DEF_BRANCH", hDefBranch);
		setValue("ERROR.PAY_BRANCH", hPayBranch);
		setValue("ERROR.ACCT_TYPE", hAcctType);
		setValue("ERROR.ID_NO", idNo);
		setValue("ERROR.P_SEQNO", hPSeqno);
		setValue("ERROR.ACNO_P_SEQNO", hAcnoPSeqno);
        setValue("ERROR.crt_user", modPgm); // update_user
        setValue("ERROR.crt_date", sysDate); // update_date
        setValue("ERROR.crt_time", sysTime); // update_time
		setValue("ERROR.mod_time", modTime);
		setValue("ERROR.mod_pgm", modPgm);
		setValue("ERROR.mod_user", modPgm);
		daoTable = "ACT_PAY_ERROR";
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("E", "", "insert ACT_PAY_ERROR error");
			return;
		}
		if("Y".equals(dupRecord)) {
			showLogMessage("I", "", "insert ACT_PAY_ERROR duplicate error");
			return;
		}
	}
	
	void insertActPayDetail() throws Exception{
		hSerialNo = String.format("%07d", maxSerialNo++);
	  extendField = "DETAIL.";
		setValue("DETAIL.BATCH_NO", hBatchNo);
		setValue("DETAIL.SERIAL_NO", hSerialNo);
		setValue("DETAIL.CURR_CODE", hCurrCode);
		setValue("DETAIL.PAYMENT_NO", hPayCardNo);
		setValueDouble("DETAIL.PAY_AMT", hPayAmt);
		setValueDouble("DETAIL.DC_PAY_AMT", hDcPayAmt);
    if (hBusDate.compareTo(hPayDate) < 0) {
         hPayDate =hBusDate  ;
    }
		setValue("DETAIL.PAY_DATE", hPayDate);
		setValue("DETAIL.PAY_TIME", hPayTime);
		setValue("DETAIL.PAYMENT_TYPE2", hPaymentType2);
		setValue("DETAIL.PAYMENT_TYPE", hPaymentType);
		setValue("DETAIL.UNITE_MARK", hUniteMark);
		setValue("DETAIL.DEF_BRANCH", hDefBranch);
		setValue("DETAIL.PAY_BRANCH", hPayBranch);
		setValue("DETAIL.ACCT_TYPE", hAcctType);
		setValue("DETAIL.ID_P_SEQNO", hIdPSeqno);
		setValue("DETAIL.P_SEQNO", hPSeqno);
		setValue("DETAIL.ACNO_P_SEQNO", hAcnoPSeqno);
        setValue("DETAIL.crt_user", modPgm); // update_user
        setValue("DETAIL.crt_date", sysDate); // update_date
        setValue("DETAIL.crt_time", sysTime); // update_time
		setValue("DETAIL.mod_time", modTime);
		setValue("DETAIL.mod_pgm", modPgm);
		setValue("DETAIL.mod_user", modPgm);
		daoTable = "ACT_PAY_DETAIL";
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("E", "", "insert ACT_PAY_DETAIL error");
			return;
		}
		if("Y".equals(dupRecord)) {
			showLogMessage("I", "", "insert ACT_PAY_DETAIL duplicate ,BATCH_NO = " + hBatchNo + " ,SERIAL_NO = " + hSerialNo);
			return;
		}
	}
	
	void insertActPayBatch(String batchNo,int batchTotCnt,double batchTotAmt) throws Exception{
    	extendField = "BATCH.";
        setValue("BATCH.BATCH_NO", batchNo);
        setValueInt("BATCH.BATCH_TOT_CNT", batchTotCnt);
        setValueDouble("BATCH.BATCH_TOT_AMT", batchTotAmt);
        setValue("BATCH.crt_user", modPgm); // update_user
        setValue("BATCH.crt_date", sysDate); // update_date
        setValue("BATCH.crt_time", sysTime); // update_time
        setValue("BATCH.mod_time", sysDate + sysTime);
        setValue("BATCH.mod_user", modPgm);
        setValue("BATCH.mod_pgm", modPgm);
        setValue("BATCH.confirm_user", "ecs");
        //setValue("BATCH.confirm_date", sysDate);
        setValue("BATCH.confirm_date", hBusDate);
        setValue("BATCH.confirm_time", sysTime);
        daoTable = "ACT_PAY_BATCH";
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("E", "", "insert ACT_PAY_BATCH error ,BATCH_NO = [" + batchNo + "]");
			return;
		}
		if("Y".equals(dupRecord)) {
			showLogMessage("I", "", "insert ACT_PAY_BATCH duplicate ,BATCH_NO = [" + batchNo + "]");
			return;
		}
	}

	void updateActRepayCreditlimit(String keyFileNo , String keySerialNo) throws Exception {
		daoTable = "ACT_REPAY_CREDITLIMIT";
		updateSQL = " APR_FLAG = 'T' ,APR_DATE = ? ,APR_USER = ? ";
		updateSQL += " ,MOD_TIME = SYSDATE ,MOD_PGM = ? ";
		whereStr = " WHERE DATA_FROM = 'B' AND FILE_NO = ? AND SERIAL_NO = ? ";
		setString(1, sysDate);
		setString(2, modPgm);
		setString(3, modPgm);
		setString(4, keyFileNo);
		setString(5, keySerialNo);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("E", "", "update ACT_REPAY_CREDITLIMIT not found");
		}		
	}
	
	void updateCcaCardAcct() throws Exception {
		daoTable = "CCA_CARD_ACCT";
		updateSQL = " PAY_AMT = 0 ";
		updateSQL += " ,MOD_TIME = SYSDATE ,MOD_PGM = ? ,MOD_USER = ? ";
		whereStr = " WHERE P_SEQNO = ? ";
		setString(1, modPgm);
		setString(2, modPgm);
		setString(3, hPSeqno);
		updateTable();
		if (notFound.equals("Y")) {
//			showLogMessage("I", "", "reset CCA_CARD_ACCT(PAY_AMT) not found P_SEQNO = [" + hPSeqno + "]'");
		}		
	}
	
	void updateCcaConsume() throws Exception {
		daoTable = "CCA_CONSUME";
		updateSQL = " TOT_UNPAID_AMT = 0 ";
		updateSQL += " ,MOD_TIME = SYSDATE ,MOD_PGM = ? ,MOD_USER = ? ";
		whereStr = " WHERE P_SEQNO = ? ";
		setString(1, modPgm);
		setString(2, modPgm);
		setString(3, hPSeqno);
		updateTable();
		if (notFound.equals("Y")) {
//			showLogMessage("I", "", "reset CCA_CONSUME(TOT_UNPAID_AMT) not found P_SEQNO = [" + hPSeqno + "]'");
		}		
	}
	
	private void selectPtrBusinday() throws Exception {
		sqlCmd = "SELECT BUSINESS_DATE,TO_CHAR(TO_DATE(BUSINESS_DATE,'YYYYMMDD')-1 DAYS, 'YYYYMMDD') AS BEFORE_BUSINESS_DATE FROM PTR_BUSINDAY ";
		selectTable();
		hBeforeBusDate = getValue("BEFORE_BUSINESS_DATE");
		hBusDate = getValue("BUSINESS_DATE");
		return;
	}
	

	/**********************************************************************/
	void calCorpAmount() throws Exception {
		fetchExtend = "corp.";
		sqlCmd = "select acct_type , corp_p_seqno , sum(unpay_amt) as unpay_amt , "
			   + "sum(tot_amt_consume) as tot_amt_consume , sum(jrnl_bal) as jrnl_bal , "
			   + "sum(total_cash_utilized) as total_cash_utilized , sum(pay_amt) as pay_amt "
			   + "from cca_card_acct where acno_flag in ('3','Y') "
			   + "group by acct_type , corp_p_seqno "
			   + "order by acct_type , corp_p_seqno "
			   ;
		
		openCursor();
		
		while(fetchTable()) {
			initCal();
			calAcctType = getValue("corp.acct_type");
			calCorpPSeqno = getValue("corp.corp_p_seqno");
			calUnpayAmt = getValueDouble("corp.unpay_amt");
			calTotAmtConsume = getValueDouble("corp.tot_amt_consume");
			calJrnlBal = getValueDouble("corp.jrnl_bal");
			calTotalCashUtilized = getValueDouble("corp.total_cash_utilized");
			calPayAmt = getValueDouble("corp.pay_amt");
			calCardAcctIdx = getCropCardAcctIdx();
			updateCcaCardAcctForCorp();
			if(corpContinue)	continue;
			calConsume();
			updateCcaConsumeForCorp();
			totalCalCnt++;
		}
		closeCursor();
	}
	
	double getCropCardAcctIdx() throws Exception {
		
		sqlCmd = "select card_acct_idx from cca_card_acct where corp_p_seqno = ? and acct_type = ? and acno_flag = '2' ";
		setString(1,calCorpPSeqno);
		setString(2,calAcctType);
		
		if(selectTable()>0)	return getValueDouble("card_acct_idx");
		
		return 0 ;
	}
	
    void getBatchNo() throws Exception {
    	hBatchNo = hBusDate + "5555" ;
    	sqlCmd = "select (nvl(max(batch_no),?) + 1) as max_batch_no from ACT_PAY_DETAIL where batch_no like ? ";
    	setString(1,hBatchNo + "0000");
    	setString(2,hBatchNo + "%");
    	selectTable();
    	hBatchNo = getValue("max_batch_no");
        showLogMessage("I", "", "取得 MAX BATCH_NO = [" + hBatchNo + "]");
    }
	
	void updateCcaCardAcctForCorp() throws Exception {
		daoTable = "cca_card_acct";
		updateSQL = "unpay_amt = ? , tot_amt_consume = ? , jrnl_bal = ? , total_cash_utilized = ? ,";
		updateSQL += "pay_amt = ? , mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), mod_user  = 'ActA300', ";
		updateSQL += "mod_pgm  = 'ActA300' ";
		whereStr = "where card_acct_idx = ? ";
		setDouble(1,calUnpayAmt);
		setDouble(2,calTotAmtConsume);
		setDouble(3,calJrnlBal);
		setDouble(4,calTotalCashUtilized);
		setDouble(5,calPayAmt);
		setString(6,sysDate+sysTime);
		setDouble(7,calCardAcctIdx);
		
		updateTable();
		if (notFound.equals("Y")) {
			corpContinue = true;
			showLogMessage("I", "", "update_cca_card_acct_Corp not found! Corp_p_seqno = "+calCorpPSeqno);
		}							
	}
	
	void calConsume() throws Exception {
		sqlCmd = "select sum(tot_amt_consume) as tot_amt_consume , sum(auth_txlog_amt_2) as auth_txlog_amt_2 ,"
			   + "sum(auth_txlog_amt_cash_2) as auth_txlog_amt_cash_2 , sum(unpaid_precash) as unpaid_precash , "
			   + "sum(tot_unpaid_amt) as tot_unpaid_amt , sum(unpaid_consume_fee) as unpaid_consume_fee , "
			   + "sum(paid_consume_fee) as paid_consume_fee , sum(pre_pay_amt) as pre_pay_amt "
			   + "from cca_consume where card_acct_idx in "
			   + "(select card_acct_idx from cca_card_acct where corp_p_seqno = ? and acct_type = ? and acno_flag in ('3','Y')) "
			   ;
		
		setString(1,calCorpPSeqno);
		setString(2,calAcctType);
		
		if(selectTable()>0) {
			consumeTotAmt = getValueDouble("tot_amt_consume");
			consumeTxLogAmt2 = getValueDouble("auth_txlog_amt_2");
			consumeTxLogAmtCash2 = getValueDouble("auth_txlog_amt_cash_2");
			consumeUnpaidPrecash = getValueDouble("unpaid_precash");
			consumeTotUnpaidAmt = getValueDouble("tot_unpaid_amt");
			consumeUnpaidConsumeFee = getValueDouble("unpaid_consume_fee");
			consumePaidConsumeFee = getValueDouble("paid_consume_fee");
			consumePrePayAmt = getValueDouble("pre_pay_amt");
		}
	}
	
	void updateCcaConsumeForCorp() throws Exception {
		daoTable = "cca_consume";
		updateSQL = "tot_amt_consume = ? , auth_txlog_amt_2 = ? , auth_txlog_amt_cash_2 = ? , ";
		updateSQL += "unpaid_precash = ? , tot_unpaid_amt = ? , ";		
		if(calJrnlBal >=0) {
			updateSQL += " unpaid_consume_fee = ? , paid_consume_fee = 0 , pre_pay_amt = 0 ,";
		} else {
			updateSQL += " unpaid_consume_fee = 0 , paid_consume_fee = 0 , pre_pay_amt = ? ,";
		}		
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
		updateSQL += "mod_user  = 'ActA300', mod_pgm  = 'ActA300' ";
		whereStr = "where card_acct_idx = ? ";
		
		setDouble(1,consumeTotAmt);
		setDouble(2,consumeTxLogAmt2);
		setDouble(3,consumeTxLogAmtCash2);
		setDouble(4,consumeUnpaidPrecash);
		setDouble(5,consumeTotUnpaidAmt);
		if(calJrnlBal >=0) {
			setDouble(6,calJrnlBal);
		} else {
			setDouble(6,calJrnlBal*-1);
		}		
		setString(7,sysDate + sysTime);
		setDouble(8,calCardAcctIdx);
		
		updateTable();

		if (notFound.equals("Y")) {
			corpContinue = true;
			showLogMessage("I", "", "update_cca_consume_Corp not found! Corp_p_seqno = "+calCorpPSeqno);
		}						
	}
	
	void selectPtrSysParm() throws Exception {
		extendField = "parm.";
		sqlCmd = "select WF_VALUE from PTR_SYS_PARM where WF_PARM='SYSPARM' and WF_KEY ='ROLLBACK_P2' ";
		selectTable();
		wfValue = getValue("parm.WF_VALUE");

	}
	
	
	/***********************************************************************/
	public void initData() {
		hSerialNo = "";
		hCurrCode = "";
		hPayCardNo = "";
		hSign = "";
		hPayAmt = 0;
		hDcPayAmt = 0;
		hPayDate = "";
		hPayTime = "";
		hPaymentType2 = "";
		hPaymentType = "";
		hUniteMark = "";
		hDefBranch = "";
		hPayBranch = "";
		hAcctType = "";
		hIdPSeqno = "";
		hPSeqno = "";
		hAcnoPSeqno = "";
		keyFileNo = "";
		keySerialNo = "";
	}
	
	void initCal() {
		calAcctType = "";
		calCorpPSeqno = "";
		calUnpayAmt = 0;
		calTotAmtConsume = 0;
		calJrnlBal = 0 ;
		calTotalCashUtilized = 0 ;
		calPayAmt = 0;
		calCardAcctIdx = 0 ;
		consumeTotAmt = 0;
		consumeTxLogAmt2 = 0;
		consumeTxLogAmtCash2 = 0;
		consumeUnpaidPrecash = 0;
		consumeTotUnpaidAmt = 0;
		consumeUnpaidConsumeFee = 0;
		consumePaidConsumeFee = 0;
		consumePrePayAmt = 0;
		corpContinue = false;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		ActA300 proc = new ActA300();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
