/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/22  V1.00.00    phopho     program initial                          *
*  108/11/29  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/13  V1.00.02    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB032 extends AccessDAO {
    private String progname = "強停報告表已繳清註記處理程式 109/12/13  V1.00.02 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";
    String hBusiBusinessDate = "";

    String hCoscStaticType = "";
    String hCoscAcctMonth = "";
    String hCoscRowid = "";

    int    totalCnt          = 0;
    String hTempAcctMonth = "";
    String temstr            = "";

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
                comc.errExit("Usage : ColB032 [date]", "              1.date  : 目前日期(yyyymmdd)");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            if ((args.length != 0) && (args[0].length()==8)) {
                hBusiBusinessDate = args[0];
                selectPtrBusinday1();
            } else {
//                temstr = comcr.get_businday(h_temp_acct_month, -3);
                temstr = comcr.getBusinday(hBusiBusinessDate, -3);
                if (hBusiBusinessDate.compareTo(temstr) != 0) {
                	exceptExit = 0;
                    String err1 = "本程式只能在每月最後營業日前 2 日執行";
                    String err2 = "最後營業日前 2 日[" + temstr + "]";
                    comcr.errRtn(err1, err2, hCallBatchSeqno);
                }
            }

            selectColOppostStatic();

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
        hBusiBusinessDate = "";
        hTempAcctMonth = "";
        sqlCmd = "select business_date, ";
        sqlCmd += "substr(business_date,1,6) acct_month ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempAcctMonth = getValue("acct_month");
        }
    }

    /***********************************************************************/
    void selectPtrBusinday1() throws Exception {
        hTempAcctMonth = comc.getSubString(hBusiBusinessDate, 0, 6);
        // h_temp_acct_month = "";
        //
        // sqlCmd = "select substr(?,1,6) acct_month ";
        // sqlCmd += "from dual ";
        // setString(1, h_busi_business_date);
        //
        // int recordCnt = selectTable();
        // if (recordCnt > 0) {
        // h_temp_acct_month = getValue("acct_month");
        // }
    }

    /***********************************************************************/
    void selectColOppostStatic() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.rowid as rowid, ";
        sqlCmd += "decode(a.acct_jrnl_bal,0,'9','1') static_type ";
        sqlCmd += "from act_acct b, col_oppost_static a ";
        sqlCmd += "where a.p_seqno = a.p_seqno ";
        sqlCmd += "and   a.static_type != decode(a.acct_jrnl_bal,0,'9','1') ";

        openCursor();
        while (fetchTable()) {
            hCoscRowid = getValue("rowid");
            hCoscStaticType = getValue("static_type");

            updateColOppostStatic();
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateColOppostStatic() throws Exception {
        daoTable = "col_oppost_static";
        updateSQL = " static_type = ?,";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm   = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hCoscStaticType);
        setString(2, javaProgram);
        setRowId(3, hCoscRowid);
        updateTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("update_col_oppost_static not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB032 proc = new ColB032();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
