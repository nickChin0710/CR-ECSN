/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/04/20  V1.00.26  Allen Ho   cyc_a140 PROD compare OK                   *
* 110/10/29  V1.01.01  Allen Ho   ptr_curr_general limit mark 8969           *
* 111/04/07  V1.02.01  Allen Ho   mantis 9335                                *
* 111-10-18  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111/12/19  V1.00.04  Simon      no update of ptr_disc from ui              *
* 112/02/27  V1.00.05  Simon      已無代償主檔，取消讀取代償主檔             *
* 112/02/28  V1.00.06  Simon      revolve_rate_s_month null handle           *
* 112/03/06  V1.00.07  Simon      merc.trans_flag='Y' changed to calculate interest*
*                                 interest[cacr.interest_amt] rounded_half_up to integer*
* 112-06-21  V1.00.08  Simon      1.上期待繳總額（負數代表溢繳金額）-> 上期金額*
*                                 2.本期總應繳款 -> 本期應繳總額             *
* 112-07-19  V1.00.09  Simon      1.tunning loadtable from act_penalty_log   *
*                                 2.revised loop cnt in selectActPenaltyLog()*
* 112-12-08  V1.00.10  Simon      initialize temp.org_pn_flag、temp.act_pn_flag*
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
public class CycA070 extends AccessDAO
{
 private final String PROGNAME = "關帳-計算利息及違約金處理程式 112-12-08  V1.00.10";
 CommFunction comm = new CommFunction();
 CommDCFund   comd = null;
 CommRoutine  comr = null;

 String business_date = "";
 String pSeqno       = "";

 int capTotCnt,capAdjCnt,feeTotCnt,feeAdjCnt,feeAnlCnt;
 int  printSeq=0;
 String tmpstr="",tmpstr1="";

 long    totalCnt=0,currCnt=0;
 boolean DEBUG = false;
 boolean DEBUGCMT = false;
 boolean DEBUGR = false;
 int paymentAmt = 0,insertCnt=0,updateCnt=0,pcodCnt=0;

 double totRealPayAmt=0 ,totDcRealPayAmt=0;
 double dcPayOverAmt =0 ,payOverAmt=0;
 double[] tempRevolvingInterest = new double[10];
 double   fitRevolvingRate1 = 0;

 int tempMCode = 0;
 int cnt1=0;
 int debtFlag=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA070 proc = new CycA070();
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
   showLogMessage("I","",javaProgram+" "+PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [p_seqno]");
       return(1);
      }

   if ( args.length >= 1 )
      business_date = args[0]; 

   if ( args.length == 2 )
      {
       pSeqno = args[1];
       DEBUG = true;
      }
   
   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","this_acct_month["+getValue("wday.this_acct_month")+"]");

   if (pSeqno.length()>0)
     {
      showLogMessage("I","","=========================================");
      showLogMessage("I","","DEBUG MODE 清檔開始...");
      showLogMessage("I","","   刪除 act_intr");
      deleteActIntrDebug();
      showLogMessage("I","","Total process records["+totalCnt+"]");
      showLogMessage("I","","   刪除 bil_adiexp");
      deleteBilAdiexpDebug();
      showLogMessage("I","","Total process records["+totalCnt+"]");
      showLogMessage("I","","   刪除 act_penalty_log");
      deleteActPenaltyLogDebug();
      showLogMessage("I","","Total process records["+totalCnt+"]");
      showLogMessage("I","","   刪除 bil_sysexp");
      deleteBilSysexpDebug();
      showLogMessage("I","","Total process records["+totalCnt+"]");
      showLogMessage("I","","   刪除 cyc_acmm");
      deleteCycAbemDebug();
      showLogMessage("I","","Total process records["+totalCnt+"]");
      showLogMessage("I","","   刪除 act_debt,bil_bill,bil_sysexp_hst,act_jrml");
      totalCnt=0;
      procBilSysexpHstDebug();
      showLogMessage("I","","Total process records["+totalCnt+"]");
      showLogMessage("I","","   刪除 adi");
      totalCnt=0;
      procAdiDebug();
      showLogMessage("I","","Total process records["+totalCnt+"]");
      if (!DEBUGCMT) commitDataBase();;
      showLogMessage("I","","DEBUG MODE 清檔完成");
      showLogMessage("I","","=========================================");
     }
    
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔...");
   displayMemory();
   loadPtrAcctType();
   loadCycAcmm();
   loadActAcctCurr();
   loadBilContract();
 //loadActCommute();
   loadActPenaltyLog();
   loadPtrCurrcode();
   loadBilMerchant();   //  must put to last
// load_ptr_curr_general();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料...");
   selectPtrDisc();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","計息 cyc_acmm 開始");
   totalCnt=0;
   selectActAcno();
   showLogMessage("I","","累計處理筆數["+totalCnt+"]");
   showLogMessage("I","","=========================================");
    
   if (DEBUGCMT) return(0);

   if (pSeqno.length()==0) finalProcess();
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

  if (business_date.length()==0)
      business_date   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+business_date+"]");
 }
// ************************************************************************ 
 int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,business_date);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  selectActAcno() throws Exception
 {
  selectSQL = "a.p_seqno as p_seqno,"
            + "a.acct_type,"
            + "b.acct_key,"
            + "b.acct_status,"
            + "b.corp_p_seqno,"
            + "b.id_p_seqno,"
            + "b.rc_use_b_adj,"
            + "b.rc_use_bs_date,"
            + "b.rc_use_be_date,"
            + "b.rc_use_indicator,"
            + "b.rc_use_s_date,"
            + "b.rc_use_e_date,"
            + "b.line_of_credit_amt,"
            + "b.revolve_int_sign,"
            + "decode(b.revolve_int_sign,'+',b.revolve_int_rate,"
            + "                              b.revolve_int_rate*-1) as revolve_int_rate,"
            + "b.revolve_rate_s_month,"
          //+ "b.revolve_rate_e_month,"
            + "decode(b.revolve_rate_e_month,'','300012',b.revolve_rate_e_month) as revolve_rate_e_month,"
            + "b.group_int_sign,"
            + "decode(b.group_int_sign,'+',b.group_int_rate,"
            + "                            b.group_int_rate*-1) as group_int_rate," 
            + "b.group_rate_s_month,"
            + "b.group_rate_e_month,"
            + "b.batch_int_sign,"
            + "decode(b.batch_int_sign,'+',b.batch_int_rate,"
            + "                            b.batch_int_rate*-1) as batch_int_rate,"
            + "b.batch_rate_s_month,"
            + "b.batch_rate_e_month,"
            + "b.payment_rate1,"
            + "b.payment_rate2,"
            + "b.special_stat_fee,"
            + "b.special_stat_s_month,"
            + "b.special_stat_e_month,"
            + "b.no_penalty_flag,"
            + "b.no_penalty_s_month,"
            + "b.no_penalty_e_month,"
            + "b.penalty_rate_s_month,"
            + "b.penalty_rate_e_month,"
            + "b.penalty_rate,"
            + "b.penalty_sign,"
            + "b.ao_int_rate,"
            + "b.ao_rate_s_month,"
            + "b.ao_rate_e_month,"
            + "b.aox_int_rate,"
            + "b.aox_rate_s_month,"
            + "b.aox_rate_e_month,"
            + "b.new_bill_flag,"
            + "b.ao_posting_date,"
            + "b.new_cycle_month,"
            + "b.last_interest_date,"
            + "b.int_rate_mcode,"
            + "b.last_credit_amt,"
            + "a.end_bal_op,"
            + "a.end_bal_lk,"
            + "a.temp_unbill_interest,"
            + "a.min_pay,"
            + "a.min_pay_bal,"
            + "a.ttl_amt,"
            + "TRUNC(a.adi_beg_bal,0) as adi_beg_bal,"
            + "TRUNC(a.adi_end_bal,0) as adi_end_bal, "
            + "TRUNC(a.adi_d_avail,0) as adi_d_avail, "
            + "decode(a.last_min_pay_date,'','99999999',a.last_min_pay_date) as last_min_pay_date";
  daoTable  = "act_acno b,act_acct_"+getValue("wday.stmt_cycle")+" a";
  whereStr  = "where a.p_seqno = b.p_seqno "
            + "and   b.stmt_cycle = ? "
            + "and   b.p_seqno = b.acno_p_seqno "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and b.p_seqno  = ? "; 
      setString(2 , pSeqno);
     }
  whereStr  = whereStr  
            + "order by substr(a.p_seqno,10.1) ";

  openCursor();

  totalCnt=0;
  String char10="";
  while( fetchTable() ) 
   { 
    setValue("acmm.p_seqno" , getValue("p_seqno"));
    cnt1 = getLoadData("acmm.p_seqno");
    if (cnt1==0) continue;
//showLogMessage("I","","p_seqno : ["+ getValue("p_seqno") +"]");

    totalCnt++;

    if (getValue("rc_use_b_adj").length()==0)         setValue("rc_use_b_adj"          , "X");
    if (getValue("rc_use_bs_date").length()==0)       setValue("rc_use_bs_date"        , "00000000");
    if (getValue("rc_use_be_date").length()==0)       setValue("rc_use_be_date"        , "99999999");
    if (getValue("rc_use_indicator").length()==0)     setValue("rc_use_indicator"      , "X");
    if (getValue("rc_use_s_date").length()==0)        setValue("rc_use_s_date"         , "00000000");
    if (getValue("rc_use_e_date").length()==0)        setValue("rc_use_e_date"         , "99999999");
    if (getValue("revolve_int_sign").length()==0)     setValue("revolve_int_sign"      , "X");
  //if (getValue("revolve_rate_s_month").length()==0) setValue("revolve_rate_s_month"  , "999999");
    if (getValue("revolve_rate_s_month").length()==0) setValue("revolve_rate_s_month"  , "000000");
    if (getValue("revolve_rate_e_month").length()==0) setValue("revolve_rate_e_month"  , "999999");
    if (getValue("group_int_sign").length()==0)       setValue("group_int_sign"        , "X");
    if (getValue("group_rate_s_month").length()==0)   setValue("group_rate_s_month"    , "999999");
    if (getValue("group_rate_e_month").length()==0)   setValue("group_rate_e_month"    , "999999");
    if (getValue("batch_int_sign").length()==0)       setValue("batch_int_sign"        , "X");
    if (getValue("batch_rate_s_month").length()==0)   setValue("batch_rate_s_month"    , "999999");
    if (getValue("batch_rate_e_month").length()==0)   setValue("batch_rate_e_month"    , "999999");
    if (getValue("payment_rate1").length()==0)        setValue("payment_rate1"         , "XX");
    if (getValue("payment_rate2").length()==0)        setValue("payment_rate2"         , "XX");
    if (getValue("special_stat_s_month").length()==0) setValue("special_stat_s_month"  , "999999");
    if (getValue("special_stat_e_month").length()==0) setValue("special_stat_e_month"  , "999999");
    if (getValue("no_penalty_flag").length()==0)      setValue("no_penalty_flag"       , "X");
    if (getValue("no_penalty_s_month").length()==0)   setValue("no_penalty_s_month"    , "999999");
    if (getValue("no_penalty_e_month").length()==0)   setValue("no_penalty_e_month"    , "999999");
    if (getValue("penalty_rate_s_month").length()==0) setValue("penalty_rate_s_month"  , "999999");
    if (getValue("penalty_rate_e_month").length()==0) setValue("penalty_rate_e_month"  , "999999");
    if (getValue("penalty_sign").length()==0)         setValue("penalty_sign"          , "X");
    if (getValue("ao_rate_e_month").length()==0)      setValue("ao_rate_e_month"       , "999912");
    if (getValue("aox_rate_e_month").length()==0)     setValue("aox_rate_e_month"      , "999912");
    if (getValue("last_min_pay_date").length()==0)    setValue("last_min_pay_date"     , "99999999");

    setValue("acmm.this_purc_end_bal" , "0"); 
    setValue("acmm.last_purc_end_bal" , "0"); 
    setValue("acmm.this_ttl_amt"      , "0"); 
    setValue("acmm.penauty_amt"       , "0"); 
    setValue("acmm.interest_amt"      , "0"); 
    setValue("acmm.rc_indicator"      , "");

    setValue("intr_enq_seqno"         , "0");
    setValue("temp_interest_amt"      , "0");  /* 記錄帳戶該cycle所有利息加總 */

    setValue("agen.acct_type" , getValue("acct_type"));
    cnt1 = getLoadData("agen.acct_type");

    tempRevolvingInterest[1]  = getValueDouble("agen.revolving_interest1");
    tempRevolvingInterest[2]  = getValueDouble("agen.revolving_interest2");
    tempRevolvingInterest[3]  = getValueDouble("agen.revolving_interest3");
    tempRevolvingInterest[4]  = getValueDouble("agen.revolving_interest4");
    tempRevolvingInterest[5]  = getValueDouble("agen.revolving_interest5");
    tempRevolvingInterest[6]  = getValueDouble("agen.revolving_interest6");

    setValueDouble("acmm.revolving_rate" , getValueDouble("agen.revolving_interest1"));

    // *************** 特殊對帳單手續費 *****************************************
    if ((getValueDouble("special_stat_fee") != 0)&&
        (getValue("wday.this_acct_month").compareTo(getValue("special_stat_s_month"))>=0)&&
        (getValue("wday.this_acct_month").compareTo(getValue("special_stat_e_month"))<=0))  
       {
        setValue("syse.curr_code"    ,"901");
        setValue("syse.txn_code"     , "BF");
        setValue("syse.dest_amt"     , getValue("special_stat_fee")); 
        setValue("syse.dc_dest_amt"  , getValue("special_stat_fee")); 
        setValue("syse.src_amt"      , getValue("special_stat_fee"));
        insertBilSysexp();
       }
    else
        {
         setValue("special_stat_fee" ,  "0");    /* 不可收費 */
        }
    // **************************************************************************
    if (getValueInt("adi_beg_bal")>0) insertBilAdiexp();

    if (DEBUG) showLogMessage("I","","acct_key =["
                                    + getValue("acct_key")
                                    + "] acct_type["
                                    + getValue("acct_type")
                                    + "] p_seqno["
                                    + getValue("p_seqno")
                                    +"]");

    selectActAcctCurr();

    if (DEBUG) showLogMessage("I","","============ 消費逐筆計息 ==============");

    if (!char10.equals(getValue("p_seqno").substring(9,10)))
       loadActDebtIntr(getValue("p_seqno").substring(9,10));

    char10 = getValue("p_seqno").substring(9,10);
    selectActDebt();

    if (DEBUG) showLogMessage("I","","============ 消費逐筆計息 END===========");
    procCycAcmmData();

    for (int intj=0;intj<currCnt;intj++)
      {
       int intSeq = getValueInt("acul.bill_sort_seq",intj);

       setValue("abem.curr_code"            , getValue("acul.curr_code",intj));
       setValue("abem.bill_sort_seq"        , getValue("acul.bill_sort_seq",intj));

       insertCycAbem(1,intSeq);
       insertCycAbem(7,intSeq);
       updateCycAcmmCurr(intSeq);
      }

    updateCycAcmm();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 double  commCurrAmt(String currCode,double val,int rnd) throws Exception
 {

  setValue("pcde.curr_code" , currCode);
  cnt1 = getLoadData("pcde.curr_code");

  val = val * 10000.0;
  val = Math.round(val);

//showLogMessage("I","","STEP 2001 ["+ val + "]");

  BigDecimal curr_amt = new BigDecimal(val).divide(new BigDecimal("10000"));

//showLogMessage("I","","STEP 2002 ["+ curr_amt + "]");

  if (cnt1==0) return(curr_amt.doubleValue());

  double retNum = 0.0;
  if (rnd>0)  retNum = curr_amt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_UP).doubleValue(); 
  if (rnd==0) retNum = curr_amt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_HALF_UP).doubleValue(); 
  if (rnd<0)  retNum = curr_amt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_DOWN).doubleValue(); 

