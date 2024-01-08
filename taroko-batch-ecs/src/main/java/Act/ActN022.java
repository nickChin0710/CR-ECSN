/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/27  V1.00.01    Brian     error correction                          *
 *  110/08/12  V1.00.04    JeffKung  ref_mantis 8299 : 分期餘額改為與ccas_limit一致 *
 *  111/10/13  V1.00.05  jiangyigndong  updated for project coding standard    *
 *  112/04/25  V1.00.06    Simon     1.kk8 只報送一般卡                        *
 *                                   2.selectBilContract() code error fixed    *
 *                                   3.check available date revised            *
 *                                   4.comc.errExit() 取代 comcr.errRtn() 顯示"本程式只在每週三或四且非關帳日才需執行"*
 *  112/05/01  V1.00.07    Simon     add fetch's daoTable                      *
 *  112/06/11  V1.00.08    Simon     本程式在每週五(換日後)執行，遇假日提前    *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*每週帳戶餘額(KK8)報送處理程式*/
public class ActN022 extends AccessDAO {

    private final String PROGNAME = "每週帳戶餘額(KK8)報送處理程式  112/06/11 V1.00.08";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActN022";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hTempBusinessDate = "";
    String hTempWeekSDate = "";
    String hTempWeekEDate = "";
    String hTempWeekNowseq = "";
    String hWdayStmtCycle = "";
    String hAjlgPSeqno = "";
    String hAjlgAcctType = "";
    String hAjlgAcctKey = "";
    double hacctacctjrnlbal = 0;
    String hAjlgRowid = "";
    String hWdayThisAcctMonth = "";
    double hAjlgBilledEndBalRi = 0;
    String hWdayThisCloseDate = "";
    String hCardOppostDate = "";
    String hCardCurrentCode = "";
    double hAcmlCashUseBalance = 0;
    double hAjlgUnpostInstFee = 0;
    double hAjlgUnpostCardFee = 0;
    double hAjlgUnpostInstStageFee = 0;
    int hAjlgValidCnt = 0;
    String hAjlgStopFlag = "";
    String hTempWeek1Date = "";
    String hThisFriDate = "";

    String[] hMCardOppostDate = new String[250];
    String[] hMCardCurrentCode = new String[250];

    int rtn = 0;
    int totalCnt = 0;

