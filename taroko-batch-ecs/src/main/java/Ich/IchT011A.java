/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/12/27  V1.01.00    Lai       program initial                           *
 *  109-12-16  V1.01.01    tanwei    updated for project coding standard       *
 *  112/06/06  V1.01.02    Justin    弱點修正                                                                                                     *
 *  112/07/31  V1.01.03    JeffKung  ARQB留在media/ich目錄下                                                              *
 *  112/12/19  V1.01.04    Wilson    傳送.rpt檔給卡部                                                      
 *  113/01/04  V1.01.05    JeffKung  tran_type分類                                                     *
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

public class IchT011A extends AccessDAO {
    private String progname = "愛金卡檔案(ARQB_A99B)FTP接收處理  113/01/04 V1.01.05";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommRoutine    comr = null;
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;

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

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IchT011 [[notify_date][force_flag]] [force_flag]", "");
            }

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
                if (selectIchNotifyLogA() == 0) {
                    exceptExit = 0;
                    comcr.errRtn(String.format("[%s]ICH 通知檔已經接收, 不可重複執行(error)", hTnlgNotifyDate), "",
                            comcr.hCallBatchSeqno);
                }
            }

            showLogMessage("I", "",
                    String.format(" Process Date  =[處理日:%s][%d][系統日:%s]", hTnlgNotifyDate, forceFlag, hTempNotifyDate));

            //重覆執行時,先把當天的收檔記錄刪除
            deleteIchNotifyLog1();

            //在backup路徑下建立一個收檔日期的目錄
            mkdirForBackup();

            //A099收檔
            int fileExist = ftpGetA099();  //0:有收到檔案; 1:收檔失敗
            if (fileExist==1) {
            	return 1;
            }

            //處理A099檔案中所有的檔案
            processA099File();    
            
            showLogMessage("I", "", "程式執行結束,筆數=[" + totCnt + "]");

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

        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hPreNotifyDate = comm.nextNDate(hTnlgNotifyDate, -1);
        
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
    
    int ftpGetA099() throws Exception {
    	
        String fileSeq = "";
        hEriaLocalDir = root;

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
        
        hTnlgFileName = String.format("ARQB_%3.3s_%8.8s", comc.ICH_BANK_ID3, hTnlgNotifyDate);
        
        String procCode = String.format("get %s", hTnlgFileName);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s] => msg_code[%d]", procCode, errCode));
            showLogMessage("I", "", String.format("[%s]ICH 今日無此檔案", hTnlgFileName));
            return 1;
        }

        moveBackup(hTnlgFileName);
        
        hTfinFileIden = "";
        hTnlgRespCode = "0000";
        insertIchNotifyLog1();
    	
    	return 0;
    	
    }

    /***********************************************************************/
    void insertIchNotifyLog1() throws Exception {
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
            showLogMessage("E","","insert_" + daoTable + " duplicate! ,[" + hTfinFileIden + "]");
        }
    }

    /***********************************************************************/
    void processA099File() throws Exception {
        int inta = 0, intb = 0, headTag = 0, tailTag = 0;
        String str600 = "";
        hTnlgRecordCnt = 0;
        String fileName = root + "/" + hTnlgFileName;
        String arqbName = hTnlgFileName;

        int br = openInputText(fileName, "MS950");
        if (br == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
            //當掉,程式會直接結束
        }

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
            if (inta > 50) {
                hTnlgRespCode = "0103"; /* RECORD_QTY_OVER */
                updateIchNotifyLog1();
                commitDataBase();
                comcr.errRtn("RECORD_QTY_OVER", "", comcr.hCallBatchSeqno);
            }
            
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
                    comcr.errRtn("HEADER_FILE_IDEN_ERROR", "", comcr.hCallBatchSeqno);
                }
                if (!comc.ICH_BANK_ID3.equals(comc.getSubString(str600, 11, 11 + 3))) {
                    hTnlgRespCode = "0117"; /* HEADER_BANK_NAME_OVER */
                    updateIchNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("HEADER_BANK_NAME_OVER", "", comcr.hCallBatchSeqno);
                }
            }

            if (comc.getSubString(str600, 0, 1).equals("D")) {
            	intb++;
            	hTnlgFileName = str600.substring(1, 1 + 22);
                hTfinFileIden = str600.substring(19, 19 + 4);
                if ("FB".equals(str600.substring(1, 1 + 2))) {
                	hTfinFileIden = "B02B";
                }
                if (ftpGetFile()==0) {
                    hTnlgRespCode = "0000";
                    if ("ARQB".equals(comc.getSubString(hTnlgFileName, 0,4))) {
                    	insertIchNotifyLog1();
                    } else {
                    	;
                    	//updateIchNotifyLog1();   //非ARQB不需要insert
                    }
                    
                }
            }
        }

        closeInputText(br);
        
        //只有空檔有可能會符合這個條件
        if (headTag != 1) {
            hTnlgRespCode = "0112"; /* HEADER_NOT_FOUND */
            updateIchNotifyLog1();
            commitDataBase();
            comcr.errRtn("HEADER_NOT_FOUND", "", comcr.hCallBatchSeqno);
        }
        
        hTnlgRespCode = "0000";
        updateIchNotifyLog1();

        moveBackupA099(arqbName);
    }

    int ftpGetFile() throws Exception {
    	
        String fileSeq = "";
        hEriaLocalDir = root;

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
        
        String procCode = String.format("get %s", hTnlgFileName);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s] => msg_code[%d]", procCode, errCode));
            showLogMessage("I", "", String.format("[%s]ICH 今日無此檔案", hTnlgFileName));
            return 1;
        } 
        
        if(comc.getSubString(hTnlgFileName,0,5).equals("BRPA_") || comc.getSubString(hTnlgFileName,0,3).equals("FB_")) {
        	procFTP(hTnlgFileName);
        }
        
        moveBackup(hTnlgFileName);
    	
    	return 0;
    	
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
    void procFTP(String rptFileName) throws Exception {
    	
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
    	
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/ich", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;

        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + rptFileName + " 開始傳送....");
        int errCode = commFTP.ftplogName("CREDITCARD", "mput " + rptFileName);

        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + rptFileName + " 資料" + " errcode:" + errCode);
            insertEcsNotifyLog(rptFileName);
        }
    }

    /****************************************************************************/
    public int insertEcsNotifyLog(String fileName) throws Exception {
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("unit_code", comr.getObjectOwner("3", javaProgram));
        setValue("obj_type", "3");
        setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
        setValue("notify_name", "媒體檔名:" + fileName);
        setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
        setValue("notify_desc2", "");
        setValue("trans_seqno", commFTP.hEflgTransSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ecs_notify_log";

        insertTable();

        return (0);
    }

    /****************************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String src = String.format("/crdataupload/%s", moveFile);
        String target = String.format("/crdataupload/backup/%s", moveFile);

        comc.fileRename(src, target);
        
    }
    
    /***********************************************************************/
    void moveBackupA099(String moveFile) throws Exception {
    	
    	//ARQB
        String src = String.format("/crdataupload/%s", moveFile);
        String target = String.format("/crdataupload/backup/%s", moveFile);

        comc.fileRename(src, target);
        
        //ARQB (回覆的程式還需要使用, 所以這裡不用搬走 2023/07/31)
        String root = String.format("%s/media/ich", comc.getECSHOME());
        src = String.format("%s/%s", root, moveFile);
        target = String.format("%s/backup/%s/%s", root, hTnlgNotifyDate, moveFile);

        //comc.fileRename(src, target);
        
        //BRPA
        String brpaFile = String.format("BRPA_%3.3s_%8.8s", comc.ICH_BANK_ID3, hTnlgNotifyDate);
        src = String.format("%s/%s", root, brpaFile);
        target = String.format("%s/backup/%s/%s", root, hTnlgNotifyDate, brpaFile);

        comc.fileRename(src, target);
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchT011A proc = new IchT011A();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
