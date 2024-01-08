/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/08/17  V1.00.01    JeffKung  program initial                           *
*                                                                             *
******************************************************************************/

package Tsc;

import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*悠遊卡會員銀行報表檔FTP接收處理程式*/
public class TscT014 extends AccessDAO {

	private String progname = "悠遊卡會員銀行報表檔FTP接收處理程式  112/08/17 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

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

//******************************************************
	public int mainProcess(String[] args) {
		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (args.length != 0 && args.length != 1) {
				comc.errExit("Usage : TscT014 [notify_date]", "");
			}

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			showLogMessage("I", "", String.format("悠遊卡會員銀行報表檔接收處理中.."));

			hTnlgNotifyDate = "";
			if (args.length == 1) {
				if (args[0].length() == 8)
					hTnlgNotifyDate = args[0];
			}

			selectPtrBusinday();
			if ("".equals(hTnlgNotifyDate)) {
				hTnlgNotifyDate = hBusiBusinessDate;
			}

			//openFile
            String fileFolder = Paths.get(comc.getECSHOME(), "reports/").toString();
            String fileName = String.format("report22.%s",hTnlgNotifyDate);
            datFilePath = Paths.get(fileFolder, fileName).toString();
    		boolean isOpen = openBinaryOutput(datFilePath);
    		if (isOpen == false) {
    			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
    			return -1;
    		}
			
    		//收檔
    		getTsccReport();
    		
    		//下傳報表檔
    		ftpMput(fileName);
    		
    		closeBinaryOutput();

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
	void getTsccReport() throws Exception {
		
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "TSCC_FTP_REPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "TSCC_FTP"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEriaLocalDir = "";
		commFTP.hEflgModPgm = this.getClass().getName();
		String hEflgRefIpCode = "TSC_FTP_GET";

		System.setProperty("user.dir", commFTP.hEriaLocalDir);

		String procCode = String.format("mget TSCCS*.%s", hTnlgNotifyDate);
		showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

		int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

		if (errCode != 0) {
			; // 沒有檔案不處理
		} else {
			selectEcsFtpLog(commFTP.hEflgTransSeqno);
		}
	}
	
	/***********************************************************************/
    void selectEcsFtpLog(String tmpTransSeqno) throws Exception {
    	String ftpFileName = "";
    	
		sqlCmd = " select file_name ";
		sqlCmd += " from ecs_ftp_log ";
		sqlCmd += " WHERE trans_seqno = ? ";
		sqlCmd += " order by file_name ";
		setString(1, tmpTransSeqno);
		int selectCnt = selectTable();
		for(int i =0; i<selectCnt;i++) {
			ftpFileName = getValue("file_name",i);
			
			comc.fileMerge(String.format("/cr/ecs/media/tsc/%s", ftpFileName), datFilePath);
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
		TscT014 proc = new TscT014();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
	/***********************************************************************/
}
