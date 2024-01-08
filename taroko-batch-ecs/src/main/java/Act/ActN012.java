/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/25  V1.00.01    Brian     error correction                          *
 *  109/11/19  V1.00.02    shiyuqi   updated for project coding standard       * 
 *  112/05/02  V1.00.03    Simon     1.國外預借現金額度、循環信用年利率 取自 act_jcic_log*
 *                                   2.add fetch's daoTable                    *
 *                                   3.結案應收帳款異動理由(hAjlgReportReason) "01" changed to "02"
 *  112/08/30  V1.00.04    Simon     催、呆更新為正常或逾期戶及強停卡更新為非強停卡報送kk4
 *                                   之理由碼不可為"02"，更正為"01"
 *  112/09/15  V1.00.05    Simon     新增帶入 cca_card_acct 臨調額度           *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*JCIC-繳款憑等異動(新格式)處理程式*/
public class ActN012 extends AccessDAO {

    private String progname = "JCIC-繳款憑等異動(新格式)處理程式  112/09/15  V1.00.05 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActN012";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hAjtnPSeqno = "";
    String hAjtnBusinessDate = "";
    String hAjtnAcctKey = "";
    String hAjtnPaymentRate = "";
    String hAjtnPaymentNum = "";
    String hAjtnThisAcctMonth = "";
    String hAjtnTxnType = "";
    String hAjtnRowid = "";
    String hAjlgPaymentAmtRate = "";
    String hAjlgPaymentTimeRate = "";
    String hAjlgReportReason = "";
    String hWdayLastAcctMonth = "";
    String hInt = "";
    double hAjlgStmtPaymentAmt = 0;
    double hAjlgTtlAmtBal = 0;
    String hAjlgRowid = "";
    String hAcnoStmtCycle = "";
    String newMonth = "";
    public static  String nFormatMonth = "200510";
    int insertCnt = 0;
    int skipCnt = 0;
    String tmpstr = "";

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
            if (args.length > 1) {
                comc.errExit("Usage : ActN012", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            newMonth = nFormatMonth;// "200510"

        /*** 報送kk4理由碼對應說明
        select * from ptr_sys_idtab where wf_type='KK4_REPORT_REASON';
        01	報送例行性月結資料
        02	報送KK4簡要版
        11	誤報/漏報/作業疏失/電腦系統錯誤
        21	消金債務協商/消債條例前置協商、更生、清算案件
        22	個別協商案件
        23	重大天然災害受災戶
        24	年費掛失費調整/小額欠款戶
        31	法院判決
        41	偽冒申請/冒用/盜刷
        42	身分證改號
        51	爭議款項
        52	非當事人疏失(調整繳評理由碼即報為 52)
        61	當月份轉催/轉呆
        62	變更結帳日
        63	變更債權結案
        ***/
        /*** 本程式主要處理： 
        1.調整繳評 
        2.催、呆更新為正常或逾期戶及強停卡更新為非強停卡報送kk4 
        ***/

            selectActJcicTxn();
            showLogMessage("I", "", String.format("累計新增 %d 筆, SKIP %d 筆, ", insertCnt, skipCnt));
             // ============================================
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
    void selectActJcicTxn() throws Exception {

        daoTable = "act_jcic_txn";
        sqlCmd = "select ";
        sqlCmd += " p_seqno,";
        sqlCmd += " business_date,";
        sqlCmd += " acct_key,";
        sqlCmd += " payment_rate,";
        sqlCmd += " payment_num,";
        sqlCmd += " this_acct_month,";
        sqlCmd += " decode(txn_type,'','00',txn_type) h_ajtn_txn_type,";
        sqlCmd += " rowid rowid ";
        sqlCmd += " from act_jcic_txn ";
        sqlCmd += "where proc_flag = 'N' ";
        sqlCmd += "  and apr_date != '' ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAjtnPSeqno = getValue("p_seqno");
            hAjtnBusinessDate = getValue("business_date");
            hAjtnAcctKey = getValue("acct_key");
            hAjtnPaymentRate = getValue("payment_rate");
            hAjtnPaymentNum = getValue("payment_num");
            hAjtnThisAcctMonth = getValue("this_acct_month");
            hAjtnTxnType = getValue("h_ajtn_txn_type");
            hAjtnRowid = getValue("rowid");

            selectActAcno0();

            if (hAjtnTxnType.substring(0, 1).equals("0")) {
                if (hAjtnTxnType.toCharArray()[1] == '0') {
                    hAjlgReportReason = "52";
                    selectPtrWorkday1();
                    if (selectActJcicLog() != 0) {
                        if (hWdayLastAcctMonth.compareTo(newMonth) < 0) {
                            updateActJcicTxn(2);
                        } else {
                            updateActJcicTxn(1);
                        }
                        skipCnt++;
                        continue;
                    }
                    insertActJcicLog();

                    if (hAjtnPaymentRate.equals("0A")) {
                        hAjlgPaymentAmtRate = "1";
                        hAjlgPaymentTimeRate = "N";
                    } else if (hAjtnPaymentRate.equals("0B")) {
                        hAjlgPaymentAmtRate = "1";
                        hAjlgPaymentTimeRate = "0";
                    } else if (hAjtnPaymentRate.equals("0C")) {
                        hAjlgPaymentAmtRate = "2";
                        hAjlgPaymentTimeRate = "N";
                    } else if (hAjtnPaymentRate.equals("0D")) {
                        hAjlgPaymentAmtRate = "2";
                        hAjlgPaymentTimeRate = "0";
                    } else if (hAjtnPaymentRate.equals("0E")) {
                        hAjlgPaymentAmtRate = "X";
                        hAjlgPaymentTimeRate = "X";
                    } else if ((hAjtnPaymentRate.compareTo("01") >= 0)
                            && (hAjtnPaymentRate.compareTo("06") <= 0)) {
                        if (hAjlgStmtPaymentAmt == 0) {
                            hAjlgPaymentAmtRate = "4";
                        } else {
                            hAjlgPaymentAmtRate = "3";
                        }
                        tmpstr = String.format("%1.1s", hAjtnPaymentRate.substring(1));
                        hAjlgPaymentTimeRate = tmpstr;
                    } else {
                        if (hAjlgStmtPaymentAmt == 0) {
                            hAjlgPaymentAmtRate = "4";
                        } else {
                            hAjlgPaymentAmtRate = "3";
                        }
                        hAjlgPaymentTimeRate = "7";
                    }
                } else if (hAjtnTxnType.toCharArray()[1] == 'L') { /* act_n019 */
                    hAjlgReportReason = "21";
                    hWdayLastAcctMonth = hAjtnThisAcctMonth;
                    if (selectActJcicLog() != 0) {
                        updateActJcicTxn(1);
                        skipCnt++;
                        continue;
                    }
                    tmpstr = String.format("%1.1s", hAjtnPaymentRate);
                    hAjlgPaymentAmtRate = tmpstr;
                    tmpstr = String.format("%1.1s", hAjtnPaymentRate.substring(1));
                    hAjlgPaymentTimeRate = tmpstr;
                }
            } else {
                hAjlgReportReason = "01";
                hWdayLastAcctMonth = hAjtnThisAcctMonth;
                if (selectActJcicLog() != 0) {
                    updateActJcicTxn(1);
                    skipCnt++;
                    continue;
                }
                insertActJcicLog();
            }

            updateActJcicLog();
            updateActJcicTxn(0);
            insertCnt++;
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectActAcno0() throws Exception {
        hAcnoStmtCycle = "";

        sqlCmd = "select stmt_cycle ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString(1, hAjtnPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno_0   not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcnoStmtCycle = getValue("stmt_cycle");
        }

    }

    /***********************************************************************/
    void selectPtrWorkday1() throws Exception {
        hWdayLastAcctMonth = "";

        sqlCmd = "select this_acct_month,";
        sqlCmd += " to_char(add_months(to_date( ? ,'yyyymm'), 1- ? ),'yyyymm') h_wday_last_acct_month ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where stmt_cycle = ? ";
        setString(1, hAjtnThisAcctMonth);
        setString(2, hAjtnPaymentNum);
        setString(3, hAcnoStmtCycle);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_workday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hWdayLastAcctMonth = getValue("h_wday_last_acct_month");
        }

    }

    /***********************************************************************/
    int selectActJcicLog() throws Exception {
        sqlCmd = "select stmt_payment_amt,";
        sqlCmd += " ttl_amt_bal,";
        sqlCmd += " rowid rowid ";
        sqlCmd += " from act_jcic_log  ";
        sqlCmd += "where log_type    = 'A'  ";
        sqlCmd += "  and acct_month  = ?  ";
        sqlCmd += "  and p_seqno     = ? ";
        setString(1, hWdayLastAcctMonth);
        setString(2, hAjtnPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAjlgStmtPaymentAmt = getValueDouble("stmt_payment_amt");
            hAjlgTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAjlgRowid = getValue("rowid");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertActJcicLog() throws Exception {
        sqlCmd = "insert into act_jcic_log ";
        sqlCmd += " (log_type,"; /* 交易代碼 */
        sqlCmd += " acct_month,";
        sqlCmd += " corp_id_p_seqno,";
        sqlCmd += " acct_type,";
        // sqlCmd += " acct_key,"; //not found
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
        sqlCmd += " unpost_inst_stage_fee,"; /* 對帳單分期之分期總額 */
        sqlCmd += " oversea_cashadv_limit,";
        sqlCmd += " year_revolve_int_rate,";
        sqlCmd += " temp_of_credit_amt,";
        sqlCmd += " cca_temp_credit_amt,";
        sqlCmd += " cca_adj_eff_start_date,";
        sqlCmd += " cca_adj_eff_end_date)";
        sqlCmd += " select ";
        sqlCmd += " 'C',";
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
        sqlCmd += " cast(? as varchar(2)),";
        sqlCmd += " 'ActN012',";
        sqlCmd += " sysdate, ";
        sqlCmd += " unpost_inst_stage_fee,"; /* 對帳單分期之分期總額 */
        sqlCmd += " oversea_cashadv_limit,";
        sqlCmd += " year_revolve_int_rate,";
        sqlCmd += " temp_of_credit_amt,";
        sqlCmd += " cca_temp_credit_amt,";
        sqlCmd += " cca_adj_eff_start_date,";
        sqlCmd += " cca_adj_eff_end_date ";
        sqlCmd += "from act_jcic_log where log_type  = 'A' and acct_month  = ? and p_seqno   = ? fetch first 1 rows only ";
        setString(1, hAjlgReportReason);
        setString(2, hWdayLastAcctMonth);
        setString(3, hAjtnPSeqno);
        insertTable();
        //if (dupRecord.equals("Y")) {
        //    comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        //}非定義為 unique key

    }

    /***********************************************************************/
    void updateActJcicLog() throws Exception {
        daoTable = "act_jcic_log";
        updateSQL = " payment_amt_rate       = decode(substr(?, 1, 1), '0', ?, payment_amt_rate),";
        updateSQL += " payment_time_rate      = decode(substr(?, 1, 1), '0', ?, payment_time_rate),";
        updateSQL += " stmt_payment_amt       = decode(substr(?, 1, 1), '0', decode(cast(? as varchar(8)), '0A', stmt_last_ttl*-1, '0B', stmt_last_ttl*-1, stmt_payment_amt), stmt_payment_amt),";
        updateSQL += " stmt_last_ttl          = decode(substr(?, 1, 1), '0', decode(cast(? as varchar(8)), '0E', 0, stmt_last_ttl), stmt_last_ttl),";
        updateSQL += " ttl_amt_bal            = decode(substr(?, 1, 1), '0', decode(cast(? as varchar(8)), '0A', 0, '0E', 0, ttl_amt_bal), ttl_amt_bal),";
        updateSQL += " status_change_date     = decode(substr(?, 2, 1), 'A', '', status_change_date),";
        updateSQL += " debt_close_date        = decode(substr(?, 2, 1), 'A', '', debt_close_date),";
        updateSQL += " jcic_acct_status_flag  = decode(substr(?, 1, 1), 'T', '', 'U', decode( substr(?, 2, 1)" + ", 'A'"
                + ", decode(cast(? as varchar(8)), '00', 'Y', 'U')" + ", decode(jcic_acct_status, 'B', 'U', 'Y'))"
                + ", jcic_acct_status_flag),";
        updateSQL += " jcic_acct_status       = decode(substr(?, 2, 1), 'A', '', jcic_acct_status),";
        updateSQL += " npl_corp_no            = decode(substr(?, 1, 1), 'T', '', npl_corp_no),";
        updateSQL += " jcic_remark            = decode(substr(?, 1, 1), 'T', '', jcic_remark),";
        updateSQL += " sale_date              = decode(substr(?, 1, 1), 'T', '', sale_date),";
        updateSQL += " mod_time               = sysdate,";
        updateSQL += " mod_pgm                = 'ActN012'";
        whereStr = "where log_type      = 'C'  ";
        whereStr += " and report_reason = ?  ";
        whereStr += " and acct_month    = ?  ";
        whereStr += " and p_seqno       = ? ";
        setString(1, hAjtnTxnType);
        setString(2, hAjlgPaymentAmtRate);
        setString(3, hAjtnTxnType);
        setString(4, hAjlgPaymentTimeRate);
        setString(5, hAjtnTxnType);
        setString(6, hAjtnPaymentRate);
        setString(7, hAjtnTxnType);
        setString(8, hAjtnPaymentRate);
        setString(9, hAjtnTxnType);
        setString(10, hAjtnPaymentRate);
        setString(11, hAjtnTxnType);
        setString(12, hAjtnTxnType);
        setString(13, hAjtnTxnType);
        setString(14, hAjtnTxnType);
        setString(15, hAjtnPaymentRate);
        setString(16, hAjtnTxnType);
        setString(17, hAjtnTxnType);
        setString(18, hAjtnTxnType);
        setString(19, hAjtnTxnType);
        setString(20, hAjlgReportReason);
        setString(21, hWdayLastAcctMonth);
        setString(22, hAjtnPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void updateActJcicTxn(int hInt) throws Exception {
        daoTable = "act_jcic_txn";
        updateSQL = "proc_flag   = decode(?, 1, 'O', 2, 'X', 'Y'),";
        updateSQL += " mod_time    = mod_time,";
        updateSQL += " mod_pgm     = 'ActN012'";
        whereStr = "where rowid    = ? ";
        setInt(1, hInt);
        setRowId(2, hAjtnRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_jcic_txn not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN012 proc = new ActN012();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
