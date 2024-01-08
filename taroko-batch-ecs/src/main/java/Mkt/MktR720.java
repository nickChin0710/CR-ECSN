/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/07/25  V1.00.00   JeffKung     program initial                        *
*****************************************************************************/
package Mkt;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxInf;

public class MktR720 extends AccessDAO {
	private final String progname = "產生理專轉介白金信用卡開卡業績作業程式  112/07/25 V1.00.01";
	private static final String MKT_FOLDER = "media/mkt/";
	private static final String DATA_FORM = "PPCARD";
	private final String lineSeparator = "\r\n";

	CommCrd comc = new CommCrd();
	CommFunction comm = new CommFunction();
	CommCrdRoutine comcr = null;
	CommRoutine comr = null;
	
	String startMonthDate = "";
	String endMonthDate = "";


	public int mainProcess(String[] args) {

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

			comr = new CommRoutine(getDBconnect(), getDBalias());
			// =====================================

			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			searchDate = getProgDate(searchDate, "D");
			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

			String runDate = "";
			String tmpRunDate = comc.getSubString(searchDate, 0, 6) + "02";
			startMonthDate = comm.lastMonth(searchDate, 3)+"01";
			endMonthDate = comm.lastMonth(searchDate, 1)+"31";

			/*
			if ("01".equals(comc.getSubString(searchDate, 4, 6)) || "04".equals(comc.getSubString(searchDate, 4, 6))
					|| "07".equals(comc.getSubString(searchDate, 4, 6))
					|| "11".equals(comc.getSubString(searchDate, 4, 6))) {

				// return false(工作日), true(假日) 
				if (checkWorkDate(tmpRunDate)) {
					comr.increaseDays(tmpRunDate, 1);
					runDate = comr.increaseNewDate;
				} else {
					runDate = tmpRunDate;
				}
			}
		    */

			// return false(工作日), true(假日) 
			if (checkWorkDate(tmpRunDate)) {
				comr.increaseDays(tmpRunDate, 1);
				runDate = comr.increaseNewDate;
			} else {
				runDate = tmpRunDate;
			}
			
			// 每月2日(假日順延)執行此程式
			if (!searchDate.equals(runDate)) {
				showLogMessage("I", "", String.format("每月2日(假日順延)執行此程式,今日[%s]不需執行!!", searchDate));
				return 0;
			}

			showLogMessage("I", "", String.format("開戶月份區間[%s~%s]", startMonthDate, endMonthDate));

			int procResult = fileOpen(searchDate);
			if (procResult != 0) {
				showLogMessage("E", "", "====執行有誤====");
			} else {
				showLogMessage("I", "", "====執行結束====");
			}
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}

	/***********************************************************************/
	int fileOpen(String searchDate) throws Exception {
		int totalCnt = 0;
		int rowCount = 0;
		String str600 = "";
		String fileNameSearchDate = searchDate.substring(2);
		String inputFileName = String.format("%s/media/mkt/WMS_RM_LIST.TXT", comc.getECSHOME());
		int br = openInputText(inputFileName, "MS950");
		if (br == -1) {
			showLogMessage("I", "", String.format("本日無上傳檔需處理,[%s]", inputFileName));
			return -1;
		}

		// get the name and the path of the .DAT file
		String datFileName = String.format("%s_%s.TXT", DATA_FORM, searchDate);
		String fileFolder = Paths.get(comc.getECSHOME(), MKT_FOLDER).toString();
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}

		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;

			if (str600.length() < 10)
				continue; // ID傳入不滿10碼,跳過

			byte[] bytes = str600.getBytes("MS950");
			String promoteEmpId = comc.subMS950String(bytes, 0, 10).trim();

			totalCnt++;

			// 抓取消費資料產檔

			sqlCmd  = "SELECT ";
			sqlCmd += "A.CARD_NO,A.ACTIVATE_DATE,A.ACTIVATE_FLAG,A.CURRENT_CODE,A.PROMOTE_EMP_NO, ";
			sqlCmd += "A.ORI_ISSUE_DATE,A.ISSUE_DATE,A.GROUP_CODE,B.GROUP_NAME,A.CARD_NOTE ";
			sqlCmd += "FROM CRD_CARD A, PTR_GROUP_CODE B ";
			sqlCmd += "WHERE 1=1 ";
			sqlCmd += "AND A.GROUP_CODE = B.GROUP_CODE ";
			sqlCmd += "AND A.PROMOTE_EMP_NO = ? ";
			sqlCmd += "AND A.CURRENT_CODE = '0' ";
			sqlCmd += "AND A.ORI_ISSUE_DATE BETWEEN ? AND ? ";

			setString(1, promoteEmpId); 
			setString(2, startMonthDate);
			setString(3, endMonthDate);
			openCursor();

			while (fetchTable()) {

				String rowOfDAT = getRowOfDAT(promoteEmpId);
				byte[] tmpBytes = rowOfDAT.getBytes("MS950");
				writeBinFile(tmpBytes, tmpBytes.length);

				rowCount++;

			}

			closeCursor();

