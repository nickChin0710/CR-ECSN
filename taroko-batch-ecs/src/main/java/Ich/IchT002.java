/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/12/22  V1.00.00  Lai         program initial                           *
 *  109-12-16   V1.00.01    tanwei      updated for project coding standard    *
 ******************************************************************************/

package Ich;

import java.text.Normalizer;


import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class IchT002 extends AccessDAO {
    private String progname = "特約機構(AnnB)媒體回覆檔產生作業  109/12/16 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    String stderr = "";
    String root = String.format("%s/media/ich/", comc.getECSHOME());

    String hTnlgNotifyDate = "";
    String hPrevNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempNotifyDate = "";
    String hTnlgNotifyTime = "";
    String hTnlgFtpSendDate = "";
    String hTnlgFileName = "";
    int hTnlgRecordCnt = 0;
    int hTnlgRecordSucc = 0;
    String hTnlgRowid = "";
    String hEflgFileName = "";
    String hEflgRowid = "";
    String hEflgFileDate = "";
    String hEflgProcCode = "";
    String hEflgProcDesc = "";

    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String hEflgRefIpCode = "";
    String nUserpid = "";
    String tmpstr = "";
    String hHash = "";
    int forceFlag = 0;
    int totCnt = 0;
    int succCnt = 0;
    int nRetcode = 0;
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
            // if (comm.isAppActive(javaProgram)) {
            // comc.err_exit("Error!! Someone is running this program now!!!", "Please wait
            // a moment to run again!!");
            // }
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IchT002 [[notify_date][force_flag]] [force_flag]", "");
            }
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = this.getClass().getName();
            String hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

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

            if (debug == 1)
                showLogMessage("I", "",
                        "Process date=[" + hTnlgNotifyDate + "]" + forceFlag + "," + hPrevNotifyDate);

            if (forceFlag == 0) {
                errCode = selectIchNotifyLog();
                if (errCode == 1) {
                    exceptExit = 0;
                    comcr.errRtn("本日會員銀行通知檔媒體已處理完成, 不可再處理!(error)", "", comcr.hCallBatchSeqno);
                }
            }

            selectIchNotifyLogA();

            hTnlgRecordCnt = totCnt;

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

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
        sqlCmd += "to_char(to_date(cast(? as varchar(10)),'yyyymmdd')- 1,'yyyymmdd') h_prev_notify_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_notify_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_tnlg_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hPrevNotifyDate = getValue("h_prev_notify_date");
            hTempNotifyDate = getValue("h_temp_notify_date");
            hTnlgNotifyTime = getValue("h_tnlg_notify_time");
        }

    }

    /***********************************************************************/
    int selectIchNotifyLog() throws Exception {
        sqlCmd = "select ftp_send_date ";
        sqlCmd += "  from ich_notify_log  ";
        sqlCmd += " where notify_date    = ?  ";
        sqlCmd += "   and ftp_send_date <> ''  ";
        sqlCmd += "   fetch first 1 rows only ";
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
    void selectIchNotifyLogA() throws Exception {
        tmpstr1 = String.format("ARPB_%3.3s_%8.8s", comc.ICH_BANK_ID3, hTnlgNotifyDate);

        String temstr2 = String.format("%s/%s", root, tmpstr1);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        int out = openOutputText(temstr2, "big5");

        hHash = "0000000000000000000000000000000000000000";

        selectSQL = " file_name  , record_cnt , record_succ, rowid as rowid1 ";
        daoTable = " ich_notify_log  ";
        whereStr = " where notify_date   = ? ";
        whereStr += "   and tran_type     = 'O' ";
        whereStr += "   and decode(ftp_send_date,'', 'N',ftp_send_date) = 'N'  ";
        whereStr += " order by file_name        ";
        setString(1, hPrevNotifyDate);
        int recCnt = selectTable();
        if (debug == 1)
            showLogMessage("I", "", "Read 'O' file cnt=[" + recCnt + "]" + hPrevNotifyDate);

        for (int inti = 0; inti < recCnt; inti++) {
            hTnlgFileName = getValue("file_name", inti);
            hTnlgRecordCnt = getValueInt("record_cnt", inti);
            hTnlgRecordSucc = getValueInt("record_succ", inti);
            hTnlgRowid = getValue("rowid1", inti);
            totCnt++;
            if (hTnlgFileName.substring(1, 3).equals("RQ")) {
                hTnlgFileName = hTnlgFileName.substring(0, 1) + "RP" + hTnlgFileName.substring(3);
            }
            if (debug == 1)
                showLogMessage("I", "", "Read 111 file=[" + totCnt + "]" + hTnlgFileName);
            if (totCnt == 1) {
                tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%04d%6.6s%40.40s", "A99B", "01", "0001",
                        comc.ICH_BANK_ID3, recCnt, " ", hHash);
                if (debug == 1)
                    showLogMessage("I", "", " head str=[" + tmpstr2 + "]" + recCnt);
                writeTextFile(out, tmpstr2 + "\r\n");
            }
            dateTime();
            tmpstr2 = String.format("D%-25.25s%08d%08d%08d%14.14s", hTnlgFileName, hTnlgRecordCnt,
                    hTnlgRecordSucc, 0, sysDate + sysTime);
            writeTextFile(out, tmpstr2 + "\r\n");

            moveBackup(hTnlgFileName);

            updateIchNotifyLog();
        }

        if (recCnt > 0) {
            closeOutputText(out);
        }

    }

    /***********************************************************************/
    void updateIchNotifyLog() throws Exception {
        daoTable = "ich_notify_log";
        updateSQL = " ftp_send_date = ?,";
        updateSQL += " ftp_send_time = ?,";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_time      = sysdate";
        whereStr = "where rowid    = ? ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyTime);
        setString(3, javaProgram);
        setRowId(4, hTnlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "", comcr.hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String src = String.format("%s/%s", root, moveFile);
//String target = String.format("%s/BACKUP/%s/%s.BAK", root, h_tnlg_notify_date, move_file);
        String target = String.format("%s/BACKUP/%s/%s", root, hTnlgNotifyDate, moveFile);

        if (debug == 1)
            showLogMessage("I", "", "MOVE_BACK=[" + src + "]" + target);

        comc.fileCopy(src, target);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchT002 proc = new IchT002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
