package Mkt;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/25  V1.00.00   Ryan     program initial                            *
*  112/05/09  V1.00.01   Ryan     檔案格式調整、新增4個檔案                                                                        *
*  112/08/22  V1.00.02   Ryan     TCBLIFE_NEW_YYYYMMDD移至第3個位置                                       *              
*  112/09/13  V1.00.03   Ryan     檔名調整 ,MKT_TCB_LIFE_LIST WHERE調整 , .TXT調整        *   
*  112/09/15  V1.00.04   Ryan     備份檔名+時間 ,where 條件調整 ,每月3日遇假日順延至下個營業日   *   
*****************************************************************************/

import com.CommCrd;
import com.CommCrdRoutine;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommString;

public class MktR150 extends BaseBatch {
	private final String progname = "合庫人壽共銷產生名單檔   112/09/15 V1.00.04";

	CommCrd comc = new CommCrd();
	CommDate commDate = new CommDate();
	CommString comStr = new CommString();
	CommFTP commFTP = null;
	CommCrdRoutine comr = null;
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private static final String CRM_FOLDER = "/crdatacrea/";
	private static final String CRM_FOLDER_COPY = "/crdatacrea/CREDITCARD/";
	private static final String BACKUP_FOLDER = "/media/mkt/backup/";
	private static final String DATA_FORM1 = "TCBLIFE_NEW_YYYYMMDD.CSV";
	private static final String DATA_FORM2 = "TCBLIFE_YYYYMMDD.CSV";
	private static final String DATA_FORM3 = "TCBLIFE2_YYYYMMDD.CSV";
	private static final String DATA_FORM4 = "TCBLIFE_COUNT_YYYYMMDD.TXT";
	private static final String DATA_FORM1_COPY = "CR_TCBLIFELIST_NEW_YYYYMMDD.TXT";
	private static final String DATA_FORM2_COPY = "CR_TCBLIFELIST_YYYYMMDD.TXT";
	private static final String DATA_FORM3_COPY = "CR_TCBLIFELIST2_YYYYMMDD.TXT";
	private final static String COL_SEPERATOR = ",";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	String allStr = "";
	String busDate = "";
	String businessMonth = "";
	int commit = 1;
	int totalCnt2 = 0;
	StringBuffer dataCountBuf = new StringBuffer();

	// =****************************************************************************
	public static void main(String[] args) {
		MktR150 proc = new MktR150();
		proc.mainProcess(args);
		proc.systemExit();
	}

	// =============================================================================
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		dbConnect();
		
		comr = new CommCrdRoutine(getDBconnect(), getDBalias());
		
		readBusinessDate();
		String parm1 = "";
		if (args.length == 1 && args[0].length() == 8) {
			parm1 = args[0];
			busDate = parm1;
		}
		
		if (!"03".equals(comStr.right(busDate, 2))) {
			printf("本日[%s]非執行日,本程式每月3號執行,程式結束", busDate);
			return;
		}
		
		String nextBusDate = comr.increase0Days(busDate);
		if(!nextBusDate.equals(busDate)) {
			printf("[%s]非營業日,順延至下一個營運日[%s]", busDate,nextBusDate);
			busDate = nextBusDate;
		}
		
		businessMonth = commDate.monthAdd(busDate, 0, -1, 0);
		
		printf("輸入參數1=[%s]", "" + parm1);
		printf("取得營業日=[%s]", "" + busDate);
		printf("取得營業日前一個月=[%s]", "" + businessMonth);

		// TCBLIFE_YYYYMMDD.CSV
		printf("開始處理第一個檔[%s]", DATA_FORM2);
		checkOpen(DATA_FORM2);
		writeText(DATA_FORM2);
		sqlCommit(commit);
		copyFile(DATA_FORM2,DATA_FORM2_COPY);
		printf("==>檔案處理結束, 處理筆數=[%s]==============", "" + totalCnt2);
		printf("", "");
		// TCBLIFE2_YYYYMMDD.CSV
		printf("開始處理第二個檔[%s]", DATA_FORM3);
		checkOpen(DATA_FORM3);
		writeText(DATA_FORM3);
		sqlCommit(commit);
		copyFile(DATA_FORM3,DATA_FORM3_COPY);
		printf("==>檔案處理結束, 處理筆數=[%s]==============", "" + totalCnt2);
		printf("", "");
		// TCBLIFE_NEW_YYYYMMDD.CSV
		printf("開始處理第三個檔[%s]", DATA_FORM1);
		checkOpen(DATA_FORM1);
		writeText(DATA_FORM1);
		sqlCommit(commit);
		copyFile(DATA_FORM1,DATA_FORM1_COPY);
		printf("==>檔案處理結束, 處理筆數=[%s]==============", "" + totalCnt2);
		printf("", "");
		// TCBLIFE2_YYYYMMDD.CSV
		printf("開始處理第四個檔[%s]", DATA_FORM4);
		checkOpen(DATA_FORM4);
		writeTextCnt();
		copyFile(DATA_FORM4);
		printf("==>檔案處理結束==============", "");
		printf("", "");
		endProgram();
	}

	/* = ************************************************************************/
	public void checkOpen(String isFileName) throws Exception {
		isFileName = isFileName.replace("YYYYMMDD", busDate);
		String lsTemp = "";
		lsTemp = String.format("%s%s", CRM_FOLDER, isFileName);
		printf("Open File =[%s]", lsTemp);
		boolean isOpen = this.openBinaryOutput(lsTemp);
		if (isOpen == false) {
			printf(String.format("此路徑或檔案不存在[%s]", lsTemp));
			this.errExit(1);
		}
	}

	// =============================================================================
	public void readBusinessDate() throws Exception {
		sqlCmd = " SELECT BUSINESS_DATE FROM PTR_BUSINDAY ";
		sqlSelect();
		busDate = colSs("BUSINESS_DATE");
	}

	private int selectMktTcbLifeLisCnt(MktR150Data mktR150Data) throws Exception {
		sqlCmd = " SELECT COUNT(*) LIS_CNT FROM MKT_TCB_LIFE_LIST WHERE ID_P_SEQNO = ? ";
		sqlCmd += " AND SUBSTR(ISSUE_DATE,1,6) BETWEEN TO_CHAR(TO_DATE(?,'YYYYMM') -6 MONTHS,'YYYYMM') ";
		sqlCmd += " AND TO_CHAR(TO_DATE(?,'YYYYMM') -1 MONTHS,'YYYYMM')  ";
		setString(1, mktR150Data.idPSeqno);
		setString(2, businessMonth);
		setString(3, businessMonth);
		sqlSelect();
		return colInt("LIS_CNT");
	}

	private String selectCrdIdnoCnt(MktR150Data mktR150Data) throws Exception {
		sqlCmd = " SELECT MARKET_AGREE_BASE FROM CRD_IDNO WHERE ID_P_SEQNO = ? ";
		setString(1, mktR150Data.idPSeqno);
		sqlSelect();
		return colSs("MARKET_AGREE_BASE");
	}
	
	// =============================================================================
	void writeText(String fileName) throws Exception {
		totalCnt2 = 0;
		sqlCmd = " SELECT CHI_NAME,BILL_SENDING_ADDR1||BILL_SENDING_ADDR2||BILL_SENDING_ADDR3||BILL_SENDING_ADDR4||BILL_SENDING_ADDR5 AS BILL_SENDING_ADDR ";
		sqlCmd += ",DECODE(SEX,'2','0',SEX) AS SEX,BILL_SENDING_ZIP,BIRTHDAY,ID_NO ,CARD_NO ,ISSUE_MONTH ,ID_P_SEQNO ,ISSUE_DATE ";
		sqlCmd += ",OFFICE_AREA_CODE1||OFFICE_TEL_NO1 AS OFFICE_TEL,OFFICE_TEL_EXT1,HOME_AREA_CODE1||HOME_TEL_NO1 AS HOME_TEL ";
		sqlCmd += ",CELLAR_PHONE,E_MAIL_ADDR,GROUP_CODE,DECODE(MARKET_AGREE_BASE,'2',RIGHT(GROUP_CODE,3),'1') AS CARD_TYPE ";
		sqlCmd += " FROM MKT_TCB_LIFE_LIST ";
		if (DATA_FORM1.equals(fileName)) {
			sqlCmd += " WHERE MARKET_AGREE_BASE IN ('1','2') ";
			sqlCmd += " AND SUBSTR(STATIC_MONTH,1,6) = ?  ";
			sqlCmd += " AND PROC_MARK = '1' ";
			setString(1, businessMonth);
		}
		if (DATA_FORM2.equals(fileName)) {
			sqlCmd += " WHERE MARKET_AGREE_BASE IN ('1','2') ";
			sqlCmd += " AND SUBSTR(STATIC_MONTH,1,6) = ?  ";
			sqlCmd += " AND PROC_MARK = '2' ";
			setString(1, businessMonth);
		}
		if (DATA_FORM3.equals(fileName)) {
			sqlCmd += " WHERE MARKET_AGREE_BASE IN ('1','2') ";
			sqlCmd += " AND SUBSTR(STATIC_MONTH,1,6) BETWEEN TO_CHAR(TO_DATE(?,'YYYYMMDD') -6 MONTHS,'YYYYMM')  ";
			sqlCmd += " AND TO_CHAR(TO_DATE(?,'YYYYMMDD') -1 MONTHS,'YYYYMM')  ";
			sqlCmd += " ORDER BY ID_P_SEQNO, STATIC_MONTH  ";
			setString(1, busDate);
			setString(2, busDate);
		}

		this.openCursor();
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.CSV檔......");
			String items = getItems();
			sb.append(items);
			while (fetchTable()) {
				MktR150Data mktR150Data = getInfData();
				if (DATA_FORM1.equals(fileName)) {
					if (selectMktTcbLifeLisCnt(mktR150Data) > 0)
						continue;
				}
				if (DATA_FORM3.equals(fileName)) {
					if ("0".equals(selectCrdIdnoCnt(mktR150Data)))
						continue;
				}
				String rowOfDAT = getRowOfDAT(mktR150Data);
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

				if (DATA_FORM1.equals(fileName)) {
					updateMktTcbLifeList(mktR150Data, "1", "A");
				}
				if (DATA_FORM2.equals(fileName)) {
					updateMktTcbLifeList(mktR150Data, "2", "U");
				}
				if (DATA_FORM3.equals(fileName)) {
					updateMktTcbLifeList(mktR150Data, "3", "");
				}
				sqlCommit(commit);
				totalCnt++;
				totalCnt2++;
			}
			// write the rest of bytes on the file
			if (countInEachBuffer > 0 || sb.length() > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes("MS950");
				writeBinFile(tmpBytes, tmpBytes.length);
			}
			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入.DAT檔");
			} else {
				showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
			}
	
			this.closeCursor();
		} finally {
			closeBinaryOutput();
		}
	}
	
	private void writeTextCnt() throws Exception{
		byte[] tmpBytes = dataCountBuf.toString().getBytes("MS950");
		writeBinFile(tmpBytes, tmpBytes.length);
		closeBinaryOutput();
	}

	private void updateMktTcbLifeList(MktR150Data mktR150Data, String procMark, String modType) throws Exception {
		daoTable = "MKT_TCB_LIFE_LIST";
		updateSQL = " SEND_DATE = ? ,PROC_MARK = ? ";
		if (!comStr.empty(modType)) {
			updateSQL += ",MOD_TYPE = ? ";
		}
		updateSQL += " ,MOD_TIME = SYSDATE ,MOD_PGM = ? ,MOD_USER = ? ";
		whereStr = " WHERE CARD_NO = ? AND ISSUE_MONTH = ? ";
		int i = 1;
		setString(i++, sysDate);
		setString(i++, procMark);
		if (!comStr.empty(modType)) {
			setString(i++, modType);
		}
		setString(i++, "MktR150");
		setString(i++, "MktR150");
		setString(i++, mktR150Data.cardNo);
		setString(i++, mktR150Data.issueMonth);
		updateTable();
	}

	/****************************************************************************/
	private void copyFile(String removeFileName) throws Exception {
		removeFileName = removeFileName.replace("YYYYMMDD", busDate);
		String tmpstr1 = Paths.get(CRM_FOLDER,removeFileName).toString();
		String tmpstr2 = Paths.get(comc.getECSHOME(),BACKUP_FOLDER,String.format("%s.%s", removeFileName ,sysDate+sysTime)).toString();
		String tmpstr3 = Paths.get(CRM_FOLDER_COPY,removeFileName).toString();
		if (comc.fileCopy(tmpstr1, tmpstr3) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]copy失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已copy至 [" + tmpstr3 + "]");
		
		if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已備份至 [" + tmpstr2 + "]");
	}
	
	/****************************************************************************/
	private void copyFile(String fileName,String copyFileName) throws Exception {
		fileName = fileName.replace("YYYYMMDD", busDate);
		copyFileName = copyFileName.replace("YYYYMMDD", busDate);
		String tmpstr1 = Paths.get(CRM_FOLDER,fileName).toString();
		String tmpstr2 = Paths.get(CRM_FOLDER_COPY,copyFileName).toString();
		String tmpstr3 = Paths.get(comc.getECSHOME(),BACKUP_FOLDER,String.format("%s.%s", fileName ,sysDate+sysTime)).toString();
		String tmpstr4 = Paths.get(comc.getECSHOME(),BACKUP_FOLDER,String.format("%s.%s", copyFileName ,sysDate+sysTime)).toString();
		
		if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]copy失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已copy至 [" + tmpstr2 + "]");
		
		if (comc.fileCopy(tmpstr1, tmpstr3) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已備份至 [" + tmpstr3 + "]");
		
		if (comc.fileCopy(tmpstr2, tmpstr4) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + copyFileName + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + copyFileName + "] 已備份至 [" + tmpstr4 + "]");
		
		getDataCountBuf(fileName,totalCnt2);
	}
	
	private String getItems() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("持卡人姓名", 50));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("地址", 100));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("性別", 4));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("郵遞區號", 8));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("出生日期", 8));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("持卡人ID", 10));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("公司電話", 14));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("公司分機", 8));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("住家電話", 14));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("行動電話", 15));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("E_MAIL", 50));
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft("卡別", 4));
		sb.append(LINE_SEPERATOR);
		
		return sb.toString();
	}
	
	private void getDataCountBuf(String fileName,int count) throws Exception {
		dataCountBuf.append(dateFormat(sysDate+sysTime));
		dataCountBuf.append("產生檔案");
		dataCountBuf.append(fileName);
		dataCountBuf.append("內含");
		dataCountBuf.append(String.format("%010d", count));
		dataCountBuf.append("筆資料");
		dataCountBuf.append(LINE_SEPERATOR);
	}
	
	private String dateFormat(String date) throws Exception {
		SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat df2 = new SimpleDateFormat("yyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(df1.parse(date));
		return df2.format(cal.getTime());
	}

	private String getRowOfDAT(MktR150Data mktrR150Data) throws Exception {
		StringBuffer sb = new StringBuffer();

		sb.append(comc.fixLeft(mktrR150Data.chiName, 50)); // 2.7.4.1. 姓名:CHI_NAME
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.billSendingAddr, 100)); // 2.7.4.2.
																	// 地址:BILL_SENDING_ADDR1||BILL_SENDING_ADDR2||
																	// BILL_SENDING_ADDR3||BILL_SENDING_ADDR4||BILL_SENDING_ADDR5
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.sex, 4));// 2.7.4.3. 性別:如果SEX=2,寫入0, 如果SEX=1,寫入1
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.billSendingZip, 8));// 2.7.4.4. 郵遞區號:BILL_SENDING_ZIP
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(String.format("%07d",comStr.ss2int(commDate.toTwDate(mktrR150Data.birthday))), 8));// 2.7.4.5. 生日: BIRTHDAY西元年轉民國年YYYMMDD(7
																				// byte)
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.idNo, 10));// 2.7.4.6. 身分證號碼:ID_NO 取前10碼 (10 byte)
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.officeTel, 14));// 2.7.4.7. 公司電話:OFFICE_AREA_CODE1||OFFICE_TEL_NO1 AS
															// office_tel, (區碼+電話+分機,14byte)
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.officeTelExt1, 8));// 2.7.4.8. 公司分機OFFICE_TEL_EXT1
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.homeTel, 14));// 2.7.4.9. 住家電話:HOME_AREA_CODE1||HOME_TEL_NO1 AS Home_Tel
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.cellarPhone, 15));// 2.7.4.10. 行動電話:CELLAR_PHONE
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.eMailAddr, 50));// 2.7.4.11. E_MAIL: E_MAIL_ADDR
		sb.append(comc.fixLeft(COL_SEPERATOR, 1));
		sb.append(comc.fixLeft(mktrR150Data.cardType, 4));// 2.7.4.12.
															// 卡別:如果market_agree_base值=1,固定填入1,如果market_agree_base=2,填入group_code後3碼的值,例:1620,寫入620
		sb.append(LINE_SEPERATOR);
		return sb.toString();

	}

	MktR150Data getInfData() throws Exception {
		MktR150Data mktrR150Data = new MktR150Data();
		mktrR150Data.chiName = getValue("CHI_NAME");
		mktrR150Data.billSendingAddr = getValue("BILL_SENDING_ADDR");
		mktrR150Data.sex = getValue("SEX");
		mktrR150Data.billSendingZip = getValue("BILL_SENDING_ZIP");
		mktrR150Data.birthday = getValue("BIRTHDAY");
		mktrR150Data.idNo = getValue("ID_NO");
		mktrR150Data.officeTel = getValue("OFFICE_TEL");
		mktrR150Data.officeTelExt1 = getValue("OFFICE_TEL_EXT1");
		mktrR150Data.homeTel = getValue("HOME_TEL");
		mktrR150Data.cellarPhone = getValue("CELLAR_PHONE");
		mktrR150Data.eMailAddr = getValue("E_MAIL_ADDR");
		mktrR150Data.cardType = getValue("CARD_TYPE");
		mktrR150Data.idPSeqno = getValue("ID_P_SEQNO");
		mktrR150Data.issueMonth = getValue("ISSUE_MONTH");
		mktrR150Data.cardNo = getValue("CARD_NO");
		return mktrR150Data;
	}
}

class MktR150Data {
	String chiName = "";
	String billSendingAddr = "";
	String sex = "";
	String billSendingZip = "";
	String birthday = "";
	String idNo = "";
	String officeTel = "";
	String officeTelExt1 = "";
	String homeTel = "";
	String cellarPhone = "";
	String eMailAddr = "";
	String cardType = "";
	String idPSeqno = "";
	String issueMonth = "";
	String cardNo = "";
}
