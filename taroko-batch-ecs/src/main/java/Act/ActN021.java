/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/27  V1.00.01    Brian     error correction                          *
 *  111/10/13  V1.00.07  jiangyigndong  updated for project coding standard    *
 *  112/04/07  V1.00.08  Simon       1.關帳後一、二日不執行改為關帳後一日不執行*
 *                                   2.TCB 商務卡個繳戶合併為一筆以公司戶報送  *
 *                                   3.新增國外預借現金額度、循環信用年利率 產生至 act_jcic_log*
 *                                   4.商務卡合計修改為 by corp_p_seqno、acct_type*
 *  112/05/01  V1.00.09    Simon     add fetch's daoTable                      *
 *  112/08/27  V1.00.10    Simon     商務卡讀取 act_acct_hst 更正為讀取 act_acct*
 *  112/09/15  V1.00.11    Simon     新增帶入 cca_card_acct 臨調額度           *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*每日帳戶(YU)結清報送簡易KK4結清處理程式*/
public class ActN021 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private final String PROGNAME = "每日帳戶(YU)結清報送簡易KK4結清處理程式 "
                                  + "112/09/15 V1.00.11";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActN021";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hAjlgPSeqno = "";
    String hAjlgCorpPSeqno = "";
    String hAjlgAcctType = "";
    String hAjlgJcicAcctStatus = "";
    String hAjlgJcicAcctStatusFlag = "";
    String hAjlgRowid = "";
    String hWdayThisAcctMonth = "";
    int hCnt = 0;
    String hWdayThisCloseDate = "";
    String hCardOppostDate = "";
    String hCardCurrentCode = "";
    String[] hMCardOppostDate = new String[250];
    String[] hMCardCurrentCode = new String[250];

    int totalFCnt = 0;
    int totalICnt = 0;
    int hAjlgValidCnt = 0;
    double hAchtAcctJrnlBal = 0;

    String hAjlgStopFlag = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comcr.errRtn("Usage : ActN021 [business_date]", "", hCallBatchSeqno);
            }

            selectPtrBusinday();

            showLogMessage("I", "", String.format("一般卡每日帳戶報送簡易KK4結清(YU)處理開始......."));
            selectActJcicLog();

            showLogMessage("I", "", String.format("商務卡每日帳戶報送簡易KK4結清(YU)處理開始......."));
            selectActJcicLog2();//TCB 商務卡個繳戶合併為一筆以公司戶報送

            showLogMessage("I", "", String.format("Total fetch,insert record[%d][%d]", totalFCnt, totalICnt));
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
        sqlCmd = "select decode( cast(? as varchar(8)) ,'',business_date, ? ) h_busi_business_date ";
        sqlCmd += " from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
        }

    }

  /***********************************************************************/
  void selectActJcicLog() throws Exception {

    fetchExtend = "ajlg1.";
    daoTable = "act_jcic_log a,act_acct b,ptr_workday c,act_acno d";

    sqlCmd = "select ";
    sqlCmd += " a.p_seqno,";
    sqlCmd += " a.jcic_acct_status,";
    sqlCmd += " a.jcic_acct_status_flag,";
    sqlCmd += " a.rowid rowid,";
    sqlCmd += " c.this_acct_month ";
    sqlCmd += " from act_jcic_log a,act_acct b,ptr_workday c,act_acno d ";
    sqlCmd += "where a.sale_date     = '' ";
    sqlCmd += "  and a.p_seqno       = b.p_seqno ";
    sqlCmd += "  and a.p_seqno       = d.p_seqno ";
    sqlCmd += "  and d.acno_flag     = '1' ";
    sqlCmd += "  and a.acct_month    = c.this_acct_month ";
    sqlCmd += "  and a.acct_jrnl_bal > 0 ";
    sqlCmd += "  and a.log_type      = 'A' ";
    sqlCmd += "  and a.stmt_cycle    = c.stmt_cycle ";
  //sqlCmd += "  and c.this_close_date != to_char(to_date(?,'yyyymmdd')-2 days,'yyyymmdd') ";
    sqlCmd += "  and c.this_close_date != to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') ";
    sqlCmd += "  and b.acct_jrnl_bal <=0 ";
    sqlCmd += "  and not exists (select h.p_seqno ";
    sqlCmd += "      from act_jcic_end h ";
    sqlCmd += "      where h.p_seqno = a.p_seqno ";
    sqlCmd += "      and h.send_flag in ('Y','U')) ";
    setString(1, hBusiBusinessDate);
  //setString(2, hBusiBusinessDate);
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
        hAjlgPSeqno = getValue("ajlg1.p_seqno");
        hAjlgJcicAcctStatus = getValue("ajlg1.jcic_acct_status");
        hAjlgJcicAcctStatusFlag = getValue("ajlg1.jcic_acct_status_flag");
        hAjlgRowid = getValue("ajlg1.rowid");
        hWdayThisAcctMonth = getValue("ajlg1.this_acct_month");

        totalFCnt++;
        if ((totalFCnt % 5000) == 0) {
            showLogMessage("I", "", String.format("Process fetch record[%d]", totalFCnt));
        }

        if (selectActJcicLog1() != 0)
            continue;

        if (hAjlgJcicAcctStatus.length() == 0) {
            selectCrdCard();
            if (hAjlgValidCnt > 0)
                continue;
            hAjlgJcicAcctStatusFlag = "Y";
            if (hAjlgStopFlag.equals("3"))
                hAjlgJcicAcctStatusFlag = "U";
        } else {
            if (hAjlgJcicAcctStatus.equals("A")) {
                hAjlgJcicAcctStatusFlag = "Y";
            } else {
                hAjlgJcicAcctStatusFlag = "U";
            }
        }

        insertActJcicLog();
        totalICnt++;
        if ((totalICnt % 500) == 0) {
            showLogMessage("I", "", String.format("Process insert record[%d]", totalICnt));
        }
    }
    closeCursor(cursorIndex);

  }

  /***********************************************************************/
  void selectActJcicLog2() throws Exception {

    fetchExtend = "ajlg2.";
    daoTable = "act_jcic_log a,ptr_workday c,act_acno d";

    sqlCmd = "select ";
    sqlCmd += " a.p_seqno,";
    sqlCmd += " d.corp_p_seqno,";
    sqlCmd += " d.acct_type,";
    sqlCmd += " a.jcic_acct_status,";
    sqlCmd += " a.jcic_acct_status_flag,";
    sqlCmd += " a.rowid rowid,";
    sqlCmd += " c.this_acct_month ";
    sqlCmd += " from act_jcic_log a,ptr_workday c,act_acno d ";
    sqlCmd += "where a.sale_date     = '' ";
    sqlCmd += "  and a.p_seqno       = d.p_seqno ";
    sqlCmd += "  and d.acno_flag     = '2' ";
    sqlCmd += "  and a.acct_month    = c.this_acct_month ";
    sqlCmd += "  and a.acct_jrnl_bal > 0 ";
    sqlCmd += "  and a.log_type      = 'A' ";
    sqlCmd += "  and a.stmt_cycle    = c.stmt_cycle ";
  //sqlCmd += "  and c.this_close_date != to_char(to_date(?,'yyyymmdd')-2 days,'yyyymmdd') ";
    sqlCmd += "  and c.this_close_date != to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') ";
    sqlCmd += "  and not exists (select h.p_seqno ";
    sqlCmd += "      from act_jcic_end h ";
    sqlCmd += "      where h.p_seqno = a.p_seqno ";
    sqlCmd += "      and h.send_flag in ('Y','U')) ";
    setString(1, hBusiBusinessDate);
  //setString(2, hBusiBusinessDate);
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
        hAjlgPSeqno = getValue("ajlg2.p_seqno");
        hAjlgCorpPSeqno = getValue("ajlg2.corp_p_seqno");
        hAjlgAcctType = getValue("ajlg2.acct_type");
        hAjlgJcicAcctStatus = getValue("ajlg2.jcic_acct_status");
        hAjlgJcicAcctStatusFlag = getValue("ajlg2.jcic_acct_status_flag");
        hAjlgRowid = getValue("ajlg2.rowid");
        hWdayThisAcctMonth = getValue("ajlg2.this_acct_month");

        totalFCnt++;
        if ((totalFCnt % 1000) == 0) {
            showLogMessage("I", "", String.format("Process fetch record[%d]", totalFCnt));
        }

        if (selectActJcicLog1() != 0)
            continue;

        hAchtAcctJrnlBal = 0;
        selectActAcct2();
        if (hAchtAcctJrnlBal > 0)
            continue;

        if (hAjlgJcicAcctStatus.length() == 0) {
            selectCrdCard2();
            if (hAjlgValidCnt > 0)
                continue;
            hAjlgJcicAcctStatusFlag = "Y";
            if (hAjlgStopFlag.equals("3"))
                hAjlgJcicAcctStatusFlag = "U";
        } else {
            if (hAjlgJcicAcctStatus.equals("A")) {
                hAjlgJcicAcctStatusFlag = "Y";
            } else {
                hAjlgJcicAcctStatusFlag = "U";
            }
        }

        insertActJcicLog();
        totalICnt++;
        if ((totalICnt % 500) == 0) {
            showLogMessage("I", "", String.format("Process insert record[%d]", totalICnt));
        }
    }
    closeCursor(cursorIndex);

  }

    /***********************************************************************/
    int selectActJcicLog1() throws Exception {
        /* select 1 改為 select count(*) 怕撈出兩筆程式當掉 */
        sqlCmd = "select count(*) h_cnt ";
        sqlCmd += " from act_jcic_log  ";
        sqlCmd += "where decode(acct_month,'','x',acct_month) = ?  ";
        sqlCmd += "  and p_seqno         = ?  ";
        sqlCmd += "  and report_reason   = '02' ";
        setString(1, hWdayThisAcctMonth);
        setString(2, hAjlgPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_jcic_log not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }
        return (hCnt);
    }

  /***********************************************************************/
  void selectCrdCard() throws Exception {

/***
    sqlCmd = "select decode(current_code,'0','', decode(sign(decode(oppost_date, '', '0', oppost_date) - decode(cast(? as varchar(8)), '', '0', cast(? as varchar(8))) ),1,'',oppost_date)) h_card_oppost_date,";
    sqlCmd += " decode(current_code,'0','0', decode(sign(decode(oppost_date, '', '0', oppost_date)- decode(cast(? as varchar(8)), '', '0', cast(? as varchar(8))) ),1,'0',current_code)) h_card_current_code ";
    sqlCmd += " from crd_card a  ";
    sqlCmd += "where a.acno_p_seqno  = ?  ";
    sqlCmd += "  and card_no  = major_card_no ORDER by decode(oppost_date,'','30001231',oppost_date) desc,issue_date desc ";
    setString(1, hWdayThisCloseDate);
    setString(2, hWdayThisCloseDate);
    setString(3, hWdayThisCloseDate);
    setString(4, hWdayThisCloseDate);
    setString(5, hAjlgPSeqno);
***/
//不必用上段多層 decode 判斷
    sqlCmd  = "select oppost_date h_card_oppost_date,";
    sqlCmd += " current_code h_card_current_code ";
    sqlCmd += " from crd_card  ";
    sqlCmd += "where p_seqno  = ?  ";
    sqlCmd += "  and card_no  = major_card_no ORDER by "
            + " decode(oppost_date,'','30001231',oppost_date) desc,issue_date desc ";
    setString(1, hAjlgPSeqno);

    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
        hMCardOppostDate[i] = getValue("h_card_oppost_date", i);
        hMCardCurrentCode[i] = getValue("h_card_current_code", i);
    }

    int ptrBintableCnt = recordCnt;

    hAjlgStopFlag = "0";
    hAjlgValidCnt = 0;

    if (ptrBintableCnt == 0) {
        hCardOppostDate = "";
        hCardCurrentCode = "";
    } else {
        hCardOppostDate = hMCardOppostDate[0];
        hCardCurrentCode = hMCardCurrentCode[0];
    }

    if (hCardCurrentCode.equals("3"))
        hAjlgStopFlag = "3";

    for (int inta1 = 0; inta1 < ptrBintableCnt; inta1++) {
        if (hMCardCurrentCode[inta1].equals("0"))
            hAjlgValidCnt++;
    }

  }

  /***********************************************************************/
  void selectActAcct2() throws Exception {

    extendField = "acct2.";
		sqlCmd  = " select ";
 		sqlCmd += " a.corp_p_seqno, a.acct_type, "; 
 		sqlCmd += " sum(b.acct_jrnl_bal) h_acct_acct_jrnl_bal "; 
	//sqlCmd += " from act_acno a, act_acct_hst h ";  
	  sqlCmd += " from act_acno a, act_acct b ";  
		sqlCmd += " where 1=1 ";
		sqlCmd += "   and a.acno_flag in ('3') ";
    sqlCmd += "   and a.p_seqno = b.p_seqno ";
		sqlCmd += "   and a.corp_p_seqno = ?  ";
		sqlCmd += "   and a.acct_type = ?  ";
	//sqlCmd += "   and h.acct_month = ?  ";
		sqlCmd += " group by a.corp_p_seqno, a.acct_type ";

    setString(1, hAjlgCorpPSeqno);
    setString(2, hAjlgAcctType);
  //setString(3, hWdayThisAcctMonth);

    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hAchtAcctJrnlBal = getValueDouble("acct2.h_acct_acct_jrnl_bal");
    }

  }

  /***********************************************************************/
  void selectCrdCard2() throws Exception {

    sqlCmd  = "select oppost_date h_card_oppost_date,";
    sqlCmd += " current_code h_card_current_code ";
    sqlCmd += " from crd_card  ";
    sqlCmd += "where corp_p_seqno  = ? ";
    sqlCmd += "  and acct_type  = ? ";
    sqlCmd += "  and card_no  = major_card_no ORDER by "
            + " decode(oppost_date,'','30001231',oppost_date) desc,issue_date desc ";
    setString(1, hAjlgCorpPSeqno);
    setString(2, hAjlgAcctType);

    int recordCnt = selectTable();
    for (int i = 0; i < recordCnt; i++) {
        hMCardOppostDate[i] = getValue("h_card_oppost_date", i);
        hMCardCurrentCode[i] = getValue("h_card_current_code", i);
    }

    int ptrBintableCnt = recordCnt;

    hAjlgStopFlag = "0";
    hAjlgValidCnt = 0;

    if (ptrBintableCnt == 0) {
        hCardOppostDate = "";
        hCardCurrentCode = "";
    } else {
        hCardOppostDate = hMCardOppostDate[0];
        hCardCurrentCode = hMCardCurrentCode[0];
    }

    if (hCardCurrentCode.equals("3"))
        hAjlgStopFlag = "3";

    for (int inta1 = 0; inta1 < ptrBintableCnt; inta1++) {
        if (hMCardCurrentCode[inta1].equals("0"))
            hAjlgValidCnt++;
    }

  }

    /***********************************************************************/
    void insertActJcicLog() throws Exception {
        sqlCmd = "insert into act_jcic_log ";
        sqlCmd += " (log_type,";
        sqlCmd += " sub_log_type,";
        sqlCmd += " acct_month,";
        sqlCmd += " corp_id_p_seqno,";
        sqlCmd += " acct_type,";
        sqlCmd += " p_seqno,";
        sqlCmd += " id_p_seqno,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " proc_date,";
        sqlCmd += " corp_no,";
        //sqlCmd += " id,";
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
        sqlCmd += " report_reason,";
        sqlCmd += " mod_pgm,";
        sqlCmd += " mod_time,";
        sqlCmd += " unpost_inst_Stage_fee,";
        sqlCmd += " oversea_cashadv_limit,";
        sqlCmd += " year_revolve_int_rate,";
        sqlCmd += " temp_of_credit_amt,";
        sqlCmd += " cca_temp_credit_amt,";
        sqlCmd += " cca_adj_eff_start_date,";
        sqlCmd += " cca_adj_eff_end_date)";
        sqlCmd += " select ";
        sqlCmd += " 'C',";
        sqlCmd += " 'A',";
        sqlCmd += " a.acct_month,";
        sqlCmd += " a.corp_id_p_seqno,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " a.p_seqno,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " a.stmt_cycle,";
        sqlCmd += " a.proc_date,";
        sqlCmd += " a.corp_no,";
        //sqlCmd += " a.id,";
        //sqlCmd += " a.id_code,";
        sqlCmd += " a.stmt_cycle_date,";
        sqlCmd += " a.line_of_credit_amt,";
        sqlCmd += " a.stmt_last_payday,";
        sqlCmd += " a.bin_type,";
        sqlCmd += " a.cash_lmt_balance,";
        sqlCmd += " a.cashadv_limit,";
        sqlCmd += " a.stmt_this_ttl_amt,";
        sqlCmd += " a.stmt_mp,";
        sqlCmd += " a.billed_end_bal_bl,";
        sqlCmd += " a.billed_end_bal_it,";
        sqlCmd += " a.billed_end_bal_id,";
        sqlCmd += " a.billed_end_bal_ot,";
        sqlCmd += " a.billed_end_bal_ca,";
        sqlCmd += " a.billed_end_bal_ao,";
        sqlCmd += " a.billed_end_bal_af,";
        sqlCmd += " a.billed_end_bal_lf,";
        sqlCmd += " a.billed_end_bal_pf,";
        sqlCmd += " a.billed_end_bal_ri,";
        sqlCmd += " a.billed_end_bal_pn,";
        sqlCmd += " a.ttl_amt_bal,";
        sqlCmd += " a.bill_interest,";
        sqlCmd += " a.stmt_adjust_amt,";
        sqlCmd += " a.unpost_inst_fee,";
        sqlCmd += " a.unpost_card_fee,";
        sqlCmd += " a.stmt_last_ttl,";
        sqlCmd += " a.payment_amt_rate,";
        sqlCmd += " a.payment_time_rate,";
        sqlCmd += " a.stmt_payment_amt,";
        sqlCmd += " a.jcic_acct_status,";
        sqlCmd += " ?,";
        sqlCmd += " a.bill_type_flag,";
        sqlCmd += " a.status_change_date,";
        sqlCmd += " decode(?,'U', decode(b.debt_close_date, '',?, b.debt_close_date), b.debt_close_date),";
        sqlCmd += " a.last_min_pay_date,";
        sqlCmd += " a.last_payment_date,";
        sqlCmd += " a.sale_date,";
        sqlCmd += " a.npl_corp_no,";
        sqlCmd += " a.jcic_remark,";
        sqlCmd += " '02',";
        sqlCmd += " 'ActN021',";
        sqlCmd += " sysdate,";
        sqlCmd += " a.unpost_inst_Stage_fee,";
        sqlCmd += " oversea_cashadv_limit,";
        sqlCmd += " year_revolve_int_rate,";
        sqlCmd += " temp_of_credit_amt,";
        sqlCmd += " cca_temp_credit_amt,";
        sqlCmd += " cca_adj_eff_start_date,";
        sqlCmd += " cca_adj_eff_end_date ";
        sqlCmd += "from act_acno b,act_jcic_log a where b.acno_p_seqno = a.p_seqno and a.rowid =  ?  ";
        setString(1, hAjlgJcicAcctStatusFlag);
        setString(2, hAjlgJcicAcctStatusFlag);
        setString(3, hBusiBusinessDate);
        setRowId(4, hAjlgRowid);
        insertTable();

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN021 proc = new ActN021();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
