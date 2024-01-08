/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/21  V1.00.00    phopho     program initial                          *
*  108/11/29  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/13  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB033 extends AccessDAO {
    private String progname = "催收延滯階段繳款狀況統計處理程式109/12/13  V1.00.02   ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";

    String hAcnoPSeqno = "";
    String hAcnoStmtCycle = "";
    String hWdayLastAcctMonth = "";
    String hWdayLlAcctMonth = "";
    double hAcctTtlAmt;
    double hAchtStmtThisTtlAmt;

    int    totalCnt              = 0;
    String hTempStmtCycle = "";
    String hTempAcctMonth = "";
    String hTempPaymentRate[] = new String[50];
    int hTempArrayMcode[]  = new int[50];;
    int hTempArrayCnt = 0;
    long staticCnt[][]        = new long[30][30];
    double staticAmt[][]        = new double[30][30];
    int    inta1, inta2;

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
                comc.errExit("Usage : ColB033 [month][stmt_cycle]", " 1.month      : 統計月份(yyyymm) 2.stmt_cycle : 統計 cycle");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            selectPtrBusinday();
            if (args.length == 1) {
                hTempAcctMonth = args[0];
                hTempStmtCycle = "";
            } else if (args.length == 2) {
                hTempAcctMonth = args[0];
                hTempStmtCycle = args[1];
            } else {
                if (selectPtrWorkday() != 0) {
                	exceptExit = 0;
                    comcr.errRtn("本日非 CYCLE+2日 不執行", "", hCallBatchSeqno);
                }
            }

            deleteColPaymentRate();
            showLogMessage("I", "", "Buisness_date[" + hBusiBusinessDate + "] last_acct_month[" + hTempAcctMonth
                    + "] stmt_cycle[" + hTempStmtCycle + "]");

            for (inta1 = 0; inta1 < 20; inta1++)
                for (inta2 = 0; inta2 < 20; inta2++) {
                    staticCnt[inta1][inta2] = 0;
                    staticAmt[inta1][inta2] = 0;
                }
            selectActAcno();

            showLogMessage("I", "",
                    "------------------------------------------------------------------------------------------");
            showLogMessage("I", "",
                    "本月\\上月        M0        M1        M2        M3        M4        M5        M6        M7+");
            showLogMessage("I", "",
                    "------------------------------------------------------------------------------------------");

            for (inta1 = 0; inta1 <= 7; inta1++) {
                if (inta1 != 7)
                    showLogMessage("I", "", String.format("M%1d   CNT= ",inta1));
                else
                    showLogMessage("I", "", String.format("M%1d   CNT= ",inta1));
                for (inta2 = 0; inta2 <= 7; inta2++) {
                    insertColPaymentRate();
                    showLogMessage("I", "", String.format("%9d ", staticCnt[inta1][inta2]));
                }
                showLogMessage("I", "", "     AMT= ");
                for (inta2 = 0; inta2 <= 7; inta2++) {
                    showLogMessage("I", "", String.format("%9.0f ", staticAmt[inta1][inta2]));
                }
                showLogMessage("I", "", "");
            }
            showLogMessage("I", "",
                    "------------------------------------------------------------------------------------------");
            showLogMessage("I", "", "累計筆數:[" + totalCnt + "]");
            showLogMessage("I", "", "程式執行結束");

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
        hTempAcctMonth = "";
        sqlCmd = "select business_date, ";
        sqlCmd += "to_char(add_months(to_date(business_date,'yyyymmdd'),-1),'yyyymm') acct_month ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempAcctMonth = getValue("acct_month");
        }
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        int int1a, inta, intb;
        sqlCmd = "select ";
        sqlCmd += "payment_rate1,";
        sqlCmd += "payment_rate2,";
        sqlCmd += "payment_rate3,";
        sqlCmd += "payment_rate4,";
        sqlCmd += "payment_rate5,";
        sqlCmd += "payment_rate6,";
        sqlCmd += "payment_rate7,";
        sqlCmd += "payment_rate8,";
        sqlCmd += "payment_rate9,";
        sqlCmd += "payment_rate10,";
        sqlCmd += "payment_rate11,";
        sqlCmd += "payment_rate12,";
        sqlCmd += "payment_rate13,";
        sqlCmd += "payment_rate14,";
        sqlCmd += "payment_rate15,";
        sqlCmd += "payment_rate16,";
        sqlCmd += "payment_rate17,";
        sqlCmd += "payment_rate18,";
        sqlCmd += "payment_rate19,";
        sqlCmd += "payment_rate20,";
        sqlCmd += "payment_rate21,";
        sqlCmd += "payment_rate22,";
        sqlCmd += "payment_rate23,";
        sqlCmd += "payment_rate24,";
        sqlCmd += "payment_rate25,";
//        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "b.stmt_cycle,";
        sqlCmd += "months_between(to_date(this_acct_month,'yyyymm'),";
        sqlCmd += "        to_date(?,'yyyymm')) temp_array_cnt,";
        sqlCmd += "b.last_acct_month,";
        sqlCmd += "to_char(add_months(to_date(?,'yyyymm'),-1),'yyyymmdd') ll_acct_month ";
        sqlCmd += "FROM    act_acno a,ptr_workday b ";
        sqlCmd += "WHERE   acct_status != '4' ";
        sqlCmd += "AND     a.stmt_cycle = b.stmt_cycle ";
        sqlCmd += "AND     a.stmt_cycle = decode(cast(? as varchar(10)),'',a.stmt_cycle,cast(? as varchar(10))) ";
        sqlCmd += "AND     acno_flag <> 'Y' ";
        sqlCmd += "AND     months_between(to_date(last_acct_month,'yyyymm'), ";
        sqlCmd += "        to_date(?,'yyyymm')) between 0 and 24 ";
        setString(1, hTempAcctMonth);
        setString(2, hTempAcctMonth);
        setString(3, hTempStmtCycle);
        setString(4, hTempStmtCycle);
        setString(5, hTempAcctMonth);

        openCursor();
        while (fetchTable()) {
//            h_acno_p_seqno = getValue("p_seqno");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hTempArrayCnt = getValueInt("temp_array_cnt");
            hWdayLastAcctMonth = hTempAcctMonth;
            hWdayLlAcctMonth = getValue("ll_acct_month");
            hTempPaymentRate[0] = getValue("payment_rate1");
            hTempPaymentRate[1] = getValue("payment_rate2");
            hTempPaymentRate[2] = getValue("payment_rate3");
            hTempPaymentRate[3] = getValue("payment_rate4");
            hTempPaymentRate[4] = getValue("payment_rate5");
            hTempPaymentRate[5] = getValue("payment_rate6");
            hTempPaymentRate[6] = getValue("payment_rate7");
            hTempPaymentRate[7] = getValue("payment_rate8");
            hTempPaymentRate[8] = getValue("payment_rate9");
            hTempPaymentRate[9] = getValue("payment_rate10");
            hTempPaymentRate[10] = getValue("payment_rate11");
            hTempPaymentRate[11] = getValue("payment_rate12");
            hTempPaymentRate[12] = getValue("payment_rate13");
            hTempPaymentRate[13] = getValue("payment_rate14");
            hTempPaymentRate[14] = getValue("payment_rate15");
            hTempPaymentRate[15] = getValue("payment_rate16");
            hTempPaymentRate[16] = getValue("payment_rate17");
            hTempPaymentRate[17] = getValue("payment_rate18");
            hTempPaymentRate[18] = getValue("payment_rate19");
            hTempPaymentRate[19] = getValue("payment_rate20");
            hTempPaymentRate[20] = getValue("payment_rate21");
            hTempPaymentRate[21] = getValue("payment_rate22");
            hTempPaymentRate[22] = getValue("payment_rate23");
            hTempPaymentRate[23] = getValue("payment_rate24");
            hTempPaymentRate[24] = getValue("payment_rate25");

            for (int i = 0; i < 25; i++)
                hTempArrayMcode[i] = comcr.str2int(hTempPaymentRate[i]);

            int1a = hTempArrayCnt;

            if (hTempPaymentRate[int1a + 1].length() == 0)
                continue;
            if ((hTempArrayMcode[int1a] >= 7) && (hTempArrayMcode[int1a + 1] >= 7))
                continue;

            inta = hTempArrayMcode[int1a];
            intb = hTempArrayMcode[int1a + 1];
            if (hTempArrayMcode[int1a] >= 7)
                inta = 7;
            if (hTempArrayMcode[int1a + 1] >= 7)
                intb = 7;

            totalCnt++;
            if (totalCnt % 100000 == 0) {
                showLogMessage("I", "", "    處理筆數:[" + totalCnt + "]");
            }

            if (hWdayLlAcctMonth.compareTo(hWdayLastAcctMonth) == 0)
                selectActAcct();
            else
                selectActAcctHst();

            if (intb + 1 < inta)
                showLogMessage("I", "", "P_SEQNO[" + hAcnoPSeqno + "] [" + intb + "][" + hTempPaymentRate[int1a + 1]
                        + "]=>[" + inta + "][" + hTempPaymentRate[int1a] + "] amt[" + hAcctTtlAmt + "]");
            staticCnt[inta][intb]++;
            staticAmt[inta][intb] = staticAmt[inta][intb] + hAcctTtlAmt;
        }
        closeCursor();
    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        hTempAcctMonth = "";
        hTempStmtCycle = "";
        sqlCmd = "select last_acct_month, ";
        sqlCmd += "stmt_cycle ";
        sqlCmd += "from ptr_workday ";
        sqlCmd += "where this_close_date = to_char(to_date(?,'yyyymmdd')-2 days,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempAcctMonth = getValue("last_acct_month");
            hTempStmtCycle = getValue("stmt_cycle");
        }
        if (notFound.equals("Y"))
            return 1;
        return 0;
    }

    /***********************************************************************/
    void deleteColPaymentRate() throws Exception {
        daoTable = "col_payment_rate";
        whereStr = "where acct_month = ? ";
        whereStr += "and   stmt_cycle = decode(cast(? as varchar(8)),'',stmt_cycle,cast(? as varchar(8)))";
        setString(1, hTempAcctMonth);
        setString(2, hTempStmtCycle);
        setString(3, hTempStmtCycle);
        deleteTable();
    }

    /***********************************************************************/
    void insertColPaymentRate() throws Exception {
        long staticData;
        double staticData1;
        staticData = staticCnt[inta1][inta2];
        staticData1 = staticAmt[inta1][inta2];

        daoTable = "col_payment_rate";
        extendField = daoTable + ".";
        setValue(extendField+"acct_month", hTempAcctMonth);
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValueInt(extendField+"this_month_mcode", inta1);
        setValueInt(extendField+"last_month_mcode", inta2);
        setValueLong(extendField+"static_cnt", staticData);
        setValueDouble(extendField+"static_amt", staticData1);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_col_payment_rate duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctTtlAmt = 0;
        sqlCmd = "select ttl_amt ";
        sqlCmd += "from act_acct ";
        sqlCmd += "where p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acct.";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_act_acct not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcctTtlAmt = getValueDouble("act_acct.ttl_amt");
        }
    }

    /***********************************************************************/
    void selectActAcctHst() throws Exception {
        hAcctTtlAmt = 0;
        sqlCmd = "select stmt_this_ttl_amt ";
        sqlCmd += "from act_acct_hst ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and   acct_month = ? ";
        setString(1, hAcnoPSeqno);
        setString(2, hTempAcctMonth);
        
        extendField = "act_acct_hst.";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcctTtlAmt = getValueDouble("act_acct_hst.stmt_this_ttl_amt");
        }
    }

//    /***********************************************************************/
//    void select_act_acct_hst_1() throws Exception {
//        h_acht_stmt_this_ttl_amt = 0;
//        sqlCmd = "select stmt_this_ttl_amt ";
//        sqlCmd += "from act_acct_hst ";
//        sqlCmd += "where p_seqno = ? ";
//        sqlCmd += "and   acct_month = ? ";
//        setString(1, h_acno_p_seqno);
//        setString(2, h_wday_ll_acct_month);
//
//        int recordCnt = selectTable();
//        if (recordCnt > 0) {
//            h_acht_stmt_this_ttl_amt = getValueDouble("stmt_this_ttl_amt");
//        }
//    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB033 proc = new ColB033();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
