/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version     AUTHOR              DESCRIPTION                     *
*  --------  ---------- ---------  ------------------------------------------*
* 111/06/06  V1.00.00     Justin   initial                                   *
* 112/08/16  V1.00.01     Ryan     每月01才執行                              *
* 112/08/23  V1.00.02     Simon    改以ptr_businday.this_close_date判斷當日是否為關帳日*
* 112/08/28  V1.00.03     Ryan     改為每月2號~1號
* 112/09/25  V1.00.04     Simon    shell cyc002、cyc003並行執行日期控制      *
* 112/10/11  V1.00.05     Ryan     增加寫入筆數log                                 *
* 112/10/27  V1.00.06     Ryan     調整DBA_ABEM.DESCRIPTION,UPDATE 增加MOD_USER,MOD_TIME      
* 112/12/21  V1.00.07     Ryan     selectDbaAcmm移除測試的p_seqno=?                *
******************************************************************************/
package Dba;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.*;

public class DbaA120 extends AccessDAO {
	private String progname = "產生VD對帳單明細多筆資料區處理程式 112/12/21 "
	                        + "V1.00.07";
	private static final String RECORD03 = "03";
	private static final String PRG_NAME = "DbaA120";
	private static final int OUTPUT_BUFF_SIZE = 100000;
	String hCurrBusinessDate = "";
	String hInputExeDateFlag = "";
  String hLastAcctMonth = "";
  CommDate  commDate = new CommDate();
  CommString commStr = new CommString();
// ************************************************************************
	public static void main(String[] args) throws Exception {
		DbaA120 proc = new DbaA120();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			/** 檢查是否程式已啟動中 **/
			CommFunction commFunction = new CommFunction();
			if (commFunction.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式已有另依程式啟動中, 不執行..");
				return (0);
			}

			if (!connectDataBase())
				return (1);

			/** Load input arguments: businessDate **/
			if (args.length > 1) {
				showLogMessage("I", "", "請輸入參數:");
				showLogMessage("I", "", "PARM 1 : [business_date]");
				return (1);
			}

		  selectPtrBusinday();
			showLogMessage("I", "", "本日營業日 : ["+hCurrBusinessDate+"]");

			if (args.length >= 1 && args[0].length() == 8) {
				businessDate = args[0];
				hInputExeDateFlag = "Y";
			}

			if (hInputExeDateFlag.equals("Y")) {
  			showLogMessage("I", "", "人工執行關帳日 : ["+businessDate+"]");
			} else {
  			showLogMessage("I", "", "系統執行關帳日 : ["+businessDate+"]");
			}
			
			/** Check whether businessDate is workday **/
			if (isWordDay(businessDate) == false) {
				showLogMessage("I", "", "本日非符合執行關帳日, 程式結束");
				return (0);
			}

			/** Start to process **/
			selectLastAcctMonth();
			dataProcess();

			finalProcess();
			return (0);
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess

// ************************************************************************

	private void dataProcess() throws Exception {
		int tolCnt = 0;
		/** 讀取DBA_ACMM相關資料 **/
		/* String acctMonth = businessDate.substring(0, 6); */
		String acctMonth = "";
		CommDate  commDate = new CommDate();
		sqlCmd = "select to_char(add_months(to_date(business_date,'yyyymmdd'),-1),'yyyymm') acctMonth ";
		sqlCmd += "from ptr_businday fetch first 1 rows only";
		int recordCnt = selectTable();
		if (recordCnt > 0)
			  acctMonth = getValue("acctMonth");

		int dbaAcmmCursor = selectDbaAcmm(acctMonth);
		while (fetchTable(dbaAcmmCursor)) {
			String pSeqno = getValue("p_seqno");
			tolCnt++;
			/** DBA_ABEM取得 **/
			List<DbaAbem> dbaAbemList = getDbaAbemList(pSeqno, hLastAcctMonth);
			
			/** 逐筆處理Insert DBA_ABEM(多筆) **/
			BigDecimal totalTransactionAmt = BigDecimal.ZERO;
			for (DbaAbem dbaAbem : dbaAbemList) {
				insertDbaAbem(dbaAbem);
				totalTransactionAmt = totalTransactionAmt.add(dbaAbem.transactionAmt);
			}
			
			/** 當P_SEQNO Break時 Update DBA_ACMM-合計資料 **/
			updateDbaAcmm(pSeqno, acctMonth, totalTransactionAmt, dbaAbemList.size());

			if (tolCnt % OUTPUT_BUFF_SIZE == 0) {
				showLogMessage("I", "", String.format("已處理%d筆資料", tolCnt));
			}
			commitDataBase();
		}
		closeCursor(dbaAcmmCursor);
	}

// ************************************************************************
	private void updateDbaAcmm(String pSeqno, String acctMonth, BigDecimal totalTransactionAmt, int cnt) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" TTL_DEDUCT_DONE = ? ,")
		  .append(" STATEMENT_COUNT  = ? ,")
		  .append(" MOD_PGM  = ? , ")
		  .append(" MOD_USER  = ? , ")
		  .append(" MOD_TIME  = timestamp_format(?,'yyyymmddhh24miss') , ")
		  .append(" MOD_SEQNO = nvl(mod_seqno,0)+1 ")
		  ;
		daoTable   = "DBA_ACMM";
		updateSQL  =  sb.toString();
	    whereStr   = " WHERE P_SEQNO = ? AND ACCT_MONTH = ? ";
	    setDouble(1, totalTransactionAmt.doubleValue());
	    setInt(2, cnt);
	    setString(3, PRG_NAME);
	    setString(4, PRG_NAME);
	    setString(5, sysDate + sysTime);
	    setString(6, pSeqno);
	    setString(7, acctMonth);
		int updateCnt = updateTable();
		if (updateCnt <= 0) showLogMessage("I", "", String.format("p_seqno[%s], acctMonth[%s] fails to udpate DBA_ACMM", pSeqno, acctMonth));
	}
	
