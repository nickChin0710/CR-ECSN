/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/12  V1.00.00   Ryan     program initial                            *
*  112/06/16  V1.00.01   Ryan     檔案日期要減一		                             *
*  112/06/17  V1.00.02   Nick     檔名西元年    	                             *
*  112/08/25  V1.00.03   Ryan     修改上次帳單地址欄位增加處理DBA_ACNO    	         *
*  112/08/31  V1.00.04   Ryan     增加金融卡帳號欄位                                                          	         *
*  112/09/01  V1.00.05   Ryan     ID 、銀行別長度修正                                                       	         *
*****************************************************************************/
package Inf;

import java.nio.file.Paths;
import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;

import Cca.CalBalance;

public class InfS012 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生送客服電子帳單資料檔程式 112/09/01 V1.00.05";
	private static final String CRM_FOLDER = "/media/crm/";
	private static final String DATA_FORM = "CCTPCY9X";
	private final static String COL_SEPERATOR = "\006";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CalBalance calBalance = null;
	String thisCloseDate = "";
	String thisLastpayDate = "";
	String thisAcctMonth = "";
	String thisBeforeMonth = "";
	
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
			// =====================================
			
			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			searchDate = getProgDate(searchDate, "D");

			showLogMessage("I", "", String.format("今日營業日=[%s]", searchDate));
			searchDate = selectBeforeDate(searchDate);
			
			// convert YYYYMMDD into YYMMDD
//			String fileNameSearchDate = searchDate.substring(2);

			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM, searchDate, CommTxInf.DAT_EXTENSION);
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			String cycle = commStr.right(searchDate, 2) ;
			boolean isWorkDay = selectPtrWorkday(cycle);
			
			// 產生主要檔案 .DAT 
			int dataCount = generateDatFile(fileFolder, datFileName ,searchDate,isWorkDay);

			dateTime(); // update the system date and time
			boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), dataCount);
			if (isGenerated == false) {
				comc.errExit("產生HDR檔錯誤!", "");
			}
			
			// 先傳*.DAT檔再傳*.HDR檔
			String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
			
			// run FTP
			procFTP(fileFolder, datFileName ,hdrFileName);

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
	 * generate a .Dat file
	 * @param fileFolder 檔案的資料夾路徑
	 * @param datFileName .dat檔的檔名
	 * @return the number of rows written. If the returned value is -1, it means the path or the file does not exist. 
	 * @throws Exception
	 */
	private int generateDatFile(String fileFolder, String datFileName ,String searchDate ,boolean isWorkDay) throws Exception {

		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		
		/* if(!isWorkDay) {
			showLogMessage("I", "", String.format("今日營業日非CYCLE次日 ,產生空檔[%s]",datFilePath));
			return 0;
		} */
		searchDate = commStr.left(searchDate, 6);//YYYYMM
		selectInfS012Data(searchDate);
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");
			while (fetchTable()) {
				InfS012Data infS012Data = getInfData();
				String rowOfDAT = getRowOfDAT(infS012Data);
				sb.append(rowOfDAT);
				rowCount++;
				countInEachBuffer++;
				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes();
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
			}
			
			// write the rest of bytes on the file 
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes();
				writeBinFile(tmpBytes, tmpBytes.length);
			}
			
			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入.DAT檔");
			}else {
				showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return rowCount;
	}
	
	String selectBeforeDate(String searchDate) throws Exception{
		sqlCmd = " SELECT to_char(to_date(?,'yyyymmdd') - 1 days,'yyyymmdd') as before_date from DUAL ";
		setString(1,searchDate);
		selectTable();
		String beforeDate = getValue("before_date");
		return beforeDate;
	}
		
	boolean selectPtrWorkday(String cycle) throws Exception {
		showLogMessage("I", "", String.format("CYCLE = [%s]",cycle));
		extendField = "WORK.";
		sqlCmd = " SELECT THIS_CLOSE_DATE ,THIS_LASTPAY_DATE ,THIS_ACCT_MONTH ";
		sqlCmd += " ,to_char(to_date(THIS_ACCT_MONTH,'yyyymm') - 1 months,'yyyymm') as THIS_BEFORE_MONTH ";
		sqlCmd += " from PTR_WORKDAY ";
		sqlCmd += " WHERE STMT_CYCLE = ? ";
		setString(1,cycle);
		int workCnt = selectTable();
		if(workCnt>0) {
			thisCloseDate = getValue("WORK.THIS_CLOSE_DATE");
			thisLastpayDate = getValue("WORK.THIS_LASTPAY_DATE");
			thisAcctMonth = getValue("WORK.THIS_ACCT_MONTH");
			thisBeforeMonth = getValue("WORK.THIS_BEFORE_MONTH");
			return true;
		}
		return false;
	}
	
