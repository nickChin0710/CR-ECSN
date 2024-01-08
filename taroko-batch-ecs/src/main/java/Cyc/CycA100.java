/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/03/19  V1.00.09  Allen Ho   cyc_a143  PROD compare OK                  *
* 109-12-17  V1.00.10  tanwei     updated for project coding standard        *
* 111/10/26  V1.00.11  Simon      sync codes with mega                       *
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
public class CycA100 extends AccessDAO
{
 private  String progname = "關帳-最低應繳金額計算處理程式 111/10/26 V1.00.11";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;

 String hBusiBusinessDate   = "";
 String tranSeqno = "";
 String fundCode="";

 long    totalCnt=0;
 boolean debug = false;
// boolean DEBUG = true;

 String tmpstr="";
 int actDebtSumCnt =0;
 int intSeq=0;
 double[][] mTempEndAmt    =  new double[6][20];
 double[][] mTempDcEndAmt =  new double[6][20];
 int mp3Flag=0;
 double tempLong;
 double[]  mpMinPayment = new double[10];
 String[]  mpCurrCode = new String[6];
 double[]  mpExchangeRate = new double[10];
 double[]  mCacrAutoPaymentAmt    = new double[6];
 double[]  mCacrDcAutoPaymentAmt = new double[6]; 
 double[]  mCacrThisMinimumPay    = new double[6]; 
 double[]  mCacrDcThisMinimumPay = new double[6]; 
  
 int paymentAmt = 0,insertCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA100 proc = new CycA100();
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

   if (args.length == 1 )
      { 
       hBusiBusinessDate = args[0];
      }
   
   if ( !connectDataBase() ) 
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }
   showLogMessage("I","","this_acct_month["+getValue("wday.this_acct_month")+"]");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","刪除 cyc_mp_method...");
   deleteCycMpMethod();
   showLogMessage("I","","刪除筆數 ["+totalCnt+"]");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料 ..");
   loadPtrCurrcode();
   loadActDebtSum();
   loadActDebtIntr1();
   loadActDebtIntr2();

   selectPtrCurrRate();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","   計算 cyc_acmm MP 開始...");
   totalCnt=0;
   setValue("exceed_flag" , "0");
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
  int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 public void  selectCycAcmm() throws Exception
 {
  selectSQL = "a.p_seqno," 
            + "a.acct_type," 
            + "a.acct_key as acno_acct_key," 
            + "a.line_of_credit_amt," 
            + "a.rc_use_indicator,"
            + "decode(a.rc_use_s_date,'','20000101',a.rc_use_s_date) as rc_use_s_date,"
            + "decode(a.rc_use_e_date,'','30001231',a.rc_use_e_date) as rc_use_e_date,"
            + "a.rc_use_b_adj as rc_use_b_adj," 
            + "a.mp_flag," 
            + "a.min_pay_rate,"
            + "decode(a.special_stat_s_month,'','200001',a.special_stat_s_month) as acno_special_stat_s_month,"
            + "decode(a.special_stat_e_month,'','300012',a.special_stat_e_month) as acno_special_stat_e_month,"
            + "special_stat_fee as acno_special_stat_fee,"
            + "decode(a.mp_flag,'1',decode(a.mp_1_s_month,'','200001',a.mp_1_s_month),"
            + "                     decode(a.min_pay_rate_s_month,'','200001',"
            + "                            a.min_pay_rate_s_month)) as min_pay_rate_s_month,"
            + "decode(a.mp_flag,'1',decode(a.mp_1_e_month,'','300012',a.mp_1_e_month),"
            + "                     decode(a.min_pay_rate_e_month,'','300012',"
            + "                            a.min_pay_rate_e_month)) as min_pay_rate_e_month,"
            + "decode(autopay_acct_s_date,'','99999999',autopay_acct_s_date) as acno_autopay_acct_s_date,"
            + "decode(autopay_acct_e_date,'','99999999',autopay_acct_e_date) as acno_autopay_acct_e_date,"
            + "a.autopay_fix_amt as acno_autopay_fix_amt,"
            + "a.autopay_rate as acno_autopay_rate,"
            + "a.int_rate_mcode as acno_int_rate_mcode,"
            + "to_char(add_months(to_date(decode(a.spec_flag_month,'','200001',"
            + "            a.spec_flag_month),'yyyymm'),1),'yyyymm') as spec_flag_month,"
            + "b.minimum_pay_bal as minimum_pay_bal,"  
            + "b.penauty_amt as acmm_penauty_amt,"  
            + "b.this_ttl_amt as acmm_this_ttl_amt,"  
            + "b.unpost_installment as unpost_installment,"  
            + "b.rowid as acmm_rowid,"  
            + "c.mp_1_rate as agnn_mp_1_rate,"  
            + "c.mp_3_rate as agnn_mp_3_rate,"  
            + "c.mp_mcode as agnn_mp_mcode,"
            + "d.rc_use_flag as rc_use_flag";
  daoTable  = "act_acno_"+ getValue("wday.stmt_cycle") 
            + " a,cyc_acmm_"+ getValue("wday.stmt_cycle") 
            + " b,ptr_actgeneral_n c,ptr_acct_type d ";
  whereStr  = "where  a.acno_flag != 'Y' "
            + "and    a.acct_type = c.acct_type "
            + "and    a.p_seqno = b.p_seqno "
            + "and    c.acct_type = d.acct_type "
            + "and    (b.this_ttl_amt > 0 "
            + " or     (b.this_ttl_amt <= 0 "
            + "  and    b.overpay_amt  !=0)) "
//          + "and    a.p_seqno = '0000426386' "   // debug
            ;

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;
    
    if ((getValue("wday.this_acct_month").compareTo(getValue("acno_special_stat_s_month"))<0)||
        (getValue("wday.this_acct_month").compareTo(getValue("acno_special_stat_e_month"))>0))
       setValueInt("acno_special_stat_fee" , 0);

    setValue("cmmd_seqno"            , "0");
    setValue("acmm.this_minimum_pay" , "0");
    setValue("spec_flag"             , "N");

    if (debug) showLogMessage("I","","p_seqno["+getValue("p_seqno")+"] ["+
                           getValue("acct_type") +"]-[" + getValue("acno_acct_key")+
                           "] 額度[" + String.format("%.0f",getValueDouble("line_of_credit_amt")) +"]");

    if (selectActDebtSum()!=0) continue;

    mp3Flag=0;
    // ************* 特殊固定 MP *start***************
    if (procMp2Method()!=0)
       {
        procLastMpMethod();
        continue;
       }
    // ************* 特殊固定 MP *end*****************

    //************* 特殊MP百分比*start*************
  if (debug)
     {
      showLogMessage("I","","STEP 0 : ["+hBusiBusinessDate+"]");
      showLogMessage("I","","STEP 1 : ["+ getValue("mp_flag") +"]");
      showLogMessage("I","","STEP 2 : ["+ getValueDouble("min_pay_rate") +"]");
      showLogMessage("I","","STEP 3 : ["+ getValue("min_pay_rate_s_month") +"]");
      showLogMessage("I","","STEP 4 : ["+ getValue("min_pay_rate_e_month") +"]");
      showLogMessage("I","","STEP 5 : ["+ hBusiBusinessDate.substring(0,6) +"]");
     }
   
    if ((!getValue("mp_flag").equals("1"))&&(getValueDouble("min_pay_rate")>0)&&
        (getValue("min_pay_rate_s_month").compareTo(hBusiBusinessDate.substring(0,6))<=0)&&
        (getValue("min_pay_rate_e_month").compareTo(hBusiBusinessDate.substring(0,6))>=0))
       {
        setValueDouble("agnn_mp_3_rate" , getValueDouble("min_pay_rate"));
        mp3Flag=1;
       }
    //************* 特殊MP百分比*end***************
    else
      {
    // ************ 不允用 RC *start****************/
      if ((getValue("rc_use_indicator").equals("2"))||
          (getValue("rc_use_indicator").equals("3")))
         {
          if ((hBusiBusinessDate.compareTo(getValue("rc_use_s_date"))<0)||
              (hBusiBusinessDate.compareTo(getValue("rc_use_e_date"))>0))
             setValue("rc_use_indicator" , getValue("rc_use_b_adj"));
         }

      if (!getValue("rc_use_indicator").equals("2"))
         if (getValue("rc_use_flag").equals("3")) setValue("rc_use_indicator" , "3");

      if (getValue("rc_use_indicator").equals("3"))
         {
          procMp1Method();
          procLastMpMethod();
          continue;
         }
       //************ 不允用 RC *end******************
      }
    setValueDouble("temp_end_a_amt" , 0); // 額度內金額 
    setValue("exceed_flag" , "0");

    if (debug) showLogMessage("I","","STEP A1 ["+getValueDouble("adsm_end_cap_amt")+
                  "] = ["+getValueDouble("line_of_credit_amt")+
                  "] ["+getValueDouble("unpost_installment")+"]");

    if (getValueDouble("adsm_end_cap_amt") > getValueDouble("line_of_credit_amt")
                                           - getValueDouble("unpost_installment"))
       {
        if (actDebtSumCnt>1) procExceedMp();
        procThirdMp();   // 第三類 MP 
        procFirstMp();   // 第一類 MP 
        procSecondMp();  // 當期 IT MP(第二類 MP) 
       }
    else
       {
        procSecondMp();  // 當期 IT MP(第二類 MP) 
        procFirstMp();   // 第一類 MP 
        procThirdMp();   // 第三類 MP 
       }
    if (actDebtSumCnt==1) procFourMp();    // 本金類 超額 
    procMp10Method();
    procLastMpMethod();
   }
  closeCursor();
  return;
 }
