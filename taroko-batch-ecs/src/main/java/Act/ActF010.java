/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  107/01/04  V1.00.01    Brian     error   correction                        *
 *  109/07/28  V1.00.02    Brian     rowid重複覆蓋修正                         *
 *  111/10/13  V1.00.03    Yang Bo   sync code from mega                       *
 *  112/06/30  V1.00.04    Simon     remove act_acag.seq_no                    *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*雙幣卡不同幣別最低應繳金額抵扣處理程式*/
public class ActF010 extends AccessDAO {

    public static final boolean debugMode = false;

    private final String PROGNAME = "雙幣卡不同幣別最低應繳金額抵扣處理程式  112/06/30  V1.00.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hWdayStmtCycle = "";
    String hWdayLastCloseDate = "";
    String hWdayThisAcctMonth = "";
    String hAcurPSeqno = "";
    String hAcurCurrCode = "";
    double hAcurTtlAmt = 0;
    double hAcurDcTtlAmt = 0;
    double hAcurTtlAmtBal = 0;
    double hAcurDcTtlAmtBal = 0;
    double hAcurDcEndBalOp = 0;
    double hAcurDcEndBalLk = 0;
    String hAmcrPSeqno = "";
    String hAmcrCurrCode = "";
    String hAmcrPaymentDate = "";
    double hAmcrBegOverAmt = 0;
    double hAmcrDcBegOverAmt = 0;
    double hAmcrEndOverAmt = 0;
    double hAmcrDcEndOverAmt = 0;
    String hAmcrRowid = "";
    String hAcctLastMinPayDate = "";
    double hAcurMinPayBal = 0;
    double hAcurDcMinPayBal = 0;
    String hAcurRowid = "";
    String hPyajPaymentDate = "";
    String hPyajClassCode = "";
    String hPyajPaymentType = "";
    String hPyajReferenceNo = "";
    double hPyajPaymentAmount = 0;
    double hPyajDcPaymentAmount = 0;
    String hDebtAcctMonth = "";
    String hDebtAcctCode = "";
//  String h_ampc_f_min_pay_bal = "";
//  String h_ampc_dc_f_min_pay_bal = "";
    double hAmpcFMinPayBal = 0;
    double hAmpcDcFMinPayBal = 0;
    String hAacrCurrCode = "";
    String hAacrAcctMonth = "";
    double hAacrPayAmt = 0;
    double hAacrDcPayAmt = 0;
    String hAacrRowid = "";
    double hAcctMinPayBal = 0;
    double hAcurDcMinPay = 0;
    double hAcurEndBalLk = 0;
    double hAcurEndBalOp = 0;
    double hAcurMinPay = 0;

