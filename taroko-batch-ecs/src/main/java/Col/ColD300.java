/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/30  V1.00.00    phopho     program initial                          *
* 109/12/15  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColD300 extends AccessDAO {
    private String progname = "IFRS9-本月帳齡寬限日處理程式 109/12/15  V1.00.02 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";

    String hWdayThisAcctMonth = "";
    String hWdayThisLastpayDate = "";
    String hWdayStmtCycle = "";

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
                comc.errExit("Usage : ColD300 [business_date]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            hBusiBusinessDate = "";
            if (args.length == 1)
                hBusiBusinessDate = args[0];
            else
                selectPtrBusinday();

            if (selectPtrWorkday() != 0) {
            	exceptExit = 0;
                comcr.errRtn("本日[" + hBusiBusinessDate + "] 本程式關帳日後一日執行", "", comcr.hCallBatchSeqno);
            }

            showLogMessage("I", "", "更新 act_acag ...");
            updateActAcag();
            showLogMessage("I", "", "累計清除 [" + totalCnt + "] 筆");

            showLogMessage("I", "", "程式執行結束");

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
        sqlCmd = "select business_date ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        hWdayThisAcctMonth = "";
        hWdayThisLastpayDate = "";
        hWdayStmtCycle = "";
        sqlCmd = "select this_acct_month, ";
        sqlCmd += "this_lastpay_date, ";
        sqlCmd += "stmt_cycle ";
        sqlCmd += "from ptr_workday ";
        sqlCmd += "where this_close_date = to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') ";
        setString(1, hBusiBusinessDate);

        selectTable();
        if ( notFound.equals("Y") ) {
            return 1;
        }
        hWdayThisAcctMonth = getValue("this_acct_month");
        hWdayThisLastpayDate = getValue("this_lastpay_date");
        hWdayStmtCycle = getValue("stmt_cycle");
            
        return 0;
    }

    /***********************************************************************/
    void updateActAcag() throws Exception {
        daoTable = "act_acag";
        updateSQL = " lastpay_date  = ? ";
        whereStr = "where acct_month = ? ";
        whereStr += "and stmt_cycle  = ? ";
        setString(1, hWdayThisLastpayDate);
        setString(2, hWdayThisAcctMonth);
        setString(3, hWdayStmtCycle);

        totalCnt = updateTable();
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColD300 proc = new ColD300();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
