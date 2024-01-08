/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/12/15  V1.00.01    Ryan                                                
*  112/12/18  V1.00.02    Ryan                         不送NEWCENTER           *	
*  112/12/27  V1.00.03    Ryan                         增加欄位            *	
*  112/12/27  V1.00.03    Sunny                        調整累計繳滿期數(cpbduePayamtCnt)僅取整數 *
*****************************************************************************/
package Col;



import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Locale;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;



public class ColC033 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "cpbude協商資料下傳卡部  112/12/27  V1.00.03";
	private static final String CRM_FOLDER = "media/col/";
	private static final String DATA_FORM = "CPBDUE_ALL";
//	private final static String FTP_FOLDER = "NEWCENTER";
	private final static String FTP_FOLDER2 = "CREDITCARD";
	private final String lineSeparator = System.lineSeparator();
	private final String COL_SEPERATOR = ",";
    CommCrdRoutine comcr = null;	
	CommCrd commCrd = new CommCrd();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CommDate commDate = new CommDate();

    String hBusiBusinessDate = "";

	String searchDate = "";

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
			// =====================================
			  // 檢核參數的日期格式
		       if (args.length >= 1) {
		     	   showLogMessage("I", "", "PARM 1 : [無參數] 表示抓取businday ");
		    	   showLogMessage("I", "", "PARM 1 : [SYSDAY] 表示抓取系統日");
		    	   showLogMessage("I", "", "PARM 1 : [YYYYMMDD] 表示人工指定執行日");
				}
				if(args.length == 1) {
					if (!args[0].toUpperCase(Locale.TAIWAN).equals("SYSDAY")) {
							if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
				        showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
				        return -1;
				    }
					}
				    searchDate = args[0];
				}
				
				//檢查日期
				 if(args.length >  0) {
		               if(args[0].length() != 8) {
		            	   comc.errExit("參數日期長度錯誤, 請重新輸入! [yyyymmdd]", "");
		                 }
		            }

			commCol = new CommCol(getDBconnect(), getDBalias());
			commTxInf = new CommTxInf(getDBconnect(), getDBalias());
				
			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));

			searchDate = getProgDate(searchDate, "D");
			//日期減一天
			searchDate = commDate.dateAdd(searchDate, 0, 0, -1);

			showLogMessage("I", "", String.format("執行日期D[%s]", searchDate));

			
			// get the name and the path of the .TXT file
			String datFileName = String.format("%s_%s.TXT", DATA_FORM, searchDate);
			String fileFolder =  Paths.get(commCrd.getECSHOME(), CRM_FOLDER).toString();

			// 產生主要檔案 .TXT 
			generateDatFile(fileFolder, datFileName);

			dateTime(); // update the system date and time
			
			// run FTP
