/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/27  V1.00.01    Brian     error correction                          *
*  106/12/21  V1.01.02    詹曜維    RECS-s1061218-106 接收SHA檔                                 *
*  107/09/26  V1.02.00    David FU  RECS-s1061218-106 (JAVA)                  *
*  109-11-18  V1.02.01    tanwei    updated for project coding standard       *
*  111/07/21  V1.02.02    JeffKung  for TCB                                   *
*  112/08/17  V1.02.03    JeffKung  move backup to end of program             *
******************************************************************************/

package Tsc;

import java.nio.file.Paths;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommIps;

/*智慧卡公司檔案通知檔(TCRQ)FTP接收處理程式*/
public class TscT011 extends AccessDAO {

	private String progname = "智慧卡公司檔案通知檔(TCRQ)FTP接收處理程式  112/08/17 V1.02.03";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommIps comips = new CommIps();

	int debug = 0;

	String hCallBatchSeqno = "";
	String hTnlgNotifyDate = "";
	String hBusiBusinessDate = "";
	String hTempNotifyd = "";
	String hTempNotifyDate = "";
	String hTempNotifyTime = "";
	String hTfinFileIden = "";
	String hTfinDateType = "";
	String hTfinRunDay = "";
	String hTfinFileDesc = "";
	int hTfinRecordLength = 0;
	String hTnlgFileName = "";
	String hTnlgRespCode = "";
	int hTnlgRecordCnt = 0;
	String hTempFileName = "";
	String hTempNotifyDate2 = "";
	String hTnlgFileIden = "";
	int hCnt = 0;
	String hTempRowid = "";
	String hInt = "";
	String hTempSomeday = "";
	String hTnlgProcFlag = "";
	int hTnlgNotifySeq = 0;
	int hTnlgRecordSucc = 0;
	int hTnlgRecordFail = 0;

	int forceFlag = 0;
	int totalCnt = 0;
	String tmpstr = "";
	String tmpstr1 = "";
	String tmpstr2 = "";
	String temstr2 = "";
	String tmpstr3 = "";
	String fileSeq = "";
	String temstr1 = "";
	String hEriaLocalDir = "";
	String msgCode = "";
	String msgDesc = "";

	String hIdenShaFlag = "";
	
	String[] fileList = null;
	int fileCnt = 0;

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
				comc.errExit("Usage : TscT011 [[notify_date][force_flag]] [force_flag]", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			fileList = new String[100];

			hTnlgNotifyDate = "";
			forceFlag = 0;
			if (args.length == 1) {
				if ((args[0].length() == 1) && (args[0].equals("Y")))
					forceFlag = 1;
				if (args[0].length() == 8) {
					String sgArgs0 = "";
					sgArgs0 = args[0];
					sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
					hTnlgNotifyDate = sgArgs0;
				}
			}
			if (args.length == 2) {
				String sgArgs0 = "";
				sgArgs0 = args[0];
				sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
				hTnlgNotifyDate = sgArgs0;
				if (args[1].equals("Y"))
					forceFlag = 1;
			}

			//設定debug開關
			for (int argi = 0; argi < args.length; argi++) {
				if (args[argi].equals("debug")) {
					debug = 1;
				}
			}

			selectPtrBusinday();

			tmpstr1 = String.format("TCRQ.%8.8s.%8.8s15", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
			hTnlgFileName = tmpstr1;

			if (forceFlag == 0) {
				if (selectTscNotifyLoga() == 0) {
					comcr.errRtn(String.format("[%s]TSCC通知檔已經接收, 不可重複執行(error)", hTnlgFileName), "", hCallBatchSeqno);
				}
			}
			deleteTscNotifyLog();
			deleteTscNotifyLog1();

			getTcrqFile();
			insertTscNotifyLog();
			selectTscFileIden();
			
			if (debug == 1)
				showLogMessage("I", "", " 888 BEGIN");
			
			fileOpen1();
			showLogMessage("I", "", String.format("TSCC檔案 file_open_1"));
			
			fileOpen2();
			showLogMessage("I", "", String.format("TSCC檔案 file_open_2"));
			
			updateTscNotifyLog();
			showLogMessage("I", "", String.format("TSCC檔案 update TCRQ "));

			if (selectTscNotifyLog1() == 0) {
				updateTscNotifyLog3();

			} else {
				showLogMessage("I", "", String.format("TSCC檔案通知檔或傳送之資料檔異常(error), 請通知相關人員處理"));
			}

			String rootDir = String.format("%s/media/tsc", comc.getECSHOME());
			rootDir = Normalizer.normalize(rootDir, java.text.Normalizer.Form.NFKD);
			String fs = String.format("%s/%s", rootDir, hTnlgFileName);
			String ft = String.format("%s/backup/%s", rootDir, hTnlgFileName);
			showLogMessage("I", "", String.format("備份處理 [%s]\n", tmpstr1));
			if (comc.fileRename(fs, ft) == false)
				showLogMessage("I", "", String.format("move error[$s]", fs));

			//最後處理結束搬到backup
			moveBackup();
			
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
		sqlCmd = "select business_date,";
		sqlCmd += " decode( cast(? as varchar(8)), '', business_date, ?) h_tnlg_notify_date,";
		sqlCmd += " to_char(to_date(decode( cast(? as varchar(8)), '', to_char(sysdate,'yyyymmdd'), ?),'yyyymmdd'),'D') h_temp_notify_d,";
		sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_notify_date,";
		sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
		sqlCmd += " from ptr_businday  ";
		sqlCmd += "fetch first 1 rows only ";
		setString(1, hTnlgNotifyDate);
		setString(2, hTnlgNotifyDate);
		setString(3, hTnlgNotifyDate);
		setString(4, hTnlgNotifyDate);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
			hTnlgNotifyDate = getValue("h_tnlg_notify_date");
			hTempNotifyd = getValue("h_temp_notify_d");
			hTempNotifyDate = getValue("h_temp_notify_date");
			hTempNotifyTime = getValue("h_temp_notify_time");
		}

	}

