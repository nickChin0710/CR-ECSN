/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  111/10/18  V1.00.00   Ryan     program initial                            *
*  112/03/01  V1.00.01   Ryan     調整為每月日曆日最後一天執行(不管是不是例假日)，調整區隔符號改為「ASC(6)*
*  112/03/05  V1.00.02   Sunny    依新格式調整各欄位長度及定義                                          *
*  112/04/18  V1.00.03   Sunny    將產生在media的檔案搬到CRM指定的目錄                          *
*  112/04/20  V1.00.04   Ryan    【指定參數日期 or執行日期 (如searchDate)】-1。                                                     *	
*****************************************************************************/
package Inf;



import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;



public class InfR005 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "每月底產生催收資料(CCTICR_YYMM.DAT )下傳給CRM 112/04/20  V1.00.04";
	private static final String CRM_FOLDER = "media/crm/";
	private static final String DATA_FORM = "CCTICR";
	private final static String COL_SEPERATOR = "\006";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CommDate commDate = new CommDate();
	private boolean isLastBusinday = false;
	
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
			// =====================================
			
			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			
			searchDate = getProgDate(searchDate, "D");
			//日期減一天
			searchDate = commDate.dateAdd(searchDate, 0, 0, -1);
			
			String searchDate2 = getProgDate(searchDate, "M");

			showLogMessage("I", "", String.format("執行日期D[%s]", searchDate));
			showLogMessage("I", "", String.format("執行日期M[%s]", searchDate2));

			//檢核searchDate是否為每月最後一天營業日
			isLastBusinday = commCol.isLastBusinday(searchDate);
			if(!isLastBusinday) {
				showLogMessage("I", "", "非每月日曆日最後一天，不執行");
				return 0;
			}
			
			// convert YYYYMMDD into YYMMDD
//			String fileNameSearchDate = searchDate.substring(2);
			// convert YYYYMM into YYMM
			String fileNameSearchDate2 = searchDate2.substring(2);
			
			// get the name and the path of the .DAT file
//			String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
//			String fileFolder =  Paths.get(commCrd.getECSHOME(), CRM_FOLDER).toString();

			String datFileName2 = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate2, CommTxInf.DAT_EXTENSION);
			String fileFolder2 =  Paths.get(commCrd.getECSHOME(), CRM_FOLDER).toString();
			
			// 產生主要檔案 .DAT 
//			int dataCount = generateDatFile(fileFolder, datFileName);
			int dataCount = generateDatFile(fileFolder2, datFileName2 ,searchDate);
			
			// 產生Header檔
			dateTime(); // update the system date and time
//			boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), dataCount);
//			if (isGenerated == false) {
//				comc.errExit("產生HDR檔錯誤!", "");
//			}
			
			//每月最後一個營業日多產生一份
			if(isLastBusinday) {
//				copyFile(datFileName,fileFolder,datFileName2,fileFolder2);
				boolean isGenerated2 = commTxInf.generateTxtCrmHdr(fileFolder2, datFileName2, searchDate, sysDate, sysTime.substring(0,4), dataCount);
				if (isGenerated2 == false) {
					comc.errExit("每月最後一個營業日產生HDR檔錯誤!", "");
				}
			}
			
			// 先傳*.DAT檔再傳*.HDR檔
//			String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
			String hdrFileName2 = datFileName2.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
			
			// run FTP