// ************************************************************************
 void deleteCycMpMethod() throws Exception
 {
  daoTable  = "cyc_mp_method";
  whereStr  = "WHERE  acct_month = ? "
//          + "and    p_seqno    = '0000426386' " // debug
            + "and    stmt_cycle = ? ";

  setString(1, getValue("wday.this_acct_month"));
  setString(2, getValue("wday.stmt_cycle"));

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
public int insertCycMpMethod(int intq) throws Exception
 {
  dateTime();
  extendField = "cmmd.";

  setValueInt("cmmd_seqno" , getValueInt("cmmd_seqno")+1);
  setValueInt("cmmd.seqno" , getValueInt("cmmd_seqno"));

//if (DEBUG) showLogMessage("I","","STEP 2 ["+ getValueDouble("acmm.this_minimum_pay")+"]");
//if (DEBUG) showLogMessage("I","","STEP 3 ["+ getValueDouble("cmmd_mp_amt") +"]");

  setValueDouble("acmm.this_minimum_pay"  , getValueDouble("acmm.this_minimum_pay") 
                                          +  getValueDouble("cmmd_mp_amt"));

//if (DEBUG) showLogMessage("I","","STEP 4 "+ getValueDouble("acmm.this_minimum_pay")+"]");

  mCacrThisMinimumPay[intq]           = mCacrThisMinimumPay[intq] 
                                          + getValueDouble("cmmd_mp_amt");
  mCacrDcThisMinimumPay[intq]        = mCacrDcThisMinimumPay[intq] 
                                          + getValueDouble("cmmd_dc_mp_amt");

  setValue("cmmd.acct_month"          , getValue("wday.this_acct_month"));
  setValue("cmmd.stmt_cycle"          , getValue("wday.stmt_cycle"));
  setValue("cmmd.p_seqno"             , getValue("p_seqno"));
  setValue("cmmd.curr_code"           , getValue("cmmd_curr_code"));
  setValue("cmmd.seqno"               , getValue("cmmd_seqno"));
  setValue("cmmd.acct_type"           , getValue("acct_type"));
  setValue("cmmd.acct_code"           , getValue("debt_acct_code"));
  setValue("cmmd.current_item"        , getValue("cmmd_current_item"));
  setValue("cmmd.mp_desc"             , getValue("cmmd_mp_desc"));
  setValue("cmmd.end_bal"             , getValue("cmmd_end_bal"));
  setValue("cmmd.dc_end_bal"          , getValue("cmmd_dc_end_bal"));
  setValue("cmmd.mcht_no"             , getValue("debt_mcht_no"));
  setValue("cmmd.mp_rate"             , getValue("cmmd_mp_rate"));
  setValue("cmmd.line_of_credit_amt"  , getValue("line_of_credit_amt"));
  setValue("cmmd.unpost_installment"  , getValue("unpost_installment"));
  setValue("cmmd.exceed_flag"         , getValue("exceed_flag"));
  setValue("cmmd.mp_amt"              , getValue("cmmd_mp_amt"));
  setValue("cmmd.dc_mp_amt"           , getValue("cmmd_dc_mp_amt"));
  setValue("cmmd.this_minmum_pay"     , getValue("acmm.this_minimum_pay"));
  setValue("cmmd.mod_user"            , javaProgram); 
  setValue("cmmd.mod_time"            , sysDate+sysTime);
  setValue("cmmd.mod_pgm"             , javaProgram);

  daoTable  = "cyc_mp_method";

  insertTable();

  if (debug) showLogMessage("I","",
        "["         + getValue("cmmd_curr_code")
       +"]["        + getValue("cmmd_mp_desc")
       +"] ["       + getValueDouble("cmmd_end_bal")
       +"] * ["     + String.format("%.2f",getValueDouble("cmmd_mp_rate"))
       +" %] = ["   + getValueDouble("cmmd_mp_amt")
       +"] 金額["   + getValueDouble("cmmd_mp_amt")
       +"]-["       + getValueDouble("cmmd_dc_mp_amt")
       +"] 累計["   + String.format("%.2f",getValueDouble("acmm.this_minimum_pay"))
       +"] 1-["     + mCacrThisMinimumPay[intq]
       +"]-["       + mCacrDcThisMinimumPay[intq]
       +"]-["       + intq
       +"]");
                 
  return(0);
 }
// ************************************************************************
 void updateCycAcmm() throws Exception
 {
  dateTime();

  updateSQL = "this_minimum_pay = ?,"
            + "spec_flag        = ?," 
            + "org_mp           = ?," 
            + "auto_payment_amt = ?," 
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "cyc_acmm_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE rowid     = ? ";

  setDouble(1  , getValueDouble("acmm.this_minimum_pay"));
  setString(2  , getValue("spec_flag"));
  setDouble(3  , getValueDouble("acmm.this_minimum_pay"));
  setDouble(4  , getValueDouble("acmm_auto_payment_amt"));
  setString(5 , javaProgram);
  setRowId(6  , getValue("acmm_rowid"));

  updateTable();

  if (debug) showLogMessage("I","","----------------------------------------------------------------------------------");
  if (debug) showLogMessage("I","","累計最低應繳金額 = ["+getValueDouble("acmm.this_minimum_pay")+"]");
  return;
 }
// ************************************************************************
 void updateCycAcmmCurr() throws Exception
 {
  dateTime();

  updateSQL = "this_minimum_pay    = ?,"
            + "dc_this_minimum_pay = ?,"
            + "auto_payment_amt    = ?,"
            + "dc_auto_payment_amt = ?,"
            + "mod_pgm             = ?, "
            + "mod_time            = sysdate";  
  daoTable  = "cyc_acmm_curr_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE  p_seqno      = ? "
            + "AND    curr_code    = ? ";
                      
  setDouble(1  , getValueDouble("cacr_this_minimum_pay"));
  setDouble(2  , getValueDouble("cacr_dc_this_minimum_pay")); 
  setDouble(3  , getValueDouble("cacr_auto_payment_amt")); 
  setDouble(4  , getValueDouble("cacr_dc_auto_payment_amt")); 
  setString(5 , javaProgram);
  setString(6 , getValue("p_seqno"));
  setString(7 , getValue("cacr_curr_code"));

  if (debug) showLogMessage("I","","--------------------update cyc_acmm_curr -----------------------------------------");
  if (debug) showLogMessage("I","","cacr_this_minimum_pay     = ["+getValueDouble("cacr_this_minimum_pay")+"]");
  if (debug) showLogMessage("I","","cacr_dc_this_minimum_pay  = ["+getValueDouble("cacr_dc_this_minimum_pay")+"]");
  if (debug) showLogMessage("I","","cacr_auto_payment_amt     = ["+getValueDouble("cacr_auto_payment_amt")+"]");
  if (debug) showLogMessage("I","","cacr_dc_auto_payment_amt  = ["+getValueDouble("cacr_dc_auto_payment_amt")+"]");
  if (debug) showLogMessage("I","","p_seqno                   = ["+getValue("p_seqno")+"]");
  if (debug) showLogMessage("I","","cacr_curr_code            = ["+getValue("cacr_curr_code")+"]");
  if (debug) showLogMessage("I","","----------------------------------------------------------------------------------");

  updateTable();
  return;
 }
// ************************************************************************
int  procMp2Method() throws Exception 
 {
  int retCode=0;
  for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     if (!getValue("mp_flag").equals("1")) continue;

     if (getValue("debt.mp_1_s_month",inti).length()==0)
         setValue("debt.mp_1_s_month" , "200001" , inti);
     if (getValue("debt.mp_1_e_month",inti).length()==0)
         setValue("debt.mp_1_e_month" , "300012" , inti);
      
     if ((getValue("wday.this_acct_month").compareTo(getValue("debt.mp_1_s_month",inti))<0)|| 
         (getValue("wday.this_acct_month").compareTo(getValue("debt.mp_1_e_month",inti))>0)) continue;

     retCode=1;
     intSeq= getValueInt("debt.bill_sort_seq",inti);

     setValue("adsm_mp_1_s_month"   , getValue("debt.mp_1_s_month",inti));
     setValue("adsm_mp_1_e_month"   , getValue("debt.mp_1_e_month",inti));
     setValue("adsm_mp_1_amt"       , getValue("debt.mp_1_amt",inti));

     setValue("spec_flag"         , "Y");
     setValue("debt_acct_code"    , "");
     tmpstr = "特殊固定 MP("+getValueDouble("adsm_mp_1_amt")+"),效期 "+
                             getValue("adsm_mp_1_s_month")+"-"+
                             getValue("adsm_mp_1_e_month")+", Mcode["+
                             getValue("acno_int_rate_mcode")+"]";
     setValue("cmmd_mp_desc"            , tmpstr);

     double  minPayment                = getValueDouble("adsm_mp_1_amt");
     setValue("cmmd_curr_code"          , mpCurrCode[intSeq]);
     setValueDouble("cmmd_mp_amt"       , getValueDouble("adsm_mp_1_amt"));

     tempLong = (int)Math.round(getValueDouble("adsm_mp_1_amt")/mpExchangeRate[intSeq])*100.0;
     setValueDouble("cmmd_dc_mp_amt"    , tempLong/100.0);

     setValue("cmmd_end_bal"            , getValue("debt.this_ttl_amt",inti));
     setValue("cmmd_dc_end_bal"         , getValue("debt.dc_this_ttl_amt",inti));

     setValue("cmmd_current_item"       , "");
     setValue("debt_mcht_no"            , "");
     setValue("cmmd_mp_rate"            , "100");
     setValue("exceed_flag"             , "");
     insertCycMpMethod(intSeq);
    }
  return(retCode);
 }
