/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/07/30  V1.00.19  Allen Ho   cyc_a190                                   *
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
import com.CommCrd;

@SuppressWarnings("unchecked")
public class CycA390 extends AccessDAO
{
 private final String PROGNAME = "關帳-台幣基金統計處理程式 111-11-11  V1.00.01";
 CommFunction comm = new CommFunction();
 CommCrd  comCrd  = new CommCrd();	
 CommString  comStr  = new CommString();	

 String businessDate   = "";
 String fundName       = "";
 double lastMonthFund=0;
 double  newAddFund=0,adjustFund=0,useFund=0,removeFund=0,netFund=0;

 int   ntCnt1=0;
 boolean DEBUG= false;

 long    totalCnt=0,updateCnt=0;
 int cnt1;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA390 proc = new CycA390();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
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
   
   if ( !connectDataBase() ) 
       return(1);

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","DEBUG 刪除資料");
   deleteMktFundHst();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktFundHst();
   loadPtrPayment();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處裡 mkt_cashback__dtl 資料");
   selectCycDcFundDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","當月基金差異計算資料");
   selectMktFundHst();
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
 void  selectCycDcFundDtl() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "acct_date,"
            + "max(a.acct_type) as acct_type,"
            + "max(a.id_p_seqno) as id_p_seqno,"
            + "max(a.fund_name) as fund_name,"
            + "substr(a.fund_code,1,4) as fund_code,"
            + "tran_code,"
            + "sum(a.beg_tran_amt) as beg_tran_amt,"
            + "sum(a.end_tran_amt+a.res_tran_amt) as end_tran_amt";
  daoTable  = "mkt_cashback_dtl a,cyc_acmm_"+ getValue("wday.stmt_cycle") +" b "; 
  whereStr  = "where a.p_seqno = b.p_seqno "
            + "and  ((a.acct_date > ? "
            + "  and  a.acct_date <= ?)  "
            + " or   (a.acct_date <= ?  "
            + "  and  a.end_tran_amt+a.res_tran_amt!=0)) " 
            + "group by a.p_seqno,substr(a.fund_code,1,4),tran_code,acct_date "
            ;

  setString(1, getValue("wday.last_close_date"));
  setString(2, getValue("wday.this_close_date"));
  setString(3, getValue("wday.last_close_date"));

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    newAddFund = adjustFund = useFund = removeFund = 0;
    if (getValue("acct_date").compareTo(getValue("wday.last_close_date"))>0)
       {
        if ((getValue("tran_code").equals("1"))||
            (getValue("tran_code").equals("2"))||
            (getValue("tran_code").equals("5")))
           newAddFund = getValueInt("beg_tran_amt");
        else    
        if ((getValue("tran_code").equals("0"))||
            (getValue("tran_code").equals("3"))||
            (getValue("tran_code").equals("7")))
           adjustFund = getValueInt("beg_tran_amt");
        else    
        if (getValue("tran_code").equals("4"))
           useFund    = getValueInt("beg_tran_amt");
        else    
        if (getValue("tran_code").equals("6"))
           removeFund      = getValueInt("beg_tran_amt");
       }
    netFund = getValueInt("end_tran_amt");

    setValue("prpt.payment_type" , getValue("fund_code"));
    cnt1 = getLoadData("prpt.payment_type");
    if (cnt1>0) fundName = getValue("prpt.bill_desc");
    //else fundName = getValue("fund_name");
    else fundName = getValue("fund_name");
    
//	if (fundName.getBytes().length > 60 ) {
		//fundName = comCrd.subMS950String(fundName.getBytes("MS950"), 0, 60);
		fundName = comStr.left(fundName, 30);
//	}     

    if (updateMktFundHst()!=0)
       {
        setValue("fund.p_seqno"   , getValue("p_seqno"));
        setValue("fund.fund_code" , getValue("fund_code"));
        cnt1 = getLoadData("fund.p_seqno,fund.fund_code");
        lastMonthFund = 0;
        if (cnt1>0) lastMonthFund = getValueInt("fund.last_month_fund");
        insertMktFundHst();
       }
   } 

  closeCursor();
  return;
 }
// ************************************************************************
void loadPtrPayment() throws Exception 
 {
  extendField = "prpt.";
  selectSQL = "payment_type,"
            + "bill_desc";
  daoTable  = "ptr_payment";
  whereStr  = "WHERE  fund_flag    = 'Y' " ;

  int  n = loadTable();

  setLoadData("prpt.payment_type");

  showLogMessage("I","","Load ptr_payment Count: ["+n+"]");
 }
