/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*    DATE   Version   AUTHOR              DESCRIPTION                         *
*  -------- --------  ---------  ------------------------------------------   *
* 110/04/29 V1.00.13  Allen Ho   mkt_t110-1                                   *
* 111/12/08 V1.00.14  Zuwei Su   sync from mega                               *
* 112/09/26 V1.00.15  Zuwei Su   無法處理檔案，修改system_id和group_id值      *
* 112/09/27 V1.00.16  Zuwei Su   更名後,檔案要搬至media\mkt\backup            *
* 112/09/28 V1.00.17  Zuwei Su   更名後檔案不搬至media\mkt\backup             *
* 112/09/28 V1.00.18  Kirin      fix 檔案無法insert                           *
* 112/10/06 V1.01.01  Lai        add backup                                   *
*                                                                             *
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
public class MktT110 extends AccessDAO
{
 private final String PROGNAME = "高鐵標準車廂-每日交易明細媒體轉入處理   112/10/06 V1.01.01";
 CommFunction comm = new CommFunction();
 CommRoutine  comr = null;
 CommCrd      comc = new CommCrd();

 String hBusinessDate = "";
 String hTempSysdate       = "";
 int    lineSeq      = 0;

 String readData="";                       
 long   totalCnt=0,errorCnt=0;
 int    fileDataCnt=0;
 int    fi;
 double fileTotalAmt =0,fileSumAmt ;
 int    fileEndFlag,errFlag;;
 int[] begPos = {0};
 String filler="";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT110 proc = new MktT110();
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
  
   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [sysdate]");
       return(1);
      }
   hTempSysdate = "";
   if ( args.length == 1 )
       if (args[0].length()==8)  hTempSysdate = args[0]; 

   if ( !connectDataBase() ) 
       return(1);

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
 void  selectPtrBusinday() throws Exception
 {
  daoTable   = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (hBusinessDate.length()==0)
      hBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusinessDate+"]");
 }
