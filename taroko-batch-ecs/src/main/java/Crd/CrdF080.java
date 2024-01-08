/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/04/14  V1.00.00    Ryan       program initial                          *
*  112/08/22  V1.00.01    Wilson     調整FTP參數                                                                                          *
*  112/09/12  V1.00.02    Wilson     修正欄位順序問題                                                                                   *
*  112/09/17  V1.00.03    Wilson     中文欄位放全形空白                                                                               *
*  112/09/21  V1.00.04    Wilson     出生日期/設立日期空白預設19110000                *
******************************************************************************/

package Crd;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCpi;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class CrdF080 extends AccessDAO {

	public final boolean debugD = false;

	private String progname = "產生中央存保信用卡客戶基本資料檔程式 112/09/21  V1.00.04 ";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString comStr = new CommString();
	CommDate comDate = new CommDate();
	CommCrdRoutine comcr = null;
	CommCpi comcpi = new CommCpi();
	
	private static final int READ_COUNT = 5000;
	private static final String CRM_FOLDER = "/media/crd";
	private static final String DATA_FORM = "0060000A51.YYYMMDD";
	private final static String COL_SEPERATOR = "";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	private final static String CRMD_BKID = "006";
	private final static String CRMD_BRID = "0000";

	int debug = 0;

	Buf1 data = new Buf1();
	String hCallBatchSeqno = "";

	private String hProcDate = "";
	private String hTwSysdate = "";

	private int fptr1 = -1;
	private long totCnt = 0;

	private String fileName = "";
	private String fmtFileName = "";

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length > 1) {
				comc.errExit("Usage : CrdF077 [sysdate ex:yyyymmdd] ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

//            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

			hProcDate = sysDate;
			hTwSysdate = comDate.toTwDate(hProcDate);

			showLogMessage("I", "", String.format("系統日 = [%s] ", hProcDate));

			fileOpen();

			selectCrdData();

			closeOutputText(fptr1);

			ftpProc();
			renameFile();
			showLogMessage("I", "", String.format("Process records = [%d]", totCnt));

			// ==============================================
			// 固定要做的
			comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
			comcr.callbatchEnd();
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***
	 * 讀取報送資料
	 ***/
	private void selectCrdData() throws Exception {
		String tmpBuf = "";
		sqlCmd = "SELECT DISTINCT ";
		sqlCmd += " 'C' AS TMPTYPE, ";
		sqlCmd += " A.ID_NO AS CUID, ";
		sqlCmd += " A.CHI_NAME AS CUCNAME, ";
		sqlCmd += " '' AS CUENAME, ";
		sqlCmd += " A.BIRTHDAY AS BRDATE, ";
		sqlCmd += " A.RESIDENT_ADDR1 || A.RESIDENT_ADDR2 || A.RESIDENT_ADDR3 || A.RESIDENT_ADDR4 || A.RESIDENT_ADDR5 AS RESADDR, ";
		sqlCmd += " CASE WHEN A.HOME_TEL_NO1 = '' THEN '' ";
		sqlCmd += " ELSE A.HOME_AREA_CODE1 || '-' || A.HOME_TEL_NO1 || '#' || A.HOME_TEL_EXT1 END AS HTEL, ";
		sqlCmd += " CASE WHEN A.CELLAR_PHONE = '' THEN '' ";
		sqlCmd += " ELSE SUBSTRING(A.CELLAR_PHONE,1,4) || '-' || SUBSTRING(A.CELLAR_PHONE,5) END AS CPHONE, ";
		sqlCmd += " CASE WHEN A.OFFICE_TEL_NO1 = '' THEN '' ";
		sqlCmd += " ELSE A.OFFICE_AREA_CODE1 || '-' || A.OFFICE_TEL_NO1 || '#' || A.OFFICE_TEL_EXT1  END AS WKCTEL, ";
		sqlCmd += " A.ID_P_SEQNO AS KEYPSEQNO ";
		sqlCmd += " FROM CRD_IDNO A,CRD_CARD B WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ";
		sqlCmd += " UNION ";
		sqlCmd += " SELECT DISTINCT ";
		sqlCmd += " 'B' AS TMPTYPE, ";
		sqlCmd += " C.CORP_NO AS CUID, ";
		sqlCmd += " C.CHI_NAME AS CUCNAME,  ";
		sqlCmd += " C.ENG_NAME AS CUENAME, ";
		sqlCmd += " C.SETUP_DATE AS BRDATE, ";
		sqlCmd += " C.REG_ADDR1 || C.REG_ADDR2 || C.REG_ADDR3 || C.REG_ADDR4 || C.REG_ADDR5 AS RESADDR, ";
		sqlCmd += " CASE WHEN C.CORP_TEL_NO1 = '' THEN '' ";
		sqlCmd += " ELSE C.CORP_TEL_ZONE1 || '-' || C.CORP_TEL_NO1 || '#' || C.CORP_TEL_EXT1 END AS HTEL, ";
		sqlCmd += " '' AS CPHONE, ";
		sqlCmd += " '' AS WKCTEL, ";
		sqlCmd += " C.CORP_P_SEQNO AS KEYPSEQNO ";
		sqlCmd += " FROM CRD_CORP C,CRD_CARD D WHERE C.CORP_P_SEQNO = D.CORP_P_SEQNO ";

		openCursor();
		while (fetchTable()) {
			data.initData();
			data.tmpType = getValue("TMPTYPE");
			data.cuId = getValue("CUID");
			data.cucName = getValue("CUCNAME");
			data.cueName = getValue("CUENAME");
			data.brDate = getValue("BRDATE");
			data.resAddr = getValue("RESADDR");
			data.hTel = getValue("HTEL");
			data.cPhone = getValue("CPHONE");
			data.wkcTel = getValue("WKCTEL");
			data.keyPSeqno = getValue("KEYPSEQNO");

			tmpBuf = data.allText();
			writeTextFile(fptr1, tmpBuf);
			if (debugD)
				showLogMessage("I", "", String.format("DETAIL DATA=[%s]", tmpBuf));

			totCnt++;
			if (totCnt % READ_COUNT == 0 || totCnt == 1)
				showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

		}
		closeCursor();
	}

	/***
	 * 讀取一般卡戶英文姓名
	 * 
	 * @throws Exception
	 */
	private void getEngName() throws Exception {
		if (!"C".equals(data.tmpType))
			return;
		extendField = "CARD_ENG.";
		sqlCmd = "SELECT ENG_NAME FROM CRD_CARD WHERE ID_P_SEQNO = ? ";
		sqlCmd += " ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY ";
		setString(1, data.keyPSeqno);
		int crdCnt = selectTable();
		if (crdCnt > 0) {
			data.cueName = getValue("CARD_ENG.ENG_NAME");
		}
	}

	/***
	 * 讀取帳戶流水號&判斷信用卡戶類型
	 * 
	 * @throws Exception
	 */
	private void getCuType() throws Exception {
		extendField = "CARD_CUTYPE.";
		int crdCnt = 0;
		if ("C".equals(data.tmpType)) {
			data.cuType = "M";
			sqlCmd = " SELECT ACNO_P_SEQNO,CU_TYPE FROM ( ";
			sqlCmd += " SELECT ACNO_P_SEQNO ,'M' CU_TYPE ,ISSUE_DATE FROM CRD_CARD WHERE ID_P_SEQNO = ? AND SUP_FLAG = '0' AND ACNO_FLAG = '1' ";
			sqlCmd += " UNION ";
			sqlCmd += " SELECT ACNO_P_SEQNO ,'P' CU_TYPE ,ISSUE_DATE FROM CRD_CARD WHERE ID_P_SEQNO = ? AND ACNO_FLAG = '3' ";
			sqlCmd += " UNION ";
			sqlCmd += " SELECT ACNO_P_SEQNO ,'S' CU_TYPE ,ISSUE_DATE FROM CRD_CARD WHERE ID_P_SEQNO = ? AND SUP_FLAG = '1' AND ACNO_FLAG = '1' ";
			sqlCmd += " ) ORDER BY CU_TYPE ,ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY ";
			setString(1, data.keyPSeqno);
			setString(2, data.keyPSeqno);
			setString(3, data.keyPSeqno);
			crdCnt = selectTable();
			if(crdCnt > 0) {
				data.acnoPSeqno = getValue("CARD_CUTYPE.ACNO_P_SEQNO");
				data.cuType = getValue("CARD_CUTYPE.CU_TYPE");
			}
		}

		if ("B".equals(data.tmpType)) {
			data.cuType = "C";
			sqlCmd = " SELECT ACNO_P_SEQNO FROM CRD_CARD WHERE CORP_P_SEQNO = ? AND ACNO_FLAG = '3' ";
			sqlCmd += " ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY ";
			setString(1, data.keyPSeqno);
			crdCnt = selectTable();
			if (crdCnt > 0) {
				data.acnoPSeqno = getValue("CARD_CUTYPE.ACNO_P_SEQNO");
			}
		}
	}

	/***
	 * 讀取帳單地址
	 * 
	 * @throws Exception
	 */
	private void getMailAddr() throws Exception {
		extendField = "ACNO_MAILADDR.";
		sqlCmd = "SELECT BILL_SENDING_ZIP AS ZCODE, BILL_SENDING_ADDR1 || BILL_SENDING_ADDR2 || BILL_SENDING_ADDR3 || ";
		sqlCmd += " BILL_SENDING_ADDR4 || BILL_SENDING_ADDR5 AS MAILADDR FROM ACT_ACNO WHERE ACNO_P_SEQNO = ? ";
		setString(1, data.acnoPSeqno);
		int crdCnt = selectTable();
		if (crdCnt > 0) {
			data.zCode = getValue("ACNO_MAILADDR.ZCODE");
			data.mailAddr = getValue("ACNO_MAILADDR.MAILADDR");
		}
	}

	/*******************************************************************/
	private void fileOpen() throws Exception {
		fmtFileName = DATA_FORM.replace("YYYMMDD", hTwSysdate);

		String temstr1 = String.format("%s%s/%s", comc.getECSHOME(), CRM_FOLDER, fmtFileName);
		fileName = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		fptr1 = openOutputText(fileName, "MS950");
		if (fptr1 == -1) {
			comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
		}
	}

	/*******************************************************************/
	private void ftpProc() throws Exception {

		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

		/**********
		 * COMM_FTP common function usage
		 ****************************************/
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */
		for (int inti = 0; inti < 1; inti++) {
			commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */
			commFTP.hEflgGroupId = "A51"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
			commFTP.hEflgSourceFrom = "TO CDIC"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
			commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(), CRM_FOLDER);
			commFTP.hEflgModPgm = javaProgram;

			showLogMessage("I", "", "mput " + fmtFileName + " 開始傳送....");
			int errCode = commFTP.ftplogName("CRDATACREA", "mput " + fmtFileName);

			if (errCode != 0) {
				showLogMessage("I", "", "ERROR:無法傳送 " + fmtFileName + " 資料" + " errcode:" + errCode);
				if (inti == 0)
					break;
			}

		}
	}

	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s%s/%s", comc.getECSHOME(), CRM_FOLDER, fmtFileName);
		String tmpstr2 = String.format("%s%s/backup/%s.%s", comc.getECSHOME(), CRM_FOLDER, fmtFileName,sysDate+sysTime);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fmtFileName + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fmtFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CrdF080 proc = new CrdF080();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String tmpType;
		String cuId;
		String cucName;
		String cueName;
		String brDate;
		String resAddr;
		String hTel;
		String cPhone;
		String wkcTel;
		String keyPSeqno;
		String zCode = "";
		String cuType = "";
		String mailAddr = "";
		String acnoPSeqno = "";

		void initData() {
			tmpType = "";
			cuId = "";
			cucName = "";
			cueName = "";
			brDate = "";
			resAddr = "";
			hTel = "";
			cPhone = "";
			wkcTel = "";
			keyPSeqno = "";
			zCode = "";
			cuType = "";
			mailAddr = "";
			acnoPSeqno = "";
		}

		String allText() throws Exception {
			// 讀取一般卡戶英文姓名
			getEngName();
			// 讀取帳戶流水號&判斷信用卡戶類型
			getCuType();
			// 讀取帳單地址
			getMailAddr();

			StringBuffer strBuf = new StringBuffer();
			strBuf.append(comc.fixLeft(CRMD_BKID, 3)) // 總機構代號 X(3)
					.append(COL_SEPERATOR).append(comc.fixLeft(CRMD_BRID, 4)) // 分支機構代號 X(4)
					.append(COL_SEPERATOR).append(comc.fixLeft(cuType, 1)) // 信用卡戶類型 X(1)
					.append(COL_SEPERATOR).append(comc.fixLeft(cuId, 20)) // 信用卡戶識別碼 X(20)
					.append(COL_SEPERATOR).append(comc.fixLeft(comcpi.commTransChinese(String.format("%-200.200s", cucName)),200)) // 戶名(姓名)1 X(200)
					.append(COL_SEPERATOR).append(comc.fixLeft(cueName, 200)) // 戶名(姓名)2 X(200)
					.append(COL_SEPERATOR).append(comc.fixLeft(brDate.length() == 0 ? "19110000" : brDate, 8)) // 出生日期/設立日期 9(8)
					.append(COL_SEPERATOR).append(comc.fixLeft(zCode, 5)) // 帳單地址郵遞區號 X(5)
					.append(COL_SEPERATOR).append(comc.fixLeft(comcpi.commTransChinese(String.format("%-80.80s", mailAddr)),80)) // 寄送帳單地址 X(80)
					.append(COL_SEPERATOR).append(comc.fixLeft(comcpi.commTransChinese(String.format("%-80.80s", resAddr)),80)) // 戶籍地址 X(80)
					.append(COL_SEPERATOR).append(comc.fixLeft(hTel, 16)) // 居住/營業電話 X(16)
					.append(COL_SEPERATOR).append(comc.fixLeft(cPhone, 16)) // 行動電話 X(16)
					.append(COL_SEPERATOR).append(comc.fixLeft(wkcTel, 16)) // 任職機構電話 X(16)
					.append(LINE_SEPERATOR);
			return strBuf.toString();
		}
	}

}