// ************************************************************************
 int insertMktFundHst() throws Exception
 {
  dateTime();
  setValue("thst.p_seqno"                  , getValue("p_seqno"));
  setValue("thst.acct_month"               , getValue("wday.this_acct_month"));
  setValue("thst.curr_code"                , "901");
  setValue("thst.fund_code"                , getValue("fund_code")); 
  //setValue("thst.fund_name"                , getValue("fund_name")); 
  setValue("thst.fund_name"                , fundName );
  setValue("thst.id_p_seqno"               , getValue("id_p_seqno")); 
  setValue("thst.stmt_cycle"               , getValue("wday.stmt_cycle")); 
  setValue("thst.acct_type"                , getValue("acct_type")); 
  //setValue("thst.bill_desc"                , getValue("fund_name")); 
  setValue("thst.bill_desc"                , fundName );
  setValueDouble("thst.last_month_fund"    , lastMonthFund); 
  setValueDouble("thst.new_add_fund"       , newAddFund); 
  setValueDouble("thst.adjust_fund"        , adjustFund); 
  setValueDouble("thst.use_fund"           , useFund); 
  setValueDouble("thst.give_fund"          , 0); 
  setValueDouble("thst.remove_fund"        , removeFund);
  setValue("thst.diff_fund"                , "0");
  setValueDouble("thst.net_fund"           , netFund); 
  setValue("thst.mod_time"                 , sysDate+sysTime);
  setValue("thst.mod_pgm"                  , javaProgram);

  extendField = "thst.";
  daoTable  = "mkt_fund_hst";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteMktFundHst() throws Exception
 {
  daoTable  = "mkt_fund_hst";
  whereStr  = "where acct_month  = ? "
            + "and   stmt_cycle  = ? "
            + "and   curr_code  = '901'  ";
             

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void  loadMktFundHst() throws Exception
 {
  extendField = "fund.";
  selectSQL = "a.p_seqno,"
            + "a.curr_code,"
            + "a.fund_code,"
            + "a.net_fund as last_month_fund";
  daoTable  = "mkt_fund_hst a,cyc_acmm_"+ getValue("wday.stmt_cycle")+" b ";
  whereStr  = "WHERE  a.acct_month = ? "
            + "and    a.curr_code  = '901' "
            + "and    a.p_seqno    = b.p_seqno "
            ;

  setString(1 , getValue("wday.last_acct_month"));

  int  n = loadTable();
  setLoadData("fund.p_seqno,fund.fund_code");
  showLogMessage("I","","Load mkt_fund_hst : ["+n+"]");
 }
// ************************************************************************
 void  selectMktFundHst() throws Exception
 {
  selectSQL = "last_month_fund,"
            + "new_add_fund,"
            + "adjust_fund,"
            + "use_fund,"
            + "remove_fund,"
            + "net_fund,"
            + "rowid as rowid";
  daoTable  = "mkt_fund_hst";
  whereStr  = "where acct_month   = ? "  
            + "and   curr_code = '901' "
            ;

  setString(1 , getValue("wday.this_acct_month"));

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    if ( getValueDouble("last_month_fund")
       + getValueDouble("new_add_fund")
       + getValueDouble("adjust_fund")
       + getValueDouble("use_fund")
       + getValueDouble("remove_fund")
       - getValueDouble("net_fund") ==0) continue;

    totalCnt++;
    updateMktFundHst1();
   }
  closeCursor();
  return;
 }
// ************************************************************************
 int updateMktFundHst() throws Exception
 {
  updateSQL = "new_add_fund     = new_add_fund + ?,"
            + "adjust_fund      = adjust_fund  + ?,"
            + "use_fund         = use_fund     + ?,"
            + "remove_fund      = remove_fund  + ?, "
            + "net_fund         = net_fund     + ? ";
  daoTable  = "mkt_fund_hst";
  whereStr  = "WHERE acct_month = ? "
            + "and   p_seqno    = ? "
            + "and   curr_code  = '901' "
            + "and   fund_code  = ? "
            ;

  setDouble(1 , newAddFund);
  setDouble(2 , adjustFund); 
  setDouble(3 , useFund); 
  setDouble(4 , removeFund);
  setDouble(5 , netFund);
  setString(6 , getValue("wday.this_acct_month"));
  setString(7 , getValue("p_seqno"));
  setString(8 , getValue("fund_code"));


  updateTable();
  if (notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************
 int updateMktFundHst1() throws Exception
 {
  updateSQL = "diff_fund        = (last_month_fund"
            + "                 + new_add_fund"
            + "                 + adjust_fund"
            + "                 + use_fund"
            + "                 + remove_fund"
            + "                 - net_fund)*-1";
  daoTable  = "mkt_fund_hst";
  whereStr  = "WHERE rowid      = ? "
            ;

  setRowId(1 , getValue("rowid"));

  updateTable();
  if (notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample


