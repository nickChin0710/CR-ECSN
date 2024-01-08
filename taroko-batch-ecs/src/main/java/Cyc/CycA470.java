/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/12/07  V1.00.03  Allen Ho   cyc_A318                                   *
* 109-12-18  V1.00.04  tanwei     updated for project coding standard        *
* 112-08-22  V1.00.05  Simon      讀取 "act_acct" 改讀取 "act_acct_"+hWdayStmtCycle*
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA470 extends AccessDAO
{
 private  String progname = "關帳-帳戶分析統計明細資料檔新增處理程式 "
                          + "112/08/22 V1.00.05";
 CommFunction comm = new CommFunction();

 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";
 String hWdayLastAcctMonth = "";

 long    totalCnt=0,currCnt=0;
 boolean debug = false;
 int paymentAmt = 0,insertCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA470 proc = new CycA470();
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

   showLogMessage("I","","Business_date["+hBusiBusinessDate+"]");
   showLogMessage("I","","last_acct_month["+hWdayLastAcctMonth+"]");
   if (debug)                    
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","刪除 act_anal_sub...");
       deleteActAnalSub();
       showLogMessage("I","","total_count=["+totalCnt+"]");
       commitDataBase();
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","新增 act_anal_sub...");
   totalCnt=0;
   selectCycAcmm();
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
  hWdayLastAcctMonth =  getValue("last_acct_month");

  return(0);
 }
// ************************************************************************
 void  selectCycAcmm() throws Exception
 {
  selectSQL = "c.p_seqno,"
            + "c.acct_type,"
            + "c.ttl_purch_cnt as his_purchase_cnt,"
            + "c.ttl_purchase as his_purchase_amt,"
            + "c.no_module2_amt as his_pur_no_m2,"
            + "c.ttl_cash_cnt as his_cash_cnt,"
            + "c.ttl_cash as his_cash_amt,"
            + "least(greatest("
            + "  decode(sign(c.this_ttl_amt-c.new_amt-c.payment_amt),"
            + "        1,round(c.payment_amt/(c.this_ttl_amt-c.new_amt-payment_amt)*-100)"
            + "         ,decode(sign(c.payment_amt),-1,100,0)),0),100) as his_pay_percentage,"
            + "least(greatest("
            + "  decode(sign(c.last_credit_amt),"
            + "        0,decode(sign(c.last_purc_end_bal),1,100,0)"
            + "         ,round((c.last_purc_end_bal/c.last_credit_amt)*100)),0),100) as his_rc_percentage,"
            + "a.pay_amt as his_pay_amt,"
            + "a.pay_cnt as his_pay_cnt,"
            + "a.adjust_dr_amt as his_adj_dr_amt,"
            + "a.adjust_cr_amt as his_adj_cr_amt,"
            + "a.adjust_dr_cnt as his_adj_dr_cnt,"
            + "a.adjust_cr_cnt as his_adj_cr_cnt,"
            + "c.his_combo_cash_amt as his_combo_cash_amt,"
            + "c.his_combo_cash_fee as his_combo_cash_fee,"
            + "c.no_consume_month   as no_consume_month,"
            + "c.unpost_installment as unpost_installment";
//daoTable  = "act_acct a,cyc_acmm_"+hWdayStmtCycle+" c"; 
  daoTable  = "act_acct_"+hWdayStmtCycle+" a, cyc_acmm_"+hWdayStmtCycle+" c"; 
  whereStr  = "WHERE  a.p_seqno    = c.p_seqno";

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    insertActAnalSub();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int insertActAnalSub() throws Exception
 {
  dateTime();

  setValue("p_seqno"                 , getValue("p_seqno"));
  setValue("acct_month"              , hWdayLastAcctMonth);
  setValue("acct_type"               , getValue("acct_type"));
  setValue("his_purchase_cnt"        , getValue("his_purchase_cnt"));
  setValue("his_purchase_amt"        , getValue("his_purchase_amt"));
  setValue("his_pur_no_m2"           , getValue("his_pur_no_m2"));
  setValue("his_cash_cnt"            , getValue("his_cash_cnt"));
  setValue("his_cash_amt"            , getValue("his_cash_amt"));
  setValue("his_pay_percentage"      , getValue("his_pay_percentage"));
  setValue("his_rc_percentage"       , getValue("his_rc_percentage"));
  setValue("his_pay_amt"             , getValue("his_pay_amt"));
  setValue("his_pay_cnt"             , getValue("his_pay_cnt"));
  setValue("his_adj_dr_amt"          , getValue("his_adj_dr_amt"));
  setValue("his_adj_cr_amt"          , getValue("his_adj_cr_amt"));
  setValue("his_adj_dr_cnt"          , getValue("his_adj_dr_cnt"));
  setValue("his_adj_cr_cnt"          , getValue("his_adj_cr_cnt"));
  setValue("his_combo_cash_amt"      , getValue("his_combo_cash_amt"));
  setValue("his_combo_cash_fee"      , getValue("his_combo_cash_fee"));
  setValue("no_consume_month"        , getValue("no_consume_month"));
  setValue("unpost_installment"      , getValue("unpost_installment"));
  setValue("mod_time"                , sysDate+sysTime);
  setValue("mod_pgm"                 , javaProgram);

  daoTable  = "act_anal_sub";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteActAnalSub() throws Exception
 {
  daoTable  = "act_anal_sub a";
  whereStr  = "WHERE  acct_month = ? "
            + "AND    exists (select 1 from cyc_acmm_"+hWdayStmtCycle
            + "               where  p_seqno = a.p_seqno) ";

  setString(1 , hWdayLastAcctMonth);

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************

}  // End of class FetchSample

