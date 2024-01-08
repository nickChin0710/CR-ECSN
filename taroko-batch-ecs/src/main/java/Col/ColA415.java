/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/07/10  V1.01.01  phopho     Initial                                    *
* 109/01/09  V1.01.02  phopho     fix SQL: round(?,0) add cast(? as double)  *
* 109/03/03  V1.01.03  phopho     Mantis 0002876: auth_uncap_amt modify      *
* 109/03/17  V1.01.04  phopho     fix bug: if (m_code > 99) m_code = 99;     *
* 109/05/06  V1.01.05  phopho     Mantis 0003354: fix bug auth_uncap_amt     *
* 109/05/20  V1.01.06  phopho     Mantis 0003354: fix sql auth_uncap_amt     *
* 109/12/09  V1.00.07  shiyuqi    updated for project coding standard        *
* 112/10/04  V1.00.08  sunny      調整未到期分期的處理條件                                                  *
*****************************************************************************/

package Col;

import com.*;

public class ColA415 extends AccessDAO {
    private String progname = "前置協商回報債權資料處理程式 112/10/04  V1.00.08 ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debug1 = 0;
    String hCallErrorDesc = "";
    String hBusiBusinessDate = "";

    double hAcctAcctJrnlBal = 0;
    double hAcctTempAdiInterest = 0;
    double hAchtStmtThisTtlAmt = 0;
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    long hAcnoLineOfCreditAmt = 0;
    String hAcnoPaymentRate1 = "";
    String hAcnoPaymentRate2 = "";
    String hAcnoPaymentRate3 = "";
    String hAcnoPaymentRate4 = "";
    String hAcnoPaymentRate5 = "";
    String hAcnoPaymentRate6 = "";
    double hAcmlCashUseBalance = 0;
    double hDebtEndBal = 0;
    String hCcdtLiacSeqno = "";
    String hCcdtId = "";
    String hCcdtIdPSeqno = "";
    String hCcdtNotifyDate = "";
    String hCcdtApplyDate = "";
    String hCcdtInterestBaseDate = "";
    String hCcdtAcctStatus = "";
    double hCcdtInEndBal = 0;
    String hCcdtEthicRiskMark = "";
    double hCcdtOutEndBal = 0;
    double hCcdtLastestPayAmt = 0;
    String hCcdtNotSendFlag = "";
    String hCcdtHasRelaFlag = "";
    String hCcdtHasSupFlag = "";
    double hCcdtEcsTotAmt = 0;
    String hCcdtNoCalcFlag = "";
    String hCcdtProcFlag = "";
    String hCcdtAprFlag = "";
    double hCcdtOutCapital = 0;
    double hCcdtOutInterest = 0;
    double hCcdtOutFee = 0;
    double hCcdtOutPn = 0;
    String hCcdtRowid = "";
    double hCcdtAuthUncapAmt = 0;
    String hCcdtAcctStatusFlag = "";
    String hCcdtDebtRemark = "";
    
    int hCcddMcode = 0;
    double hCcddUnbillTotAmt = 0;
    double hCcddBilledTotAmt = 0;
    double hCcddUnbillCapitalAmt = 0;
    double hCcddBilledCapitalAmt = 0;
    double hCcddUnbillFeeAmt = 0;
    double hCcddUnbillItAmt = 0;
    int hCcddInterestDays = 0;
    double hCcddAddedInterestAmt = 0;
    double hCcddLastestPayAmt = 0;
    String hCcddEthicRiskMark = "";
    double hCcddBilledFeeAmt = 0;
    double hCcddBilledInterestAmt = 0;
    double hCcddBilledPnAmt = 0;
    String hOwsmWfValue = "";
    double hOwsmWfValue6 = 0;
    double hAgenRevolvingInterest1 = 0;
    double hAgenRevolvingInterest2 = 0;
    String hCallBatchSeqno = "";
    String hWdayThisAcctMonth = "";
    String hWdayLastAcctMonth = "";

