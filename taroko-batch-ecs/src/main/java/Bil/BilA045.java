/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/03/08  V1.00.01  JeffKung    program initial                           *
*  112/07/19  V1.00.02  JeffKung    調整email長度                                                           *
******************************************************************************/

package Bil;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxInf;

public class BilA045 extends AccessDAO {
	private final String progname = "產生送郵件消費通知-信用卡當日交易資料檔程式  112/07/19 V1.00.02";
	private static final String BIL_FOLDER = "media/bil/";
	private static final String DATA_FORM = "PYMT_EMAIL";
	private final String lineSeparator = System.lineSeparator();
	String keepIdNo = "";

	CommCrd comc = new CommCrd();
	CommFunction comm = new CommFunction();

	public int mainProcess(String[] args) {

		try {
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
			searchDate = getProgDate(searchDate, "D");
			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

			// convert YYYYMMDD
			String fileNameSearchDate = searchDate;
			String datFileName = "";
			String fileFolder = "";
			int dataCount = 0;

			//get the name and the path of the CARDCM_YYYYMMDD.DAT file
			datFileName = String.format("%s_%s.%s", "CARDCM", fileNameSearchDate, "dat");
			fileFolder = Paths.get(comc.getECSHOME(), BIL_FOLDER).toString();
			dataCount = generateDatFileCardCM(fileFolder, datFileName, searchDate);
			procFTP(fileFolder, datFileName,"CRDATACREA");
			
			//get the name and the path of the VDCM_YYYYMMDD.DAT file
			datFileName = String.format("%s_%s.%s", "VDCM", fileNameSearchDate, "dat");
			fileFolder = Paths.get(comc.getECSHOME(), BIL_FOLDER).toString();
			dataCount = generateDatFileVDCM(fileFolder, datFileName, searchDate);
			procFTP(fileFolder, datFileName,"CRDATACREA");

			// get the name and the path of the .TXT file (PYMT_EMAIL)
			datFileName = String.format("%s_%s.%s", DATA_FORM, fileNameSearchDate, "TXT");
			fileFolder = Paths.get(comc.getECSHOME(), BIL_FOLDER).toString();
			dataCount = generateDatFile(fileFolder, datFileName, searchDate);
			procFTP(fileFolder, datFileName,"CRDATACREA");
			
			procFTP(fileFolder, datFileName,"CREDITCARD");

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
	
	private int generateDatFileCardCM(String fileFolder, String datFileName, String searchDate) throws Exception {

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
			showLogMessage("I", "", "開始產生CARDCM_YYYYMMDD.dat檔......");
			selectBilBillData(searchDate);
			while (fetchTable()) {

				if ("".equals(getValue("id_no"))) {
					continue; // 沒有email_addr的id bypass
				}

				rowCount++;
				getRowOfDAT();

			}
			closeCursor();

			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入CARDCM_YYYYMMDD.dat檔");
			} else {
				showLogMessage("I", "", String.format("產生CARDCM_YYYYMMDD.dat檔完成！，共產生%d筆資料", rowCount));
			}

		} finally {
			closeBinaryOutput();
		}

		return rowCount;
	}

