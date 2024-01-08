/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/12/21  V1.00.00    Wendy Lu                     program initial        *
 ******************************************************************************/

package Inf;

import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.BaseBatch;
import com.CommString;

public class InfC042 extends BaseBatch {
	private final String progname = "產生送CRDB 42異動申請 COMBO白金卡註記資料檔程式   109/12/21 V1.00.00";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	String isFileName = "";
	private int ilFile42;
	String hSysDate = "";
	String hIdNo = "";

	public static void main(String[] args) {
		InfC042 proc = new InfC042();
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : InfCrb42 [business_date]");
			errExit(1);
		}
		dbConnect();
		if (liArg == 1) {
			hSysDate = args[0];
		}

		if (empty(hSysDate))
			hSysDate =  commDate.dateAdd(sysDate, 0, 0, -1);
		
		System.out.print(hSysDate);

		isFileName = "CRU23B1_TYPE_42_" + hSysDate + ".txt";
		
				
		checkOpen();
		selectIdNo(hSysDate);
		closeOutputText(ilFile42);
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		renameFile();
		endProgram();
	}


	void selectIdNo(String hSysDate) throws Exception {
		sqlCmd = "with ";
	    sqlCmd += "temp as( ";
	    sqlCmd += "select a.id_p_seqno ";
	    sqlCmd += "from crd_card a ";
	    sqlCmd += "inner join ptr_card_type b on a.card_type = b.card_type ";
	    sqlCmd += "where a.crt_date = ? and b.card_note = 'P' ";
	    sqlCmd += ") ";
		sqlCmd += "select id_no ";
		sqlCmd += "from crd_idno ";
		sqlCmd += "where id_p_seqno in (select id_p_seqno from temp) ";
		
		setString(1, hSysDate);
		
		int llCnt = selectTable();
		for (int ii = 0; ii < llCnt; ii++) {
			hIdNo = colSs(ii, "id_no");
			writeTextFile();
		}
	}


	void checkOpen() throws Exception {
		String lsTemp = "";
		lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
		ilFile42 = openOutputText(lsTemp, "big5");
		if (ilFile42 < 0) {
			printf("CRU23B1-TYPE-42 產檔失敗 ! ");
			errExit(1);
		}
	}
	
	void writeTextFile() throws Exception {
		StringBuffer tempBuf = new StringBuffer();
		String tempStr = "", newLine = "\r\n";
			tempBuf.append("42"); // --代碼 固定 42
			tempBuf.append(comc.fixLeft("", 16)); 
			tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主身分證 11 碼
			tempBuf.append("Y"); 
			tempBuf.append(comc.fixLeft("", 120)); 
			tempBuf.append(newLine);
			totalCnt++;		
		this.writeTextFile(ilFile42, tempBuf.toString());
	}

	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
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

	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/crdb/%s", getEcsHome(), isFileName);
		String tmpstr2 = String.format("%s/media/crdb/backup/%s", getEcsHome(), isFileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + isFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + isFileName + "] 已移至 [" + tmpstr2 + "]");
	}

}
