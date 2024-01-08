/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/07/27  V1.00.00   Ryan     program initial                            *
*  112/10/26  V1.00.01   Ryan     selectActAcno 修改corp_p_seqno 空值直接return  *
*  112/11/27  V1.00.02   Ryan     add and correlate_id_code = 'X',增加分隔符號   *
*  112/12/12  V1.00.03   Ryan     調整主SQL                                    *
*  112/12/13  V1.00.04   Ryan     調整檔名、金額右靠                                                                                     *
*  112/12/19  V1.00.05   Ryan     移除CRD_CORRELATE Table與相關欄位                                       *                                                *
*****************************************************************************/
package Cyc;

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

public class CycG010 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private static final int COMMIT_SIZE = 5000;
	private final String progname = "信用卡利害關係人有動用循環信用名單(CRM07H) 112/12/19 V1.00.05";
	private static final String CRM_FOLDER = "/media/cyc/";
	private static final String DATA_FORM = "CRM07H_YYYYMMDD.TXT";
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
	String skipFlag = "";
	
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
			String parmDate = (args.length > 0 && args[0].length() == 8) ? args[0] :"";
	
			String searchDate = getProgDate(parmDate, "D");
			if(!"02".equals(commStr.right(searchDate, 2))) {
				showLogMessage("I", "", String.format("本月日期[%s]非每月2日", searchDate));
				return 0;
			}

			// get the name and the path of the .TXT file
			String datFileName = DATA_FORM.replace("YYYYMMDD", commCol.lastdateOfmonth(commDate.dateAdd(searchDate, 0, -1, 0)));
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			// 產生主要檔案 .TXT 
			generateDatFile(fileFolder, datFileName ,searchDate);

			dateTime(); // update the system date and time
			
			// run FTP
			procFTP(fileFolder, datFileName);
			
			//檔案備份
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
	 * generate file
	 * @param fileFolder 檔案的資料夾路徑
	 * @param datFileName 檔名
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
		selectCycG010Data(searchDate);
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.TXT檔......");
			while (fetchTable()) {
				CycG010Data cycG010Data = getInfData();
				String rowOfTXT = getRowOfTXT(cycG010Data);
				sb.append(rowOfTXT);
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
			closeCursor();
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

	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfTXT(CycG010Data cycG010Data) throws Exception {
		StringBuffer sb = new StringBuffer();

		sb.append(commCrd.fixLeft(cycG010Data.type, 3)); //Type X(3)
		sb.append(";");
		sb.append(commCrd.fixLeft(cycG010Data.correlateId, 10));//身分證字號 X(10)
		sb.append(";");
		sb.append(commCrd.fixLeft(cycG010Data.chiName, 12));//姓名 X(12)
		sb.append(";");
		sb.append(commCrd.fixRight(String.format("%,.2f", cycG010Data.debtAcctSum), 14));//循環信用餘額 X(14)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	CycG010Data getInfData() throws Exception {
		CycG010Data cycG010Data = new CycG010Data();
		cycG010Data.type = getValue("type");
		cycG010Data.correlateId = getValue("correlate_id");
		cycG010Data.chiName = getValue("chi_name");
		cycG010Data.debtAcctSum = getValueDouble("sum_amt");
		return cycG010Data;
	}

	private void selectCycG010Data(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" (select '106' type,a.acct_month,c.ID_NO as correlate_id,c.chi_name,(a.STMT_LAST_TTL + a.STMT_PAYMENT_AMT) as sum_amt,a.acct_type ")
		.append(" from CRD_IDNO c ")
		.append(" left OUTER join act_acno b on b.id_p_seqno = c.id_p_seqno ")
		.append(" left OUTER join act_acct_hst a on b.p_seqno = a.p_seqno ")
		.append(" where a.acct_month = ? ")
		.append(" and (a.STMT_LAST_TTL + a.STMT_PAYMENT_AMT) > 0 ")
		.append(" order by c.ID_NO) ")
		.append(" union all ")
		.append(" (select '306' type,a.acct_month,c.corp_no as correlate_id,c.chi_name,sum(a.STMT_LAST_TTL + a.STMT_PAYMENT_AMT) as sum_amt,a.acct_type ")
		.append(" from CRD_CORP c ")
		.append(" left OUTER join act_acno b on b.corp_p_seqno = c.corp_p_seqno ")
		.append(" left OUTER join act_acct_hst a on b.p_seqno = a.p_seqno ")
		.append(" where ")
		.append(" ((a.acct_month = ? and a.stmt_cycle = '01') ")
		.append(" or (a.acct_month = ? and a.stmt_cycle in ('20','25'))) ")
		.append(" and b.ACNO_FLAG<>'2' ")
		.append(" and (a.STMT_LAST_TTL + a.STMT_PAYMENT_AMT) > 0 ")
		.append(" group by a.acct_month,a.acct_type,c.corp_no, c.chi_name ")
		.append(" order by a.acct_month desc,c.corp_no,a.acct_type) order by type ");
		sqlCmd = sb.toString();
		setString(1,commDate.monthAdd(searchDate, -2));
		setString(2,commDate.monthAdd(searchDate, -2));
		setString(3,commDate.monthAdd(searchDate, -3));
		openCursor();
	}
	
	private 
	
	void procFTP(String fileFolder, String datFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 傳送TXT
		String ftpCommand = String.format("mput %s", datFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CREDITCARD", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void renameFile(String fileFolder1 , String datFileName1 ) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder1, "backup/", datFileName1).toString();

		if (commCrd.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + tmpstr1 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}

	public static void main(String[] args) {
		CycG010 proc = new CycG010();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class CycG010Data{
	String type = "";
	String correlateId = "";
	String chiName = "";
	double debtAcctSum = 0;
}




