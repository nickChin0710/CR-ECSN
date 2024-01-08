/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/03/19  V1.00.08  Allen Ho   cyc_c210                                   *
* 109/11/13  V1.00.09  Zuwei      naming rule                                *
* 109-12-22  V1.00.10  tanwei     updated for project coding standard        *                                                                           
* 112-05-29  V1.00.11  Simon      add cyc_pyaj.fund_code                     * 
* 112-10-02  V1.00.12  Holmes     cyc_dc_fund_dtl.mod_desc=現金回饋銷帳入帳日*                                                                          *
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
public class CycC210 extends AccessDAO
{
 private  String progname = "雙幣基金-外幣基金銷帳處理程式  112/10/02  V1.00.12";
 CommFunction comm = new CommFunction();
 CommDCFund   comd = null;
 CommRoutine  comr = null;

 String hBusiBusinessDate      = "";
 String tranSeqno                = "";

 int capTotCnt,capAdjCnt,feeTotCnt,feeAdjCnt,feeAnlCnt;
 int  printSeq=0;
  String tmpstr="",tmpstr1="";

 long    totalCnt=0,currCnt=0;
 int paymentAmt = 0,insertCnt=0,updateCnt=0,pcodCnt=0;


 boolean debug= false;

 double totRealPayAmt=0 ,totDcRealPayAmt=0;
 double dcPayOverAmt =0 ,payOverAmt=0;
 int cnt1=0;
 int n=0;
 int recCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycC210 proc = new CycC210();
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

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comd = new CommDCFund(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","本次關帳月份["+getValue("wday.this_acct_month")+"]");
   selectPtrActcode();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadActDebt3();
   loadActDebt();
   loadPtrCurrcode();
   loadCrdCard();
   loadActAcctCurr();
   loadPtrCurrRate();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 cyc_dc_fund_dtl"); 
   totalCnt=0;
   selectCycDcFundDtl();
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
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where next_close_date = ? ";

  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  selectCycDcFundDtl() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.curr_code,"
            + "a.fund_code,"
            + "max(a.acct_type) as acct_type,"
            + "max(a.fund_name) as fund_name,"
            + "max(a.id_p_seqno) as id_p_seqno,"
            + "sum(a.end_tran_amt) as fund_amt ";
  daoTable  = "cyc_dc_fund_dtl a,act_acno b";
  whereStr  = "where a.p_seqno = b.p_seqno "
//          + "and a.p_seqno = '0002029299' "
            + "and b.stmt_cycle = ? "
            + "GROUP BY a.p_seqno,a.curr_code,a.fund_code "
            + "HAVING sum(end_tran_amt) > 0 "
            ;

  setString(1 , getValue("wday.stmt_cycle"));
  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    setValue("card.p_seqno"   , getValue("p_seqno"));
    setValue("card.curr_code" , getValue("curr_code"));
    cnt1 = getLoadData("card.p_seqno,card.curr_code");
    if (cnt1==0) continue;

    setValue("deb3.p_seqno"   , getValue("p_seqno"));
    setValue("deb3.curr_code" , getValue("curr_code"));
    cnt1 = getLoadData("deb3.p_seqno,deb3.curr_code");
    if (cnt1==0) continue;

    if (debug)
       {
        showLogMessage("I","","STEP 1 p_seqno   : ["+getValue("p_seqno")+"]");
        showLogMessage("I","","       curr_code : ["+getValue("curr_code")+"]");
        showLogMessage("I","","       fund_amt  : ["+getValueDouble("fund_amt")+"]");
       }

    setValue("jrnl.jrnl_seqno"  , getSeqno("ecs_jrnlseq"));

    if (getValueInt("jrnl.enq_seqno") > 99900) 
       setValue("jrnl.enq_seqno" , "0");

    setValue("jrnl,order_seq" , "1");
    /*************************************************************/
    totRealPayAmt = totDcRealPayAmt = 0;

    dcPayOverAmt = getValueDouble("fund_amt");
          
    if ((getValueDouble("deb3.dc_end_bal")>0)&&(getValueDouble("deb3.end_bal") >0))
       {
        setValueDouble("rate.exchange_rate" , (getValueDouble("deb3.end_bal")
                                            / getValueDouble("deb3.dc_end_bal")*1.0)+0.0000001);
       }
    else
       {
        setValue("rate.curr_code" , getValue("curr_code"));
        cnt1 = getLoadData("rate.curr_code");
       }
    payOverAmt = commCurrAmt("901",(dcPayOverAmt * getValueDouble("rate.exchange_rate")),0);

    /*************************************************************/
    setValue("acur.p_seqno"   , getValue("p_seqno"));
    setValue("acur.curr_code" , getValue("curr_code"));
    cnt1 = getLoadData("acur.p_seqno,acur.curr_code");
    if (cnt1==0) continue;
    /*************************************************************/
    if (debug)
       {
        showLogMessage("I","","STEP 2 pay_over_amt    : ["+payOverAmt+"]");
        showLogMessage("I","","       dc_pay_over_amt : ["+dcPayOverAmt+"]");
        showLogMessage("I","","       acct_jrnl_bal   : ["+getValueDouble("acur.acct_jrnl_bal")+"]");
        showLogMessage("I","","       dc_acct_jrnl_bal: ["+getValueDouble("acur.dc_acct_jrnl_bal")+"]");
        showLogMessage("I","","       min_pay_bal     : ["+getValueDouble("acur.min_pay_bal")+"]");
        showLogMessage("I","","       dc_min_pay_bal  : ["+getValueDouble("acur.dc_min_pay_bal")+"]");
        showLogMessage("I","","       ttl_amt_bal     : ["+getValueDouble("acur.ttl_amt_bal")+"]");
        showLogMessage("I","","       dc_ttl_amt_bal  : ["+getValueDouble("acur.dc_ttl_amt_bal")+"]");
       }
    if (selectActDebt()!=0) continue;
    setValueDouble("cdfe.end_fund_amt" , dcPayOverAmt);
    /*************************************************************/
    insertCycPyaj();
    lastCurrData();
    updateActAcctCurr();
    updateActAcct();
    /*************************************************************/
    setValueDouble("cdfd.beg_tran_amt" , getValueDouble("fund_amt") 
                                       - getValueDouble("cdfe.end_fund_amt")); 
    if (getValueDouble("cdfd.beg_tran_amt")==0) continue;
    insertCycDcFundDtl();
    comd.dcfundFunc(tranSeqno);
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void insertCycPyaj() throws Exception
 {
  extendField = "pyaj.";

  setValue("pyaj.p_seqno"              , getValue("p_seqno"));
  setValue("pyaj.acct_type"            , getValue("acct_type"));
  setValue("pyaj.class_code"           , "B");
  setValue("pyaj.payment_date"         , hBusiBusinessDate);
  setValueDouble("pyaj.payment_amt"    , totRealPayAmt);
  setValue("pyaj.curr_code"            , getValue("curr_code"));
  setValueDouble("pyaj.dc_payment_amt" , totDcRealPayAmt);
  setValue("pyaj.payment_type"         , getValue("fund_code").substring(0,4));
  setValue("pyaj.fund_code"            , getValue("fund_code"));
  setValue("pyaj.fee_flag"             , "N");
  setValue("pyaj.stmt_cycle"           , getValue("wday.stmt_cycle"));
  setValue("pyaj.settle_flag"          , "U");
  setValue("pyaj.reference_no"         , getValue("pyaj.reference_no"));
  setValue("pyaj.crt_date"             , sysDate);
  setValue("pyaj.crt_user"             , javaProgram);
  setValue("pyaj.mod_pgm"              , javaProgram);
  setValue("pyaj.mod_time"             , sysDate+sysTime);

  daoTable    = "cyc_pyaj";
  insertTable();
  return;
}
// ************************************************************************
 int updateActAcctCurr() throws Exception
 {
   daoTable = "act_acct_curr";
   updateSQL = "acct_jrnl_bal           = ?,"
             + "dc_acct_jrnl_bal        = ?,"
             + "min_pay_bal             = ?,"
             + "dc_min_pay_bal          = ?,"
             + "ttl_amt_bal             = ?,"
             + "dc_ttl_amt_bal          = ?,"
             + "mod_time                = sysdate,"
             + "mod_pgm                 = ? ";
    whereStr = "where rowid = ? ";
   
    setDouble(1 , getValueDouble("acur.acct_jrnl_bal"));
    setDouble(2 , getValueDouble("acur.dc_acct_jrnl_bal"));
    setDouble(3 , getValueDouble("acur.min_pay_bal"));
    setDouble(4 , getValueDouble("acur.dc_min_pay_bal"));
    setDouble(5 , getValueDouble("acur.ttl_amt_bal"));
    setDouble(6 , getValueDouble("acur.dc_ttl_amt_bal"));
    setString(7 , javaProgram);
    setRowId(8  , getValue("acur.rowid"));


    int n = updateTable();

    if ( n == 0 )
       { showLogMessage("E","","update_act_acct_curr ERROR ");  }


   // UPDATE ACT_ACCT_CURR LOAD BUFFER
    setUpdateLoad("acur.acct_jrnl_bal"     , getValue("acur.acct_jrnl_bal"));
    setUpdateLoad("acur.dc_acct_jrnl_bal"  , getValue("acur.dc_acct_jrnl_bal"));
    setUpdateLoad("acur.min_pay_bal"       , getValue("acur.min_pay_bal"));
    setUpdateLoad("acur.dc_min_pay_bal"    , getValue("acur.dc_min_pay_bal"));
    setUpdateLoad("acur.ttl_amt_bal"       , getValue("acur.ttl_amt_bal"));
    setUpdateLoad("acur.dc_ttl_amt_bal"    , getValue("acur.dc_ttl_amt_bal"));

    setValue("acur.p_seqno"   , getValue("p_seqno"));
    setValue("acur.curr_code" , getValue("curr_code"));
    updateLoadTable("acur.p_seqno,acur.curr_code");

/*
    // 重 新讀 ACT_ACCT_CURR
    setValue("acur.p_seqno"   , getValue("p_seqno"));
    setValue("acur.curr_code" , getValue("curr_code"));
    getLoadData("acur.p_seqno,acur.curr_code");
*/

    return n;
 }
// ************************************************************************
 int updateActAcct() throws Exception
 {
  setValue("acur.p_seqno"   , getValue("p_seqno"));
  cnt1 = getLoadData("acur.p_seqno");

  setValueDouble("acct.acct_jrnl_bal" , 0);
  setValueDouble("acct.min_pay_bal"   , 0);
  setValueDouble("acct.ttl_amt_bal"   , 0);
  for (int inti=0;inti<cnt1;inti++)
    {
     setValueDouble("acct.acct_jrnl_bal" , getValueDouble("acct.acct_jrnl_bal")
                                         + getValueDouble("acur.acct_jrnl_bal",inti));
     setValueDouble("acct.min_pay_bal"   , getValueDouble("acct.min_pay_bal")
                                         + getValueDouble("acur.min_pay_bal",inti));
     setValueDouble("acct.ttl_amt_bal"   , getValueDouble("acct.ttl_amt_bal")
                                         + getValueDouble("acur.ttl_amt_bal",inti));
    }

  daoTable  =  "act_acct";
  updateSQL =  "acct_jrnl_bal         = ?,"
            +  "min_pay_bal           = ?,"
            +  "ttl_amt_bal           = ?,"
            +  "mod_time              = sysdate,"
            +  "mod_pgm               = ? ";
  whereStr = " where  p_seqno        = ? ";

  setDouble(1 , getValueDouble("acct.acct_jrnl_bal"));
  setDouble(2 , getValueDouble("acct.min_pay_bal"));
  setDouble(3 , getValueDouble("acct.ttl_amt_bal"));
  setString(4 , javaProgram);
  setString(5 , getValue("p_seqno"));

  int n = updateTable();

  if ( n == 0 )
     { showLogMessage("E","","update_act_acct ERROR ");  }

  return n;
}
// ************************************************************************
 int insertCycDcFundDtl() throws Exception
 {
  tranSeqno     = comr.getSeqno("ecs_dbmseq");
  extendField = "fund.";
  dateTime();
  setValue("fund.tran_date"            , sysDate);
  setValue("fund.tran_time"            , sysTime);
  setValue("fund.curr_code"            , getValue("curr_code"));
  setValue("fund.acct_type"            , getValue("acct_type"));
  setValue("fund.tran_seqno"           , tranSeqno);
  setValue("fund.fund_code"            , getValue("fund_code"));
  setValue("fund.p_seqno"              , getValue("p_seqno"));
  setValue("fund.tran_code"            , "4");
  setValue("fund.id_p_seqno"           , getValue("id_p_seqno"));

  setValue("fund.fund_name"            , getValue("fund_name"));
  //setValue("fund.active_name"          , "基金銷帳:"+hBusiBusinessDate);
  setValue("fund.mod_desc"             , "現金回饋銷帳入帳日:"+hBusiBusinessDate);
  setValueDouble("fund.beg_tran_amt"   , getValueDouble("cdfd.beg_tran_amt")*-1);
  setValueDouble("fund.end_tran_amt"   , getValueDouble("cdfd.beg_tran_amt")*-1);

  setValue("fund.acct_date"            , hBusiBusinessDate);
  setValue("fund.tran_pgm"             , javaProgram);
  setValue("fund.apr_flag"             , "Y");
  setValue("fund.apr_user"             , javaProgram);
  setValue("fund.apr_date"             , sysDate);
  setValue("fund.crt_user"             , javaProgram);
  setValue("fund.crt_date"             , sysDate);
  setValue("fund.mod_user"             , javaProgram);
  setValue("fund.mod_time"             , sysDate+sysTime);
  setValue("fund.mod_pgm"              , javaProgram);
  daoTable  = "cyc_dc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteActAcag() throws Exception
 {
  daoTable  = "act_acag";
  whereStr  = "WHERE  p_seqno    = ? "
            + "and    pay_amt    = 0 ";

  setString(1, getValue("p_seqno"));

  deleteTable();

  return;
 }
// ************************************************************************
 int selectActDebt() throws Exception
 {
  setValue("debt.p_seqno"   , getValue("p_seqno"));
  setValue("debt.curr_code" , getValue("curr_code"));
  int cnt1 = getLoadData("debt.p_seqno,debt.curr_code");
  if (cnt1==0) return(1);

    if (debug)
        showLogMessage("I","","STEP 3 debt cnt      : ["+ cnt1 +"]");

  for (int inti=0;inti<cnt1;inti++)
    {
    if (getValueDouble("debt.end_bal",inti)+
        getValueDouble("debt.dc_end_bal",inti)==0) continue;

    if (debug)
       {
        showLogMessage("I","","STEP 4 dc_pay_over_amt  : ["+ dcPayOverAmt +"]");
        showLogMessage("I","","       end_bal       : ["+ getValueDouble("debt.end_bal",inti) +"]");
        showLogMessage("I","","       dc_end_bal       : ["+ getValueDouble("debt.dc_end_bal",inti) +"]");
       }

     if (dcPayOverAmt >= getValueDouble("debt.dc_end_bal",inti))
        {
         setValue("jrnl.transaction_amt"     , getValue("debt.end_bal",inti));
         setValue("jrnl.dc_transaction_amt"  , getValue("debt.dc_end_bal",inti));
    if (debug)
       {
        showLogMessage("I","","STEP 5 jrnl.transaction_amt    :"+ getValue("jrnl.transaction_amt") +"]");
        showLogMessage("I","","       jrnl.dc_transaction_amt : "+ getValue("jrnl.dc_transaction_amt") +"]");
       }
          
         dcPayOverAmt         = commCurrAmt(getValue("curr_code"),
                                   dcPayOverAmt 
                                 - getValueDouble("debt.dc_end_bal",inti),0);

         payOverAmt            = payOverAmt    - getValueDouble("debt.end_bal",inti);
         if ((payOverAmt<0)||(dcPayOverAmt==0)) payOverAmt = 0;
         setValue("debt.end_bal"    , "0"  ,inti);
         setValue("debt.dc_end_bal" , "0"  ,inti);
        }
     else
        {
         if (!getValue("pcod.part_rev").equals("Y")) continue;

         setValueDouble("debt.dc_end_bal" , commCurrAmt(getValue("curr_code"), 
                                            getValueDouble("debt.dc_end_bal",inti)
                                          - dcPayOverAmt,0),inti);
         setValueDouble("jrnl.dc_transaction_amt"  , dcPayOverAmt);
          
         setValueDouble("jrnl.transaction_amt" , commCurrAmt(getValue("curr_code"), 
                                                 getValueDouble("debt.end_bal",inti)
                                               -  commCurrAmt("901",getValueDouble("debt.dc_end_bal",inti)
                                               * (getValueDouble("debt.beg_bal",inti)
                                               /  getValueDouble("debt.dc_beg_bal",inti)),0),0));
    if (debug)
       {
        showLogMessage("I","","STEP 6 jrnl.transaction_amt    : ["+ getValue("jrnl.transaction_amt") +"]");
        showLogMessage("I","","       jrnl.dc_transaction_amt : ["+ getValue("jrnl.dc_transaction_amt") +"]");
       }

         if (getValueDouble("jrnl.dc_transaction_amt") ==0) 
            setValue("jrnl.transaction_amt"  , "0");

         setValueDouble("debt.end_bal" ,  commCurrAmt("901",getValueDouble("debt.dc_end_bal",inti)
                                       * (getValueDouble("debt.beg_bal",inti)
                                       /  getValueDouble("debt.dc_beg_bal",inti)),0),inti);

         if (getValueDouble("debt.dc_end_bal",inti)==0)
            setValueDouble("debt.end_bal"    , 0  ,inti);
         dcPayOverAmt            = 0;
         payOverAmt               = 0;
        }
      totDcRealPayAmt = commCurrAmt(getValue("curr_code"), 
                            totDcRealPayAmt 
                          + getValueDouble("jrnl.dc_transaction_amt"),0);
      totRealPayAmt    = totRealPayAmt + getValueDouble("jrnl.transaction_amt");

    if (debug)
       {
        showLogMessage("I","","STEP 7 tot_dc_real_pay_amt  : ["+ totDcRealPayAmt +"]");
        showLogMessage("I","","       tot_real_pay_amt     : ["+ totRealPayAmt +"]");
       }

     /*********************** 會計分錄資料獨力程式處理 ************/
     insertCycVouchData(inti);
     /***************** 沖銷金額寫入 act_jrnl ***********************/
     insertActJrnl(inti);
     updateActDebt(inti);

     setValue("pyaj.reference_no"   , getValue("debt.reference_no",inti));
    if (debug)
       {
        showLogMessage("I","","STEP 8 reference_no         : ["+ getValue("debt.reference_no",inti) +"]");
        showLogMessage("I","","       end_bal       : ["+ getValueDouble("debt.end_bal",inti) +"]");
        showLogMessage("I","","       dc_end_bal       : ["+ getValueDouble("debt.dc_end_bal",inti) +"]");
       }

     if (dcPayOverAmt <= 0) break;
    }
  return(0);
 }
// ************************************************************************
 void insertCycVouchData(int inti) throws Exception
 {
  daoTable    = "cyc_vouch_data";

  setValue("create_date"     , sysDate);
  setValue("create_time"     , sysTime);
  setValue("p_seqno"         , getValue("p_seqno"));
  setValue("curr_code"       , getValue("curr_code"));
  setValue("acct_type"       , getValue("acct_type"));
  setValue("business_date"   , hBusiBusinessDate);
  setValue("payment_type"    , getValue("fund_code").substring(0,4));
  setValue("reference_seq"   , getValue("debt.reference_no",inti));

  setValue("vouch_data_type" , "4");
  setValue("acct_code"       , "BL");

  setValueDouble("vouch_amt"    , getValueDouble("jrnl.dc_transaction_amt"));
  setValueDouble("d_vouch_amt"  , getValueDouble("jrnl.dc_transaction_amt"));

  setValue("proc_flag"      , "N");
  setValue("src_pgm"        , javaProgram);
  setValue("mod_pgm"        , javaProgram);
  setValue("mod_time"       , sysDate+sysTime);

  insertTable();
  return;
}
// ************************************************************************
 int insertActJrnl(int inti) throws Exception
 {
  dateTime();

  setValueInt("enq_seqno"             , getValueInt("jrnl.enq_seqno")+1);

  setValue("p_seqno"                  , getValue("p_seqno"));
  setValue("curr_code"                , getValue("curr_code"));
  setValue("acct_type"                , getValue("acct_type"));
  setValue("acct_date"                , hBusiBusinessDate);
  setValue("id_p_seqno"               , getValue("id_p_seqno"));
  setValue("corp_p_seqno"             , getValue("card.corp_p_seqno"));

  setValue("tran_type"                , getValue("fund_code").substring(0,4));
  setValue("tran_class"               , "P");
  setValue("item_ename"               , "PY");
  setValue("dr_cr"                    , "D");

  setValue("item_d_bal"               , getValue("debt.d_avail_bal",inti)); 
  setValue("dc_item_d_bal"            , getValue("debt.dc_d_avail_bal",inti)); 
  setValueDouble("can_by_fund_bal"    , getValueDouble("debt.can_by_fund_bal",inti)
                                      + getValueDouble("debt.end_bal",inti));
  setValueDouble("dc_can_by_fund_bal" , getValueDouble("debt.dc_can_by_fund_bal",inti)
                                      + getValueDouble("debt.dc_end_bal",inti));

  setValue("transaction_amt"          , getValue("jrnl.transaction_amt"));
  setValue("dc_transaction_amt"       , getValue("jrnl.dc_transaction_amt"));

  setValueDouble("jrnl_bal"           , getValueDouble("acur.acct_jrnl_bal")
                                      - totRealPayAmt);
  setValueDouble("dc_jrnl_bal"        , commCurrAmt(getValue("curr_code"),
                                        getValueDouble("acur.dc_acct_jrnl_bal")
                                      - totDcRealPayAmt,0));

  setValue("item_bal"                 , getValue("debt.end_bal",inti));
  setValue("dc_item_bal"              , getValue("debt.dc_end_bal",inti));

  setValue("item_date"                , getValue("debt.post_date",inti));
  setValue("interest_date"            , getValue("debt.interest_date",inti));
  setValue("reference_no"             , getValue("debt.reference_no",inti));
  setValue("stmt_cycle"               , getValue("wday.stmt_cycle"));
  setValueInt("order_seq"             , getValueInt("jrnl.order_seq")+1);
  setValue("jrnl_seqno"               , getValue("jrnl.jrnl_seqno"));
  setValue("crt_date"                 , sysDate);
  setValue("crt_time"                 , sysTime);
  setValue("mod_time"                 , sysDate+sysTime);
  setValue("mod_pgm"                  , javaProgram);

  daoTable  = "act_jrnl";
  insertTable();

  return(0);
 }
// ************************************************************************
 int updateActDebt(int inti) throws Exception
 {
  daoTable = "act_debt";
  updateSQL = "end_bal                 = ?,"
            + "dc_end_bal              = ?,"
            + "can_by_fund_bal         = can_by_fund_bal - end_bal + ?,"
            + "dc_can_by_fund_bal      = dc_can_by_fund_bal - dc_end_bal + ?,"
            + "mod_time                = sysdate,"
            + "mod_pgm                 = ? ";
  whereStr = "where rowid = ? ";

  setDouble(1  , getValueDouble("debt.end_bal",inti));
  setDouble(2  , getValueDouble("debt.dc_end_bal",inti));
  setDouble(3  , getValueDouble("debt.end_bal",inti));
  setDouble(4  , getValueDouble("debt.dc_end_bal",inti));
  setString(5  , javaProgram);
  setRowId(6   , getValue("debt.rowid",inti));

  int n = updateTable();
  if ( n == 0 )
     { showLogMessage("E","","update_act_debt ERROR ");  }

  /* UPDATE ACT_DEBT LOAD BUFFER */
  setValue("debt.p_seqno"   , getValue("p_seqno"));
  setValue("debt.curr_code" , getValue("curr_code"));
  setUpdateLoad("debt.end_bal"     , getValue("debt.end_bal",inti));
  setUpdateLoad("debt.dc_end_bal"  , getValue("debt.dc_end_bal",inti));
  updateLoadTable("debt.p_seqno,debt.curr_code",inti);

  return n;
 }
// ************************************************************************
 int selectActAcagCurr()  throws Exception
 {
  double tempDcDouble = getValueDouble("acur.dc_min_pay_bal");
    if (debug)
       {
        showLogMessage("I","","STEP C curr_code        : ["+ getValue("curr_code") +"]");
        showLogMessage("I","","       dc_end_bal       : ["+ getValueDouble("acur.dc_min_pay_bal") +"]");
       }
  setValue("acur.min_pay_bal","0");

  daoTable    = "act_acag_curr";
  extendField = "aacr.";
  selectSQL   = "curr_code,"
              + "acct_month,"
              + "pay_amt,"
              + "dc_pay_amt,"
              + "rowid as rowid ";
  whereStr    = "where p_seqno   = ? "
              + "and   curr_code = ? "
              + "order by acct_month desc";

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("curr_code"));

  int recCnt = selectTable();

  if ( recCnt == 0 ) return 0; 

  for (int inti=0; inti<recCnt; inti++)   
     {
    if (debug)
       {
        showLogMessage("I","","STEP C1 temp_dc_double  : ["+ tempDcDouble +"]");
        showLogMessage("I","","        dc_pay_amt      : ["+ getValueDouble("aacr.dc_pay_amt",inti) +"]");
       }
      if ((getValueLong("aacr.dc_pay_amt",inti)==0)||
          (tempDcDouble==0))
         {
           deleteActAcagCurr(inti);
           updateActAcag(inti);
           continue;
         }

      if (tempDcDouble >= getValueDouble("aacr.dc_pay_amt",inti) )
         {
          tempDcDouble = commCurrAmt(getValue("curr_code"),
                           tempDcDouble 
                         - getValueDouble("aacr.dc_pay_amt",inti),0);
          setValueDouble("acur.min_pay_bal" , getValueDouble("acur.min_pay_bal") 
                                            + getValueDouble("aacr.pay_amt",inti));
          continue;
         }
      setValueDouble("aacr.pay_amt" , commCurrAmt("901",
                                      tempDcDouble 
                                    * (getValueDouble("aacr.pay_amt",inti)
                                    /  getValueDouble("aacr.dc_pay_amt",inti)),0),inti);

      setValueDouble("acur.min_pay_bal", getValueDouble("acur.min_pay_bal") 
                                       + getValueDouble("aacr.pay_amt",inti));

      setValueDouble("aacr.dc_pay_amt", commCurrAmt(getValue("curr_code"), 
                                        tempDcDouble,0),inti);

      if (getValue("curr_code").equals("901"))
          setValue("aacr.pay_amt", getValue("aacr.dc_pay_amt",inti) ,inti); 

      if (getValueDouble("aacr.dc_pay_amt",inti) == 0 )
          setValue("aacr.pay_amt","0",inti); 

      tempDcDouble = 0;

      if ( getValueDouble("aacr.dc_pay_amt",inti) <= 0  )
         { deleteActAcagCurr(inti); }
      else
         { updateActAcagCurr(inti); }

      updateActAcag(inti);
     }

  return 0;
 }
// ************************************************************************
 int deleteActAcagCurr(int inti) throws Exception
 {
   daoTable = "act_acag_curr";
   whereStr = "where rowid = ? ";

   setRowId(1,getValue("aacr.rowid",inti));

   int recCnt = deleteTable();

   return recCnt;
 }
// ************************************************************************
 int updateActAcag(int inti) throws Exception
 {
  updateSQL = "pay_amt = (select nvl(sum(pay_amt),0) "
            + "           from   act_acag_curr "
            + "           where  p_seqno    = a.p_seqno "
            + "           and    acct_month = a.acct_month) ";
  daoTable  = "act_acag a";
  whereStr  = "WHERE   p_seqno    = ? "
            + "AND     acct_month = ? ";

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("aacr.acct_month",inti));

  updateTable();

  if ( notFound.equals("Y") ) insertActAcag1(inti);

  return(0);
 }
// ************************************************************************
 int insertActAcag1(int inti) throws Exception
 {
  sqlCmd   = "insert into act_acag( "
           + "       p_seqno,"
           + "       seq_no,"
           + "       acct_type,"
           + "       acct_month,"
           + "       stmt_cycle,"
           + "       pay_amt,"
           + "       mod_time,"
           + "       mod_pgm"
           + "       ) "
           + "select p_seqno, "
           + "       max(1),"
           + "       max(acct_type),"
           + "       acct_month,"
           + "       max(stmt_cycle),"
           + "       sum(pay_amt),"
           + "       max(sysdate),"
           + "       max('"+javaProgram+"') "
           + "FROM   act_acag_curr "
           + "WHERE  p_seqno    = ? "
           + "AND    acct_month = ? "
           + "GROUP BY p_seqno,acct_month "
           ;
  daoTable  = "act_acag";

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("aacr.acct_month",inti));

