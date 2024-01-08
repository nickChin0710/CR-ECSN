/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/09/27  V1.00.00   Ryan     program initial                            *
*  112/10/03  V1.00.01   Ryan     修正FTP-->CRDATACREA                        *  
*****************************************************************************/
package Act;

import java.nio.file.Paths;
import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;

public class ActA600 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private static final int COMMIT_SIZE = 5000;
	private final String progname = "產生信用卡繳費通知推播檔(AI_STMT.TXT) 112/10/03 V1.00.01";
	private static final String CRM_FOLDER = "/media/act/";
	private static final String DATA_FORM = "AI_STMT.TXT";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	
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
			
			// get searchDate
			String parmDate = (args.length > 0 && args[0].length() == 8) ? args[0] :"";
			String searchDate = getProgDate(parmDate, "D");
			
			// get the name and the path of the .TXT file
			String datFileName = DATA_FORM;
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			// 產生主要檔案 .TXT 
			int resultCnt = generateDatFile(fileFolder, datFileName ,searchDate);

			dateTime(); // update the system date and time
			
			if(resultCnt == 0)
				return 0;
				
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
		selectActA600Data(searchDate);
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.TXT檔......");
			while (fetchTable()) {
				ActA600Data actA600Data = getInfData();
				String rowOfTXT = getRowOfTXT(actA600Data);
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
	private String getRowOfTXT(ActA600Data actA600Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft(actA600Data.idNo, 11)); //客戶ID X(11)
		sb.append(commCrd.fixLeft(String.valueOf(actA600Data.ttlAmtBal), 12));//總應繳金額 X(12)
		sb.append(commCrd.fixLeft(String.valueOf(actA600Data.minPay), 12));//最低繳款金額 X(12)
		sb.append(commCrd.fixRight("0" + commDate.toTwDate(actA600Data.thiLlastpayDate), 7));//繳款截止日 X(7)
		sb.append(commCrd.fixLeft(actA600Data.autopayAcctNo, 5));//扣款帳號末五碼 X(5)
		sb.append(commCrd.fixLeft(actA600Data.autopayIndicatorFlag, 1));//是否自動扣款 X(1)
		sb.append(commCrd.fixLeft(actA600Data.autopayAcctBank, 3));//扣款銀行代碼 X(3)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	ActA600Data getInfData() throws Exception {
		ActA600Data actA600Data = new ActA600Data();
		actA600Data.idNo = getValue("id_no");
		actA600Data.ttlAmtBal = getValueDouble("ttl_amt_bal");
		actA600Data.minPay = getValueDouble("min_pay");
		actA600Data.thiLlastpayDate = getValue("this_lastpay_date");
		actA600Data.autopayAcctNo = getValue("autopay_acct_no");
		actA600Data.autopayIndicatorFlag = getValue("autopay_indicator_flag");
		actA600Data.autopayAcctBank = getValue("autopay_acct_bank");
		return actA600Data;
	}

	private void selectActA600Data(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" select i.id_no, ")//--客戶ID
		.append(" decode(a.autopay_indicator,'','N','Y') as autopay_indicator_flag, ")//--是否自動扣款
		.append(" substr(a.autopay_acct_bank,1,3) as autopay_acct_bank, ")//--扣款銀行代碼
		.append(" substr(a.autopay_acct_no,12,5) as autopay_acct_no, ")//--扣款帳號末五碼
		.append(" t.ttl_amt_bal, t.min_pay, ") //--總應繳金額, 最低繳款金額
		.append(" w.this_close_date, w.this_lastpay_date, w.this_acct_month ")
		.append(" from act_acct t left join act_acno a ON t.p_seqno = a.p_seqno ")
		.append(" left join crd_idno i on a.id_p_seqno  = i.id_p_seqno ")
		.append(" join ptr_workday w on t.stmt_cycle = w.stmt_cycle ")
		.append(" where to_char(to_date(w.this_lastpay_date,'yyyymmdd') - 1 days,'yyyymmdd') = ? ")
		.append(" and t.ttl_amt_bal > 0 and a.acno_flag <> '2' ")
		.append(" order by t.acct_type,i.id_no ");
		setString(1,searchDate);
		sqlCmd = sb.toString();
		openCursor();
	}
	
	private 
	
	void procFTP(String fileFolder, String datFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 傳送TXT
		String ftpCommand = String.format("mput %s", datFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CRDATACREA", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void renameFile(String fileFolder1 , String datFileName1 ) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder1, "backup/", datFileName1 + "." + sysDate+sysTime).toString();

		if (commCrd.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + tmpstr1 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}

	public static void main(String[] args) {
		ActA600 proc = new ActA600();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class ActA600Data{
	String idNo = "";
	String autopayIndicatorFlag = "";
	String autopayAcctBank = "";
	String autopayAcctNo = "";
	double ttlAmtBal = 0;
	double minPay = 0;
	String thiLlastpayDate = "";
}




