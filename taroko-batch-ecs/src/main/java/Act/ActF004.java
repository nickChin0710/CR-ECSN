/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/29  V1.00.01    SUP       error   correction                        *
 *  110/11/05  V1.01.02    JH        --uf_hi_idno()                            *
 *  111/10/12  V1.00.03    Yang Bo   sync code from mega                       *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡人帳務調整(帳外息及溢繳)處理程式*/
public class ActF004 extends AccessDAO {

    private final String PROGNAME = "卡人帳務調整(帳外息及溢繳)處理程式  111/10/12  V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";

    String hAcajPSeqno = "";
    String hAcajAcctType = "";
    String hAcajAcctKey = "";
    String hAcajAdjustType = "";
    String hAcajPostDate = "";
    String hAcajCurrCode = "";
    double hAcajDrAmt = 0;
    double hAcajCrAmt = 0;
    double hAcajDcDrAmt = 0;
    double hAcajDcCrAmt = 0;
    String hAcajCardNo = "";
    String hAcajCashType = "";
    String hAcajTransAcctType = "";
    String hAcajTransAcctKey = "";
    String hAca1TransAcctKey = "";
    String hAcajInterestDate = "";
    String hAcajJobCode = "";
    String hAcajVouchJobCode = "";
    String hAcajRowid = "";
    String hBusiBusinessDate = "";
    String hTempCreateDate = "";
    String hTempCreateTime = "";
    String hAcctCorpPSeqno = "";
    String hAcctStmtCycle = "";
    String hAcctIdPSeqno = "";
    String hAcctAcctHolderId = "";
    String hAcctAcctHolderIdCode = "";
    double hAcctAdiBegBal = 0;
    double hAcctAdiEndBal = 0;
    double hAccxAdiEndBal = 0;
    String hAcctRowid = "";
    double hAcurAcctJrnlBal = 0;
    double hAcurDcAcctJrnlBal = 0;
    double hAcurEndBalOp = 0;
    double hAcurDcEndBalOp = 0;
    double hAcurEndBalLk = 0;
    double hAcurDcEndBalLk = 0;
    double hAcurPayAmt = 0;
    double hAcurDcPayAmt = 0;
    int hAcurPayCnt = 0;
    String hAcurOverpayLockStaDate = "";
    String hAcurOverpayLockDueDate = "";
    String hAcurRowid = "";
    double hAcctAcctJrnlBal = 0;
    double hAcctTtlAmtBal = 0;
    double hAcctEndBalOp = 0;
    double hAcctEndBalLk = 0;
    double hAcctPayAmt = 0;
    double hAcctAdiDAvail = 0;
    int hAcctPayCnt = 0;
    String hAcctOverpayLockStaDate = "";
    String hAcctOverpayLockDueDate = "";
    String hAcctModUser = "";
    String hApdlBatchNo = "";
    String hApdlSerialNo = "";
    String hInt = "";
    int enqNo = 0;
    String hJrnlAcctCode = "";
    String hJrnlJrnlSeqno = "";
    String hIdnoChiName = "";
    int hCount = 0;
    String hAdclSerialNo = "";
    String seqno = "";

    int totalCnt = 0;
    int debtInt = 0;
    int nSerialNo = 0;
    int nSerialNo2 = 0;
    double hAdiVouchAmt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : ActF004 , this program need only one parameter", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            /* get common information */
            hModUser = comc.commGetUserID();
            hAcctModUser = hModUser;

            selectPtrBusinday();

