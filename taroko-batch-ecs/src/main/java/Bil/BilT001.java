/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/05/22  V1.00.01  Jeff Kung   收檔測試                                   *
*  109-07-22    yanghan       修改了字段名称            *                                     
*  109-10-19  V1.00.07    shiyuqi       updated for project coding standard     *                                      
*  109-10-23  V1.00.08    JeffKung     change from delete to move
*  112-09-09  V1.00.09    JeffKung     能只取當日的檔案
******************************************************************************/
package Bil;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;
import java.text.Normalizer;

@SuppressWarnings("unchecked")
public class BilT001 extends AccessDAO
{
	private final String progname = "財金清算檔處理程式 112/09/09 V1.00.09";
	CommFunction comm = new CommFunction();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();

	String hBusiBusinessDate = "";
	String hFileDate = "";
	String hEflgFileName = "";
	String hEflgProcCode = "";
	String hEflgProcDesc = "";
	String hEflgRowid = "";
	String hEflgRefIpCode = "";
	String hMdzlParkVendor = "";
	String argFileStr = "";
	String argGroupId = "";

	long totalCnt = 0;

// ************************************************************************
	public static void main(String[] args) throws Exception {
		BilT001 proc = new BilT001();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			/*
			if (comm.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式已有另依程序啟動中, 不執行..");
				return (0);
			}
			*/

			if (args.length > 3 || args.length == 0) {
				showLogMessage("I", "", "請輸入參數:");
				showLogMessage("I", "", "PARM 1 : [file_name]");
				showLogMessage("I", "", "PARM 2 : [group_id]");
				showLogMessage("I", "", "PARM 3 : [yymmdd]");
				return (1);
			}

			argFileStr = args[0];
			argGroupId = args[1];

			if (!connectDataBase())
				return (1);

			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();

			if (args.length == 3) {
				if ("yymmdd".equals(args[2])) {
					int hYY= comc.str2int(comc.getSubString(hBusiBusinessDate,0,4)) - 1911;
					hFileDate = String.format("%02d%s",(hYY % 100),comc.getSubString(hBusiBusinessDate,4));
				} else {
					hFileDate = args[2];
				}
				showLogMessage("I", "", "檔案日期=["+hFileDate+"]");
			}

			showLogMessage("I", "", "開始FTP匯入檔案.....");

			ftpMgetFiles(hFileDate);

			selectEcsFtpLog();

			finalProcess();
			return (0);
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess
// ************************************************************************

	public void selectPtrBusinday() throws Exception {
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		int recordCnt = selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		}

		hBusiBusinessDate = getValue("business_date");
		showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "]");
	}

//************************************************************************ 
	public void ftpMgetFiles(String fileDate) throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "FISC_FTP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEflgGroupId = argGroupId; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "FISC_FCARD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEriaLocalDir = Normalizer.normalize(System.getenv("PROJ_HOME") + "/media/bil", Normalizer.Form.NFD);
		commFTP.hEflgModPgm = javaProgram;

		String fileStr = "";
		String ipStr = "BIL_FTP_GET";
		if ("".equals(fileDate)) {
			if (argFileStr.equalsIgnoreCase("INSTQQN")) {
				fileStr = argFileStr + "*";
			} else {
				fileStr = "F00600000." + argFileStr + "*";
			}
		} else {
			if (argFileStr.equalsIgnoreCase("INSTQQN")) {
				fileStr = argFileStr + comc.getSubString(fileDate,2) + "*";
			} else {
				fileStr = "F00600000." + argFileStr + "." + fileDate + "*";
			}
		}

		System.setProperty("user.dir", commFTP.hEriaLocalDir);

		showLogMessage("I", "", "get " + fileStr + " 開始接收....");
		int errCode = commFTP.ftplogName(ipStr, "mget " + fileStr);

		errCode = commFTP.fileList.size() == 0 ? 3 : errCode;
		if (errCode == 3)
			showLogMessage("I", "", String.format("無檔案可傳輸!"));

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法接收 " + fileStr + " 資料");
			insertEcsNotifyLog(fileStr);
		}

		totalCnt++;
		showLogMessage("I", "", "FTP完成.....");
	}

