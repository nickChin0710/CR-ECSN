
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/29  V1.00.00   Ryan     program initial                          *
*****************************************************************************/
package Inf;

import com.CommCrd;

import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;

public class InfC061 extends BaseBatch {
	private final String progname = "產生送CRDB 61異動到期不續約註記資料檔程式   112/03/29 V1.00.00";

	CommCrd comc = new CommCrd();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	private final static String LINE_SEPERATOR = "\r\n";
	private final static String FILE_NAME = "CRU23B1_TYPE_61_YYYYMMDD.txt";
    private final static String FILE_PATH = "/media/crdb/";
	private int iiFileNum = -1;
	String hChgDate = "";
	String hOpenDate = "";
	int commit = 1;

	// =****************************************************************************
	public static void main(String[] args) {
		InfC061 proc = new InfC061();
		proc.mainProcess(args);
		proc.systemExit();
	}

	// =============================================================================
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			errExit(1);
		}
		dbConnect();

		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		
		readSysdate();

		if (liArg == 0) {
			hChgDate = hOpenDate;
			showLogMessage("I", "", "參數值為系統日前一日[" + hChgDate + "]");
		}
		if (liArg == 1) {
			hChgDate = args[0];
			showLogMessage("I", "", "參數值為指定日期[" + hChgDate + "]");
		}

		checkOpen();

		sqlCommit(commit);
		printf("==>程式執行結束, 處理筆數=[%s]==============", "" + totalCnt);
		endProgram();
	}

	/* = ************************************************************************/
	public void checkOpen() throws Exception {
		String isFileName = FILE_NAME.replace("YYYYMMDD", hChgDate);
		String lsTemp = "";
		lsTemp = String.format("%s%s%s", comc.getECSHOME(),FILE_PATH, isFileName);
		printf("Open File =[%s]", lsTemp);
		iiFileNum = this.openOutputText(lsTemp);
		if (iiFileNum < 0) {
			printf("[%s]在程式執行目錄下沒有權限讀寫資料", lsTemp);
			errExit(1);
		}
		
		String allStr = writeText();
		this.writeTextFile(iiFileNum, allStr);
		this.closeCursor();
		closeOutputText(iiFileNum);
		procFTP(isFileName);
		renameFile(isFileName);
	}

	// =============================================================================
	public void readSysdate() throws Exception {

		sqlCmd = " select to_char(sysdate-1,'yyyymmdd') as sysdate1 ";
		sqlCmd += "from dual";
		sqlSelect();
		hOpenDate = colSs("sysdate1");

	}

	
	
	// =============================================================================
	String writeText() throws Exception {
	    sqlCmd = "SELECT A.CARD_NO,B.ID_NO,A.EXPIRE_CHG_DATE,A.CANCEL_EXPIRE_CHG_DATE FROM CRD_CARD A,CRD_IDNO B ";
	    sqlCmd += " WHERE A.MAJOR_ID_P_SEQNO = B.ID_P_SEQNO AND (A.EXPIRE_CHG_DATE = ? OR A.CANCEL_EXPIRE_CHG_DATE = ?) "; 
	    sqlCmd += " UNION "; 
	    sqlCmd += " SELECT A.CARD_NO,B.ID_NO,A.EXPIRE_CHG_DATE,A.CANCEL_EXPIRE_CHG_DATE FROM DBC_CARD A,DBC_IDNO B "; 
	    sqlCmd += " WHERE A.MAJOR_ID_P_SEQNO = B.ID_P_SEQNO AND (A.EXPIRE_CHG_DATE = ? OR A.CANCEL_EXPIRE_CHG_DATE = ?) ";
	
		setString(1,hChgDate);
		setString(2,hChgDate);
		setString(3,hChgDate);
		setString(4,hChgDate);
		this.openCursor();
		
		StringBuffer sbStr = new StringBuffer();
		while (fetchTable()) {
			String cracaid = "";
			if(hChgDate.equals(getValue("EXPIRE_CHG_DATE")))
				cracaid = "3";
			if(hChgDate.equals(getValue("CANCEL_EXPIRE_CHG_DATE")))
				cracaid = "";
			sbStr.append("61") 
			.append(comc.fixLeft(getValue("CARD_NO"), 16))
			.append(comc.fixLeft(getValue("ID_NO"), 10))
			.append(comc.fixLeft(cracaid, 1))
			.append(comc.fixLeft("",121))
			.append(LINE_SEPERATOR);
			totalCnt++;
		}
		return sbStr.toString();
	}

	/***********************************************************************/
	void procFTP(String isFileName) throws Exception {
		commFTP.hEflgTransSeqno = comr
				.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(),FILE_PATH);
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(isFileName);
		}
	}

	/****************************************************************************/
	public int insertEcsNotifyLog(String fileName) throws Exception {
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("unit_code", comr.getObjectOwner("3", javaProgram));
		setValue("obj_type", "3");
		setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_name", "媒體檔名:" + fileName);
		setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_desc2", "");
		setValue("trans_seqno", commFTP.hEflgTransSeqno);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "ecs_notify_log";

		insertTable();

		return (0);
	}

	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		
		String tmpstr1 = String.format("%s%s%s", comc.getECSHOME(),FILE_PATH,removeFileName);
		String tmpstr2 = String.format("%s%sbackup/%s",comc.getECSHOME(),FILE_PATH,removeFileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

}
