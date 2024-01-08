/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/29  V1.00.01    SUP       error   correction                        *
  *  109/11/17  V1.00.02    shiyuqi       updated for project coding standard     *  
 ******************************************************************************/

package Act;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡人帳務調整(沖帳還原)處理程式*/
public class ActF005 extends AccessDAO {

    private boolean debugMode = true;
    private String progname = "卡人帳務調整(沖帳還原)處理程式  109/11/17  V1.00.02  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hBusiBusinessDate = "";
    String hTempCreateDate = "";
    String hTempCreateTime = "";
    String hAcajPSeqno = "";
    String hAcajCurrCode = "";
    String hAcajAcctType = "";
    String hAcajAcctKey = "";
    String hAcajAdjustType = "";
    String hAcajReferenceNo = "";
    String hAcajPostDate = "";
    double hAcajCrAmt = 0;
    double hAcajDcCrAmt = 0;
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
    int seqno = 0;
    String hAcnoCorpPSeqno = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoAcctHolderId = "";
    String hAcnoAcctHolderIdCode = "";
    String hAcnoIdPSeqno = "";
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
    int hAcurPayCnt = 0;
    double hAcurPayAmt = 0;
    double hAcurDcPayAmt = 0;
    double hAcurAdjustCrAmt = 0;
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
    double hJrnlTransactionAmt = 0;
    double hJrnlDcTransactionAmt = 0;
    double hJrnlJrnlBal = 0;
    double hJrnlDcJrnlBal = 0;
    String hAcajInterestDate = "";
    String hJrnlJrnlSeqno = "";
    int hJrnlOrderSeq = 0;
    String hJrn1CreateDate = "";
    String hJrn1CreateTime = "";
    String hJrn1PSeqno = "";
    String hJrn1CurrCode = "";
    String hJrn1AcctType = "";
    String hJrn1AcctKey = "";
    String hJrn1CorpPSeqno = "";
    String hJrn1IdPSeqno = "";
    String hJrn1AcctDate = "";
    String hJrn1TranClass = "";
    String hJrn1TranType = "";
    String hJrn1AcctCode = "";
    String hJrn1DrCr = "";
    double hJrn1TransactionAmt = 0;
    double hJrn1DcTransactionAmt = 0;
    double hJrn1JrnlBal = 0;
    double hJrn1DcJrnlBal = 0;
    double hJrn1ItemBal = 0;
    double hJrn1DcItemBal = 0;
    double hJrn1ItemDBal = 0;
    double hJrn1DcItemDBal = 0;
    String hJrn1ItemDate = "";
    String hJrn1InterestDate = "";
    String hJrn1AdjReasonCode = "";
    String hJrn1AdjComment = "";
    String hJrn1ReferenceNo = "";
    String hJrn1ValueType = "";
    String hJrn1PayId = "";
    String hJrn1StmtCycle = "";
    String hJrn1CDebtKey = "";
    String hJrn1DebitItem = "";
    String hJrn1BatchNo = "";
    String hJrn1SerialNo = "";
    String hJrn1OrderSeq = "";
    int hInt = 0;
    String hPya1PSeq = "";
    String hPya1AcctType = "";
    String hPya1AcctKey = "";
    String hPya1ClassCode = "";
    String hPya1PaymentDate = "";
    double hPya1PaymentAmount = 0;
    double hPya1DcPaymentAmount = 0;
    String hPya1PaymentType = "";
    String hPya1StmtCycle = "";
    String hPya1SettlementFlag = "";
    String hPya1ReferenceNo = "";
    String hPya1FeeFlag = "";
    int hAcctPayCnt = 0;
    double hAcctPayAmt = 0;
    double hAcctMinPayBal = 0;
    String hAcctLastCancelDebtDate = "";
    double hAvdaVouchAmt = 0;
    double hAvdaDVouchAmt = 0;
    String hAvdaProcStage = "";
    int hTempSerialNo = 0;
    int hCount = 0;
    String hAdclSerialNo = "";

