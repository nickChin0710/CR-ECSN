/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/11  V1.00.00   Ryan     program initial                            *
*  112/06/16  V1.00.01   Ryan     檔案日期要減一 	                             *
*  112/06/17  V1.00.02   Nick     檔名西元年    	                             *
*  112/10/04  V1.00.03   Ryan     selectInfS005Data where + cycle    	     *
*****************************************************************************/
package Inf;

import java.math.BigDecimal;
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

public class InfS005 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生送客服歸戶帳單主檔程式 112/10/04 V1.00.03";
	private static final String CRM_FOLDER = "/media/crm/";
	private static final String DATA_FORM = "CCTOLSX";
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
	String stmtCycle = "";
	
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
			
			String cycle = commStr.right(searchDate, 2);
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
		
		if(!isWorkDay) {
			showLogMessage("I", "", String.format("今日營業日非CYCLE次日 ,產生空檔[%s]",datFilePath));
			return 0;
		}
		
		selectInfS005Data();
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");
			while (fetchTable()) {
				InfS005Data infS005Data = getInfData();
				String rowOfDAT = getRowOfDAT(infS005Data);
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
	
	/***
	 * 讀取CRD_CARD
	 * @param infS005Data
	 * @throws Exception
	 */
	void selectCrdCard(InfS005Data infS005Data) throws Exception {
		extendField = "CARD.";
		sqlCmd = " SELECT CARD_NO ,GROUP_CODE ";
	/*sqlCmd += " ,decode(LEFT(GROUP_CODE,1),'1','106','2','206','3','306','6',decode(?,'901','106','840','606','392','607'),'') as ORG "; */
		sqlCmd += " ,decode(CURR_CODE,'840', '606','392', '607', decode(ACCT_TYPE,'01','106','306')) as ORG ";
		sqlCmd += " from CRD_CARD ";
		sqlCmd += " WHERE P_SEQNO = ?  AND curr_Code=?  ";
		sqlCmd += " FETCH FIRST 1 ROWS ONLY ";
		setString(1,infS005Data.pSeqno);
		setString(2,infS005Data.currCode);
		selectTable();

		infS005Data.cardNo = getValue("CARD.CARD_NO");
		infS005Data.groupCode = getValue("CARD.GROUP_CODE");
		infS005Data.org = getValue("CARD.ORG");
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
		sqlCmd = " SELECT THIS_CLOSE_DATE ,THIS_LASTPAY_DATE ,THIS_ACCT_MONTH ,STMT_CYCLE ";
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
			stmtCycle = getValue("WORK.STMT_CYCLE");
			return true;
		}
		return false;
	}
	
	
	Double add(Double v1, Double v2 ,Double v3) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());
		
		BigDecimal b3 = new BigDecimal(v3.toString());

		return b1.add(b2).add(b3).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

	}

	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDAT(InfS005Data infS005Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		//讀取CRD_CARD
		selectCrdCard(infS005Data);
		//消費款及借方 
		double acctDebit = add(infS005Data.stmtNewAmt,infS005Data.stmtAdjustAmt ,0.0);
		//利息費用
		double acctInt = add(infS005Data.billedBegBalRi,infS005Data.billedBegBalPf,infS005Data.billedBegBalLf);
	
		sb.append(commCrd.fixLeft(infS005Data.idNo, 16)); //身分證號 X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS005Data.cardNo, 16));//卡號 X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS005Data.stmtLastTtl, "#0.00"), 14));//前期餘額 9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS005Data.stmtPaymentAmt , "#0.00"), 14));//繳款及貸方 9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(acctDebit , "#0.00"), 14));//消費款及借方 9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS005Data.billedBegBalCa , "#0.00"), 14));//預借現金 9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(acctInt , "#0.00"), 14));//利息費用 9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS005Data.stmtThisTtlAmt , "#0.00"), 14));//應繳總額 9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS005Data.minPay , "#0.00"), 14));//本期最低  9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS005Data.stmtOverDueAmt , "#0.00"), 14));  //累計最低 9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS005Data.stmtMp , "#0.00"), 14));  //最低應繳 9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commDate.toTwDate(thisCloseDate), 7));  //帳單結帳日 X(7) 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commDate.toTwDate(thisLastpayDate), 7));  //繳款截止日 X(7)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commDate.toTwDate(thisAcctMonth+"01"), 5));  //帳單月份 X(5)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS005Data.org, 3));  //ORG X(3)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS005Data.groupCode,4));  //團體代碼 X(4)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS005Data.currCode,3));  //幣別 X(3)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS005Data.pSeqno,10));  //pSeqno X(10)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	InfS005Data getInfData() throws Exception {
		InfS005Data infS005Data = new InfS005Data();
		infS005Data.idNo = getValue("ID_NO");
		infS005Data.stmtLastTtl = getValueDouble("STMT_LAST_TTL");
		infS005Data.stmtPaymentAmt = getValueDouble("STMT_PAYMENT_AMT");
		infS005Data.stmtNewAmt = getValueDouble("STMT_NEW_AMT");
		infS005Data.stmtAdjustAmt = getValueDouble("STMT_ADJUST_AMT");
		infS005Data.billedBegBalCa = getValueDouble("BILLED_END_BAL_CA");
		infS005Data.billedBegBalRi = getValueDouble("BILLED_END_BAL_RI");
		infS005Data.billedBegBalPf = getValueDouble("BILLED_END_BAL_PF");
		infS005Data.billedBegBalLf = getValueDouble("BILLED_END_BAL_LF");
		infS005Data.stmtThisTtlAmt = getValueDouble("STMT_THIS_TTL_AMT");
		infS005Data.minPay = getValueDouble("MIN_PAY");
		infS005Data.stmtOverDueAmt = getValueDouble("STMT_OVER_DUE_AMT");
		infS005Data.stmtMp = getValueDouble("STMT_MP");
		infS005Data.currCode = getValue("CURR_CODE");
		infS005Data.pSeqno = getValue("P_SEQNO");
	
		return infS005Data;
	}

	private void selectInfS005Data() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT C.ID_NO ,A.STMT_LAST_TTL ")
		.append(",(case when A.STMT_ADJUST_AMT <= 0  then ((A.STMT_PAYMENT_AMT + A.STMT_ADJUST_AMT) *-1)  else (A.STMT_PAYMENT_AMT *-1) end) as STMT_PAYMENT_AMT ")
		.append(",(case when A.STMT_ADJUST_AMT >  0  then ((A.STMT_NEW_AMT     - A.STMT_ADJUST_AMT)    )  else (A.STMT_NEW_AMT        ) end) as STMT_NEW_AMT ")
		.append(",A.STMT_ADJUST_AMT ,A.BILLED_END_BAL_CA ")
		.append(",A.BILLED_END_BAL_RI ,A.BILLED_END_BAL_PF ,A.BILLED_END_BAL_LF ,A.STMT_THIS_TTL_AMT ,(A.STMT_MP - A.STMT_OVER_DUE_AMT ) as MIN_PAY ,A.STMT_OVER_DUE_AMT ")
		.append(",A.STMT_MP ,A.CURR_CODE ,A.P_SEQNO ")
		.append(" FROM ACT_CURR_HST A left join ACT_ACNO B ON A.P_SEQNO = B.ACNO_P_SEQNO ")
		.append(" LEFT JOIN CRD_IDNO C ON B.ID_P_SEQNO = C.ID_P_SEQNO ")
		.append(" Where  A.ACCT_MONTH = ?  and  B.ACNO_FLAG <>'2' and A.STMT_CYCLE = ? ")
		.append(" Order By  A.ACCT_TYPE , A.STMT_CYCLE, A.P_SEQNO ");
		sqlCmd = sb.toString();
		setString(1,thisBeforeMonth);
		setString(2,stmtCycle);
		showLogMessage("I", "", String.format("THIS_BEFORE_MONTH = [%s] ,STMT_CYCLE = [%s] ",thisBeforeMonth,stmtCycle));
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
		InfS005 proc = new InfS005();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class InfS005Data{
	String idNo = "";
	String idPSeqno = "";
	String pSeqno = "";
	String cardNo = "";
	double stmtLastTtl = 0;
	double stmtPaymentAmt = 0;
	double stmtNewAmt = 0;
	double stmtAdjustAmt = 0;
	double billedBegBalCa = 0;
	double billedBegBalRi = 0;
	double billedBegBalPf = 0;
	double billedBegBalLf = 0;
	double stmtThisTtlAmt = 0;
	double minPay = 0;
	double stmtOverDueAmt = 0;
	double stmtMp = 0;
	String thisCloseDate = "";
	String thisLastpayDate = "";
	String thisAcctMonth = "";
	String groupCode = "";
	String currCode = "";
	String org = "";
}




