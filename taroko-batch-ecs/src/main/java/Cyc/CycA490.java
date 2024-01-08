/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/11/20  V1.00.16  Allen Ho   cyc_A320 V2.12.03 108/12/31                *
* 111/10/18  V1.00.02  Yang Bo    sync code from mega                        *
* 112-06-21  V1.00.03  Simon      新增維護 act_acno.last_stmt_date           *
******************************************************************************/
package Cyc;

import com.*;

import java.util.*;
import java.lang.*;

@SuppressWarnings("unchecked")
public class CycA490 extends AccessDAO
{
 private final String PROGNAME = "關帳-更改帳務主檔及新增歷史檔處理程式 112/06/21  V1.00.03";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";

 long    totalCnt=0,currCnt=0;
 final boolean DEBUG = false;
 final boolean DEBUG_P = false;
 String pSeqno = "0001437503";
 String hStmtOnFlag = "";

 int paymentAmt = 0,insertCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA490 proc = new CycA490();
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
   showLogMessage("I","",javaProgram+" "+ PROGNAME);

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
      { businessDate = args[0]; }
   
   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   if (DEBUG)                    
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","還原 act_acno...");
       updateActAcnoDebug();
       showLogMessage("I","","=========================================");
       showLogMessage("I","","還原 act_acct...");
       updateActAcctDebug();
       commitDataBase();
      }

   showLogMessage("I；","","====================================");
   showLogMessage("I","","載入暫存資料");
   loadActAcag();
   showLogMessage("I","","====================================");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新 act_acno...");
   totalCnt=0;
   selectCycAcmm();
   showLogMessage("I","","total_count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新 act_acct...");
   totalCnt=0;
   selectCycAcmm1();
   showLogMessage("I","","total_count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新 act_acct_curr...");
   totalCnt=0;
   selectCycAcmmCurr();
   showLogMessage("I","","total_count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新 act_acno payment_rate1 只欠年費...");
   totalCnt=0;
   selectActAcctHst();
   showLogMessage("I","","total_count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");
   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
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
      businessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
 }
// ************************************************************************ 
  public int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1, businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void selectCycAcmm() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.acct_type,"
            + "a.last_ttl_amt,"
            + "a.this_ttl_amt,"
            + "a.payment_amt,"
            + "a.adjust_amt,"
            + "a.minimum_pay_bal,"
            + "a.spec_flag,"
            + "a.stmt_new_amt,"
            + "a.ll_stmt_new_amt,"
            + "a.repay_amt,"
            + "b.acct_jrnl_bal as jrnl_bal," // changed
            + "b.min_pay,"
            + "b.min_pay_bal,"
            + "b.last_min_pay_date,"
            + "b.last_cancel_debt_date,"
            + "b.ttl_amt,"
            + "b.ttl_amt_bal,"
            + "b.rowid as acct_rowid,"
            + "d.payment_rate1,"
            + "d.worse_mcode,"
            + "d.spec_flag_month,"
            + "d.rowid as acno_rowid,"
            + "e.mix_mp_balance ";
  daoTable  = "act_acct b,act_acno d,cyc_acmm_"+ getValue("wday.stmt_cycle") +" a,ptr_actgeneral_n e"; 
  whereStr  = "WHERE b.p_seqno      = d.p_seqno "
            + "and   d.p_seqno      = a.p_seqno "
            + "and   d.p_seqno      = d.acno_p_seqno "
            + "and   a.acct_type    = e.acct_type ";


  if (DEBUG_P)
     whereStr  = whereStr 
               + "and   d.p_seqno      = '" + pSeqno +"' ";

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    if (getValue("last_min_pay_date").length()==0) 
       setValue("last_min_pay_date","99999999");

    if (getValue("last_cancel_debt_date").length()==0) 
       setValue("last_cancel_debt_date","99999999");

    setValue("new.spec_flag_month", "");
    if (getValue("spec_flag_month").length()==0)
        setValue("spec_flag_month","200001");
     else
       setValue("new.spec_flag_month", comm.nextMonth(getValue("spec_flag_month")));

    setValue("spec_flag_month", comm.nextMonth(getValue("spec_flag_month")));

    if (getValueDouble("this_ttl_amt") != 0  ||
        getValueDouble("payment_amt")  != 0  ||
        getValueDouble("adjust_amt")   != 0  ||  
        getValueDouble("stmt_new_amt") != 0     ) 
    {
    	hStmtOnFlag = "Y";
    } else {
    	hStmtOnFlag = "";
    }

    setValue("payment_rate" , "");
    processPaymentRate();
    updateActAcno();
    if (DEBUG_P) showLogMessage("I","","  STEP 1 ["+getValue("payment_rate")+"]");

    if (totalCnt%100000==0)
      {
       showLogMessage("I","","    Proc Records :  "+totalCnt);
       countCommit();
      }   
   } 

  closeCursor();
  countCommit();
  return;
 }