            selectActAcaj();

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
        hBusiBusinessDate = "";
        hTempCreateDate = "";
        hTempCreateTime = "";
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
        sqlCmd += " acct_type,";
        sqlCmd += " UF_ACNO_KEY(p_seqno) acct_key,"; // acct_key
        sqlCmd += " adjust_type,";
        sqlCmd += " post_date,";
        sqlCmd += " decode(curr_code,'','901',curr_code) h_acaj_curr_code,";
        sqlCmd += " dr_amt,";
        sqlCmd += " cr_amt,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',dr_amt,dc_dr_amt) h_acaj_dc_dr_amt,";
        sqlCmd += " decode(decode(curr_code,'','901',curr_code),'901',cr_amt,dc_cr_amt) h_acaj_dc_cr_amt,";
        sqlCmd += " card_no,";
        sqlCmd += " cash_type,";
        sqlCmd += " trans_acct_type,";
        sqlCmd += " trans_acct_key,";
//        sqlCmd += " uf_hi_idno(trans_acct_key) h_aca1_trans_acct_key,";
        sqlCmd += " interest_date,";
        sqlCmd += " job_code,";
        sqlCmd += " decode(vouch_job_code,'','00',vouch_job_code) h_acaj_vouch_job_code,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acaj ";
        sqlCmd += " where adjust_type in ('OP01','OP02','OP03','OP04','AI01') ";
        sqlCmd += "   and decode(process_flag,'','N',process_flag) <> 'Y' ";
        sqlCmd += "   and apr_flag ='Y' ";
        sqlCmd += " order by curr_code,adjust_type, cash_type, vouch_job_code, p_seqno, crt_time";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcajPSeqno = getValue("p_seqno");
            hAcajAcctType = getValue("acct_type");
            hAcajAcctKey = getValue("acct_key");
            hAcajAdjustType = getValue("adjust_type");
            hAcajPostDate = getValue("post_date");
            hAcajCurrCode = getValue("h_acaj_curr_code");
            hAcajDrAmt = getValueDouble("dr_amt");
            hAcajCrAmt = getValueDouble("cr_amt");
            hAcajDcDrAmt = getValueDouble("h_acaj_dc_dr_amt");
            hAcajDcCrAmt = getValueDouble("h_acaj_dc_cr_amt");
            hAcajCardNo = getValue("card_no");
            hAcajCashType = getValue("cash_type");
            hAcajTransAcctType = getValue("trans_acct_type");
            hAcajTransAcctKey = getValue("trans_acct_key");
//            h_aca1_trans_acct_key = getValue("h_aca1_trans_acct_key");
            hAcajInterestDate = getValue("interest_date");
            hAcajJobCode = getValue("job_code");
            hAcajVouchJobCode = getValue("h_acaj_vouch_job_code");
            hAcajRowid = getValue("rowid");

            totalCnt++;
            if ((hAcajCrAmt != 0) && (hAcajDrAmt != 0)) {
                showLogMessage("I", "", String.format("* The adjust cr & dr amt both in acaj error!"));
                showLogMessage("I", "", String.format("acct_type[%s] acct_key[%s] adjust_type[%s] ", hAcajAcctType,
                        hAcajAcctKey, hAcajAdjustType));
                insertActAcajErr();
                updateActAcaj();
                continue;
            }

            hJrnlJrnlSeqno = String.format("%012.0f", getJRNLSeq());
            hAdiVouchAmt = 0;
            if (enqNo > 99900)
                enqNo = 0;

            selectActAcct();
            selectActAcctCurr();
            selectCrdIdno();
            /******************************************************************/
            hJrnlAcctCode = "OP";
            /******************************************************************/
            if ((hAcajAdjustType.equals("OP01")) || (hAcajAdjustType.equals("OP04"))) {
                if (adjustOverpayCase1() != 0) {
                    insertActAcajErr();
                    updateActAcaj();
                    continue;
                }

                insertActJrnl(1);
                insertCycPyaj(1);
                insertActJ001R2(1);
            }
            /******************************************************************/
            if ((hAcajAdjustType.equals("OP02")) || (hAcajAdjustType.equals("OP03"))) {
                if (adjustOverpayCase2() != 0) {
                    insertActAcajErr();
                    updateActAcaj();
                    continue;
                }

                insertActJrnl(2);
                insertCycPyaj(1);
                insertActJ001R2(2);
                if (hAcajAdjustType.equals("OP03"))
                    insertActDebtCancel();
            }
            /******************************************************************/
            if (hAcajCurrCode.equals("901"))
                if (hAcajAdjustType.equals("AI01")) {
                    if (adjustAdiCase() != 0) {
                        insertActAcajErr();
                        updateActAcaj();
                        continue;
                    }

                    hJrnlAcctCode = "AI";
                    insertActJrnl(2);
                    insertCycPyaj(2);
                }
            /******************************************************************/
            updateActAcctCurr();
            updateActAcct();
            insertActVouchData(1);

            updateActAcaj();
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctCorpPSeqno = "";
        hAcctStmtCycle = "";
        hAcctIdPSeqno = "";
        hAcctAcctHolderId = "";
        hAcctAcctHolderIdCode = "";
        hAcctAdiBegBal = 0;
        hAcctAdiEndBal = 0;
        hAccxAdiEndBal = 0;
        hAcctAdiDAvail = 0;
        hAcctRowid = "";

