/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 112/06/21  V1.00.01   JeffKung    program initial                           *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*悠遊卡簽帳資料檔(DCST)資料處理程式*/
public class TscB001V extends AccessDAO {
    private boolean debugT = false;
    private final String progname = "悠遊卡簽帳資料檔(DCST)資料處理程式  112/06/21 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug   = 1;
    String hCallBatchSeqno = "";

    String hStmtCrtDate = "";
    String hBusiBusinessDate = "";
    String hBillAcctMonth = "";
    String hStmtCrtTime = "";
    String hTspmItemEnameBl = "";
    String hTspmItemEnameIt = "";
    String hTspmItemEnameId = "";
    String hTspmItemEnameCa = "";
    String hTspmItemEnameAo = "";
    String hTspmItemEnameOt = "";
    String hTspmExclMccFlag = "";
    String hTspmExclMchtGroupFlag = "";
    String hMBillRealCardNo = "";
    double hMBillDestinationAmt = 0;
    int hBillDestinationCnt = 0;
    double hBillDestinationAmt = 0;
    double hStmtFeedbackAmt = 0;
    String hBillRealCardNo = "";
    double hTshtDestinationAmt = 0;
    double hTshtFeedbackAmt = 0;
    String hTrlgTranCode = "";
    double hTrlgTranAmt = 0;
    int hTempCnt = 0;
    long hTspmRepayAmt1s = 0;
    long hTspmRepayAmt2s = 0;
    long hTspmRepayAmt3s = 0;
    long hTspmRepayAmt4s = 0;
    long hTspmRepayAmt5s = 0;
    double hTspmRepayRate1 = 0;
    double hTspmRepayRate2 = 0;
    double hTspmRepayRate3 = 0;
    double hTspmRepayRate4 = 0;
    double hTspmRepayRate5 = 0;
    String hTspmExclMchtFlag = "";
    String hTfinRunDay = "";
    double hTsrdRefundAmt = 0;
    long[] nTempRepayAmt = new long[10];
    double[] nTempRepayRate = new double[10];

    int forceFlag = 0;
    int totalCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 3) {
                comc.errExit("Usage : TscB001V [notify_date] [flag]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hStmtCrtDate = "";
            forceFlag = 0;
            
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hStmtCrtDate = args[0];
            } else if (args.length >= 2) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hStmtCrtDate = args[0];
                if ((args[1].length() == 1) && (args[1].equals("Y")))
                    forceFlag = 1;
            }

            selectPtrBusinday();
            if(hStmtCrtDate.substring(6, 8).equals("01") == false) {
               exceptExit = 0;
               String stderr = String.format("本程式限每月01日 7:00 前執行 [%s]",hStmtCrtDate);
               comcr.errRtn(stderr, "", hCallBatchSeqno);
            }
            showLogMessage("I", "", String.format("處理月份 [%s]", hBillAcctMonth));
            if (forceFlag == 0) {
                if (selectTscDcstLoga() != 0) {
                	exceptExit = 0;
                    comcr.errRtn("程式結束", "", hCallBatchSeqno);
                }
            }

            //deleteTscDcstHst();  //table沒有建
            deleteTscDcstLog();
            hTshtDestinationAmt = 0;
            hTshtFeedbackAmt = 0;
            selectDbbBill();

            //insertTscDcstHst();  //table沒有建
            showLogMessage("I", "", String.format("Process records = [%d]", totalCnt));
            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            comcr.callbatchEnd();
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
        sqlCmd  = "select business_date,";
        sqlCmd += "to_char( decode(sign(substr(to_char(sysdate,'hh24miss'),1,2)-'07'),1," 
                + "sysdate+1 days,sysdate), 'yyyymmdd') h_stmt_crt_date,";
        sqlCmd += "decode(cast(? as varchar(10)),'',"
                + "to_char( decode(sign(substr(to_char(sysdate,'hh24miss'),1,2)-'07'),"
                + "1,add_months(sysdate+1 days,-1),"
                + "add_months(sysdate,-1)), 'yyyymm'),to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')) h_bill_acct_month,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_stmt_crt_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hStmtCrtDate);
        setString(2, hStmtCrtDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hStmtCrtDate = hStmtCrtDate.length() == 0 ? getValue("h_stmt_crt_date") : hStmtCrtDate;
            hBillAcctMonth = getValue("h_bill_acct_month");
            hStmtCrtTime = getValue("h_stmt_crt_time");
        }
    }
    /***********************************************************************/
    int selectTscDcstLoga() throws Exception {
        sqlCmd  = "select 1 cnt ";
        sqlCmd += " from tsc_dcst_log  ";
        sqlCmd += "where acct_month = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hBillAcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempCnt = getValueInt("cnt");
        } else
            return (0);

        showLogMessage("I", "", String.format("本月[%s]簽帳資料已產生, 不可重複執行 , 請通知相關人員處理(error)", hBillAcctMonth));
        return (1);
    }
    /***********************************************************************/
    void deleteTscDcstHst() throws Exception {
        daoTable = "tsc_dcst_hst";
        whereStr = "where acct_month = ? ";
        setString(1, hBillAcctMonth);
        deleteTable();
    }
    /***********************************************************************/
    void deleteTscDcstLog() throws Exception {
        daoTable = "tsc_dcst_log";
        whereStr = "where acct_month = ? ";
        setString(1, hBillAcctMonth);
        deleteTable();

    }

	/***********************************************************************/
	void selectDbbBill() throws Exception {
		daoTable = "bil_fiscdtl";

		sqlCmd = "SELECT a.batch_date,a.ecs_real_card_no,a.ecs_platform_kind,a.mcht_chi_name,      ";
		sqlCmd += "       a.ecs_sign_code,a.mcc_code,a.ecs_tx_code,round(a.dest_amt) dest_amt,     ";
		sqlCmd += "       a.ecs_bill_type                                                          ";
		sqlCmd += "FROM bil_fiscdtl a,                                                             ";
		sqlCmd += "    (select vd_card_no                                                          ";
		sqlCmd += "     from tsc_vd_card                                                           ";
		sqlCmd += "     where 1=1                                                                  ";
		sqlCmd += "     and autoload_flag  = 'Y'                                                   ";
		sqlCmd += "     and ((current_code = '0' and substr(new_end_date,1,6) > ? ) or             ";
		sqlCmd += "           (current_code!='0' and                                               ";
		sqlCmd += "            substr(decode(oppost_date ,'','19110101', oppost_date),1,6) > ?))   ";
		sqlCmd += "     and ((substr(decode(lock_date ,'','30001231', lock_date)  ,1,6) > ?) and   ";
		sqlCmd += "          (substr(decode(balance_date,'','30001231',balance_date),1,6) > ?) and ";
		sqlCmd += "          (substr(decode(return_date ,'','30001231',return_date) ,1,6) > ?))    ";
		sqlCmd += "     GROUP by vd_card_no ) c                                                    ";
		sqlCmd += "WHERE a.card_no = c.vd_card_no                                                  ";
		sqlCmd += "AND   a.batch_date like ?                                                       ";
		sqlCmd += "AND   a.ecs_bill_type = 'FISC'                                                  ";
		
		setString(1, hBillAcctMonth);
		setString(2, hBillAcctMonth);
		setString(3, hBillAcctMonth);
		setString(4, hBillAcctMonth);
		setString(5, hBillAcctMonth);
		setString(6, hBillAcctMonth+"%");

		//平台交易
		String[] listOfSkipKind = new String[]
			    {"f1","G1","G2","d1","M1","e1",
			     "10","11","12","13","14",
			     "20","21","22","23","24","25",
			     "V1","V2","V3","V4","V5","V6",
			     "FL","CL"};
		
		//特定MCC
		String[] listOfSkipMCC = new String[]
				{"9311","8398","0000","","0037","5960",
			     "5965","6300"};

		String keepCardNo = "";
		boolean firstCardNo = true;
		
		openCursor();
		while (fetchTable()) {
			
			for (int i = 0; i < listOfSkipKind.length; i++) {
				if (getValue("ecs_platform_kind").equals(listOfSkipKind[i])) {
					continue;
				}
			}
			
			for (int j = 0; j < listOfSkipMCC.length; j++) {
				if (getValue("mcc_code").equals(listOfSkipMCC[j])) {
					continue;
				}
			}
			
			//特店中文內含保險兩個字
			if (getValue("mcht_chi_name").indexOf("保險") > 0 ) continue;
			
			hBillRealCardNo = getValue("ecs_real_card_no");
			if (keepCardNo.equals(hBillRealCardNo) == false ) {
				if (firstCardNo==false) {
					if (hBillDestinationAmt > 0) {
						insertTscDcstLog(keepCardNo);
					}
				}
				
				keepCardNo = hBillRealCardNo;
				hBillDestinationAmt = 0;
				hBillDestinationCnt = 0;
				firstCardNo = false;
			}
			
			if ("+".equals(getValue("ecs_sign_code"))) {
				hBillDestinationAmt += getValueDouble("dest_amt");
			} else {
				hBillDestinationAmt -= getValueDouble("dest_amt");
			}
			
			hBillDestinationCnt ++;

			hTshtDestinationAmt = hTshtDestinationAmt + hBillDestinationAmt;

			totalCnt++;
		}
		
		if (hBillDestinationAmt > 0) {
			insertTscDcstLog(keepCardNo);
		}

		closeCursor();

	}

    /***********************************************************************/
    void insertTscDcstLog(String keepCardNo) throws Exception {
        sqlCmd  = "insert into tsc_dcst_log ";
        sqlCmd += "(crt_date,";
        sqlCmd += "crt_time,";
        sqlCmd += "acct_month,";
        sqlCmd += "tran_code,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "dest_amt,";
        sqlCmd += "feedback_amt,";
        sqlCmd += "proc_flag,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_time)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "'I',";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "'N',";
        sqlCmd += "?,";
        
        //sqlCmd += "?,";//test only
        
        sqlCmd += "sysdate ";
        sqlCmd += " from tsc_vd_card a ";
        sqlCmd += " where vd_card_no  = ? ";
        sqlCmd += "   and new_end_date = (select max(new_end_date) from tsc_vd_card b where b.vd_card_no = ?) ";
        sqlCmd += " fetch first 1 rows only ";
        
        setString(1, hStmtCrtDate);
        setString(2, hStmtCrtTime);
        setString(3, hBillAcctMonth);
        setDouble(4, hBillDestinationAmt);
        setDouble(5, hStmtFeedbackAmt);
        setString(6, javaProgram);
        setString(7, keepCardNo);
        setString(8, keepCardNo);
        insertTable();
        if (dupRecord.equals("Y")) {
        	showLogMessage("E","","insert" + daoTable + " "+ keepCardNo + "duplicate!");
        }

    }

    /***********************************************************************/
    void insertTscDcstHst() throws Exception {
        setValue("acct_month"        , hBillAcctMonth);
        setValueDouble("dest_amt"    , hTshtDestinationAmt);
        setValueDouble("feedback_amt", hTshtFeedbackAmt);
        setValue("mod_pgm"           , javaProgram);
        setValue("mod_time"          , sysDate + sysTime);
        daoTable = "tsc_dcst_hst";
        insertTable();
        if (dupRecord.equals("Y")) {
        	showLogMessage("E","","insert_tsc_dcst_hst duplicate!");
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscB001V proc = new TscB001V();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