// ************************************************************************
void procMp1Method() throws Exception 
 {
  for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     intSeq= getValueInt("debt.bill_sort_seq",inti);

     setValue("cmmd_curr_code"  , getValue("debt.curr_code",inti));

     setValue("debt_acct_code"          , "");
     setValue("cmmd_mp_desc"            , "不允用 RC (INDICATORC='3') , 須全額繳清");
     setValue("cmmd_mp_amt"             , getValue("debt.this_ttl_amt",inti));
     setValue("cmmd_dc_mp_amt"          , getValue("debt.dc_this_ttl_amt",inti));
     setValue("cmmd_end_bal"            , getValue("debt.this_ttl_amt",inti));
     setValue("cmmd_dc_end_bal"         , getValue("debt.dc_this_ttl_amt",inti));
     setValue("cmmd_current_item"       , "");
     setValue("debt_mcht_no"            , "");
     setValue("cmmd_mp_rate"            , "100");
     setValue("exceed_flag"             , "");
     insertCycMpMethod(intSeq);
    }
 }
// ************************************************************************
void procThirdMp() throws Exception 
 {
  for (int inta=10;inta<18;inta++)
    {
     for (int inti=0;inti<actDebtSumCnt;inti++)
       {
        intSeq= getValueInt("debt.bill_sort_seq",inti);

        setValue("cmmd_curr_code" , mpCurrCode[intSeq]);

        if (mTempEndAmt[intSeq][inta] <=0) continue;

        setValueDouble("cmmd_end_bal"    , mTempEndAmt[intSeq][inta]);
        setValueDouble("cmmd_dc_end_bal" , mTempDcEndAmt[intSeq][inta]);

        if (getValueDouble("temp_end_a_amt") + mTempEndAmt[intSeq][inta] > 
            getValueDouble("line_of_credit_amt") - getValueDouble("unpost_installment")) /*當期 第三類已超額 */
           {
            setValueDouble("cmmd_end_bal" , getValueDouble("line_of_credit_amt")
                                          - getValueDouble("unpost_installment")
                                          - getValueDouble("temp_end_a_amt"));
            tempLong         = Math.round(getValueDouble("cmmd_end_bal")/mpExchangeRate[intSeq])*100.0;
            setValueDouble("cmmd_dc_end_bal"  , tempLong/100.0);
           }
        else
           {
            setValueDouble("cmmd_end_bal"    , Math.round(getValueDouble("cmmd_end_bal")*1000.0)/1000.0); 
            setValueDouble("cmmd_dc_end_bal" , Math.round(getValueDouble("cmmd_dc_end_bal")*1000.0)/1000.0); 
           }
        procMp7Method(inta-10);
        setValueDouble("temp_end_a_amt" , getValueDouble("temp_end_a_amt") + getValueDouble("cmmd_end_bal"));

        mTempEndAmt[intSeq][inta]    = mTempEndAmt[intSeq][inta] 
                                         - getValueDouble("cmmd_end_bal");
        mTempDcEndAmt[intSeq][inta] = mTempDcEndAmt[intSeq][inta] 
                                         - getValueDouble("cmmd_dc_end_bal");
        if (getValueDouble("temp_end_a_amt")  >= getValueDouble("line_of_credit_amt") 
                                              -  getValueDouble("unpost_installment")) break;
       }
     if (getValueDouble("temp_end_a_amt") >= getValueDouble("line_of_credit_amt")
                                          -  getValueDouble("unpost_installment")) break;
    }
 }
// ************************************************************************
void procMp7Method(int inta) throws Exception 
 {
  String[] acctCode={"BL","AO","OT","CA","ID","IT","CB","DB"};
  tmpstr = acctCode[inta];

  setValue("debt_acct_code"     , tmpstr);

  if (mp3Flag==0)
     tmpstr="第三類本金("+getValue("debt_acct_code")+")("+getValueDouble("agnn_mp_3_rate")+" %)";
  else
     tmpstr="第三類本金("+getValue("debt_acct_code")+")特殊MP("+getValueDouble("agnn_mp_3_rate")+" %)";
  setValue("cmmd_mp_desc"            , tmpstr);
  setValue("cmmd_curr_code"          , mpCurrCode[intSeq]);
  setValueDouble("cmmd_mp_amt"       , getValueDouble("cmmd_end_bal") * getValueDouble("agnn_mp_3_rate")/100.0);
  setValueDouble("cmmd_dc_mp_amt"    , getValueDouble("cmmd_dc_end_bal") * getValueDouble("agnn_mp_3_rate")/100.0);
  setValue("cmmd_current_item"       , "N");
  setValueDouble("cmmd_mp_rate"      , getValueDouble("agnn_mp_3_rate"));
  insertCycMpMethod(intSeq);
 }
// ************************************************************************
void procFourMp() throws Exception 
 {
  for (int inta=10;inta<18;inta++) /* 超額全額繳清 */
    {
     for (int inti=0;inti<actDebtSumCnt;inti++)
       {
        intSeq= getValueInt("debt.bill_sort_seq",inti);
        if (mTempEndAmt[intSeq][inta] <=0) continue;

        setValueDouble("cmmd_end_bal"    , mTempEndAmt[intSeq][inta]);
        setValueDouble("cmmd_dc_end_bal" , mTempDcEndAmt[intSeq][inta]);

        procMp8Method(inta-10);
       }
    }
 }
