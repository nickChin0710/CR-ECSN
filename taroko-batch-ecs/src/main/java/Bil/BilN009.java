/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/06/26  V1.00.00    Brian     program initial                           *
 *  109/12/03  V1.00.01    shiyuqi       updated for project coding standard   * 
 * 111/05/25  V1.00.02    Ryan       修改O953規格並傳送給財金                                                                     * 
******************************************************************************/

package Bil;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/* 傳送O953詐欺提報檔至NMIP */
public class BilN009 extends AccessDAO {

	private String progname = "傳送O953詐欺提報檔至NMIP 109/12/03  V1.00.01 ";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	String hCallBatchSeqno = "";

	String hArgv = "";
	String hDateFm = "";
	String hDateTo = "";
//	String hSystemPrev7Date = "";
//	String hSystemPrevDate = "";
	String hBusinessDate = "";
//	String hSystemMmddyy = "";
	long maxSendCnt = 300000;
	long totCnt = 0;
	long fileCnt = 0;
	long realCnt = 0;
//	String hSystemDate = "";
//	String hSystemYddd = "";
//	String hSystemDateF = "";
	int fileSeq = 0;
	String temstr1 = "";
	String str600 = "";
	double totAmt = 0;
	double hBlfrFraudAmt = 0;
	String hBlfrFunctionCode = "";
//    String hBlfrCardType = "";
	String hBlfrBinType = "";
	String hBlfrRealCardNo = "";
	String hBlfrFilmNo = "";
	String hBlfrPurchaseDate = "";
	String hBlfrFraudType = "";
	String hBlfrMerchantCategory = "";
	String hBlfrMerchantEngName = "";
	String hBlfrMerchantCity = "";
	String hBlfrMerchantZip = "";
	String hBlfrPosEntryMode = "";
	String hBlfrCrtDate = "";
	String hBlfrConfirmFlag = "";
	String hBlfrRowid = "";

	BufferedWriter out = null;
	BufferedWriter out1 = null;

	Buf13 dt1 = new Buf13();

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================

			if (args.length > 3) {
				comc.errExit("Usage : BilN009 [P/T] [from_date] [to_date]", "");
			}
			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			commonRtn();
			hArgv = "P";
//			hDateFm = hSystemPrev7Date;
//			hDateTo = hSystemPrevDate;
			hDateFm = hBusinessDate;
			hDateTo = hBusinessDate;

			if (args.length > 0) {
				hArgv = args[0];
				if (args.length == 2) {
					hDateFm = args[1];
					hDateTo = args[2];
				}
				if (args.length == 3) {
					hDateFm = args[1];
					hDateTo = args[2];
				}
			}
			showLogMessage("I", "",
					String.format("****  Process date =[%s]-[%s], max=[%d]", hDateFm, hDateTo, maxSendCnt));

			selectBilFraudReport();

			showLogMessage("I", "", String.format("\n程式執行結束"));
			showLogMessage("I", "", String.format("\n總筆數=[%d],檔案數=[%d]", totCnt, fileCnt));

			// ==============================================
			// 固定要做的
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***************************************************************************/
	void commonRtn() throws Exception {
		sqlCmd = "select business_date  ";
		sqlCmd += "  from ptr_businday ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		hBusinessDate = getValue("business_date");

//        hSystemMmddyy = "";
//        hSystemPrevDate = "";
//
//        sqlCmd = " select to_char(sysdate,'yyyymmdd')   h_system_date, ";
//        sqlCmd += "        to_char(sysdate,'mmddyy')     h_system_mmddyy, ";
//        sqlCmd += "        to_char(sysdate,'YDDD')       h_system_yddd, ";
//        sqlCmd += "        to_char(sysdate-1,'yyyymmdd') h_system_prev_date, ";
//        sqlCmd += "        to_char(sysdate-7,'yyyymmdd') h_system_prev_7_date, ";
//        sqlCmd += "        to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
//        sqlCmd += "   from dual ";
//        int recordCnt = selectTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("select_dual not found!", "", hCallBatchSeqno);
//        }
//        if (recordCnt > 0) {
//            hSystemDate = getValue("h_system_date");
//            hSystemMmddyy = getValue("h_system_mmddyy");
//            hSystemYddd = getValue("h_system_yddd");
//            hSystemPrevDate = getValue("h_system_prev_date");
//            hSystemPrev7Date = getValue("h_system_prev_7_date");
//            hSystemDateF = getValue("h_system_date_f");
//        }
	}

