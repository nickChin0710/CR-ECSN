/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/21  V1.00.00    phopho     program initial                          *
*  108/11/29  V1.00.01    phopho     fix err_rtn bug                          *
*  109/02/21  V1.00.02    Brian      remove select_act_acno join table crd_idno&crd_crop *
* 109/12/12  V1.00.03    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB019 extends AccessDAO {
    private String progname = "催呆帳外息資料處理程式  109/12/12  V1.00.03  ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";

    double hAcctAcctJrnlBal = 0;
    double hAcctAdiBegBal          = 0;
    double hAcctAdiEndBal = 0;
    double hAcctAdiDAvail = 0;
    String hAcctRowid = "";
    String hAcnoPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoIdPSeqno = "";
    String hAcnoNoInterestFlag = "";
    String hAcnoNoInterestSMonth = "";
    String hAcnoNoInterestEMonth = "";
    String hAcnoRevolveIntSign = "";
    double hAcnoRevolveIntRate = 0;
    String hAcnoRevolveRateSMonth = "";
    String hAcnoRevolveRateEMonth = "";
    String hAcnoModUser = "";
    double hDebtEndBal = 0;
    String hPcodAcctCode = "";
    String hPcodInterRateCode = "";
    double hAgenRevolvingInterest1 = 0;
    double hAgenRevolvingInterest2 = 0;
    double hAgenRevolvingInterest3 = 0;
    double hAgenRevolvingInterest4 = 0;
    double hAgenRevolvingInterest5 = 0;
    double hAgenRevolvingInterest6 = 0;
    double hPcglPurchBalWave = 0;
    String hWdayNextAcctMonth = "";

    int    totalCnt      = 0;
    int    insertCnt     = 0;
    double hTempRate[] = new double[2];
    double wsInterestRate;
    double hAdiIntrs;
    long hJrnlIntrs;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 0) {
                comc.errExit("Usage : ColB019 ", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            hAcnoModUser = comc.commGetUserID();

            selectPtrBusinday();
            selectPtrCurrGeneral();
            selectPtrActgeneral();
            selectPtrActcode();

            selectActAcno();

            showLogMessage("I", "", "程式執行結束");
            showLogMessage("I", "", "累計筆數:[" + totalCnt + "] 累計新增:[" + insertCnt + "]");

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
        hBusiBusinessDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectPtrCurrGeneral() throws Exception {
        hPcglPurchBalWave = 0;
        sqlCmd = "select purch_bal_wave ";
        sqlCmd += "from ptr_curr_general ";
        sqlCmd += "where curr_code = '901' ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.hCallErrorDesc = "select_ptr_curr_general 失敗";
            comcr.errRtn("select_ptr_curr_general not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPcglPurchBalWave = getValueDouble("purch_bal_wave");
        }
    }

    /***********************************************************************/
    void selectPtrActgeneral() throws Exception {
        hAgenRevolvingInterest1 = 0;
        hAgenRevolvingInterest2 = 0;
        hAgenRevolvingInterest3 = 0;
        hAgenRevolvingInterest4 = 0;
        hAgenRevolvingInterest5 = 0;
        hAgenRevolvingInterest6 = 0;
        sqlCmd = "select revolving_interest1, ";
        sqlCmd += "revolving_interest2, ";
        sqlCmd += "revolving_interest3, ";
        sqlCmd += "revolving_interest4, ";
        sqlCmd += "revolving_interest5, ";
        sqlCmd += "revolving_interest6 ";
        sqlCmd += "from ptr_actgeneral ";
        sqlCmd += "fetch first 1 row only ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_actgeneral not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAgenRevolvingInterest1 = getValueDouble("revolving_interest1");
            hAgenRevolvingInterest2 = getValueDouble("revolving_interest2");
            hAgenRevolvingInterest3 = getValueDouble("revolving_interest3");
            hAgenRevolvingInterest4 = getValueDouble("revolving_interest4");
            hAgenRevolvingInterest5 = getValueDouble("revolving_interest5");
            hAgenRevolvingInterest6 = getValueDouble("revolving_interest6");
        }
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        long hTemp1 = 0, hTemp2 = 0;
        sqlCmd = "select ";
        sqlCmd += "b.p_seqno, ";
        sqlCmd += "max(a.acct_type) acct_type, ";
        sqlCmd += "max(a.acct_key) acct_key, ";
        sqlCmd += "max(a.stmt_cycle) stmt_cycle, ";
        sqlCmd += "max(a.id_p_seqno) id_p_seqno, ";
        sqlCmd += "max(a.corp_p_seqno) corp_p_seqno, ";
        sqlCmd += "max(f.corp_no) corp_no, ";
        // sqlCmd += "max(a.corp_no_code) corp_no_code, ";
        sqlCmd += "max(a.acct_status) acct_status, ";
        sqlCmd += "max(a.no_interest_flag) no_interest_flag, ";
//        sqlCmd += "max(a.no_interest_s_month) no_interest_s_month, ";  //phopho mod
        sqlCmd += "max(decode(a.no_interest_s_month,'','000000',a.no_interest_s_month)) no_interest_s_month, ";
        sqlCmd += "max(decode(a.no_interest_e_month,'','999912',a.no_interest_e_month)) no_interest_e_month, ";
        sqlCmd += "max(a.revolve_int_sign) revolve_int_sign, ";
        sqlCmd += "max(a.revolve_int_rate) revolve_int_rate, ";
//        sqlCmd += "max(a.revolve_rate_s_month) revolve_rate_s_month, ";  //phopho mod
        sqlCmd += "max(decode(a.revolve_rate_s_month,'','000000',a.revolve_rate_s_month)) revolve_rate_s_month, ";
        sqlCmd += "max(decode(a.revolve_rate_e_month,'','999912',a.revolve_rate_e_month)) revolve_rate_e_month, ";
        sqlCmd += "max(c.next_acct_month) next_acct_month, ";
        sqlCmd += "sum(b.end_bal) end_bal, ";
        sqlCmd += "max(d.adi_end_bal) adi_end_bal, ";
        sqlCmd += "max(d.adi_d_avail) adi_d_avail, ";
        sqlCmd += "max(d.adi_beg_bal) adi_beg_bal, ";
        sqlCmd += "max(d.acct_jrnl_bal) acct_jrnl_bal ";
//        sqlCmd += "max(d.rowid) as rowid ";
        sqlCmd += "FROM    act_debt b,act_acct d,ptr_workday c,act_acno a  ";
        sqlCmd += "  left join crd_corp f on f.corp_p_seqno = a.corp_p_seqno "; //find corp_no in crd_corp
//        sqlCmd += "WHERE   b.p_seqno   = a.p_seqno ";
        sqlCmd += "WHERE   b.p_seqno   = a.acno_p_seqno ";
        sqlCmd += "AND     (b.acct_code = 'CB' ";
        sqlCmd += " or      (b.acct_code = 'DB' ";
        sqlCmd += "  and     b.acct_code_type  = 'B')) ";
//        sqlCmd += "AND     d.acct_type = a.acct_type ";
//        sqlCmd += "AND     d.acct_key = a.acct_key ";
//        sqlCmd += "AND     d.p_seqno = a.p_seqno ";
        sqlCmd += "AND     d.p_seqno = a.acno_p_seqno ";
        sqlCmd += "AND      (decode(a.no_interest_flag,'','N',a.no_interest_flag) = 'N' ";
        sqlCmd += " or      (decode(a.no_interest_flag,'','N',a.no_interest_flag) = 'Y' ";
        sqlCmd += "  and     (c.next_acct_month<decode(a.no_interest_s_month,'','000000',a.no_interest_s_month) ";
        sqlCmd += "   or      c.next_acct_month>decode(a.no_interest_e_month,'','999912',a.no_interest_e_month)))) ";
        sqlCmd += "AND     a.acct_status >= '3' ";
        sqlCmd += "AND     a.stmt_cycle = c.stmt_cycle ";
        sqlCmd += "AND     b.end_bal    > 0 ";
        sqlCmd += "GROUP   BY b.p_seqno ";
        sqlCmd += "HAVING  sum(b.end_bal) >= ? ";
        setDouble(1, hPcglPurchBalWave);

        openCursor();
        while (fetchTable()) {
            hAcnoPSeqno = getValue("p_seqno");
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hAcnoCorpPSeqno = getValue("corp_p_seqno");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoNoInterestFlag = getValue("no_interest_flag");
            hAcnoNoInterestSMonth = getValue("no_interest_s_month");
            hAcnoNoInterestEMonth = getValue("no_interest_e_month");
            hAcnoRevolveIntSign = getValue("revolve_int_sign");
            hAcnoRevolveIntRate = getValueDouble("revolve_int_rate");
            hAcnoRevolveRateSMonth = getValue("revolve_rate_s_month");
            hAcnoRevolveRateEMonth = getValue("revolve_rate_e_month");
            hWdayNextAcctMonth = getValue("next_acct_month");
            hDebtEndBal = getValueDouble("end_bal");
            hAcctAdiEndBal = getValueDouble("adi_end_bal");
            hAcctAdiDAvail = getValueDouble("adi_d_avail");
            hAcctAdiBegBal = getValueDouble("adi_beg_bal");
            hAcctAcctJrnlBal = getValueDouble("acct_jrnl_bal");
//            h_acct_rowid = getValue("rowid");  //DB2 SQL Error: SQLCODE=-119, SQLSTATE=42803, SQLERRMC=ROWID, DRIVER=4.16.53
            //ERROR解不掉, 改以p_seqno 取代 rowid

            totalCnt++;
            if (totalCnt % 20000 == 0) {
                showLogMessage("I", "", "    處理筆數:[" + totalCnt + "] 新增筆數:[" + insertCnt + "]");
            }

            if ((hWdayNextAcctMonth.substring(0, 6).compareTo(hAcnoRevolveRateSMonth.substring(0, 6)) >= 0)
                    && (hWdayNextAcctMonth.substring(0, 6)
                            .compareTo(hAcnoRevolveRateEMonth.substring(0, 6)) <= 0)) {
                if (hAcnoRevolveIntSign.equals("+"))
                    wsInterestRate = hTempRate[comcr.str2int(hAcnoAcctStatus) - 3] + hAcnoRevolveIntRate;
                else
                    wsInterestRate = hTempRate[comcr.str2int(hAcnoAcctStatus) - 3] - hAcnoRevolveIntRate;
            } else {
                wsInterestRate = hTempRate[comcr.str2int(hAcnoAcctStatus) - 3];
            }

            if (wsInterestRate <= 0)
                continue;

            hAdiIntrs = (hDebtEndBal * (wsInterestRate / 10000));

            hTemp1 = (long) (hAdiIntrs * 100);
            hAdiIntrs = (double) hTemp1 / 100;

            hTemp1 = (long) hAcctAdiEndBal;
            hTemp2 = (long) (hAcctAdiEndBal + hAdiIntrs);
            hJrnlIntrs = (hTemp2 - hTemp1);

            insertCnt++;
            updateActAcct();
            updateActAcctCurr();
            insertActJrnl();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectPtrActcode() throws Exception {
        int recordCnt = 0;
        sqlCmd = "select ";
        sqlCmd += "acct_code, ";
        sqlCmd += "inter_rate_code ";
        sqlCmd += "from ptr_actcode ";
        sqlCmd += "where acct_code in ('CB','DB') ";
        sqlCmd += "order by acct_code ";

        openCursor();
        while (fetchTable()) {
            hPcodAcctCode = getValue("acct_code");
            hPcodInterRateCode = getValue("inter_rate_code");

            switch (hPcodInterRateCode) {
            case "1":
                hTempRate[recordCnt] = hAgenRevolvingInterest1;
                break;
            case "2":
                hTempRate[recordCnt] = hAgenRevolvingInterest2;
                break;
            case "3":
                hTempRate[recordCnt] = hAgenRevolvingInterest3;
                break;
            case "4":
                hTempRate[recordCnt] = hAgenRevolvingInterest4;
                break;
            case "5":
                hTempRate[recordCnt] = hAgenRevolvingInterest5;
                break;
            case "6":
                hTempRate[recordCnt] = hAgenRevolvingInterest6;
                break;
            default:
                hTempRate[recordCnt] = 0;
                break;
            }
            recordCnt++;
        }
        closeCursor();
    }

    /***********************************************************************/
    void insertActJrnl() throws Exception {
        dateTime();
        daoTable = "act_jrnl";
        extendField = daoTable + ".";
        setValue(extendField+"crt_date", sysDate);
        setValue(extendField+"crt_time", sysTime);
        setValue(extendField+"p_seqno", hAcnoPSeqno);
        setValue(extendField+"curr_code", "901");
        setValue(extendField+"acct_type", hAcnoAcctType);
        setValue(extendField+"id_p_seqno", hAcnoIdPSeqno);
        setValue(extendField+"corp_p_seqno", hAcnoCorpPSeqno);
        setValue(extendField+"acct_date", hBusiBusinessDate);
        setValue(extendField+"tran_class", "B");
        setValue(extendField+"tran_type", "AI01");
        setValue(extendField+"acct_code", "AI"); //item_ename
        setValue(extendField+"dr_cr", "C");
        setValueDouble(extendField+"transaction_amt", hAdiIntrs);
        setValueDouble(extendField+"jrnl_bal", hAcctAcctJrnlBal + hJrnlIntrs);
        setValueDouble(extendField+"item_bal", hAcctAdiEndBal + hAdiIntrs);
        setValueDouble(extendField+"item_d_bal", hAcctAdiDAvail + hAdiIntrs);
        setValueDouble(extendField+"dc_transaction_amt", hAdiIntrs);
        setValueDouble(extendField+"dc_jrnl_bal", hAcctAcctJrnlBal + hJrnlIntrs);
        setValueDouble(extendField+"dc_item_bal", hAcctAdiEndBal + hAdiIntrs);
        setValueDouble(extendField+"dc_item_d_bal", hAcctAdiDAvail + hAdiIntrs);
        setValue(extendField+"stmt_cycle", hAcnoStmtCycle);
        setValue(extendField+"mod_user", hAcnoModUser);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);
        
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_jrnl duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcct() throws Exception {
        daoTable = "act_acct";
        updateSQL  = " adi_end_bal     =  adi_end_bal + ?,";
        updateSQL += " adi_d_avail     =  adi_d_avail + ?,";
        updateSQL += " adi_beg_bal     =  adi_beg_bal + ?,";
        updateSQL += " acct_jrnl_bal   =  acct_jrnl_bal + ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = ? ";
//        whereStr = "where rowid = ? ";  //phopho mod
        whereStr = "where p_seqno = ? ";
        setDouble(1, hAdiIntrs);
        setDouble(2, hAdiIntrs);
        setDouble(3, hAdiIntrs);
        setDouble(4, hJrnlIntrs);
        setString(5, javaProgram);
//        setRowId(6, h_acct_rowid);  //phopho mod
        setString(6, hAcnoPSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        daoTable = "act_acct_curr";
        updateSQL = " acct_jrnl_bal     = acct_jrnl_bal + ?,";
        updateSQL += " dc_acct_jrnl_bal  = dc_acct_jrnl_bal + ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = ? ";
        whereStr = "where p_seqno = ? ";
        whereStr += "and curr_code = '901' ";
        setDouble(1, hJrnlIntrs);
        setDouble(2, hJrnlIntrs);
        setString(3, javaProgram);
        setString(4, hAcnoPSeqno);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB019 proc = new ColB019();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
