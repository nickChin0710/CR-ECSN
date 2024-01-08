/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  107/02/23  V1.00.01    Brian     error correction                          *
 *  109/11/16  V1.00.02    shiyuqi   updated for project coding standard       *
 *  111/12/07  V1.00.03    Simon     累計專款專用餘額                          *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*產生科目餘額統計檔程式*/
public class ActE006 extends AccessDAO {

    public static final boolean debugMode = false;

    private String progname = "產生科目餘額統計檔程式   111/12/07  V1.00.03 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActE006";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hAcsuCardSpecBal = "";
    String hDebtPSeqno = "";
    String hDebtAcctCode = "";
    String hDebtCurrCode = "";

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
                comc.errExit("Usage : ActE006", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            deleteActAcctSum();
            deleteActCurrSum();

            selectPtrBusinday();
            if (args.length == 1)
                hBusiBusinessDate = args[0];

            insertActAcctSum2();
            showLogMessage("I", "", String.format("一般資料處理完畢 !"));
            insertActCurrSum2();
            showLogMessage("I", "", String.format("幣別-一般資料處理完畢 !"));
            selectActDebt3();
            showLogMessage("I", "", String.format("卡優待戶資料處理完畢 !"));
            selectActDebt4();
            showLogMessage("I", "", String.format("幣別-一卡優待戶資料處理完畢 !"));

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
    void deleteActAcctSum() throws Exception {
        daoTable = "act_acct_sum";
        deleteTable();
    }

    /***********************************************************************/
    void deleteActCurrSum() throws Exception {
        daoTable = "act_curr_sum";
        deleteTable();
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void insertActAcctSum2() throws Exception {
        sqlCmd = "insert into act_acct_sum ";
        sqlCmd += "(p_seqno,";
        sqlCmd += " acct_code,";
        sqlCmd += " acct_type,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " unbill_end_bal,";
        sqlCmd += " unbill_end_bal_m2,";
        sqlCmd += " billed_end_bal,";
        sqlCmd += " billed_end_bal_m2,";
        sqlCmd += " end_bal_db_b,";
        sqlCmd += " end_bal_db_c,";
        sqlCmd += " end_bal_db_i,";
        sqlCmd += " end_bal_spec,";
        sqlCmd += " mod_time,";
        sqlCmd += " mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += " p_seqno,";
        sqlCmd += " acct_code,";
        sqlCmd += " max(acct_type),";
        sqlCmd += " max(a.stmt_cycle),";
        sqlCmd += " sum(decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,end_bal,0)),";
        sqlCmd += " sum(decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,";
        sqlCmd += "            decode(a.acct_code,'ID',";
        sqlCmd += "                   decode(substr(bill_type,2,1),'2',0,end_bal), 0), 0)),";
        sqlCmd += " sum(decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,0,end_bal)),";
        sqlCmd += " sum(decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,0,";
        sqlCmd += "            decode(a.acct_code,'ID',";
        sqlCmd += "                   decode(substr(bill_type,2,1),'2',";
        sqlCmd += "                          decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),0,";
        sqlCmd += "                                 decode(sign( ? -b.this_delaypay_date),-1,0, end_bal), 0), 0), 0))+";
        sqlCmd += " decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,0,";
        sqlCmd += "        decode(a.acct_code,'ID',";
        sqlCmd += "               decode(substr(bill_type,2,1),'2',";
        sqlCmd += "                      decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),0,0,end_bal),";
        sqlCmd += "                      end_bal),0))),";
        sqlCmd += " sum(decode(a.acct_code,'DB',decode(a.acct_code_type,'B',a.end_bal,0),0)),";
        sqlCmd += " sum(decode(a.acct_code,'DB',decode(a.acct_code_type,'C',a.end_bal,0),0)),";
        sqlCmd += " sum(decode(a.acct_code,'DB',decode(a.acct_code_type,'I',a.end_bal,0),0)),";
        sqlCmd += " sum(decode(a.spec_flag,'Y',a.end_bal,0)),";
        sqlCmd += " min(sysdate),";
        sqlCmd += " 'ActE006' ";
        sqlCmd += "  from act_debt a,ptr_workday b where end_bal > 0 and a.stmt_cycle = b.stmt_cycle GROUP BY p_seqno,acct_code ";
        setString(1, hBusiBusinessDate);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActCurrSum2() throws Exception {
        sqlCmd = "insert into act_curr_sum ";
        sqlCmd += "(p_seqno,";
        sqlCmd += " curr_code,";
        sqlCmd += " acct_code,";
        sqlCmd += " acct_type,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " unbill_end_bal,";
        sqlCmd += " unbill_end_bal_m2,";
        sqlCmd += " billed_end_bal,";
        sqlCmd += " billed_end_bal_m2,";
        sqlCmd += " end_bal_db_b,";
        sqlCmd += " end_bal_db_c,";
        sqlCmd += " end_bal_db_i,";
        sqlCmd += " end_bal_spec,";
        sqlCmd += " mod_time,";
        sqlCmd += " mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += " p_seqno,";
        sqlCmd += " curr_code,";
        sqlCmd += " acct_code,";
        sqlCmd += " max(acct_type),";
        sqlCmd += " max(a.stmt_cycle),";
        sqlCmd += " sum(decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,dc_end_bal,0)),";
        sqlCmd += " sum(decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,";
        sqlCmd += "            decode(a.acct_code,'ID',";
        sqlCmd += "                   decode(substr(bill_type,2,1),'2',0,dc_end_bal), 0), 0)),";
        sqlCmd += " sum(decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,0,dc_end_bal)),";
        sqlCmd += " sum(decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,0,";
        sqlCmd += "            decode(a.acct_code,'ID',";
        sqlCmd += "                   decode(substr(bill_type,2,1),'2',";
        sqlCmd += "                          decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),0,";
        sqlCmd += "                                 decode(sign( ? -b.this_delaypay_date),-1,0, dc_end_bal), 0), 0), 0))+";
        sqlCmd += " decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),1,0,";
        sqlCmd += "        decode(a.acct_code,'ID',";
        sqlCmd += "               decode(substr(bill_type,2,1),'2',";
        sqlCmd += "                      decode(sign(decode(a.acct_month, '','200407',a.acct_month)-b.this_acct_month),0,0,dc_end_bal),";
        sqlCmd += "                      dc_end_bal),0))),";
        sqlCmd += " sum(decode(a.acct_code,'DB',decode(a.acct_code_type,'B',a.end_bal,0),0)),";
        sqlCmd += " sum(decode(a.acct_code,'DB',decode(a.acct_code_type,'C',a.end_bal,0),0)),";
        sqlCmd += " sum(decode(a.acct_code,'DB',decode(a.acct_code_type,'I',a.end_bal,0),0)),";
        sqlCmd += " sum(decode(a.spec_flag,'Y',a.dc_end_bal,0)),";
        sqlCmd += " min(sysdate),";
        sqlCmd += " 'ActE006' ";
        sqlCmd += "  from act_debt a,ptr_workday b where dc_end_bal > 0 and a.stmt_cycle = b.stmt_cycle GROUP BY p_seqno,curr_code,acct_code ";
        setString(1, hBusiBusinessDate);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectActDebt3() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " p_seqno,";
        sqlCmd += " acct_code,";
        sqlCmd += " sum(end_bal) h_acsu_card_spec_bal ";
        sqlCmd += "  from act_debt a, bil_merchant c ";
        sqlCmd += " where a.mcht_no = c.mcht_no ";
        sqlCmd += "   and end_bal > 0 ";
        sqlCmd += "   and c.borrow_flag = 'Y' ";
        sqlCmd += " group by p_seqno,acct_code ";
        sqlCmd += " having sum(end_bal) > 0 ";
        if (debugMode)
            sqlCmd += " fetch first 10 rows only ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hDebtPSeqno = getValue("p_seqno");
            hDebtAcctCode = getValue("acct_code");
            hAcsuCardSpecBal = getValue("h_acsu_card_spec_bal");

            updateActAcctSum3();
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void updateActAcctSum3() throws Exception {
        daoTable = "act_acct_sum";
        updateSQL = "card_spec_bal = ?";
        whereStr = "where p_seqno = ?  ";
        whereStr += "and acct_code = ? ";
        setString(1, hAcsuCardSpecBal);
        setString(2, hDebtPSeqno);
        setString(3, hDebtAcctCode);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_sum not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectActDebt4() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " p_seqno,";
        sqlCmd += " curr_code,";
        sqlCmd += " acct_code,";
        sqlCmd += " sum(dc_end_bal) h_acsu_card_spec_bal ";
        sqlCmd += "  from act_debt a,bil_merchant c ";
        sqlCmd += " where a.mcht_no = c.mcht_no ";
        sqlCmd += "   and dc_end_bal > 0 ";
        sqlCmd += "   and c.borrow_flag = 'Y' ";
        sqlCmd += " group by p_seqno,curr_code,acct_code ";
        sqlCmd += " having sum(dc_end_bal) > 0 ";
        if (debugMode)
            sqlCmd += " fetch first 10 rows only ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hDebtPSeqno = getValue("p_seqno");
            hDebtCurrCode = getValue("curr_code");
            hDebtAcctCode = getValue("acct_code");
            hAcsuCardSpecBal = getValue("h_acsu_card_spec_bal");

            updateActCurrSum3();
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void updateActCurrSum3() throws Exception {
        daoTable = "act_curr_sum";
        updateSQL = "card_spec_bal = ?";
        whereStr = "where p_seqno = ?  ";
        whereStr += "and curr_code = ?  ";
        whereStr += "and acct_code = ? ";
        setString(1, hAcsuCardSpecBal);
        setString(2, hDebtPSeqno);
        setString(3, hDebtCurrCode);
        setString(4, hDebtAcctCode);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_curr_sum not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActE006 proc = new ActE006();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