//showLogMessage("I","","STEP 2003 ["+ retNum + "]");

  return(retNum);
 }
// ************************************************************************
 double  xcommCurrAmt(String currCode,double currAmt,int rnd) throws Exception
 {

  setValue("pcde.curr_code" , currCode);
  cnt1 = getLoadData("pcde.curr_code");


  currAmt = currAmt * 10000.0;
  currAmt = Math.round(currAmt);
  currAmt = currAmt / 10000.0;
//showLogMessage("I","","STEP 2001 ["+ curr_code + "]");
//showLogMessage("I","","STEP 2002 ["+ curr_amt + "]");

  if (cnt1==0) return(currAmt);

  double newCurrAmt=currAmt;

  for (int inti=0;inti<getValueInt("pcde.curr_amt_dp");inti++)
      newCurrAmt=newCurrAmt*10;
 
  newCurrAmt = newCurrAmt * 10000.0;
  newCurrAmt = Math.round(newCurrAmt);
  newCurrAmt = newCurrAmt / 10000.0;

//showLogMessage("I","","STEP 2003 ["+ new_curr_amt + "]");

  if (rnd>0)  newCurrAmt =  Math.ceil(newCurrAmt);
  if (rnd==0) newCurrAmt =  Math.round(newCurrAmt);
  if (rnd<0)  newCurrAmt =  Math.floor(newCurrAmt);

//showLogMessage("I","","STEP 2004 ["+ new_curr_amt + "]");

  for (int inti=0;inti<getValueInt("pcde.curr_amt_dp");inti++)
      newCurrAmt=newCurrAmt/10;
       
  newCurrAmt = newCurrAmt * 10000.0;
  newCurrAmt = Math.round(newCurrAmt);
  newCurrAmt = newCurrAmt / 10000.0;

//showLogMessage("I","","STEP 2005 ["+ new_curr_amt + "]");

  return(newCurrAmt);
 }
// ************************************************************************
 void  selectPtrDisc() throws Exception
 {
//no update of ptr_disc from ui
/***
  extendField = "disc.";
  daoTable  = "ptr_disc";
  whereStr  = "where message_seq = '00' ";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_disc error!" );
      exitProgram(1);
     }
***/
  setValue("disc.rc_rate"        , "100"); 
  setValue("disc.rc_rate_limit"  , "100"); 
  setValue("disc.waive_intr_pena", "0"); 
  setValue("disc.waive_penauty"  , "0"); 

 }
