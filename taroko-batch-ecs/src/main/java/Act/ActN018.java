/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/27  V1.00.01    Brian     error correction                          *
 *  109/11/19  V1.00.02    shiyuqi   updated for project coding standard       *  
 *  112/05/02  V1.00.03    Simon     1.國外預借現金額度、循環信用年利率 取自 act_jcic_cmp*
 *                                   2.add fetch's daoTable                    *
 *  112/09/15  V1.00.04    Simon     新增帶入 cca_card_acct 臨調額度           *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*JCICKK4線上啟動資料異動處理程式*/
public class ActN018 extends AccessDAO {

    public static  boolean debugMode = false;

    private String progname = "JCICKK4線上啟動資料異動處理程式  112/09/15  V1.00.04  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActN018";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    int recordCnt = 0;

    String hBusiBusinessDate = "";
    String hAjcpSubLogType = "";
    String hAjcpPSeqno = "";
    String hAjcpStmtCycle = "";
    String hAjcpAcctMonth = "";
    String hAjtnPaymentNum = "";
    String hAjcpReportReason = "";
    String hAjcpRowid = "";
    int hCnt = 0;
    String hInt1 = "";
    String hInt2 = "";
    String hWdayThisCloseDate = "";
    int totcnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            exceptExit = 1;
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : ActN018 p_seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            selectPtrBusinday();

            selectActJcicCmp();

            showLogMessage("I", "", String.format("累計筆數 : [%d]", totcnt));
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
    void selectActJcicCmp() throws Exception {

        daoTable = "act_jcic_cmp a,ptr_workday b";
        sqlCmd = "select ";
        sqlCmd += " a.sub_log_type,";
        sqlCmd += " a.p_seqno,";
        sqlCmd += " a.stmt_cycle,";
        sqlCmd += " a.acct_month,";
        sqlCmd += " months_between(to_date(b.this_acct_month,'yyyymm'),to_date(a.acct_month,'yyyymm'))+70 h_ajtn_payment_num,";
        sqlCmd += " a.report_reason,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "from act_jcic_cmp a,ptr_workday b ";
        sqlCmd += "where a.stmt_cycle = b.stmt_cycle ";
        sqlCmd += "  and a.apr_flag   = 'Y' ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAjcpSubLogType = getValue("sub_log_type");
            hAjcpPSeqno = getValue("p_seqno");
            hAjcpStmtCycle = getValue("stmt_cycle");
            hAjcpAcctMonth = getValue("acct_month");
            hAjtnPaymentNum = getValue("h_ajtn_payment_num");
            hAjcpReportReason = getValue("report_reason");
            hAjcpRowid = getValue("rowid");

            if (hAjcpSubLogType.equals("A")) {
                deleteActJcicLog1();
                deleteActJcicLog2();
            }

          //if (h_ajcp_sub_log_type.equals("C"))
            if (hAjcpSubLogType.equals("C") || hAjcpSubLogType.equals("D"))
                deleteActJcicLog2();

            if (hAjcpSubLogType.equals("A")) {
                insertActJcicLog(0, 0);
                selectPtrWorkday();
                if (!hBusiBusinessDate.equals(hWdayThisCloseDate))
                    insertActJcicLog(1, 1);
            }

            if (hAjcpSubLogType.equals("C")) {
                if (selectActJcicLog() != 0)
                    insertActJcicLog(0, 0);
                insertActJcicLog(1, 0);
            }
            
            /* added "D" process on 2019/08/20  */
            if (hAjcpSubLogType.equals("D")) {
                if (selectActJcicLog() != 0)
                    insertActJcicLog(0, 0);
                insertActJcicLog(1, 2);
            }
            
            insertActJcicTxn();
            deleteActJcicCmp();
            totcnt++;
            if (totcnt % 10000 == 0)
                showLogMessage("I", "", String.format("處理筆數 : [%d]", totcnt));
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void deleteActJcicLog1() throws Exception {
        daoTable = "act_jcic_log";
        whereStr = "where acct_month   = ?  ";
        whereStr += " and p_seqno      = ?  ";
        whereStr += " and stmt_cycle   = ?  ";
        whereStr += " and log_type     = 'A' ";
        setString(1, hAjcpAcctMonth);
        setString(2, hAjcpPSeqno);
        setString(3, hAjcpStmtCycle);
        deleteTable();

    }

    /***********************************************************************/
    void deleteActJcicLog2() throws Exception {
        daoTable = "act_jcic_log";
        whereStr = "where acct_month   = ?  ";
        whereStr += " and p_seqno      = ?  ";
        whereStr += " and stmt_cycle   = ?  ";
        whereStr += " and decode(proc_flag,'','N',proc_flag) != 'Y'  ";
        whereStr += " and log_type     = 'C' ";
        setString(1, hAjcpAcctMonth);
        setString(2, hAjcpPSeqno);
        setString(3, hAjcpStmtCycle);
        deleteTable();

    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        hWdayThisCloseDate = "";

        sqlCmd = "select to_char(to_date(this_close_date,'yyyymmdd')+2 days,'yyyymmdd') h_wday_this_close_date ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where stmt_cycle = ? ";
        setString(1, hAjcpStmtCycle);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_workday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hWdayThisCloseDate = getValue("h_wday_this_close_date");
        }

        return (0);
    }