    public int mainProcess(String[] args) {

      try {

        // ====================================
        // 固定要做的
        dateTime();
        setConsoleMode("Y");
        javaProgram = this.getClass().getName();
        showLogMessage("I", "", javaProgram + " " + PROGNAME);
        // =====================================
        exceptExit = 1;
        if (args.length != 0 && args.length != 1 && args.length != 2) {
            comc.errExit("Usage : ActN022 [business_date]", "");
        }

        // 固定要做的

        if (!connectDataBase()) {
            comc.errExit("connect DataBase error", "");
        }

        hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
        if ((args.length == 1) && (args[0].length() == 8)) {
          hBusiBusinessDate = args[0];
          hCallBatchSeqno = "";
        }
        comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

        comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

      //selectPtrBusinday();
        getWeekNowSeq();
        checkFriDate();
        showLogMessage("I", "", String.format("本程式在每週五(換日後)執行，遇假日提前"));
        showLogMessage("I", "", String.format("本週預計執行日(換日後)[%s]",hThisFriDate));
        showLogMessage("I", "", String.format("本日資料日[%s], 換日後日期[%s]",
        hTempBusinessDate, hBusiBusinessDate));

        if (!hBusiBusinessDate.equals(hThisFriDate)) {
          exceptExit = 0;
          comc.errExit("本日非執行日！", hCallBatchSeqno);
        } 

/***
        showLogMessage("I", "", String.format("本週可執行日期[%s]-[%s]",
        hTempWeekSDate, hTempWeekEDate));
        if ((!hTempWeekNowseq.substring(0, 1).equals("5"))
                && (!hTempWeekNowseq.substring(0, 1).equals("6"))) {
            exceptExit = 0;
          //comcr.errRtn(String.format("本程式只在每週三或每週四,換日後才需執行"), "", hCallBatchSeqno);
            comc.errExit(String.format("本程式只在每週三或每週四,換日後才需執行"), 
            hCallBatchSeqno);
        }

        rtn = selectPtrWorkday();
        if (rtn == 0) { 
          if (hTempBusinessDate.equals(hTempWeekSDate)) {
            showLogMessage("I", "", String.format("本程式只在每週三或四且非關帳日才需執行"));
            showLogMessage("I", "", String.format("本程式在每日換日後執行, 營業日會多一天"));
            exceptExit = 0;
          //comcr.errRtn(String.format("本日[%s]為[%s]cycle執行日期, 故不執行", 
          //hTempBusinessDate, hWdayStmtCycle), "", hCallBatchSeqno);
            comc.errExit(String.format("本日[%s]為[%s]cycle執行日期, 故不執行", 
            hTempBusinessDate, hWdayStmtCycle), hCallBatchSeqno);
          } 
        } else {
          if (hTempBusinessDate.equals(hTempWeekEDate)) {
            showLogMessage("I", "", String.format("本程式只在每週三或四且非關帳日才需執行"));
            showLogMessage("I", "", String.format("本程式在每日換日後執行, 營業日會多一天"));
            exceptExit = 0;
          //comcr.errRtn(String.format("本日[%s]  已執行日期[%s], 故不執行", 
          //hTempBusinessDate, hTempWeekSDate), "", hCallBatchSeqno);
            comc.errExit(String.format("本日[%s]  已執行日期[%s], 故不執行", 
            hTempBusinessDate, hTempWeekSDate), hCallBatchSeqno);
          } 
        }
***/

        deleteActJcicBal();
        commitDataBase();
        selectActJcicLog();

        showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));

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
        sqlCmd = "select decode( cast(? as varchar(8)) ,'',business_date, ? ) h_busi_business_date,";
        sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)) , '',business_date, ? ),'yyyymmdd')-1 days, 'yyyymmdd') h_temp_business_date,";
        sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)) , '',business_date, ? ),'yyyymmdd')+4 days- to_number(to_char(to_date(decode( cast(? as varchar(8)) , '', business_date, ? ),'yyyymmdd'),'D')) days,'yyyymmdd') h_temp_week_s_date,";
        sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)) , '',business_date, ? ),'yyyymmdd')+5 days- to_number(to_char(to_date(decode( cast(? as varchar(8)) , '', business_date, ? ),'yyyymmdd'),'D')) days,'yyyymmdd') h_temp_week_e_date,";
        sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)) , '',business_date, ? ),'yyyymmdd'),'D') h_temp_week_nowseq ";
        sqlCmd += " from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);
        setString(7, hBusiBusinessDate);
        setString(8, hBusiBusinessDate);
        setString(9, hBusiBusinessDate);
        setString(10, hBusiBusinessDate);
        setString(11, hBusiBusinessDate);
        setString(12, hBusiBusinessDate);
        setString(13, hBusiBusinessDate);
        setString(14, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hTempBusinessDate = getValue("h_temp_business_date");
            hTempWeekSDate = getValue("h_temp_week_s_date");
            hTempWeekEDate = getValue("h_temp_week_e_date");
            hTempWeekNowseq = getValue("h_temp_week_nowseq");
        }

    }

  /***********************************************************************/
  void getWeekNowSeq() throws Exception {
    sqlCmd  = "select decode( cast(? as varchar(8)) ,'',business_date, ?) "
            + " h_busi_business_date,";
    sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)),'',business_date, ?),"
            + " 'yyyymmdd')-1 days, 'yyyymmdd') h_temp_business_date,";
    sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)),'',business_date, ?),"
            + " 'yyyymmdd'),'D') h_temp_week_nowseq ";
    sqlCmd += " from ptr_businday ";
    setString(1, hBusiBusinessDate);
    setString(2, hBusiBusinessDate);
    setString(3, hBusiBusinessDate);
    setString(4, hBusiBusinessDate);
    setString(5, hBusiBusinessDate);
    setString(6, hBusiBusinessDate);
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
    }
    if (recordCnt > 0) {
        hBusiBusinessDate = getValue("h_busi_business_date");
        hTempBusinessDate = getValue("h_temp_business_date");
        hTempWeekNowseq = getValue("h_temp_week_nowseq");
    }

  }

  /***********************************************************************/
  void checkFriDate() throws Exception {

    if (hTempWeekNowseq.substring(0, 1).equals("1")) {
      hTempWeekEDate = comm.nextNDate(hBusiBusinessDate, 5);
    } else if (hTempWeekNowseq.substring(0, 1).equals("2")) {
      hTempWeekEDate = comm.nextNDate(hBusiBusinessDate, 4);
    } else if (hTempWeekNowseq.substring(0, 1).equals("3")) {
      hTempWeekEDate = comm.nextNDate(hBusiBusinessDate, 3);
    } else if (hTempWeekNowseq.substring(0, 1).equals("4")) {
      hTempWeekEDate = comm.nextNDate(hBusiBusinessDate, 2);
    } else if (hTempWeekNowseq.substring(0, 1).equals("5")) {
      hTempWeekEDate = comm.nextNDate(hBusiBusinessDate, 1);
    } else if (hTempWeekNowseq.substring(0, 1).equals("6")) {
      hTempWeekEDate = comm.nextNDate(hBusiBusinessDate, 0);
    } else if (hTempWeekNowseq.substring(0, 1).equals("7")) {
      hTempWeekEDate = comm.nextNDate(hBusiBusinessDate, 6);
    } 

    hTempWeekSDate = comm.nextNDate(hTempWeekEDate, -6);
    hTempWeek1Date = comm.nextNDate(hTempWeekEDate, -1);

    String tempDate = comcr.increaseDays(hTempWeek1Date, 1);
    if (tempDate.equals(hTempWeekEDate)) {
      hThisFriDate = hTempWeekEDate;
    } else {
      tempDate = comcr.increaseDays(hTempWeekEDate, -1);
      if (tempDate.compareTo(hTempWeekSDate)<0) {
        hThisFriDate = "";
      } else {
        hThisFriDate = tempDate;
      }
    }

  }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception { /* 0 : 表首日非cycle date */
        sqlCmd = "select stmt_cycle ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where stmt_cycle = substr(to_char(to_date(?, 'yyyymmdd'),'yyyymmdd'),7,2) ";
        setString(1, hTempWeekSDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void deleteActJcicBal() throws Exception {
        daoTable = "act_jcic_bal";
        whereStr = "where crt_date = ? ";
        setString(1, hBusiBusinessDate);
        deleteTable();

    }

    /***********************************************************************/
    void selectActJcicLog() throws Exception {

      fetchExtend = "ajlg.";
      daoTable = "act_jcic_log a,act_acct b,ptr_workday c,act_acno d";

      sqlCmd = "select ";
      sqlCmd += " a.p_seqno,";
      sqlCmd += " a.acct_type,";
      sqlCmd += " d.acct_key,";
      sqlCmd += " b.acct_jrnl_bal,";
      sqlCmd += " a.rowid rowid,";
      sqlCmd += " c.this_acct_month, ";
      sqlCmd += " c.this_close_date ";
      sqlCmd += " from act_jcic_log a,act_acct b,ptr_workday c,act_acno d ";
      sqlCmd += "where a.sale_date        = '' ";
      sqlCmd += "  and a.jcic_acct_status = '' ";
      sqlCmd += "  and decode(a.stop_flag,'','x',a.stop_flag) != '3' ";
      sqlCmd += "  and a.log_type         = 'A' ";
      sqlCmd += "  and (b.acct_jrnl_bal   > 0 ";
      sqlCmd += "       or (    b.acct_jrnl_bal <= 0 ";
      sqlCmd += "           and a.valid_cnt     > 0)) ";
      sqlCmd += "  and not exists (select h.p_seqno ";
      sqlCmd += " from act_jcic_end h ";
      sqlCmd += "where h.p_seqno = b.p_seqno ";
      sqlCmd += "  and h.send_flag in ('Y','U')) ";
      sqlCmd += "  and decode(a.acct_month,'','x',a.acct_month) = c.this_acct_month ";
      sqlCmd += "  and a.p_seqno    = b.p_seqno ";
      sqlCmd += "  and b.stmt_cycle = c.stmt_cycle ";
      sqlCmd += "  and d.acno_p_seqno = a.p_seqno ";
      sqlCmd += "  and d.acno_flag = '1' ";
      int cursorIndex = openCursor();
      while (fetchTable(cursorIndex)) {
          hAjlgPSeqno = getValue("ajlg.p_seqno");
          hAjlgAcctType = getValue("ajlg.acct_type");
          hAjlgAcctKey = getValue("ajlg.acct_key");
          hacctacctjrnlbal = getValueDouble("ajlg.acct_jrnl_bal");
          hAjlgRowid = getValue("ajlg.rowid");
          hWdayThisAcctMonth = getValue("ajlg.this_acct_month");
          hWdayThisCloseDate = getValue("ajlg.this_close_date");

          hAjlgUnpostInstFee = 0;
          hAjlgUnpostCardFee = 0;
          hAjlgUnpostInstStageFee = 0;

          if (hacctacctjrnlbal == 0) {
              selectCrdCard();
              if (hAjlgValidCnt == 0)
                  continue;
          }

          selectBilContract();
        //selectActComboMJrnl();

          insertActJcicBal();

          totalCnt++;
          if ((totalCnt % 5000) == 0) {
              showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
          }
      }
      closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {

        sqlCmd = "select decode(current_code,'0','', decode(sign(decode(oppost_date, '', '0', oppost_date) - decode(cast(? as varchar(8)), '', '0', cast(? as varchar(8))) ),1,'',oppost_date)) h_card_oppost_date,";
        sqlCmd += " decode(current_code,'0','0', decode(sign(decode(oppost_date, '', '0', oppost_date)- decode(cast(? as varchar(8)), '', '0', cast(? as varchar(8))) ),1,'0',current_code)) h_card_current_code ";
        sqlCmd += " from crd_card a  ";
        //sqlCmd += "where a.acno_p_seqno = ?  ";
        sqlCmd += "where a.p_seqno = ?  ";
        sqlCmd += "  and card_no  = major_card_no ORDER by decode(oppost_date,'','30001231',oppost_date) desc,issue_date desc ";
        setString(1, hWdayThisCloseDate);
        setString(2, hWdayThisCloseDate);
        setString(3, hWdayThisCloseDate);
        setString(4, hWdayThisCloseDate);
        setString(5, hAjlgPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMCardOppostDate[i] = getValue("h_card_oppost_date", i);
            hMCardCurrentCode[i] = getValue("h_card_current_code", i);
        }

        int ptr_bintable_cnt = recordCnt;

        hAjlgStopFlag = "0";
        hAjlgValidCnt = 0;
        hCardOppostDate = hMCardOppostDate[0];
        hCardCurrentCode = hMCardCurrentCode[0];

        if (hCardCurrentCode.substring(0, 1).equals("3"))
            hAjlgStopFlag = "3";

        for (int inta1 = 0; inta1 < ptr_bintable_cnt; inta1++) {
            if (hMCardCurrentCode[inta1].equals("0"))
                hAjlgValidCnt++;
        }

    }

    /***********************************************************************/
    void selectBilContract() throws Exception {
        hAjlgUnpostInstFee = 0;
        hAjlgUnpostCardFee = 0;
        hAjlgBilledEndBalRi = 0;
        hAjlgUnpostInstStageFee = 0; /* 增設：對帳單分期之分期總額 */

        sqlCmd = "select sum(decode(b.loan_flag, 'C',0,'Y',0, a.unit_price*(a.install_tot_term- a.install_curr_term)+a.remd_amt+decode(install_curr_term,0,first_remd_amt,0))) h_ajlg_unpost_inst_fee,";
        sqlCmd += " sum(decode(b.loan_flag, 'C',a.unit_price*(a.install_tot_term- a.install_curr_term)+a.remd_amt+decode(install_curr_term,0,first_remd_amt,0), 0)) h_ajlg_unpost_card_fee,";
        sqlCmd += " sum(decode(b.loan_flag, 'C',decode(a.install_curr_term,0,0,a.install_tot_term, a.unit_price+a.remd_amt,a.unit_price), 0)) h_ajlg_billed_end_bal_ri,";
        sqlCmd += " sum(decode(b.loan_flag, 'Y',a.unit_price*(a.install_tot_term- a.install_curr_term)+a.remd_amt+decode(install_curr_term,0,first_remd_amt,0), 0)) h_ajlg_unpost_inst_stage_fee ";
        sqlCmd += " from act_acno c , bil_contract a left join bil_merchant b ";
        sqlCmd += " on a.mcht_no = b.mcht_no ";
        sqlCmd += "where a.acct_type = ? ";
        sqlCmd += " and c.acct_key = ? and c.acno_p_seqno = a.p_seqno ";
        sqlCmd += " and a.install_tot_term != a.install_curr_term ";
        sqlCmd += " and a.contract_kind = '1' ";
      //sqlCmd += " and nvl(b.trans_flag,'N') != 'Y' ";
      //sqlCmd += " and a.auth_code NOT IN ('', 'N', 'REJECT', 'P', 'reject', 'LOAN') ";
      //sqlCmd += " and ( (a.post_cycle_dd > 0 OR a.installment_kind = 'F') ";
      //sqlCmd += " or ( a.post_cycle_dd = 0 ";
      //sqlCmd += " and a.DELV_CONFIRM_FLAG = 'Y' ";
      //sqlCmd += " and a.auth_code = 'DEBT')) ";
        setString(1, hAjlgAcctType);
        setString(2, hAjlgAcctKey);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAjlgUnpostInstFee = getValueDouble("h_ajlg_unpost_inst_fee");
            hAjlgUnpostCardFee = getValueDouble("h_ajlg_unpost_card_fee");
            hAjlgBilledEndBalRi = getValueDouble("h_ajlg_billed_end_bal_ri");
            hAjlgUnpostInstStageFee = getValueDouble("h_ajlg_unpost_inst_stage_fee");
        }

    }

    /***********************************************************************/
/***
    void selectActComboMJrnl() throws Exception {
        hAcmlCashUseBalance = 0;
        sqlCmd = "select sum(cash_use_balance) h_acml_cash_use_balance ";
        //sqlCmd += " from act_combo_jrnl a, act_acno c ";
        sqlCmd += " from act_combo_m_jrnl a, act_acno c ";
        sqlCmd += "where a.acct_type = ?  ";
        sqlCmd += "  and c.acct_key  = ? ";
        sqlCmd += "  and a.p_seqno   = c.acno_p_seqno ";
        setString(1, hAjlgAcctType);
        setString(2, hAjlgAcctKey);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcmlCashUseBalance = getValueDouble("h_acml_cash_use_balance");
        }

    }
***/
    /***********************************************************************/
    void insertActJcicBal() throws Exception {
        // sqlCmd = "insert into act_jcic_bal ";
        // sqlCmd += " (log_type,";
        // sqlCmd += " crt_date,";
        // sqlCmd += " acct_month,";
        // sqlCmd += " corp_id_p_seqno,";
        // sqlCmd += " acct_type,";
        // sqlCmd += " p_seqno,";
        // sqlCmd += " id_p_seqno,";
        // sqlCmd += " stmt_cycle,";
        // sqlCmd += " corp_no,";
        // sqlCmd += " bill_type_flag,";
        // sqlCmd += " stmt_this_ttl_amt,";
        // sqlCmd += " unpost_inst_fee,";
        // sqlCmd += " unpost_card_fee,";
        // sqlCmd += " mod_pgm,";
        // sqlCmd += " mod_time,";
        // sqlCmd += " unpost_inst_stage_fee)";
        // sqlCmd += " select ";
        // sqlCmd += " 'A',";
        // sqlCmd += " ?,";
        // sqlCmd += " ?,";
        // sqlCmd += " a.corp_id_p_seqno,";
        // sqlCmd += " a.acct_type,";
        // sqlCmd += " a.p_seqno,";
        // sqlCmd += " a.id_p_seqno,";
        // sqlCmd += " a.stmt_cycle,";
        // sqlCmd += " a.corp_no,";
        // sqlCmd += " a.bill_type_flag,";
        // sqlCmd += " ?,";
        // sqlCmd += " ?,";
        // sqlCmd += " ?,";
        // sqlCmd += " 'ActN022',";
        // sqlCmd += " sysdate,";
        // sqlCmd += " ? ";
        // sqlCmd += "from act_jcic_log a " + "where a.rowid = ? ";

        sqlCmd += " select ";
        sqlCmd += " a.corp_id_p_seqno,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " a.p_seqno,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " a.stmt_cycle,";
        sqlCmd += " a.corp_no,";
        sqlCmd += " a.bill_type_flag ";
        sqlCmd += "from act_jcic_log a where a.rowid =  ?  ";
        setRowId(1, hAjlgRowid);
        int recordCnt = selectTable();
      //for (int i = 0; i < recordCnt; i++) {
        if (recordCnt > 0) {
            String tmpCorpIdPSeqno = getValue("corp_id_p_seqno");
            String tmpAcctType = getValue("acct_type");
            String tmpPSeqno = getValue("p_seqno");
            String tmpIdPSeqno = getValue("id_p_seqno");
            String tmpStmtCycle = getValue("stmt_cycle");
            String tmpCorpNo = getValue("corp_no");
            String tmpBillTypeFlag = getValue("bill_type_flag");

            setValue("log_type", "A");
            setValue("crt_date", hBusiBusinessDate);
            setValue("acct_month", hWdayThisAcctMonth);
            setValue("corp_id_p_seqno", tmpCorpIdPSeqno);
            setValue("acct_type", tmpAcctType);
            setValue("p_seqno", tmpPSeqno);
            setValue("id_p_seqno", tmpIdPSeqno);
            setValue("stmt_cycle", tmpStmtCycle);
            setValue("corp_no", tmpCorpNo);
            setValue("bill_type_flag", tmpBillTypeFlag);
            setValueDouble("stmt_this_ttl_amt", hacctacctjrnlbal);
            setValueDouble("unpost_inst_fee", hAjlgUnpostInstFee);
          //setValueDouble("unpost_card_fee", hAjlgUnpostCardFee + hAcmlCashUseBalance);
            setValueDouble("unpost_card_fee", hAjlgUnpostCardFee);
            setValue("mod_pgm", javaProgram);
            setValue("mod_time", sysDate + sysTime);
            setValueDouble("unpost_inst_stage_fee", hAjlgUnpostInstStageFee);
            daoTable = "act_jcic_bal";
            insertTable();
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActN022 proc = new ActN022();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