// ************************************************************************ 
 void  loadBilMerchant() throws Exception
 {
  extendField = "merc.";
  selectSQL = "mcht_no,"
            + "trans_flag";
  daoTable  = "bil_merchant";
  whereStr  = "where trans_flag = 'Y'";

  int  n = loadTable();

  setLoadData("merc.mcht_no");

  showLogMessage("I","","Load bil_merchant Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActCommute() throws Exception
 {
  extendField = "acom.";
  selectSQL = "reference_no,"
            + "ao_int_rate,"
            + "ao_rate_date";
  daoTable  = "act_commute";
  whereStr  = "where ao_rate_date = ? ";

  setString(1 , business_date);

  int  n = loadTable();

  setLoadData("acom.reference_no");

  showLogMessage("I","","Load act_commute Count: ["+n+"]");
 }
// ************************************************************************
 void insertCycAbem(int inti,int intSeq) throws Exception
 {
  dateTime();
  extendField = "abem.";

  setValue("abem.curr_code"          , getValue("abem.curr_code"));
  setValue("abem.bill_sort_seq"      , getValue("abem.bill_sort_seq"));
  setValue("abem.p_seqno"            , getValue("p_seqno"));
  setValue("abem.acct_type"          , getValue("acct_type"));
  setValue("abem.print_seq"          , "0001");

  if (inti==1)
     {
    //setValue("abem.description"    , "上期待繳總額（負數代表溢繳金額）");
      setValue("abem.description"    , "上期金額");
      setValue("abem.print_type"     , "01");
      setValue("abem.dest_amt"       , getValue("acur.ttl_amt",intSeq));
      setValue("abem.dc_dest_amt"    , getValue("acur.dc_ttl_amt",intSeq));
     }
  else
     {
    //setValue("abem.description"    , "本期總應繳款");
      setValue("abem.description"    , "本期應繳總額");
      setValue("abem.print_type"     , "07");
      setValue("abem.dest_amt"       , getValue("cacr.this_ttl_amt",intSeq));
      setValue("abem.dc_dest_amt"    , getValue("cacr.dc_this_ttl_amt",intSeq));
     }    
  setValue("abem.crt_pgm"            , javaProgram);
  setValue("abem.mod_pgm"            , javaProgram);
  setValue("abem.mod_time"           , sysDate+sysTime);

  daoTable = "cyc_abem_"+getValue("wday.stmt_cycle");
  insertTable();
  return;
}
// ************************************************************************
 void insertActPenaltyLog() throws Exception
 {
  daoTable    = "act_penalty_log";
  extendField = "iplg.";

  setValue("iplg.p_seqno"            , getValue("p_seqno"));
  setValue("iplg.acct_month"         , getValue("wday.this_acct_month"));
  setValue("iplg.stmt_cycle"         , getValue("wday.stmt_cycle"));
  setValue("iplg.penalty_amt"        , getValue("aplg.penalty_amt"));
  setValue("iplg.org_pn_flag"        , getValue("aplg.org_pn_flag"));
  setValue("iplg.act_pn_flag"        , getValue("aplg.act_pn_flag"));
  setValue("iplg.mod_pgm"            , javaProgram);
  setValue("iplg.mod_time"           , sysDate+sysTime);

  insertTable();
  return;
}
// ************************************************************************
 int updateCycAcmm() throws Exception
 {
  daoTable  = "cyc_acmm_"+getValue("wday.stmt_cycle");

  updateSQL = "this_ttl_amt         = ?, "
            + "minimum_pay_bal      = ?, "
            + "overpay_amt          = ?, "
            + "interest_amt         = ?, "
            + "penauty_amt          = ?, "
            + "rc_indicator         = ?, "
            + "this_purc_end_bal    = ?, "
            + "last_purc_end_bal    = ?, "
            + "revolving_rate       = ?, "
            + "ttl_amt              = ?, "
            + "last_credit_amt      = ?, "
            + "mod_time             = sysdate,"
            + "mod_pgm              = ? ";
    whereStr  = "where rowid          = ? ";

  setDouble(1 , getValueDouble("acmm.this_ttl_amt"));
  setDouble(2 , getValueDouble("min_pay_bal"));
  setDouble(3 , getValueDouble("end_bal_op")  
              + getValueDouble("end_bal_lk"));
  setDouble(4 , getValueDouble("acmm.interest_amt"));
  setDouble(5 , getValueDouble("acmm.penauty_amt"));    
  setString(6 , getValue("acmm.rc_indicator"));    
  setDouble(7 , getValueDouble("acmm.this_purc_end_bal"));    
  setDouble(8 , getValueDouble("acmm.last_purc_end_bal"));    
  setDouble(9 , getValueDouble("acmm.revolving_rate"));    
  setDouble(10, getValueDouble("ttl_amt"));    
  setDouble(11, getValueDouble("last_credit_amt"));

  setString(12, javaProgram);
  setRowId(13 , getValue("acmm.rowid"));

  int n = updateTable();
  if ( n == 0 )
     { showLogMessage("E","","update_cyc_acmm ERROR ");  }

  return n;
 }
// ************************************************************************
 int updateCycAcmmCurr(int intSeq) throws Exception
 {
  daoTable  = "cyc_acmm_curr_"+getValue("wday.stmt_cycle");
  updateSQL = "this_ttl_amt         = ?, "
            + "dc_this_ttl_amt      = ?, "
            + "this_purc_end_bal    = ?, "
            + "dc_this_purc_end_bal = ?, "
            + "minimum_pay_bal      = ?, "
            + "dc_minimum_pay_bal   = ?, "
            + "overpay_amt          = ?, "
            + "dc_overpay_amt       = ?, "
            + "interest_amt         = ?, "
            + "dc_interest_amt      = ?, "
            + "mod_time             = sysdate,"
            + "mod_pgm              = ? ";
  whereStr  = "WHERE p_seqno   = ? "
            + "AND   curr_code = ? ";

  setDouble(1 , getValueDouble("cacr.this_ttl_amt",intSeq));
  setDouble(2 , getValueDouble("cacr.dc_this_ttl_amt",intSeq));            
  setDouble(3 , getValueDouble("cacr.this_purc_end_bal",intSeq));            
  setDouble(4 , commCurrAmt(getValue("abem.curr_code"),
                getValueDouble("cacr.dc_this_purc_end_bal",intSeq),0));
  setDouble(5 , getValueDouble("acur.min_pay_bal",intSeq));            
  setDouble(6 , getValueDouble("acur.dc_min_pay_bal",intSeq));            
  setDouble(7 , commCurrAmt(getValue("abem.curr_code"), 
                getValueDouble("acur.end_bal_op",intSeq)    
              + getValueDouble("acur.end_bal_lk",intSeq),0));
  setDouble(8 , commCurrAmt(getValue("abem.curr_code"), 
                getValueDouble("acur.dc_end_bal_op",intSeq) 
              + getValueDouble("acur.dc_end_bal_lk",intSeq),0));
  setDouble(9 , getValueDouble("cacr.interest_amt",intSeq));
  setDouble(10, getValueDouble("cacr.dc_interest_amt",intSeq));
  setString(11, javaProgram);
  setString(12, getValue("p_seqno"));
  setString(13, getValue("abem.curr_code"));

  int n = updateTable();
  if ( n == 0 )
     { showLogMessage("E","","update_cyc_acmm_curr p _seqno["+getValue("p_seqno")+"]-["+getValue("abem.curr_code")+"] ERROR ");  }

  return n;
 }
// ************************************************************************
 void  loadActAcctCurr() throws Exception
 {
  extendField = "acul.";
  selectSQL = "c.p_seqno,"
            + "c.curr_code,"
            + "c.end_bal_op,"
            + "c.end_bal_lk,"
            + "c.temp_unbill_interest,"
            + "c.min_pay,"
            + "c.min_pay_bal,"
            + "c.ttl_amt,"
            + "c.ttl_amt_bal,"
            + "c.last_ttl_amt,"
            + "c.dc_end_bal_op,"
            + "c.dc_end_bal_lk,"
            + "c.dc_temp_unbill_interest,"
            + "c.dc_min_pay,"
            + "c.dc_min_pay_bal,"
            + "c.dc_ttl_amt,"
            + "decode(c.delaypay_ok_flag,'','X',c.delaypay_ok_flag) as delaypay_ok_flag,"
            + "c.dc_last_ttl_amt,"
            + "decode(c.no_interest_flag,'','X',c.no_interest_flag) as no_interest_flag,"
            + "decode(c.no_interest_s_month,'','000000',c.no_interest_s_month) as no_interest_s_month,"
            + "decode(c.no_interest_e_month,'','999999',c.no_interest_e_month) as no_interest_e_month,"
            + "c.bill_sort_seq,"
            + "a.purch_bal_parm,"
            + "a.purch_bal_wave,"
            + "a.total_bal";
  daoTable  = "act_acct_curr_"+getValue("wday.stmt_cycle")+" c,cyc_acmm_curr_"+getValue("wday.stmt_cycle")+" b,ptr_curr_general a";
  whereStr  = "where  c.curr_code = b.curr_code "
            + "and    c.p_seqno   = b.p_seqno "
            + "and    b.curr_code = a.curr_code "
            + "and    a.acct_type = '01' "     // mark 8969
            ;

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and b.p_seqno  = ? "; 
      setString(1 , pSeqno);
     }
  whereStr  = whereStr
            + "order by c.p_seqno,c.curr_code ";
    
  int n  = loadTable();

  setLoadData("acul.p_seqno");

  showLogMessage("I","","Load act_acct_curr Count: ["+n+"]");
 }
// ************************************************************************
void selectActAcctCurr() throws Exception 
 {
  setValue("acul.p_seqno"   , getValue("p_seqno"));
  currCnt  = getLoadData("acul.p_seqno");

  for (int intj=0;intj<currCnt;intj++)
    {
     int int_seq = getValueInt("acul.bill_sort_seq",intj);

     setValue("acur.curr_code"              , getValue("acul.curr_code",intj)                , int_seq);
     setValue("acur.delaypay_ok_flag"       , getValue("acul.delaypay_ok_flag",intj)         , int_seq);
     setValue("acur.no_interest_flag"       , getValue("acul.no_interest_flag",intj)         , int_seq);
     setValue("acur.no_interest_s_month"    , getValue("acul.no_interest_s_month",intj)      , int_seq);
     setValue("acur.no_interest_e_month"    , getValue("acul.no_interest_e_month",intj)      , int_seq);
     setValue("acur.end_bal_op"             , getValue("acul.end_bal_op",intj)               , int_seq);
     setValue("acur.end_bal_lk"             , getValue("acul.end_bal_lk",intj)               , int_seq);
     setValue("acur.temp_unbill_interest"   , getValue("acul.temp_unbill_interest",intj)     , int_seq);
     setValue("acur.min_pay"                , getValue("acul.min_pay" ,intj)                 , int_seq);
     setValue("acur.min_pay_bal"            , getValue("acul.min_pay_bal",intj)              , int_seq);
     setValue("acur.ttl_amt"                , getValue("acul.ttl_amt",intj)                  , int_seq);
     setValue("acur.ttl_amt_bal"            , getValue("acul.ttl_amt_bal",intj)              , int_seq);
     setValue("acur.last_ttl_amt"           , getValue("acul.last_ttl_amt",intj)             , int_seq);
     setValue("acur.dc_end_bal_op"          , getValue("acul.dc_end_bal_op",intj)           , int_seq);
     setValue("acur.dc_end_bal_lk"          , getValue("acul.dc_end_bal_lk",intj)            , int_seq);
     setValue("acur.dc_temp_unbill_interest", getValue("acul.dc_temp_unbill_interest",intj)  , int_seq);
     setValue("acur.dc_min_pay"             , getValue("acul.dc_min_pay",intj)               , int_seq);
     setValue("acur.dc_min_pay_bal"         , getValue("acul.dc_min_pay_bal",intj)           , int_seq);
     setValue("acur.dc_ttl_amt"             , getValue("acul.dc_ttl_amt",intj)               , int_seq);
     setValue("acur.dc_last_ttl_amt"        , getValue("acul.dc_last_ttl_amt",intj)          , int_seq);

//   setValue("pcgl.curr_code" , getValue("acul.curr_code",intj));
//   int recCnt  = getLoadData("pcgl.curr_code");
//   if (recCnt==0)
//      showLogMessage("I","","p_seqni["+getValue("p_seqno")+"] curr_code["+getValue("acul.curr_code",intj)+"] not found ptr_curr_general error");

     setValue("acur.purch_bal_parm"         , getValue("acul.purch_bal_parm")           , int_seq);
     setValue("acur.purch_bal_wave"         , getValue("acul.purch_bal_wave")           , int_seq);
     setValue("acur.total_bal"              , getValue("acul.total_bal")                , int_seq);

     setValue("mp_temp_last_purch_bal"      , "0"  , int_seq);
     setValue("cacr.this_ttl_amt"           , "0"  , int_seq);
     setValue("cacr.dc_this_ttl_amt"        , "0.0", int_seq);
     setValue("cacr.this_purc_end_bal"      , "0"  , int_seq);
     setValue("cacr.dc_this_purc_end_bal"   , "0.0", int_seq);
     setValue("cacr.interest_amt"           , "0.0", int_seq);
     setValue("cacr.dc_interest_amt"        , "0.0", int_seq);
     setValue("temp.interest_amt"           , "0.0" , int_seq);   /* 記錄帳戶該cycle所有利息加總 */
     setValue("temp.dc_interest_amt"        , "0.0" , int_seq);   /* 記錄帳戶該cycle所有利息加總 */
     setValue("temp.org_interest_amt"       , "0.0" , int_seq);   /* 記錄帳戶減免前所有利息      */
     setValue("temp.dc_org_interest_amt"    , "0.0" , int_seq);   /* 記錄帳戶減免前所有利息      */
    }
}
/*****************************************************************************/
 void  loadActDebtIntr(String p10char) throws Exception
 {
  extendField = "debt.";
  selectSQL = "p_seqno,"
            + "curr_code,"       
            + "reference_no,"
            + "acct_month,"      
            + "purchase_date,"   
            + "post_date,"  
            + "end_bal,"         
            + "dc_end_bal,"
            + "card_no,"
            + "acct_code,"
            + "interest_date,"
            + "interest_rs_date,"
            + "ao_flag,"
            + "mcht_no,"
            + "int_rate,"
            + "int_rate_flag,"
            + "new_it_flag,"
            + "inter_rate_code,"
            + "inter_rate_code2,"
            + "interest_method,"
            + "bill_sort_seq";
  daoTable  = "act_debt_intr_"+getValue("wday.stmt_cycle");
  whereStr  = "where substr(p_seqno,10,1) = ? "
            ;

  setString(1 , p10char);

  showLogMessage("I",""," 載入區段 : ["+p10char+"]");

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and p_seqno  = ? "; 
      setString(2 , pSeqno);
     }
  whereStr  = whereStr
            + "order by p_seqno";

  int  n = loadTable();

  setLoadData("debt.p_seqno");

  showLogMessage("I","","Load act_debt_intr Count: ["+n+"]");
 }
// ************************************************************************
void selectActDebt() throws Exception 
 {
  setValue("debt.p_seqno"   , getValue("p_seqno"));
  cnt1 = getLoadData("debt.p_seqno");

  for (int inti=0;inti<cnt1;inti++)
     {
      setValue("intr.intr_s_date"     , "");
      setValue("intr.interest_rate"   , "0");

      int intSeq=getValueInt("debt.bill_sort_seq",inti);


      if (DEBUG) showLogMessage("I","","----------------------------------------");

      if (DEBUG) showLogMessage("I","","step 20  科目["
                                      + getValue("debt.acct_code",inti) 
                                      + "]  end_bal["
                                      + String.format("%.2f",getValueDouble("debt.end_bal",inti))
                                      + "]  dc_end_bal["
                                      + String.format("%.2f",getValueDouble("debt.dc_end_bal",inti))
                                      + "]  月份["
                                      + getValue("debt.acct_month",inti)
                                      +"]");
      if (DEBUG) showLogMessage("I","","         mcode["
                                      + getValueInt("int_rate_mcode") 
                                      + "]  end_bal["
                                      + String.format("%.2f",getValueDouble("debt.end_bal",inti))
                                      + "]  int_rate_flag["
                                      + getValue("debt.int_rate_flag",inti)
                                      + "]  reference_no["
                                      + getValue("debt.reference_no",inti)
                                      + "]");

      setValue("temp_interest_amt"     ,  "0");
      if (getValue("debt.interest_method",inti).equals("Y"))   /*** 需算利息科目 */
        {
          if (getValue("debt.post_date",inti).compareTo(getValue("wday.last_close_date"))<= 0)
             setValueDouble("acmm.last_purc_end_bal" , getValueDouble("acmm.last_purc_end_bal")
                                                     + getValueDouble("debt.end_bal",inti));

          setValueDouble("acmm.this_purc_end_bal" , getValueDouble("acmm.this_purc_end_bal")
                                                  + getValueDouble("debt.end_bal",inti));
          setValueDouble("cacr.this_purc_end_bal" , getValueDouble("cacr.this_purc_end_bal",intSeq)
                                                  + getValueDouble("debt.end_bal",inti) , intSeq);

              if (DEBUG) showLogMessage("I","","step 11-0  dc_this_purc["
                                              + getValueDouble("cacr.dc_this_purc_end_bal",intSeq)
                                              + "] - debt_end_bal=["
                                              + getValueDouble("debt.dc_end_bal",inti)
                                              + "]");


          setValueDouble("cacr.dc_this_purc_end_bal" , getValueDouble("cacr.dc_this_purc_end_bal",intSeq)
                                                     + getValueDouble("debt.dc_end_bal",inti) ,intSeq);

              if (DEBUG) showLogMessage("I","","step 11-1  dc_this_purc["
                                              + getValueDouble("cacr.dc_this_purc_end_bal",intSeq)
                                              + "]");

          setValue("merc.trans_flag" , "");
/***
          if (getValue("debt.acct_code",inti).equals("IT"))
             {
              setValue("merc.mcht_no" , getValue("debt.mcht_no",inti));
              int cnt2 = getLoadData("merc.mcht_no");
              if (DEBUG) showLogMessage("I","","step xxxxx01 mcht_no ["+ getValue("merc.mcht_no") +"] ["+cnt2+"]");

              if (cnt2==0) setValue("merc.trans_flag" , "N");
              if (DEBUG) showLogMessage("I","","step 11    分期免息["
                                              + getValue("merc.trans_flag")
                                              + "]");
             }
***/
          if (!getValue("merc.trans_flag").equals("Y")) 
            procInterest(inti,intSeq);
        }

      if (getValue("debt.interest_rs_date",inti).length()==8) 
        updateActDebt1(inti);

      setValueDouble("acmm.this_ttl_amt"  , getValueDouble("acmm.this_ttl_amt") 
                                          + getValueDouble("debt.end_bal",inti));

      setValueDouble("cacr.this_ttl_amt" , getValueDouble("cacr.this_ttl_amt",intSeq)
                                         + getValueDouble("debt.end_bal",inti) ,intSeq);
                                           
      setValueDouble("cacr.dc_this_ttl_amt" , getValueDouble("cacr.dc_this_ttl_amt",intSeq)
                                            + getValueDouble("debt.dc_end_bal",inti) ,intSeq);

//    showLogMessage("I","", "step 001 ["+ getValueDouble("cacr.dc_this_ttl_amt",int_seq)
//                                       + "] ["
//                                       + getValueDouble("debt.dc_end_bal",inti) +"]");  
      if (DEBUG) showLogMessage("I","", "step 19-["
                                      + getValue("debt.curr_code",inti)
                                      + "]   台幣累計欠款總額["
                                      + String.format("%.0f",getValueDouble("acmm.this_ttl_amt"))
                                      + "]");
      if (DEBUG) showLogMessage("I","", "       -NT 累計欠款總額[" 
                                      + String.format("%.0f",getValueDouble("cacr.this_ttl_amt",intSeq))
                                      + "]");
      if (DEBUG) showLogMessage("I","", "       -DC 累計欠款總額["
                                      + String.format("%.0f",getValueDouble("cacr.dc_this_ttl_amt",intSeq))
                                      + "]");
     }
 }
// ************************************************************************
void procCycAcmmData() throws Exception 
 {
  if (DEBUG) showLogMessage("I","","============= 計息累計 ================="); 
  for (int intj=0;intj<currCnt;intj++)
   {
    int int_seq=getValueInt("acul.bill_sort_seq",intj);

    // cycle利息及繳款時利息
    setValueDouble("cacr.interest_amt" , (getValueDouble("temp.interest_amt",int_seq)
                                         +getValueDouble("acur.temp_unbill_interest",int_seq)) 
                                       , int_seq);

    if (DEBUG) showLogMessage("I","","step test1  最後利息  ["
                                 + getValueDouble("cacr.interest_amt",int_seq)
                                 + "]=["
                                 + getValueDouble("temp.interest_amt",int_seq)
                                 + "]+["
                                 + getValueDouble("acur.temp_unbill_interest",int_seq)
                                 + "]");

    setValueDouble("cacr.dc_interest_amt" , getValueDouble("temp.dc_interest_amt",int_seq) // cycle利息及繳款時利息
                                          + getValueDouble("acur.dc_temp_unbill_interest",int_seq) ,int_seq);

  //setValueDouble("cacr.interest_amt" , commCurrAmt("901",getValueDouble("cacr.interest_amt",int_seq),-1),int_seq);
    setValueDouble("cacr.interest_amt" , commCurrAmt("901",getValueDouble("cacr.interest_amt",int_seq),0),int_seq);

    if (DEBUG) showLogMessage("I","","step test2   [" 
                                 + getValueDouble(" acmm.interest_amt")
                                 + "]+["
                                 + getValueDouble("cacr.interest_amt",int_seq)
                                 + "] - DC["
                                 + getValueDouble("cacr.dc_interest_amt",int_seq)
                                 + "]");

    setValueDouble("cacr.dc_interest_amt" , commCurrAmt(getValue("acur.curr_code",int_seq)
                                        //, getValueDouble("cacr.dc_interest_amt",int_seq),-1),int_seq);
                                          , getValueDouble("cacr.dc_interest_amt",int_seq),0),int_seq);

    if (getValue("acur.curr_code",int_seq).equals("901"))
       setValueDouble("cacr.interest_amt" , getValueDouble("cacr.dc_interest_amt",int_seq) , int_seq);

    setValueDouble("temp.org_interest_amt" , getValueDouble("cacr.interest_amt",int_seq) , int_seq);
    setValueDouble("temp.dc_org_interest_amt" , getValueDouble("cacr.dc_interest_amt",int_seq) , int_seq);

    setValueDouble("acmm.interest_amt" , (getValueDouble("acmm.interest_amt")
                                         +getValueDouble("cacr.interest_amt",int_seq))
                                         );
   
   if (DEBUG) showLogMessage("I","", "step 29-["
                                   + getValue("acur.curr_code",int_seq)
                                   + "]   台幣總合計利息["
                                   + String.format("%.0f",getValueDouble("acmm.interest_amt"))
                                   + "]");

   if (DEBUG) showLogMessage("I","", "        NT 總合計利息["
                                   + String.format("%.0f",getValueDouble("cacr.interest_amt",int_seq))
                                   + "] 未入帳利息["
                                   + String.format("%f",getValueDouble("acur.temp_unbill_interest",int_seq))
                                   + "]");
   if (DEBUG) showLogMessage("I","", "        DC 總合計利息["
                                   + String.format("%.2f",getValueDouble("cacr.dc_interest_amt",int_seq))
                                   + "] 未入帳利息["
                                   + String.format("%.2f",getValueDouble("acur.dc_temp_unbill_interest",int_seq))
                                   + "]");
   }

  if (DEBUG) showLogMessage("I","","============= 計息累計 END ============="); 

  if (getValueDouble("acmm.interest_amt")<0) 
     {
      setValue("acmm.interest_amt" , "0" );
      for (int intj=0;intj<currCnt;intj++) 
        {
         int intSeq=getValueInt("acul.bill_sort_seq",intj);

         setValueDouble("cacr.interest_amt"    , 0 , intSeq);
         setValueDouble("cacr.dc_interest_amt" , 0 , intSeq); 
        }
     }

  int tempRtn = selectActPenaltyLog();  // 20200103

  if (DEBUG) showLogMessage("I","","============= 違約金計算 =[" + tempRtn + "]==============");
  procPenautyAmt();  /** 跟利息有關需在利息後計算(acmm.penauty_amt) **/
  if (DEBUG) showLogMessage("I","","============= 違約金計算 END============"); 
  if (DEBUG) showLogMessage("I","","step 38 合計台幣總利息["
                                  + String.format("%.0f",getValueDouble("acmm.interest_amt"))
                                  +"] 違約金["
                                  + String.format("%.0f",getValueDouble("acmm.penauty_amt"))
                                  +"]");
  if (DEBUG) showLogMessage("I","","============= 利息違約金減免 ===============");
  interestPenautyDiscount(); 
  if (DEBUG) showLogMessage("I","","============= 利息違約金減免 END============");
  if (DEBUG) showLogMessage("I","","step 60 最後 合計台幣總利息["
                                  + String.format("%.0f",getValueDouble("acmm.interest_amt"))
                                  + "] 違約金["
                                  + String.format("%.0f",getValueDouble("acmm.penauty_amt"))
                                  + "]");
  /*** 處理利息 START **********************/
  setValueDouble("acmm.interest_amt"   , 0);
  for (int intj=0;intj<currCnt;intj++)
    {
     int intSeq=getValueInt("acul.bill_sort_seq",intj);

     setValue("intr_reason_code" , getValue("intr.reason_code"));

     if (getValueDouble("cacr.interest_amt" , intSeq) >0)
        {
         setValue("syse.curr_code"                , getValue("acur.curr_code",intSeq));
         setValue("syse.txn_code"                 , "IF");
         setValueDouble("syse.dest_amt"           , getValueDouble("cacr.interest_amt",intSeq));
         setValueDouble("acmm.interest_amt"       , getValueDouble("acmm.interest_amt") 
                                                  + getValueDouble("cacr.interest_amt",intSeq));
         setValueDouble("syse.dc_dest_amt"        , getValueDouble("cacr.dc_interest_amt",intSeq));
         setValueDouble("syse.src_amt"            , getValueDouble("cacr.dc_interest_amt",intSeq));
         insertBilSysexp();

         if (DEBUG) showLogMessage("I","","step 61-"
                                  + getValue("acur.curr_code",intSeq)
                                  + "    入帳利息"
                                  + String.format("%.2f",getValueDouble("cacr.interest_amt",intSeq))
                                  +"] 台幣累計入帳利息["
                                  + String.format("%.0f",getValueDouble("acmm.interest_amt"))
                                  +"]");
        }
  
       if (((getValueDouble("temp.dc_interest_amt",intSeq)>0)||
            (getValueDouble("acur.dc_temp_unbill_interest",intSeq)>0))&& /* 減免利息,增加負項交易 */
            (getValueDouble("temp.dc_org_interest_amt",intSeq) > 
             getValueDouble("cacr.dc_interest_amt",intSeq)))
          {
           setValue("intr_reason_code " , getValue("intr.reason_code",intSeq));

         if (DEBUG) showLogMessage("I","","step 61a-"
                                  + getValue("acur.curr_code",intSeq)
                                  + "    減免利息"
                                  + String.format("%.2f",getValueDouble("temp.dc_org_interest_amt",intSeq))
                                  + "]-["
                                  + String.format("%.2f",getValueDouble("cacr.dc_interest_amt",intSeq))
                                  + "]");

         if (DEBUG) showLogMessage("I","","step 61b-"
                                  + "    temp_dc_interest_amt"
                                  + String.format("%.2f",getValueDouble("temp.dc_interest_amt",intSeq))
                                  + "]-["
                                  + String.format("%.2f",getValueDouble("acur.dc_temp_unbill_interest",intSeq))
                                  + "]");
           insertActIntr(1,0,intSeq);
          }
    }
  /*** 處理利息 END **********************/

  /*** 處理違約金 START **不可罰三期以上**/


  if (tempRtn!=0) 
     {
      if (getValueInt("acmm.penauty_amt") != 0)
         {
          setValue("syse.curr_code"          ,"901");
          setValue("syse.txn_code"           ,"DF");
          setValue("syse.dest_amt"           , getValue("acmm.penauty_amt"));
          setValue("syse.dc_dest_amt"        , getValue("acmm.penauty_amt"));
          setValue("syse.src_amt"            , getValue("acmm.penauty_amt"));
          insertBilSysexp();
         }
      else
         {
          if (getValue("aplg.act_pn_flag").equals("Y"))
             setValue("aplg.act_pn_flag" , String.format("%d",tempMCode));
         }
     }
  else
     {
      setValue("acmm.penauty_amt"  , "0");
      setValue("aplg.act_pn_flag" , "N");
     }

  setValue("aplg.penalty_amt" , getValue("acmm.penauty_amt"));
  insertActPenaltyLog();
  /*** 處理違約金 END ********************/
  setValue("acmm.this_ttl_amt" , "0");
  for (int intj=0;intj<currCnt;intj++)
    {
     int intSeq=getValueInt("acul.bill_sort_seq",intj);

      setValueDouble("cacr.this_ttl_amt"  , getValueDouble("cacr.this_ttl_amt",intSeq) 
                                          - getValueDouble("acur.end_bal_op",intSeq)  
                                          - getValueDouble("acur.end_bal_lk",intSeq)   
                                          + getValueDouble("cacr.interest_amt",intSeq)
                                          , intSeq);

      if (getValue("acur.curr_code",intSeq).equals("901")) 
         setValueDouble("cacr.this_ttl_amt" , getValueDouble("cacr.this_ttl_amt",intSeq) 
                                            + getValueDouble("adi_end_bal")    
                                            + getValueDouble("acmm.penauty_amt")
                                            + getValueDouble("special_stat_fee")
                                            , intSeq);

      setValueDouble("cacr.this_ttl_amt"  , commCurrAmt("901",getValueDouble("cacr.this_ttl_amt",intSeq),0)
                                          , intSeq);

      setValueDouble("cacr.dc_this_ttl_amt" , getValueDouble("cacr.dc_this_ttl_amt",intSeq) 
                                            - getValueDouble("acur.dc_end_bal_op",intSeq)  
                                            - getValueDouble("acur.dc_end_bal_lk",intSeq)
                                            + getValueDouble("cacr.dc_interest_amt",intSeq)
                                            , intSeq);

//    showLogMessage("I","", "step 002-1 ["+getValue("acur.curr_code",int_seq)+"]");  
//    showLogMessage("I","", "step 002-2 ["+getValueDouble("cacr.dc_this_ttl_amt",int_seq)+"]");  

      if (getValue("acur.curr_code",intSeq).equals("901")) 
         setValueDouble("cacr.dc_this_ttl_amt" , getValueDouble("cacr.dc_this_ttl_amt",intSeq) 
                                               + getValueDouble("adi_end_bal")    
                                               + getValueDouble("acmm.penauty_amt") 
                                               + getValueDouble("special_stat_fee")
                                               , intSeq);

//    showLogMessage("I","", "step 002-3 ["+getValueDouble("acmm.penauty_amt")+"]");  

      setValueDouble("cacr.dc_this_ttl_amt" , commCurrAmt(getValue("acur.curr_code",intSeq)
                                            , getValueDouble("cacr.dc_this_ttl_amt",intSeq),0)
                                            , intSeq);

//    showLogMessage("I","", "step 003 ["+getValueDouble("cacr.dc_this_ttl_amt",int_seq)+"]");  

      if (getValue("acur.curr_code",intSeq).equals("901")) 
         setValueDouble("cacr.this_ttl_amt" , getValueDouble("cacr.dc_this_ttl_amt",intSeq) 
                                            , intSeq);

      setValueDouble("acmm.this_ttl_amt" , getValueDouble("acmm.this_ttl_amt")   
                                         + getValueDouble("cacr.this_ttl_amt",intSeq));

      if (DEBUG) showLogMessage("I","","step 90a-"
                                  + getValue("acur.curr_code",intSeq)
                                  + "    台幣總合計欠款金額["
                                  + String.format("%.0f",getValueDouble("acmm_this_ttl_amt"))
                                  +"] 帳外息["
                                  + String.format("%.4f",getValueDouble("adi_end_bal"))
                                  +"] 違約金["
                                  + String.format("%.0f",getValueDouble("acmm.penauty_amt"))
                                  +"] 特殊對帳單費用["
                                  + String.format("%.0f",getValueDouble("special_stat_fee"))
                                  +"]");

      if (DEBUG) showLogMessage("I","","        NT 總合計欠款金額["
                                      + String.format("%.0f",getValueDouble("cacr.this_ttl_amt",intSeq))
                                      +"]");
      if (DEBUG) showLogMessage("I","","        DC 總合計欠款金額["
                                      + String.format("%.4f",getValueDouble("cacr.dc_this_ttl_amt",intSeq))
                                      +"]");
    }

  procRcIndicator(); /* 設定 h_acmm_rc_indicator */
 }