//	void selectCmsChgcolumnLog(InfS012Data infS012Data) throws Exception{
//		sqlCmd = " SELECT CHG_DATA_OLD FROM CMS_CHGCOLUMN_LOG WHERE (CHG_DATE||CHG_TIME) = ";
//		sqlCmd += " (SELECT MAX(CHG_DATE||CHG_TIME) FROM CMS_CHGCOLUMN_LOG ";
//		sqlCmd += " WHERE CHG_TABLE = 'act_acno' AND CHG_COLUMN = 'e_mail_ebill' AND P_SEQNO = ? ) ";
//		setString(1,infS012Data.pSeqno);
//		selectTable();
//		infS012Data.chgDataOld = getValue("CHG_DATA_OLD");
//	}

	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDAT(InfS012Data infS012Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		//上次設定之帳單電子郵件地址
//		selectCmsChgcolumnLog(infS012Data);
	
		sb.append(commCrd.fixLeft(infS012Data.idNo, 10)); //身分證號 X(10)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS012Data.bankCode, 3));//銀行別 X(3)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS012Data.eMailEbill, 55));//E-MAIL X(55)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS012Data.status, 2));//狀態 X(2)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS012Data.internetUpdUser, 10));//使用者代碼 X(10)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS012Data.statSendSMonth2, 16));//申請日期時間 X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS012Data.statSendEMonth2, 16));//取消日期時間 X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS012Data.internetUpdDate, 16));//更新日期 X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(" " , 55));//上次設定之帳單電子郵件地址 X(55)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%07d", commStr.ss2int(commDate.toTwDate(thisAcctMonth+"01"))), 5));//最近產製為訂閱電子帳單月份1  9(5)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("0", 5));  //最近產製為訂閱電子帳單月份2 9(5)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("0", 5));  //最近產製為訂閱電子帳單月份3  9(5)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS012Data.acctNo, 16));//金融卡帳號 X(16)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	InfS012Data getInfData() throws Exception {
		InfS012Data infS012Data = new InfS012Data();
		infS012Data.idNo = getValue("ID_NO");
		infS012Data.bankCode = getValue("BANK_CODE");
		infS012Data.eMailEbill = getValue("E_MAIL_EBILL");
		infS012Data.status = getValue("STATUS");
		infS012Data.statSendInternet = getValue("STAT_SEND_INTERNET");
		infS012Data.statSendSMonth2 = getValue("STAT_SEND_S_MONTH2");
		infS012Data.statSendEMonth2 = getValue("STAT_SEND_E_MONTH2");
		infS012Data.internetUpdUser = getValue("INTERNET_UPD_USER");
		infS012Data.internetUpdDate = getValue("INTERNET_UPD_DATE");
		infS012Data.pSeqno = getValue("P_SEQNO");
		infS012Data.acctNo = getValue("ACCT_NO");
		return infS012Data;
	}

	private void selectInfS012Data(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT  1 as TYPE ,A.STMT_CYCLE, A.P_SEQNO, B.ID_NO ,'106' AS BANK_CODE ,A.E_MAIL_EBILL ,A.STAT_SEND_INTERNET ,A.STAT_SEND_S_MONTH2 as STAT_SEND_S_MONTH2 ")
		.append(",(CASE WHEN A.STAT_SEND_INTERNET = 'Y' AND A.STAT_SEND_S_MONTH2 <= ? ")
		.append("AND DECODE(A.STAT_SEND_E_MONTH2,'','999912',A.STAT_SEND_E_MONTH2) >= ? THEN '00' ELSE '99' END) AS STATUS ")
		.append(",decode(A.STAT_SEND_E_MONTH2,'','999912',A.STAT_SEND_E_MONTH2) as STAT_SEND_E_MONTH2 ,A.INTERNET_UPD_USER as INTERNET_UPD_USER ,A.INTERNET_UPD_DATE ,'' AS ACCT_NO ")
		.append(" FROM ACT_ACNO A LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" WHERE A.ACCT_TYPE = '01' AND A.STAT_SEND_INTERNET = 'Y' AND A.E_MAIL_EBILL<>'' ")
		.append(" AND A.STAT_SEND_S_MONTH2 <= ? AND decode(A.STAT_SEND_E_MONTH2,'','999999',A.STAT_SEND_E_MONTH2) >= ? ");
		sb.append(" UNION ALL ")
		.append(" SELECT 2 as TYPE ,A.STMT_CYCLE, A.P_SEQNO, B.ID_NO ,'206' AS BANK_CODE, A.E_MAIL_EBILL , A.STAT_SEND_INTERNET ,decode(A.STAT_SEND_S_MONTH,'','000000',A.STAT_SEND_S_MONTH) as STAT_SEND_S_MONTH2 ")
		.append(",(CASE WHEN A.STAT_SEND_INTERNET = 'Y' AND A.STAT_SEND_S_MONTH <= ? ")
		.append("AND DECODE(A.STAT_SEND_E_MONTH,'','999912',A.STAT_SEND_E_MONTH) >= ? THEN '00' ELSE '99' END) AS STATUS ")
		.append(" ,decode(A.STAT_SEND_E_MONTH,'','999912',A.STAT_SEND_E_MONTH) as STAT_SEND_E_MONTH2 ,'' as INTERNET_UPD_USER ,A.E_MAIL_EBILL_DATE as INTERNET_UPD_DATE ,A.ACCT_NO ")
		.append(" FROM DBA_ACNO A LEFT JOIN DBC_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" WHERE A.ACCT_TYPE = '90' AND A.STAT_SEND_INTERNET = 'Y' AND A.E_MAIL_EBILL<>'' ")
		.append(" AND A.STAT_SEND_S_MONTH <= ? AND decode(A.STAT_SEND_E_MONTH,'','999999',A.STAT_SEND_E_MONTH) >= ? ")
		.append(" ORDER BY TYPE,STMT_CYCLE, P_SEQNO ");
	
		
		sqlCmd = sb.toString();
		setString(1,searchDate);
		setString(2,searchDate);
		setString(3,searchDate);
		setString(4,searchDate);
		setString(5,searchDate);
		setString(6,searchDate);
		setString(7,searchDate);
		setString(8,searchDate);
		showLogMessage("I", "", String.format("THIS_ACCT_MONTH = [%s]",thisAcctMonth));
		openCursor();
	}
	
	void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NEWCENTER"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("NEWCENTER", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
			commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
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

	public static void main(String[] args) {
		InfS012 proc = new InfS012();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class InfS012Data{
	String idNo = "";
	String bankCode = "";
	String eMailEbill = "";
	String statSendInternet = "";
	String statSendSMonth2 = "";
	String statSendEMonth2 = "";
	String status = "";
	String internetUpdUser = "";
	String internetUpdDate = "";
	String chgDataOld = "";
	String pSeqno = "";
	String acctNo = "";
}




