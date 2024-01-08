/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/12  V1.00.00    phopho     program initial                          *
*  108/12/02  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/14  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC025 extends AccessDAO {
    private String progname = "帳戶報送JCIC(Z13)結清處理程式 109/12/14  V1.00.02 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hCcieIdPSeqno = "";
    String hCciePSeqno = "";
    String hCcieAcctType = "";
//    String h_ccie_acct_key = "";
    String hCcieInstFlag = "";
    String hCcieInstRate = "";
    String hCcieCloseDate = "";
    String hAcctLastPaymentDate = "";
    String hCcieRowid = "";
    int hCnt = 0;
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
                comc.errExit("Usage : ColC025 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            if (args.length == 1)
                hBusiBusinessDate = args[0];
            selectPtrBusinday();

            selectColCsInstbase();

            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));
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
        sqlCmd += " from ptr_businday  ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectColCsInstbase() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "b.id_p_seqno,";
        sqlCmd += "b.p_seqno,";
        sqlCmd += "b.acct_type,";
//        sqlCmd += "b.acct_key,";  //no column
        sqlCmd += "b.inst_flag,";
        sqlCmd += "b.inst_rate,";
//        sqlCmd += "decode(sign(a.last_payment_date-b.inst_e_date),-1,a.last_payment_date,b.inst_e_date) h_ccie_close_date,";
        sqlCmd += "decode(sign(a.last_payment_date-decode(b.inst_e_date,'','0',b.inst_e_date)),-1,a.last_payment_date,b.inst_e_date) h_ccie_close_date,";
        sqlCmd += "a.last_payment_date,";
        sqlCmd += "b.rowid as rowid ";
        sqlCmd += "from act_acct a,col_cs_instbase b ";
        sqlCmd += "where a.p_seqno = b.p_seqno ";
        sqlCmd += "and a.acct_jrnl_bal <= 0 ";
        sqlCmd += "and b.proc_flag != 'Y'  "; /* 'Y':表已結清 */
        sqlCmd += "and b.close_date = '' ";

        openCursor();
        while (fetchTable()) {
            hCcieIdPSeqno = getValue("id_p_seqno");
            hCciePSeqno = getValue("p_seqno");
            hCcieAcctType = getValue("acct_type");
//            h_ccie_acct_key = getValue("acct_key");
            hCcieInstFlag = getValue("inst_flag");
            hCcieInstRate = getValue("inst_rate");
            hCcieCloseDate = getValue("h_ccie_close_date");
            hAcctLastPaymentDate = getValue("last_payment_date");
            hCcieRowid = getValue("rowid");

            totalCnt++;
            if (totalCnt % 5000 == 0)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            if (selectCrdIdno() != 0)
                continue;

            insertColCsRemod();
            updateColCsInstbase();
        }
        closeCursor();
    }

    /***********************************************************************/
    int selectCrdIdno() throws Exception {
        int hCnt = 0;

        sqlCmd = "select decode(decode(salary_holdin_flag,'','N',salary_holdin_flag),'Y',1,0) h_cnt ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hCcieIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }

        return hCnt;
    }

    /***********************************************************************/
    void insertColCsRemod() throws Exception {
        String decode = "9";
        if (hCcieInstFlag.equals("1"))
            decode = "2";
        if (hCcieInstFlag.equals("3"))
            decode = "4";
        setValue("p_seqno", hCciePSeqno);
        setValue("acct_type", hCcieAcctType);
//        setValue("acct_key", h_ccie_acct_key);
        setValue("inst_flag", decode);
        setValue("inst_rate", hCcieInstRate);
        setValue("proc_flag", "0");
        setValue("proc_date", hBusiBusinessDate);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "col_cs_remod";
        insertTable();
        if (dupRecord.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("insert_col_cs_remod duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColCsInstbase() throws Exception {
        daoTable = "col_cs_instbase";
        updateSQL = "inst_flag = decode(inst_flag,'1','2','3','4','9'),";
        updateSQL += " close_date = ?,";
        updateSQL += " last_pay_date = ?,";
        updateSQL += " proc_date = ?,";
        updateSQL += " proc_flag = decode(inst_flag,'1','2','3','4','9'),";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hCcieCloseDate);
        setString(2, hAcctLastPaymentDate);
        setString(3, hBusiBusinessDate);
        setString(4, javaProgram);
        setRowId(5, hCcieRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_cs_instbase not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC025 proc = new ColC025();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
