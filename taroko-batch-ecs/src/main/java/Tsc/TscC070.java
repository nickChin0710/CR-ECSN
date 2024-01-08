/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  110/01/26  V1.00.00    Wendy Lu                     program initial        *
 *  112/05/26  V1.00.01    Wilson    不異動停卡日期                                                                                          *
 ******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;


public class TscC070 extends AccessDAO {
    private final String progname = "悠遊VD卡餘額轉置通知-DCBQ前置作業   112/05/26 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hSystemDate = "";
    String hSystemTime = "";
    String hDcbqTscCardNo = "";
    String hDcbqCardNo = "";
    String hDcbqEmbossKind = "";
    String hDcbqCreateDate = "";
    String hDcbqBalanceDatePlan = "";
    String hDcbqBalanceDate = "";
    String hDcbqBalanceDateRtn = "";
    String hDcbqRowid = "";
    String hFeepExpireFeeFlag = "";
    String dateFmt = "";
    int hFeepMonthMoney = 0;
    int hFeepMonthTimes = 0;
    int hFeepDaysToTsc = 0;
    int hFeepUseTimes = 0;
    String dateFmm = "";
    int hFeepUseMoney = 0;
    String hCardAcctType = "";
    String hCardAcctKey = "";
    String sqlSt = "";
    double hFeepChargeFee = 0;
    int hCnt = 0;
    double tempAmt = 0;
    int totCnt = 0;
    String chkPay = "";
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
            if (args.length > 1) {
                comc.errExit("Usage : TscC070 [batch_seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = this.getClass().getName();
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = "user_id ";
                daoTable = "ptr_callbatch ";
                whereStr = "where batch_seqno = ? ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            selectPtrBusinday();

            showLogMessage("I", "", String.format("Process Date =[%s]\n", hSystemDate));

            selectTscDcbqLog();

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = "select business_date, ";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_system_date, ";
        sqlCmd += "to_char(sysdate,'hh24miss') h_system_time  ";
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
        }

    }

    /***********************************************************************/
    void selectTscDcbqLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "tsc_card_no, ";
        sqlCmd += "card_no, ";
        sqlCmd += "emboss_kind, ";
        sqlCmd += "create_date, ";
        sqlCmd += "balance_date_plan, ";
        sqlCmd += "balance_date, ";
        sqlCmd += "balance_date_rtn, ";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from tsc_dcbq_log ";
        sqlCmd += "where balance_date_plan = '' ";
        sqlCmd += "and appr_user <> '' ";
        openCursor();
        while (fetchTable()) {
            hDcbqTscCardNo = getValue("tsc_card_no");
            hDcbqCardNo = getValue("card_no");
            hDcbqEmbossKind = getValue("emboss_kind");
            hDcbqCreateDate = getValue("create_date");
            hDcbqBalanceDatePlan = getValue("balance_date_plan");
            hDcbqBalanceDate = getValue("balance_date");
            hDcbqBalanceDateRtn = getValue("balance_date_rtn");
            hDcbqRowid = getValue("rowid");

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

            selectTscFeeParm();

            tempAmt = 0;
            if (hFeepExpireFeeFlag.equals("Y")) {
                chkPay = "Y";
                chkRtn();
                if (chkPay.equals("Y")) {
                    tempAmt = hFeepChargeFee;
                }
            }

            updateTscDcbqLog();
            updateTscCard();

        }
        closeCursor();

    }

    /***********************************************************************/
    void selectTscFeeParm() throws Exception {

        hFeepDaysToTsc = 0;
        hFeepExpireFeeFlag = "";
        hFeepChargeFee = 0;
        hFeepMonthTimes = 0;
        hFeepUseTimes = 0;
        hFeepMonthMoney = 0;
        hFeepUseMoney = 0;
        dateFmt = "";
        dateFmm = "";

        sqlCmd = "select days_to_tsc, ";
        sqlCmd += "decode(expire_fee_flag,'','N',expire_fee_flag) h_feep_expire_fee_flag, ";
        sqlCmd += "charge_fee, ";
        sqlCmd += "month_times, ";
        sqlCmd += "to_char(add_months(sysdate, month_times * -1),'yyyymm')||'01' date_fm_t, ";
        sqlCmd += "use_times, ";
        sqlCmd += "month_money, ";
        sqlCmd += "to_char(add_months(sysdate, month_money * -1),'yyyymm')||'01' date_fm_m, ";
        sqlCmd += "use_money ";
        sqlCmd += "from tsc_fee_parm ";
        sqlCmd += "where emboss_kind = ? ";
        setString(1, hDcbqEmbossKind);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_fee_parm not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hFeepDaysToTsc = getValueInt("days_to_tsc");
            hFeepExpireFeeFlag = getValue("h_feep_expire_fee_flag");
            hFeepChargeFee = getValueDouble("charge_fee");
            hFeepMonthTimes = getValueInt("month_times");
            dateFmt = getValue("date_fm_t");
            hFeepUseTimes = getValueInt("use_times");
            hFeepMonthMoney = getValueInt("month_money");
            dateFmm = getValue("date_fm_m");
            hFeepUseMoney = getValueInt("use_money");
        }

    }

    /***********************************************************************/
    void chkRtn() throws Exception {

        if (hFeepUseTimes > 0) {
            joinSqlTime();
            if (hCnt >= hFeepUseTimes) {
                chkPay = "N";
                return;
            }
        }

        if (hFeepUseMoney > 0) {
            joinSqlAmt();
            if (hCnt >= hFeepUseMoney) {
                chkPay = "N";
            }
        }

        return;
    }

    /***********************************************************************/
    void joinSqlTime() throws Exception {
        String dateTot = hSystemDate;
        sqlCmd = "select sum(case when txn_code in ('06','25','27','28','29') ";
        sqlCmd += "                then -1 else 1 end) as h_cnt ";
        sqlCmd += "from dbb_bill ";
        sqlCmd += "where card_no = ? ";
        sqlCmd += "and purchase_date >= ? ";
        sqlCmd += "and purchase_date <= ? ";
        sqlCmd += "and decode(rsk_type,'','N',rsk_type) = 'N' ";
        sqlCmd += "and acct_code  in ('BL','IT','CA','ID','AO','OT') ";
        setString(1, hDcbqCardNo);
        setString(2, dateFmt);
        setString(3, dateTot);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("join_sql_time() not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }
    }

    /***********************************************************************/
    void joinSqlAmt() throws Exception {
        String dateTom = hSystemDate;
        sqlCmd = "select sum(case when txn_code in ('06','25','27','28','29') ";
        sqlCmd += "                then dest_amt * -1 else dest_amt end) as h_cnt ";
        sqlCmd += "from dbb_bill ";
        sqlCmd += " where card_no = ? ";
        sqlCmd += " and purchase_date >= ? ";
        sqlCmd += " and purchase_date <= ? ";
        sqlCmd += " and decode(rsk_type,'','N',rsk_type) = 'N'  ";
        sqlCmd += " and acct_code in ('BL','IT','CA','ID','AO','OT') ";
        setString(1, hDcbqCardNo);
        setString(2, dateFmm);
        setString(3, dateTom);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("join_sql_amt() not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }
    }

    /***********************************************************************/
    void updateTscDcbqLog() throws Exception {
        sqlCmd = "select to_char(to_date(? ,'yyyymmdd') + ? days,'yyyymmdd') as h_dcbq_balance_date_plan ";
        sqlCmd += "from dual ";
        setString(1, hDcbqCreateDate);
        setInt(2, hFeepDaysToTsc);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDcbqBalanceDatePlan = getValue("h_dcbq_balance_date_plan");
        }

        daoTable = "tsc_dcbq_log";
        updateSQL = "balance_date_plan = ?,";
        updateSQL += "mod_pgm = ?,";
        updateSQL += "mod_time = sysdate";
        whereStr = "where rowid = ? ";
        setString(1, hDcbqBalanceDatePlan);
        setString(2, javaProgram);
        setRowId(3, hDcbqRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_dcbq_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateTscCard() throws Exception {
        daoTable = "tsc_vd_card ";
        updateSQL += "balance_rtn_fee = ?, ";
        updateSQL += "mod_pgm = ?, ";
        updateSQL += "mod_time = sysdate ";
        whereStr = "where tsc_card_no  = ? ";
        setDouble(1, tempAmt);
        setString(2, javaProgram);
        setString(3, hDcbqTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_vd_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscC070 proc = new TscC070();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