// ************************************************************************
 void selectCycAcmm1() throws Exception
 {
  selectSQL = "p_seqno,"
            + "b.this_minimum_pay as min_pay,"
            + "b.this_minimum_pay as min_pay_bal,"
            + "b.auto_payment_amt as autopay_beg_amt,"
            + "b.auto_payment_amt as  autopay_bal,"
            + "b.this_purc_end_bal as last_ttl_amt,"
            + "b.this_ttl_amt as ttl_amt_bal,"
            + "b.this_ttl_amt as ttl_amt,"
            + "decode(b.rc_indicator,'3',0,"
            + "  decode(b.this_minimum_pay,0,0,-1)) as rc_min_pay,"
            + "decode(b.rc_indicator,'3',0,"
            + "  decode(b.this_minimum_pay,0,0,-1)) as rc_min_pay_bal,"
            + "decode(b.rc_indicator,'3',0,"
            + "  decode(b.this_minimum_pay,0,0,-1)) as rc_min_pay_m0";
  daoTable  = "cyc_acmm_"+ getValue("wday.stmt_cycle") +" b"; 
  whereStr  = "";

  if (DEBUG_P)
     whereStr  = whereStr 
               + "where   p_seqno      = '" + pSeqno +"' ";

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    updateActAcct();

    if (totalCnt%100000==0)
      {
       showLogMessage("I","","    Proc Records :  "+totalCnt);
       countCommit();
      }   
   } 
  closeCursor();
  countCommit();
  return;
 }
// ************************************************************************
 void selectCycAcmmCurr() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "this_minimum_pay     as min_pay,"
            + "dc_this_minimum_pay  as dc_min_pay,"
            + "this_minimum_pay     as min_pay_bal,"
            + "dc_this_minimum_pay  as dc_min_pay_bal,"
            + "auto_payment_amt as  autopay_beg_amt,"
            + "dc_auto_payment_amt  as dc_autopay_beg_amt,"
            + "auto_payment_amt as  autopay_bal,"
            + "dc_auto_payment_amt  as dc_autopay_bal,"
            + "this_purc_end_bal    as last_ttl_amt,"
            + "dc_this_purc_end_bal as dc_last_ttl_amt,"
            + "this_ttl_amt         as ttl_amt_bal,"
            + "dc_this_ttl_amt      as dc_ttl_amt_bal,"
            + "this_ttl_amt         as ttl_amt,"
            + "dc_this_ttl_amt      as dc_ttl_amt";
  daoTable  = "cyc_acmm_curr_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "";

  if (DEBUG_P)
     whereStr  = whereStr 
               + "where   p_seqno      = '" + pSeqno +"' ";

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    updateActAcctCurr();
    if (totalCnt%100000==0)
      {
       showLogMessage("I","","    Proc Records :  "+totalCnt);
       countCommit();
      }   
   } 
  closeCursor();
  countCommit();
  return;
 }
