/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/08/28  V1.00.00  Allen Ho                                              *
* 109-12-09  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktH090 extends AccessDAO
{
 private  String progname = "基金-無效卡達三個月移除處理程式109/12/09 V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comc = null;
 CommRoutine comr = null;

 String businessDate  = "";
 String tranSeqno     = "";

 int    cnt1=0,cnt2=0,parmCnt=0;
 long    totalCnt=0,updateCnt=0;
 long    updateCnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktH090 proc = new MktH090();
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

   if (comm.isAppActive2(javaProgram))
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
   comr   = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本程式只再關帳日次一日執行 !");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadActAcno();
   loadCrdCard();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理停卡資料");
   selectMktCashbackDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"]["+updateCnt1+"] 筆");
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
 void  selectMktCashbackDtl() throws Exception
 {
  selectSQL = "p_seqno,"
            + "fund_code,"
            + "max(fund_name) as fund_name,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type,"
            + "sum(end_tran_amt) as end_tran_amt";
  daoTable  = "mkt_cashback_dtl a";
  whereStr  = "where  end_tran_amt != 0 "
            + "group by p_seqno,fund_code "
            + "having sum(end_tran_amt) != 0  "
            ;

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    setValue("acno.p_seqno" ,  getValue("p_seqno"));
    cnt1 =  getLoadData("acno.p_seqno");
    if (cnt1==0) continue;

    setValue("card.p_seqno" ,  getValue("p_seqno"));
    cnt1 =  getLoadData("card.p_seqno");
    if (cnt1!=0) continue;

    updateCnt++;
    insertMktCashbackDtl();
    updateMktCashbackDtl();
//    if (getValueInt("end_tran_amt")<0)
//       insertCycFundDtl(1);
//    else
//       insertCycFundDtl(2);

//  showLogMessage("I","","2 fund_code ["+ getValue("fund_code") +"]");
//  showLogMessage("I","","  p_seqno   ["+ getValue("p_seqno") +"]");
    } 
  closeCursor();
  return;
 }
// ************************************************************************
 int insertMktCashbackDtl() throws Exception
 {
  dateTime();
  extendField = "cash.";
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");
  setValue("cash.tran_date"            , sysDate);
  setValue("cash.tran_time"            , sysTime);
  setValue("cash.tran_seqno"           , tranSeqno);
  setValue("cash.fund_code"            , getValue("fund_code"));
  setValue("cash.fund_name"            , getValue("fund_name"));
  setValue("cash.acct_type"            , getValue("acct_type"));
  setValue("cash.tran_code"            , "6");
  setValueInt("cash.beg_tran_amt"      , getValueInt("end_tran_amt")*-1);
  setValueInt("cash.end_tran_amt"      , 0);
//setValue("cash.mod_desc"             , "指定團代無有效卡移除");
  setValue("cash.mod_desc"             , "一般基金無效卡達三個月移除");
  setValue("cash.mod_memo"             , "");
  setValue("cash.p_seqno"              , getValue("p_seqno"));
  setValue("cash.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("cash.acct_date"            , businessDate);
  setValue("cash.tran_pgm"             , javaProgram);
  setValue("cash.acct_month"           , getValue("wday.next_acct_month"));
  setValue("cash.proc_month"           , businessDate.substring(0,6));
  setValue("cash.apr_user"             , javaProgram);
  setValue("cash.apr_flag"             , "Y");
  setValue("cash.apr_date"             , sysDate);
  setValue("cash.crt_user"             , javaProgram);
  setValue("cash.crt_date"             , sysDate);
  setValue("cash.mod_user"             , javaProgram); 
  setValue("cash.mod_time"             , sysDate+sysTime);
  setValue("cash.mod_pgm"              , javaProgram);

  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktCashbackDtl() throws Exception
 {
  dateTime();
  updateSQL = "effect_flag   = 'Y', "
            + "remove_date   = ?, "
            + "mod_memo      = ?,"
            + "link_seqno    = ?,"
            + "link_tran_amt = end_tran_amt,"
            + "end_tran_amt   = 0,"
            + "mod_pgm        = ?,"
            + "mod_time       = sysdate";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "WHERE p_seqno       = ? "
            + "and   fund_code     = ? "
            + "and   end_tran_amt != 0 ";

  setString(1 , sysDate);
  setString(2 , "移除序號["+tranSeqno+"]");
  setString(3 , tranSeqno);
  setString(4 , javaProgram);
  setString(5 , getValue("p_seqno"));
  setString(6 , getValue("fund_code"));

  updateTable();
  return;
 }
// ************************************************************************
 int insertCycFundDtl(int numType) throws Exception
 {
  dateTime();
  setValue("fund.create_date"          , sysDate);
  setValue("fund.create_time"          , sysTime);
  setValue("fund.curr_code"            , "901");
  setValue("fund.business_date"        , businessDate);
  setValue("fund.acct_type"            , getValue("acct_type"));
  setValue("fund.p_seqno"              , getValue("p_seqno"));
  setValue("fund.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("fund.fund_code"            , getValue("fund_code"));
  setValue("fund.vouch_type"           , "3");
  if (numType==1)
     {
      setValue("fund.cd_kind"              , "A394");   /* 基金負項移除 */
     }
  else
     {
      setValue("fund.cd_kind"              , "A393");
     }
  setValue("fund.tran_code"            , "6");
  setValue("fund.memo1_type"           , "1");
  setValue("fund.fund_amt"             , getValue("end_tran_amt"));   // remove minus will ass new cd_kind
  setValue("fund.proc_flag"            , "N");
  setValue("fund.proc_date"            , "");
  setValue("fund.execute_date"         , businessDate);
  setValue("fund.fund_cnt"             , "1");
  setValue("fund.mod_time"             , sysDate+sysTime);
  setValue("fund.mod_pgm"              , javaProgram);

  extendField = "fund.";
  daoTable  = "cyc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "stmt_cycle,"
            + "next_acct_month";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1 , comm.nextNDate(businessDate , -1));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 void  loadActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "p_seqno";
  daoTable  = "act_acno";
  whereStr  = "WHERE stmt_cycle = ? "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  int  n = loadTable();
  setLoadData("acno.p_seqno");

  showLogMessage("I","","Load act_acno Count: ["+n+"]");
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "p_seqno";
  daoTable  = "crd_card";
  whereStr  = "WHERE stmt_cycle = ? "
            + "and   (oppost_date = '' "
            + " or    oppost_date >= ? )"
            + "and   card_no = major_card_no "
            + "order by p_seqno"
            ;

  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , comm.nextMonthDate(businessDate,-3));

  showLogMessage("I","","oppost_date >= ["+ comm.nextMonthDate(businessDate,-3) +"]");

  int  n = loadTable();
  setLoadData("card.p_seqno");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample

