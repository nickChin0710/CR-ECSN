/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 107/12/10  V1.00.06  Allen Ho   cyc_A315 PROD compare OK                   *
 * 109-12-18   V1.00.07  tanwei      updated for project coding standard      *
 ******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA460 extends AccessDAO
{
 private  String progname = "關帳-對帳單更改帳務主檔前置處理程式 109/12/18 V1.00.07";
 CommFunction comm = new CommFunction();

 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";
 String hWdayLlAcctMonth   = "";

 long    totalCnt=0,currCnt=0;
 boolean debug = false;
 int paymentAmt = 0,insertCnt=0,updateCnt=0;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA460 proc = new CycA460();
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

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
   {
    showLogMessage("I","","本日非關帳日, 不需執行");
    return(0);
   }

   if (debug)
   {
    showLogMessage("I","","=========================================");
    showLogMessage("I","","更新 cyc_acmm(act_acno) 開始...");
    selectCycAcmm0();
    showLogMessage("I","","更新 cyc_acmm(act_acno) 結束...");
   }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料 開始...");
   loadActAcno() ;
   loadActAcctHst() ;
   loadActAnalSub() ;

   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新 更新 cyc_acmm(act_anal_sub) 開始...");
   selectCycAcmm1();
   showLogMessage("I","","更新 更新 cyc_acmm(act_anal_sub) 結束.");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新 更新 cyc_acmm(act_acct_hst) 結束 結束...");
   selectCycAcmm2();
   showLogMessage("I","","更新 更新 cyc_acmm(act_acct_hst) 結束...");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","total_count=["+totalCnt+"] curr_cnt["+updateCnt+"]");

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
  hWdayLlAcctMonth   =  getValue("ll_acct_month");

  return(0);
 }
 // ************************************************************************
 void  selectCycAcmm0() throws Exception
 {
  selectSQL = "p_seqno,"
          + "rowid as rowid";
  daoTable  = "cyc_acmm_"+hWdayStmtCycle;

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() )
  {
   totalCnt++;

   setValue("acno.p_seqno",getValue("p_seqno"));
   int cnt1 = getLoadData("acno.p_seqno");
   if (cnt1==0) continue;

   updateCycAcmm0();
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 void  selectCycAcmm1() throws Exception
 {
  selectSQL = "p_seqno,"
          + "rowid as rowid";
  daoTable  = "cyc_acmm_"+hWdayStmtCycle;
  whereStr  = "WHERE  ttl_purch_cnt <= 0 "
          + "and    ttl_cash_cnt <= 0 "
          + "and    ttl_purchase <= 0 "
          + "and    ttl_cash <= 0 ";

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() )
  {
   totalCnt++;

   setValue("anal.p_seqno",getValue("p_seqno"));
   int cnt1 = getLoadData("anal.p_seqno");
   if (cnt1==0)
   {
    setValueInt("anal.no_consume_month" , 1);
    setValueInt("anal.stmt_new_amt"     , 0);
   }

   updateCycAcmm1();
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 void  selectCycAcmm2() throws Exception
 {
  selectSQL = "p_seqno,"
          + "rowid as rowid";
  daoTable  = "cyc_acmm_"+hWdayStmtCycle;
  whereStr  = "where ttl_amt <= 0 ";

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() )
  {
   totalCnt++;

   setValue("acht.p_seqno",getValue("p_seqno"));
   int cnt1 = getLoadData("acht.p_seqno");
   if (cnt1==0) continue;

   updateCycAcmm2();
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 int updateCycAcmm0() throws Exception
 {
  dateTime();
  updateSQL = "no_consume_month = 0,"
          + "stmt_new_amt     = 0,"
          + "last_credit_amt  = ? ";
  daoTable  = "cyc_acmm_"+hWdayStmtCycle;
  whereStr  = "WHERE  rowid = ? "
          + "and   (last_credit_amt  != ? "
          + " or    no_consume_month != 0 "
          + " or    stmt_new_amt     != 0) "
  ;

  setInt(1     , getValueInt("acno.last_credit_amt"));
  setRowId(2  , getValue("rowid"));
  setInt(3     , getValueInt("acno.last_credit_amt"));

  updateTable();

  return(0);
 }
 // ************************************************************************
 int updateCycAcmm1() throws Exception
 {
  dateTime();
  updateSQL = "no_consume_month = ?, "
          + "stmt_new_amt     = ? ";
  daoTable  = "cyc_acmm_"+hWdayStmtCycle;
  whereStr  = "WHERE  rowid = ? "
          + "and   (no_consume_month != ? "
          + " or    stmt_new_amt     != ?) "
  ;

  setInt(1 , getValueInt("anal.no_consume_month"));
  setInt(2 , getValueInt("anal.stmt_new_amt"));
  setRowId(3  , getValue("rowid"));
  setInt(4 , getValueInt("anal.no_consume_month"));
  setInt(5 , getValueInt("anal.stmt_new_amt"));

  updateTable();

  return(0);
 }
 // ************************************************************************
 int updateCycAcmm2() throws Exception
 {
  dateTime();
  updateSQL = "ll_stmt_new_amt = ? ";
  daoTable  = "cyc_acmm_"+hWdayStmtCycle;
  whereStr  = "WHERE  rowid = ? "
          + "and    ll_stmt_new_amt != ? "
  ;

  setInt(1 , getValueInt("acht.stmt_new_amt"));
  setRowId(2  , getValue("rowid"));
  setInt(3 , getValueInt("acht.stmt_new_amt"));

  updateTable();

  return(0);
 }
 // ************************************************************************
 public void  loadActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "p_seqno, "
          + "last_credit_amt";
  daoTable  = "act_acno_"+hWdayStmtCycle;

  int  n = loadTable();
  setLoadData("acno.p_seqno");

  showLogMessage("I","","Load Count: ["+n+"]");
 }
 // ************************************************************************
 public void  loadActAcctHst() throws Exception
 {
  extendField = "acht.";
  selectSQL = "p_seqno, "
          + "stmt_new_amt";
  daoTable  = "act_acct_hst";
  whereStr  = "WHERE  acct_month = ? ";

  setString(1  , hWdayLlAcctMonth);

  int  n = loadTable();
  setLoadData("acht.p_seqno");

  showLogMessage("I","","Load act_acct_hst Count: ["+n+"]");
 }
 // ************************************************************************
 public void  loadActAnalSub() throws Exception
 {
  extendField = "anal.";
  selectSQL = "p_seqno, "
          + "(no_consume_month+1) as no_consume_month,"
          + "(his_purchase_amt+his_cash_amt) as stmt_new_amt";
  daoTable  = "act_anal_sub";
  whereStr  = "WHERE  acct_month = ? ";

  setString(1  , hWdayLlAcctMonth);

  int  n = loadTable();
  setLoadData("anal.p_seqno");

  showLogMessage("I","","Load act_anal_sub Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample

