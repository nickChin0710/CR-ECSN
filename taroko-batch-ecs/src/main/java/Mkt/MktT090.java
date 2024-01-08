/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/02/23  V1.00.07  Allen Ho   new                                        *
* 111/12/07  V1.00.08  Zuwei      sync from mega                             *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;                                      
import java.nio.*;
@SuppressWarnings("unchecked")
public class MktT090 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-每月紅利扣點卡類明細處理程式 111/12/07 V1.00.08";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";

 int     totalCnt=0;
 int    cnt1=0;
 int[]  procInt = new int[20];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT090 proc = new MktT090();
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
      {
       showLogMessage("I","","本程式已有另依程序啟動中, 不執行..");
       return(0);
      }

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 )
      { businessDate = args[0]; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (!businessDate.substring(6,8).equals("01"))
      {
       showLogMessage("I","","本程式只在每月1日執行,本日為"+businessDate+"日..");
       return(0);
      }
          
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   deleteMktThsrUpdeduct();
   selectMktThsrUptxn();
   showLogMessage("I","","處理筆數           ["+procInt[0]+"] 筆"); 
   showLogMessage("I","","    非紅利扣點     ["+procInt[1]+"] 筆");
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
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

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
 void  selectMktThsrUptxn() throws Exception
 {
  selectSQL = "max(p_seqno) as p_seqno,"
            + "max(acct_type) as acct_type,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(group_code) as group_code,"
            + "max(card_type) as card_type,"
            + "max(major_card_no) as major_card_no,"
            + "min(card_mode) as card_mode,"
            + "card_no";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE file_date like ? "
            + "AND   trans_type = 'P' "
            + "AND   pay_type in ('1','2','3') "
            + "group by card_no "
            ;

  setString(1 , comm.lastMonth(businessDate)+"%");              

  openCursor();

  while( fetchTable() ) 
   {
    procInt[10]++; 
    selectMktThsrUpmode();
    if (!getValue("mode.ticket_pnt_cond").equals("Y"))
       {
        procInt[1]++;
        continue;
       }

    selectMktThsrUptxn1();
    insertMktThsrUpdeduct();
    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 int insertMktThsrUpdeduct() throws Exception
 {
  extendField = "duct.";
                   
  setValue("duct.proc_month"         , comm.lastMonth(businessDate)); 
  setValue("duct.card_no"            , getValue("card_no"));
  setValue("duct.major_card_no"      , getValue("major_card_no"));
  setValue("duct.ticket_pnt_cnt"     , getValue("mode.ticket_pnt_cnt"));
  setValue("duct.acct_type"          , getValue("acct_type"));
  setValue("duct.p_seqno"            , getValue("p_seqno"));
  setValue("duct.id_p_seqno"         , getValue("id_p_seqno"));
  setValue("duct.group_code"         , getValue("group_code"));
  setValue("duct.card_type"          , getValue("card_type"));
  setValue("duct.card_mode"          , getValue("card_mode"));
  setValue("duct.card_cnt"           , getValue("mtun.card_cnt"));
  setValue("duct.deduct_bp_cnt"      , getValue("mtun.deduct_bp_cnt"));
  setValue("duct.deduct_bp"          , getValue("mtun.deduct_bp"));
  setValue("duct.deduct_amt_cnt"     , getValue("mtun.deduct_amt_cnt"));
  setValue("duct.deduct_amt"         , getValue("mtun.deduct_amt"));
  setValue("duct.sub_cnt"            , getValue("mtun.sub_cnt"));
  setValue("duct.mod_time"           , sysDate+sysTime);
  setValue("duct.mod_pgm"            , javaProgram);
                     
  daoTable  = "mkt_thsr_updeduct";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectMktThsrUpmode() throws Exception
 {
  extendField = "mode.";
  selectSQL = "ticket_pnt_cond,"
            + "ticket_pnt_cnt";
  daoTable  = "mkt_thsr_upmode";
  whereStr  = "WHERE card_mode = ? "
            ;

  setString(1 , getValue("card_mode"));

  selectTable();

  return(0);
 }
// ************************************************************************
 int selectMktThsrUptxn1() throws Exception 
 {
  extendField = "mtun.";
  selectSQL = "count(*) as card_cnt,"
            + "sum(decode(pay_type,'1',decode(deduct_bp+deduct_bp_tax,0,0,1),0)) as deduct_bp_cnt,"
            + "sum(decode(pay_type,'1',deduct_bp+deduct_bp_tax,0)) as deduct_bp,"
            + "sum(decode(pay_type,'2',decode(deduct_amt,0,0,1),0)) as deduct_amt_cnt,"
            + "sum(decode(pay_type,'2',deduct_amt,0)) as deduct_amt,"
            + "sum(decode(pay_type,'3',1,0)) as sub_cnt";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE file_date like ? "
            + "AND   trans_type = 'P' "
            + "AND   pay_type in ('1','2','3') "
            + "and   card_no  = ?"
            ;

  setString(1 , comm.lastMonth(businessDate)+"%");              
  setString(2 , getValue("card_no"));

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int deleteMktThsrUpdeduct() throws Exception
 {
  daoTable  = "mkt_thsr_updeduct";
  whereStr  = "WHERE proc_month  = ? ";

  setString(1 , comm.lastMonth(businessDate)); 

  int recCnt = deleteTable();

  showLogMessage("I","","delete mkt_thsr_updeduct  筆數 ["+ recCnt +"]");

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample
