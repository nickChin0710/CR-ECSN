/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/28  V1.00.01    SUP       error correction                         
 *  111-10-13  V1.00.02    Machao    sync from mega & updated for project coding standard * *
 ******************************************************************************/

package Act;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*沖帳還原批次處理程式*/
public class ActC003 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private final String PROGNAME = "沖帳還原批次處理程式  111-10-13  V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActC003";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";
    String buf = "";

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "ACTC003";
    String prgName1 = "系統自動Payment reversal失敗報表";
    int rptSeq1 = 0;
    String szTmp = "";

    String hBusiBusinessDate = "";
    String hApreCurrCode = "";
    String hAprePSeqno = "";
    String hApreAcctType = "";
    String hApreAcctKey = "";
    String hApreAcctDate = "";
    String hApreAdjReasonCode = "";
    String hApreAdjComment = "";
    String hApreCDebtKey = "";
    String hApreCrItem = "";
    String hApreJobCode = "";
    String hApreVouchJobCode = "";
    double hApreTransactionAmt = 0;
    double hApreDcTransactionAmt = 0;
    double hApreThisReversalAmt = 0;
    String hApreJrnlSeqno = "";
    String hApreAprDate = "";
    String hApreCrtUser = "";
    String hAcnoCardIndicator = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoIdPSeqno = "";
    String hApreRowid = "";
    String hNrnlReferenceNo = "";
    double hJrnlTransactionAmt = 0;
    double hJrnlDcTransactionAmt = 0;
    String hJrnlRowid = "";
    String hAcajPostDate = "";
    double hAcajDcOrginalAmt = 0;
    double hAcajOrginalAmt = 0;
    double hAcajDcBefAmt = 0;
    double hAcajBefAmt = 0;
    double hAcajDcBefDAmt = 0;
    double hAcajBefDAmt = 0;
    double hAcajDcAftDAmt = 0;
    double hAcajAftDAmt = 0;
    String hAcajAcctCode = "";
    int hAcajCount = 0;
    double hTempBefAmt = 0;
    double hTempDcBefAmt = 0;
    double hTempRevAmt = 0;
    double hAcajDcCrAmt = 0;
    double hAcajCrAmt = 0;
    String hPrintName = "";
    String hRptName = "";
    String hIdnoChiName = "";
    String hIdnoId = "";
    String hIdnoBusinessCode = "";
    String hCorpCorpNo = "";
    String hCorpBusinessCode = "";
    int hJrnlEnqSeqno = 0;
    String hPcceCurrChiName = "";
    double hDebtDcBegBal = 0;
    double hDehtDcBegBal = 0;
    int hJrnlOrderSeq = 0;
    int hApreEnqSeqno = 0;
    String hCorpChiName = "";
    String hErrProcMark = "";

    String dispDate = "";
    int errorCount = 0;
    int noneCount = 0;
    int procCount = 0;
    int pageCnt = 0;
    int totalCnt = 0;
  //int page_line = 0;
    int pageLineCnt = 0;
    double pageAmt = 0;
    double pageAmt1 = 0;
    double totalAmt = 0;
    double totalAmt1 = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : ActC003 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            selectPtrBusinday();

            String temstr = String.format("%s/reports/ACT_C003_%s", comc.getECSHOME(), hBusiBusinessDate);
            showLogMessage("I", "", String.format("File_name[%s]", temstr));
            selectActPayRev();
            if (totalCnt > 0) {
              if (pageLineCnt > 33) { /* pdf橫印每頁列印35行，此表 footer 佔2行 */
                 lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", "##PPP"));//跳頁
                 printHeader();
                 printFooter();
              } else {
                 printFooter();
              }
            }

            comc.writeReport(temstr, lpar1);
            comcr.insertPtrBatchRpt(lpar1);

            showLogMessage("I", "",
                    String.format("處理 [%d] 筆, 錯誤 [%d] 筆,不處理 [%d] 筆", procCount, errorCount, noneCount));

            if ((args.length == 0) && (totalCnt > 0))
                comcr.lpRtn("ACT_C003", hBusiBusinessDate);

            deleteActPayRev();

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
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

    /***********************************************************************/
    void selectActPayRev() throws Exception {

        sqlCmd = "select decode(curr_code,'','901',curr_code) h_apre_curr_code,";
        sqlCmd += " p_seqno ";
        sqlCmd += "  from act_pay_rev ";
        sqlCmd += " where decode(apr_flag ,'','N',apr_flag ) = 'Y' ";
        sqlCmd += "   and apr_user != '' ";
        sqlCmd += "   and decode(proc_mark,'','N',proc_mark) = 'N' ";
        sqlCmd += "   and this_reversal_amt > 0 ";
        sqlCmd += " group by p_seqno, decode(curr_code,'','901',curr_code) ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hApreCurrCode = getValue("h_apre_curr_code");
            hAprePSeqno = getValue("p_seqno");

          //if (select_act_acaj() != 0) {
          //    none_count++;
          //    continue;
          //}

          //page_line_cnt = 0;
            selectActPayRev1();
            procCount++;
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    int selectActAcaj() throws Exception {

        sqlCmd = "select count(*) h_acaj_count ";
        sqlCmd += "  from act_acaj  ";
        sqlCmd += " where p_seqno  = ?  ";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = ? ";
        setString(1, hAprePSeqno);
        setString(2, hApreCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acaj not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcajCount = getValueInt("h_acaj_count");
        }

        return (hAcajCount);
    }

    /***********************************************************************/
    void selectActPayRev1() throws Exception {

        sqlCmd = "select a.acct_type,";
        sqlCmd += " b.acct_key,";
        sqlCmd += " a.enq_seqno,";
        sqlCmd += " a.acct_date,";
        sqlCmd += " a.adj_reason_code,";
        sqlCmd += " a.adj_comment,";
        sqlCmd += " a.c_debt_key,";
        sqlCmd += " a.cr_item,";
        sqlCmd += " a.job_code,";
        sqlCmd += " a.vouch_job_code,";
        sqlCmd += " a.transaction_amt,";
        sqlCmd += " a.dc_transaction_amt,";
        sqlCmd += " a.this_reversal_amt,";
        sqlCmd += " a.jrnl_seqno,";
        sqlCmd += " a.apr_date,";
        sqlCmd += " a.crt_user,";
        sqlCmd += " b.card_indicator,";
        sqlCmd += " b.corp_p_seqno,";
        sqlCmd += " b.id_p_seqno,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "  from act_acno b,act_pay_rev a ";
        sqlCmd += " where a.p_seqno = ? ";
        sqlCmd += "   and decode(a.curr_code,'','901',a.curr_code) = ? ";
        sqlCmd += "   and decode(a.apr_flag,'','N',a.apr_flag) = 'Y' ";
        sqlCmd += "   and a.apr_user != '' ";
        sqlCmd += "   and a.p_seqno   = b.acno_p_seqno ";
        sqlCmd += "   and decode(a.proc_mark,'','N',a.proc_mark) = 'N' ";
        sqlCmd += " order by b.card_indicator, ";
        sqlCmd += "    a.acct_type, ";
        sqlCmd += "    a.crt_date, "; // create_date
        sqlCmd += "    a.crt_time"; // create_time
        setString(1, hAprePSeqno);
        setString(2, hApreCurrCode);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hApreAcctType = getValue("acct_type", i);
            hApreAcctKey = getValue("acct_key", i);
            hApreEnqSeqno = getValueInt("enq_seqno", i);
            hApreAcctDate = getValue("acct_date", i);
            hApreAdjReasonCode = getValue("adj_reason_code", i);
            hApreAdjComment = getValue("adj_comment", i);
            hApreCDebtKey = getValue("c_debt_key", i);
            hApreCrItem = getValue("cr_item", i);
            hApreJobCode = getValue("job_code", i);
            hApreVouchJobCode = getValue("vouch_job_code", i);
            hApreTransactionAmt = getValueDouble("transaction_amt", i);
            hApreDcTransactionAmt = getValueDouble("dc_transaction_amt", i);
            hApreThisReversalAmt = getValueDouble("this_reversal_amt", i);
            hApreJrnlSeqno = getValue("jrnl_seqno", i);
            hApreAprDate = getValue("apr_date", i);
            hApreCrtUser = getValue("crt_user", i);
            hAcnoCardIndicator = getValue("card_indicator", i);
            hAcnoCorpPSeqno = getValue("corp_p_seqno", i);
            hAcnoIdPSeqno = getValue("id_p_seqno", i);
            hApreRowid = getValue("rowid", i);

            checkActDebt();
            checkActDebtHst();

            if (selectActAcaj() != 0) {
                hErrProcMark = "1";
                noneCount++;
                if (pageLineCnt == 0) {
                    printHeader();
                    pageLineCnt = 5;
                }

                if (hAcnoIdPSeqno.length() != 0) {
                    selectCrdIdno();
                } else {
                    selectCrdCorp();
                }

                pageLineCnt++;
    
                if (pageLineCnt >= 36) {
                  //print_footer();
                    lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", "##PPP"));//跳頁
                    printHeader();
                    pageLineCnt = 6;
                }
                printDetail();

                updateActPayRev1(hErrProcMark);
                continue;
            }

            if (hApreThisReversalAmt > (hDebtDcBegBal + hDehtDcBegBal)) {
                hErrProcMark = "2";
                errorCount++;
                if (pageLineCnt == 0) {
                    printHeader();
                    pageLineCnt = 5;
                }

                if (hAcnoIdPSeqno.length() != 0) {
                    selectCrdIdno();
                } else {
                    selectCrdCorp();
                }

                pageLineCnt++;
                if (pageLineCnt >= 36) {
                  //print_footer();
                    lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", "##PPP"));//跳頁
                    printHeader();
                    pageLineCnt = 6;
                }
                printDetail();

                updateActPayRev1(hErrProcMark);
                continue;
            }

            hTempRevAmt = 0;
            selectActJrnlMax();

            selectActJrnl();
            updateActJrnl();
            updateActPayRev();
        }

    }

    /***********************************************************************/
    void checkActDebt() throws Exception {

        hDebtDcBegBal = 0;

        sqlCmd = "select sum(decode(" + "decode(a.curr_code, '','901',a.curr_code),'901', "
                + "decode(sign(beg_bal - end_bal - b.transaction_amt)," + "-1, beg_bal - end_bal,b.transaction_amt), "
                + "decode(sign(dc_beg_bal - dc_end_bal - b.dc_transaction_amt),"
                + "-1, dc_beg_bal - dc_end_bal,b.dc_transaction_amt)" + ")) h_debt_dc_beg_bal ";
        sqlCmd += "  from act_debt a,act_jrnl b  ";
        sqlCmd += " where b.acct_type   = ?  ";
        sqlCmd += "   and b.p_seqno = ?  ";
        sqlCmd += "   and a.reference_no = b.reference_no  ";
        sqlCmd += "   and decode(b.jrnl_seqno,'','x',b.jrnl_seqno) = ?  ";
        sqlCmd += "   and b.tran_class  = 'D' ";
        setString(1, hApreAcctType);
        setString(2, hAprePSeqno);
        setString(3, hApreJrnlSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDebtDcBegBal = getValueDouble("h_debt_dc_beg_bal");
        }

    }

    /***********************************************************************/
    void checkActDebtHst() throws Exception {

        hDehtDcBegBal = 0;

        sqlCmd = "select sum(decode(decode(a.curr_code, '','901',a.curr_code),'901'"
                + ", decode(sign(beg_bal - end_bal - b.transaction_amt),-1, beg_bal - end_bal,b.transaction_amt)"
                + ", decode(sign(dc_beg_bal - dc_end_bal - b.dc_transaction_amt),-1, dc_beg_bal - dc_end_bal,b.dc_transaction_amt))) h_deht_dc_beg_bal ";
        sqlCmd += "  from act_debt_hst a,act_jrnl b  ";
        sqlCmd += " where b.acct_type    = ?  ";
        sqlCmd += "   and b.p_seqno      = ?  "; 
        sqlCmd += "   and a.reference_no = b.reference_no  ";
        sqlCmd += "   and decode(b.jrnl_seqno,'','x',b.jrnl_seqno) = ?  ";
        sqlCmd += "   and b.tran_class   = 'D' ";
        setString(1, hApreAcctType);
        setString(2, hAprePSeqno);
        setString(3, hApreJrnlSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDehtDcBegBal = getValueDouble("h_deht_dc_beg_bal");
        }

    }

    /***********************************************************************/
    void printHeader() throws Exception {
        pageCnt++;
      //if (page_line==1) 
      //    lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", "##PPP"));

        buf = "";
        buf = comcr.insertStr(buf, "ACT_C003", 1);
        szTmp = comcr.bankName;
        buf = comcr.insertStrCenter(buf, szTmp, 132);
        buf = comcr.insertStr(buf, "列印表日 :", 110);
        dispDate = comc.convDates(sysDate, 1);
        buf = comcr.insertStr(buf, dispDate, 121);
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStrCenter(buf, "系統自動Payment reversal失敗報表", 132);
        buf = comcr.insertStr(buf, "列印頁數 :", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 125);
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        selectPtrCurrcode();
        buf = "";
        buf = comcr.insertStr(buf, "幣        別 :", 10);
        szTmp = String.format("%s", hPcceCurrChiName);
        buf = comcr.insertStr(buf, szTmp, 25);
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, " 放行日期 ", 1);
        buf = comcr.insertStr(buf, "帳戶帳號 ", 15);
        buf = comcr.insertStr(buf, "中文姓名/公司名稱", 30);
        buf = comcr.insertStr(buf, "擬沖回金額", 56);
        buf = comcr.insertStr(buf, "可沖回金額", 71);
        buf = comcr.insertStr(buf, "貸方科目", 86);
        buf = comcr.insertStr(buf, "銷帳鍵值", 96);
        buf = comcr.insertStr(buf, "登錄人員", 118);
        buf = comcr.insertStr(buf, "失敗原因", 126);
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        for (int i = 0; i < 136; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void selectPtrCurrcode() throws Exception {
        hPcceCurrChiName = "";
        sqlCmd = "select curr_chi_name ";
        sqlCmd += "  from ptr_currcode  ";
        sqlCmd += " where curr_code = ? ";
        setString(1, hApreCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_currcode not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPcceCurrChiName = getValue("curr_chi_name");
        }

    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoChiName = "";
        hIdnoId = "";
        hIdnoBusinessCode = "";

        sqlCmd = "select chi_name,";
        sqlCmd += " id_no,"; // id
        sqlCmd += " business_code ";
        sqlCmd += "  from crd_idno  ";
        sqlCmd += " where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("chi_name");
            hIdnoId = getValue("id_no"); // id
            hIdnoBusinessCode = getValue("business_code");
        }

    }

    /***********************************************************************/
    void selectCrdCorp() throws Exception {
        hCorpCorpNo = "";
        hCorpChiName = "";
        hCorpBusinessCode = "";

        sqlCmd = "select chi_name,";
        sqlCmd += " corp_no,";
        sqlCmd += " business_code ";
        sqlCmd += "  from crd_corp  ";
        sqlCmd += " where corp_p_seqno = ? ";
        setString(1, hAcnoCorpPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("chi_name");
            hCorpCorpNo = getValue("corp_no");
            hCorpBusinessCode = getValue("business_code");
        }

    }

    /***********************************************************************/
    void printDetail() throws Exception {

        buf = "";

        szTmp = String.format("%4.4s", hApreAprDate);
        szTmp = String.format("%03d/%2.2s/%2.2s", comcr.str2long(szTmp) - 1911, hApreAprDate.substring(4),
                hApreAprDate.substring(6));
        buf = comcr.insertStr(buf, szTmp, 2);

        szTmp = String.format("%2.2s-%s", hApreAcctType, hApreAcctKey);
        buf = comcr.insertStr(buf, szTmp, 15);
        buf = comcr.insertStr(buf, hIdnoChiName, 30);

        szTmp = String.format("%10s", String.format("$%,.0f", hApreThisReversalAmt));
        buf = comcr.insertStr(buf, szTmp, 56);
        szTmp = String.format("%10s", String.format("$%,.0f", hDebtDcBegBal + hDehtDcBegBal));
        buf = comcr.insertStr(buf, szTmp, 71);

        buf = comcr.insertStr(buf, hApreCrItem, 86);
        buf = comcr.insertStr(buf, hApreCDebtKey, 96);
        buf = comcr.insertStr(buf, hApreCrtUser, 118);
        if  (hErrProcMark.equals("1"))  {
             buf = comcr.insertStr(buf, "有其他調整", 126);
        }
        
        if  (hErrProcMark.equals("2"))  {
             buf = comcr.insertStr(buf, "擬沖回金額大於可沖回金額", 126);
        }
        
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

      //page_cnt++;
        totalCnt++;

        pageAmt = pageAmt + hApreThisReversalAmt;
        pageAmt1 = pageAmt1 + hDebtDcBegBal + hDehtDcBegBal;

        totalAmt = totalAmt + hApreThisReversalAmt;
        totalAmt1 = totalAmt1 + hDebtDcBegBal + hDehtDcBegBal;
    }

    /***********************************************************************/
    void printFooter() throws Exception {
        buf = "";
        for (int i = 0; i < 136; i++)
            buf += "=";
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "筆數:", 2);
        szTmp = String.format("%6d", totalCnt);
        buf = comcr.insertStr(buf, szTmp, 10);
      //szTmp = comcr.commFormat("2$,3$,3$,3$", page_amt);
        szTmp = String.format("%10s", String.format("$%,.0f", pageAmt));
        buf = comcr.insertStr(buf, szTmp, 56);
      //szTmp = comcr.commFormat("2$,3$,3$,3$", page_amt1);
        szTmp = String.format("%10s", String.format("$%,.0f", pageAmt1));
        buf = comcr.insertStr(buf, szTmp, 71);
        lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));
      //buf = "##PPP";
      //lpar1.add(comcr.putReport(rptName1, prgName1, sysDate, ++rptSeq1, "0", buf));
        pageCnt = 0;
        pageAmt = 0;
    }

    /***********************************************************************/
    void selectActJrnlMax() throws Exception {

        sqlCmd = "select enq_seqno,";
        sqlCmd += " order_seq ";
        sqlCmd += "  from act_jrnl  ";
        sqlCmd += " where acct_type  = ? ";
        sqlCmd += "   and p_seqno   = ? ";
        sqlCmd += "   and decode(jrnl_seqno,'','x',jrnl_seqno) = ? ";
        sqlCmd += "   and tran_class = 'P' ";
        sqlCmd += " order by order_seq ";
        setString(1, hApreAcctType);
        setString(2, hAprePSeqno);
        setString(3, hApreJrnlSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hJrnlEnqSeqno = getValueInt("enq_seqno", i);
            hJrnlOrderSeq = getValueInt("order_seq", i);

            if (hApreEnqSeqno == hJrnlEnqSeqno)
                break;

            if (i == recordCnt - 1) {
                hJrnlOrderSeq = 999;
            }

        }
    }

    /***********************************************************************/
    void selectActJrnl() throws Exception {
        double tempCmpAmt;
        double tempDcCmpAmt;

        sqlCmd = "select reference_no,";
        sqlCmd += " transaction_amt,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',transaction_amt,dc_transaction_amt) h_jrnl_dc_transaction_amt,";
        sqlCmd += " rowid as rowid ";
        sqlCmd += "  from act_jrnl ";
        sqlCmd += " where acct_type  = ? ";
        sqlCmd += "   and p_seqno = ? "; 
        sqlCmd += "   and decode(jrnl_seqno,'','x',jrnl_seqno) = ? ";
        sqlCmd += "   and tran_class = 'D' ";
        sqlCmd += "   and order_seq  < ? ";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = ? ";
        sqlCmd += " order by order_seq desc ";
        setString(1, hApreAcctType);
        setString(2, hAprePSeqno);
        setString(3, hApreJrnlSeqno);
        setInt(4, hJrnlOrderSeq);
        setString(5, hApreCurrCode);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hNrnlReferenceNo = getValue("reference_no", i);
            hJrnlTransactionAmt = getValueDouble("transaction_amt", i);
            hJrnlDcTransactionAmt = getValueDouble("h_jrnl_dc_transaction_amt", i);
            hJrnlRowid = getValue("rowid", i);

            if (selectActDebt() != 0)
                selectActDebtHst();
            selectActAcaj1();
            hAcajDcBefAmt = hAcajDcBefAmt + hTempDcBefAmt;
            hAcajBefAmt = hAcajBefAmt + hTempBefAmt;

            if (hAcajDcOrginalAmt <= hAcajDcBefAmt)
                continue;

            if ((hAcajDcOrginalAmt - hAcajDcBefAmt) <= hJrnlDcTransactionAmt) {
                tempDcCmpAmt = hAcajDcOrginalAmt - hAcajDcBefAmt;
                tempCmpAmt = hAcajOrginalAmt - hAcajBefAmt;
            } else {
                tempDcCmpAmt = hJrnlDcTransactionAmt;
                tempCmpAmt = hJrnlTransactionAmt;
            }

            if ((hApreThisReversalAmt - tempDcCmpAmt) < 0) {
                hAcajDcCrAmt = hApreThisReversalAmt;
                hAcajCrAmt = comcr.commCurrAmt("901",
                        hApreThisReversalAmt * hJrnlTransactionAmt / hJrnlDcTransactionAmt, 0);

            } else {
                hAcajDcCrAmt = tempDcCmpAmt;
                hAcajCrAmt = tempCmpAmt;
            }
            if (hAcajDcCrAmt <= 0)
                continue;

            if (hApreCurrCode.equals("901"))
                hAcajCrAmt = hAcajDcCrAmt;

            if ((hAcajDcCrAmt + hAcajDcBefAmt > hAcajDcAftDAmt)
                    || (hAcajCrAmt + hAcajBefAmt > hAcajAftDAmt))
                continue;

            insertActAcaj();
            hTempRevAmt = hTempRevAmt + hAcajDcCrAmt;
            if ((hApreThisReversalAmt - tempDcCmpAmt) <= 0) {
                break;
            }
            hApreThisReversalAmt = hApreThisReversalAmt - tempDcCmpAmt;
        }

    }

    /***********************************************************************/
    int selectActDebt() throws Exception {
        hAcajPostDate = "";
        hAcajDcOrginalAmt = 0;
        hAcajOrginalAmt = 0;
        hAcajDcBefAmt = 0;
        hAcajBefAmt = 0;
        hAcajDcBefDAmt = 0;
        hAcajBefDAmt = 0;
        hAcajDcAftDAmt = 0;
        hAcajAftDAmt = 0;
        hAcajAcctCode = "";

        sqlCmd = "select POST_DATE,"; // item_post_date
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',beg_bal     ,dc_beg_bal        ) h_acaj_dc_orginal_amt,";
        sqlCmd += " beg_bal,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',end_bal     ,dc_end_bal        ) h_acaj_dc_bef_amt,";
        sqlCmd += " end_bal,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',D_AVAIL_BAL ,DC_D_AVAIL_BAL) h_acaj_dc_bef_d_amt,";
        sqlCmd += " D_AVAIL_BAL,"; // d_available_bal
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',D_AVAIL_BAL ,DC_D_AVAIL_BAL) h_acaj_dc_aft_d_amt,";
        sqlCmd += " D_AVAIL_BAL,"; // d_available_bal
        sqlCmd += " ACCT_CODE"; // acct_code
        sqlCmd += "  from act_debt  ";
        sqlCmd += " where reference_no = ? "; // reference_seq
        setString(1, hNrnlReferenceNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcajPostDate = getValue("POST_DATE"); // item_post_date
            hAcajDcOrginalAmt = getValueDouble("h_acaj_dc_orginal_amt");
            hAcajOrginalAmt = getValueDouble("beg_bal");
            hAcajDcBefAmt = getValueDouble("h_acaj_dc_bef_amt");
            hAcajBefAmt = getValueDouble("end_bal");
            hAcajDcBefDAmt = getValueDouble("h_acaj_dc_bef_d_amt");
            hAcajBefDAmt = getValueDouble("D_AVAIL_BAL"); // d_available_bal
            hAcajDcAftDAmt = getValueDouble("h_acaj_dc_aft_d_amt");
            hAcajAftDAmt = getValueDouble("D_AVAIL_BAL"); // d_available_bal
            hAcajAcctCode = getValue("ACCT_CODE"); // acct_code
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    int selectActDebtHst() throws Exception {
        hAcajPostDate = "";
        hAcajDcOrginalAmt = 0;
        hAcajOrginalAmt = 0;
        hAcajDcBefAmt = 0;
        hAcajBefAmt = 0;
        hAcajDcBefDAmt = 0;
        hAcajBefDAmt = 0;
        hAcajDcAftDAmt = 0;
        hAcajAftDAmt = 0;
        hAcajAcctCode = "";

        sqlCmd = "select POST_DATE,"; // item_post_date
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',beg_bal    ,dc_beg_bal    ) h_acaj_dc_orginal_amt,";
        sqlCmd += " beg_bal,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',end_bal    ,dc_end_bal    ) h_acaj_dc_bef_amt,";
        sqlCmd += " end_bal,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',D_AVAIL_BAL,DC_D_AVAIL_BAL) h_acaj_dc_bef_d_amt,";
        sqlCmd += " D_AVAIL_BAL,"; // d_available_bal
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',D_AVAIL_BAL,DC_D_AVAIL_BAL) h_acaj_dc_aft_d_amt,";
        sqlCmd += " D_AVAIL_BAL,"; // d_available_bal
        sqlCmd += " ACCT_CODE"; // acct_code
        sqlCmd += "  from act_debt_hst  ";
        sqlCmd += " where reference_no= ? "; // reference_seq
        setString(1, hNrnlReferenceNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcajPostDate = getValue("POST_DATE"); // item_post_date
            hAcajDcOrginalAmt = getValueDouble("h_acaj_dc_orginal_amt");
            hAcajOrginalAmt = getValueDouble("beg_bal");
            hAcajDcBefAmt = getValueDouble("h_acaj_dc_bef_amt");
            hAcajBefAmt = getValueDouble("end_bal");
            hAcajDcBefDAmt = getValueDouble("h_acaj_dc_bef_d_amt");
            hAcajBefDAmt = getValueDouble("D_AVAIL_BAL"); // d_available_bal
            hAcajDcAftDAmt = getValueDouble("h_acaj_dc_aft_d_amt");
            hAcajAftDAmt = getValueDouble("D_AVAIL_BAL"); // d_available_bal
            hAcajAcctCode = getValue("ACCT_CODE"); // acct_code
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void selectActAcaj1() throws Exception {
        hTempDcBefAmt = 0;
        hTempBefAmt = 0;

        sqlCmd = "select sum(cr_amt) h_temp_bef_amt,";
        sqlCmd += " sum(decode(decode(curr_code,'','901',curr_code),'901',cr_amt,dc_cr_amt)) h_temp_dc_bef_amt ";
        sqlCmd += "  from act_acaj  ";
        sqlCmd += " where reference_no = ?  ";
        sqlCmd += "   and  p_seqno     = ?  ";
        sqlCmd += "   and  adjust_type = 'DR11' ";
        setString(1, hNrnlReferenceNo);
        setString(2, hAprePSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempBefAmt = getValueDouble("h_temp_bef_amt");
            hTempDcBefAmt = getValueDouble("h_temp_dc_bef_amt");
        }

    }

    /***********************************************************************/
    void insertActAcaj() throws Exception {
        daoTable = "act_acaj";
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("p_seqno", hAprePSeqno);
        setValue("curr_code", hApreCurrCode);
        setValue("acct_type", hApreAcctType);
        //setValue("acct_key", h_apre_acct_key);
        setValue("adjust_type", "DR11");
        setValue("reference_no", hNrnlReferenceNo);
        setValue("post_date", hAcajPostDate);
        setValueDouble("dc_orginal_amt", hAcajDcOrginalAmt);
        setValueDouble("orginal_amt", hAcajOrginalAmt);
        setValueDouble("dc_cr_amt", hAcajDcCrAmt);
        setValueDouble("cr_amt", hAcajCrAmt);
        setValueDouble("dc_bef_amt", hAcajDcBefAmt);
        setValueDouble("bef_amt", hAcajBefAmt);
        setValueDouble("dc_aft_amt", hAcajDcCrAmt + hAcajDcBefAmt);
        setValueDouble("aft_amt", hAcajCrAmt + hAcajBefAmt);
        setValueDouble("dc_bef_d_amt", hAcajDcBefDAmt);
        setValueDouble("bef_d_amt", hAcajBefDAmt);
        setValueDouble("dc_aft_d_amt", hAcajDcAftDAmt);
        setValueDouble("aft_d_amt", hAcajAftDAmt);
        setValue("acct_code", hAcajAcctCode); // acct_code
        setValue("function_code", "U");
        setValue("value_type", "2");
        setValue("interest_date", hBusiBusinessDate);
        setValue("adj_reason_code", hApreAdjReasonCode);
        setValue("adj_comment", hApreAdjComment);
        setValue("c_debt_key", hApreCDebtKey);
        setValue("debit_item", hApreCrItem);
        setValue("apr_flag", "Y");
        setValue("job_code", hApreJobCode);
        setValue("vouch_job_code", hApreVouchJobCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acaj duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActJrnl() throws Exception {
        daoTable = "act_jrnl";
        updateSQL = "payment_rev_amt = payment_rev_amt + ?,";
        updateSQL += "reversal_flag   = 'S',";
        updateSQL += "mod_pgm         = 'ActC003',";
        updateSQL += "mod_time        = sysdate";
        whereStr = "where decode(jrnl_seqno,'','x',jrnl_seqno) = ?  ";
        whereStr += "and acct_type   =  ?  ";
        whereStr += "and p_seqno = ? ";
        whereStr += "and enq_seqno   =  ?  ";
        whereStr += "and tran_class  = 'P' ";
        setDouble(1, hTempRevAmt);
        setString(2, hApreJrnlSeqno);
        setString(3, hApreAcctType);
        setString(4, hAprePSeqno);
        setInt(5, hApreEnqSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_jrnl not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActPayRev() throws Exception {
        daoTable = "act_pay_rev";
        updateSQL = "proc_mark = 'Y',";
        updateSQL += " mod_pgm  = 'ActC003',";
        updateSQL += " mod_time = sysdate";
        whereStr = "where decode(jrnl_seqno,'','x',jrnl_seqno) = ?  ";
        whereStr += "and rowid = ? ";
        setString(1, hApreJrnlSeqno);
        setRowId(2, hApreRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_pay_rev not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActPayRev1(String t_proc_mark) throws Exception {
        daoTable = "act_pay_rev";
        updateSQL = "proc_mark = ?,";
        updateSQL += " mod_pgm  = 'ActC003',";
        updateSQL += " mod_time = sysdate";
        whereStr = "where decode(jrnl_seqno,'','x',jrnl_seqno) = ?  ";
        whereStr += "and rowid = ? ";
        setString(1, t_proc_mark);
        setString(2, hApreJrnlSeqno);
        setRowId(3, hApreRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_pay_rev not found!", "", hCallBatchSeqno);
        }

    }

    /*************************************************************************/
    void deleteActPayRev() throws Exception {
        daoTable = "act_pay_rev";
        whereStr = "WHERE proc_mark  in ('Y','1','2') ";
      //whereStr += "   or (  proc_mark ='1'" + "and to_number(sysdate - mod_time) > 7)";
      //whereStr += "   or ( (proc_mark ='1' or proc_mark ='2') and to_number(sysdate - mod_time) > 7)";
        deleteTable();

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActC003 proc = new ActC003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
