/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/02/22  V1.00.09  Allen Ho   sms_e080                                   *
* 112/02/07  V1.01.10  jiangyigndong  updated for project coding standard    *
*                                                                            *
******************************************************************************/
package Sms;

import com.*;

import java.io.*;
import java.text.Normalizer;

@SuppressWarnings("unchecked")
public class SmsE080 extends AccessDAO
{
 private final String PROGNAME = "電子發票-主動註銷載具通知清冊媒體轉入處理程式 110/02/22 V1.00.09";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String hBusiBusinessDate = "";
 String hTempSysdate = "";
 int lineSeq = 0;

 String readData="";                       
 int fi = 0;
 int[] begPos = {0};
 long    totalCnt=0,errorCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  SmsE080 proc = new SmsE080();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);
  
    if (comm.isAppActive(javaProgram)) 
       return(1);

   if ( args.length == 1 ) 
      if (args[0].length()==8)  hTempSysdate = args[0];

   if ( !connectDataBase() )
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
  
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理匯入檔案.....");
   selectEcsFtpLog();
   showLogMessage("I","","===============================");
  
   finalProcess();
   return(0);
  }
  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void selectPtrBusinday() throws Exception
 {
  selectSQL = "business_date, "
            + "to_char(sysdate,'yyyymmdd') AS FILE_DATE ";
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();
    
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }
  hBusiBusinessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ hBusiBusinessDate +"]");
 }
// ************************************************************************
 void selectEcsFtpLog() throws Exception
 {
  selectSQL  = "file_name, "
             + "local_dir,"
             + "rowid as rowid";
  daoTable   = "ecs_ftp_log";
  whereStr   = "WHERE proc_code       = '0' " 
          //   + "AND   system_id       = 'CF305' "
             + "AND   system_id       = 'FISC_FTP_PUT' "
             + "AND   group_id        = 'CF305' "
             + "AND   trans_resp_code = 'Y' "
             + "AND   trans_mode      = 'RECV' "
             + "";

  if (hTempSysdate.length()!=0)
     {
      showLogMessage("I","","依指定日期["+ hTempSysdate +"]執行.....");
      whereStr = whereStr + "and  crt_date   = ? ";
      setString(1 , hTempSysdate);
     }
  else
     {
      showLogMessage("I","","依傳輸記錄執行.....");
     }

  whereStr = whereStr + "ORDER BY file_date,crt_date,crt_time" ;

  openCursor();

 int FtotalCnt=0;
  while( fetchTable() )
   {
    FtotalCnt++;
    showLogMessage("I","","處理檔案["+getValue("file_name")+"].....");

    openFile();

    setValue("proc_code" , "Y");
    if (errorCnt>0) setValue("proc_code" , "X");
    updateEcsFtpLog();
    renameFile(getValue("file_name"));
    commitDataBase();
   }
  if (FtotalCnt==0) 
     {
      showLogMessage("I","","本日 ["+ hTempSysdate +"] 無傳輸記錄可執行.");
      return;
     }
 }