    int totalCnt = 0;
    int insertCnt = 0;

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
                comc.errExit("Usage : ActF010 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            if (args.length == 1)
                hBusiBusinessDate = args[0];
            selectPtrBusinday();
            showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));

            if (selectPtrWorkday() != 0) {
                finalProcess();
                return 0;
            }

            showLogMessage("I", "", String.format("檢核繳超過MP處理...."));
            totalCnt = insertCnt = 0;
            selectActAcctCurr0();
            showLogMessage("I", "", String.format("     Total process records[%d] insert[%d]", totalCnt, insertCnt));

            showLogMessage("I", "", String.format("沖抵其他幣別處理...."));
            totalCnt = 0;
            selectActMpCurr1();
            showLogMessage("I", "", String.format("     Total process records[%d]", totalCnt));

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
        hWdayStmtCycle = "";
        hWdayLastCloseDate = "";
        hWdayThisAcctMonth = "";

        sqlCmd = "select stmt_cycle,";
        sqlCmd += " last_close_date,";
        sqlCmd += " this_acct_month ";
        sqlCmd += "  from ptr_workday  ";
        sqlCmd += " where this_close_date = ? ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayLastCloseDate = getValue("last_close_date");
            hWdayThisAcctMonth = getValue("this_acct_month");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void selectActAcctCurr0() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.p_seqno ";
        sqlCmd += "  from act_acct_curr a,act_acno b ";
        sqlCmd += " where a.p_seqno = b.acno_p_seqno ";
        sqlCmd += "   and b.stmt_cycle = ? ";
        sqlCmd += " group by a.p_seqno ";
        sqlCmd += "HAVING count(*) >1 ";
        sqlCmd += "   and sum(dc_min_pay_bal) >0 ";
        sqlCmd += "   and sum(decode(sign(dc_ttl_amt - dc_ttl_amt_bal - dc_min_pay + dc_end_bal_op + dc_end_bal_lk),1,1,0)) > 0 ";

        setString(1, hWdayStmtCycle);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcurPSeqno = getValue("p_seqno");

            totalCnt++;

            selectActAcctCurr1();
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectActAcctCurr1() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.curr_code,";
        sqlCmd += " a.ttl_amt,";
        sqlCmd += " a.dc_ttl_amt,";
        sqlCmd += " a.ttl_amt_bal,";
        sqlCmd += " a.dc_ttl_amt_bal,";
        sqlCmd += " a.min_pay,";
        sqlCmd += " a.dc_min_pay,";
        sqlCmd += " a.end_bal_op,";
        sqlCmd += " a.dc_end_bal_op,";
        sqlCmd += " a.end_bal_lk,";
        sqlCmd += " a.dc_end_bal_lk ";
        sqlCmd += "  from act_acct_curr a ";
        sqlCmd += " where a.p_seqno = ? ";
        sqlCmd += "   and dc_min_pay_bal = 0 ";
        sqlCmd += "   and dc_ttl_amt-dc_ttl_amt_bal-dc_min_pay+dc_end_bal_op+dc_end_bal_lk > 0 ";

        setString(1, hAcurPSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcurCurrCode = getValue("curr_code", i);
            hAcurTtlAmt = getValueDouble("ttl_amt", i);
            hAcurDcTtlAmt = getValueDouble("dc_ttl_amt", i);
            hAcurTtlAmtBal = getValueDouble("ttl_amt_bal", i);
            hAcurDcTtlAmtBal = getValueDouble("dc_ttl_amt_bal", i);
            hAcurMinPay = getValueDouble("min_pay", i);
            hAcurDcMinPay = getValueDouble("dc_min_pay", i);
            hAcurEndBalOp = getValueDouble("end_bal_op", i);
            hAcurDcEndBalOp = getValueDouble("dc_end_bal_op", i);
            hAcurEndBalLk = getValueDouble("end_bal_lk", i);
            hAcurDcEndBalLk = getValueDouble("dc_end_bal_lk", i);

            selectActMpCurr();
            hAmcrDcBegOverAmt = hAcurDcTtlAmt - hAcurDcTtlAmtBal - hAcurDcMinPay
                    + hAcurDcEndBalOp + hAcurDcEndBalLk - hAmcrDcBegOverAmt;

            if (hAmcrDcBegOverAmt == 0)
                continue;
            if (hAmcrDcBegOverAmt < 0) {
                showLogMessage("I", "", String.format("p_seqno[%s] curr_code[%s] ,aybe payment reverser!",
                        hAcurPSeqno, hAcurCurrCode));
                updateActMpCurr1();
                continue;
            }
            hAmcrBegOverAmt = hAcurTtlAmt - hAcurTtlAmtBal - hAcurMinPay + hAcurEndBalOp
                    + hAcurEndBalLk - hAmcrBegOverAmt;

            selectCycPyaj();
        }
    }

    /***********************************************************************/
    void selectActMpCurr() throws Exception {
        hAmcrDcBegOverAmt = 0;
        hAmcrBegOverAmt = 0;

        sqlCmd = "select nvl(sum(dc_beg_over_amt),0) h_amcr_dc_beg_over_amt,";
        sqlCmd += " nvl(sum(beg_over_amt)   ,0) h_amcr_beg_over_amt ";
        sqlCmd += "  from act_mp_curr  ";
        sqlCmd += " where acct_month = ?  ";
        sqlCmd += "   and curr_code  = ?  ";
        sqlCmd += "   and p_seqno    = ? ";
        setString(1, hWdayThisAcctMonth);
        setString(2, hAcurCurrCode);
        setString(3, hAcurPSeqno);
        int recordCnt1 = selectTable();
        if (recordCnt1 > 0) {
            hAmcrDcBegOverAmt = getValueDouble("h_amcr_dc_beg_over_amt");
            hAmcrBegOverAmt = getValueDouble("h_amcr_beg_over_amt");
        }

    }

    /***********************************************************************/
    void updateActMpCurr1() throws Exception {
        daoTable = "act_mp_curr";
        updateSQL = "proc_mark                              = 'X'";
        whereStr = "where acct_month                       = ?  ";
        whereStr += "and curr_code                          = ?  ";
        whereStr += "and p_seqno                            = ?  ";
        whereStr += "and decode(proc_mark,'','N',proc_mark)!= 'X' ";
        setString(1, hWdayThisAcctMonth);
        setString(2, hAcurCurrCode);
        setString(3, hAcurPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void selectCycPyaj() throws Exception {
        double tempDouble = 0, dcTempDouble;

        dcTempDouble = hAmcrDcBegOverAmt;
        tempDouble = hAmcrBegOverAmt;

        sqlCmd = "select ";
        sqlCmd += " a.payment_date,";
        sqlCmd += " a.class_code,";
        sqlCmd += " a.payment_type,";
        sqlCmd += " a.reference_no,";
        sqlCmd += " a.payment_amt,";
        sqlCmd += " a.dc_payment_amt ";
        sqlCmd += "  from cyc_pyaj a ";
        sqlCmd += " where a.p_seqno = ? ";
        sqlCmd += "   and a.curr_code = ? ";
        sqlCmd += "   and a.dc_payment_amt != 0 ";
        sqlCmd += "   and (a.class_code = 'P' ";
        sqlCmd += "    OR (a.class_code = 'A' ";
        sqlCmd += "   and a.reference_no != '')) ";
        sqlCmd += " order by a.payment_date desc ";
        setString(1, hAcurPSeqno);
        setString(2, hAcurCurrCode);
        int recordCnt1 = selectTable();
        for (int i = 0; i < recordCnt1; i++) {
            hPyajPaymentDate = getValue("payment_date", i);
            hPyajClassCode = getValue("class_code", i);
            hPyajPaymentType = getValue("payment_type", i);
            hPyajReferenceNo = getValue("reference_no", i);
            hPyajPaymentAmount = getValueDouble("payment_amt", i);
            hPyajDcPaymentAmount = getValueDouble("dc_payment_amt", i);

            if (comc.getSubString(hPyajPaymentType, 0,2).equals("OP")) { //"OP01"~"OP04"其 class_code='P' & payment_amt<0
                continue;
            }

            if (hPyajClassCode.equals("A")) {
                if (selectActDebt() != 0)
                    selectActDebtHst();

                if ((!hDebtAcctCode.equals("BL")) && (!hDebtAcctCode.equals("AO"))
                        && (!hDebtAcctCode.equals("CA")) && (!hDebtAcctCode.equals("IT"))
                        && (!hDebtAcctCode.equals("ID")) && (!hDebtAcctCode.equals("OT")))
                    hDebtAcctMonth = hWdayThisAcctMonth;

                if (!hDebtAcctMonth.equals(hWdayThisAcctMonth)) {
                    dcTempDouble += hPyajDcPaymentAmount;
                    tempDouble += hPyajPaymentAmount;
                }
                continue;
            }

            if (hPyajDcPaymentAmount >= dcTempDouble) {
                hAmcrDcBegOverAmt = dcTempDouble;
                hAmcrBegOverAmt = tempDouble;
                dcTempDouble = 0;
                tempDouble = 0;
            } else {
                hAmcrDcBegOverAmt = hPyajDcPaymentAmount;
                hAmcrBegOverAmt = hPyajPaymentAmount;
                dcTempDouble -= hPyajDcPaymentAmount;
                tempDouble -= hPyajPaymentAmount;
            }

            hAmcrPaymentDate = hPyajPaymentDate;

            insertActMpCurr();
            if (dcTempDouble == 0) {
                break;
            }
        }
    }

    /***********************************************************************/
    int selectActDebt() throws Exception {
        hDebtAcctMonth = "";
        hDebtAcctCode = "";
        sqlCmd = "select acct_month,";
        sqlCmd += " acct_code ";
        sqlCmd += "  from act_debt  ";
        sqlCmd += " where reference_no = ? ";
        setString(1, hPyajReferenceNo);
        int recordCnt2 = selectTable();
        if (recordCnt2 > 0) {
            hDebtAcctMonth = getValue("acct_month");
            hDebtAcctCode = getValue("acct_code");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    int selectActDebtHst() throws Exception {
        sqlCmd = "select acct_month,";
        sqlCmd += " acct_code ";
        sqlCmd += "  from act_debt_hst  ";
        sqlCmd += " where reference_no = ? ";
        setString(1, hPyajReferenceNo);
        int recordCnt2 = selectTable();
        if (recordCnt2 > 0) {
            hDebtAcctMonth = getValue("acct_month");
            hDebtAcctCode = getValue("acct_code");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertActMpCurr() throws Exception {
        insertCnt++;
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("acct_month", hWdayThisAcctMonth);
        setValue("curr_code", hAcurCurrCode);
        setValue("p_seqno", hAcurPSeqno);
        setValue("payment_date", hPyajPaymentDate);
        setValueDouble("ttl_amt", hAcurTtlAmt);
        setValueDouble("dc_ttl_amt", hAcurDcTtlAmt);
        setValueDouble("ttl_amt_bal", hAcurTtlAmtBal);
        setValueDouble("dc_ttl_amt_bal", hAcurDcTtlAmtBal);
        setValueDouble("min_pay", hAcurMinPay);
        setValueDouble("dc_min_pay", hAcurDcMinPay);
        setValueDouble("end_bal_op", hAcurEndBalOp);
        setValueDouble("dc_end_bal_op", hAcurDcEndBalOp);
        setValueDouble("end_bal_lk", hAcurEndBalLk);
        setValueDouble("dc_end_bal_lk", hAcurDcEndBalLk);
        setValueDouble("beg_over_amt", hAmcrBegOverAmt);
        setValueDouble("dc_beg_over_amt", hAmcrDcBegOverAmt);
        setValueDouble("end_over_amt", hAmcrBegOverAmt);
        setValueDouble("dc_end_over_amt", hAmcrDcBegOverAmt);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "act_mp_curr";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_mp_curr duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectActMpCurr1() throws Exception {

        sqlCmd = "select a.p_seqno,";
        sqlCmd += " a.curr_code,";
        sqlCmd += " a.payment_date,";
        sqlCmd += " a.beg_over_amt,";
        sqlCmd += " a.dc_beg_over_amt,";
        sqlCmd += " a.end_over_amt,";
        sqlCmd += " a.dc_end_over_amt,";
        sqlCmd += " a.rowid rowid,";
        sqlCmd += " b.last_min_pay_date ";
        sqlCmd += "  from act_mp_curr a,act_acct b,act_acno c ";
        sqlCmd += " where a.acct_month = ? ";
        sqlCmd += "   and c.stmt_cycle = ? ";
        sqlCmd += "   and c.acno_p_seqno  = a.p_seqno ";
        sqlCmd += "   and b.p_seqno  = a.p_seqno ";
        sqlCmd += "   and dc_end_over_amt > 0 ";
        sqlCmd += "   and decode(proc_mark,'','N',proc_mark)!='X' ";
        sqlCmd += " order by payment_date ";
        setString(1, hWdayThisAcctMonth);
        setString(2, hWdayStmtCycle);
        extendField = "act_mp_curr.";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAmcrPSeqno = getValue("act_mp_curr.p_seqno", i);
            hAmcrCurrCode = getValue("act_mp_curr.curr_code", i);
            hAmcrPaymentDate = getValue("act_mp_curr.payment_date", i);
            hAmcrBegOverAmt = getValueDouble("act_mp_curr.beg_over_amt", i);
            hAmcrDcBegOverAmt = getValueDouble("act_mp_curr.dc_beg_over_amt", i);
            hAmcrEndOverAmt = getValueDouble("act_mp_curr.end_over_amt", i);
            hAmcrDcEndOverAmt = getValueDouble("act_mp_curr.dc_end_over_amt", i);
            hAmcrRowid = getValue("act_mp_curr.rowid", i);
          //雖然是抓act_acct的繳足最低應繳日期，但因設定 extendField = "act_mp_curr."，因此仍需以act_mp_curr.last_min_pay_date變數取值
            hAcctLastMinPayDate = getValue("act_mp_curr.last_min_pay_date", i);

            if (hAcctLastMinPayDate.length() != 0)
                continue;

            selectActAcctCurr2();
            updateActMpCurr();
        }
    }

    /***********************************************************************/
    void selectActAcctCurr2() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.curr_code,";
        sqlCmd += " a.min_pay,";
        sqlCmd += " a.dc_min_pay,";
        sqlCmd += " a.min_pay_bal,";
        sqlCmd += " a.dc_min_pay_bal,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "  from act_acct_curr a ";
        sqlCmd += " where a.p_seqno = ? ";
        sqlCmd += "   and a.curr_code != ? ";
        sqlCmd += "   and a.dc_min_pay_bal != 0 ";
        setString(1, hAmcrPSeqno);
        setString(2, hAmcrCurrCode);
        extendField = "act_acct_curr.";
        int recordCnt1 = selectTable();
        for (int i = 0; i < recordCnt1; i++) {
            hAcurCurrCode = getValue("act_acct_curr.curr_code", i);
            hAcurMinPay = getValueDouble("act_acct_curr.min_pay", i);
            hAcurDcMinPay = getValueDouble("act_acct_curr.dc_min_pay", i);
            hAcurMinPayBal = getValueDouble("act_acct_curr.min_pay_bal", i);
            hAmpcFMinPayBal = getValueDouble("act_acct_curr.min_pay_bal", i);//original min_pay_bal
            hAcurDcMinPayBal = getValueDouble("act_acct_curr.dc_min_pay_bal", i);
            hAmpcDcFMinPayBal = getValueDouble("act_acct_curr.dc_min_pay_bal", i);//original dc_min_pay_bal
            hAcurRowid = getValue("act_acct_curr.rowid", i);

            hAmcrBegOverAmt = hAmcrEndOverAmt;
            hAmcrDcBegOverAmt = hAmcrDcEndOverAmt;

            totalCnt++;

            hAcurMinPay = comcr.commCurrAmt("901", hAcurMinPay, 0);
            hAcurMinPayBal = comcr.commCurrAmt("901", hAcurMinPayBal, 0);
            hAmpcFMinPayBal = hAcurMinPayBal;//original min_pay_bal 四捨五入

            if (hAmcrEndOverAmt >= hAcurMinPayBal)
                hAcurDcMinPayBal = 0;
            else
                hAcurDcMinPayBal = hAcurDcMinPayBal - comcr.commCurrAmt(hAcurCurrCode,
                        hAmcrEndOverAmt * (hAcurDcMinPay / hAcurMinPay), 0);

            selectActAcagCurr();
            deleteActAcag();

            if (hAcurCurrCode.equals("901"))
                hAcurMinPayBal = hAcurDcMinPayBal;

            updateActAcctCurr();
            updateActAcct();

          //h_amcr_end_over_amt = h_amcr_end_over_amt - h_acur_min_pay_bal + h_acur_min_pay_bal;
            hAmcrEndOverAmt = hAmcrEndOverAmt - hAmpcFMinPayBal + hAcurMinPayBal;
            hAmcrDcEndOverAmt = hAmcrEndOverAmt * (hAmcrDcBegOverAmt / hAmcrBegOverAmt);
            hAmcrDcEndOverAmt = comcr.commCurrAmt(hAmcrCurrCode, hAmcrDcEndOverAmt, 0);
            if (hAmcrDcEndOverAmt <= 0)
                hAmcrEndOverAmt = 0;

            insertActMpCurrDtl();
            if ((hAmcrDcEndOverAmt == 0) || (hAcurDcMinPayBal == 0)) {
                break;
            }
        }
    }

    /***********************************************************************/
    void selectActAcagCurr() throws Exception {
        double tempDcDouble;

        tempDcDouble = hAcurDcMinPayBal;
        hAcurMinPayBal = 0;
        sqlCmd = "select ";
        sqlCmd += " curr_code,";
        sqlCmd += " acct_month,";
        sqlCmd += " pay_amt,";
        sqlCmd += " dc_pay_amt,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acag_curr ";
        sqlCmd += " where p_seqno   = ? ";
        sqlCmd += "   and curr_code = ? ";
        sqlCmd += " order by acct_month DESC ";
        setString(1, hAmcrPSeqno);
        setString(2, hAcurCurrCode);
        extendField = "act_acag_curr.";
        int recordCnt2 = selectTable();
        for (int i = 0; i < recordCnt2; i++) {
            hAacrCurrCode = getValue("act_acag_curr.curr_code", i);
            hAacrAcctMonth = getValue("act_acag_curr.acct_month", i);
            hAacrPayAmt = getValueDouble("act_acag_curr.pay_amt", i);
            hAacrDcPayAmt = getValueDouble("act_acag_curr.dc_pay_amt", i);
            hAacrRowid = getValue("act_acag_curr.rowid", i);

            if ((hAacrDcPayAmt == 0) || (tempDcDouble == 0)) {
                deleteActAcagCurr();
                updateActAcag();
                continue;
            }
            if (tempDcDouble >= hAacrDcPayAmt) {
                tempDcDouble = tempDcDouble - hAacrDcPayAmt;
                hAcurMinPayBal = hAcurMinPayBal + hAacrPayAmt;
                continue;
            }
            hAacrPayAmt = comcr.commCurrAmt("901", tempDcDouble * (hAacrPayAmt / hAacrDcPayAmt), 0);
            hAcurMinPayBal = hAcurMinPayBal + hAacrPayAmt;
            hAacrDcPayAmt = tempDcDouble;
            if (hAcurCurrCode.equals("901"))
                hAacrPayAmt = hAacrDcPayAmt;
            if (hAacrDcPayAmt == 0)
                hAacrPayAmt = 0;
            tempDcDouble = 0;
            if (hAacrDcPayAmt <= 0)
                deleteActAcagCurr();
            else
                updateActAcagCurr();
            updateActAcag();
        }
    }

    /***********************************************************************/
    void deleteActAcagCurr() throws Exception {
        daoTable = "act_acag_curr";
        whereStr = "where rowid = ? ";
        setRowId(1, hAacrRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_act_acag_curr not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcagCurr() throws Exception {
        daoTable = "act_acag_curr";
        updateSQL = "pay_amt      = ?,";
        updateSQL += " dc_pay_amt  = ?";
        whereStr = "where rowid  = ? ";
        setDouble(1, hAacrPayAmt);
        setDouble(2, hAacrDcPayAmt);
        setRowId(3, hAacrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acag_curr not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcag() throws Exception {
        daoTable  = "act_acag a";
        updateSQL = " pay_amt = ( select nvl(sum(pay_amt),0) from act_acag_curr"
                  + " where p_seqno = a.p_seqno  and acct_month = a.acct_month ) ";
        whereStr  = " where a.p_seqno = ?  and  a.acct_month = ? ";
        setString(1, hAmcrPSeqno);
        setString(2, hAacrAcctMonth);
        updateTable();
        if (!notFound.equals("Y"))
            return;

        sqlCmd = "insert into act_acag ";
        sqlCmd += " (p_seqno,";
      //sqlCmd += " seq_no,";
        sqlCmd += " acct_type,";
        sqlCmd += " acct_month,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " pay_amt,";
        sqlCmd += " mod_time,";
        sqlCmd += " mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += " p_seqno,";
      //sqlCmd += " max(0),";
        sqlCmd += " max(acct_type),";
        sqlCmd += " acct_month,";
        sqlCmd += " max(stmt_cycle),";
        sqlCmd += " sum(pay_amt),";
        sqlCmd += " max(sysdate),";
        sqlCmd += " max('ActF010') ";
        sqlCmd += "  from act_acag_curr where p_seqno = ? and acct_month = ? GROUP BY p_seqno,acct_month ";
        setString(1, hAmcrPSeqno);
        setString(2, hAacrAcctMonth);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acag a duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteActAcag() throws Exception {

        daoTable = "act_acag";
        whereStr = "where p_seqno = ?  ";
        whereStr += "and  pay_amt = 0 ";
        setString(1, hAmcrPSeqno);
        deleteTable();

    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        daoTable = "act_acct_curr";
        updateSQL = "min_pay_bal     = ?,";
        updateSQL += " dc_min_pay_bal = ?,";
        updateSQL += " mod_time       = sysdate,";
        updateSQL += " mod_pgm        = 'ActF010'";
        whereStr = "where rowid     = ? ";
        setDouble(1, hAcurMinPayBal);
        setDouble(2, hAcurDcMinPayBal);
        setRowId(3, hAcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcct() throws Exception {
        selectActAcctCurr3();
        if (hAcctMinPayBal == 0)
            hAcctLastMinPayDate = hAmcrPaymentDate;

        daoTable = "act_acct a";
        updateSQL = " min_pay_bal   = ?,";
        updateSQL += " last_min_pay_date  = decode(?, 0, cast(? as varchar(8)), last_min_pay_date),";
        updateSQL += " mod_time    = sysdate,";
        updateSQL += " mod_pgm    = ?";
        whereStr = "where p_seqno    =? ";
        setDouble(1, hAcctMinPayBal);
        setDouble(2, hAcctMinPayBal);
        setString(3, hAmcrPaymentDate);
        setString(4, javaProgram);
        setString(5, hAmcrPSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct a not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectActAcctCurr3() throws Exception {
        sqlCmd = "select sum(min_pay_bal) h_acct_min_pay_bal ";
        sqlCmd += "  from act_acct_curr  ";
        sqlCmd += " where p_seqno = ? ";
        setString(1, hAmcrPSeqno);
        int recordCnt2 = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct_curr not found!", "", hCallBatchSeqno);
        }
        if (recordCnt2 > 0) {
            hAcctMinPayBal = getValueDouble("h_acct_min_pay_bal");
        }

    }

    /***********************************************************************/
    void insertActMpCurrDtl() throws Exception {
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("acct_month", hWdayThisAcctMonth);
        setValue("curr_code", hAmcrCurrCode);
        setValue("p_seqno", hAmcrPSeqno);
        setValueDouble("beg_over_amt", hAmcrBegOverAmt);
        setValueDouble("dc_beg_over_amt", hAmcrDcBegOverAmt);
        setValueDouble("end_over_amt", hAmcrEndOverAmt);
        setValueDouble("dc_end_over_amt", hAmcrDcEndOverAmt);
        setValue("curr_code_1", hAcurCurrCode);
        setValueDouble("min_pay", hAcurMinPay);
        setValueDouble("dc_min_pay", hAcurDcMinPay);
      //setValue("f_min_pay_bal", h_ampc_f_min_pay_bal);
      //setValue("dc_f_min_pay_bal", h_ampc_dc_f_min_pay_bal);
        setValueDouble("f_min_pay_bal", hAmpcFMinPayBal);
        setValueDouble("dc_f_min_pay_bal", hAmpcDcFMinPayBal);
        setValueDouble("min_pay_bal", hAcurMinPayBal);
        setValueDouble("dc_min_pay_bal", hAcurDcMinPayBal);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "act_mp_curr_dtl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_mp_curr_dtl duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActMpCurr() throws Exception {
        daoTable = "act_mp_curr";
        updateSQL = " proc_mark       = decode(?, 0, 'Y', proc_mark),";
        updateSQL += " end_over_amt    = ?,";
        updateSQL += " dc_end_over_amt = ?,";
        updateSQL += " mod_time        = sysdate,";
        updateSQL += " mod_pgm         = 'ActF010'";
        whereStr = "where rowid      = ? ";
        setDouble(1, hAmcrDcEndOverAmt);
        setDouble(2, hAmcrEndOverAmt);
        setDouble(3, hAmcrDcEndOverAmt);
        setRowId(4, hAmcrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_mp_curr not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActF010 proc = new ActF010();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