  insertTable();

  sqlCmd   = "";

  return(0);
 }
// ************************************************************************
 int updateActAcagCurr(int inti) throws Exception
 {
  daoTable  = "act_acag_curr";
  updateSQL = "pay_amt    = ?,"
            + "dc_pay_amt = ?";
  whereStr  = "WHERE  rowid = ? ";

  setDouble(1 , getValueDouble("aacr.pay_amt",inti));
  setDouble(2 , getValueDouble("aacr.dc_pay_amt",inti));
  setRowId(3  , getValue("aacr.rowid",inti));

  int recCnt = updateTable();
  if ( recCnt == 0 )
     { showLogMessage("E","","update_act_acag_curr ERROR");  }

  return recCnt;
 }
// ************************************************************************
 void lastCurrData() throws Exception 
 {
  setValueDouble("acur.dc_acct_jrnl_bal" , commCurrAmt(getValue("curr_code"),
                                           getValueDouble("acur.dc_acct_jrnl_bal") 
                                         - totDcRealPayAmt,0));

  setValueDouble("acur.acct_jrnl_bal" , getValueDouble("acur.acct_jrnl_bal")
                                       - totRealPayAmt);

  if (getValueDouble("acur.dc_acct_jrnl_bal")==0) 
     setValue("acur.acct_jrnl_bal" , "0");

  setValueDouble("acur.dc_ttl_amt_bal" , commCurrAmt(getValue("curr_code"),
                                         getValueDouble("acur.dc_ttl_amt_bal") 
                                       - totDcRealPayAmt,0)); 
  setValueDouble("acur.ttl_amt_bal"    , getValueDouble("acur.ttl_amt_bal") 
                                       - totRealPayAmt);

   if (getValueDouble("acur.dc_ttl_amt_bal") <= 0)          
      {
       setValue("acur.ttl_amt_bal"    , "0");
       setValue("acur.dc_ttl_amt_bal" , "0");
      }

  setValueDouble("acur.dc_min_pay_bal" , commCurrAmt(getValue("curr_code"), 
                                         getValueDouble("acur.dc_min_pay_bal") 
                                       - totDcRealPayAmt,0));
   
  if (getValueDouble("acur.dc_min_pay_bal")<0) 
     setValueDouble("acur.dc_min_pay_bal" , 0);

  if (getValueDouble("acur.dc_ttl_amt_bal")==0) 
     setValueDouble("acur.dc_min_pay_bal" , 0);

  selectActAcagCurr();
  deleteActAcag();
 }