// ************************************************************************
 void openFile() throws Exception
 {
  String checkHome = System.getenv("PROJ_HOME");
  String fileName = checkHome + "/media/sms/"+getValue("file_name");
  fi = openInputText(fileName,"MS950");
  if (fi == -1 ) 
     {
       setValue("noti.notify_head"      , "媒體檔無開啟權限");
       //setValue("noti.notify_name"      , "媒體檔名:"+ getValue("file_name"));
       setValue("noti.notify_name"      , getValue("file_name"));
       setValue("noti.notify_desc1"     , getValue("file_name")+" 權限不足, 無法開啟");
       setValue("noti.notify_desc2"     , "");
       insertEcsNotifyLog();
       renameFile(getValue("file_name"));
       showLogMessage("I","", getValue("noti.notify_head")+" 不處理...");
       return;
     }
  setValue("file_desc" , "檔案處理中");
  commitDataBase();

  lineSeq =0;
  errorCnt=0;
  int    fileCnt =0;
  totalCnt=0;
  int errFlag=0;
  int dup_flag=0;

  while ( true )
   {
    readData = readTextFile(fi).replace("^\n","").replace("^\r","").replace("\t", "").trim();
    if ( endFile[fi].equals("Y") ) break;
    lineSeq++;
    if ( readData.length()<2 ) continue; 

    begPos[0] = 1;
    if (lineSeq ==1)
       {
        setValue("secl.file_date"      , comm.structStr(readData,begPos,8));
        setValue("secl.file_time"      , comm.structStr(readData,begPos,6));
        setValue("secl.query_records"  , comm.structStr(readData,begPos,10));
        setValue("secl.result_records" , comm.structStr(readData,begPos,10));
        continue;
       }
    setValue("secl.snd_date"       , comm.structStr(readData,begPos,8));
    setValue("secl.snd_time"       , comm.structStr(readData,begPos,6));
    setValue("secl.card_encr"      , comm.structStr(readData,begPos,64));
    setValue("secl.apr_code"       , comm.structStr(readData,begPos,10));
    setValue("secl.apr_sec_flag"   , comm.structStr(readData,begPos, 1));
    setValue("enc_card_no"         , getValue("secl.card_encr").substring(6,50));
/*
    showLogMessage("I","","secl.snd_date     ["+ getValue("secl.snd_date")+"]");
    showLogMessage("I","","secl.snd_time     ["+ getValue("secl.snd_time")+"]");    
    showLogMessage("I","","secl.card_encr    ["+ getValue("secl.card_encr")+"]");    
    showLogMessage("I","","secl.apr_code     ["+ getValue("secl.apr_code")+"]");    
    showLogMessage("I","","secl.apr_sec_flag ["+ getValue("secl.apr_sec_flag")+"]");    
    showLogMessage("I","","enc_card_no ["+ getValue("enc_card_no")+"]");    
*/      

    if (selectSmsEinvoCard()!=0)
       {
//     update_sms_einvo_cancel_1();
        errFlag=1;
        errorCnt++;
        if (errorCnt>100) continue;
        showLogMessage("I","","Line ["+(lineSeq -1)+"] 找不到對應卡號["+getValue("enc_card_no")+"]");

        setValue("elog.main_desc"        , "資料內容錯誤");
        setValue("elog.error_seq"        , "2");
        setValue("elog.column_seq"       , "2");
        setValue("elog.column_data"      , "卡號");
        setValue("elog.error_desc"       , "找不到對應卡號");
        setValue("elog.column_desc"      , "解碼後卡檔找不到");
        insertEcsMediaErrlog();
        commitDataBase();
        continue;
       }

    updateSmsEinvoCancel();
   }

  if (lineSeq !=Integer.valueOf(getValueInt("secl.query_records"))+1)
     {
      showLogMessage("I","","ERROR:資料筆數與實際筆數不符["+
                            (lineSeq -1)+"]!=["+(getValueInt("semt.query_records")+1)+"]");
//    update_sms_einvo_cancel_1();
      errFlag=1;

      setValue("elog.main_desc"        , "資料內容錯誤");
      setValue("elog.error_seq"        , "2");
      setValue("elog.column_seq"       , "6");
      setValue("elog.column_data"      , "資料筆數");
      setValue("elog.error_desc"       , "資料筆數與實際筆數不符");
      setValue("elog.column_desc"      , "實際筆數"+totalCnt+"] 媒體標示筆數["+getValue("semt.query_records")+1+"]" );
      insertEcsMediaErrlog();
      commitDataBase();
      setValue("proc_desc"  , "ERROR:資料筆數與實際筆數不符");
      return;
     }
  if (errFlag!=0)
     {
      showLogMessage("I","","ERROR:檔案資料有錯, 資料不寫入");

      setValue("noti.notify_head"      , "媒體檔轉入資料有誤(只記錄前100筆)");
      //setValue("noti.notify_name"      , "媒體檔名:"+getValue("file_name"));
      setValue("noti.notify_name"      , getValue("file_name"));
      setValue("noti.notify_desc1"     , "程式 "+javaProgram+" 轉 "+ getValue("file_name") +" 有"+errorCnt+" 筆錯誤");
      setValue("noti.notify_desc2"     , "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤");
      insertEcsNotifyLog();
      commitDataBase();
      setValue("proc_desc"  , "ERROR:檔案資料有錯, 資料不寫入");
      return;
     }
  setValue("proc_desc"  , "+累計讀取["+(lineSeq)+"]筆, 錯誤["+errorCnt+"]");
  showLogMessage("I","","   "+ getValue("proc_desc"));
 }
