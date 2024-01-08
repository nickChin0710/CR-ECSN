/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/02/18  V1.00.00  Brian       Program initial                           *
*  109-12-16  V1.00.01  tanwei      updated for project coding standard    *
*  112/08/01  V1.00.02  Wilson      FTP參數調整                                                                                            *
*  112/08/13  V1.00.03  Wilson      FTP分成兩次處理                                                                                     *
******************************************************************************/

package Ich;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class IchT014 extends AccessDAO {
    private String progname = "愛金卡會員銀行報表檔FTP接收處理程式  112/08/13 V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String hCallBatchSeqno = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String datFilePath = "";
    int forceFlag = 0;
    int totalCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String temstr2 = "";
    String fileSeq = "";
    String temstr1 = "";
    String msgCode = "";
    String hFDirectory = "";
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
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : IchT014 [notify_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] :
            // "";
            // comcr.callbatch(0, 0, 0);
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            showLogMessage("I", "", String.format("愛金卡會員銀行報表檔接收處理中.."));

            hTnlgNotifyDate = "";
            if (args.length == 1) {
                if (args[0].length() == 8)
                    hTnlgNotifyDate = args[0];
            }

            /*改成抓營業日
            sqlCmd = "select decode(cast(? as varchar(8)), '', to_char(sysdate,'yyyymmdd'), ?) h_tnlg_notify_date ";
            sqlCmd += " from dual";
            setString(1, hTnlgNotifyDate);
            setString(2, hTnlgNotifyDate);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hTnlgNotifyDate = getValue("h_tnlg_notify_date");
            }
            */
            
            selectPtrBusinday();
			if ("".equals(hTnlgNotifyDate)) {
				hTnlgNotifyDate = hBusiBusinessDate;
			}

            //openFile
            String fileFolder = Paths.get(comc.getECSHOME(), "reports/").toString();
            String fileName = String.format("report33.%s",hTnlgNotifyDate);
            datFilePath = Paths.get(fileFolder, fileName).toString();
    		boolean isOpen = openBinaryOutput(datFilePath);
    		if (isOpen == false) {
    			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
    			return -1;
    		}
            // ======================================================
            // FTP

            CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
            CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

//            commFTP.hEflgTransSeqno = String.format("%010.0f", (double) comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
//            commFTP.hEflgSystemId = "ICH_SFTP_OUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
//            commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
//            commFTP.hEflgSourceFrom = "ICAH_FTP"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
//            commFTP.hEriaLocalDir = comc.getECSHOME() + "/media/ich/";
//            commFTP.hEflgModPgm = this.getClass().getName();
//            String hEflgRefIpCode = "ICH_SFTP_OUT";
//
//            System.setProperty("user.dir", commFTP.hEriaLocalDir);
//
//           String procCode = String.format("get RPT_006_%s.zip", hTnlgNotifyDate);
//            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");
//
//            int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
//
//            if (errCode != 0) {
//                comcr.errRtn(String.format("[%s] => msg_code[%d] error\n", procCode, errCode)
//                        + String.format("愛金卡會員銀行報表檔接收有誤(error), 請通知相關人員處理"), "", hCallBatchSeqno);
//            }
//            /* unzip */
//            /*** PKZIP 解壓縮 ***/
//            selectEcsRefIpAddr(hEflgRefIpCode);
//            String zipFile = String.format("%sRPT_006_%s.zip", commFTP.hEriaLocalDir, hTnlgNotifyDate);
//            showLogMessage("I", "", "Unzip file=[" + hEriaUnzipHidewd + "]" + zipFile);
//            comm.unzipFile(zipFile, commFTP.hEriaLocalDir, hEriaUnzipHidewd);
//
//            comc.fileMove(String.format("%sRPT_006_%s.zip", commFTP.hEriaLocalDir, hTnlgNotifyDate),
//                    String.format("%s/BACKUP/%s/RPT_006_%s.zip", commFTP.hEriaLocalDir, hTnlgNotifyDate,
//                            hTnlgNotifyDate));
            // ======================================================
            // FTP

            commFTP.hEflgTransSeqno = String.format("%010.0f", (double) comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
            commFTP.hEflgSystemId = "ICH_REPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
            commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
            commFTP.hEflgSourceFrom = "ICH"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
            commFTP.hEriaLocalDir = comc.getECSHOME() + "/media/ich";
            commFTP.hEflgModPgm = this.getClass().getName();
            hEflgRefIpCode = "ICH_FTP_GET";

            System.setProperty("user.dir", commFTP.hEriaLocalDir);
            
            String procCode = String.format("get RPT_006_%s ", hTnlgNotifyDate);
            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

            int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

            if (errCode != 0) {
//                comcr.errRtn(String.format("愛金卡會員銀行報表檔傳送TCFS有誤(error), 請通知相關人員處理"), "", hCallBatchSeqno);
            }
            else {
            	selectEcsFtpLog(commFTP.hEflgTransSeqno,"0");
            }
            
            
            commFTP.hEflgTransSeqno = String.format("%010.0f", (double) comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
            commFTP.hEflgSystemId = "ICH_REPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
            commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
            commFTP.hEflgSourceFrom = "ICH"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
            commFTP.hEriaLocalDir = comc.getECSHOME() + "/media/ich";
            commFTP.hEflgModPgm = this.getClass().getName();
            hEflgRefIpCode = "ICH_FTP_GET";

            System.setProperty("user.dir", commFTP.hEriaLocalDir);
            
            procCode = String.format("mget R_IC-006*%s.txt ", hTnlgNotifyDate);
            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

            errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

            if (errCode != 0) {
//                comcr.errRtn(String.format("愛金卡會員銀行報表檔傳送TCFS有誤(error), 請通知相關人員處理"), "", hCallBatchSeqno);
            }
            else {
            	selectEcsFtpLog(commFTP.hEflgTransSeqno,"MergeFile");
            }
//            deleteFiles();
                                    
            //下傳報表檔
    		ftpMput(fileName);
    		
    		closeBinaryOutput();
            
            // ==============================================
            // 固定要做的
            // comcr.callbatch(1, 0, 0);
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
		sqlCmd = "select business_date ";
		sqlCmd += " from ptr_businday  ";
		sqlCmd += "fetch first 1 rows only ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
		}

	}

    /***********************************************************************/
//    private void deleteFiles() {
//        String folder = String.format("%s/media/ich/", comc.getECSHOME());
//        List<String> files = comc.listFS(folder, "", "");
//
//        for (String file : files) {
//            if ((comc.getSubString(file, 0, 8).equals("R_IC-006") && file.contains(hTnlgNotifyDate))
//                    || file.equals(String.format("RPT_006_%s", hTnlgNotifyDate))) {
//                comc.fileDelete(folder + file);
//            }
//        }
//    }

    /***********************************************************************/
//    void selectEcsRefIpAddr(String ipCode) throws Exception {
//        hEriaUnzipHidewd = "";
//
//        sqlCmd = "select FILE_ZIP_HIDEWD , FILE_UNZIP_HIDEWD";
//        sqlCmd += "  from ecs_ref_ip_addr ";
//        sqlCmd += " where ref_ip_code = ? ";
//        setString(1, ipCode);
//        int recordCnt = selectTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("select_ecs_ref_ip_addr not found!", ipCode, comcr.hCallBatchSeqno);
//        }
//        if (recordCnt > 0) {
//            hEriaUnzipHidewd = getValue("FILE_UNZIP_HIDEWD");
//        }
//    }

    /***********************************************************************/
    void selectEcsFtpLog(String tmpTransSeqno,String mergeFile) throws Exception {
    	String ftpFileName;
    	
		sqlCmd = " select file_name ";
		sqlCmd += " from ecs_ftp_log ";
		sqlCmd += " WHERE trans_seqno = ? ";
		sqlCmd += " order by file_name ";
		setString(1, tmpTransSeqno);
		int selectCnt = selectTable();
		for(int i =0; i<selectCnt;i++) {
			ftpFileName = getValue("file_name",i);
			
			if ("MergeFile".equals(mergeFile)) {
				comc.fileMerge(String.format("/cr/ecs/media/ich/%s", ftpFileName), datFilePath);
			}
			
			moveBackup(ftpFileName);
		}
    }
    
    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String src = String.format("/crdataupload/%s", moveFile);
        String target = String.format("/crdataupload/backup/%s", moveFile);

        comc.fileMove(src, target);
        
    }
    
    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "CREDITCARD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "CREDITCARD";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
        }
        return (0);
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchT014 proc = new IchT014();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
