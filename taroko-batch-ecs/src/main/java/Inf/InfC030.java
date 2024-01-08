package Inf;
/** 
 * V00.01	2020-1214	Ryan		initial
 * V00.02	2023-0530	Wilson		增加附卡變更ID資料
 * */

import com.CommCrd;

import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;

public class InfC030 extends BaseBatch {
	private final String progname = "產生送CRDB 30新增附卡/商務卡資料檔程式   112/05/30 V1.00.02";

	CommCrd comc = new CommCrd();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;

	String isFileName = "";
	private int iiFileNum = -1;
	String allStr = "";
	String hChgDate = "";
	String hOpenDate = "";
	int commit = 1;

	// =****************************************************************************
	public static void main(String[] args) {
		InfC030 proc = new InfC030();
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

		readSysdate();

		if (liArg == 0) {
			hChgDate = hOpenDate;
		}
		if (liArg == 1) {
			hChgDate = args[0];
		}
		// printf("傳送日期 =[%s]", crtDate);
		
		isFileName = "CRU23B1_TYPE_30_" + hChgDate + ".txt";

		writeText();

		checkOpen();
		this.writeTextFile(iiFileNum, allStr);
		this.closeCursor();

		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		closeOutputText(iiFileNum);
		renameFile(isFileName);

		sqlCommit(commit);
		printf("==>程式執行結束, 處理筆數=[%s]==============", "" + totalCnt);
		endProgram();
	}

	/* = ************************************************************************/
	public void checkOpen() throws Exception {

		String lsTemp = "";
		lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
		printf("Open File =[%s]", lsTemp);
		iiFileNum = this.openOutputText(lsTemp);
		if (iiFileNum < 0) {
			printf("[%s]在程式執行目錄下沒有權限讀寫資料", lsTemp);
			errExit(1);
		}
	}

	// =============================================================================
	public void readSysdate() throws Exception {

		sqlCmd = " select to_char(sysdate-1,'yyyymmdd') as sysdate1 ";
		sqlCmd += "from dual";
		sqlSelect();
		hOpenDate = colSs("sysdate1");

	}

	
	
	// =============================================================================
	void writeText() throws Exception {
		sqlCmd = " select a.card_no ";
	    sqlCmd += " ,UF_IDNO_ID(a.id_p_seqno) as h_id_no ";
	    sqlCmd += " ,(CASE WHEN a.acno_flag != '1' THEN (select corp_no from crd_corp where corp_p_seqno = a.corp_p_seqno) ";
	    sqlCmd += " WHEN (a.acno_flag = '1' and  a.sup_flag = '1') THEN (select id_no from crd_idno where id_p_seqno = a.major_id_p_seqno) END) as h_id_no2 ";
	    sqlCmd += " from crd_card as a ";
		sqlCmd += " where a.crt_date = ? ";
		sqlCmd += " and (a.acno_flag != '1' or a.sup_flag = '1') ";
		sqlCmd += "UNION ";
		sqlCmd += "select a.card_no ";
		sqlCmd += " ,UF_IDNO_ID(a.id_p_seqno) as h_id_no ";
		sqlCmd += " ,(select id_no from crd_idno where id_p_seqno = a.major_id_p_seqno)  as h_id_no2 ";
		sqlCmd += " from crd_card a,crd_chg_id b where a.id_p_seqno = b.id_p_seqno ";
		sqlCmd += " and b.chg_date = ? ";
		setString(1,hChgDate);
		setString(2,hChgDate);
		this.openCursor();

		while (fetchTable()) {

			allStr += "30";
			allStr += commString.fixlenNum(colSs("card_no"), 16);
			allStr += commString.rpad(colSs("h_id_no"), 11);
			allStr += commString.rpad(colSs("h_id_no2"), 11);
			allStr += commString.space(110);
			allStr += "\r\n";
			totalCnt++;
		
		}
	}

	/***********************************************************************/
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr
				.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crdb/", comc.getECSHOME());
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
		String tmpstr1 = comc.getECSHOME() + "/media/crdb/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/crdb/backup/" + removeFileName;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

}
