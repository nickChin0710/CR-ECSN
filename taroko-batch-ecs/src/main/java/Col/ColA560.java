/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/06  V1.00.00    phopho     program initial                          *
*  109/12/10  V1.00.01    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColA560 extends AccessDAO {
    private String progname = "無擔保債務-展延成功\\繳款評等設定處理程式   109/12/10  V1.00.01 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;
    String hCurpModLog = "";
    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hWdayThisLastpayDate = "";
    String hWdayStmtCycle = "";
    String hClncPSeqno = "";
    String hClncPaymentRate1 = "";
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
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : ColA560 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            if ((args.length == 1) && (args[0].length() == 8)) {
                hBusiBusinessDate = args[0];
            }
            if (selectPtrWorkday() != 0) {
            	exceptExit = 0;
                comcr.errRtn(String.format("Today[%s] is not cycle_date.Program will not process ! ", hBusiBusinessDate),
                        "", hCallBatchSeqno);
            }

            selectColLiauNegoAct();
            showLogMessage("I", "", String.format("Total process record [%d]....", totalCnt));
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
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        hWdayThisLastpayDate = "";
        hWdayStmtCycle = "";
        sqlCmd = "select this_lastpay_date,";
        sqlCmd += "stmt_cycle ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where this_close_date = ? ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayThisLastpayDate = getValue("this_lastpay_date");
            hWdayStmtCycle = getValue("stmt_cycle");
        } else {
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void selectColLiauNegoAct() throws Exception {
    	sqlCmd = "select ";
        sqlCmd += "b.p_seqno,";
        sqlCmd += "b.payment_rate1 ";
//        sqlCmd += "from cyc_acmm a,col_liau_nego_act b,col_liau_nego c ";  //cyc_acmm --> cyc_acmm_xx (stmt_cycle) 
        sqlCmd += "from cyc_acmm_"+ hWdayStmtCycle +" a,col_liau_nego_act b,col_liau_nego c ";
        sqlCmd += "where a.p_seqno = b.p_seqno ";
        sqlCmd += "and b.id_no  = c.id_no ";
        sqlCmd += "and b.apply_date = c.apply_date ";
        sqlCmd += "and c.report_date2 <> '' ";
        sqlCmd += "and c.agree_flag = 'Y' ";
        sqlCmd += "and ? between c.extend_s_date and c.extend_e_date ";
        setString(1, hBusiBusinessDate);

        openCursor();
        while (fetchTable()) {
            hClncPSeqno = getValue("p_seqno");
            hClncPaymentRate1 = getValue("payment_rate1");

            updateActAcno();

            totalCnt++;
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateActAcno() throws Exception {
        daoTable = "act_acno";
        updateSQL = "payment_rate1 = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm  = ? ";
//        whereStr = "where p_seqno = ? ";
        whereStr = "where acno_p_seqno = ? ";
        setString(1, hClncPaymentRate1);
        setString(2, javaProgram);
        setString(3, hClncPSeqno);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_acno not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA560 proc = new ColA560();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