// ************************************************************************
void procMp8Method(int intw) throws Exception 
 {
  String[] acctCode={"BL","AO","OT","CA","ID","IT","CB","DB"};
  tmpstr = acctCode[intw];
  setValue("debt_acct_code"    , tmpstr);

  tmpstr = "超額  本金("+getValue("debt_acct_code")+")(100 %";
  setValue("cmmd_mp_desc"            , tmpstr);
  setValue("cmmd_curr_code"          , mpCurrCode[intSeq]);
  setValueDouble("cmmd_mp_amt"       , getValueDouble("cmmd_end_bal"));
  setValueDouble("cmmd_dc_mp_amt"    , getValueDouble("cmmd_dc_end_bal"));
  setValue("cmmd_current_item"       , "Y");
  setValue("cmmd_mp_rate"            , "100");
  setValue("exceed_flag"             , "1");
  insertCycMpMethod(intSeq);
 }
// ************************************************************************
void procSecondMp() throws Exception 
 {
  for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     intSeq= getValueInt("debt.bill_sort_seq",inti);
     setValueDouble("temp_end_7b_amt"    , 0);
     setValueDouble("temp_dc_end_7b_amt" , 0);
     if (getValueDouble("debt.end_2_it_amt",inti)>0)
        {
         setValue("cmmd_curr_code" , mpCurrCode[intSeq]);

         selectActDebt1();                                      /* 當期未超額 */
         if (getValueDouble("temp_end_a_amt")>=getValueDouble("line_of_credit_amt") 
                                              - getValueDouble("unpost_installment"))
            {
             mTempEndAmt[intSeq][15] = mTempEndAmt[intSeq][15] 
                                         + getValueDouble("debt.end_2_it_amt",inti)
                                         - getValueDouble("temp_end_7b_amt");
             mTempDcEndAmt[intSeq][15] = mTempDcEndAmt[intSeq][15] 
                                            + getValueDouble("debt.dc_end_2_it_amt",inti)
                                            - getValueDouble("temp_dc_end_7b_amt");  /* 未計算或超額歸第三類 */
            }
        }
    }
 }
// ************************************************************************
void procMp4Method() throws Exception 
 {
  setValue("debt_acct_code"    , "IT");
  setValue("cmmd_curr_code"          , "901");
  tmpstr = "第二類分期(IT)("+ getValueInt("merc_mp_rate") +" %)";
  setValue("cmmd_mp_desc"            , tmpstr);
  setValueDouble("cmmd_mp_amt"       , getValueDouble("cmmd_end_bal") * getValueInt("merc_mp_rate") /100.0);
  setValueDouble("cmmd_dc_mp_amt"    , getValueDouble("cmmd_dc_end_bal") * getValueInt("merc_mp_rate") /100.0);
  setValue("cmmd_current_item"       , "Y");
  setValueDouble("cmmd_mp_rate"      , getValueInt("merc_mp_rate"));
  setValue("exceed_flag"             , "0");
  insertCycMpMethod(intSeq);
 }
// ************************************************************************
void procFirstMp() throws Exception 
 {
  for (int inta=0;inta<5;inta++)
    {
     for (int inti=0;inti<actDebtSumCnt;inti++)
       {
        intSeq= getValueInt("debt.bill_sort_seq",inti);

        if (mTempEndAmt[intSeq][inta] <=0) continue;

        setValueDouble("cmmd_end_bal"    , mTempEndAmt[intSeq][inta]);
        setValueDouble("cmmd_dc_end_bal" , mTempDcEndAmt[intSeq][inta]);

        if ((getValueDouble("temp_end_a_amt") + mTempEndAmt[intSeq][inta]) > 
            (getValueDouble("line_of_credit_amt") - getValueDouble("unpost_installment"))) /*當期 第一類已超額 */
           {
            setValueDouble("cmmd_end_bal" , getValueDouble("line_of_credit_amt") 
                                          - getValueDouble("unpost_installment") 
                                          - getValueDouble("temp_end_a_amt"));
            tempLong         = Math.round(getValueDouble("cmmd_end_bal")/mpExchangeRate[intSeq])*100.0;
            setValueDouble("cmmd_dc_end_bal"  , tempLong/100.0);
           }
        procMp6Method(inta);
        setValueDouble("temp_end_a_amt" , getValueDouble("temp_end_a_amt") 
                                        + getValueDouble("cmmd_end_bal"));

        mTempEndAmt[intSeq][inta]    = mTempEndAmt[intSeq][inta] - getValueDouble("cmmd_end_bal");
        mTempDcEndAmt[intSeq][inta] = mTempDcEndAmt[intSeq][inta] - getValueDouble("cmmd_dc_end_bal");
        if (getValueDouble("temp_end_a_amt")  >= getValueDouble("line_of_credit_amt") 
                                              - getValueDouble("unpost_installment")) break;
       }
    }

  for (int inta=0;inta<5;inta++) /* 超額歸第三類 */
    {
     for (int inti=0;inti<actDebtSumCnt;inti++)
       {
        intSeq= getValueInt("debt.bill_sort_seq",inti);
        if (mTempEndAmt[intSeq][inta] <=0) continue;

        mTempEndAmt[intSeq][inta+10] = mTempEndAmt[intSeq][inta+10] 
                                         + mTempEndAmt[intSeq][inta];
        mTempDcEndAmt[intSeq][inta+10] = mTempDcEndAmt[intSeq][inta+10] 
                                            + mTempDcEndAmt[intSeq][inta];
       }
    }
 }
// ************************************************************************
void procMp6Method(int inta) throws Exception 
 {
  String[] acctCode={"BL","AO","OT","CA","ID","IT"};
  tmpstr  = acctCode[inta];
  setValue("debt_acct_code"    , tmpstr);

  tmpstr = "第一類本金("+getValue("debt_acct_code")+")("+getValueInt("agnn_mp_1_rate")+" %)";
  setValue("cmmd_mp_desc"             , tmpstr);
  setValue("cmmd_curr_code"           , mpCurrCode[intSeq]);
  setValueDouble("cmmd_mp_amt"        , getValueDouble("cmmd_end_bal") * getValueInt("agnn_mp_1_rate") /100.0);
  setValueDouble("cmmd_dc_mp_amt"     , getValueDouble("cmmd_dc_end_bal") * getValueInt("agnn_mp_1_rate")/100.0);
  setValue("cmmd_current_item"        , "Y");
  setValueDouble("cmmd_mp_rate"       , getValueInt("agnn_mp_1_rate"));
  insertCycMpMethod(intSeq);
 }
