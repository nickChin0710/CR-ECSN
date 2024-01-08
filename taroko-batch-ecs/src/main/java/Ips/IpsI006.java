/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109-12-15   V1.00.01    tanwei      updated for project coding standard    *
 ******************************************************************************/

package Ips;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*黑名單清除暨回覆檔後續處理*/
public class IpsI006 extends AccessDAO {
    private String progname = "黑名單清除暨回覆檔後續處理  109/12/15 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hB2i3CrtDate = "";
    String hB2i3CrtTime = "";
    String hMIardIpsCardNo = "";
    String hMIardCardNo = "";
    String hMI2b6Rowid = "";
    String hIardIpsCardNo = "";
    String hIardCardNo = "";
    String hB2i3FromMark = "";
    String hI2b6Rowid = "";
    int totCnt = 0;
    int insertCnt = 0;

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
                comc.errExit("Usage : IpsI006 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if (args.length == 1)
                if (args[0].length() == 8)
                    hBusiBusinessDate = args[0];

            selectPtrBusinday();

            selectIpsCard();

            showLogMessage("I", "", String.format("Process records = [%d][%d]", totCnt, insertCnt));

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
        hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? sysDate : hBusiBusinessDate;
        sqlCmd = "select ";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_b2i3_crt_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_b2i3_crt_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hB2i3CrtDate = getValue("h_b2i3_crt_date");
            hB2i3CrtTime = getValue("h_b2i3_crt_time");
        }

    }

    /***********************************************************************/
    void selectIpsCard() throws Exception {
        /*
         * 卡號排除已有 1.鎖卡註記 2.退卡註記 3.一卡通效期屆期
         * 4.已關閉自動加值功能之卡片後，其餘資料再列入【B2I003要求列入黑名單功能卡號檔】送一卡通
         */

        /* 不看關閉自動加值，即使關閉...卡友仍可自行加值，一卡通功能仍未完全封閉，要再報黑名單 */

        sqlCmd = "select ";
        sqlCmd += "a.ips_card_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "b.rowid as rowid ";
        sqlCmd += "from ips_card a , ips_i2b006_log b ";
        sqlCmd += "where b.post_flag  = 'N' ";
        sqlCmd += "and decode(a.lock_flag,'','N',a.lock_flag) = 'N' ";
        sqlCmd += "and decode(a.return_flag,'','N',a.return_flag) = 'N' ";
        sqlCmd += "and a.new_end_date  >= ? ";
        sqlCmd += "and b.ips_card_no  = a.ips_card_no ";
        setString(1, hBusiBusinessDate);
        openCursor();
        while (fetchTable()) {
            hIardIpsCardNo = getValue("ips_card_no");
            hIardCardNo = getValue("card_no");
            hI2b6Rowid = getValue("rowid");

            hB2i3FromMark = "4";

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("crd Process record=[%d]", totCnt));

            insertIpsB2i003Log();

            updateIpsI2b006Log();
        }
        closeCursor();
    }

    /***********************************************************************/
    int insertIpsB2i003Log() throws Exception {

        insertCnt++;

        setValue("crt_date", hB2i3CrtDate);
        setValue("crt_time", hB2i3CrtTime);
        setValue("ips_card_no", hIardIpsCardNo);
        setValue("card_no", hIardCardNo);
        setValue("from_mark", hB2i3FromMark);
        setValue("proc_flag", "N");
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ips_b2i003_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            return 1;
        }

        return (0);
    }

    /***********************************************************************/
    void updateIpsI2b006Log() throws Exception {
        daoTable = "ips_i2b006_log";
        updateSQL = "post_flag   = 'Y' ,";
        updateSQL += " mod_pgm   = ? ,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where rowid    = ? ";
        setString(1, javaProgram);
        setRowId(2, hI2b6Rowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_i2b006_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsI006 proc = new IpsI006();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
