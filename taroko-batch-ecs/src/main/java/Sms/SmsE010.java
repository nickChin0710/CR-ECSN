/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/07/31  V1.00.11  Allen Ho   initial                                    *
* 112/02/07  V1.00.12  Machao     sync from mega & updated for project coding standard 
* 112/04/27  V1.00.13  Sunny      mark test程式                                                              *
* 112/10/19  V1.00.14  Sunny      調整VD處理邏輯程式，增加判斷停卡日期不為空的判斷，避免出現SQLCODE=-20448的問題*
******************************************************************************/
package Sms;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class SmsE010 extends AccessDAO
{
 private final String  PROGNAME = "電子發票-卡號加密處理程式 112/10/19  V1.00.14";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 final Base64.Decoder decoder = Base64.getDecoder();
 final Base64.Encoder encoder = Base64.getEncoder();
 String hBusiBusinessDate = "";

 long   totalCnt;
 int    cnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  SmsE010 proc = new SmsE010();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
 
//  String Text = comm.sha256("3565538600088991");
//   showLogMessage("I","","Text["+ Text +"]");
//  byte[] textByte = comm.hexToByte(Text);
//   showLogMessage("I","","hex["+ Text +"]");
//   showLogMessage("I","","data["+encoder.encodeToString(textByte)+"]");
//   if (Text.length()!=0) return(0);

   dateTime();
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);
  
    if (comm.isAppActive(javaProgram)) 
       return(1);

   if ( args.length == 1 ) 
      { hBusiBusinessDate = args[0]; }

   if ( !connectDataBase() )
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

/*
   showLogMessage("I","","=========================================");
   showLogMessage("I","","清除 credit card 過時資料.....");
   select_crd_card_0();
   showLogMessage("I","","累計處理筆數 ["+totalCnt   +"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","清除 debit card 過時資料.....");
   select_dbc_card_0();
   showLogMessage("I","","累計處理筆數 ["+totalCnt   +"]");
*/
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 credit card .....");
   selectCrdCard();
   showLogMessage("I","","累計處理筆數 ["+totalCnt   +"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 debit card .....");
   selectDbcCard();
   showLogMessage("I","","累計處理筆數 ["+totalCnt   +"]");
   showLogMessage("I","","=========================================");
  
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
 void  selectCrdCard0() throws Exception
 {
  selectSQL = "b.card_no";
  daoTable  = "crd_card a,sms_einvo_card b";
  whereStr  = "where a.card_no  = b.card_no "
            + "and   a.current_code != '0' "
            + "and   ? >to_char(add_months( "
            + "         to_date(a.oppost_date,'yyyymmdd'),3),'yyyymmdd') "
            + "and   a.oppost_date != '' "
            ;

  setString(1 , hBusiBusinessDate);

  openCursor();

  totalCnt =0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    deleteSmsEinvoCard();

    if (totalCnt % 100000 ==0) commitDataBase(); 

    if (totalCnt % 500000 ==0) 
       {
        showLogMessage("I","","處理筆數 ["+totalCnt   +"] 超過每日限量");
        break;
      }
   }
  commitDataBase();   
  closeCursor();
 }
// ************************************************************************
 void  selectDbcCard0() throws Exception
 {
  selectSQL  = "b.card_no";
  daoTable   = "dbc_card a,sms_einvo_card b";
  whereStr   = "where a.card_no  = b.card_no "
             + "and   a.current_code != '0' "
             + "and   ? >to_char(add_months( "
             + "         to_date(a.oppost_date,'yyyymmdd'),3),'yyyymmdd') "
            + "and   a.oppost_date != '' "
             ;

  setString(1 , hBusiBusinessDate);

  openCursor();

  totalCnt =0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    deleteSmsEinvoCard();

    if (totalCnt % 100000 ==0) commitDataBase(); 

    if (totalCnt % 500000 ==0) 
       {
        showLogMessage("I","","處理筆數 ["+totalCnt   +"] 超過每日限量");
        break;
      }
   }
  commitDataBase();   
  closeCursor();
 }
// ************************************************************************
 void  selectCrdCard() throws Exception
 {
  sqlCmd    = "SELECT card_no "
            + "FROM   crd_card "
            + "WHERE  (current_code='0' "
            + "OR      (current_code!='0' "
            + "  and    oppost_date !='' "
            + "  and    ? <= to_char(add_months( "
            + "              to_date(oppost_date,'yyyymmdd'),3),'yyyymmdd'))) "
            + "MINUS "
            + "SELECT  card_no "
            + "FROM    sms_einvo_card ";

  daoTable  = "crd_card";

  setString(1 , hBusiBusinessDate);

  openCursor();

  totalCnt =0;
  while( fetchTable() ) 
   { 
    totalCnt++;

     insertSmsEinvoCard();

    if (totalCnt % 50000 ==0) 
       showLogMessage("I","","處理筆數 ["+totalCnt   +"]");
   } 
  closeCursor();
 }
// ************************************************************************
 void  selectDbcCard() throws Exception
 {
  sqlCmd    = "SELECT card_no "
            + "FROM   dbc_card "
            + "WHERE  (current_code='0' "
            + "OR      (current_code!='0' "
            + "  and    oppost_date !='' "
            + "  and    ? <= to_char(add_months( "
            + "              to_date(oppost_date,'yyyymmdd'),3),'yyyymmdd'))) "
            + "MINUS "
            + "SELECT  card_no "
            + "FROM    sms_einvo_card ";
  daoTable  = "dbc_card";

  setString(1 , hBusiBusinessDate);

  openCursor();
  totalCnt =0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    insertSmsEinvoCard();

    if (totalCnt % 50000 ==0) 
       showLogMessage("I","","處理筆數 ["+totalCnt   +"]");
   } 
  closeCursor();
 }
// ************************************************************************
 void  insertSmsEinvoCard() throws Exception
 {
  dateTime();
  extendField = "esms.";

  String Text = comm.sha256(getValue("card_no"));
  byte[] textByte = comm.hexToByte(Text);
  setValue("esms.enc_card_no" , encoder.encodeToString(textByte));

  setValue("esms.card_no"      , getValue("card_no")); 
  setValue("esms.mod_time"     , sysDate+sysTime);
  setValue("esms.mod_pgm"      , javaProgram);

  daoTable = "sms_einvo_card";

  insertTable();

  if ( dupRecord.equals("Y") )
     { 
      showLogMessage("I","","insert_sms_einvo_card  error[dupRecord]");
      showLogMessage("I","","   card_no ["+getValue("esms.card_no")+"]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
 int deleteSmsEinvoCard() throws Exception
 {
  daoTable  = "sms_einvo_card";
  whereStr  = "WHERE card_no  = ? ";

  setString(1 , getValue("card_no"));

  deleteTable();

  return(0);
 }
// ************************************************************************



}  // End of class FetchSample