	/***************************************************************************/
	void checkOpen() throws IOException {
		String tFileName = "";

		fileSeq++;
//		tFileName = String.format("O953017%-8.8s%02dC", sysDate, fileSeq);
		tFileName = String.format("O953006%-8.8s%02d", sysDate, fileSeq);
		temstr1 = String.format("%s/media/bil/%s", comc.getECSHOME(), tFileName);

		out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temstr1), "MS950"));

		showLogMessage("I", "", String.format(" Write file=[%s]", temstr1));

		recordHead();
	}

	/***************************************************************************/
	void recordHead() throws IOException {
//        out.write(String.format("FH0171117%-6.6s%02d%-1.1s%-252.252s%s", hSystemDate.substring(2), fileSeq, hArgv, " ", "\n"));
		out.write(String.format("FH0061039%-6.6s%02d%-1.1s%-252.252s%s", sysDate.substring(2), fileSeq, hArgv, " ",
				"\n"));
	}

	/***************************************************************************/
	void recordTail() throws Exception {
		if (realCnt > maxSendCnt)
			realCnt = maxSendCnt;
		str600 = String.format("FT%06d%015.0f%247.247s%s", realCnt, totAmt, " ", "\n");
		out.write(str600);

		out.close();
		ftpMput();
		realCnt = 1;
		fileCnt++;
	}

	/***************************************************************************/
	void selectBilFraudReport() throws Exception {

		sqlCmd = "  SELECT  function_code, ";
//        sqlCmd += "          card_type, "; //notfound
		sqlCmd += "          bin_type, ";
		sqlCmd += "          card_no, ";
		sqlCmd += "          film_no, ";
		sqlCmd += "          purchase_date, ";
		sqlCmd += "          fraud_type, ";
		sqlCmd += "          fraud_amt, ";
		sqlCmd += "          mcht_category, ";
		sqlCmd += "          mcht_eng_name, ";
		sqlCmd += "          mcht_city, ";
		sqlCmd += "          mcht_zip, ";
		sqlCmd += "          pos_entry_mode, ";
		sqlCmd += "          crt_date, ";
		sqlCmd += "          apr_flag, ";
		sqlCmd += "          rowid rowid ";
		sqlCmd += "  FROM bil_fraud_report ";
		sqlCmd += " WHERE crt_date between ? and ? ";
		sqlCmd += "   and (apr_flag = '' or apr_flag = 'N') ";
        setString(1, hDateFm);
        setString(2, hDateTo);
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {
			hBlfrFunctionCode = getValue("function_code");
//            h_blfr_card_type = getValue("card_type");
			hBlfrBinType = getValue("bin_type");
			hBlfrRealCardNo = getValue("card_no");
			hBlfrFilmNo = getValue("film_no");
			hBlfrPurchaseDate = getValue("purchase_date");
			hBlfrFraudType = getValue("fraud_type");
			hBlfrFraudAmt = getValueDouble("fraud_amt");
			hBlfrMerchantCategory = getValue("mcht_category");
			hBlfrMerchantEngName = getValue("mcht_eng_name");
			hBlfrMerchantCity = getValue("mcht_city");
			hBlfrMerchantZip = getValue("mcht_zip");
			hBlfrPosEntryMode = getValue("pos_entry_mode");
			hBlfrCrtDate = getValue("crt_date");
			hBlfrConfirmFlag = getValue("apr_flag");
			hBlfrRowid = getValue("rowid");

			totCnt++;
			totAmt += hBlfrFraudAmt;

			if (totCnt % 10000 == 0 || totCnt == 1) {
				showLogMessage("I", "", String.format("Process record=[%d]", totCnt));
			}
			realCnt++;

			if (realCnt == 1 || realCnt == maxSendCnt + 1) {
				if (realCnt == maxSendCnt + 1) {
					recordTail();
				}
				checkOpen();
			}
			updateBilFraudReport();
			record1Rtn();
		}
		closeCursor(cursorIndex);

		if (realCnt > 0) {
			recordTail();
		}
	}

	/*************************************************************************/
	void updateBilFraudReport() throws Exception {
		daoTable = "bil_fraud_report";
		updateSQL = " apr_flag     = 'Y', ";
		updateSQL += " mod_time     = sysdate ";
		whereStr = " where rowid  = ? ";
		setRowId(1, hBlfrRowid);
		updateTable();

		if (notFound.equals("Y")) {
			comcr.errRtn("update_bil_fraud_report not found!", "", hCallBatchSeqno);
		}
	}

	/*************************************************************************/
	void record1Rtn() throws UnsupportedEncodingException, IOException {
		dt1.functionCode = hBlfrFunctionCode;
//        dt1.cardType = hBlfrCardType;
		dt1.binType = hBlfrBinType;
		dt1.realCardNo = hBlfrRealCardNo;
		dt1.filmNo = hBlfrFilmNo;
		dt1.purchaseDate = hBlfrPurchaseDate;
		dt1.fraudType = hBlfrFraudType;
		dt1.fraudAmt = String.format("%012.0f", hBlfrFraudAmt);
		dt1.merchantCategory = hBlfrMerchantCategory;
		dt1.merchantEngName = hBlfrMerchantEngName;
		dt1.merchantCity = hBlfrMerchantCity;
		dt1.merchantZip = hBlfrMerchantZip;
		dt1.posEntryMode = hBlfrPosEntryMode;

		out.write(String.format("%-270.270s%s", dt1.allText(), "\n"));
	}

	/***************************************************************************/
	int ftpMput() throws Exception {

		// ======================================================
		// FTP

		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值(必要) */
		commFTP.hEflgSystemId = "BilN009"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEflgGroupId = "BilN009"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
//        commFTP.hEflgSourceFrom = "NCCC_CARD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/bil/", comc.getECSHOME());
		commFTP.hEflgModPgm = this.getClass().getName();
//        String hEflgRefIpCode  = "NMIP_IP_O952";
		String hEflgRefIpCode = "NCR2FISC";

//        System.setProperty("user.dir", commFTP.hEriaLocalDir);

//        /* * O95301 * */
//        String nmipStartFile = String.format("%s", "O95301");
//        writeFile(commFTP.hEriaLocalDir + nmipStartFile, nmipStartFile);
//        String procCode = String.format("put %s", nmipStartFile);
//        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");
//        int errCode1 = commFTP.ftplogName(hEflgRefIpCode, procCode);
//
//        /* * O953FILE * */
//        procCode = String.format("put O953017%-8.8s%02dC", hSystemDate, fileSeq);
//        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");
//        int errCode3 = commFTP.ftplogName(hEflgRefIpCode, procCode);

//        /* * O95300 * */
//        String nmipEndFile = String.format("%s", "O95300");
//        writeFile(commFTP.hEriaLocalDir + nmipEndFile, nmipEndFile);
//        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");
//        procCode = String.format("put %s", nmipEndFile);
//        int errCode2 = commFTP.ftplogName(hEflgRefIpCode, procCode);

		/* * O953006 * */
		String fileName = String.format("O953006%-8.8s%02d", sysDate, fileSeq);
		showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2FISC", "mput " + fileName);

//        if (errCode1 != 0 && errCode2 != 0 && errCode3 != 0) {
		if (errCode != 0) {
			showLogMessage("I", "", String.format(String.format("[BilN009] FTP [%s] 無法連線 error!", hEflgRefIpCode)));
			return (-1);
		}

		// ==================================================

		return (0);
	}

	/*************************************************************************/
	int writeFile(String filename, String data) throws IOException {

		out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "MS950"));

		out1.write(String.format("file_name[%s]\n", data));
		out1.close();
		return (0);
	}

	/***************************************************************************/
	public static void main(String[] args) {
		BilN009 proc = new BilN009();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf13 { /* O953 詐欺提報檔 */
		String functionCode = "";
//        String cardType = "";
		String binType = "";
		String realCardNo = "";
		String filmNo = "";
		String purchaseDate = "";
		String fraudType = "";
		String fraudAmt = "";
		String merchantCategory = "";
		String merchantEngName = "";
		String merchantCity = "";
		String merchantZip = "";
		String posEntryMode = "";

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(functionCode, 1);
//            rtn += comc.fixLeft(cardType, 1);
			rtn += comc.fixLeft(binType, 1);
			rtn += comc.fixLeft(realCardNo, 19);
			rtn += comc.fixLeft(filmNo, 23);
			rtn += comc.fixLeft(purchaseDate, 8);
			rtn += comc.fixLeft(fraudType, 2);
			rtn += comc.fixLeft(fraudAmt, 12);
			rtn += comc.fixLeft(merchantCategory, 4);
			rtn += comc.fixLeft(merchantEngName, 25);
			rtn += comc.fixLeft(merchantCity, 13);
			rtn += comc.fixLeft(merchantZip, 5);
			rtn += comc.fixLeft(posEntryMode, 3);
			return rtn;
		}
	}

}
