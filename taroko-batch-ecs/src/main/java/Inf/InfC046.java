/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  111/12/14  V1.00.00    Ryan                 program initial               *
*  112/04/20  V1.00.01   Ryan     【指定參數日期 or執行日期 (如searchDate)】-1。                                                     *	
*****************************************************************************/
package Inf;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommFunction;

import Dxc.Util.SecurityUtil;

public class InfC046 extends AccessDAO {
	private final String progname = "產生送CRDB 46異動 CRRBIRTH正卡人生日 112/04/20 V1.00.01";
	CommCrdRoutine comcr = null;
	CommCrd comc = new CommCrd();
	CommFunction comm = new CommFunction();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommDate comDate = new CommDate();
	String tempUser = "";
	String filePath1 = "";

	private final String filePathFromDb = "media/crdb";
	private final String fileName = "CRU23B1_TYPE_46_";

	public int mainProcess(String[] args) {
		String callBatchSeqno = "";

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			tempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, callBatchSeqno);
			// =====================================

			// 檢核參數的日期格式
			if (args.length >= 1) {
				showLogMessage("I", "", "PARM 1 : [無參數] 表示抓取businday ");
				showLogMessage("I", "", "PARM 1 : [SYSDAY] 表示抓取系統日");
				showLogMessage("I", "", "PARM 1 : [YYYYMMDD] 表示人工指定執行日");
			}

			if (args.length == 1) {
				if (!args[0].toUpperCase(Locale.TAIWAN).equals("SYSDAY")) {
					if (!new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
						showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
						return -1;
					}
				}
			}

			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			searchDate = getProgDate(searchDate, "D");
			
			//日期減一天
			searchDate = comDate.dateAdd(searchDate, 0, 0, -1);
			
			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

			List<ChgObj003> chgObjList = findChgObjList(searchDate);

			// (3) 產生檔案CRU23B1_TYPE_46_yyyymmdd.txt

			// get the fileFolderPath such as C:\EcsWeb\media\crdb
			String filePath = getFilePath(comc.getECSHOME(), filePathFromDb, fileName + searchDate + ".txt");
			filePath1 = fileName + searchDate + ".txt";

			// open CRU23B1_TYPE_46_yyyymmdd.txt file
			int outputFile = openOutputText(filePath);
			if (outputFile == -1) {
				showLogMessage("E", "", String.format("此路徑或檔案不存在: %s", fileName));
				return -1;
			}

			String outputString = "";
			if (chgObjList != null) {
				outputString = getOutputString(chgObjList);
			}

			boolean isWriteOk = writeTextFile(outputFile, outputString);
			if (!isWriteOk) {
				throw new Exception("writeTextFile Exception");
			}
			closeOutputText(outputFile);

			// (4) 將檔案PUT到/crdatacrea/NCR2TCB路徑下
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());
			procFTP();
			renameFile1(filePath1);

			showLogMessage("I", "", "執行結束");
			comcr.hCallErrorDesc = "程式執行結束";
			comcr.callbatchEnd();
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}

	/**
	 * 一、建立CMS_CHGColumn_Log1，主要是篩選CMS_CHGColumn_Log有異動birthday紀錄中
	 * 二、以CMS_CHGColumn_Log1別名C1為基礎，同一個ID_P_SEQNO最新一筆的資料，並確認ID_P_SEQNO於卡檔(CRD_CARD)中存在於持有正卡(sup_flag=0)
	 * 同時再檢核確認此ID_P_SEQNO存在於卡人檔之中
	 * 
	 * @param searchDate
	 * @return ChgObjList: 有異動的資料
	 * @throws Exception
	 */
	private List<ChgObj003> findChgObjList(String searchDate) throws Exception {

		sqlCmd = " WITH CMS_CHGColumn_Log1 AS ";
		sqlCmd += " (SELECT ROW_NUMBER () OVER (PARTITION BY ID_P_SEQNO,CHG_COLUMN ORDER BY MOD_TIME DESC) AS SN,ID_P_SEQNO,CHG_DATA ";
		sqlCmd += " FROM CMS_CHGColumn_Log ";
		sqlCmd += " WHERE CHG_COLUMN ='birthday' AND DEBIT_FLAG='N' ";
		sqlCmd += " AND CHG_DATE = ? ) ";
		sqlCmd += " SELECT UF_IDNO_id(ID_P_SEQNO) as id_no,* FROM CMS_CHGColumn_Log1 C1 ";
		sqlCmd += " WHERE C1.SN=1 ";
		sqlCmd += " AND EXISTS (SELECT 1 FROM CRD_CARD C2 WHERE C1.ID_P_SEQNO=C2.MAJOR_ID_P_SEQNO) ";
		sqlCmd += " AND EXISTS (SELECT 1 FROM CRD_IDNO C3 WHERE C1.ID_P_SEQNO=C3.ID_P_SEQNO) ";
		setString(1, searchDate);
		int selectCount = selectTable();

		if (selectCount == 0) {
			return null;
		}

		List<ChgObj003> chgObjList = new LinkedList<ChgObj003>();
		ChgObj003 chgObj = null;

		for (int i = 0; i < selectCount; i++) {

			chgObj = new ChgObj003();
			chgObj.idNo = getValue("ID_NO", i);
			chgObj.chgData = getValue("CHG_DATA", i);

			chgObjList.add(chgObj);
		}

		return chgObjList;
	}

	/**
	 * get file folder path by the project path and the file path selected from
	 * database
	 * 
	 * @param projectPath
	 * @param filePathFromDb
	 * @param fileNameAndTxt
	 * @return
	 * @throws Exception
	 */
	private String getFilePath(String projectPath, String filePathFromDb, String fileNameAndTxt) throws Exception {
		String fileFolderPath = null;

		if (filePathFromDb.isEmpty() || filePathFromDb == null) {
			throw new Exception("file path selected from database is error");
		}
		String[] arrFilePathFromDb = filePathFromDb.split("/");

		projectPath = SecurityUtil.verifyPath(projectPath);
		fileFolderPath = Paths.get(projectPath).toString();
		for (int i = 0; i < arrFilePathFromDb.length; i++) {
			fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();
		}

		fileFolderPath = SecurityUtil.verifyPath(fileFolderPath);
		fileNameAndTxt = SecurityUtil.verifyPath(fileNameAndTxt);
		return Paths.get(fileFolderPath, fileNameAndTxt).toString();
	}

	/**
	    *   產生output字串 
	 * CRU23B1-46-CODE 代碼(值為46) 9(02) 1-2 
	 * CRU23B1-46-CARD-NMBR 卡號(放空白)  9(16) 3-18 
	 * CRU23B1-46-ID-NMBR 主卡ID X(11) 19-29 
	 * CRU23B1-46-CRRBIRTH 生日YYYMMDD 9(07) 30-36 
	 * FILLER 保留 X(114) 37-150
	 * 
	 * @param chgObjList
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getOutputString(List<ChgObj003> chgObjList) throws UnsupportedEncodingException {
		byte[] lineSeparatorArr = { '\r', '\n' }; // 0D0A
		String nextLine = new String(lineSeparatorArr, "MS950");

		StringBuilder sb = new StringBuilder();

		for (ChgObj003 chgObj : chgObjList) {

			sb.append("46");
			sb.append(comc.fixLeft("", 16));
			sb.append(comc.fixLeft(chgObj.idNo, 11));
			sb.append(comc.fixLeft(zeroLeft(comDate.toTwDate(chgObj.chgData), 7), 7));
			sb.append(comc.fixLeft("", 114));

			sb.append(nextLine);
		}

		return sb.toString();
	}

	private String zeroLeft(String str, int length) {
		if (str == null) {
			str = "";
		}
		if (str.trim().length() == 0) {
			return str;
		}
		if (str.length() > length) {
			return str;
		}
		String pattern = "%" + length + "s";
		return String.format(pattern, str).replace(' ', '0');
	}

	public static void main(String[] args) {
		InfC046 proc = new InfC046();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

	/***********************************************************************/
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + filePath1 + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + filePath1);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + filePath1 + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(filePath1);
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
	void renameFile1(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/crdb/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/crdb/backup/" + removeFileName + "." + sysDate;

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

}

class ChgObj003 {
	String idNo = "";
	String chgData = "";
}
