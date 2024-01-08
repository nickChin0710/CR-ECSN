package Icu;
/** 
 * V00.01	2020-0908	Ryan		initial
 * 109/09/14  V1.00.02  Wilson      新增procFTP                              *
 * 109/09/30  V1.00.03  Wilson      無資料產生空檔                                                                                    *
 * 109/10/14  V1.00.04  Wilson      LOCAL_FTP_PUT -> NCR2TCB                
 *  109-10-19  V1.00.03    shiyuqi       updated for project coding standard     *
 *  109/12/30  V1.00.03  yanghan       修改了部分无意义的變量名稱          *
 *  110/02/01  V1.00.07  Wilson       改營業日          *
 *  110/12/28  V1.00.08  Ryan       writeText() 增加排除條件 mod_pgm != 'CnvOempayCard'  and 'CnvOempayCard2'    *
 * */ 

import com.CommCrd;

import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;


public class IcuD081 extends BaseBatch {
private String progname = "產生google pay卡號對照檔 110/12/28 V1.00.08";

CommCrd comc = new CommCrd();
CommDate  commDate = new CommDate();
CommFTP commFTP = null;
CommRoutine comr = null;

private String fileName="EPAYNO_";
private int iiFileNum=-1;
String allStr = "";
String crtDate = "";
String sysDate = "";
String hOpenDate = "";
int commit=1;
String busiDate = "";
String isFileName = "";

//=**************************************************************************** 
public static void main(String[] args) {
	IcuD081 proc = new IcuD081();
	proc.mainProcess(args);
	proc.systemExit();
}

//=============================================================================
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	int liArg=args.length;
	if (liArg > 1) {
		errExit(1);
	}
	dbConnect();
	
	busiDate = getBusiDate();
	readSysdate();
	
	if(liArg == 0){
		crtDate = busiDate;
	}
	if(liArg == 1){
		crtDate = args[0];
	}
	//printf("傳送日期 =[%s]", crtDate);
	
	writeText();
	    isFileName = fileName + crtDate + ".txt";
		checkOpen();
		this.writeTextFile(iiFileNum,allStr);	
		this.closeCursor();
		
		commFTP = new CommFTP(getDBconnect(), getDBalias());
	    comr = new CommRoutine(getDBconnect(), getDBalias());
	    procFTP();
	    renameFile1(isFileName);

	sqlCommit(commit);
	printf("==>程式執行結束, 處理筆數=[%s]==============",""+totalCnt);
	endProgram();
}

/*=************************************************************************/
public void checkOpen() throws Exception {
	
  // String ls_file = this.get_ecsHome()+"/media/cca/"+is_file_name;
	String lsTemp = "";
	lsTemp = String.format("%s/media/icu/out/%s", comc.getECSHOME(), isFileName); 
	printf("Open File =[%s]", lsTemp);
	iiFileNum =this.openOutputText(lsTemp);
	if( iiFileNum<0) {
		printf("[%s]在程式執行目錄下沒有權限讀寫資料",lsTemp);
		errExit(1);
	}
}

//=============================================================================
public void readSysdate() throws Exception{
	
	sqlCmd = "select to_char(sysdate,'yyyymmdd') as sysdate, ";
	sqlCmd += " to_char(sysdate-1,'yyyymmdd') as sysdate1 ";
	sqlCmd += "from dual";
	sqlSelect();
	sysDate = colSs("sysdate");
	hOpenDate = colSs("sysdate1");
	
}
//=============================================================================
void writeText() throws Exception {
	sqlCmd = " select card_no,v_card_no "
		+ " from oempay_card  "
		+ " where crt_date = ? and mod_pgm not in('CnvOempayCard','CnvOempayCard2') "
		;
	setString(1,crtDate);
	this.openCursor();

	while (fetchTable()) {
		allStr += commString.rpad(colSs("card_no"),19);
		allStr += commString.rpad(colSs("v_card_no"),19);
		allStr += "\r\n";
		totalCnt++;
	    }
    }
/***********************************************************************/
void procFTP() throws Exception {
	  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
      commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
      commFTP.hEriaLocalDir = String.format("%s/media/icu/out", comc.getECSHOME());
      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
      commFTP.hEflgModPgm = javaProgram;
      

      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
      showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
      int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);
      
      if (errCode != 0) {
          showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料"+" errcode:"+errCode);
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
	void renameFile1(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/out/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
		
		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

}
