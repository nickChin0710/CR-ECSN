/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/11/08  V1.00.01    SUP       error correction                          *
 *  109-11-16  V1.00.02    tanwei    updated for project coding standard       *
 ******************************************************************************/

package Tsc;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*悠遊卡黑名單檔(BKEC)媒體RPT處理程式*/
public class TscF012 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;

    private final String progname = "悠遊卡黑名單檔(BKEC)媒體RPT處理程式  109/11/16 V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTnlgFileName = "";
    String hTnlgFileIden = "";
    String hTnlgRespCode = "";
    String hTnlgRowid = "";
    String hTnlgCheckCode = "";

    int hTnlgRecordCnt = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int nUserpid = 0;
    int succCnt = 0;
    int hCgecSeqNo = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String fileSeq = "";
    String temstr1 = "";
    String tmpstr2 = "";
    String hCgecBillType = "";
    String hTnlgProcFlag = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }

            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : TscF012 [notify_date [force_flag]]", "");
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

            hTnlgProcFlag = "N";
            hTnlgNotifyDate = "";
            if (args.length >= 1)
                hTnlgNotifyDate = args[0];
            if ((args.length == 2) && (args[1].toCharArray()[0] == 'Y'))
                hTnlgProcFlag = "Y";
            selectPtrBusinday();

            selectTscNotifyLog();

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
        sqlCmd += "fetch first 1 rows only";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? comm.nextNDate(hBusiBusinessDate,-1) : hTnlgNotifyDate;
    }

    /***********************************************************************/
    void selectTscNotifyLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " file_name,";
        sqlCmd += " file_iden,";
        sqlCmd += " resp_code,";
        sqlCmd += " rowid rowid";
        sqlCmd += " from tsc_notify_log ";
        sqlCmd += "where proc_flag = '1' ";
        sqlCmd += "  and file_iden = 'BKEC' ";
        sqlCmd += "  and notify_date = decode(cast(? as varchar(8)) , '', notify_date, ?) ";
        sqlCmd += "order by notify_date ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hTnlgFileName = getValue("file_name");
            hTnlgFileIden = getValue("file_iden");
            hTnlgRespCode = getValue("resp_code");
            hTnlgRowid = getValue("rowid");

            showLogMessage("I", "", String.format("[%s] 處理中 ..", hTnlgFileName));
            if (!hTnlgRespCode.equals("0000")) {
                updateTscNotifyLog();
                showLogMessage("I", "",
                        String.format("[%s] 整檔處理失敗, 錯誤代碼[%s](error)!", hTnlgFileName, hTnlgRespCode));
                continue;
            }
            hTnlgCheckCode = "0000";
            updateTscNotifyLog();

            temstr1 = String.format("%s/media/tsc/%s.RPT", comc.getECSHOME(), hTnlgFileName);
            tmpstr1 = String.format("%s/media/tsc", comc.getECSHOME());
            tmpstr2 = String.format("%s/media/tsc/backup/%s.RPT", comc.getECSHOME(), hTnlgFileName);
            tmpstr2 = Normalizer.normalize(tmpstr2, java.text.Normalizer.Form.NFKD);
            comc.fileRename(temstr1, tmpstr2);
            showLogMessage("I", "", String.format("     處理錯誤筆數[%d]", totalCnt));
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void updateTscNotifyLog() throws Exception {
        daoTable = "tsc_notify_log";
        updateSQL = " check_code  = ?,";
        updateSQL += " proc_flag = '2',";
        updateSQL += " proc_date = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " proc_time = to_char(sysdate, 'hh24miss'),";
        updateSQL += " mod_pgm   = 'TscF012',";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where file_name  = ? ";
        setString(1, hTnlgCheckCode);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF012 proc = new TscF012();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