// ************************************************************************
	public void selectEcsFtpLog() throws Exception {
		selectSQL = "FILE_NAME, " + "REF_IP_CODE, " + "ROWID as rowid";
		daoTable = "ECS_FTP_LOG";
		whereStr = "WHERE trans_seqno   = ? " + "AND   trans_resp_code = 'Y' " + "";

		setString(1, commFTP.hEflgTransSeqno);

		openCursor();

		while (fetchTable()) {
			hEflgFileName = getValue("FILE_NAME");

			hEflgRefIpCode = getValue("REF_IP_CODE");
			hEflgRowid = getValue("ROWID");
			hEflgProcCode = "0";
			hEflgProcDesc = "";
			
            String cmdStr = String.format("mv -i -f %s/%s %s/backup/%s", commFTP.hEriaRemoteDir,
                    hEflgFileName, commFTP.hEriaRemoteDir, hEflgFileName);
            String fs = String.format("%s/%s", commFTP.hEriaRemoteDir, hEflgFileName);
            String ft = String.format("%s/backup/%s", commFTP.hEriaRemoteDir, hEflgFileName);

            showLogMessage("I", "", "備份遠端檔案: mv 檔案=" + cmdStr);

            if (comc.fileMove(fs, ft) == false) {
                showLogMessage("I", "", "ERROR : mv 檔案=" + cmdStr);
                hEflgProcCode = "B";
                hEflgProcDesc = "備份遠端檔案[" + hEflgFileName + "%]失敗";
                updateEcsFtpLog();

                continue;
            }
            showLogMessage("I", "", "備份遠端檔案[" + hEflgFileName + "]完成.....");

            hEflgProcDesc = "+備份遠端檔案完成";
			updateEcsFtpLog();
//
//			showLogMessage("I", "", "刪除檔案[" + hEflgFileName + "]");
//			int errCode = commFTP.ftplogName(hEflgRefIpCode, "delete " + hEflgFileName);
//
//			if (errCode != 0) {
//				showLogMessage("I", "", "ERROR:刪除檔案[" + hEflgFileName + "%]失敗.....");
//				hEflgProcCode = "B";
//				hEflgProcDesc = "刪除檔案[" + hEflgFileName + "%]失敗";
//				updateEcsFtpLog();
//				continue;
//			}
//			showLogMessage("I", "", "刪除檔案[" + hEflgFileName + "]完成.....");
//
//			hEflgProcDesc = "+刪除檔案完成";
//			updateEcsFtpLog();
		}
	}

// ************************************************************************
	public void updateEcsFtpLog() throws Exception {
		dateTime();
		updateSQL = "file_date  = ?, " + "proc_code  = ?, " + "trans_desc = trans_desc||?, " + "proc_desc  = ?, "
				+ "mod_pgm    = ?, " + "mod_time   = timestamp_format(?,'yyyymmddhh24miss')";
		daoTable = "ecs_ftp_log";
		whereStr = "WHERE rowid = ?";

		setString(1, hBusiBusinessDate);
		setString(2, hEflgProcCode);
		setString(3, hEflgProcDesc);
		setString(4, hEflgProcDesc);
		setString(5, javaProgram);
		setString(6, sysDate + sysTime);
		setRowId(7, hEflgRowid);

		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "UPDATE ecs_ftp_log error " + hEflgRowid);
			exitProgram(0);
		}
		return;
	}

// ************************************************************************
	public int insertEcsNotifyLog(String fileName) throws Exception {
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("unit_code", comr.getObjectOwner("3", javaProgram));
		setValue("obj_type", "3");
		setValue("notify_head", "無法 FTP 接收 " + fileName + " 資料");
		setValue("notify_name", "媒體檔名:" + fileName);
		setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 接收 " + fileName + " 資料");
		setValue("notify_desc2", "");
		setValue("trans_seqno", commFTP.hEflgTransSeqno);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "ecs_notify_log";

		insertTable();

		return (0);
	}
// ************************************************************************

} // End of class FetchSample
