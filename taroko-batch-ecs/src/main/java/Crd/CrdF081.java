/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/05/03  V1.00.00    Ryan                    program initial             *
******************************************************************************/

package Crd;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class CrdF081 extends AccessDAO {

	private String progname = "產生送財管&私銀持卡人申請卡片狀態檔程式 112/05/03  V1.00.00 ";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString comStr = new CommString();
	CommDate comDate = new CommDate();
	CommCrdRoutine comcr = null;
	private static final int READ_COUNT = 5000;
	private static final String CRM_FOLDER = "/media/crd";
	private static final String DATA_FORM = "CPCRD_YYYYMMDD.txt";
	private final static String COL_SEPERATOR = "";
	private final static String LINE_SEPERATOR = System.lineSeparator();

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
				comc.errExit("Usage : CrdF081 [sysdate ex:yyyymmdd] ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			hProcDate = sysDate;
			String parmDate = "";
			if(args.length==1 && args[0].length()==8) {
				parmDate = args[0];
				hProcDate = parmDate;
			}
			hProcDate = comDate.dateAdd(hProcDate, 0, 0, -1);
			showLogMessage("I", "", String.format("輸入參數日期 = [%s] ", parmDate));
			showLogMessage("I", "", String.format("取得系統日-1日 = [%s] ", hProcDate));

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
		sqlCmd = "SELECT B.ID_NO,A.CARD_NO,D.GROUP_NAME,A.SUP_FLAG,A.ACTIVATE_FLAG,A.INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT,A.CURRENT_CODE ";
		sqlCmd += " FROM CRD_CARD A,CRD_IDNO B,ACT_ACNO C ";
		sqlCmd += " LEFT JOIN PTR_GROUP_CODE D ON A.GROUP_CODE = D.GROUP_CODE ";
		sqlCmd += " WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ";
		sqlCmd += " AND A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ";
		sqlCmd += " AND A.CRT_DATE = ? ";
		sqlCmd += " UNION ";
		sqlCmd += " SELECT B.ID_NO,A.CARD_NO,D.GROUP_NAME,A.SUP_FLAG,A.ACTIVATE_FLAG,A.INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT,A.CURRENT_CODE ";
		sqlCmd += " FROM CRD_CARD A,CRD_IDNO B,ACT_ACNO C,CRD_CHG_ID E ";
		sqlCmd += " LEFT JOIN PTR_GROUP_CODE D ON A.GROUP_CODE = D.GROUP_CODE ";
		sqlCmd += " WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ";
		sqlCmd += " AND A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ";
		sqlCmd += " AND A.ID_P_SEQNO = E.ID_P_SEQNO ";
		sqlCmd += " AND E.CHG_DATE = ? ";
		sqlCmd += " UNION ";
		sqlCmd += " SELECT B.ID_NO,A.CARD_NO,D.GROUP_NAME,A.SUP_FLAG,A.ACTIVATE_FLAG,A.INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT,A.CURRENT_CODE ";
		sqlCmd += " FROM CRD_CARD A,CRD_IDNO B,ACT_ACNO C,CCA_CARD_OPEN E ";
		sqlCmd += " LEFT JOIN PTR_GROUP_CODE D ON A.GROUP_CODE = D.GROUP_CODE ";
		sqlCmd += " WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ";
		sqlCmd += " AND A.ACNO_P_SEQNO = C.ACNO_P_SEQNO  ";
		sqlCmd += " AND A.CARD_NO = E.CARD_NO ";
		sqlCmd += " AND E.OPEN_DATE = ? ";
		sqlCmd += " UNION ";
		sqlCmd += " SELECT B.ID_NO,A.CARD_NO,D.GROUP_NAME,A.SUP_FLAG,A.ACTIVATE_FLAG,A.INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT,A.CURRENT_CODE ";
		sqlCmd += " FROM CRD_CARD A,CRD_IDNO B,ACT_ACNO C,RSK_ACNOLOG E ";
		sqlCmd += " LEFT JOIN PTR_GROUP_CODE D ON A.GROUP_CODE = D.GROUP_CODE ";
		sqlCmd += " WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ";
		sqlCmd += " AND A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ";
		sqlCmd += " AND A.ACNO_P_SEQNO = E.ACNO_P_SEQNO ";
		sqlCmd += " AND ((E.KIND_FLAG = 'C' AND E.EMEND_TYPE ='4' AND E.SON_CARD_FLAG = 'Y' ";
		sqlCmd += " AND E.APR_FLAG ='Y') OR (E.KIND_FLAG ='A' AND E.EMEND_TYPE ='1' ";
		sqlCmd += " AND E.APR_FLAG ='Y')) AND E.LOG_DATE = ? ";
		sqlCmd += " UNION ";
		sqlCmd += " SELECT B.ID_NO,A.CARD_NO,D.GROUP_NAME,A.SUP_FLAG,A.ACTIVATE_FLAG,A.INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT,A.CURRENT_CODE ";
		sqlCmd += " FROM CRD_CARD A,CRD_IDNO B,ACT_ACNO C,CMS_CHGCOLUMN_LOG E ";
		sqlCmd += " LEFT JOIN PTR_GROUP_CODE D ON A.GROUP_CODE = D.GROUP_CODE ";
		sqlCmd += " WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ";
		sqlCmd += " AND A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ";
		sqlCmd += " AND A.CARD_NO = E.CARD_NO ";
		sqlCmd += " AND E.CHG_COLUMN = 'current_code' ";
		sqlCmd += " AND E.CHG_DATE = ? ";
		setString(1,hProcDate);
		setString(2,hProcDate);
		setString(3,hProcDate);
		setString(4,hProcDate);
		setString(5,hProcDate);
		openCursor();
		while (fetchTable()) {
			data.initData();
			data.idNo = getValue("ID_NO");
			data.cardNo = getValue("CARD_NO");
			data.groupName = getValue("GROUP_NAME");
			data.supFlag = getValue("SUP_FLAG");
			data.activateFlag = getValue("ACTIVATE_FLAG");
			data.indivCrdLmt = getValueLong("INDIV_CRD_LMT");
			data.lineOfCreditAmt = getValueLong("LINE_OF_CREDIT_AMT");
			data.currentCode = getValue("CURRENT_CODE");

			tmpBuf = data.allText();
			writeTextFile(fptr1, tmpBuf);

			totCnt++;
			if (totCnt % READ_COUNT == 0 || totCnt == 1)
				showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

		}
		closeCursor();
	}


	/*******************************************************************/
	private void fileOpen() throws Exception {
		fmtFileName = DATA_FORM.replace("YYYYMMDD", hProcDate);

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
			commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
			commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
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
		String tmpstr2 = String.format("%s%s/backup/%s", comc.getECSHOME(), CRM_FOLDER, fmtFileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fmtFileName + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fmtFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CrdF081 proc = new CrdF081();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String idNo;
		String cardNo;
		String groupName;
		String supFlag;
		String activateFlag;
		long indivCrdLmt;
		long lineOfCreditAmt;
		String currentCode;

		void initData() {
			 idNo = "";
			 cardNo = "";
			 groupName = "";
			 supFlag = "";
			 activateFlag = "";
			 indivCrdLmt = 0;
			 lineOfCreditAmt = 0;
			 currentCode = "";
		}

		String allText() throws Exception {
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(comc.fixLeft(idNo, 11)) // 身分證字號
					.append(COL_SEPERATOR).append(comc.fixLeft(cardNo, 20)) // 信用卡卡號
					.append(COL_SEPERATOR).append(comc.fixLeft(comStr.right(groupName, 15), 30)) // 卡片名稱
					.append(COL_SEPERATOR).append(comc.fixLeft(comStr.decode(supFlag, ",0,1", ",1,2"), 1)) // 正/附卡
					.append(COL_SEPERATOR).append(comc.fixLeft(comStr.decode(activateFlag, ",1,2", ",N,Y"), 1)) // 開卡狀態
					.append(COL_SEPERATOR).append(comc.fixRight(String.valueOf(indivCrdLmt), 8)) // 卡片信用額度
					.append(COL_SEPERATOR).append(comc.fixRight(String.valueOf(lineOfCreditAmt), 8)) // 卡人額度
					.append(COL_SEPERATOR).append(comc.fixLeft(comStr.pos(",1,2,3,4,5", currentCode)>0?"S":"A", 1)) // 卡片狀態 
					.append(LINE_SEPERATOR);
			return strBuf.toString();
		}
	}

}
