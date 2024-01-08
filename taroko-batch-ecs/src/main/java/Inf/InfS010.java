/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/17  V1.00.00   NickChin   program initial                          *
*  112/06/20  V1.00.01   NickChin   加參數  			                         *
*  112/06/21  V1.00.01   Kirin      Fix Date     	                         *
*  112/07/04  V1.00.02   NickChin   加複製檔案									 *
*  112/07/16  V1.00.03   NickChin   調整複製檔案								 *
*  112/08/15  V1.00.04   NickChin   取消隱碼(身分證字號、卡號、持卡人ID)				 *
*  112/08/26  V1.00.05   NickChin   修改讀取資料(selectBilDodoDtlTempListData)條件*
*  112/08/28  V1.00.06   NickChin   selectBilDodoDtlTempListData刪除<			 *
*  112/09/08  V1.00.07   NickChin   調整selectBilDodoDtlTempListData參數		 *
*  112/09/12  V1.00.08   kirin      sql條件use_month                          *
*  112/09/12  V1.00.08   Ryan       檔名日期-1                                 *
*  112/10/06  V1.00.09   NickChin   調整複製檔案								 *
*  112/10/08  V1.00.10   Kirin     fix date -2,sql條件acct_month 6號以前,減2個月	 *
*  112/11/23  V1.00.11   Kirin     0筆要產空檔                                   *
*****************************************************************************/
package Inf;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;
import Cca.CalBalance;