    /***********************************************************************/
    int selectActJcicLog() throws Exception {
        sqlCmd = "select 1 cnt";
        sqlCmd += " from act_jcic_log  ";
        sqlCmd += "where p_seqno    = ?  ";
        sqlCmd += "  and acct_month = ?  ";
        sqlCmd += "  and stmt_cycle = ?  ";
        sqlCmd += "  and log_type   = 'A' ";
        setString(1, hAjcpPSeqno);
        setString(2, hAjcpAcctMonth);
        setString(3, hAjcpStmtCycle);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCnt = getValueInt("cnt");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertActJcicLog(int hInt1, int hInt2) throws Exception {
        sqlCmd = "insert into act_jcic_log ";
        sqlCmd += " (log_type,";
        sqlCmd += " sub_log_type,";
        sqlCmd += " acct_month,";
        sqlCmd += " corp_id_p_seqno,";
        sqlCmd += " acct_type,";
        // sqlCmd += " acct_key,";
        sqlCmd += " p_seqno,";
        sqlCmd += " id_p_seqno,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " proc_date,";
        sqlCmd += " corp_no,";
      //sqlCmd += " id,";
      //sqlCmd += " id_code,";
        sqlCmd += " stmt_cycle_date,"; /* 結帳日 */
        sqlCmd += " line_of_credit_amt,"; /* 永久信用額度 */
        sqlCmd += " stmt_last_payday,"; /* 繳款截止日 */
        sqlCmd += " bin_type,"; /* 信用卡別名稱 */
        sqlCmd += " cash_lmt_balance,"; /* fancy 預借現金額度 */
        sqlCmd += " cashadv_limit,"; /* 一般卡預借現金額度 */
        sqlCmd += " stmt_this_ttl_amt,"; /* 本期應付帳款金額 */
        sqlCmd += " stmt_mp,"; /* 本期最低應繳金額 */
        sqlCmd += " billed_end_bal_bl,"; /* 本期消費金額 bl */
        sqlCmd += " billed_end_bal_it,"; /* 本期消費金額 it */
        sqlCmd += " billed_end_bal_id,"; /* 本期消費金額 id */
        sqlCmd += " billed_end_bal_ot,"; /* 本期消費金額 ot */
        sqlCmd += " billed_end_bal_ca,"; /* 本期預借現金金額(含分期費用金額) */
        sqlCmd += " billed_end_bal_ao,"; /* 本期餘額代償金額 */
        sqlCmd += " billed_end_bal_af,"; /* 本期其他費用金額 af */
        sqlCmd += " billed_end_bal_lf,"; /* 本期其他費用金額 lf */
        sqlCmd += " billed_end_bal_pf,"; /* 本期其他費用金額 pf */
        sqlCmd += " billed_end_bal_ri,"; /* 分期預借現金金額(借用該欄位) */
        sqlCmd += " billed_end_bal_pn,"; /* 本期其他費用金額 pn */
        sqlCmd += " ttl_amt_bal,"; /* 截至上期之循環信用餘額金額 */
        sqlCmd += " bill_interest,"; /* 本期應繳循環信用利息 */
        sqlCmd += " stmt_adjust_amt,"; /* 本期調整金額 */
        sqlCmd += " unpost_inst_fee,"; /* 未到期代墊消費款金額 */
        sqlCmd += " unpost_card_fee,"; /* 未到期分期預借現金金額 */
        sqlCmd += " stmt_last_ttl,"; /* 上期應付帳款金額 */
        sqlCmd += " payment_amt_rate,"; /* 上期繳款之繳款狀態代號(金額) */
        sqlCmd += " payment_time_rate,"; /* 上期繳款之繳款狀態代號(時間) */
        sqlCmd += " stmt_payment_amt,"; /* 上期繳款金額 */
        sqlCmd += " jcic_acct_status,"; /* 債權狀態註記 */
        sqlCmd += " jcic_acct_status_flag,"; /* 債權狀態結案註記 */
        sqlCmd += " bill_type_flag,"; /* 帳單別 */
        sqlCmd += " status_change_date,";
        sqlCmd += " debt_close_date,"; /* 不良債權清償日期 */
        sqlCmd += " last_min_pay_date,";
        sqlCmd += " last_payment_date,";
        sqlCmd += " sale_date,";
        sqlCmd += " npl_corp_no,";
        sqlCmd += " jcic_remark,";
        sqlCmd += " ecs_ttl_amt_bal,";
        sqlCmd += " acct_jrnl_bal,";
        sqlCmd += " valid_cnt,";
        sqlCmd += " acct_status,";
        sqlCmd += " stop_flag,";
        sqlCmd += " report_reason,";
        sqlCmd += " mod_pgm,";
        sqlCmd += " mod_time,";
        sqlCmd += " unpost_inst_stage_fee,";
        sqlCmd += " oversea_cashadv_limit,";
        sqlCmd += " year_revolve_int_rate,";
        sqlCmd += " temp_of_credit_amt,";
        sqlCmd += " cca_temp_credit_amt,";
        sqlCmd += " cca_adj_eff_start_date,";
        sqlCmd += " cca_adj_eff_end_date)";
        sqlCmd += " select ";
        sqlCmd += " decode(?,0,'A','C'),";
      //sqlCmd += " decode(?,0,'C','A'),";
        sqlCmd += " decode(?,0,'C',2,'D','A'),";
        sqlCmd += " acct_month,";
        sqlCmd += " corp_id_p_seqno,";
        sqlCmd += " acct_type,";
        sqlCmd += " p_seqno,";
        sqlCmd += " id_p_seqno,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " proc_date,";
        sqlCmd += " corp_no,";
      //sqlCmd += " id_no,";
      //sqlCmd += " id_code,";
        sqlCmd += " stmt_cycle_date,";
        sqlCmd += " line_of_credit_amt,";
        sqlCmd += " stmt_last_payday,";
        sqlCmd += " bin_type,";
        sqlCmd += " cash_lmt_balance,";
        sqlCmd += " cashadv_limit,";
        sqlCmd += " stmt_this_ttl_amt,";
        sqlCmd += " stmt_mp,";
        sqlCmd += " billed_end_bal_bl,";
        sqlCmd += " billed_end_bal_it,";
        sqlCmd += " billed_end_bal_id,";
        sqlCmd += " billed_end_bal_ot,";
        sqlCmd += " billed_end_bal_ca,";
        sqlCmd += " billed_end_bal_ao,";
        sqlCmd += " billed_end_bal_af,";
        sqlCmd += " billed_end_bal_lf,";
        sqlCmd += " billed_end_bal_pf,";
        sqlCmd += " billed_end_bal_ri,";
        sqlCmd += " billed_end_bal_pn,";
        sqlCmd += " ttl_amt_bal,";
        sqlCmd += " bill_interest,";
        sqlCmd += " stmt_adjust_amt,";
        sqlCmd += " unpost_inst_fee,";
        sqlCmd += " unpost_card_fee,";
        sqlCmd += " stmt_last_ttl,";
        sqlCmd += " payment_amt_rate,";
        sqlCmd += " payment_time_rate,";
        sqlCmd += " stmt_payment_amt,";
        sqlCmd += " jcic_acct_status,";
        sqlCmd += " jcic_acct_status_flag,";
        sqlCmd += " bill_type_flag,";
        sqlCmd += " status_change_date,";
        sqlCmd += " debt_close_date,";
        sqlCmd += " last_min_pay_date,";
        sqlCmd += " last_payment_date,";
        sqlCmd += " sale_date,";
        sqlCmd += " npl_corp_no,";
        sqlCmd += " jcic_remark,";
        sqlCmd += " ecs_ttl_amt_bal,";
        sqlCmd += " acct_jrnl_bal,";
        sqlCmd += " valid_cnt,";
        sqlCmd += " acct_status,";
        sqlCmd += " stop_flag,";
        sqlCmd += " report_reason,";
        sqlCmd += " 'ActN018',";
        sqlCmd += " sysdate,";
        sqlCmd += " unpost_inst_stage_fee,"; 
        sqlCmd += " oversea_cashadv_limit,";
        sqlCmd += " year_revolve_int_rate,";
        sqlCmd += " temp_of_credit_amt,";
        sqlCmd += " cca_temp_credit_amt,";
        sqlCmd += " cca_adj_eff_start_date,";
        sqlCmd += " cca_adj_eff_end_date ";
        sqlCmd += " from act_jcic_cmp where rowid =  ?  ";
        setInt(1, hInt1);
        setInt(2, hInt2);
        setRowId(3, hAjcpRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActJcicTxn() throws Exception {
        sqlCmd = "insert into act_jcic_txn ";
        sqlCmd += " (this_acct_month,";
        sqlCmd += " p_seqno,";
        sqlCmd += " business_date,";
        sqlCmd += " payment_num,";
        sqlCmd += " acct_type,";
        sqlCmd += " acct_key,";
        sqlCmd += " mod_time,";
        sqlCmd += " mod_pgm,";
        sqlCmd += " apr_date,";
        sqlCmd += " apr_user,";
        sqlCmd += " proc_flag,";
        sqlCmd += " proc_date,";
        sqlCmd += " txn_type)";
        sqlCmd += "select ";
        sqlCmd += " a.acct_month,";
        sqlCmd += " a.p_seqno,";
        sqlCmd += " ?,";
        sqlCmd += " ?,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " b.acct_key,";
        sqlCmd += " sysdate,";
        sqlCmd += " 'ActN018',";
        sqlCmd += " a.apr_date,";
        sqlCmd += " a.apr_user,";
        sqlCmd += " 'Y',";
        sqlCmd += " ?,";
        sqlCmd += " decode(a.sub_log_type, 'A', '17', '18') ";
        sqlCmd += " from act_jcic_cmp a, act_acno b ";
        sqlCmd += "where a.rowid = ? ";
        sqlCmd += "  and a.p_seqno = b.acno_p_seqno ";
        setString(1, hBusiBusinessDate);
        setString(2, hAjtnPaymentNum);
        setString(3, hBusiBusinessDate);
        setRowId(4, hAjcpRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteActJcicCmp() throws Exception {
        daoTable = "act_jcic_cmp";
        whereStr = "where rowid = ? ";
        setRowId(1, hAjcpRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_act_jcic_cmp not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN018 proc = new ActN018();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