// ************************************************************************
 void selectActAcctHst() throws Exception
 {
  selectSQL = "b.p_seqno";
  daoTable  = "act_acct_hst a,cyc_acmm_"+ getValue("wday.stmt_cycle") +" b"; 
  whereStr  = "WHERE a.p_seqno      = b.p_seqno "
            + "and   acct_month     = ? "
            + "and   (unbill_end_bal_af+billed_end_bal_af) = stmt_this_ttl_amt "
            + "and   stmt_this_ttl_amt > 0 ";

  if (DEBUG_P)
     whereStr  = whereStr 
               + "and     a.p_seqno      = '" + pSeqno +"' ";

  setString(1 , getValue("wday.ll_acct_month"));

  openCursor();

  totalCnt=0;
  int okFlag=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    if (selectActAcno1()!=0) continue;
    updateActAcno1();
    if (totalCnt%100000==0)
      {
       showLogMessage("I","","    Proc Records :  "+totalCnt);
       countCommit();
      }   
   } 
  closeCursor();
  countCommit();
  return;
 }
// ************************************************************************
 void processPaymentRate() throws Exception 
 {
  setValue("int_rate_mcode","0");

  if (getValueInt("ttl_amt") <= 0) 
     {
      setValue("payment_rate" , "0E");
      if ((getValueInt("ll_stmt_new_amt") > 0)||
          (getValueInt("stmt_new_amt") > 0))
         setValue("payment_rate" , "0A");
     }
  if (getValue("payment_rate").length()!=0) return;

  if ((getValueInt("ttl_amt_bal") <= 0)&&
      (getValue("last_cancel_debt_date").compareTo( getValue("wday.last_delaypay_date")) <= 0))
     setValue("payment_rate" , "0A");
  if (getValue("payment_rate").length()!=0) return;

  if (getValueInt("ttl_amt_bal") <= 0)
     {
      if (getValue("last_min_pay_date").compareTo( getValue("wday.last_delaypay_date"))<= 0)
         setValue("payment_rate" , "0A");
      else setValue("payment_rate" , "0B");
       
      if (getValue("payment_rate").length()!=0) return;
     }

  // *** 若有欠款，則欠款金額扣除一個月內已結案的問交款，若欠款為問交款則，0A ***

  if (((getValueInt("ttl_amt_bal") > 0)&&
       (getValueInt("min_pay") == 0))||
      ((getValueInt("ttl_amt_bal") > 0)&&
       (getValue("last_min_pay_date").compareTo(getValue("wday.last_delaypay_date"))<= 0)))
     setValue("payment_rate" , "0C");
  if (getValue("payment_rate").length()!=0) return;

  if ((getValueInt("min_pay_bal") == 0)&&(getValueInt("ttl_amt_bal")> 0)) 
     setValue("payment_rate" , "0D");
  if (getValue("payment_rate").length()!=0) return;

  if (getValueInt("min_pay_bal") <= getValueInt("mix_mp_balance")) 
     setValue("payment_rate" , "0D");
  if (getValue("payment_rate").length()!=0) return;

  if ((getValueInt("payment_amt")==0)&&
      (getValueInt("adjust_amt")+getValueInt("last_ttl_amt")<=0))
     setValue("payment_rate" , "0E");
  if (getValue("payment_rate").length()!=0) return;

  if ((getValue("spec_flag").equals("Y"))&&
      (!getValue("wday.this_acct_month").equals(getValue("spec_flag_month"))))
     {
      if (getValueInt("ttl_amt_bal")<=0) 
         setValue("payment_rate" , "0E");
      else setValue("payment_rate" , "0C");
     }
  if (getValue("payment_rate").length()!=0) return;

  getMCode();

  if (getValueInt("int_rate_mcode")==0)
     {
//    showLogMessage("I","","p_seqno["+getValue("p_seqno")+"] has mcode [00] error!");
      setValue("payment_rate" , "0E");
     }
  if (getValue("payment_rate").length()!=0) return;

  int lastIntRate = 0;
  if (!Arrays.asList("0A","0B","0C","0D","0E","").contains(getValue("payment_rate1")))
     lastIntRate = Integer.valueOf(getValue("payment_rate1"));

  int lastRate =0;
  if (getValueInt("int_rate_mcode")>lastIntRate+1)
     lastRate = lastIntRate+1;
  else
     lastRate = getValueInt("int_rate_mcode");

  if (lastRate==0)
     {
      setValue("payment_rate" , "0E");
     }
  else
     {
      setValue("payment_rate" , String.format("%02d",lastRate));
     }
 }
