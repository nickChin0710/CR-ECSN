/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/09/27  V1.00.00   Ryan     program initial                            *
*  112/10/03  V1.00.01   Ryan     修正FTP-->CRDATACREA                        *                                                                                        *
*****************************************************************************/
package Act;

import java.nio.file.Paths;
import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;

public class ActA610 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private static final int COMMIT_SIZE = 5000;
	private final String progname = "產生信用卡繳費入帳通知推播檔(AI_PYMT.TXT) 112/10/03 V1.00.01";
	private static final String CRM_FOLDER = "/media/act/";
	private static final String DATA_FORM = "AI_PYMT.TXT";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommString commStr = new CommString();
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
		selectActA610Data(searchDate);
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.TXT檔......");
			while (fetchTable()) {
				ActA610Data actA610Data = getInfData();
				String rowOfTXT = getRowOfTXT(actA610Data);
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
	private String getRowOfTXT(ActA610Data actA610Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft(actA610Data.idNo, 11)); //客戶ID X(11)
		sb.append(commCrd.fixLeft(String.format("%,.2f", actA610Data.sumDcPaymentAmt), 17));//繳款金額 X(17)
		sb.append(commCrd.fixRight("0" + commDate.toTwDate(actA610Data.paymentDate), 7));//繳款日期 X(7)
		sb.append(commCrd.fixLeft(commStr.decode(actA610Data.currCode,",901,840,392", ",TWD,USD,JPY"),3)); //幣別 X(3)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	ActA610Data getInfData() throws Exception {
		ActA610Data actA610Data = new ActA610Data();
		actA610Data.idNo = getValue("id_no");
		actA610Data.sumDcPaymentAmt = getValueDouble("sum_dc_payment_amt");
		actA610Data.paymentDate = getValue("payment_date");
		actA610Data.currCode = getValue("curr_code");
		return actA610Data;
	}

	private void selectActA610Data(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT c.id_no, ")//--客戶ID
		.append(" a.payment_date,a.crt_date,a.curr_code, a.payment_type, ")//--繳款日期 ,幣別
		.append(" sum(a.dc_payment_amt) as sum_dc_payment_amt ") //繳款金額
		.append(" FROM cyc_pyaj a ,act_acno b ,crd_idno c ")
		.append(" WHERE a.CLASS_CODE IN ('P') ")
		.append(" AND a.PAYMENT_TYPE NOT IN ('REFU') ")
		.append(" AND a.DC_PAYMENT_AMT > 0 ")
		.append(" AND a.CRT_DATE = ? ")
		.append(" and a.p_seqno = b.p_seqno ")
		.append(" and c.id_p_seqno = b.id_p_seqno ")
		.append(" GROUP BY c.id_no,a.PAYMENT_DATE,a.CRT_DATE,a.CURR_CODE,a.PAYMENT_TYPE ")
		.append(" ORDER BY c.id_no,a.PAYMENT_DATE,a.CRT_DATE,a.CURR_CODE,a.PAYMENT_TYPE ");
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
		ActA610 proc = new ActA610();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class ActA610Data{
	String idNo = "";
	double sumDcPaymentAmt = 0;
	String paymentDate = "";
	String currCode = "";
}




