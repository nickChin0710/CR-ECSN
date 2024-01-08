package Cca;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 109-06-11  V0.00.01     Ryan     initial                                   *
* 109-10-16  V0.00.02     Ryan     add ftp                                   *
* 110-02-04  V0.00.03     Ryan     修改檔名日期為營業日                                                                               *
* 111-03-09  V0.00.04     Ryan     只處理2440 2442 2447                                                                               *
* 111-03-22  V0.00.05     Ryan     改為處理營業日當天、增加可以輸入參數                                                  *
* 111-04-14  V0.00.06     Ryan     and proc_code = 'Y' ==>  and proc_code in ('Y','X')  *
*****************************************************************************/

import com.CommCrd;
import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class CcaB001 extends BaseBatch {
private String PROGNAME = "產生VD沖正異常處理檔 111/04/14 V0.00.06";
CommFunction comm = new CommFunction();
CommString zzstr = new CommString();
CommFTP commFTP = null;
CommRoutine comr = null;
CommCrd comc = new CommCrd();
CommDate zzdate = new CommDate();
private String isFileName="VD_REVERSE.";
private int iiFileNum=-1;
private String rowid = "";
private String tt = "";
private String bodytt = "";
private String days = "";
String businessdate = "";
int tolChargeAmt = 0;
int commit=1;
StringBuffer  str  = null;
int liArg  = 0;
String crtDate1 = "",crtDate2 = "";
//=**************************************************************************** 
public static void main(String[] args) {
	CcaB001 proc = new CcaB001();
	proc.mainProcess(args);
	proc.systemExit();
}

//=============================================================================
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(PROGNAME);

	liArg = args.length;
	
	if (liArg > 2) {
		errExit(1);
	}

	if(liArg == 1) {
		crtDate1 = args[0];
		if(crtDate1.length()!=8) {
			printf("[%s]輸入的參數日期格式錯誤",crtDate1);
			errExit(1);
		}
		printf("crtDate1 = [%s]",crtDate1);
	}
	
	if(liArg == 2) {
		crtDate1 = args[0];
		crtDate2 = args[1];
		if(crtDate1.length()!=8) {
			printf("[%s]輸入的參數日期格式錯誤",crtDate1);
			errExit(1);
		}
		if(crtDate2.length()!=8) {
			printf("[%s]輸入的參數日期格式錯誤",crtDate2);
			errExit(1);
		}
		printf("crtDate1 = [%s]",crtDate1);
		printf("crtDate2 = [%s]",crtDate2);
	}
	
	dbConnect();
	writeBody();
	writeHeader();
	writeFooter();
	checkOpen();
	this.writeTextFile(iiFileNum,tt);
	closeOutputText(iiFileNum);
	commFTP = new CommFTP(getDBconnect(), getDBalias());
	comr = new CommRoutine(getDBconnect(), getDBalias());
	procFTP();
	renameFile();
    sqlCommit(commit);
	//printf("==>程式執行結束, 處理筆數=[%s]==============",""+totalCnt);
	endProgram();
}

/*=************************************************************************/
public void checkOpen() throws Exception {
	
  // String ls_file = this.get_ecsHome()+"/media/cca/"+is_file_name;
	String lsTemp = "";
	lsTemp = String.format("%s/media/cca/%s", comc.getECSHOME(), isFileName); 
	printf("Open File =[%s]", lsTemp);
	iiFileNum =this.openOutputText(lsTemp);
	if( iiFileNum<0) {
		printf("[%s]在程式執行目錄下沒有權限讀寫資料",lsTemp);
		errExit(1);
	}
}

boolean getDays() throws Exception{
	for(int day = 0 ; day <= 99; day++){
		str = new StringBuffer();
//		str.append(" select to_char(sysdate - ");
		str.append(" select to_char(to_date(business_date,'YYYYMMDD') - ");
		str.append(day);
//		str.append(" ,'YYYYMMDD') as beforeday from dual ");
		str.append(" day,'YYYYMMDD') as beforeday from ptr_businday ");
		sqlCmd = str.toString();
		sqlSelect();
		String beforeday = colSs("beforeday");
		sqlCmd = "select count(*) as cnt from ptr_holiday  where holiday = ? ";
		setString(1, beforeday);
		sqlSelect();
		int cnt = colInt("cnt");
		if(day == 0 && cnt > 0){//當天為假日return
			return false;
		}
		if(day > 0 && cnt == 0){//假日併檔
			return true;
		}
		days += ("'" + beforeday + "',");
	}
	return false;
}

//=============================================================================
public void writeHeader() throws Exception{
	
	sqlCmd = "select business_date from ptr_businday";
	sqlSelect();
	businessdate = colSs("business_date");
	isFileName += businessdate;
	
	tt += "1";
	tt += "006";
	tt += businessdate;
	tt += zzstr.space(176-tt.length());
	tt += "\r\n";
	
}
//=============================================================================
void writeBody() throws Exception {
	if(liArg==0) {
		if(!getDays()){
			return;
		}
	}
	str = new StringBuffer();
	str.append(" select hex(rowid) as rowid , ims_reversal_data ,trans_amt ");
	str.append(" from cca_ims_log where 1=1 ");
	if(liArg==0) {
		str.append(" and crt_date in ( ");
		str.append(zzstr.mid(days,0,days.length()-1));
		str.append(" ) ");
	}
	if(liArg==1) {
		str.append(" and crt_date = '");
		str.append(crtDate1);
		str.append("' ");
	}
	if(liArg==2) {
		str.append(" and crt_date >= '");
		str.append(crtDate1);
		str.append("' ");
		str.append(" and crt_date <= '");
		str.append(crtDate2);
		str.append("' ");
	}
	str.append(" and send_date = '' ");
//	str.append(" and proc_code = 'Y' ");
	str.append(" and proc_code in ('Y','X') ");
	sqlCmd = str.toString();
	this.openCursor();
	
	while (fetchTable()) {
		String tt = "";
		String imsReversalData = colSs("ims_reversal_data");
		if(empty(zzstr.mid(imsReversalData, 0,1))) {
			imsReversalData = imsReversalData.replaceFirst("\\s", "");
		}
		if(commString.pos(",2440,2442,2447", commString.mid(imsReversalData, 4,4)) <= 0) {
			continue;
		}
		rowid = colSs("rowid");
		tt += "2";//明細資料 固定值: 2
		tt += "006";//銀行代碼 固定值: 006
		tt += zzstr.mid(imsReversalData, 12,10);//IMS-A-HOST-SEQ-NO 由財金或新信用卡系統編的交易序號
		tt += zzstr.mid(imsReversalData, 28,8);//IMS-A-TXN-DATE 原交易之交易日期
		tt += zzstr.mid(imsReversalData, 36,6);//IMS-A-TXN-TIME 原交易之交易時間
//		tt += zzstr.mid(imsReversalData, 52,4);//處理結果
		tt += zzstr.space(4);//處理結果
		tt += zzstr.mid(imsReversalData, 42,1);//交易代碼
		tt += zzstr.mid(imsReversalData, 57);//上行電文 BODY 長度143
		tt += zzstr.space(176-tt.length());
		bodytt += tt;
		bodytt += "\r\n";
		tolChargeAmt += zzstr.ss2int(zzstr.mid(imsReversalData, 106,10));//總金額CHARGE-AMT
		updateReversalLog();
		totalCnt++;
	}
	this.closeCursor();
}
//=============================================================================
public void writeFooter() throws Exception{
	tt += bodytt;
	String footertt = "";
	footertt += "3";
	footertt += "006";
	footertt += businessdate;
	footertt += zzstr.fixnum("%d",totalCnt,10);
	footertt += zzstr.fixnum("%d",tolChargeAmt,14);
	footertt += zzstr.space(176-footertt.length());
	tt += footertt;
	tt += "\r\n";
	
}

void updateReversalLog() throws Exception{
	sqlCmd = " update cca_ims_log set send_date = to_char(sysdate,'yyyymmdd') ,mod_pgm = 'CcaB001' ,mod_time = sysdate where rowid = ? ";
	sqlExec(new Object[] {hexStrToByteArr(rowid) });
	if (sqlNrow < 0) {
		printf("update cca_ims_log error");
		errExit(sqlNrow);
	}
}


void procFTP() throws Exception {
	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	commFTP.hEriaLocalDir = String.format("%s/media/cca", comc.getECSHOME());
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
	String tmpstr1 = String.format("%s/media/cca/%s", getEcsHome(), isFileName);
	String tmpstr2 = String.format("%s/media/cca/backup/%s", getEcsHome(), isFileName);

	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
		showLogMessage("I", "", "ERROR : 檔案[" + isFileName + "]更名失敗!");
		return;
	}
	showLogMessage("I", "", "檔案 [" + isFileName + "] 已移至 [" + tmpstr2 + "]");
}

}