// ************************************************************************ 
 void selectEcsFtpLog() throws Exception
 {
  extendField = "eflg.";
  selectSQL  = "file_name, "
             + "local_dir, "
             + "file_date, "
             + "trans_seqno, "
             + "rowid as rowid";
  daoTable   = "ecs_ftp_log";
  whereStr   = "WHERE system_id  = 'COUP_FTP' "
//  whereStr   = "WHERE system_id  = 'THSR_BILL' "
             + "AND   proc_code  = '0' "  
             + "AND   group_id = 'COUP' "
             + "AND   ftp_type != '' "
             + "AND   file_name like 'cosd%' "
             ;
  
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

  int recCnt = selectTable();

  if (recCnt==0) 
     {
      showLogMessage("I","","本日 ["+hBusinessDate+"] 無傳輸記錄可執行.");
      return;
     }

  for (int inti=0;inti<recCnt;inti++)
    {
     setValue("file_name"   , getValue("eflg.file_name",inti)); 
     setValue("local_dir"   , getValue("eflg.local_dir",inti)); 
     setValue("file_date"   , getValue("eflg.file_date",inti)); 
     setValue("trans_seqno" , getValue("eflg.trans_seqno",inti)); 
     setValue("rowid"       , getValue("eflg.rowid",inti));
      
     showLogMessage("I","","處理檔案 ["+ getValue("file_name")+"].....");

     if (selectMktThsrDisc()!=0)
        {
         setValue("proc_desc" , "檔案名稱已轉入, 不可重複轉入");
         showLogMessage("I","",getValue("proc_desc"));
         setValue("proc_code" , "R");
         renameFile(getValue("file_name"));
         updateEcsFtpLog();
         commitDataBase();
         return;
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
      exitProgram(0); 
     }
  return;
 }
// ************************************************************************
 void openFile() throws Exception
 {
  String checkHome = System.getenv("PROJ_HOME");
  String fileDate = getValue("file_name").substring("cosd".length(), "cosd".length() + 8);
  String fileName = checkHome + "/media/mkt/"+getValue("file_name");
  fi = openInputText(fileName, "MS950");
  if (fi == -1 ) 
     {
       setValue("noti.notify_head"      , "媒體檔無開啟權限");
       setValue("noti.notify_name"      , "媒體檔名:"+ getValue("file_name"));
       setValue("noti.notify_desc1"     , getValue("file_name")+" 權限不足, 無法開啟");
       setValue("noti.notify_desc2"     , "");
       insertEcsNotifyLog();
       renameFile(getValue("file_name"));
       showLogMessage("I","", getValue("noti.notify_head")+" 不處理...");

       setValue("proc_desc" , "媒體檔無開啟權限");
       setValue("proc_code" , "U");
       updateEcsFtpLog();
       commitDataBase();

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

    dataHead    = readData.substring(0,1);
    if (dataHead.equals("D"))
       {
        begPos[0] = 1;
        filler                                  = comm.structStr(readData,begPos,1).trim();
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.trans_type"              , comm.structStr(readData,begPos,1).trim()); 
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.trans_date"              , comm.structStr(readData,begPos,8).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.trans_time"              , comm.structStr(readData,begPos,6).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.serial_no"               , comm.structStr(readData,begPos,14).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.orig_trans_date"         , comm.structStr(readData,begPos,8).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.pay_cardid"              , comm.structStr(readData,begPos,16).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.authentication_code"     , comm.structStr(readData,begPos,6).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.station_id"              , comm.structStr(readData,begPos,2).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.pnr"                     , comm.structStr(readData,begPos,8).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.ticket_id"               , comm.structStr(readData,begPos,13).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.trans_amount"            , comm.structStr(readData,begPos,6).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.train_no"               , comm.structStr(readData,begPos,4).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.departure_station_id"    , comm.structStr(readData,begPos,2).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.arrival_station_id"      , comm.structStr(readData,begPos,2).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.seat_no"                , comm.structStr(readData,begPos,3).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.depart_date"            , comm.structStr(readData,begPos,8).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.car_no"                 , comm.structStr(readData,begPos,2).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.discount_value"          , comm.structStr(readData,begPos,6).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.total_ticket_number"     , comm.structStr(readData,begPos,6).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.total_amount"            , comm.structStr(readData,begPos,6).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.ticketing_station_id"    , comm.structStr(readData,begPos,2).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.orig_serial_no"          , comm.structStr(readData,begPos,14).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.plan_code"               , comm.structStr(readData,begPos,6).trim());
        filler                                  = comm.structStr(readData,begPos,1).trim();
       } 

    if (!Arrays.asList("H","D","T","S").contains(dataHead))
       {
        setValue("elog.main_desc"        , "資料格式錯誤");
        setValue("elog.error_seq"        , "1");
        setValue("elog.column_seq"       , "1");
        setValue("elog.column_data"      , "屬性資料");
        setValue("elog.error_desc"       , "每筆資料第一碼需為 H,D,T,S 內容");
        setValue("elog.column_desc"    , "資料屬性錯誤");
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
//  if (lineSeq == 2 && "D".equals(dataHead) && !fileDate.equals(CommFunction.getStr(readData, 3 ,"|"))) {
    if (lineSeq == 1 && "D".equals(dataHead) && !fileDate.equals(CommFunction.getStr(readData, 3 ,"|"))) {
     setValue("elog.main_desc"        , "日期錯誤");
     setValue("elog.error_seq"        , "1");
     setValue("elog.column_seq"       , "1");
     setValue("elog.column_data"      , "屬性資料");
     setValue("elog.error_desc"       , "媒體首筆日期與檔名不同");
     setValue("elog.column_desc"      , "媒體無首筆資料定義");
     insertEcsMediaErrlog();
     errFlag=1;
    }
    if (lineSeq==1)
       {
        begPos[0] = 1;
        filler                                  = comm.structStr(readData,begPos,1).trim();
        filler                                  = comm.structStr(readData,begPos,1).trim();
        setValue("mtdc.file_date"               , comm.structStr(readData,begPos,8).trim());
         
        continue;
       }
    if (dataHead.equals("T"))
       {
        begPos[0] = 1;
        filler                                  = comm.structStr(readData,begPos,1).trim();
        filler                                  = comm.structStr(readData,begPos,1).trim();
        dataText                                = comm.structStr(readData,begPos,6).trim();
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

        if (insertMktThsrDisc()!=0)
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
      deleteMktThsrDisc();
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
      deleteMktThsrDisc();
      setValue("noti.notify_head"      , "媒體檔筆數不符");
      setValue("noti.notify_name"      , "媒體檔名:"+ getValue("file_name"));
      setValue("noti.notify_desc1"     , getValue("file_name")+" 明細筆數累計與尾筆總筆數不符!");
      setValue("noti.notify_desc2"     , "");
      insertEcsNotifyLog();
      commitDataBase();
      return;
     }

  if (errorCnt!=0)
     {
      showLogMessage("I","","媒體檔轉入資料有誤!" );
      deleteMktThsrDisc();
      setValue("noti.notify_head"  , "媒體檔轉入資料有誤(只記錄前100筆)");
      setValue("noti.notify_name"  , "媒體檔名:"+ getValue("file_name"));
      setValue("noti.notify_desc1" , "程式 "+javaProgram+" 轉 "+ getValue("file_name") +" 有"+errorCnt+" 筆錯誤");
      setValue("noti.notify_desc2" , "請至 mktc0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤");
      insertEcsNotifyLog();
      commitDataBase();
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
  setValue("noti.crt_date"           , sysDate);
  setValue("noti.crt_time"           , sysTime);
  setValue("noti.unit_code"          , comr.getObjectOwner("3",javaProgram));
  setValue("noti.obj_type"           , "3");
  setValue("noti.trans_seqno"        , getValue("trans_seqno"));
  setValue("noti.mod_time"           , sysDate+sysTime);
  setValue("noti.mod_pgm"            , javaProgram);

  extendField = "noti.";
  daoTable  = "ecs_notify_log";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertMktThsrDisc() throws Exception
 {
  dateTime();
  setValue("mtdc.business_date"            , hBusinessDate);
  setValue("mtdc.file_time"                , sysTime);
  setValue("mtdc.file_name"                , getValue("file_name"));
  setValue("mtdc.proc_flag"                , "0");
  setValue("mtdc.proc_date"                , "00000000");
  setValue("mtdc.crt_date"                 , sysDate);
  setValue("mtdc.crt_time"                 , sysTime);
  setValue("mtdc.crt_user"                 , javaProgram);
  setValue("mtdc.mod_time"                 , sysDate+sysTime);
  setValue("mtdc.mod_user"                 , javaProgram);
  setValue("mtdc.mod_pgm"                  , javaProgram);

  extendField = "mtdc.";
  daoTable  = "mkt_thsr_disc";

  insertTable();

  if ( dupRecord.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktThsrDisc() throws Exception
 {
  extendField = "disc.";
  daoTable   = "mkt_thsr_disc";
  whereStr  = "where file_date = ? "
            + "fetch first 1 rows only "
            ;

  setString(1 , getValue("file_date"));

  selectTable();

  if ( notFound.equals("Y") ) return(0);

  return(1);
 }
// ************************************************************************ 
 int deleteMktThsrDisc() throws Exception
 {
  daoTable  = "mkt_thsr_disc";
  whereStr  = "where file_date     = ? "
            + "and   business_date = ? "
            ;

  setString(1 , getValue("mtdc.file_date"));
  setString(2 , hBusinessDate);

  int recCnt = deleteTable();

  showLogMessage("I","","刪除 mkt_thsr_disc 筆數 :["+ recCnt +"]");

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