// ************************************************************************
 String  getSeqno(String seqName) throws Exception
 {
  selectSQL = "NEXTVAL FOR " + seqName + " AS MOD_SEQNO " ;
  daoTable  = "SYSIBM.SYSDUMMY1";
  whereStr  = "";
  selectTable();

  String output = String.format("%012.0f",getValueDouble("MOD_SEQNO"));
  while (output.length() < 12) output = "0" + output;

  return output;
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "corp_p_seqno";
  daoTable  = "crd_card";
  whereStr  = "where curr_code != '901' "
            + "and   stmt_cycle = ? "
            + "and   current_code = '0' "
            + "and   card_no = major_card_no "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  int  n = loadTable();

  setLoadData("card.p_seqno,card.curr_code");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActDebt3() throws Exception
 {
  extendField = "deb3.";
  selectSQL = "c.p_seqno,"
            + "c.curr_code,"
            + "sum(c.end_bal) as end_bal,"
            + "sum(c.dc_end_bal) as dc_end_bal"
            ;
  daoTable  = "act_debt c, "
            + "      (select a.p_seqno,a.curr_code "
            + "       from cyc_dc_fund_dtl a,act_acno b "
            + "       where   a.p_seqno    = b.p_seqno "
            + "       and     b.stmt_cycle = '"+getValue("wday.stmt_cycle")+"' "
            + "       GROUP BY a.p_seqno,a.curr_code "
            + "       HAVING sum(end_tran_amt) > 0) d ";
  whereStr  = "where c.curr_code != '901' "
            + "and   c.end_bal + c.dc_end_bal > 0 "
            + "and   c.p_seqno    = d.p_seqno "
            + "and   c.curr_code  = d.curr_code "
            + "group by c.p_seqno,c.curr_code "
            ;

  int  n = loadTable();

  setLoadData("deb3.p_seqno,deb3.curr_code");

  showLogMessage("I","","Load act_debt_3 Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActDebt() throws Exception
 {
  extendField = "debt.";
  selectSQL = "c.p_seqno,"
            + "c.curr_code,"
            + "c.beg_bal,"
            + "c.dc_beg_bal,"
            + "c.end_bal,"
            + "c.dc_end_bal,"
            + "c.reference_no,"
            + "c.d_avail_bal,"
            + "c.dc_d_avail_bal,"
            + "(can_by_fund_bal - end_bal) as can_by_fund_bal,"
            + "(dc_can_by_fund_bal - dc_end_bal) as dc_can_by_fund_bal,"
            + "post_date,"
            + "interest_date,"
            + "c.rowid as rowid";
  daoTable  = "act_debt c, "
            + "      (select a.p_seqno,a.curr_code "
            + "       from cyc_dc_fund_dtl a,act_acno b "
            + "       where   a.p_seqno    = b.p_seqno "
            + "       and     b.stmt_cycle = '"+getValue("wday.stmt_cycle")+"' "
            + "       GROUP BY a.p_seqno,a.curr_code "
            + "       HAVING sum(end_tran_amt) > 0) d ";
  whereStr  = "where c.curr_code != '901' "
            + "and   c.end_bal + c.dc_end_bal > 0 "
            + "and   c.acct_code  = 'BL' "
            + "and   c.acct_month = ? "
            + "and   c.dc_end_bal > 0 "
            + "and   c.p_seqno    = d.p_seqno "
            + "and   c.curr_code  = d.curr_code "
            + "order by c.p_seqno,c.curr_code "
            ;
  setString(1 , getValue("wday.next_acct_month"));

  int  n = loadTable();

  setLoadData("debt.p_seqno,debt.curr_code");

  showLogMessage("I","","Load act_debt Count: ["+n+"]");
 }
// ************************************************************************
 void  loadPtrCurrRate() throws Exception
 {
  extendField = "rate.";
  selectSQL = "exchange_rate,"
            + "curr_code";
  daoTable  = "ptr_curr_rate";
  whereStr  = "";

  int  n = loadTable();

  setLoadData("rate.curr_code");

  showLogMessage("I","","Load ptr_curr_rate Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActAcctCurr() throws Exception
 {
  extendField = "acur.";
  selectSQL = "c.p_seqno,"
            + "c.curr_code,"
            + "c.acct_jrnl_bal,"
            + "c.dc_acct_jrnl_bal,"
            + "c.min_pay_bal,"
            + "c.dc_min_pay_bal,"
            + "c.ttl_amt_bal,"
            + "c.dc_ttl_amt_bal,"
            + "c.rowid as rowid";
  daoTable  = "act_acct_curr c, "
            + "      (select a.p_seqno "
            + "       from cyc_dc_fund_dtl a,act_acno b "
            + "       where   a.p_seqno    = b.p_seqno "
            + "       and     b.stmt_cycle = '"+getValue("wday.stmt_cycle")+"' "
            + "       GROUP BY a.p_seqno"
            + "       HAVING sum(end_tran_amt) > 0) d ";
  whereStr  = "where c.p_Seqno = d.p_seqno "
            ;

  int  n = loadTable();

  setLoadData("acur.p_seqno,acur.curr_code");
  setLoadData("acur.p_seqno");

  showLogMessage("I","","Load act_acct_curr Count: ["+n+"]");
 }
// ************************************************************************
 double  commCurrAmt(String currCode,double val,int rnd) throws Exception
 {

  setValue("pcde.curr_code" , currCode);
  int cnt1 = getLoadData("pcde.curr_code");

  val = val * 10000.0;
  val = Math.round(val);

//showLogMessage("I","","STEP 2001 ["+ val + "]");

  BigDecimal currAmt = new BigDecimal(val).divide(new BigDecimal("10000"));

//showLogMessage("I","","STEP 2002 ["+ curr_amt + "]");

  if (cnt1==0) return(currAmt.doubleValue());

  double retNum = 0.0;
  if (rnd>0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_UP).doubleValue(); 
  if (rnd==0) retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_HALF_UP).doubleValue(); 
  if (rnd<0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_DOWN).doubleValue(); 

//showLogMessage("I","","STEP 2003 ["+ retNum + "]");

  return(retNum);
 }
// ************************************************************************
 void  loadPtrCurrcode() throws Exception
 {
  extendField = "pcde.";
  selectSQL = "curr_code,"
            + "curr_amt_dp";
  daoTable  = "ptr_currcode";
  whereStr  = "";

  int  n = loadTable();

  setLoadData("pcde.curr_code");

  showLogMessage("I","","Load ptr_currcode Count: ["+n+"]");
 }
// ************************************************************************
 void selectPtrActcode() throws Exception
 {
  extendField = "pcod.";
  selectSQL = "part_rev,"
            + "inter_rate_code,"
            + "inter_rate_code2,"
            + "revolve,"
            + "interest_method";
  daoTable  = "ptr_actcode";
  whereStr  = "where acct_code = 'BL' ";

  int recordCnt = selectTable();
 }
// ************************************************************************ 

}  // End of class FetchSample

