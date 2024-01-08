/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  98/03/12  V1.01.01  Lai        RECS971231-045                              *
*  98/07/14  V1.02.01  Lai        BECS-980710-103 bil_sysexp1 mark            *
* 103/09/15  V1.03.02  Lai        RECS-XXXXXX=XXX 續不續卡 均產生                                                  *
* 106/06/01  V1.04.00  Edson      program initial                             *
* 107/03/06  V1.04.01  詹曜維                RECS-s1070126-008 鎖卡可餘額轉置                                                *
* 107/09/26  V1.04.02  David FU   ECS-1070126-008(JAVA)                       *
* 109-11-16  V1.04.03  tanwei    updated for project coding standard        *
* 112/05/09  V1.04.04  Wilson     delete select tsc_fee_parm                  *
* 112/05/26  V1.04.05  Wilson     調整where條件                                                                                             *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*停卡、毀補、卡到期產生餘額轉置-BTRQ程式V1.03.02*/
public class TscC061 extends AccessDAO {
    private final String progname = "停卡、毀補、卡到期產生餘額轉置-BTRQ程式  112/05/26 V1.04.05";
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
    String hBtrqBalanceDatePlan = "";
    String hBtrqEmbossKind = "";
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
                comc.errExit("Usage : TscC061 [yyyymmdd] [batch_seq]", "");
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
                sqlCmd += " from ptr_callbatch  ";
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
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_system_time, ";
        sqlCmd += "to_char(sysdate-1,'yyyymmdd') as h_process_date ";
        sqlCmd += " from ptr_businday  ";
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
        sqlCmd += "new_tsc_card_no,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "card_no,";
        sqlCmd += "new_end_date,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from tsc_card ";
        sqlCmd += "where current_code not in ('0','2') ";
        sqlCmd += "and return_date = '' "; 
        sqlCmd += "and balance_date = '' ";
        sqlCmd += "and tsc_oppost_date = ? ";
        setString(1, hParmDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTardNewTscCardNo = getValue("new_tsc_card_no", i);
            hTardTscCardNo = getValue("tsc_card_no", i);
            hTardCardNo = getValue("card_no", i);
            hTardNewEndDate = getValue("new_end_date", i);
            hTardRowid = getValue("rowid", i);

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

            selectTscBtrqLog();
            if (tempInt > 0)
                continue;

            selectTscBtrdLog();
            if (tempInt > 0)
                continue;

            recCnt++;

            insertTscBtrqLog();
 
        }

    }

    /***********************************************************************/
    void selectTscBtrqLog() throws Exception {
        tempInt = 0;
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += " from tsc_btrq_log  ";
        sqlCmd += "where tsc_card_no  = ? ";
        setString(1, hTardTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

    }

    /***********************************************************************/
    void selectTscBtrdLog() throws Exception {
        tempInt = 0;
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += " from tsc_btrd_log  ";
        sqlCmd += "where tsc_card_no  = ? ";
        setString(1, hTardTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

    }

    /***********************************************************************/
    void insertTscBtrqLog() throws Exception {

        hBtrqEmbossKind = "8";
        setValue("tsc_card_no", hTardTscCardNo);
        setValue("card_no", hTardCardNo);
        setValue("emboss_kind", hBtrqEmbossKind);
        setValue("create_date", hSystemDate);
        setValue("balance_date_plan", "");
        setValue("appr_user", javaProgram);
        setValue("appr_date", hSystemDate);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "tsc_btrq_log";
        insertTable();
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscC061 proc = new TscC061();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
