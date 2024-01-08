/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/02/08  V1.00.11  Allen Ho   sms_e070                                   *
* 112/02/07  V1.01.12  jiangyigndong  updated for project coding standard    *
* 112/09/27  V1.01.13  Sunny      調整收檔檔名處理邏輯                                                        *
******************************************************************************/
package Sms;

import com.*;

import java.lang.*;
import java.text.Normalizer;

@SuppressWarnings("unchecked")
public class SmsE070 extends AccessDAO
{
 private final String PROGNAME = "電子發票-註銷信用卡載具清冊檔案FTP接收處理程式 112/09/27  V1.01.13";
 CommFunction comm = new CommFunction();
 CommFTP commFTP = null;
 CommRoutine comr = null;

 String businessDate = "";
 String systemDate = "";

 String fileName="";
 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  SmsE070 proc = new SmsE070();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);
  
    if (comm.isAppActive(javaProgram))
       return(1);

   systemDate = sysDate;
   if ( args.length == 1 ) 
      { systemDate = args[0]; }

   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   commFTP = new CommFTP(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","開始FTP匯入檔案.....");

   showLogMessage("I","","===============================");
   showLogMessage("I","","檢核FTP匯出檔案是否回應.....");
   selectEcsFtpLog();
   showLogMessage("I","","===============================");

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
 void selectEcsFtpLog() throws Exception
 {
  extendField = "eflg.";
  selectSQL  = "file_name, "
//		     + "left(file_name,37) file_name_str37, "
             + "rowid as rowid";
  daoTable   = "ecs_ftp_log";
  whereStr   = "where decode(proc_code,'','N',proc_code)  = 'N' "
             + "and   system_id       = 'FISC_FTP_PUT' "
             + "and   group_id        = 'CF305' "
             + "and   trans_mode      = 'SEND' "
             + "and   file_name  LIKE 'CF305-EK9999%' "
             + "and   trans_resp_code = 'Y' "                      // 20210201
             + "and   crt_date        = ? "
             + "order by file_date ASC";

  setString(1, systemDate); //systemDate

  int recCnt = selectTable();
 
  if (recCnt==0)
     {
	  showLogMessage("I","","本日 ["+ systemDate +"] 無傳輸記錄可接收");
     return;
     }

  int runFlag=0;
  for ( int inti=0; inti<recCnt; inti++ )
    {
     totalCnt=0;
     //String hFileName="";
     showLogMessage("I","","轉入媒體檔["+ getValue("eflg.file_name",inti)+"]");
     fileName=getValue("eflg.file_name",inti).substring(0, 37); //取檔名前37個字元
     showLogMessage("I","","取檔名前37個字["+ fileName +"]");
     if (ftpMget(fileName)!=0) continue;

     selectEcsFtpLog1();
     updateEcsFtpLog1();
     commitDataBase();

     if (runFlag==0)
       {
        runFlag=1;
        commitDataBase();
        showLogMessage("I","","啟動 SmsE080 執行後續動作 .....");
        showLogMessage("I","","===============================");

        String[] hideArgs = new String[1];
        try {
             //hideArgs[0] = systemDate;
        	hideArgs[0] = businessDate;
        
             SmsE080 smsE080 = new SmsE080();
             int rtn = smsE080.mainProcess(hideArgs);
             if(rtn < 0)   return ;
             showLogMessage("I","","SmsE080 執行結束");
             showLogMessage("I","","===============================");
            } catch (Exception ex) 
                 {
                  showLogMessage("I","","無法執行 SmsE080 ERROR!");
                 }
       }
   }
 }
// ************************************************************************
 int ftpMget(String fileName) throws Exception
 {
  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  commFTP.hEflgSystemId   = "FISC_FTP_PUT";        /* 區分不同類的 FTP 檔案-大類  (必要)*/
  commFTP.hEflgGroupId    = "CF305";               /* 區分不同類的 FTP 檔案-次分類 (非必要)*/
  commFTP.hEflgSourceFrom = "CF305";               /* 區分不同類的 FTP 檔案-細分類 (非必要)*/
  commFTP.hEriaLocalDir   =  Normalizer.normalize(System.getenv("PROJ_HOME")+"/media/sms/", Normalizer.Form.NFD);
  commFTP.hEflgModPgm = "SmsE070";
  System.setProperty("user.dir",commFTP.hEriaLocalDir);
  setValue("ref_ip_code"     , "SMS_FTP_GET");

  //fileName = "I027017"+ systemDate;
 
  //fileName = fileName +"-R-"+ businessDate+"*";
  fileName = fileName +"-R-*";
  showLogMessage("I","","mget "+fileName+ " 開始接收....");
  //int errCode = commFTP.ftplogName(getValue("ref_ip_code"),"mget "+fileName+"*");
  int errCode = commFTP.ftplogName(getValue("ref_ip_code"),"mget "+fileName);

  if (errCode!=0)
     {
      showLogMessage("I","","ERROR:無法接收資料");
      insertEcsNotifyLog(fileName);
      return(1);
     }
  totalCnt++;
  showLogMessage("I","","FTP完成.....");
  return(0);
 } 
// ************************************************************************
 void selectEcsFtpLog1() throws Exception
 {
  selectSQL  = "file_name, "
             + "ref_ip_code, "
             + "source_from,"
             + "rowid as rowid";
  daoTable   = "ecs_ftp_log";
  whereStr   = "where trans_seqno   = ? "
             + "and   system_id     = 'FISC_FTP_PUT' "
             + "and   group_id      = 'CF305' "
             + "and   trans_resp_code = 'Y' " 
             + "";

  setString(1,commFTP.hEflgTransSeqno);
                   
  openCursor();

  while( fetchTable() )
   { 
    commFTP.hEflgSourceFrom  =  getValue("SOURCE_FROM");
    setValue("proc_code"    , "0");
    setValue("proc_desc"    , "");

    showLogMessage("I","","刪除檔案["+ getValue("file_name") +"]");
    
    int errCode = commFTP.ftplogName(getValue("ref_ip_code"),"delete "+getValue("file_name"));

    if (errCode!=0)
       {
        showLogMessage("I","","ERROR:刪除檔案["+getValue("file_name")+"%]失敗.....");
        setValue("proc_code"       , "B");
        setValue("proc_desc"       , "刪除檔案["+getValue("file_name")+"%]失敗");
        updateEcsFtpLog();
        continue;
       }
    showLogMessage("I","","刪除檔案["+getValue("file_name")+"]完成.....");

    setValue("proc_desc"    , "遠端刪除檔案完成");
    updateEcsFtpLog();
   }
  closeCursor();
 }
// ************************************************************************
 void updateEcsFtpLog() throws Exception
 {
  dateTime();
  updateSQL = "file_date = ?, "
            + "proc_code = ?, "
            + "proc_desc = ?, "
            + "trans_desc = trans_desc||?, "
            + "mod_pgm   = ?, "
            + "mod_time  = sysdate ";   
  daoTable  = "ecs_ftp_log";
  whereStr  = "where rowid = ?";

  setString(1 , businessDate);
  setString(2 , getValue("proc_code"));
  setString(3 , getValue("proc_desc"));
  setString(4 , getValue("proc_desc"));
  setString(5 , javaProgram);
  setRowId(6  , getValue("rowid"));

  int cnt = updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE ecs_ftp_log error "+getValue("eflg.eflg_rowid")); 
      exitProgram(0); 
     }
  return;
 }
// ************************************************************************
 void updateEcsFtpLog1() throws Exception
 {
  dateTime();
  updateSQL = "proc_code  = 'Y', "
            + "proc_desc  = ?, "
            + "trans_desc = trans_desc||?, "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate ";   
  daoTable  = "ecs_ftp_log";
  whereStr  = "where rowid = ?";

  setString(1 , "註銷清冊已接收完成");
  setString(2 , "註銷清冊已接收完成");
  setString(3 , javaProgram);
  setRowId(4  , getValue("eflg.rowid"));

  int cnt = updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE ecs_ftp_log_1 error "+getValue("rowid")); 
      exitProgram(0); 
     }
  return;
 }
// ************************************************************************
 int insertEcsNotifyLog(String fileName) throws Exception
 {
  extendField = "noti.";
  setValue("noti.crt_date"           , sysDate);
  setValue("noti.crt_time"           , sysTime);
  setValue("noti.unit_code"          , comr.getObjectOwner("3",javaProgram));
  setValue("noti.obj_type"           , "3");
  setValue("noti.notify_head"        , "無法 FTP 接收 註銷信用卡載具清冊檔案 資料");
  setValue("noti.notify_name"        , "媒體檔名:"+fileName);
  setValue("noti.notify_desc1"       , "程式 "+javaProgram+" 無法 FTP 接收 註銷信用卡載具清冊檔案 資料");
  setValue("noti.notify_desc2"       , "");
  setValue("noti.trans_seqno"        , commFTP.hEflgTransSeqno);
  setValue("noti.mod_time"           , sysDate+sysTime);
  setValue("noti.mod_pgm"            , javaProgram);
  daoTable  = "ecs_notify_log";

  insertTable();

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

