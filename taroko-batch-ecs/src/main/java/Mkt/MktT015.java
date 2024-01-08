/*****************************************************************************
*                                                                            *
* 6891                         MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/05/10  V1.00.02  Allen Ho   New                                        *
* 111/12/06  V1.00.03  Zuwei      sync from mega                             *
* 112/10/16  V1.00.04  Kirin      移除MktT030                                 *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktT015 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-媒體分派處理程式 111/12/06 V1.00.03";
 CommFunction comm = new CommFunction();
 CommFTP commFTP = null;
 CommRoutine comr = null;

 String businessDate = "";

 String readData="";
 long    totalCnt=0,errorCnt=0;
 int fi;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT015 proc = new MktT015();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
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
        return(0);
/* 20231015 update
   if (args.length != 0)
      {
       showLogMessage("I","","請輸入參數:");
       return(1);
      } 
*/

   if ( !connectDataBase() )
      return(1);

   commFTP = new CommFTP(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());
// 20231015 add   
   if ( args.length == 1 )
       if (args[0].length()==8)  businessDate = args[0];    
   else
	//  20231015 end	   
   selectPtrBusinday();
   
   showLogMessage("I","","輸入營業日 : ["+businessDate+"]");
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   showLogMessage("I","","依傳輸記錄執行.....");
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

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
 void selectEcsFtpLog() throws Exception
 {
  selectSQL  = "trans_seqno,"
             + "file_date";
  daoTable   = "ecs_ftp_log";
//  whereStr   = "WHERE system_id  = 'THSR_UP' "
  whereStr   = "WHERE system_id  = 'COUP_FTP' "
             + "AND   proc_code  = 'N' "     
             + "AND   file_name like 'coup%' "     
             + "order by file_date "     
             ;

  int recCnt = selectTable();

  if (recCnt==0) 
     {
      showLogMessage("I","","本日 ["+ businessDate +"] 無傳輸記錄可執行.");
      return;
     }

  int[] arrInt = {1};
  String[] returnMsg = {""};

  for ( int inti=0; inti<recCnt; inti++ )
    {
     setValue("eflg.trans_seqno" , getValue("trans_seqno" , inti));
     showLogMessage("I","","檔案日期 ["+getValue("file_date",inti)  +"]");

     // *****************************************************************
     showLogMessage("I","","啟動 MktT020 執行後續動作 .....");
     showLogMessage("I","","===============================");

     String[] tranArgs = new String[2];
     tranArgs[0] = getValue("trans_seqno",inti);
     tranArgs[1] = getValue("file_date",inti);
     try {
          MktT020 mktT020 = new MktT020();
          int rtn = mktT020.mainProcess(tranArgs);
          if (rtn < 0)   
             {
              setValue("notify_head"   , "程式 MktT020("+ getValue("file_date",inti) +") 執行錯誤");
              setValue("notify_name"   , "程式名稱:MktT020 高鐵車廂升等-檢核授權處理程式 ");
              setValue("notify_desc1"  , "程式啟動時收到失敗訊息");
              setValue("notify_desc2"  ,  returnMsg[0]);
              insertEcsNotifyLog();
              commitDataBase();
              exitProgram(1);
             }
          showLogMessage("I","","MktT020 執行結束");
          showLogMessage("I","","===============================");
         } catch (Exception ex)
              {
               showLogMessage("I","","無法執行 MktT020 ERROR!");
              }
     // *****************************************************************
/* 20231016 移除 退紅利點數     
     showLogMessage("I","","啟動 MktT030 執行後續動作 .....");
     showLogMessage("I","","===============================");
     try {
          MktT030 mktT030 = new MktT030();
          int rtn = mktT030.mainProcess(tranArgs);
          if (rtn < 0)   
             {
              setValue("notify_head"   , "程式 MktT030 執行錯誤");
              setValue("notify_name"   , "程式名稱:MktT030 高鐵車廂升等-退票退紅利點數處理程式 ");
              setValue("notify_desc1"  , "程式啟動時收到失敗訊息");
              setValue("notify_desc2"  ,  returnMsg[0]);
              insertEcsNotifyLog();
              commitDataBase();
              exitProgram(1);
             }
          showLogMessage("I","","MktT030 執行結束");
          showLogMessage("I","","===============================");
         } catch (Exception ex)
              {
               showLogMessage("I","","無法執行 MktT030 ERROR!");
              }
*/              
     // *****************************************************************
     showLogMessage("I","","啟動 MktT040 執行後續動作 .....");
     showLogMessage("I","","===============================");
     try {
          MktT040 mktT040 = new MktT040();
          int rtn = mktT040.mainProcess(tranArgs);
          if (rtn < 0)   
             {
              setValue("notify_head"   , "程式 MktT040 執行錯誤");
              setValue("notify_name"   , "程式名稱:MktT040 高鐵車廂升等-退票退還費用處理程式 "); 
              setValue("notify_desc1"  , "程式啟動時收到失敗訊息");
              setValue("notify_desc2"  ,  returnMsg[0]);
              insertEcsNotifyLog();
              commitDataBase();
              exitProgram(1);
             }
          showLogMessage("I","","MktT040 執行結束");
          showLogMessage("I","","===============================");
         } catch (Exception ex)
              {
               showLogMessage("I","","無法執行 MktT040 ERROR!");
              }
     // *****************************************************************
     showLogMessage("I","","啟動 MktT050 執行後續動作 .....");
     showLogMessage("I","","===============================");
     try {
          MktT050 mktT050 = new MktT050();
          int rtn = mktT050.mainProcess(tranArgs);
          if (rtn < 0)   
             {
              setValue("notify_head"   , "程式 MktT050 執行錯誤");
              setValue("notify_name"   , "程式名稱:MktT050 鐵車廂升等-紅利點數扣點或加檔款處理程式"); 
              setValue("notify_desc1"  , "程式啟動時收到失敗訊息");
              setValue("notify_desc2"  ,  returnMsg[0]);
              insertEcsNotifyLog();
              commitDataBase();
              exitProgram(1);
             }
          showLogMessage("I","","MktT050 執行結束");
          showLogMessage("I","","===============================");
         } catch (Exception ex)
              {
               showLogMessage("I","","無法執行 MktT050 ERROR!");
              }
     // *****************************************************************
     updateEcsFtpLog(getValue("trans_seqno",inti));
     showLogMessage("I","","處理日期 ["+ getValue("file_date",inti) +"] 結束("+inti+")");
     commitDataBase();
    }
 }
