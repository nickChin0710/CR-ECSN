/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/29  V1.00.01    SUP       error   correction                        *
 *  108/03/07  V1.08.01  陳君暘      BECS-1080306-016  curr_term > 1, fee = null*
 *  108/03/08  V1.08.02  David       整合                                      *
 *  109/11/17  V1.00.03    shiyuqi   updated for project coding standard       *  
 *  112/03/25  V1.00.04  Simon       add value_type="3" to update act_debt.interest_rs_date*  
 *  112/12/18  V1.00.05  Simon       連動手續費調整 reversal 之 adjust_type DR07*
 *                                   更改為 DR12(對應ActF001之DE12)            *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡人帳務調整(D檔還原)處理程式*/
public class ActF002 extends AccessDAO {

    public static final boolean debugMode = false;

    private String progname = "卡人帳務調整(D檔還原)處理程式  112/12/18  V1.00.05  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTempCreateDate = "";
    String hTempCreateTime = "";
    String hAcajPSeqno = "";
    String hAcajCurrCode = "";
    String hAcajAcctType = "";
    String hAcajAdjustType = "";
    String hAcajReferenceNo = "";
    String hAcajPostDate = "";
    double hAcajCrAmt = 0;
    double hAcajDcCrAmt = 0;
    double hAcajBefDAmt = 0;
    double hAcajDcBefDAmt = 0;
    double hAcajAftDAmt = 0;
    double hAcajDcAftDAmt = 0;
    String hAcajAcctCode = "";
    String hAcajValueType = "";
    String hAcajAdjReasonCode = "";
    String hAcajAdjComment = "";
    String hAcajCDebtKey = "";
    String hAcajDebitItem = "";
    String hAcajJrnlDate = "";
    String hAcajJobCode = "";
    String hAcajVouchJobCode = "";
    String hAcajRowid = "";
    double hDeb1BegBal = 0;
    double hDeb1DcBegBal = 0;
    double hDeb1EndBal = 0;
    double hDeb1DcEndBal = 0;
    double hDeb1DAvailableBal = 0;
    double hDeb1DcDAvailableBal = 0;
    String seqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoAcctHolderId = "";
    String hAcnoRecourseMark = "";
    String hAcnoRowid = "";
    String hWdayThisAcctMonth = "";
    String hWdayLastAcctMonth = "";
    String hWdayIlAcctMonth = "";
    String hWdayNextAcctMonth = "";
    String hWdayThisCloseDate = "";
    String hWdayLastCloseDate = "";
    String hWdayNextCloseDate = "";
    String hWdayThisInterestDate = "";
    String hWdayThisLastpayDate = "";
    double hAcctAcctJrnlBal = 0;
    double hAcctAdjustCrAmt = 0;
    int hAcctAdjustCrCnt = 0;
    double hAcctTtlAmt = 0;
    double hAcctTtlAmtBal = 0;
    String hAcctRowid = "";
    double hAcurAcctJrnlBal = 0;
    double hAcurDcAcctJrnlBal = 0;
    double hAcurTtlAmt = 0;
    double hAcurDcTtlAmt = 0;
    double hAcurTtlAmtBal = 0;
    double hAcurDcTtlAmtBal = 0;
    double hAcurMinPayBal = 0;
    double hAcurDcMinPayBal = 0;
    double hAcurDcAdjustCrAmt = 0;
    int hAcurAdjustCrCnt = 0;
    String hAcurRowid = "";
    String hDeb1Rowid = "";
    String hDeb1ReferenceSeq = "";
    String hDeb1ItemPostDate = "";
    String hDeb1AcctMonth = "";
    String hDeb1StmtCycle = "";
    String hDeb1AcctCode = "";
    String hDeb1InterestDate = "";
    int hJrnlEnqSeqno = 0;
    String hJrnlTranClass = "";
    String hJrnlTranType = "";
    int hInt = 0;
    double hJrnlTransactionAmt = 0;
    double hJrnlDcTransactionAmt = 0;
    double hJrnlJrnlBal = 0;
    double hJrnlDcJrnlBal = 0;
    String hAcajInterestDate = "";
    String hJrnlJrnlSeqno = "";
    int hJrnlOrderSeq = 0;
    String hDeb1InterestRsDate = "";
    double hAchtWaiveTtlBal = 0;
    double hDebaEndBal = 0;
    double hAchtStmtCreditLimit = 0;
    String hBillFeesReferenceNo = "";
    String hBillReferenceNoFeef = "";
    String hBillAcctMonth = "";
    double hAcctMinPayBal = 0;
    String hAcctLastCancelDebtDate = "";
    double hAvdaVouchAmt = 0;
    double hAvdaDVouchAmt = 0;
    String hAvdaProcStage = "";

    int totalCnt = 0;
    int debtInt = 0;
    int hAcctAdjustDrCnt = 0;
    String hTempReferenceNo = "";
    String hDebtBillType = "";
    String hWdayLastDelaypayDate = "";
    String hWdayllLastDelaypayDate = "";
    String hDebtAcctMonth = "";
    String hAchtAcctMonth = "";
    double hAvdaOVouchAmt = 0;
    double totWaiveMinAmt = 0;
    double hDebtDcBegBal = 0;
    double hDebtBegBal = 0;
    double totRealWaiveAmt = 0;
    double totDcRealWaiveAmt = 0;
    double hDebtDcEndBal = 0;
    double hDebtEndBal = 0;
    double hDebtDcDAvailableBal = 0;
    double hDebtDAvailableBal = 0;
    double hDebeDcEndBal = 0;
    double hDebeEndBal = 0;
    double totDcWaiveMinAmt = 0;
    double dcRealCancelAmt = 0;
    double dcWantWaiveAmt = 0;
    double wantWaiveAmt = 0;
    double realCancelAmt = 0;
    double wsMinpayRate = 0;
    double hDebeDAvailableBal = 0;
    double dcRealWaiveAmt = 0;
    double realWaiveAmt = 0;
    double hDebeDcDAvailableBal = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : ActF002 , this program need only one parameter  ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            selectPtrBusinday();
            showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));

            selectActAcaj();

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
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_create_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_create_time ";
        sqlCmd += "  from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempCreateDate = getValue("h_temp_create_date");
            hTempCreateTime = getValue("h_temp_create_time");
        }

    }

    /***********************************************************************/
    void selectActAcaj() throws Exception {

        sqlCmd = "select p_seqno,";
        sqlCmd += " decode(curr_code,'','901',curr_code) h_acaj_curr_code,";
        sqlCmd += " acct_type,";
        sqlCmd += " adjust_type,";
        sqlCmd += " reference_no,";
        sqlCmd += " post_date,";
        sqlCmd += " cr_amt,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',cr_amt,dc_cr_amt) h_acaj_dc_cr_amt,";
        sqlCmd += " bef_d_amt,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',bef_d_amt,dc_bef_d_amt) h_acaj_dc_bef_d_amt,";
        sqlCmd += " aft_d_amt,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',aft_d_amt,dc_aft_d_amt) h_acaj_dc_aft_d_amt,";
        sqlCmd += " acct_code,"; // acct_code
        sqlCmd += " value_type,";
        sqlCmd += " adj_reason_code,";
        sqlCmd += " adj_comment,";
        sqlCmd += " c_debt_key,";
        sqlCmd += " debit_item,";
        sqlCmd += " jrnl_date,";
        sqlCmd += " job_code,";
        sqlCmd += " decode(vouch_job_code,'','00',vouch_job_code) h_acaj_vouch_job_code,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acaj ";
        sqlCmd += " where substr(adjust_type,1,2) = 'DR' ";
        sqlCmd += "   and adjust_type != 'DR11' ";
        sqlCmd += "   and decode(process_flag,'','N',process_flag) != 'Y' ";
        sqlCmd += "   and apr_flag     = 'Y' ";
        sqlCmd += " order by vouch_job_code, p_seqno, crt_time ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcajPSeqno = getValue("p_seqno");
            hAcajCurrCode = getValue("h_acaj_curr_code");
            hAcajAcctType = getValue("acct_type");
            hAcajAdjustType = getValue("adjust_type");
            hAcajReferenceNo = getValue("reference_no");
            hAcajPostDate = getValue("post_date");
            hAcajCrAmt = getValueDouble("cr_amt");
            hAcajDcCrAmt = getValueDouble("h_acaj_dc_cr_amt");
            hAcajBefDAmt = getValueDouble("bef_d_amt");
            hAcajDcBefDAmt = getValueDouble("h_acaj_dc_bef_d_amt");
            hAcajAftDAmt = getValueDouble("aft_d_amt");
            hAcajDcAftDAmt = getValueDouble("h_acaj_dc_aft_d_amt");
            hAcajAcctCode = getValue("acct_code"); // acct_code
            hAcajValueType = getValue("value_type");
            hAcajAdjReasonCode = getValue("adj_reason_code");
            hAcajAdjComment = getValue("adj_comment");
            hAcajCDebtKey = getValue("c_debt_key");
            hAcajDebitItem = getValue("debit_item");
            hAcajJrnlDate = getValue("jrnl_date");
            hAcajJobCode = getValue("job_code");
            hAcajVouchJobCode = getValue("h_acaj_vouch_job_code");
            hAcajRowid = getValue("rowid");

            if (debugMode) {
                showLogMessage("I", "", String.format("STEP A1 ====== SELECT ACT_ACAJ ==============="));
                showLogMessage("I", "", String.format("p_seqno                        = [%s]", hAcajPSeqno));
                showLogMessage("I", "", String.format("curr_code                      = [%s]", hAcajCurrCode));
                showLogMessage("I", "", String.format("acct_type                      = [%s]", hAcajAcctType));
                showLogMessage("I", "", String.format("acct_code                = [%s]", hAcajAcctCode));
                showLogMessage("I", "", String.format("adjust_type                    = [%s]", hAcajAdjustType));
                showLogMessage("I", "", String.format("value_type                     = [%s]", hAcajValueType));
                showLogMessage("I", "", String.format("reference_no                   = [%s]", hAcajReferenceNo));
                showLogMessage("I", "", String.format("cr_amt                         = [%f]", hAcajCrAmt));
                showLogMessage("I", "", String.format("dc_cr_amt                      = [%f]", hAcajDcCrAmt));
                showLogMessage("I", "", String.format("dc_cr_amt                      = [%f]", hAcajDcCrAmt));
                showLogMessage("I", "", String.format("bef_d_amt                      = [%f]", hAcajBefDAmt));
                showLogMessage("I", "", String.format("dc_bef_d_amt                   = [%f]", hAcajDcBefDAmt));
                showLogMessage("I", "", String.format("aft_d_amt                      = [%f]", hAcajAftDAmt));
                showLogMessage("I", "", String.format("dc_aft_d_amt                   = [%f]", hAcajDcAftDAmt));
                showLogMessage("I", "", String.format("============== SELECT ACT_ACAJ ==============="));
            }

            totalCnt++;
            if (totalCnt % 1000 == 0)
                showLogMessage("I", "", String.format("   Process records[%d]", totalCnt));
            /***************** Initial data value **********************/
            hJrnlJrnlSeqno = String.format("%012.0f", getJRNLSeq());
            if (hJrnlEnqSeqno > 99900)
                hJrnlEnqSeqno = 0;
            hJrnlOrderSeq = 1;
            totDcRealWaiveAmt = totRealWaiveAmt = 0;
            /***************** Initial data value **********************/
            selectActAcno();
            selectActAcct();
            selectActAcctCurr();
            selectPtrWorkday();
            /*************************************************************/
            if (procDebtData() != 0) {
                insertActAcajErr();
                updateActAcaj();
                continue;
            }
            /********************************************/
            procFeeWaive();
            /********************************************/
            lastCurrData();
            updateActAcctCurr();
            lastAcctData();
            updateActAcct();
            updateActAcno();

            updateActAcaj();
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hAcnoCorpPSeqno = "";
        hAcnoAcctStatus = "";
        hAcnoStmtCycle = "";
        hAcnoAcctHolderId = "";
        hAcnoRecourseMark = "";
        hAcnoRowid = "";

        sqlCmd = "select corp_p_seqno,";
        sqlCmd += " acct_status,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " id_p_seqno,";
        sqlCmd += " recourse_mark,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acno  ";
        sqlCmd += " where acno_p_seqno = ? ";
        setString(1, hAcajPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoAcctHolderId = getValue("id_p_seqno");
            hAcnoRecourseMark = getValue("recourse_mark");
            hAcnoRowid = getValue("rowid");
        }

    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctAcctJrnlBal = 0;
        hAcctAdjustDrCnt = 0;
        hAcctTtlAmt = 0;
        hAcctTtlAmtBal = 0;
        hAcctRowid = "";

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += " adjust_cr_amt ,";
        sqlCmd += " adjust_cr_cnt ,";
        sqlCmd += " ttl_amt,";
        sqlCmd += " ttl_amt_bal,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acct  ";
        sqlCmd += " where p_seqno = ? ";
        setString(1, hAcajPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcctAcctJrnlBal = getValueDouble("acct_jrnl_bal");
            hAcctAdjustCrAmt = getValueDouble("adjust_cr_amt");
            hAcctAdjustCrCnt = getValueInt("adjust_cr_cnt");
            hAcctTtlAmt = getValueDouble("ttl_amt");
            hAcctTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAcctRowid = getValue("rowid");
        }
        if (debugMode) {
            showLogMessage("I", "", String.format("============== FIRST ACT_ACCT ================"));
            showLogMessage("I", "", String.format("acct_jrnl_bal+adi              = [%f]", hAcctAcctJrnlBal));
            showLogMessage("I", "", String.format("adjust_cr_amt                  = [%f]", hAcctAdjustCrAmt));
            showLogMessage("I", "", String.format("adjust_cr_cnt                  = [%d]", hAcctAdjustCrCnt));
            showLogMessage("I", "", String.format("ttl_amt                        = [%f]", hAcctTtlAmt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]", hAcctTtlAmtBal));
            showLogMessage("I", "", String.format("============== FIRST ACT_ACCT ================"));
        }

    }

    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        hAcurAcctJrnlBal = 0;
        hAcurDcAcctJrnlBal = 0;
        hAcurTtlAmt = 0;
        hAcurDcTtlAmt = 0;
        hAcurTtlAmtBal = 0;
        hAcurDcTtlAmtBal = 0;
        hAcurMinPayBal = 0;
        hAcurDcMinPayBal = 0;
        hAcurDcAdjustCrAmt = 0;
        hAcurDcAdjustCrAmt = 0;
        hAcurAdjustCrCnt = 0;

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += " dc_acct_jrnl_bal,";
        sqlCmd += " ttl_amt,";
        sqlCmd += " decode(curr_code,'901',ttl_amt     ,dc_ttl_amt   ) h_acur_dc_ttl_amt,";
        sqlCmd += " ttl_amt_bal,";
        sqlCmd += " decode(curr_code,'901',ttl_amt_bal,dc_ttl_amt_bal) h_acur_dc_ttl_amt_bal,";
        sqlCmd += " min_pay_bal,";
        sqlCmd += " decode(curr_code,'901',min_pay_bal,dc_min_pay_bal) h_acur_dc_min_pay_bal,";
        sqlCmd += " adjust_cr_amt,";
        sqlCmd += " dc_adjust_cr_amt,";
        sqlCmd += " adjust_cr_cnt,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acct_curr  ";
        sqlCmd += " where p_seqno   = ?  ";
        sqlCmd += "   and curr_code = ? ";
        setString(1, hAcajPSeqno);
        setString(2, hAcajCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct_curr not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcurAcctJrnlBal = getValueDouble("acct_jrnl_bal");
            hAcurDcAcctJrnlBal = getValueDouble("dc_acct_jrnl_bal");
            hAcurTtlAmt = getValueDouble("ttl_amt");
            hAcurDcTtlAmt = getValueDouble("h_acur_dc_ttl_amt");
            hAcurTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAcurDcTtlAmtBal = getValueDouble("h_acur_dc_ttl_amt_bal");
            hAcurMinPayBal = getValueDouble("min_pay_bal");
            hAcurDcMinPayBal = getValueDouble("h_acur_dc_min_pay_bal");
            hAcurDcAdjustCrAmt = getValueDouble("adjust_cr_amt");
            hAcurDcAdjustCrAmt = getValueDouble("dc_adjust_cr_amt");
            hAcurAdjustCrCnt = getValueInt("adjust_cr_cnt");
            hAcurRowid = getValue("rowid");
        }

    }

    /***********************************************************************/
    void selectPtrWorkday() throws Exception {
        hWdayThisAcctMonth = "";
        hWdayLastAcctMonth = "";
        hWdayNextAcctMonth = "";
        hWdayThisCloseDate = "";
        hWdayLastCloseDate = "";
        hWdayNextCloseDate = "";
        hWdayThisInterestDate = "";

        sqlCmd = "select this_acct_month,";
        sqlCmd += " last_acct_month,";
        sqlCmd += " ll_acct_month,";
        sqlCmd += " next_acct_month,";
        sqlCmd += " this_close_date,";
        sqlCmd += " last_close_date,";
        sqlCmd += " next_close_date,";
        sqlCmd += " this_interest_date,";
        sqlCmd += " this_lastpay_date ";
        sqlCmd += "  from ptr_workday  ";
        sqlCmd += " where stmt_cycle = ? ";
        setString(1, hAcnoStmtCycle);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_workday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayLastAcctMonth = getValue("last_acct_month");
            hWdayIlAcctMonth = getValue("ll_acct_month");
            hWdayNextAcctMonth = getValue("next_acct_month");
            hWdayThisCloseDate = getValue("this_close_date");
            hWdayLastCloseDate = getValue("last_close_date");
            hWdayNextCloseDate = getValue("next_close_date");
            hWdayThisInterestDate = getValue("this_interest_date");
            hWdayThisLastpayDate = getValue("this_lastpay_date");
        }

    }

    /***********************************************************************/
    int procDebtData() throws Exception {
        hDeb1ReferenceSeq = hAcajReferenceNo;
        initActDebt();
        if (selectActDebt() != 0)
            selectActDebtHst();

        hDeb1InterestRsDate = hDeb1InterestDate;
        if (hAcajValueType.equals("2")) {
            hDeb1InterestRsDate = hBusiBusinessDate;
        } else if (hAcajValueType.equals("3")) {
            hDeb1InterestRsDate = hWdayNextCloseDate;
        }

        hDebtAcctMonth = hDeb1AcctMonth;
        hDebtDcEndBal = hDeb1DcEndBal;
        hDebtEndBal = hDeb1EndBal;
        hDebtDcBegBal = hDeb1DcBegBal;
        hDebtBegBal = hDeb1BegBal;
        hDebtDcDAvailableBal = hDeb1DcDAvailableBal;
        hDebtDAvailableBal = hDeb1DAvailableBal;

        hDeb1DcEndBal += (hAcajDcBefDAmt - hAcajDcAftDAmt) * -1;
        hDeb1EndBal += (hAcajBefDAmt - hAcajAftDAmt) * -1;
        if (hAcajCurrCode.equals("901"))
            hDeb1EndBal = hDeb1DcEndBal;
        if (hDeb1DcEndBal == 0)
            hDeb1EndBal = 0;

        hDebeDcEndBal = hDeb1DcEndBal;
        hDebeEndBal = hDeb1EndBal;
        /****************************************************************/
        hDeb1DcDAvailableBal += (hAcajDcBefDAmt - hAcajDcAftDAmt) * -1;
        hDeb1DAvailableBal += (hAcajBefDAmt - hAcajAftDAmt) * -1;
        if (hDeb1DcDAvailableBal > hDeb1DcBegBal)
            return (1);
        /****************************************************************/
        hAvdaVouchAmt = (hAcajDcBefDAmt - hAcajDcAftDAmt) * -1;
        hAvdaDVouchAmt = (hDebtDcEndBal - hDeb1DcEndBal) * -1;
        hAvdaProcStage = "1";
        insertActVouchData(1);

        if (hAcajCurrCode.equals("901"))
            hDeb1DcDAvailableBal = hDeb1DAvailableBal;
        if (hDeb1DcDAvailableBal == 0)
            hDeb1DAvailableBal = 0;

        hDebeDcDAvailableBal = hDeb1DcDAvailableBal;
        hDebeDAvailableBal = hDeb1DAvailableBal;
        /****************************************************************/
        hJrnlTranClass = "A";
        hJrnlTranType = hAcajAdjustType;
        hJrnlTransactionAmt = (hAcajBefDAmt - hAcajAftDAmt) * -1;
        hJrnlDcTransactionAmt = (hAcajDcBefDAmt - hAcajDcAftDAmt) * -1;
        totDcRealWaiveAmt += (hAcajDcBefDAmt - hAcajDcAftDAmt) * -1;
        totRealWaiveAmt += (hAcajBefDAmt - hAcajAftDAmt) * -1;

        insertActJrnl(2);
        updateActDebt1();
        insertCycPyaj(1);
        if (debtInt != 0) {
            insertActDebt();
            deleteActDebtHst();
        }
        /********************************************/
        hDeb1AcctMonth = hDebtAcctMonth;
        hAchtWaiveTtlBal = (hAcajDcBefDAmt - hAcajDcAftDAmt) * -1;
        updateActAcctHst();
        /********************************************/
        return (0);
    }

    /***********************************************************************/
    void insertActAcajErr() throws Exception {
        daoTable = "act_acaj_err";
        setValue("print_date", hBusiBusinessDate);
        setValue("p_seqno", hAcajPSeqno);
        setValue("curr_code", hAcajCurrCode);
        setValue("acct_type", hAcajAcctType);
        setValue("reference_no", hAcajReferenceNo);
        setValue("adjust_type", hAcajAdjustType);
        setValueDouble("beg_bal", hDeb1BegBal);
        setValueDouble("dc_beg_bal", hDeb1DcBegBal);
        setValueDouble("end_bal", hDeb1EndBal);
        setValueDouble("dc_end_bal", hDeb1DcEndBal);
        setValueDouble("d_avail_bal", hDeb1DAvailableBal); // d_available_bal
        setValueDouble("dc_d_avail_bal", hDeb1DcDAvailableBal); // dc_d_available_bal
        setValueDouble("tx_amt", hAcajCrAmt);
        setValueDouble("dc_tx_amt", hAcajDcCrAmt);
        setValue("error_reason", "02");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acaj_err duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void procFeeWaive() throws Exception {
        if (selectBilBill() != 0)
            return;
        if ((!hAcajAcctCode.equals("CF")) && (!hAcajAcctCode.equals("PF"))
                && (!hAcajAcctCode.equals("CB")) && (!hAcajAcctCode.equals("DB"))) {
            hJrnlTranClass = "A";
          //hJrnlTranType = "DR07";
            hJrnlTranType = "DR12";
            if (hBillFeesReferenceNo.length() != 0) {
                hDeb1ReferenceSeq = hBillFeesReferenceNo;
                if (hDebeDAvailableBal == hDebtBegBal) {
                    procActDebt(0);
                    /****************************************************************/
                } else {
                    procActDebt(1);
                }
                hAvdaVouchAmt = dcRealCancelAmt;
                hAvdaDVouchAmt = dcRealWaiveAmt;
                hAvdaProcStage = "4";
                insertActVouchData(1);
                /****************************************************************/
            }
            if (hBillReferenceNoFeef.length() != 0) {
                hDeb1ReferenceSeq = hBillReferenceNoFeef;
                if (hDebeDAvailableBal == hDebtBegBal) {
                    procActDebt(0);
                } else {
                    procActDebt(1);
                }
                /****************************************************************/
                hAvdaVouchAmt = dcRealCancelAmt;
                hAvdaDVouchAmt = dcRealWaiveAmt;
                hAvdaProcStage = "4";
                insertActVouchData(1);
                /****************************************************************/
            }
        }
    }

    /***********************************************************************/
    int selectBilBill() throws Exception {
        hBillFeesReferenceNo = "";
        hBillReferenceNoFeef = "";
        hBillAcctMonth = "";
        int hBillInstallCurrTerm    = 0;

        sqlCmd = "select fees_reference_no,";
        sqlCmd += " reference_no_fee_f,";
        sqlCmd += " acct_month, ";
        sqlCmd += " install_curr_term ";
        sqlCmd += "  from bil_bill ";
        sqlCmd += " where reference_no = ? ";
        setString(1, hAcajReferenceNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBillFeesReferenceNo = getValue("fees_reference_no");
            hBillReferenceNoFeef = getValue("reference_no_fee_f");
            hBillAcctMonth = getValue("acct_month");
            hBillInstallCurrTerm = getValueInt("install_curr_term");
            if (hBillInstallCurrTerm > 1) {
                hBillFeesReferenceNo  = "";
                hBillReferenceNoFeef = "";
            }
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void procActDebt(int hInt) throws Exception { /* 0:waive all 1:by acaj */

        initActDebt();
        realWaiveAmt = dcRealWaiveAmt = realCancelAmt = dcRealCancelAmt = 0;

        if (selectActDebt() != 0)
            if (selectActDebtHst() != 0)
                return;

        switch (hInt) {
        case 0:
          //dc_want_waive_amt = h_deb1_dc_beg_bal;
          //want_waive_amt = h_deb1_beg_bal;
            dcWantWaiveAmt = hDeb1DcBegBal - hDeb1DcDAvailableBal;
            wantWaiveAmt = hDeb1BegBal - hDeb1DAvailableBal;
            break;
        case 1:
            dcWantWaiveAmt = comcr.commCurrAmt(hAcajCurrCode,
                    hDeb1DcBegBal * (hAcajDcBefDAmt - hAcajDcAftDAmt) * -1 / hDebtDcBegBal, 1);
            if (dcWantWaiveAmt > hDeb1BegBal - hDeb1DAvailableBal)
                dcWantWaiveAmt = hDeb1BegBal - hDeb1DAvailableBal;
            wantWaiveAmt = comcr.commCurrAmt("901", dcWantWaiveAmt * (hDeb1BegBal / hDeb1DcBegBal), 0);
            if (wantWaiveAmt > hDeb1BegBal - hDeb1DAvailableBal)
                wantWaiveAmt = hDeb1BegBal - hDeb1DAvailableBal;
            if (hAcajCurrCode.equals("901"))
                dcWantWaiveAmt = wantWaiveAmt;
            if (dcWantWaiveAmt==0) wantWaiveAmt=0;
            break;
        case 2: /* 呼叫前設定 */
            break;
        }

        realWaiveAmt = wantWaiveAmt;
        dcRealWaiveAmt = dcWantWaiveAmt;
        hDeb1DAvailableBal += wantWaiveAmt;
        hDeb1DcDAvailableBal += dcWantWaiveAmt;

        realCancelAmt = realWaiveAmt;
        dcRealCancelAmt = dcRealWaiveAmt;
        hDeb1EndBal += realWaiveAmt;
        hDeb1DcEndBal += dcRealWaiveAmt;

        if (dcRealWaiveAmt == 0)
            return;

        totDcWaiveMinAmt += dcRealWaiveAmt;
        totWaiveMinAmt += realWaiveAmt;
        totDcRealWaiveAmt += dcRealWaiveAmt;
        totRealWaiveAmt += realWaiveAmt;

        hJrnlTransactionAmt = realWaiveAmt;
        hJrnlDcTransactionAmt = dcRealWaiveAmt;
        insertActJrnl(0);
        updateActDebt1();
        insertCycPyaj(2);
        hAchtWaiveTtlBal = realWaiveAmt;
        hAchtAcctMonth = hDeb1AcctMonth;
        updateActAcctHst();

        if (debtInt != 0) {
            insertActDebt();
            deleteActDebtHst();
        }
    }

    /***********************************************************************/
    void initActDebt() throws Exception {
        hDeb1ItemPostDate = "";
        hDeb1AcctMonth = "";
        hDeb1StmtCycle = "";
        hDeb1BegBal = 0;
        hDeb1DcBegBal = 0;
        hDeb1EndBal = 0;
        hDeb1DcEndBal = 0;
        hDeb1DAvailableBal = 0;
        hDeb1DcDAvailableBal = 0;
        hDeb1AcctCode = "";
        hDeb1InterestDate = "";
    }

    /***********************************************************************/
    int selectActDebt() throws Exception {
        hTempReferenceNo = hDeb1ReferenceSeq;
        sqlCmd = "select reference_no,"; // reference_seq
        sqlCmd += " post_date,"; // item_post_date
        sqlCmd += " acct_month,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " beg_bal,";
        sqlCmd += " dc_beg_bal,";
        sqlCmd += " end_bal,";
        sqlCmd += " dc_end_bal,";
        sqlCmd += " d_avail_bal,"; // d_available_bal
        sqlCmd += " dc_d_avail_bal,"; // dc_d_available_bal
        sqlCmd += " acct_code,"; // acct_code
        sqlCmd += " interest_date,";
        sqlCmd += " rowid as rowid ";
        sqlCmd += "  from act_debt  ";
        sqlCmd += " where reference_no= ?  "; // reference_seq
        sqlCmd += "   and decode(curr_code,'901',beg_bal,dc_beg_bal) > 0 ";
        setString(1, hDeb1ReferenceSeq);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDeb1ReferenceSeq = getValue("reference_no"); // reference_seq
            hDeb1ItemPostDate = getValue("post_date"); // item_post_date
            hDeb1AcctMonth = getValue("acct_month");
            hDeb1StmtCycle = getValue("stmt_cycle");
            hDeb1BegBal = getValueDouble("beg_bal");
            hDeb1DcBegBal = getValueDouble("dc_beg_bal");
            hDeb1EndBal = getValueDouble("end_bal");
            hDeb1DcEndBal = getValueDouble("dc_end_bal");
            hDeb1DAvailableBal = getValueDouble("d_avail_bal"); // d_available_bal
            hDeb1DcDAvailableBal = getValueDouble("dc_d_avail_bal"); // dc_d_available_bal
            hDeb1AcctCode = getValue("acct_code"); // acct_code
            hDeb1InterestDate = getValue("interest_date");
            hDeb1Rowid = getValue("rowid");
        } else
            return (1);
        debtInt = 0;
        return (0);
    }

    /***********************************************************************/
    int selectActDebtHst() throws Exception {
        hDeb1ReferenceSeq = hTempReferenceNo;
        sqlCmd = "select reference_no,"; // reference_seq
        sqlCmd += " post_date,"; // item_post_date
        sqlCmd += " acct_month,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " beg_bal,";
        sqlCmd += " dc_beg_bal,";
        sqlCmd += " end_bal,";
        sqlCmd += " dc_end_bal,";
        sqlCmd += " d_avail_bal,"; // d_available_bal
        sqlCmd += " dc_d_avail_bal,"; // dc_d_available_bal
        sqlCmd += " acct_code,"; // acct_code
        sqlCmd += " interest_date,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_debt_hst  ";
        sqlCmd += " where reference_no= ?  "; // reference_seq
        sqlCmd += "   and decode(curr_code,'901',beg_bal,dc_beg_bal) > 0 ";
        setString(1, hDeb1ReferenceSeq);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDeb1ReferenceSeq = getValue("reference_no"); // reference_seq
            hDeb1ItemPostDate = getValue("post_date"); // item_post_date
            hDeb1AcctMonth = getValue("acct_month");
            hDeb1StmtCycle = getValue("stmt_cycle");
            hDeb1BegBal = getValueDouble("beg_bal");
            hDeb1DcBegBal = getValueDouble("dc_beg_bal");
            hDeb1EndBal = getValueDouble("end_bal");
            hDeb1DcEndBal = getValueDouble("dc_end_bal");
            hDeb1DAvailableBal = getValueDouble("d_avail_bal"); // d_available_bal
            hDeb1DcDAvailableBal = getValueDouble("dc_d_avail_bal"); // dc_d_available_bal
            hDeb1AcctCode = getValue("acct_code"); // acct_code
            hDeb1InterestDate = getValue("interest_date");
            hDeb1Rowid = getValue("rowid");
        } else
            return (1);
        if (debugMode) {
            showLogMessage("I", "", String.format("STEP A3 ====== SELECT ACT_DEBT_HST ==========="));
            showLogMessage("I", "", String.format("reference_seq                  = [%s]", hDeb1ReferenceSeq));
            showLogMessage("I", "", String.format("item_post_date                 = [%s]", hDeb1ItemPostDate));
            showLogMessage("I", "", String.format("acct_month                     = [%s]", hDeb1AcctMonth));
            showLogMessage("I", "", String.format("stmt_cycle                     = [%s]", hDeb1StmtCycle));
            showLogMessage("I", "", String.format("beg_bal                        = [%f]", hDeb1BegBal));
            showLogMessage("I", "", String.format("dc_beg_bal                     = [%f]", hDeb1DcBegBal));
            showLogMessage("I", "", String.format("end_bal                        = [%f]", hDeb1EndBal));
            showLogMessage("I", "", String.format("dc_end_bal                     = [%f]", hDeb1DcEndBal));
            showLogMessage("I", "", String.format("d_available_bal                = [%f]", hDeb1DAvailableBal));
            showLogMessage("I", "", String.format("dc_d_available_bal             = [%f]", hDeb1DcDAvailableBal));
            showLogMessage("I", "", String.format("acct_code                = [%s]", hDeb1AcctCode));
            showLogMessage("I", "", String.format("interest_date                  = [%s]", hDeb1InterestDate));
            showLogMessage("I", "", String.format("STEP A3 ====== SELECT ACT_DEBT_HST ==========="));
        }
        debtInt = 1;
        return (0);
    }

    /***********************************************************************/
    void insertActJrnl(int hInt) throws Exception {
        hJrnlEnqSeqno++;
        hJrnlOrderSeq++;
        hJrnlJrnlBal = hAcurAcctJrnlBal + totRealWaiveAmt;
        hJrnlDcJrnlBal = hAcurDcAcctJrnlBal + totDcRealWaiveAmt;
        if (debugMode) {
            showLogMessage("I", "", String.format("STEP A6 ====== INSERT ACT_JRNL ==============="));
            showLogMessage("I", "", String.format("transaction_class              = [%s]", hJrnlTranClass));
            showLogMessage("I", "", String.format("transaction_type               = [%s]", hJrnlTranType));
            showLogMessage("I", "", String.format("transaction_amt                = [%f]", hJrnlTransactionAmt));
            showLogMessage("I", "", String.format("dc_transaction_amt             = [%f]", hJrnlDcTransactionAmt));
            showLogMessage("I", "", String.format("jrnl_bal,                      = [%f]", hJrnlJrnlBal));
            showLogMessage("I", "", String.format("dc_jrnl_bal                    = [%f]", hJrnlDcJrnlBal));
            showLogMessage("I", "", String.format("STEP A6 ====== INSERT ACT_JRNL ==============="));
        }
        daoTable = "act_jrnl";
        setValue("crt_date", hTempCreateDate);
        setValue("crt_time", hTempCreateTime);
        setValueInt("enq_seqno", hJrnlEnqSeqno);
        setValue("p_seqno", hAcajPSeqno);
        setValue("curr_code", hAcajCurrCode);
        setValue("acct_type", hAcajAcctType);
        setValue("corp_p_seqno", hAcnoCorpPSeqno);
        setValue("id_p_seqno", hAcnoAcctHolderId);
        setValue("acct_date", hBusiBusinessDate);
        setValue("tran_class", hJrnlTranClass);
        setValue("tran_type", hJrnlTranType);
        setValue("acct_code", hInt == 1 ? "AD" : hDeb1AcctCode);
        setValue("dr_cr", "C");
        setValueDouble("transaction_amt", hJrnlTransactionAmt);
        setValueDouble("dc_transaction_amt", hJrnlDcTransactionAmt);
        setValueDouble("jrnl_bal", hJrnlJrnlBal);
        setValueDouble("dc_jrnl_bal", hJrnlDcJrnlBal);
        setValueDouble("item_bal", hInt == 1 ? 0 : hDeb1EndBal);
        setValueDouble("dc_item_bal", hInt == 1 ? 0 : hDeb1DcEndBal);
        setValueDouble("item_d_bal", hDeb1DAvailableBal);
        setValueDouble("dc_item_d_bal", hDeb1DcDAvailableBal);
        setValue("item_date", hDeb1ItemPostDate);
        setValue("interest_date", hInt == 1 ? hAcajInterestDate : hDeb1InterestDate);
        setValue("adj_reason_code", hAcajAdjReasonCode);
        setValue("adj_comment", hAcajAdjComment);
        setValue("reference_no", hDeb1ReferenceSeq);
        setValue("value_type", hAcajValueType);
        setValue("pay_id", "");
        setValue("stmt_cycle", hAcnoStmtCycle);
        setValue("c_debt_key", hAcajCDebtKey);
        setValue("debit_item", hAcajDebitItem);
        setValue("jrnl_seqno", hJrnlJrnlSeqno);
        setValueDouble("order_seq", hJrnlOrderSeq);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_jrnl duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActDebt1() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("STEP B1 ====== UPDATE ACT_DEBT ==============="));
            showLogMessage("I", "", String.format("end_bal                        = [%f]", hDeb1EndBal));
            showLogMessage("I", "", String.format("dc_end_bal                     = [%f]", hDeb1DcEndBal));
            showLogMessage("I", "", String.format("d_available_bal                = [%f]", hDeb1DAvailableBal));
            showLogMessage("I", "", String.format("dc_d_available_bal             = [%f]", hDeb1DcDAvailableBal));
            showLogMessage("I", "", String.format("interest_rs_date               = [%s]", hDeb1InterestRsDate));
            showLogMessage("I", "", String.format("STEP A6 ====== INSERT ACT_JRNL ==============="));
        }
        if (debtInt == 0) {
            daoTable = "act_debt";
            updateSQL = "end_bal          = ?,";
            updateSQL += "dc_end_bal       = ?,";
            updateSQL += "d_avail_bal      = ?,"; // d_available_bal
            updateSQL += "dc_d_avail_bal   = ?,"; // dc_d_available_bal
            updateSQL += "interest_rs_date = ?,";
            updateSQL += "mod_time         = sysdate,";
            updateSQL += "mod_pgm          = ?";
            whereStr = "where rowid      = ? ";
            setDouble(1, hDeb1EndBal);
            setDouble(2, hDeb1DcEndBal);
            setDouble(3, hDeb1DAvailableBal);
            setDouble(4, hDeb1DcDAvailableBal);
            setString(5, hDeb1InterestRsDate);
            setString(6, javaProgram);
            setRowId(7, hDeb1Rowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_debt not found!", "", hCallBatchSeqno);
            }
        } else {
            daoTable = "act_debt_hst";
            updateSQL = "end_bal          = ?,";
            updateSQL += "dc_end_bal       = ?,";
            updateSQL += "d_avail_bal      = ?,"; // d_available_bal
            updateSQL += "dc_d_avail_bal   = ?,"; // dc_d_available_bal
            updateSQL += "interest_rs_date = ?,";
            updateSQL += "mod_time         = sysdate,";
            updateSQL += "mod_pgm          = ? ";
            whereStr = "where rowid      = ? ";
            setDouble(1, hDeb1EndBal);
            setDouble(2, hDeb1DcEndBal);
            setDouble(3, hDeb1DAvailableBal);
            setDouble(4, hDeb1DcDAvailableBal);
            setString(5, hDeb1InterestRsDate);
            setString(6, javaProgram);
            setRowId(7, hDeb1Rowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_debt_hst not found!", "", hCallBatchSeqno);
            }
        }

    }

    /***********************************************************************/
    void insertCycPyaj(int hInt) throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("STEP A7 ====== INSERT CYC_PYAJ ==============="));
            showLogMessage("I", "", String.format("payment_amount                 = [%f]", hJrnlTransactionAmt));
            showLogMessage("I", "", String.format("dc_payment_amount              = [%f]", hJrnlDcTransactionAmt));
            showLogMessage("I", "", String.format("STEP A7 ====== INSERT CYC_PYAJ ==============="));
        }
        daoTable = "cyc_pyaj";
        setValue("p_seqno", hAcajPSeqno); // p_seq
        setValue("curr_code", hAcajCurrCode);
        setValue("acct_type", hAcajAcctType);
        setValue("class_code", "A");
        setValue("payment_date", hBusiBusinessDate);
        setValueDouble("payment_amt", hJrnlTransactionAmt); // payment_amount
        setValueDouble("dc_payment_amt", hJrnlDcTransactionAmt); // dc_payment_amount
        setValue("payment_type", hJrnlTranType);
        setValue("stmt_cycle", hAcnoStmtCycle);
        setValue("settle_flag", "U"); // settlement_flag
        setValue("reference_no", hDeb1ReferenceSeq);
        setValue("fee_flag", hInt == 1 ? "N" : "Y");
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cyc_pyaj duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcctHst() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("STEP C1 ====== UPDATE ACT_ACCT_HST ==========="));
            showLogMessage("I", "", String.format("acct_month                     = [%s]", hDeb1AcctMonth));
            showLogMessage("I", "", String.format("waive_ttl_bal                  = [%f]", hAchtWaiveTtlBal));
            showLogMessage("I", "", String.format("STEP C1 ====== UPDATE ACT_ACCT_HST ==========="));
        }
        if (hAcajCurrCode.equals("901")) {
            daoTable = "act_acct_hst";
            updateSQL = "waive_ttl_bal   = waive_ttl_bal + ?,";
            updateSQL += " mod_time       = sysdate,";
            updateSQL += " mod_pgm        = ? ";
            whereStr = "where p_seqno   = ?  ";
            whereStr += "and acct_month >= ? ";
            setDouble(1, hAchtWaiveTtlBal);
            setString(2, javaProgram);
            setString(3, hAcajPSeqno);
            setString(4, hDeb1AcctMonth);
            updateTable();
        } else {
            daoTable = "act_curr_hst";
            updateSQL = "waive_ttl_bal   = waive_ttl_bal + ?,";
            updateSQL += " mod_time       = sysdate,";
            updateSQL += " mod_pgm        = ?";
            whereStr = "where p_seqno   = ?  ";
            whereStr += "and acct_month >= ?  ";
            whereStr += "and curr_code   = ? ";
            setDouble(1, hAchtWaiveTtlBal);
            setString(2, javaProgram);
            setString(3, hAcajPSeqno);
            setString(4, hDeb1AcctMonth);
            setString(5, hAcajCurrCode);
            updateTable();
        }

    }

    /***********************************************************************/
    void insertActDebt() throws Exception {
        sqlCmd = "insert into act_debt select * from act_debt_hst ";
        sqlCmd += " where  rowid = ? ";
        setRowId(1, hDeb1Rowid);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void deleteActDebtHst() throws Exception {
        daoTable = "act_debt_hst";
        whereStr = "where rowid = ? ";
        setRowId(1, hDeb1Rowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_act_debt_hst not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActVouchData(int hInt) throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("============= INSERT ACT_VOUCH_DATA ========[%d]=", hInt));
            showLogMessage("I", "", String.format("vouch_amt, mt                  = [%f]", hAvdaVouchAmt));
            showLogMessage("I", "", String.format("d_vouch_amt                    = [%f]", hAvdaDVouchAmt));
            showLogMessage("I", "", String.format("============= INSERT ACT_VOUCH_DATA ========[%d]=", hInt));
        }
        daoTable = "act_vouch_data";
        setValue("crt_date", hTempCreateDate);
        setValue("crt_time", hTempCreateTime);
        setValue("business_date", hBusiBusinessDate);
        setValue("curr_code", hAcajCurrCode);
        setValue("p_seqno", hAcajPSeqno);
        setValue("acct_type", hAcajAcctType);
        setValueDouble("vouch_amt", hAvdaVouchAmt);
        setValueDouble("d_vouch_amt", hAvdaDVouchAmt);
        setValue("vouch_data_type", hInt + "");
        setValue("acct_code", hDeb1AcctCode);
        setValue("recourse_mark", hAcnoRecourseMark);
        setValue("payment_type", hAcajAdjustType);
        setValue("proc_stage", hAvdaProcStage);
        setValueDouble("pay_amt", hAcajDcCrAmt);
        setValue("reference_no", hAcajReferenceNo);
        setValue("reference_seq", hDeb1ReferenceSeq);
        setValue("c_debt_key", hAcajCDebtKey);
        setValue("debit_item", hAcajDebitItem);
        setValue("job_code", hAcajVouchJobCode);
        setValue("src_pgm", javaProgram);
        setValue("proc_flag", "N");
        setValue("jrnl_seqno", hJrnlJrnlSeqno);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_vouch_data duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void lastCurrData() throws Exception {
        hAcurDcAcctJrnlBal += totDcRealWaiveAmt;
        hAcurAcctJrnlBal += totRealWaiveAmt;
        if (hAcajCurrCode.equals("901"))
            hAcurDcAcctJrnlBal = hAcurAcctJrnlBal;

        if (!hDebtAcctMonth.equals(hWdayNextAcctMonth)) {
            hAcurDcTtlAmtBal += totDcRealWaiveAmt;
            hAcurTtlAmtBal += totRealWaiveAmt;

            if (hAcurDcTtlAmtBal == 0)
                hAcurTtlAmtBal = 0;

            if (hAcajCurrCode.equals("901"))
                hAcurDcTtlAmtBal = hAcurTtlAmtBal;

            if (hAcurDcTtlAmtBal <= 0) {
                hAcurMinPayBal = 0;
                hAcurDcMinPayBal = 0;
                deleteActAcagCurr();
                updateActAcag();
                deleteActAcag();
            }
        }

        hAcurAdjustCrCnt++;
        hAcurDcAdjustCrAmt += totRealWaiveAmt;
        hAcurDcAdjustCrAmt += totDcRealWaiveAmt;
    }

    /***********************************************************************/
    void deleteActAcagCurr() throws Exception {
        daoTable = "act_acag_curr";
        whereStr = "where p_seqno  = ? ";
        whereStr += "and  curr_code = ? ";
        setString(1, hAcajPSeqno);
        setString(2, hAcajCurrCode);
        deleteTable();

    }

    /***********************************************************************/
    void updateActAcag() throws Exception {
        daoTable = "act_acag a";
        updateSQL = "pay_amt = (select sum(pay_amt)";
        updateSQL += " from act_acag_curr";
        whereStr = "where p_seqno = a.p_seqno  ";
        whereStr += "and acct_month = a.acct_month) where p_seqno = ? ";
        setString(1, hAcajPSeqno);
        updateTable();
    }

    /***********************************************************************/
    void deleteActAcag() throws Exception {
        daoTable = "act_acag";
        whereStr = "where p_seqno = ?  ";
        whereStr += "and  pay_amt  = 0 ";
        setString(1, hAcajPSeqno);
        deleteTable();

    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("=============== LAST ACT_ACCT_CURR ==========="));
            showLogMessage("I", "", String.format("acct_jrnl_bal                  = [%f]", hAcurAcctJrnlBal));
            showLogMessage("I", "", String.format("dc_acct_jrnl_bal               = [%f]", hAcurDcAcctJrnlBal));
            showLogMessage("I", "", String.format("adjust_cr_amt                  = [%f]", hAcurDcAdjustCrAmt));
            showLogMessage("I", "", String.format("dc_adjust_cr_amt               = [%f]", hAcurDcAdjustCrAmt));
            // showLogMessage("I", "", String.format("adjust_dr_cnt =
            // [%d]",h_acur_adjust_dr_cnt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]", hAcurTtlAmtBal));
            showLogMessage("I", "", String.format("dc_ttl_amt_bal                 = [%f]", hAcurDcTtlAmtBal));
            showLogMessage("I", "", String.format("min_pay_bal                    = [%f]", hAcurMinPayBal));
            showLogMessage("I", "", String.format("dc_min_pay_bal                 = [%f]", hAcurDcMinPayBal));
            showLogMessage("I", "", String.format("=============== LAST ACT_ACCT_CURR ==========="));
        }
        daoTable = "act_acct_curr a";
        updateSQL = "acct_jrnl_bal    = ?,";
        updateSQL += "dc_acct_jrnl_bal = ?,";
        updateSQL += "adjust_cr_amt    = ?,";
        updateSQL += "dc_adjust_cr_amt = ?,";
        updateSQL += "adjust_cr_cnt    = ?,";
        updateSQL += "ttl_amt_bal      = ?,";
        updateSQL += "dc_ttl_amt_bal   = ?,";
        updateSQL += "min_pay_bal      = ?,";
        updateSQL += "dc_min_pay_bal   = ?,";
        updateSQL += "mod_time         = sysdate,";
        updateSQL += "mod_pgm          = ?";
        whereStr = "where rowid      = ? ";
        setDouble(1, hAcurAcctJrnlBal);
        setDouble(2, hAcurDcAcctJrnlBal);
        setDouble(3, hAcurDcAdjustCrAmt);
        setDouble(4, hAcurDcAdjustCrAmt);
        setInt(5, hAcurAdjustCrCnt);
        setDouble(6, hAcurTtlAmtBal);
        setDouble(7, hAcurDcTtlAmtBal);
        setDouble(8, hAcurMinPayBal);
        setDouble(9, hAcurDcMinPayBal);
        setString(10, javaProgram);
        setRowId(11, hAcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr a not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void lastAcctData() throws Exception {
        selectActAcctCurr1();

        if (!hDebtAcctMonth.equals(hWdayNextAcctMonth))
            hAcctLastCancelDebtDate = "";

    }

    /***********************************************************************/
    void selectActAcctCurr1() throws Exception {
        sqlCmd = "select sum(acct_jrnl_bal) h_acct_acct_jrnl_bal,";
        sqlCmd += " sum(ttl_amt_bal)   h_acct_ttl_amt_bal,";
        sqlCmd += " sum(min_pay_bal)   h_acct_min_pay_bal,";
        sqlCmd += " sum(adjust_cr_amt) h_acct_adjust_cr_amt,";
        sqlCmd += " sum(adjust_cr_cnt) h_acct_adjust_cr_cnt ";
        sqlCmd += "  from act_acct_curr  ";
        sqlCmd += " where p_seqno = ? ";
        setString(1, hAcajPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct_curr not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcctAcctJrnlBal = getValueDouble("h_acct_acct_jrnl_bal");
            hAcctTtlAmtBal = getValueDouble("h_acct_ttl_amt_bal");
            hAcctMinPayBal = getValueDouble("h_acct_min_pay_bal");
            hAcctAdjustCrAmt = getValueDouble("h_acct_adjust_cr_amt");
            hAcctAdjustCrCnt = getValueInt("h_acct_adjust_cr_cnt");
        }

    }

    /***********************************************************************/
    void updateActAcct() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("=============== LAST ACT_ACCT ================"));
            showLogMessage("I", "", String.format("acct_jrnl_bal+adi              = [%f]", hAcctAcctJrnlBal));
            showLogMessage("I", "", String.format("adjust_cr_amt                  = [%f]", hAcctAdjustCrAmt));
            showLogMessage("I", "", String.format("adjust_cr_cnt                  = [%d]", hAcctAdjustCrCnt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]", hAcctTtlAmtBal));
            showLogMessage("I", "", String.format("min_pay_bal                    = [%f]", hAcctMinPayBal));
            showLogMessage("I", "",
                    String.format("last_cancel_debt_date          = [%s]", hAcctLastCancelDebtDate));
            showLogMessage("I", "", String.format("=============== LAST ACT_ACCT ================"));
        }
        daoTable = "act_acct a";
        updateSQL = "acct_jrnl_bal          = ?,";
        updateSQL += " adjust_cr_amt         = ?,";
        updateSQL += " adjust_cr_cnt         = ?,";
        updateSQL += " ttl_amt_bal           = ?,";
        updateSQL += " min_pay_bal           = ?,";
        updateSQL += " last_cancel_debt_date = ?,";
        updateSQL += " mod_time              = sysdate,";
        updateSQL += " mod_pgm               = ? ";
        whereStr = "where rowid            = ? ";
        setDouble(1, hAcctAcctJrnlBal);
        setDouble(2, hAcctAdjustCrAmt);
        setInt(3, hAcctAdjustCrCnt);
        setDouble(4, hAcctTtlAmtBal);
        setDouble(5, hAcctMinPayBal);
        setString(6, hAcctLastCancelDebtDate);
        setString(7, javaProgram);
        setRowId(8, hAcctRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct a not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
        daoTable = "act_acno";
        updateSQL = "special_comment = ?,";
        updateSQL += " mod_time       = sysdate,";
        updateSQL += " mod_pgm        = ? ";
        whereStr = "where rowid     = ? ";
        setString(1, hAcajAdjComment);
        setString(2, javaProgram);
        setRowId(3, hAcnoRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcaj() throws Exception {
        daoTable = "act_acaj";
        updateSQL = "process_flag  = 'Y',";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_pgm      = ?";
        whereStr = "where rowid   = ? ";
        setString(1, javaProgram);
        setRowId(2, hAcajRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acaj not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActF002 proc = new ActF002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    double getJRNLSeq() throws Exception {
        double seqno = 0;
        sqlCmd = "select ecs_jrnlseq.nextval nextval";
        sqlCmd += "  from dual ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            exceptExit = 0;
            comcr.errRtn("select_ecs_jrnlseq not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            seqno = getValueDouble("nextval");
        }
        return (seqno);
    }

}