    int totalCnt = 0;
    int debtInt = 0;
    int hAcctAdjustDrCnt = 0;
    String hDebtAcctMonth = "";
    String hDeb1InterestRsDate = "";
    String hTempReferenceNo = "";
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

    int hAgenMinPercentPayment = 0;
    double hPcglTotalBal = 0;

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
                comc.errExit("Usage : ActF005 , this program need only one parameter  ", "");
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

            selectActDebtCancel();
            hJrnlOrderSeq = 1;
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
    int selectActDebtCancel() throws Exception {
        sqlCmd = "select max(to_number(serial_no)) h_temp_serial_no ";
        sqlCmd += "  from act_debt_cancel  ";
        sqlCmd += " where batch_no = ? ";
        setString(1, hBusiBusinessDate + "99990005");
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempSerialNo = getValueInt("h_temp_serial_no");
        } else
            hTempSerialNo = 1;

        return 0;
    }

    /***********************************************************************/
    void selectActAcaj() throws Exception {

        sqlCmd = "select p_seqno,";
        sqlCmd += " decode(curr_code,'','901',curr_code) h_acaj_curr_code,";
        sqlCmd += " acct_type,";
        sqlCmd += " UF_ACNO_KEY(p_seqno) acct_key,";
        sqlCmd += " adjust_type,";
        sqlCmd += " reference_no,";
        sqlCmd += " post_date,";
        sqlCmd += " cr_amt,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',cr_amt,dc_cr_amt) h_acaj_dc_cr_amt,";
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
        sqlCmd += " where adjust_type = 'DR11' ";
        sqlCmd += "   and decode(process_flag,'','N',process_flag) != 'Y' ";
        sqlCmd += "   and apr_flag    = 'Y' "; // confirm_flag
        sqlCmd += " order by vouch_job_code, p_seqno, CRT_TIME"; // create_time
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcajPSeqno = getValue("p_seqno");
            hAcajCurrCode = getValue("h_acaj_curr_code");
            hAcajAcctType = getValue("acct_type");
            hAcajAcctKey = getValue("acct_key");
            hAcajAdjustType = getValue("adjust_type");
            hAcajReferenceNo = getValue("reference_no");
            hAcajPostDate = getValue("post_date");
            hAcajCrAmt = getValueDouble("cr_amt");
            hAcajDcCrAmt = getValueDouble("h_acaj_dc_cr_amt");
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

            totalCnt++;
            if(debugMode)
                showLogMessage("I", "",
                    String.format("p_seqno[%s] curr_code[%s] acct_key[%s-%s] adjust_type[%s] reference_no[%s] dc_cr_amt[%f]",
                            hAcajPSeqno, hAcajCurrCode, hAcajAcctType, hAcajAcctKey, hAcajAdjustType,
                            hAcajReferenceNo, hAcajDcCrAmt));
            if (totalCnt % 1000 == 0)
                showLogMessage("I", "", String.format("   Process records[%d]", totalCnt));
            /***************** Initial data value **********************/
            String tmpstr = String.format("%012.0f", getJRNLSeq());
            hJrnlJrnlSeqno = tmpstr;
            if (hJrnlEnqSeqno > 99900)
                hJrnlEnqSeqno = 0;
            totDcRealWaiveAmt = totRealWaiveAmt = 0;
            /***************** Initial data value **********************/
            selectActAcno();
            selectActAcct();
            selectActAcctCurr();
            selectPtrWorkday();
            if(debugMode)
                showLogMessage("I", "", String.format("STEP 1"));
            /*************************************************************/
            if (procDebtData() != 0) {
                insertActAcajErr();
                updateActAcaj();
                continue;
            }
            if(debugMode)
                showLogMessage("I", "", String.format("STEP 2"));
            /********************************************/
            insertActDebtCancel();
            if(debugMode)
                showLogMessage("I", "", String.format("STEP 6"));
            lastCurrData();
            updateActAcctCurr();
            if(debugMode)
                showLogMessage("I", "", String.format("STEP 7"));
            lastAcctData();
            updateActAcct();
            updateActAcno();
            if(debugMode)
                showLogMessage("I", "", String.format("STEP 8"));

            updateActAcaj();
            /********************************************/
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hAcnoCorpPSeqno = "";
        hAcnoAcctStatus = "";
        hAcnoStmtCycle = "";
        hAcnoAcctHolderId = "";
        hAcnoAcctHolderIdCode = "";
        hAcnoIdPSeqno = "";
        hAcnoRecourseMark = "";
        hAcnoRowid = "";

        sqlCmd = "select a.corp_p_seqno,";
        sqlCmd += " a.acct_status,";
        sqlCmd += " a.stmt_cycle,";
         sqlCmd += " b.id_no,";
         sqlCmd += " b.id_no_code,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " a.recourse_mark,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "  from act_acno a ";
        sqlCmd += " left join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += " where a.acno_p_seqno = ? ";
        setString(1, hAcajPSeqno);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno not found!", "", hCallBatchSeqno);
        }
        hAcnoCorpPSeqno = getValue("corp_p_seqno");
        hAcnoAcctStatus = getValue("acct_status");
        hAcnoStmtCycle = getValue("stmt_cycle");
        hAcnoAcctHolderId = getValue("id_no");
        hAcnoAcctHolderIdCode = getValue("id_no_code");
        hAcnoIdPSeqno = getValue("id_p_seqno");
        hAcnoRecourseMark = getValue("recourse_mark");
        hAcnoRowid = getValue("rowid");
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
        hAcurPayCnt = 0;
        hAcurPayAmt = 0;
        hAcurDcPayAmt = 0;
        hAcurAdjustCrAmt = 0;
        hAcurDcAdjustCrAmt = 0;
        hAcurAdjustCrCnt = 0;

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += " dc_acct_jrnl_bal,";
        sqlCmd += " ttl_amt,";
        sqlCmd += " decode(curr_code,'901',ttl_amt,dc_ttl_amt) h_acur_dc_ttl_amt,";
        sqlCmd += " ttl_amt_bal,";
        sqlCmd += " decode(curr_code,'901',ttl_amt_bal,dc_ttl_amt_bal) h_acur_dc_ttl_amt_bal,";
        sqlCmd += " min_pay_bal,";
        sqlCmd += " decode(curr_code,'901',min_pay_bal,dc_min_pay_bal) h_acur_dc_min_pay_bal,";
        sqlCmd += " pay_cnt,";
        sqlCmd += " pay_amt,";
        sqlCmd += " dc_pay_amt,";
        sqlCmd += " adjust_cr_amt,";
        sqlCmd += " dc_adjust_cr_amt,";
        sqlCmd += " adjust_cr_cnt,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acct_curr  ";
        sqlCmd += " where p_seqno = ?  ";
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
            hAcurPayCnt = getValueInt("pay_cnt");
            hAcurPayAmt = getValueDouble("pay_amt");
            hAcurDcPayAmt = getValueDouble("dc_pay_amt");
            hAcurAdjustCrAmt = getValueDouble("adjust_cr_amt");
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
        hDeb1InterestRsDate = hAcajInterestDate;

        hDebtAcctMonth = hDeb1AcctMonth;
        hDebtDcEndBal = hDeb1DcEndBal;
        hDebtEndBal = hDeb1EndBal;
        hDebtDcBegBal = hDeb1DcBegBal;
        hDebtBegBal = hDeb1BegBal;
        hDebtDcDAvailableBal = hDeb1DcDAvailableBal;
        hDebtDAvailableBal = hDeb1DAvailableBal;

        hDeb1DcEndBal += hAcajDcCrAmt;
        if (hDeb1DcEndBal > hDebtDcBegBal)
            return (1);
        hDeb1EndBal = comcr.commCurrAmt("901", hDeb1DcEndBal * (hDeb1BegBal / hDeb1DcBegBal), 0);
        if (hAcajCurrCode.equals("901"))
            hDeb1EndBal = hDeb1DcEndBal;
        if ((hDeb1DcEndBal <= 0) || (hDeb1EndBal <= 0))
            hDeb1DcEndBal = hDeb1EndBal = 0;

        hDebeDcEndBal = hDeb1DcEndBal;
        hDebeEndBal = hDeb1EndBal;
        /****************************************************************/
        hAvdaVouchAmt = hAcajDcCrAmt;
        hAvdaDVouchAmt = hDeb1DcEndBal - hDebtDcEndBal;
        hAvdaProcStage = "1";
        insertActVouchData(1);
        /****************************************************************/
        hJrnlTranClass = "A";
        hJrnlTranType = hAcajAdjustType;
        hJrnlTransactionAmt = hAcajCrAmt;
        hJrnlDcTransactionAmt = hAcajDcCrAmt;
        totDcRealWaiveAmt += hAcajDcCrAmt;
        totRealWaiveAmt += hAcajCrAmt;

        insertActJrnl(2);
        updateActDebt1();
        insertCycPyaj(1);
        if (debtInt != 0) {
            insertActDebt();
            deleteActDebtHst();
        }
        /********************************************/
        return (0);
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
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_debt  ";
        sqlCmd += " where reference_no= ? "; // reference_seq
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
        sqlCmd += " where reference_no= ? "; // reference_seq
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
        debtInt = 1;
        return (0);
    }

    /***********************************************************************/
    void insertActVouchData(int hInt) throws Exception {
        daoTable = "act_vouch_data";
        extendField = "actvouchd.";
        setValue(extendField + "crt_date", hTempCreateDate);
        setValue(extendField + "crt_time", hTempCreateTime);
        setValue(extendField + "business_date", hBusiBusinessDate);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValueDouble(extendField + "vouch_amt", hAvdaVouchAmt);
        setValueDouble(extendField + "d_vouch_amt", hAvdaDVouchAmt);
        setValue(extendField + "vouch_data_type", hInt + "");
        setValue(extendField + "acct_code", hDeb1AcctCode);
        setValue(extendField + "recourse_mark", hAcnoRecourseMark);
        setValue(extendField + "payment_type", hAcajAdjustType);
        setValue(extendField + "proc_stage", hAvdaProcStage);
        setValueDouble(extendField + "pay_amt", hAcajCrAmt);
        setValue(extendField + "reference_no", hAcajReferenceNo);
        setValue(extendField + "reference_seq", hDeb1ReferenceSeq);
        setValue(extendField + "c_debt_key", hAcajCDebtKey);
        setValue(extendField + "debit_item", hAcajDebitItem);
        setValue(extendField + "job_code", hAcajVouchJobCode);
        setValue(extendField + "src_pgm", javaProgram);
        setValue(extendField + "proc_flag", "N");
        setValue(extendField + "jrnl_seqno", hJrnlJrnlSeqno);
        setValue(extendField + "mod_pgm", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_vouch_data duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertActJrnl(int hInt) throws Exception {
        hJrnlEnqSeqno++;
        hJrnlOrderSeq++;
        hJrnlJrnlBal = hAcurAcctJrnlBal + totRealWaiveAmt;
        hJrnlDcJrnlBal = hAcurDcAcctJrnlBal + totDcRealWaiveAmt;

        daoTable = "act_jrnl";
        extendField = "actjrnl.";
        setValue(extendField + "crt_date", hTempCreateDate);
        setValue(extendField + "crt_time", hTempCreateTime);
        setValueInt(extendField + "enq_seqno", hJrnlEnqSeqno);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "corp_p_seqno", hAcnoCorpPSeqno);
        setValue(extendField + "id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField + "acct_date", hBusiBusinessDate);
        setValue(extendField + "tran_class", hJrnlTranClass);
        setValue(extendField + "tran_type", hJrnlTranType);
        setValue(extendField + "acct_code", hDeb1AcctCode);
        setValue(extendField + "dr_cr", "C");
        setValueDouble(extendField + "transaction_amt", hJrnlTransactionAmt);
        setValueDouble(extendField + "dc_transaction_amt", hJrnlDcTransactionAmt);
        setValueDouble(extendField + "jrnl_bal", hJrnlJrnlBal);
        setValueDouble(extendField + "dc_jrnl_bal", hJrnlDcJrnlBal);
        setValueDouble(extendField + "item_bal", hDeb1EndBal);
        setValueDouble(extendField + "dc_item_bal", hDeb1DcEndBal);
        setValueDouble(extendField + "item_d_bal", hDeb1DAvailableBal);
        setValueDouble(extendField + "dc_item_d_bal", hDeb1DcDAvailableBal);
        setValue(extendField + "item_date", hDeb1ItemPostDate);
        setValue(extendField + "interest_date", hAcajInterestDate);
        setValue(extendField + "adj_reason_code", hAcajAdjReasonCode);
        setValue(extendField + "adj_comment", hAcajAdjComment);
        setValue(extendField + "reference_no", hDeb1ReferenceSeq);
        setValue(extendField + "value_type", hAcajValueType);
        setValue(extendField + "pay_id", "");
        setValue(extendField + "stmt_cycle", hAcnoStmtCycle);
        setValue(extendField + "c_debt_key", hAcajCDebtKey);
        setValue(extendField + "debit_item", hAcajDebitItem);
        setValue(extendField + "jrnl_seqno", hJrnlJrnlSeqno);
        setValueInt(extendField + "order_seq", hJrnlOrderSeq);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_jrnl duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActDebt1() throws Exception {
        if (debtInt == 0) {
            daoTable = "act_debt";
            updateSQL = "end_bal        = ?,";
            updateSQL += "dc_end_bal     = ?,";
            updateSQL += "interest_date  = ?,";
            updateSQL += "mod_time       = sysdate,";
            updateSQL += "mod_pgm        = 'ActF005'";
            whereStr = "where rowid    = ? ";
            setDouble(1, hDeb1EndBal);
            setDouble(2, hDeb1DcEndBal);
            setString(3, hDeb1InterestDate);
            setRowId(4, hDeb1Rowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_debt not found!", "", hCallBatchSeqno);
            }
        } else {
            daoTable = "act_debt_hst";
            updateSQL = "end_bal        = ?,";
            updateSQL += "dc_end_bal     = ?,";
            updateSQL += "interest_date  = ?,";
            updateSQL += "mod_time       = sysdate,";
            updateSQL += "mod_pgm        = 'ActF005'";
            whereStr = "where rowid    = ? ";
            setDouble(1, hDeb1EndBal);
            setDouble(2, hDeb1DcEndBal);
            setString(3, hDeb1InterestDate);
            setRowId(4, hDeb1Rowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_debt_hst not found!", "", hCallBatchSeqno);
            }
        }
        if (debugMode) {
            showLogMessage("I", "", String.format("=== UPDATE ACT_DEBT (%d)==========", debtInt));
            showLogMessage("I", "", String.format("rowid[%s]                  ", hDeb1Rowid));
            showLogMessage("I", "", String.format("end_ball[%f]               ", hDeb1EndBal));
            showLogMessage("I", "", String.format("dc_end_ball[%f]            ", hDeb1DcEndBal));
            showLogMessage("I", "", String.format("d_available_ball[%f]       ", hDeb1DAvailableBal));
            showLogMessage("I", "", String.format("dc_d_available_ball[%f]    ", hDeb1DcDAvailableBal));
            showLogMessage("I", "", String.format("=================================="));
        }
    }

    /***********************************************************************/
    void insertCycPyaj(int hInt) throws Exception {
        daoTable = "cyc_pyaj";
        extendField = "cycpyaj.";
        setValue(extendField + "P_SEQNO", hAcajPSeqno); // p_seq
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "class_code", "P");
        setValue(extendField + "payment_date", hBusiBusinessDate);
        setValueDouble(extendField + "PAYMENT_AMT", hJrnlTransactionAmt * -1); // payment_amount
        setValueDouble(extendField + "DC_PAYMENT_AMT", hJrnlDcTransactionAmt * -1); // dc_payment_amount
        setValue(extendField + "payment_type", hJrnlTranType);
        setValue(extendField + "stmt_cycle", hAcnoStmtCycle);
        setValue(extendField + "SETTLE_FLAG", "U"); // settlement_flag
        setValue(extendField + "reference_no", hDeb1ReferenceSeq);
        setValue(extendField + "fee_flag", hInt == 1 ? "N" : "Y");
        setValue(extendField + "mod_pgm", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cyc_pyaj duplicate!", "", hCallBatchSeqno);
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
    void insertActAcajErr() throws Exception {
        daoTable = "act_acaj_err";
        extendField = "actacajerr.";
        setValue(extendField + "print_date", hBusiBusinessDate);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "reference_no", hAcajReferenceNo);
        setValue(extendField + "adjust_type", hAcajAdjustType);
        setValueDouble(extendField + "beg_bal", hDeb1BegBal);
        setValueDouble(extendField + "dc_beg_bal", hDeb1DcBegBal);
        setValueDouble(extendField + "end_bal", hDeb1EndBal);
        setValueDouble(extendField + "dc_end_bal", hDeb1DcEndBal);
        setValueDouble(extendField + "d_avail_bal", hDeb1DAvailableBal); // d_available_bal
        setValueDouble(extendField + "dc_d_avail_bal", hDeb1DcDAvailableBal); // dc_d_available_bal
        setValueDouble(extendField + "tx_amt", hAcajCrAmt);
        setValueDouble(extendField + "dc_tx_amt", hAcajDcCrAmt);
        setValue(extendField + "error_reason", "05");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acaj_err duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActDebtCancel() throws Exception {
        int hCount = 0;

        sqlCmd = "select count(*) h_count ";
        sqlCmd += "  from act_debt_cancel  ";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and substr(batch_no,9,4) = '9999' ";
        setString(1, hAcajPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCount = getValueInt("h_count");
        }

        if (hCount > 0)
            return;

        hTempSerialNo++;
        hAdclSerialNo = String.format("%05d", hTempSerialNo);

        daoTable = "act_debt_cancel";
        extendField = "actdebtcancel.";
        setValue(extendField + "batch_no", hBusiBusinessDate + "99990005");
        setValue(extendField + "serial_no", hAdclSerialNo);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "id_p_seqno", hAcnoIdPSeqno);
      //setValue(extendField + "id", h_acno_acct_holder_id);
      //setValue(extendField + "id_code", h_acno_acct_holder_id_code);
        setValueDouble(extendField + "pay_amt", 0);
        setValueDouble(extendField + "dc_pay_amt", 0);
        setValue(extendField + "pay_date", "19900101");
        setValue(extendField + "payment_type", "DUMY");
        setValue(extendField + "update_user", "ActF005");
        setValue(extendField + "update_date", sysDate);
        setValue(extendField + "update_time", sysTime);
        setValue(extendField + "mod_user", "ActF005");
        setValue(extendField + "mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_debt_cancel duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void lastCurrData() throws Exception {
        hAcurAcctJrnlBal += totRealWaiveAmt;
        hAcurDcAcctJrnlBal += totDcRealWaiveAmt;
        if (hAcajCurrCode.equals("901"))
            hAcurDcAcctJrnlBal = hAcurAcctJrnlBal;
        if (!hDebtAcctMonth.equals(hWdayNextAcctMonth)) {
            hAcurDcTtlAmtBal += totDcRealWaiveAmt;
            hAcurTtlAmtBal += totRealWaiveAmt;

            if (hAcurDcTtlAmtBal == 0)
                hAcurTtlAmtBal = 0;

            if (hAcajCurrCode.equals("901"))
                hAcurTtlAmtBal = hAcurDcTtlAmtBal;

            if (hAcurDcTtlAmtBal <= 0) {
                hAcurMinPayBal = 0;
                hAcurDcMinPayBal = 0;
                deleteActAcagCurr();
                updateActAcag();
                deleteActAcag();
            }
        }

        hAcurAdjustCrCnt++;
        hAcurAdjustCrAmt += totRealWaiveAmt;
        hAcurDcAdjustCrAmt += totDcRealWaiveAmt;

        hAcurDcPayAmt -= hAcajDcCrAmt;
        hAcurPayAmt -= comcr.commCurrAmt("901", hAcajDcCrAmt * (hDebtBegBal / hDebtDcBegBal), 0);
        if (hAcajCurrCode.equals("901"))
            hAcurPayAmt -= hAcajDcCrAmt;
        if ((hAcurDcPayAmt <= 0) || (hAcurPayAmt <= 0)) {
            hAcurDcPayAmt = 0;
            hAcurPayAmt = 0;
        }
        hAcurPayCnt--;
        if (hAcurPayCnt < 0)
            hAcurPayCnt = 0;
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
        updateSQL = "pay_amt = (select sum(pay_amt) from act_acag_curr";
        whereStr = "where p_seqno  = a.p_seqno  ";
        whereStr += "and acct_month = a.acct_month) where p_seqno = ? ";
        setString(1, hAcajPSeqno);
        updateTable();

    }

    /***********************************************************************/
    void deleteActAcag() throws Exception {
        daoTable = "act_acag";
        whereStr = "where p_seqno = ?  ";
        whereStr += "  and pay_amt = 0 ";
        setString(1, hAcajPSeqno);
        deleteTable();

    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        daoTable = "act_acct_curr a";
        updateSQL = "acct_jrnl_bal    = ?,";
        updateSQL += "dc_acct_jrnl_bal = ?,";
        updateSQL += "adjust_cr_amt    = ?,";
        updateSQL += "dc_adjust_cr_amt = ?,";
        updateSQL += "adjust_cr_cnt    = ?,";
        updateSQL += "pay_cnt          = ?,";
        updateSQL += "pay_amt          = ?,";
        updateSQL += "dc_pay_amt       = ?,";
        updateSQL += "ttl_amt_bal      = ?,";
        updateSQL += "dc_ttl_amt_bal   = ?,";
        updateSQL += "min_pay_bal      = ?,";
        updateSQL += "dc_min_pay_bal   = ?,";
        updateSQL += "mod_time         = sysdate,";
        updateSQL += "mod_pgm          = 'ActF005'";
        whereStr = "where rowid      = ? ";
        setDouble(1, hAcurAcctJrnlBal);
        setDouble(2, hAcurDcAcctJrnlBal);
        setDouble(3, hAcurAdjustCrAmt);
        setDouble(4, hAcurDcAdjustCrAmt);
        setInt(5, hAcurAdjustCrCnt);
        setInt(6, hAcurPayCnt);
        setDouble(7, hAcurPayAmt);
        setDouble(8, hAcurDcPayAmt);
        setDouble(9, hAcurTtlAmtBal);
        setDouble(10, hAcurDcTtlAmtBal);
        setDouble(11, hAcurMinPayBal);
        setDouble(12, hAcurDcMinPayBal);
        setRowId(13, hAcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr a not found!", "", hCallBatchSeqno);
        }
        if (debugMode) {
            showLogMessage("I", "", String.format("=== UPDATE ACT_ACCT_CURR ========="));
            showLogMessage("I", "", String.format("rowid[%s]                  ", hAcurRowid));
            showLogMessage("I", "", String.format("adjust_cr_amt[%f]          ", hAcurAdjustCrAmt));
            showLogMessage("I", "", String.format("dc_adjust_cr_amt[%f]       ", hAcurDcAdjustCrAmt));
            showLogMessage("I", "", String.format("ttl_amt_bal[%f]            ", hAcurTtlAmtBal));
            showLogMessage("I", "", String.format("dc_ttl_amt_bal[%f]         ", hAcurDcTtlAmtBal));
            showLogMessage("I", "", String.format("=================================="));
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
        sqlCmd += " sum(ttl_amt_bal)   h_acct_ttl_amt_bal,";
        sqlCmd += " sum(min_pay_bal)   h_acct_min_pay_bal,";
        sqlCmd += " sum(pay_cnt)       h_acct_pay_cnt,";
        sqlCmd += " sum(pay_amt)       h_acct_pay_amt,";
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
            hAcctTtlAmtBal = getValueDouble("h_acct_ttl_amt_bal");
            hAcctMinPayBal = getValueDouble("h_acct_min_pay_bal");
            hAcctPayCnt = getValueInt("h_acct_pay_cnt");
            hAcctPayAmt = getValueDouble("h_acct_pay_amt");
            hAcctAdjustCrAmt = getValueDouble("h_acct_adjust_cr_amt");
            hAcctAdjustCrCnt = getValueInt("h_acct_adjust_cr_cnt");
        }

    }

    /***********************************************************************/
    void updateActAcct() throws Exception {
        daoTable = "act_acct a";
        updateSQL = "acct_jrnl_bal         = ?,";
        updateSQL += "adjust_cr_amt         = ?,";
        updateSQL += "pay_cnt               = ?,";
        updateSQL += "pay_amt               = ?,";
        updateSQL += "adjust_cr_cnt         = ?,";
        updateSQL += "ttl_amt_bal           = ?,";
        updateSQL += "min_pay_bal           = ?,";
        updateSQL += "last_cancel_debt_date = ?,";
        updateSQL += "mod_time              = sysdate,";
        updateSQL += "mod_pgm               = 'ActF005'";
        whereStr = "where rowid           = ? ";
        setDouble(1, hAcctAcctJrnlBal);
        setDouble(2, hAcctAdjustCrAmt);
        setInt(3, hAcctPayCnt);
        setDouble(4, hAcctPayAmt);
        setInt(5, hAcctAdjustCrCnt);
        setDouble(6, hAcctTtlAmtBal);
        setDouble(7, hAcctMinPayBal);
        setString(8, hAcctLastCancelDebtDate);
        setRowId(9, hAcctRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct a not found!", "", hCallBatchSeqno);
        }
        if (debugMode) {
            showLogMessage("I", "", String.format("=== UPDATE ACT_ACCT =============="));
            showLogMessage("I", "", String.format("rowid[%s]                  ", hAcctRowid));
            showLogMessage("I", "", String.format("acct_jrnl_ball[%f]         ", hAcctAcctJrnlBal));
            showLogMessage("I", "", String.format("adjust_cr_amtl[%f]         ", hAcctAdjustCrAmt));
            showLogMessage("I", "", String.format("adjust_dr_cnt[%d]          ", hAcctAdjustDrCnt));
            showLogMessage("I", "", String.format("ttl_amt_bal[%f]            ", hAcctTtlAmtBal));
            showLogMessage("I", "", String.format("last_cancel_debt_date[%s]  ", hAcctLastCancelDebtDate));
            showLogMessage("I", "", String.format("=================================="));
        }
    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
        daoTable = "act_acno";
        updateSQL = "special_comment = ?,";
        updateSQL += "mod_time        = sysdate,";
        updateSQL += "mod_pgm         = 'ActF005'";
        whereStr = "where rowid     = ? ";
        setString(1, hAcajAdjComment);
        setRowId(2, hAcnoRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcaj() throws Exception {
        daoTable = "act_acaj";
        updateSQL = "process_flag  = 'Y',";
        updateSQL += "mod_time      = sysdate,";
        updateSQL += "mod_pgm       = 'ActF005'";
        whereStr = "where rowid   = ? ";
        setRowId(1, hAcajRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acaj not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActF005 proc = new ActF005();
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