	private int generateDatFileVDCM(String fileFolder, String datFileName, String searchDate) throws Exception {

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
			showLogMessage("I", "", "開始產生VDCM_YYYYMMDD.dat檔......");

			selectDbbBillData(searchDate);
			
			while (fetchTable()) {

				if ("".equals(getValue("id_no"))) {
					continue; // 沒有email_addr的id bypass
				}

				rowCount++;
				getRowOfDAT();

			}
			closeCursor();

			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入VDCM_YYYYMMDD.dat檔");
			} else {
				showLogMessage("I", "", String.format("產生VDCM_YYYYMMDD.dat檔完成！，共產生%d筆資料", rowCount));
			}

		} finally {
			closeBinaryOutput();
		}

		return rowCount;
	}

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
			showLogMessage("I", "", "開始產生PYMT_EMAIL_YYYYMMDD.TXT檔......");

			// 處理bil_bill
			showLogMessage("I", "", "開始處理bil_bill檔......");
			selectBilBillData(searchDate);
			while (fetchTable()) {

				if ("".equals(getValue("id_no"))) {
					continue; // 沒有email_addr的id bypass
				}

				rowCount++;
				getRowOfTXT();

			}
			closeCursor();

			// 處理dbb_bill
			showLogMessage("I", "", "開始處理dbb_bill檔......");
			selectDbbBillData(searchDate);
			
			while (fetchTable()) {

				if ("".equals(getValue("id_no"))) {
					continue; // 沒有email_addr的id bypass
				}

				rowCount++;
				getRowOfTXT();

			}
			closeCursor();

			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入PYMT_EMAIL_YYYYMMDD.TXT檔");
			} else {
				showLogMessage("I", "", String.format("產生PYMT_EMAIL_YYYYMMDD.TXT檔完成！，共產生%d筆資料", rowCount));
			}

		} finally {
			closeBinaryOutput();
		}

		return rowCount;
	}

	/**
	 * 產生檔案.dat
	 * 
	 * @return String
	 * @throws Exception
	 */
	private void getRowOfDAT() throws Exception {

		String dataFrom = "";
		String acctType = "";
		String groupCode = "";
		String txnCode = "";
		String cardNo = "";
		String purchaseDate = "";
		String postDate = "";
		String destAmt = "";
		String sourceAmt = "";
		String dcDestAmt = "";
		String mchtCategory = "";
		String currCode = "";
		String sourceCurr = "";
		String mchtEngName = "";
		String mchtChiName = "";
		String authCode = "";
		String mchtNo = "";
		String contractNo = "";
		String majoridNo = "";
		String idNo = "";
		double doubleDestAmt = 0;
		double doubleSourceAmt = 0;
		double doubleDcDestAmt = 0;

		if (!keepIdNo.equals(getValue("ID_NO"))) {
			if ("BIL_BILL".equals(getValue("DATA_FROM_TABLE"))) {
				selectCrdIdno();
				keepIdNo = getValue("ID_NO");
			} else {
				selectDbcIdno();
				keepIdNo = getValue("ID_NO");
			}

			StringBuffer sb = null;
			
			if ("BIL_BILL".equals(getValue("DATA_FROM_TABLE"))) {
				sb = new StringBuffer();
				sb.append(comc.fixLeft("00", 2));
				sb.append(comc.fixLeft("|&", 2));
				sb.append(comc.fixLeft(keepIdNo, 11));
				sb.append(comc.fixLeft("", 153));

				sb.append("\r\n");

			} else {
				sb = new StringBuffer();
				sb.append(comc.fixLeft("00", 2));
				sb.append(comc.fixLeft("|&", 2));
				sb.append(comc.fixLeft(keepIdNo, 11));
				sb.append(comc.fixLeft("", 129));

				sb.append("\r\n");
			}
			
			byte[] tmpBytes = sb.toString().getBytes("MS950");
			writeBinFile(tmpBytes, tmpBytes.length);
		}

		idNo = getValue("ID_NO");
		acctType = getValue("ACCT_TYPE");
		groupCode = getValue("GROUP_CODE");
		txnCode = getValue("TXN_CODE");
		cardNo = getValue("CARD_NO");
		purchaseDate = getValue("PURCHASE_DATE");
		purchaseDate = (comc.str2int(purchaseDate.substring(0, 4)) - 1911) + purchaseDate.substring(4);
		postDate = getValue("POST_DATE");
		postDate = (comc.str2int(postDate.substring(0, 4)) - 1911) + postDate.substring(4);
		if ("-".equals(getValue("sign_flag"))) {
			doubleDestAmt = getValueDouble("DEST_AMT") * -1;
			doubleSourceAmt = getValueDouble("SOURCE_AMT") * -1;
			doubleDcDestAmt = getValueDouble("DC_DEST_AMT") * -1;
		} else {
			doubleDestAmt = getValueDouble("DEST_AMT");
			doubleSourceAmt = getValueDouble("SOURCE_AMT");
			doubleDcDestAmt = getValueDouble("DC_DEST_AMT");
		}

		destAmt = String.format("%-,17.2f", doubleDestAmt).trim();
		sourceAmt = String.format("%-,17.2f", doubleSourceAmt).trim();
		dcDestAmt = String.format("%-,17.2f", doubleDcDestAmt).trim();

		mchtCategory = getValue("MCHT_CATEGORY");
		currCode = getValue("CURR_CODE");
		if ("901".equals(currCode)) {
			currCode = "TWN";
		} else if ("840".equals(currCode)) {
			currCode = "USD";
		} else if ("392".equals(currCode)) {
			currCode = "JPY";
		}

		sourceCurr = getValue("curr_eng_name");

		mchtEngName = getValue("MCHT_ENG_NAME");
		mchtChiName = getValue("MCHT_CHI_NAME");
		authCode = getValue("AUTH_CODE");
		mchtNo = getValue("MCHT_NO");
		contractNo = getValue("CONTRACT_NO");

		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("01", 2));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(getValue("idno.e_mail_addr"), 50));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(cardNo, 16));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(purchaseDate, 7)); // 民國年月日
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft("", 6)); // 放空白
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(sourceCurr, 3));   //VD如果非國內交易才放原始交易幣別 
		sb.append(comc.fixLeft("|&", 2));
		if ("DBB_BILL".equals(getValue("DATA_FROM_TABLE")) && "TWD".equals(sourceCurr)) {
			sb.append(comc.fixRight(destAmt, 17));  //VD如果國內交易放台幣金額
		} else {
			sb.append(comc.fixRight(sourceAmt, 17));  //VD如果非國內交易才放原始交易金額
		}
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(postDate, 7));
		sb.append(comc.fixLeft("|&", 2));
		if (mchtChiName.length() > 0) {
			sb.append(comc.fixLeft(mchtChiName, 40));
		} else {
			sb.append(comc.fixLeft(mchtEngName, 40));
		}
		
		//信用卡才有這兩個欄位,VD沒有
		if ("BIL_BILL".equals(getValue("DATA_FROM_TABLE"))) {
			sb.append(comc.fixLeft("|&", 2));
			sb.append(comc.fixLeft(currCode, 3));
			sb.append(comc.fixLeft("|&", 2));
			sb.append(comc.fixRight(dcDestAmt, 17));
		}

		sb.append("\r\n");

		byte[] tmpBytes = sb.toString().getBytes("MS950");
		writeBinFile(tmpBytes, tmpBytes.length);
		

		/* 同ID只寫一筆
		if ("BIL_BILL".equals(getValue("DATA_FROM_TABLE"))) {
			sb = new StringBuffer();
			sb.append(comc.fixLeft(keepIdNo, 11));
			sb.append(comc.fixLeft("00", 2));
			sb.append(comc.fixLeft("|&", 2));
			sb.append(comc.fixLeft(keepIdNo, 11));
			sb.append(comc.fixLeft("", 153));

			sb.append("\r\n");

		} else {
			sb = new StringBuffer();
			sb.append(comc.fixLeft(keepIdNo, 11));
			sb.append(comc.fixLeft("00", 2));
			sb.append(comc.fixLeft("|&", 2));
			sb.append(comc.fixLeft(keepIdNo, 11));
			sb.append(comc.fixLeft("", 129));

			sb.append("\r\n");
		}

		tmpBytes = sb.toString().getBytes("MS950");
		writeBinFile(tmpBytes, tmpBytes.length);
		*/

	}
	
	/**
	 * 產生檔案.TXT
	 * 
	 * @return String
	 * @throws Exception
	 */
	private void getRowOfTXT() throws Exception {

		String dataFrom = "";
		String acctType = "";
		String groupCode = "";
		String txnCode = "";
		String cardNo = "";
		String purchaseDate = "";
		String postDate = "";
		String destAmt = "";
		String sourceAmt = "";
		String dcDestAmt = "";
		String mchtCategory = "";
		String currCode = "";
		String sourceCurr = "";
		String mchtEngName = "";
		String mchtChiName = "";
		String authCode = "";
		String mchtNo = "";
		String contractNo = "";
		String majoridNo = "";
		String idNo = "";
		double doubleDestAmt = 0;
		double doubleSourceAmt = 0;
		double doubleDcDestAmt = 0;

		if (!keepIdNo.equals(getValue("ID_NO"))) {
			if ("BIL_BILL".equals(getValue("DATA_FROM_TABLE"))) {
				selectCrdIdno();
				keepIdNo = getValue("ID_NO");
			} else {
				selectDbcIdno();
				keepIdNo = getValue("ID_NO");
			}

			StringBuffer sb = new StringBuffer();
			sb.append(comc.fixLeft("00", 2));
			sb.append(comc.fixLeft("|&", 2));
			sb.append(comc.fixLeft(keepIdNo, 11));
			sb.append(comc.fixLeft("", 153));

			sb.append("\r\n");

			byte[] tmpBytes = sb.toString().getBytes("MS950");
			writeBinFile(tmpBytes, tmpBytes.length);
		}

		idNo = getValue("ID_NO");
		acctType = getValue("ACCT_TYPE");
		groupCode = getValue("GROUP_CODE");
		txnCode = getValue("TXN_CODE");
		cardNo = getValue("CARD_NO");
		purchaseDate = getValue("PURCHASE_DATE");
		purchaseDate = (comc.str2int(purchaseDate.substring(0, 4)) - 1911) + purchaseDate.substring(4);
		postDate = getValue("POST_DATE");
		postDate = (comc.str2int(postDate.substring(0, 4)) - 1911) + postDate.substring(4);
		if ("-".equals(getValue("sign_flag"))) {
			doubleDestAmt = getValueDouble("DEST_AMT") * -1;
			doubleSourceAmt = getValueDouble("SOURCE_AMT") * -1;
			doubleDcDestAmt = getValueDouble("DC_DEST_AMT") * -1;
		} else {
			doubleDestAmt = getValueDouble("DEST_AMT");
			doubleSourceAmt = getValueDouble("SOURCE_AMT");
			doubleDcDestAmt = getValueDouble("DC_DEST_AMT");
		}

		destAmt = String.format("%-,17.2f", doubleDestAmt).trim();
		sourceAmt = String.format("%-,17.2f", doubleSourceAmt).trim();
		dcDestAmt = String.format("%-,17.2f", doubleDcDestAmt).trim();

		mchtCategory = getValue("MCHT_CATEGORY");
		currCode = getValue("CURR_CODE");
		if ("901".equals(currCode)) {
			currCode = "TWN";
		} else if ("840".equals(currCode)) {
			currCode = "USD";
		} else if ("392".equals(currCode)) {
			currCode = "JPY";
		}

		sourceCurr = getValue("curr_eng_name");

		mchtEngName = getValue("MCHT_ENG_NAME");
		mchtChiName = getValue("MCHT_CHI_NAME");
		authCode = getValue("AUTH_CODE");
		mchtNo = getValue("MCHT_NO");
		contractNo = getValue("CONTRACT_NO");

		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("01", 2));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(getValue("idno.e_mail_addr"), 50));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(cardNo, 16));
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(purchaseDate, 7)); // 民國年月日
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft("", 6)); // 放空白
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(sourceCurr, 3));   //VD如果非國內交易才放原始交易幣別 
		sb.append(comc.fixLeft("|&", 2));
		if ("DBB_BILL".equals(getValue("DATA_FROM_TABLE")) && "TWD".equals(sourceCurr)) {
			sb.append(comc.fixRight(destAmt, 17));  //VD如果國內交易放台幣金額
		} else {
			sb.append(comc.fixRight(sourceAmt, 17));  //VD如果非國內交易才放原始交易金額
		}
		sb.append(comc.fixLeft("|&", 2));
		sb.append(comc.fixLeft(postDate, 7));
		sb.append(comc.fixLeft("|&", 2));
		if (mchtChiName.length() > 0) {
			sb.append(comc.fixLeft(mchtChiName, 40));
		} else {
			sb.append(comc.fixLeft(mchtEngName, 40));
		}
		
		//信用卡才有這兩個欄位,VD沒有
		if ("BIL_BILL".equals(getValue("DATA_FROM_TABLE"))) {
			sb.append(comc.fixLeft("|&", 2));
			sb.append(comc.fixLeft(currCode, 3));
			sb.append(comc.fixLeft("|&", 2));
			sb.append(comc.fixRight(dcDestAmt, 17));
		} else {
			sb.append(comc.fixLeft("", 24));  //VD放空白
		}

		sb.append("\r\n");

		byte[] tmpBytes = sb.toString().getBytes("MS950");
		writeBinFile(tmpBytes, tmpBytes.length);

	}

	private void selectBilBillData(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT  ");
		sb.append(" decode(acct_type,'01', ");
		sb.append("    decode(nvl((SELECT id_no FROM crd_idno c WHERE a.id_p_seqno = c.id_p_seqno AND c.e_mail_addr LIKE '%@%' ),''),'',  ");
		sb.append("    nvl((SELECT id_no FROM crd_idno d WHERE a.major_id_p_seqno = d.id_p_seqno AND d.e_mail_addr LIKE '%@%' ),''),  ");
		sb.append("    (SELECT id_no FROM crd_idno c WHERE a.id_p_seqno = c.id_p_seqno AND c.e_mail_addr LIKE '%@%' )), ");
		sb.append("    decode(nvl((SELECT id_no FROM crd_idno c WHERE a.id_p_seqno = c.id_p_seqno AND c.e_mail_addr LIKE '%@%' ),''),'', ");
		sb.append("    nvl((SELECT id_no||'R' FROM crd_idno d WHERE a.major_id_p_seqno = d.id_p_seqno AND d.e_mail_addr LIKE '%@%' ),''), ");
		sb.append("    (SELECT id_no||'R' FROM crd_idno c WHERE a.id_p_seqno = c.id_p_seqno AND c.e_mail_addr LIKE '%@%' ))) AS id_no, ");
		sb.append(" ACCT_TYPE,GROUP_CODE,TXN_CODE,CARD_NO,PURCHASE_DATE,POST_DATE,DEST_AMT,sign_flag,");
		sb.append(" MCHT_CATEGORY,CURR_CODE,MCHT_ENG_NAME,MCHT_CHI_NAME,AUTH_CODE,");
		sb.append(" nvl((select curr_eng_name from ptr_currcode e where e.curr_code = a.SOURCE_CURR) ,'TWD') as curr_eng_name,");
		sb.append(" SOURCE_AMT,MCHT_NO,CONTRACT_NO,DC_DEST_AMT,'BIL_BILL' AS DATA_FROM_TABLE ");
		sb.append("     FROM BIL_BILL a ");
		sb.append("     WHERE 1=1 ");
		sb.append("     AND a.RSK_TYPE NOT IN ('1','2','3') "); // 落問交的資料要踢除
		sb.append("     AND a.POST_DATE = ? ");
		sb.append("     AND a.ACCT_CODE IN ('BL','CA') "); // 只下本金類
		sb.append("     order by id_no ");
		sqlCmd = sb.toString();
		setString(1, searchDate); // 批次處理日期
		openCursor();
	}

	private void selectDbbBillData(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT  ");
		sb.append(" nvl((SELECT id_no FROM dbc_idno c WHERE a.id_p_seqno = c.id_p_seqno AND c.e_mail_addr LIKE '%@%' ),'') AS id_no, ");
		sb.append(" ACCT_TYPE,GROUP_CODE,TXN_CODE,CARD_NO,PURCHASE_DATE,POST_DATE,DEST_AMT,sign_flag,");
		sb.append(" MCHT_CATEGORY,'901' as CURR_CODE,MCHT_ENG_NAME,MCHT_CHI_NAME,AUTH_CODE,");
		sb.append(" nvl((select curr_eng_name from ptr_currcode e where e.curr_code = a.SOURCE_CURR) ,'TWD') as curr_eng_name,");
		sb.append(" SOURCE_AMT,MCHT_NO,CONTRACT_NO,DEST_AMT as DC_DEST_AMT,'DBB_BILL' AS DATA_FROM_TABLE ");
		sb.append("     FROM DBB_BILL a ");
		sb.append("     WHERE 1=1 ");
		sb.append("     AND a.RSK_TYPE NOT IN ('1') "); // 落問交的資料要踢除
		sb.append("     AND a.POST_DATE = ? ");
		sb.append("     AND a.ACCT_CODE IN ('BL','CA') "); // 只下本金類
		sb.append("     order by id_no ");

		sqlCmd = sb.toString();
		setString(1, searchDate); // 批次處理日期
		openCursor();
	}

	private void selectCrdIdno() throws Exception {

		extendField = "idno.";
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT  ");
		sb.append("     E_MAIL_ADDR ");
		sb.append(" FROM CRD_IDNO ");
		sb.append("     WHERE 1=1 ");
		sb.append("     AND ID_NO = ? ");

		sqlCmd = sb.toString();
		setString(1, comc.getSubString(getValue("ID_NO"),0,10));

		int cardCnt = selectTable();

		if (cardCnt == 0) {
			setValue("idno.e_mail_addr", "");
		}

	}

	private void selectDbcIdno() throws Exception {

		extendField = "idno.";
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT  ");
		sb.append("     E_MAIL_ADDR ");
		sb.append(" FROM DBC_IDNO ");
		sb.append("     WHERE 1=1 ");
		sb.append("     AND ID_NO = ? ");

		sqlCmd = sb.toString();
		setString(1, getValue("ID_NO"));

		int cardCnt = selectTable();

		if (cardCnt == 0) {
			setValue("idno.e_mail_addr", "");
		}

	}

	void procFTP(String fileFolder, String datFileName, String refIpCode) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s ", datFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName(refIpCode, ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	public static void main(String[] args) {
		BilA045 proc = new BilA045();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}
