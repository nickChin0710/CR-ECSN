/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/12/13  V1.00.03  Allen Ho   cyc_A321                                   *
* 109-12-18   V1.00.04  tanwei      updated for project coding standard      *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA500 extends AccessDAO
{
 private  String progname = "關帳-acct_jrnl_bal不一致顯示處理程式 109/12/18 V1.00.04";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";

 long    totalCnt=0,currCnt=0;
 int debug =1;
 int paymentAmt = 0,insertCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA500 proc = new CycA500();
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
   showLogMessage("I","",javaProgram+" "+progname);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if (( args.length >= 1 )&&(args[0].length()==8))
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) 
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   totalCnt=0;
   selectCycAcmmCurr();
   showLogMessage("I","","total_count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");

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

  if (hBusiBusinessDate.length()==0)
      hBusiBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
 }
// ************************************************************************ 
  public int  selectPtrWorkday() throws Exception
 {
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  hWdayStmtCycle      =  getValue("STMT_CYCLE");

  return(0);
 }
// ************************************************************************
 void  selectCycAcmmCurr() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.acct_type,"
            + "c.curr_code,"
            + "b.acct_jrnl_bal,"
            + "c.this_ttl_amt";
  daoTable  = "act_acct_curr b,cyc_acmm_"+hWdayStmtCycle+" a,cyc_acmm_curr_"+hWdayStmtCycle+" c";
  whereStr  = "where b.p_seqno      = c.p_seqno "
            + "and   b.curr_code    = c.curr_code "
            + "and   c.p_seqno      = a.p_seqno "
            + "and   b.acct_jrnl_bal != c.this_ttl_amt "
            + "fetch first 10 rows only "
            ;

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    showLogMessage("I","","ERROR! ACCT_JRNL_BAL != THIS_TTL_AMT !! p_seqno["+getValue("p_seqno")+"]-["+getValue("curr_code")+"] ["+getValue("acct_type")+"]");
   } 
  closeCursor();
  return;
 }
// ************************************************************************

}  // End of class FetchSample

