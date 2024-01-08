/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/06/12  V1.00.00   Ryan     program initial                            *
*  112/07/07  V1.00.01   Ryan     cash_type=1才送ap1                          *
*  112/11/16  V1.00.02   Ryan     讀不到溢付款提領資料不要產生檔案                                                          *
*  112/11/17  V1.00.03   Ryan     轉MS950                                    *
*****************************************************************************/
package Act;

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

public class ActJ010 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "溢付款自行提領轉出作業 112/11/17 V1.00.03";
	private static final String CRM_FOLDER = "/media/act/";
	private static final String DATA_FORM = "ECSOP02_REMIT";
	private final static String LINE_SEPERATOR = "\r\n";
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	
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

			showLogMessage("I", "", String.format("今日營業日=[%s]", searchDate));
			
			// get the name and the path of the .DAT file
			String datFileName = String.format("%s%s", DATA_FORM, CommTxInf.DAT_EXTENSION);
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			String nextSearchDate = commDate.dateAdd(searchDate, 0, 0, 1) ;
			
			// 產生主要檔案 .DAT 
			int rowCount = generateDatFile(fileFolder, datFileName ,nextSearchDate);

			dateTime(); // update the system date and time
			// run FTP
			if(rowCount > 0) {
				procFTP(fileFolder, datFileName);
				renameFile(datFileName,fileFolder);
			}
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
	private int generateDatFile(String fileFolder, String datFileName ,String nextSearchDate) throws Exception {

		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		
		if(selectPtrHoliday(nextSearchDate)) {
			showLogMessage("I", "", String.format("明日日期非營業日[%s]，程式不執行", nextSearchDate));
			return 0;
		}
		
		selectActJ010Data();
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			showLogMessage("I", "", "開始產生.DAT檔......");
			//寫入首筆
			String rowOfDAT = getRowOfHeaderDAT(nextSearchDate);
			byte[] tmpBytes = rowOfDAT.getBytes("MS950");
			writeBinFile(tmpBytes, tmpBytes.length);
			double totalDrAmt = 0;
			
			//寫入明細
			StringBuffer sb = new StringBuffer();
			while (fetchTable()) {
				ActJ010Data actJ010Data = getInfData();
				rowOfDAT = getRowOfDetailDAT(actJ010Data,nextSearchDate);
				sb.append(rowOfDAT);
				rowCount++;
				countInEachBuffer++;
				totalDrAmt = new BigDecimal(totalDrAmt).add(BigDecimal.valueOf(actJ010Data.drAmt)).doubleValue();
				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					tmpBytes = sb.toString().getBytes("MS950");
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
				updateActAcaj(actJ010Data);
			}
			
			// write the rest of bytes on the file 
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				tmpBytes = sb.toString().getBytes("MS950");
				writeBinFile(tmpBytes, tmpBytes.length);
			}
			
			//寫入尾筆
			rowOfDAT = getRowOfFooterDAT(nextSearchDate,rowCount,totalDrAmt);
			tmpBytes = rowOfDAT.getBytes("MS950");
			writeBinFile(tmpBytes, tmpBytes.length);
			
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
	
	boolean selectPtrHoliday(String searchDate) throws Exception{
		sqlCmd = " SELECT COUNT(*) HOLIDAY_CNT FROM PTR_HOLIDAY WHERE HOLIDAY = ? ";
		setString(1,searchDate);
		selectTable();
		int holidayCnt = getValueInt("HOLIDAY_CNT");
		return holidayCnt > 0 ;
	}


	/**
	 * 首筆
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfHeaderDAT(String nextSearchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft("H", 1)); //首尾筆註記 X(1)
		sb.append(commCrd.fixLeft(String.format("%06d", 0), 6));//序號（編號） X(6)
		sb.append(commCrd.fixLeft("1", 1));//轉帳類別 X(1)
		sb.append(commCrd.fixLeft(commDate.toTwDate(nextSearchDate), 7));//日期（入扣帳日） X(7)
		sb.append(commCrd.fixLeft(" ", 13));//專戶帳號 X(13)
		sb.append(commCrd.fixLeft(" ", 10));//專戶統編 X(10)
		sb.append(commCrd.fixLeft(" ", 1));//交易類別 X(1)
		sb.append(commCrd.fixLeft(" ", 4));//業務類別 X(4)
		sb.append(commCrd.fixRight(String.format("%06d", 0), 6));  //總筆數 X(6)
		sb.append(commCrd.fixRight(String.format("%013d", 0), 13));  //總金額 9(11)V99
		sb.append(commCrd.fixRight(String.format("%013d", 0), 13));  //總手續費 9(11)V99
		sb.append(commCrd.fixLeft(" ", 125));  //經銷商代號（存摺顯示） X(125)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	/**
	 * 明細
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDetailDAT(ActJ010Data actJ010Data,String nextSearchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		int seqNo = 1;
		sb.append(commCrd.fixLeft("D", 1)); //首尾筆註記 X(1)
		sb.append(commCrd.fixLeft(String.format("%06d", seqNo++), 6));//序號（編號） X(6)
		sb.append(commCrd.fixLeft("1", 1));//轉帳類別 X(1)
		sb.append(commCrd.fixLeft(commDate.toTwDate(nextSearchDate), 7));//日期（入扣帳日） X(7)
		sb.append(commCrd.fixLeft(actJ010Data.transAcctKey, 13));//入扣帳號一 X(13)
		sb.append(commCrd.fixLeft(actJ010Data.idNo, 10));//客戶統編 X(10)
		sb.append(commCrd.fixLeft("8", 1));//交易類別 X(1)
		sb.append(commCrd.fixLeft("ICCN", 4));//業務類別 X(4)
		sb.append(commCrd.fixRight(String.format("%014.2f", actJ010Data.drAmt).replace(".", ""), 13));//交易金額 9(11)V99
		sb.append(commCrd.fixRight(String.format("%013d", 0), 13));  //手續費 9(11)V99
		sb.append(commCrd.fixLeft("3144", 4));  //受理單位  X(4)
		sb.append(commCrd.fixLeft("218", 3));  //BC/BS 903掛帳科目  X(3)
		sb.append(commCrd.fixLeft("5", 1));  //BC/BS 903掛帳塊數  X(1)
		sb.append(commCrd.fixLeft(" ", 13));  //入扣帳號二  X(13)
		sb.append(commCrd.fixLeft(" ", 10));  //帳號二之客戶統編  X(10)
		sb.append(commCrd.fixLeft(" ", 3));  //幣別  X(3)
		sb.append(commCrd.fixLeft(actJ010Data.pSeqno, 10));  //帳務流水號 X(10)
		sb.append(commCrd.fixLeft(" ", 46));  //FILLER X(46)
		sb.append(commCrd.fixLeft(" ", 4));  //帳務行 X(4)
		sb.append(commCrd.fixRight(String.format("%013d", 0), 13));  //實際交易金額 9(11)V99
		sb.append(commCrd.fixLeft(" ", 4));  //回覆碼 X(4)
		sb.append(commCrd.fixLeft("信用卡退款", 20));  //經銷商代號（存摺顯示） X(20)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	/**
	 * 尾筆
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfFooterDAT(String nextSearchDate,int rowCount ,double totalDrAmt) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft("E", 1)); //首尾筆註記 X(1)
		sb.append(commCrd.fixLeft(String.format("%06d", 0), 6));//序號（編號） X(6)
		sb.append(commCrd.fixLeft("1", 1));//轉帳類別 X(1)
		sb.append(commCrd.fixLeft(commDate.toTwDate(nextSearchDate), 7));//日期（入扣帳日） X(7)
		sb.append(commCrd.fixLeft(" ", 13));//專戶帳號 X(13)
		sb.append(commCrd.fixLeft(" ", 10));//專戶統編 X(10)
		sb.append(commCrd.fixLeft(" ", 1));//交易類別 X(1)
		sb.append(commCrd.fixLeft(" ", 4));//業務類別 X(4)
		sb.append(commCrd.fixRight(String.format("%06d", rowCount), 6));  //總筆數 X(6)
		sb.append(commCrd.fixRight(String.format("%014.2f", totalDrAmt).replace(".", ""), 13));  //總金額 9(11)V99
		sb.append(commCrd.fixRight(String.format("%013d", 0), 13));  //總手續費 9(11)V99
		sb.append(commCrd.fixLeft(" ", 125));  //經銷商代號（存摺顯示） X(125)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	void updateActAcaj(ActJ010Data actJ010Data) throws Exception {
		daoTable = "act_acaj";
		updateSQL = " is_transfer_op02 = 'Y' , ";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = 'SYSTEM',";
		updateSQL += " mod_pgm   = 'ActJ010',";
		updateSQL += " mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = "where rowid = ? ";
		setRowId(1, actJ010Data.rowid);
		updateTable();
	}
	
	ActJ010Data getInfData() throws Exception {
		ActJ010Data actJ010Data = new ActJ010Data();
		actJ010Data.idNo = getValue("ID_NO");
		actJ010Data.transAcctKey = getValue("TRANS_ACCT_KEY");
		actJ010Data.drAmt = getValueDouble("DR_AMT");
		actJ010Data.pSeqno = getValue("P_SEQNO");
		actJ010Data.rowid = getValue("ROWID");
		actJ010Data.cashType = getValue("CASH_TYPE");
		return actJ010Data;
	}

	private void selectActJ010Data() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT A.DR_AMT ,A.TRANS_ACCT_KEY ,C.ID_NO ,A.P_SEQNO ,A.ROWID AS ROWID ,A.CASH_TYPE ")
		.append(" FROM ACT_ACAJ A LEFT JOIN ACT_ACNO B ON A.P_SEQNO = B.P_SEQNO ")
		.append(" LEFT JOIN CRD_IDNO C ON B.ID_P_SEQNO = C.ID_P_SEQNO ")
		.append(" WHERE decode(A.CURR_CODE,'','901',A.CURR_CODE)='901' AND A.ADJUST_TYPE = 'OP02' AND A.APR_FLAG = 'Y' AND A.IS_TRANSFER_OP02 = '' AND A.CASH_TYPE = '1' ");
		sqlCmd = sb.toString();
		openCursor();
	}
	
	void procFTP(String fileFolder, String datFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 傳送DAT
		String ftpCommand = String.format("mput %s", datFileName);
		
		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("NCR2TCB", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void renameFile(String datFileName1, String fileFolder1 ) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder1,"/backup/" ,datFileName1 + "_" + sysDate+sysTime).toString();

		if (commCrd.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName1 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}

	public static void main(String[] args) {
		ActJ010 proc = new ActJ010();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class ActJ010Data{
	String idNo = "";
	double drAmt = 0;
	String transAcctKey = "";
	String pSeqno = "";
	String rowid = "";
	String cashType = "";
}