	/***********************************************************************/
	int selectTscNotifyLoga() throws Exception {

		sqlCmd = "select 1  h_cnt ";
		sqlCmd += " from tsc_notify_log  ";
		sqlCmd += "where file_name  = ?  ";
		sqlCmd += "  and check_code = '0000' ";
		setString(1, hTnlgFileName);
		int recordCnt = selectTable();
		if (debug == 1)
			showLogMessage("I", "", " Log_a=[" + recordCnt + "]" + hTnlgFileName);
		if (recordCnt > 0) {
			hCnt = getValueInt("h_cnt");
		} else
			return (1);
		return (0);
	}

	/***********************************************************************/
	void deleteTscNotifyLog() throws Exception {
		daoTable = "tsc_notify_log";
		whereStr = "where notify_date = ?  ";
		whereStr += "  and file_iden   = 'TCRQ'  ";
		whereStr += "  and file_name   = ? ";
		setString(1, hTnlgNotifyDate);
		setString(2, hTnlgFileName);
		deleteTable();
	}

	/***********************************************************************/
	void deleteTscNotifyLog1() throws Exception {
		daoTable = "tsc_notify_log";
		whereStr = "where notify_date = ?  ";
		whereStr += "  and tran_type   = 'O' ";
		setString(1, hTnlgNotifyDate);
		deleteTable();
	}

	/***********************************************************************/
	void getTcrqFile() throws Exception {

		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "TSC_FTP_GET"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "TSC_FTP"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/tsc", comc.getECSHOME());
		commFTP.hEflgModPgm = this.getClass().getName();
		String hEflgRefIpCode = "TSC_FTP_GET";

		System.setProperty("user.dir", commFTP.hEriaLocalDir);

		String procCode = String.format("get %s", hTnlgFileName);
		showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載 3..");
		int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

		if (errCode != 0) {
			comcr.errRtn(
					String.format(String.format("[%s] => msg_code[%d] error\n", procCode, errCode), hTnlgFileName,
							errCode) + String.format("[%s]TSCC檔案通知檔接收有誤(error), 請通知相關人員處理", hTnlgFileName),
					"", hCallBatchSeqno);
		} else {
			
			fileList[fileCnt]=hTnlgFileName;
			fileCnt++;
			
			/*程式處理完畢再一起搬到backup
			String fs = Paths.get(commFTP.hEriaRemoteDir, hTnlgFileName).toString();
			String ft = Paths.get(commFTP.hEriaRemoteDir, "backup", hTnlgFileName).toString();
			String cmdStr = String.format("mv -i -f %s %s", fs, ft);

			showLogMessage("I", "", "備份遠端檔案: mv 檔案指令=" + cmdStr);

			if (comc.fileMove(fs, ft) == false) {
				showLogMessage("E", "", "ERROR : mv 檔案指令=" + cmdStr);
			}
			*/
		}
	}

