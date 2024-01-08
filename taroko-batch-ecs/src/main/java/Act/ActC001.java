/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/28  V1.00.01    SUP       error correction                          *
 *  106/11/22  V1.10.01    陳君暘          BECS-1061124-094 keep payment_rate1       *
 *  109/03/22  V1.11.01    陳君暘          BECS-1090319-018 change acct_month rule   *
 *  109/03/31  V1.11.01    Brian     update to V1.11.01                        *
 *  110/07/05  V1.12.00    Brian     Record last execution date             
 *  111-10-13  V1.00.02    Machao    sync from mega & updated for project coding standard *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*更改cycle批次處理程式*/
public class ActC001 extends AccessDAO {


    private final String PROGNAME = "更改cycle批次處理程式  111-10-13  V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hWdayStmtCycle = "";
    String hWdayLastAcctMonth = "";
    String hWdayThisAcctMonth = "";
    String hWdayNextAcctMonth = "";
    String hWdayThisCloseDate = "";
    String hTempNextAcctMonth = "";
    String hAcceCardIndicator = "";
    String hAcceStmtCycle = "";
    String hAcceNewStmtCycle = "";
    String hAcceCorpPSeqno = "";
    String hAcceIdPSeqno = "";
    String hAcceLastCycleMonth = "";
    String hAcceAprDate = "";
    String hAcceRowid = "";
    String hTempNewCycleDate = "";
    String hAcceLastInterestDate = "";
    String hAcceBatchProcDate = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctPSeqno = "";
    String hAcnoRowid = "";
    String hAcnoNewCycleMonth = "";
    String hAcagAcctMonth = "";
    String hTempAcctMonth = "";
    double hAcagPayAmt = 0;
    String hAcagRowid = "";
    String hAcurCurrCode = "";
    int actAcctCurrCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : ActC001 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            if (args.length != 0 && args[0].length() == 8) {
                hBusiBusinessDate = args[0];
            } else {
                selectPtrBusinday();
            }
            deleteActChgCycle();
            selectPtrWorkday();
            
            updatePtrSysParm();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
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
        hBusiBusinessDate = "";

        sqlCmd = "select business_date ";
        sqlCmd += "  from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

    /***********************************************************************/
    void deleteActChgCycle() throws Exception {
        daoTable = "act_chg_cycle";
        whereStr = "where apr_date = ''  ";
        whereStr += "  and months_between(to_date(?,'yyyymmdd'), to_date(chg_cycle_date,'yyyymmdd')) >= 1 ";
        whereStr += "  and decode(batch_proc_mark,'','N',batch_proc_mark) = 'N' ";
        setString(1, hBusiBusinessDate);
        deleteTable();

    }

