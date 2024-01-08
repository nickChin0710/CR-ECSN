/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109-12-15  V1.00.01    tanwei      updated for project coding standard     *
 *  112/03/06  V1.00.02    Wilson    接收檔案名稱調整                                                                                     *
 ******************************************************************************/

package Ips;

import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*會員銀行(B2I)媒體FTP接收處理*/
public class IpsT003 extends AccessDAO {
    private String progname = "會員銀行(B2I)媒體FTP接收處理  112/03/06 V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempNotifyDate = "";
    String hTnlgNotifyTime = "";
    String hTnlgFtpSendDate = "";
    String hTnlgFileName = "";
    String hTnlgRowid = "";
    String hEflgTransSeqno = "";
    String hEflgFileName = "";
    String hEflgRowid = "";
    String hEflgFileDate = "";
    String hEflgProcCode = "";
    String hEflgProcDesc = "";

    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hEriaLocalDir = "";
    int forceFlag = 0;
    int totCnt = 0;
    int succCnt = 0;
    int totalCnt = 0;
    String nUserpid = "";
    String tmpstr = "";
    int nRetcode = 0;
    int totalCnt1 = 0;
    int hTnlgRecordCnt = 0;
    int errCode = 0;

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
                comc.errExit("Usage : IpsT003 [[notify_date][force_flag]] [force_flag]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hTnlgNotifyDate = args[0];
            }
            if (args.length == 2) {
                hTnlgNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();

            if (forceFlag == 0) {
                errCode = selectIpsNotifyLog();
                if (errCode == 1) {
                    exceptExit = 0;
                    comcr.errRtn("本日會員銀行通知檔媒體已處理完成, 不可再處理!(error)", "", hCallBatchSeqno);
                }
            }

            showLogMessage("I", "", String.format(" Process Date=[%s][%d]", hTnlgNotifyDate, forceFlag));

            selectIpsNotifyLog1();

            showLogMessage("I", "", String.format("Total process [%d] Records", totCnt));
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
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? sysDate : hTnlgNotifyDate;
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_notify_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_tnlg_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempNotifyDate = getValue("h_temp_notify_date");
            hTnlgNotifyTime = getValue("h_tnlg_notify_time");
        }

    }

    /***********************************************************************/
    int selectIpsNotifyLog() throws Exception {
        sqlCmd = "select ftp_send_date ";
        sqlCmd += " from ips_notify_log  ";
        sqlCmd += "where notify_date  = ?  ";
        sqlCmd += "and ftp_receive_date <> ''  ";
        sqlCmd += "and tran_type  = 'I'  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }
        if (recordCnt > 0) {
            hTnlgFtpSendDate = getValue("ftp_send_date");
        }

        return 1;
    }

    /***********************************************************************/
    void selectIpsNotifyLog1() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "file_name,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from ips_notify_log ";
        sqlCmd += "where notify_date = ? ";
        sqlCmd += "and tran_type = 'I' ";
        sqlCmd += "and ftp_send_date <> '' ";
        setString(1, hTnlgNotifyDate);
        openCursor();

        while (fetchTable()) {
            hTnlgFileName = getValue("file_name");
            hTnlgRowid = getValue("rowid");

            procFtp(0);

            selectEcsFtpLog();
            updateIpsNotifyLog();
            commitDataBase();
            totCnt++;
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateIpsNotifyLog() throws Exception {
        daoTable = "ips_notify_log";
        updateSQL = "ftp_receive_date = ?,";
        updateSQL += " ftp_receive_time = ?,";
        updateSQL += " check_code  = '0000',";
        updateSQL += " proc_date  = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " proc_time  = to_char(sysdate,'hh24miss'),";
        updateSQL += " proc_flag  = '3',";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where rowid   = ? ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyTime);
        setString(3, javaProgram);
        setRowId(4, hTnlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void procFtp(int intCnt) throws Exception {
        String[] fileExt = { ".zip.ack", ".zip.err" };

        for (int inti = 0; inti < 2; inti++) {

            tmpstr1 = String.format("%s/media/ips", comc.getECSHOME());
            tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
            hEriaLocalDir = tmpstr1;

            CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
            CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

            commFTP.hEflgTransSeqno = comr
                    .getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
            commFTP.hEflgSystemId = "IPS_SFTP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
            commFTP.hEflgGroupId = "0000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
            commFTP.hEflgSourceFrom = "IPS"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
            commFTP.hEriaLocalDir = hEriaLocalDir;
            commFTP.hEflgModPgm = this.getClass().getName();
            String hEflgRefIpCode = "IPS_SFTP_IN";

            System.setProperty("user.dir", commFTP.hEriaLocalDir);

            String procCode = String.format("get %21.21s%s", hTnlgFileName, fileExt[inti]);
            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

            int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
            if (errCode != 0) {
                showLogMessage("I", "", String.format("[%s] => msg_code[%d]", procCode, errCode));
            }
        }

    }

    /***********************************************************************/
    void selectEcsFtpLog() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "file_name,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from ecs_ftp_log ";
        sqlCmd += "where trans_seqno = ? ";
        sqlCmd += "and trans_resp_code = 'Y' ";
        setString(1, hEflgTransSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hEflgFileName = getValue("file_name", i);
            hEflgRowid = getValue("rowid", i);
            hEflgProcCode = "0";

            hEflgProcDesc = "FTP檔案完成";
            updateEcsFtpLog();
        }

    }

    /***********************************************************************/
    void updateEcsFtpLog() throws Exception {
        daoTable = "ecs_ftp_log";
        updateSQL = "file_date = ?,";
        updateSQL += " proc_code = ?,";
        updateSQL += " proc_desc = ?,";
        updateSQL += " mod_pgm = ?,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where rowid  = ? ";
        setString(1, hEflgFileDate);
        setString(2, hEflgProcCode);
        setString(3, hEflgProcDesc);
        setString(4, javaProgram);
        setRowId(5, hEflgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ecs_ftp_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsT003 proc = new IpsT003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
