/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/01/01  V1.00.00    Edson     program initial                           *
 *  107/01/22  V1.00.01    Brian     error correction                          *
 *  109-12-04  V1.00.02  tanwei      updated for project coding standard       *
 *  110/09/30  V1.00.03  Yang Bo     update program code for business logic    *
 *  110/10/13  V1.00.04  Yang Bo     fix program issue                         *
 *  110/10/15  V1.00.04  Yang Bo     replace promote_emp_no=> introduce_emp_no *
 *  110/10/19  V1.00.05  Yang Bo     fix lack query param                      *
 *  110/10/25  V1.00.06  Yang Bo     update query condition                    *
 *  110/10/27  V1.00.07  Yang Bo     break up query statement                  *
 ******************************************************************************/

package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*員工信用卡招攬獎勵紀錄處理程式*/
public class MktB100 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private String progname = "員工信用卡招攬獎勵紀錄處理程式  109/12/04 V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";
    String hWdayStmtCycle = "";
    String hWdayThisAcctMonth = "";
    String hMifdProgramCode = "";
//    String hMifdExclObjFlag = "";
//    String hMifdExclPurFlag = "";
    String hMifdItemEnameBl = "";
    String hMifdItemEnameCa = "";
    String hMifdItemEnameIt = "";
    String hMifdItemEnameId = "";
    String hMifdItemEnameOt = "";
    String hMifdItemEnameAo = "";
    String hMifdApplyDateS = "";
    String hMifdApplyDateE = "";
    String hMifdDebutYearFlag = "";
    Integer hMifdDebutMonth1 = 0;
    String hMifdDebutSupFlag0 = "";
    String hMifdDebutSupFlag1 = "";
    String hMifdRewardBank = "";
    String hMifdExcludeBank = "";
    String hMifdExcludeFinance = "";
    String hMifdAcctTypeFlag = "";
    String hMifdGroupCodeFlag = "";
    String hMifdCardTypeFlag = "";
    String hMifdConsumeType = "";
    String hMifdConsumeFlag = "";
    Integer hMifdCurrMonth = 0;
    Integer hMifdNextMonth = 0;
    Double hMifdCurrAmt = 0.0;
    String hMifdCurrTotCond = "";
    Integer hMifdCurrTotCnt = 0;
    String hMifdDataType = "";
    String hMifdDataCode1 = "";
    String hBillAcctMonth = "";
    String hBillReferenceNo = "";
    Double hBillDestinationAmt = 0.0;
    Integer hBillDestinationCnt = 0;
    String hCardCardNo = "";
    String hCardIssueDate = "";
    String hEmplId = "";
    String hEmplEmployNo = "";
    // -- 條件核對所需參數
    String hCardSupFlag = "";
    String hCardIntroduceEmpNo = "";
    String hCrdEmpEmployNo = "";
    String hCrdEmpStatusId = "";
    String hCrdIdIdPSeqno = "";
    String hCrdIdIdNo = "";
    String hCardAcctType = "";
    String hCardGroupCode = "";
    String hCardCardType = "";

    int totalCnt = 0;
    int inta = 0;
    int tempTime = 1;
    String[] tempWday = { "03", "06", "09", "12", "15", "18", "21", "24", "27" };
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
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : MktB100 [[businessdate][acct_month]]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            // comcr.h_call_batch_seqno = h_call_batch_seqno;
            // comcr.h_call_r_program_code = javaProgram;

            // comcr.callbatch(0, 0, 0);

            tempTime = 1;
            hBusiBusinessDate = "";
            if ((args.length == 1) && (args[0].length() == 8)) {
                hBusiBusinessDate = args[0];
                tmpstr = String.format("%6.6s", hBusiBusinessDate);
                hWdayThisAcctMonth = tmpstr;
                tmpstr = String.format("%2.2s", hBusiBusinessDate.substring(6));
                hWdayStmtCycle = tmpstr;
            } else if ((args.length == 1) && (args[0].length() == 6)) {
                tempTime = 9;
            } else {
                selectPtrBusinday();

                if (selectPtrWorkday() != 0) {
                    exceptExit = 0;
                    comcr.errRtn(
                            String.format("Today[%s] is not cycle_date. It will not process ! ", hBusiBusinessDate),
                            "", hCallBatchSeqno);
                }
            }

            if (tempTime == 1) {
                showLogMessage("I", "", String.format("Processing acct_month[%s] stmt_cycle[%s]",
                        hWdayThisAcctMonth, hWdayStmtCycle));

                if (selectMktIntrFund() != 0) {
                    exceptExit = 0;
                    comcr.errRtn("select Mkt_Intr not found ! ", "", hCallBatchSeqno);
                }
            } else {
                for (inta = 0; inta < 9; inta++) {
                    tmpstr = String.format("%6.6s%2.2s", args[0], tempWday[inta]);
                    hBusiBusinessDate = tmpstr;
                    tmpstr = String.format("%6.6s", hBusiBusinessDate);
                    hWdayThisAcctMonth = tmpstr;
                    tmpstr = String.format("%2.2s", hBusiBusinessDate.substring(6));
                    hWdayStmtCycle = tmpstr;
                    showLogMessage("I", "", String.format("[%s]", hBusiBusinessDate));
                    showLogMessage("I", "", String.format("Processing acct_month[%s] stmt_cycle[%s]",
                            hWdayThisAcctMonth, hWdayStmtCycle));
                    if (selectMktIntrFund() != 0) {
                        exceptExit = 0;
                        comcr.errRtn("select Mkt_Intr not found ! ", "", hCallBatchSeqno);
                    }
                }
            }

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
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,?) h_busi_business_date ";
        sqlCmd += "  from ptr_businday ";
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
    int selectPtrWorkday() throws Exception {

        sqlCmd = "select stmt_cycle,";
        sqlCmd += " this_acct_month ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where this_close_date = to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayThisAcctMonth = getValue("this_acct_month");
            return (0);
        } else {
            return (1);
        }
    }

    /***********************************************************************/
    int selectMktIntrFund() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.program_code,";
        sqlCmd += " debut_year_flag,";
        sqlCmd += " debut_month1,";
        sqlCmd += " debut_sup_flag_0,";
        sqlCmd += " debut_sup_flag_1,";
        sqlCmd += " reward_bank,";
        sqlCmd += " exclude_bank,";
        sqlCmd += " exclude_finance,";
        sqlCmd += " acct_type_flag,";
        sqlCmd += " group_code_flag,";
        sqlCmd += " card_type_flag,";
        sqlCmd += " consume_type,";
        sqlCmd += " consume_flag,";
        sqlCmd += " curr_month,";
        sqlCmd += " next_month,";
        sqlCmd += " curr_amt,";
        sqlCmd += " curr_tot_cond,";
        sqlCmd += " curr_tot_cnt,";
        sqlCmd += " b.data_type,";
        sqlCmd += " b.data_code1,";
