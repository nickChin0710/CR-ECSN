/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/07/21  V1.00.00  Allen Ho   mkt_H080                                   *
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
public class MktH080 extends AccessDAO
{
 private  String progname = "台幣基金-基金效期到期移除處理程式 109/12/09 V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comc = null;
 CommRoutine comr = null;

 String hBusiBusinessDate   = "";
 String tranSeqno="";

 long    totalCnt=0,updateCnt=0;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktH080 proc = new MktH080();
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

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 )
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) exitProgram(1);
   comc = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectMktCashbackDtl0()>0)   // if no this check, will not right
     {
      commitDataBase();
      showLogMessage("I","","啟動 MktH070 執行後續動作 .....");
      showLogMessage("I","","===============================");

      String[] hideArgs = new String[1];
      try {
           hideArgs[0] = "";

           MktH070 mktH070 = new MktH070();
           int rtn = mktH070.mainProcess(hideArgs);
           if(rtn < 0)   return (1);
           showLogMessage("I","","MktH070 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex) 
               {
                showLogMessage("I","","無法執行 MktH070 ERROR!");
               }
     }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理移除基金資料...");
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
  int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "next_acct_month";
  daoTable  = "act_acno a,ptr_workday b";
  whereStr  = "where a.stmt_cycle = b.stmt_cycle "
            + "and   a.p_seqno = ? ";

  setString(1, getValue("p_seqno"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktCashbackDtl0() throws Exception
 {
  selectSQL = "count(*) as data_cnt";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where  end_tran_amt != 0 "
            + "group by p_seqno,fund_code "
            + "having (sum(decode(sign(end_tran_amt),1,end_tran_amt,0))!=0 "
            + " and    sum(decode(sign(end_tran_amt),-1,end_tran_amt,0))!=0) "
            + "and count(*) > 0 "
            ;

  showLogMessage("I","","===============================");
  showLogMessage("I","","檢查是否已重整 ...");

  int recCnt = selectTable();

  if (recCnt==0) 
     {
      showLogMessage("I","","資料已重整");
      return(0);
     }

  showLogMessage("I","","資料未重整");

  return(1);
 }
// ************************************************************************
 void  selectMktCashbackDtl() throws Exception
 {
  selectSQL = "p_seqno,"
            + "effect_e_date,"
            + "fund_code, "
            + "max(fund_name) as fund_name,"
            + "sum(end_tran_amt) as end_tran_amt, "
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where end_tran_amt > 0 "
            + "and   effect_e_date between ? and ? "
            + "group by p_seqno,effect_e_date,fund_code "
            ;

  if (hBusiBusinessDate.substring(6,8).equals("01"))
     setString(1 , comm.nextMonthDate(hBusiBusinessDate , -36));
  else
     setString(1 , comm.nextNDate(hBusiBusinessDate , -7));

  setString(2 , comm.nextNDate(hBusiBusinessDate , -1));

  if (hBusiBusinessDate.substring(6,8).equals("01"))
     {
      showLogMessage("I","","判斷日期 : ["
                           + comm.nextMonthDate(hBusiBusinessDate , -36)
                           +"]-["
                           + comm.nextNDate(hBusiBusinessDate , -1)
                           + "]");
     }
  else
     {
      showLogMessage("I","","判斷日期 : ["
                           + comm.nextNDate(hBusiBusinessDate , -7)
                           +"]-["
                           + comm.nextNDate(hBusiBusinessDate , -1)
                           + "]");
     }

  openCursor();
  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    selectPtrWorkday();
    insertMktCashbackDtl();
    updateMktCashbackDtl();
//  insertCycFundDtl();

    processDisplay(10000); // every 10000 display message
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
  setValue("cash.tax_flag"             , "N");
  setValue("cash.mod_desc"             , "效期到期移除");
  setValue("cash.mod_memo"             , "效期到期日"+getValue("effect_e_date")); 
  setValue("cash.p_seqno"              , getValue("p_seqno"));
  setValue("cash.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("cash.acct_date"            , hBusiBusinessDate);
  setValue("cash.tran_pgm"             , javaProgram);
  setValue("cash.acct_month"           , getValue("wday.next_acct_month"));
  setValue("cash.proc_month"           , hBusiBusinessDate.substring(0,6));
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
            + "and   effect_e_date = ? "
            + "and   end_tran_amt > 0 ";

  setString(1 , sysDate);
  setString(2 , "移除序號["+tranSeqno+"]");
  setString(3 , tranSeqno);
  setString(4 , javaProgram);
  setString(5 , getValue("p_seqno"));
  setString(6 , getValue("fund_code"));
  setString(7 , getValue("effect_e_date"));


  updateTable();
  return;
 }
// ************************************************************************
 int insertCycFundDtl() throws Exception
 {
  extendField = "fund.";
  setValue("fund.create_date"          , sysDate);
  setValue("fund.create_time"          , sysTime);
  setValue("fund.business_date"        , hBusiBusinessDate);
  setValue("fund.p_seqno"              , getValue("p_seqno"));
  setValue("fund.acct_type"            , getValue("mcdl.acct_type",inti));
  setValue("fund.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("fund.execute_date"         , hBusiBusinessDate);
  setValue("fund.curr_code"            , "901");
  setValue("fund.fund_code"            , getValue("fund_code"));
  setValue("fund.tran_code"            , "6");
  setValue("fund.vouch_type"           , "3");
  setValue("fund.fund_amt"             , getValue("end_tran_amt"));
  setValue("fund.cd_kind"              , "A393");
  setValue("fund.memo1_type"           , "1");
  setValue("fund.src_pgm"              , javaProgram); 
  setValue("fund.proc_flag"            , "N");
  setValue("fund.mod_time"             , sysDate+sysTime);
  setValue("fund.mod_pgm"              , javaProgram);

  daoTable  = "cyc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

