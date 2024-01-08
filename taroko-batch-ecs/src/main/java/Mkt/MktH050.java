/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/10/24  V1.00.00  Allen Ho   mkt_h050                                   *
* 109-12-09  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;
import java.lang.Math;

@SuppressWarnings("unchecked")
public class MktH050 extends AccessDAO
{
 private  String progname = "台幣基金-專案回饋金每月結算檔處理程式 109/12/09 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusiBusinessDate  = "";
 int  newAddFund=0,adjustFund=0,useFund=0,removeFund=0,netFund=0,lastMonthFund=0;
 String fundName ="";
 String fundCode = "";

 long    totalCnt=0,updateCnt=0;
 int cnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktH050 proc = new MktH050();
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

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","清檔 ...");
   deleteMktLoanStat();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔 ...");
   loadMktLoanStat1();
   loadPtrPayment();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理主檔 ...");
   selectMktCashbackDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存歷史資料");
   loadMktLoanStat();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","補當月無基金資料");
   selectMktLoanStat();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 新增 ["+updateCnt+"] 筆");
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
 public void  selectMktCashbackDtl() throws Exception
 {
  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
            + "substr(acct_date,1,6) as acct_month,"
            + "max(a.fund_name) as fund_name,"
            + "sum(decode(tran_code,'1',a.beg_tran_amt,0)) as beg_tran_amt1,"
            + "sum(decode(tran_code,'2',a.beg_tran_amt,0)) as beg_tran_amt2,"
            + "sum(decode(tran_code,'3',a.beg_tran_amt,0)) as beg_tran_amt3,"
            + "sum(decode(tran_code,'4',a.beg_tran_amt,0)) as beg_tran_amt4,"
            + "sum(decode(tran_code,'5',a.beg_tran_amt,0)) as beg_tran_amt5,"
            + "sum(decode(tran_code,'6',a.beg_tran_amt,0)) as beg_tran_amt6,"
            + "sum(decode(tran_code,'7',a.beg_tran_amt,0)) as beg_tran_amt7,"
            + "sum(end_tran_amt) as end_tran_amt ";
  daoTable  = "mkt_cashback_dtl a,mkt_loan_parm b";
  whereStr  = "where ((a.acct_date like ?||'%') "
            + " or    (a.acct_date <   ? "
            + "  and   a.end_tran_amt!=0)) " 
            + "and   a.fund_code = b.fund_code "
            + "and   a.tran_code != '0' "
            + "group by substr(a.fund_code,1,4),substr(acct_date,1,6) ";
            ;

  setString(1 , comm.lastMonth(hBusiBusinessDate));
  setString(2 , comm.lastMonth(hBusiBusinessDate)+"01");

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    if (fundCode.length()!=0)
    if (!getValue("fund_code").equals(fundCode))
       {
        setValue("fund.fund_code" , fundCode);
        cnt1 = getLoadData("fund.fund_code");
        lastMonthFund = 0;
        if (cnt1>0) lastMonthFund = getValueInt("fund.last_month_fund");

        setValue("type.payment_type" , fundCode);
        cnt1 = getLoadData("type.payment_type");
        if (cnt1>0) fundName = getValue("type.bill_desc");
        else fundName = getValue("fund_name");

        insertMktLoanStat();
        newAddFund = adjustFund = useFund = removeFund = netFund=0;
       }
    fundCode  = getValue("fund_code");

    netFund = netFund 
             + getValueInt("end_tran_amt");

    if (getValue("acct_month").compareTo(comm.lastMonth(hBusiBusinessDate).substring(0,6))<0)
        continue;

    newAddFund = newAddFund 
                 + getValueInt("beg_tran_amt1")
                 + getValueInt("beg_tran_amt2") 
                 + getValueInt("beg_tran_amt5");

    adjustFund  = adjustFund  
                 + getValueInt("beg_tran_amt3")
                 + getValueInt("beg_tran_amt7");
 
    useFund     = useFund
                 + getValueInt("beg_tran_amt4");

    removeFund  = removeFund  
                 + getValueInt("beg_tran_amt6");

   }
  if (totalCnt>0)
     {  
      fundCode  = getValue("fund_code");
      showLogMessage("I","","STEP 7 fund_code ["+fundCode+"]");

      setValue("fund.fund_code" , fundCode);
      cnt1 = getLoadData("fund.fund_code");
      lastMonthFund = 0;
      if (cnt1>0) lastMonthFund = getValueInt("fund.last_month_fund");

      setValue("type.payment_type" , fundCode);
      cnt1 = getLoadData("type.payment_type");
      if (cnt1>0) fundName = getValue("type.bill_desc");
      else fundName = getValue("fund_name");

      insertMktLoanStat();
     }
  closeCursor();
  return;
 }
// ************************************************************************
void loadMktLoanStat1() throws Exception 
 {
  extendField = "fund.";
  selectSQL = "a.fund_code,"
            + "a.net_ttl_fund as last_month_fund";
  daoTable  = "mkt_loan_stat a";
  whereStr  = "WHERE  stat_month = ? "
            ;

  setString(1 , comm.lastMonth(hBusiBusinessDate));

  int  n = loadTable();
  setLoadData("fund.fund_code");

  showLogMessage("I","","Load mkt_loan_stat: ["+n+"]");
     
 }
// ************************************************************************
void loadPtrPayment() throws Exception 
 {
  extendField = "type.";
  selectSQL = "payment_type,"
            + "bill_desc";
  daoTable  = "ptr_payment";
  whereStr  = "WHERE  fund_flag    = 'Y' ";
            ;

  int  n = loadTable();
  setLoadData("type.payment_type");

  showLogMessage("I","","Load ptr_payment : ["+n+"]");
 }
// ************************************************************************
 int insertMktLoanStat() throws Exception
 {
  dateTime();
  extendField = "thst.";

  setValue("thst.stat_month"              , hBusiBusinessDate.substring(0,6));
  setValue("thst.acct_type"               , "");
  setValue("thst.fund_code"               , fundCode);
  setValueInt("thst.last_month_fund"      , lastMonthFund);
  setValueInt("thst.new_add_fund"         , newAddFund);
  setValueInt("thst.adjust_fund"          , adjustFund);
  setValueInt("thst.use_fund"             , useFund);
  setValueInt("thst.remove_fund"          , removeFund);
  setValueDouble("thst.diff_fund"         , lastMonthFund
                                          + newAddFund
                                          + adjustFund
                                          + useFund
                                          + removeFund
                                          - netFund);
  setValueInt("thst.net_ttl_fund"         , netFund); 
  setValue("thst.mod_time"                , sysDate+sysTime);
  setValue("thst.mod_pgm"                 , javaProgram);

  daoTable  = "mkt_loan_stat";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteMktLoanStat() throws Exception
 {
  daoTable  = "mkt_loan_stat";
  whereStr  = "where stat_month = ? "
            ;

  setString(1 , hBusiBusinessDate.substring(0,6));

  totalCnt = deleteTable();

  showLogMessage("I","","deletet mkt_loan_stat : ["+ totalCnt +"]");

  return;
 }
// ************************************************************************
 void  loadMktLoanStat() throws Exception
 {
  extendField = "fhst.";
  selectSQL   = "fund_code";
  daoTable    = "mkt_loan_stat  a";
  whereStr    = "where stat_month = ? "  
              ;

  setString(1 , hBusiBusinessDate.substring(0,6));

  int  n = loadTable();

  setLoadData("fhst.fund_code");

  showLogMessage("I","","Load mkt_loan_stat Count: ["+n+"]");
 }
// ************************************************************************
 void  selectMktLoanStat() throws Exception
 {
  selectSQL = "a.fund_code,"
            + "c.bill_desc as fund_name,"
            + "a.net_ttl_fund as last_month_fund"; 
  daoTable  = "mkt_loan_stat a,mkt_loan_parm b,ptr_payment c";
  whereStr  = "where stat_month  = ? "  
            + "and   a.fund_code = substr(b.fund_code,1,4) "
            + "and   a.fund_code = c.payment_type "
            + "and   c.fund_flag = 'Y' "
            + "and   a.net_ttl_fund > 0 "
            ;

  setString(1 , comm.lastMonth(hBusiBusinessDate));

  openCursor();

  totalCnt=0;
  updateCnt=0;
  int cnt2=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    setValue("fhst.fund_code" , getValue("fund_code"));
    cnt1 = getLoadData("fhst.fund_code");
    if (cnt1!=0) continue;

    newAddFund=adjustFund=useFund=removeFund=netFund=0;

    netFund        = getValueInt("last_month_fund");
    lastMonthFund = getValueInt("last_month_fund");
    fundName = getValue("fund_name");
     
    fundCode  = getValue("fund_code");

    insertMktLoanStat();
    updateCnt++;
   }
  closeCursor();
  return;
 }
// ************************************************************************

}  // End of class FetchSample
