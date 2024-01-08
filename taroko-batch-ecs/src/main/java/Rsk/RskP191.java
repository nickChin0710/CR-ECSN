/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- * 
*  112-03-13  V1.00.00  JH          program initial                          *
*  112-04-06  V1.00.01  Alex        檔案長度=00                                *
*  112-04-24  V1.00.02  Alex        新增將檔案進行FTP至 /crdatacrea/ELOAN 並backup *    
*  112-05-10  V1.00.03  Alex        取消 HDR 檔案                                                                                * 
*  112-06-06  V1.00.04  Alex        副檔名修正為.txt , 取消檔名日期
 *  2023-1120 V1.00.05  JH       is busi-date run
*****************************************************************************/
package Rsk;

import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;
public class RskP191 extends com.BaseBatch {
private final String PROGNAME = "傳送給ELOAN使用卡片資料檔-附卡(CDESEG)  2023-1120 V1.00.05";
RskP191.HH hh=new RskP191.HH();
//-----------
class HH {
   String id_pseqno="";
   String majid_pseqno="";
   //-------
   String id_no="";  //附卡人ID	   crdidn	X(10)	只要是附卡就列
   String card_no="";  //信用卡卡號	crdcno	X(16)
   String maj_idno="";  //正卡人ID	   crdpid	X(10)	crd_card.major_id_p_seqno 去串crd_idno
   String card_since="";  //轉入日期	   crddate	TIMESTAMP	crd_idno.card_since

   void init_data() {
      id_pseqno="";
      majid_pseqno="";
      card_no="";
      id_no="";
      maj_idno="";
      card_since="";
   }
}
//---------
String isFileName="CDESEG";
//int iiFileNumHH=-1, 
int iiFileNumDD=-1;
int iiOutCnt=0;
CommFTP commFTP = null;
CommRoutine comr = null;
CommCrd comc = new CommCrd();
//=*****************************************************************************
public static void main(String[] args) {
   RskP191 proc = new RskP191();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 1) {
      printf("Usage : RskP191 [busi_date]");
      okExit(0);
   }

   dbConnect();

   String ls_runDate="";
   if (args.length >= 1) {
      setBusiDate(args[0]);
      ls_runDate =hBusiDate;
   }
   else ls_runDate =sysDate;
//   callBatch(0, 0, 0);

   if (checkWorkDate(ls_runDate)) {
      printf("-- [%s]非營業日, 不執行", ls_runDate);
      okExit(0);
   }

   fileOpen();
   selectCrdCard();

   printf("處理筆數:[%s]",totalCnt);
   sqlCommit();
   
   //--傳檔
   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();		 
   
   endProgram();
}
//==============
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
	
	showLogMessage("I", "", "mput " + isFileName+".txt" + " 開始傳送....");	
	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + isFileName+".txt");

	if (errCode != 0) {
//		showLogMessage("I", "", "ERROR:無法傳送 " + isFileName+"_"+commString.right(sysDate,6)+".DAT" + " 資料" + " errcode:" + errCode);
//		insertEcsNotifyLog(isFileName+"_"+commString.right(sysDate,6)+".DAT");
		showLogMessage("I", "", "ERROR:無法傳送 " + isFileName+".txt" + " 資料" + " errcode:" + errCode);
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
//		showLogMessage("I", "", "ERROR : 檔案[" + isFileName+"_"+commString.right(sysDate,6)+".DAT" + "]更名失敗!");
		showLogMessage("I", "", "ERROR : 檔案[" + isFileName+".txt" + "]更名失敗!");
		return;
	}
