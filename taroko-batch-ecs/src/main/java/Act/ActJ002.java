/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/12/14  V1.00.01    Brian     error correction                          *
 *  109/11/18  V1.00.02    shiyuqi   updated for project coding standard       *  
 *  111/10/25  V1.00.03    Simon     sync codes with mega                      *
 ******************************************************************************/

package Act;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*寬限日MP<=1000設定旗標存處理程式處理程式*/
public class ActJ002 extends AccessDAO {

    private String progname = "寬限日MP<=1000設定旗標存處理程式處理程式  111/10/25  V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hModUser = "";
    long hModSeqno = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    int recordCnt = 0;

    String hBusiBusinessDate = "";
    String hAcurPSeqno = "";
    String hAcurCurrCode = "";
    double hAcurTtlAmt = 0;
    double hAcurTtlAmtBal = 0;
    double hPcglTotalBal = 0;
    int totalCnt = 0;
    int updateCnt = 0;

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
                comc.errExit("Usage : ActJ002", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            selectPtrBusinday();

            selectActAcctCurr();

            showLogMessage("I", "",
                    String.format("Adjust total proecess cnt[%d] update_cnt[%d]", totalCnt, updateCnt));
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
    void selectActAcctCurr() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " a.p_seqno,";
        sqlCmd += " a.curr_code,";
        sqlCmd += " decode(curr_code,'901',a.ttl_amt,a.dc_ttl_amt) h_acur_ttl_amt,";
        sqlCmd += " decode(curr_code,'901',a.ttl_amt_bal,a.dc_ttl_amt_bal) h_acur_ttl_amt_bal ";
        sqlCmd += " from act_acct_curr a,act_acct b,ptr_workday c ";
        sqlCmd += "where a.p_seqno = b.p_seqno ";
        sqlCmd += " and b.stmt_cycle = c.stmt_cycle ";
        sqlCmd += " and c.this_delaypay_date = ? ";
        sqlCmd += " and decode(curr_code,'901',a.ttl_amt,a.dc_ttl_amt) > 0 ";
        sqlCmd += " and decode(a.delaypay_ok_flag,'','N',a.delaypay_ok_flag) != 'Y' ";
        setString(1, hBusiBusinessDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hAcurPSeqno = getValue("p_seqno");
            hAcurCurrCode = getValue("curr_code");
            hAcurTtlAmt = getValueDouble("h_acur_ttl_amt");
            hAcurTtlAmtBal = getValueDouble("h_acur_ttl_amt_bal");

            totalCnt++;
            selectPtrCurrGeneral();
            if (hAcurTtlAmtBal > hPcglTotalBal)
                continue;

            updateActAcctCurr();
            updateCnt++;
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void selectPtrCurrGeneral() throws Exception {
        hPcglTotalBal = 0;

        sqlCmd = "select total_bal ";
        sqlCmd += " from ptr_curr_general  ";
        sqlCmd += "where curr_code = ? ";
        setString(1, hAcurCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_curr_general not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPcglTotalBal = getValueDouble("total_bal");
        }

    }

    /***********************************************************************/
    void updateActAcctCurr() throws Exception {
        daoTable = "act_acct_curr";
        updateSQL = " delaypay_ok_flag = 'Y',";
        updateSQL += " mod_time         = sysdate,";
        updateSQL += " mod_pgm          = 'ActJ002'";
        whereStr  = "where p_seqno      = ? ";
        whereStr  += " and curr_code    = ? ";
        setString(1, hAcurPSeqno);
        setString(2, hAcurCurrCode);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acct_curr not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {

        ActJ002 proc = new ActJ002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