public class InfS010 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 5000;
	private final String progname = "市區免費停車產製名單程式(客服) 112/10/06 V1.00.11";
	private static final String CRM_FOLDER = "/media/crm/";
	private static final String NEWCENTER_FOLDER = "/crdatacrea/NEWCENTER/";
	private static final String DATA_FORM = "CCTPARKX";
	private static final String COL_SEPERATOR = "\006";// 區隔號
	private final static String FTP_FOLDER = "NEWCENTER";
	private final String lineSeparator = System.lineSeparator();
	private String busiDate = "";
	private String busiYm = "";
	
	String hBusiBusinessDate  = "";
	private String hBusinessPrevMonth  = "";
	CommString commString = new CommString();

	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommTxInf commTxInf = null;
	CalBalance calBalance = null;

	public int mainProcess(String[] args) {
		try {
			calBalance = new CalBalance(conn, getDBalias());
			commTxInf = new CommTxInf(getDBconnect(), getDBalias());
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();

			if (!connectDataBase()) {
				commCrd.errExit("connect DataBase error", "");
			}
			// =====================================

			if (args.length > 0) {
				busiYm = args[0].substring(0, 6);
				Date bDate = new SimpleDateFormat("yyyyMMdd").parse(args[0]);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(bDate);
				// 日期减一天
//				calendar.add(Calendar.DATE, -1);

				busiDate = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
//				System.out.println("參數减一天 : " + busiDate);

				hBusiBusinessDate = args[0];
			
				System.out.println("參數年月 : " + busiYm);
			} else {
				selectPtrBusinday();
			}
            //6號以前,減2個月
			int h_ten_day  = Integer.parseInt(commCrd.getSubString(hBusiBusinessDate, 6, 8));
			if(h_ten_day < 6)			     {
				 hBusinessPrevMonth= commDate.dateAdd(hBusiBusinessDate, 0,-1, 0).substring(0, 6);
			    
			 } else {
				 hBusinessPrevMonth= commDate.dateAdd(hBusiBusinessDate, 0,0, 0).substring(0, 6); 
			 }
			
			showLogMessage("I", "", javaProgram + " " + progname);
			showLogMessage("I", "", javaProgram + ": 每日產製整市區免費停車名單檔." + busiDate);
			showLogMessage("I", "", String.format("執行日期[%s]", sysDate));

			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM,  commDate.dateAdd(busiDate, 0, 0, -1), CommTxInf.DAT_EXTENSION);
			String fileFolder = Paths.get(commCrd.getECSHOME()/* FOLDER */, CRM_FOLDER).toString();
			
			System.out.println("參數减一天 datFileName: " + datFileName);
			
			// 產生主要檔案.DAT
			int dataCount = generateDatFile(fileFolder, datFileName);

			dateTime(); // update the system date and time
			boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, sysDate, sysDate,sysTime.substring(0, 4), dataCount);
			if (isGenerated == false) {
				commCrd.errExit("產生HDR檔錯誤!", "");
			}

			String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);
			
			// run FTP
			procFTP(fileFolder, datFileName, hdrFileName);

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

	void copyFile(String datFileName1, String fileFolder1 ,String datFileName2, String fileFolder2) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder2, datFileName2).toString();

		if (commCrd.fileCopy(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName2 + "]copy失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已copy至 [" + tmpstr2 + "]");
	}

	private void selectPtrBusinday() throws Exception {
		busiDate = "";
		sqlCmd = " select business_date ";
		sqlCmd += " from ptr_businday ";
		selectTable();
		if (notFound.equals("Y")) {
			commCrd.errExit("business_date not found!", "");
		}
		busiDate = getValue("business_date");
		busiYm = busiDate.substring(0, 6);
		hBusiBusinessDate = getValue("business_date");
	}

	/**
	 * generate a .Dat file
	 * 
	 * @param fileFolder  檔案的資料夾路徑
	 * @param datFilename .dat檔的檔名
	 * @return the number of rows written. If the returned value is -1, it means the
	 *         path or the file does not exist.
	 * @throws Exception
	 */
	private int generateDatFile(String fileFolder, String datFilename) throws Exception {

		String datFilePath = Paths.get(fileFolder, datFilename).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}

		int rowCount = 0;
		int countInEachBuffer = 0;

		try {
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");

			int totalCnt = selectBilDodoDtlTempListData();
//			if (totalCnt == 0) {
//				commCrd.errExit("沒有符合資料可產檔", "");
//			}
			for (int i = 0; i < totalCnt; i++) {
				String rowOfDAT = getRowOfDAT(i);
				sb.append(rowOfDAT);
				rowCount++;
				countInEachBuffer++;
				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes("UTF-8");
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
			}

			// write the rest of bytes on the file
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes("UTF-8");
				writeBinFile(tmpBytes, tmpBytes.length);
			}

			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入.DAT檔");
			} else {
				showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
			}

		} finally {
			closeBinaryOutput();
		}

		return rowCount;
	}

	/**
	 * 產生檔案
	 * 
	 * @return String
	 * @throws Exception
	 */
	private String getRowOfDAT(int i) throws Exception {

		// 身分證字號:取MAJOR_ID前10碼,5~7碼用X取代
		String majorId = getValue("MAJOR_ID", i);
		String major_id = majorId;//0815不隱碼 " ";
//		0815不隱碼
//		if (!majorId.equals("")) {
//			major_id = majorId.substring(0, 4) + "XXX" + majorId.substring(7);
//		}

		// 卡號:取CARD_NO前16碼，9~13碼用X取代
		String cardNo = getValue("CARD_NO", i);
		String card_no = cardNo;//0815不隱碼 cardNo.substring(0, 8) + "XXXXX" + cardNo.substring(13);

		// 卡別:取group_code的值後3碼寫入
		String groupCode = getValue("GROUP_CODE", i);
		String group_code = "";
		if (groupCode.length() >= 3) {
			group_code = groupCode.substring(groupCode.length() - 3);
		} else {
			group_code = groupCode;
		}

		// 持卡人ID:取ID_NO前10碼, 其中第5~7碼用X取代
		String idNo = getValue("ID_NO", i);
		String id_no = idNo;//0815不隱碼 idNo.substring(0, 4) + "XXX" + idNo.substring(7);

		// 消費金額(14碼): 取TOT_AMT
		String tot_amt = commDate.toTwDate(getValue("TOT_AMT", i));

		// 異動碼: 如果AUD_TYPE=A,寫入1
		String audType = getValue("AUD_TYPE", i);
		String aud_type = " ";
		if (audType.equals("A")) {
			aud_type = "1";
		}

		StringBuffer sb = new StringBuffer();

		sb.append(commCrd.fixLeft(major_id, 10)); // 身份證字號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(card_no, 16)); // 卡號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(group_code, 3)); // 車卡別
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(id_no, 10)); // 持卡人ID
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commString.lpad(tot_amt, 14, "0"), 14)); // 消費金額,不足14碼左補0
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(aud_type, 1)); // 異動碼
		sb.append(lineSeparator);
		return sb.toString();
	}

	// 讀取資料
	private int selectBilDodoDtlTempListData() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT a.MAJOR_ID,a.CARD_NO,a.GROUP_CODE,a.ID_NO,a.TOT_AMT,a.AUD_TYPE ");
		sb.append("FROM BIL_DODO_DTL_TEMP a left join crd_card b ON b.card_no =a.card_no ");
		sb.append("WHERE use_month = ?");
//		sb.append("WHERE ACCT_MONTH = ?");

		sqlCmd = sb.toString();
//		setString(1, busiYm);
		setString(1, hBusinessPrevMonth);
		showLogMessage("I", "", "" + "hBusinessPrevMonth= " + hBusinessPrevMonth);
		return selectTable();
	}

	void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = FTP_FOLDER; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName(FTP_FOLDER, ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
            commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	public static void main(String[] args) {
		InfS010 proc = new InfS010();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}
