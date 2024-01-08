/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/12/17  V1.00.03  Allen Ho   cyc_C240  may be no need include in on;ine *
* 109/11/13  V1.00.04  Zuwei      naming rule                                *
*                                                                            *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycC240 extends AccessDAO
{
 private  String progname = "雙幣卡-人工異動會計分錄資料處理程式 109/11/13 V1.00.04";
 CommFunction comm = new CommFunction();
 CommDCFund comDCF = null;

 String hBusiBusinessDate   = "";
 String tranSeqno="";

 long    totalCnt=0,updateCnt=0;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycC240 proc = new CycC240();
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

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 )
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) 
       return(1);
   comDCF = new CommDCFund(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理人工異動資料");
   selectCycDcFundDtl();
   showLogMessage("I","","=========================================");

   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"] 筆");

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
 public void  selectCycDcFundDtl() throws Exception
 {
  selectSQL = "a.fund_code,"
            + "a.tran_seqno,"
            + "a.tran_date,"
            + "a.curr_code,"
            + "a.p_seqno,"
            + "a.id_p_seqno,"
            + "a.acct_type,"
            + "a.beg_tran_amt,"
            + "a.fund_name";
  daoTable  = "cyc_dc_fund_dtl a";
  whereStr  = "where acct_date  = ? "
            + "and   tran_code = '3' "
            ;

  setString(1, hBusiBusinessDate);
  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    if (getValue("fund_code").length()==0)
       {
        selectCycDcFundParm(); 
        showLogMessage("I","","p_seqno =["+getValue("p_seqno")+"] tran_seqno["+ getValue("tran_Seqno")+"]");
       }

    insertCycVouchData();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
public int insertCycVouchData() throws Exception
 {
  setValue("create_date"          , sysDate);
  setValue("create_time"          , sysTime);
  setValue("business_date"        , hBusiBusinessDate);
  setValue("curr_code"            , getValue("curr_code"));
  setValue("p_seqno"              , getValue("p_seqno"));
  setValue("acct_type"            , getValue("acct_type")); 
  setValueInt("vouch_amt"         , getValueInt("beg_tran_amt"));
  setValueInt("d_vouch_amt"       , getValueInt("beg_tran_amt"));
  setValue("vouch_data_type"      , "3");
  setValue("payment_type"         , getValue("fund_code").substring(0,4)); 
  setValue("src_pgm"              , javaProgram); 
  setValue("proc_flag"            , "N");
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);

  daoTable  = "cyc_vouch_data";

  insertTable();

  return(0);
 }
// ************************************************************************
 void  selectCycDcFundParm() throws Exception
 {
  extendField = "parm.";
  daoTable  = "cyc_dc_fund_parm";
  whereStr  = "where curr_code = ? "
            + "and   fund_crt_date_s <= ? "
            + "order by fund_crt_date_s desc "
            + "FETCH FIRST 1 ROW ONLY";

  setString(1, getValue("curr_code"));
  setString(2, getValue("tran_date"));

  int recordCnt = selectTable();

  setValue("fund_code" , getValue("parm.fund_code",0));
 }
// ************************************************************************ 
}  // End of class FetchSample