    long totalCnt = 0;
    int batchFlag = 0;
    int hTempDays = 0;
    String hTempCardBlock = "";
    double hTempUnbillFeeAmt = 0;
    String hTempNotSendFlag = "";
    String[] tempPaymentRate = new String[10];

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColA415 proc = new ColA415();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
        	dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }

            // 檢查參數
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : ColA415 ", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            if (args.length == 1)
                batchFlag = 1;

            hBusiBusinessDate = "";
            selectPtrBusinday();
            selectPtrActgeneral();
            selectOfwSysparm();
            selectColLiacDebt();

            showLogMessage("I", "", "程式執行結束,累計筆數 : [" + totalCnt + "]");

            comcr.callbatch(1, 0, 0);

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
      // ************************************************************************

    private void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        
        selectSQL = "business_date ";
        daoTable = "ptr_businday";
        whereStr = "fetch first 1 row only";

        if (selectTable() > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }

    }

    // ************************************************************************
    private void selectOfwSysparm() throws Exception {
        hOwsmWfValue6 = 0;
        selectSQL = "wf_value6 ";
        daoTable = "ptr_sys_parm";
        whereStr = "where wf_parm = 'SYSPARM' and wf_key = 'COL_LIAC'";

        if (selectTable() > 0) {
            hOwsmWfValue6 = getValueDouble("wf_value6");
            showLogMessage("I", "", "select_ofw_sysparm，取得wf_parm[COL_LIAC]，wf_value6["+hOwsmWfValue6+"]");
        }

        if (notFound.equals("Y")) {
        	hOwsmWfValue6 = 0;  //預設為0
            //comcr.errRtn("select_ofw_sysparm error!", "", hCallBatchSeqno);
        	showLogMessage("I", "", "select_ofw_sysparm error!wf_parm[COL_LIAC]，wf_value6[0]，預設為0");
            
        }
    }

    // ************************************************************************
    private void selectColLiacDebt() throws Exception {
        selectSQL = "id_no, uf_idno_pseqno(id_no) as id_p_seqno, liac_seqno, interest_base_date, notify_date, apply_date, "
                + "decode(no_calc_flag,'','N',no_calc_flag) no_calc_flag, decode(not_send_flag,'','N',not_send_flag) not_send_flag, "
                + "decode(apr_flag,'','N',apr_flag) apr_flag, proc_flag, debt_remark, "
//                + "to_date(?,'yyyymmdd') - to_date(apply_date,'yyyymmdd') temp_days, rowid as rowid ";
                + "days(to_date(?,'yyyymmdd')) - days(to_date(apply_date,'yyyymmdd')) temp_days, rowid as rowid ";
        daoTable = "col_liac_debt";
        whereStr = "where proc_flag in ('0','1','R')"; /* '0':default '1':已計算 '2':已產生債權回報媒體  (0.資料轉入,1.待報送,2.已報送,A.不須報送,R.待處理)*/
        setString(1, hBusiBusinessDate);

        openCursor();
        while (fetchTable()) {
            hCcdtId = getValue("id_no");
            hCcdtIdPSeqno = getValue("id_p_seqno");
            hCcdtLiacSeqno = getValue("liac_seqno");
            hCcdtInterestBaseDate = getValue("interest_base_date");
            hCcdtNotifyDate = getValue("notify_date");
            hCcdtApplyDate = getValue("apply_date");
            hCcdtNoCalcFlag = getValue("no_calc_flag");
            hCcdtNotSendFlag = getValue("not_send_flag");
            hCcdtAprFlag = getValue("apr_flag");
            hCcdtProcFlag = getValue("proc_flag");
            hTempDays = getValueInt("temp_days");
            hCcdtRowid = getValue("rowid");
            hTempNotSendFlag = "N";
            hCcdtDebtRemark = getValue("debt_remark");  //phopho add

            if ((!hCcdtProcFlag.equals("0")) && (hCcdtNoCalcFlag.equals("Y"))) {
                hCcdtProcFlag = "1";
                updateColLiacDebt1();
                continue;
            }
            if (hCcdtProcFlag.equals("1")) {
                if ((hTempDays != 10) && (hCcdtNotSendFlag.equals("N")) && (hCcdtAprFlag.equals("N")))
                    continue;
                if (batchFlag == 0)
                    continue;
            }
            if ((hCcdtProcFlag.equals("0")) || ((hCcdtProcFlag.equals("1")) && (hCcdtNoCalcFlag.equals("N")))
                    || ((hCcdtProcFlag.equals("R")) && (hCcdtNoCalcFlag.equals("N"))))
                updateColLiacDebtDtl0();

            hCcdtEcsTotAmt = 0;
            hCcdtInEndBal = 0;
            hCcdtOutEndBal = 0;
            hCcdtLastestPayAmt = 0;
            hCcdtOutCapital = 0;
            hCcdtOutInterest = 0;
            hCcdtOutPn = 0;
            hCcdtOutFee = 0;
            hCcddEthicRiskMark = "     ";
            hCcdtEthicRiskMark = "     ";
            hCcdtAcctStatus = "0";

            selectActAcno();
            if (selectColLiacNego() != 0) {
                hCcdtProcFlag = "A"; /* 狀態已改變, 不回報 */
                updateColLiacDebt1();
                continue;
            }
            hCcdtHasSupFlag = "N";
            if (selectCrdCard() != 0) {
                hCcdtHasSupFlag = "Y";
                hTempNotSendFlag = "Y";
            }

            hCcdtHasRelaFlag = "N";
            if (selectCrdRela() != 0) {
                hCcdtHasRelaFlag = "Y";
                hTempNotSendFlag = "Y";
            }

            if ((hCcdtInEndBal == 0) && (hCcdtOutEndBal != 0))
                hTempNotSendFlag = "Y";

            if ((hCcdtInEndBal <= hOwsmWfValue6) && (hCcdtInEndBal != 0))
                hTempNotSendFlag = "Y";

            if (hCcdtProcFlag.equals("0")) {
                updateColLiacDebt();
            } else {
                updateColLiacDebt2();
            }

            totalCnt++;
            processDisplay(100); // every nnnnn display message
            commitDataBase();
        }

        closeCursor();
    }

    // ************************************************************************
    private void selectActAcno() throws Exception {
        double tempDouble;
        long tempLong;
//        selectSQL = "a.p_seqno, a.acct_type, a.acct_key, a.acct_status, a.stmt_cycle, "
        selectSQL = "a.acno_p_seqno, a.acct_type, a.acct_key, a.acct_status, a.stmt_cycle, "
                + "a.payment_rate1, a.payment_rate2, a.payment_rate3, a.payment_rate4, "
                + "a.payment_rate5, a.payment_rate6, a.line_of_credit_amt, "
                + "decode(sign(a.acct_status-'3'),-1,decode(sign( ? -b.this_close_date),1,"
//                + "to_date( ? ,'yyyymmdd')-to_date(b.this_close_date,'yyyymmdd'),0),decode(sign( ? - ? ),1,"
//                + "to_date( ? ,'yyyymmdd')-to_date( ? ,'yyyymmdd'),0)) interest_days,b.last_acct_month, "
//                + "days(to_date( ? ,'yyyymmdd'))-days(to_date(b.this_close_date,'yyyymmdd')),0),decode(sign( ? - ? ),1,"
//                + "days(to_date( ? ,'yyyymmdd'))-days(to_date( ? ,'yyyymmdd')),0)) interest_days, b.last_acct_month, "
				+ "days(to_date( ? ,'yyyymmdd'))-days(to_date(b.this_close_date,'yyyymmdd')),0),decode(sign( ? - ? ),1,"
				+ "days(to_date( ? ,'yyyymmdd'))-days(to_date( ? ,'yyyymmdd')),0)) interest_days, b.last_acct_month, "
				+ "b.this_acct_month, a.int_rate_mcode ";
//        daoTable = "act_acno a,ptr_workday b, crd_idno c ";
//        whereStr = "where c.id_no = ?  and a.id_p_seqno = c.id_p_seqno and acno_flag <> 'Y' and a.stmt_cycle = b.stmt_cycle ";
        daoTable = "act_acno a, ptr_workday b ";
        //whereStr = "where a.id_p_seqno = ? and acno_flag <> 'Y' and a.stmt_cycle = b.stmt_cycle";
        whereStr = "where a.id_p_seqno = ? and a.acno_flag = '1' and a.stmt_cycle = b.stmt_cycle"; //只計算一般卡
        setString(1, hCcdtInterestBaseDate);
        setString(2, hCcdtInterestBaseDate);
        setString(3, hCcdtInterestBaseDate);
        setString(4, hBusiBusinessDate);
        setString(5, hCcdtInterestBaseDate);
        setString(6, hBusiBusinessDate);
//        setString(7, h_ccdt_id);
        setString(7, hCcdtIdPSeqno);
        
        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
//            h_acno_p_seqno = getValue("p_seqno", i);
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno", i);
            hAcnoAcctType = getValue("act_acno.acct_type", i);
            hAcnoAcctKey = getValue("act_acno.acct_key", i);
            hAcnoAcctStatus = getValue("act_acno.acct_status", i);
            hAcnoStmtCycle = getValue("act_acno.stmt_cycle", i);
            hAcnoPaymentRate1 = getValue("act_acno.payment_rate1", i);
            hAcnoPaymentRate2 = getValue("act_acno.payment_rate2", i);
            hAcnoPaymentRate3 = getValue("act_acno.payment_rate3", i);
            hAcnoPaymentRate4 = getValue("act_acno.payment_rate4", i);
            hAcnoPaymentRate5 = getValue("act_acno.payment_rate5", i);
            hAcnoPaymentRate6 = getValue("act_acno.payment_rate6", i);
            hAcnoLineOfCreditAmt = getValueLong("act_acno.line_of_credit_amt", i);
            hCcddInterestDays = getValueInt("act_acno.interest_days", i);
            hWdayLastAcctMonth = getValue("act_acno.last_acct_month", i);
            hWdayThisAcctMonth = getValue("act_acno.this_acct_month", i);
            tempPaymentRate[0] = getValue("act_acno.payment_rate1", i);
            tempPaymentRate[1] = getValue("act_acno.payment_rate2", i);
            tempPaymentRate[2] = getValue("act_acno.payment_rate3", i);
            tempPaymentRate[3] = getValue("act_acno.payment_rate4", i);
            tempPaymentRate[4] = getValue("act_acno.payment_rate5", i);
            tempPaymentRate[5] = getValue("act_acno.payment_rate6", i);

            // h_ccdd_mcode = get_M_code(h_acno_p_seqno.arr);
//            h_ccdd_mcode = comr.getMcode(h_acno_acct_type, h_acno_p_seqno);
            hCcddMcode = getValueInt("act_acno.int_rate_mcode", i);
            if (hCcddMcode > 99) hCcddMcode = 99;  //phopho mod 2020.3.17
            selectActComboMJrnl();
            selectActDebt();
            selectBilContract();
            selectActAcct();
            selectActAcctHst();
            checkEthicRisk();

            if (hAcnoAcctStatus.compareTo(hCcdtAcctStatus) > 0) {
                hCcdtAcctStatus = hAcnoAcctStatus;
            }
            hCcddUnbillCapitalAmt = hCcddUnbillCapitalAmt + hAcmlCashUseBalance;
            hCcddUnbillTotAmt = hCcddUnbillTotAmt + hAcmlCashUseBalance + hTempUnbillFeeAmt;

            if (comcr.str2int(hAcnoAcctStatus) < 3) {
                tempDouble = (hCcddBilledCapitalAmt + hCcddUnbillCapitalAmt + hCcddUnbillItAmt)
                        * hCcddInterestDays * hAgenRevolvingInterest1 / 100.0;
                tempLong = (long) (tempDouble / 100.0 + 0.5);
                hCcddAddedInterestAmt = tempLong;
                hCcdtInEndBal = hCcdtInEndBal + hCcddBilledTotAmt + hCcddUnbillCapitalAmt
                        + hCcddUnbillItAmt + hCcddUnbillFeeAmt + hTempUnbillFeeAmt + hCcddAddedInterestAmt;
                hCcdtOutEndBal = hCcdtOutEndBal + hCcddBilledTotAmt + hCcddUnbillCapitalAmt
                        + hCcddUnbillItAmt + hCcddUnbillFeeAmt + hTempUnbillFeeAmt + hCcddAddedInterestAmt;
                hCcdtOutCapital = hCcdtOutCapital + hCcddBilledCapitalAmt + hCcddUnbillCapitalAmt
                        + hCcddUnbillItAmt;
                hCcdtOutInterest = hCcdtOutInterest + hCcddBilledInterestAmt + hCcddAddedInterestAmt;
                hCcdtOutPn = hCcdtOutPn + hCcddBilledPnAmt;
                hCcdtOutFee = hCcdtOutFee + hCcddUnbillFeeAmt + hCcddBilledFeeAmt + hTempUnbillFeeAmt;
            } else {
                tempDouble = (hCcddBilledCapitalAmt + hCcddUnbillCapitalAmt + hCcddUnbillItAmt)
                        * hCcddInterestDays * hAgenRevolvingInterest2 / 100.0;
                tempLong = (long) (tempDouble / 100.0 + 0.5);
                hCcddAddedInterestAmt = tempLong;
                hCcdtInEndBal = hCcdtInEndBal + hCcddBilledTotAmt + hCcddUnbillTotAmt - hDebtEndBal;
                hCcdtOutEndBal = hCcdtOutEndBal + hCcddBilledTotAmt + hCcddUnbillTotAmt
                        + hCcddAddedInterestAmt + hAcctTempAdiInterest;

                hCcdtOutCapital = hCcdtOutCapital + hCcddBilledCapitalAmt + hCcddUnbillCapitalAmt;
                hCcdtOutInterest = hCcdtOutInterest + hCcddBilledInterestAmt + hCcddAddedInterestAmt
                        + hAcctTempAdiInterest;
                hCcdtOutPn = hCcdtOutPn + hCcddBilledPnAmt;
                hCcdtOutFee = hCcdtOutFee + hCcddUnbillFeeAmt + hCcddBilledFeeAmt;
            }
            selectActJrnl();
            hCcdtLastestPayAmt = hCcdtLastestPayAmt + hCcddLastestPayAmt;

            hCcdtEcsTotAmt = hCcdtEcsTotAmt + hCcddUnbillTotAmt + hCcddBilledTotAmt;
            if (insertColLiacDebtDtl() != 0)
                updateColLiacDebtDtl1();
        }
    }

    // ************************************************************************
    private void selectActDebt() throws Exception {
        hCcddBilledTotAmt = 0;
        hCcddUnbillTotAmt = 0;
        hCcddBilledCapitalAmt = 0;
        hCcddUnbillCapitalAmt = 0;
        hCcddBilledFeeAmt = 0;
        hCcddUnbillFeeAmt = 0;
        hDebtEndBal = 0;
        hCcddBilledInterestAmt = 0;
        hCcddBilledPnAmt = 0;
        sqlCmd = "select sum(decode(sign(acct_month- ? ),1,0,end_bal)) as billed_tot_amt, "; /* 已posting 欠款總額 */
        sqlCmd += "sum(decode(sign(acct_month- ? ),1,end_bal,0)) as unbill_tot_amt, "; /* 未posting 欠款總額 */
        sqlCmd += "sum(decode(sign(acct_month- ? ),1,0, "; /* 已posting 欠款本金 */
        sqlCmd += "decode(acct_code,'BL',end_bal,'CA',end_bal,'IT',end_bal, ";
        sqlCmd += "                       'ID',end_bal,'AO',end_bal,'OT',end_bal, ";
        sqlCmd += "                       'CB',end_bal,'DB',end_bal,0))) as billed_capital_amt, ";
        sqlCmd += "sum(decode(sign(acct_month- ? ),1, "; /* 未posting 欠款本金 */
        sqlCmd += "decode(acct_code,'BL',end_bal,'CA',end_bal,'IT',end_bal, ";
        sqlCmd += "                       'ID',end_bal,'AO',end_bal,'OT',end_bal, ";
        sqlCmd += "                       'CB',end_bal,'DB',end_bal,0),0)) as unbill_capital_amt, ";
        sqlCmd += "sum(decode(acct_code,'PN',end_bal,0)) as billed_pn_amt, "; /* 違約金 */
        sqlCmd += "sum(decode(sign(acct_month- ? ),1,0, "; /* 已posting 費用 */
        sqlCmd += "decode(acct_code,'CF',end_bal,'SF',end_bal,'AF',end_bal, ";
        sqlCmd += "                       'LF',end_bal,'PF',end_bal,'DP',end_bal,'CC',end_bal,0))) as billed_fee_amt, ";
        sqlCmd += "sum(decode(sign(acct_month- ? ),1, "; /* 未posting 費用 */
        sqlCmd += "decode(acct_code,'CF',end_bal,'SF',end_bal,'AF',end_bal, ";
        sqlCmd += "                       'LF',end_bal,'PF',end_bal,'DP',end_bal,'CC',end_bal,0),0)) as unbill_fee_amt, ";
        sqlCmd += "sum(decode(acct_code,'RI',end_bal,'AI',end_bal,'CI',end_bal,0)) as billed_interest_amt, "; /* 利息 */
        sqlCmd += "sum(decode(acct_code,'AI',end_bal,0)) as end_bal ";
        sqlCmd += "from act_debt where p_seqno = ?";
        setString(1, hWdayThisAcctMonth);
        setString(2, hWdayThisAcctMonth);
        setString(3, hWdayThisAcctMonth);
        setString(4, hWdayThisAcctMonth);
        setString(5, hWdayThisAcctMonth);
        setString(6, hWdayThisAcctMonth);
        setString(7, hAcnoPSeqno);
        
        extendField = "act_debt.";

        if (selectTable() > 0) {
            hCcddBilledTotAmt = getValueDouble("act_debt.billed_tot_amt");
            hCcddUnbillTotAmt = getValueDouble("act_debt.unbill_tot_amt");
            hCcddBilledCapitalAmt = getValueDouble("act_debt.billed_capital_amt");
            hCcddUnbillCapitalAmt = getValueDouble("act_debt.unbill_capital_amt");
            hCcddBilledPnAmt = getValueDouble("act_debt.billed_pn_amt");
            hCcddBilledFeeAmt = getValueDouble("act_debt.billed_fee_amt");
            hCcddUnbillFeeAmt = getValueDouble("act_debt.unbill_fee_amt");
            hCcddBilledInterestAmt = getValueDouble("act_debt.billed_interest_amt");
            hDebtEndBal = getValueDouble("act_debt.end_bal");
        }
    }

    // ************************************************************************
    private void checkEthicRisk() throws Exception {
        hCcddEthicRiskMark = "     ";
        int inta;
        for (inta = 0; inta < 6; inta++) {
            if (tempPaymentRate[inta].equals(" ")) {
                hCcddEthicRiskMark = "1" + comc.getSubString(hCcddEthicRiskMark, 1);
                hCcdtEthicRiskMark = "1" + comc.getSubString(hCcdtEthicRiskMark, 1);
                break;
            }
        }
        for (inta = 0; inta < 2; inta++) {
            if (tempPaymentRate[inta].equals(" ") == false)
                break;
        }
        if ((inta >= 2) && (hCcddBilledCapitalAmt + hCcddUnbillCapitalAmt > 0)) {
            for (inta = 2; inta < 6; inta++) {
                if ((tempPaymentRate[inta].compareTo("0A") == 0) || (tempPaymentRate[inta].compareTo("0B") == 0)
                        || (tempPaymentRate[inta].compareTo("0E") == 0))
                    continue;
                else
                    break;
            }
            if (inta >= 6) {
                hCcddEthicRiskMark = comc.getSubString(hCcddEthicRiskMark, 0, 1) + "2"
                        + comc.getSubString(hCcddEthicRiskMark, 2);
                hCcdtEthicRiskMark = comc.getSubString(hCcdtEthicRiskMark, 0, 1) + "2"
                        + comc.getSubString(hCcdtEthicRiskMark, 2);
            }
        }
        if ((hCcddBilledTotAmt + hCcddUnbillTotAmt + hCcddUnbillItAmt
                - hAchtStmtThisTtlAmt) > (hAcnoLineOfCreditAmt * 0.3)) {
            hCcddEthicRiskMark = comc.getSubString(hCcddEthicRiskMark, 0, 2) + "3"
                    + comc.getSubString(hCcddEthicRiskMark, 3);
            hCcdtEthicRiskMark = comc.getSubString(hCcdtEthicRiskMark, 0, 2) + "3"
                    + comc.getSubString(hCcdtEthicRiskMark, 3);
        }
        
//        d.	check_ethic_risk()，增加道德風險內容4，表示近半年單筆消費金額超過10,000(含)以上。並將筆誤寫入備註，例如:【近半年單筆金額超過10,000，共XX筆】。須注意文字長度不得超過資料庫設定。
//        e.	上述邏輯內容，TABLE: BIL_BILL 帳單明細檔。相關欄位:近半年之資料，以【PURCHASE_DATE(消費日期)】為條件。金額指【DEST_AMT(目的地金額)】，此欄位為台幣欄位，若以外幣消費，此欄位代表轉為台幣的金額。
        int hCnt = 0;

        sqlCmd = "select count(dest_amt) dcnt from bil_bill ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and purchase_date >= to_char(add_months(to_date(?, 'yyyymmdd'), -6), 'yyyymmdd') ";
        sqlCmd += "and dest_amt >= 10000 ";
        setString(1, hCcdtIdPSeqno);
        setString(2, hBusiBusinessDate);
        selectTable();
        hCnt = getValueInt("dcnt");
        if (hCnt > 0) {
        	hCcddEthicRiskMark = comc.getSubString(hCcddEthicRiskMark, 0, 3) + "4"
                    + comc.getSubString(hCcddEthicRiskMark, 4);
            hCcdtEthicRiskMark = comc.getSubString(hCcdtEthicRiskMark, 0, 3) + "4"
                    + comc.getSubString(hCcdtEthicRiskMark, 4);
        	hCcdtDebtRemark = comcr.right(hCcdtDebtRemark + String.format("近半年單筆金額超過10,000，共%d筆。", hCnt),60);
        }
        
    }

    // ************************************************************************
    private void selectActAcctHst() throws Exception {
        hAchtStmtThisTtlAmt = 0;
        sqlCmd = "select stmt_this_ttl_amt ";
        sqlCmd += "from act_acct_hst where p_seqno = ? and acct_month = ? ";
        setString(1, hAcnoPSeqno);
        setString(2, hWdayLastAcctMonth);
        
        extendField = "act_acct_hst.";

        if (selectTable() > 0) {
            hAchtStmtThisTtlAmt = getValueDouble("act_acct_hst.stmt_this_ttl_amt");
        }
    }

    // ************************************************************************
    private void selectActAcct() throws Exception {
        hAcctTempAdiInterest = 0;
        hAcctAcctJrnlBal = 0;
        sqlCmd = "select adi_beg_bal, acct_jrnl_bal ";
        sqlCmd += "from act_acct where p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_acct.";

        if (selectTable() > 0) {
            hAcctTempAdiInterest = getValueDouble("act_acct.adi_beg_bal");
            hAcctAcctJrnlBal = getValueDouble("act_acct.acct_jrnl_bal");
        }

        if (notFound.equals("Y")) {
            String err1 = "select_act_acct error!";
            String err2 = "p_seqno=[" + hAcnoPSeqno + "]";
            rollbackDataBase();
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private int selectCrdCard() throws Exception {
        sqlCmd = "select 1 as hcnt ";
        sqlCmd += "from crd_card ";
        sqlCmd += "where major_id_p_seqno = ? and id_p_seqno != major_id_p_seqno ";
        sqlCmd += "fetch first 1 row only";
        setString(1, hCcdtIdPSeqno);

        selectTable();

        if (notFound.equals("Y"))
            return 0;
        return 1;
    }

    // ************************************************************************
    private void selectPtrActgeneral() throws Exception {
        sqlCmd = "select max(revolving_interest1) revolving_interest1, ";
        sqlCmd += "max(revolving_interest2) revolving_interest2 ";
        sqlCmd += "from ptr_actgeneral_n";

        if (selectTable() > 0) {
            hAgenRevolvingInterest1 = getValueDouble("revolving_interest1");
            hAgenRevolvingInterest2 = getValueDouble("revolving_interest2");
        }

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_actgeneral error!";
            String err2 = "";
            comcr.hCallErrorDesc = "系統錯誤, 請通知資訊室";
            rollbackDataBase();
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void selectBilContract() throws Exception {
        hCcddUnbillItAmt = 0;
        hTempUnbillFeeAmt = 0;
        sqlCmd = "select sum(unit_price*(install_tot_term-";
        sqlCmd += "install_curr_term)+remd_amt+";
        sqlCmd += "decode(install_curr_term,0,first_remd_amt,0)) as unbill_it_amt, ";
        sqlCmd += "sum(clt_unit_price*(decode(sign(clt_install_tot_term-";
        sqlCmd += "install_curr_term),-1,0,";
        sqlCmd += "clt_install_tot_term-install_curr_term))";
        sqlCmd += "+clt_remd_amt) as unbill_fee_amt ";
        sqlCmd += "from bil_contract ";
//        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "where acno_p_seqno = ? ";
        sqlCmd += "and   install_tot_term != install_curr_term ";        
//        sqlCmd += "and   decode(refund_apr_flag,'','N',refund_apr_flag) <> 'Y' ";
//        sqlCmd += "and   (((decode(auth_code,'','N',auth_code) not in ('N','REJECT','P','reject')";
//        sqlCmd += "and    contract_kind = '2' ) or contract_kind = '1')";
//        sqlCmd += "and   ( apr_date <> '' or delv_confirm_date <> '' )) ";
        setString(1, hAcnoPSeqno);
        
        extendField = "bil_contract.";

        if (selectTable() > 0) {
            hCcddUnbillItAmt = getValueDouble("bil_contract.unbill_it_amt");
            hTempUnbillFeeAmt = getValueDouble("bil_contract.unbill_fee_amt");
        }
    }

    // ************************************************************************
    private void selectActJrnl() throws Exception {
        hCcddLastestPayAmt = 0;
        sqlCmd = "select sum(nvl(transaction_amt,0)) lastest_pay_amt ";
        sqlCmd += "from act_jrnl where p_seqno = ? ";
//      sqlCmd += " AND acct_type = ? AND    acct_key = ? ";
        sqlCmd += "and crt_date > to_char(add_months(to_date( ? ,'yyyymmdd'),-1),'yyyymmdd') ";
        sqlCmd += "and tran_type in ('ACH1','AUT1','AUT2','AUT3','AUT4','AUT5',";
        sqlCmd += "                  'AUT6','COU1','COU2','COU3','COU4','COU5',";
        sqlCmd += "                  'COU6','EPAY','TIKT','IBC1','IBC2','IBC3',";
        sqlCmd += "                  'IBC4','IBA1','IBA2','IBA3','IBA4','TEBK',";
        sqlCmd += "                  'INBK','IBOT','OTHR')";
        setString(1, hAcnoPSeqno);
        setString(2, hCcdtApplyDate);
//        setString(1, h_acno_acct_type);
//        setString(2, h_acno_acct_key);

        extendField = "act_jrnl.";
        
        if (selectTable() > 0) {
            hCcddLastestPayAmt = getValueDouble("act_jrnl.lastest_pay_amt");
        }
    }

    // ************************************************************************
    private void selectActComboMJrnl() throws Exception {
        hAcmlCashUseBalance = 0;
        sqlCmd = "select sum(cash_use_balance) cash_use_balance ";
        sqlCmd += "from act_combo_m_jrnl where p_seqno = ? ";
//        sqlCmd += " and acct_type = ? and acct_key = ? "; //no column
        setString(1, hAcnoPSeqno);
//        setString(2, h_acno_acct_type);
//        setString(2, h_acno_acct_key);
        
        extendField = "act_combo_m_jrnl.";

        if (selectTable() > 0) {
            hAcmlCashUseBalance = getValueDouble("act_combo_m_jrnl.cash_use_balance");
        }
    }

    // ************************************************************************
    private int selectColLiacNego() throws Exception {
        sqlCmd = "select 1 as hcnt ";
        sqlCmd += "from col_liac_nego ";
        sqlCmd += "where liac_seqno = ? and liac_status in ('1','2') ";
        sqlCmd += "fetch first 1 row only";
        setString(1, hCcdtLiacSeqno);

        selectTable();

        if (notFound.equals("Y"))
            return 1;
        return 0;
    }

    // ************************************************************************
    private int insertColLiacDebtDtl() throws Exception {
        dateTime();
        daoTable = "col_liac_debt_dtl";
        extendField = daoTable + ".";
        setValue(extendField+"id_p_seqno", hCcdtIdPSeqno);
        setValue(extendField+"id_no", hCcdtId);
        setValue(extendField+"liac_seqno", hCcdtLiacSeqno);
        setValue(extendField+"notify_date", hCcdtNotifyDate);
        setValue(extendField+"apply_date", hCcdtApplyDate);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"acct_key", hAcnoAcctKey);
        setValue(extendField+"acct_status", hAcnoAcctStatus);
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"acct_month", hWdayThisAcctMonth);
        setValueInt(extendField+"mcode", hCcddMcode);
        setValue(extendField+"payment_rate1", hAcnoPaymentRate1);
        setValue(extendField+"payment_rate2", hAcnoPaymentRate2);
        setValue(extendField+"payment_rate3", hAcnoPaymentRate3);
        setValue(extendField+"payment_rate4", hAcnoPaymentRate4);
        setValue(extendField+"payment_rate5", hAcnoPaymentRate5);
        setValue(extendField+"payment_rate6", hAcnoPaymentRate6);
        setValueDouble(extendField+"line_of_credit_amt", hAcnoLineOfCreditAmt);
        setValueDouble(extendField+"stmt_this_ttl_amt", hAchtStmtThisTtlAmt);
        setValueDouble(extendField+"tot_amt", hCcddUnbillTotAmt + hCcddBilledTotAmt + hCcddUnbillItAmt);
        setValueDouble(extendField+"unbill_tot_amt", hCcddUnbillTotAmt);
        setValueDouble(extendField+"billed_tot_amt", hCcddBilledTotAmt);
        setValueDouble(extendField+"unbill_capital_amt", hCcddUnbillCapitalAmt);
        setValueDouble(extendField+"billed_capital_amt", hCcddBilledCapitalAmt);
        setValueDouble(extendField+"unbill_fee_amt", hCcddUnbillFeeAmt + hTempUnbillFeeAmt);
        setValueDouble(extendField+"unbill_it_amt", hCcddUnbillItAmt);
        setValueDouble(extendField+"added_interest_amt", hCcddAddedInterestAmt);
        setValueDouble(extendField+"billed_fee_amt", hCcddBilledFeeAmt);
        setValueDouble(extendField+"billed_interest_amt", hCcddBilledInterestAmt);
        setValueDouble(extendField+"billed_pn_amt", hCcddBilledPnAmt);
        setValueDouble(extendField+"lastest_pay_amt", hCcddLastestPayAmt);
        setValue(extendField+"ethic_risk_mark", hCcddEthicRiskMark);
        setValueInt(extendField+"interest_days", hCcddInterestDays);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            return -1;
        }
        return 0;
    }

    // ************************************************************************
    private void updateColLiacDebt() throws Exception {
//    	a.	針對要重算債權的資料，增加 col_liac_debt.auth_uncap_amt 欄位計算。
//    	b.	上述欄位值，以id_p_seqno 查詢 crd_idno, crd_card, cca_auth_txlog。Mtch_flag, 若為Y，表示已請款。取得 Mtch_fla <> 'Y'，加總 nt_amt。-->取得授權未請款金額。
//    	c.	重新取得【ACCT_STATUS_FLAG (帳戶狀態是否有呆帳戶)】欄位值。邏輯參考【col_a460 前置協商主檔轉入處理程式】修改內容。
    	selectAuthUncapAmt();
    	selectAcctStatusFlag();
    	
        //Mantis 0002876: auth_uncap_amt modify (add out_capital, in_end_bal, out_end_bal)
    	//h_ccdt_auth_uncap_amt += h_ccdt_out_capital + h_ccdt_in_end_bal + h_ccdt_out_end_bal;
    	//Mantis 0003354:
//    	將【已授權未請款金額(auth_uncap_amt)】加入【本金(out_capital)】、【對內債權餘額(in_end_bal)】、【對外債權餘額(out_end_bal)】等。
//    	【本金(out_capital)】 　　　 = 【本金(out_capital)】+【已授權未請款金額(auth_uncap_amt)】。
//    	【對內債權餘額(in_end_bal)】 　= 【對內債權餘額(in_end_bal)】+【已授權未請款金額(auth_uncap_amt)】。
//    	【對外債權餘額(out_end_bal)】 = 【對外債權餘額(out_end_bal)】+【已授權未請款金額(auth_uncap_amt)】。
    	hCcdtOutCapital = hCcdtOutCapital + hCcdtAuthUncapAmt;
    	hCcdtInEndBal = hCcdtInEndBal + hCcdtAuthUncapAmt;
    	hCcdtOutEndBal = hCcdtOutEndBal + hCcdtAuthUncapAmt;
    	
        updateSQL = "proc_date      = ?, proc_flag         = '1', acct_status         = ?, "
                + "in_end_bal       = round(cast(? as double),0), ecs_tot_amt         = ?, out_end_bal     = round(cast(? as double),0), "
                + "out_capital      = round(cast(? as double),0), out_interest        = round(cast(? as double),0), out_fee    = round(cast(? as double),0), "
                + "out_pn           = round(cast(? as double),0), lastest_pay_amt     = ?, in_end_bal_new  = round(cast(? as double),0), "
                + "out_end_bal_new  = round(cast(? as double),0), lastest_pay_amt_new = ?, out_capital_new = round(cast(? as double),0), "
                + "out_interest_new = round(cast(? as double),0), out_fee_new         = round(cast(? as double),0), out_pn_new = round(cast(? as double),0), "
                + "ethic_risk_mark  = ?, not_send_flag = decode(trim(cast(? as varchar(5))),'',"
                + "                (case when (round(cast(? as double),0)=0) and (round(cast(? as double),0)=0) "
                + "                then 'N' else ? end),'Y'), has_sup_flag    = ?, has_rela_flag   = ?, "
                + "acct_status_flag = ?, auth_uncap_amt = ?, debt_remark = ?, "  //phopho add
                + "mod_time = sysdate, mod_pgm = ? ";
        daoTable = "col_liac_debt";
        whereStr = "WHERE rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hCcdtAcctStatus);
        setDouble(3, hCcdtInEndBal);
        setDouble(4, hCcdtEcsTotAmt);
        setDouble(5, hCcdtOutEndBal);
        setDouble(6, hCcdtOutCapital);
        setDouble(7, hCcdtOutInterest);
        setDouble(8, hCcdtOutFee);
        setDouble(9, hCcdtOutPn);
        setDouble(10, hCcdtLastestPayAmt);
        setDouble(11, hCcdtInEndBal);
        setDouble(12, hCcdtOutEndBal);
        setDouble(13, hCcdtLastestPayAmt);
        setDouble(14, hCcdtOutCapital);
        setDouble(15, hCcdtOutInterest);
        setDouble(16, hCcdtOutFee);
        setDouble(17, hCcdtOutPn);
        setString(18, hCcdtEthicRiskMark);
        setString(19, hCcdtEthicRiskMark);
        setDouble(20, hCcdtInEndBal);
        setDouble(21, hCcdtOutEndBal);
        setString(22, hTempNotSendFlag);
        setString(23, hCcdtHasSupFlag);
        setString(24, hCcdtHasRelaFlag);
        setString(25, hCcdtAcctStatusFlag);  //add
        setDouble(26, hCcdtAuthUncapAmt);    //add
        setString(27, hCcdtDebtRemark);       //add
        setString(28, javaProgram);
        setRowId(29, hCcdtRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_col_liac_debt error!";
            String err2 = "rowid=[" + hCcdtRowid + "]";
            comcr.hCallErrorDesc = "系統錯誤, 請通知資訊室";
            rollbackDataBase();
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void updateColLiacDebt1() throws Exception {
        dateTime();
        updateSQL = "proc_date  = ?, proc_flag  = ?, mod_pgm    = ?, mod_time   = sysdate ";
        daoTable = "col_liac_debt";
        whereStr = "WHERE rowid = ?";
        setString(1, hBusiBusinessDate);
        setString(2, hCcdtProcFlag);
        setString(3, javaProgram);
        setRowId(4, hCcdtRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_col_liac_debt_1 error!";
            String err2 = "rowid=[" + hCcdtRowid + "]";
            comcr.hCallErrorDesc = "系統錯誤, 請通知資訊室";
            rollbackDataBase();
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void updateColLiacDebt2() throws Exception {
//    	a.	針對要重算債權的資料，增加 col_liac_debt.auth_uncap_amt 欄位計算。
//    	b.	上述欄位值，以id_p_seqno 查詢 crd_idno, crd_card, cca_auth_txlog。Mtch_flag, 若為Y，表示已請款。取得 Mtch_fla <> 'Y'，加總 nt_amt。-->取得授權未請款金額。
//    	c.	重新取得【ACCT_STATUS_FLAG (帳戶狀態是否有呆帳戶)】欄位值。邏輯參考【col_a460 前置協商主檔轉入處理程式】修改內容。
    	selectAuthUncapAmt();
    	selectAcctStatusFlag();
    	
        //Mantis 0002876: auth_uncap_amt modify (add out_capital, in_end_bal, out_end_bal)
    	//h_ccdt_auth_uncap_amt += h_ccdt_out_capital + h_ccdt_in_end_bal + h_ccdt_out_end_bal;
    	//Mantis 0003354:
//    	將【已授權未請款金額(auth_uncap_amt)】加入【本金(out_capital)】、【對內債權餘額(in_end_bal)】、【對外債權餘額(out_end_bal)】等。
//    	【本金(out_capital)】 　　　 = 【本金(out_capital)】+【已授權未請款金額(auth_uncap_amt)】。
//    	【對內債權餘額(in_end_bal)】 　= 【對內債權餘額(in_end_bal)】+【已授權未請款金額(auth_uncap_amt)】。
//    	【對外債權餘額(out_end_bal)】 = 【對外債權餘額(out_end_bal)】+【已授權未請款金額(auth_uncap_amt)】。
    	hCcdtOutCapital = hCcdtOutCapital + hCcdtAuthUncapAmt;
    	hCcdtInEndBal = hCcdtInEndBal + hCcdtAuthUncapAmt;
    	hCcdtOutEndBal = hCcdtOutEndBal + hCcdtAuthUncapAmt;
    	
        dateTime();
        updateSQL = "proc_date     = ?, proc_flag       = '1', in_end_bal      = round(cast(? as double),0), "
                + "ecs_tot_amt     = ?, out_end_bal     = round(cast(? as double),0), lastest_pay_amt = ?, "
                + "in_end_bal_new  = round(cast(? as double),0), out_end_bal_new  = round(cast(? as double),0), out_capital = round(cast(? as double),0), "
                + "out_interest    = round(cast(? as double),0), out_fee          = round(cast(? as double),0), out_pn      = round(cast(? as double),0), "
                + "out_capital_new = round(cast(? as double),0), out_interest_new = round(cast(? as double),0), out_fee_new = round(cast(? as double),0), "
                + "out_pn_new      = round(cast(? as double),0), lastest_pay_amt_new = ?, "
                + "acct_status_flag = ?, auth_uncap_amt = ?, "  //phopho add
                + "mod_time        = sysdate, mod_pgm = ? ";
        daoTable = "col_liac_debt";
        whereStr = "WHERE rowid = ?";
        setString(1, hBusiBusinessDate);
        setDouble(2, hCcdtInEndBal);
        setDouble(3, hCcdtEcsTotAmt);
        setDouble(4, hCcdtOutEndBal);
        setDouble(5, hCcdtLastestPayAmt);
        setDouble(6, hCcdtInEndBal);
        setDouble(7, hCcdtOutEndBal);
        setDouble(8, hCcdtOutCapital);
        setDouble(9, hCcdtOutInterest);
        setDouble(10, hCcdtOutFee);
        setDouble(11, hCcdtOutPn);
        setDouble(12, hCcdtOutCapital);
        setDouble(13, hCcdtOutInterest);
        setDouble(14, hCcdtOutFee);
        setDouble(15, hCcdtOutPn);
        setDouble(16, hCcdtLastestPayAmt);
        setString(17, hCcdtAcctStatusFlag);  //add
        setDouble(18, hCcdtAuthUncapAmt);    //add
        setString(19, javaProgram);
        setRowId(20, hCcdtRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_col_liac_debt_2 error!";
            String err2 = "rowid=[" + hCcdtRowid + "]";
            comcr.hCallErrorDesc = "系統錯誤, 請通知資訊室";
            rollbackDataBase();
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectAuthUncapAmt() throws Exception {
    	hCcdtAuthUncapAmt = 0;
 
        //20231003 sunny modify
        sqlCmd = "select nvl(sum(g.nt_amt),0) nt_amt from cca_auth_txlog g,act_acno a ";
        sqlCmd += "where g.acno_p_seqno=a.p_seqno ";
        sqlCmd += "and a.id_p_seqno = ? ";
        sqlCmd += "and g.mtch_flag <> 'Y' ";
        sqlCmd += "and g.nt_amt > 1 ";
        sqlCmd += "and g.iso_resp_code = '00' ";
        setString(1, hCcdtIdPSeqno);
        
        extendField = "auth_uncap_amt.";
        
        selectTable();
        hCcdtAuthUncapAmt = getValueDouble("auth_uncap_amt.nt_amt");
    }
    
    /***********************************************************************/
    void selectAcctStatusFlag() throws Exception {
    	int hCnt = 0;
    	hCcdtAcctStatusFlag = "";

        sqlCmd = "select count(a.p_seqno) hcnt from act_acno a, act_acct c ";
        sqlCmd += "where a.p_seqno = c.p_seqno ";
        sqlCmd += "and a.id_p_seqno = ? ";
        sqlCmd += "and a.acno_flag='1'"; //只計算一般卡
        sqlCmd += "and a.acct_status='4' and c.acct_jrnl_bal > 0 ";
        setString(1, hCcdtIdPSeqno);
        
        extendField = "acct_status_flag.";
        
        selectTable();
        hCnt = getValueInt("acct_status_flag.hcnt");
        if (hCnt > 0) {
        	hCcdtAcctStatusFlag = "Y";
        } else 
        	hCcdtAcctStatusFlag = "N";
        
    }
    
    // ************************************************************************
    private void updateColLiacDebtDtl0() throws Exception {
        dateTime();
        updateSQL = "unbill_tot_amt       = 0, billed_tot_amt       = 0, unbill_capital_amt   = 0, "
                + "billed_capital_amt   = 0, unbill_fee_amt       = 0, unbill_it_amt        = 0, "
                + "added_interest_amt   = 0, lastest_pay_amt      = 0, interest_days        = 0, "
                + "billed_fee_amt       = 0, billed_interest_amt  = 0, billed_pn_amt        = 0, "
                + "mod_pgm         = ?, mod_time        = sysdate ";
        daoTable = "col_liac_debt_dtl";
        whereStr = "WHERE liac_seqno = ? ";
        setString(1, javaProgram);
        setString(2, hCcdtLiacSeqno);

        updateTable();

    }

    // ************************************************************************
    private void updateColLiacDebtDtl1() throws Exception {
        dateTime();
        updateSQL = "unbill_tot_amt       = ?, billed_tot_amt       = ?, unbill_capital_amt   = ?, "
                + "billed_capital_amt   = ?, unbill_fee_amt       = ?, unbill_it_amt        = ?, "
                + "added_interest_amt   = ?, lastest_pay_amt      = ?, interest_days        = ?, "
                + "billed_fee_amt       = ?, billed_interest_amt  = ?, billed_pn_amt        = ?, "
                + "mod_pgm         = ?, mod_time        = sysdate ";
        daoTable = "col_liac_debt_dtl";
        whereStr = "WHERE liac_seqno = ? ";
        setDouble(1, hCcddUnbillTotAmt);
        setDouble(2, hCcddBilledTotAmt);
        setDouble(3, hCcddUnbillCapitalAmt);
        setDouble(4, hCcddBilledCapitalAmt);
        setDouble(5, hCcddUnbillFeeAmt + hTempUnbillFeeAmt);
        setDouble(6, hCcddUnbillItAmt);
        setDouble(7, hCcddAddedInterestAmt);
        setDouble(8, hCcddLastestPayAmt);
        setDouble(9, hCcddInterestDays);
        setDouble(10, hCcddBilledFeeAmt);
        setDouble(11, hCcddBilledInterestAmt);
        setDouble(12, hCcddBilledPnAmt);
        setString(13, javaProgram);
        setString(14, hCcdtLiacSeqno);

        updateTable();
    }

    // ************************************************************************
    private int selectCrdRela() throws Exception {
        sqlCmd = "select 1 as hcnt ";
        sqlCmd += "from crd_rela ";
//        sqlCmd += "where rela_type = '1' and rela_id = ? ";
        sqlCmd += "where rela_type = '1' and id_p_seqno = ? ";
        sqlCmd += "and  (rela_name <> '' or rela_id <> '') ";
        sqlCmd += "fetch first 1 row only";
//        setString(1, h_ccdt_id);
        setString(1, hCcdtIdPSeqno);

        selectTable();

        if (notFound.equals("Y"))
            return (0);
        return (1);
    }
    // ************************************************************************
}