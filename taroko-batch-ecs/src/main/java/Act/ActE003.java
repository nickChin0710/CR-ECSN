/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  107/02/23  V1.00.01    Brian     error correction                          *
 *  109/11/16  V1.00.02    shiyuqi       updated for project coding standard     *
 ******************************************************************************/

package Act;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*溢付款自動銷帳DUMMYRECORD新增處理*/
public class ActE003 extends AccessDAO {

    public static final boolean debugMode = false;

    private String progname = "溢付款自動銷帳DUMMYRECORD新增處理  109/11/16  V1.00.02 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActE003";
    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String ecsServer = "";

    String hAcctPSeqno = "";
    String hAcctAcctType = "";
    String hAcctAcctKey = "";
    String hAcctIdPSeqno = "";
    String hAcctAcctHolderId = "";
    String hAcctAcctHolderIdCode = "";
    String hAcurCurrCode = "";
    String hBusiBusinessDate = "";
    String hAdclSerialNo = "";
    String hAdclModUser = "";
    // String h_adcl_mod_ws = "";
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
            if (args.length > 1) {
                comc.errExit("Usage : ActE003", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hModUser = comc.commGetUserID();
            hAdclModUser = hModUser;

            selectPtrBusinday();

            selectActAcct();
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
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

    /***********************************************************************/
    void selectActAcct() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.p_seqno,";
        sqlCmd += " a.acct_type,";
        sqlCmd += " UF_ACNO_KEY(a.p_seqno) acct_key,";
        sqlCmd += " a.id_p_seqno,";
        sqlCmd += " c.id_no ,";
        sqlCmd += " c.id_no_code ,";
        sqlCmd += " b.curr_code ";
        sqlCmd += "  from act_acct_curr b, act_acct a left join crd_idno c on a.id_p_seqno = c.id_p_seqno";
        sqlCmd += " where a.p_seqno = b.p_seqno ";
        sqlCmd += "   and (b.dc_end_bal_op > 0 or b.dc_end_bal_lk > 0) ";
        sqlCmd += "   and (-b.dc_acct_jrnl_bal != b.dc_end_bal_op + b.dc_end_bal_lk) ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcctPSeqno = getValue("p_seqno");
            hAcctAcctType = getValue("acct_type");
            hAcctAcctKey = getValue("acct_key");
            hAcctIdPSeqno = getValue("id_p_seqno");
            hAcctAcctHolderId = getValue("id_no");
            hAcctAcctHolderIdCode = getValue("id_no_code");
            hAcurCurrCode = getValue("curr_code");

            totalCnt++;
            insertActDebtCancel();
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void insertActDebtCancel() throws Exception {
        String tmpstr = "";

        tmpstr = String.format("%05d", totalCnt);
        hAdclSerialNo = tmpstr;

        setValue("batch_no", hBusiBusinessDate + "99990099");
        setValue("serial_no", hAdclSerialNo);
        setValue("p_seqno", hAcctPSeqno);
        setValue("acno_p_seqno", hAcctPSeqno);
        setValue("acct_type", hAcctAcctType);
        setValue("id_p_seqno", hAcctIdPSeqno);
      //setValue("id", h_acct_acct_holder_id);
      //setValue("id_code", h_acct_acct_holder_id_code);
        setValue("curr_code", hAcurCurrCode);
        setValueDouble("pay_amt", 0);
        setValueDouble("dc_pay_amt", 0);
        setValue("pay_date", "19900101");
        setValue("payment_type", "DUMY");
        setValue("update_user", hAdclModUser);
        setValue("update_date", sysDate);
        setValue("update_time", sysTime);
        setValue("mod_user", hAdclModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "act_debt_cancel";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_debt_cancel duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActE003 proc = new ActE003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