	/***********************************************************************/
	void insertTscNotifyLog() throws Exception {
		setValue("file_iden", "TCRQ");
		setValue("tran_type", "R");
		setValue("file_name", hTnlgFileName);
		setValue("notify_date", hTnlgNotifyDate);
		setValue("notify_time", hTempNotifyTime);
		setValueInt("notify_seq", 15);
		setValue("ftp_receive_date", hTempNotifyDate);
		setValue("ftp_receive_time", hTempNotifyTime);
		setValue("check_code", "XXXX");
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		daoTable = "tsc_notify_log";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_tsc_notify_log duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void selectTscFileIden() throws Exception {
		sqlCmd = "select file_iden,";
		sqlCmd += " date_type,";
		sqlCmd += " run_day,";
		sqlCmd += " file_desc,";
		sqlCmd += " record_length ";
		sqlCmd += " from tsc_file_iden ";
		sqlCmd += "where tran_type = 'O' ";
		sqlCmd += "  and use_flag  = 'Y' ";
		int cursorIndex = openCursor();

		while (fetchTable(cursorIndex)) {
			hTfinFileIden = getValue("file_iden");
			hTfinDateType = getValue("date_type");
			hTfinRunDay = getValue("run_day");
			hTfinFileDesc = getValue("file_desc");
			hTfinRecordLength = getValueInt("record_length");

			for (int int1a = 0; int1a < hTfinDateType.length(); int1a++) {
				if (hTfinDateType.toCharArray()[int1a] == 'F') {
					if (!comc.getSubString(hTnlgNotifyDate, 6, 6 + 2)
							.equals(comc.getSubString(hTfinRunDay, int1a * 2, int1a * 2 + 2)))
						continue;
				}
				if (hTfinDateType.toCharArray()[int1a] == 'W') {
					tmpstr1 = comcr.increaseDays(hTnlgNotifyDate, -1);
					tmpstr2 = comcr.increaseDays(tmpstr1, 1);
					if (!hTnlgNotifyDate.equals(tmpstr2))
						continue;
				}
				if (hTfinDateType.toCharArray()[int1a] == 'K') {
					if (hTempNotifyd.toCharArray()[0] < hTfinRunDay.toCharArray()[int1a * 2 + 1])
						continue;
					selectSomeday(int1a * 2 + 2);
					tmpstr1 = comcr.increaseDays(hTempSomeday, -1);
					tmpstr2 = comcr.increaseDays(tmpstr1, 1);
					if (!hTnlgNotifyDate.equals(tmpstr2))
						continue;
				}
				if (hTfinDateType.toCharArray()[int1a] == 'H') {
					tmpstr1 = String.format("%6.6s%2.2s", hTnlgNotifyDate, hTfinRunDay + int1a * 2);
					tmpstr2 = comcr.increaseDays(tmpstr1, -1);
					tmpstr1 = comcr.increaseDays(tmpstr2, 1);
					if (!hTnlgNotifyDate.equals(tmpstr1))
						continue;
				}
				hTempNotifyDate2 = hTnlgNotifyDate;
				if (selectTscNotifyLogc() == 0)
					continue;
				insertTscNotifyLog1();
			}
		}
		closeCursor(cursorIndex);

	}

	/***********************************************************************/
	void selectSomeday(int hInt) throws Exception {
		sqlCmd = "select to_char(to_date(?, 'yyyymmdd')- to_number(?) days + to_number(?) days ,'yyyymmdd') h_temp_someday ";
		sqlCmd += " from DUAL ";
		setString(1, hTnlgNotifyDate);
		setString(2, hTempNotifyd);
		setString(3, comc.getSubString(hTfinRunDay, hInt - 1, 1).length() == 0 ? "0"
				: comc.getSubString(hTfinRunDay, hInt - 1, 1));
//        setInt(4, h_int);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_dual not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hTempSomeday = getValue("h_temp_someday");
		}

	}

