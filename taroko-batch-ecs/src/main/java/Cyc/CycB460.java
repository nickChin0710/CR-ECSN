/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/06/29  V1.00.00  Allen Ho   cyc_a452                                   *
* 109-12-21  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycB460 extends AccessDAO
{
 private  String progname = "免年費-依據凍結碼處理程式  109/12/21 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";
 String hWdayThisAcctMonth = "";
 String hCfeeReasonCode     = ""; 

 long    totalCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycB460 proc = new CycB460();
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
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) exitProgram(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日次一日, 不需執行");
       return(0);
      }

   showLogMessage("I","","this_acct_month["+hWdayThisAcctMonth+"]");
   showLogMessage("I","","處理月份: ["+ comm.nextMonth(hWdayThisAcctMonth,-12)+"]["+comm.nextMonth(hWdayThisAcctMonth,-1)+"]");
   showLogMessage("I","","=========================================");

   loadPtrSysIdtab();
   selectCycAfee();

   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"] 筆");

   finalProcess();
   return 0;
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
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

  setString(1,comm.lastDate(hBusiBusinessDate));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  hWdayStmtCycle      =  getValue("STMT_CYCLE");
  hWdayThisAcctMonth =  getValue("this_acct_month");

  return(0);
 }
// ************************************************************************
 public void  selectCycAfee() throws Exception
 {
  selectSQL = "a.block_reason1,"
            + "a.block_reason2,"
            + "a.block_reason3,"
            + "a.block_reason4,"
            + "a.block_reason5,"
            + "b.rowid as rowid";
  daoTable  = "cca_card_acct a,cyc_afee b";
  whereStr  = "where a.p_seqno    = b.p_seqno "
            + "and   a.debit_flag = 'N' "
            + "and   (b.stmt_cycle = ? "
            + " or   b.old_stmt_cycle = ?) "
            + "and   b.rcv_annual_fee >  0 "
            + "and   b.maintain_code != 'Y' "
            ;

  setString(1 , hWdayStmtCycle);
  setString(2 , hWdayStmtCycle);

  openCursor();

  int cnt1=0;
  totalCnt=0;
  while( fetchTable() ) 
   { 

    totalCnt++;

    if (getValue("block_reason1").length()>0)
       {
        setValue("data.wf_id",getValue("block_reason1"));
         cnt1 = getLoadData("data.wf_id");
         if (cnt1>0) 
            {
             updateCycAfee();
             continue;
            }
       }
    if (getValue("block_reason2").length()>0)
       {
        setValue("data.wf_id",getValue("block_reason2"));
         cnt1 = getLoadData("data.wf_id");
         if (cnt1>0) 
            {
             updateCycAfee();
             continue;
            }
       }
    if (getValue("block_reason3").length()>0)
       {
        setValue("data.wf_id",getValue("block_reason3"));
         cnt1 = getLoadData("data.wf_id");
         if (cnt1>0) 
            {
             updateCycAfee();
             continue;
            }
       }
    if (getValue("block_reason4").length()>0)
       {
        setValue("data.wf_id",getValue("block_reason4"));
         cnt1 = getLoadData("data.wf_id");
         if (cnt1>0) 
            {
             updateCycAfee();
             continue;
            }
       }
    if (getValue("block_reason5").length()>0)
       {
        setValue("data.wf_id",getValue("block_reason5"));
         cnt1 = getLoadData("data.wf_id");
         if (cnt1>0) 
            {
             updateCycAfee();
             continue;
            }
       }
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 public void updateCycAfee() throws Exception
 {
  dateTime();
  updateSQL = "rcv_annual_fee = 0,"
            + "reason_code    = 'I1',"
            + "mod_pgm        = ?, "
            + "mod_time       = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "cyc_afee";
  whereStr  = "WHERE  rowid   = ? ";

  setString(1 , javaProgram);
  setString(2 , sysDate+sysTime);
  setRowId(3  , getValue("rowid"));

  updateTable();
  if (!notFound.equals("Y") ) updateCnt++;

  return;
 }
// ************************************************************************
 void  loadPtrSysIdtab() throws Exception
 {
  extendField = "data.";
  selectSQL = "wf_id";
  daoTable  = "ptr_sys_idtab";
  whereStr  = "WHERE wf_type = 'CYCM0190' ";

  int  n = loadTable();
  setLoadData("data.wf_id");

  showLogMessage("I","","Load ptr_sys_idtab : ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample
