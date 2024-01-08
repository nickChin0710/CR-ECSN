/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/08/22  V1.01.01  phopho     Initial                                    *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
*****************************************************************************/
package Col;

import com.*;

public class ColC120 extends AccessDAO {
    private String progname = "傳送CS(M1)C-刪除非本期資料處理程式 109/12/15  V1.00.01  ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    int denugD = 0;
    String hBusiBusinessDate = "";

    String hCcotPSeqno = "";
    String hCcotFormType = "";
    String hCcotRowid = "";
    int hCprmGenCsDay = 0;
    String hWdayStmtCycle = "";
    String hWdayThisAcctMonth = "";
    String hWdayThisCloseDate = "";

    long totalCnt = 0;
    int reasonCnt[] = new int[10];

    String hTempBusinessDate = "";
    String hTempDeleteType = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC120 proc = new ColC120();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            // 檢查參數
            if (args.length != 1 && args.length != 2) {
                String err1 = "Usage : ColC120 delete_type [business_date]";
                String err2 = "        delete_type 1 : 刪除當日無繳款                    2 : 刪除當日有繳款";
                comc.errExit(err1, err2);
            }
            
            hTempDeleteType = args[0];
            if ((hTempDeleteType.equals("1") == false)
                    && (hTempDeleteType.equals("2") == false)) {
                comc.errExit("參數錯誤", "");
            }
            hBusiBusinessDate = "";
            if (args.length == 2) {
                hBusiBusinessDate = args[1];
            }
            
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            selectPtrBusinday();
            selectColParam();
            if (selectPtrWorkday() != 0) {
            	exceptExit = 0;
                comcr.errRtn("本日[" + hBusiBusinessDate + "]+[" + hCprmGenCsDay + "]不需執行", "", "");
            }

            showLogMessage("I", "", "本日[" + hBusiBusinessDate + "]-[" + hTempBusinessDate + "]");
            showLogMessage("I", "", "=========================================");
            if (hTempDeleteType.equals("1"))
                showLogMessage("I", "", "刪除M1非本期當日無繳款資料開始....");
            else
                showLogMessage("I", "", "刪除M1非本期當日有繳款資料開始....");

            totalCnt = 0;
            selectColCsOut();
            showLogMessage("I", "", "刪除帳戶        筆數 [" + totalCnt + "]");
            showLogMessage("I", "", "    公司戶      筆數 [" + reasonCnt[1] + "]");
            showLogMessage("I", "", "    公司個繳戶  筆數 [" + reasonCnt[2] + "]");
            showLogMessage("I", "", "    一般卡戶    筆數 [" + reasonCnt[3] + "]");
            showLogMessage("I", "", "=========================================");

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "程式執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
      // ************************************************************************

    private void selectPtrBusinday() throws Exception {
        hTempBusinessDate = "";
        selectSQL = "decode(cast(? as varchar(8)),'',business_date, cast(? as varchar(8))) business_date, "
                + "to_char(to_date(decode(cast(? as varchar(8)),'',business_date, cast(? as varchar(8))),'yyyymmdd')-1 days,'yyyymmdd') temp_date ";
        daoTable = "ptr_businday";
        whereStr = "fetch first 1 row only";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        hBusiBusinessDate = getValue("business_date");
        hTempBusinessDate = getValue("temp_date");
    }

    // ************************************************************************
    private void selectColParam() throws Exception {
        hCprmGenCsDay = 0;
        sqlCmd = "select nvl(gen_cs_day,0) gen_cs_day ";
        sqlCmd += "from col_param ";

        selectTable();
        if (notFound.equals("Y")) {
            String err1 = "select_col_param error!";
            String err2 = "";
            comcr.errRtn(err1, err2, "");
        }
        hCprmGenCsDay = getValueInt("gen_cs_day");
    }

    // ************************************************************************
    private int selectPtrWorkday() throws Exception {
        hCprmGenCsDay = 0;
        sqlCmd = "select stmt_cycle, ";
        sqlCmd += "this_acct_month, ";
        sqlCmd += "this_close_date ";
        sqlCmd += "from ptr_workday ";
        sqlCmd += "where to_date(this_close_date,'yyyymmdd') + ? days= to_date(?,'yyyymmdd') -1 days ";
        setInt(1, hCprmGenCsDay);
        setString(2, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y"))
            return 1;
        hWdayStmtCycle = getValue("stmt_cycle");
        hWdayThisAcctMonth = getValue("this_acct_month");
        hWdayThisCloseDate = getValue("this_close_date");
        showLogMessage("I", "", "stmt_cycle[" + hWdayStmtCycle + "] acct_month[" + hWdayThisAcctMonth + "]");

        return 0;
    }

    // ************************************************************************
    private void selectColCsOut() throws Exception {
        int inti;
        for (inti = 0; inti < 10; inti++)
            reasonCnt[inti] = 0;
        selectSQL = "form_type, p_seqno, rowid as rowid ";
        daoTable = "col_cs_out";
        whereStr = "where stmt_cycle = ? and  acct_month != ? and  ((? = '1' and decode(pay_date,'','x', pay_date) != ?) "
                + " or    (? = '2'  and decode(pay_date,'','x', pay_date) = ?)) ";
        setString(1, hWdayStmtCycle);
        setString(2, hWdayThisAcctMonth);
        setString(3, hTempDeleteType);
        setString(4, hTempBusinessDate);
        setString(5, hTempDeleteType);
        setString(6, hTempBusinessDate);

        openCursor();
        while (fetchTable()) {
            hCcotFormType = getValue("form_type");
            hCcotPSeqno = getValue("p_seqno");
            hCcotRowid = getValue("rowid");

            totalCnt++;
            if (totalCnt % 3000 == 0)
                showLogMessage("I", "", " 目前處理筆數 =[" + totalCnt + "]");

            reasonCnt[comcr.str2int(hCcotFormType)]++;
            deleteColCsOut();
        }
        closeCursor();
    }

    // ************************************************************************
    private void deleteColCsOut() throws Exception {
        daoTable = "col_cs_out";
        whereStr = "WHERE rowid = ? ";
        setRowId(1, hCcotRowid);

        deleteTable();

        if (notFound.equals("Y")) {
            String err1 = "delete_col_cs_out error!";
            String err2 = "rowid=[" + hCcotRowid + "]";
            comcr.errRtn(err1, err2, "");
        }
    }
    // ************************************************************************
}