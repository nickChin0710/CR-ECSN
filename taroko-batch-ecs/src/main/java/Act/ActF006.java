/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/29  V1.00.01    SUP       error   correction                        *
 *  108/03/07  V1.12.01    陳君暘    BECS-1080306-016 curr_term > 1, fee = null*
 *  108/03/08  V1.12.02    David     整合                                      *
 *  109/11/17  V1.00.03    shiyuqi   updated for project coding standard       *  
 *  111/10/25  V1.00.04    Simon     sync codes with mega                      *
 ******************************************************************************/

package Act;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡人帳務調整(問交結案)處理程式*/
public class ActF006 extends AccessDAO {

    private boolean debugMode = false;

    private String progname = "卡人帳務調整(問交結案)處理程式   111/10/25  V1.00.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActF006";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRprogramCode = "";
    String buf = "";

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptId1 = "ACT_F006";
    String rptName1 = "問交D檔費用加回明細表";
    int rptSeq1 = 0;
    String szTmp = "";

    String hBusiBusinessDate = "";
    String hTempCreateDate = "";
    String hTempCreateTime = "";
    String hAcajPSeqno = "";
    String hAcajCurrCode = "";
    String hAcajAcctType = "";
    String hAcajAcctKey = "";
    String hAcajCardNo = "";
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
    String hPlemCloResult = "";
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
    double hAcctEndBalOp = 0;
    double hAcctEndBalLk = 0;
    double hAcctAdjustCrAmt = 0;
    int hAcctAdjustCrCnt = 0;
    double hAcctTtlAmt = 0;
    double hAcctTtlAmtBal = 0;
    String hAcctRowid = "";
    double hAcurAcctJrnlBal = 0;
    double hAcurDcAcctJrnlBal = 0;
    double hAcurEndBalOp = 0;
    double hAcurDcEndBalOp = 0;
    double hAcurEndBalLk = 0;
    double hAcurDcEndBalLk = 0;
    double hAcurTtlAmt = 0;
    double hAcurDcTtlAmt = 0;
    double hAcurTtlAmtBal = 0;
    double hAcurDcTtlAmtBal = 0;
    double hAcurMinPayBal = 0;
    double hAcurDcMinPayBal = 0;
    int hAcurPayCnt = 0;
    double hAcurAdjustCrAmt = 0;
    double hAcurDcAdjustCrAmt = 0;
    int hAcurAdjustCrCnt = 0;
    String hAcurRowid = "";
    String hBillFeesReferenceNo = "";
    String hBillReferenceNoFeef = "";
    String hBillAcctMonth = "";
    String hDeb1Rowid = "";
    String hDeb1DpReferenceNo = "";
    String hDeb1ReferenceSeq = "";
    String hDeb1ItemPostDate = "";
    String hDeb1AcctMonth = "";
    String hDeb1StmtCycle = "";
    String hDeb1AcctCode = "";
    String hDeb1InterestRsDate = "";
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
    int hInt = 0;
    double hAcctMinPayBal = 0;
    String hAcctLastCancelDebtDate = "";
    double hAvdaVouchAmt = 0;
    double hAvdaDVouchAmt = 0;
    String hAvdaProcStage = "";
    int hTempSerialNo = 0;
    int hCount = 0;
    String hAdclSerialNo = "";
    String hPrintName = "";
    String hRptName = "";
    int lineCnt = 0;
    int totalCnt = 0;
    int debtInt = 0;
    int pageLine = 0;
    String dispDate = "";
    String hTempReferenceNo = "";
    double twAmt = 0;
    double usAmt = 0;
    double jpAmt = 0;
    String hDebtAcctMonth = "";
    double totDcRealWaiveAmt = 0;
    double totRealWaiveAmt = 0;
    double hDebtBegBal = 0;
    double hDebeDAvailableBal = 0;
    double realWaiveAmt = 0;
    double wantWaiveAmt = 0;
    double dcRealWaiveAmt = 0;
    double dcWantWaiveAmt = 0;
    double realCancelAmt = 0;
    double dcRealCancelAmt = 0;
    double totDcWaiveMinAmt = 0;
    double totWaiveMinAmt = 0;
    double hDebtDcBegBal = 0;
    double hDebtDcEndBal = 0;
    double hDebtEndBal = 0;
    double hDebtDcDAvailableBal = 0;
    double hDebtDAvailableBal = 0;
    double hDebeDcEndBal = 0;
    double hDebeEndBal = 0;
    double hDebeDcDAvailableBal = 0;
    double hThisBusiAdjustAmt = 0;
    long   hSrcPgmPostseq = 0;
    String hPostNote = "問交結案恢復交易手續費 by seqno";
    String hDeb1BillType = "";
    String hDeb1TxnCode = "";


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
                comc.errExit("Usage : ActF006 , this program need only one parameter  ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            selectPtrBusinday();
            String temstr = String.format("%s/reports/ACT_F006_%s", comc.getECSHOME(), hBusiBusinessDate);
            temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
            printHeader();

            showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));

            selectActDebtCancel();
            hJrnlOrderSeq = 1;
            selectActAcaj();
            if (lineCnt == 0) {
                printNoDetail();
            } else {
                printTailer();
            }

            showLogMessage("I", "", String.format("Total process records[%d]", totalCnt));

            comc.writeReport(temstr, lpar1);
            comcr.insertPtrBatchRpt(lpar1);
            comcr.lpRtn("ACT_F006", hBusiBusinessDate);
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
        setString(1, hBusiBusinessDate + "99990006");
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempSerialNo = getValueInt("h_temp_serial_no");
        } else
            hTempSerialNo = 1;
    }

    /***********************************************************************/
    void selectActAcaj() throws Exception {
        sqlCmd = "select a.p_seqno,";
        sqlCmd += " decode(a.curr_code,'','901',a.curr_code) h_acaj_curr_code,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " UF_ACNO_KEY(a.p_seqno) acct_key,";
        sqlCmd += " a.card_no,";
        sqlCmd += " a.adjust_type,";
        sqlCmd += " a.reference_no,";
        sqlCmd += " a.post_date,";
        sqlCmd += " a.cr_amt,";
        sqlCmd += " decode(decode(a.curr_code,'','901',a.curr_code),'901',a.cr_amt,a.dc_cr_amt) h_acaj_dc_cr_amt,";
        sqlCmd += " a.bef_d_amt,";
        sqlCmd += " decode(decode(a.curr_code,'','901',a.curr_code),'901',a.bef_d_amt,a.dc_bef_d_amt) h_acaj_dc_bef_d_amt,";
        sqlCmd += " a.aft_d_amt,";
        sqlCmd += " decode(decode(a.curr_code,'','901',a.curr_code),'901',a.aft_d_amt,a.dc_aft_d_amt) h_acaj_dc_aft_d_amt,";
        sqlCmd += " a.acct_code,"; // acct_code
        sqlCmd += " a.value_type,";
        sqlCmd += " a.adj_reason_code,";
        sqlCmd += " a.adj_comment,";
        sqlCmd += " a.c_debt_key,";
        sqlCmd += " a.debit_item,";
        sqlCmd += " a.jrnl_date,";
        sqlCmd += " a.job_code,";
        sqlCmd += " decode(a.vouch_job_code,'','00',a.vouch_job_code) h_acaj_vouch_job_code,";
        sqlCmd += " a.rowid rowid,";
        sqlCmd += " r.clo_result ";
        sqlCmd += "  from act_acaj a, rsk_problem r ";
        sqlCmd += " where a.adjust_type in ('DP02','DP03') ";
        sqlCmd += "   and decode(a.process_flag,'','N',a.process_flag) != 'Y' ";
        sqlCmd += "   and a.apr_flag= 'Y' "; // confirm_flag
        sqlCmd += "   and  a.reference_no = r.reference_no ";
        sqlCmd += " order by a.vouch_job_code, a.p_seqno, a.CRT_TIME"; // create_time
        /*
         * "DP02" -> 問交結案 , 問交不還原 "DP03" -> 問交結案 , 恢復正常交易
         */
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcajPSeqno = getValue("p_seqno");
            hAcajCurrCode = getValue("h_acaj_curr_code");
            hAcajAcctType = getValue("acct_type");
            hAcajAcctKey = getValue("acct_key");
            hAcajCardNo = getValue("card_no");
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
            hPlemCloResult = getValue("clo_result");

            if (debugMode) {
                showLogMessage("I", "", "STEP A1 ====== SELECT ACT_ACAJ ===============\n");
                showLogMessage("I", "", String.format("p_seqno                        = [%s]\n", hAcajPSeqno));
                showLogMessage("I", "", String.format("curr_code                      = [%s]\n", hAcajCurrCode));
                showLogMessage("I", "", String.format("acct_type                      = [%s]\n", hAcajAcctType));
                showLogMessage("I", "", String.format("acct_key                       = [%s]\n", hAcajAcctKey));
                showLogMessage("I", "",
                        String.format("acct_code                = [%s]\n", hAcajAcctCode));
                showLogMessage("I", "", String.format("adjust_type                    = [%s]\n", hAcajAdjustType));
                showLogMessage("I", "", String.format("value_type                     = [%s]\n", hAcajValueType));
                showLogMessage("I", "", String.format("reference_no                   = [%s]\n", hAcajReferenceNo));
                showLogMessage("I", "", String.format("cr_amt                         = [%f]\n", hAcajCrAmt));
                showLogMessage("I", "", String.format("dc_cr_amt                      = [%f]\n", hAcajDcCrAmt));
                showLogMessage("I", "", String.format("dc_cr_amt                      = [%f]\n", hAcajDcCrAmt));
                showLogMessage("I", "", String.format("bef_d_amt                      = [%f]\n", hAcajBefDAmt));
                showLogMessage("I", "", String.format("dc_bef_d_amt                   = [%f]\n", hAcajDcBefDAmt));
                showLogMessage("I", "", String.format("aft_d_amt                      = [%f]\n", hAcajAftDAmt));
                showLogMessage("I", "", String.format("dc_aft_d_amt                   = [%f]\n", hAcajDcAftDAmt));
                showLogMessage("I", "", "============== SELECT ACT_ACAJ ===============\n");
            }
            totalCnt++;
            if (totalCnt % 1000 == 0)
                showLogMessage("I", "", String.format("   Process records[%d]", totalCnt));
            /***************** Initial data value **********************/
            String tmpstr = String.format("%012.0f", getJRNLSeq());
            hJrnlJrnlSeqno = tmpstr;
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
            if (hAcajAdjustType.equals("DP03"))
                procFeeWaive();
            /********************************************/
            if ((hAcurDcEndBalOp > 0) || (hAcurDcEndBalLk > 0))
                insertActDebtCancel();
            lastCurrData();
            updateActAcctCurr();
            lastAcctData();
            updateActAcct();

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
        sqlCmd += " a.rowid as rowid ";
        sqlCmd += "  from act_acno a ";
        sqlCmd += " left join crd_idno b on b.id_p_seqno = a.id_p_seqno ";
        sqlCmd += " where a.acno_p_seqno = ? ";
        setString(1, hAcajPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoAcctHolderId = getValue("id_no");
            hAcnoAcctHolderIdCode = getValue("id_no_code");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoRecourseMark = getValue("recourse_mark");
            hAcnoRowid = getValue("rowid");
        }

    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctAcctJrnlBal = 0;
        hAcctEndBalOp = 0;
        hAcctEndBalLk = 0;
        hAcctAdjustCrAmt = 0;
        hAcctAdjustCrCnt = 0;
        hAcctTtlAmt = 0;
        hAcctTtlAmtBal = 0;
        hAcctRowid = "";

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += " end_bal_op,";
        sqlCmd += " end_bal_lk,";
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
            hAcctEndBalOp = getValueDouble("end_bal_op");
            hAcctEndBalLk = getValueDouble("end_bal_lk");
            hAcctAdjustCrAmt = getValueDouble("adjust_cr_amt");
            hAcctAdjustCrCnt = getValueInt("adjust_cr_cnt");
            hAcctTtlAmt = getValueDouble("ttl_amt");
            hAcctTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAcctRowid = getValue("rowid");
        }
        if (debugMode) {
            showLogMessage("I", "", "============== FIRST ACT_ACCT ================\n");
            showLogMessage("I", "", String.format("acct_jrnl_bal                  = [%f]\n", hAcctAcctJrnlBal));
            showLogMessage("I", "", String.format("end_bal_op                     = [%f]\n", hAcctEndBalOp));
            showLogMessage("I", "", String.format("end_bal_lk                     = [%f]\n", hAcctEndBalLk));
            showLogMessage("I", "", String.format("adjust_cr_amt                  = [%f]\n", hAcctAdjustCrAmt));
            showLogMessage("I", "", String.format("adjust_cr_cnt                  = [%d]\n", hAcctAdjustCrCnt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]\n", hAcctTtlAmtBal));
            showLogMessage("I", "", "============== FIRST ACT_ACCT ================\n");
        }
    }

    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        hAcurAcctJrnlBal = 0;
        hAcurEndBalOp = 0;
        hAcurDcEndBalOp = 0;
        hAcurEndBalLk = 0;
        hAcurDcEndBalLk = 0;
        hAcurDcAcctJrnlBal = 0;
        hAcurTtlAmt = 0;
        hAcurDcTtlAmt = 0;
        hAcurTtlAmtBal = 0;
        hAcurDcTtlAmtBal = 0;
        hAcurMinPayBal = 0;
        hAcurDcMinPayBal = 0;
        hAcurPayCnt = 0;
        hAcurAdjustCrAmt = 0;
        hAcurDcAdjustCrAmt = 0;
        hAcurAdjustCrCnt = 0;

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += " dc_acct_jrnl_bal,";
        sqlCmd += " end_bal_op,";
        sqlCmd += " dc_end_bal_op,";
        sqlCmd += " end_bal_lk,";
        sqlCmd += " dc_end_bal_lk,";
        sqlCmd += " ttl_amt,";
        sqlCmd += " dc_ttl_amt,";
        sqlCmd += " ttl_amt_bal,";
        sqlCmd += " dc_ttl_amt_bal,";
        sqlCmd += " min_pay_bal,";
        sqlCmd += " decode(curr_code,'901',min_pay_bal,dc_min_pay_bal) h_acur_dc_min_pay_bal,";
        sqlCmd += " pay_cnt,";
        sqlCmd += " adjust_cr_amt,";
        sqlCmd += " dc_adjust_cr_amt,";
        sqlCmd += " adjust_cr_cnt,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acct_curr ";
        sqlCmd += " where p_seqno   = ? ";
        sqlCmd += "   and curr_code = ? ";
        setString(1, hAcajPSeqno);
        setString(2, hAcajCurrCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcurAcctJrnlBal = getValueDouble("acct_jrnl_bal");
            hAcurDcAcctJrnlBal = getValueDouble("dc_acct_jrnl_bal");
            hAcurEndBalOp = getValueDouble("end_bal_op");
            hAcurDcEndBalOp = getValueDouble("dc_end_bal_op");
            hAcurEndBalLk = getValueDouble("end_bal_lk");
            hAcurDcEndBalLk = getValueDouble("dc_end_bal_lk");
            hAcurTtlAmt = getValueDouble("ttl_amt");
            hAcurDcTtlAmt = getValueDouble("dc_ttl_amt");
            hAcurTtlAmtBal = getValueDouble("ttl_amt_bal");
            hAcurDcTtlAmtBal = getValueDouble("dc_ttl_amt_bal");
            hAcurMinPayBal = getValueDouble("min_pay_bal");
            hAcurDcMinPayBal = getValueDouble("h_acur_dc_min_pay_bal");
            hAcurPayCnt = getValueInt("pay_cnt");
            hAcurAdjustCrAmt = getValueDouble("adjust_cr_amt");
            hAcurDcAdjustCrAmt = getValueDouble("dc_adjust_cr_amt");
            hAcurAdjustCrCnt = getValueInt("adjust_cr_cnt");
            hAcurRowid = getValue("rowid");
        }
        if (debugMode) {
            showLogMessage("I", "", "============== FIRST ACT_ACCT_CURR ===========\n");
            showLogMessage("I", "", String.format("acct_jrnl_bal                  = [%f]\n", hAcurAcctJrnlBal));
            showLogMessage("I", "", String.format("dc_acct_jrnl_bal               = [%f]\n", hAcurDcAcctJrnlBal));
            showLogMessage("I", "", String.format("end_bal_op                     = [%f]\n", hAcurEndBalOp));
            showLogMessage("I", "", String.format("dc_end_bal_op                  = [%f]\n", hAcurDcEndBalOp));
            showLogMessage("I", "", String.format("end_bal_lk                     = [%f]\n", hAcurEndBalLk));
            showLogMessage("I", "", String.format("dc_end_bal_lk                  = [%f]\n", hAcurDcEndBalLk));
            showLogMessage("I", "", String.format("pay_cnt                        = [%d]\n", hAcurPayCnt));
            showLogMessage("I", "", String.format("adjust_cr_amt                  = [%f]\n", hAcurAdjustCrAmt));
            showLogMessage("I", "", String.format("dc_adjust_cr_amt               = [%f]\n", hAcurDcAdjustCrAmt));
            showLogMessage("I", "", String.format("adjust_cr_cnt                  = [%d]\n", hAcurAdjustCrCnt));
            showLogMessage("I", "", String.format("ttl_amt                        = [%f]\n", hAcurTtlAmt));
            showLogMessage("I", "", String.format("dc_ttl_amt                     = [%f]\n", hAcurDcTtlAmt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]\n", hAcurTtlAmtBal));
            showLogMessage("I", "", String.format("dc_ttl_amt_bal                 = [%f]\n", hAcurDcTtlAmtBal));
            showLogMessage("I", "", String.format("min_pay_bal                    = [%f]\n", hAcurMinPayBal));
            showLogMessage("I", "", String.format("dc_min_pay_bal                 = [%f]\n", hAcurDcMinPayBal));
            showLogMessage("I", "", "============== FIRST ACT_ACCT_CURR ===========\n");
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
        if (hAcajValueType.equals("2"))
            hDeb1InterestRsDate = hBusiBusinessDate;

        hDebtAcctMonth = hDeb1AcctMonth;
        hDebtDcEndBal = hDeb1DcEndBal;
        hDebtEndBal = hDeb1EndBal;
        hDebtDcBegBal = hDeb1DcBegBal;
        hDebtBegBal = hDeb1BegBal;
        hDebtDcDAvailableBal = hDeb1DcDAvailableBal;
        hDebtDAvailableBal = hDeb1DAvailableBal;

        hDeb1DcEndBal += hAcajDcCrAmt;
        hDeb1EndBal += hAcajCrAmt;
        if (hAcajCurrCode.equals("901"))
            hDeb1EndBal = hDeb1DcEndBal;
        if (hDeb1DcEndBal == 0)
            hDeb1EndBal = 0;

        hDebeDcEndBal = hDeb1DcEndBal;
        hDebeEndBal = hDeb1EndBal;
        /****************************************************************/
        hDeb1DcDAvailableBal += (hAcajDcAftDAmt - hAcajDcBefDAmt);
        if (hDeb1DcDAvailableBal > hDeb1DcBegBal)
            return (1);
        hDeb1DAvailableBal = comcr.commCurrAmt("901",
                hDeb1DcDAvailableBal * (hDeb1BegBal / hDeb1DcBegBal), 0);
        if (hAcajCurrCode.equals("901"))
            hDeb1DAvailableBal = hDeb1DcDAvailableBal;
        hDebeDcDAvailableBal = hDeb1DcDAvailableBal;
        hDebeDAvailableBal = hDeb1DAvailableBal;
        /****************************************************************/
        if (hDeb1DpReferenceNo.length() != 0)
            if (deleteActDebtDp() != 0)
                return (1);
        /****************************************************************/
        hAvdaVouchAmt = (hAcajDcAftDAmt - hAcajDcBefDAmt);
        hAvdaDVouchAmt = hDeb1DcEndBal - hDebtDcEndBal;
        hAvdaProcStage = "1";
        insertActVouchData(1);
        /****************************************************************/

        hJrnlTranClass = "A";
        hJrnlTranType = hAcajAdjustType;
        if (hAcajAdjustType.equals("DP02")) {
            hJrnlDcTransactionAmt = 0;
            hJrnlTransactionAmt = 0;
            insertActJrnl(2);
            return (0);
        }
        hJrnlTransactionAmt = (hAcajAftDAmt - hAcajBefDAmt);
        hJrnlDcTransactionAmt = (hAcajDcAftDAmt - hAcajDcBefDAmt);
        totDcRealWaiveAmt += (hAcajDcAftDAmt - hAcajDcBefDAmt);
        totRealWaiveAmt += (hAcajAftDAmt - hAcajBefDAmt);

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
    int deleteActDebtDp() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", "STEP F2 ====== DELETE ACT_DEBT ===============\n");
            showLogMessage("I", "", String.format("reference_seq                  = [%s]\n", hDeb1DpReferenceNo));
            showLogMessage("I", "", "STEP F2 ====== DELETE ACT_DEBT ===============\n");
        }
        daoTable = "act_debt";
        whereStr = "where reference_no= ?  "; // reference_seq
        whereStr += "and (     ? != 'DP03' " + "or  (    ?  = 'DP03'"
                + "and decode( cast(? as varchar(3)),'901',end_bal,dc_end_bal) >= ?)) ";
        setString(1, hDeb1DpReferenceNo);
        setString(2, hAcajAdjustType);
        setString(3, hAcajAdjustType);
        setString(4, hAcajCurrCode);
        setDouble(5, hAcajDcCrAmt);
        deleteTable();
        if (notFound.equals("Y"))
            return (1);
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
        setValueDouble(extendField + "pay_amt", hAcajDcCrAmt);
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
        setValueDouble(extendField + "d_avail_bal", hDeb1DAvailableBal);
        setValueDouble(extendField + "dc_d_avail_bal", hDeb1DcDAvailableBal);
        setValueDouble(extendField + "tx_amt", hAcajBefDAmt - hAcajAftDAmt);
        setValueDouble(extendField + "dc_tx_amt", hAcajDcBefDAmt - hAcajDcAftDAmt);
        setValue(extendField + "error_reason", "06");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acaj_err duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void procFeeWaive() throws Exception {
        if (selectBilBill() != 0)
            return;

        if ((!hAcajAcctCode.equals("CF")) && (!hAcajAcctCode.equals("PF"))) {
            hJrnlTranClass = "A";
            hJrnlTranType = "DR07";

            if (hBillFeesReferenceNo.length() != 0) {
                hDeb1ReferenceSeq = hBillFeesReferenceNo;
                if (hDebeDAvailableBal == hDebtBegBal) {
                    procActDebt(0);
                } else {
                    procActDebt(1);
                }
                if (hDeb1DpReferenceNo.length() != 0)
                    deleteActDebtDp1();
                /****************************************************************
                 * h_avda_vouch_amt = dc_real_waive_amt; h_avda_d_vouch_amt =
                 * dc_real_cancel_amt; str2var(h_avda_proc_stage , "4");
                 * insert_act_vouch_data(1);
                 ****************************************************************/
            }

            if (hPlemCloResult.equals("21"))
                return;

            if (hBillReferenceNoFeef.length() != 0) {
                hDeb1ReferenceSeq = hBillReferenceNoFeef;
                if (hDebeDAvailableBal == hDebtBegBal) {
                    procActDebt(0);
                } else {
                    procActDebt(1);
                }
                if (hDeb1DpReferenceNo.length() != 0)
                    deleteActDebtDp1();
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
        sqlCmd += "  from bil_bill  ";
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
                    hDeb1DcBegBal * (hAcajDcAftDAmt - hAcajDcBefDAmt) / hDebtDcBegBal, 0);
            if (dcWantWaiveAmt > hDeb1DcBegBal - hDeb1DcDAvailableBal)
                dcWantWaiveAmt = hDeb1DcBegBal - hDeb1DcDAvailableBal;
            wantWaiveAmt = comcr.commCurrAmt("901", dcWantWaiveAmt * (hDeb1BegBal / hDeb1DcBegBal), 0);
            if (wantWaiveAmt > hDeb1BegBal - hDeb1DAvailableBal)
                wantWaiveAmt = hDeb1BegBal - hDeb1DAvailableBal;
            if (hAcajCurrCode.equals("901"))
                dcWantWaiveAmt = wantWaiveAmt;
            if (dcWantWaiveAmt == 0)
                wantWaiveAmt = 0;
            break;
        case 2:/* 呼叫前設定 */
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

        hThisBusiAdjustAmt = hJrnlDcTransactionAmt;
        if ( (Arrays.asList("CF","PF").contains(hDeb1AcctCode)) && 
             (hThisBusiAdjustAmt  != 0) ) 
           insertThisActPostLog();

        printDetail();
        if (debtInt != 0) {
            insertActDebt();
            deleteActDebtHst();
        }
    }

    /***********************************************************************/
    int selectActDebt() throws Exception {
        hTempReferenceNo = hDeb1ReferenceSeq;
        sqlCmd = "select reference_no,"; // reference_seq
        sqlCmd += " dp_reference_no,";
        sqlCmd += " post_date,"; // item_post_date
        sqlCmd += " acct_month,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " beg_bal,";
        sqlCmd += " decode(curr_code,'901',beg_bal,dc_beg_bal) h_deb1_dc_beg_bal,";
        sqlCmd += " end_bal,";
        sqlCmd += " decode(curr_code,'901',end_bal,dc_end_bal) h_deb1_dc_end_bal,";
        sqlCmd += " d_avail_bal,"; // d_available_bal
        sqlCmd += " decode(curr_code,'901',d_avail_bal,dc_d_avail_bal) h_deb1_dc_d_available_bal,"; // d_available_bal
                                                                                                    // //dc_d_available_bal
        sqlCmd += " acct_code,"; // acct_code
        sqlCmd += " bill_type,"; 
        sqlCmd += " txn_code,"; 
        sqlCmd += " interest_rs_date,";
        sqlCmd += " interest_date,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_debt  ";
        sqlCmd += " where reference_no= ?  ";
        sqlCmd += "   and decode(curr_code,'901',beg_bal,dc_beg_bal) > 0 ";
        setString(1, hDeb1ReferenceSeq);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDeb1ReferenceSeq = getValue("reference_no");
            hDeb1DpReferenceNo = getValue("dp_reference_no");
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
            hDeb1BillType = getValue("bill_type"); 
            hDeb1TxnCode  = getValue("txn_code"); 
            hDeb1InterestRsDate = getValue("interest_rs_date");
            hDeb1InterestDate = getValue("interest_date");
            hDeb1Rowid = getValue("rowid");
        } else
            return (1);
        if (debugMode) {
            showLogMessage("I", "", "STEP A2 ====== SELECT ACT_DEBT ===============\n");
            showLogMessage("I", "", String.format("reference_seq                  = [%s]\n", hDeb1ReferenceSeq));
            showLogMessage("I", "", String.format("dp_reference_no                = [%s]\n", hDeb1DpReferenceNo));
            showLogMessage("I", "", String.format("item_post_date                 = [%s]\n", hDeb1ItemPostDate));
            showLogMessage("I", "", String.format("acct_month                     = [%s]\n", hDeb1AcctMonth));
            showLogMessage("I", "", String.format("stmt_cycle                     = [%s]\n", hDeb1StmtCycle));
            showLogMessage("I", "", String.format("beg_bal                        = [%f]\n", hDeb1BegBal));
            showLogMessage("I", "", String.format("dc_beg_bal                     = [%f]\n", hDeb1DcBegBal));
            showLogMessage("I", "", String.format("end_bal                        = [%f]\n", hDeb1EndBal));
            showLogMessage("I", "", String.format("dc_end_bal                     = [%f]\n", hDeb1DcEndBal));
            showLogMessage("I", "", String.format("d_available_bal                = [%f]\n", hDeb1DAvailableBal));
            showLogMessage("I", "",
                    String.format("dc_d_available_bal             = [%f]\n", hDeb1DcDAvailableBal));
            showLogMessage("I", "", String.format("acct_code                = [%s]\n", hDeb1AcctCode));
            showLogMessage("I", "", String.format("interest_date                  = [%s]\n", hDeb1InterestDate));
            showLogMessage("I", "", "STEP A2 ====== SELECT ACT_DEBT ===============\n");
        }
        debtInt = 0;
        return (0);
    }

    /***********************************************************************/
    int selectActDebtHst() throws Exception {
        hDeb1ReferenceSeq = hTempReferenceNo;
        sqlCmd = "select reference_no,"; // reference_seq
        sqlCmd += " dp_reference_no,";
        sqlCmd += " post_date,"; // item_post_date
        sqlCmd += " acct_month,";
        sqlCmd += " stmt_cycle,";
        sqlCmd += " beg_bal,";
        sqlCmd += " decode(curr_code,'901',beg_bal,dc_beg_bal) h_deb1_dc_beg_bal,";
        sqlCmd += " end_bal,";
        sqlCmd += " decode(curr_code,'901',end_bal,dc_end_bal) h_deb1_dc_end_bal,";
        sqlCmd += " d_avail_bal,";
        sqlCmd += " decode(curr_code,'901',d_avail_bal,dc_d_avail_bal) h_deb1_dc_d_available_bal,"; // d_available_bal
                                                                                                    // //dc_d_available_bal
        sqlCmd += " acct_code,"; // acct_code
        sqlCmd += " bill_type,"; 
        sqlCmd += " txn_code,"; 
        sqlCmd += " interest_rs_date,";
        sqlCmd += " interest_date,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_debt_hst  ";
        sqlCmd += " where reference_no= ?  ";
        sqlCmd += "   and decode(curr_code,'901',beg_bal,dc_beg_bal) > 0 ";
        setString(1, hDeb1ReferenceSeq);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDeb1ReferenceSeq = getValue("reference_no"); // reference_seq
            hDeb1DpReferenceNo = getValue("dp_reference_no");
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
            hDeb1BillType = getValue("bill_type"); 
            hDeb1TxnCode  = getValue("txn_code"); 
            hDeb1InterestRsDate = getValue("interest_rs_date");
            hDeb1InterestDate = getValue("interest_date");
            hDeb1Rowid = getValue("rowid");
        }
        hDeb1ReferenceSeq = hTempReferenceNo;
        if (recordCnt == 0)
            return (1);
        if (debugMode) {
            showLogMessage("I", "", "STEP A2 ====== SELECT ACT_DEBT_HST ===========\n");
            showLogMessage("I", "", String.format("reference_seq                  = [%s]\n", hDeb1ReferenceSeq));
            showLogMessage("I", "", String.format("dp_reference_no                = [%s]\n", hDeb1DpReferenceNo));
            showLogMessage("I", "", String.format("item_post_date                 = [%s]\n", hDeb1ItemPostDate));
            showLogMessage("I", "", String.format("acct_month                     = [%s]\n", hDeb1AcctMonth));
            showLogMessage("I", "", String.format("stmt_cycle                     = [%s]\n", hDeb1StmtCycle));
            showLogMessage("I", "", String.format("beg_bal                        = [%f]\n", hDeb1BegBal));
            showLogMessage("I", "", String.format("dc_beg_bal                     = [%f]\n", hDeb1DcBegBal));
            showLogMessage("I", "", String.format("end_bal                        = [%f]\n", hDeb1EndBal));
            showLogMessage("I", "", String.format("dc_end_bal                     = [%f]\n", hDeb1DcEndBal));
            showLogMessage("I", "", String.format("d_available_bal                = [%f]\n", hDeb1DAvailableBal));
            showLogMessage("I", "",
                    String.format("dc_d_available_bal             = [%f]\n", hDeb1DcDAvailableBal));
            showLogMessage("I", "", String.format("acct_code                = [%s]\n", hDeb1AcctCode));
            showLogMessage("I", "", String.format("interest_date                  = [%s]\n", hDeb1InterestDate));
            showLogMessage("I", "", "STEP A2 ====== SELECT ACT_DEBT ===============\n");
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
            showLogMessage("I", "", "STEP A6 ====== INSERT ACT_JRNL ===============\n");
            showLogMessage("I", "", String.format("transaction_class              = [%s]\n", hJrnlTranClass));
            showLogMessage("I", "", String.format("transaction_type               = [%s]\n", hJrnlTranType));
            showLogMessage("I", "", String.format("transaction_amt                = [%f]\n", hJrnlTransactionAmt));
            showLogMessage("I", "",
                    String.format("dc_transaction_amt             = [%f]\n", hJrnlDcTransactionAmt));
            showLogMessage("I", "", String.format("jrnl_bal,                      = [%f]\n", hJrnlJrnlBal));
            showLogMessage("I", "", String.format("dc_jrnl_bal                    = [%f]\n", hJrnlDcJrnlBal));
            showLogMessage("I", "", "STEP A6 ====== INSERT ACT_JRNL ===============\n");
        }

        daoTable = "act_jrnl";
        extendField = "actjrnl.";
        setValue(extendField + "crt_date", hTempCreateDate);
        setValue(extendField + "crt_time", hTempCreateTime);
        setValueDouble(extendField + "enq_seqno", hJrnlEnqSeqno);
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
        setValueDouble(extendField + "order_seq", hJrnlOrderSeq);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_jrnl duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActDebt1() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", "STEP B1 ====== UPDATE ACT_DEBT ===============\n");
            showLogMessage("I", "", String.format("end_bal                        = [%f]\n", hDeb1EndBal));
            showLogMessage("I", "", String.format("dc_end_bal                     = [%f]\n", hDeb1DcEndBal));
            showLogMessage("I", "", String.format("d_available_bal                = [%f]\n", hDeb1DAvailableBal));
            showLogMessage("I", "",
                    String.format("dc_d_available_bal             = [%f]\n", hDeb1DcDAvailableBal));
            showLogMessage("I", "", String.format("interest_rs_date               = [%s]\n", hDeb1InterestDate));
            showLogMessage("I", "", "STEP A6 ====== INSERT ACT_JRNL ===============\n");
        }
        if (debtInt == 0) {
            daoTable = "act_debt";
            updateSQL = "end_bal           = ?,";
            updateSQL += " dc_end_bal       = ?,";
            updateSQL += " d_avail_bal      = ?,"; // d_available_bal
            updateSQL += " dc_d_avail_bal   = ?,"; // dc_d_available_bal
            updateSQL += " interest_rs_date = ?,";
            updateSQL += " mod_time         = sysdate,";
            updateSQL += " mod_pgm          = 'ActF006'";
            whereStr = "where rowid       = ? ";
            setDouble(1, hDeb1EndBal);
            setDouble(2, hDeb1DcEndBal);
            setDouble(3, hDeb1DAvailableBal);
            setDouble(4, hDeb1DcDAvailableBal);
            setString(5, hDeb1InterestRsDate);
            setRowId(6, hDeb1Rowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_debt not found!", "", hCallBatchSeqno);
            }
        } else {
            daoTable = "act_debt_hst";
            updateSQL = "end_bal           = ?,";
            updateSQL += " dc_end_bal       = ?,";
            updateSQL += " d_avail_bal      = ?,"; // d_available_bal
            updateSQL += " dc_d_avail_bal   = ?,"; // dc_d_available_bal
            updateSQL += " interest_rs_date = ?,";
            updateSQL += " mod_time         = sysdate,";
            updateSQL += " mod_pgm          = 'ActF006'";
            whereStr = "where rowid       = ? ";
            setDouble(1, hDeb1EndBal);
            setDouble(2, hDeb1DcEndBal);
            setDouble(3, hDeb1DAvailableBal);
            setDouble(4, hDeb1DcDAvailableBal);
            setString(5, hDeb1InterestRsDate);
            setRowId(6, hDeb1Rowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_act_debt_hst not found!", "", hCallBatchSeqno);
            }
        }

    }

    /***********************************************************************/
    void insertCycPyaj(int hInt) throws Exception {
        if (debugMode) {
            showLogMessage("I", "", "STEP A7 ====== INSERT CYC_PYAJ ===============\n");
            showLogMessage("I", "", String.format("payment_amount                 = [%f]\n", hJrnlTransactionAmt));
            showLogMessage("I", "",
                    String.format("dc_payment_amount              = [%f]\n", hJrnlDcTransactionAmt));
            showLogMessage("I", "", "STEP A7 ====== INSERT CYC_PYAJ ===============\n");
        }
        daoTable = "cyc_pyaj";
        extendField = "cycpyaj.";
        setValue(extendField + "P_SEQNO", hAcajPSeqno);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "class_code", "A");
        setValue(extendField + "payment_date", hBusiBusinessDate);
        setValueDouble(extendField + "PAYMENT_AMT", hJrnlTransactionAmt);
        setValueDouble(extendField + "DC_PAYMENT_AMT", hJrnlDcTransactionAmt);
        setValue(extendField + "payment_type", hJrnlTranType);
        setValue(extendField + "stmt_cycle", hAcnoStmtCycle);
        setValue(extendField + "SETTLE_FLAG", "U");
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
    void insertThisActPostLog() throws Exception {
            
        hSrcPgmPostseq = 0;
        daoTable    = "act_post_log";
        selectSQL   = " nvl(max(SRC_PGM_POSTSEQ), 0) + 1 as h_src_pgm_postseq";
        whereStr  = " where BUSINESS_DATE    = ? ";
        whereStr += "  and CURR_CODE         = ? ";      
        whereStr += "  and ACCT_CODE         = ? ";      
        whereStr += "  and SRC_PGM           = ? ";      
        setString(1, hBusiBusinessDate);
        setString(2, hAcajCurrCode);
        setString(3, hDeb1AcctCode);
        setString(4, javaProgram);
        int m = selectTable();
        hSrcPgmPostseq = getValueLong("h_src_pgm_postseq");

        daoTable    = "act_post_log";
        extendField = "post.";
        setValue("post.BUSINESS_DATE",hBusiBusinessDate);
        setValue("post.CURR_CODE",hAcajCurrCode);
        setValue("post.ACCT_CODE",hDeb1AcctCode);
        setValue("post.SRC_PGM",javaProgram);
        setValueLong("post.SRC_PGM_POSTSEQ", hSrcPgmPostseq );
        setValue("post.POST_TYPE","D1");
        
        hThisBusiAdjustAmt = convAmt(hThisBusiAdjustAmt);
        setValueDouble("post.POST_TYPE_AMT",hThisBusiAdjustAmt);
        setValue("post.POST_NOTE",hPostNote);
        setValue("post.BILL_TYPE",hDeb1BillType);
        setValue("post.TXN_CODE",hDeb1TxnCode);
        setValue("post.ACCT_TYPE",hAcajAcctType);
        setValue("post.P_SEQNO",hAcajPSeqno);
        setValue("post.MOD_TIME",sysDate + sysTime);	
        setValue("post.MOD_PGM",javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) 
           { 
            comcr.errRtn("insert_this act_post_log ERROR ", hBusiBusinessDate+" "+
            hAcajCurrCode+" "+hDeb1AcctCode+" "+javaProgram+hSrcPgmPostseq, comcr.hCallBatchSeqno);
           } 

    }

    /***********************************************************************/
    public double  convAmt(double cvtAmt) throws Exception
    {
      long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
      double cvtDouble =  ((double) cvtLong) / 100;
      return cvtDouble;
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, hDeb1AcctCode, 2);
        buf = comcr.insertStr(buf, hAcajCurrCode, 13);
        buf = comcr.insertStr(buf, hAcajCardNo, 30);

        if (hAcajCurrCode.equals("901")) {
            twAmt += hJrnlDcTransactionAmt;
        } else if (hAcajCurrCode.equals("840")) {
            usAmt += hJrnlDcTransactionAmt;
        } else if (hAcajCurrCode.equals("392")) {
            jpAmt += hJrnlDcTransactionAmt;
        } else {
            showLogMessage("I", "", String.format(" curr_code error [%f]", hJrnlDcTransactionAmt));
        }
        szTmp = comcr.commFormat("3$,3$,3$.2$", hJrnlDcTransactionAmt);
        buf = comcr.insertStr(buf, szTmp, 48);
      //lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = comcr.insertStr(buf, hDeb1ReferenceSeq, 74);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt++;
        if (lineCnt >= 45) {
            printHeader();
        }
    }

    /***********************************************************************/
    void printHeader() throws Exception {
        pageLine++;
        buf = "";
        buf = comcr.insertStr(buf, "ACT_F006", 1);
        szTmp = comcr.bankName;
        buf = comcr.insertStrCenter(buf, szTmp, 119);
        buf = comcr.insertStr(buf, "列印表日 :", 90);
        dispDate = comc.convDates(sysDate, 1);
        buf = comcr.insertStr(buf, dispDate, 101);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStrCenter(buf, "問交D檔費用加回明細表", 119);
        buf = comcr.insertStr(buf, "列印頁數 :", 90);
        szTmp = String.format("%4d", pageLine);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "類    型", 1);
        buf = comcr.insertStr(buf, "幣    別", 13);
        buf = comcr.insertStr(buf, "卡    號", 30);
        buf = comcr.insertStr(buf, "金    額", 55);
        buf = comcr.insertStr(buf, "參考號碼", 74);

        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void insertActDebt() throws Exception {
        sqlCmd = "insert into act_debt ";
        sqlCmd += "select * from act_debt_hst ";
        sqlCmd += " where rowid = ? ";
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
    void initActDebt() throws Exception {
        hDeb1DpReferenceNo = "";
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
        hDeb1InterestRsDate = "";
    }

    /***********************************************************************/
    int deleteActDebtDp1() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", "STEP F3 ====== DELETE ACT_DEBT ===============\n");
            showLogMessage("I", "", String.format("reference_seq                  = [%s]\n", hDeb1DpReferenceNo));
            showLogMessage("I", "", "STEP F3 ====== DELETE ACT_DEBT ===============\n");
        }
        daoTable = "act_debt";
        whereStr = "where reference_no= ? "; // reference_seq
        setString(1, hDeb1DpReferenceNo);
        deleteTable();
        if (notFound.equals("Y")) {
            if (debugMode) {
                showLogMessage("I", "", "STEP F4 ====== DELETE ACT_DEBT_HST ===========\n");
                showLogMessage("I", "",
                        String.format("reference_seq                  = [%s]\n", hDeb1DpReferenceNo));
                showLogMessage("I", "", "STEP F4 ====== DELETE ACT_DEBT_HST ===========\n");
            }
            daoTable = "act_debt_hst";
            whereStr = "where reference_no = ? "; // reference_seq
            setString(1, hDeb1DpReferenceNo);
            deleteTable();
        }

        return 0;
    }

    /***********************************************************************/
    void insertActDebtCancel() throws Exception {
        int hCount = 0;

        sqlCmd = "select count(*) h_count ";
        sqlCmd += "  from act_debt_cancel  ";
        sqlCmd += " where p_seqno = ?  ";
        sqlCmd += "   and substr(batch_no,9,4)='9999' ";
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
        setValue(extendField + "batch_no", hBusiBusinessDate + "99990006");
        setValue(extendField + "serial_no", hAdclSerialNo);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "curr_code", hAcajCurrCode);
      //setValue(extendField + "acct_key", h_acaj_acct_key); changed to 'acct_type' below on 2019/01/23
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
        hAcurAcctJrnlBal += totRealWaiveAmt;
        hAcurDcAcctJrnlBal += totDcRealWaiveAmt;
        if (hAcajAdjustType.equals("DP02")) {
            hAcurAcctJrnlBal -= (hAcajAftDAmt - hAcajBefDAmt);
            hAcurDcAcctJrnlBal -= (hAcajDcAftDAmt - hAcajDcBefDAmt);
        }
        if (hAcajCurrCode.equals("901"))
            hAcurAcctJrnlBal = hAcurDcAcctJrnlBal;
        /*
         * 關帳日才結案,金額不宜加入上期應繳餘額 if (strcmp(h_debt_acct_month.arr,
         * h_wday_next_acct_month.arr) != 0) { h_acur_dc_ttl_amt_bal +=
         * tot_dc_real_waive_amt; h_acur_ttl_amt_bal += tot_real_waive_amt; if
         * (strcmp(h_acaj_adjust_type.arr, "DP02") == 0) { h_acur_dc_ttl_amt_bal
         * -= (h_acaj_dc_aft_d_amt-h_acaj_dc_bef_d_amt); h_acur_ttl_amt_bal -=
         * (h_acaj_aft_d_amt-h_acaj_bef_d_amt); }
         * 
         * if (h_acur_dc_ttl_amt_bal==0) h_acur_ttl_amt_bal=0;
         * 
         * if (strcmp(h_acaj_curr_code.arr,"901")==0)
         * h_acur_ttl_amt_bal=h_acur_dc_ttl_amt_bal;
         * 
         * if (h_acur_dc_ttl_amt_bal<=0) { h_acur_min_pay_bal=0;
         * h_acur_dc_min_pay_bal=0; delete_act_acag_curr(); update_act_acag();
         * delete_act_acag(); } }
         */

        hAcurAdjustCrCnt++;
        hAcurAdjustCrAmt += totRealWaiveAmt;
        hAcurDcAdjustCrAmt += totDcRealWaiveAmt;
        if (hAcajAdjustType.equals("DP02")) {
            hAcurAdjustCrCnt--;
            hAcurDcAdjustCrAmt -= (hAcajDcAftDAmt - hAcajDcBefDAmt);
            hAcurAdjustCrAmt -= (hAcajAftDAmt - hAcajBefDAmt);
        }
    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        if (debugMode) {
            showLogMessage("I", "", "=============== LAST ACT_ACCT_CURR ===========\n");
            showLogMessage("I", "", String.format("acct_jrnl_bal                  = [%f]\n", hAcurAcctJrnlBal));
            showLogMessage("I", "", String.format("end_bal_op                     = [%f]\n", hAcurEndBalOp));
            showLogMessage("I", "", String.format("dc_end_bal_op                  = [%f]\n", hAcurDcEndBalOp));
            showLogMessage("I", "", String.format("end_bal_lk                     = [%f]\n", hAcurEndBalLk));
            showLogMessage("I", "", String.format("dc_end_bal_lk                  = [%f]\n", hAcurDcEndBalLk));
            showLogMessage("I", "", String.format("dc_acct_jrnl_bal               = [%f]\n", hAcurDcAcctJrnlBal));
            showLogMessage("I", "", String.format("adjust_cr_amt                  = [%f]\n", hAcurAdjustCrAmt));
            showLogMessage("I", "", String.format("dc_adjust_cr_amt               = [%f]\n", hAcurDcAdjustCrAmt));
            showLogMessage("I", "", String.format("adjust_cr_cnt                  = [%d]\n", hAcurAdjustCrCnt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]\n", hAcurTtlAmtBal));
            showLogMessage("I", "", String.format("dc_ttl_amt_bal                 = [%f]\n", hAcurDcTtlAmtBal));
            showLogMessage("I", "", String.format("min_pay_bal                    = [%f]\n", hAcurMinPayBal));
            showLogMessage("I", "", String.format("dc_min_pay_bal                 = [%f]\n", hAcurDcMinPayBal));
            showLogMessage("I", "", "=============== LAST ACT_ACCT_CURR ===========\n");
        }
        daoTable = "act_acct_curr a";
        updateSQL = "acct_jrnl_bal      = ?,";
        updateSQL += " dc_acct_jrnl_bal  = ?,";
        updateSQL += " adjust_cr_amt     = ?,";
        updateSQL += " dc_adjust_cr_amt  = ?,";
        updateSQL += " adjust_cr_cnt     = ?,";
        updateSQL += " ttl_amt_bal       = ?,";
        updateSQL += " dc_ttl_amt_bal    = ?,";
        updateSQL += " min_pay_bal       = ?,";
        updateSQL += " dc_min_pay_bal    = ?,";
        updateSQL += " mod_time          = sysdate,";
        updateSQL += " mod_pgm           = 'ActF006'";
        whereStr = "where rowid        = ? ";
        setDouble(1, hAcurAcctJrnlBal);
        setDouble(2, hAcurDcAcctJrnlBal);
        setDouble(3, hAcurAdjustCrAmt);
        setDouble(4, hAcurDcAdjustCrAmt);
        setInt(5, hAcurAdjustCrCnt);
        setDouble(6, hAcurTtlAmtBal);
        setDouble(7, hAcurDcTtlAmtBal);
        setDouble(8, hAcurMinPayBal);
        setDouble(9, hAcurDcMinPayBal);
        setRowId(10, hAcurRowid);
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
            showLogMessage("I", "", "=============== LAST ACT_ACCT_CURR ===========\n");
            showLogMessage("I", "", String.format("acct_jrnl_bal                  = [%f]\n", hAcurAcctJrnlBal));
            showLogMessage("I", "", String.format("end_bal_op                     = [%f]\n", hAcurEndBalOp));
            showLogMessage("I", "", String.format("dc_end_bal_op                  = [%f]\n", hAcurDcEndBalOp));
            showLogMessage("I", "", String.format("end_bal_lk                     = [%f]\n", hAcurEndBalLk));
            showLogMessage("I", "", String.format("dc_end_bal_lk                  = [%f]\n", hAcurDcEndBalLk));
            showLogMessage("I", "", String.format("dc_acct_jrnl_bal               = [%f]\n", hAcurDcAcctJrnlBal));
            showLogMessage("I", "", String.format("adjust_cr_amt                  = [%f]\n", hAcurAdjustCrAmt));
            showLogMessage("I", "", String.format("dc_adjust_cr_amt               = [%f]\n", hAcurDcAdjustCrAmt));
            showLogMessage("I", "", String.format("adjust_cr_cnt                  = [%d]\n", hAcurAdjustCrCnt));
            showLogMessage("I", "", String.format("ttl_amt_bal                    = [%f]\n", hAcurTtlAmtBal));
            showLogMessage("I", "", String.format("dc_ttl_amt_bal                 = [%f]\n", hAcurDcTtlAmtBal));
            showLogMessage("I", "", String.format("min_pay_bal                    = [%f]\n", hAcurMinPayBal));
            showLogMessage("I", "", String.format("dc_min_pay_bal                 = [%f]\n", hAcurDcMinPayBal));
            showLogMessage("I", "", "=============== LAST ACT_ACCT_CURR ===========\n");
        }
        daoTable = "act_acct a";
        updateSQL = "acct_jrnl_bal          = ?,";
        updateSQL += " adjust_cr_amt         = ?,";
        updateSQL += " adjust_cr_cnt         = ?,";
        updateSQL += " ttl_amt_bal           = ?,";
        updateSQL += " min_pay_bal           = ?,";
        updateSQL += " last_cancel_debt_date = ?,";
        updateSQL += " mod_time              = sysdate,";
        updateSQL += " mod_pgm               = 'ActF006'";
        whereStr = "where rowid            = ? ";
        setDouble(1, hAcctAcctJrnlBal);
        setDouble(2, hAcctAdjustCrAmt);
        setInt(3, hAcctAdjustCrCnt);
        setDouble(4, hAcctTtlAmtBal);
        setDouble(5, hAcctMinPayBal);
        setString(6, hAcctLastCancelDebtDate);
        setRowId(7, hAcctRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct a not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcaj() throws Exception {
        daoTable = "act_acaj";
        updateSQL = "process_flag = 'Y',";
        updateSQL += " mod_time    = sysdate,";
        updateSQL += " mod_pgm     = 'ActF006'";
        whereStr = "where rowid  = ? ";
        setRowId(1, hAcajRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acaj not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void printNoDetail() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "本日無資料列印！！", 52);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void printTailer() throws Exception {
        buf = "";
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = comcr.insertStr(buf, "筆數：", 1);
        szTmp = String.format("%4d", lineCnt);
        buf = comcr.insertStr(buf, szTmp, 7);

        buf = comcr.insertStr(buf, "台幣：", 15);
        szTmp = String.format("%,.0f", twAmt);
        buf = comcr.insertStr(buf, szTmp, 21);

        buf = comcr.insertStr(buf, "美金：", 40);
        szTmp = String.format("%,.2f", usAmt);
        buf = comcr.insertStr(buf, szTmp, 46);

        buf = comcr.insertStr(buf, "日幣：", 65);
        szTmp = String.format("%,.0f", jpAmt);
        buf = comcr.insertStr(buf, szTmp, 73);

        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActF006 proc = new ActF006();
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