			if ((totalCnt % 3000) == 0)
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

		}

		closeBinaryOutput();
		closeInputText(br);

		if (rowCount == 0) {
			showLogMessage("I", "", "無資料可寫入.TXT檔");
		} else {
			showLogMessage("I", "", String.format("產生.TXT檔完成！，共產生%d筆資料", rowCount));
		}

		// run FTP
		procFTP(fileFolder, datFileName);

		return 0;
	}

	private String getRowOfDAT(String promoteEmpId) throws Exception {

		String groupCode = "";
		String groupName = "";
		String openFlag = "N";
		String cardNote = "";
		String cardNo = "";
		String sumAmt = "";
		String issueDate = "";

		groupCode = getValue("GROUP_CODE");
		groupName = getValue("GROUP_NAME");
		cardNote = getValue("CARD_NOTE");
		cardNo = comc.getSubString(getValue("CARD_NO"),10); //卡號末6碼
		if ("2".equals(getValue("ACTIVATE_FLAG"))) {
			openFlag = "Y";
		}
		sumAmt = getSpendingAmt(getValue("CARD_NO"));
		issueDate = getValue("ISSUE_DATE");
		
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft(promoteEmpId, 10));
		sb.append(comc.fixLeft(",", 1));
		sb.append(comc.fixLeft(groupCode, 4));
		sb.append(comc.fixLeft(",", 1));
		sb.append(comc.fixLeft(groupName, 20));
		sb.append(comc.fixLeft(",", 1));
		sb.append(comc.fixLeft(cardNote, 1));
		sb.append(comc.fixLeft(",", 1));
		sb.append(comc.fixLeft(cardNo, 6));
		sb.append(comc.fixLeft(",", 1));
		sb.append(comc.fixLeft(issueDate, 8));
		sb.append(comc.fixLeft(",", 1));
		sb.append(comc.fixLeft(openFlag, 1)); // 開卡註記(Y:已開卡)
		sb.append(comc.fixLeft(",", 1));
		sb.append(comc.fixLeft(sumAmt, 12)); // 累計消費金額(若為負值放0)
		sb.append(lineSeparator);

		return sb.toString();
	}

	private String getSpendingAmt(String cardNo) throws Exception {
		String sumAmt = "";
		long billSumAmtWithSign = 0;
		long contractSumAmtWithSign = 0;
		long sumAmtWithSign = 0;
		int recordCnt = 0;
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT  SUM(DECODE(SIGN_FLAG,'+',DEST_AMT,DEST_AMT*-1)) as BILL_SUM_AMT ");
		sb.append(" FROM BIL_BILL a ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND CARD_NO = ? ");
		sb.append(" AND ACCT_CODE IN ('BL','CA') ");
		sqlCmd = sb.toString();
		setString(1, cardNo);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			billSumAmtWithSign = getValueLong("BILL_SUM_AMT");
		}
		
		sb = new StringBuffer();
		sb.append(" SELECT  SUM(TOT_AMT) as CONTRACT_SUM_AMT ");
		sb.append(" FROM BIL_CONTRACT a ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND CARD_NO = ? ");
		sb.append(" AND MCHT_NO NOT IN ('106000000005','106000000007') ");
		sqlCmd = sb.toString();
		setString(1, cardNo);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			contractSumAmtWithSign = getValueLong("CONTRACT_SUM_AMT");
		}
		
		sumAmtWithSign = billSumAmtWithSign + contractSumAmtWithSign;
		//負值時放0
		if (sumAmtWithSign < 0) {
			sumAmtWithSign=0;
		}
			
		sumAmt = String.format("%010d00", sumAmtWithSign);

		return sumAmt;

	}

	private boolean produceHdrFile(String filePath, String fileName, String dataDate, String createDate,
			String createTime, int dataCount) throws Exception, UnsupportedEncodingException {

		boolean isOpen = openBinaryOutput(filePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", filePath));
			return false;
		}

		try {
			StringBuffer sb = new StringBuffer();
			sb.append(comc.fixLeft(dataDate, 8));
			sb.append(comc.fixLeft(",", 1));
			sb.append(comc.fixLeft(dataDate, 8));
			sb.append(comc.fixLeft(",", 1));
			sb.append(comc.fixLeft(fileName, 50));
			sb.append(comc.fixLeft(",", 1));
			sb.append(comc.fixLeft(createDate + createTime, 14));
			sb.append(comc.fixLeft(",", 1));
			sb.append(comc.fixLeft(String.format("%09d", dataCount), 9));
			// sb.append(comc.fixLeft(",", 1)); //最後一個欄位,不用再加分隔符號
			sb.append(lineSeparator);

			byte[] tmpBytes = sb.toString().getBytes("MS950");
			writeBinFile(tmpBytes, tmpBytes.length);

		} finally {
			closeBinaryOutput();
		}

		return true;
	}

	void procFTP(String fileFolder, String datFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "WMS"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		String ftpCommand = String.format("mput %s ", datFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CRDATACREA", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	public static void main(String[] args) {
		MktR720 proc = new MktR720();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}
