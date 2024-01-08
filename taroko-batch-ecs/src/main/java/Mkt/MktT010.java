/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*    DATE   Version   AUTHOR              DESCRIPTION                         *
*  -------- --------  -----------  ------------------------------------------ *
* 108/11/05 V1.00.00  Allen Ho     mkt_t010                                   *
* 109-12-11 V1.00.01  tanwei       updated for project coding standard        *
* 111/12/06 V1.00.02  Zuwei        sync from mega                             *
* 112/10/06 V1.01.01  Lai          add backup                                 *
* 112/11/13 V1.01.01  Kirin        fix check dataHead filedate                *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;
import java.text.Normalizer;

@SuppressWarnings("unchecked")
public class MktT010 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-每日交易明細媒體檔匯入處理  112/10/06 V1.01.01";
 CommFunction comm = new CommFunction();
 CommRoutine  comr = null;
 CommCrd      comc = new CommCrd();

 String businessDate = "";
 int    lineSeq      = 0;

 String readData="";                       
 long   totalCnt=0,errorCnt=0;
 int    fileDataCnt=0;
 int    fi;
 double fileTotalAmt =0,fileSumAmt ;
 int    fileEndFlag,errFlag;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT010 proc = new MktT010();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);
  
   if ( !connectDataBase() ) 
       return(1);

   if (args.length > 1)
   {
    showLogMessage("I","","請輸入參數:");
    showLogMessage("I","","PARM 1 : [sysdate]");
    return(1);
   }
   businessDate = "";
   if ( args.length == 1 )
    if (args[0].length()==8)  businessDate = args[0];

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   selectEcsFtpLog();
   showLogMessage("I","","===============================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
 {
  daoTable   = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
 void selectEcsFtpLog() throws Exception
 {
  extendField = "eftp.";
  selectSQL  = "file_name, "
             + "local_dir, "
             + "file_date, "
             + "trans_seqno, "
             + "rowid as rowid";
  daoTable   = "ecs_ftp_log";
//whereStr   = "WHERE system_id  = 'THSR_UP' "
  whereStr   = "WHERE system_id  = 'COUP_FTP' "
             + "AND   proc_code  = '0' "     
             + "AND   file_name like 'coup%' "
             + "ORDER BY file_date,file_name,crt_date,crt_time " 
             ;
  
  int recCnt = selectTable();

  if (recCnt==0) 
     {
      showLogMessage("I","","本日 ["+ businessDate +"] 無傳輸記錄可執行.");
      return;
     }

  for (int inti=0;inti<recCnt;inti++)
    {
     setValue("file_name"   , getValue("eftp.file_name",inti)); 
     setValue("local_dir"   , getValue("eftp.local_dir",inti)); 
     setValue("file_date"   , getValue("eftp.file_date",inti)); 
     setValue("trans_seqno" , getValue("eftp.trans_seqno",inti)); 
     setValue("rowid"       , getValue("eftp.rowid",inti));
      
     showLogMessage("I","","處理檔案["+getValue("file_name")+"].....");

     if (selectMktThsrUptxn()!=0)
        {
         setValue("proc_desc" , "檔案名稱已轉入, 不可重複轉入");
         showLogMessage("I","",getValue("proc_desc"));
         setValue("proc_code" , "R");
         renameFile(getValue("file_name"));
         updateEcsFtpLog();
         commitDataBase();
         continue;
        }

     openFile();

     setValue("proc_code" , "N");
     if (errorCnt>0) setValue("proc_code" , "X");
     updateEcsFtpLog();
     renameFile(getValue("file_name"));
     commitDataBase();
    }
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
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 void openFile() throws Exception
 {
  String checkHome = System.getenv("PROJ_HOME");
  String fileDate = getValue("file_name").substring("coup".length(), "coup".length() + 8);
  String fileName = checkHome + "/media/mkt/"+getValue("file_name");
  fi = openInputText(fileName,"MS950");
  if (fi == -1 ) 
     {
       setValue("noti.notify_head"      , "媒體檔無開啟權限");
       setValue("noti.notify_name"      , "媒體檔名:"+ getValue("file_name"));
       setValue("noti.notify_desc1"     , getValue("file_name")+" 權限不足, 無法開啟");
       setValue("noti.notify_desc2"     , "");
       insertEcsNotifyLog();
       renameFile(getValue("file_name"));
       showLogMessage("I","", getValue("noti.notify_head")+" 不處理...");
       return;
     }
  setValue("file_desc" , "檔案處理中");
  commitDataBase();

  lineSeq=0;
  errorCnt=0;
  int    fileCnt =0;
  String dataHead="";
  String dataText="";

  fileEndFlag  = 0;
  totalCnt=0;
  while ( true )
   {
    lineSeq++;
    readData = readTextFile(fi).replace("^\n","").replace("^\r","").replace("\t", "").trim();
    if ( endFile[fi].equals("Y") ) break;
    if ( readData.length()<2 ) continue;


    dataHead    = comm.getStr(readData, 1 ,"|");
    if (dataHead.equals("D"))
       { 
        setValue("iupx.trans_type"               , comm.getStr(readData, 2 ,"|"));
        setValue("iupx.trans_date"               , comm.getStr(readData, 3 ,"|"));
        setValue("iupx.trans_time"               , comm.getStr(readData, 4 ,"|"));
        setValue("iupx.serial_no"                , comm.getStr(readData, 5 ,"|"));
        setValue("iupx.org_trans_date"           , comm.getStr(readData, 6 ,"|"));
        setValue("iupx.pay_cardid"               , comm.getStr(readData, 7 ,"|"));
        setValue("iupx.authentication_code"      , comm.getStr(readData, 8 ,"|"));
        setValue("iupx.station_id"               , comm.getStr(readData, 9 ,"|"));
        setValue("iupx.pnr"                      , comm.getStr(readData, 10,"|"));
        setValue("iupx.ticket_id"                , comm.getStr(readData, 11,"|"));
        setValue("iupx.trans_amount"             , comm.getStr(readData, 12,"|"));
        setValue("iupx.train_no"                 , comm.getStr(readData, 13,"|"));
        setValue("iupx.departure_station_id"     , comm.getStr(readData, 14,"|"));
        setValue("iupx.arrival_station_id"       , comm.getStr(readData, 15,"|"));
        setValue("iupx.seat_no"                  , comm.getStr(readData, 16,"|"));
        setValue("iupx.depart_date"              , comm.getStr(readData, 17,"|"));
        setValue("iupx.org_serial_no"            , comm.getStr(readData, 18,"|"));
        setValue("iupx.issue_station_id"         , comm.getStr(readData, 19,"|"));
       } 

    if (!Arrays.asList("H","D","T","S").contains(dataHead))
       {
        setValue("elog.main_desc"        , "資料格式錯誤");
        setValue("elog.error_seq"        , "1");
        setValue("elog.column_seq"       , "1");
        setValue("elog.column_data"      , "屬性資料");
        setValue("elog.error_desc"       , "每筆資料第一碼需為 H,D,T,S 內容");
        setValue("elog.column_desc"      , "資料屬性錯誤");
        insertEcsMediaErrlog();
        errFlag=1;
       }
    if ((lineSeq==1)&&(!dataHead.equals("H")))
       {
        setValue("elog.main_desc"        , "資料格式錯誤");
        setValue("elog.error_seq"        , "1");
        setValue("elog.column_seq"       , "1");
        setValue("elog.column_data"      , "屬性資料");
        setValue("elog.error_desc"       , "媒體首筆資料定義須為[H]");
        setValue("elog.column_desc"      , "媒體無首筆資料定義");
        insertEcsMediaErrlog();
        errFlag=1;
       }
    // 檢查檔名日期與檔案內第1列第3碼開始取8碼的日期要一樣
//  if ("D".equals(dataHead) && !fileDate.equals(CommFunction.getStr(readData, 3 ,"|"))) {
    if ((lineSeq==1)&&(!dataHead.equals("H"))&& !fileDate.equals(CommFunction.getStr(readData, 3 ,"|"))) {    	
     setValue("elog.main_desc"        , "資料格式錯誤");
     setValue("elog.error_seq"        , "1");
     setValue("elog.column_seq"       , "1");
     setValue("elog.column_data"      , "屬性資料");
     setValue("elog.error_desc"       , "資料異常:檔名日期與檔案日期不一致");
     setValue("elog.column_desc"      , "媒體日期資料異常");
     insertEcsMediaErrlog();
     errFlag=1;
    }
    if (lineSeq==1)
       {
        setValue("iupx.file_date" ,comm.getStr(readData, 2 ,"|")); 
        continue;
       }
    if (dataHead.equals("T"))
       {
        dataText = comm.getStr(readData, 2 ,"|");
        fileCnt = Integer.valueOf(dataText);
        continue;
       }

    if (dataHead.equals("S"))
       {
        // 前一天筆數及金額 , 不使用

        fileEndFlag = 1;
        continue;
       }

    if (dataHead.equals("D")) 
       {
        totalCnt++;
        if (insertMktThsrUptxn()!=0)
           {
            setValue("elog.main_desc"        , "資料重複錯誤");
            setValue("elog.error_seq"        , "2");
            setValueInt("elog.column_seq"    , lineSeq);
            setValue("elog.column_data"      , "重複資料");
            setValue("elog.error_desc"       , "資料重複");
            setValue("elog.column_desc"      , "資料重複錯誤");
            insertEcsMediaErrlog();
            errFlag=1;
           }
        if (errFlag==1) errorCnt++; 
       }   
   }
  closeInputText(fi);

  if (fileEndFlag!=1)
     {
      showLogMessage("I","","檔案無尾筆資料!" );
      rollbackDataBase();
      setValue("noti.notify_head"      , "媒體檔無尾筆資料");
      setValue("noti.notify_name"      , "媒體檔名:"+ getValue("file_name"));
      setValue("noti.notify_desc1"     , getValue("file_name")+" 檔案無尾筆資料!");
      setValue("noti.notify_desc2"     , "");
      insertEcsNotifyLog();
      commitDataBase();
      return;
     }
  if (totalCnt!=fileCnt)
     {
      showLogMessage("I","","媒體明細筆數累計["+totalCnt+"]與尾筆總筆數["+fileCnt+"]不符!" );
      rollbackDataBase();
      setValue("noti.notify_head"      , "媒體檔筆數不符");
      setValue("noti.notify_name"      , "媒體檔名:"+ getValue("file_name"));
      setValue("noti.notify_desc1"     , getValue("file_name")+" 明細筆數累計與尾筆總筆數不符!");
      setValue("noti.notify_desc2"     , "");
      insertEcsNotifyLog();
      commitDataBase();
      return;
     }


  setValue("proc_desc"  , "+累計讀取["+totalCnt+"]筆, 錯誤["+errorCnt+"]");
  showLogMessage("I","", getValue("proc_desc"));
 } 
// ************************************************************************
 void renameFile(String renameFileName) throws Exception
 {
  String tmpstr1 = getValue("local_dir")+"/"+renameFileName;
         tmpstr1 = Normalizer.normalize(tmpstr1, Normalizer.Form.NFD);
  File oldName   = new File(tmpstr1);
  String tmpstr2 = getValue("local_dir")+"/"+renameFileName+"."+sysDate;
         tmpstr2 = Normalizer.normalize(tmpstr2, Normalizer.Form.NFD);
  File newName = new File(tmpstr2);

  if (!oldName.renameTo(newName))
     {
      showLogMessage("I","","ERROR : 檔案["+tmpstr1+"] ==> ["+tmpstr2+"]更名失敗!");
      return;
     }
  showLogMessage("I","","檔案 ["+renameFileName+"] 已移至 ["+tmpstr2+"]");

  backFile(renameFileName+"."+sysDate);
 } 
/***************************************************************************/
void backFile(String filename) throws Exception {
   String tmpstr1 = String.format("%s/media/mkt/%s", comc.getECSHOME(), filename);
   String tmpstr2 = String.format("%s/media/mkt/backup/%s",comc.getECSHOME(),filename);

   if (comc.fileRename(tmpstr1, tmpstr2) == false) {
       showLogMessage("I", "", "ERROR : 檔案["+tmpstr1+" to "+tmpstr2+"]備份失敗!");
       return;
   }

   comc.fileDelete(tmpstr1);
   showLogMessage("I", "", "檔案 [" +tmpstr1 + "] 已移至 [" + tmpstr2 + "]");
}
// ************************************************************************
 int  insertEcsMediaErrlog() throws Exception
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
 int insertMktThsrUptxn() throws Exception
 {
  dateTime();
  extendField = "iupx.";
  setValue("iupx.trans_seqno"              , getValue("trans_seqno"));
  setValue("iupx.file_name"                , getValue("file_name"));
  setValue("iupx.proc_flag"                , "0");
  setValue("iupx.proc_date"                , "00000000");
  setValue("iupx.crt_date"                 , sysDate);
  setValue("iupx.crt_time"                 , sysTime);
  setValue("iupx.crt_user"                 , javaProgram);
  setValue("iupx.mod_time"                 , sysDate+sysTime);
  setValue("iupx.mod_user"                 , javaProgram);
  setValue("iupx.mod_pgm"                  , javaProgram);

  daoTable  = "mkt_thsr_uptxn";

  insertTable();

  if ( dupRecord.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktThsrUptxn() throws Exception
 {
  extendField = "supx.";
  daoTable   = "mkt_thsr_uptxn";
  whereStr  = "where file_name = ? "
            + "fetch first 1 rows only ";

  setString(1 , getValue("file_name"));

  selectTable();

  if ( notFound.equals("Y") ) return(0);

  return(1);
 }
// ************************************************************************ 

}  // End of class FetchSample