    /***********************************************************************/
    void selectPtrWorkday() throws Exception {
        sqlCmd = "select stmt_cycle,";
        sqlCmd += " last_acct_month,";
        sqlCmd += " this_acct_month,";
        sqlCmd += " next_acct_month,";
        sqlCmd += " this_close_date,";
        sqlCmd += " to_char(add_months(to_date(next_acct_month,'yyyymm'),1),'yyyymm') h_temp_next_acct_month ";
        sqlCmd += "  from ptr_workday ";
        sqlCmd += " where next_close_date = ? ";
        setString(1, hBusiBusinessDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayLastAcctMonth = getValue("last_acct_month");
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayNextAcctMonth = getValue("next_acct_month");
            hWdayThisCloseDate = getValue("this_close_date");
            hTempNextAcctMonth = getValue("h_temp_next_acct_month");

            showLogMessage("I", "",
                    String.format("cycle[%s] last[%s]  this[%s]  next[%s]  date[%s]  nextnext[%s]", hWdayStmtCycle,
                            hWdayLastAcctMonth, hWdayThisAcctMonth, hWdayNextAcctMonth,
                            hWdayThisCloseDate, hTempNextAcctMonth));
            selectActChgCycle();
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectActChgCycle() throws Exception {

        sqlCmd = "select a.card_indicator,";
        sqlCmd += " a.stmt_cycle,";
        sqlCmd += " a.new_stmt_cycle,";
        sqlCmd += " a.corp_p_seqno,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " a.last_cycle_month,";
        sqlCmd += " a.apr_date,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "  from act_chg_cycle a ";
        sqlCmd += " where a.stmt_cycle = ? ";
        sqlCmd += "   and a.apr_date != '' ";
        sqlCmd += "   and a.chg_cycle_date in (select max(b.chg_cycle_date) " + "  from act_chg_cycle b "
                + " where b.id_p_seqno        = a.id_p_seqno ";
        sqlCmd += "   and b.apr_date      != '' ";
        sqlCmd += "   and decode(b.batch_proc_mark,'','N',b.batch_proc_mark) = 'N' "
                + "   and b.CARD_INDICATOR = a.CARD_INDICATOR) ";
        sqlCmd += "   and decode(a.batch_proc_mark,'','N',a.batch_proc_mark) = 'N' ";
        setString(1, hWdayStmtCycle);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcceCardIndicator = getValue("card_indicator", i);
            hAcceStmtCycle = getValue("stmt_cycle", i);
            hAcceNewStmtCycle = getValue("new_stmt_cycle", i);
            hAcceCorpPSeqno = getValue("corp_p_seqno", i);
            hAcceIdPSeqno = getValue("id_p_seqno", i);
            hAcceLastCycleMonth = getValue("last_cycle_month", i);
            hAcceAprDate = getValue("apr_date", i);
            hAcceRowid = getValue("rowid", i);

            // showLogMessage("I", "", String.format("acct_key[%s]",
            // h_acce_p_seqno));

            hAcceLastInterestDate = "";
            selectActChgCycle1();
            if (hAcceLastInterestDate.length() == 0)
                hAcceLastInterestDate = hWdayThisCloseDate;

            if (hAcceNewStmtCycle.compareTo(hAcceStmtCycle) < 0) {
                hAcnoNewCycleMonth = hTempNextAcctMonth;
            } else {
                hAcnoNewCycleMonth = hWdayNextAcctMonth;
            }
            if (hAcceCardIndicator.equals("1")) {
                selectActAcno1();
                selectEcsActAcno1();
            }
            if (hAcceCardIndicator.equals("2")) {
                selectActAcno2();
                selectEcsActAcno2();
            }
            updateActChgCycle();
            updateActChgCycle1();

        }

    }

    /***********************************************************************/
    void selectActChgCycle1() throws Exception {
        String tempProcDate = "";

        sqlCmd = "select new_cycle_month||new_stmt_cycle h_temp_new_cycle_date,";
        sqlCmd += " to_char( add_months( to_date(new_cycle_month,'yyyymm')"
                + ", decode(sign(new_stmt_cycle-stmt_cycle),1,-1,-2))"
                + ", 'yyyymm')||stmt_cycle h_acce_last_interest_date,";
        sqlCmd += " batch_proc_date ";
        sqlCmd += "  from act_chg_cycle ";
        sqlCmd += " where decode(batch_proc_mark,'','N',batch_proc_mark) = 'Y' ";
        sqlCmd += "   and stmt_cycle       = ? ";
        sqlCmd += "   and id_p_seqno          = ? ";
        sqlCmd += "   and new_cycle_month != '' ";
        sqlCmd += " order by batch_proc_date desc ";
        setString(1, hWdayStmtCycle);
        setString(2, hAcceIdPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTempNewCycleDate = getValue("h_temp_new_cycle_date", i);
            hAcceLastInterestDate = getValue("h_acce_last_interest_date", i);
            hAcceBatchProcDate = getValue("batch_proc_date", i);

            if (tempProcDate.compareTo(hTempNewCycleDate) > 0)
                break;
            tempProcDate = String.format("%s", hAcceBatchProcDate);
        }
    }

    /***********************************************************************/
    void selectActAcno1() throws Exception {
        sqlCmd = "select acct_type,";
        sqlCmd += " acct_key,";
      //sqlCmd += " p_seqno,";
      //sqlCmd += " gp_no,"; // acct_p_seqno
        sqlCmd += " acno_p_seqno,";
        sqlCmd += " p_seqno,"; // acct_p_seqno
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acno ";
        sqlCmd += " where card_indicator = ? ";
        sqlCmd += "   and acno_p_seqno   = p_seqno "; // acno_flag <> 'Y'
        sqlCmd += "   and stmt_cycle     = ? ";
        sqlCmd += "   and id_p_seqno     = ? ";
        setString(1, hAcceCardIndicator);
        setString(2, hWdayStmtCycle);
        setString(3, hAcceIdPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoAcctType = getValue("acct_type", i);
            hAcnoAcctKey = getValue("acct_key", i);
          //h_acno_p_seqno = getValue("p_seqno", i);
          //h_acno_acct_p_seqno = getValue("gp_no", i); // acct_p_seqno
            hAcnoPSeqno = getValue("acno_p_seqno", i);
            hAcnoAcctPSeqno = getValue("p_seqno", i); // acct_p_seqno
            hAcnoRowid = getValue("rowid", i);

            if (hAcceNewStmtCycle.compareTo(hAcceStmtCycle) < 0) {
                insertActAcctHst();
                insertActCurrHst();
                insertActAnalSub();
                updatePaymentRate();
                selectActAcag();
                selectActAcctCurr();
                selectActAcagCurr();
            }

            if (hAcceCardIndicator.equals("1"))
                updateActAcno();
            if (hAcceCardIndicator.equals("2"))
                updateActAcno1();
            updateActAcag();
            updateActAcagCurr();
            updateActAcct();
            updateActChkno();
            updateActMod1();
            updateActMod2();
            updateActDebt();
            updateBilBill();
            updateBilCurpost();
            updateCrdCard();
            updateCrdIdno();
            updateCycPyaj();
            updateCycAfee();

        }

    }

    /***********************************************************************/
    void selectEcsActAcno1() throws Exception {
        sqlCmd = "select acct_type,";
        sqlCmd += " acct_key,";
      //sqlCmd += " p_seqno,";
      //sqlCmd += " acct_p_seqno,";
        sqlCmd += " acno_p_seqno,";
        sqlCmd += " p_seqno,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from ecs_act_acno ";
        sqlCmd += " where card_indicator = ? ";
      //sqlCmd += "   and p_seqno        = acct_p_seqno ";
        sqlCmd += "   and acno_p_seqno   = p_seqno ";
        sqlCmd += "   and stmt_cycle     = ? ";
        sqlCmd += "   and id_p_seqno     = ? ";
        setString(1, hAcceCardIndicator);
        setString(2, hWdayStmtCycle);
        setString(3, hAcceIdPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoAcctType = getValue("acct_type", i);
            hAcnoAcctKey = getValue("acct_key", i);
          //h_acno_p_seqno = getValue("p_seqno", i);
          //h_acno_acct_p_seqno = getValue("acct_p_seqno", i);
            hAcnoPSeqno = getValue("acno_p_seqno", i);
            hAcnoAcctPSeqno = getValue("p_seqno", i);
            hAcnoRowid = getValue("rowid", i);

            /*
             * 已瘦身不用處理 if (strcmp((const char *)h_acce_new_stmt_cycle.arr,
             * (const char *)h_acce_stmt_cycle.arr)<0) { insert_act_acct_hst();
             * insert_act_anal_sub(); select_act_acag(); }
             */

            if (hAcceCardIndicator.equals("1"))
                updateEcsActAcno();
            if (hAcceCardIndicator.equals("2"))
                updateEcsActAcno1();
            updateActAcag();
            updateActAcagCurr();
            updateEcsActAcct();
            updateActChkno();
            updateActMod1();
            updateActMod2();
            updateActDebt();
            updateBilBill();
            updateBilCurpost();
            updateEcsCrdCard();
            updateCrdIdno();
            updateCycPyaj();
            updateCycAfee();

        }

    }

    /***********************************************************************/
    void selectActAcno2() throws Exception {
        sqlCmd = "select acct_type,";
        sqlCmd += " acct_key,";
      //sqlCmd += " p_seqno,";
        sqlCmd += " acno_p_seqno,";
        sqlCmd += " p_seqno,"; // acct_p_seqno
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acno ";
        sqlCmd += " where card_indicator = ? ";
      //sqlCmd += "   and p_seqno        = acct_p_seqno ";
        sqlCmd += "   and acno_p_seqno   = p_seqno ";
        sqlCmd += "   and stmt_cycle     = ? ";
        sqlCmd += "   and corp_p_seqno   = ? ";
        setString(1, hAcceCardIndicator);
        setString(2, hWdayStmtCycle);
        setString(3, hAcceCorpPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoAcctType = getValue("acct_type", i);
            hAcnoAcctKey = getValue("acct_key", i);
          //h_acno_p_seqno = getValue("p_seqno", i);
          //h_acno_acct_p_seqno = getValue("p_seqno", i); // acct_p_seqno
            hAcnoPSeqno = getValue("acno_p_seqno", i);
            hAcnoAcctPSeqno = getValue("p_seqno", i); // acct_p_seqno
            hAcnoRowid = getValue("rowid", i);

            if (hAcceNewStmtCycle.compareTo(hAcceStmtCycle) < 0) {
                insertActAcctHst();
                insertActCurrHst();
                insertActAnalSub();
                updatePaymentRate();
                selectActAcag();
                selectActAcctCurr();
                selectActAcagCurr();
            }

            if (hAcceCardIndicator.equals("1"))
                updateActAcno();
            if (hAcceCardIndicator.equals("2"))
                updateActAcno1();
            updateActAcag();
            updateActAcagCurr();
            updateActAcct();
            updateActChkno();
            updateActMod1();
            updateActMod2();
            updateActDebt();
            updateBilBill();
            updateBilCurpost();
            updateCrdCard();
            updateCrdIdno();
            updateCycPyaj();
            updateCycAfee();

        }

    }

    /***********************************************************************/
    void insertActAcctHst() throws Exception {
        daoTable = "act_acct_hst";
        sqlCmd = "insert into act_acct_hst (";
        sqlCmd += "P_SEQNO,";
        sqlCmd += "ACCT_MONTH,";
        sqlCmd += "ACCT_TYPE,";
        sqlCmd += "CORP_P_SEQNO,";
        sqlCmd += "ID_P_SEQNO,";
        sqlCmd += "STMT_CYCLE,";
        sqlCmd += "ACCT_JRNL_BAL,";
        sqlCmd += "BEG_BAL_OP,";
        sqlCmd += "END_BAL_OP,";
        sqlCmd += "BEG_BAL_LK,";
        sqlCmd += "END_BAL_LK,";
        sqlCmd += "OVERPAY_LOCK_STA_DATE,";
        sqlCmd += "OVERPAY_LOCK_DUE_DATE,";
        sqlCmd += "TEMP_UNBILL_INTEREST,";
        sqlCmd += "UNBILL_BEG_BAL_AF,";
        sqlCmd += "UNBILL_BEG_BAL_LF,";
        sqlCmd += "UNBILL_BEG_BAL_CF,";
        sqlCmd += "UNBILL_BEG_BAL_PF,";
        sqlCmd += "UNBILL_BEG_BAL_BL,";
        sqlCmd += "UNBILL_BEG_BAL_CA,";
        sqlCmd += "UNBILL_BEG_BAL_IT,";
        sqlCmd += "UNBILL_BEG_BAL_ID,";
        sqlCmd += "UNBILL_BEG_BAL_RI,";
        sqlCmd += "UNBILL_BEG_BAL_PN,";
        sqlCmd += "UNBILL_BEG_BAL_AO,";
        sqlCmd += "UNBILL_BEG_BAL_OT,";
        sqlCmd += "UNBILL_BEG_BAL_AI,";
        sqlCmd += "UNBILL_BEG_BAL_SF,";
        sqlCmd += "UNBILL_BEG_BAL_DP,";
        sqlCmd += "UNBILL_BEG_BAL_CB,";
        sqlCmd += "UNBILL_BEG_BAL_CI,";
        sqlCmd += "UNBILL_BEG_BAL_CC,";
        sqlCmd += "UNBILL_BEG_BAL_DB,";
        sqlCmd += "UNBILL_END_BAL_AF,";
        sqlCmd += "UNBILL_END_BAL_LF,";
        sqlCmd += "UNBILL_END_BAL_CF,";
        sqlCmd += "UNBILL_END_BAL_PF,";
        sqlCmd += "UNBILL_END_BAL_BL,";
        sqlCmd += "UNBILL_END_BAL_CA,";
        sqlCmd += "UNBILL_END_BAL_IT,";
        sqlCmd += "UNBILL_END_BAL_ID,";
        sqlCmd += "UNBILL_END_BAL_RI,";
        sqlCmd += "UNBILL_END_BAL_PN,";
        sqlCmd += "UNBILL_END_BAL_AO,";
        sqlCmd += "UNBILL_END_BAL_OT,";
        sqlCmd += "UNBILL_END_BAL_AI,";
        sqlCmd += "UNBILL_END_BAL_SF,";
        sqlCmd += "UNBILL_END_BAL_DP,";
        sqlCmd += "UNBILL_END_BAL_CB,";
        sqlCmd += "UNBILL_END_BAL_CI,";
        sqlCmd += "UNBILL_END_BAL_CC,";
        sqlCmd += "UNBILL_END_BAL_DB,";
        sqlCmd += "BILLED_BEG_BAL_AF,";
        sqlCmd += "BILLED_BEG_BAL_LF,";
        sqlCmd += "BILLED_BEG_BAL_CF,";
        sqlCmd += "BILLED_BEG_BAL_PF,";
        sqlCmd += "BILLED_BEG_BAL_BL,";
        sqlCmd += "BILLED_BEG_BAL_CA,";
        sqlCmd += "BILLED_BEG_BAL_IT,";
        sqlCmd += "BILLED_BEG_BAL_ID,";
        sqlCmd += "BILLED_BEG_BAL_RI,";
        sqlCmd += "BILLED_BEG_BAL_PN,";
        sqlCmd += "BILLED_BEG_BAL_AO,";
        sqlCmd += "BILLED_BEG_BAL_OT,";
        sqlCmd += "BILLED_BEG_BAL_AI,";
        sqlCmd += "BILLED_BEG_BAL_SF,";
        sqlCmd += "BILLED_BEG_BAL_DP,";
        sqlCmd += "BILLED_BEG_BAL_CB,";
        sqlCmd += "BILLED_BEG_BAL_CI,";
        sqlCmd += "BILLED_BEG_BAL_CC,";
        sqlCmd += "BILLED_BEG_BAL_DB,";
        sqlCmd += "BILLED_END_BAL_AF,";
        sqlCmd += "BILLED_END_BAL_LF,";
        sqlCmd += "BILLED_END_BAL_CF,";
        sqlCmd += "BILLED_END_BAL_PF,";
        sqlCmd += "BILLED_END_BAL_BL,";
        sqlCmd += "BILLED_END_BAL_CA,";
        sqlCmd += "BILLED_END_BAL_IT,";
        sqlCmd += "BILLED_END_BAL_ID,";
        sqlCmd += "BILLED_END_BAL_RI,";
        sqlCmd += "BILLED_END_BAL_PN,";
        sqlCmd += "BILLED_END_BAL_AO,";
        sqlCmd += "BILLED_END_BAL_OT,";
        sqlCmd += "BILLED_END_BAL_AI,";
        sqlCmd += "BILLED_END_BAL_SF,";
        sqlCmd += "BILLED_END_BAL_DP,";
        sqlCmd += "BILLED_END_BAL_CB,";
        sqlCmd += "BILLED_END_BAL_CI,";
        sqlCmd += "BILLED_END_BAL_CC,";
        sqlCmd += "BILLED_END_BAL_DB,";
        sqlCmd += "MIN_PAY,";
        sqlCmd += "MIN_PAY_BAL,";
        sqlCmd += "RC_MIN_PAY,";
        sqlCmd += "RC_MIN_PAY_BAL,";
        sqlCmd += "RC_MIN_PAY_M0,";
        sqlCmd += "AUTOPAY_BEG_AMT,";
        sqlCmd += "AUTOPAY_BAL,";
        sqlCmd += "PAY_BY_STAGE_AMT,";
        sqlCmd += "PAY_BY_STAGE_BAL,";
        sqlCmd += "PAY_BY_STAGE_DATE,";
        sqlCmd += "PAYMENT_STATUS,";
        sqlCmd += "LAST_PAYMENT_DATE,";
        sqlCmd += "LAST_MIN_PAY_DATE,";
        sqlCmd += "LAST_CANCEL_DEBT_DATE,";
        sqlCmd += "BILL_INTEREST,";
        sqlCmd += "TTL_AMT_BAL,";
        sqlCmd += "ADI_BEG_BAL,";
        sqlCmd += "ADI_END_BAL,";
        sqlCmd += "ADI_D_AVAIL,";
        sqlCmd += "DELAYPAY_OK_FLAG,";
        sqlCmd += "MOD_TIME,";
        sqlCmd += "MOD_PGM,";
        sqlCmd += "WAIVE_TTL_BAL,";
        sqlCmd += "chg_cycle_flag";
        ///////////// SELECT/////////////
        sqlCmd += ") select ";
        sqlCmd += "P_SEQNO,";
        sqlCmd += "?,";
        sqlCmd += "ACCT_TYPE,";
        sqlCmd += "CORP_P_SEQNO,";
        sqlCmd += "ID_P_SEQNO,";
        sqlCmd += "STMT_CYCLE,";
        sqlCmd += "ACCT_JRNL_BAL,";
        sqlCmd += "BEG_BAL_OP,";
        sqlCmd += "END_BAL_OP,";
        sqlCmd += "BEG_BAL_LK,";
        sqlCmd += "END_BAL_LK,";
        sqlCmd += "OVERPAY_LOCK_STA_DATE,";
        sqlCmd += "OVERPAY_LOCK_DUE_DATE,";
        sqlCmd += "TEMP_UNBILL_INTEREST,";
        sqlCmd += "UNBILL_BEG_BAL_AF,";
        sqlCmd += "UNBILL_BEG_BAL_LF,";
        sqlCmd += "UNBILL_BEG_BAL_CF,";
        sqlCmd += "UNBILL_BEG_BAL_PF,";
        sqlCmd += "UNBILL_BEG_BAL_BL,";
        sqlCmd += "UNBILL_BEG_BAL_CA,";
        sqlCmd += "UNBILL_BEG_BAL_IT,";
        sqlCmd += "UNBILL_BEG_BAL_ID,";
        sqlCmd += "UNBILL_BEG_BAL_RI,";
        sqlCmd += "UNBILL_BEG_BAL_PN,";
        sqlCmd += "UNBILL_BEG_BAL_AO,";
        sqlCmd += "UNBILL_BEG_BAL_OT,";
        sqlCmd += "UNBILL_BEG_BAL_AI,";
        sqlCmd += "UNBILL_BEG_BAL_SF,";
        sqlCmd += "UNBILL_BEG_BAL_DP,";
        sqlCmd += "UNBILL_BEG_BAL_CB,";
        sqlCmd += "UNBILL_BEG_BAL_CI,";
        sqlCmd += "UNBILL_BEG_BAL_CC,";
        sqlCmd += "UNBILL_BEG_BAL_DB,";
        sqlCmd += "UNBILL_END_BAL_AF,";
        sqlCmd += "UNBILL_END_BAL_LF,";
        sqlCmd += "UNBILL_END_BAL_CF,";
        sqlCmd += "UNBILL_END_BAL_PF,";
        sqlCmd += "UNBILL_END_BAL_BL,";
        sqlCmd += "UNBILL_END_BAL_CA,";
        sqlCmd += "UNBILL_END_BAL_IT,";
        sqlCmd += "UNBILL_END_BAL_ID,";
        sqlCmd += "UNBILL_END_BAL_RI,";
        sqlCmd += "UNBILL_END_BAL_PN,";
        sqlCmd += "UNBILL_END_BAL_AO,";
        sqlCmd += "UNBILL_END_BAL_OT,";
        sqlCmd += "UNBILL_END_BAL_AI,";
        sqlCmd += "UNBILL_END_BAL_SF,";
        sqlCmd += "UNBILL_END_BAL_DP,";
        sqlCmd += "UNBILL_END_BAL_CB,";
        sqlCmd += "UNBILL_END_BAL_CI,";
        sqlCmd += "UNBILL_END_BAL_CC,";
        sqlCmd += "UNBILL_END_BAL_DB,";
        sqlCmd += "BILLED_BEG_BAL_AF,";
        sqlCmd += "BILLED_BEG_BAL_LF,";
        sqlCmd += "BILLED_BEG_BAL_CF,";
        sqlCmd += "BILLED_BEG_BAL_PF,";
        sqlCmd += "BILLED_BEG_BAL_BL,";
        sqlCmd += "BILLED_BEG_BAL_CA,";
        sqlCmd += "BILLED_BEG_BAL_IT,";
        sqlCmd += "BILLED_BEG_BAL_ID,";
        sqlCmd += "BILLED_BEG_BAL_RI,";
        sqlCmd += "BILLED_BEG_BAL_PN,";
        sqlCmd += "BILLED_BEG_BAL_AO,";
        sqlCmd += "BILLED_BEG_BAL_OT,";
        sqlCmd += "BILLED_BEG_BAL_AI,";
        sqlCmd += "BILLED_BEG_BAL_SF,";
        sqlCmd += "BILLED_BEG_BAL_DP,";
        sqlCmd += "BILLED_BEG_BAL_CB,";
        sqlCmd += "BILLED_BEG_BAL_CI,";
        sqlCmd += "BILLED_BEG_BAL_CC,";
        sqlCmd += "BILLED_BEG_BAL_DB,";
        sqlCmd += "BILLED_END_BAL_AF,";
        sqlCmd += "BILLED_END_BAL_LF,";
        sqlCmd += "BILLED_END_BAL_CF,";
        sqlCmd += "BILLED_END_BAL_PF,";
        sqlCmd += "BILLED_END_BAL_BL,";
        sqlCmd += "BILLED_END_BAL_CA,";
        sqlCmd += "BILLED_END_BAL_IT,";
        sqlCmd += "BILLED_END_BAL_ID,";
        sqlCmd += "BILLED_END_BAL_RI,";
        sqlCmd += "BILLED_END_BAL_PN,";
        sqlCmd += "BILLED_END_BAL_AO,";
        sqlCmd += "BILLED_END_BAL_OT,";
        sqlCmd += "BILLED_END_BAL_AI,";
        sqlCmd += "BILLED_END_BAL_SF,";
        sqlCmd += "BILLED_END_BAL_DP,";
        sqlCmd += "BILLED_END_BAL_CB,";
        sqlCmd += "BILLED_END_BAL_CI,";
        sqlCmd += "BILLED_END_BAL_CC,";
        sqlCmd += "BILLED_END_BAL_DB,";
        sqlCmd += "MIN_PAY,";
        sqlCmd += "MIN_PAY_BAL,";
        sqlCmd += "RC_MIN_PAY,";
        sqlCmd += "RC_MIN_PAY_BAL,";
        sqlCmd += "RC_MIN_PAY_M0,";
        sqlCmd += "AUTOPAY_BEG_AMT,";
        sqlCmd += "AUTOPAY_BAL,";
        sqlCmd += "PAY_BY_STAGE_AMT,";
        sqlCmd += "PAY_BY_STAGE_BAL,";
        sqlCmd += "PAY_BY_STAGE_DATE,";
        sqlCmd += "PAYMENT_STATUS,";
        sqlCmd += "LAST_PAYMENT_DATE,";
        sqlCmd += "LAST_MIN_PAY_DATE,";
        sqlCmd += "LAST_CANCEL_DEBT_DATE,";
        sqlCmd += "BILL_INTEREST,";
        sqlCmd += "TTL_AMT_BAL,";
        sqlCmd += "ADI_BEG_BAL,";
        sqlCmd += "ADI_END_BAL,";
        sqlCmd += "ADI_D_AVAIL,";
        sqlCmd += "DELAYPAY_OK_FLAG,";
        sqlCmd += "sysdate,";
        sqlCmd += "?,";
        sqlCmd += "WAIVE_TTL_BAL,";
        sqlCmd += "'Y'";
        sqlCmd += "  from act_acct_hst ";
        sqlCmd += " where p_seqno    = ? ";
        sqlCmd += "   and acct_month = ? ";
        setString(1, hWdayThisAcctMonth);
        setString(2, javaProgram);
        setString(3, hAcnoPSeqno);
        setString(4, hWdayLastAcctMonth);
        insertTable();

    }

    /***********************************************************************/
    void insertActCurrHst() throws Exception {
        daoTable = "act_curr_hst";
        sqlCmd = "insert into act_curr_hst (";
        sqlCmd += "p_seqno,";
        sqlCmd += "curr_code,";
        sqlCmd += "acct_month,";
        sqlCmd += "acct_type,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "unbill_beg_bal_af,";
        sqlCmd += "unbill_beg_bal_lf,";
        sqlCmd += "unbill_beg_bal_pf,";
        sqlCmd += "unbill_beg_bal_bl,";
        sqlCmd += "unbill_beg_bal_ca,";
        sqlCmd += "unbill_beg_bal_it,";
        sqlCmd += "unbill_beg_bal_id,";
        sqlCmd += "unbill_beg_bal_ri,";
        sqlCmd += "unbill_beg_bal_pn,";
        sqlCmd += "unbill_beg_bal_ao,";
        sqlCmd += "unbill_beg_bal_ot,";
        sqlCmd += "unbill_beg_bal_ai,";
        sqlCmd += "unbill_beg_bal_sf,";
        sqlCmd += "unbill_beg_bal_dp,";
        sqlCmd += "unbill_beg_bal_cb,";
        sqlCmd += "unbill_beg_bal_ci,";
        sqlCmd += "unbill_beg_bal_cc,";
        sqlCmd += "unbill_beg_bal_cf,";
        sqlCmd += "unbill_beg_bal_db,";
        sqlCmd += "unbill_end_bal_af,";
        sqlCmd += "unbill_end_bal_lf,";
        sqlCmd += "unbill_end_bal_pf,";
        sqlCmd += "unbill_end_bal_bl,";
        sqlCmd += "unbill_end_bal_ca,";
        sqlCmd += "unbill_end_bal_it,";
        sqlCmd += "unbill_end_bal_id,";
        sqlCmd += "unbill_end_bal_ri,";
        sqlCmd += "unbill_end_bal_pn,";
        sqlCmd += "unbill_end_bal_ao,";
        sqlCmd += "unbill_end_bal_ot,";
        sqlCmd += "unbill_end_bal_ai,";
        sqlCmd += "unbill_end_bal_sf,";
        sqlCmd += "unbill_end_bal_dp,";
        sqlCmd += "unbill_end_bal_cb,";
        sqlCmd += "unbill_end_bal_ci,";
        sqlCmd += "unbill_end_bal_cc,";
        sqlCmd += "unbill_end_bal_cf,";
        sqlCmd += "unbill_end_bal_db,";
        sqlCmd += "billed_beg_bal_af,";
        sqlCmd += "billed_beg_bal_lf,";
        sqlCmd += "billed_beg_bal_pf,";
        sqlCmd += "billed_beg_bal_bl,";
        sqlCmd += "billed_beg_bal_ca,";
        sqlCmd += "billed_beg_bal_it,";
        sqlCmd += "billed_beg_bal_id,";
        sqlCmd += "billed_beg_bal_ri,";
        sqlCmd += "billed_beg_bal_pn,";
        sqlCmd += "billed_beg_bal_ao,";
        sqlCmd += "billed_beg_bal_ot,";
        sqlCmd += "billed_beg_bal_ai,";
        sqlCmd += "billed_beg_bal_sf,";
        sqlCmd += "billed_beg_bal_dp,";
        sqlCmd += "billed_beg_bal_cb,";
        sqlCmd += "billed_beg_bal_ci,";
        sqlCmd += "billed_beg_bal_cc,";
        sqlCmd += "billed_beg_bal_cf,";
        sqlCmd += "billed_beg_bal_db,";
        sqlCmd += "billed_end_bal_af,";
        sqlCmd += "billed_end_bal_lf,";
        sqlCmd += "billed_end_bal_pf,";
        sqlCmd += "billed_end_bal_bl,";
        sqlCmd += "billed_end_bal_ca,";
        sqlCmd += "billed_end_bal_it,";
        sqlCmd += "billed_end_bal_id,";
        sqlCmd += "billed_end_bal_ri,";
        sqlCmd += "billed_end_bal_pn,";
        sqlCmd += "billed_end_bal_ao,";
        sqlCmd += "billed_end_bal_ot,";
        sqlCmd += "billed_end_bal_ai,";
        sqlCmd += "billed_end_bal_sf,";
        sqlCmd += "billed_end_bal_dp,";
        sqlCmd += "billed_end_bal_cb,";
        sqlCmd += "billed_end_bal_ci,";
        sqlCmd += "billed_end_bal_cc,";
        sqlCmd += "billed_end_bal_cf,";
        sqlCmd += "billed_end_bal_db,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "waive_ttl_bal,";
        sqlCmd += "acct_jrnl_bal,";
        sqlCmd += "stmt_auto_pay_bank,";
        sqlCmd += "stmt_auto_pay_no,";
        sqlCmd += "stmt_auto_pay_amt,";
        sqlCmd += "stmt_last_ttl,";
        sqlCmd += "stmt_payment_amt,";
        sqlCmd += "stmt_adjust_amt,";
        sqlCmd += "stmt_new_amt,";
        sqlCmd += "stmt_this_ttl_amt,";
        sqlCmd += "stmt_mp,";
        sqlCmd += "stmt_over_due_amt,";
        sqlCmd += "stmt_auto_pay_date,";
        sqlCmd += "bill_curr_code,";
        sqlCmd += "bill_sort_seq,";
        sqlCmd += "stmt_auto_dc_flag,";
        sqlCmd += "temp_unbill_interest,";
        sqlCmd += "delaypay_ok_flag,";
        sqlCmd += "ttl_amt_bal";
        ///////////// SELECT/////////////
        sqlCmd += ") select ";
        sqlCmd += "p_seqno,";
        sqlCmd += "curr_code,";
        sqlCmd += "?,";
        sqlCmd += "acct_type,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "unbill_beg_bal_af,";
        sqlCmd += "unbill_beg_bal_lf,";
        sqlCmd += "unbill_beg_bal_pf,";
        sqlCmd += "unbill_beg_bal_bl,";
        sqlCmd += "unbill_beg_bal_ca,";
        sqlCmd += "unbill_beg_bal_it,";
        sqlCmd += "unbill_beg_bal_id,";
        sqlCmd += "unbill_beg_bal_ri,";
        sqlCmd += "unbill_beg_bal_pn,";
        sqlCmd += "unbill_beg_bal_ao,";
        sqlCmd += "unbill_beg_bal_ot,";
        sqlCmd += "unbill_beg_bal_ai,";
        sqlCmd += "unbill_beg_bal_sf,";
        sqlCmd += "unbill_beg_bal_dp,";
        sqlCmd += "unbill_beg_bal_cb,";
        sqlCmd += "unbill_beg_bal_ci,";
        sqlCmd += "unbill_beg_bal_cc,";
        sqlCmd += "unbill_beg_bal_cf,";
        sqlCmd += "unbill_beg_bal_db,";
        sqlCmd += "unbill_end_bal_af,";
        sqlCmd += "unbill_end_bal_lf,";
        sqlCmd += "unbill_end_bal_pf,";
        sqlCmd += "unbill_end_bal_bl,";
        sqlCmd += "unbill_end_bal_ca,";
        sqlCmd += "unbill_end_bal_it,";
        sqlCmd += "unbill_end_bal_id,";
        sqlCmd += "unbill_end_bal_ri,";
        sqlCmd += "unbill_end_bal_pn,";
        sqlCmd += "unbill_end_bal_ao,";
        sqlCmd += "unbill_end_bal_ot,";
        sqlCmd += "unbill_end_bal_ai,";
        sqlCmd += "unbill_end_bal_sf,";
        sqlCmd += "unbill_end_bal_dp,";
        sqlCmd += "unbill_end_bal_cb,";
        sqlCmd += "unbill_end_bal_ci,";
        sqlCmd += "unbill_end_bal_cc,";
        sqlCmd += "unbill_end_bal_cf,";
        sqlCmd += "unbill_end_bal_db,";
        sqlCmd += "billed_beg_bal_af,";
        sqlCmd += "billed_beg_bal_lf,";
        sqlCmd += "billed_beg_bal_pf,";
        sqlCmd += "billed_beg_bal_bl,";
        sqlCmd += "billed_beg_bal_ca,";
        sqlCmd += "billed_beg_bal_it,";
        sqlCmd += "billed_beg_bal_id,";
        sqlCmd += "billed_beg_bal_ri,";
        sqlCmd += "billed_beg_bal_pn,";
        sqlCmd += "billed_beg_bal_ao,";
        sqlCmd += "billed_beg_bal_ot,";
        sqlCmd += "billed_beg_bal_ai,";
        sqlCmd += "billed_beg_bal_sf,";
        sqlCmd += "billed_beg_bal_dp,";
        sqlCmd += "billed_beg_bal_cb,";
        sqlCmd += "billed_beg_bal_ci,";
        sqlCmd += "billed_beg_bal_cc,";
        sqlCmd += "billed_beg_bal_cf,";
        sqlCmd += "billed_beg_bal_db,";
        sqlCmd += "billed_end_bal_af,";
        sqlCmd += "billed_end_bal_lf,";
        sqlCmd += "billed_end_bal_pf,";
        sqlCmd += "billed_end_bal_bl,";
        sqlCmd += "billed_end_bal_ca,";
        sqlCmd += "billed_end_bal_it,";
        sqlCmd += "billed_end_bal_id,";
        sqlCmd += "billed_end_bal_ri,";
        sqlCmd += "billed_end_bal_pn,";
        sqlCmd += "billed_end_bal_ao,";
        sqlCmd += "billed_end_bal_ot,";
        sqlCmd += "billed_end_bal_ai,";
        sqlCmd += "billed_end_bal_sf,";
        sqlCmd += "billed_end_bal_dp,";
        sqlCmd += "billed_end_bal_cb,";
        sqlCmd += "billed_end_bal_ci,";
        sqlCmd += "billed_end_bal_cc,";
        sqlCmd += "billed_end_bal_cf,";
        sqlCmd += "billed_end_bal_db,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "waive_ttl_bal,";
        sqlCmd += "acct_jrnl_bal,";
        sqlCmd += "stmt_auto_pay_bank,";
        sqlCmd += "stmt_auto_pay_no,";
        sqlCmd += "stmt_auto_pay_amt,";
        sqlCmd += "stmt_last_ttl,";
        sqlCmd += "stmt_payment_amt,";
        sqlCmd += "stmt_adjust_amt,";
        sqlCmd += "stmt_new_amt,";
        sqlCmd += "stmt_this_ttl_amt,";
        sqlCmd += "stmt_mp,";
        sqlCmd += "stmt_over_due_amt,";
        sqlCmd += "stmt_auto_pay_date,";
        sqlCmd += "bill_curr_code,";
        sqlCmd += "bill_sort_seq,";
        sqlCmd += "stmt_auto_dc_flag,";
        sqlCmd += "temp_unbill_interest,";
        sqlCmd += "delaypay_ok_flag,";
        sqlCmd += "ttl_amt_bal ";
        sqlCmd += "  from act_curr_hst ";
        sqlCmd += " where p_seqno = ? ";
        sqlCmd += "   and acct_month = ? ";
        setString(1, hWdayThisAcctMonth);
        setString(2, hAcnoPSeqno);
        setString(3, hWdayLastAcctMonth);
        int retCode = insertTable();

        if (retCode == 0) {
            showLogMessage("I", "",
            String.format("insert_act_curr_hst failed --> h_acno_p_seqno= [%s], h_wday_this_acct_month= [%s], h_wday_last_acct_month= [%s]", 
            hAcnoPSeqno, hWdayThisAcctMonth, hWdayLastAcctMonth));
        }

    }

    /***********************************************************************/
    void insertActAnalSub() throws Exception {
        daoTable = "act_anal_sub";
        sqlCmd = "insert into act_anal_sub (";
        sqlCmd += "P_SEQNO,";
        sqlCmd += "ACCT_MONTH,";
        sqlCmd += "ACCT_TYPE,";
        sqlCmd += "HIS_PURCHASE_CNT,";
        sqlCmd += "HIS_PURCHASE_AMT,";
        sqlCmd += "HIS_PUR_NO_M2,";
        sqlCmd += "HIS_CASH_CNT,";
        sqlCmd += "HIS_CASH_AMT,";
        sqlCmd += "HIS_PAY_PERCENTAGE,";
        sqlCmd += "HIS_RC_PERCENTAGE,";
        sqlCmd += "HIS_PAY_AMT,";
        sqlCmd += "HIS_PAY_CNT,";
        sqlCmd += "HIS_ADJ_DR_AMT,";
        sqlCmd += "HIS_ADJ_CR_AMT,";
        sqlCmd += "HIS_ADJ_DR_CNT,";
        sqlCmd += "HIS_ADJ_CR_CNT,";
        sqlCmd += "MOD_TIME,";
        sqlCmd += "MOD_PGM,";
        sqlCmd += "HIS_COMBO_CASH_AMT,";
        sqlCmd += "HIS_COMBO_CASH_FEE";
        ///////////// SELECT/////////////
        sqlCmd += ") select ";
        sqlCmd += "P_SEQNO,";
        sqlCmd += "?,";
        sqlCmd += "ACCT_TYPE,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "sysdate,";
        sqlCmd += "?,";
        sqlCmd += "0,";
        sqlCmd += "0 ";
        sqlCmd += "  from act_anal_sub ";
        sqlCmd += " where p_seqno = ? ";
        sqlCmd += "   and acct_month = ? ";
        setString(1, hWdayThisAcctMonth);
        setString(2, javaProgram);
        setString(3, hAcnoPSeqno);
        setString(4, hWdayLastAcctMonth);
        int retCode = insertTable();

        if (retCode == 0) {
            showLogMessage("I", "",
            String.format("insert_act_anal_sub failed --> h_acno_p_seqno= [%s], h_wday_this_acct_month= [%s], h_wday_last_acct_month= [%s]", 
            hAcnoPSeqno, hWdayThisAcctMonth, hWdayLastAcctMonth));
        }

    }

    /***********************************************************************/
    void selectActAcag() throws Exception {
        String tAcagAcctMonth = "";
        String tAcagRowid = "";
        double tAcagPayAmt = 0;

        sqlCmd = "select acct_month,";
        sqlCmd += " to_char(add_months(to_date(acct_month,'yyyymm'),1),'yyyymm') h_temp_acct_month,";
        sqlCmd += " pay_amt,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acag ";
        sqlCmd += " where p_seqno =  ? ";
        sqlCmd += " order by acct_month ";
        setString(1, hAcnoPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcagAcctMonth = getValue("acct_month", i);
            hTempAcctMonth = getValue("h_temp_acct_month", i);
            hAcagPayAmt = getValueDouble("pay_amt", i);
            hAcagRowid = getValue("rowid", i);

            if (i > 0) {
                tAcagAcctMonth = hTempAcctMonth;
                if (tAcagAcctMonth.equals(hAcagAcctMonth)) {
                    tAcagPayAmt = tAcagPayAmt + tAcagPayAmt;
                    updateActAcag1();
                    hAcagRowid = tAcagRowid;
                    deleteActAcag();
                } else {
                    tAcagRowid = hAcagRowid;
                    tAcagPayAmt = hAcagPayAmt;
                    updateActAcag1();
                }
            }

        }
    }

    /***********************************************************************/
    void deleteActAcag() throws Exception {
        daoTable = "act_acag";
        whereStr = "where rowid = ? ";
        setRowId(1, hAcagRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_act_acag not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcag1() throws Exception {
        daoTable = "act_acag";
        updateSQL = "acct_month  =  ? ,";
        updateSQL += " pay_amt    =  ? ,";
        updateSQL += " mod_pgm    = 'ActC001',";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where rowid =  ? ";
        setString(1, hAcagAcctMonth);
        setDouble(2, hAcagPayAmt);
        setRowId(3, hAcagRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acag not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        sqlCmd = "select curr_code ";
        sqlCmd += "  from act_acct_curr  ";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and bill_sort_seq != '' ";
        sqlCmd += " order by bill_sort_seq ";
        setString(1, hAcnoPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcurCurrCode = getValue("curr_code");
        }

        actAcctCurrCnt = recordCnt;

    }

    /***********************************************************************/
    void selectActAcagCurr() throws Exception {
        String tAcagAcctMonth = "";
        String tAcagRowid = "";
        double tAcagPayAmt = 0;

        sqlCmd = "select acct_month,";
        sqlCmd += " to_char(add_months(to_date(acct_month,'yyyymm'),1),'yyyymm') h_temp_acct_month,";
        sqlCmd += " pay_amt,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acag_curr ";
        sqlCmd += " where p_seqno   = ? ";
        sqlCmd += "   and curr_code = ? ";
        sqlCmd += " order by acct_month ";
        setString(1, hAcnoPSeqno);
        setString(2, hAcurCurrCode);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcagAcctMonth = getValue("acct_month", i);
            hTempAcctMonth = getValue("h_temp_acct_month", i);
            hAcagPayAmt = getValueDouble("pay_amt", i);
            hAcagRowid = getValue("rowid", i);

            if (i > 0) {
                if (hTempAcctMonth.equals(tAcagAcctMonth)) {
                    tAcagRowid = hAcagRowid;
                    tAcagPayAmt = tAcagPayAmt + hAcagPayAmt;
                    updateActAcagCurr1();
                    hAcagRowid = tAcagRowid;
                    deleteActAcagCurr();
                } else {
                    tAcagRowid = hAcagRowid;
                    tAcagPayAmt = hAcagPayAmt;
                    updateActAcagCurr1();
                }
            } else {
                tAcagAcctMonth = hTempAcctMonth;

            }
        }
    }

    /***********************************************************************/
    void deleteActAcagCurr() throws Exception {
        daoTable = "act_acag_curr";
        whereStr = "where rowid = ? ";
        setRowId(1, hAcagRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_act_acag_curr not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcagCurr1() throws Exception {
        daoTable = "act_acag_curr";
        updateSQL = "acct_month  =  ? ,";
        updateSQL += " pay_amt    =  ? ,";
        updateSQL += " mod_pgm    = 'ActC001',";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where rowid =  ? ";
        setString(1, hAcagAcctMonth);
        setDouble(2, hAcagPayAmt);
        setRowId(3, hAcagRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acag_curr not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
        daoTable = "act_acno";
        updateSQL = "stmt_cycle           = ? ,";
        updateSQL += "new_cycle_month      = ? ,";
        updateSQL += "last_interest_date   = ?";
        whereStr = "where decode(acct_type,'','x',acct_type) = ?  ";
        whereStr += "  and card_indicator = ? ";
        whereStr += "  and stmt_cycle     = ? ";
        whereStr += "  and id_p_seqno     = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoNewCycleMonth);
        setString(3, hAcceLastInterestDate);
        setString(4, hAcnoAcctType);
        setString(5, hAcceCardIndicator);
        setString(6, hWdayStmtCycle);
        setString(7, hAcceIdPSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcno1() throws Exception {
        daoTable = "act_acno";
        updateSQL = "stmt_cycle         = ?,";
        updateSQL += "new_cycle_month    = ?,";
        updateSQL += "last_interest_date = ? ";
        whereStr = "where decode(acct_type,'','x',acct_type) = ?  ";
        whereStr += "and card_indicator = ? ";
        whereStr += "and stmt_cycle     = ? ";
        whereStr += "and corp_p_seqno   = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoNewCycleMonth);
        setString(3, hAcceLastInterestDate);
        setString(4, hAcnoAcctType);
        setString(5, hAcceCardIndicator);
        setString(6, hWdayStmtCycle);
        setString(7, hAcceCorpPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateActAcct() throws Exception {
        daoTable = "act_acct";
        updateSQL = "stmt_cycle    = ?";
        whereStr = "where p_seqno = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateCrdCard() throws Exception {
        daoTable = "crd_card";
        updateSQL = "stmt_cycle  = ?";
        whereStr = "where p_seqno = ? "; // acct_p_seqno
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void selectEcsActAcno2() throws Exception {

        sqlCmd = "select acct_type,";
        sqlCmd += " acct_key,";
        sqlCmd += " acno_p_seqno,";
        sqlCmd += " p_seqno,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from ecs_act_acno ";
        sqlCmd += " where card_indicator = ? ";
        sqlCmd += "   and acno_p_seqno   = p_seqno ";
        sqlCmd += "   and stmt_cycle     = ? ";
        sqlCmd += "   and corp_p_seqno   = ? ";
        setString(1, hAcceCardIndicator);
        setString(2, hWdayStmtCycle);
        setString(3, hAcceCorpPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoAcctType = getValue("acct_type", i);
            hAcnoAcctKey = getValue("acct_key", i);
            hAcnoPSeqno = getValue("acno_p_seqno", i);
            hAcnoAcctPSeqno = getValue("p_seqno", i);
            hAcnoRowid = getValue("rowid", i);

            if (hAcceCardIndicator.equals("1"))
                updateEcsActAcno();
            if (hAcceCardIndicator.equals("2"))
                updateEcsActAcno1();
            updateActAcag();
            updateActAcagCurr();
            updateEcsActAcct();
            updateActChkno();
            updateActMod1();
            updateActMod2();
            updateActDebt();
            updateBilBill();
            updateBilCurpost();
            updateEcsCrdCard();
            updateCrdIdno();
            updateCycPyaj();
            updateCycAfee();

        }

    }

    /***********************************************************************/
    void updateEcsActAcno() throws Exception {
        daoTable = "ecs_act_acno";
        updateSQL = "stmt_cycle          = ?,";
        updateSQL += " new_cycle_month    = ?,";
        updateSQL += " last_interest_date = ?";
        whereStr = "where decode(acct_type,'','x',acct_type) = ?  ";
        whereStr += "and card_indicator  = ? ";
        whereStr += "and stmt_cycle      = ? ";
        whereStr += "and id_p_seqno      = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoNewCycleMonth);
        setString(3, hAcceLastInterestDate);
        setString(4, hAcnoAcctType);
        setString(5, hAcceCardIndicator);
        setString(6, hWdayStmtCycle);
        setString(7, hAcceIdPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateEcsActAcno1() throws Exception {
        daoTable = "ecs_act_acno";
        updateSQL = "stmt_cycle          = ?,";
        updateSQL += " new_cycle_month    = ?,";
        updateSQL += " last_interest_date = ? ";
        whereStr = "where decode(acct_type,'','x',acct_type) = ?  ";
        whereStr += "and card_indicator  = ? ";
        whereStr += "and stmt_cycle      = ? ";
        whereStr += "and corp_p_seqno    = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoNewCycleMonth);
        setString(3, hAcceLastInterestDate);
        setString(4, hAcnoAcctType);
        setString(5, hAcceCardIndicator);
        setString(6, hWdayStmtCycle);
        setString(7, hAcceCorpPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateActAcag() throws Exception {
        daoTable = "act_acag";
        updateSQL = "stmt_cycle    = ? ";
        whereStr = "where p_seqno = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateActAcagCurr() throws Exception {
        daoTable = "act_acag_curr";
        updateSQL = "stmt_cycle    = ?";
        whereStr = "where p_seqno = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateEcsActAcct() throws Exception {
        daoTable = "ecs_act_acct";
        updateSQL = "stmt_cycle    = ?";
        whereStr = "where p_seqno = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();
    }

    /***********************************************************************/
    void updateActChkno() throws Exception {
        daoTable = "act_chkno";
        updateSQL = "stmt_cycle    = ?";
        whereStr = "where p_seqno = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateActMod1() throws Exception {
        daoTable = "act_mod1 a";
        updateSQL = "stmt_cycle    = ?";
        whereStr = "where p_seqno = ?  ";
        whereStr += "and reference_no in (select b.reference_no " + "  from bil_bill b "
                + " where b.reference_no = a.reference_no  ";
        whereStr += "and decode(billed_flag,'','N',billed_flag) !='B') ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateActMod2() throws Exception {
        daoTable = "act_mod2 a";
        updateSQL = "stmt_cycle    = ?";
        whereStr = "where p_seqno = ?  ";
        whereStr += "and reference_no in (select b.reference_no " + "  from bil_bill b "
                + " where b.reference_no = a.reference_no  ";
        whereStr += "and decode(billed_flag,'','N',billed_flag) !='B') ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateActDebt() throws Exception {
        daoTable = "act_debt a";
        updateSQL = "stmt_cycle    = ?,";
        updateSQL += " acct_month   = ? ";
        whereStr = "where p_seqno = ? ";
        whereStr += "and decode(reference_no, '','x',reference_no) in (select reference_no " // decode(reference_seq)
                + "  from bil_bill " + " where reference_no = a.reference_no "; // =
                                                                                // a.reference_seq
        whereStr += " and decode(billed_flag,'','N',billed_flag) !='B') ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoNewCycleMonth);
        setString(3, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateBilBill() throws Exception {
        daoTable = "bil_bill";
        updateSQL = "stmt_cycle    = ?,";
        updateSQL += "acct_month    = ?,";
        updateSQL += "mod_time      = sysdate,";
        updateSQL += "mod_pgm       = ? ";
        whereStr = "where p_seqno = ?  ";
        whereStr += "  and ( decode(billed_flag,'','N',billed_flag) !='B' )  ";
        whereStr += "  and decode(rsk_type, '','x',rsk_type) not in ('1','2','3') ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoNewCycleMonth);
        setString(3, javaProgram);
        setString(4, hAcnoPSeqno);
        updateTable();

        daoTable = "bil_bill";
        updateSQL = "stmt_cycle = ?,";
        updateSQL += "mod_time   = sysdate,";
        updateSQL += "mod_pgm    = 'ActC001'";
        whereStr = "where p_seqno = ?  ";
        whereStr += "  and (   decode(billed_flag, '','N',billed_flag) != 'B' "
                + "or decode(rsk_type   , '','x',rsk_type)    in ('1','2','3','4') "
                + "or rsk_post                                != '') ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateBilCurpost() throws Exception {
        daoTable = "bil_curpost";
        updateSQL = "stmt_cycle    = ?";
        whereStr = "where p_seqno = ? ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateEcsCrdCard() throws Exception {
        daoTable = "ecs_crd_card";
        updateSQL = "stmt_cycle  = ?";
        whereStr = "where p_seqno = ? "; // acct_p_seqno
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateCrdIdno() throws Exception {
        daoTable = "crd_idno";
        updateSQL = "fst_stmt_cycle   = ? ";
        whereStr = "where id_p_seqno = ? ";
        whereStr += "  and ?          = '1' ";
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcceIdPSeqno);
        setString(3, hAcceCardIndicator);
        updateTable();

    }

    /***********************************************************************/
    void updateCycPyaj() throws Exception {
        daoTable = "cyc_pyaj";
        updateSQL = "stmt_cycle    = ? ";
        whereStr = "where p_seqno = ? "; // p_seq
        whereStr += "and decode(settle_flag,'',' ',settle_flag) = 'U' "; // settlement_flag
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateCycAfee() throws Exception {
        daoTable = "cyc_afee";
        updateSQL = "stmt_cycle    = ? ";
        whereStr = "where p_seqno = ? "; // acct_p_seqno
        setString(1, hAcceNewStmtCycle);
        setString(2, hAcnoPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateActChgCycle() throws Exception {
        daoTable = "act_chg_cycle";
        updateSQL = "batch_proc_date    =  ? ,";
        updateSQL += "last_interest_date =  ? ,";
        updateSQL += "last_cycle_month   = substr(?,1,6),";
        updateSQL += "batch_proc_mark    = 'Y',";
        updateSQL += "new_cycle_month    =  ? ,";
        updateSQL += "mod_time           = sysdate,";
        updateSQL += "mod_pgm            = ? ";
        whereStr = "where rowid        =  ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hAcceLastInterestDate);
        setString(3, hAcceLastInterestDate);
        setString(4, hAcnoNewCycleMonth);
        setString(5, javaProgram);
        setRowId(6, hAcceRowid);
        updateTable();

    }

    /***********************************************************************/
    void updateActChgCycle1() throws Exception {
        daoTable = "act_chg_cycle";
        updateSQL = "batch_proc_date  = ?,";
        updateSQL += "batch_proc_mark  = 'Y',";
        updateSQL += "apr_user         = 'system',"; // apr_id
        updateSQL += "apr_date         = to_char(sysdate,'yyyymmdd'),";
        updateSQL += "mod_time         = sysdate,";
        updateSQL += "mod_pgm          = ? ";
        whereStr = "where stmt_cycle = ?  ";
        whereStr += "and id_p_seqno      = ?  ";
        whereStr += "and decode(batch_proc_mark,'','N',batch_proc_mark) = 'N' ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setString(3, hWdayStmtCycle);
        setString(4, hAcceIdPSeqno);
        updateTable();

    }

    /***************************************************************************/
    void updatePaymentRate() throws Exception {
        showLogMessage("I", "", String.format("keep payment_rate1 [%s]", hAcnoAcctKey));
        daoTable = "act_acno";
        updateSQL = "  payment_rate2    = payment_rate1,";
        updateSQL += "  payment_rate3    = payment_rate2,";
        updateSQL += "  payment_rate4    = payment_rate3,";
        updateSQL += "  payment_rate5    = payment_rate4,";
        updateSQL += "  payment_rate6    = payment_rate5,";
        updateSQL += "  payment_rate7    = payment_rate6,";
        updateSQL += "  payment_rate8    = payment_rate7,";
        updateSQL += "  payment_rate9    = payment_rate8,";
        updateSQL += "  payment_rate10   = payment_rate9,";
        updateSQL += "  payment_rate11   = payment_rate10,";
        updateSQL += "  payment_rate12   = payment_rate11,";
        updateSQL += "  payment_rate13   = payment_rate12,";
        updateSQL += "  payment_rate14   = payment_rate13,";
        updateSQL += "  payment_rate15   = payment_rate14,";
        updateSQL += "  payment_rate16   = payment_rate15,";
        updateSQL += "  payment_rate17   = payment_rate16,";
        updateSQL += "  payment_rate18   = payment_rate17,";
        updateSQL += "  payment_rate19   = payment_rate18,";
        updateSQL += "  payment_rate20   = payment_rate19,";
        updateSQL += "  payment_rate21   = payment_rate20,";
        updateSQL += "  payment_rate22   = payment_rate21,";
        updateSQL += "  payment_rate23   = payment_rate22,";
        updateSQL += "  payment_rate24   = payment_rate23,";
        updateSQL += "  payment_rate25   = payment_rate24,";
        updateSQL += "  mod_pgm          = 'ActC001',";
        updateSQL += "  mod_time         = sysdate";
        whereStr = " WHERE  rowid            = ?  ";
        setRowId(1, hAcnoRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_payment_rate error!!", "", hCallBatchSeqno);
        }
    }
    

    /***********************************************************************/
    void updatePtrSysParm() throws Exception {
        daoTable = "ptr_sys_parm";
        updateSQL = " WF_VALUE = ? ";
        whereStr = " where WF_PARM = 'CRD_BATCH' and WF_KEY = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, "ActC001");
        updateTable();

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActC001 proc = new ActC001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