// ************************************************************************
 int updateActAcno() throws Exception
 {
  dateTime();
  if (getValue("spec_flag").equals("Y")) 
     setValue("new.spec_flag_month",getValue("wday.this_acct_month"));

//if (comm.isNumber(getValue("payment_rate")))
  if (!Arrays.asList("0A","0B","0C","0D","0E").contains(getValue("payment_rate")))
     if (getValue("worse_mcode").compareTo(getValue("payment_rate"))<0)
         setValue("worse_mcode",getValue("payment_rate"));

  updateSQL = "last_credit_amt  = line_of_credit_amt,"
            + "payment_rate1    = ?,"
            + "payment_rate2    = payment_rate1,"
            + "payment_rate3    = payment_rate2,"
            + "payment_rate4    = payment_rate3,"
            + "payment_rate5    = payment_rate4,"
            + "payment_rate6    = payment_rate5,"
            + "payment_rate7    = payment_rate6,"
            + "payment_rate8    = payment_rate7,"
            + "payment_rate9    = payment_rate8,"
            + "payment_rate10   = payment_rate9,"
            + "payment_rate11   = payment_rate10,"
            + "payment_rate12   = payment_rate11,"
            + "payment_rate13   = payment_rate12,"
            + "payment_rate14   = payment_rate13,"
            + "payment_rate15   = payment_rate14,"
            + "payment_rate16   = payment_rate15,"
            + "payment_rate17   = payment_rate16,"
            + "payment_rate18   = payment_rate17,"
            + "payment_rate19   = payment_rate18,"
            + "payment_rate20   = payment_rate19,"
            + "payment_rate21   = payment_rate20,"
            + "payment_rate22   = payment_rate21,"
            + "payment_rate23   = payment_rate22,"
            + "payment_rate24   = payment_rate23,"
            + "payment_rate25   = payment_rate24,"
            + "worse_mcode      = ?,"
            + "atm_pay_flag     = '',"
            + "spec_flag_month  = ?,"
            + "int_rate_mcode   = ?, "
            + "last_stmt_date   = decode(cast(? as varchar(1)),'Y',"
            + "cast(? as varchar(8)),last_stmt_date), "
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "act_acno";
  whereStr  = "WHERE  rowid = ? ";

  setString(1 , getValue("payment_rate"));
  setString(2 , getValue("worse_mcode"));
  setString(3 , getValue("new.spec_flag_month"));
  setInt(4 ,    getValueInt("int_rate_mcode"));
  setString(5 , hStmtOnFlag);
  setString(6 , businessDate);
  setString(7 , javaProgram);
  setRowId(8  , getValue("acno_rowid"));

  updateTable();

  return(0);
 }