// ************************************************************************
 int selectSmsEinvoCard() throws Exception
 {
  extendField = "invo.";
  selectSQL = "card_no ";
  daoTable  = "sms_einvo_card";
  whereStr  = "where enc_card_no = ?";
 
  setString(1 , getValue("enc_card_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int updateSmsEinvoCancel() throws Exception
 {
  updateSQL = "proc_flag     = 'Y', "
            + "proc_date     = ?, "
            + "apr_code      = ?, "
            + "apr_sec_flag  = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate"; 
  daoTable  = "sms_einvo_cancel";
  whereStr  = "WHERE card_no   = ? "
            + "AND   send_date = ? "
            + "AND   send_time = ? ";
  
  setString(1 , hBusiBusinessDate);
  setString(2 , getValue("secl.apr_code"));
  setString(3 , getValue("secl.apr_sec_flag"));
  setString(4 , javaProgram);
  setString(5 , getValue("invo.card_no"));
  setString(6 , getValue("secl.snd_date"));
  setString(7 , getValue("secl.snd_time"));

  int cnt = updateTable();
  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void updateEcsFtpLog() throws Exception
 {
  dateTime();
  updateSQL = "proc_code  = ?, "
            + "trans_desc = trans_desc||?, "
            + "proc_desc  = ?, "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "ecs_ftp_log";
  whereStr  = "WHERE rowid = ?";

  setString(1 , getValue("proc_code"));
  setString(2 , getValue("proc_desc"));
  setString(3 , getValue("proc_desc"));
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE ecs_ftp_log error "+getValue("rowid")); 
      exitProgram(0); 
     }
  return;
 }
// ************************************************************************
 void updateSmsEinvoCancel1() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag     = 'N', "
            + "proc_date     = '', "
            + "apr_code      = '', "
            + "apr_sec_flag  = '', "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "sms_einvo_cancel";
  whereStr  = "WHERE  proc_date = ?";

  setString(1 , javaProgram);
  setString(2 , hBusiBusinessDate);

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE sms_einvo_cancel_1 error "+getValue("rowid")); 
      exitProgram(0); 
     }
//return;
 }
// ************************************************************************
 int insertEcsMediaErrlog() throws Exception
 {
  dateTime();
  extendField = "elog.";
  setValue("elog.crt_date"           , sysDate);
  setValue("elog.crt_time"           , sysTime);
  setValue("elog.file_name"          , getValue("file_name"));
  setValue("elog.unit_code"          , comr.getObjectOwner("3",javaProgram));
  setValueInt("elog.line_seq"        , lineSeq);
  setValue("elog.trans_seqno"        , getValue("trans_seqno"));
  setValue("elog.program_code"       , javaProgram);
  setValue("elog.mod_time"           , sysDate+sysTime);
  setValue("elog.mod_pgm"            , javaProgram);

  daoTable  = "ecs_media_errlog";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertEcsNotifyLog() throws Exception
 {
  dateTime();
  extendField = "noti.";
  setValue("noti.crt_date"           , sysDate);
  setValue("noti.crt_time"           , sysTime);
  setValue("noti.unit_code"          , comr.getObjectOwner("3",javaProgram));
  setValue("noti.obj_type"           , "3");
  setValue("noti.trans_seqno"        , getValue("trans_seqno"));
  setValue("noti.mod_time"           , sysDate+sysTime);
  setValue("noti.mod_pgm"            , javaProgram);

  daoTable  = "ecs_notify_log";

  insertTable();

  return(0);
 }
// ************************************************************************
 void renameFile(String renameFileName) throws Exception
 {
  String tmpstr1 = getValue("local_dir")+"/"+renameFileName;
         tmpstr1 = Normalizer.normalize(tmpstr1, Normalizer.Form.NFD);
  File oldName   = new File(tmpstr1);
  String tmpstr2 = getValue("local_dir")+"/backup/"+renameFileName+"."+sysDate;
         tmpstr2 = Normalizer.normalize(tmpstr2, Normalizer.Form.NFD);
  File newName = new File(tmpstr2);

  if (!oldName.renameTo(newName))
     {
      showLogMessage("I","","ERROR : 檔案["+tmpstr1+"] ==> ["+tmpstr2+"]更名失敗!");
      return;
     }
  showLogMessage("I","","檔案 ["+renameFileName+"] 已移至 ["+tmpstr2+"]");
 } 
// ************************************************************************

}  // End of class FetchSample


