/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 111/03/18  V1.00.03  Allen Ho                                              *
* 111-11-11  V1.00.01    Machao   sync from mega & updated for project coding standard                                                                           *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class CycA380 extends AccessDAO
{
 private final String PROGNAME = "關帳-專案基金統計處理程式 111-11-11  V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comc = null;

 String businessDate   = "";
 String cycleEDate    = "";

 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA380 proc = new CycA380();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
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

   comc = new CommCashback(getDBconnect(),getDBalias());
   comc.modPgm = javaProgram; 

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","DEBUG 刪除資料");
   deleteMktLoanHst();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","重整專案基金資料");
   selectMktCashbackDtl0();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","當月基金計算資料");
   selectMktCashbackDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
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

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
  public int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  selectMktCashbackDtl() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.fund_code,"
            + "acct_date,"
            + "effect_e_date,"
            + "max(a.acct_type) as acct_type,"
            + "max(a.id_p_seqno) as id_p_seqno,"
            + "max(a.fund_name) as fund_name,"
            + "sum(a.end_tran_amt+a.res_tran_amt) as net_fund";
  daoTable  = "mkt_cashback_dtl a,cyc_acmm_"+ getValue("wday.stmt_cycle") +" b,mkt_loan_parm c"; 
  whereStr  = "where a.p_seqno = b.p_seqno "
            + "and   a.fund_code = c.fund_code "
            + "and   a.end_tran_amt+a.res_tran_amt!=0 " 
            + "and   a.acct_date <= ? "
            + "group by a.p_seqno,a.fund_code,acct_date,effect_e_date "
            ;

  setString(1 , businessDate);

  openCursor();

  totalCnt=0;


  while( fetchTable() ) 
   { 
    totalCnt++;

    setValue("cycle_e_date" , "");
    if (getValue("effect_e_date").length()>0)
       {
        cycleEDate = getValue("effect_e_date").substring(0,6)+getValue("wday.stmt_cycle");
        if (getValue("effect_e_date").substring(6,8).compareTo(getValue("wday.stmt_cycle"))<0)
           cycleEDate = comm.lastMonth(cycleEDate,1)+getValue("wday.stmt_cycle");
        setValue("cycle_e_date" , cycleEDate);
       }

//  if (getValue("wday.this_acct_month").equals(cycle_e_date)) continue;

    insertMktLoanHst();
   } 

  closeCursor();
  return;
 }
// ************************************************************************
 int insertMktLoanHst() throws Exception
 {
  dateTime();
  setValue("lhst.p_seqno"                  , getValue("p_seqno"));
  setValue("lhst.acct_month"               , getValue("wday.this_acct_month"));
  setValue("lhst.fund_code"                , getValue("fund_code")); 
  setValue("lhst.fund_name"                , getValue("fund_name")); 
  setValue("lhst.id_p_seqno"               , getValue("id_p_seqno")); 
  setValue("lhst.stmt_cycle"               , getValue("wday.stmt_cycle")); 
  setValue("lhst.acct_type"                , getValue("acct_type")); 
  setValue("lhst.acct_date"                , getValue("acct_date"));
  setValue("lhst.cycle_e_date"             , getValue("cycle_e_date"));
  setValue("lhst.effect_e_date"            , getValue("effect_e_date"));
  setValue("lhst.net_fund"                 , getValue("net_fund"));
  setValue("lhst.mod_time"                 , sysDate+sysTime);
  setValue("lhst.mod_pgm"                  , javaProgram);

  extendField = "lhst.";
  daoTable  = "mkt_loan_hst";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteMktLoanHst() throws Exception
 {
  daoTable  = "mkt_loan_hst";
  whereStr  = "where acct_month  = ? "
            + "and   stmt_cycle  = ? "
            ; 

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void selectMktCashbackDtl0() throws Exception
 {
  selectSQL = "sum(decode(sign(end_tran_amt),-1,end_tran_amt,0)) as m_tran_amt," 
            + "sum(decode(sign(end_tran_amt), 1,end_tran_amt,0)) as p_tran_amt,"
            + "a.fund_code,"
            + "p_seqno"; 
  daoTable  = "mkt_cashback_dtl a,mkt_loan_parm b";
  whereStr  = "where end_tran_amt != 0 "
            + "and   a.fund_code = b.fund_code "
            + "group by p_seqno,a.fund_code "
            + "having sum(decode(sign(end_tran_amt),1,end_tran_amt,0))!=0 "
            + "and    sum(decode(sign(end_tran_amt),-1,end_tran_amt,0))!=0"
            ;

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    selectMktCashbackDtl1();

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 void  selectMktCashbackDtl1() throws Exception
 {
  extendField = "mcdl.";
  selectSQL = "tran_seqno";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "WHERE fund_code = ? "
            + "and   p_seqno   = ? "  
            + "and   end_tran_amt < 0 "
            + "order by  tran_date,tran_time,end_tran_amt ";

  setString(1 , getValue("fund_code"));
  setString(2 , getValue("p_seqno"));

  int recCnt = selectTable();

  for ( int inti=0; inti<recCnt; inti++ )
    {
     comc.cashbackFunc(getValue("mcdl.tran_seqno",inti));
    }
  return;
 }
// ************************************************************************ 


}  // End of class FetchSample