// ************************************************************************
 void procRcIndicator()  throws Exception 
 {
  if (getValue("rc_use_indicator").equals("1"))
     {setValue("acmm.rc_indicator" ,"1"); }
  else
  if ((getValue("rc_use_indicator").equals("2"))&&
      (business_date.compareTo(getValue("rc_use_s_date")) >= 0) &&
      (business_date.compareTo(getValue("rc_use_e_date")) <= 0 ))
     {setValue("acmm.rc_indicator" ,"2"); }
  else
  if ((getValue("rc_use_indicator").equals("3"))&&
      (business_date.compareTo(getValue("rc_use_s_date")) >= 0) &&
      (business_date.compareTo(getValue("rc_use_e_date")) <= 0))
     {setValue("acmm.rc_indicator" ,"3"); }
  else
     {setValue("acmm.rc_indicator" ,getValue("rc_use_b_adj")); }

  if (getValue("agen.rc_use_flag").equals("3")) 
     setValue("acmm.rc_indicator" ,"3"); 
 }
/*****************************************************************************/
 void  loadActPenaltyLog() throws Exception
 {
  extendField = "pnlg.";
  selectSQL = "a.p_seqno,"
            + "a.acct_month,"
            + "a.org_pn_flag,"
            + "a.act_pn_flag";
  daoTable  = "act_penalty_log a,cyc_acmm_"+ getValue("wday.stmt_cycle") +" b,ptr_actpenalty c";
  whereStr  = "where a.acct_month between to_char(add_months(to_date(?, "
            + "                           'yyyymm'),(c.pn_max_cnt-1)*-1),'yyyymm') "
            + "                   and      ? "
            + "and   b.acct_type  = c.acct_type "
            + "and   a.p_seqno    = b.p_seqno "
            + "and not exists (select 1 from act_penalty_log d where "
            + "d.p_seqno  = b.p_seqno and d.org_pn_flag = 'N' and d.act_pn_flag = 'N' " 
            + "and d.acct_month = ? ) " 
            ;

  setString(1 , getValue("wday.last_acct_month"));
  setString(2 , getValue("wday.last_acct_month"));
  setString(3 , getValue("wday.last_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and b.p_seqno = ? "; 
      setString(3 , pSeqno);
     }
  whereStr  = whereStr  
            + "order by a.p_seqno,a.acct_month desc";

  showLogMessage("I","","     penalty last month : ["+getValue("wday.last_acct_month")+"]");

  int  n = loadTable();

  setLoadData("pnlg.p_seqno");

  showLogMessage("I","","Load act_penality_log  Count: ["+n+"]");
 }
// ************************************************************************
int selectActPenaltyLog() throws Exception 
 {
  setValue("pnlg.p_seqno"   , getValue("p_seqno"));
  int cnt1 = getLoadData("pnlg.p_seqno");

  setValue("temp.org_pn_flag", "");
  setValue("temp.act_pn_flag", "");
  tempMCode = 1;

//for (int int2=0;int2< getValueInt("agen.pn_max_cnt");int2++)
  for (int int2=0;int2< cnt1;int2++)
    {
     if (tempMCode == 1)
        {
         setValue("temp.org_pn_flag", getValue("pnlg.org_pn_flag",int2));
         setValue("temp.act_pn_flag", getValue("pnlg.act_pn_flag",int2));

         if (Arrays.asList("1","2","3").contains(getValue("pnlg.act_pn_flag",int2)))
           {
            tempMCode = Integer.valueOf(getValue("pnlg.act_pn_flag",int2));
            return(1);
           }
        }
     
     if (tempMCode == 2)
        {
         if (getValue("pnlg.act_pn_flag",int2).equals("3")) return(0);
         if (getValue("pnlg.act_pn_flag",int2).equals("2")) 
           {
            tempMCode = tempMCode + 1;
            return(1);
           }
         if (getValue("pnlg.act_pn_flag",int2).equals("1")) return(1);
        }
     
     if (tempMCode == 3)
        {
         if (getValue("pnlg.act_pn_flag",int2).equals("1")) return(1);
        }
        
     if ((getValue("pnlg.org_pn_flag",int2).equals("Y"))&&
         (getValue("pnlg.act_pn_flag",int2).equals("Y")))
        tempMCode = tempMCode + 1;

     if (getValue("pnlg.org_pn_flag",int2).equals("N")) return(1);

     if ((getValue("pnlg.org_pn_flag",int2).equals("Y"))&&
         (getValue("pnlg.act_pn_flag",int2).equals("N")))
        return(0);
    }
  if (cnt1<getValueInt("agen.pn_max_cnt")) return(1);  
  return(0); 
 }
// ************************************************************************
 void procPenautyAmt() throws Exception 
 {
  double  wsPenaltyRate=0;

  setValue("aplg.org_pn_flag" , "N");
  setValue("aplg.act_pn_flag" , "N");

  if (getValueDouble("min_pay")==0) 
     {
      if (DEBUG) showLogMessage("I","","step 31   無MP, 無違約金");
      return;
     }

  /*** 連續逾期期間以繳款截止日認定是否逾期 ***/
  if ((getValue("temp.org_pn_flag").equals("Y"))&&(!getValue("temp.act_pn_flag").equals("1")))
    {
     if ((getValue("last_min_pay_date").compareTo(getValue("wday.last_lastpay_date"))<=0)&&
         (getValueDouble("min_pay_bal")==0)) 
        {
         if (DEBUG) showLogMessage("I","","step 32.1   最後繳足MP日["
                                      + getValue("last_min_pay_date")
                                      + "]<["
                                      + getValue("wday.last_lastpay_date")
                                      + "], 無違約金");
         return;
        }
    }
  else
    {
     if ((getValue("last_min_pay_date").compareTo(getValue("wday.last_delaypay_date"))<=0)&&
         (getValueDouble("min_pay_bal")==0)) 
        {
         if (DEBUG) showLogMessage("I","","step 32.2   最後繳足MP日["
                                      + getValue("last_min_pay_date")
                                      + "]<["
                                      + getValue("wday.last_delaypay_date")
                                      + "], 無違約金");
         return;
        }
    }

  setValue("aplg.org_pn_flag" , "Y");
  setValue("aplg.act_pn_flag" , "Y");

  /*** 連續逾期期間繳款寬限前繳足MP，仍認定逾期但不收逾期手續費 ***/
  if ((getValue("last_min_pay_date").compareTo(getValue("wday.last_delaypay_date"))<= 0)&&
      (getValueDouble("min_pay_bal")==0)) 
      {
      if (DEBUG) showLogMessage("I","","step 33   最後繳足MP日["
                                      + getValue("last_min_pay_date")
                                      + "]<["
                                      + getValue("wday.last_delaypay_date")
                                      + "], 無違約金");
       return;
      }

  if ((getValue("no_penalty_flag").equals("Y"))&&
      (getValue("wday.this_acct_month").compareTo(getValue("no_penalty_s_month"))>=0)&&
      (getValue("wday.this_acct_month").compareTo(getValue("no_penalty_e_month"))<=0)) 
     {
      if (DEBUG) showLogMessage("I","","step 33   免違約金日期["
                                      + getValue("no_penalty_s_month")
                                      + "]-["
                                      + getValue("no_penalty_e_month")
                                      + "], 無違約金");
      return;
     }

  if (DEBUG) showLogMessage("I","","step 24   違約金公式["
                                  + getValue("agen.method")
                                  + "] 0:比率 1:金額");
  if (getValue("agen.method").equals("0"))
     {
      wsPenaltyRate = getValueDouble("agen.percent_penalty");
      if ((getValue("wday.this_acct_month").compareTo(getValue("penalty_rate_s_month"))>=0)&&
          (getValue("wday.this_acct_month").compareTo(getValue("penalty_rate_e_month"))<=0))
         {
          if (getValue("penalty_sign").equals("+"))
             {
              wsPenaltyRate = getValueDouble("agen.percent_penalty") 
                              + getValueDouble("penalty_rate");
             }
          else
             {
              wsPenaltyRate = getValueDouble("agen.percent_penalty") 
                              - getValueDouble("penalty_rate");
             }
         }
      setValueDouble("acmm.penauty_amt" , getValueDouble("agen.fix_penalty") 
                                        + getValueDouble("temp_interest_amt") 
                                        * wsPenaltyRate/100.0);

     if (DEBUG) showLogMessage("I","","step 35   違約金["
                                      + getValueInt("acmm.penauty_amt")
                                      + "]=固定金額["
                                      + getValueDouble("agen.fix_penalty")
                                      + "]+(總利息["
                                      + getValueDouble("temp_interest_amt")
                                      + "]*比率["
                                      + wsPenaltyRate
                                      + "]/100)");
             
     }
  else if (getValue("agen.method").equals("1"))
     {
      setValueDouble("acmm.penauty_amt" , getValueDouble("agen.third_penalty"));
      if (tempMCode <= getValueInt("agen.first_month"))
         {setValueDouble("acmm.penauty_amt" , getValueDouble("agen.first_penalty"));}
      else if (tempMCode <= getValueInt("agen.second_month"))
         {setValueDouble("acmm.penauty_amt" , getValueDouble("agen.second_penalty"));}
/* 20200107 change
      if (getValueInt("int_rate_mcode") <= getValueInt("agen.first_month"))
         {setValueDouble("acmm.penauty_amt" , getValueDouble("agen.first_penalty"));}
      else if (getValueInt("int_rate_mcode") <= getValueInt("agen.second_month"))
         {setValueDouble("acmm.penauty_amt" , getValueDouble("agen.second_penalty"));}
*/

      if (DEBUG) showLogMessage("I","","step 36   違約金["
                                       + getValueInt("acmm.penauty_amt")
                                       + "] 違約金mcode["
                                       + tempMCode
                                       + "] 目前mcode["
                                       + getValueInt("int_rate_mcode")
                                       + "] mcode["
                                       + getValueInt("agen.first_month")
                                       + "]["
                                       + getValueDouble("agen.first_penalty")
                                       + "] mcode["
                                       + getValueInt("agen.second_month")
                                       + "]["
                                       + getValueDouble("agen.second_penalty")
                                       + "] 其他["
                                       + getValueDouble("agen.third_penalty")
                                       +"]");
     }
  /*** 最大固定違約金 ***/
  if (getValueDouble("acmm.penauty_amt") >= getValueDouble("agen.max_penalty"))
     setValue("acmm.penauty_amt" , getValue("agen.max_penalty"));

  /*** 最小固定違約金 ***/
  if (getValueDouble("acmm.penauty_amt")<=getValueDouble("agen.min_penalty"))
     setValueDouble("acmm.penauty_amt" , getValueDouble("agen.min_penalty"));

  if (DEBUG) showLogMessage("I","","step 37   最終違約金["
                                       + getValueInt("acmm.penauty_amt")
                                       + "] 最小固定違約金["
                                       + getValueDouble("agen.min_penalty")
                                       + "] 最大固定違約金["
                                       + getValueDouble("agen.max_penalty")
                                       +"]");
}
// ************************************************************************
void interestPenautyDiscount() throws Exception 
{
  double  wsDiscRate=0,calRate;

  setValue("acmm.interest_amt" , "0");
  for (int intj=0;intj<currCnt;intj++) 
    {
     int intSeq=getValueInt("acul.bill_sort_seq",intj);

     setValue("intr.reason_code"  , "" ,intSeq);
                           
     if (getValue("acur.delaypay_ok_flag",intSeq).equals("Y"))
        {
         if (DEBUG) showLogMessage("I","","step 41   幣別["  
                                         +  getValue("acur.curr_code",intSeq)
                                         + "]  期限內繳足 delaypay_ok_flag["
                                         +  getValue("acur.delaypay_ok_flag",intSeq)
                                         + "]");

         setValue("intr.reason_code"     , "CY01" ,intSeq);
         setValue("cacr.interest_amt"    , "0"    ,intSeq);
         setValue("cacr.dc_interest_amt" , "0"    ,intSeq);
         continue;
        }

     if ((getValue("acur.no_interest_flag",intSeq).equals("Y"))&&
         (getValue("wday.this_acct_month").compareTo(getValue("acur.no_interest_s_month",intSeq))>=0)&&
         (getValue("wday.this_acct_month").compareTo(getValue("acur.no_interest_e_month",intSeq))<=0))
        {
         if (DEBUG) showLogMessage("I","","step 40A  幣別[" 
                                       +  getValue("acur.curr_code",intSeq)
                                       + "]  免息旗標(違約金) 日期[["
                                       + getValue("acur.no_interest_s_month",intSeq)
                                       + "]-["
                                       +  getValue("acur.no_interest_e_month",intSeq)
                                       + "]");

         setValue("intr.reason_code"     , "CY0B" ,intSeq);
         setValue("cacr.interest_amt"    , "0"    ,intSeq);
         setValue("cacr.dc_interest_amt" , "0"    ,intSeq);
         continue;
        }

     if ((getValueDouble("acur.min_pay_bal",intSeq)!=0)||
         ((!getValue("payment_rate1").equals("0A"))&&
          (!getValue("payment_rate1").equals("0B")))||
         ((!getValue("payment_rate2").equals("0A"))&&
          (!getValue("payment_rate2").equals("0B"))))
        { 
         setValue("intr.reason_code"        , "CY03" ,intSeq);
         setValueDouble("acmm.interest_amt" , getValueDouble("acmm.interest_amt")
                                            + getValueDouble("cacr.interest_amt",intSeq));
         if (DEBUG) showLogMessage("I","","step 40   無減免"); 
        }
     else
        {
         if (DEBUG) showLogMessage("I","","step 42   近二期payment_rate["
                                       + getValue("payment_rate1")
                                       + "]-["
                                       + getValue("payment_rate2")
                                       + "] 0A 0B或繳足MP 者");

         if (getValueDouble("acur.last_ttl_amt",intSeq) == 0 )
            {
             if (DEBUG) showLogMessage("I","","step 43   無欠款減免比率 [100]"); 
             wsDiscRate = 100;
            }
         else
            {
              wsDiscRate = (getValueDouble("mp_temp_last_purch_bal",intSeq) 
                           / getValueDouble("acur.last_ttl_amt",intSeq) ) 
                           * 100.0;
             if (DEBUG) showLogMessage("I","","step 44   減免比率 ["
                                       + wsDiscRate
                                       + "]=(計息本金類金額["
                                       + getValueDouble("mp_temp_last_purch_bal",intSeq)
                                       + "]/尚期總欠款["
                                       + getValueDouble("acur.last_ttl_amt",intSeq)
                                       + "])*100");

            }
         if (DEBUG) showLogMessage("I","","step 45   減免比率["
                                       + wsDiscRate
                                       +"] rc減免比率["
                                       + getValueDouble("disc.rc_rate")
                                       +"] rc減免比率下線["
                                       + getValueDouble("disc.rc_rate_limit")
                                       +"]");

         if (DEBUG) showLogMessage("I","","step 46   計息本金類金額["
                                       + getValueDouble("mp_temp_last_purch_bal",intSeq)
                                       +"] 參數減免金額["
                                       + getValueDouble("acur.purch_bal_parm",intSeq)
                                       +"]");

         if ((wsDiscRate  > getValueDouble("disc.rc_rate"))&&
             (getValueDouble("mp_temp_last_purch_bal",intSeq) > getValueDouble("acur.purch_bal_parm",intSeq)))
            {
             setValue("intr.reason_code" , "CY04" ,intSeq);
            }
         else
            {
             if (wsDiscRate  > getValueDouble("disc.rc_rate"))
                {
                 setValue("intr.reason_code" , "CY06" , intSeq);
                 if (wsDiscRate  > getValueDouble("disc.rc_rate_limit"))
                     setValue("intr.reason_code" , "CY05" ,intSeq);
                }
             else
                {
                 setValue("intr.reason_code" , "CY08" , intSeq);
                 if (wsDiscRate  > getValueDouble("disc.rc_rate_limit"))
                    setValue("intr.reason_code" , "CY07" , intSeq);
                }
             calRate = getValueDouble("disc.rc_rate_limit");
             if (wsDiscRate  > getValueDouble("disc.rc_rate_limit")) 
                 calRate = wsDiscRate;

             setValueDouble("cacr.interest_amt" , (getValueDouble("cacr.interest_amt",intSeq)* calRate) 
                                                / 100 ,intSeq);
             setValueDouble("cacr.interest_amt" , commCurrAmt("901",
                                                  getValueDouble("cacr.interest_amt",intSeq),0),intSeq);

             setValueDouble("acmm.interest_amt" , getValueDouble("acmm.interest_amt") 
                                                + getValueDouble("cacr.interest_amt",intSeq));

             setValueDouble("cacr.dc_interest_amt" , (getValueDouble("cacr.dc_interest_amt",intSeq)
                                                   * calRate) / 100 , intSeq);
             setValueDouble("cacr.dc_interest_amt" , commCurrAmt(getValue("acur.curr_code",intSeq)
                                                   , getValueDouble("cacr.dc_interest_amt",intSeq),0)
                                                   , intSeq);

             if (getValue("acur.curr_code",intSeq).equals("901"))
                setValueDouble("cacr.dc_interest_amt" , getValueDouble("cacr.interest_amt",intSeq) , intSeq);

             if (DEBUG) showLogMessage("I","","step 47"
                                             + getValue("acur.curr_code",intSeq)
                                             + "    tot intr_amt["
                                             + getValueDouble("acmm.interest_amt")
                                             + "]");
             if (DEBUG) showLogMessage("I","","        NT last_intr_amt["
                                             + getValueDouble("cacr.interest_amt",intSeq)
                                             + "]");
             if (DEBUG) showLogMessage("I","","        DC last_intr_amt["
                                             + getValueDouble("cacr.dc_interest_amt",intSeq)
                                             +"]");
            }
        }
    }

  if (getValueDouble("acmm.interest_amt") <= getValueDouble("disc.waive_intr_pena"))
     { 
      if (DEBUG) showLogMessage("I","","step 48   利息["
                                      + getValueDouble("acmm.interest_amt")
                                      + "] 免息下限["
                                      + getValueDouble("disc.waive_intr_pena")
                                      + "] 免息違約金");
      setValue("acmm.interest_amt" , "0");
      setValue("acmm.penauty_amt"  , "0"); 
      for (int intj=0;intj<currCnt;intj++) 
        {
         int intSeq=getValueInt("acul.bill_sort_seq",intj);

         setValue("cacr.interest_amt"     , "0" , intSeq); 
         setValue("cacr.dc_interest_amt"  , "0" , intSeq); 
        }
     }
  else if (getValueDouble("acmm.penauty_amt") <= getValueDouble("disc.waive_penauty"))
     {
      if (DEBUG) showLogMessage("I","","step 49   違約金["
                                      + getValueDouble("acmm.penauty_amt")
                                      +"] 違約金下限["
                                      + getValueDouble("disc.waive_penauty")
                                      +"] 免息違約金");
      setValue("acmm.penauty_amt" , "0");
     }
  if (DEBUG) showLogMessage("I","","step 50   利息["
                                  + getValueDouble("acmm.interest_amt")
                                  + "] 違約金["
                                  + getValueDouble("acmm.penauty_amt")
                                  + "]");
 }
// ************************************************************************
 void loadBilContract() throws Exception
 {
  extendField = "cont.";
  selectSQL = "b.reference_no as cont_reference_no,"
            + "a.install_tot_term,"
            + "a.install_curr_term,"
            + "a.int_rate,"
            + "a.int_rate_flag,"
            + "a.rowid as rowid";
  daoTable  = "bil_contract a,bil_bill b";
  whereStr  = "WHERE a.contract_no     = b.contract_no "
            + "AND   a.contract_seq_no = b.contract_seq_no "
            + "AND   b.acct_code       = 'IT' "
            + "AND   b.stmt_cycle      = ? "
            + "AND   b.acct_month      = ? ";

  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , getValue("wday.this_acct_month"));
   
  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and b.p_seqno = ? "; 
      setString(3 , pSeqno);
     }

  int  n = loadTable();

  setLoadData("cont.cont_reference_no");

  showLogMessage("I","","Load bil_contract Count: ["+n+"]");
 }