// ************************************************************************
void procLastMpMethod() throws Exception 
 {
  setValueDouble("acmm.this_minimum_pay" , Math.round(getValueDouble("acmm.this_minimum_pay")));

  procMp12Method();
         
  if (getValueDouble("minimum_pay_bal") !=0)
  if (((getValue("spec_flag").equals("Y"))&&
       (getValue("wday.this_acct_month").equals(getValue("spec_flag_month"))))||
      (!getValue("spec_flag").equals("Y"))) procLastMinMethod();

  procMp13Method();

  procAutoPayAmt();

  setValue("acmm.this_minimum_pay" , "0");
  setValue("acmm_auto_payment_amt" , "0");

 for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     intSeq= getValueInt("debt.bill_sort_seq",inti);
     setValue("cacr_curr_code" , mpCurrCode[intSeq]);

     mCacrThisMinimumPay[intSeq] = 
          commCurrAmt("901",mCacrThisMinimumPay[intSeq],0);

     mCacrDcThisMinimumPay[intSeq] = 
          commCurrAmt(getValue("cacr_curr_code"),mCacrDcThisMinimumPay[intSeq],0);

     setValueDouble("cacr_this_minimum_pay"    , mCacrThisMinimumPay[intSeq]);
     setValueDouble("cacr_dc_this_minimum_pay" , mCacrDcThisMinimumPay[intSeq]);

     if (getValue("cacr_curr_code.ar").equals("901"))
        setValueDouble("cacr_this_minimum_pay" , getValueDouble("cacr_dc_this_minimum_pay"));

     if (getValueDouble("cacr_dc_this_minimum_pay")<=0)
        { 
         setValueDouble("cacr_this_minimum_pay"    , 0);
         setValueDouble("cacr_dc_this_minimum_pay" , 0);
        }

     setValueDouble("acmm.this_minimum_pay" , getValueDouble("acmm.this_minimum_pay") 
                                            + getValueDouble("cacr_this_minimum_pay"));

     mCacrDcAutoPaymentAmt[intSeq] = 
       commCurrAmt(getValue("cacr_curr_code"),mCacrDcAutoPaymentAmt[intSeq],0);

     mCacrAutoPaymentAmt[intSeq] = 
       commCurrAmt("901",mCacrAutoPaymentAmt[intSeq],0);

     setValueDouble("cacr_dc_auto_payment_amt" , mCacrDcAutoPaymentAmt[intSeq]);
     setValueDouble("cacr_auto_payment_amt"    , mCacrAutoPaymentAmt[intSeq]);

     if (getValue("cacr_curr_code").equals("901"))
        setValueDouble("cacr_auto_payment_amt" , getValueDouble("cacr_dc_auto_payment_amt"));

     if (getValueDouble("cacr_dc_auto_payment_amt")<0)
        {
         setValueDouble("cacr_auto_payment_amt" , 0);
         setValueDouble("cacr_dc_auto_payment_amt" ,0);
        }

     setValueDouble("acmm_auto_payment_amt" , getValueDouble("acmm_auto_payment_amt") 
                                            + getValueDouble("cacr_auto_payment_amt")) ;

     updateCycAcmmCurr();
    }

  updateCycAcmm();
 }
// ************************************************************************
void procMp12Method() throws Exception 
 {
  for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     intSeq= getValueInt("debt.bill_sort_seq",inti);
     if (mCacrThisMinimumPay[intSeq]>=0) continue;

     mCacrThisMinimumPay[intSeq] = Math.round((mCacrThisMinimumPay[intSeq]
                                      + 0.000001)*1000.0)/1000.0;
     setValue("debt_acct_code"          , "");
     tmpstr = "MP ("+ mCacrThisMinimumPay[intSeq] +")小於 0, 最低應繳金額歸零";
     setValue("cmmd_mp_desc"            , tmpstr);
     setValue("cmmd_curr_code"          , mpCurrCode[intSeq]);
     setValueDouble("cmmd_end_bal"      , 0 - mCacrThisMinimumPay[intSeq]);
     setValueDouble("cmmd_dc_end_bal"   , 0 - mCacrDcThisMinimumPay[intSeq]); 
     setValueDouble("cmmd_mp_amt"       , 0 - mCacrThisMinimumPay[intSeq]);
     setValueDouble("cmmd_dc_mp_amt"    , 0 - mCacrDcThisMinimumPay[intSeq]); 
     setValue("cmmd_current_item"       , "N");
     setValue("debt_mcht_no"            , "");
     setValue("cmmd_mp_rate"            , "100");
     setValue("exceed_flag"             , "0");
     insertCycMpMethod(intSeq);
    }
 }
// ************************************************************************
void procLastMinMethod() throws Exception  
 {
  for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     intSeq= getValueInt("debt.bill_sort_seq",inti);
     setValue("adsm_minimum_pay_bal"    , getValue("debt.minimum_pay_bal",inti));
     setValue("adsm_dc_minimum_pay_bal" , getValue("debt.dc_minimum_pay_bal",inti));

     if (getValueDouble("debt.minimum_pay_bal",inti) <=0) continue;

     setValue("cmmd_mp_desc"            , "上期最低應繳金額餘額");
     setValue("cmmd_curr_code"          , mpCurrCode[intSeq]);
     setValueDouble("cmmd_mp_amt"       , getValueDouble("adsm_minimum_pay_bal")); 
     setValueDouble("cmmd_end_bal"      , getValueDouble("adsm_minimum_pay_bal")); 
     setValueDouble("cmmd_dc_mp_amt"    , getValueDouble("adsm_dc_minimum_pay_bal")); 
     setValueDouble("cmmd_dc_end_bal"   , getValueDouble("adsm_dc_minimum_pay_bal"));
     insertCycMpMethod(intSeq);
    }
 }
// ************************************************************************
void procMp13Method() throws Exception 
 {
  setValue("debt_acct_code"          , "");
  setValue("cmmd_current_item"       , "N");
  setValue("debt_mcht_no"            , "");
  setValue("cmmd_mp_rate"            , "100");
  setValue("exceed_flag"             , "0");

  for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     intSeq= getValueInt("debt.bill_sort_seq",inti);

     if (mCacrThisMinimumPay[intSeq]<=getValueDouble("debt.this_ttl_amt",inti)) continue;

     setValue("cmmd_curr_code"          , mpCurrCode[intSeq]);
     tmpstr = "最低應繳("+mCacrThisMinimumPay[intSeq]+")大於欠款總額["+ getValueDouble("debt.this_ttl_amt",inti)+"]";

     setValue("cmmd_mp_desc"            , tmpstr);

/*
  showLogMessage("I","","STEP 1 : ["+ getValueDouble("debt.this_ttl_amt",inti) +"]");
  showLogMessage("I","","STEP 2 : ["+ m_cacr_this_minimum_pay[int_seq] +"]");
  showLogMessage("I","","STEP 3 : ["+ Math.round((getValueDouble("debt.this_ttl_amt",inti) - m_cacr_this_minimum_pay[int_seq])*1000.0)/1000.0 +"]");
*/

     setValueDouble("cmmd_end_bal"      , Math.round((getValueDouble("debt.this_ttl_amt",inti)
                                        - mCacrThisMinimumPay[intSeq]+0.000001)*1000.0)/1000.0);
     setValueDouble("cmmd_dc_end_bal"   , Math.round((getValueDouble("debt.dc_this_ttl_amt",inti)
                                        - mCacrDcThisMinimumPay[intSeq]+0.000001)*1000.0)/1000.0);
     setValueDouble("cmmd_mp_amt"       , Math.round((getValueDouble("debt.this_ttl_amt",inti)
                                        - mCacrThisMinimumPay[intSeq]+0.000001)*1000.0)/1000.0); 
     setValueDouble("cmmd_dc_mp_amt"    , Math.round((getValueDouble("debt.dc_this_ttl_amt",inti)
                                        - mCacrDcThisMinimumPay[intSeq]+0.000001)*1000.0)/1000.0);  
     insertCycMpMethod(intSeq);
    }
 }
// ************************************************************************
void procAutoPayAmt() throws Exception 
 {
  for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     intSeq= getValueInt("debt.bill_sort_seq",inti);

     if (intSeq==1)
        if ((hBusiBusinessDate.compareTo(getValue("acno_autopay_acct_s_date"))< 0)||
            (getValue("wday.this_lastpay_date").compareTo(getValue("acno_autopay_acct_e_date"))> 0)) continue;

     if (getValue("debt.autopay_indicator",inti).equals("1"))
      {
       mCacrAutoPaymentAmt[intSeq]    = getValueDouble("debt.this_ttl_amt",inti);
       mCacrDcAutoPaymentAmt[intSeq] = getValueDouble("debt.dc_this_ttl_amt",inti);
       continue;
      }
 
     if (getValue("debt.autopay_indicator",inti).equals("2"))
        {
         mCacrAutoPaymentAmt[intSeq]    = mCacrThisMinimumPay[intSeq];
         mCacrDcAutoPaymentAmt[intSeq] = mCacrDcThisMinimumPay[intSeq];
         continue;
        }


     if (intSeq==1)
        {
         if (getValueDouble("acno_autopay_fix_amt") > 0)
            {
             mCacrAutoPaymentAmt[intSeq]    = getValueDouble("acno_autopay_fix_amt");
             mCacrDcAutoPaymentAmt[intSeq] = getValueDouble("acno_autopay_fix_amt");
            }
         else
            {
             tempLong = (int)Math.round(getValueDouble("debt.this_ttl_amt",inti) 
                                        * getValueDouble("acno_autopay_rate"))/100;
             mCacrAutoPaymentAmt[intSeq] = tempLong;
             mCacrDcAutoPaymentAmt[intSeq] = tempLong;
            }

         if (mCacrAutoPaymentAmt[intSeq] < mCacrThisMinimumPay[intSeq])
            {
             mCacrAutoPaymentAmt[intSeq]    = mCacrThisMinimumPay[intSeq];
             mCacrDcAutoPaymentAmt[intSeq] = mCacrDcThisMinimumPay[intSeq];
            }

         if (mCacrAutoPaymentAmt[intSeq] > getValueDouble("debt.this_ttl_amt",inti))
            {                                   
             mCacrAutoPaymentAmt[intSeq]      = getValueDouble("debt.this_ttl_amt",inti);
             mCacrDcAutoPaymentAmt[intSeq] = getValueDouble("debt.dc_this_ttl_amt",inti);
            }
        }
    }
 }
