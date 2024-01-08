/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/12/27  V1.01.00  Lai         program initial                           *
 *  109-12-16   V1.01.01    tanwei      updated for project coding standard    *
 ******************************************************************************/

package Ich;


import java.util.concurrent.TimeUnit;
import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class IchT012 extends AccessDAO {
    private String progname = "愛金卡檔案(AnnB)接收後當天的檔案處理(O) 109/12/16 V1.01.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;

    int debug = 0;
    String root = String.format("%s/media/ich", comc.getECSHOME());

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempNotifyD = "";
    String hTempNotifyDate = "";
    String hTempNotifyTime = "";
    String hTfinFileIden = "";
    String hTfinDateType = "";
    String hTfinRunDay = "";
    String hTfinFileDesc = "";
    String hTfinRecordLength = "";
    int hCnt = 0;
    String hTempRowid = "";
    String hTempNotifyDate2 = "";
    String hTnlgRespCode = "";
    String hTnlgNotifySeq = "";
    String hInt = "";
    String hTempSomeday = "";
    String hEflgTransSeqno = "";
    String hEflgFileName = "";
    String hEflgRowid = "";
    String hEflgFileDate = "";
    String hEflgProcCode = "";
    String hEflgProcDesc = "";
    String hEflgRefIpCode = "";
    String hEflgTransDesc = "檔案傳輸正常";

    String hEriaFtpType = "";
    String hEriaRefIp = "";
    String hEriaRefName = "";
    String hEriaUserId = "";
    String hEriaUserHidewd = "";
    String hEriaTransType = "";
    String hEriaRemoteDir = "";
    String hEriaLocalDir = "";
    String hEriaPortNo = "";
    String hEriaRefIpCode = "";
    String hEriaFileZipHidewd = "";
    String hEriaFileUnzipHidewd = "";

    String tmpstr1 = "";
    String tmpstr2 = "";
    String temstr1 = "";
    String fileSeq = "";
    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hTnlgFileName = "";
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
            // if (comm.isAppActive(javaProgram)) {
            // comc.err_exit("Error!! Someone is running this program now!!!", "Please wait
            // a moment to run again!!");
            // }
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IchT012 [[notify_date][force_flag]] [force_flag]", "");
            }
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

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

            if (forceFlag == 0) {
                if (selectIchNotifyLogA() == 0) {
                    exceptExit = 0;
                    comcr.errRtn(String.format("[%s]ICH 通知檔已經接收, 不可重複執行(error)", hTnlgNotifyDate), "",
                            comcr.hCallBatchSeqno);
                }
            }

            showLogMessage("I", "",
                    String.format(" Process Date  =[%s][%d][%s]", hTnlgNotifyDate, forceFlag, hTempNotifyDate));

            deleteIchNotifyLog();

            fileOpen();

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
        // sqlCmd += "to_char(DAYOFWEEK(to_date(?,'yyyymmdd'))) h_temp_notify_d,";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(8)) ,'',to_char(sysdate,'yyyymmdd'),cast(? as varchar(8)) ),'yyyymmdd'),'D') h_temp_notify_d,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_notify_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempNotifyD = getValue("h_temp_notify_d");
            hTempNotifyDate = getValue("h_temp_notify_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
        }

    }

    /***********************************************************************/
    int selectIchNotifyLogA() throws Exception {
        sqlCmd = "select 1 h_cnt";
        sqlCmd += "  from ich_notify_log    ";
        sqlCmd += " where tran_type   = 'O' ";
        sqlCmd += "   and file_iden  <> ''  "; // ex. not ARQB_017_20181227.zip
        sqlCmd += "   and notify_date = ?   ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return (1);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }
        return (0);
    }

    /***********************************************************************/
    void deleteIchNotifyLog() throws Exception {
        daoTable = "ich_notify_log";
        whereStr = "where notify_date       = ?   ";
        whereStr += "  and tran_type         = 'O' ";
        whereStr += "  and file_iden        <> ''  ";
        whereStr += "  and length(file_name) > 17 ";
        setString(1, hTnlgNotifyDate);
        deleteTable();

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";

        String tempFileName = String.format("ARQB_%3.3s_%8.8s", comc.ICH_BANK_ID3, hTnlgNotifyDate);
        temstr1 = String.format("%s/%s", root, tempFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

        int f = openInputText(temstr1);
        if (f == -1) {
            comcr.errRtn("File error  not found!", temstr1, comcr.hCallBatchSeqno);
        }

        if (debug == 1)
            showLogMessage("I", "", "Open Main=[" + temstr1 + "]");

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y"))
                break;

            if (str600.length() < 18) {
                continue;
            }
            if (!str600.substring(0, 1).equals("D"))
                continue;

            hTnlgFileName = str600.substring(1, 1 + 22);
            hTfinFileIden = str600.substring(19, 19 + 4);
            totCnt++;

            String temstr0 = String.format("%s/%s", root, hTnlgFileName);
            temstr0 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
            int f0 = openInputText(temstr0);
            if (f0 == -1) {
                comcr.errRtn("File loop error not found!", temstr0, comcr.hCallBatchSeqno);
            }
            closeInputText(f0);

            if (debug == 1)
                showLogMessage("I", "",
                        " Read file=[" + totCnt + "]" + hTfinFileIden + ",[" + hTnlgFileName + "]," + str600);

            selectIchFileIden();
        }
        closeInputText(br);

        moveBackup(tempFileName);
    }

    /***********************************************************************/
    void selectIchFileIden() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "file_iden,";
        sqlCmd += "date_type,";
        sqlCmd += "run_day,";
        sqlCmd += "file_desc,";
        sqlCmd += "record_length ";
        sqlCmd += " from ich_file_iden ";
        sqlCmd += "where tran_type = 'O' ";
        sqlCmd += "  and use_flag  = 'Y' ";
        sqlCmd += "  and file_iden = ?   ";
        setString(1, hTfinFileIden);
        openCursor();

        while (fetchTable()) {
            hTfinFileIden = getValue("file_iden");
            hTfinDateType = getValue("date_type");
            hTfinRunDay = getValue("run_day");
            hTfinFileDesc = getValue("file_desc");
            hTfinRecordLength = getValue("record_length");

            if (debug == 1)
                showLogMessage("I", "", " read iden=[" + hTfinFileIden + "]" + hTfinRecordLength);

            for (int int1a = 0; int1a < hTfinDateType.length(); int1a++) {

                hTempNotifyDate2 = hTnlgNotifyDate;
                if (selectIchNotifyLogC() == 0)
                    continue;

                insertIchNotifyLog1();

                //moveBackup(hTnlgFileName);

//       select_ecs_ftp_log();
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    int selectIchNotifyLogC() throws Exception {
        sqlCmd = "select rowid as rowid1";
        sqlCmd += "  from ich_notify_log  ";
        sqlCmd += " where notify_date = ?  ";
        sqlCmd += "   and  file_iden  = ?  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hTempNotifyDate2);
        setString(2, hTfinFileIden);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return (1);
        }
        if (recordCnt > 0) {
            hTempRowid = getValue("rowid1");
        }

        return 0;
    }

    // ************************************************************************
    void insertIchNotifyLog1() throws Exception {
        setValue("file_iden", hTfinFileIden);
        setValue("tran_type", "O");
        setValue("notify_date", hTempNotifyDate2);
        setValue("notify_time", hTempNotifyTime);
        setValue("check_code", "0000");
        setValue("perform_flag", "Y");
        setValue("file_name", hTnlgFileName);
        setValue("notify_seq", hTnlgNotifySeq);
        setValue("ftp_receive_date", hTempNotifyDate);
        setValue("ftp_receive_time", hTempNotifyTime);
        setValue("proc_flag", "1");
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ich_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", hTfinFileIden, comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectEcsFtpLog() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "file_name,";
        sqlCmd += "rowid as rowid1";
        sqlCmd += " from ecs_ftp_log ";
        sqlCmd += "where trans_seqno     = ? ";
        sqlCmd += "  and trans_resp_code = 'Y' ";
        setString(1, hEflgTransSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hEflgFileName = getValue("file_name", i);
            hEflgRowid = getValue("rowid1", i);
            hEflgProcCode = "0";

            hEflgProcDesc = "FTP檔案完成";
            updateEcsFtpLog();
        }

    }

    /***********************************************************************/
    void updateEcsFtpLog() throws Exception {
        daoTable = "ecs_ftp_log";
        updateSQL = " file_date = ?,";
        updateSQL += " proc_code = ?,";
        updateSQL += " proc_desc = ?,";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where rowid  = ? ";
        setString(1, hEflgFileDate);
        setString(2, hEflgProcCode);
        setString(3, hEflgProcDesc);
        setString(4, javaProgram);
        setRowId(5, hEflgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectSomeday(int hInt) throws Exception {
        int days = comcr.str2int(hTempNotifyD) - comcr.str2int(comc.getSubString(hTfinRunDay, hInt, 1));

        sqlCmd = "select to_char(to_date(?,'yyyymmdd')- ?,'yyyymmdd') ";
        sqlCmd += "  from DUAL ";
        setString(1, hTnlgNotifyDate);
        setInt(2, days);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_DUAL not found!", String.format("%d", days), comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempSomeday = getValue("h_temp_someday");
        }

    }

    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String src = String.format("%s/%s", root, moveFile);
        String target = String.format("%s/backup/%s/%s", root, hTnlgNotifyDate, moveFile);

        comc.fileRename(src, target);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchT012 proc = new IchT012();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