// ************************************************************************	
	private void insertDbaAbem(DbaAbem dbaAbem) throws Exception {
		daoTable = "DBA_ABEM";
		setValue("P_SEQNO", dbaAbem.pSeqno);
		setValue("ACCT_MONTH", dbaAbem.acctMonth);
		setValue("PRINT_TYPE", dbaAbem.printType);
		setValueInt("PRINT_SEQ", dbaAbem.printSeq);
		setValue("ACCT_CODE", dbaAbem.acctCode);
		setValue("TRAN_CLASS", dbaAbem.tranClass);
		setValue("REFERENCE_NO", dbaAbem.referenceNo);
		setValue("CARD_NO", dbaAbem.cardNo);
		setValue("PURCHASE_DATE", dbaAbem.purchaseDate);
		setValue("ACCT_DATE", dbaAbem.acctDate);
		setValueDouble("TRANSACTION_AMT", dbaAbem.transactionAmt.doubleValue());
		setValue("DESCRIPTION", dbaAbem.description);
		setValue("TXN_CODE", dbaAbem.txnCode);
		setValue("EXCHANGE_DATE", dbaAbem.exchangeDate);
		setValue("SOURCE_CURR", dbaAbem.sourceCurr);
		setValueDouble("SOURCE_AMT", dbaAbem.sourceAmt.doubleValue());
		setValue("MCHT_COUNTRY", dbaAbem.mchtCountry);
		setValue("MOD_PGM", dbaAbem.modPgm);
		setValueInt("MOD_SEQNO", dbaAbem.modSeqno);
        insertTable();
	}

// ************************************************************************
	private List<DbaAbem> getDbaAbemList(String pSeqno, String acctMonth) throws Exception {
		/** 讀取DBA_JRNL(多筆) **/
		int selectCnt = selectDbaJrnl(pSeqno, acctMonth);
		List<DbaAbem> dbaAbemList = new ArrayList<>(selectCnt);
		for (int i = 0 ; i < selectCnt ; i++) {
			DbaAbem dbaAbem = new DbaAbem();
			
			/** Primary Key **/
			dbaAbem.pSeqno = pSeqno;
			dbaAbem.acctMonth = acctMonth;
			dbaAbem.printType = RECORD03;
			dbaAbem.printSeq = i+1;
			
			/** DBA_JRNL DEBIT流水帳務資料檔 **/
			dbaAbem.acctCode = getValue("ACCT_CODE", i);
			dbaAbem.tranClass = getValue("TRAN_CLASS", i);
			dbaAbem.referenceNo = getValue("REFERENCE_NO", i);
			dbaAbem.cardNo = getValue("CARD_NO", i);
			dbaAbem.purchaseDate = getValue("PURCHASE_DATE", i);
			dbaAbem.acctDate = getValue("ACCT_DATE", i);
			dbaAbem.transactionAmt = getValueBigDecimal("TRANSACTION_AMT", i);
			
			/** DBB_BILL VD清算帳單檔 **/
			boolean sqlResult = selectDbbBill(dbaAbem.referenceNo);
			if (sqlResult) {
				dbaAbem.description = commStr.empty(getValue("MCHT_CHI_NAME")) ? getValue("MCHT_ENG_NAME") : getValue("MCHT_CHI_NAME");
				dbaAbem.txnCode = getValue("TXN_CODE");
				dbaAbem.exchangeDate = getValue("EXCHANGE_DATE");
				dbaAbem.sourceCurr = getValue("SOURCE_CURR");
				dbaAbem.mchtCountry = getValue("MCHT_COUNTRY");
				dbaAbem.signFlag = getValue("SIGN_FLAG");
				dbaAbem.sourceAmt = getValueBigDecimal("SOURCE_AMT");
				
				if ("-".equals(dbaAbem.signFlag)) {
					dbaAbem.sourceAmt = dbaAbem.sourceAmt.multiply(BigDecimal.valueOf(-1.0));
					dbaAbem.transactionAmt = dbaAbem.transactionAmt.multiply(BigDecimal.valueOf(-1.0));
				}
			}
			
			/** Other columns **/
			dbaAbem.modPgm = javaProgram;
			
			dbaAbemList.add(dbaAbem);
		}

		return dbaAbemList;
	}
