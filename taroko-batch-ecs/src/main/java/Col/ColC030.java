/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/08/31  V1.00.00   PhoPho     program initial                           *
*  109/12/14  V1.00.01    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC030 extends AccessDAO {
    private String progname = "更生清算帳戶繳款記錄處理程式109/12/14  V1.00.01  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";
    String hMCurpModPgm = "";
    String hMCurpModTime = "";
    String hMCurpModUser = "";
    String hMCurpModWs = "";
    long hMCurpModSeqno = 0;
    String hMCurpModLog = "";
    String hCallRProgramCode = "";

    String hBusiBusinessDate = "";
    String hMCldpHolderId = "";
    String hMCldpPaymentDateS = "";
    String hTempOldType = "";
    String hMAcnoIdPSeqno = "";
    String hMJrnlAcctDate = "";
    double hMJrnlTransactionAmt = 0;
    String hCldpHolderId = "";
    String hAcnoIdPSeqno = "";
    String hJrnlAcctDate = "";
    double hJrnlTransactionAmt = 0;
    int totalCnt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : ColC030 [old_type/business_date] [callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);
            
            hBusiBusinessDate = "";
            hTempOldType = "0";

            if (args.length >= 1) {
                if (args[0].length() == 8)
                    hBusiBusinessDate = args[0];
                if (args[0].length() == 1)
                    hTempOldType = "1";
            }

            selectPtrBusinday();

            selectActJrnl();

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
        selectSQL = "decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date ";
        daoTable = "ptr_businday";
        whereStr = "fetch first 1 row only";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
    }

    /**********************************************************************/
    void selectActJrnl() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "b.holder_id,";
        sqlCmd += "c.acct_date,";
        sqlCmd += "c.transaction_amt ";
        sqlCmd += "from act_jrnl c,act_acno a,(select holder_id, HOLDER_ID_P_SEQNO, ";
        sqlCmd += "min(payment_date_s) pay_date ";
        sqlCmd += "from col_liad_paymain ";
        sqlCmd += "where liad_type in ('1','2') ";
        sqlCmd += "group by holder_id, HOLDER_ID_P_SEQNO) b ";
//        sqlCmd += "where c.p_seqno = a.p_seqno ";
        sqlCmd += "where c.p_seqno = a.acno_p_seqno ";
        sqlCmd += "and c.acct_type = a.acct_type ";
        sqlCmd += "and c.tran_class = 'P' ";
        sqlCmd += "and a.id_p_seqno = b.HOLDER_ID_P_SEQNO ";
        sqlCmd += "and c.acct_date >= b.pay_date ";
        sqlCmd += "and c.acct_date = decode(cast(? as varchar(1)),'0', ";
        sqlCmd += "                  to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd'), ";
        sqlCmd += "c.acct_date) ";
        setString(1, hTempOldType);
        setString(2, hBusiBusinessDate);

        openCursor();
        while (fetchTable()) {
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hCldpHolderId = getValue("holder_id");
            hJrnlAcctDate = getValue("acct_date");
            hJrnlTransactionAmt = getValueDouble("transaction_amt");

            insertColLiadJrnl();

            totalCnt++;
            if (totalCnt % 5000 == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }
        }
        closeCursor();
    }

    /**********************************************************************/
    void insertColLiadJrnl() throws Exception {
        setValue("id_no", hCldpHolderId);
        setValue("id_p_seqno", hAcnoIdPSeqno);
        setValue("acct_date", hJrnlAcctDate);
        setValueDouble("tran_amt", hJrnlTransactionAmt);
        setValueDouble("tran_amt_bal", hJrnlTransactionAmt);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "col_liad_jrnl";
        insertTable();
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC030 proc = new ColC030();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