//			procFTP(fileFolder, datFileName, FTP_FOLDER);
			procFTP(fileFolder, datFileName, FTP_FOLDER2);
			renameFile(datFileName,fileFolder);
			
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
	private int generateDatFile(String fileFolder, String datFileName) throws Exception {

		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		int rowCount = 0;
		int rowCount01 = 0;
		int rowCount03 = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		selectCrStatusData();
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");
			String header = printHeader();
			sb.append(header);
			while (fetchTable()) {
				ColC033Data colC033Data = getData();
				String rowOfDAT = getRowOfDAT(colC033Data);
				sb.append(rowOfDAT);
				if("01".equals(colC033Data.type)) rowCount01 ++;
				if("03".equals(colC033Data.type)) rowCount03 ++;
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
				showLogMessage("I", "", "無資料可寫入.TXT檔");
			}else {
				showLogMessage("I", "","");
				showLogMessage("I", "","產生.TXT檔完成！");
				showLogMessage("I", "", String.format("一般卡寫入%d筆資料", rowCount01));
				showLogMessage("I", "", String.format("商務卡寫入%d筆資料", rowCount03));
				showLogMessage("I", "", String.format("共產生%d筆資料", rowCount));
				showLogMessage("I", "","");
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return rowCount;
	}
	/***
	 * ID或統編,戶況,目前協商種類,公會協商狀態,個別協商狀態,前置調解狀態
	 * @param corpIdNo
	 * @param acctStatus
	 * @param cpbdueType
	 * @param cpbdueBankType
	 * @param cpbdueTcbType
	 * @param cpbdueMediType
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getRowOfDAT(ColC033Data colC033Data) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft(colC033Data.corpIdNo, 11));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(colC033Data.chiName);
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.acctStatus, 1));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.cpbdueType, 1));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.cpbdueCurrType, 1));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.0f",colC033Data.acctJrnlbal), 11));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.cpbdueSeqno, 8));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.cpbdueBeginDate, 8));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.0f",colC033Data.cpbdueTotalAmt), 11));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.0f",colC033Data.cpbdueTotalAmtBal), 11));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.0f",colC033Data.cpbdueDueCardAmt), 11));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.0f",colC033Data.cpbdueTotalPayamt), 11));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.0f",colC033Data.cpbdueTotalDueamt), 11));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.cpbdueRate, 6));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.0f",colC033Data.cpbduePeriod), 3));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%d",colC033Data.cpbduePayamtCnt.intValue()), 3));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.cpbdueBranch, 4));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.cpbdueOverDays, 7));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.cpbdueApplyMcode, 3));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(colC033Data.cpbdueApplyAcctStatus, 2));
		sb.append(lineSeparator);
		return sb.toString();
	}
	
	/***
	 * 表頭
	 * @return
	 * @throws Exception
	 */
	private String printHeader() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft("ID或統編", 8));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("姓名", 4));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("戶況", 4));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("協商方式", 12));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("協商註記", 12));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("現欠餘額", 8));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("序號", 4));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("簽約日", 6));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("債權金額", 8));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("債權餘額", 8));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("每月應繳金額", 12));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("累計繳款金額", 12));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("累計欠款金額", 12));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("利率", 4));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("期數", 4));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("累計繳滿期數", 12));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("分行", 4));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("逾期天數", 8));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("申請協商mcode", 13));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("申請協商戶況", 12));
		sb.append(lineSeparator);
		return sb.toString();
	}

	private void selectCrStatusData() throws Exception {
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT '01' type ,B.ID_NO as CORP_ID_NO,B.CHI_NAME,C.ACCT_STATUS,A.CPBDUE_TYPE,A.CPBDUE_CURR_TYPE,A.CPBDUE_SEQNO, ");
		sb.append("A.CPBDUE_TOTAL_AMT ,A.CPBDUE_DUE_CARD_AMT ,A.CPBDUE_TOTAL_PAYAMT ,A.CPBDUE_TOTAL_DUEAMT ,A.CPBDUE_PERIOD , ");
		sb.append("A.CPBDUE_OVER_DAYS ,A.CPBDUE_APPLY_MCODE ,A.CPBDUE_APPLY_ACCT_STATUS ,A.CPBDUE_BEGIN_DATE ,A.CPBDUE_RATE ,A.CPBDUE_BRANCH, ");
		sb.append("( SELECT A1.ACCT_JRNL_BAL FROM ACT_ACCT A1, ACT_ACNO B1 WHERE A1.P_SEQNO=B1.P_SEQNO AND B1.ACCT_TYPE='01' AND B1.ID_P_SEQNO = A.CPBDUE_ID_P_SEQNO ) ACCT_JRNL_BAL ");
//		sb.append("FROM COL_CPBDUE A ,CRD_IDNO B,ACT_ACNO C ");
		sb.append("FROM COL_CPBDUE A ,CRD_IDNO B ");
//		sb.append("FROM COL_CPBDUE A ");
//		sb.append("LEFT JOIN CRD_IDNO B ON A.CPBDUE_ID_P_SEQNO=B.ID_P_SEQNO ");
		sb.append("LEFT JOIN ACT_ACNO C ON C.ID_P_SEQNO=B.ID_P_SEQNO AND A.CPBDUE_ACCT_TYPE = C.ACCT_TYPE ");
//		sb.append("LEFT JOIN ACT_ACCT_CURR D ON C.P_SEQNO=D.P_SEQNO AND D.ACCT_TYPE='01' AND D.CURR_CODE='901' ");
		sb.append("WHERE A.CPBDUE_ID_P_SEQNO = B.ID_P_SEQNO ");
//		sb.append("AND b.ID_P_SEQNO = c.ID_P_SEQNO ");
//		sb.append("WHERE A.CPBDUE_ACCT_TYPE = C.ACCT_TYPE ");  
		sb.append("AND A.CPBDUE_ACCT_TYPE ='01' "); /*CPBDUE_ACCT_TYPE=01,就找出持卡人ID*/
//		sb.append("AND C.ACCT_STATUS<>'4' ");   /*20231212 TCB需求改變，除了呆帳其他全下檔*/
//		sb.append("AND A.CPBDUE_CURR_TYPE IN ('1','2','3','4','5','6') ");
		sb.append("UNION ALL ");
		sb.append("SELECT DISTINCT '03' type ,B.CORP_NO as CORP_ID_NO,B.CHI_NAME,C.ACCT_STATUS,A.CPBDUE_TYPE,A.CPBDUE_CURR_TYPE,A.CPBDUE_SEQNO, ");
		sb.append("A.CPBDUE_TOTAL_AMT ,A.CPBDUE_DUE_CARD_AMT ,A.CPBDUE_TOTAL_PAYAMT ,A.CPBDUE_TOTAL_DUEAMT ,A.CPBDUE_PERIOD , ");
		sb.append("A.CPBDUE_OVER_DAYS ,A.CPBDUE_APPLY_MCODE ,A.CPBDUE_APPLY_ACCT_STATUS ,A.CPBDUE_BEGIN_DATE ,A.CPBDUE_RATE ,A.CPBDUE_BRANCH, ");
		sb.append("(SELECT SUM(A1.ACCT_JRNL_BAL) as ACCT_JRNL_BAL FROM ACT_ACCT A1, ACT_ACNO B1 WHERE A1.P_SEQNO=B1.P_SEQNO AND B1.ACCT_TYPE='03' AND B1.CORP_P_SEQNO=B.CORP_P_SEQNO) ACCT_JRNL_BAL ");
		//sb.append("FROM COL_CPBDUE A ,CRD_CORP B,ACT_ACNO C ");
		sb.append("FROM COL_CPBDUE A ,CRD_CORP B ");
//		sb.append("FROM COL_CPBDUE A ");
//		sb.append("LEFT JOIN CRD_CORP B ON A.CPBDUE_ID_P_SEQNO=B.CORP_P_SEQNO ");
		sb.append("LEFT JOIN ACT_ACNO C ON C.CORP_P_SEQNO=B.CORP_P_SEQNO AND A.CPBDUE_ACCT_TYPE = C.ACCT_TYPE ");
		sb.append("LEFT JOIN ACT_ACCT_CURR D ON C.P_SEQNO=D.P_SEQNO AND D.CURR_CODE='901' ");
		sb.append("WHERE A.CPBDUE_ID_P_SEQNO = B.CORP_P_SEQNO ");
//		sb.append("AND b.CORP_P_SEQNO = c.CORP_P_SEQNO ");  
		sb.append("AND A.CPBDUE_ACCT_TYPE = C.ACCT_TYPE "); 
		sb.append("AND A.CPBDUE_ACCT_TYPE ='03' ");  /*如果CPBDUE_ACCT_TYPE=03,就找出公司統編*/
		sb.append("AND C.ACNO_FLAG='2' "); 
		//sb.append("AND C.ACCT_STATUS<>'4' ");   /*20231212 TCB需求改變，除了呆帳其他全下檔*/
//		sb.append("AND A.CPBDUE_CURR_TYPE IN ('1','2','3','4','5','6') ");
		
		

		sqlCmd = sb.toString();
		openCursor();
	}
	
	ColC033Data getData() throws Exception {
		ColC033Data colC033Data = new ColC033Data();
		colC033Data.type = getValue("TYPE");
		colC033Data.corpIdNo = getValue("CORP_ID_NO");
		colC033Data.chiName = getValue("chi_name");
		colC033Data.acctStatus = getValue("ACCT_STATUS");
		colC033Data.cpbdueType = getValue("CPBDUE_TYPE");
		colC033Data.cpbdueCurrType = getValue("CPBDUE_CURR_TYPE");
		colC033Data.acctJrnlbal = getValueDouble("ACCT_JRNL_BAL");
		colC033Data.cpbdueSeqno = getValue("CPBDUE_SEQNO");
		colC033Data.cpbdueBeginDate = getValue("CPBDUE_BEGIN_DATE");
		colC033Data.cpbdueTotalAmt = getValueDouble("CPBDUE_TOTAL_AMT");
		colC033Data.cpbdueDueCardAmt = getValueDouble("CPBDUE_DUE_CARD_AMT");
		colC033Data.cpbdueBranch = getValue("CPBDUE_BRANCH");
		colC033Data.cpbdueTotalPayamt = getValueDouble("CPBDUE_TOTAL_PAYAMT");
		colC033Data.cpbdueTotalDueamt = getValueDouble("CPBDUE_TOTAL_DUEAMT");
		colC033Data.cpbdueRate = getValue("CPBDUE_RATE");
		colC033Data.cpbduePeriod = getValueDouble("CPBDUE_PERIOD");
		colC033Data.cpbdueOverDays = getValue("CPBDUE_OVER_DAYS");
		colC033Data.cpbdueApplyMcode = getValue("CPBDUE_APPLY_MCODE");
		colC033Data.cpbdueApplyAcctStatus = getValue("CPBDUE_APPLY_ACCT_STATUS");
		colC033Data.cpbdueTotalAmtBal = (getValueDouble("CPBDUE_DUE_CARD_AMT") * getValueDouble("CPBDUE_PERIOD")) - getValueDouble("CPBDUE_TOTAL_PAYAMT");
		double cpbduePayamtCnt = (getValueDouble("CPBDUE_TOTAL_PAYAMT") > 0 && getValueDouble("CPBDUE_DUE_CARD_AMT") > 0)? getValueDouble("CPBDUE_TOTAL_PAYAMT")/getValueDouble("CPBDUE_DUE_CARD_AMT"):0;
//		if(cpbduePayamtCnt >= getValueDouble("CPBDUE_PERIOD")) {
//			colC033Data.cpbduePayamtCnt = getValueDouble("CPBDUE_PERIOD");
//		}else {
			colC033Data.cpbduePayamtCnt = cpbduePayamtCnt;
//		}
		
		return colC033Data;
	}
	
	void procFTP(String fileFolder, String datFileName, String ftpFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = ftpFileName; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s", datFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName(ftpFileName, ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void renameFile(String datFileName, String fileFolder ) throws Exception {
		String tmpstr1 = Paths.get(fileFolder, datFileName).toString();
		String tmpstr2 = Paths.get(fileFolder,"/backup/" ,String.format("%s.%s", datFileName , sysDate + sysTime)).toString();

		if (commCrd.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + tmpstr1 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}
	
	public static void main(String[] args) {
		ColC033 proc = new ColC033();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
	
	class ColC033Data {
		String type = "";
		String corpIdNo = "";
		String chiName = "";
		String acctStatus = "";
		String cpbdueType = "";
		String cpbdueCurrType = "";
		String cpbdueSeqno = "";
		Double acctJrnlbal = Double.valueOf(0.0);
		Double cpbdueTotalAmt = Double.valueOf(0.0);
		Double cpbdueTotalAmtBal = Double.valueOf(0.0);
		Double cpbdueDueCardAmt = Double.valueOf(0.0);
		Double cpbdueTotalPayamt = Double.valueOf(0.0);
		Double cpbdueTotalDueamt = Double.valueOf(0.0);
		Double cpbduePeriod = Double.valueOf(0.0);
		Double cpbduePayamtCnt = Double.valueOf(0.0);		
		String cpbdueOverDays = "";
		String cpbdueApplyMcode = "";
		String cpbdueApplyAcctStatus = "";
		String cpbdueBeginDate="";
		String cpbdueRate="";
		String cpbdueBranch="";
	}
}


