/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/06/06  V1.00.00   Ryan     program initial                            *
*****************************************************************************/
package Sms;

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

public class SmsP110 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private static final int COMMIT_SIZE = 1000;
	private final String progname = "簡訊發送產生檔案處理 112/06/06 V1.00.00";
	private static final String CRM_FOLDER = "/crdatacrea/";
	private static final String DATA_FORM = "CRSMSG.txt";
	private final static String COL_SEPERATOR = "|&";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CalBalance calBalance = null;
	String busDate = "";
	
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
			
			busDate = getBusiDate();
			
			// get searchDat
			if(args.length != 1) {
				showLogMessage("I", "", String.format("需輸入程式參數msg_userid"));
				return 0;
			}
			
			String searchData = args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchData));

			// 產生主要檔案 .TXT 
			generateDatFile(CRM_FOLDER, DATA_FORM ,searchData);

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
		selectSmsP110Data(searchData);
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.TXT檔......");
			String rowOfTXT = getRowOfHeaderTXT();
			sb.append(rowOfTXT);
			while (fetchTable()) {
				SmsP110Data smsP110Data = getInfData();
				rowOfTXT = getRowOfDetailTXT(smsP110Data);
				sb.append(rowOfTXT);
				updateSmsDetl(smsP110Data);
				rowCount++;
				countInEachBuffer++;	
				if(rowCount % COMMIT_SIZE == 0) {
				     commitDataBase();
				}
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
				showLogMessage("I", "", "無資料可寫入.TXT檔");
			}else {
				showLogMessage("I", "", String.format("產生.TXT檔完成！，共產生%d筆資料", rowCount));
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return rowCount;
	}

	/***
	 * Header
	 * @param smsP110Data
	 * @return
	 * @throws Exception
	 */
	private String getRowOfHeaderTXT() throws Exception {
		StringBuffer sb = new StringBuffer();
	
		sb.append(commCrd.fixLeft("H00", 3));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("CR", 10));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("JCR1800D", 13));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("催繳手機簡訊通知", 100));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("SMS", 10));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("2", 1));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("PAYER", 20));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("CRPAYMENTASK", 20));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("0000001", 10));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("3144", 6));
		sb.append(commCrd.fixLeft(" ", 17));
		sb.append(COL_SEPERATOR);
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	/**
	 * DETAIL-DATA
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDetailTXT(SmsP110Data smsP110Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft("M10", 3)); 
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(smsP110Data.idNo, 36));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("886", 3));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(smsP110Data.cellarPhone, 10));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(busDate+"140000", 14));
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft(smsP110Data.msgContent, 140).trim());
		sb.append(COL_SEPERATOR);
		sb.append(commCrd.fixLeft("3144", 6));
		sb.append(commCrd.fixLeft(" ", 4));
		sb.append(COL_SEPERATOR);
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	SmsP110Data getInfData() throws Exception {
		SmsP110Data smsP110Data = new SmsP110Data();
		smsP110Data.cellarPhone = getValue("CELLAR_PHONE");
		smsP110Data.idNo = getValue("ID_NO");
		smsP110Data.msgContent = getValue("MSG_CONTENT");
		smsP110Data.msgSeqno = getValue("MSG_SEQNO");
		return smsP110Data;
	}

	private void selectSmsP110Data(String searchData) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT A.* ,C.MSG_CONTENT ")
		.append(" FROM SMS_MSG_DTL A JOIN SMS_MSG_ID B ON A.MSG_PGM = B.MSG_PGM ")
		.append(" LEFT JOIN SMS_MSG_CONTENT C ON A.MSG_ID = C.MSG_ID ")
		.append(" WHERE A.CELLPHONE_CHECK_FLAG ='Y' AND A.PROC_FLAG <> 'Y' AND A.MSG_USERID = ? ")
		.append(" AND B.MSG_SEND_FLAG = 'Y' ");
		sqlCmd = sb.toString();
		setString(1,searchData);
		openCursor();
	}
	
	void updateSmsDetl(SmsP110Data smsP110Data) throws Exception {
		daoTable = "sms_msg_dtl";
		updateSQL = "proc_flag = 'Y' , ";
		updateSQL += " send_flag = 'Y' ,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = 'SYSTEM',";
		updateSQL += " mod_pgm   = 'SmsP110',";
		updateSQL += " mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = "where msg_seqno = ? ";
		setString(1, smsP110Data.msgSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "",
					String.format("update sms_msg_dtl not found ,msg_seqno = [%s]", smsP110Data.msgSeqno));
		}
		
	}

	public static void main(String[] args) {
		SmsP110 proc = new SmsP110();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class SmsP110Data{
	String cellarPhone = "";
	String idNo = "";
	String msgContent = "";
	String msgSeqno = "";
}




