/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-13  V1.00.01    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*悠遊卡簽帳資料檔(STMT)退貨資料處理程式*/
public class TscB002 extends AccessDAO {
    private final String progname = "悠遊卡簽帳資料檔(STMT)退貨資料處理程式   109/11/13 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTsrdAcctMonth = "";
    String hTrlgAcctMonth = "";
    String hMTsrdCardNo = "";
    int hCnt = 0;
    double hTsrdRefundAmt = 0;
    String hTsrdCardNo = "";
    double hTrlgTranAmt = 0;
    int forceFlag = 0;
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
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : TscB002 [business_date] [flag]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hBusiBusinessDate = args[0];
            }
            if (args.length == 2) {
                if ((args[1].length() == 1) && (args[1].equals("Y")))
                    forceFlag = 1;
                hBusiBusinessDate = args[0];
            }
            selectPtrBusinday();
            showLogMessage("I", "", String.format("處理月份 [%s]==>[%s]", hTrlgAcctMonth, hTsrdAcctMonth));
            if (forceFlag == 0) {
                if (selectTscStmtRefunda() != 0) {
                    String stderr = String.format("本月份不可重複處理 [%s]", hTsrdAcctMonth);
                    comcr.errRtn(stderr, "", hCallBatchSeqno);
                }
            }

            deleteTscStmtRefund();
            selectTscStmtRefund();
            showLogMessage("I", "", String.format("Process records = [%d]", totalCnt));
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
        sqlCmd = "select decode(cast(? as varchar(10)),'',business_date, ?) as h_busi_business_date,";
        sqlCmd += "substr(decode(cast(? as varchar(10)),'',business_date, ?),1,6) as h_tsrd_acct_month,";
        sqlCmd += "to_char(add_months(to_date(decode(cast(? as varchar(10)),'', business_date, ?),'yyyymmdd'),-1),'yyyymm') as h_trlg_acct_month ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hTsrdAcctMonth = getValue("h_tsrd_acct_month");
            hTrlgAcctMonth = getValue("h_trlg_acct_month");
        }

    }

    /***********************************************************************/
    int selectTscStmtRefunda() throws Exception {
        sqlCmd = "select 1 h_cnt ";
        sqlCmd += " from tsc_stmt_refund  ";
        sqlCmd += "where acct_month = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTsrdAcctMonth);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        } else
            return (0);
        return (1);
    }

    /***********************************************************************/
    void deleteTscStmtRefund() throws Exception {
        daoTable = "tsc_stmt_refund";
        whereStr = "where acct_month = ? ";
        setString(1, hTsrdAcctMonth);
        deleteTable();

    }

    /***********************************************************************/
    void selectTscStmtRefund() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "card_no ";
        sqlCmd += "from tsc_stmt_refund ";
        sqlCmd += "where acct_month = ? ";
        sqlCmd += "UNION ";
        sqlCmd += "select card_no ";
        sqlCmd += "from tsc_refund_log ";
        sqlCmd += "where acct_month = ? ";
        setString(1, hTrlgAcctMonth);
        setString(2, hTrlgAcctMonth);
        openCursor();
        while (fetchTable()) {
            hTsrdCardNo = getValue("card_no");

            totalCnt++;
            selectTscStmtRefund1();
            selectTscRefundLog1();
            if (hTsrdRefundAmt + hTrlgTranAmt > 0) {
                showLogMessage("I", "", String.format("卡號[%s]計算有不合理值之錯誤", hTsrdCardNo));
            }
            insertTscStmtRefund();
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectTscStmtRefund1() throws Exception {
        hTsrdRefundAmt = 0;
        sqlCmd = "select sum(refund_amt) h_tsrd_refund_amt ";
        sqlCmd += " from tsc_stmt_refund  ";
        sqlCmd += "where acct_month = ? ";
        sqlCmd += "  and card_no    = ? ";
        setString(1, hTrlgAcctMonth);
        setString(2, hTsrdCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTsrdRefundAmt = getValueDouble("h_tsrd_refund_amt");
        }

    }

    /***********************************************************************/
    void selectTscRefundLog1() throws Exception {
        hTrlgTranAmt = 0;
        sqlCmd = "select sum(tran_amt) h_trlg_tran_amt ";
        sqlCmd += " from tsc_refund_log  ";
        sqlCmd += "where acct_month = ?  ";
        sqlCmd += "  and card_no    = ?  ";
        setString(1, hTrlgAcctMonth);
        setString(2, hTsrdCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTrlgTranAmt = getValueDouble("h_trlg_tran_amt");
        }

    }
    /***********************************************************************/
    void insertTscStmtRefund() throws Exception {
        sqlCmd = "insert into tsc_stmt_refund ";
        sqlCmd += "(acct_month,";
        sqlCmd += "card_no,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "refund_amt,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_time)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "sysdate ";
        sqlCmd += "from tsc_card a where card_no  = ? "
                + "and new_end_date = (select max(new_end_date) from tsc_card b where b.card_no = ?) "
                + "fetch first 1 rows only ";
        setString(1, hTsrdAcctMonth);
        setString(2, hTsrdCardNo);
        setDouble(3, hTsrdRefundAmt);
        setString(4, javaProgram);
        setString(5, hTsrdCardNo);
        setString(6, hTsrdCardNo);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscB002 proc = new TscB002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
