/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/07/21  V1.00.12  Allen Ho   initial                                    *
* 110/01/04  V1.00.13  Alex Wang  NMIP FileName Fix    
* 112/02/07  V1.00.14  Machao     sync from mega & updated for project coding standard                      *
* 112/08/27  V1.00.15  Sunny      調整ftp檔案的分類處理                      *
******************************************************************************/
package Sms;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;
import java.text.Normalizer;

@SuppressWarnings("unchecked")
public class SmsE020 extends AccessDAO
{
 private final String PROGNAME = "電子發票-中獎信用卡載具清冊檔案FTP接收處理程式 112/08/27  V1.00.15";
 CommFunction comm = new CommFunction();
 CommFTP commFTP = null;
 CommRoutine comr = null;

 String hBusiBusinessDate = "";
 String hEflgFileName     = "";
 String hEflgProcCode     = "";
 String hEflgProcDesc     = "";
 String hEflgRowid         = "";
 String hEflgRefIpCode   = "";
 String tmpstr1 = "";
 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  SmsE020 proc = new SmsE020();
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

   if ( args.length == 1 ) 
      { hBusiBusinessDate = args[0]; }

   if ( !connectDataBase() )
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   commFTP = new CommFTP(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","===============================");
   /*01*/
   showLogMessage("I","","開始FTP匯入檔案01.....");
   ftpMget("EK0002","01"); 
   showLogMessage("I","","===============================");
   showLogMessage("I","","刪除遠端檔案.....");
   selectEcsFtpLog();
   /*02*/
   showLogMessage("I","","開始FTP匯入檔案02.....");
   ftpMget("EK0002","02"); 
   showLogMessage("I","","===============================");
   showLogMessage("I","","刪除遠端檔案.....");
   selectEcsFtpLog();
   commitDataBase();
   showLogMessage("I","","===============================");
   
   if (totalCnt>0)
     {
      commitDataBase();
      showLogMessage("I","","啟動 SmsE030 執行後續動作 .....");
      showLogMessage("I","","===============================");

      String[] hideArgs = new String[1];
      try {
           hideArgs[0] = hBusiBusinessDate ;
           showLogMessage("I","","hideArgs[0]="+hideArgs[0]);

           SmsE030 smsE030 = new SmsE030();
           int rtn = smsE030.mainProcess(hideArgs);
           if(rtn < 0)   return (1);
           showLogMessage("I","","SmsE030 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex) 
               {
                showLogMessage("I","","無法執行 SmsE030 ERROR!");
               }
     }
   
   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void  selectPtrBusinday() throws Exception
 {
  daoTable   = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (hBusiBusinessDate.length()==0)
      hBusiBusinessDate   =  getValue("BUSINESS_DATE");
  
  showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
 }
// ************************************************************************
 void  ftpMget(String extStr,String srcStr ) throws Exception
 {
//  String[] extStr={"EK0002","EK0002"};
//  String[] srcStr={"01","02"};
//  int inta=0;
  
//  int inta=1;
//  if (hBusiBusinessDate.substring(6,8).equals("29")) inta=0;   
  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  commFTP.hEflgSystemId   = "SMS_FTP_GET";         /* 區分不同類的 FTP 檔案-大類  (必要)*/
  commFTP.hEflgGroupId    = srcStr;     /* 區分不同類的 FTP 檔案-次分類 (非必要)*/
  commFTP.hEflgSourceFrom = "SMS_FTP_GET";           /* 區分不同類的 FTP 檔案-細分類 (非必要)*/
  commFTP.hEriaLocalDir   =  Normalizer.normalize(System.getenv("PROJ_HOME")+"/media/sms/", Normalizer.Form.NFD); 
  commFTP.hEflgModPgm     = "SmsE020";
  System.setProperty("user.dir",commFTP.hEriaLocalDir);
  //tmpstr1 = String.format("get %s017%s*", extStr[inta],hBusiBusinessDate);
  tmpstr1 = String.format("get %s-16744111-006-%s*%s-R.txt", extStr,hBusiBusinessDate,srcStr);
  showLogMessage("I","","get "+extStr+ " 開始接收....");
  int errCode = commFTP.ftplogName("SMS_FTP_GET",tmpstr1);

  if (errCode!=0)
     {
      showLogMessage("I","","ERROR:無法接收資料");
      insertEcsNotifyLog(extStr);
      return;
     }
  totalCnt++;
  showLogMessage("I","","FTP完成.....");
  }
// } 
// ************************************************************************
 void  selectEcsFtpLog() throws Exception
 {
  selectSQL  = "FILE_NAME, "
             + "REF_IP_CODE, "
             + "SOURCE_FROM,"
             + "ROWID as rowid";
  daoTable   = "ECS_FTP_LOG";
  whereStr   = "WHERE trans_seqno     = ? "
             + "AND   system_id       = ? "
             + "AND   trans_resp_code = 'Y' " 
             + "";

  setString(1,commFTP.hEflgTransSeqno);                 
  setString(2,commFTP.hEflgSystemId);                 

  openCursor();

  while( fetchTable() ) 
   { 
    commFTP.hEflgSourceFrom  =  getValue("SOURCE_FROM");
    setValue("proc_code"  , "0");
    setValue("proc_desc" , "");

    showLogMessage("I","","刪除檔案["+ getValue("file_name") +"]");
    int errCode = commFTP.ftplogName(getValue("ref_ip_code"),"delete "+ getValue("file_name"));

    if (errCode!=0)
       {
        showLogMessage("I","","ERROR:刪除檔案["+ getValue("file_name") +"%]失敗.....");
        setValue("proc_code" , "B");
        setValue("proc_desc"  , "刪除檔案["+ getValue("file_name") +"%]失敗");
        updateEcsFtpLog();
        continue;
       }

    setValue("proc_desc"    , "刪除檔案完成");
    updateEcsFtpLog();
   }
  closeCursor();
 }
// ************************************************************************
 void  updateEcsFtpLog() throws Exception
 {
  dateTime();
  updateSQL = "file_name  = ?, "
            + "file_date  = ?, "
            + "proc_code  = ?, "
            + "trans_desc = trans_desc||'+'||?, "
            + "proc_desc  = ?, "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate ";   
  daoTable  = "ecs_ftp_log";
  whereStr  = "where rowid = ?";

  setString(1 , getValue("file_name"));
  setString(2 , hBusiBusinessDate);
  setString(3 , getValue("proc_code"));
  setString(4 , getValue("proc_desc"));
  setString(5 , getValue("proc_desc"));
  setString(6 , javaProgram);
  setRowId(7  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE ecs_ftp_log error "+ getValue("rowid")); 
      exitProgram(0); 
     }
  return;
 }
// ************************************************************************
 int insertEcsNotifyLog(String fileName) throws Exception
 {
  setValue("crt_date"           , sysDate);
  setValue("crt_time"           , sysTime);
  setValue("unit_code"          , comr.getObjectOwner("3",javaProgram));
  setValue("obj_type"           , "3");
  setValue("notify_head"        , "無法 FTP 接收 "+fileName+"資料");
  setValue("notify_name"        , "媒體檔名:"+fileName);
  setValue("notify_desc1"       , "程式 "+javaProgram+" 無法 FTP 接收 "+fileName+" 資料");
  setValue("notify_desc2"       , "");
  setValue("trans_seqno"        , commFTP.hEflgTransSeqno);
  setValue("mod_time"           , sysDate+sysTime);
  setValue("mod_pgm"            , javaProgram);
  daoTable  = "ecs_notify_log";

  insertTable();

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

