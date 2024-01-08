/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- * 
*  112-04-26  V1.00.01  Alex        program initial                          * 
*****************************************************************************/
package Rsk;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class RskP210 extends BaseBatch {
	//--收取卡部 O714NCCC.TXT 後 改名為 O714006YYYYMMDDNN 轉傳至NMIP
	private final String progname = "送NCCC檔案-信用卡輔助持卡人身分驗證平台送驗機構資料通知檔 112/04/26 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	
	String inFileName = "";
	String outFileName = "";	
	
	public static void main(String[] args) {
		RskP210 proc = new RskP210();
		// proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP210 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}

		if (empty(hBusiDate))
			hBusiDate = comc.getBusiDate();

		dateTime();
		inFileName = "O714NCCC.TXT";
		outFileName = "O714006"+hBusiDate+"01";
		String lsPath1 = getEcsHome() + "/media/rsk/"+inFileName;
		String lsPath2 = getEcsHome() + "/media/rsk/"+outFileName;
		if (comc.fileCopy(lsPath1,lsPath2)) {
			comc.fileDelete(lsPath1);
			printf("file move OK=[%s]",lsPath1);
			//--傳檔
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());
			procFTP();
			renameFile();
		}
		else {
			printf("file not found =[%s]",lsPath1);			
		}						
		endProgram();
	}
	
	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), outFileName);
		String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), outFileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + outFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + outFileName + "] 已移至 [" + tmpstr2 + "]");	
	}
	
	void procFTP() throws Exception {
		//--HDR
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "FISC_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + outFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("FISC_FTP_PUT", "mput " + outFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + outFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(outFileName);
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
	
}