//	showLogMessage("I", "", "檔案 [" + isFileName+"_"+commString.right(sysDate,6)+".DAT" + "] 已移至 [" + tmpstr4 + "]");
	showLogMessage("I", "", "檔案 [" + isFileName+".txt" + "] 已移至 [" + tmpstr4 + "]");
	
}
//==============
void selectCrdCard() throws Exception {
   String cc="" , newLine = "\r\n";  //"$$";
   sqlCmd = "select A.card_no, A.issue_date, A.ori_issue_date"
             +", A.id_p_seqno, A.major_id_p_seqno"
             +", uf_idno_id(A.major_id_p_seqno) maj_idno"
             +", B.id_no, B.card_since"
             + " from crd_card A JOIN crd_idno B ON B.id_p_seqno=A.id_p_seqno"
             + " where 1=1"
             +" and A.sup_flag ='1'"
//             + " order by A.card_no"
//	+ commSqlStr.rownum(2000)
   ;

   //ppp(1,isProcYymm);

   this.openCursor();
   while (fetchTable()) {
      totalCnt++;
      hh.init_data();

      dspProcRow(10000);

      hh.id_pseqno =colSs("id_p_seqno");
      hh.majid_pseqno =colSs("major_id_p_seqno");
//      if (empty(hh.id_pseqno)) continue;

      //1.附卡人ID	   crdidn	X(10)	只要是附卡就列
      hh.id_no =colSs("id_no");
      //2.信用卡卡號	crdcno	X(16)
      hh.card_no =colSs("card_no");
      //3.正卡人ID	   crdpid	X(10)
      hh.maj_idno =colSs("maj_idno");
      //4.轉入日期	   crddate	TIMESTAMP
      hh.card_since =colSs("card_since");
      if (empty(hh.card_since))
         hh.card_since =colSs("ori_issue_date");
      if (empty(hh.card_since))
         hh.card_since =colSs("issue_date");

      //--
      iiOutCnt++;
      String lsTxt=commString.rpad(hh.id_no,10)+cc  //1.附卡人ID	   crdidn	X(10)
              +"00"+cc  //檔案長度	PIC 9(03)  	2
              +commString.rpad(hh.card_no,16)+cc	 //2.信用卡卡號	crdcno	X(16)
                    +commString.rpad(hh.maj_idno,10)+cc  //3.正卡人ID	   crdpid	X(10)
                    //+commString.rpad(hh.card_since,8)+cc  //4.轉入日期	   crddate	TIMESTAMP
              +commString.space(22)  //
                    +newLine;
      writeTextFile(iiFileNumDD,lsTxt);
   }
   closeCursor();

   closeOutputText(iiFileNumDD);
   printf(" 產生筆數[%s]", iiOutCnt);

   //--
   //FILENAME_YYMMDD.HDR
   //1-32 為檔名(左靠)
   //33-40 為處理日
   //41-42 塞 ‘00’
   //43-56 為檔案產生年月日時分杪
   //57-64為筆數(右靠)

//   String ss=isFileName+"_"+commString.right(sysDate,6)+".DAT"
//              +sysDate+"00"+sysDate+sysTime
//              +commString.lpad(""+iiOutCnt,8,"0");
//   writeTextFile(iiFileNumHH,ss);
//   closeOutputText(iiFileNumHH);
}

//=========
void fileOpen() throws Exception {
   //--
//   String lsFileHH= getEcsHome() + "/media/rsk/" + isFileName+"_"+commString.right(sysDate,6)+".HDR";
//   printf("open out-file [%s]", lsFileHH);
//   iiFileNumHH =openOutputText(lsFileHH);
//   if (iiFileNumHH <0) {
//      errmsg("在程式執行目錄下沒有權限讀寫資料, file[%s]",lsFileHH);
//      errExit(1);
//   }
   //--
//   String lsFileDD= getEcsHome() + "/media/rsk/" + isFileName+"_"+commString.right(sysDate,6)+".DAT";
   //isFileName="CDESEG";
   String lsFileDD= getEcsHome() + "/media/rsk/" + isFileName+".txt";
   printf("open out-file [%s]", lsFileDD);
   iiFileNumDD =openOutputText(lsFileDD);
   if (iiFileNumDD <0) {
      errmsg("在程式執行目錄下沒有權限讀寫資料, file[%s]",lsFileDD);
      errExit(1);
   }
}

}