// ************************************************************************
 void  loadCycAcmm() throws Exception
 {
  extendField = "acmm.";
  selectSQL = "p_seqno,"
            + "rowid as rowid";
  daoTable  = "cyc_acmm_"+getValue("wday.stmt_cycle");
  whereStr  = "where 1 = 1 ";

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and p_seqno = ? "; 
      setString(1 , pSeqno);
     }

  int  n = loadTable();

  setLoadData("acmm.p_seqno");

  showLogMessage("I","","Load cyc_acmm Count: ["+n+"]");
 }
// ************************************************************************
 void  loadPtrCurrGeneral() throws Exception
 {
  extendField = "pcgl.";
  selectSQL = "curr_code,"
            + "purch_bal_parm,"
            + "purch_bal_wave,"
            + "total_bal";
  daoTable  = "ptr_curr_general";
  whereStr  = "";

  int  n = loadTable();

  setLoadData("pcgl.curr_code");

  showLogMessage("I","","Load ptr_curr_general Count: ["+n+"]");
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
 void  loadPtrAcctType() throws Exception
 {
  extendField = "agen.";
  selectSQL = "e.acct_type,"
            + "c.revolving_interest1,"
            + "c.revolving_interest2,"
            + "c.revolving_interest3,"
            + "c.revolving_interest4,"
            + "c.revolving_interest5,"
            + "c.revolving_interest6,"
            + "c.rc_max_rate,"
            + "d.method,"
            + "d.fix_penalty,"
            + "d.percent_penalty,"
            + "d.max_penalty,"
            + "d.min_penalty,"
            + "d.first_month,"
            + "d.first_penalty,"
            + "d.second_month,"
            + "d.second_penalty,"
            + "d.third_month,"
            + "d.third_penalty,"
            + "decode(d.pn_max_cnt,0,3,d.pn_max_cnt) as pn_max_cnt,"
            + "e.rc_use_flag";
  daoTable  = "ptr_actgeneral_n c,ptr_actpenalty d ,ptr_acct_type e";
  whereStr  = "WHERE   c.acct_type = d.acct_type "
            + "AND     d.acct_type = e.acct_type ";

  int  n = loadTable();

  setLoadData("agen.acct_type");

  showLogMessage("I","","Load ptr_acct_type Count: ["+n+"]");
 }
// ************************************************************************
 void insertBilSysexp() throws Exception
 {
  daoTable    = "bil_sysexp";
  extendField = "syse.";

  setValue("syse.acct_type"          , getValue("acct_type"));
  setValue("syse.p_seqno"            , getValue("p_seqno"));
  setValue("syse.curr_code"          , getValue("syse.curr_code"));
  setValue("syse.card_no"            , "");
  setValue("syse.bill_type"          , "OSSG");
  setValue("syse.txn_code"           , getValue("syse.txn_code"));
  setValue("syse.purchase_date"      , business_date);
  setValue("syse.dest_amt"           , getValue("syse.dest_amt"));
  setValue("syse.dc_dest_amt"        , getValue("syse.dc_dest_amt"));
  setValue("syse.src_amt"            , getValue("syse.src_amt"));
  setValue("syse.dest_curr"          , "901");
  setValue("syse.post_flag"          , "U");
  setValue("syse.mod_pgm"            , javaProgram);
  setValue("syse.mod_time"           , sysDate+sysTime);

  insertTable();
  return;
}
// ************************************************************************
 void insertBilAdiexp() throws Exception
 {
  daoTable    = "bil_adiexp";
  extendField = "adie.";

  setValue("adie.acct_type"          , getValue("acct_type"));
  setValue("adie.p_seqno"            , getValue("p_seqno"));
  setValue("adie.id_p_seqno"         , getValue("id_p_seqno"));
  setValue("adie.card_no"            , "");
  setValue("adie.bill_type"          , "OSSG");
  setValue("adie.txn_code"           , "AI");
  setValue("adie.purchase_date"      , business_date);
  setValue("adie.beg_bal"            , getValue("adi_beg_bal"));
  setValue("adie.end_bal"            , getValue("adi_end_bal"));
  setValue("adie.d_avail_bal"        , getValue("adi_d_avail")); 
  setValue("adie.post_flag"          , "U");
  setValue("adie.mod_pgm"            , javaProgram);
  setValue("adie.mod_time"           , sysDate+sysTime);

  insertTable();
  return;
}
// ************************************************************************
 void deleteBilBillDebug() throws Exception
 {
  daoTable  = "bil_bill";
  whereStr  = "WHERE  reference_no = ? ";

  setString(1, getValue("reference_no"));

  deleteTable();

  if (!notFound.equals("Y") ) debtFlag=1;
  
  return;
 }
// ************************************************************************
 void deleteActDebtDebug() throws Exception
 {
  daoTable  = "act_debt";
  whereStr  = "WHERE  reference_no = ? ";

  setString(1, getValue("reference_no"));

  deleteTable();

  return;
 }
// ************************************************************************
 void deleteBilSysexpHstDebug() throws Exception
 {
  daoTable  = "bil_sysexp_hst";
  whereStr  = "WHERE  rowid = ? ";

  setString(1, getValue("rowid"));

  deleteTable();

  return;
 }
// ************************************************************************
 void deleteActIntrDebug() throws Exception
 {
  daoTable  = "act_intr";
  whereStr  = "WHERE acct_month  = ? "
            + "AND   mod_pgm     = ? "
            + "AND   update_date = ? "
            ;

  setString(1, getValue("wday.this_acct_month"));
  setString(2, javaProgram);
  setString(3, business_date);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and p_seqno = ? "; 
      setString(4 , pSeqno);
     }
  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void deleteBilAdiexpDebug() throws Exception
 {
  daoTable  = "bil_adiexp";
  whereStr  = "WHERE  purchase_date = ? "
            + "AND    mod_pgm       = ? "
            ;

  setString(1, business_date);
  setString(2, javaProgram);
  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and p_seqno = ? "; 
      setString(3 , pSeqno);
     }

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void deleteBilSysexpDebug() throws Exception
 {
  daoTable  = "bil_sysexp";
  whereStr  = "WHERE  purchase_date = ? "
            + "AND    mod_pgm       = ? "
            ;

  setString(1, business_date);
  setString(2, javaProgram);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and p_seqno = ? "; 
      setString(3 , pSeqno);
     }
  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void deleteActPenaltyLogDebug() throws Exception
 {
  daoTable  = "act_penalty_log a";
  whereStr  = "WHERE acct_month = ? "
            + "AND   stmt_cycle = ? "
            + "AND   exists (select 1 "
            + "              from  cyc_acmm_"+getValue("wday.stmt_cycle")
            + "              where p_seqno = a.p_seqno) "
            ;

  setString(1, getValue("wday.this_acct_minth"));
  setString(2, getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and p_seqno = ? "; 
      setString(3 , pSeqno);
     }
  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void deleteCycAbemDebug() throws Exception
 {
  daoTable  = "cyc_abem_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE  print_type in ('01','07') "
            + "AND    mod_pgm    = ? ";

  setString(1, javaProgram);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and p_seqno = ? "; 
      setString(2 , pSeqno);
     }

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 int updateActAcctDebug() throws Exception
 {
  daoTable  = "act_acct a";
  updateSQL = "acct_jrnl_bal   = acct_jrnl_bal  - ?";
  whereStr  = "WHERE p_seqno   = ? ";

  setDouble(1 , getValueDouble("dest_amt"));
  setString(2 , getValue("p_seqno"));


  updateTable();

  return 0;
 }
// ************************************************************************
 void procBilSysexpHstDebug() throws Exception
 {
  selectSQL = "a.reference_no,"
            + "b.p_seqno,"
            + "a.curr_code,"
            + "a.dest_amt,"
            + "a.dc_dest_amt,"
            + "a.rowid as rowid";
  daoTable  = "bil_sysexp_hst a,crd_card b";
  whereStr  = "WHERE a.crt_date   = ? "
            + "AND   a.mod_pgm    = ? "
            + "AND   a.card_no    = b.card_no "
            + "AND   b.stmt_cycle = ? "
            ;

  setString(1 , business_date);
  setString(2 , javaProgram);
  setString(3 , getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and b.p_seqno = ? "; 
      setString(4 , pSeqno);
     }

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    deleteBilBillDebug();
    deleteActDebtDebug();
    deleteBilSysexpHstDebug();
    if (debtFlag==1)
       {
        updateActAcctDebug();
        updateActAcctCurrDebug();
       }
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void procAdiDebug() throws Exception
 {
  selectSQL = "reference_no,"
            + "curr_code,"
            + "p_seqno,"
            + "dest_amt,"
            + "dc_dest_amt,"
            + "rowid as rowid";
  daoTable  = "bil_bill";
  whereStr  = "WHERE   acct_month = ? "
            + "AND     acct_code  = 'AI' "
            + "AND     stmt_cycle = ?  "
            ;

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and p_seqno = ? "; 
      setString(3 , pSeqno);
     }

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    deleteBilBillDebug();
    deleteActDebtDebug();
    updateActAcctDebug();
    updateActAcctCurrDebug();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int updateActAcctCurrDebug() throws Exception
 {
  daoTable  = "act_acct_curr a";
  updateSQL = "acct_jrnl_bal    = acct_jrnl_bal    - ?,"
            + "dc_acct_jrnl_bal = dc_acct_jrnl_bal - ?,"
            + "mod_time           = sysdate,"
            + "mod_pgm            = ? ";
  whereStr  = "WHERE p_seqno    = ? "
            + "AND   curr_code  = ? ";

  setDouble(1 , getValueDouble("dest_amt"));
  setDouble(2 , getValueDouble("dc_dest_amt"));
  setString(3 , javaProgram);
  setString(4 , getValue("p_seqno"));
  setString(5 , getValue("curr_code"));


  updateTable();

  return 0;
 }
// ************************************************************************ 
 int updateActDebt2(int inti) throws Exception
 {
  daoTable  = "act_debt";
  updateSQL = "int_rate           = ?,"
            + "int_rate_flag      = ?,"
            + "mod_time           = sysdate,"
            + "mod_pgm            = ? ";
  whereStr  = "where reference_no = ? ";

  setDouble(1 , getValueDouble("debt.int_rate",inti));
  setString(2 , getValue("debt.int_rate_flag",inti));
  setString(3 , javaProgram);
  setString(4 , getValue("debt.reference_no",inti));


  int n = updateTable();
  if ( n == 0 )
     { showLogMessage("E","","update_act_debt_2 ERROR ");  }

  return n;
 }
// ************************************************************************
 int updateActDebt(int inti) throws Exception
 {
  daoTable  = "act_debt";
  updateSQL = "int_rate_flag      = 'Y',"
            + "int_rate           = ?,"
            + "mod_time           = sysdate,"
            + "mod_pgm            = ? ";
  whereStr  = "where reference_no = ? ";
   
  setDouble(1 , getValueDouble("debt.int_rate",inti));
  setString(2 , javaProgram);
  setString(3 , getValue("debt.reference_no",inti));

  int n = updateTable();  
  if ( n == 0 )
     { showLogMessage("E","","update_act_debt ERROR ");  }

  return n;
 }
// ************************************************************************
 int updateActDebt1(int inti) throws Exception
 {
  showLogMessage("I","","DEBT RS : "+ getValue("debt.reference_no",inti) + " " 
                                    + getValue("debt.interest_rs_date",inti));
  daoTable  = "act_debt";
  updateSQL = "interest_rs_date   = ''";
  whereStr  = "where reference_no = ? ";
   
  setString(1 , getValue("debt.reference_no",inti));

  int n = updateTable();  
  if ( n == 0 )
     { showLogMessage("E","","update_act_debt_1 ERROR ");  }

  return n;
 }
// ************************************************************************
 int updateBilContract(int inti) throws Exception
 {
  if (pSeqno.length()!=0)
     {
      showLogMessage("I","", "STEP P_SEQNO-007  int_rate        [" + getValueDouble("debt.int_rate",inti) +"]");
      showLogMessage("I","", "STEP P_SEQNO-008  rowid           [" + getValue("cont.rowid") +"]");
     }
  daoTable  = "bil_contract";
  updateSQL = "int_rate             = ?,"
            + "int_rate_flag        = 'Y',"
            + "mod_time             = sysdate,"
            + "mod_pgm              = ? ";
  whereStr  = "where rowid = ? ";

  setDouble(1 , getValueDouble("debt.int_rate",inti));
  setString(2 , javaProgram);
  setRowId(3  , getValue("cont.rowid"));


  int n = updateTable();
  if ( n == 0 )
     { showLogMessage("E","","update_bil_contract ERROR ");  }

  return n;
 }
// ************************************************************************
 void insertActIntr(int intk,int inti,int intSeq) throws Exception
 {
  daoTable    = "act_intr";
  extendField = "intr.";

  setValue("intr.p_seqno"            , getValue("p_seqno"));
  setValue("intr.curr_code"          , getValue("acur.curr_code",intSeq));
  setValue("intr.acct_type"          , getValue("acct_type"));
  setValue("intr.post_date"          , getValue("wday.this_close_date"));
  setValue("intr.acct_month"         , getValue("wday.this_acct_month"));
  setValue("intr.stmt_cycle"         , getValue("wday.stmt_cycle"));

  if (intk==0)
     {
      setValue("intr.intr_org_captial"     , String.format("%.2f",getValueDouble("debt.end_bal",inti)));
      setValue("intr.dc_intr_org_captial"  , String.format("%.4f",getValueDouble("debt.dc_end_bal",inti)));
      setValue("intr.intr_s_date"          , getValue("intr.intr_s_date"));
      setValue("intr.intr_e_date"          , getValue("intr.intr_e_date"));
      setValue("intr.interest_sign"        , "+");
      setValue("intr.interest_amt"         , String.format("%.2f",getValueDouble("intr.interest_amt")));
      setValue("intr.dc_interest_amt"      , String.format("%.4f",getValueDouble("intr.dc_interest_amt")));
      setValue("intr.inte_d_amt"           , String.format("%.2f",getValueDouble("intr.interest_amt")));
      setValue("intr.dc_inte_d_amt"        , String.format("%.4f",getValueDouble("intr.dc_interest_amt")));
      setValue("intr.interest_rate"        , String.format("%.3f",getValueDouble("intr.interest_rate")));
      setValue("intr.reference_no"         , getValue("debt.reference_no",inti));
      setValue("intr.ao_flag"              , getValue("debt.ao_flag",inti));
     }
  else
     {
      setValue("intr.intr_org_captial"      , "0");
      setValue("intr.dc_intr_org_captial"   , "0");
      setValue("intr.intr_s_date"           , "");
      setValue("intr.intr_e_date"           , "");
      setValue("intr.interest_sign"         , "-");
      setValue("intr.interest_amt"          , String.format("%.2f",getValueDouble("temp.org_interest_amt",intSeq)
                                            - getValueDouble("cacr.interest_amt",intSeq)));
      setValue("intr.dc_interest_amt"       , String.format("%.4f",getValueDouble("temp.dc_org_interest_amt",intSeq)
                                            - getValueDouble("cacr.dc_interest_amt",intSeq)));
      setValue("intr.inte_d_amt"            , "0");
      setValue("intr.dc_inte_d_amt"         , "0");
      setValue("intr.interest_rate"         , "0");
      setValue("intr.reference_no"          , "");
      setValue("intr.ao_flag"              , "");
     }    

  setValue("intr.reason_code"        , getValue("intr.reason_code"));
  setValue("intr.update_date"        , business_date);
  setValue("intr.crt_date"           , sysDate);
  setValue("intr.crt_time"           , sysTime);
  setValue("intr.mod_pgm"            , javaProgram);
  setValue("intr.mod_time"           , sysDate+sysTime);

  insertTable();
  return;
}
// ************************************************************************
void procInterest(int inti,int intSeq) throws Exception 
 {
   double tempIntRate=0,minIntRate,totIntAmt=0;
   String tempIntSign="",intrSDate="",intrEDate="";
   int    standFlag=0;

   if (getValue("debt.post_date",inti).compareTo(getValue("wday.last_close_date"))<=0)
      setValueDouble("mp_temp_last_purch_bal" , getValueDouble("mp_temp_last_purch_bal",intSeq)
                                              + getValueDouble("debt.end_bal",inti) ,intSeq);
   /***** 設定利息起算日 START ************************************************************/
   if (getValue("debt.post_date",inti).compareTo(getValue("wday.ll_close_date"))>0)
      { 
       if (getValue("debt.new_it_flag",inti).equals("Y"))
          {
           setValue("intr.intr_s_date" , getValue("wday.last_lastpay_date"));
          }
       else
          {
           setValue("intr.intr_s_date" , getValue("debt.interest_date",inti));
          }  
      }
   else
      {
       if ((getValue("new_cycle_month").length()>0)&&
           (getValue("wday.this_acct_month").compareTo(getValue("new_cycle_month"))==0))
          {
           /* 變更cycle以前cycle利息起算日 */
           setValue("intr.intr_s_date" , getValue("last_interest_date"));
          }
       else
          {
           setValue("intr.intr_s_date" , getValue("wday.last_interest_date"));
          }
      }
   if (getValue("debt.interest_rs_date",inti).length() == 8)
      setValue("intr.intr_s_date" , getValue("debt.interest_rs_date",inti));

   if (getValue("intr.intr_s_date").equals("00000000"))
      setValue("intr.intr_s_date" , getValue("debt.post_date",inti));

   if (getValue("debt.post_date" ,inti).equals("00000000")) return;
 
// if (getValue("debt.acct_month",inti).compareTo(getValue("wday.this_acct_month"))>=0) return;
//  fix BECS-1060614-063  ny MEGA IN 108/04/17  V1.25.02

//    showLogMessage("I","", "step 005c["+ getValue("debt.acct_month" ,inti) +"]");

   fitRevolvingRate1  = tempRevolvingInterest[getValueInt("debt.inter_rate_code",inti)];

   if (getValue("debt.ao_flag",inti).equals("Y"))     /* 代償使用第二段利率, 非 act_commute */
      fitRevolvingRate1 = tempRevolvingInterest[getValueInt("debt.inter_rate_code2",inti)]; 

   int cnt3=0;
   if ((getValue("debt.acct_code",inti).equals("IT"))&&
       (getValue("debt.acct_month",inti).compareTo(getValue("wday.this_acct_month"))==0)&&
       (!getValue("debt.int_rate_flag",inti).equals("Y")))
      {
       setValue("cont.cont_reference_no" , getValue("debt.reference_no",inti));
       cnt3 = getLoadData("cont.cont_reference_no");

       if (cnt3>0)
       if (getValue("cont.int_rate_flag").equals("Y"))
          {
           setValue("debt.int_rate_flag" , "Y" ,inti);
           setValue("debt.int_rate"      , getValue("cont.int_rate")  , inti);
           updateActDebt2(inti);
          }
      }
   /***** 設定利息起算日 END **************************************************************/
   if ((getValueInt("int_rate_mcode")==0)&&
       (getValue("debt.int_rate_flag",inti).equals("Y")))
      {
       setValue("intr.interest_rate" , getValue("debt.int_rate",inti));
       if (DEBUG) showLogMessage("I","","step 11    分期 interest_rate["
                                       + getValue("intr.interest_rate")
                                       + "]");
      }
   else
      {
       standFlag = 0;
       /***** 設定循環息利率 START ************************************************************/
       setValueDouble("intr.interest_rate" , fitRevolvingRate1);

       if (DEBUG) showLogMessage("I","","step 21   標準  interest_rate1["
                                       + getValue("intr.interest_rate")
                                       + "]-["
                                       + standFlag
                                       + "]");

//    showLogMessage("I","", "step 005d["+ getValue("debt.acct_month" ,inti) +"]");
       minIntRate = 0;
       if ((getValue("wday.this_acct_month").compareTo(getValue("revolve_rate_s_month"))>=0)&&
           (getValue("wday.this_acct_month").compareTo(getValue("revolve_rate_e_month"))<= 0))
           minIntRate = getValueDouble("revolve_int_rate");

       if ((getValue("wday.this_acct_month").compareTo(getValue("group_rate_s_month"))>=0)&&
           (getValue("wday.this_acct_month").compareTo(getValue("group_rate_e_month"))<= 0))
          {
           if (minIntRate>getValueDouble("group_int_rate")) 
              minIntRate = getValueDouble("group_int_rate");
          }

       if ((getValue("wday.this_acct_month").compareTo(getValue("batch_rate_s_month"))>=0)&&
           (getValue("wday.this_acct_month").compareTo(getValue("batch_rate_e_month"))<= 0))
          {
           if (minIntRate>getValueDouble("batch_int_rate"))
              minIntRate = getValueDouble("batch_int_rate");
          }

       setValueDouble("intr.interest_rate" , getValueDouble("intr.interest_rate")
                                           + minIntRate);

       if (DEBUG) showLogMessage("I","","step 22   減碼後  利率["
                                       + String.format("%.3f",getValueDouble("intr.interest_rate"))
                                       + "]- org["
                                       + standFlag
                                       + "]  減碼["
                                       + String.format("%.3f",minIntRate)
                                       + "]");

/***
       if (getValue("debt.ao_flag",inti).equals("Y"))    // 代償 
          {
           setValue("acom.reference_no" , getValue("debt.reference_no",inti));
           int cnt4 = getLoadData("acom.reference_no");

           if ((cnt4>0)&&
               (getValueDouble("intr.interest_rate") > getValueDouble("acom.ao_int_rate")))
               {   
                setValue("intr.interest_rate" , getValue("acom.ao_int_rate"));
                standFlag=1;

                if (DEBUG) showLogMessage("I","", "step 23 interest_rate["
                                                + getValue("intr.interest_rate")
                                                + "]-["
                                                + standFlag
                                                + "]");
               }
          }
       else if ((getValue("new_bill_flag").equals("Y"))&&   // 代償戶之新增'BL'消費 
                (getValue("debt.acct_code",inti).equals("BL")))
          {
           if((getValue("debt.purchase_date",inti).compareTo(getValue("ao_posting_date"))>=0)&&
              (getValue("ao_posting_date").length()>0))
             {
              if ((getValueDouble("intr.interest_rate") > getValueDouble("aox_int_rate"))&&
                  (getValueDouble("aox_int_rate") > 0))
                 {
                  setValue("intr.interest_rate" , getValue("aox_int_rate"));
                  standFlag=1;
                 }
             }
          }
***/

       if (pSeqno.length()!=0)
          {
           showLogMessage("I","", "STEP P_SEQNO-000  reference_no    [" + getValue("debt.reference_noh",inti) +"]");
           showLogMessage("I","", "STEP P_SEQNO-001  this_acct_month [" + getValue("wday.this_acct_month") +"]");
           showLogMessage("I","", "STEP P_SEQNO-002  debt.acct_month [" + getValue("debt.acct_month",inti) +"]");
           showLogMessage("I","", "STEP P_SEQNO-003  interest_rate   [" + String.format("%.3f", getValueDouble("intr.interest_rate")) +"]");
           showLogMessage("I","", "STEP P_SEQNO-004  rc_max_rate     [" + getValueDouble("agen.rc_max_rate") +"]");
          }

       if (getValueDouble("intr.interest_rate") < 0)
          setValueDouble("intr.interest_rate" , 0);   // mantis 9335

       if (getValueDouble("agen.rc_max_rate") > 0)
          if (getValueDouble("intr.interest_rate") > getValueDouble("agen.rc_max_rate"))
             setValueDouble("intr.interest_rate" , getValueDouble("agen.rc_max_rate"));   // mantis 9335

       if (pSeqno.length()!=0)
           showLogMessage("I","", "STEP P_SEQNO-005  interest_rate   [" + String.format("%.3f", getValueDouble("intr.interest_rate")) +"]");

       if (getValue("wday.this_acct_month").equals(getValue("debt.acct_month",inti)))
          {
           setValue("debt.int_rate" , getValue("intr.interest_rate") , inti);
           if (standFlag == 0) updateActDebt(inti);

           if (pSeqno.length()!=0)
              showLogMessage("I","", "STEP P_SEQNO-003  acct_code       [" + getValue("debt.acct_code",inti) +"][" + cnt3 +"]");
           if ((getValue("debt.acct_code",inti).equals("IT"))&&(cnt3>0))
              {
               if (pSeqno.length()!=0)
                  {
                   showLogMessage("I","", "STEP P_SEQNO-004  int_rate_flag   [" + getValue("cont.int_rate_flag") +"]");
                   showLogMessage("I","", "STEP P_SEQNO-005  tot_term        [" + getValueInt("cont.install_tot_term") +"]");
                   showLogMessage("I","", "STEP P_SEQNO-006  curr_term       [" + getValueInt("cont.install_curr_term") +"]");
                  }
               if ((!getValue("cont.int_rate_flag").equals("Y"))&&
                   (getValueInt("cont.install_tot_term")>1)&&
                   (getValueInt("cont.install_curr_term")==1))
                  updateBilContract(inti);
              }
          }
       /***** 設定循環息利率 END **************************************************************/
      }
   setValue("intr.interest_amt" , "0");
   if (getValue("debt.post_date",inti).compareTo(getValue("wday.last_close_date"))>0) return;

   if ((getValue("new_cycle_month").length()>0)&&   /* 變更 cycle */
       (getValue("wday.this_acct_month").compareTo(getValue("new_cycle_month"))==0))
      if (getValue("debt.post_date",inti).compareTo(getValue("last_interest_date"))>0) return;

   setValue("intr.intr_e_date" , business_date);

   int cal_days = comm.datePeriod(getValue("intr.intr_s_date"),business_date);
    
// showLogMessage("I","","step 006-D ["+getValue("intr.intr_s_date")+"]");
// showLogMessage("I","","step 006-2 ["+ cal_days +"]");
// showLogMessage("I","","step 006-3 ["+ fit_revolving_rate1 +"]");

   if (getValueDouble("intr.interest_rate") > fitRevolvingRate1)
      setValueDouble("intr.interest_rate" , fitRevolvingRate1);


   setValueDouble("intr.interest_amt"    , cal_days 
                                         * getValueDouble("debt.end_bal",inti)    
                                         * getValueDouble("intr.interest_rate")
                                         / 10000.0);

// showLogMessage("I","","step 006-1 ["+ getValue("intr.interest_amt") +"]");

   setValueDouble("intr.dc_interest_amt"  , cal_days 
                                          * getValueDouble("debt.dc_end_bal",inti) 
                                          * getValueDouble("intr.interest_rate")
                                          /10000.0);

// showLogMessage("I","","step 006-2 ["+ getValue("intr.interest_amt") +"]");


   setValueDouble("intr.interest_amt"    , (int)Math.round(getValueDouble("intr.interest_amt")*100.0 + 0.00001)/100.0);
// showLogMessage("I","","step 006-3 ["+ getValue("intr.interest_amt") +"]");

   setValueDouble("intr.dc_interest_amt" , (int)Math.round(getValueDouble("intr.dc_interest_amt")*100.0 + 0.00001)/100.0);
// showLogMessage("I","","step 006-4 ["+ getValue("intr.dc_interest_amt") +"]");

// showLogMessage("I","","step 007-1 ["+ getValueDouble("intr.interest_amt") +"]");
// showLogMessage("I","","step 007-2 ["+ getValueDouble("intr.dc_interest_amt") +"]");

   if (DEBUG) showLogMessage("I","","step 26-["
                                   + getValue("debt.curr_code",inti)
                                   + "]    計息期間["
                                   + getValue("intr.intr_s_date")
                                   + "]=["
                                   + getValue("intr.intr_e_date")
                                   + "] 利率["
                                   + String.format("%.4f",getValueDouble("intr.interest_rate"))
                                   + "]  計息天數["
                                   + cal_days
                                   +"]");
   if (DEBUG) showLogMessage("I","","       -NT 計息金額["
                                   + String.format("%.2f",getValueDouble("debt.end_bal",inti))
                                   +"] 利息["
                                   + String.format("%.2f",getValueDouble("intr.interest_amt"))
                                   +"]");

   if (DEBUG) showLogMessage("I","", "       -DC 計息金額["
                                   + String.format("%.2f",getValueDouble("debt.dc_end_bal",inti))
                                   + "] 利息["
                                   + String.format("%.2f",getValueDouble("intr.dc_interest_amt"))
                                   + "]");
   if (getValueDouble("intr.dc_interest_amt") > 0 )
      {
       setValue("intr.reason_code" , "CY0A");
       insertActIntr(0,inti,intSeq);
      }

   setValueDouble("temp_interest_amt"     , getValueDouble("temp_interest_amt")  
                                          + getValueDouble("intr.interest_amt"));
   setValueDouble("temp.interest_amt"     , getValueDouble("temp.interest_amt",intSeq) 
                                          + getValueDouble("intr.interest_amt")    , intSeq);
   setValueDouble("temp.dc_interest_amt"  , getValueDouble("temp.dc_interest_amt",intSeq) 
                                          + getValueDouble("intr.dc_interest_amt") , intSeq);

   if (DEBUG) showLogMessage("I","", "step 28-["
                                   + getValue("debt.curr_code",inti)
                                   + "]   台幣利息累計欠款總額["
                                   + String.format("%.0f",getValueDouble("temp_interest_amt"))
                                   + "]");
   if (DEBUG) showLogMessage("I","", "       -NT 累計利息["
                                   + String.format("%.2f",getValueDouble("temp.interest_amt",intSeq))
                                   + "]");
   if (DEBUG) showLogMessage("I","","       -DC 累計利息["
                                   + String.format("%.2f",getValueDouble("temp.dc_interest_amt",intSeq))
                                   + "]");
 }
// ************************************************************************
 void  displayMemory() throws Exception
 {
    long total = (long) (Runtime.getRuntime().totalMemory() / (1024 * 1024) );
    long free  = (long) (Runtime.getRuntime().freeMemory()  / (1024 * 1024) );
    long use   = total - free;
    showLogMessage("I","","TOTAL : " + total + " FREE : " +free+ " USE : "+use);

    return;
 }
// ************************************************************************


}  // End of class FetchSample