	/***********************************************************************/
	int selectTscNotifyLogc() throws Exception {
		sqlCmd = "select rowid rowid ";
		sqlCmd += " from tsc_notify_log  ";
		sqlCmd += "where notify_date = ?  ";
		sqlCmd += " and file_iden   = ?  ";
		sqlCmd += "fetch first 1 rows only";
		setString(1, hTempNotifyDate2);
		setString(2, hTfinFileIden);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hTempRowid = getValue("rowid");
		} else
			return (1);
		return (0);
	}

	/***********************************************************************/
	void insertTscNotifyLog1() throws Exception {
		if (debug == 1) {
			showLogMessage("I", "", "  111 insert_tsc_notify_log_1=" + hTfinFileIden);
		}
		
		setValue("file_iden", hTfinFileIden);
		setValue("file_name", "");
		setValue("tran_type", "O");
		setValue("notify_date", hTempNotifyDate2);
		setValue("notify_time", hTempNotifyTime);
		setValue("check_code", "XXXX");
		setValue("perform_flag", "Y");
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		daoTable = "tsc_notify_log";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_tsc_notify_log duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void fileOpen1() throws Exception {
		int inta = 0, intb = 0, headTag = 0, tailTag = 0;
		String str600 = "";
		hTnlgRecordCnt = 0;
		tmpstr1 = String.format("TCRQ.%8.8s.%8.8s15", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
		temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		
		if (debug == 1) {
			showLogMessage("I", "", "888 Open 1 str=[" + temstr1 + "]"); 
		}
		
		int f = openInputText(temstr1);
		if (f == -1) {
			comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
		}

		int br = openInputText(temstr1, "MS950");
		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;
			inta++;
			
			if (debug == 1) {
				showLogMessage("I", "", " Read 1=[" + comc.getSubString(str600, 0, 51) + "]" + inta);
			}
			
			if (!comc.getSubString(str600, 0, 1).equals("H") && !comc.getSubString(str600, 0, 1).equals("D")
					&& !comc.getSubString(str600, 0, 1).equals("T")) {
				hTnlgRespCode = "0102"; /* FIRST_CHR_ERR */
				showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
				updateTscNotifyLog1();
				commitDataBase();
				comcr.errRtn("FIRST_CHR_ERR [0102] ", "", hCallBatchSeqno);
			}
			
			if ((!comc.getSubString(str600, 0, 1).equals("H")) && (inta == 1)) {
				hTnlgRespCode = "0112"; /* HEADER_NOT_FOUND */
				showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
				updateTscNotifyLog1();
				commitDataBase();
				comcr.errRtn("HEADER_NOT_FOUND [0112] !!", "", hCallBatchSeqno);
			}
			
			if (inta > 500000) {
				hTnlgRespCode = "0103"; /* RECORD_QTY_OVER */
				showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
				updateTscNotifyLog1();
				commitDataBase();
				comcr.errRtn("RECORD_QTY_OVER [0103] ", "", hCallBatchSeqno);
			}

			if (comc.getSubString(str600, 0, 1).equals("D"))
				intb++;
			if (comc.getSubString(str600, 0, 1).equals("H")) {
				if (headTag != 0) {
					hTnlgRespCode = "0111"; /* HEADER_DUPLICATE */
					showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
					updateTscNotifyLog1();
					commitDataBase();
					comcr.errRtn("HEADER_DUPLICATE [0111] ", "", hCallBatchSeqno);
				}
				
				headTag = 1;
				
				if (!"TCRQ".equals(comc.getSubString(str600, 1, 1 + 4))) {
					hTnlgRespCode = "0114"; /* HEADER_FILE_NAME_OVER */
					showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
					updateTscNotifyLog1();
					commitDataBase();
					comcr.errRtn("HEADER_FILE_NAME_OVER [0114] ", "", hCallBatchSeqno);
				}
				
				if (!comc.TSCC_BANK_ID8.substring(0, 8).equals(comc.getSubString(str600, 5, 5 + 8))) {
					hTnlgRespCode = "0117"; /* HEADER_BANK_NAME_OVER */
					showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
					updateTscNotifyLog1();
					commitDataBase();
					comcr.errRtn("HEADER_BANK_NAME_OVER [0117] ", "", hCallBatchSeqno);
				}
				
				if (!comc.commDateCheck(comc.getSubString(str600, 13))) {
					hTnlgRespCode = "0115"; /* HEADER_DATE_NAME_OVER */
					showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
					updateTscNotifyLog1();
					commitDataBase();
					comcr.errRtn("HEADER_DATE_NAME_OVER [0115] ", "", hCallBatchSeqno);
				}
				
				if (!comc.commTimeCheck(comc.getSubString(str600, 21))) {
					hTnlgRespCode = "0116"; /* HEADER_TIME_NAME_OVER */
					showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
					updateTscNotifyLog1();
					commitDataBase();
					comcr.errRtn("HEADER_TIME_NAME_OVER [0116] ", "", hCallBatchSeqno);
				}
			}
			
			if (comc.getSubString(str600, 0, 1).equals("T")) {
				if (tailTag != 0) {
					hTnlgRespCode = "0121"; /* TAILER_DUPLICATE */
					showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
					updateTscNotifyLog1();
					commitDataBase();
					comcr.errRtn("TAILER_DUPLICATE [0121] ", "", hCallBatchSeqno);
				}
				
				tailTag = 1;
				tmpstr3 = String.format("%8.8s", comc.getSubString(str600, 1));
				hTnlgRecordCnt = comcr.str2int(tmpstr3);
				
				if (comcr.str2long(tmpstr3) != intb) {
					hTnlgRespCode = "0124"; /* TOTAL_QTY_ERR */
					updateTscNotifyLog1();
					showLogMessage("I", "", "  err1=[" + hTnlgRespCode + "]" + inta);
					commitDataBase();
					comcr.errRtn("TOTAL_QTY_ERR [0124] ", "", hCallBatchSeqno);
				}
			}
		}

		if (debug == 1) {
			showLogMessage("I", "", "  1 end=[" + headTag + "]");
		}
		
		closeInputText(br);
		
		if (headTag != 1) {
			hTnlgRespCode = "0112"; /* HEADER_NOT_FOUND */
			updateTscNotifyLog1();
			commitDataBase();
			comcr.errRtn("HEADER_NOT_FOUND [0112] ", "", hCallBatchSeqno);
		}
		
		if (tailTag != 1) {
			hTnlgRespCode = "0120"; /* TAILER_NOT_FOUND */
			updateTscNotifyLog1();
			commitDataBase();
			comcr.errRtn("TAILER_NOT_FOUND [0120] ", "", hCallBatchSeqno);
		}
		
		hTnlgRespCode = "0000";
		updateTscNotifyLog1();
	}

	/***********************************************************************/
	void updateTscNotifyLog1() throws Exception {
		daoTable = " tsc_notify_log";
		updateSQL = " check_code  = ?,";
		updateSQL += " record_cnt  = ?,";
		updateSQL += " proc_flag   = '1',";
		updateSQL += " mod_pgm     = ?,";
		updateSQL += " mod_time    = sysdate ";
		whereStr = "where notify_date  = ?  ";
		whereStr += "  and notify_time  = ?  ";
		whereStr += "  and file_iden    = 'TCRQ' ";
		setString(1, hTnlgRespCode);
		setInt(2, hTnlgRecordCnt);
		setString(3, javaProgram);
		setString(4, hTnlgNotifyDate);
		setString(5, hTempNotifyTime);
		updateTable();
		
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void fileOpen2() throws Exception {
		String str600 = "";
		tmpstr1 = String.format("%s", hTnlgFileName);
		temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		
		if (debug == 1) {
			showLogMessage("I", "", "888 Open 2 str=[" + temstr1 + "]");
		}

		int fptr = openInputText(temstr1, "MS950");
		if (fptr == -1) {
			comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
		}
		while (true) {
			str600 = readTextFile(fptr);
			if (endFile[fptr].equals("Y"))
				break;
			if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
				continue;
			
			tmpstr1 = String.format("%24.24s", comc.getSubString(str600, 3));
			hTempFileName = tmpstr1;
			tmpstr1 = String.format("%4.4s", comc.getSubString(str600, 3));
			hTnlgFileIden = tmpstr1;
			tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 17));
			hTempNotifyDate2 = tmpstr1;
			tmpstr1 = String.format("%2.2s", comc.getSubString(str600, 25));
			hTnlgNotifySeq = comcr.str2int(tmpstr1);
			tmpstr1 = String.format("%1.1s", comc.getSubString(str600, 27));
			hTnlgProcFlag = tmpstr1;
			tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 28));
			hTnlgRecordSucc = comcr.str2int(tmpstr1);
			tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 36));
			hTnlgRecordFail = comcr.str2int(tmpstr1);
			tmpstr1 = String.format("%4.4s", comc.getSubString(str600, 60));
			hTnlgRespCode = tmpstr1;

			// ======================================================
			// FTP

			CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
			CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

			commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
			commFTP.hEflgSystemId = "TSC_FTP_GET"; /* 區分不同類的 FTP 檔案-大類 (必要) */
			commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
			commFTP.hEflgSourceFrom = "TSC_FTP"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
			commFTP.hEriaLocalDir = String.format("%s/media/tsc", comc.getECSHOME());
			commFTP.hEflgModPgm = this.getClass().getName();
			String hEflgRefIpCode = "TSC_FTP_GET";

			System.setProperty("user.dir", commFTP.hEriaLocalDir);

			String procCode = String.format("get %s", hTempFileName);
			showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載 2..");
			int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

			if (errCode != 0) {
				comcr.errRtn(String.format("[%s] => msg_code[%d] error\n", hEflgRefIpCode, errCode)
						+ String.format("檔案[%s]傳送失敗", hTempFileName), "", hCallBatchSeqno);
			} else {
				
				fileList[fileCnt]=hTempFileName;
				fileCnt++;
				
				/*程式全部處理完再一起搬到backup
				String fs = Paths.get(commFTP.hEriaRemoteDir, hTempFileName).toString();
				String ft = Paths.get(commFTP.hEriaRemoteDir, "backup", hTempFileName).toString();
				String cmdStr = String.format("mv -i -f %s %s", fs, ft);

				showLogMessage("I", "", "備份遠端檔案: mv 檔案指令=" + cmdStr);

				if (comc.fileMove(fs, ft) == false) {
					showLogMessage("E", "", "ERROR : mv 檔案指令=" + cmdStr);
				}
				*/
			}
			
			// ===================================================
			hIdenShaFlag = "";
			sqlCmd = "SELECT sha_flag ";
			sqlCmd += " FROM tsc_file_iden  ";
			sqlCmd += "WHERE file_iden   = ? ";
			setString(1, hTnlgFileIden);
			if (selectTable() > 0)
				hIdenShaFlag = getValue("sha_flag");

			if (hIdenShaFlag.equals("Y")) {
				procCode = String.format("get %s.SHA", hTempFileName);
				showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載 1..");

				errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

				if (errCode != 0) {
					comcr.errRtn(
							String.format("[%s] => msg_code[%d] error", hEflgRefIpCode, errCode)
									+ String.format("[%s]SHA檔接收有誤(error), 請通知相關人員處理", hTempFileName),
							"", hCallBatchSeqno);
				}

			}
			
			// ==================================================
			/*
			 * if (select_tsc_notify_log_b()!=0) {
			 * ECSprintf(stderr,"檔案[%s]非當日應接收檔案\n",h_temp_file_name.arr);
			 * delete_tsc_notify_log_2(); insert_tsc_notify_log_1(); }
			 */

			tmpstr1 = String.format("%s/media/tsc", comc.getECSHOME());
			tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
			tmpstr1 = String.format("%44.44s", str600);
			tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
			if (!comc.getSubString(str600, 44, 44 + 16).equals(comc.getSubString(tmpstr2, 0, 16))) {
				/*
				 * str2var(h_tnlg_resp_code , "A001"); file hash value error
				 * update_tsc_notify_log_2(); db_commit();
				 */
				showLogMessage("I", "", String.format("TSCC 檔案[%s] hash 值有錯!", hTempFileName));
				/*
				 * ECSprintf(stderr," hash[%16.16s]-[%s]\n",str600+44,tmpstr2); continue;
				 */
			}
			tmpstr1 = commTSCCFileCheck(hTempFileName, hTnlgFileIden, tmpstr1);
			hTnlgRespCode = tmpstr1;
			updateTscNotifyLog2();
			if (debug == 1)
				showLogMessage("I", "", "  222 update_tsc_notify_log_2 code=" + hTnlgRespCode);
			if (!hTnlgRespCode.equals("0000")) {
				commitDataBase();
				showLogMessage("I", "", String.format("TSCC file[%s] error_code[%s]!", hTempFileName, hTnlgRespCode));
			}
		}
		closeInputText(fptr);
	}

	/***********************************************************************/
	void updateTscNotifyLog2() throws Exception {
		if (debug == 1) {
			showLogMessage("I", "", "  222 update_tsc_notify_log_2=" + hTempFileName);
		}
		
		daoTable = " tsc_notify_log";
		updateSQL = " check_code         = ?,";
		updateSQL += " file_name          = ?,";
		updateSQL += " notify_seq         = ?,";
		updateSQL += " ftp_receive_date   = ?,";
		updateSQL += " ftp_receive_time   = ?,";
		updateSQL += " proc_flag          = '1',";
		updateSQL += " mod_pgm            = ?,";
		updateSQL += " mod_time           = sysdate";
		whereStr = "where notify_date   = ? ";
		whereStr += "  and file_iden     = ?  ";
		whereStr += "  and check_code    = 'XXXX' ";
		setString(1, hTnlgRespCode);
		setString(2, hTempFileName);
		setInt(3, hTnlgNotifySeq);
		setString(4, hTempNotifyDate);
		setString(5, hTempNotifyTime);
		setString(6, javaProgram);
		setString(7, hTempNotifyDate2);
		setString(8, hTnlgFileIden);

		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("收到TSCC傳送之資料檔[%s]非在定義時間送達(error), 請通知相關人員處理", hTempNotifyDate2));
			insertTscNotifyLog2();
		}
	}

	/***********************************************************************/
	void insertTscNotifyLog2() throws Exception {
		if (debug == 1) {
			showLogMessage("I", "", "  222 insert_tsc_notify_log_2 = " + hTfinFileIden);
		}
		
		setValue("file_iden", hTfinFileIden);
		setValue("tran_type", "O");
		setValue("notify_date", hTempNotifyDate2);
		setValue("notify_time", hTempNotifyTime);
		setValue("check_code", hTnlgRespCode);
		setValue("perform_flag", "Y");
		setValue("file_name", hTempFileName);
		setValueInt("notify_seq", hTnlgNotifySeq);
		setValue("ftp_receive_date", hTempNotifyDate);
		setValue("ftp_receive_time", hTempNotifyTime);
		setValue("proc_flag", "1");
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		daoTable = "tsc_notify_log";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_tsc_notify_log duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void updateTscNotifyLog() throws Exception {
		daoTable = " tsc_notify_log";
		updateSQL = " ftp_receive_date  = to_char(sysdate, 'yyyymmdd'),";
		updateSQL += " ftp_receive_time  = to_char(sysdate, 'hh24miss'),";
		updateSQL += " check_code        = '0000',";
		updateSQL += " proc_date         = to_char(sysdate, 'yyyymmdd'),";
		updateSQL += " proc_time         = to_char(sysdate, 'hh24miss'),";
		updateSQL += " proc_flag         = 'Y',";
		updateSQL += " mod_pgm           = ?,";
		updateSQL += " mod_time          = sysdate";
		whereStr = "where notify_date  = ?  ";
		whereStr += "  and file_iden    = 'TCRQ'  ";
		whereStr += "  and file_name    = ? ";
		setString(1, javaProgram);
		setString(2, hTnlgNotifyDate);
		setString(3, hTnlgFileName);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	int selectTscNotifyLog1() throws Exception {
		if (debug == 1) {
			showLogMessage("I", "", "  111 select_tsc_notify_log_1" + hTnlgNotifyDate + "," + hTempNotifyTime);
		}
		
		sqlCmd = "select count(*) h_cnt ";
		sqlCmd += " from tsc_notify_log  ";
		sqlCmd += "where notify_date  = ?  ";
		sqlCmd += "  and notify_time  = ?  ";
		sqlCmd += "  and check_code  != '0000' ";
		setString(1, hTnlgNotifyDate);
		setString(2, hTempNotifyTime);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_tsc_notify_log not found!", hTnlgNotifyDate, hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hCnt = getValueInt("h_cnt");
		}

		if (debug == 1) {
			showLogMessage("I", "", "  select_tsc_notify_log_1=[" + hCnt + "]");
		}
		
		return (hCnt);
	}

	/***********************************************************************/
	void updateTscNotifyLog3() throws Exception {
		if (debug == 1) {
			showLogMessage("I", "", "  333 update_tsc_notify_log_3");
		}
		
		daoTable = " tsc_notify_log";
		updateSQL = " perform_flag     = 'Y',";
		updateSQL += " proc_flag        = '1',";
		updateSQL += " mod_pgm          = ?,";
		updateSQL += " mod_time         = sysdate";
		whereStr = "where notify_date = ?  ";
		whereStr += " and notify_time  = ? ";
		setString(1, javaProgram);
		setString(2, hTnlgNotifyDate);
		setString(3, hTempNotifyTime);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	String commTSCCFileCheck(String tmpstr1, String fileIden, String returnCode) throws Exception {
		int int1 = 0;
		int inta = 0;
		int intb = 0;
		int headTag = 0;
		int tailTag = 0;
		String tmpstr4 = "";
		String str600 = "";
		returnCode = String.format("%4.4s", "0000");
		String fileName = String.format("%s", tmpstr1);
		int1 = commTSCCFileNameCheck(fileName, fileIden);
		if (int1 != 0) {
			returnCode = String.format("0104"); /* FILE_NAME_ERR */
			showLogMessage("I", "", String.format("0104 return_code[%d]", int1));
			return returnCode;
		}

		selectTscFileIden1(fileIden);
		temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);

		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

		int file = openInputText(temstr1);
		if (file == -1) {
			returnCode = String.format("0105"); /* FILE_NOT_FOUND */
			return returnCode;
		}
		closeInputText(file);

		if ((comc.getFileLength(temstr1) % hTfinRecordLength) != 0) {
			returnCode = String.format("0101"); /* FILE_SIZE_ERR */
			return returnCode;
		}

		int br = openInputText(temstr1, "MS950");
		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;
			tmpstr4 = String.format(str600);
			if (hTfinRecordLength - 18 >= 0) {
				if (tmpstr4.length() > hTfinRecordLength - 18 + 1)
					tmpstr4 = comc.getSubString(tmpstr4, 0, hTfinRecordLength - 18) + ""
							+ comc.getSubString(tmpstr4, hTfinRecordLength - 18 + 1);
				if (tmpstr4.length() == hTfinRecordLength - 18 + 1)
					tmpstr4 = comc.getSubString(tmpstr4, 0, hTfinRecordLength - 18) + "";
			}
			inta++;
			
			if (debug == 1) {
				
				showLogMessage("I", "",
						" Read File=[" + str600 + "]" + str600.getBytes("MS950").length + "," + hTfinRecordLength);
			}

			// big5 [恒]字為1byte
			if ((str600.getBytes("MS950").length + 2) != hTfinRecordLength) {
				returnCode = String.format("0106"); /* RECORD_LEN_ERR */
				break;
			}
			
			if (!comc.getSubString(str600, 0, 1).equals("H") && !comc.getSubString(str600, 0, 1).equals("D")
					&& !comc.getSubString(str600, 0, 1).equals("T")) {
				returnCode = String.format("0102"); /* FIRST_CHR_ERR */
				break;
			}
			
			if ((!comc.getSubString(str600, 0, 1).equals("H")) && (inta == 1)) {
				returnCode = String.format("0112"); /* HEADER_NOT_FOUND */
				break;
			}
			
			if (inta > 500000) {
				returnCode = String.format("0103"); /* RECORD_QTY_OVER */
				break;
			}

			if (comc.getSubString(str600, 0, 1).equals("D"))
				intb++;
			
			if (comc.getSubString(str600, 0, 1).equals("H")) {
				if (headTag != 0) {
					returnCode = String.format("0111"); /* HEADER_DUPLICATE */
					break;
				}
				headTag = 1;
				tmpstr1 = comc.subBIG5String(str600.getBytes("big5"), 0, hTfinRecordLength - 18);
				tmpstr3 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
				// tmpstr3 = new String(comips.COMM_hash_unpack(tmpstr4.getBytes()));
				if (!comc.getSubString(tmpstr3, 0, 16)
						.equals(comc.getSubString(str600, hTfinRecordLength - 18, hTfinRecordLength - 18 + 16))) {
					returnCode = String.format("0113"); /* HEADER_HASH_OVER */
					showLogMessage("I", "", String.format("Error 2=[0113] HEADER_HASH_OVER"));
					showLogMessage("I", "", "  1=[" + comc.getSubString(tmpstr3, 0, 16) + "]");
					showLogMessage("I", "", "  2=["
							+ comc.getSubString(str600, hTfinRecordLength - 18, hTfinRecordLength - 18 + 16) + "]");
					// break;
				}
				if (!comc.getSubString(fileIden, 0, 4).equals(comc.getSubString(str600, 1, 1 + 4))) {
					returnCode = String.format("0114"); /* HEADER_FILE_NAME_OVER */
					break;
				}
				if (!comc.TSCC_BANK_ID8.substring(0, 8).equals(comc.getSubString(str600, 5, 5 + 8))) {
					returnCode = String.format("0117"); /* HEADER_BANK_NAME_OVER */
					break;
				}
				if (!comc.commDateCheck(comc.getSubString(str600, 13))) {
					returnCode = String.format("0115"); /* HEADER_DATE_NAME_OVER */
					break;
				}
				if (!comc.commTimeCheck(comc.getSubString(str600, 21))) {
					returnCode = String.format("0116"); /* HEADER_TIME_NAME_OVER */
					break;
				}
			}
			if (comc.getSubString(str600, 0, 1).equals("T")) {
				if (tailTag != 0) {
					returnCode = String.format("0121"); /* TAILER_DUPLICATE */
					break;
				}
				tailTag = 1;
				tmpstr1 = comc.subBIG5String(str600.getBytes("big5"), 0, hTfinRecordLength - 18);
				tmpstr3 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
				// tmpstr3 = new String(comips.COMM_hash_unpack(tmpstr4.getBytes()));
				if (debug == 1) {
					showLogMessage("I", "", "  TTTT1=[" + comc.getSubString(tmpstr3, 0, 16) + "]");
					showLogMessage("I", "", "  TTTT2=["
							+ comc.getSubString(str600, hTfinRecordLength - 18, hTfinRecordLength - 18 + 16) + "]");
				}

				if (!comc.getSubString(tmpstr3, 0, 16)
						.equals(comc.getSubString(str600, hTfinRecordLength - 18, hTfinRecordLength - 18 + 16))) {

					returnCode = "0113";
					showLogMessage("I", "", String.format("Error 1=[0113] HEADER_HASH_OVER"));
					showLogMessage("I", "", "  1=[" + comc.getSubString(tmpstr3, 0, 16) + "]");
					showLogMessage("I", "", "  2=["
							+ comc.getSubString(str600, hTfinRecordLength - 18, hTfinRecordLength - 18 + 16) + "]");

				}
				tmpstr3 = String.format("%8.8s", comc.getSubString(str600, 1));
				if (comcr.str2long(tmpstr3) != intb) {
					returnCode = String.format("0124"); /* TOTAL_QTY_ERR */
					break;
				}
			}
		}
		
		closeInputText(br);
		
		if (debug == 1) {
			showLogMessage("I", "", "  888 END=[" + returnCode + "]" + headTag + "," + tailTag);
		}
		
		if (!returnCode.equals("0000"))
			return returnCode;

		if (headTag != 1) {
			returnCode = String.format("0112"); /* HEADER_NOT_FOUND */
			return returnCode;
		}
		
		if (tailTag != 1) {
			returnCode = String.format("0122"); /* TAILER_NOT_FOUND */
			return returnCode;
		}
		
		returnCode = String.format("0000");
		return returnCode;
	}

	/***********************************************************************/
	int commTSCCFileNameCheck(String fileName, String fileIden) throws Exception { /* 判別檔名是否依TSCC規定 */
		String tmpstr1;
		if ((fileName.toCharArray()[4] != '.') && (fileName.toCharArray()[13] != '.'))
			return (1);
		if (!comc.getSubString(fileName, 0, 4).equals(comc.getSubString(fileIden, 0, 4)))
			return (2);
		if (!comc.getSubString(fileName, 5, 5 + 8).equals(comc.TSCC_BANK_ID8.substring(0, 8)))
			return (3);
		tmpstr1 = String.format("%8.8s", comc.getSubString(fileName, 14));
		if (!comc.commDateCheck(tmpstr1))
			return (4);
		tmpstr1 = String.format("%2.2s", comc.getSubString(fileName, 22));
		if ((comcr.str2int(tmpstr1) < 0) || (comcr.str2int(tmpstr1) > 99))
			return (5);
		return (0);
	}

	/***********************************************************************/
	void selectTscFileIden1(String tmpstr1) throws Exception {
		hTfinFileIden = tmpstr1;
		hTfinRecordLength = 0;
		hTfinFileDesc = "";

		sqlCmd = "select record_length,";
		sqlCmd += " file_desc ";
		sqlCmd += " from tsc_file_iden  ";
		sqlCmd += "where file_iden = ? ";
		setString(1, hTfinFileIden);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_tsc_file_iden not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hTfinRecordLength = getValueInt("record_length");
			hTfinFileDesc = getValue("file_desc");
		}

	}
	
	/***********************************************************************/
    void moveBackup() throws Exception {
    	for (int i=0;i < fileCnt ; i++) {
    		
            String src = String.format("/crdataupload/%s", fileList[i]);
            String target = String.format("/crdataupload/backup/%s", fileList[i]);

            comc.fileMove(src, target);
    	}
    }

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		TscT011 proc = new TscT011();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