// ************************************************************************
void procMp10Method() throws Exception 
 {
  if (getValueDouble("acmm.this_minimum_pay") < 0) procMp12Method();
         
  procMp11Method();

  setValue("debt_acct_code"           , "");
  setValue("cmmd_current_item"        , "");
  setValue("debt_mcht_no"             , "");
  setValue("cmmd_mp_rate"             , "100");
  setValue("exceed_flag"              , "");

  if (getValueDouble("acno_special_stat_fee") !=0)
     {
      setValue("cmmd_mp_desc"            , "特殊帳單費用");
      setValue("cmmd_curr_code"          , "901");
      setValueDouble("cmmd_mp_amt"       , getValueDouble("acno_special_stat_fee"));
      setValueDouble("cmmd_end_bal"      , getValueDouble("acno_special_stat_fee"));
      setValueDouble("cmmd_dc_mp_amt"    , getValueDouble("acno_special_stat_fee"));
      setValueDouble("cmmd_dc_end_bal"   , getValueDouble("acno_special_stat_fee"));
      if (actDebtSumCnt==0) procUnexpectData();
      insertCycMpMethod(1);
     }
  if (getValueDouble("acmm_penauty_amt") !=0)
     {
      setValue("cmmd_mp_desc"           , "違約金");
      setValue("cmmd_curr_code"         , "901");
      setValueDouble("cmmd_mp_amt"      , getValueDouble("acmm_penauty_amt"));
      setValueDouble("cmmd_end_bal"     , getValueDouble("acmm_penauty_amt"));
      setValueDouble("cmmd_dc_mp_amt"   , getValueDouble("acmm_penauty_amt"));
      setValueDouble("cmmd_dc_end_bal"  , getValueDouble("acmm_penauty_amt"));
      if (actDebtSumCnt==0) procUnexpectData();
      insertCycMpMethod(1);
     }

  for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     intSeq= getValueInt("debt.bill_sort_seq",inti);

     if (getValueDouble("debt.end_fee_amt",inti) !=0)
        {
         setValue("cmmd_mp_desc"            , "本期費用金額");
         setValue("cmmd_curr_code"          , mpCurrCode[intSeq]);
         setValue("cmmd_mp_amt"             , getValue("debt.end_fee_amt",inti));
         setValue("cmmd_end_bal"            , getValue("debt.end_fee_amt",inti));   /* 本期費用金額 */ 
         setValue("cmmd_dc_mp_amt"          , getValue("debt.dc_end_fee_amt",inti));      /* 本期費用金額 */ 
         setValue("cmmd_dc_end_bal"         , getValue("debt.dc_end_fee_amt",inti));      /* 本期費用金額 */ 
         insertCycMpMethod(intSeq);
        }
     if (getValueDouble("debt.interest_amt",inti) !=0)
        {
         setValue("cmmd_mp_desc"            , "本期利息");
         setValue("cmmd_curr_code"          , mpCurrCode[intSeq]);
         setValue("cmmd_mp_amt"             , getValue("debt.interest_amt",inti));
         setValue("cmmd_end_bal"            , getValue("debt.interest_amt",inti));
         setValue("cmmd_dc_mp_amt"          , getValue("debt.dc_interest_amt",inti));
         setValue("cmmd_dc_end_bal"         , getValue("debt.dc_interest_amt",inti));
         insertCycMpMethod(intSeq);
        }
     if (getValueDouble("debt.end_bal_op",inti) !=0)
        {
         setValue("cmmd_mp_desc"             , "溢付款");
         setValue("cmmd_curr_code"           , mpCurrCode[intSeq]);
         setValueDouble("cmmd_mp_amt"        , getValueDouble("debt.end_bal_op",inti)*-1);
         setValueDouble("cmmd_end_bal"       , getValueDouble("debt.end_bal_op",inti)*-1);
         setValueDouble("cmmd_dc_mp_amt"     , getValueDouble("debt.dc_end_bal_op",inti)*-1);
         setValueDouble("cmmd_dc_end_bal"    , getValueDouble("debt.dc_end_bal_op",inti)*-1);
         insertCycMpMethod(intSeq);
        }

     if (intSeq==1)
     if (getValueDouble("debt.adi_end_bal",inti) !=0)
        {
         setValue("cmmd_mp_desc "            , "帳外息");
         setValue("cmmd_curr_code"           , "901");
         setValue("cmmd_mp_amt"              , getValue("debt.adi_end_bal",inti));
         setValue("cmmd_end_bal"             , getValue("debt.adi_end_bal",inti));
         setValue("cmmd_dc_mp_amt"           , getValue("debt.adi_end_bal",inti));
         setValue("cmmd_dc_end_bal"          , getValue("debt.adi_end_bal",inti));
         insertCycMpMethod(1);
        }
    }
 }
// ************************************************************************
void procUnexpectData() throws Exception 
 {
  mpCurrCode[1] = "901";
  setValue("debt.bill_sort_seq", "1" , 0);
  actDebtSumCnt=1; 
  setValue("debt.end_fee_amt"  , "0" , 0);
  setValue("debt.interest_amt" , "0" , 0);
  setValue("debt.end_bal_op"   , "0" , 0); 
  setValue("debt.adi_end_bal"  , "0" , 0);
 }
// ************************************************************************
void procMp11Method() throws Exception 
 {
  for (int inti=0;inti<actDebtSumCnt;inti++)
    {
     intSeq = getValueInt("debt.bill_sort_seq",inti);

     if (debug)
        {
         showLogMessage("I","","int_seq =["+ intSeq +"]");
         showLogMessage("I","","curr_code =["+ mpCurrCode[intSeq] +"]");
         showLogMessage("I","","mp_minpay =["+ mpMinPayment[intSeq] +"]");
         showLogMessage("I","","this_minpay =["+ mCacrDcThisMinimumPay[intSeq] +"]");
        }
     if (mpMinPayment[intSeq]<= mCacrDcThisMinimumPay[intSeq]) continue;

     setValue("cmmd_curr_code"                 , mpCurrCode[intSeq]);
     setValue("debt_acct_code"                 , "");
     setValueDouble("cacr_dc_this_minimum_pay" , mpMinPayment[intSeq] 
                                               - mCacrDcThisMinimumPay[intSeq]);


     if (mCacrDcThisMinimumPay[intSeq]==0)
        {
         setValueDouble("cacr_this_minimum_pay" , Math.round(getValueDouble("cacr_dc_this_minimum_pay")
                                                * mpExchangeRate[intSeq]*1000.0)/1000.0);
        }
     else
        {
         setValueDouble("cacr_this_minimum_pay" , Math.round(getValueDouble("cacr_dc_this_minimum_pay")
                                                * (mCacrThisMinimumPay[intSeq]
                                                / mCacrDcThisMinimumPay[intSeq])*1000.0)/1000.0);
        }

     tmpstr = "補小於最低應繳全額["+mpMinPayment[intSeq]+"]";
     setValue("cmmd_mp_desc"            , tmpstr);
     setValue("cmmd_end_bal"            , getValue("debt.this_ttl_amt",inti));
     setValue("cmmd_dc_end_bal"         , getValue("debt.dc_this_ttl_amt",inti));
     setValue("cmmd_mp_amt"             , getValue("cacr_this_minimum_pay"));
     setValue("cmmd_dc_mp_amt"          , getValue("cacr_dc_this_minimum_pay"));
     setValue("cmmd_current_item"       , "N");
     setValue("debt_mcht_no"            , "");
     setValue("cmmd_mp_rate"            , "100");
     setValue("exceed_flag"             , "0");
     insertCycMpMethod(intSeq);
    }
 }
