/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 107/12/10  V1.00.04  Allen Ho   cyc_A235 PROD compare OK                   *
 * 109-12-18   V1.00.05  tanwei      updated for project coding standard      *
 ******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA550 extends AccessDAO
{
 private  String progname = "關帳-對帳單顯示分期零期資訊處理程式 109/12/18 V1.00.05";
 CommFunction comm = new CommFunction();

 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";

 long    totalCnt=0,currCnt=0;
 int debug =1;
 int paymentAmt = 0,insertCnt=0,updateCnt=0;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA550 proc = new CycA550();
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

   loadActAcno() ;
   showLogMessage("I","","=========================================");
   showLogMessage("I","","bil_contract 處理開始...");
   selectBilContract();
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

  return(0);
 }
 // ************************************************************************
 void  selectBilContract() throws Exception
 {
  selectSQL = "p_seqno,"
          + "stmt_cycle,"
          + "rowid as rowid";
  daoTable  = "bil_contract";
  whereStr  = "WHERE   merge_flag       != 'Y' "
          + "AND     installment_kind != 'F' "
          + "AND     install_tot_term > 1 "
          + "AND     new_it_flag = 'Y' "
          + "AND     new_proc_date != first_post_date "
          + "AND     first_post_date > ? "
          + "AND     (stmt_cycle        = ? "
          + " or      p_seqno in (select p_seqno from cyc_acmm_"+hWdayStmtCycle+" )) "
          + "AND     install_curr_term = 0 ";

  setString(1  , hBusiBusinessDate);
  setString(2  , hWdayStmtCycle);

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() )
  {
   totalCnt++;
   okFlag = 0;
   setValue("billing_disp_date",hBusiBusinessDate);
   if (updateCycAcmm()!=0)
   {
    setValue("acno.p_seqno",getValue("p_seqno"));
    int cnt1 = getLoadData("acno.p_seqno");

    if (getValue("acno.new_cycle_month").length()==0)  continue;
    setValue("billing_disp_date", getValue("acno.new_cycle_month")+getValue("stmt_cycle"));
    okFlag = 1;
   }

   updateBilContract(okFlag);
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 void updateBilContract(int okFlag) throws Exception
 {
  currCnt++;
  dateTime();
  updateSQL = "billing_disp_date = ?,"
          + "stmt_cycle        = ?,"
          + "mod_pgm           = ?,"
          + "mod_time        = sysdate";
  daoTable  = "bil_contract";
  whereStr  = "WHERE  rowid  = ?";

  if (okFlag==0)
  {
   setString(1  , getValue("billing_disp_date"));
   setString(2  , getValue("stmt_cycle"));
  }
  else
  {
   setString(1  , getValue("acno.new_cycle_month")
           + getValue("acno.stmt_cycle"));
   setString(2  , getValue("acno.stmt_cycle"));
  }
  setString(3 , javaProgram);
  setRowId(4  , getValue("rowid"));

  updateTable();
  return;
 }
 // ************************************************************************
 int updateCycAcmm() throws Exception
 {
  currCnt++;
  dateTime();
  updateSQL = "it_disp0_flag = 'Y',"
          + "mod_pgm         = ?, "
          + "mod_time        = timestamp_format(?,'yyyymmddhh24miss')";
  daoTable  = "cyc_acmm_"+hWdayStmtCycle;
  whereStr  = "WHERE  p_seqno = ? ";

  setString(1 , javaProgram);
  setString(2 , sysDate+sysTime);
  setString(3  , getValue("p_seqno"));

  updateTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
 // ************************************************************************
 public void  loadActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "p_seqno, "
          + "new_cycle_month,"
          + "stmt_cycle";
  daoTable  = "act_acno_"+hWdayStmtCycle;

  int  n = loadTable();
  setLoadData("acno.p_seqno");

  showLogMessage("I","","Load Count: ["+n+"]");
 }
// ************************************************************************



}  // End of class FetchSample

