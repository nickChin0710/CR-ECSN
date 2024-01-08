/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  111/12/15  V1.00.00    Ryan                 program initial               *
*  112/02/27  V1.00.01    Sunny                fix chgData                   *
*  112/04/20  V1.00.02    Ryan     【指定參數日期 or執行日期 (如searchDate)】-1。                                                     *	
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

public class InfC048 extends AccessDAO {
	private final String progname = "產生送CRDB 48異動 CDESEG FOR拒絕行銷註記  112/04/20  V1.00.02";
	CommCrdRoutine comcr = null;
	CommCrd comc = new CommCrd();
	CommFunction comm = new CommFunction();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommDate comDate = new CommDate();
	String tempUser = "";
	String filePath1 = "";

	private final String filePathFromDb = "media/crdb";
	private final String fileName = "CRU23B1_TYPE_48_";

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

			List<ChgObj004> chgObjList = findChgObjList(searchDate);

			// (3) 產生檔案CRU23B1_TYPE_48_yyyymmdd.txt

			// get the fileFolderPath such as C:\EcsWeb\media\crdb
			String filePath = getFilePath(comc.getECSHOME(), filePathFromDb, fileName + searchDate + ".txt");
			filePath1 = fileName + searchDate + ".txt";

			// open CRU23B1_TYPE_48_yyyymmdd.txt file
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
	 * 一、建立C1，篩選CMS_CHGColumn_Log有異動拒絕行銷註記(market_agree_base)紀錄中
	 * 二、挑選C1符合名單中同一個ID_P_SEQNO最新一筆的資料，並取卡人基本資料檔的欄位
	 * 三、報送定義，Y = 拒絕行銷(0)，N = 同意行銷(1,2)。
	 * 
	 * @param searchDate
	 * @return ChgObjList: 有異動的資料
	 * @throws Exception
	 */
	private List<ChgObj004> findChgObjList(String searchDate) throws Exception {

		sqlCmd = " WITH CMS_CHGColumn_Log1 AS ";
		sqlCmd += " (SELECT ROW_NUMBER () OVER (PARTITION BY ID_P_SEQNO,CHG_COLUMN ORDER BY MOD_TIME DESC) AS SN, ";
		sqlCmd += " ID_P_SEQNO,DECODE(CHG_DATA,'0','Y','1','N','2','N','') AS CHG_DATA,DEBIT_FLAG,MOD_TIME,CHG_DATE FROM CMS_CHGColumn_Log ";
		sqlCmd += " WHERE CHG_COLUMN ='market_agree_base' AND DEBIT_FLAG='N' ";
		sqlCmd += " AND CHG_DATE = ? ) ";
		sqlCmd += " SELECT UF_IDNO_id(ID_P_SEQNO) as ID_NO,* FROM CMS_CHGColumn_Log1 C1 ";
		sqlCmd += " WHERE C1.SN=1 ";
		sqlCmd += " AND EXISTS (SELECT 1 FROM CRD_IDNO C2 WHERE C1.ID_P_SEQNO=C2.ID_P_SEQNO) ";
		setString(1, searchDate);
		int selectCount = selectTable();

		if (selectCount == 0) {
			return null;
		}

		List<ChgObj004> chgObjList = new LinkedList<ChgObj004>();
		ChgObj004 chgObj = null;

		for (int i = 0; i < selectCount; i++) {

			chgObj = new ChgObj004();
			chgObj.idNo = getValue("ID_NO", i);
			//chgObj.debitFlag = getValue("DEBIT_FLAG", i);
			chgObj.chgData = getValue("CHG_DATA", i);
			chgObj.chgDate = getValue("CHG_DATE", i);

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
	 * 產生output字串
	 * CRU23B1-48-CODE	代碼(值為48)	9(02)	1-2
       CRU23B1-48-CLASS	資料種類(值固定為A01)	X(03)	3-5
       CRU23B1-48-ID	主卡ID	X(10)	6-15
       CRU23B1-48-CRDXFLG	資料內容(Y/N/空白)拒絕行銷註記	X(01)	16 ==> 同意放Y(1,2)，不同意放N(0)
       CRU23B1-48-CRDXTXD	建檔日YYYMMDD即卡人建檔日期，需要轉民國年7碼，如日期原始資料為空白，則產生空白	X(07)	17-23 ==> 放異動日
       CRU23B1-48-CRDXBHI	分行代號	X(04)	24-27
       CRU23B1-48-CRDXTLI	櫃員代號	X(02)	28-29
       CRU23B1-48-CRDXQTD	徵提日YYYMMDD開戶時，「建檔日」與「徵提日」相同，之後異動「徵提日」	X(07)	30-36         ==> 放異動日
       CRU23B1-48-CRDXSEQ	序號(一律放00000)	X(05)	37-41
       FILLER	保留	X(109)	42-150
	 * @param chgObjList
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getOutputString(List<ChgObj004> chgObjList) throws UnsupportedEncodingException {
		byte[] lineSeparatorArr = { '\r', '\n' }; // 0D0A
		String nextLine = new String(lineSeparatorArr, "MS950");

		StringBuilder sb = new StringBuilder();

		for (ChgObj004 chgObj : chgObjList) {

			sb.append("48");
			sb.append("A01");
			sb.append(comc.fixLeft(chgObj.idNo, 10));
			//sb.append(comc.fixLeft(chgObj.debitFlag, 1));
			sb.append(comc.fixLeft(chgObj.chgData, 1));
			//sb.append(comc.fixLeft(zeroLeft(comDate.toTwDate(""), 7), 7));//建檔日無法確認定義，先塞空白
			sb.append(comc.fixLeft(zeroLeft(comDate.toTwDate(chgObj.chgDate), 7), 7));
			sb.append(comc.fixLeft("", 4)); //分行代號無法確認內容，先塞空白
			sb.append(comc.fixLeft("", 2)); //櫃員代號無法確認內容，先塞空白
			sb.append(comc.fixLeft(zeroLeft(comDate.toTwDate(chgObj.chgDate), 7), 7));
			sb.append("00000");
			sb.append(comc.fixLeft("", 109));

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
		InfC048 proc = new InfC048();
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

class ChgObj004 {
	String idNo = "";
	String chgDate = "";
	//String debitFlag = "";
	String chgData = "";
}
