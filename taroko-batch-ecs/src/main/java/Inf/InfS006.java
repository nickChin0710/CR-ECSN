/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/12  V1.00.00   Ryan     program initial                            *
*  112/06/16  V1.00.01   Ryan     檔案日期要減一			                         *
*  112/06/17  V1.00.02   Nick     檔名西元年    	                             *
*  112/10/04  V1.00.03   Ryan     selectInfS006Data where + cycle    	     *
*  112/11/06  V1.00.04   Ryan     add selectCycPyaj   	                     *
*  112/11/07  V1.00.05   Ryan     相同p_seqno + curr_code 才 selectCycPyaj   	                     *
*****************************************************************************/
package Inf;

import java.nio.file.Paths;
import java.util.HashMap;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;

import Cca.CalBalance;

public class InfS006 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生送客服歸戶帳單明細檔程式 112/17/07  V1.00.05";
	private static final String CRM_FOLDER = "/media/crm/";
	private static final String DATA_FORM = "CCTOLS2X";
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
	int pyajCnt = 0;
	HashMap<String,Integer> pSeqnoTmp = new HashMap<String,Integer>();
	
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
		
		if(!isWorkDay) {
			showLogMessage("I", "", String.format("今日營業日非CYCLE次日 ,產生空檔[%s]",datFilePath));
			return 0;
		}
		
		selectInfS006Data();
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");
			while (fetchTable()) {
				InfS006Data infS006Data = getInfData();
				String rowOfDAT = getRowOfDAT(infS006Data);
				sb.append(rowOfDAT);
				rowCount++;
				countInEachBuffer++;
				pyajCnt = 0;
				String keyTmp = infS006Data.pSeqno + "#" + infS006Data.currCode;
				if(pSeqnoTmp.get(keyTmp) == null) {
					pSeqnoTmp.put(keyTmp, 0);
					String rowOfDAT4Pyaj = selectCycPyaj(infS006Data);
					sb.append(rowOfDAT4Pyaj);
				}
				rowCount += pyajCnt;
				countInEachBuffer += pyajCnt;
				
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

	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDAT(InfS006Data infS006Data) throws Exception {
		StringBuffer sb = new StringBuffer();
	
		sb.append(commCrd.fixLeft(infS006Data.idNo, 16)); //身分證號 X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS006Data.cardNo, 16));//卡號 X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commDate.toTwDate(infS006Data.postDate), 7));//入帳日期 9(7)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS006Data.mchtChiName, 40));//帳務摘要敘述 X(40)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%011d", 0), 11));//查證號碼(補0) 9(11)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commDate.toTwDate(infS006Data.purchaseDate), 7));//消費日期 9(7)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS006Data.txnCode, 2));//TXCD 9(2)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS006Data.dcBegBal , "#0.00"), 14));//帳單金額 9(11).99 14碼(含負號及.99)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS006Data.mchtCity, 13));//消費地  X(13)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS006Data.org, 3));  //ORG X(3)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS006Data.mccCode, 14));  //特店類別碼 9(5)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS006Data.groupCode, 4));  //團體代碼 9(4) 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS006Data.currCode, 3));  //幣別 X(3)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commDate.toTwDate(infS006Data.processDate), 7));  //外幣折算日 9(7)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS006Data.sourceCurr, 3));  //外幣幣別 X(3)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS006Data.sourceAmt, "#0.00"), 14));  //外幣金額 9(11).99 14碼(含負號及.99)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	InfS006Data getInfData() throws Exception {
		InfS006Data infS006Data = new InfS006Data();
		infS006Data.idNo = getValue("ID_NO");
		infS006Data.cardNo = getValue("CARD_NO");
		infS006Data.postDate = getValue("POST_DATE");
		infS006Data.mchtChiName = getValue("MCHT_CHI_NAME");
		infS006Data.purchaseDate = getValue("PURCHASE_DATE");
		infS006Data.txnCode = getValue("TXN_CODE");
		infS006Data.org = getValue("ORG");
		infS006Data.dcBegBal = getValueDouble("DC_BEG_BAL");
		infS006Data.mchtCity = getValue("MCHT_CITY");
		infS006Data.groupCode = getValue("GROUP_CODE");
		infS006Data.mccCode = getValue("MCC_CODE");
		infS006Data.currCode = getValue("CURR_CODE");
		infS006Data.processDate = getValue("PROCESS_DATE");
		infS006Data.sourceCurr = getValue("SOURCE_CURR");
		infS006Data.sourceAmt = getValueDouble("SOURCE_AMT");
		infS006Data.pSeqno = getValue("P_SEQNO");
		return infS006Data;
	}

	private void selectInfS006Data() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT D.ID_NO ,A.CARD_NO ,A.POST_DATE ,decode(B.MCHT_CHI_NAME,'',B.MCHT_ENG_NAME,B.MCHT_CHI_NAME) as MCHT_CHI_NAME ,A.PURCHASE_DATE ")
//		.append(",DECODE(A.TXN_CODE,'IF', '12','AF', '60','DF', '60','05', '40', '06', '41', '07', '30', '25', '43', '26', '42', '27', '31', A.TXN_CODE) AS TXN_CODE")
		.append(",decode(A.TXN_CODE,'IN', '40','LF', '60','RI', '12','TL', '16','IF', '12','AF','60','CF','37','DF','60','05',(decode(A.ACCT_CODE,'PF','16','40')),'06', '41','07','30','25','43','26','42','27','31', A.TXN_CODE) AS TXN_CODE")
//		.append(",DECODE(LEFT(A.GROUP_CODE,1),'1','106','2','206','3','306','6',DECODE(A.CURR_CODE,'901','106','840','606','392','607'),'') AS ORG ")
		.append(",decode(A.CURR_CODE,'840', '606','392', '607', decode(A.ACCT_TYPE,'01','106','306')) AS ORG ")
		.append(",A.DC_BEG_BAL ,B.MCHT_CITY ,A.GROUP_CODE ,C.MCC_CODE ,A.CURR_CODE ,B.PROCESS_DATE ,B.SOURCE_AMT ,B.SOURCE_CURR ,A.P_SEQNO ")
		.append(" FROM ACT_DEBT A LEFT JOIN BIL_BILL B ON A.REFERENCE_NO = B.REFERENCE_NO ")
		.append(" LEFT JOIN BIL_MERCHANT C ON B.MCHT_NO = C.MCHT_NO ")
		.append(" LEFT JOIN CRD_IDNO D ON B.major_id_p_seqno = D.ID_P_SEQNO ")
		.append(" WHERE A.ACCT_MONTH = ? AND A.STMT_CYCLE = ? ")
		.append(" ORDER BY A.ACCT_TYPE , A.STMT_CYCLE, A.P_SEQNO, A.POST_DATE ");
		sqlCmd = sb.toString();
		setString(1,thisAcctMonth);
		setString(2,stmtCycle);
		showLogMessage("I", "", String.format("THIS_ACCT_MONTH = [%s] ,STMT_CYCLE = [%s] ",thisAcctMonth,stmtCycle));
		openCursor();
	}
	
	private String selectCycPyaj(InfS006Data infS006Data) throws Exception {
		InfS006Data infS006Data4Pyaj = new InfS006Data();
		StringBuffer sb = new StringBuffer();
		infS006Data4Pyaj.idNo = infS006Data.idNo;
		extendField = "pyaj.";
		sqlCmd = "select a.PAYMENT_DATE ,a.DC_PAYMENT_AMT ,a.CLASS_CODE ,a.PAYMENT_TYPE ,b.BILL_DESC "
				+ ",(SELECT CARD_NO FROM CRD_CARD WHERE P_SEQNO = a.P_SEQNO order by CURRENT_CODE FETCH FIRST 1 ROWS ONLY) AS CARD_NO "
				+ " FROM CYC_PYAJ a left join PTR_PAYMENT b on a.PAYMENT_TYPE = b.PAYMENT_TYPE ";
		sqlCmd += " WHERE a.P_SEQNO = ? AND a.CURR_CODE = ? AND a.SETTLE_FLAG = 'B' AND substr(a.SETTLE_DATE,1,6) = ? ";
		setString(1,infS006Data.pSeqno);
		setString(2,infS006Data.currCode);
		setString(3,thisAcctMonth);
		
		int n = selectTable();
		for(int i = 0;i<n;i++) {
			pyajCnt ++;
			infS006Data4Pyaj.purchaseDate = getValue("pyaj.PAYMENT_DATE",i);
			infS006Data4Pyaj.postDate = getValue("pyaj.PAYMENT_DATE",i);
			infS006Data4Pyaj.dcBegBal = getValueDouble("pyaj.DC_PAYMENT_AMT",i);
			String classCode = getValue("pyaj.CLASS_CODE",i);
			String paymentType = getValue("pyaj.PAYMENT_TYPE",i);
			String billDesc = getValue("pyaj.BILL_DESC",i);
			infS006Data4Pyaj.mchtChiName = paymentType + billDesc;
			infS006Data4Pyaj.cardNo = getValue("pyaj.CARD_NO",i);
			if("P".equals(classCode)) {
				if("OP02".equals(paymentType)) {
					infS006Data4Pyaj.txnCode = "27";
				}else if ("REFU".equals(paymentType)) {
					infS006Data4Pyaj.txnCode = "41";
				}else if ("0501".equals(paymentType)) {
					infS006Data4Pyaj.txnCode = "43";
				}else {
					infS006Data4Pyaj.txnCode = "20";
				}
			}
			if("B".equals(classCode)) {
				infS006Data4Pyaj.txnCode = "43";
			}
			if("A".equals(classCode)) {
				if("DE14".equals(paymentType)) {
					infS006Data4Pyaj.txnCode = "61";
				}else if ("DE09".equals(paymentType)) {
					infS006Data4Pyaj.txnCode = "61";
				}else if ("DE10".equals(paymentType)) {
					infS006Data4Pyaj.txnCode = "61";
				}else if ("DE13".equals(paymentType)) {
					infS006Data4Pyaj.txnCode = "13";
				}else {
					infS006Data4Pyaj.txnCode = "48";
				}
			}
			sb.append(getRowOfDAT(infS006Data4Pyaj));
		}
		return sb.toString();
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
		InfS006 proc = new InfS006();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class InfS006Data{
	String idNo = "";
	String pSeqno = "";
	String cardNo = "";
	String postDate = "";
	String mchtChiName = "";
	String purchaseDate = "";
	String txnCode = "";
	double dcBegBal = 0;
	String mchtCity = "";
	String org = "";
	String mccCode = "";
	String groupCode = "";
	String currCode = "";
	String processDate = "";
	String sourceCurr = "";
	double sourceAmt = 0;

}




