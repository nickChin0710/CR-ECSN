/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/02  V1.00.00    phopho     program initial                          *
*  109/12/09  V1.00.01    shiyuqi       updated for project coding standard   *
*  112/09/05  V1.00.02    sunny      調整只抓一般卡的帳務                                                    *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA424 extends AccessDAO {
    private String progname = "前置協商協商狀態統計處理程式  112/09/05  V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";

    String hClnoId = "";
    String hClnoLiacSeqno = "";
    String hClnoLiacStatus = "";
    String hClnoIdPSeqno = "";
    String hClnoBankCode = "";
    String hClnoRegBankNo = "";
    String hClnoEndReason = "";
    String hClnoContractDate = "";
    String hCcddAcctStatus = "";
    double hCcddTotAmt = 0;
    double hAcnoTotAmtRunbatch = 0;
    String hClctInstallSDate = "";
    double hClctPerAllocateAmt = 0;
    int hTempCnt = 0;
    int totalCnt = 0;

    String hAcnoPSeqno = "";
    String hWdayThisAcctMonth = "";
    double hCcddUnbillTotAmt = 0;
    double hCcddBilledTotAmt = 0;
    double hCcddUnbillItAmt = 0;
    double hAcmlCashIseBalance = 0;
    double hTempUnbillFeeAmt = 0;

    public int mainProcess(String[] args) {
        try {
        	dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
        	
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ColA424 [business_date] [start_month]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            if ((args.length == 1) && (args[0].length() == 8))
                hBusiBusinessDate = args[0];
            selectPtrBusinday();

            deleteColLiacStatBystatus();

            totalCnt = 0;
            selectColLiacNego();

            // ==============================================
            // 固定要做的
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
    	sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
    }

    /***********************************************************************/
    void deleteColLiacStatBystatus() throws Exception {
        daoTable = "col_liac_stat_bystatus";
        whereStr = "where static_date = to_char(sysdate,'yyyymmdd') ";
        
        deleteTable();
    }

    /***********************************************************************/
    void selectColLiacNego() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "id_no,";
        sqlCmd += "liac_seqno,";
        sqlCmd += "liac_status, ";
        sqlCmd += "bank_code,";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "end_reason,";
        sqlCmd += "contract_date ";
        sqlCmd += "from col_liac_nego ";
        sqlCmd += "where credit_card_flag ='Y' ";

        openCursor();
        while (fetchTable()) {
            hClnoId = getValue("id_no");
            hClnoLiacSeqno = getValue("liac_seqno");
            hClnoLiacStatus = getValue("liac_status");
            hClnoIdPSeqno = getValue("id_p_seqno");
            hClnoBankCode = getValue("bank_code");
            hClnoRegBankNo = getValue("reg_bank_no");
            hClnoEndReason = getValue("end_reason");
            hClnoContractDate = getValue("contract_date");

            selectColLiacDebtDtl();
            totalCnt++;
            if ((totalCnt % 1000) == 0)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectColLiacDebtDtl() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "max(decode(acct_status,'4','4','1')) h_ccdd_acct_status,";
        sqlCmd += "sum(decode(sign(tot_amt),-1,0,tot_amt)) h_ccdd_tot_amt,";
        sqlCmd += "count(*) h_temp_cnt ";
        sqlCmd += "from col_liac_debt_dtl ";
        sqlCmd += "where liac_seqno = ? ";
        setString(1, hClnoLiacSeqno);
        
        extendField = "col_liac_debt_dtl.";

        int recordCnt = selectTable();
        for(int i = 0; i < recordCnt ; i++) {
            hCcddAcctStatus = getValue("col_liac_debt_dtl.h_ccdd_acct_status",i);
            hCcddTotAmt = getValueDouble("col_liac_debt_dtl.h_ccdd_tot_amt",i);
            hTempCnt = getValueInt("col_liac_debt_dtl.h_temp_cnt",i);

            if (hTempCnt == 0)
                continue;
            if (hCcddTotAmt == 0)
                continue;

            selectColLiacContract();
            selectActAcno1(); // 參考【col_a415 前置協商回報債權資料處理程式】。
                                 // insert_col_liac_debt_dtl()中，塞入tot_amt 欄位之內容。
            insertColLiacStatBystatus();
        }
    }

    /***********************************************************************/
    void selectColLiacContract() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "install_s_date,";
        sqlCmd += "per_allocate_amt ";
        sqlCmd += "from col_liac_contract ";
        sqlCmd += "where liac_seqno = ? ";
        setString(1, hClnoLiacSeqno);
        
        extendField = "col_liac_contract.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hClctInstallSDate = getValue("col_liac_contract.install_s_date");
            hClctPerAllocateAmt = getValueDouble("col_liac_contract.per_allocate_amt");
        }
    }

    /***********************************************************************/
    void insertColLiacStatBystatus() throws Exception {
    	daoTable = "col_liac_stat_bystatus";
    	extendField = daoTable + ".";
        setValue(extendField+"static_date", sysDate);
        setValue(extendField+"id_p_seqno", hClnoIdPSeqno);
        setValue(extendField+"id_no", hClnoId);
        setValue(extendField+"liac_seqno", hClnoLiacSeqno);
        setValue(extendField+"liac_status", hClnoLiacStatus);
        setValue(extendField+"acct_status", hCcddAcctStatus);
        setValue(extendField+"bank_code", hClnoBankCode);
        setValue(extendField+"reg_bank_no", hClnoRegBankNo);
        setValue(extendField+"end_reason", hClnoEndReason);
        setValue(extendField+"contract_date", hClnoContractDate);
        setValueDouble(extendField+"tot_amt_apply", hCcddTotAmt);
        setValueDouble(extendField+"tot_amt_runbatch", hAcnoTotAmtRunbatch);
        setValue(extendField+"install_s_date", hClctInstallSDate);
        setValueDouble(extendField+"per_allocate_amt", hClctPerAllocateAmt);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_liac_stat_bystatus duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectActAcno1() throws Exception {
        hAcnoTotAmtRunbatch = 0;
        sqlCmd = "select ";
//        sqlCmd += "a.p_seqno, ";
        sqlCmd += "a.acno_p_seqno, ";
        sqlCmd += "b.this_acct_month ";
        sqlCmd += "from act_acno a,ptr_workday b ";
        sqlCmd += "where a.id_p_seqno = ? ";
//        sqlCmd += "and a.p_seqno = a.gp_no ";
//        sqlCmd += "and a.acno_flag <> 'Y' ";
        sqlCmd += "and a.acno_flag = '1' ";   //只抓一般卡的帳務
        sqlCmd += "and a.stmt_cycle = b.stmt_cycle ";
        setString(1, hClnoIdPSeqno);
        
        extendField = "act_acno_1.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
//            h_acno_p_seqno = getValue("p_seqno", i);
            hAcnoPSeqno = getValue("act_acno_1.acno_p_seqno", i);
            hWdayThisAcctMonth = getValue("act_acno_1.this_acct_month", i);

            selectActComboMJrnl();
            selectActDebt();
            selectBilContract();
            hCcddUnbillTotAmt = hCcddUnbillTotAmt + hAcmlCashIseBalance + hTempUnbillFeeAmt;

            // 加總相同 ID_P_SEQNO(代表有多個 P_SEQNO帳戶)的tot_amt值。
            hAcnoTotAmtRunbatch += hCcddUnbillTotAmt + hCcddBilledTotAmt + hCcddUnbillItAmt;
        }
    }

    /***********************************************************************/
    void selectActComboMJrnl() throws Exception {
        hAcmlCashIseBalance = 0;
        sqlCmd = "select sum(nvl(cash_use_balance,0)) cash_use_balance ";
        sqlCmd += "from act_combo_m_jrnl where p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        
        extendField = "act_combo_m_jrnl.";

        if (selectTable() > 0) {
            hAcmlCashIseBalance = getValueDouble("act_combo_m_jrnl.cash_use_balance");
        }
    }

    /***********************************************************************/
    private void selectActDebt() throws Exception {
        hCcddBilledTotAmt = 0;
        hCcddUnbillTotAmt = 0;

        sqlCmd = "select sum(decode(sign(acct_month- ? ),1,0,end_bal)) as billed_tot_amt, "; /* 已posting 欠款總額 */
        sqlCmd += "sum(decode(sign(acct_month- ? ),1,end_bal,0)) as unbill_tot_amt "; /* 未posting 欠款總額 */
        sqlCmd += "from act_debt where p_seqno = ?";
        setString(1, hWdayThisAcctMonth);
        setString(2, hWdayThisAcctMonth);
        setString(3, hAcnoPSeqno);

        extendField = "act_debt.";
        
        if (selectTable() > 0) {
            hCcddBilledTotAmt = getValueDouble("act_debt.billed_tot_amt");
            hCcddUnbillTotAmt = getValueDouble("act_debt.unbill_tot_amt");
        }
    }

    /***********************************************************************/
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
        sqlCmd += "and   decode(refund_apr_flag,'','N',refund_apr_flag) <> 'Y' ";
        sqlCmd += "and   (((decode(auth_code,'','N',auth_code) not in ('N','REJECT','P','reject')";
        sqlCmd += "and    contract_kind = '2' ) or contract_kind = '1')";
        sqlCmd += "and   ( apr_date <> '' or delv_confirm_date <> '' )) ";
        setString(1, hAcnoPSeqno);

        extendField = "bil_contract.";
        
        if (selectTable() > 0) {
            hCcddUnbillItAmt = getValueDouble("bil_contract.unbill_it_amt");
            hTempUnbillFeeAmt = getValueDouble("bil_contract.unbill_fee_amt");
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA424 proc = new ColA424();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
