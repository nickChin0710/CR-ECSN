/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- * 
*  112-03-10  V1.00.00  JH          program initial                          *
*  112-03-30  V1.00.01  JH          re-coding                                *
*  112-04-24  V1.00.02  Alex        新增將檔案進行FTP至 /crdatacrea/ELOAN 並backup *    
*  112-05-10  V1.00.03  Alex        取消 HDR 檔案                                                                                * 
*  112-06-06  V1.00.04  Alex        副檔名修正為.txt , 取消檔名日期
 *  2023-0830 V1.00.04  JH          endFile.break
 *  2023-1120 V1.00.05  JH          fileOpen: okExit(0)
*****************************************************************************/
package Rsk;
import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;

@SuppressWarnings({"unchecked", "deprecation"})
public class RskP193 extends com.BaseBatch {
private String progname = "傳送新ELOAN資料檔處理  2023-1120 V1.00.05";
CommCrd comc = new CommCrd();
//------------
String isPath="";
String inFileName="ENVELT1CUST_XL";
String isFileName="ENVELT1CUST_XLC";
int iiFileNum=-1;
//int iiFileNumHH=-1, 
int iiFileNumDD=-1;
int iiRespCnt=0;
CommFTP commFTP = null;
CommRoutine comr = null;
//=*****************************************************************************
public static void main(String[] args) {
	RskP193 proc = new RskP193();

//	proc.debug = true;
	proc.runCheck =true;
	proc.mainProcess(args);
	proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	if (args.length > 1) {
		printf("Usage : RskP193 [busi_date]");
		okExit(0);
	}

	dbConnect();
   String ls_runDate="";
   if (args.length >= 1) {
      setBusiDate(args[0]);
      ls_runDate =hBusiDate;
   }
   else ls_runDate =sysDate;

   if (checkWorkDate(ls_runDate)) {
      printf("-- [%s]非營業日, 不執行", ls_runDate);
      okExit(0);
   }

	fileOpen();

	fileRead();

	printf("處理筆數:[%s]",totalCnt);
	sqlCommit();

	String lsFile =inFileName+".txt";
	String lsPathBak =getEcsHome() + "/media/rsk/backup/"+lsFile+"_"+sysDate;
	if (comc.fileCopy(isPath,lsPathBak)) {
		comc.fileDelete(isPath);
		printf("file move OK=[%s]",lsFile);
	}
	else {
		printf("file move 失敗=[%s]",lsFile);
	}
	
	//--傳檔
	commFTP = new CommFTP(getDBconnect(), getDBalias());
	comr = new CommRoutine(getDBconnect(), getDBalias());
	procFTP();
	renameFile();	
	
	endProgram();
}

//=====================
void procFTP() throws Exception {
	//--HDR
//	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
//	commFTP.hEflgSystemId = "ELOAN_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
//	commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
//	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
//	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
//	commFTP.hEflgModPgm = javaProgram;
//
//	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
//	showLogMessage("I", "", "mput " + isFileName+"_"+commString.right(sysDate,6)+".HDR" + " 開始傳送....");
//	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + isFileName+"_"+commString.right(sysDate,6)+".HDR");
//
//	if (errCode != 0) {
//		showLogMessage("I", "", "ERROR:無法傳送 " + isFileName+"_"+commString.right(sysDate,6)+".HDR" + " 資料" + " errcode:" + errCode);
//		insertEcsNotifyLog(isFileName+"_"+commString.right(sysDate,6)+".HDR");
//	}
	
	//--DAT
	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	commFTP.hEflgSystemId = "ELOAN_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	commFTP.hEflgModPgm = javaProgram;

	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
//	showLogMessage("I", "", "mput " + isFileName+"_"+commString.right(sysDate,6)+".DAT" + " 開始傳送....");
//	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + isFileName+"_"+commString.right(sysDate,6)+".DAT");
   printf("mput " + isFileName+".txt" + " 開始傳送....");
	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + isFileName+".txt");

	if (errCode != 0) {
		printf("ERROR:無法傳送 " + isFileName+".txt" + " 資料" + " errcode:" + errCode);
		insertEcsNotifyLog(isFileName+".txt");
	}
}

//=====================
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

//=====================
void renameFile() throws Exception {
//	String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), isFileName+"_"+commString.right(sysDate,6)+".HDR");
//	String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), isFileName+"_"+commString.right(sysDate,6)+".HDR");
//
//	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
//		showLogMessage("I", "", "ERROR : 檔案[" + isFileName+"_"+commString.right(sysDate,6)+".HDR" + "]更名失敗!");
//		return;
//	}
//	showLogMessage("I", "", "檔案 [" + isFileName+"_"+commString.right(sysDate,6)+".HDR" + "] 已移至 [" + tmpstr2 + "]");
	
//	String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), isFileName+"_"+commString.right(sysDate,6)+".DAT");
//	String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), isFileName+"_"+commString.right(sysDate,6)+".DAT");
	String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), isFileName+".txt");
	String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), isFileName+".txt"+"_"+sysDate);

	if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
		showLogMessage("I", "", "ERROR : 檔案[" + isFileName+".txt" + "]更名失敗!");
		return;
	}
	printf("檔案 [" + isFileName+".txt" + "] 已移至 [" + tmpstr4 + "]");
	
}

//--
void fileRead() throws Exception {
	iiRespCnt =0;
	String newLine = "\r\n";
	while (true && totalCnt <99999) {
		totalCnt++;

		int liRc =0;
		String txt = this.readTextFile(iiFileNum);
		if (eq(endFile[iiFileNum],"Y")) break;

		if (empty(txt)) continue;
		if (txt.length() < 28) continue;

		String[] tt=new String[]{txt,"$$"};

		String lsBran=commString.token(tt);
		String lsImpId =commString.token(tt);
		String lsCorpNo =commString.token(tt).trim();

		liRc =selectCrdCard(lsCorpNo);
		if (liRc ==0) {
			continue;
		}

		//-無有效卡-
		iiRespCnt++;
		writeTextFile(iiFileNumDD, txt+newLine);
	}
	closeInputText(iiFileNum);
	closeOutputText(iiFileNumDD);

	printf(" 回饋筆數[%s]", iiRespCnt);
	//--
	//ENVELT1CUST_XLC_YYMMDD.HDR
	//1-32 為檔名(左靠)
	//33-40 為處理日
	//41-42 塞 ‘00’
	//43-56 為檔案產生年月日時分杪
	//57-64為筆數(右靠)

//	String ss=isFileName+"_"+commString.right(sysDate,6)+".DAT"
//	+sysDate+"00"+sysDate+sysTime
//	+commString.lpad(""+iiRespCnt,8,"0");
//	writeTextFile(iiFileNumHH,ss);
//	closeOutputText(iiFileNumHH);
}
//--
int tiCard=-1;
int selectCrdCard(String aCorpNo) throws Exception {
	if (empty(aCorpNo)) return 1;

	//團代3700 & 3720
	if (tiCard <=0) {
		sqlCmd ="select count(*) as xx_cnt"+
		" from crd_card"+
		" where current_code ='0'"+
		" and group_code in ('3700','3720')"+
		" and corp_no =?";
		tiCard =ppStmtCrt("ti-S-card","");
	}

	ppp(1, aCorpNo);
	sqlSelect(tiCard);
	if (sqlNrow <=0) {
		return 1;
	}
	if (colNum("xx_cnt") ==0) return 1;

	return 0;
}

//--
void fileOpen() throws Exception {
//String inFileName="ENVELT1CUST_XL";
//String isFileName="ENVELT1CUST_XLC";

	isPath = getEcsHome() + "/media/rsk/" + inFileName+".TXT";
	printf("open file [%s]", isPath);
	iiFileNum = this.openInputText(isPath);
	if (iiFileNum < 0) {
		printf("在程式執行目錄下沒有權限讀寫資料");
		okExit(0);
	}
	//--
//	String lsFileHH= getEcsHome() + "/media/rsk/" + isFileName+"_"+commString.right(sysDate,6)+".HDR";
//	printf("open file [%s]", lsFileHH);
//	iiFileNumHH =openOutputText(lsFileHH);
//	if (iiFileNumHH <0) {
//		errmsg("在程式執行目錄下沒有權限讀寫資料, file[%s]",lsFileHH);
//		errExit(1);
//	}
	//--
//	String lsFileDD= getEcsHome() + "/media/rsk/" + isFileName+"_"+commString.right(sysDate,6)+".DAT";
	String lsFileDD= getEcsHome() + "/media/rsk/" + isFileName+".txt";
	printf("open file [%s]", lsFileDD);
	iiFileNumDD =openOutputText(lsFileDD);
	if (iiFileNumDD <0) {
		printf("在程式執行目錄下沒有權限讀寫資料, out-file[%s]",lsFileDD);
		okExit(0);
	}
}

}