// ************************************************************************
 int selectActAcno1() throws Exception
 {
  extendField = "acno.";
  selectSQL = "payment_rate1";
  daoTable  = "act_acno";
  whereStr  = "WHERE p_seqno = ? "
            + "AND   payment_rate1 not in ( '0A','0E')  ";

  setString(1  , getValue("p_seqno"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

//showLogMessage("I","","P_SEQNO ["+ getValue("p_seqno") 
//                     + "] payment_rate1 ["
//                     + getValue("acno.payment_rate1")
//                     + "]==>[0E]");

  return(0);
 }
// ************************************************************************
 int updateActAcno1() throws Exception
 {
  updateSQL = "payment_rate1    = '0E'";
  daoTable  = "act_acno";
  whereStr  = "WHERE p_seqno = ? "
            + "AND   payment_rate1 not in ('0A','0E')  ";
//          + "AND   payment_rate1 != '0A' ";

  setString(1  , getValue("p_seqno"));

  updateTable();

  return(0);
 }
// ************************************************************************
 int updateActAcct() throws Exception
 {
  updateSQL = "beg_bal_op            = end_bal_op,"
            + "beg_bal_lk            = end_bal_lk,"
            + "temp_unbill_interest  = 0,"
            + "temp_unbill_penalty   = 0,"
            + "temp_adi_interest     = 0,"
            + "pay_amt               = 0,"
            + "pay_cnt               = 0,"
            + "adjust_dr_amt         = 0,"
            + "adjust_cr_amt         = 0,"
            + "adjust_dr_cnt         = 0,"
            + "adjust_cr_cnt         = 0,"
            + "last_min_pay_date     = '',"
            + "last_cancel_debt_date = '',"
            + "adi_beg_bal           = 0,"
            + "adi_end_bal           = 0,"
            + "adi_d_avail           = 0,"
            + "delaypay_ok_flag      = '',"
            + "update_date           = ?,"
            + "min_pay               = ?," 
            + "min_pay_bal           = ?," 
            + "autopay_beg_amt       = ?," 
            + "autopay_bal           = ?," 
            + "last_ttl_amt          = ?," 
            + "ttl_amt_bal           = ?," 
            + "ttl_amt               = ?,"
            + "rc_min_pay            = decode(?,-1,rc_min_pay,?)," 
            + "rc_min_pay_bal        = decode(?,-1,rc_min_pay_bal,?),"
            + "rc_min_pay_m0         = decode(?,-1,rc_min_pay_m0,?),"
            + "mod_pgm               = ?,"
            + "mod_time              = sysdate";  
  daoTable  = "act_acct";
  whereStr  = "WHERE p_seqno = ? ";

  setString(1  , businessDate);
  setInt(2  , getValueInt("min_pay"));  
  setInt(3  , getValueInt("min_pay_bal"));  
  setInt(4  , getValueInt("autopay_beg_amt"));  
  setInt(5  , getValueInt("autopay_bal"));  
  setInt(6  , getValueInt("last_ttl_amt"));  
  setInt(7  , getValueInt("ttl_amt_bal"));  
  setInt(8  , getValueInt("ttl_amt"));  
  setInt(9  , getValueInt("rc_min_pay"));  
  setInt(10 , getValueInt("rc_min_pay"));  
  setInt(11 , getValueInt("rc_min_pay_bal"));
  setInt(12 , getValueInt("rc_min_pay_bal"));  
  setInt(13 , getValueInt("rc_min_pay_m0"));
  setInt(14 , getValueInt("rc_min_pay_m0"));
  setString(15, javaProgram);
  setString(16 , getValue("p_seqno"));

  updateTable();

  return(0);
 }
// ************************************************************************
 int updateActAcctCurr() throws Exception
 {
  updateSQL = "beg_bal_op              = end_bal_op,"
            + "dc_beg_bal_op           = dc_end_bal_op,"
            + "beg_bal_lk              = end_bal_lk,"
            + "dc_beg_bal_lk           = dc_end_bal_lk,"
            + "temp_unbill_interest    = 0," 
            + "dc_temp_unbill_interest = 0," 
            + "min_pay                 = ?,"
            + "dc_min_pay              = ?,"
            + "min_pay_bal             = ?,"
            + "dc_min_pay_bal          = ?,"
            + "autopay_beg_amt         = ?,"
            + "dc_autopay_beg_amt      = ?,"
            + "autopay_bal             = ?,"
            + "dc_autopay_bal          = ?,"
            + "last_ttl_amt            = ?,"
            + "dc_last_ttl_amt         = ?,"
            + "ttl_amt_bal             = ?,"
            + "dc_ttl_amt_bal          = ?,"
            + "ttl_amt                 = ?,"
            + "dc_ttl_amt              = ?,"
            + "pay_amt                 = 0," 
            + "dc_pay_amt              = 0," 
            + "pay_cnt                 = 0," 
            + "adjust_dr_amt           = 0," 
            + "dc_adjust_dr_amt        = 0," 
            + "adjust_dr_cnt           = 0," 
            + "adjust_cr_amt           = 0," 
            + "dc_adjust_cr_amt        = 0," 
            + "adjust_cr_cnt           = 0," 
            + "delaypay_ok_flag        = ''," 
            + "mod_pgm                 = ?,"
            + "mod_time                = sysdate";  
  daoTable  = "act_acct_curr";
  whereStr  = "WHERE p_seqno   = ? "
            + "and   curr_code = ? ";

  setDouble(1  , getValueDouble("min_pay"));
  setDouble(2  , getValueDouble("dc_min_pay")); 
  setDouble(3  , getValueDouble("min_pay_bal")); 
  setDouble(4  , getValueDouble("dc_min_pay_bal")); 
  setDouble(5  , getValueDouble("autopay_beg_amt")); 
  setDouble(6  , getValueDouble("dc_autopay_beg_amt")); 
  setDouble(7  , getValueDouble("autopay_bal")); 
  setDouble(8  , getValueDouble("dc_autopay_bal")); 
  setDouble(9  , getValueDouble("last_ttl_amt")); 
  setDouble(10 , getValueDouble("dc_last_ttl_amt")); 
  setDouble(11 , getValueDouble("ttl_amt_bal")); 
  setDouble(12 , getValueDouble("dc_ttl_amt_bal")); 
  setDouble(13 , getValueDouble("ttl_amt")); 
  setDouble(14 , getValueDouble("dc_ttl_amt"));
  setString(15 , javaProgram);
  setString(16 , getValue("p_seqno"));
  setString(17 , getValue("curr_code"));

  updateTable();

  return(0);
 }
// ************************************************************************
 void updateActAcnoDebug() throws Exception
 {
  sqlCmd    = "update act_acno a "
            + " set ("
            + "last_credit_amt  , "
            + "payment_rate1    , "
            + "payment_rate2    , "
            + "payment_rate3    , "
            + "payment_rate4    , "
            + "payment_rate5    , "
            + "payment_rate6    , "
            + "payment_rate7    , "
            + "payment_rate8    , "
            + "payment_rate9    , "
            + "payment_rate10   , "
            + "payment_rate11   , "
            + "payment_rate12   , "
            + "payment_rate13   , "
            + "payment_rate14   , "
            + "payment_rate15   , "
            + "payment_rate16   , "
            + "payment_rate17   , "
            + "payment_rate18   , "
            + "payment_rate19   , "
            + "payment_rate20   , "
            + "payment_rate21   , "
            + "payment_rate22   , "
            + "payment_rate23   , "
            + "payment_rate24   , "
            + "payment_rate25   , "
            + "worse_mcode      , "
            + "atm_pay_flag     , "
            + "spec_flag_month  ) = "
            + " (select " 
            + "        last_credit_amt  , "
            + "        payment_rate1    , "
            + "        payment_rate2    , "
            + "        payment_rate3    , "
            + "        payment_rate4    , "
            + "        payment_rate5    , "
            + "        payment_rate6    , "
            + "        payment_rate7    , "
            + "        payment_rate8    , "
            + "        payment_rate9    , "
            + "        payment_rate10   , "
            + "        payment_rate11   , "
            + "        payment_rate12   , "
            + "        payment_rate13   , "
            + "        payment_rate14   , "
            + "        payment_rate15   , "
            + "        payment_rate16   , "
            + "        payment_rate17   , "
            + "        payment_rate18   , "
            + "        payment_rate19   , "
            + "        payment_rate20   , "
            + "        payment_rate21   , "
            + "        payment_rate22   , "
            + "        payment_rate23   , "
            + "        payment_rate24   , "
            + "        payment_rate25   , "
            + "        worse_mcode      , "
            + "        atm_pay_flag     , "
            + "        spec_flag_month   "
            + "        from act_acno_" + getValue("wday.stmt_cycle")        
            + "        where p_seqno = a.p_seqno "
            + "        and   p_seqno = acno_p_seqno) "
            + "WHERE   p_seqno in (select p_seqno "
            + "                    from cyc_acmm_"+ getValue("wday.stmt_cycle") +") "
            + "and     p_seqno = acno_p_seqno ";

  if (DEBUG_P)
     sqlCmd  = sqlCmd 
             + "and     p_seqno      = '" + pSeqno +"' ";
               
  daoTable  = "act_acno";

  executeSqlCommand(sqlCmd);

 }
// ************************************************************************
 void updateActAcctDebug() throws Exception
 {
  sqlCmd    = "update act_acct a "
            + " set ("
            + "beg_bal_op            , "
            + "beg_bal_lk            , "
            + "temp_unbill_interest  , "
            + "temp_unbill_penalty   , "
            + "temp_adi_interest     , "
            + "pay_amt               , "
            + "pay_cnt               , "
            + "adjust_dr_amt         , "
            + "adjust_cr_amt         , "
            + "adjust_dr_cnt         , "
            + "adjust_cr_cnt         , "
            + "last_min_pay_date     , "
            + "last_cancel_debt_date , "
            + "adi_beg_bal           , "
            + "adi_end_bal           , "
            + "adi_d_avail           , "
            + "delaypay_ok_flag      , "
            + "update_date           , "
            + "min_pay               , "
            + "min_pay_bal           , "
            + "autopay_beg_amt       , "
            + "autopay_bal           , "
            + "last_ttl_amt          , "
            + "ttl_amt_bal           , "
            + "ttl_amt               , "
            + "rc_min_pay            , "
            + "rc_min_pay_bal        , "
            + "rc_min_pay_m0  ) =      "
            + " (select " 
            + "        beg_bal_op            , "
            + "        beg_bal_lk            , "
            + "        temp_unbill_interest  , "
            + "        temp_unbill_penalty   , "
            + "        temp_adi_interest     , "
            + "        pay_amt               , "
            + "        pay_cnt               , "
            + "        adjust_dr_amt         , "
            + "        adjust_cr_amt         , "
            + "        adjust_dr_cnt         , "
            + "        adjust_cr_cnt         , "
            + "        last_min_pay_date     , "
            + "        last_cancel_debt_date , "
            + "        adi_beg_bal           , "
            + "        adi_end_bal           , "
            + "        adi_d_avail           , "
            + "        delaypay_ok_flag      , "
            + "        update_date           , "
            + "        min_pay               , "
            + "        min_pay_bal           , "
            + "        autopay_beg_amt       , "
            + "        autopay_bal           , "
            + "        last_ttl_amt          , "
            + "        ttl_amt_bal           , "
            + "        ttl_amt               , "
            + "        rc_min_pay            , "
            + "        rc_min_pay_bal        , "
            + "        rc_min_pay_m0           "
            + "  from act_acct_" + getValue("wday.stmt_cycle")        
            + "  where p_seqno = a.p_seqno) "
            + "WHERE   p_seqno in (select p_seqno "
            + "                    from cyc_acmm_"+ getValue("wday.stmt_cycle") +") "
            ;

  if (DEBUG_P)
     sqlCmd  = sqlCmd 
             + "and     p_seqno      = '" + pSeqno +"' ";

  daoTable  = "act_acct";

  executeSqlCommand(sqlCmd);

 }
// ************************************************************************
 void loadActAcag() throws Exception
 {
  extendField = "acag.";
  selectSQL = "p_seqno, "
            + "acct_month,"
            + "sum(pay_amt) as pay_amt ";
  daoTable  = "act_acag";
  whereStr  = "where p_seqno in (select p_seqno from cyc_acmm_"+ getValue("wday.stmt_cycle") +") "
            + "and   acct_month != ? "; 


  if (DEBUG_P)
     whereStr  = whereStr 
               + "and     p_seqno      = '" + pSeqno +"' ";

  whereStr  = whereStr 
            + "group by p_seqno,acct_month "
            + "order by p_seqno,acct_month "
            ;

  setString(1 , getValue("wday.this_acct_month"));

  int  n = loadTable();
  setLoadData("acag.p_seqno");

  showLogMessage("I","","Load act_acag Count: ["+n+"]");
 }
// ************************************************************************
 void getMCode() throws Exception
 {
  int mCode=0;
  setValue("acag.p_seqno" , getValue("p_seqno"));
  int cnt1 = getLoadData("acag.p_seqno");
  if (cnt1==0)
     {
      setValue("int_rate_mcode","0");
      return;
     }

  double minAmount = 0;
  mCode =0;
  for (int inti=0; inti<cnt1; inti++ )
    {
     minAmount = minAmount + getValueDouble("acag.pay_amt",inti);
     if (minAmount > getValueInt("mix_mp_balance"))
        {
         mCode = (int)comm.monthBetween(getValue("acag.acct_month",inti),
                                        getValue("wday.this_acct_month"));
         break;
        }
    }

  if (mCode>99) mCode=99;
  setValueInt("int_rate_mcode" , mCode);
 }
// ************************************************************************


}  // End of class FetchSample

