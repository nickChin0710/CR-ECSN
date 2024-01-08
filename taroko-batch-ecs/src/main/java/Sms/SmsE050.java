/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/08/06  V1.00.08  Allen Ho   initial                                    *
* 112/02/07  V1.00.11  YANG HAN   updated for project coding standard        *
* 112/05/12  V1.00.12  Sunny      change File Name                           *
* 112/08/31  V1.00.13  Ryan       檔案傳送至FISC_FTP_PUT並備份               *
* 112/09/27  V1.01.14  Sunny      調整收檔檔名處理邏輯                       *                                * 
* 112/10/19  V1.01.15  Sunny      調整收檔group_id為CF305，與SmsE070一致     *
* 112/10/23  V1.01.16  Sunny      取消count數乘以2的動作                     *
******************************************************************************/
package Sms;

import com.*;

import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("unchecked")
public class SmsE050 extends AccessDAO
{
 private final String PROGNAME = "電子發票-主動註銷載具通知清冊媒體產處理程式 112/10/19  V1.01.15";
 CommCrd comc = new CommCrd();
 CommDate  commDate = new CommDate();
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommFTP commFTP = null;

 String isFileName = "CF305-EK9999-16744111-006-"+ commDate.sysDate() +"-01-D.txt";
 
 final Base64.Decoder decoder = Base64.getDecoder();
 final Base64.Encoder encoder = Base64.getEncoder();

 String businessDate = "";

 String fileName1="";
 String fileName="";
 String outData = "";
 String dateStr = "";
 int fo;
 long    totalCnt=0;
 String  newLine="\n";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  SmsE050 proc = new SmsE050();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   dateStr= sysDate;
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);
  
    if (comm.isAppActive(javaProgram)) 
       return(1);

   if ( args.length == 1 ) 
      { businessDate = args[0]; }

   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
  
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理送出資料");
   selectSmsEinvoCancel0();
/*
   if (getValueInt("cncl.h_count")==0)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","本日無媒體資料需處理");
       showLogMessage("I","","=========================================");
       finalProcess();
       return(0);
      }
*/
   openFile();
   selectSmsEinvoCancel();
   closeOutputText(fo);

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   moveFile(isFileName,String.format("%s/media/sms", comc.getECSHOME()));
   showLogMessage("I","","=========================================");
   showLogMessage("I","","產生送出紀錄檔");
   if (insertSmsEinvoFtplog()!=0)
       updateSmsEinvoFtplog();
   showLogMessage("I","","=========================================");
  
   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
 {
  daoTable   = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (businessDate.length()==0)
      businessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
 }
// ************************************************************************
 void selectSmsEinvoCancel() throws Exception
 {
  selectSQL = "card_no, "
            + "substr(card_no,13,4) as card_no4,"
            + "e_mail_addr,"
            + "cellar_phone,"
            + "rowid as rowid";
  daoTable  = "sms_einvo_cancel";
  whereStr  = "WHERE send_date = ''";

  openCursor();

  while( fetchTable() ) 
   { 
    totalCnt++;                 
    dateTime();
    String Text = comm.sha256(getValue("card_no"));
    byte[] textByte = comm.hexToByte(Text);

    String tmpStr = getValue("card_no").substring(0,6)+ encoder.encodeToString(textByte) ;
    String tmpStrNew = "B00006"+ encoder.encodeToString(textByte) ;

    outData = comm.formatReport(outData,dateStr ,1);
    outData = comm.formatReport(outData,sysTime ,9);
    outData = comm.formatReport(outData,tmpStr  ,15);
    outData = comm.formatReport(outData,getValue("card_no4")  ,65);
    outData = comm.formatReport(outData,"0" ,89);
    outData = comm.formatReport(outData,getValue("e_mail_addr")  ,90);
    outData = comm.formatReport(outData,getValue("cellar_phone") ,154);
    outData = comm.formatReport(outData, newLine ,174);

    writeTextFile(fo, outData);

    updateSmsEinvoCancel();

    processDisplay(10000); // every 10000 display message
   }
  closeCursor();
 }
// ************************************************************************
 void openFile() throws Exception
 {
  //fileName1 = "O027017" + "01C";
  isFileName = "CF305-EK9999-16744111-006-"+ businessDate +"-01-D.txt";
  fileName= System.getenv("PROJ_HOME")+"/media/sms/"+isFileName;
  fo = openOutputText(fileName,"MS950");
  if (fo == -1)
     {
      showLogMessage("I","","ERROR:無法產生 OK檔 資料");
      insertEcsNotifyLog(fileName);
      commitDataBase();
      exitProgram(1);
     }
  outData = comm.formatReport(outData,dateStr ,1);
  outData = comm.formatReport(outData,sysTime ,9);
  //String kk3 = String.format("%d", getValueInt("cncl.h_count")*2);
  String kk3 = String.format("%d", getValueInt("cncl.h_count"));
  while (kk3.length() < 10) kk3 = "0" + kk3;
  outData = comm.formatReport(outData,kk3  ,15);
  outData = comm.formatReport(outData, newLine ,45);
  writeTextFile(fo, outData);
 }
// ************************************************************************
 void updateSmsEinvoCancel() throws Exception
 {
  updateSQL = "send_date = ?, "
            + "send_time = ?, "
            + "mod_time  = sysdate,"  
            + "mod_pgm   = ? ";
  daoTable  = "sms_einvo_cancel";
  whereStr  = "where rowid   = ? ";

  setString(1 , dateStr);
  setString(2 , sysTime);
  setString(3 , javaProgram);
  setRowId(4 , getValue("rowid"));

  int cnt = updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE sms_einvo_cancel error "+getValue("rowid"));
      exitProgram(1);
     }

  return;
 }
// ************************************************************************
 int selectSmsEinvoCancel0() throws Exception
 {
  extendField = "cncl.";
  selectSQL = "count(*) as h_count";  
  daoTable  = "sms_einvo_cancel";
  whereStr  = "WHERE send_date = ''";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int insertSmsEinvoFtplog() throws Exception
 {
  dateTime();
  extendField = "elog.";
  setValue("elog.file_name"            , fileName1);
  setValue("elog.file_date"            , dateStr);
  setValue("elog.file_time"            , sysTime);
  setValueInt("elog.total_cnt"         , getValueInt("cncl.h_count")*2);
  setValue("elog.ftp_date"             , "");
  setValue("elog.ftp_time"             , "");
  setValue("elog.mod_time"             , sysDate+sysTime);
  setValue("elog.mod_pgm"              , javaProgram);

  daoTable = "sms_einvo_ftplog";

  insertTable();

  if ( dupRecord.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 void updateSmsEinvoFtplog() throws Exception
 {
  dateTime();

  updateSQL = "ftp_date   = '', "
            + "ftp_time   = '', "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "sms_einvo_ftplog";
  whereStr  = "WHERE file_name = ?";

  setString(1 , javaProgram);
  setString(2 , fileName1);

  updateTable();

  return;
 }
// ************************************************************************
 int insertEcsNotifyLog(String fileName) throws Exception
 {
  dateTime();
  extendField = "noti.";
  setValue("noti.crt_date"           , sysDate);
  setValue("noti.crt_time"           , sysTime);
  setValue("noti.unit_code"          , comr.getObjectOwner("3",javaProgram));
  setValue("noti.obj_type"           , "3");
  setValue("noti.notify_head"        , "無法產生 主動註銷載具通知清冊 資料");
  setValue("noti.notify_name"        , "媒體檔名:"+fileName);
  setValue("noti.notify_desc1"       , "程式 SmsE050 無法產生 主動註銷載具通知清冊 資料");
  setValue("noti.notify_desc2"       , "");
  setValue("noti.trans_seqno"        , "");
  setValue("noti.mod_time"           , sysDate+sysTime);
  setValue("noti.mod_pgm"            , javaProgram);
  daoTable  = "ecs_notify_log";

  insertTable();

  return(0);
 }
 
 //***********************************************************************/
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "FISC_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/sms/", comc.getECSHOME());
		commFTP.hEflgGroupId = "CF305"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("FISC_FTP_PUT", "mput " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(isFileName);
		}
	}
	
	void moveFile(String datFileName1, String fileFolder1) throws Exception {
		String tmpFileName = String.format("%s.%s", datFileName1,sysDate);
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder1, "/backup", tmpFileName).toString();

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName1 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}
	
// ************************************************************************

}  // End of class FetchSample

