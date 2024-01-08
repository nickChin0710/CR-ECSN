/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/06/27  V1.00.00   Ryan     program initial                            *
*  112/07/03  V1.00.01   Wilson   調整參數判斷                                                                                                *
*  112/08/24  V1.00.02   Wilson   調整FTP參數                                                                                                *
*  112/08/25  V1.00.03   Ryan     檔案格式UTF8改MS950                            *
*****************************************************************************/
package Crd;

import java.nio.file.Paths;
import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;

import Cca.CalBalance;

public class CrdF084 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生發送新卡權益手冊名單檔處理程式 112/08/25 V1.00.03";
	private static final String CRM_FOLDER = "/media/crd/";
	private static final String DATA_FORM = "CREDIT_NAMUAL_LIST";
	private final static String COL_SEPERATOR = "|&";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CommCrdRoutine comcr = null;
	CalBalance calBalance = null;
	String twSysDate = "";
	public int mainProcess(String[] args) {

		try {
			CommCrd comc = new CommCrd();

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			
			commCol = new CommCol(getDBconnect(), getDBalias());
			commTxInf = new CommTxInf(getDBconnect(), getDBalias());
			calBalance = new CalBalance(getDBconnect(), getDBalias());
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			// =====================================
			
			String searchData = getBusiDate();
			String parm1 = "";
			// get searchDat
			if(args.length == 1) {
				if(!commDate.isDate(args[0])) {
					comc.errExit("參數日期格式輸入錯誤", "");
				}
				parm1 = args[0];
				searchData = parm1;
			}

			showLogMessage("I", "", String.format("程式參數1[%s]", parm1));
			showLogMessage("I", "", String.format("今日營業日 = [%s]", searchData));
			if(isHoliday(searchData)) {
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
			}
			
			String searchDataLast2 = comcr.increaseDays(searchData,-2);
			String searchDataLast9 = comcr.increaseDays(searchData,-9);
			twSysDate = commDate.toTwDate(sysDate);
			
			showLogMessage("I", "", String.format("取得前2天營業日 = [%s]", searchDataLast2));
			showLogMessage("I", "", String.format("取得前9天營業日 = [%s]", searchDataLast9));
			
			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s.dat", DATA_FORM, sysDate);
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			// 產生主要檔案 .TXT 
			generateDatFile(fileFolder, datFileName ,searchDataLast2 , searchDataLast9);

			procFTP(fileFolder,datFileName);
			copyFile(fileFolder,datFileName);
			
			dateTime(); // update the system date and time
			
			showLogMessage("I", "", "執行結束");
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
	 * generate file
	 * @param fileFolder 檔案的資料夾路徑
	 * @param datFileName 檔名
	 * @return the number of rows written. If the returned value is -1, it means the path or the file does not exist. 
	 * @throws Exception
	 */
	private int generateDatFile(String fileFolder, String datFileName ,String searchDataLast2 ,String searchDataLast9) throws Exception {
		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		selectCrdF084Data(searchDataLast2 , searchDataLast9);
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生檔案......");
			String rowOfTXT = "";
			while (fetchTable()) {
				CrdF084Data crdF084Data = getInfData();
				rowOfTXT = getRowOfDetailTXT(crdF084Data);
				sb.append(rowOfTXT);
				rowCount++;
				countInEachBuffer++;	
				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes("MS950");
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
			}
			
			// write the rest of bytes on the file 
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes("MS950");
				writeBinFile(tmpBytes, tmpBytes.length);
			}
			
			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入檔案");
			}else {
				showLogMessage("I", "", String.format("產生檔案完成！，共產生%d筆資料", rowCount));
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return rowCount;
	}

	/**
	 * DETAIL-DATA
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDetailTXT(CrdF084Data crdF084Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft("00", 2)); 
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(crdF084Data.idNo, 11));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(crdF084Data.eMailAddr, 60));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(twSysDate, 7));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(crdF084Data.chiName, 30));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	CrdF084Data getInfData() throws Exception {
		CrdF084Data crdF084Data = new CrdF084Data();
		crdF084Data.idNo = getValue("ID_NO");
		crdF084Data.eMailAddr = getValue("E_MAIL_ADDR");
		crdF084Data.chiName = getValue("CHI_NAME");
		return crdF084Data;
	}

	private void selectCrdF084Data(String searchDataLast2 ,String searchDataLast9) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT B.ID_NO,B.E_MAIL_ADDR,B.CHI_NAME ")
		.append(" FROM CRD_CARD A,CRD_IDNO B ")
		.append(" WHERE A.MAJOR_ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" AND B.E_MAIL_ADDR <> '' ")
		.append(" AND ((A.COMBO_INDICATOR = 'Y' AND A.ORI_ISSUE_DATE = ? ) ")
		.append(" OR (A.COMBO_INDICATOR <> 'Y' AND A.ORI_ISSUE_DATE = ? )) ");
		sqlCmd = sb.toString();
		setString(1,searchDataLast9);
		setString(2,searchDataLast2);
		openCursor();
	}
	
	private boolean isHoliday(String searchData) throws Exception {
		sqlCmd = "select holiday from ptr_holiday where holiday = ? ";
		setString(1,searchData);
		int n = selectTable();
		if(n>0) {
			return true;
		}
		return false;
	}
	
	private void procFTP(String fileFolder, String datFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 傳送CR_STATUS_YYMMDD.DAT
		String ftpCommand = String.format("mput %s ", datFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CRDATACREA", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}
	
	private void copyFile(String fileFolder ,String datFileName) throws Exception {
		String tmpstr1 = Paths.get(fileFolder, datFileName).toString();
		String tmpstr2 = Paths.get(fileFolder, "/backup/" ,datFileName).toString();

		if (commCrd.fileCopy(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName + "]備份失敗!");
			return;
		}
		commCrd.fileDelete(tmpstr1);
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已移至 [" + tmpstr2 + "]");
	}
	
	public static void main(String[] args) {
		CrdF084 proc = new CrdF084();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class CrdF084Data{
	String idNo = "";
	String eMailAddr = "";
	String chiName = "";
}