//        sqlCmd += " excl_obj_flag,";
//        sqlCmd += " excl_pur_flag,";
        sqlCmd += " item_ename_bl,";
        sqlCmd += " item_ename_ca,";
        sqlCmd += " item_ename_it,";
        sqlCmd += " item_ename_id,";
        sqlCmd += " item_ename_ot,";
        sqlCmd += " item_ename_ao,";
        sqlCmd += " a.apply_date_s,";
        sqlCmd += " decode(a.apply_date_e, '', '30001231', a.apply_date_e) apply_date_e ";
        sqlCmd += "from mkt_intr_fund a";
        sqlCmd += "  left join mkt_intr_dtl b on a.program_code = b.program_code ";
        sqlCmd += "where a.apr_flag = 'Y' ";
        sqlCmd += "  and a.reward_bank = 'Y' ";
        sqlCmd += "  and ? >= decode(a.apply_date_s, '', '20100101', a.apply_date_s) ";
        sqlCmd += "  and ? <= decode(a.apply_date_e, '', '30001231', a.apply_date_e) ";
//        sqlCmd += " to_char(add_months(to_date(decode(apply_date_e,'','30001231', apply_date_e),'yyyymmdd'),12),'yyyymmdd') ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        int cursorIndex = openCursor();
        int tableCol = 0;
        while (fetchTable(cursorIndex)) {
            hMifdProgramCode = getValue("program_code");
            hMifdDebutYearFlag = getValue("debut_year_flag");
            hMifdDebutMonth1 = getValueInt("debut_month1");
            hMifdDebutSupFlag0 = getValue("debut_sup_flag_0");
            hMifdDebutSupFlag1 = getValue("debut_sup_flag_1");
            hMifdRewardBank = getValue("reward_bank");
            hMifdExcludeBank = getValue("exclude_bank");
            hMifdExcludeFinance = getValue("exclude_finance");
            hMifdAcctTypeFlag = getValue("acct_type_flag");
            hMifdGroupCodeFlag = getValue("group_code_flag");
            hMifdCardTypeFlag = getValue("card_type_flag");
            hMifdConsumeType = getValue("consume_type");
            hMifdConsumeFlag = getValue("consume_flag");
            hMifdCurrMonth = getValueInt("curr_month");
            hMifdNextMonth = getValueInt("next_month");
            hMifdCurrAmt = getValueDouble("curr_amt");
            hMifdCurrTotCond= getValue("curr_tot_cond");
            hMifdCurrTotCnt= getValueInt("curr_tot_cnt");
//            hMifdExclObjFlag = getValue("excl_obj_flag");
//            hMifdExclPurFlag = getValue("excl_pur_flag");
            hMifdItemEnameBl = getValue("item_ename_bl");
            hMifdItemEnameCa = getValue("item_ename_ca");
            hMifdItemEnameIt = getValue("item_ename_it");
            hMifdItemEnameId = getValue("item_ename_id");
            hMifdItemEnameOt = getValue("item_ename_ot");
            hMifdItemEnameAo = getValue("item_ename_ao");
            hMifdApplyDateS = getValue("apply_date_s");
            hMifdApplyDateE = getValue("apply_date_e");
            hMifdDataType = getValue("data_type");
            hMifdDataCode1 = getValue("data_code1");

            showLogMessage("I", "", String.format("Fund_code=[%s] Processing....", hMifdProgramCode));
            totalCnt = 0;
            tableCol++;
            selectCrdCard();
            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));
        }
        closeCursor(cursorIndex);
        if (tableCol > 0) {
            return 0;
        } else {
            return -1;
        }
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {

        sqlCmd = "  select ";
        sqlCmd += "   c.reference_no, ";
        sqlCmd += "   decode(c.txn_code,'06',-1,'25',-1,'27',-1,'28',-1,'29',-1,1) * c.dest_amt h_bill_destination_amt, ";
        sqlCmd += "   case when c.txn_code in ('06', '25', '27', '28', '29') then -1 else 1 end h_bill_destination_cnt, ";
        sqlCmd += "   a.card_no, ";
        sqlCmd += "   a.issue_date, ";
        sqlCmd += "   b.id, ";
        sqlCmd += "   b.employ_no, ";
        sqlCmd += "   c.acct_month, ";
        sqlCmd += "   a.sup_flag, a.introduce_emp_no, b.status_id, i.id_p_seqno, i.id_no, ";
        sqlCmd += "   a.acct_type, a.group_code, a.card_type ";
        sqlCmd += " from crd_card a ";
        sqlCmd += "   inner join bil_bill c on a.card_no = c.card_no ";
        // 核卡日期為上個月 && current_code = '0' && old_card_no = ''
        sqlCmd += "     and a.issue_date <= to_char(last_day(to_date(?, 'yyyymmdd')), 'yyyymmdd') ";
        sqlCmd += "     and a.issue_date >= to_char(first_day(to_date(?, 'yyyymmdd')), 'yyyymmdd') ";
        sqlCmd += "     and a.current_code = '0' ";
        sqlCmd += "     and a.old_card_no = '' ";
        sqlCmd += "   left join crd_employee b on a.introduce_emp_no = b.employ_no ";
        sqlCmd += "   left join crd_idno i on a.id_p_seqno = i.id_p_seqno ";
        sqlCmd += " where 1 = 1 ";
        // 消費金額是否滿足條件
        sqlCmd += "   and ((c.dest_amt >= ? ";
        // 或消費筆數設定檔有效時, 消費筆數是否滿足條件
        sqlCmd += "           or ( ? = 'Y' ";
        sqlCmd += "              and decode(c.txn_code,'06',-1,'25',-1,'27',-1,'28',-1,'29',-1, 1) >= ? )) ";
        // If CONSUME_TYPE = 1, 核對是否為核卡後N個月內刷卡消費
        sqlCmd += "       and (( ? = '1' ";
        sqlCmd += "             and c.acct_month between substr(to_date(a.issue_date, 'yyyymmdd'), 1, 6) ";
        sqlCmd += "                              and substr(to_char(last_day(add_months(to_date(a.issue_date, 'yyyymmdd'), ?)), 'yyyymmdd'), 1, 6)) ";
        // If CONSUME_TYPE = 2, 核對是否為核卡年度至下一年度N個月內刷卡消費
        sqlCmd += "           or ( ? = '2' ";
        sqlCmd += "              and c.acct_month between substr(to_date(a.issue_date, 'yyyymmdd'), 1, 6) ";
        sqlCmd += "                               and (substr(a.issue_date, 1, 4) + 1)||?))) ";
        sqlCmd += "   and a.card_no in (select card_no ";
        sqlCmd += "                         from crd_card ";
        sqlCmd += "                     where card_no = decode(?,'1',major_id_p_seqno,'2',id_p_seqno,'3',card_no, 'x')) ";
        sqlCmd += "   and c.acct_month||c.stmt_cycle between ? and ? ";
        sqlCmd += "   and decode(c.acct_code, '', 'x', c.acct_code) in (decode(?,'Y','BL','xx'), ";
        sqlCmd += "                                                decode(?,'Y','CA','xx'), ";
        sqlCmd += "                                                decode(?,'Y','IT','xx'), ";
        sqlCmd += "                                                decode(?,'Y','ID','xx'), ";
        sqlCmd += "                                                decode(?,'Y','OT','xx'), ";
        sqlCmd += "                                                decode(?,'Y','AO','xx')) ";
        sqlCmd += "   and decode(c.stmt_cycle,'','x',c.stmt_cycle) = ? ";
        sqlCmd += "   and decode( c.rsk_type, '', 'x',  c.rsk_type) not in ('1','2','3') ";
        sqlCmd += "   and c.acct_month = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setDouble(3, hMifdCurrAmt);
        setString(4, hMifdCurrTotCond);
        setInt(5, hMifdCurrTotCnt);
        setString(6, hMifdConsumeFlag);
        setInt(7, hMifdCurrMonth);
        setString(8, hMifdConsumeFlag);
        setString(9, hMifdNextMonth.toString().length() == 2 ? hMifdNextMonth.toString() : "0" + hMifdNextMonth);
        setString(10, hMifdConsumeType);
        setString(11, hMifdApplyDateS);
        setString(12, hMifdApplyDateE);
        setString(13, hMifdItemEnameBl);
        setString(14, hMifdItemEnameCa);
        setString(15, hMifdItemEnameIt);
        setString(16, hMifdItemEnameId);
        setString(17, hMifdItemEnameOt);
        setString(18, hMifdItemEnameAo);
        setString(19, hWdayStmtCycle);
        setString(20, hWdayThisAcctMonth);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hBillAcctMonth = getValue("acct_month", i);
            hBillReferenceNo = getValue("reference_no", i);
            hBillDestinationAmt = getValueDouble("h_bill_destination_amt", i);
            hBillDestinationCnt = getValueInt("h_bill_destination_cnt", i);
            hCardCardNo = getValue("card_no", i);
            hCardIssueDate = getValue("issue_date", i);
            hEmplId = getValue("id", i);
            hEmplEmployNo = getValue("employ_no", i);
            hCardSupFlag = getValue("sup_flag", i);
            hCardIntroduceEmpNo = getValue("introduce_emp_no", i);
            hCrdEmpEmployNo = getValue("employ_no", i);
            hCrdEmpStatusId = getValue("status_id", i);
            hCrdIdIdPSeqno = getValue("id_p_seqno", i);
            hCrdIdIdNo = getValue("id_no", i);
            hCardAcctType = getValue("acct_type", i);
            hCardGroupCode = getValue("group_code", i);
            hCardCardType = getValue("card_type", i);

            // -- 根據MKT_INTR_FUND參數, 進行條件核對

            // DEBUT_YEAR_FLAG  -- 是否為新卡
            if ("1".equalsIgnoreCase(hMifdDebutYearFlag)) {
                // 不滿足條件則跳過此筆數據
                if (commDebutYearFlag1() == 0) {
                    continue;
                }
            } else if ("2".equalsIgnoreCase(hMifdDebutYearFlag)) {
                if (commDebutYearFlag2() == 0) {
                    continue;
                }
            }

            // DEBUT_SUP_FLAG_0
            if ("Y".equalsIgnoreCase(hMifdDebutSupFlag0)) {
                // SUP_FLAG == 1, 不滿足條件, 跳過此筆
                if ("1".equalsIgnoreCase(hCardSupFlag)) {
                    continue;
                }
            }

            // DEBUT_SUP_FLAG_1
            if ("Y".equalsIgnoreCase(hMifdDebutSupFlag1)) {
                // SUP_FLAG == 0, 不滿足條件, 跳過此筆
                if ("0".equalsIgnoreCase(hCardSupFlag)) {
                    continue;
                }
            }

            // REWARD_BANK
            if ("Y".equalsIgnoreCase(hMifdRewardBank)) {
                if (commRewardBank() == 0) {
                    continue;
                }
            }

            // EXCLUDE_BANK
            if ("Y".equalsIgnoreCase(hMifdExcludeBank)) {
                if (commExcludeBank() == 0) {
                    continue;
                }
            }

            // EXCLUDE_FINANCE
            if ("Y".equalsIgnoreCase(hMifdExcludeFinance)) {
                if (commExcludeFinance() == 0) {
                    continue;
                }
            }

            // ACCT_TYPE
            if (commAcctType() == 0) {
                continue;
            }

            // GROUP_CODE
            if (commGroupCode() == 0) {
                continue;
            }

            // CARD_TYPE
            if (commCardType() == 0) {
                continue;
            }

            totalCnt++;
            if ((totalCnt % 10000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
                commitDataBase();
            }
            insertMktIntrLog();
        }

        selectCrdCardForOld();
    }

    // --非上個月新卡(計算消費金額及消費筆數存入log表)
    void selectCrdCardForOld() throws Exception {
        sqlCmd = "  select ";
        sqlCmd += "   c.reference_no, ";
        sqlCmd += "   decode(c.txn_code,'06',-1,'25',-1,'27',-1,'28',-1,'29',-1,1) * c.dest_amt h_bill_destination_amt, ";
        sqlCmd += "   case when c.txn_code in ('06', '25', '27', '28', '29') then -1 else 1 end h_bill_destination_cnt, ";
        sqlCmd += "   a.card_no, ";
        sqlCmd += "   a.issue_date, ";
        sqlCmd += "   b.id, ";
        sqlCmd += "   b.employ_no, ";
        sqlCmd += "   c.acct_month, ";
        sqlCmd += "   a.sup_flag, a.introduce_emp_no, b.status_id, i.id_p_seqno, i.id_no, ";
        sqlCmd += "   a.acct_type, a.group_code, a.card_type" ;
        sqlCmd += " from mkt_intr_log m ";
        sqlCmd += "   inner join crd_card a on a.card_no = m.card_no ";
        // -- issue_date 非上個月新戶
        sqlCmd += "     and a.issue_date = m.issue_date ";
        sqlCmd += "     and (a.issue_date like substr(?, 1, 4)||'%' ";
        sqlCmd += "         or a.issue_date like (substr(?, 1, 4) - 1)||'%') ";
        sqlCmd += "     and a.issue_date <= to_char(last_day(add_months(to_date(?, 'yyyymmdd'), -1)), 'yyyymmdd') ";
        sqlCmd += "     and a.current_code = '0' ";
        sqlCmd += "     and a.old_card_no = '' ";
        sqlCmd += "   inner join bil_bill c on a.card_no = c.card_no ";
        sqlCmd += "   left join crd_employee b on a.introduce_emp_no = b.employ_no ";
        sqlCmd += "   left join crd_idno i on a.id_p_seqno = i.id_p_seqno ";
        sqlCmd += " where 1 = 1 ";
        sqlCmd += "   and ((c.dest_amt >= ? ";
        sqlCmd += "           or ( ? = 'Y' ";
        sqlCmd += "              and decode(c.txn_code,'06',-1,'25',-1,'27',-1,'28',-1,'29',-1, 1) >= ? )) ";
        sqlCmd += "       and (( ? = '1' ";
        sqlCmd += "             and c.acct_month between substr(to_date(a.issue_date, 'yyyymmdd'), 1, 6) ";
        sqlCmd += "                              and substr(to_char(last_day(add_months(to_date(a.issue_date, 'yyyymmdd'), ?)), 'yyyymmdd'), 1, 6)) ";
        sqlCmd += "           or ( ? = '2' ";
        sqlCmd += "              and c.acct_month between substr(to_date(a.issue_date, 'yyyymmdd'), 1, 6) ";
        sqlCmd += "                               and (substr(a.issue_date, 1, 4) + 1)||?))) ";
        sqlCmd += "   and a.card_no in (select card_no ";
        sqlCmd += "                         from crd_card ";
        sqlCmd += "                     where card_no = decode(?,'1',major_id_p_seqno,'2',id_p_seqno,'3',card_no, 'x')) ";
        sqlCmd += "   and c.acct_month||c.stmt_cycle between ? and ? ";
        sqlCmd += "   and decode(c.acct_code, '', 'x', c.acct_code) in (decode(?,'Y','BL','xx'), ";
        sqlCmd += "                                                decode(?,'Y','CA','xx'), ";
        sqlCmd += "                                                decode(?,'Y','IT','xx'), ";
        sqlCmd += "                                                decode(?,'Y','ID','xx'), ";
        sqlCmd += "                                                decode(?,'Y','OT','xx'), ";
        sqlCmd += "                                                decode(?,'Y','AO','xx')) ";
        sqlCmd += "   and decode(c.stmt_cycle,'','x',c.stmt_cycle) = ? ";
        sqlCmd += "   and decode( c.rsk_type, '', 'x',  c.rsk_type) not in ('1','2','3') ";
        sqlCmd += "   and c.acct_month = ? ";
        sqlCmd += "   and m.program_code = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setDouble(4, hMifdCurrAmt);
        setString(5, hMifdCurrTotCond);
        setInt(6, hMifdCurrTotCnt);
        setString(7, hMifdConsumeFlag);
        setInt(8, hMifdCurrMonth);
        setString(9, hMifdConsumeFlag);
        setString(10, hMifdNextMonth.toString().length() == 2 ? hMifdNextMonth.toString() : "0" + hMifdNextMonth);
        setString(11, hMifdConsumeType);
        setString(12, hMifdApplyDateS);
        setString(13, hMifdApplyDateE);
        setString(14, hMifdItemEnameBl);
        setString(15, hMifdItemEnameCa);
        setString(16, hMifdItemEnameIt);
        setString(17, hMifdItemEnameId);
        setString(18, hMifdItemEnameOt);
        setString(19, hMifdItemEnameAo);
        setString(20, hWdayStmtCycle);
        setString(21, hWdayThisAcctMonth);
        setString(22, hMifdProgramCode);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hBillAcctMonth = getValue("acct_month", i);
            hBillReferenceNo = getValue("reference_no", i);
            hBillDestinationAmt = getValueDouble("h_bill_destination_amt", i);
            hBillDestinationCnt = getValueInt("h_bill_destination_cnt", i);
            hCardCardNo = getValue("card_no", i);
            hCardIssueDate = getValue("issue_date", i);
            hEmplId = getValue("id", i);
            hEmplEmployNo = getValue("employ_no", i);
            hCardSupFlag = getValue("sup_flag", i);
            hCardIntroduceEmpNo = getValue("introduce_emp_no", i);
            hCrdEmpEmployNo = getValue("employ_no", i);
            hCrdEmpStatusId = getValue("status_id", i);
            hCrdIdIdPSeqno = getValue("id_p_seqno", i);
            hCrdIdIdNo = getValue("id_no", i);
            hCardAcctType = getValue("acct_type", i);
            hCardGroupCode = getValue("group_code", i);
            hCardCardType = getValue("card_type", i);

            // -- 根據MKT_INTR_FUND參數, 進行條件核對
            // -- 不檢查是否為全新卡及核卡日期

            // DEBUT_SUP_FLAG_0
            if ("Y".equalsIgnoreCase(hMifdDebutSupFlag0)) {
                // SUP_FLAG == 1, 不滿足條件, 跳過此筆
                if ("1".equalsIgnoreCase(hCardSupFlag)) {
                    continue;
                }
            }

            // DEBUT_SUP_FLAG_1
            if ("Y".equalsIgnoreCase(hMifdDebutSupFlag1)) {
                // SUP_FLAG == 0, 不滿足條件, 跳過此筆
                if ("0".equalsIgnoreCase(hCardSupFlag)) {
                    continue;
                }
            }

            // REWARD_BANK
            if ("Y".equalsIgnoreCase(hMifdRewardBank)) {
                if (commRewardBank() == 0) {
                    continue;
                }
            }

            // EXCLUDE_BANK
            if ("Y".equalsIgnoreCase(hMifdExcludeBank)) {
                if (commExcludeBank() == 0) {
                    continue;
                }
            }

            // EXCLUDE_FINANCE
            if ("Y".equalsIgnoreCase(hMifdExcludeFinance)) {
                if (commExcludeFinance() == 0) {
                    continue;
                }
            }

            // ACCT_TYPE
            if (commAcctType() == 0) {
                continue;
            }

            // GROUP_CODE
            if (commGroupCode() == 0) {
                continue;
            }

            // CARD_TYPE
            if (commCardType() == 0) {
                continue;
            }

            totalCnt++;
            if ((totalCnt % 10000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
                commitDataBase();
            }
            insertMktIntrLog();
        }
    }

    // 判斷參數條件：DEBUT_YEAR_FLAG == 1, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commDebutYearFlag1() throws Exception {
        sqlCmd = " select count(1) as crd_cnt from crd_card cc " +
                " where 1 = 1 " +
                " and cc.id_p_seqno = ? " +
                " and cc.issue_date between to_char(add_months(to_date(?, 'yyyymmdd'), '-'||?), 'yyyymmdd') " +
                " and to_char(add_days(to_date(?, 'yyyymmdd'), '-1'), 'yyyymmdd') ";
        setString(1, hCrdIdIdPSeqno);
        setString(2, hCardIssueDate);
        setString(3, hMifdDebutMonth1.toString());
        setString(4, hCardIssueDate);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            int count = getValueInt("crd_cnt", i);
            if (count > 0) {
                return 0;
            }
        }
        return 1;
    }

    // 判斷參數條件：DEBUT_YEAR_FLAG == 2, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commDebutYearFlag2() throws Exception {
        sqlCmd = " select count(1) as crd_cnt from crd_card cc " +
                " where cc.issue_date < ? " +
                " and cc.id_p_seqno = ? ";
        setString(1, hCardIssueDate);
        setString(2, hCrdIdIdPSeqno);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            int count = getValueInt("crd_cnt", i);
            if (count > 0) {
                return 0;
            }
        }
        return 1;
    }

    // 判斷參數條件：REWARD_BANK == Y, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commRewardBank() {
        if (hCardIntroduceEmpNo.equalsIgnoreCase(hCrdEmpEmployNo)) {
            if ("1".equalsIgnoreCase(hCrdEmpStatusId)
                    || "7".equalsIgnoreCase(hCrdEmpStatusId)) {
                return 1;
            }
        }
        return 0;
    }

    // 判斷參數條件：EXCLUDE_BANK == Y, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commExcludeBank() throws Exception {
        if (hCrdIdIdPSeqno == null) {
            return 0;
        }
        sqlCmd = " select count(1) from crd_idno i " +
                " inner join crd_employee e on e.id = ? " +
                " where i.id_p_seqno = ? and i.staff_flag = 'Y' ";
        setString(1, hCrdIdIdNo);
        setString(2, hCrdIdIdPSeqno);
        int recordCnt = selectTable();

        for (int i = 0; i < recordCnt; i++) {
            int count = getValueInt("crd_cnt", i);
            if (count > 0) {
                return 0;
            }
        }
        return 1;
    }

    // 判斷參數條件：EXCLUDE_FINANCE, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commExcludeFinance() throws Exception {
        if (hCrdIdIdPSeqno == null) {
            return 0;
        }
        sqlCmd = " select count(1) from crd_employee_a e where e.id = ? ";
        setString(1, hCrdIdIdNo);
        int recordCnt = selectTable();

        for (int i = 0; i < recordCnt; i++) {
            int count = getValueInt("crd_cnt", i);
            if (count > 0) {
                return 0;
            }
        }
        return 1;
    }

    // 判斷參數條件：ACCT_TYPE, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commAcctType() throws Exception {
        // IF ACCT_TYPE = 0 不檢查此筆
        if ("0".equalsIgnoreCase(hMifdAcctTypeFlag)) {
            return 1;
        } else {
            sqlCmd = "select data_code1 from mkt_intr_dtl m " +
                    " where m.program_code = ? and m.data_type = '01'";
            setString(1, hMifdProgramCode);
            int recordCnt = selectTable();

            // 查出符合參數要求的DATA_CODE, 並將所有結果存入數組
            List<String> dataCodes = new ArrayList<>(recordCnt);

            for (int i = 0; i < recordCnt; i++) {
                String dataCode1 = getValue("data_code1", i);
                dataCodes.add(dataCode1);
            }

            // IF ACCT_TYPE = 1 && ACCT_TYPE 存在於結果中，滿足條件
            if ("1".equalsIgnoreCase(hMifdAcctTypeFlag)) {
                if (dataCodes.contains(hCardAcctType)) {
                    return 1;
                }
            }
            // IF ACCT_TYPE = 2, ACCT_TYPE 不存在於結果中，滿足條件
            else if ("2".equalsIgnoreCase(hMifdAcctTypeFlag)) {
                if (!dataCodes.contains(hCardAcctType)) {
                    return 1;
                }
            }
        }

        return 0;
    }

    // 判斷參數條件：GROUP_CODE, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commGroupCode() throws Exception {
        // IF GROUP_CODE_FLAG = 0 不檢查此筆
        if ("0".equalsIgnoreCase(hMifdGroupCodeFlag)) {
            return 1;
        } else {
            sqlCmd = "select data_code1 from mkt_intr_dtl m " +
                    " where m.program_code = ? and m.data_type = '02'";
            setString(1, hMifdProgramCode);
            int recordCnt = selectTable();

            // 查出符合參數要求的DATA_CODE, 並將所有結果存入數組
            List<String> dataCodes = new ArrayList<>(recordCnt);

            for (int i = 0; i < recordCnt; i++) {
                String dataCode1 = getValue("data_code1", i);
                dataCodes.add(dataCode1);
            }

            // IF GROUP_CODE_FLAG = 1 && GROUP_CODE 存在於結果中，滿足條件
            if ("1".equalsIgnoreCase(hMifdGroupCodeFlag)) {
                if (dataCodes.contains(hCardGroupCode)) {
                    return 1;
                }
            }
            // IF GROUP_CODE_FLAG = 2, GROUP_CODE 不存在於結果中，滿足條件
            else if ("2".equalsIgnoreCase(hMifdGroupCodeFlag)) {
                if (!dataCodes.contains(hCardGroupCode)) {
                    return 1;
                }
            }
        }

        return 0;
    }

    // 判斷參數條件：CARD_TYPE, 返回值為 1 滿足條件, 為 0 不滿足條件
    int commCardType() throws Exception {
        // IF CARD_TYPE_FLAG = 0 不檢查此筆
        if ("0".equalsIgnoreCase(hMifdCardTypeFlag)) {
            return 1;
        } else {
            sqlCmd = "select data_code1 from mkt_intr_dtl m " +
                    " where m.program_code = ? and m.data_type = '03'";
            setString(1, hMifdProgramCode);
            int recordCnt = selectTable();

            // 查出符合參數要求的DATA_CODE, 並將所有結果存入數組
            List<String> dataCodes = new ArrayList<>(recordCnt);

            for (int i = 0; i < recordCnt; i++) {
                String dataCode1 = getValue("data_code1", i);
                dataCodes.add(dataCode1);
            }

            // IF CARD_TYPE_FLAG = 1 && CARD_TYPE 存在於結果中，滿足條件
            if ("1".equalsIgnoreCase(hMifdCardTypeFlag)) {
                if (dataCodes.contains(hCardCardType)) {
                    return 1;
                }
            }
            // IF CARD_TYPE_FLAG = 2, CARD_TYPE 不存在於結果中，滿足條件
            else if ("2".equalsIgnoreCase(hMifdCardTypeFlag)) {
                if (!dataCodes.contains(hCardCardType)) {
                    return 1;
                }
            }
        }

        return 0;
    }

    /***********************************************************************/
    void insertMktIntrLog() throws Exception {
        setValue("program_code", hMifdProgramCode);
        setValue("acct_month", hBillAcctMonth);
        setValue("vd_flag", "N");
        setValue("ref_no", hBillReferenceNo);
        setValueDouble("dest_amt", hBillDestinationAmt);
        setValueDouble("dest_cnt", hBillDestinationCnt);
        setValue("card_no", hCardCardNo);
        setValue("issue_date", hCardIssueDate);
        setValue("employ_id", hEmplId);
        setValue("employ_no", hEmplEmployNo);
        setValue("crt_date", hBusiBusinessDate);
        setValue("crt_time", sysTime);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "mkt_intr_log";
        insertTable();

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktB100 proc = new MktB100();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