// ************************************************************************
 int insertEcsNotifyLog() throws Exception
 {
  dateTime();
  setValue("crt_date"           , sysDate);
  setValue("crt_time"           , sysTime);
  setValue("unit_code"          , comr.getObjectOwner("1",javaProgram));
  setValue("obj_type"           , "1");
  setValue("notify_head"        , getValue("notify_head"));
  setValue("notify_name"        , getValue("notify_name")); 
  setValue("notify_desc1"       , getValue("notify_desc1")); 
  setValue("notify_desc2"       , getValue("notify_desc2")); 
  setValue("trans_seqno"        , getValue("eflg.trans_seqno"));
  setValue("mod_time"           , sysDate+sysTime);
  setValue("mod_pgm"            , javaProgram);
  daoTable  = "ecs_notify_log";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateEcsFtpLog(String transSeqno) throws Exception
 {
  dateTime();
  updateSQL = "proc_code  = 'Y', "
            + "proc_desc  = ?, "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "ecs_ftp_log";
  whereStr  = "WHERE trans_seqno = ?";

  setString(1 , "處理完成");
  setString(2 , javaProgram);
  setString(3 , transSeqno);

  updateTable();

  if ( notFound.equals("Y") )
      showLogMessage("I","","UPDATE ecs_ftp_log error "+transSeqno); 

  return;
 }
// ************************************************************************


}  // End of class FetchSample

