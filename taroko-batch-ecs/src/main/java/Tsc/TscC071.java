/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*  DATE       Version    AUTHOR                       DESCRIPTION             *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/26  V1.01.00   Wendy Lu                     program initial         *
*  112/05/09  V1.01.01   Wilson     delete select tsc_fee_parm                *
*  112/05/26  V1.01.02   Wilson     調整where條件                                                                                       *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;


public class TscC071 extends AccessDAO {
    private final String progname = "悠遊VD卡停卡、毀補、卡到期產生餘額轉置-DCBQ程式  112/05/26 V1.01.02";
    CommFunction    comm = new CommFunction();
    CommCrd         comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hSystemDate = "";
    String hSystemTime = "";
    String hParmDate = "";
    String hTardNewTscCardNo = "";
    String hTardTscCardNo = "";
    String hTardCardNo = "";
    String hTardNewEndDate = "";
    String hTardRowid = "";
    int tempInt = 0;
    String hFeepExpireFeeFlag = "";
    double hFeepChargeFee = 0;
    String dateFmt = "";
    String dateFmm = "";
    String embossKind = "";
    String hDcbqBalanceDatePlan = "";
    String hDcbqEmbossKind = "";
    String sqlSt = "";
    int hFeepMonthMoney = 0;
    int hFeepMonthTimes = 0;
    int hFeepDaysToTsc = 0;
    int hCnt = 0;
    double tempAmt = 0;
    int totCnt = 0;
    int recCnt = 0;
    String chkPay = "";
    int hFeepUseTimes = 0;
    int hFeepUseMoney = 0;
    String sData = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : TscC071 [yyyymmdd] [batch_seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (comc.getSubString(hCallBatchSeqno, 0, 8).equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
                hCallBatchSeqno = "no-call";
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempUser = "";
            if (hCallBatchSeqno.length() == 20) {

                comcr.hCallBatchSeqno = hCallBatchSeqno;
                comcr.hCallRProgramCode = javaProgram;

                comcr.callbatch(0, 0, 1);
                sqlCmd = "select user_id ";
                sqlCmd += "from ptr_callbatch ";
                sqlCmd += "where batch_seqno = ? ";
                setString(1, hCallBatchSeqno);
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hTempUser = getValue("user_id");
                }
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            selectPtrBusinday();
        
            if (args.length > 0 && args[0].length() == 8) {
                hParmDate = args[0];
            }

            showLogMessage("I", "", String.format(" Process date =[%s]\n", hParmDate));

            selectTscCard();

            showLogMessage("I", "", String.format("程式執行結束,筆數=[%d][%d]\n", totCnt, recCnt));
            // ==============================================
            // 固定要做的
            if (hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1);
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
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = "select business_date, ";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_system_date, ";
        sqlCmd += "to_char(sysdate,'hh24miss') h_system_time, ";
        sqlCmd += "to_char(sysdate-1,'yyyymmdd') as h_process_date ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hSystemDate = getValue("h_system_date");
            hSystemTime = getValue("h_system_time");
            hParmDate = getValue("h_process_date");
        }

    }

    /***********************************************************************/
    void selectTscCard() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "new_tsc_card_no, ";
        sqlCmd += "tsc_card_no, ";
        sqlCmd += "vd_card_no, ";
        sqlCmd += "new_end_date, ";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from tsc_vd_card ";
        sqlCmd += "where current_code not in ('0','2') ";
        sqlCmd += "and return_date = '' "; 
        sqlCmd += "and balance_date = '' ";
        sqlCmd += "and tsc_oppost_date = ? ";
        setString(1, hParmDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTardNewTscCardNo = getValue("new_tsc_card_no", i);
            hTardTscCardNo = getValue("tsc_card_no", i);
            hTardCardNo = getValue("vd_card_no", i);
            hTardNewEndDate = getValue("new_end_date", i);
            hTardRowid = getValue("rowid", i);

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

            selectTscDcbqLog();
            if (tempInt > 0)
                continue;

            selectTscDcbdLog();
            if (tempInt > 0)
                continue;

            recCnt++;

            insertTscDcbqLog();

        }

    }

    /***********************************************************************/
    void selectTscDcbqLog() throws Exception {
        tempInt = 0;
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += "from tsc_dcbq_log ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTardTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

    }

    /***********************************************************************/
    void selectTscDcbdLog() throws Exception {
        tempInt = 0;
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += "from tsc_dcbd_log ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTardTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

    }

    /***********************************************************************/
     void insertTscDcbqLog() throws Exception {

    	hDcbqEmbossKind = "8";
        setValue("tsc_card_no", hTardTscCardNo);
        setValue("card_no", hTardCardNo);
        setValue("emboss_kind", hDcbqEmbossKind);
        setValue("create_date", hSystemDate);
        setValue("balance_date_plan", "");
        setValue("appr_user", javaProgram);
        setValue("appr_date", hSystemDate);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "tsc_dcbq_log";
        insertTable();
     }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscC071 proc = new TscC071();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
