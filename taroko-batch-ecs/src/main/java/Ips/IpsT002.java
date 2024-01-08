/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-15  V1.00.01    tanwei      updated for project coding standard     *
*  112/05/12  V1.00.02    Wilson    change to IPS_FTP_PUT                     *
*  112/05/17  V1.00.03    Wilson    procFTP調整                                                                                         *
*  112/07/31  V1.00.04    Wilson    增加FTP OK檔                                                                                         *
******************************************************************************/

package Ips;

import java.io.File;
import java.text.Normalizer;
import java.sql.Connection;

import org.apache.commons.io.FileUtils;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*會員銀行(B2I)媒體FTP傳送程式*/
public class IpsT002 extends AccessDAO {
    private String progname = "會員銀行(B2I)媒體FTP傳送程式  112/07/31 V1.00.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommRoutine    comr  = null;
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;

    int    debug = 1;
    String root    = String.format("%s/media/ich/", comc.getECSHOME());
    String stderr = "";

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTnlgNotifyTime = "";
    String hTnlgFtpSendDate = "";
    String hTnlgFileName = "";
    String hTnlgFileName1 = "";
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
                comc.errExit("Usage : IpsT002 [[notify_date][force_flag]] [force_flag]", "");
            }
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comr  = new CommRoutine(getDBconnect(), getDBalias());
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
                    comcr.errRtn("本日會員銀行通知檔媒體已處理完成, 不可再處理!(error)", "",comcr.hCallBatchSeqno);
                }
            }

            selectIpsNotifyLog1();

            hTnlgRecordCnt = totCnt;

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

        sqlCmd = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8)), '', business_date, ?) h_tnlg_notify_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_tnlg_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTnlgNotifyDate = getValue("h_tnlg_notify_date");
            hTnlgNotifyTime = getValue("h_tnlg_notify_time");
        }

    }

    /***********************************************************************/
    int selectIpsNotifyLog() throws Exception {
        sqlCmd = "select ftp_send_date ";
        sqlCmd += " from ips_notify_log  ";
        sqlCmd += "where notify_date = ?  ";
        sqlCmd += "and ftp_send_date <> ''  ";
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
        sqlCmd += "and ftp_send_date = '' ";
        setString(1, hTnlgNotifyDate);
        openCursor();
        while (fetchTable()) {
            hTnlgFileName = getValue("file_name");
            hTnlgRowid = getValue("rowid");

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            procFtp();
            renameFile1(hTnlgFileName);
            
            hTnlgFileName1 = String.format("%21.21s.zip.ok",comc.getSubString(hTnlgFileName, 0, 21));
            procFtp1();
            renameFile1(hTnlgFileName1);

            updateIpsNotifyLog();
            selectEcsFtpLog();
            commitDataBase();
            totCnt++;
        }
        closeCursor();
    }

    /***********************************************************************/
    void procFtp() throws Exception {
    	
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "IPS_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/ips", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "IPS_FTP"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
        
        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + hTnlgFileName + " 開始傳送....");
        int errCode = commFTP.ftplogName("IPS_FTP_PUT", "mput " + hTnlgFileName);
        
        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + hTnlgFileName + " 資料"+" errcode:"+errCode);
            insertEcsNotifyLog(hTnlgFileName);          
        }
    	
    }

    /***********************************************************************/
    void procFtp1() throws Exception {
    	
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "IPS_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/ips", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "IPS_FTP"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
        
        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + hTnlgFileName1 + " 開始傳送....");
        int errCode = commFTP.ftplogName("IPS_FTP_PUT", "mput " + hTnlgFileName1);
        
        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + hTnlgFileName1 + " 資料"+" errcode:"+errCode);
            insertEcsNotifyLog(hTnlgFileName1);          
        }
    	
    }

    /***********************************************************************/
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
  	void renameFile1(String removeFileName) throws Exception {
  		String tmpstr1 = comc.getECSHOME() + "/media/ips/" + removeFileName;
  		String tmpstr2 = comc.getECSHOME() + "/media/ips/backup/" + removeFileName;
  		
  		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
  			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
  			return;
  		}
  		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
  	}
  	
    /****************************************************************************/ 
    void updateIpsNotifyLog() throws Exception {
        daoTable = "ips_notify_log";
        updateSQL = "ftp_send_date = ?,";
        updateSQL += " ftp_send_time = ?,";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where rowid   = ? ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyTime);
        setString(3, javaProgram);
        setRowId(4, hTnlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_notify_log not found!", "",comcr.hCallBatchSeqno);
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

            tmpstr1 = String.format("%s/media/ips", comc.getECSHOME());

            tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);

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
            comcr.errRtn("update_ecs_ftp_log not found!", "",comcr.hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsT002 proc = new IpsT002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