// ************************************************************************
int  selectActDebtSum() throws Exception 
 {
  int inta,intj,inti;

  for (inta=0;inta<6;inta++)
    {
     mCacrAutoPaymentAmt[inta]    = 0;
     mCacrDcAutoPaymentAmt[inta] = 0;
     mCacrThisMinimumPay[inta]    = 0;
     mCacrDcThisMinimumPay[inta] = 0;
     mpCurrCode[inta]               = "";
     for (inti=0;inti<20;inti++) mTempEndAmt[inta][inti] = 0;
    }
  setValueDouble("adsm_end_cap_amt"  , 0);
   
  setValue("debt.p_seqno",getValue("p_seqno"));
  actDebtSumCnt = getLoadData("debt.p_seqno");

  if (actDebtSumCnt==0) return(1);

  for (inti=0;inti<actDebtSumCnt;inti++)
    {
     intj = Integer.valueOf(getValue("debt.bill_sort_seq",inti));
     mpCurrCode[intj] = getValue("debt.curr_code",inti);

     mTempEndAmt[intj][0] = getValueDouble("debt.end_1_bl_amt",inti);  /* 第一類 BL 欠款金額    */ 
     mTempEndAmt[intj][1] = getValueDouble("debt.end_1_ao_amt",inti);   /* 第一類 AO 欠款金額    */ 
     mTempEndAmt[intj][2] = getValueDouble("debt.end_1_ot_amt",inti);   /* 第一類 OT 欠款金額    */ 
     mTempEndAmt[intj][3] = getValueDouble("debt.end_1_ca_amt",inti);   /* 第一類 CA 欠款金額    */
     mTempEndAmt[intj][4] = getValueDouble("debt.end_1_id_amt",inti);   /* 第一類 ID 欠款金額    */    
     mTempEndAmt[intj][5] = getValueDouble("debt.end_2_it_amt",inti);   /* 第二類 IT 欠款金額    */ 

     mTempEndAmt[intj][10] = getValueDouble("debt.end_1_3bl_amt",inti)  /* 第一類+第三類 BL 欠款金額    */ 
                              - getValueDouble("debt.end_1_bl_amt",inti);    
     mTempEndAmt[intj][11] = getValueDouble("debt.end_1_3ao_amt",inti)  /* 第一類+第三類 AO 欠款金額    */ 
                              - getValueDouble("debt.end_1_ao_amt",inti);     
     mTempEndAmt[intj][12] = getValueDouble("debt.end_1_3ot_amt",inti)  /* 第一類+第三類 OT 欠款金額    */ 
                              - getValueDouble("debt.end_1_ot_amt",inti);    
     mTempEndAmt[intj][13] = getValueDouble("debt.end_1_3ca_amt",inti)  /* 第一類+第三類 CA 欠款金額    */ 
                              - getValueDouble("debt.end_1_ca_amt",inti);
     mTempEndAmt[intj][14] = getValueDouble("debt.end_1_3id_amt",inti)  /* 第一類+第三類 ID 欠款金額    */ 
                              - getValueDouble("debt.end_1_id_amt",inti);        
     mTempEndAmt[intj][15] = getValueDouble("debt.end_2_3it_amt",inti)  /* 第二類+第三類 IT 欠款金額    */ 
                              - getValueDouble("debt.end_2_it_amt",inti);    
     mTempEndAmt[intj][16] = getValueDouble("debt.end_3cb_amt",inti);   /*        第三類 CB 欠款金額    */ 
     mTempEndAmt[intj][17] = getValueDouble("debt.end_3db_amt",inti);   /*        第三類 DB 欠款金額    */ 

     mTempDcEndAmt[intj][0]  = getValueDouble("debt.dc_end_1_bl_amt",inti);     /* 第一類 BL 欠款金額    */ 
     mTempDcEndAmt[intj][1]  = getValueDouble("debt.dc_end_1_ao_amt",inti);     /* 第一類 AO 欠款金額    */ 
     mTempDcEndAmt[intj][2]  = getValueDouble("debt.dc_end_1_ot_amt",inti);     /* 第一類 OT 欠款金額    */ 
     mTempDcEndAmt[intj][3]  = getValueDouble("debt.dc_end_1_ca_amt",inti);     /* 第一類 CA 欠款金額    */ 
     mTempDcEndAmt[intj][4]  = getValueDouble("debt.dc_end_1_id_amt",inti);     /* 第一類 ID 欠款金額    */ 
     mTempDcEndAmt[intj][5]  = getValueDouble("debt.dc_end_2_it_amt",inti);     /* 第二類 IT 欠款金額    */ 

     mTempDcEndAmt[intj][10] = getValueDouble("debt.dc_end_1_3bl_amt",inti)     /* 第一類+第三類 BL 欠款金額    */ 
                                 - getValueDouble("debt.dc_end_1_bl_amt",inti);    
     mTempDcEndAmt[intj][11] = getValueDouble("debt.dc_end_1_3ao_amt",inti)     /* 第一類+第三類 AO 欠款金額    */ 
                                 - getValueDouble("debt.dc_end_1_ao_amt",inti);   
     mTempDcEndAmt[intj][12] = getValueDouble("debt.dc_end_1_3ot_amt",inti)     /* 第一類+第三類 OT 欠款金額    */ 
                                 - getValueDouble("debt.dc_end_1_ot_amt",inti);    
     mTempDcEndAmt[intj][13] = getValueDouble("debt.dc_end_1_3ca_amt",inti)     /* 第一類+第三類 CA 欠款金額    */ 
                                 - getValueDouble("debt.dc_end_1_ca_amt",inti);    
     mTempDcEndAmt[intj][14] = getValueDouble("debt.dc_end_1_3id_amt",inti)     /* 第一類+第三類 ID 欠款金額    */ 
                                 - getValueDouble("debt.dc_end_1_id_amt",inti);    
     mTempDcEndAmt[intj][15] = getValueDouble("debt.dc_end_2_3it_amt",inti)     /* 第二類+第三類 IT 欠款金額    */ 
                                 - getValueDouble("debt.dc_end_2_it_amt",inti);
     mTempDcEndAmt[intj][16] = getValueDouble("debt.dc_end_3cb_amt",inti);      /*        第三類 CB 欠款金額    */ 
     mTempDcEndAmt[intj][17] = getValueDouble("debt.dc_end_3db_amt",inti);      /*        第三類 DB 欠款金額    */ 

     setValueDouble("adsm_end_cap_amt" ,  getValueDouble("adsm_end_cap_amt") 
                                       + getValueDouble("debt.end_cap_amt",inti));       /* 累計本金類欠款金額    */ 
    }

  for (intj=0;intj<20;intj++)
    for (inti=1;inti<6;inti++)
       mTempEndAmt[0][intj] = mTempEndAmt[0][intj] + mTempEndAmt[inti][intj];

  return(0);
 }
