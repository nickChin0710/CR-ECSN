/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/11/03  V1.00.01   JeffKung     program initial                        *
*  112/12/03  V1.00.02   JeffKung     修改換行符號為\r\n
*****************************************************************************/
package Inf;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxInf;

public class InfR016 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 5000;
	private final String progname = "產生送卡部-信用卡當日交易資料檔程式  112/12/03 V1.00.02";
	private static final String CRM_FOLDER = "/cr/ecs/media/crm/";
	private static final String DATA_FORM = "DAILY_TXN_3144";
	private final String lineSeparator = "\r\n";  //改成CRLF

	CommCrd commCrd = new CommCrd();
	CommFunction comm = new CommFunction();

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
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));

			// 取前一日的日期(日曆日)
			if ("".equals(searchDate)) {
				searchDate = getProgDate(searchDate, "D");
				searchDate = comm.nextNDate(searchDate, -1);
			}

			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

			// convert YYYYMMDD into YYMMDD
			String fileNameSearchDate = searchDate.substring(2);

			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
			String fileFolder = Paths.get(CRM_FOLDER).toString();

			// 產生主要檔案 .DAT
			int dataCount = generateDatFile(fileFolder, datFileName, searchDate);

			procFTP(fileFolder, datFileName);

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
	 * 
	 * @param fileFolder  檔案的資料夾路徑
	 * @param datFileName .dat檔的檔名
	 * @return the number of rows written. If the returned value is -1, it means the
	 *         path or the file does not exist.
	 * @throws Exception
	 */
	private int generateDatFile(String fileFolder, String datFileName, String searchDate) throws Exception {

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

			// 處理bil_bill
			showLogMessage("I", "", "開始處理bil_bill檔......");
			selectBilBillData(searchDate);
			while (fetchTable()) {

				String rowOfDAT = getRowOfDAT();
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
			closeCursor();

			// write the rest of bytes on the file
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes("MS950");
				writeBinFile(tmpBytes, tmpBytes.length);
			}

			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入.DAT檔");
			} else {
				showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
			}

		} finally {
			closeBinaryOutput();
		}

		return rowCount;
	}

	/**
	 * 產生檔案
	 * 
	 * @return String
	 * @throws Exception
	 */
	private String getRowOfDAT() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft(getValue("ACCT_TYPE"), 2));
		sb.append(commCrd.fixLeft(getValue("GROUP_CODE"), 4));
		sb.append(commCrd.fixLeft(getValue("CARD_NO"), 16));
		sb.append(commCrd.fixLeft(getValue("PURCHASE_DATE"), 8));
		sb.append(commCrd.fixLeft(getValue("POST_DATE"), 8));
		sb.append(commCrd.fixLeft(getValue("BILL_TYPE"), 4));
		sb.append(commCrd.fixLeft(getValue("TXN_CODE"), 2));
		sb.append(commCrd.fixLeft(getValue("SIGN_FLAG"), 1));
		sb.append(commCrd.fixLeft(String.format("%014.2f", getValueDouble("DEST_AMT")), 14));
		sb.append(commCrd.fixLeft(getValue("DEST_CURR"), 3));
		sb.append(commCrd.fixLeft(String.format("%014.2f", getValueDouble("SOURCE_AMT")), 14));
		sb.append(commCrd.fixLeft(getValue("SOURCE_CURR"), 3));
		sb.append(commCrd.fixLeft(String.format("%014.2f", getValueDouble("DC_DEST_AMT")), 14));
		sb.append(commCrd.fixLeft(getValue("CURR_CODE"), 3));
		sb.append(commCrd.fixLeft(String.format("%08d", getValueInt("DEDUCT_BP")), 8));
		sb.append(commCrd.fixLeft(String.format("%014.2f", getValueDouble("CASH_PAY_AMT")), 14));
		sb.append(commCrd.fixLeft(getValue("MCHT_NO"), 20));
		sb.append(commCrd.fixLeft(getValue("MCHT_COUNTRY"), 3));
		sb.append(commCrd.fixLeft(getValue("MCHT_CATEGORY"), 4));
		sb.append(commCrd.fixLeft(getValue("POS_ENTRY_MODE"), 3));
		sb.append(commCrd.fixLeft(getValue("AUTH_CODE"), 6));
		sb.append(commCrd.fixLeft(getValue("FILM_NO"), 23));
		sb.append(commCrd.fixLeft(getValue("EC_IND"), 1));
		sb.append(commCrd.fixLeft(getValue("UCAF"), 3));
		sb.append(commCrd.fixLeft(getValue("BILL_INFO"), 45));
		sb.append(commCrd.fixLeft(getValue("PAYMENT_TYPE"), 1));
		sb.append(commCrd.fixLeft(getValue("DCC_IND"), 2));
		sb.append(commCrd.fixLeft(getValue("MCHT_ENG_NAME"), 25));
		sb.append(commCrd.fixLeft(getValue("ECS_PLATFORM_KIND"), 2));
		sb.append(commCrd.fixLeft(getValue("MCHT_CHI_NAME"), 40));
		sb.append(lineSeparator);

		return sb.toString();
	}

	private void selectBilBillData(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT  ");
		sb.append(" a.ACCT_TYPE,a.GROUP_CODE,a.CARD_NO,a.PURCHASE_DATE,a.POST_DATE,");
		sb.append(" a.bill_type,a.TXN_CODE,a.sign_flag,a.DEST_AMT,a.dest_curr,");
		sb.append(" a.source_amt,a.SOURCE_CURR,a.DC_DEST_AMT,a.CURR_CODE,");
		sb.append(" a.DEDUCT_BP,a.CASH_PAY_AMT,");
		sb.append(" a.MCHT_NO,a.MCHT_COUNTRY,a.MCHT_CATEGORY,a.POS_ENTRY_MODE,");
		sb.append(" a.AUTH_CODE,a.FILM_NO,a.EC_IND,a.UCAF,nvl(f.REIMB_INFO,'') bill_info,");
		sb.append(" a.payment_type,nvl(f.DCC_IND,'') dcc_ind,");
		sb.append(" a.MCHT_ENG_NAME,a.ECS_PLATFORM_KIND,a.MCHT_CHI_NAME,");
		sb.append(" 'BIL_BILL' AS DATA_FROM_TABLE");
		sb.append("     FROM BIL_BILL a  LEFT JOIN bil_fiscdtl f on a.REFERENCE_NO=f.ECS_REFERENCE_NO ");
		sb.append("     WHERE 1=1 ");
		sb.append("     AND a.RSK_TYPE NOT IN ('1','2','3') "); // 落問交的資料要踢除
		sb.append("     AND a.POST_DATE = ? ");
		sb.append("     AND a.ACCT_CODE IN ('BL','CA','IT') "); // 只下本金類
		sqlCmd = sb.toString();
		setString(1, searchDate); // 批次處理日期
		openCursor();
	}

	void procFTP(String fileFolder, String datFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		String ftpCommand = String.format("mput %s", datFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CREDITCARD", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	public static void main(String[] args) {
		InfR016 proc = new InfR016();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}
