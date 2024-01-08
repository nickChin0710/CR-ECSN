/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/26  V1.00.00   Ryan     program initial                            *
*  112/04/27  V1.00.01   Ryan     調整同一個ID只出一筆，   取得一般卡逾期天數邏輯調整           *
*  113/01/04  V1.00.03   Ryan     改為MS950,LINE_SEPERATOR改為\r\n              *
*****************************************************************************/
package Inf;

import java.nio.file.Paths;
import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;

import Cca.CalBalance;

public class InfC055 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生送CRDB-CRU23X1-TYPE-55異動 CRRDUD 逾期天數 113/01/04 V1.00.03";
	private static final String CRM_FOLDER = "/media/crdb/";
	private static final String DATA_FORM = "CRU23B1_TYPE_55";
	private final static String LINE_SEPERATOR = "\r\n";
	
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

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (!connectDataBase()) {
				commCrd.errExit("connect DataBase error", "");
			}
			
			commCol = new CommCol(getDBconnect(), getDBalias());
			commTxInf = new CommTxInf(getDBconnect(), getDBalias());
			calBalance = new CalBalance(getDBconnect(), getDBalias());
			// =====================================
			
			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			
			if(args.length == 1) {
		          if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
		              showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
		              return -1;
		          }
		          searchDate = args[0];
		      }
			
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			searchDate = getProgDate(searchDate, "D");
			
			//日期-1天
			searchDate = commDate.dateAdd(searchDate, 0, 0, -1);
			showLogMessage("I", "", String.format("取得營業日前一日=[%s]", searchDate));

			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s.txt", DATA_FORM, searchDate);
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();

			generateDatFile(fileFolder, datFileName ,searchDate);
			
			// run FTP
			procFTP(fileFolder,datFileName);
			renameFile(fileFolder,datFileName);
			

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
	private int generateDatFile(String fileFolder, String datFileName ,String searchDate) throws Exception {

		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		
		selectInfC055Data(searchDate);
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");
			while (fetchTable()) {
				InfC055Data infC055Data = getInfData();
				String rowOfDAT = getRowOfDAT(infC055Data ,searchDate);
				sb.append(rowOfDAT);
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
				showLogMessage("I", "", "無資料可寫入.DAT檔");
			}else {
				showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return rowCount;
	}
	
	/****
	 * 查詢統編歸戶層最差的MCODE 
	 * 不含06採購卡，因為採購卡不轉催，故不納入逾期
	 * @param infC055Data
	 * @return
	 * @throws Exception
	 */
	private String getMaxMcode(InfC055Data infC055Data) throws Exception{
		extendField = "MAX_MCODE.";
		sqlCmd = " SELECT MAX(INT_RATE_MCODE) as MAX_INT_RATE_MCODE FROM ACT_ACNO  ";
		sqlCmd += " WHERE ACCT_TYPE = '03' AND ACNO_FLAG = '3' AND CORP_P_SEQNO = ? ";
		setString(1,infC055Data.corpPSeqno);
		selectTable();
		String maxIntRateMcode = getValue("MAX_MCODE.MAX_INT_RATE_MCODE");
		return maxIntRateMcode;
	}
	
	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDAT(InfC055Data infC055Data ,String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		String delayDay = "0";
		//如為acno_flag ='1'，直接取得一般卡歸戶層的MCODE 
		//取得逾期天數
		if("1".equals(infC055Data.acnoFlag)) {
			infC055Data.intRateMcode = getValue("INT_RATE_MCODE");
			delayDay = commCol.getPSeqnoDelayDay(infC055Data.intRateMcode, infC055Data.pSeqno ,searchDate);
		}
		//如為acno_flag ='3'，需要增加查詢統編歸戶層最差的MCODE 
		//取得逾期天數
		if("3".equals(infC055Data.acnoFlag)) {
			infC055Data.intRateMcode = getMaxMcode(infC055Data);
			delayDay = commCol.getCorpPSeqnoDelayDay(infC055Data.intRateMcode, infC055Data.corpPSeqno, searchDate);
		}
		sb.append(commCrd.fixLeft("55", 2)); //代碼 X(2)
		sb.append(commCrd.fixLeft("3".equals(infC055Data.acnoFlag)?infC055Data.corpNo:infC055Data.idNo, 10));//主卡ID-身份證字號公司統編X(10)
		sb.append(commCrd.fixLeft(delayDay, 7));//逾期天數(判斷MCODE) X(7)
		sb.append(commCrd.fixLeft(" ", 131));  //保留  X(131)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	InfC055Data getInfData() throws Exception {
		InfC055Data infC055Data = new InfC055Data();
		infC055Data.pSeqno = getValue("P_SEQNO");
		infC055Data.corpPSeqno = getValue("CORP_P_SEQNO");
		infC055Data.acctType = getValue("ACCT_TYPE");
		infC055Data.acnoFlag = getValue("ACNO_FLAG");
		infC055Data.idPSeqno = getValue("ID_P_SEQNO");
		infC055Data.idNo = getValue("ID_NO");
		infC055Data.corpNo = getValue("CORP_NO");
		return infC055Data;
	}

	/***
	 * 以帳齡異動檔(LOG_ACT_ACAG)為查詢基礎，主要條件主檔
	 * 【帶入程式執行日期-1，即取前一天的異動資料】
	 *註：MOD_TIME放的是系統日期
	 * @param searchDate
	 * @throws Exception
	 */
	private void selectInfC055Data(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT B.P_SEQNO,B.CORP_P_SEQNO,B.INT_RATE_MCODE, ")
		.append(" B.ACCT_TYPE,B.ACNO_FLAG,B.ID_P_SEQNO,C.ID_NO,D.CORP_NO ")
		.append(" FROM (SELECT DISTINCT K_P_SEQNO FROM LOG_ACT_ACAG WHERE LEFT(MOD_TIME,8) = ?) A,ACT_ACNO B,CRD_IDNO C ")
		.append(" LEFT JOIN CRD_CORP D ON B.CORP_P_SEQNO = D.CORP_P_SEQNO ")
		.append(" WHERE A.K_P_SEQNO = B.P_SEQNO ")
		.append(" AND B.ID_P_SEQNO = C.ID_P_SEQNO ");
		sqlCmd = sb.toString();
		setString(1,searchDate);
		openCursor();
	}
	
	void procFTP(String fileFolder ,String datFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s ", datFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("NCR2TCB", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void renameFile(String fileFolder ,String datFileName) throws Exception {
		String tmpstr1 = Paths.get(fileFolder, datFileName).toString();
		String tmpstr2 = String.format("%s/backup/%s.%s", fileFolder,datFileName,sysDate+sysTime);
		if (commCrd.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}

	public static void main(String[] args) {
		InfC055 proc = new InfC055();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class InfC055Data{
	String pSeqno = "";
	String corpPSeqno = "";
	String intRateMcode = "";
	String acctType = "";
	String acnoFlag = "";
	String idPSeqno = "";
	String idNo = "";
	String corpNo = "";
}