// ************************************************************************
 void  loadActDebtSum() throws Exception
 {
  extendField = "debt.";
  selectSQL = "";
  daoTable  = "act_debt_sum_"+getValue("wday.stmt_cycle");
  whereStr  = "ORDER  BY p_seqno,bill_sort_seq";

  int  n = loadTable();
  setLoadData("debt.p_seqno");
  showLogMessage("I","","Load act_debt_sum Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActDebtIntr1() throws Exception
 {
  extendField = "intr1.";
  selectSQL = "p_seqno,"
            + "bill_sort_seq,"
            + "curr_seq,"
            + "end_bal,"
            + "dc_end_bal";
  daoTable  = "act_debt_intr_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE end_bal        > 0 "
            + "AND   acct_code_flag = 'Y' "
            + "ORDER BY p_seqno,acct_month desc,post_date desc,"
            + "           purchase_date desc,curr_seq desc,curr_code desc";

  int  n = loadTable();
  setLoadData("intr1.p_seqno");
  showLogMessage("I","","Load act_debt_intr1 Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActDebtIntr2() throws Exception
 {
  extendField = "intr2.";
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "mcht_no,"
            + "decode(new_it_flag,'N',mp_rate,100) as mp_rate,"
            + "sum(end_bal) as end_bal,"
            + "sum(dc_end_bal) as dc_end_bal";
  daoTable  = "act_debt_intr_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE acct_code = 'IT' "
            + "AND   acct_month >= ? "
            + "and   purchase_date >= '20060401' "                     /*** 固定值 ***/
            + "group by p_seqno,curr_code,mcht_no,decode(new_it_flag,'N',mp_rate,100) "
            + "order by p_seqno,curr_code "
            ;

  setString(1, getValue("wday.this_acct_month"));
  int  n = loadTable();
  setLoadData("intr2.p_seqno,intr2.curr_code");

  showLogMessage("I","","Load act_debt_intr2 Count: ["+n+"]");
 }
// ************************************************************************
 void selectPtrCurrRate() throws Exception 
 {
  selectSQL = "b.bill_sort_seq,"
            + "max(a.exchange_rate) as exchange_rate,"
            + "max(c.min_payment) as min_payment";
  daoTable  = "ptr_curr_rate a,ptr_currcode b,ptr_curr_general c";
  whereStr  = "WHERE  a.curr_code = b.curr_code "
            + "AND    b.curr_code = c.curr_code "
            + "AND    b.bill_sort_seq != '' "
            + "group by b.bill_sort_seq"
            ;

  int recCnt = selectTable();

  if (recCnt==0) return;

  for ( int inti=0; inti<recCnt; inti++ ) mpExchangeRate[inti]=0;
   
  for ( int inti=0; inti<recCnt; inti++ )
    {
     mpExchangeRate[Integer.valueOf(getValue("bill_sort_seq",inti))] = getValueDouble("exchange_rate",inti);
     mpMinPayment[Integer.valueOf(getValue("bill_sort_seq",inti))] = getValueDouble("min_payment",inti);
    }
 }
// ************************************************************************
 void procExceedMp() throws Exception
 {
  int inta,intj,inti;

  double tempRate,exceedAmt;
  double[][] mpExceedAmt    = new double[10][10];
  double[][] mpDcExceedAmt = new double[6][10];

  for (inti=0;inti<10;inti++) 
    for (intj=0;intj<6;intj++) 
      {
       mpExceedAmt[intj][inti]=0;
       mpDcExceedAmt[intj][inti]=0;
      }

  exceedAmt = getValueDouble("adsm_end_cap_amt") 
             - getValueDouble("line_of_credit_amt") 
             + getValueDouble("unpost_installment");  /* 超額金類 */
   
  setValue("intr1.p_seqno",getValue("p_seqno"));
  int actDebtIntrCnt = getLoadData("intr1.p_seqno");

  if (actDebtIntrCnt==0) return;

  String hPcceBillSortSeq = "";
  for (inti=0;inti<actDebtIntrCnt;inti++)
      {
       hPcceBillSortSeq     = getValue("intr1.bill_sort_seq",inti);
       int hTempCurrSeq      = getValueInt("intr1.curr_seq",inti);
       double hDebtEndBal    = getValueDouble("intr1.end_bal",inti);
       double hDebtDcEndBal = getValueDouble("intr1.dc_end_bal",inti);

       intj = Integer.valueOf(hPcceBillSortSeq);
       if (hDebtEndBal > exceedAmt)
          {
           tempRate = hDebtEndBal/hDebtDcEndBal*1.0;
           hDebtEndBal = exceedAmt;
           hDebtDcEndBal = hDebtEndBal/tempRate;
          }
       mpExceedAmt[intj][hTempCurrSeq] = mpExceedAmt[intj][hTempCurrSeq] + hDebtEndBal;

       mpDcExceedAmt[intj][hTempCurrSeq] = mpDcExceedAmt[intj][hTempCurrSeq] + hDebtDcEndBal;
       exceedAmt = exceedAmt - hDebtEndBal;
       if (exceedAmt<0) exceedAmt=0;
       setValueDouble("temp_end_a_amt" , exceedAmt);

       if (exceedAmt<=0) break;
      }

  for (inti=0;inti<9;inti++) 
    for (intj=1;intj<6;intj++)
      {
       if (mpExceedAmt[intj][inti]<=0) continue;
       if (mTempEndAmt[intj][10+inti]>mpExceedAmt[intj][inti])
           {
            mTempEndAmt[intj][10+inti]    = mTempEndAmt[intj][10+inti] 
                                             - mpExceedAmt[intj][inti];
            mTempDcEndAmt[intj][10+inti] = mTempDcEndAmt[intj][10+inti] 
                                             - mpDcExceedAmt[intj][inti];
           }
        else
           {
            mTempEndAmt[intj][inti]       = mTempEndAmt[intj][inti] 
                                             + mTempEndAmt[intj][10+inti] 
                                             - mpExceedAmt[intj][inti];
            mTempDcEndAmt[intj][inti]    = mTempDcEndAmt[intj][inti] 
                                             + mTempDcEndAmt[intj][10+inti] 
                                             - mpDcExceedAmt[intj][inti];
            mTempEndAmt[intj][10+inti]    = 0;
            mTempDcEndAmt[intj][10+inti] = 0;
           }
       intSeq= intj;
       setValue("cmmd_curr_code"        , mpCurrCode[intSeq]);
       setValueDouble("cmmd_end_bal"    , mpExceedAmt[intj][inti]);
       setValueDouble("cmmd_dc_end_bal" , mpDcExceedAmt[intj][inti]);
       procMp8Method(inti);
      }
  return;
 }
// ************************************************************************
 void selectActDebt1() throws Exception
 {
  setValue("intr2.p_seqno", getValue("p_seqno"));
  setValue("intr2.curr_code",getValue("cmmd_curr_code"));
  int cnt1 = getLoadData("intr2.p_seqno,intr2.curr_code");

  for (int inti=0;inti<cnt1;inti++)
    {
     setValue("debt_mcht_no"            ,  getValue("intr2.mcht_no",inti));
     double hDebtEndBal              =  getValueDouble("intr2.end_bal",inti);
     double hDebtDcEndBal           =  getValueDouble("intr2.dc_end_bal",inti); 
     setValueInt("merc_mp_rate"         ,  getValueInt("intr2.mp_rate",inti));

     if (getValueInt("merc_mp_rate") < getValueInt("agnn_mp_1_rate")) 
        setValueInt("merc_mp_rate" ,  getValueInt("agnn_mp_1_rate"));

     setValueDouble("cmmd_end_bal"    , hDebtEndBal);
     setValueDouble("cmmd_dc_end_bal" , hDebtDcEndBal);
     if (getValueDouble("temp_end_a_amt") + hDebtEndBal > getValueDouble("line_of_credit_amt") 
                                                           - getValueDouble("unpost_installment"))
        {
         setValueDouble("cmmd_end_bal" , getValueDouble("line_of_credit_amt") 
                                       - getValueDouble("unpost_installment") 
                                       - getValueDouble("temp_end_a_amt"));

         double tempLong          = Math.round((getValueDouble("cmmd_end_bal")
                                   / mpExchangeRate[intSeq])*100.0);
         setValueDouble("cmmd_dc_end_bal"  , tempLong/100.0);
        }

     if (getValueDouble("cmmd_end_bal") > 0) procMp4Method();

     setValueDouble("temp_end_a_amt"     , getValueDouble("temp_end_a_amt") + getValueDouble("cmmd_end_bal"));
     setValueDouble("temp_end_7b_amt"    , getValueDouble("temp_end_7b_amt") + getValueDouble("cmmd_end_bal"));
     setValueDouble("temp_dc_end_7b_amt" , getValueDouble("temp_dc_end_7b_amt") + getValueDouble("cmmd_dc_end_bal"));
     if (getValueDouble("temp_end_a_amt") >= getValueDouble("line_of_credit_amt") 
                                          -  getValueDouble("unpost_installment")) break;
    }
  setValue("debt_mcht_no" , "");
  return;
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
  
}  // End of class FetchSample

