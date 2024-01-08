/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/12/27  V1.01.00  Lai         program initial                           *
 *  109-12-16   V1.01.01    tanwei      updated for project coding standard    *
 *  111/06/16  V1.01.02    Justin    弱點修正                                  *
 ******************************************************************************/

package Ich;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import org.apache.commons.io.FilenameUtils;

public class IchT011 extends AccessDAO {
    private String progname = "愛金卡檔案(AnnB)FTP接收處理  111/06/16 V1.01.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    String root = String.format("%s/media/ich", comc.getECSHOME());

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempNotifyD = "";
    String hTempNotifyDate = "";
    String hPreNotifyDate = "";
    String hTempNotifyTime = "";
    String hTfinFileIden = "";
    String hTfinDateType = "";
    String hTfinRunDay = "";
    String hTfinFileDesc = "";
    String hTfinRecordLength = "";
    int hCnt = 0;
    String hTempRowid = "";
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

    String temstr1 = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hEriaLocalDir = "";
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
    String hEflgRefIpCode = "";
    String hEriaUnzipHidewd = "";

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
                comc.errExit("Usage : IchT011 [[notify_date][force_flag]] [force_flag]", "");
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

            if (forceFlag == 0) {
                if (selectIchNotifyLogA() == 0) {
                    exceptExit = 0;
                    comcr.errRtn(String.format("[%s]ICH 通知檔已經接收, 不可重複執行(error)", hTempNotifyDate), "",
                            comcr.hCallBatchSeqno);
                }
            }

            showLogMessage("I", "",
                    String.format(" Process Date  =[%s][%d][%s]", hTnlgNotifyDate, forceFlag, hTempNotifyDate));

            //重覆執行時,先把當天的收檔記錄刪除
            deleteIchNotifyLog1();

            //在backup路徑下建立一個收檔日期的目錄
            mkdirForBackup();

            
            selectIchTotFile();