        sqlCmd = "select a.corp_p_seqno,";
        sqlCmd += " a.stmt_cycle,";
        sqlCmd += " a.id_p_seqno,";
         sqlCmd += " b.id_no,";
         sqlCmd += " b.id_no_code,";
        sqlCmd += " floor(a.adi_beg_bal) h_acct_adi_beg_bal,";
        sqlCmd += " floor(a.adi_end_bal) h_acct_adi_end_bal,";
        sqlCmd += " floor(a.adi_end_bal) h_accx_adi_end_bal,";
        sqlCmd += " floor(a.adi_d_avail) h_acct_adi_d_avail,";
        sqlCmd += " a.rowid rowid ";
        sqlCmd += "  from act_acct a ";
        sqlCmd += " left join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += " where a.p_seqno = ? ";
        setString(1, hAcajPSeqno);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct not found!", "", hCallBatchSeqno);
        }
        hAcctCorpPSeqno = getValue("corp_p_seqno");
        hAcctStmtCycle = getValue("stmt_cycle");
        hAcctIdPSeqno = getValue("id_p_seqno");
        hAcctAcctHolderId = getValue("id_no");
        hAcctAcctHolderIdCode = getValue("id_no_code");
        hAcctAdiBegBal = getValueDouble("h_acct_adi_beg_bal");
        hAcctAdiEndBal = getValueDouble("h_acct_adi_end_bal");
        hAccxAdiEndBal = getValueDouble("h_accx_adi_end_bal");
        hAcctAdiDAvail = getValueDouble("h_acct_adi_d_avail");
        hAcctRowid = getValue("rowid");
    }

    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        hAcurAcctJrnlBal = 0;
        hAcurDcAcctJrnlBal = 0;
        hAcurEndBalOp = 0;
        hAcurDcEndBalOp = 0;
        hAcurEndBalLk = 0;
        hAcurDcEndBalLk = 0;
        hAcurPayAmt = 0;
        hAcurDcPayAmt = 0;
        hAcurPayCnt = 0;
        hAcurOverpayLockStaDate = "";
        hAcurOverpayLockDueDate = "";
        hAcurRowid = "";

        sqlCmd = "select acct_jrnl_bal,";
        sqlCmd += " dc_acct_jrnl_bal,";
        sqlCmd += " end_bal_op,";
        sqlCmd += " dc_end_bal_op,";
        sqlCmd += " end_bal_lk,";
        sqlCmd += " dc_end_bal_lk,";
        sqlCmd += " pay_amt,";
        sqlCmd += " dc_pay_amt,";
        sqlCmd += " pay_cnt,";
        sqlCmd += " overpay_lock_sta_date,";
        sqlCmd += " overpay_lock_due_date,";
        sqlCmd += " rowid rowid ";
        sqlCmd += "  from act_acct_curr ";
        sqlCmd += " where p_seqno   = ? ";
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
            hAcurEndBalOp = getValueDouble("end_bal_op");
            hAcurDcEndBalOp = getValueDouble("dc_end_bal_op");
            hAcurEndBalLk = getValueDouble("end_bal_lk");
            hAcurDcEndBalLk = getValueDouble("dc_end_bal_lk");
            hAcurPayAmt = getValueDouble("pay_amt");
            hAcurDcPayAmt = getValueDouble("dc_pay_amt");
            hAcurPayCnt = getValueInt("pay_cnt");
            hAcurOverpayLockStaDate = getValue("overpay_lock_sta_date");
            hAcurOverpayLockDueDate = getValue("overpay_lock_due_date");
            hAcurRowid = getValue("rowid");
        }
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoChiName = "";
        if (hAcctIdPSeqno.length() == 0) {
            selectCrdCorp();
            return;
        }

        sqlCmd = "select chi_name ";
        sqlCmd += "  from crd_idno  ";
        sqlCmd += " where id_p_seqno = ? ";
        setString(1, hAcctIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("chi_name");
        }

    }

    /***********************************************************************/
    void selectCrdCorp() throws Exception {
        hIdnoChiName = "";
        sqlCmd = "select substrb(chi_name,1,20) h_idno_chi_name ";
        sqlCmd += "  from crd_corp  ";
        sqlCmd += " where corp_p_seqno = ? ";
        setString(1, hAcctCorpPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("h_idno_chi_name");
        }
    }

    /***********************************************************************/
    int adjustOverpayCase1() throws Exception {
        if (hAcajDrAmt != 0) {
            hAcurEndBalOp = hAcurEndBalOp - hAcajDrAmt;
            hAcurEndBalLk = hAcurEndBalLk + hAcajDrAmt;
            hAcurDcEndBalOp = hAcurDcEndBalOp - hAcajDcDrAmt;
            hAcurDcEndBalLk = hAcurDcEndBalLk + hAcajDcDrAmt;
        } else {
            hAcurEndBalOp = hAcurEndBalOp + hAcajCrAmt;
            hAcurEndBalLk = hAcurEndBalLk - hAcajCrAmt;
            hAcurDcEndBalOp = hAcurDcEndBalOp + hAcajDcCrAmt;
            hAcurDcEndBalLk = hAcurDcEndBalLk - hAcajDcCrAmt;
        }

        if ((hAcurEndBalOp < 0) || (hAcurEndBalLk < 0)) {
            showLogMessage("I", "", String.format("*** The  overpay lock amt < 0, error"));
            showLogMessage("I", "", String.format("acct_type[%s] acct_key[%s] adjust_type[%s] ", hAcajAcctType,
                    hAcajAcctKey, hAcajAdjustType));
            return (1);
        }

        hAcurOverpayLockStaDate = "";
        hAcurOverpayLockDueDate = "";
        if ((hAcajAdjustType.equals("OP01")) && (hAcurEndBalLk != 0)) {
            hAcurOverpayLockStaDate = hAcajPostDate;
            hAcurOverpayLockDueDate = hAcajInterestDate;
        }

        return (0);
    }

    /***********************************************************************/
    int adjustOverpayCase2() throws Exception {
        if (hAcajDrAmt != 0) {
            hAcurEndBalOp = hAcurEndBalOp - hAcajDrAmt;
            hAcurDcEndBalOp = hAcurDcEndBalOp - hAcajDcDrAmt;
            hAcurAcctJrnlBal = hAcurAcctJrnlBal + hAcajDrAmt;
            hAcurDcAcctJrnlBal = hAcurDcAcctJrnlBal + hAcajDcDrAmt;
        } else {
            hAcurEndBalOp = hAcurEndBalOp + hAcajCrAmt;
            hAcurDcEndBalOp = hAcurDcEndBalOp + hAcajDcCrAmt;
            hAcurAcctJrnlBal = hAcurAcctJrnlBal - hAcajCrAmt;
            hAcurDcAcctJrnlBal = hAcurDcAcctJrnlBal - hAcajDcCrAmt;
        }

        if ((hAcurEndBalOp < 0)) {
            showLogMessage("I", "", String.format("*** The opverpay amt < 0, error"));
            showLogMessage("I", "", String.format("acct_type[%s] acct_key[%s] adjust_type[%s] ", hAcajAcctType,
                    hAcajAcctKey, hAcajAdjustType));
            return (1);
        }

        hAcurPayCnt--;
        if (hAcurPayCnt < 0)
            hAcurPayCnt = 0;

        hAcurPayAmt -= hAcajDrAmt;
        if (hAcurPayAmt < 0)
            hAcurPayAmt = 0;
        hAcurDcPayAmt -= hAcajDcDrAmt;
        if (hAcurDcPayAmt < 0)
            hAcurDcPayAmt = 0;

        return (0);
    }

    /***********************************************************************/
    void insertActJ001R2(int hInt) throws Exception {
        daoTable = "act_j001r2";
        extendField = "actj001r2.";
        setValue(extendField + "print_date", hTempCreateDate);
        setValue(extendField + "crt_time", hTempCreateTime);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "acct_type", hAcajAcctType);
      //setValue(extendField + "acct_key", h_acaj_acct_key);
        setValue(extendField + "chi_name", hIdnoChiName);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValueDouble(extendField + "end_bal_op", hAcurDcEndBalOp);
        setValueDouble(extendField + "end_bal_lk", hAcurDcEndBalLk);
        setValueDouble(extendField + "lock_amt", hAcajDrAmt == 0 ? 0 : hAcajDcDrAmt);
        setValueDouble(extendField + "unlock_amt", hAcajDrAmt == 0 ? hAcajDcCrAmt : 0);
        setValue(extendField + "lock_flag", hInt == 1 ? "1" : "3");
        setValue(extendField + "overpay_lock_sta_date", hAcurOverpayLockStaDate);
        setValue(extendField + "overpay_lock_due_date", hAcurOverpayLockDueDate);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_j001r2 duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActDebtCancel() throws Exception {
        hApdlBatchNo = String.format("%s90060001", hBusiBusinessDate);
        hApdlSerialNo = String.format("%05d", nSerialNo++);
        sqlCmd = "insert into act_debt_cancel (";
        sqlCmd += "batch_no,";
        sqlCmd += "serial_no,";
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_p_seqno,";
      //sqlCmd += "id,";
      //sqlCmd += "id_code,";
        sqlCmd += "curr_code,";
        sqlCmd += "pay_amt,";
        sqlCmd += "dc_pay_amt,";
        sqlCmd += "pay_date,";
        sqlCmd += "payment_type,";
        sqlCmd += "update_user,";
        sqlCmd += "update_date,";
        sqlCmd += "update_time,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_user,";
        sqlCmd += "mod_pgm";
        //////////// SELECT////////////
        sqlCmd += " )select ";
        sqlCmd += "?,";
        sqlCmd += "?,";
      //sqlCmd += "acct_p_seqno,";  modified on 2019/06/12
        sqlCmd += "p_seqno,";
        sqlCmd += "?,";
        sqlCmd += "id_p_seqno,";
      //sqlCmd += "acct_holder_id,";
      //sqlCmd += "acct_holder_id_code,";
      //sqlCmd += "?,"; id
      //sqlCmd += "?,"; id_code
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "'MIST',";
        sqlCmd += "?,";
        sqlCmd += "to_char(sysdate,'yyyymmdd'),";
        sqlCmd += "to_char(sysdate,'hh24miss'),";
        sqlCmd += "sysdate,";
        sqlCmd += "?,";
        sqlCmd += "?";
        sqlCmd += "  from act_acno where acct_type =  ?  and acct_key =  ?  ";
        setString(1, hApdlBatchNo);
        setString(2, hApdlSerialNo);
        setString(3, hAcajTransAcctType);
        setString(4, hAcajCurrCode);
        setDouble(5, hAcajDrAmt);
        setDouble(6, hAcajDcDrAmt);
        setString(7, hAcajPostDate);
        setString(8, hAcctModUser);
        setString(9, hAcctModUser);
        setString(10, javaProgram);
        setString(11, hAcajTransAcctType);
        setString(12, hAcajTransAcctKey);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int adjustAdiCase() throws Exception {
        if (hAcajDrAmt != 0) {
            hAcctAdiEndBal = hAcctAdiEndBal - hAcajDrAmt;
            hAcctAdiDAvail = hAcctAdiDAvail - hAcajDrAmt;
            hAcurAcctJrnlBal -= hAcajDrAmt;
            hAcurDcAcctJrnlBal -= hAcajDcDrAmt;
        } else {
            hAcctAdiEndBal = hAcctAdiEndBal + hAcajCrAmt;
            hAcctAdiDAvail = hAcctAdiDAvail + hAcajCrAmt;
            hAcurAcctJrnlBal += hAcajCrAmt;
            hAcurDcAcctJrnlBal += hAcajDcCrAmt;
        }

        if ((hAcctAdiDAvail < 0) || (hAcctAdiDAvail > hAcctAdiBegBal)) {
            showLogMessage("I", "", String.format("*** After adjust, the d_avail < 0 or > beg amt, error"));
            showLogMessage("I", "", String.format("acct_type[%s] acct_key[%s] adjust_type[%s] ", hAcajAcctType,
                    hAcajAcctKey, hAcajAdjustType));
            return (1);
        }

        if (hAcctAdiEndBal < 0) {
            hAdiVouchAmt = (-hAcctAdiEndBal);
            hAcurEndBalOp = hAcurEndBalOp + hAdiVouchAmt;
            hAcurDcEndBalOp = hAcurDcEndBalOp + hAdiVouchAmt;
            hAcctAdiEndBal = 0;
            insertActDebtCancel1();
        }
        return (0);
    }

    /***********************************************************************/
    void insertActDebtCancel1() throws Exception {
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

        hAdclSerialNo = String.format("%05d", nSerialNo2++);
        daoTable = "act_debt_cancel";
        extendField = "actdebtcancel.";
        setValue(extendField + "batch_no", hBusiBusinessDate + "99990004");
        setValue(extendField + "serial_no", hAdclSerialNo);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "id_p_seqno", hAcctIdPSeqno);
      //setValue(extendField + "id", h_acct_acct_holder_id);
      //setValue(extendField + "id_code", h_acct_acct_holder_id_code);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValueDouble(extendField + "pay_amt", 0);
        setValueDouble(extendField + "dc_pay_amt", 0);
        setValue(extendField + "pay_date", "19900101");
        setValue(extendField + "payment_type", "DUMY");
        setValue(extendField + "update_user", hAcctModUser);
        setValue(extendField + "update_date", sysDate);
        setValue(extendField + "update_time", sysTime);
        setValue(extendField + "mod_user", hAcctModUser);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_debt_cancel duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActAcajErr() throws Exception {
        int hInt = 1;
        if (hAcajAdjustType.equals("AI01"))
            hInt = 2;
        daoTable = "act_acaj_err";
        extendField = "actacajerr.";
        setValue(extendField + "print_date", hBusiBusinessDate);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "adjust_type", hAcajAdjustType);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValueDouble(extendField + "beg_bal", hInt == 1 ? hAcurEndBalOp : hAcctAdiBegBal);
        setValueDouble(extendField + "end_bal", hInt == 1 ? hAcurEndBalLk : hAcctAdiEndBal);
        setValueDouble(extendField + "d_avail_bal", hInt == 1 ? 0 : hAcctAdiDAvail); // d_available_bal
        setValueDouble(extendField + "tx_amt", hAcajDrAmt == 0 ? hAcajCrAmt : hAcajDrAmt);
        setValueDouble(extendField + "dc_beg_bal", hInt == 1 ? hAcurDcEndBalOp : hAcctAdiBegBal);
        setValueDouble(extendField + "dc_end_bal", hInt == 1 ? hAcurDcEndBalLk : hAcctAdiEndBal);
        setValueDouble(extendField + "dc_d_avail_bal", hInt == 1 ? 0 : hAcctAdiDAvail); // dc_d_available_bal
        setValueDouble(extendField + "dc_tx_amt", hAcajDrAmt == 0 ? hAcajDcCrAmt : hAcajDcDrAmt);
        setValue(extendField + "error_reason", "04");
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acaj_err duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertActJrnl(int hInt) throws Exception {
        enqNo++;
        daoTable = "act_jrnl";
        extendField = "actjrnl.";
        setValue(extendField + "crt_date", hTempCreateDate);
        setValue(extendField + "crt_time", hTempCreateTime);
        setValueInt(extendField + "enq_seqno", enqNo);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "corp_p_seqno", hAcctCorpPSeqno);
        setValue(extendField + "id_p_seqno", hAcctIdPSeqno);
        setValue(extendField + "acct_date", hBusiBusinessDate);
        setValue(extendField + "tran_class", "A");
        setValue(extendField + "tran_type", hAcajAdjustType);
        setValue(extendField + "acct_code", hJrnlAcctCode);
        setValue(extendField + "dr_cr", hAcajDrAmt == 0 ? "C" : "D");
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValueDouble(extendField + "transaction_amt", hAcajDrAmt == 0 ? hAcajCrAmt : hAcajDrAmt);
        setValueDouble(extendField + "dc_transaction_amt", hAcajDrAmt == 0 ? hAcajDcCrAmt : hAcajDcDrAmt);
        setValueDouble(extendField + "jrnl_bal", hAcurAcctJrnlBal);
        setValueDouble(extendField + "dc_jrnl_bal", hAcurDcAcctJrnlBal);
        setValueDouble(extendField + "item_bal", hInt == 1 ? hAcurEndBalOp : hAcctAdiEndBal);
        setValueDouble(extendField + "dc_item_bal", hInt == 1 ? hAcurDcEndBalOp : hAcctAdiEndBal);
        setValueDouble(extendField + "item_d_bal", hInt == 1 ? hAcurEndBalLk : hAcctAdiDAvail);
        setValueDouble(extendField + "dc_item_d_bal", hInt == 1 ? hAcurDcEndBalLk : hAcctAdiDAvail);
        setValue(extendField + "item_date", hAcajPostDate);
        setValue(extendField + "interest_date", hInt == 1 ? hAcajInterestDate : "");
        setValue(extendField + "pay_id", hAcajCardNo);
        setValue(extendField + "stmt_cycle", hAcctStmtCycle);
        setValue(extendField + "cash_type", hInt == 1 ? "" : hAcajCashType);
        setValue(extendField + "trans_acct_type", hInt == 1 ? "" : hAcajTransAcctType);
        setValue(extendField + "trans_acct_key", hInt == 1 ? "" : hAcajTransAcctKey);
        setValue(extendField + "jrnl_seqno", hJrnlJrnlSeqno);
        setValue(extendField + "mod_user", hAcctModUser);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_jrnl duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertCycPyaj(int hInt) throws Exception {
        daoTable = "cyc_pyaj";
        extendField = "cycpyaj.";
        setValue(extendField + "P_SEQNO", hAcajPSeqno); // p_seq
        setValue(extendField + "acct_type", hAcajAcctType);
        setValue(extendField + "class_code", hInt == 1 ? "P" : "B");
        setValue(extendField + "payment_date", hBusiBusinessDate);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValueDouble(extendField + "PAYMENT_AMT", (hAcajDrAmt == 0 ? hAcajCrAmt : hAcajDrAmt) * -1); // payment_amount
        setValueDouble(extendField + "DC_PAYMENT_AMT", (hAcajDrAmt == 0 ? hAcajDcCrAmt : hAcajDcDrAmt) * -1); // dc_payment_amount
        setValue(extendField + "payment_type", hAcajAdjustType);
        setValue(extendField + "stmt_cycle", hAcctStmtCycle);
        setValue(extendField + "SETTLE_FLAG", "U"); // settlement_flag
        setValue(extendField + "mod_pgm", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_cyc_pyaj duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        daoTable = "act_acct_curr";
        //updateSQL = "ttl_amt_bal            = decode(cast(? as varchar(8)), 'OP02',?, 'OP03',?, ttl_amt_bal   ),";
        //updateSQL += " dc_ttl_amt_bal        = decode(cast(? as varchar(8)), 'OP02',?, 'OP03',?, dc_ttl_amt_bal),";
        updateSQL = " acct_jrnl_bal         = ?,";
        updateSQL += " dc_acct_jrnl_bal      = ?,";
        updateSQL += " end_bal_op            = ?,";
        updateSQL += " dc_end_bal_op         = ?,";
        updateSQL += " end_bal_lk            = ?,";
        updateSQL += " dc_end_bal_lk         = ?,";
        updateSQL += " pay_amt               = ?,";
        updateSQL += " dc_pay_amt            = ?,";
        updateSQL += " pay_cnt               = ?,";
        updateSQL += " overpay_lock_sta_date = ?,";
        updateSQL += " overpay_lock_due_date = ?,";
        updateSQL += " mod_time              = sysdate,";
        updateSQL += " mod_user              = ?,";
        updateSQL += " mod_pgm               = 'ActF004'";
        whereStr = "where rowid            = ? ";
        int index = 1;
//        setString(1, h_acaj_adjust_type);
//        setDouble(2, h_acur_end_bal_op);
//        setDouble(3, h_acur_end_bal_op);
//        setString(4, h_acaj_adjust_type);
//        setDouble(5, h_acur_dc_end_bal_op);
//        setDouble(6, h_acur_dc_end_bal_op);
        setDouble(index++, hAcurAcctJrnlBal);
        setDouble(index++, hAcurDcAcctJrnlBal);
        setDouble(index++, hAcurEndBalOp);
        setDouble(index++, hAcurDcEndBalOp);
        setDouble(index++, hAcurEndBalLk);
        setDouble(index++, hAcurDcEndBalLk);
        setDouble(index++, hAcurPayAmt);
        setDouble(index++, hAcurDcPayAmt);
        setInt(index++, hAcurPayCnt);
        setString(index++, hAcurOverpayLockStaDate);
        setString(index++, hAcurOverpayLockDueDate);
        setString(index++, hAcctModUser);
        setRowId(index++, hAcurRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateActAcct() throws Exception {
        selectActAcctCurr1();

        daoTable = "act_acct";
        updateSQL = "acct_jrnl_bal          = ?,";
        updateSQL += " ttl_amt_bal           = ?,";
        updateSQL += " end_bal_op            = ?,";
        updateSQL += " end_bal_lk            = ?,";
        updateSQL += " pay_amt               = ?,";
        updateSQL += " pay_cnt               = ?,";
        updateSQL += " adi_beg_bal           = ?,";
        updateSQL += " adi_end_bal           = ?,";
        updateSQL += " adi_d_avail           = ?,";
        updateSQL += " overpay_lock_sta_date = ?,";
        updateSQL += " overpay_lock_due_date = ?,";
        updateSQL += " mod_time              = sysdate,";
        updateSQL += " mod_user              = ?,";
        updateSQL += " mod_pgm               = 'ActF004'";
        whereStr = "where rowid            = ? ";
        setDouble(1, hAcctAcctJrnlBal);
        setDouble(2, hAcctTtlAmtBal);
        setDouble(3, hAcctEndBalOp);
        setDouble(4, hAcctEndBalLk);
        setDouble(5, hAcctPayAmt);
        setInt(6, hAcctPayCnt);
        setDouble(7, hAcctAdiBegBal);
        setDouble(8, hAcctAdiEndBal);
        setDouble(9, hAcctAdiDAvail);
        setString(10, hAcctOverpayLockStaDate);
        setString(11, hAcctOverpayLockDueDate);
        setString(12, hAcctModUser);
        setRowId(13, hAcctRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectActAcctCurr1() throws Exception {
        sqlCmd = "select sum(acct_jrnl_bal)         h_acct_acct_jrnl_bal,";
        sqlCmd += " sum(ttl_amt_bal)           h_acct_ttl_amt_bal,";
        sqlCmd += " sum(end_bal_op)            h_acct_end_bal_op,";
        sqlCmd += " sum(end_bal_lk)            h_acct_end_bal_lk,";
        sqlCmd += " sum(pay_amt)               h_acct_pay_amt,";
        sqlCmd += " sum(pay_cnt)               h_acct_pay_cnt,";
        sqlCmd += " min(overpay_lock_sta_date) h_acct_overpay_lock_sta_date,";
        sqlCmd += " min(overpay_lock_due_date) h_acct_overpay_lock_due_date ";
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
            hAcctEndBalOp = getValueDouble("h_acct_end_bal_op");
            hAcctEndBalLk = getValueDouble("h_acct_end_bal_lk");
            hAcctPayAmt = getValueDouble("h_acct_pay_amt");
            hAcctPayCnt = getValueInt("h_acct_pay_cnt");
            hAcctOverpayLockStaDate = getValue("h_acct_overpay_lock_sta_date");
            hAcctOverpayLockDueDate = getValue("h_acct_overpay_lock_due_date");
        }
    }

    /***********************************************************************/
    void insertActVouchData(int hInt) throws Exception {
        double lmDVouchAmt = 0;
        if (hAcajAdjustType.equals("AI01")) {
          if (hAccxAdiEndBal > hAcajDrAmt) {
             lmDVouchAmt = hAcajDrAmt;
          }
          else {
             lmDVouchAmt = hAccxAdiEndBal;
          }
        } 
        else {
          if (hAcajCurrCode.equals("901")) {
             lmDVouchAmt = hAcajDrAmt;
          }
          else {
             lmDVouchAmt = hAcajDcDrAmt;
          }
        } 
        
        daoTable = "act_vouch_data";
        extendField = "actvouchd.";
        setValue(extendField + "crt_date", hTempCreateDate);
        setValue(extendField + "crt_time", hTempCreateTime);
        setValue(extendField + "business_date", hBusiBusinessDate);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "acct_type", hAcajAcctType);
        setValueDouble(extendField + "o_vouch_amt", hAcajCurrCode.equals("901") ? hAcajDrAmt : hAcajDcDrAmt);
        setValueDouble(extendField + "vouch_amt", hAcajCurrCode.equals("901") ? hAcajDrAmt : hAcajDcDrAmt);
      //setValueDouble(extendField + "d_vouch_amt", h_acaj_curr_code.equals("AI01") ? h_acaj_dr_amt : h_acaj_dc_dr_amt);
      //setValueDouble(extendField + "d_vouch_amt", h_acaj_curr_code.equals("901") ? h_acaj_dr_amt : h_acaj_dc_dr_amt);
        setValueDouble(extendField + "d_vouch_amt", lmDVouchAmt);
        setValue(extendField + "vouch_data_type", hInt + "");
        setValue(extendField + "acct_code",
                hAcajAdjustType.equals("AI01") ? "AI" : hAcajTransAcctType); /* trans_acct_type */
        setValue(extendField + "pay_card_no",
                hAcajAdjustType.equals("AI01") ? hAcajCardNo : hAcajTransAcctKey); /* tran_acct_key */
        setValue(extendField + "payment_type", hAcajAdjustType);
        setValue(extendField + "proc_stage",
                hAcajAdjustType.equals("OP02") ? hAcajCashType : "X"); /* cash_type */
        setValueDouble(extendField + "pay_amt", hAcajDrAmt);
        setValue(extendField + "job_code", hAcajVouchJobCode); /* vouch_job_code */
        setValue(extendField + "mcht_no", hAcajJobCode); /* job_code */ // merchant_no
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
    void updateActAcaj() throws Exception {
        daoTable = "act_acaj";
        updateSQL = "process_flag  = 'Y',";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_user     = ?,";
        updateSQL += " mod_pgm      = 'ActF004'";
        whereStr = "where rowid   = ? ";
        setString(1, hAcctModUser);
        setRowId(2, hAcajRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acaj not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActF004 proc = new ActF004();
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