//			procFTP(fileFolder, datFileName, hdrFileName);
			if(isLastBusinday)
				procFTP(fileFolder2, datFileName2, hdrFileName2);

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
	private int generateDatFile(String fileFolder, String datFileName ,String searchDate2) throws Exception {
		
		selectCrStatusData(searchDate2);
		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");
			while (fetchTable()) {
				String idNo = getValue("ID_CORP_NO");
				String chiName = getValue("CHI_NAME");
				String cardNo = getValue("CARD_NO");
				String pSeqno = getValue("ACNO_P_SEQNO");
//				String acctType = getValue("acct_type");
//				String currCode = getValue("curr_code");
				String outOrg = getValue("ORG");
//				int delayDay = commCol.getDelayDay(pSeqno);
//				String rangeDayStr = commCol.getDelayDayRange(delayDay);
				String intRateMcode = getValue("INT_RATE_MCODE");
				String acctStatus = getValue("ACCT_STATUS");
				/*
				String ttlAmtBal = commStr.numFormat(getValueDouble("DC_TTL_AMT_BAL"), "#.00").replace(".", "");
				String minPayBal = commStr.numFormat(getValueDouble("DC_MIN_PAY_BAL"), "#.00").replace(".", "");
				*/
				String ttlAmtBal = String.format("%014.2f",getValueDouble("DC_TTL_AMT_BAL"));
				String minPayBal = String.format("%014.2f",getValueDouble("DC_MIN_PAY_BAL"));
//				String outOrg = commTxInf.getAcctTypeToOrg(acctType,currCode);			
				//String rowOfDAT = getRowOfDAT(idNo ,chiName ,cardNo ,rangeDayStr, ttlAmtBal ,minPayBal ,outOrg);
				String rowOfDAT = getRowOfDAT(idNo ,chiName ,cardNo ,intRateMcode ,acctStatus, ttlAmtBal ,minPayBal ,outOrg);
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

	/**
	 * 產生檔案，如下格式<br>
	 * 01	身份證號		X(10)	1-10	　<br>
	 * 		區隔符號		X(1)	11		區隔符號「ASC(6)」<br>
	 * 02	本人姓名		X(14)	12-25	CRD_IDNO.CHI_NAME <br>
	 * 		區隔符號		X(1)	26		區隔符號「ASC(6)」<br>
	 * 03	卡號	        X(16)	27-42	CRD_CARD.CARD_NO <br>
	 * 		區隔符號		X(1)	43		區隔符號「ASC(6)」<br>
	 * 04	逾期天數	    X(7)	44-50	改為MCODE <br>
	 * 		區隔符號		X(1)	41		區隔符號「ASC(6)」<br>
	 * 05	卡片狀況	    X(1)	52	        若為呆帳戶，值放2 ; 若為催收戶，值放1; 若為逾期戶，值放0 <br> 
	 * 		區隔符號		X(1)	53		區隔符號「ASC(6)」<br>
	 * 06	應繳金額	    9(10)	54-65	Act_Acct.ttl_amt_bal,10位整數2位小數(不帶小數點), 需要*100 <br> 
	 * 		區隔符號		X(1)	66		區隔符號「ASC(6)」<br>
	 * 07	最底應繳金額	9(10)	67-78	Act_Acct.min_pay_bal,10位整數2位小數(不帶小數點), 需要*100 <br> 
	 * 		區隔符號		X(1)	79		區隔符號「ASC(6)」<br>
	 * 08	銀行別	    9(3)	80-82	getAcctTypeToOrg()取得 <br>
	 * 		區隔符號		X(1)	83		區隔符號「ASC(6)」<br> 
	 * 04	FILLER		X(125)	84-208	 
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
	//private String getRowOfDAT(String idNo, String chiName, String cardNo ,String rangeDayStr ,String ttlAmtBal ,String minPayBal ,String acctTypeToOrg) throws UnsupportedEncodingException {
	private String getRowOfDAT(String idNo, String chiName, String cardNo ,String intRateMcode,String acctStatus ,String ttlAmtBal ,String minPayBal ,String acctTypeToOrg) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft(idNo, 10));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(chiName, 14));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(cardNo, 16));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
//		sb.append(commCrd.fixLeft(rangeDayStr, 7));
		sb.append(commCrd.fixLeft(intRateMcode, 7));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(acctStatus, 1));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(ttlAmtBal, 14));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(minPayBal, 14));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(acctTypeToOrg, 3));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(" ", 125));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	private void selectCrStatusData(String searchDate2) throws Exception {
		StringBuffer sb = new StringBuffer();
//		sb.append("select d.p_seqno,a.id_no,a.chi_name,b.card_no,b.curr_code,c.ttl_amt_bal,c.min_pay,c.min_pay_bal,d.acct_status,d.payment_rate1,d.acct_type");
//		sb.append("	from crd_idno a,crd_card b,act_acct c,act_acno d");
//		sb.append("	where a.id_p_seqno =b.id_p_seqno");
//		sb.append("	and b.acno_p_seqno =c.p_seqno");
//		sb.append("	and b.acno_p_seqno =d.p_seqno");
//		sb.append("	and d.payment_rate1 >='01'"); //帳齡大於等於1
//		sb.append("	and d.acct_status not in ('3','4')"); //非催呆資料
		sb.append(" SELECT DECODE(ACCT_TYPE,'01',ID_NO,CORP_NO) AS ID_CORP_NO ")
		.append(" ,DECODE(ACCT_TYPE,'01',CHI_NAME,CORP_CHI_NAME) AS CHI_NAME ")
		.append(" ,CARD_NO,'M'||INT_RATE_MCODE AS INT_RATE_MCODE ")
		.append(" ,decode(ACCT_STATUS,'4','2','3','1','0') AS ACCT_STATUS ")
		.append(" ,DC_TTL_AMT_BAL,DC_MIN_PAY_BAL,ACNO_P_SEQNO ")
		.append(" ,CASE WHEN CURR_CODE='840' THEN '606' ")//美金
		.append(" WHEN CURR_CODE='392' THEN '607' ")//日幣
		.append(" WHEN CURR_CODE='901' AND ACCT_TYPE='01' THEN '106' ")
		.append(" WHEN CURR_CODE='901' AND ACCT_TYPE='03' THEN '306' ")
		.append(" WHEN CURR_CODE='901' AND ACCT_TYPE='06' THEN '306' ")
		.append(" END AS ORG ")
		.append(" FROM COL_CS_RPT ")
		.append(" WHERE CREATE_DATE = ? ")
		.append(" AND ACCT_STATUS <> '4' ");//排除呆帳
		sqlCmd = sb.toString();
		setString(1,searchDate2);
		openCursor();
	}
	
	void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "CRM"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CRM", ftpCommand);

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
		InfR005 proc = new InfR005();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}