            fileOpen1();    /* update check_code */
            
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
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time, ";
        sqlCmd += "to_char(to_date(?, 'yyyymmdd' )- 1,'yyyymmdd') h_pre_notify_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        setString(3, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempNotifyD = getValue("h_temp_notify_d");
            hTempNotifyDate = getValue("h_temp_notify_date");
            hPreNotifyDate = getValue("h_pre_notify_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
        }

    }

    /***********************************************************************/
    int selectIchNotifyLogA() throws Exception {
        sqlCmd = "select 1 h_cnt";
        sqlCmd += "  from ich_notify_log    ";
        sqlCmd += " where tran_type   = 'O' ";
        sqlCmd += "   and notify_date = ?   ";
        sqlCmd += "   fetch first 1 rows only ";
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
    void deleteIchNotifyLog1() throws Exception {
        daoTable = "ich_notify_log";
        whereStr = "where notify_date = ?   ";
        whereStr += "  and tran_type   = 'O' ";
        setString(1, hTnlgNotifyDate);
        deleteTable();
    }

    /***********************************************************************/
    void mkdirForBackup() throws Exception {

        tmpstr1 = String.format("%s/backup/%s", root , hTnlgNotifyDate);
        tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
        
        comc.mkdirsFromFilenameWithPath(tmpstr1);

    }
    /***********************************************************************/
    int selectIchNotifyLogC() throws Exception {
        sqlCmd = "select rowid as rowid1";
        sqlCmd += "  from ich_notify_log  ";
        sqlCmd += " where notify_date = ?  ";
        sqlCmd += "   and file_iden   = ?  ";
        sqlCmd += "   fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
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

    /***********************************************************************/
    void selectIchTotFile() throws Exception {

        ftpGetRtn();

        selectEcsFtpLog();
    }

    /***********************************************************************/
    void ftpGetRtn() throws Exception {
        String fileSeq = "";

        TimeUnit.SECONDS.sleep(10);

        String tmpstr0 = root;
        tmpstr0 = Normalizer.normalize(tmpstr0, java.text.Normalizer.Form.NFKD);
        hEriaLocalDir = tmpstr0;

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        hEflgTransSeqno = String.format("%010.0f", (double) comcr.getModSeq());
        commFTP.hEflgTransSeqno = hEflgTransSeqno; /* 串聯 log 檔所使用 鍵值(必要) */
        commFTP.hEflgSystemId = "ICH_SFTP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "0000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "ICH"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = hEriaLocalDir;
        commFTP.hEflgModPgm = this.getClass().getName();
        hEflgRefIpCode = "ICH_FTP_GET";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        tmpstr2 = String.format("ARQB_%3.3s_%8.8s", comc.ICH_BANK_ID3, hTnlgNotifyDate);
        hTnlgFileName = tmpstr2;
        
        String procCode = String.format("get %s*", tmpstr2);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s] => msg_code[%d]", procCode, errCode));
            showLogMessage("I", "", String.format("[%s]ICH 今日無此檔案", hTnlgFileName));
            return;
        }

        String tmpstr3 = String.format("BRPA_%3.3s_%8.8s", comc.ICH_BANK_ID3, hTnlgNotifyDate);
        
        procCode = String.format("get %s*", tmpstr3);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

        errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s] => msg_code[%d]", procCode, errCode));
            showLogMessage("I", "", String.format("[%s]ICH 今日無此檔案", hTnlgFileName));
            return;
        }
        
        hTnlgRespCode = "0000";
        totCnt++;

        String temstr1 = String.format("%s/%s", root, hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int f = openInputText(temstr1);
        if (f == -1) {
            showLogMessage("I", "", temstr1 + " file not exists!");
            return;
        } else {
            insertIchNotifyLog1();

            /*
            tmpstr2 = String.format("%s/backup/%s/%s.BAK", root, hTnlgNotifyDate, hTnlgFileName);
            comc.fileCopy(temstr1, tmpstr2);

            // PKZIP 解壓縮 
            selectEcsRefIpAddr(hEflgRefIpCode);
            String zipFile = hEriaLocalDir + "/" + hTnlgFileName;
            showLogMessage("I", "", "Unzip file=[" + hEriaUnzipHidewd + "]" + zipFile);
            comm.unzipFile(zipFile, hEriaLocalDir, hEriaUnzipHidewd);
            */
        }
        closeInputText(f);

        //moveBackup(hTnlgFileName + ".FILEOK");
        

        /* get file by the biggest seqno */
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s", comc.ICH_BANK_ID3, hTnlgNotifyDate), 18, 20,""); /* 總檔 */
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s_A01B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s_A03B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s_A04B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s_A07B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s_A09B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s_A10B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s_A12B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s_A16B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("ARQB_%3.3s_%8.8s_A17B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("BRPA_%3.3s_%8.8s", comc.ICH_BANK_ID3, hTnlgNotifyDate), 18, 20,""); /* 總檔 */
        repalceFilesBySeqno(String.format("BRPA_%3.3s_%8.8s_A01B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        
        sqlCmd = "select ";
        sqlCmd += "file_name ";
        sqlCmd += "from ich_notify_log ";
        sqlCmd += "where file_iden   = 'B02B' ";
        sqlCmd += "  and notify_date = decode(cast(? as varchar(10)) , '', notify_date,?) ";
        setString(1, hPreNotifyDate);
        setString(2, hPreNotifyDate);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            String tempName = "";
            if (getValue("file_name", i).substring(0, 2).equals("FC")) {
                tempName = "FB" + getValue("file_name", i).substring(2, 13) + hTnlgNotifyDate;
            }
            repalceFilesBySeqno(tempName, 22, 24, "");
        }
        repalceFilesBySeqno(String.format("BRPA_%3.3s_%8.8s_A03B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("BRPA_%3.3s_%8.8s_A04B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("BRPA_%3.3s_%8.8s_A06B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("BRPA_%3.3s_%8.8s_A07B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("BRPA_%3.3s_%8.8s_A08B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("BRPA_%3.3s_%8.8s_A09B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");
        repalceFilesBySeqno(String.format("BRPA_%3.3s_%8.8s_A10B", comc.ICH_BANK_ID3, hTnlgNotifyDate), 23, 25, "");

    }

    /***********************************************************************/
    void repalceFilesBySeqno(String filename, int pos1, int pos2, String extension) {
        String folder = String.format("%s/media/ich/", comc.getECSHOME());
        int tempSeqno = 0;
        int currSeqno = 0;
        String tempFileNmae = "";
        List<String> files = comc.listFS(folder, "", "");

        for (String file : files) {
            tempFileNmae = file;
            if (tempFileNmae.contains(comc.getSubString(filename, 0, pos1 - 1))
                    && FilenameUtils.getExtension(tempFileNmae).toUpperCase(Locale.TAIWAN).equals(extension.toUpperCase(Locale.TAIWAN))) {
                tempSeqno = comc.str2int(comc.getSubString(tempFileNmae, pos1, pos2));

                if (tempSeqno > currSeqno) {
                    currSeqno = tempSeqno;
                    comc.fileDelete(folder + "/" + filename);
                    comc.fileMove(folder + "/" + tempFileNmae, folder + "/" + filename);
                }

            }
        }
    }

    /***********************************************************************/
    void insertIchNotifyLog1() throws Exception {
        showLogMessage("I", "", "file_iden = " + hTfinFileIden + ", h_tnlg_file_name = " + hTnlgFileName);
        setValue("file_iden", hTfinFileIden);
        setValue("tran_type", "O");
        setValue("notify_date", hTnlgNotifyDate);
        setValue("notify_time", hTempNotifyTime);
        setValue("check_code", hTnlgRespCode);
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

        sqlCmd = "select to_char(to_date(?,'yyyymmdd')- ?,'yyyymmdd') h_temp_someday, ";
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
    void selectEcsRefIpAddr(String ipCode) throws Exception {
        hEriaUnzipHidewd = "";

        sqlCmd = "select FILE_zip_hidewd , FILE_unzip_hidewd ";
        sqlCmd += "  from ecs_ref_ip_addr ";
        sqlCmd += " where ref_ip_code = ? ";
        setString(1, ipCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ecs_ref_ip_addr not found!", ipCode, comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hEriaUnzipHidewd = getValue("FILE_unzip_hidewd");
        }
    }

    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String src = String.format("%s/%s", root, moveFile);
        String target = String.format("%s/backup/%s/%s", root, hTnlgNotifyDate, moveFile);

        comc.fileRename(src, target);
    }

    /***********************************************************************/
    void fileOpen1() throws Exception {
        int inta = 0, intb = 0, headTag = 0, tailTag = 0;
        String str600 = "";
        hTnlgRecordCnt = 0;
        tmpstr1 = String.format("ARQB_%3.3s_%8.8s", comc.ICH_BANK_ID3, hTnlgNotifyDate);
        temstr1 = root + "/" + tmpstr1;
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int f = openInputText(temstr1);
        if (f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", comcr.hCallBatchSeqno);
        }
        closeInputText(f);

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y"))
                break;

            inta++;

            if (!comc.getSubString(str600, 0, 1).equals("H") && !comc.getSubString(str600, 0, 1).equals("D")) {
                hTnlgRespCode = "0102"; /* FIRST_CHR_ERR */
                updateIchNotifyLog1();
                commitDataBase();
                comcr.errRtn("FIRST_CHR_ERR", "", comcr.hCallBatchSeqno);
            }
            if ((comc.getSubString(str600, 0, 1).equals("H"))==false && (inta == 1)) {
                hTnlgRespCode = "0112"; /* HEADER_NOT_FOUND */
                updateIchNotifyLog1();
                commitDataBase();
                comcr.errRtn("HEADER_NOT_FOUND", "", comcr.hCallBatchSeqno);
            }
            if (inta > 500000) {
                hTnlgRespCode = "0103"; /* RECORD_QTY_OVER */
                updateIchNotifyLog1();
                commitDataBase();
                comcr.errRtn("RECORD_QTY_OVER", "", comcr.hCallBatchSeqno);
            }

            if (comc.getSubString(str600, 0, 1).equals("D"))
                intb++;
            if (comc.getSubString(str600, 0, 1).equals("H")) {
                if (headTag != 0) {
                    hTnlgRespCode = "0111"; /* HEADER_DUPLICATE */
                    updateIchNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("HEADER_DUPLICATE", "", comcr.hCallBatchSeqno);
                }
                headTag = 1;
                if (!"A99B".equals(comc.getSubString(str600, 1, 1 + 4))) {
                    hTnlgRespCode = "0114"; /* HEADER_FILE_NAME_OVER */
                    updateIchNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("", "", comcr.hCallBatchSeqno);
                }
                if (!comc.ICH_BANK_ID3.equals(comc.getSubString(str600, 11, 11 + 3))) {
                    hTnlgRespCode = "0117"; /* HEADER_BANK_NAME_OVER */
                    updateIchNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("HEADER_BANK_NAME_OVER", "", comcr.hCallBatchSeqno);
                }
                // if (!comc.COMM_date_check(comc.getSubString(str600, 13))) {
                // h_tnlg_resp_code = "0115"; /* HEADER_DATE_NAME_OVER */
                // update_ich_notify_log_1();
                // commitDataBase();
                // comcr.err_rtn("", "", comcr.h_call_batch_seqno);
                // }
                // if (!comc.COMM_time_check(comc.getSubString(str600, 21))) {
                // h_tnlg_resp_code = "0116"; /* HEADER_TIME_NAME_OVER */
                // update_ich_notify_log_1();
                // commitDataBase();
                // comcr.err_rtn("", "", comcr.h_call_batch_seqno);
                // }
            }
        }

        closeInputText(br);
        if (headTag != 1) {
            hTnlgRespCode = "0112"; /* HEADER_NOT_FOUND */
            updateIchNotifyLog1();
            commitDataBase();
            comcr.errRtn("HEADER_NOT_FOUND", "", comcr.hCallBatchSeqno);
        }
        hTnlgRespCode = "0000";
        updateIchNotifyLog1();
    }

    /***********************************************************************/
    void updateIchNotifyLog1() throws Exception {
        daoTable = " ich_notify_log";
        updateSQL = " check_code  = ?,";
        updateSQL += " proc_flag   = '1',";
        updateSQL += " mod_pgm     = ?,";
        updateSQL += " mod_time    = sysdate ";
        whereStr = "where notify_date = ?  ";
        whereStr += " and tran_type    = 'I' ";
        setString(1, hTnlgRespCode);
        setString(2, javaProgram);
        setString(3, hPreNotifyDate);
        updateTable();
        if (notFound.equals("Y")) {
        	showLogMessage("E", "", "update_ich_notify_log not found!");
            //comcr.errRtn("update_ich_notify_log not found!", "", comcr.hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchT011 proc = new IchT011();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