// ************************************************************************
	private boolean selectDbbBill(String referenceNo) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT MCHT_CHI_NAME ,MCHT_ENG_NAME, TXN_CODE, EXCHANGE_DATE, SOURCE_CURR, SOURCE_AMT, MCHT_COUNTRY, SIGN_FLAG ")
		  .append(" FROM DBB_BILL ")
		  .append(" WHERE REFERENCE_NO = ? ")
		  ;
		sqlCmd = sb.toString();
		setString(1, referenceNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) return true;
		else return false;
	}
// ************************************************************************
	private int selectDbaJrnl(String pSeqno, String acctMonth) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT ACCT_CODE, TRAN_CLASS, REFERENCE_NO, CARD_NO, PURCHASE_DATE, ACCT_DATE, TRANSACTION_AMT ")
		  .append(" FROM DBA_JRNL ")
		  .append(" WHERE TRAN_CLASS in ('D', 'C') ")
		  .append(" AND P_SEQNO = ? ")
		  .append(" AND ACCT_DATE >= ? ")
		  .append(" AND ACCT_DATE <= ? ")
		  .append(" ORDER BY ACCT_DATE, ENQ_SEQNO, DEDUCT_SEQ ")
		  ;
		sqlCmd = sb.toString();
		setString(1, pSeqno);
		setString(2, acctMonth + "02");
		setString(3, commDate.monthAdd(acctMonth, 1) + "01");
		return selectTable();
	}
	
//************************************************************************
	private int selectDbaAcmm(String acctMonth) throws Exception {
		sqlCmd = " SELECT P_SEQNO FROM DBA_ACMM WHERE ACCT_MONTH = ? ";
		setString(1, acctMonth);
		return openCursor();
	}

//************************************************************************
//private String selectPtrBusinday() throws Exception {
	void selectPtrBusinday() throws Exception {
		selectSQL = " BUSINESS_DATE,THIS_CLOSE_DATE ";
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		}

	  hCurrBusinessDate = getValue("BUSINESS_DATE");
		businessDate      = getValue("THIS_CLOSE_DATE");
		return;
	}

// ************************************************************************ 
	public boolean isWordDay(String businessDate) throws Exception {
		CommString  commStr = new CommString();
		if(!"01".equals(commStr.right(businessDate, 2))) {
			return false;
		}
		extendField = "wday.";
		selectSQL = "stmt_cycle";
		daoTable = "ptr_workday";
		whereStr = "where this_close_date = ? ";

		setString(1, businessDate);

		selectTable();

		if (notFound.equals("Y"))
			return false;

		return true;
	}
//************************************************************************ 
    void selectLastAcctMonth() throws Exception {
        hLastAcctMonth = "";
        hLastAcctMonth = commDate.dateAdd(businessDate, 0, -1, 0).substring(0,6) ;
        /*
        sqlCmd = "select to_char(add_months(to_date(business_date,'yyyymmdd'),-1),'yyyymm') LastAcctMonth ";
		    sqlCmd += "from ptr_businday fetch first 1 rows only";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hLastAcctMonth = getValue("LastAcctMonth");
        } */
    }

} // End of class FetchSample
