/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/29  V1.00.01    SUP       error   correction                        *
 *  108/03/06  V1.14.01    陳君暘    BECS-1080306-016  move get_dp_reference_no*
 *  108/03/08  V1.14.02    David     整合                                      *
 *  109/11/17  V1.00.03    shiyuqi   updated for project coding standard       *  
 *  111/10/25  V1.00.04    Simon     sync codes with mega                      *
 *  112/03/10  V1.00.05    Jeff      bil_postseq 取號8碼變10碼                 *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡人帳務調整(問交)處理程式*/
public class ActF003 extends AccessDAO {

    public static final boolean debugMode = false;

    private String progname = "卡人帳務調整(問交)處理程式  112/03/10  V1.00.05  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActF003";
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
    String hAcajCardNo = "";
    String hAcajAdjustType = "";
    String hAcajReferenceNo = "";
    String hAcajPostDate = "";
    double hAcajDrAmt = 0;
    double hAcajDcDrAmt = 0;
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
    double hPcglTotalBal = 0;
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
    String hAcnoAcctHolderIdCode = "";
    String hAcnoIdPSeqno = "";
    double hAcnoLineOfCreditAmt = 0;
    double hAcnoRevolveIntRate = 0;
    String hAcnoRevolveRateSMonth = "";
    String hAcnoRevolveRateEMonth = "";
    double hAcnoMinPayRate = 0;
    String hAcnoMinPayRateSMonth = "";
    String hAcnoMinPayRateEMonth = "";
    double hAcnoLastPayAmt = 0;
    String hAcnoLastPayDate = "";
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
    String hWdayThisDelaypayDate = "";
    double hAcctAcctJrnlBal = 0;
    double hAcctEndBalOp = 0;
    double hAcctTempUnbillInterest = 0;
    double hAcctMinPay = 0;
    double hAcctMinPayBal = 0;
    double hAcctRcMinPayBal = 0;
    double hAcctRcMinPayM0 = 0;
    double hAcctAdjustDrAmt = 0;
    int hAcctAdjustDrCnt = 0;
    double hAcctTtlAmt = 0;
    double hAcctTtlAmtBal = 0;
    String hAcctLastMinPayDate = "";
    String hAcctLastCancelDebtDate = "";
    String hAcctRowid = "";
    double hAcurAcctJrnlBal = 0;
    double hAcurDcAcctJrnlBal = 0;
    double hAcurMinPay = 0;
    double hAcurDcMinPay = 0;
    double hAcurMinPayBal = 0;
    double hAcurDcMinPayBal = 0;
    double hAcurTtlAmt = 0;
    double hAcurDcTtlAmt = 0;
    double hAcurTtlAmtBal = 0;
    double hAcurDcTtlAmtBal = 0;
    double hAcurBegBalOp = 0;
    double hAcurDcBegBalOp = 0;
    double hAcurEndBalOp = 0;
    double hAcurDcEndBalOp = 0;
    double hAcurTempUnbillInterest = 0;
    double hAcurDcTempUnbillInterest = 0;
    double hAcurAdjustDrAmt = 0;
    double hAcurDcAdjustDrAmt = 0;
    int hAcurAdjustDrCnt = 0;
    String hAcurDelaypayOkFlag = "";
    String hAcurRowid = "";
    String hDeb1ReferenceSeq = "";
    String hDeb1ItemPostDate = "";
    String hDeb1AcctMonth = "";
    String hDeb1StmtCycle = "";
    String hDeb1AcctCode = "";
    String hDeb1InterestDate = "";
    String hDeb1BillType = "";
    String hDeb1Rowid = "";
    int hIntrEnqSeqno = 0;
    String hJrnlTranClass = "";
    String hJrnlTranType = "";
    int hInt = 0;
    double hJrnlTransactionAmt = 0;
    double hJrnlDcTransactionAmt = 0;
    double hJrnlJrnlBal = 0;
    double hJrnlDcJrnlBal = 0;
    String hJrnlJrnlSeqno = "";
    int hJrnlOrderSeq = 0;
    String hDeb1DpReferenceNo = "";
    String hPcodInterRateCode = "";
    String hPcodInterRateCode2 = "";
    String hPcodPartRev = "";
    String hPcodRevolve = "";
    String hPcodInterestMethod = "";
    String hDebtAcctMonth = "";
    String hIntrPostDate = "";
    String hIntrAcctMonth = "";
    int hTempMonthInt = 0;
    String hIntrIntrOrgCaptial = "";
    String hIntrDcIntrOrgCaptial = "";
    String hIntrIntrSDate = "";
    String hIntrIntrEDate = "";
    String hIntrInterestSign = "";
    double hIntrInterestAmt = 0;
    double hIntrDcInterestAmt = 0;
    double hIntrInteDAmt = 0;
    double hIntrDcInteDAmt = 0;
    double hIntrInterestRate = 0;
    String hIntrRowid = "";
    double hInt2InteDAmt = 0;
    double hInt2DcInteDAmt = 0;
    double hAchtWaiveTtlBal = 0;
    double hAacrPayAmt = 0;
    double hAacrDcPayAmt = 0;
    String hAacrAcctMonth = "";
    String hAacrRowid = "";
    double hDebaEndBal = 0;
    double hAchtStmtCreditLimit = 0;
    String hAchtAcctMonth = "";
    String hAchtLastPaymentDate = "";
    double hAchtMinPayBal = 0;
    double hAgenMinPercentPayment = 0;
    double hPcreExchangeRate = 0;
    String hBillFeesReferenceNo = "";
    String hBillReferenceNoFeef = "";
    String hBillAcctMonth = "";
    String hBillContractNo = "";
    int hTempSerialNo = 0;
    int hCount = 0;
    String hAdclSerialNo = "";
    String hAacrCurrCode = "";
    String hType = "";
    double realWaiveAmt = 0;
    double dcRealWaiveAmt = 0;
    double hAvdaVouchAmt = 0;
    double hAvdaDVouchAmt = 0;
    String hAvdaProcStage = "";
    String hAvdaCashType = "";
    int hBillInstallCurrTerm = 0;
    int hBillContractSeqNo = 0;

    int totalCnt = 0;
    int debtInt = 0;
    String hDeb1InterestRsDate = "";
    String hTempReferenceNo = "";
    String hDebtBillType = "";
    String hWdayLastDelaypayDate = "";
    String hWdayllLastDelaypayDate = "";
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
                comc.errExit("Usage : ActF003 , this program need only one parameter", "");
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
    void selectActDebtCancel() throws Exception {
        sqlCmd = "select max(to_number(serial_no)) h_temp_serial_no ";
        sqlCmd += "  from act_debt_cancel  ";
        sqlCmd += " where batch_no = ? ";
        setString(1, hBusiBusinessDate + "99990003");
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempSerialNo = getValueInt("h_temp_serial_no");
        } else
            hTempSerialNo = 1;
    }

    /***********************************************************************/
    void selectActAcaj() throws Exception {
        sqlCmd = "select p_seqno,";
        sqlCmd += " decode(curr_code,'','901',curr_code) h_acaj_curr_code,";
        sqlCmd += " acct_type,";
        sqlCmd += " card_no,";
        sqlCmd += " adjust_type,";
        sqlCmd += " reference_no,";
        sqlCmd += " post_date,";
        sqlCmd += " dr_amt,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',dr_amt,dc_dr_amt) h_acaj_dc_dr_amt,";
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
        sqlCmd += " where adjust_type in ('DP01','DP04')  ";
        if (!debugMode) {
            sqlCmd += "   and decode(process_flag,'','N',process_flag) != 'Y' ";
        }
        sqlCmd += "   and apr_flag= 'Y' "; // confirm_flag
        sqlCmd += " order by vouch_job_code, p_seqno, crt_time"; // create_time
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcajPSeqno = getValue("p_seqno");
            hAcajCurrCode = getValue("h_acaj_curr_code");
            hAcajAcctType = getValue("acct_type");
            hAcajCardNo = getValue("card_no");
            hAcajAdjustType = getValue("adjust_type");
            hAcajReferenceNo = getValue("reference_no");
            hAcajPostDate = getValue("post_date");
            hAcajDrAmt = getValueDouble("dr_amt");
            hAcajDcDrAmt = getValueDouble("h_acaj_dc_dr_amt");
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

            selectPtrActgeneral();
            selectPtrCurrGeneral();
            totalCnt++;
            if (totalCnt % 1000 == 0)
                showLogMessage("I", "", String.format("   Process records[%d]", totalCnt));
            String tmpstr = String.format("%012.0f", getJRNLSeq());
            hJrnlJrnlSeqno = tmpstr;
            if (hIntrEnqSeqno > 99900)
                hIntrEnqSeqno = 0;
            hJrnlOrderSeq = 1;
            totDcRealWaiveAmt = totRealWaiveAmt = 0;
            totDcWaiveMinAmt  = totWaiveMinAmt  = 0;
            selectPtrCurrRate();
            selectActAcno();
            selectActAcct();
            selectActAcctCurr();
            selectPtrWorkday();
            if (procDebtData() != 0) {
                insertActAcajErr();
                updateActAcaj();
                continue;
            }
            if (!hDebtAcctMonth.equals(hWdayNextAcctMonth))
                proMinPay();
            if (hDebtBillType.toCharArray()[1] == '1')
                updateActMod1();
            if (hDebtBillType.toCharArray()[1] == '2')
                updateActMod2();
            procFeeWaive();
            if (hAcurDcEndBalOp > 0)
                insertActDebtCancel();

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
    void selectPtrActgeneral() throws Exception {
        hAgenMinPercentPayment = 0;
        sqlCmd = "select mp_3_rate ";
        sqlCmd += "  from ptr_actgeneral_n  ";
        sqlCmd += " where acct_type = ? ";
        setString(1, hAcajAcctType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAgenMinPercentPayment = getValueDouble("mp_3_rate");
        }
    }

    /***********************************************************************/
    void selectPtrCurrGeneral() throws Exception {
        hPcglTotalBal = 0;
        sqlCmd = "select total_bal ";
        sqlCmd += "  from ptr_curr_general  ";
        sqlCmd += " where curr_code = ? ";
        setString(1, hAcajCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_curr_general not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPcglTotalBal = getValueDouble("total_bal");
        }
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hAcnoCorpPSeqno = "";
        hAcnoAcctStatus = "";
        hAcnoStmtCycle = "";
        hAcnoAcctHolderId = "";
        hAcnoAcctHolderIdCode = "";
        hAcnoIdPSeqno = "";
        hAcnoLineOfCreditAmt = 0;
        hAcnoRevolveIntRate = 0;
        hAcnoRevolveRateSMonth = "";
        hAcnoRevolveRateEMonth = "";
        hAcnoMinPayRate = 0;
        hAcnoMinPayRateSMonth = "";
        hAcnoMinPayRateEMonth = "";
        hAcnoLastPayAmt = 0;
        hAcnoLastPayDate = "";
        hAcnoRecourseMark = "";
        hAcnoRowid = "";

        sqlCmd = "select a.corp_p_seqno,";
        sqlCmd += " a.acct_status,";
        sqlCmd += " a.stmt_cycle,";
         sqlCmd += " UF_IDNO_ID(id_p_seqno) id_no,";
         sqlCmd += " (select id_no_code from crd_idno where id_p_seqno = a.id_p_seqno) id_no_code,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " a.line_of_credit_amt,";
        sqlCmd += " decode(a.revolve_int_sign,'+',a.revolve_int_rate, a.revolve_int_rate*-1) h_acno_revolve_int_rate,";
        sqlCmd += " a.revolve_rate_s_month,";
        sqlCmd += " decode(a.revolve_rate_e_month,'','999912',a.revolve_rate_e_month) h_acno_revolve_rate_e_month,";
        sqlCmd += " a.min_pay_rate h_acno_min_pay_rate,";
        sqlCmd += " a.min_pay_rate_s_month,";
        sqlCmd += " decode(a.min_pay_rate_e_month,'','999912',a.min_pay_rate_e_month) h_acno_min_pay_rate_e_month,";
        sqlCmd += " a.last_pay_amt,";
        sqlCmd += " a.last_pay_date,";
        sqlCmd += " a.recourse_mark,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "  from act_acno a  ";
        sqlCmd += " where a.acno_p_seqno = ? ";
        setString(1, hAcajPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoAcctHolderId = getValue("id_no");
            hAcnoAcctHolderIdCode = getValue("id_no_code");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoLineOfCreditAmt = getValueDouble("line_of_credit_amt");
            hAcnoRevolveIntRate = getValueDouble("h_acno_revolve_int_rate");
            hAcnoRevolveRateSMonth = getValue("revolve_rate_s_month");
            hAcnoRevolveRateEMonth = getValue("h_acno_revolve_rate_e_month");
            hAcnoMinPayRate = getValueDouble("h_acno_min_pay_rate");
            hAcnoMinPayRateSMonth = getValue("min_pay_rate_s_month");
            hAcnoMinPayRateEMonth = getValue("h_acno_min_pay_rate_e_month");
            hAcnoLastPayAmt = getValueDouble("last_pay_amt");
            hAcnoLastPayDate = getValue("last_pay_date");
            hAcnoRecourseMark = getValue("recourse_mark");
            hAcnoRowid = getValue("rowid");
        }
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctAcctJrnlBal = 0;
        hAcctEndBalOp = 0;
        hAcctTempUnbillInterest = 0;
        hAcctMinPay = 0;
        hAcctMinPayBal = 0;
        hAcctRcMinPayBal = 0;
        hAcctRcMinPayM0 = 0;
        hAcctAdjustDrAmt = 0;
        hAcctAdjustDrCnt = 0;
        hAcctTtlAmt = 0;
        hAcctTtlAmtBal = 0;
        hAcctLastMinPayDate = "";
        hAcctLastCancelDebtDate = "";
        hAcctRowid = "";

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += " end_bal_op,";
        sqlCmd += " temp_unbill_interest,";
        sqlCmd += " min_pay,";
        sqlCmd += " min_pay_bal,";
        sqlCmd += " rc_min_pay_bal,";
        sqlCmd += " rc_min_pay_m0,";
        sqlCmd += " adjust_dr_amt h_acct_adjust_dr_amt,";
        sqlCmd += " adjust_dr_cnt h_acct_adjust_dr_cnt,";
        sqlCmd += " ttl_amt,";
        sqlCmd += " ttl_amt_bal,";
        sqlCmd += " last_min_pay_date,";
        sqlCmd += " last_cancel_debt_date,";
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
            hAcctEndBalOp = getValueDouble("end_bal_op");
            hAcctTempUnbillInterest = getValueDouble("temp_unbill_interest");
            hAcctMinPay = getValueDouble("min_pay");
            hAcctMinPayBal = getValueDouble("min_pay_bal");
            hAcctRcMinPayBal = getValueDouble("rc_min_pay_bal");
            hAcctRcMinPayM0 = getValueDouble("rc_min_pay_m0");
            hAcctAdjustDrAmt = getValueDouble("h_acct_adjust_dr_amt");
            hAcctAdjustDrCnt = getValueInt("h_acct_adjust_dr_cnt");
            hAcctTtlAmt = getValueDouble("ttl_amt");
            hAcctTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAcctLastMinPayDate = getValue("last_min_pay_date");
            hAcctLastCancelDebtDate = getValue("last_cancel_debt_date");
            hAcctRowid = getValue("rowid");
        }
        if (debugMode) {
            showLogMessage("I", "", String.format("============== FIRST ACT_ACCT ================"));
            showLogMessage("I", "", String.format("acct_jrnl_bal+adi              = [%f]", hAcctAcctJrnlBal));
            showLogMessage("I", "", String.format("end_bal_op                     = [%f]", hAcctEndBalOp));
            showLogMessage("I", "",
                    String.format("temp_unbill_interest           = [%f]", hAcctTempUnbillInterest));
            showLogMessage("I", "", String.format("min_pay_bal                    = [%f]", hAcctMinPayBal));
            showLogMessage("I", "", String.format("rc_min_pay_bal                 = [%f]", hAcctRcMinPayBal));
            showLogMessage("I", "", String.format("rc_min_pay_m0                  = [%f]", hAcctRcMinPayM0));
            showLogMessage("I", "", String.format("adjust_dr_amt                  = [%f]", hAcctAdjustDrAmt));
            showLogMessage("I", "", String.format("adjust_dr_cnt                  = [%d]", hAcctAdjustDrCnt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]", hAcctTtlAmtBal));
            showLogMessage("I", "", String.format("last_min_pay_date              = [%s]", hAcctLastMinPayDate));
            showLogMessage("I", "",
                    String.format("last_cancel_debt_date          = [%s]", hAcctLastCancelDebtDate));
            showLogMessage("I", "", String.format("============== FIRST ACT_ACCT ================"));
        }
    }

    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        hAcurAcctJrnlBal = 0;
        hAcurDcAcctJrnlBal = 0;
        hAcurMinPay = 0;
        hAcurDcMinPay = 0;
        hAcurMinPayBal = 0;
        hAcurDcMinPayBal = 0;
        hAcurTtlAmt = 0;
        hAcurDcTtlAmt = 0;
        hAcurTtlAmtBal = 0;
        hAcurDcTtlAmtBal = 0;
        hAcurBegBalOp = 0;
        hAcurDcBegBalOp = 0;
        hAcurEndBalOp = 0;
        hAcurDcEndBalOp = 0;
        hAcurTempUnbillInterest = 0;
        hAcurDcTempUnbillInterest = 0;
        hAcurAdjustDrAmt = 0;
        hAcurDcAdjustDrAmt = 0;
        hAcurAdjustDrCnt = 0;
        hAcurDelaypayOkFlag = "";

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += " dc_acct_jrnl_bal,";
        sqlCmd += " min_pay,";
        sqlCmd += " dc_min_pay,";
        sqlCmd += " min_pay_bal,";
        sqlCmd += " dc_min_pay_bal,";
        sqlCmd += " ttl_amt,";
        sqlCmd += " dc_ttl_amt,";
        sqlCmd += " ttl_amt_bal,";
        sqlCmd += " dc_ttl_amt_bal,";
        sqlCmd += " beg_bal_op,";
        sqlCmd += " dc_beg_bal_op,";
        sqlCmd += " end_bal_op,";
        sqlCmd += " dc_end_bal_op,";
        sqlCmd += " temp_unbill_interest,";
        sqlCmd += " dc_temp_unbill_interest,";
        sqlCmd += " adjust_dr_amt,";
        sqlCmd += " dc_adjust_dr_amt,";
        sqlCmd += " adjust_dr_cnt,";
        sqlCmd += " delaypay_ok_flag,";
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
            hAcurMinPay = getValueDouble("min_pay");
            hAcurDcMinPay = getValueDouble("dc_min_pay");
            hAcurMinPayBal = getValueDouble("min_pay_bal");
            hAcurDcMinPayBal = getValueDouble("dc_min_pay_bal");
            hAcurTtlAmt = getValueDouble("ttl_amt");
            hAcurDcTtlAmt = getValueDouble("dc_ttl_amt");
            hAcurTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAcurDcTtlAmtBal = getValueDouble("dc_ttl_amt_bal");
            hAcurBegBalOp = getValueDouble("beg_bal_op");
            hAcurDcBegBalOp = getValueDouble("dc_beg_bal_op");
            hAcurEndBalOp = getValueDouble("end_bal_op");
            hAcurDcEndBalOp = getValueDouble("dc_end_bal_op");
            hAcurTempUnbillInterest = getValueDouble("temp_unbill_interest");
            hAcurDcTempUnbillInterest = getValueDouble("dc_temp_unbill_interest");
            hAcurAdjustDrAmt = getValueDouble("adjust_dr_amt");
            hAcurDcAdjustDrAmt = getValueDouble("dc_adjust_dr_amt");
            hAcurAdjustDrCnt = getValueInt("adjust_dr_cnt");
            hAcurDelaypayOkFlag = getValue("delaypay_ok_flag");
            hAcurRowid = getValue("rowid");
        }
        if (debugMode) {
            showLogMessage("I", "", String.format("============== FIRST ACT_ACCT_CURR ==========="));
            showLogMessage("I", "", String.format("acct_jrnl_bal                  = [%f]", hAcurAcctJrnlBal));
            showLogMessage("I", "", String.format("dc_acct_jrnl_bal               = [%f]", hAcurDcAcctJrnlBal));
            showLogMessage("I", "", String.format("end_bal_op                     = [%f]", hAcurEndBalOp));
            showLogMessage("I", "", String.format("dc_end_bal_op                  = [%f]", hAcurDcEndBalOp));
            showLogMessage("I", "",
                    String.format("temp_unbill_interest           = [%f]", hAcurTempUnbillInterest));
            showLogMessage("I", "",
                    String.format("dc_temp_unbill_interest        = [%f]", hAcurDcTempUnbillInterest));
            showLogMessage("I", "", String.format("min_pay_bal                    = [%f]", hAcurMinPayBal));
            showLogMessage("I", "", String.format("dc_min_pay_bal                 = [%f]", hAcurDcMinPayBal));
            showLogMessage("I", "", String.format("adjust_dr_amt                  = [%f]", hAcurAdjustDrAmt));
            showLogMessage("I", "", String.format("dc_adjust_dr_amt               = [%f]", hAcurDcAdjustDrAmt));
            showLogMessage("I", "", String.format("adjust_dr_cnt                  = [%d]", hAcurAdjustDrCnt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]", hAcurTtlAmtBal));
            showLogMessage("I", "", String.format("dc_ttl_amt_bal                 = [%f]", hAcurDcTtlAmtBal));
            showLogMessage("I", "", String.format("delaypay_ok_flag               = [%s]", hAcurDelaypayOkFlag));
            showLogMessage("I", "", String.format("============== FIRST ACT_ACCT_CURR ==========="));
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
        hWdayThisDelaypayDate = "";

        sqlCmd = "select this_acct_month,";
        sqlCmd += " last_acct_month,";
        sqlCmd += " ll_acct_month,";
        sqlCmd += " next_acct_month,";
        sqlCmd += " this_close_date,";
        sqlCmd += " last_close_date,";
        sqlCmd += " next_close_date,";
        sqlCmd += " this_interest_date,";
        sqlCmd += " this_lastpay_date,";
        sqlCmd += " this_delaypay_date ";
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
            hWdayThisDelaypayDate = getValue("this_delaypay_date");
        }
    }

    /***********************************************************************/
    int procDebtData() throws Exception {
        hDeb1ReferenceSeq = hAcajReferenceNo;
        initActDebt();
        if (selectActDebt() != 0)
            selectActDebtHst();
        /*****************************************************/
        hDebtAcctMonth = hDeb1AcctMonth;
        hDebtBillType = hDeb1BillType;
        hDebtDcEndBal = hDeb1DcEndBal;
        hDebtEndBal = hDeb1EndBal;
        hDebtDcBegBal = hDeb1DcBegBal;
        hDebtBegBal = hDeb1BegBal;
        hDebtDcDAvailableBal = hDeb1DcDAvailableBal;
        hDebtDAvailableBal = hDeb1DAvailableBal;
        /*****************************************************/
        hDeb1DcEndBal -= hAcajDcDrAmt;
        hDeb1EndBal = comcr.commCurrAmt("901", hDeb1DcEndBal * (hDeb1BegBal / hDeb1DcBegBal), 0);

        if (hDeb1DcEndBal <= 0)
            hDeb1DcEndBal = hDeb1EndBal = 0;
        if (hAcajCurrCode.equals("901"))
            hDeb1EndBal = hDeb1DcEndBal;
        /****************************************************************/
        hDeb1DcDAvailableBal -= (hAcajDcBefDAmt - hAcajDcAftDAmt);
        if (hDeb1DcDAvailableBal < 0)
            return (1);
        hDeb1DAvailableBal = comcr.commCurrAmt("901",
                hDeb1DcDAvailableBal * (hDeb1BegBal / hDeb1DcBegBal), 0);

        if (hAcajCurrCode.equals("901"))
            hDeb1DAvailableBal = hDeb1DcDAvailableBal;
        if (hDeb1DcDAvailableBal == 0)
            hDeb1DAvailableBal = 0;
        /********************************************/
        hAvdaOVouchAmt = hAcajDcDrAmt;
        hAvdaVouchAmt = (hAcajDcBefDAmt - hAcajDcAftDAmt);
        hAvdaDVouchAmt = (hDebtDcEndBal - hDeb1DcEndBal);

        hAvdaProcStage = "1";
        hAvdaCashType = "";
        if (hDebtBillType.toCharArray()[1] == '2')
            hAvdaCashType = "2";
        insertActVouchData(1);
        hAvdaCashType = "";
        /********************************************/
        hAcurDcEndBalOp += (hAcajDcBefDAmt - hAcajDcAftDAmt) - (hDebtDcEndBal - hDeb1DcEndBal);
        hAcurEndBalOp += (hAcajBefDAmt - hAcajAftDAmt) - (hDebtEndBal - hDeb1EndBal);
        /********************************************/
        totDcRealWaiveAmt += hAcajDcDrAmt;
        totRealWaiveAmt += hAcajDrAmt;
        /********************************************/
        hJrnlTranClass = "A";
        hJrnlTranType = hAcajAdjustType;
        hJrnlDcTransactionAmt = hAcajDcBefDAmt - hAcajDcAftDAmt;
        hJrnlTransactionAmt = hAcajBefDAmt - hAcajAftDAmt;

        insertActJrnl(2);
        hDeb1DpReferenceNo = "";

        if (hAcajAdjustType.equals("DP01"))
            getDpReferenceNo();
        updateActDebt1();
        if (hAcajAdjustType.equals("DP01")) {
            if (debtInt == 0)
                insertActDebt(1);
            else
                insertActDebt1(1);
        }
        insertCycPyaj(1);
        /********************************************/
        selectPtrActcode();
        if ((hPcodInterestMethod.equals("Y")) && (hAcajValueType.equals("1")))
            selectActIntr();
        /********************************************/
        hDeb1AcctMonth = hDebtAcctMonth;
        hAchtWaiveTtlBal = hAcajDrAmt;
        updateActAcctHst();
        /********************************************/
        return (0);
    }

    /***********************************************************************/
    void selectPtrActcode() throws Exception {
        hPcodInterRateCode = "";
        hPcodInterRateCode2 = "";
        hPcodPartRev = "";
        hPcodRevolve = "";
        hPcodInterestMethod = "";

        sqlCmd = "select inter_rate_code,";
        sqlCmd += " inter_rate_code2,";
        sqlCmd += " part_rev,";
        sqlCmd += " revolve,";
        sqlCmd += " interest_method ";
        sqlCmd += "  from ptr_actcode  ";
        sqlCmd += " where acct_code = ? ";
        setString(1, hDeb1AcctCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actcode not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPcodInterRateCode = getValue("inter_rate_code");
            hPcodInterRateCode2 = getValue("inter_rate_code2");
            hPcodPartRev = getValue("part_rev");
            hPcodRevolve = getValue("revolve");
            hPcodInterestMethod = getValue("interest_method");
        }

    }

    /***********************************************************************/
    void selectActIntr() throws Exception {
        int maxInt = 0, debtRetCode = 0, dFlag = 0;
        String[] acctMonth = new String[250];
        double[] dcMonthWaiveAmt = new double[250];
        double[] monthWaiveAmt = new double[250];
        double totIntrWaiveAmt = 0, totDcIntrWaiveAmt = 0;

        for (int i = 0; i < 250; i++)
            dcMonthWaiveAmt[i] = monthWaiveAmt[i] = 0;

        sqlCmd = "select post_date,";
        sqlCmd += " acct_month,";
        sqlCmd += " months_between(to_date(acct_month,'yyyymm'),to_date( ? ,'yyyymm')) h_temp_month_int,";
        sqlCmd += " intr_org_captial,";
        sqlCmd += " decode( cast(? as varchar(3)) ,'901',intr_org_captial,dc_intr_org_captial) h_intr_dc_intr_org_captial,";
        sqlCmd += " intr_s_date,";
        sqlCmd += " intr_e_date,";
        sqlCmd += " interest_sign,";
        sqlCmd += " interest_amt,";
        sqlCmd += " decode( cast(? as varchar(3)) ,'901',interest_amt,dc_interest_amt) h_intr_dc_interest_amt,";
        sqlCmd += " inte_d_amt,";
        sqlCmd += " decode( cast(? as varchar(3)) ,'901',inte_d_amt,dc_inte_d_amt) h_intr_dc_inte_d_amt,";
        sqlCmd += " interest_rate,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_intr ";
        sqlCmd += " Where reference_no   = ? ";
        sqlCmd += "   and interest_sign != '-' ";
        sqlCmd += "   and inte_d_amt    != 0 ";
        sqlCmd += " order by acct_month ";
        setString(1, hDebtAcctMonth);
        setString(2, hAcajCurrCode);
        setString(3, hAcajCurrCode);
        setString(4, hAcajCurrCode);
        setString(5, hAcajReferenceNo);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hIntrPostDate = getValue("post_date", i);
            hIntrAcctMonth = getValue("acct_month", i);
            hTempMonthInt = getValueInt("h_temp_month_int", i);
            hIntrIntrOrgCaptial = getValue("intr_org_captial", i);
            hIntrDcIntrOrgCaptial = getValue("h_intr_dc_intr_org_captial", i);
            hIntrIntrSDate = getValue("intr_s_date", i);
            hIntrIntrEDate = getValue("intr_e_date", i);
            hIntrInterestSign = getValue("interest_sign", i);
            hIntrInterestAmt = getValueDouble("interest_amt", i);
            hIntrDcInterestAmt = getValueDouble("h_intr_dc_interest_amt", i);
            hIntrInteDAmt = getValueDouble("inte_d_amt", i);
            hIntrDcInteDAmt = getValueDouble("h_intr_dc_inte_d_amt", i);
            hIntrInterestRate = getValueDouble("interest_rate", i);
            hIntrRowid = getValue("rowid", i);

            if (hDeb1DcDAvailableBal == 0) {
                dcMonthWaiveAmt[hTempMonthInt] += hIntrDcInteDAmt;
                monthWaiveAmt[hTempMonthInt] += hIntrInteDAmt;
                hInt2InteDAmt = hIntrDcInteDAmt;
                hInt2DcInteDAmt = hIntrInteDAmt;
                hIntrInteDAmt = 0;
                hIntrDcInteDAmt = 0;
            } else {
                dcWantWaiveAmt = comcr.commCurrAmt(hAcajCurrCode,
                        hIntrDcInterestAmt * ((hAcajDcBefDAmt - hAcajDcAftDAmt) / hDebtDcBegBal), 1);
                wantWaiveAmt = dcWantWaiveAmt * (hDeb1BegBal / hDeb1DcBegBal);
                if (hAcajCurrCode.equals("901"))
                    wantWaiveAmt = dcWantWaiveAmt;

                if (dcWantWaiveAmt > hIntrDcInteDAmt) {
                    dcMonthWaiveAmt[hTempMonthInt] += hIntrDcInteDAmt;
                    monthWaiveAmt[hTempMonthInt] += hIntrInteDAmt;
                    hInt2DcInteDAmt = hIntrDcInteDAmt;
                    hInt2InteDAmt = hIntrInteDAmt;
                    hIntrDcInteDAmt = 0;
                    hIntrInteDAmt = 0;
                } else {
                    dcMonthWaiveAmt[hTempMonthInt] += dcWantWaiveAmt;
                    monthWaiveAmt[hTempMonthInt] += wantWaiveAmt;
                    hInt2InteDAmt = wantWaiveAmt;
                    hInt2DcInteDAmt = dcWantWaiveAmt;
                    hIntrDcInteDAmt -= dcWantWaiveAmt;
                    hIntrInteDAmt -= wantWaiveAmt;
                }
            }

            updateActIntr();

            if (hIntrAcctMonth.equals(hWdayNextAcctMonth)) {
                insertActIntr(1);
            } else {
                insertActIntr(2);
            }

            maxInt = hTempMonthInt;
            acctMonth[maxInt] = String.format("%s", hIntrAcctMonth);
        }

        for (int inti = 0; inti <= maxInt; inti++) {
            if (dcMonthWaiveAmt[inti] == 0)
                continue;
            if (acctMonth[inti].equals(hWdayNextAcctMonth)) {
                hAcurDcTempUnbillInterest -= dcMonthWaiveAmt[inti];
                hAcurTempUnbillInterest -= monthWaiveAmt[inti];
                if (hAcurTempUnbillInterest < 0)
                    hAcurDcTempUnbillInterest = hAcurTempUnbillInterest = 0;
            } else {
                /******************************************************/
                hAvdaProcStage = "2";
                hDeb1AcctMonth = acctMonth[inti];
                wantWaiveAmt = comcr.commCurrAmt("901", monthWaiveAmt[inti], 0);
                dcWantWaiveAmt = comcr.commCurrAmt(hAcajCurrCode,
                        monthWaiveAmt[inti] * (hDeb1DcBegBal / hDeb1BegBal), 0);
                if (hAcajCurrCode.equals("901"))
                    wantWaiveAmt = dcWantWaiveAmt;

                debtRetCode = selectActDebt1();
                if (debtRetCode != 0)
                    debtRetCode = selectActDebtHst1();
                if (debtRetCode == 0) {
                    hJrnlTranClass = "D";
                    hJrnlTranType = "WAIN";
                    hDeb1DpReferenceNo = "";
                    procActDebt(2);
                    if (dcWantWaiveAmt > dcRealWaiveAmt) {
                        hInt2DcInteDAmt = dcWantWaiveAmt - dcRealWaiveAmt;
                        hInt2InteDAmt = wantWaiveAmt - realWaiveAmt;
                        insertActIntr(0);
                    }
                    hAvdaVouchAmt = dcRealWaiveAmt;
                    hAvdaDVouchAmt = dcRealCancelAmt;
                    insertActVouchData(1);
                }
                /******************************************************/
                hAvdaProcStage = "3";
                debtRetCode = selectActDebt2();
                if (debtRetCode != 0)
                    debtRetCode = selectActDebtHst2();
                if (debtRetCode == 0) {
                    selectActAcctHst1();
                    dFlag = 0;
                    if (hAchtAcctMonth.equals(hWdayIlAcctMonth)) {
                        if (hAchtLastPaymentDate.compareTo(hWdayllLastDelaypayDate) <= 0) {
                            if (hAchtMinPayBal <= (hAcajDcBefDAmt - hAcajDcAftDAmt))
                                dFlag = 1;
                        }
                    } else if (hAchtAcctMonth.equals(hWdayLastAcctMonth)) {
                        if (hAchtLastPaymentDate.compareTo(hWdayLastDelaypayDate) <= 0) {
                            if (hAchtMinPayBal <= (hAcajDcBefDAmt - hAcajDcAftDAmt))
                                dFlag = 1;
                        }
                    }
                    if (dFlag == 1) {
                        hJrnlTranClass = "D";
                        hJrnlTranType = "WAPE";
                        procActDebt(0);
                        hAvdaOVouchAmt = dcRealWaiveAmt;
                        hAvdaVouchAmt = dcRealWaiveAmt;
                        hAvdaDVouchAmt = dcRealCancelAmt;
                        hAvdaProcStage = "3";
                        insertActVouchData(1);
                    }
                }
                /******************************************************/
            }
        }
        /********************************************/
        if (totDcIntrWaiveAmt > 0) {
            hJrnlTranClass = "D";
            hJrnlTranType = "WAIP";
            hJrnlDcTransactionAmt = totDcIntrWaiveAmt;
            hJrnlTransactionAmt = totIntrWaiveAmt;
            insertActJrnl(1);
            insertCycPyaj(2);
        }
    }

    /***********************************************************************/
    void updateActIntr() throws Exception {
        daoTable = "act_intr";
        updateSQL = "inte_d_amt    = ?,";
        updateSQL += "dc_inte_d_amt = ?,";
        updateSQL += "mod_time      = sysdate,";
        updateSQL += "mod_pgm       = 'ActF003'";
        whereStr = "where rowid   = ? ";
        setDouble(1, hIntrInteDAmt);
        setDouble(2, hIntrDcInteDAmt);
        setRowId(3, hIntrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_intr not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int selectActDebt1() throws Exception {
        hDeb1ReferenceSeq = "";
        sqlCmd = "select reference_no"; // reference_seq
        sqlCmd += "  from act_debt  ";
        sqlCmd += " where p_seqno =  ? ";
        sqlCmd += "   and acct_month =  ? ";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = ? ";
        sqlCmd += "   and acct_code  = 'RI' "; // acct_code
        sqlCmd += "   and txn_code   = 'IF' "; // transaction_code
        sqlCmd += "   and bill_type  = 'OSSG' ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hAcajPSeqno);
        setString(2, hDeb1AcctMonth);
        setString(3, hAcajCurrCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDeb1ReferenceSeq = getValue("reference_no"); // reference_seq
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    int selectActDebtHst1() throws Exception {
        hDeb1ReferenceSeq = "";
        sqlCmd = "select reference_no"; // reference_seq
        sqlCmd += "  from act_debt_hst  ";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and acct_month  = ?  ";
        sqlCmd += "   and acct_code   = 'RI' "; // acct_code
        sqlCmd += "   and txn_code    = 'IF' "; // transaction_code
        sqlCmd += "   and bill_type   = 'OSSG' ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hAcajPSeqno);
        setString(2, hDeb1AcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDeb1ReferenceSeq = getValue("reference_no"); // reference_seq
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertActIntr(int hInt) throws Exception {
        hIntrEnqSeqno++;
        daoTable = "act_intr";
        extendField = "acti.";
        setValue(extendField + "crt_date", hTempCreateDate);
        setValue(extendField + "crt_time", hTempCreateTime);
        setValueInt(extendField + "enq_seqno", hIntrEnqSeqno);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "post_date", hBusiBusinessDate);
        setValue(extendField + "stmt_cycle", hAcnoStmtCycle);
        setValue(extendField + "acct_month", hWdayNextAcctMonth);
        setValueDouble(extendField + "intr_org_captial", hAcajDrAmt);
        setValueDouble(extendField + "dc_intr_org_captial", hAcajDcDrAmt);
        setValue(extendField + "intr_s_date", hIntrIntrSDate);
        setValue(extendField + "intr_e_date", hIntrIntrEDate);
        setValue(extendField + "interest_sign", hInt == 0 ? "+" : "-");
        setValueDouble(extendField + "interest_amt", hInt2InteDAmt);
        setValueDouble(extendField + "dc_interest_amt", hInt2DcInteDAmt);
        setValueDouble(extendField + "inte_d_amt", hInt == 0 ? 0 : hIntrInteDAmt);
        setValueDouble(extendField + "dc_inte_d_amt", hInt == 0 ? 0 : hIntrDcInteDAmt);
        String code = "";
        switch (hInt) {
        case 0:
            code = "DB0A";
            break;
        case 1:
            code = "DB0D";
            break;
        case 2:
            code = "DB04";
            break;
        }
        setValue(extendField + "reason_code", code);
        setValueDouble(extendField + "interest_rate", hIntrInterestRate);
        setValue(extendField + "reference_no", hAcajReferenceNo);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int selectActDebt2() throws Exception {
        hDeb1ReferenceSeq = "";
        sqlCmd = "select reference_no"; // reference_seq
        sqlCmd += "  from act_debt  ";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and acct_month = ?  ";
        sqlCmd += "   and acct_code  = 'PN'  "; // acct_code
        sqlCmd += "   and txn_code   = 'DF'  "; // transaction_code
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hAcajPSeqno);
        setString(2, hDeb1AcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDeb1ReferenceSeq = getValue("reference_no"); // reference_seq
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    int selectActDebtHst2() throws Exception {
        hDeb1ReferenceSeq = "";
        sqlCmd = "select reference_no"; // reference_seq
        sqlCmd += "  from act_debt_hst  ";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and acct_month  = ?  ";
        sqlCmd += "   and acct_code   = 'PN'  "; // acct_code
        sqlCmd += "   and txn_code    = 'DF'  "; // transaction_code
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hAcajPSeqno);
        setString(2, hDeb1AcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDeb1ReferenceSeq = getValue("reference_no"); // reference_seq
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void selectActAcctHst1() throws Exception {
        hAchtAcctMonth = "";
        hAchtLastPaymentDate = "";
        hAchtMinPayBal = 0;

        sqlCmd = "select acct_month,";
        sqlCmd += " last_payment_date,";
        sqlCmd += " min_pay_bal ";
        sqlCmd += "  from act_acct_hst  ";
        sqlCmd += " where p_seqno    = ?  ";
        sqlCmd += "   and acct_month = to_char(add_months(to_date(?,'yyyymm'),-1),'yyyymm') ";
        setString(1, hAcajPSeqno);
        setString(2, hDebtAcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAchtAcctMonth = getValue("acct_month");
            hAchtLastPaymentDate = getValue("last_payment_date");
            hAchtMinPayBal = getValueDouble("min_pay_bal");
        }
    }

    /***********************************************************************/
    void insertActAcajErr() throws Exception {
        daoTable = "act_acaj_err";
        extendField = "aae.";
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
        setValueDouble(extendField + "d_avail_bal", hDeb1DAvailableBal);
        setValueDouble(extendField + "dc_d_avail_bal", hDeb1DcDAvailableBal);
        setValueDouble(extendField + "tx_amt", hAcajBefDAmt - hAcajAftDAmt);
        setValueDouble(extendField + "dc_tx_amt", hAcajDcBefDAmt - hAcajDcAftDAmt);
        setValue(extendField + "error_reason", "03");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acaj_err duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void proMinPay() throws Exception {
        totDcWaiveMinAmt = hAcajDcDrAmt;
        totWaiveMinAmt = hAcajDrAmt;
        if ((hAcurTtlAmt == hAcurMinPay) || (hAcurTtlAmtBal <= 0))
            return;

        if ((!hAcajAcctCode.equals("BL")) && (!hAcajAcctCode.equals("CA"))
                && (!hAcajAcctCode.equals("IT")) && (!hAcajAcctCode.equals("ID"))
                && (!hAcajAcctCode.equals("OT")) && (!hAcajAcctCode.equals("AO")))
            return;

        wsMinpayRate = hAgenMinPercentPayment;
        if (hAcnoMinPayRate > 0)
            if ((hBusiBusinessDate.compareTo(hAcnoMinPayRateSMonth) >= 0)
                    && (hBusiBusinessDate.compareTo(hAcnoMinPayRateEMonth) <= 0))
                wsMinpayRate = hAcnoMinPayRate;

        selectActAcagCurr1();
    }

    /***********************************************************************/
    void selectActAcagCurr1() throws Exception {
        double wsDcDrAmt, wsDrAmt, tempDrAmt;

        totDcWaiveMinAmt = totWaiveMinAmt = 0;
        wsDcDrAmt = hAcajDcDrAmt;
        wsDrAmt = hAcajDrAmt;

        sqlCmd = "select pay_amt,";
        sqlCmd += " dc_pay_amt,";
        sqlCmd += " acct_month,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acag_curr ";
        sqlCmd += " where p_seqno   = ? ";
        sqlCmd += "   and curr_code = ? ";
        sqlCmd += " order by acct_month ";
        setString(1, hAcajPSeqno);
        setString(2, hAcajCurrCode);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAacrPayAmt = getValueDouble("pay_amt", i);
            hAacrDcPayAmt = getValueDouble("dc_pay_amt", i);
            hAacrAcctMonth = getValue("acct_month", i);
            hAacrRowid = getValue("rowid", i);

            if (hAacrAcctMonth.equals(hWdayThisAcctMonth)) {
                selectActAcctHst();
                if (hAchtStmtCreditLimit == 0)
                    hAchtStmtCreditLimit = hAcnoLineOfCreditAmt;

                if (hAcctTtlAmtBal <= hAchtStmtCreditLimit) {
                    wsDcDrAmt = comcr.commCurrAmt(hAcajCurrCode, wsDcDrAmt * (wsMinpayRate / 100.0), 0);
                    wsDrAmt = comcr.commCurrAmt("901", wsDrAmt * (wsMinpayRate / 100.0), 0);
                    if (hAcajCurrCode.equals("901"))
                        wsDcDrAmt = wsDrAmt;
                } else {
                    selectActDebtA();
                    if (hDebaEndBal <= hAchtStmtCreditLimit) {
                        wsDcDrAmt = comcr.commCurrAmt(hAcajCurrCode, wsDcDrAmt * (wsMinpayRate / 100.0),
                                0);
                        wsDrAmt = comcr.commCurrAmt("901", wsDrAmt * (wsMinpayRate / 100.0), 0);
                        if (hAcajCurrCode.equals("901"))
                            wsDcDrAmt = wsDrAmt;
                    } else {
                        tempDrAmt = wsDrAmt - (hDebaEndBal - hAchtStmtCreditLimit);
                        if (tempDrAmt > 0) {
                            wsDrAmt = comcr.commCurrAmt("901", wsDrAmt * (wsMinpayRate / 100.0), 0);
                            selectPtrCurrRate();
                            wsDrAmt = comcr.commCurrAmt("901",
                                    wsDrAmt + (hDebaEndBal - hAchtStmtCreditLimit) / hPcreExchangeRate, 0);
                            wsDcDrAmt = comcr.commCurrAmt(hAcajCurrCode,
                                    wsDrAmt * (hAcajDcDrAmt / hAcajDrAmt), 0);
                            if (hAcajCurrCode.equals("901"))
                                wsDcDrAmt = wsDrAmt;
                        }
                    }
                }

                if ((hAacrDcPayAmt - wsDcDrAmt) > hAcurDcTtlAmtBal) {
                    wsDcDrAmt = hAacrDcPayAmt - hAcurDcTtlAmtBal;
                    wsDrAmt = hAacrPayAmt - hAcurTtlAmtBal;
                }
            }

            if (wsDcDrAmt >= hAacrDcPayAmt) {
                wsDcDrAmt -= hAacrDcPayAmt;
                wsDrAmt -= hAacrPayAmt;
                totDcWaiveMinAmt += hAacrDcPayAmt;
                totWaiveMinAmt += hAacrPayAmt;
                if ((wsDcDrAmt == 0) || (wsDrAmt == 0))
                    wsDcDrAmt = wsDrAmt = 0;
            } else {
                totDcWaiveMinAmt += wsDcDrAmt;
                totWaiveMinAmt += wsDrAmt;
                wsDcDrAmt = 0;
                wsDrAmt = 0;
            }

            if (wsDcDrAmt == 0) {
                break;
            }

        }
    }

    /***********************************************************************/
    void selectActAcctHst() throws Exception {
        hAchtStmtCreditLimit = 0;
        sqlCmd = "select stmt_credit_limit ";
        sqlCmd += "  from act_acct_hst  ";
        sqlCmd += " where p_seqno    = ? ";
        sqlCmd += "   and acct_month = ? ";
        setString(1, hAcajPSeqno);
        setString(2, hWdayLastAcctMonth);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct_hst not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAchtStmtCreditLimit = getValueDouble("stmt_credit_limit");
        }

    }

    /***********************************************************************/
    void selectActDebtA() throws Exception {
        hDebaEndBal = 0;
        sqlCmd = "select sum(end_bal) h_deba_end_bal ";
        sqlCmd += "  from act_debt  ";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and acct_code in ('BL','CA','IT','ID','OT','AO')  "; // acct_code
        sqlCmd += "   and curr_code   = ?  ";
        sqlCmd += "   and acct_month != ? ";
        setString(1, hAcajPSeqno);
        setString(2, hAcajCurrCode);
        setString(3, hWdayNextAcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDebaEndBal = getValueDouble("h_deba_end_bal");
        }
        hDebaEndBal += hAcajDrAmt;
    }

    /***********************************************************************/
    void selectPtrCurrRate() throws Exception {
        sqlCmd = "select exchange_rate ";
        sqlCmd += "  from ptr_curr_rate  ";
        sqlCmd += " where curr_code = ? ";
        setString(1, hAcajCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_curr_rate not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPcreExchangeRate = getValueDouble("exchange_rate");
        }

    }

    /***********************************************************************/
    void updateActMod1() throws Exception {
        daoTable = "act_mod1";
        updateSQL = "process_code = '4',";
        updateSQL += "adj_amt      = ? - ? ,";
        updateSQL += "process_date = ?,";
        updateSQL += "mod_time     = sysdate,";
        updateSQL += "mod_pgm      = 'ActF003'";
        whereStr = "where reference_no = ? ";
        setDouble(1, hDeb1BegBal);
        setDouble(2, hDeb1DAvailableBal);
        setString(3, hBusiBusinessDate);
        setString(4, hAcajReferenceNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_mod1 not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActMod2() throws Exception {
        daoTable = "act_mod2";
        updateSQL = "process_code = '4',";
        updateSQL += "adj_amt      = ? - ? ,";
        updateSQL += "process_date = ?,";
        updateSQL += "mod_time     = sysdate,";
        updateSQL += "mod_pgm      = 'ActF003'";
        whereStr = "where reference_no = ? ";
        setDouble(1, hDeb1BegBal);
        setDouble(2, hDeb1DAvailableBal);
        setString(3, hBusiBusinessDate);
        setString(4, hAcajReferenceNo);
        updateTable();
        if (notFound.equals("Y")) {
            hAvdaVouchAmt = hDeb1DcDAvailableBal;
            hAvdaDVouchAmt = hDeb1DcBegBal;
            hAvdaProcStage = "0";
            insertActVouchData(2);
        }
    }

    /***********************************************************************/
    void procFeeWaive() throws Exception {
        if (selectBilBill() != 0)
            return;
        if ((!hAcajAcctCode.equals("CF")) && (!hAcajAcctCode.equals("PF"))
                && (!hAcajAcctCode.equals("CB")) && (!hAcajAcctCode.equals("DB"))) {
            hJrnlTranClass = "A";
            if (hBillFeesReferenceNo.length() != 0) {
                hJrnlTranType = hAcajAdjustType;

                hDeb1ReferenceSeq = hBillFeesReferenceNo;
                if (hDebtDAvailableBal == 0) {
                    procActDebt(0);
                } else {
                    procActDebt(1);
                }
                /****************************************************************/
                hAvdaVouchAmt = dcRealWaiveAmt;
                hAvdaDVouchAmt = dcRealCancelAmt;
                hAvdaProcStage = "4";
                insertActVouchData(1);
                /****************************************************************/
            }
            if (hBillReferenceNoFeef.length() != 0) {
                hJrnlTranType = "DE12";
                hDeb1ReferenceSeq = hBillReferenceNoFeef;
                if (hDebtDAvailableBal == 0) {
                    procActDebt(0);
                } else {
                    procActDebt(1);
                }
                /****************************************************************/
                hAvdaVouchAmt = dcRealWaiveAmt;
                hAvdaDVouchAmt = dcRealCancelAmt;
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
        hBillContractNo = "";
        hBillContractSeqNo = 0;
        hBillInstallCurrTerm = 0;

        sqlCmd = "select fees_reference_no,";
        sqlCmd += " reference_no_fee_f,";
        sqlCmd += " acct_month,";
        sqlCmd += " contract_no,";
        sqlCmd += " contract_seq_no,";
        sqlCmd += " install_curr_term ";
        sqlCmd += "  from bil_bill  ";
        sqlCmd += " where reference_no = ? ";
        setString(1, hAcajReferenceNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBillFeesReferenceNo = getValue("fees_reference_no");
            hBillReferenceNoFeef = getValue("reference_no_fee_f");
            hBillAcctMonth = getValue("acct_month");
            hBillContractNo = getValue("contract_no");
            hBillContractSeqNo = getValueInt("contract_seq_no");
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
            dcWantWaiveAmt = hDeb1DcDAvailableBal;
            wantWaiveAmt = hDeb1DAvailableBal;
            break;
        case 1:
            dcWantWaiveAmt = comcr.commCurrAmt(hAcajCurrCode,
                    hDeb1DcBegBal * (hAcajDcBefDAmt - hAcajDcAftDAmt) / hDebtDcBegBal, 0);
            wantWaiveAmt = comcr.commCurrAmt("901", dcWantWaiveAmt * (hDeb1BegBal / hDeb1DcBegBal), 0);
            if (hAcajCurrCode.equals("901"))
                wantWaiveAmt = dcWantWaiveAmt;
            if (dcWantWaiveAmt == 0)
                wantWaiveAmt = 0;
            break;
        case 2:/* 呼叫前設定 */
            break;
        }
        if (dcWantWaiveAmt >= hDeb1DcDAvailableBal) {
            realWaiveAmt = hDeb1DAvailableBal;
            dcRealWaiveAmt = hDeb1DcDAvailableBal;
            hDeb1DcDAvailableBal = 0;
            hDeb1DAvailableBal = 0;
        } else {
            realWaiveAmt = wantWaiveAmt;
            dcRealWaiveAmt = dcWantWaiveAmt;
            hDeb1DAvailableBal -= wantWaiveAmt;
            hDeb1DcDAvailableBal -= dcWantWaiveAmt;
        }

        if (dcRealWaiveAmt > hDeb1DcEndBal) {
            realCancelAmt = hDeb1EndBal;
            dcRealCancelAmt = hDeb1DcEndBal;
            hDeb1EndBal = 0;
            hDeb1DcEndBal = 0;
        } else {
            realCancelAmt = realWaiveAmt;
            dcRealCancelAmt = dcRealWaiveAmt;
            hDeb1EndBal -= realWaiveAmt;
            hDeb1DcEndBal -= dcRealWaiveAmt;
        }

        if (dcRealWaiveAmt > 0) {
            if (!hDeb1AcctMonth.equals(hWdayNextAcctMonth)) {
              totDcWaiveMinAmt += dcRealWaiveAmt;
              totWaiveMinAmt += realWaiveAmt;
            }
            totDcRealWaiveAmt += dcRealWaiveAmt;
            totRealWaiveAmt += realWaiveAmt;

            hAcurDcEndBalOp += dcRealWaiveAmt - dcRealCancelAmt;
            hAcurEndBalOp += realWaiveAmt - realCancelAmt;
            /********************************************/
            hJrnlTransactionAmt = realWaiveAmt;
            hJrnlDcTransactionAmt = dcRealWaiveAmt;
            insertActJrnl(0);
            insertCycPyaj(2);
            hAchtWaiveTtlBal = realWaiveAmt;
            updateActAcctHst();
        }
        
        hDeb1DpReferenceNo = "";
        if (hAcajAdjustType.equals("DP01")) getDpReferenceNo();

        if ((dcRealWaiveAmt > 0) || (hDeb1DpReferenceNo.length() > 0))
            updateActDebt1();

        if ((hAcajAdjustType.equals("DP01"))
                && ((hAvdaProcStage.equals("1")) || (hAvdaProcStage.equals("4")))) {

            if (debtInt == 0)
                insertActDebt(2);
            else
                insertActDebt1(2);
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
        sqlCmd += " decode(curr_code,'901',beg_bal,dc_beg_bal) h_deb1_dc_beg_bal,";
        sqlCmd += " end_bal,";
        sqlCmd += " decode(curr_code,'901',end_bal,dc_end_bal) h_deb1_dc_end_bal,";
        sqlCmd += " d_avail_bal,"; // d_available_bal
        sqlCmd += " decode(curr_code,'901', d_avail_bal, dc_d_avail_bal) h_deb1_dc_d_available_bal,"; // d_available_bal
                                                                                                      // //dc_d_available_bal
        sqlCmd += " acct_code,"; // acct_code
        sqlCmd += " interest_date,";
        sqlCmd += " bill_type,";
        sqlCmd += " rowid rowid ";
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
            hDeb1DcBegBal = getValueDouble("h_deb1_dc_beg_bal");
            hDeb1EndBal = getValueDouble("end_bal");
            hDeb1DcEndBal = getValueDouble("h_deb1_dc_end_bal");
            hDeb1DAvailableBal = getValueDouble("d_avail_bal"); // d_available_bal
            hDeb1DcDAvailableBal = getValueDouble("h_deb1_dc_d_available_bal");
            hDeb1AcctCode = getValue("acct_code"); // acct_code
            hDeb1InterestDate = getValue("interest_date");
            hDeb1BillType = getValue("bill_type");
            hDeb1Rowid = getValue("rowid");
        } else
            return (1);
        if (debugMode) {
            showLogMessage("I", "", String.format("STEP A2 ====== SELECT ACT_DEBT ==============="));
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
            showLogMessage("I", "", String.format("bill_type                      = [%s]", hDeb1BillType));
            showLogMessage("I", "", String.format("STEP A2 ====== SELECT ACT_DEBT ==============="));
        }
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
        sqlCmd += " decode(curr_code,'901',beg_bal,dc_beg_bal) h_deb1_dc_beg_bal,";
        sqlCmd += " end_bal,";
        sqlCmd += " decode(curr_code,'901',end_bal,dc_end_bal) h_deb1_dc_end_bal,";
        sqlCmd += " d_avail_bal,"; // d_available_bal
        sqlCmd += " decode(curr_code,'901', d_avail_bal, dc_d_avail_bal) h_deb1_dc_d_available_bal,"; // d_available_bal
                                                                                                      // //dc_d_available_bal
        sqlCmd += " acct_code,"; // acct_code
        sqlCmd += " interest_date,";
        sqlCmd += " bill_type,";
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
            hDeb1DcBegBal = getValueDouble("h_deb1_dc_beg_bal");
            hDeb1EndBal = getValueDouble("end_bal");
            hDeb1DcEndBal = getValueDouble("h_deb1_dc_end_bal");
            hDeb1DAvailableBal = getValueDouble("d_avail_bal"); // d_available_bal
            hDeb1DcDAvailableBal = getValueDouble("h_deb1_dc_d_available_bal");
            hDeb1AcctCode = getValue("acct_code"); // acct_code
            hDeb1InterestDate = getValue("interest_date");
            hDeb1BillType = getValue("bill_type");
            hDeb1Rowid = getValue("rowid");
        } else
            return (1);
        debtInt = 1;
        return (0);
    }

    /***********************************************************************/
    void insertActJrnl(int hInt) throws Exception {
        hIntrEnqSeqno++;
        hJrnlOrderSeq++;

        if ((!hJrnlTranType.equals("WAIN")) && (!hJrnlTranType.equals("WAPE"))) {
            hJrnlJrnlBal = hAcurAcctJrnlBal - totRealWaiveAmt;
            hJrnlDcJrnlBal = hAcurDcAcctJrnlBal - totDcRealWaiveAmt;
        }
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
        extendField = "actjrnl.";
        setValue(extendField + "crt_date", hTempCreateDate);
        setValue(extendField + "crt_time", hTempCreateTime);
        setValueInt(extendField + "enq_seqno", hIntrEnqSeqno);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "corp_p_seqno", hAcnoCorpPSeqno);
        setValue(extendField + "id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField + "acct_date", hBusiBusinessDate);
        setValue(extendField + "tran_class", hJrnlTranClass);
        setValue(extendField + "tran_type", hJrnlTranType);
        setValue(extendField + "acct_code", hInt == 1 ? "AD" : hDeb1AcctCode);
        setValue(extendField + "dr_cr", "D");
        setValueDouble(extendField + "transaction_amt", hJrnlTransactionAmt);
        setValueDouble(extendField + "dc_transaction_amt", hJrnlDcTransactionAmt);
        setValueDouble(extendField + "jrnl_bal", hJrnlJrnlBal);
        setValueDouble(extendField + "dc_jrnl_bal", hJrnlDcJrnlBal);
        setValueDouble(extendField + "item_bal", hInt == 1 ? 0 : hDeb1EndBal);
        setValueDouble(extendField + "dc_item_bal", hInt == 1 ? 0 : hDeb1DcEndBal);
        setValueDouble(extendField + "item_d_bal", hDeb1DAvailableBal);
        setValueDouble(extendField + "dc_item_d_bal", hDeb1DcDAvailableBal);
        setValue(extendField + "item_date", hDeb1ItemPostDate);
        setValue(extendField + "interest_date", hDeb1InterestDate);
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
    void insertCycPyaj(int hInt) throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("STEP A7 ====== INSERT CYC_PYAJ ==============="));
            showLogMessage("I", "",
                    String.format("payment_amount                 = [%f]", hJrnlTransactionAmt * -1));
            showLogMessage("I", "",
                    String.format("dc_payment_amount              = [%f]", hJrnlDcTransactionAmt * -1));
            showLogMessage("I", "", String.format("STEP A7 ====== INSERT CYC_PYAJ ==============="));
        }
        daoTable = "cyc_pyaj";
        extendField = "cycpyaj.";
        setValue(extendField + "P_SEQNO", hAcajPSeqno); // p_seq
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "class_code", "A");
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
    void updateActAcctHst() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("STEP C1 ====== UPDATE ACT_ACCT_HST ==========="));
            showLogMessage("I", "", String.format("acct_month                     = [%s]", hDeb1AcctMonth));
            showLogMessage("I", "", String.format("waive_ttl_bal                  = [%f]", hAchtWaiveTtlBal));
            showLogMessage("I", "", String.format("STEP C1 ====== UPDATE ACT_ACCT_HST ==========="));
        }
        if (hAcajCurrCode.equals("901")) {
            daoTable = "act_acct_hst";
            updateSQL = "waive_ttl_bal   = waive_ttl_bal - ?,";
            updateSQL += "mod_time        = sysdate,";
            updateSQL += "mod_pgm         = 'ActF003'";
            whereStr = "where p_seqno   = ?  ";
            whereStr += "and acct_month >= ? ";
            setDouble(1, hAchtWaiveTtlBal);
            setString(2, hAcajPSeqno);
            setString(3, hDeb1AcctMonth);
            updateTable();
        } else {
            daoTable = "act_curr_hst";
            updateSQL = "waive_ttl_bal   = waive_ttl_bal - ?,";
            updateSQL += "mod_time        = sysdate,";
            updateSQL += "mod_pgm         = 'ActF003'";
            whereStr = "where p_seqno   = ?  ";
            whereStr += "and acct_month >= ?  ";
            whereStr += "and curr_code   = ? ";
            setDouble(1, hAchtWaiveTtlBal);
            setString(2, hAcajPSeqno);
            setString(3, hDeb1AcctMonth);
            setString(4, hAcajCurrCode);
            updateTable();
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
            showLogMessage("I", "", String.format("dp_reference_no                = [%s]", hDeb1DpReferenceNo));
            showLogMessage("I", "", String.format("STEP A6 ====== INSERT ACT_JRNL ==============="));
        }
        if (debtInt == 0) {
            daoTable = "act_debt";
            updateSQL = "end_bal         = ?,";
            updateSQL += "dc_end_bal      = ?,";
            updateSQL += "d_avail_bal     = ?,"; // d_available_bal
            updateSQL += "dc_d_avail_bal  = ?,"; // dc_d_available_bal
            updateSQL += "dp_reference_no = ?,";
            updateSQL += "mod_time        = sysdate,";
            updateSQL += "mod_pgm         = 'ActF003'";
            whereStr = "where rowid     = ? ";
            setDouble(1, hDeb1EndBal);
            setDouble(2, hDeb1DcEndBal);
            setDouble(3, hDeb1DAvailableBal);
            setDouble(4, hDeb1DcDAvailableBal);
            setString(5, hDeb1DpReferenceNo);
            setRowId(6, hDeb1Rowid);
            updateTable();
        } else {
            daoTable = "act_debt_hst";
            updateSQL = "end_bal         = ?,";
            updateSQL += "dc_end_bal      = ?,";
            updateSQL += "d_avail_bal     = ?,";
            updateSQL += "dc_d_avail_bal  = ?,";
            updateSQL += "dp_reference_no = ?,";
            updateSQL += "mod_time        = sysdate,";
            updateSQL += "mod_pgm         = 'ActF003'";
            whereStr = "where rowid     = ? ";
            setDouble(1, hDeb1EndBal);
            setDouble(2, hDeb1DcEndBal);
            setDouble(3, hDeb1DAvailableBal);
            setDouble(4, hDeb1DcDAvailableBal);
            setString(5, hDeb1DpReferenceNo);
            setRowId(6, hDeb1Rowid);
            updateTable();
        }
        if (notFound.equals("Y")) {
            if (!debugMode)
                comcr.errRtn("update_" + daoTable + " not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void getDpReferenceNo() throws Exception {
        sqlCmd = "select substr( ? ,3,2)|| substr(to_char(bil_postseq.nextval,'0000000000'),4,8) reference_no";
        sqlCmd += "  from dual ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dual not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDeb1DpReferenceNo = getValue("reference_no");
        }

    }

    /***********************************************************************/
    void insertActDebt(int hType) throws Exception {
        if (debugMode) {
            if (hType == 1) {
                showLogMessage("I", "", String.format("STEP F1 ====== INSERT ACT_DEBT [%d]===========", hType));
                showLogMessage("I", "",
                        String.format("beg_bal,                       = [%f]", hAcajBefDAmt - hAcajAftDAmt));
                showLogMessage("I", "", String.format("dc_beg_bal,                    = [%f]",
                        hAcajDcBefDAmt - hAcajDcAftDAmt));
                showLogMessage("I", "",
                        String.format("end_bal                        = [%f]", hAcajBefDAmt - hAcajAftDAmt));
                showLogMessage("I", "", String.format("dc_end_bal,                    = [%f]",
                        hAcajDcBefDAmt - hAcajDcAftDAmt));
                showLogMessage("I", "",
                        String.format("d_available_bal,               = [%f]", hAcajBefDAmt - hAcajAftDAmt));
                showLogMessage("I", "", String.format("dc_d_available_bal             = [%f]",
                        hAcajDcBefDAmt - hAcajDcAftDAmt));
                showLogMessage("I", "", String.format("STEP F1 ====== INSERT ACT_DEBT ==============="));
            } else {
                showLogMessage("I", "", String.format("STEP F1 ====== INSERT ACT_DEBT [%d]===========", hType));
                showLogMessage("I", "", String.format("beg_bal,                       = [%f]", realWaiveAmt));
                showLogMessage("I", "", String.format("dc_beg_bal,                    = [%f]", dcRealWaiveAmt));
                showLogMessage("I", "", String.format("end_bal                        = [%f]", 0));
                showLogMessage("I", "", String.format("dc_end_bal,                    = [%f]", 0));
                showLogMessage("I", "", String.format("d_available_bal,               = [%f]", realWaiveAmt));
                showLogMessage("I", "", String.format("dc_d_available_bal             = [%f]", dcRealWaiveAmt));
                showLogMessage("I", "", String.format("STEP F1 ====== INSERT ACT_DEBT ==============="));
            }
        }
        sqlCmd = "insert into act_debt (";
        sqlCmd += "reference_no,"; // reference_seq
        sqlCmd += "p_seqno,";
        sqlCmd += "curr_code,";
        sqlCmd += "acct_type,";
        // sqlCmd += "acct_key,";
        sqlCmd += "post_date,"; // item_post_date
        sqlCmd += "item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "acct_month,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,"; // transaction_code
        sqlCmd += "beg_bal,";
        sqlCmd += "dc_beg_bal,";
        sqlCmd += "end_bal,";
        sqlCmd += "dc_end_bal,";
        sqlCmd += "d_avail_bal,"; // d_available_bal
        sqlCmd += "dc_d_avail_bal,"; // dc_d_available_bal
        sqlCmd += "card_no,";
        sqlCmd += "acct_code,"; // acct_code
        // sqlCmd += "acct_item_cname,";
        sqlCmd += "interest_date,";
        sqlCmd += "purchase_date,";
        // sqlCmd += "acquire_date,";
        // sqlCmd += "film_no,";
        sqlCmd += "mcht_no,"; // merchant_no
        // sqlCmd += "prod_no,";
        sqlCmd += "interest_rs_date,";
        sqlCmd += "dp_reference_no,";
        sqlCmd += "crt_date,"; // update_date
        sqlCmd += "crt_user,"; // update_user
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm";
        ///////////// SELECT/////////////
        sqlCmd += ") select ";
        sqlCmd += "?,";
        sqlCmd += "p_seqno,";
        sqlCmd += "?,";
        sqlCmd += "acct_type,";
        // sqlCmd += "acct_key,";
        sqlCmd += "?,";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "?,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,"; // transaction_code
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "card_no,";
        sqlCmd += "'DP',";
        // sqlCmd += "'爭議款',";
        sqlCmd += "interest_date,";
        sqlCmd += "purchase_date,";
        // sqlCmd += "acquire_date,";
        // sqlCmd += "film_no,";
        sqlCmd += "mcht_no,"; // merchant_no
        // sqlCmd += "prod_no,";
        sqlCmd += "interest_rs_date,";
        sqlCmd += "reference_no,"; // reference_seq
        sqlCmd += "crt_date,"; // update_date
        sqlCmd += "crt_user,"; // update_user
        sqlCmd += "sysdate,";
        sqlCmd += "'ActF003'";
        sqlCmd += "  from act_debt where rowid =  ?  ";
        setString(1, hDeb1DpReferenceNo);
        setString(2, hAcajCurrCode);
        setString(3, hBusiBusinessDate);
        setString(4, hWdayNextAcctMonth);
        setDouble(5, hType == 1 ? (hAcajBefDAmt - hAcajAftDAmt) : realWaiveAmt);
        setDouble(6, hType == 1 ? (hAcajDcBefDAmt - hAcajDcAftDAmt) : dcRealWaiveAmt);
        setDouble(7, hType == 1 ? (hAcajBefDAmt - hAcajAftDAmt) : 0);
        setDouble(8, hType == 1 ? (hAcajDcBefDAmt - hAcajDcAftDAmt) : 0);
        setDouble(9, hType == 1 ? (hAcajBefDAmt - hAcajAftDAmt) : realWaiveAmt);
        setDouble(10, hType == 1 ? (hAcajDcBefDAmt - hAcajDcAftDAmt) : dcRealWaiveAmt);
        setRowId(11, hDeb1Rowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActDebt1(int hType) throws Exception {
        if (debugMode) {
            if (hType == 1) {
                showLogMessage("I", "", String.format("STEP F1 ====== INSERT ACT_DEBT [%d]===========", hType));
                showLogMessage("I", "",
                        String.format("beg_bal,                       = [%f]", hAcajBefDAmt - hAcajAftDAmt));
                showLogMessage("I", "", String.format("dc_beg_bal,                    = [%f]",
                        hAcajDcBefDAmt - hAcajDcAftDAmt));
                showLogMessage("I", "",
                        String.format("end_bal                        = [%f]", hAcajBefDAmt - hAcajAftDAmt));
                showLogMessage("I", "", String.format("dc_end_bal,                    = [%f]",
                        hAcajDcBefDAmt - hAcajDcAftDAmt));
                showLogMessage("I", "",
                        String.format("d_available_bal,               = [%f]", hAcajBefDAmt - hAcajAftDAmt));
                showLogMessage("I", "", String.format("dc_d_available_bal             = [%f]",
                        hAcajDcBefDAmt - hAcajDcAftDAmt));
                showLogMessage("I", "", String.format("STEP F1 ====== INSERT ACT_DEBT ==============="));
            } else {
                showLogMessage("I", "", String.format("STEP F1 ====== INSERT ACT_DEBT [%d]===========", hType));
                showLogMessage("I", "", String.format("beg_bal,                       = [%f]", realWaiveAmt));
                showLogMessage("I", "", String.format("dc_beg_bal,                    = [%f]", dcRealWaiveAmt));
                showLogMessage("I", "", String.format("end_bal                        = [%f]", 0));
                showLogMessage("I", "", String.format("dc_end_bal,                    = [%f]", 0));
                showLogMessage("I", "", String.format("d_available_bal,               = [%f]", realWaiveAmt));
                showLogMessage("I", "", String.format("dc_d_available_bal             = [%f]", dcRealWaiveAmt));
                showLogMessage("I", "", String.format("STEP F1 ====== INSERT ACT_DEBT ==============="));
            }
        }
        sqlCmd = "insert into act_debt (";
        sqlCmd += "reference_no,"; // reference_no
        sqlCmd += "p_seqno,";
        sqlCmd += "curr_code,";
        sqlCmd += "acct_type,";
        sqlCmd += "post_date,"; // item_post_date
        sqlCmd += "item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "acct_month,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,"; // transaction_code
        sqlCmd += "beg_bal,";
        sqlCmd += "dc_beg_bal,";
        sqlCmd += "end_bal,";
        sqlCmd += "dc_end_bal,";
        sqlCmd += "d_avail_bal,"; // d_available_bal
        sqlCmd += "dc_d_avail_bal,"; // dc_d_available_bal
        sqlCmd += "card_no,";
        sqlCmd += "acct_code,"; // acct_code
        // sqlCmd += "acct_item_cname,";
        sqlCmd += "interest_date,";
        sqlCmd += "purchase_date,";
        // sqlCmd += "acquire_date,";
        // sqlCmd += "film_no,";
        sqlCmd += "mcht_no,"; // merchant_no
        // sqlCmd += "prod_no,";
        sqlCmd += "interest_rs_date,";
        sqlCmd += "dp_reference_no,";
        sqlCmd += "crt_date,"; // update_date
        sqlCmd += "crt_user,"; // update_user
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm";
        ///////////// SELECT/////////////
        sqlCmd += ") select ";
        sqlCmd += "?,";
        sqlCmd += "p_seqno,";
        sqlCmd += "?,";
        sqlCmd += "acct_type,";
        sqlCmd += "?,";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "'00',";
        sqlCmd += "?,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "card_no,";
        sqlCmd += "'DP',";
        sqlCmd += "interest_date,";
        sqlCmd += "purchase_date,";
        sqlCmd += "mcht_no,";
        sqlCmd += "interest_rs_date,";
        sqlCmd += "reference_no,";
        sqlCmd += "crt_date,";
        sqlCmd += "crt_user,";
        sqlCmd += "sysdate,";
        sqlCmd += "'ActF003' ";
        sqlCmd += "  from act_debt_hst where rowid =  ?  ";
        setString(1, hDeb1DpReferenceNo);
        setString(2, hAcajCurrCode);
        setString(3, hBusiBusinessDate);
        setString(4, hWdayNextAcctMonth);
        setDouble(5, hType == 1 ? (hAcajBefDAmt - hAcajAftDAmt) : realWaiveAmt);
        setDouble(6, hType == 1 ? (hAcajDcBefDAmt - hAcajDcAftDAmt) : dcRealWaiveAmt);
        setDouble(7, hType == 1 ? (hAcajBefDAmt - hAcajAftDAmt) : 0);
        setDouble(8, hType == 1 ? (hAcajDcBefDAmt - hAcajDcAftDAmt) : 0);
        setDouble(9, hType == 1 ? (hAcajBefDAmt - hAcajAftDAmt) : realWaiveAmt);
        setDouble(10, hType == 1 ? (hAcajDcBefDAmt - hAcajDcAftDAmt) : dcRealWaiveAmt);
        setRowId(11, hDeb1Rowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActVouchData(int hInt) throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("STEP A4 ====== INSERT ACT_VOUCH_DATA ========="));
            showLogMessage("I", "", String.format("vouch_amt                      = [%f]", hAvdaVouchAmt));
            showLogMessage("I", "", String.format("d_vouch_amt                    = [%f]", hAvdaDVouchAmt));
            showLogMessage("I", "", String.format("STEP A4 ====== INSERT ACT_VOUCH_DATA ========="));
        }
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
        setValue(extendField + "cash_type", hAvdaCashType);
        setValueDouble(extendField + "pay_amt", hAcajDrAmt);
        setValue(extendField + "pay_card_no", hAcajCardNo);
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
        setValue(extendField + "batch_no", hBusiBusinessDate + "99990003");
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
        setValue(extendField + "update_user", javaProgram);
        setValue(extendField + "update_date", sysDate);
        setValue(extendField + "update_time", sysTime);
        setValue(extendField + "mod_user", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_debt_cancel duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void lastCurrData() throws Exception {
        hAcurAcctJrnlBal -= totRealWaiveAmt;
        hAcurDcAcctJrnlBal -= totDcRealWaiveAmt;
        if (hAcajCurrCode.equals("901"))
            hAcurAcctJrnlBal = hAcurDcAcctJrnlBal;
        /****************************************************/
        if (!hDebtAcctMonth.equals(hWdayNextAcctMonth)) {
            hAcurDcTtlAmtBal -= totDcRealWaiveAmt;
            hAcurTtlAmtBal -= totRealWaiveAmt;

            if (hAcurDcTtlAmtBal == 0)
                hAcurTtlAmtBal = 0;

            if (hAcajCurrCode.equals("901"))
                hAcurTtlAmtBal = hAcurDcTtlAmtBal;
        }

        /****************************************************/
        if (hAcurDcEndBalOp == 0)
            hAcurEndBalOp = 0;
        if (hAcajCurrCode.equals("901"))
            hAcurEndBalOp = hAcurDcEndBalOp;
        /****************************************************/
        hAcurDcMinPayBal -= totDcWaiveMinAmt;
        if (hAcurDcMinPayBal < 0)
            hAcurDcMinPayBal = 0;
        if ((hAcurDcMinPayBal >= hAcurDcTtlAmtBal) && (hAcurDcMinPayBal > 0))
            hAcurDcMinPayBal = hAcurDcTtlAmtBal;
        if (hAcurDcTtlAmtBal <= 0)
            hAcurDcMinPayBal = 0;

        selectActAcagCurr();
        deleteActAcag();
        if (hAcajCurrCode.equals("901"))
            hAcurMinPayBal = hAcurDcMinPayBal;
        /****************************************************/
        hAcurAdjustDrCnt++;
        hAcurAdjustDrAmt += totRealWaiveAmt;
        hAcurDcAdjustDrAmt += totDcRealWaiveAmt;
        if (hAcajCurrCode.equals("901"))
            hAcurAdjustDrAmt = hAcurDcAdjustDrAmt;

        if (hAcurDcTtlAmtBal <= hPcglTotalBal)
            if (hBusiBusinessDate.compareTo(hWdayThisDelaypayDate) <= 0)
                hAcurDelaypayOkFlag = "Y";
    }

    /***********************************************************************/
    void selectActAcagCurr() throws Exception {
        double tempDcDouble = 0;

        tempDcDouble = hAcurDcMinPayBal;
        hAcurMinPayBal = 0;
        sqlCmd = "select curr_code,";
        sqlCmd += " acct_month,";
        sqlCmd += " pay_amt,";
        sqlCmd += " dc_pay_amt,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acag_curr ";
        sqlCmd += " where p_seqno   = ? ";
        sqlCmd += "   and curr_code = ? ";
        sqlCmd += " order by acct_month DESC ";
        setString(1, hAcajPSeqno);
        setString(2, hAcajCurrCode);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAacrCurrCode = getValue("curr_code", i);
            hAacrAcctMonth = getValue("acct_month", i);
            hAacrPayAmt = getValueDouble("pay_amt", i);
            hAacrDcPayAmt = getValueDouble("dc_pay_amt", i);
            hAacrRowid = getValue("rowid", i);

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
            if (hAcajCurrCode.equals("901"))
                hAacrPayAmt = hAacrDcPayAmt;
            if ((hAacrPayAmt == 0) || (hAacrDcPayAmt == 0))
                hAacrPayAmt = hAacrDcPayAmt = 0;
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
        updateSQL = "pay_amt     = ?,";
        updateSQL += "dc_pay_amt  = ?";
        whereStr = "where rowid = ? ";
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
        daoTable = "act_acag a";
        updateSQL = "pay_amt = (select nvl(sum(pay_amt),0)";
        updateSQL += " from act_acag_curr";
        whereStr = "where p_seqno = a.p_seqno  ";
        whereStr += "and acct_month = a.acct_month) where p_seqno = ?  ";
        whereStr += "and acct_month = ? ";
        setString(1, hAcajPSeqno);
        setString(2, hAacrAcctMonth);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acag a not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteActAcag() throws Exception {
        daoTable = "act_acag";
        whereStr = "where p_seqno = ? ";
        whereStr += "  and pay_amt = 0 ";
        setString(1, hAcajPSeqno);
        deleteTable();

    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("=============== LAST ACT_ACCT_CURR ==========="));
            showLogMessage("I", "", String.format("acct_jrnl_bal                  = [%f]", hAcurAcctJrnlBal));
            showLogMessage("I", "", String.format("dc_acct_jrnl_bal               = [%f]", hAcurDcAcctJrnlBal));
            showLogMessage("I", "", String.format("end_bal_op                     = [%f]", hAcurEndBalOp));
            showLogMessage("I", "", String.format("dc_end_bal_op                  = [%f]", hAcurDcEndBalOp));
            showLogMessage("I", "",
                    String.format("temp_unbill_interest           = [%f]", hAcurTempUnbillInterest));
            showLogMessage("I", "",
                    String.format("dc_temp_unbill_interest        = [%f]", hAcurDcTempUnbillInterest));
            showLogMessage("I", "", String.format("min_pay_bal                    = [%f]", hAcurMinPayBal));
            showLogMessage("I", "", String.format("dc_min_pay_bal                 = [%f]", hAcurDcMinPayBal));
            showLogMessage("I", "", String.format("adjust_dr_amt                  = [%f]", hAcurAdjustDrAmt));
            showLogMessage("I", "", String.format("dc_adjust_dr_amt               = [%f]", hAcurDcAdjustDrAmt));
            showLogMessage("I", "", String.format("adjust_dr_cnt                  = [%d]", hAcurAdjustDrCnt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]", hAcurTtlAmtBal));
            showLogMessage("I", "", String.format("dc_ttl_amt_bal                 = [%f]", hAcurDcTtlAmtBal));
            showLogMessage("I", "", String.format("delaypay_ok_flag               = [%s]", hAcurDelaypayOkFlag));
            showLogMessage("I", "", String.format("=============== LAST ACT_ACCT_CURR ==========="));
        }
        daoTable = "act_acct_curr a";
        updateSQL = "acct_jrnl_bal           = ?,";
        updateSQL += "dc_acct_jrnl_bal        = ?,";
        updateSQL += "end_bal_op              = ?,";
        updateSQL += "dc_end_bal_op           = ?,";
        updateSQL += "temp_unbill_interest    = ?,";
        updateSQL += "dc_temp_unbill_interest = ?,";
        updateSQL += "min_pay_bal             = ?,";
        updateSQL += "dc_min_pay_bal          = ?,";
        updateSQL += "adjust_dr_amt           = ?,";
        updateSQL += "dc_adjust_dr_amt        = ?,";
        updateSQL += "adjust_dr_cnt           = ?,";
        updateSQL += "ttl_amt_bal             = ?,";
        updateSQL += "dc_ttl_amt_bal          = ?,";
        updateSQL += "delaypay_ok_flag        = ?,";
        updateSQL += "mod_time                = sysdate,";
        updateSQL += "mod_pgm                 = 'ActF003'";
        whereStr = "where rowid             = ? ";
        setDouble(1, hAcurAcctJrnlBal);
        setDouble(2, hAcurDcAcctJrnlBal);
        setDouble(3, hAcurEndBalOp);
        setDouble(4, hAcurDcEndBalOp);
        setDouble(5, hAcurTempUnbillInterest);
        setDouble(6, hAcurDcTempUnbillInterest);
        setDouble(7, hAcurMinPayBal);
        setDouble(8, hAcurDcMinPayBal);
        setDouble(9, hAcurAdjustDrAmt);
        setDouble(10, hAcurDcAdjustDrAmt);
        setInt(11, hAcurAdjustDrCnt);
        setDouble(12, hAcurTtlAmtBal);
        setDouble(13, hAcurDcTtlAmtBal);
        setString(14, hAcurDelaypayOkFlag);
        setRowId(15, hAcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr a not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void lastAcctData() throws Exception {
        selectActAcctCurr1();

        hAcctRcMinPayBal -= totWaiveMinAmt;
        if (hAcctRcMinPayBal < 0)
            hAcctRcMinPayBal = 0;

        if (hAcctRcMinPayBal < hAcctRcMinPayM0)
            hAcctRcMinPayM0 = hAcctRcMinPayBal;

        if ((hAcctMinPayBal == 0) && (hAcctMinPay > 0)) {
            if ((hAcctLastMinPayDate.length() == 0)
                    || (hBusiBusinessDate.compareTo(hAcctLastMinPayDate) < 0))
                hAcctLastMinPayDate = hBusiBusinessDate;
        }

        if ((hAcctTtlAmtBal <= 0) && (hAcctTtlAmt > 0)) {
            if ((hAcctLastCancelDebtDate.length() == 0)
                    || (hBusiBusinessDate.compareTo(hAcctLastCancelDebtDate) < 0))
                hAcctLastCancelDebtDate = hBusiBusinessDate;
        }

    }

    /***********************************************************************/
    void selectActAcctCurr1() throws Exception {
        sqlCmd = "select sum(acct_jrnl_bal) h_acct_acct_jrnl_bal,";
        sqlCmd += " sum(ttl_amt_bal)   h_acct_ttl_amt_bal,";
        sqlCmd += " sum(ttl_amt_bal)   h_acct_ttl_amt_bal,";
        sqlCmd += " sum(min_pay_bal)   h_acct_min_pay_bal,";
        sqlCmd += " sum(end_bal_op)    h_acct_end_bal_op,";
        sqlCmd += " sum(adjust_dr_amt) h_acct_adjust_dr_amt,";
        sqlCmd += " sum(adjust_dr_cnt) h_acct_adjust_dr_cnt,";
        sqlCmd += " sum(temp_unbill_interest) h_acct_temp_unbill_interest ";
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
            hAcctEndBalOp = getValueDouble("h_acct_end_bal_op");
            hAcctAdjustDrAmt = getValueDouble("h_acct_adjust_dr_amt");
            hAcctAdjustDrCnt = getValueInt("h_acct_adjust_dr_cnt");
            hAcctTempUnbillInterest = getValueDouble("h_acct_temp_unbill_interest");
        }

    }

    /***********************************************************************/
    void updateActAcct() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", String.format("=============== LAST ACT_ACCT ================"));
            showLogMessage("I", "", String.format("acct_jrnl_bal+adi              = [%f]", hAcctAcctJrnlBal));
            showLogMessage("I", "", String.format("end_bal_op                     = [%f]", hAcctEndBalOp));
            showLogMessage("I", "",
                    String.format("temp_unbill_interest           = [%f]", hAcctTempUnbillInterest));
            showLogMessage("I", "", String.format("min_pay_bal                    = [%f]", hAcctMinPayBal));
            showLogMessage("I", "", String.format("rc_min_pay_bal                 = [%f]", hAcctRcMinPayBal));
            showLogMessage("I", "", String.format("rc_min_pay_m0                  = [%f]", hAcctRcMinPayM0));
            showLogMessage("I", "", String.format("adjust_dr_amt                  = [%f]", hAcctAdjustDrAmt));
            showLogMessage("I", "", String.format("adjust_dr_cnt                  = [%d]", hAcctAdjustDrCnt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]", hAcctTtlAmtBal));
            showLogMessage("I", "", String.format("last_min_pay_date              = [%s]", hAcctLastMinPayDate));
            showLogMessage("I", "",
                    String.format("last_cancel_debt_date          = [%s]", hAcctLastCancelDebtDate));
            showLogMessage("I", "", String.format("=============== LAST ACT_ACCT ================"));
        }
        daoTable = "act_acct a";
        updateSQL = "acct_jrnl_bal         = ?,";
        updateSQL += "end_bal_op            = ?,";
        updateSQL += "temp_unbill_interest  = ?,";
        updateSQL += "min_pay_bal           = ?,";
        updateSQL += "rc_min_pay_bal        = ?,";
        updateSQL += "rc_min_pay_m0         = ?,";
        updateSQL += "adjust_dr_amt         = ?,";
        updateSQL += "adjust_dr_cnt         = ?,";
        updateSQL += "ttl_amt_bal           = ?,";
        updateSQL += "last_min_pay_date     = ?,";
        updateSQL += "last_cancel_debt_date = ?,";
        updateSQL += "mod_time              = sysdate,";
        updateSQL += "mod_pgm               = 'ActF003'";
        whereStr = "where rowid           = ? ";
        setDouble(1, hAcctAcctJrnlBal);
        setDouble(2, hAcctEndBalOp);
        setDouble(3, hAcctTempUnbillInterest);
        setDouble(4, hAcctMinPayBal);
        setDouble(5, hAcctRcMinPayBal);
        setDouble(6, hAcctRcMinPayM0);
        setDouble(7, hAcctAdjustDrAmt);
        setInt(8, hAcctAdjustDrCnt);
        setDouble(9, hAcctTtlAmtBal);
        setString(10, hAcctLastMinPayDate);
        setString(11, hAcctLastCancelDebtDate);
        setRowId(12, hAcctRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct a not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
        daoTable = "act_acno";
        updateSQL = "special_comment = ?,";
        updateSQL += "mod_time        = sysdate,";
        updateSQL += "mod_pgm         = 'ActF003'";
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
        updateSQL += "mod_pgm       = 'ActF003'";
        whereStr = "where rowid   = ? ";
        setRowId(1, hAcajRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acaj not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActF003 proc = new ActF003();
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
