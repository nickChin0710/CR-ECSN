/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/01/02  V1.00.06  Allen Ho   cyc_A142 PROD compare OK                   *
* 109-12-17   V1.00.07  tanwei      updated for project coding standard      *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA090 extends AccessDAO
{
 private  String progname = "關帳-特殊MP超出適用MCODE處理程式 109/12/17 V1.00.07";
 CommFunction comm = new CommFunction();

 String hBusiBusinessDate   = "";
 String tranSeqno = "";
 String fundCode="";

 long    totalCnt=0;
 int parmCnt=0;
 int debug =1;
 int[] dInt = {0,0,0,0};
 String pSeqno = "";
 int paymentAmt = 0,insertCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA090 proc = new CycA090();
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

   showLogMessage("I","","this_acct_month["+getValue("wday.this_acct_month")+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新開始....");
   selectActAcno();
   showLogMessage("I","","累計處理筆數["+totalCnt+"]");
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
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 public void  selectActAcno() throws Exception
 {
  selectSQL = "p_seqno,"
            + "decode(mp_flag,'1','1','0') as mp_flag,"
            + "a.rowid as rowid";
  daoTable  = " act_acno_"+getValue("wday.stmt_cycle") +" a,ptr_actgeneral_n b";
  whereStr  = "WHERE ((a.mp_flag  = '1') "
            + " or      (a.min_pay_rate > 0 "
            + "  and     ? between a.min_pay_rate_s_month " 
            + "            and     decode(a.min_pay_rate_e_month,'','300012',a.min_pay_rate_e_month))) "
            + "AND   a.acct_type       = b.acct_type "
            + "AND   b.mp_mcode        > 0 "
            + "AND   a.int_rate_mcode >= b.mp_mcode "
            ;

  setString(1 , getValue("wday.this_acct_month"));

  openCursor();

  pSeqno = "";
  paymentAmt = 0;
  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    if (!getValue("mp_flag").equals("1")) 
       {
        updateActAcno();
        updateActAcno1();
       }
     else 
       {
        updateActAcctCurr();
        updateActAcctCurr1();
       }
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void updateActAcno() throws Exception
 {
  dateTime();
  updateSQL = "min_pay_rate_e_month = ?,"
            + "mod_pgm   = ?, "
            + "mod_time             = sysdate";
  daoTable  = "act_acno";
  whereStr  = "WHERE  p_seqno = ? ";

  setString(1 , getValue("wday.last_acct_month"));
  setString(2 , javaProgram);
  setString(3 , getValue("p_seqno"));

  updateTable();
  updateCnt++;
  return;
 }
// ************************************************************************
 void updateActAcno1() throws Exception
 {
  dateTime();
  updateSQL = "min_pay_rate_e_month = ?,"
            + "mod_pgm   = ?, "
            + "mod_time             = sysdate";
  daoTable  = "act_acno_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE  rowid  = ? ";

  setString(1 , getValue("wday.last_acct_month"));
  setString(2 , javaProgram);
  setRowId(3  , getValue("rowid"));

  updateTable();
  updateCnt++;
  return;
 }
// ************************************************************************
 void updateActAcctCurr() throws Exception
 {
  dateTime();
  updateSQL = "mp_1_e_month = ?,"
            + "mod_pgm   = ?, "
            + "mod_time             = sysdate";
  daoTable  = "act_acct_curr";
  whereStr  = "WHERE p_seqno = ? "
            + "and   ? between decode(mp_1_s_month,'','200001',mp_1_s_month) "
            + "        and     decode(mp_1_e_month,'','300012',mp_1_e_month) ";

  setString(1 , getValue("wday.last_acct_month"));
  setString(2 , javaProgram);
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("wday.this_acct_month"));

  updateTable();
  updateCnt++;
  return;
 }
// ************************************************************************
 void updateActAcctCurr1() throws Exception
 {
  dateTime();
  updateSQL = "mp_1_e_month = ?,"
            + "mod_pgm   = ?, "
            + "mod_time             = sysdate";
  daoTable  = "act_acct_curr_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE p_seqno = ? "
            + "and   ? between decode(mp_1_s_month,'','200001',mp_1_s_month) "
            + "        and     decode(mp_1_e_month,'','300012',mp_1_e_month) ";

  setString(1 , getValue("wday.last_acct_month"));
  setString(2 , javaProgram);
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("wday.this_acct_month"));

  updateTable();
  updateCnt++;
  return;
 }
// ************************************************************************

}  // End of class FetchSample

