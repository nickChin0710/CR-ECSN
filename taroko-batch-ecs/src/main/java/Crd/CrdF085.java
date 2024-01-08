/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/06/27  V1.00.00   Ryan     program initial                            *
*  112/07/03  V1.00.01   Wilson   調整參數判斷                                                                                                *
*  112/07/19  V1.00.02   Wilson   檔名為2個底線                                                                                             *
*  112/08/24  V1.00.03   Wilson   調整FTP參數                                                                                                *
*  112/08/25  V1.00.04   Ryan     檔案格式UTF8改MS950                            *
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

public class CrdF085 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生信用卡尚未開卡EMAIL通知檔處理程式 112/08/25 V1.00.04";
	private static final String CRM_FOLDER = "/media/crd/";
	private static final String DATA_FORM = "CARDM11";
	private final static String COL_SEPERATOR = "|&";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CommCrdRoutine comcr = null;
	CalBalance calBalance = null;

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
			String newSearchData = commCol.lastdateOfmonth(searchData);
			while(true) {
				if(!isHoliday(newSearchData))
					break;
				newSearchData = comcr.increaseDays(newSearchData,-1);
			}
			if(!searchData.equals(newSearchData)) {
				showLogMessage("E", "", "今日非本月最後一天營業日,不執行此程式");
				return 0;
			}
			
			String searchDataLastMonth = commDate.monthAdd(searchData, -1);	
			showLogMessage("I", "", String.format("取得上個月營業日yyyymm = [%s]", searchDataLastMonth));
			
			// get the name and the path of the .DAT file
			String datFileName = String.format("%s__%s.dat", DATA_FORM, sysDate);
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			// 產生主要檔案 .TXT 
			generateDatFile(fileFolder, datFileName ,searchDataLastMonth);

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
	private int generateDatFile(String fileFolder, String datFileName ,String searchData) throws Exception {
		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		selectCrdF085Data(searchData);
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生檔案......");
			String rowOfTXT = "";
			while (fetchTable()) {
				CrdF085Data crdF085Data = getInfData();
				rowOfTXT = getRowOfDetail00(crdF085Data);
				rowOfTXT += getRowOfDetail01();
				rowOfTXT += getRowOfDetail02();
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
	private String getRowOfDetail00(CrdF085Data crdF085Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft("00", 2)); 
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(crdF085Data.idNo, 11));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("02", 2));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(crdF085Data.eMailAddr, 30));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("合作金庫商業銀行信用卡開卡通知", 50));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("親愛的卡友您好：", 30));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 19));
		sb.append(commCrd.fixLeft(" ", 60));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	private String getRowOfDetail01() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft("01", 2)); 
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("您近期申辦的合作金庫信用卡尚未開卡，提醒您儘早開卡使用以享卡友優惠權益，請 <a href='https://cobank.tcb-bank.com.tw/TCB.TWNB.IDV.WEB/general/creditCardOpen/creditCardOpen.faces?_menu=TWNCUT07'>按此開卡</a>", 210));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	private String getRowOfDetail02() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft("02", 2)); 
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("若有疑問請撥本行二四小時客服務電話，若已開卡請忽略本訊息。謹慎理財信用至上循環利率4.15%~14.75%.", 210));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	CrdF085Data getInfData() throws Exception {
		CrdF085Data crdF085Data = new CrdF085Data();
		crdF085Data.idNo = getValue("ID_NO");
		crdF085Data.eMailAddr = getValue("E_MAIL_ADDR");
		return crdF085Data;
	}

	private void selectCrdF085Data(String searchData) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT B.ID_NO,B.E_MAIL_ADDR ")
		.append(" FROM CRD_CARD A,CRD_IDNO B ")
		.append(" WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" AND B.E_MAIL_ADDR <>'' ")
		.append(" AND A.COMBO_INDICATOR <> 'Y' ")
		.append(" AND A.CURRENT_CODE = '0' ")
		.append(" AND ACTIVATE_DATE = '' ")
		.append(" AND SUBSTRING(A.ORI_ISSUE_DATE,1,6) = ? ");
		sqlCmd = sb.toString();
		setString(1,searchData);
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
		CrdF085 proc = new CrdF085();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class CrdF085Data{
	String idNo = "";
	String eMailAddr = "";
}




