/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/12/24  V1.00.10  Allen Ho   cyc_A319 PROD compare OK                   *
* 111/10/17  V1.00.02  Yang Bo    sync code from mega                        *
* 112/03/09  V1.00.03  Simon      add unprint_flag to act_acct_hst           *
* 112/05/12  V1.00.04  Simon      add unprint_flag_regular to act_acct_hst   *
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class CycA480 extends AccessDAO
{
 private final String PROGNAME = "關帳-帳務資料主檔新增處理程式 112/05/12  V1.00.04";
 CommFunction comm = new CommFunction();

 String businessDate = "";
 String hWdayStmtCycle = "";
 String hWdayLastAcctMonth = "";
 String hWdayLastCloseDate = "";
 String hWdayThisLastpayDate = "";

 String[]  billStr = new String[4];
 long    totalCnt=0,currCnt=0;
 boolean debug = false;
 int paymentAmt = 0,insertCnt=0,updateCnt=0,pcodCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA480 proc = new CycA480();
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
   
   if ( !connectDataBase() )
       return(1);

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料...");
   loadActDebt();
// load_ptr_curr_rate();
   selectPtrActcode();
   showLogMessage("I","","Business_date["+ businessDate +"]");
   showLogMessage("I","","last_acct_month["+ hWdayLastAcctMonth +"]");
   showLogMessage("I","","last_close_Date["+ hWdayLastCloseDate +"]");
   if (debug)                    
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","刪除 act_acct_hst...");
       deleteActAcctHst();
       showLogMessage("I","","total_count=["+totalCnt+"]");
       showLogMessage("I","","=========================================");
       showLogMessage("I","","刪除 act_curr_hst...");
       deleteActCurrHst();
       showLogMessage("I","","total_count=["+totalCnt+"]");
       commitDataBase();
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","新增 act_acct_hst...");
   totalCnt=0;
   selectCycAcmm1();
   showLogMessage("I","","total_count=["+totalCnt+"]");
  
   showLogMessage("I","","=========================================");
   showLogMessage("I","","新增 act_curr_hst...");
   totalCnt=0;
   selectCycAcmm2();
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
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1, businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  hWdayStmtCycle =  getValue("STMT_CYCLE");
  hWdayLastAcctMonth =  getValue("last_acct_month");
  hWdayLastCloseDate =  getValue("last_close_date");
  hWdayThisLastpayDate =  getValue("this_lastpay_date");

  return(0);
 }
// ************************************************************************
 void selectCycAcmm1() throws Exception
 {
  selectSQL = "c.p_seqno,"
            + "c.last_interest_date,"
            + "c.acct_type,"
            + "b.corp_p_seqno,"
            + "b.id_p_seqno,"
            + "b.acct_jrnl_bal,"
            + "b.beg_bal_op,"
            + "b.end_bal_op,"
            + "b.beg_bal_lk,"
            + "b.end_bal_lk,"
            + "b.overpay_lock_due_date,"
            + "c.interest_amt as bill_interest,"
            + "b.min_pay,"
            + "b.min_pay_bal,"
            + "b.autopay_beg_amt,"
            + "b.autopay_bal,"
            + "b.pay_by_stage_amt,"
            + "b.pay_by_stage_bal,"
            + "b.pay_by_stage_date,"
            + "b.payment_status,"
            + "b.last_payment_date,"
            + "b.last_min_pay_date,"
            + "b.last_cancel_debt_date,"
            + "c.cycle_date as stmt_cycle_date,"
            + "c.auto_pay_date as stmt_auto_pay_date,"
            + "c.payment_number as stmt_payment_no,"
            + "c.autopay_acct_bank as stmt_auto_pay_bank,"
            + "c.auto_pay_acct as stmt_auto_pay_no,"
            + "c.auto_payment_amt as stmt_auto_pay_amt,"
            + "c.credit_limit as stmt_credit_limit,"
            + "c.revolving_rate as stmt_revol_rate,"
            + "c.last_ttl_amt as stmt_last_ttl,"
            + "c.payment_amt as stmt_payment_amt,"
            + "c.adjust_amt as stmt_adjust_amt,"
            + "c.new_amt as stmt_new_amt,"
            + "c.this_ttl_amt as stmt_this_ttl_amt,"
            + "c.this_minimum_pay as stmt_mp,"
            + "c.lastpay_date as stmt_last_payday,"
            + "c.last_month_bonus as stmt_last_month_bp,"
            + "c.new_add_bonus as stmt_new_add_bp,"
            + "c.adjust_bonus as stmt_adjust_bp,"
            + "c.use_bonus as stmt_use_bp,"
            + "c.give_bonus as stmt_give_bp,"
            + "c.net_bonus as stmt_net_bp,"
            + "c.erase_bonus as stmt_erase_bp,"
            + "c.erase_date as stmt_erase_date,"
            + "c.give_reason_a as stmt_give_reason1,"
            + "c.give_reason_b as stmt_give_reason2,"
            + "c.give_reason_c as stmt_give_reason3,"
            + "c.give_reason_d as stmt_give_reason4,"
            + "c.minimum_pay_bal as stmt_over_due_amt,"
            + "c.gold_card as stmt_gold_card,"
            + "b.ttl_amt_bal,"
            + "b.ttl_amt_bal as waive_ttl_bal,"
            + "b.temp_unbill_interest,"
            + "b.delaypay_ok_flag,"
            + "c.revolve_int_sign,"
            + "c.revolve_int_rate,"
            + "c.revolve_rate_s_month,"
            + "c.revolve_rate_e_month,"
            + "c.revolve_int_sign_2,"
            + "c.revolve_int_rate_2,"
            + "c.revolve_rate_s_month_2,"
            + "c.revolve_rate_e_month_2,"
            + "c.unprint_flag,"
            + "c.unprint_flag_regular,"
            + "c.combo_indicator,"
            + "c.combo_acct_no";
  daoTable  = "act_acct  b, cyc_acmm_"+ hWdayStmtCycle +" c"; 
  whereStr  = "WHERE  b.p_seqno    = c.p_seqno"
            ;

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    if (getValue("last_interest_date").length()==0) 
       setValue("last_interest_date", hWdayLastCloseDate);
    if (getValue("last_payment_date").equals("99999999")) 
       setValue("last_payment_date","");
    if (getValue("last_min_pay_date").equals("99999999")) 
       setValue("last_min_pay_date","");
    if (getValue("last_cancel_debt_date").equals("99999999")) 
       setValue("last_cancel_debt_date","");

    initDefaultValue();

    setValue("debt.p_seqno",getValue("p_seqno"));
    int cnt1 = getLoadData("debt.p_seqno");
    for (int inti=0;inti<cnt1;inti++) procActDebt(inti);

    insertActAcctHst();
    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void selectCycAcmm2() throws Exception
 {
  selectSQL = "b.p_seqno,"
            + "c.last_interest_date,"
            + "b.curr_code,"
            + "b.bill_curr_code,"
            + "b.bill_sort_seq,"
            + "c.acct_type,"
            + "d.dc_acct_jrnl_bal as acct_jrnl_bal,"
            + "c.auto_pay_date as stmt_auto_pay_date,"
            + "b.autopay_acct_bank as stmt_auto_pay_bank,"
            + "b.autopay_acct_no as stmt_auto_pay_no,"
            + "b.dc_auto_payment_amt as stmt_auto_pay_amt,"
            + "b.autopay_dc_flag as stmt_auto_dc_flag,"
            + "b.dc_last_ttl_amt as stmt_last_ttl,"
            + "b.dc_payment_amt as stmt_payment_amt,"
            + "b.dc_adjust_amt as stmt_adjust_amt,"
            + "b.dc_new_amt as stmt_new_amt,"
            + "b.dc_this_ttl_amt as stmt_this_ttl_amt,"
            + "b.dc_this_minimum_pay as stmt_mp,"
            + "b.dc_minimum_pay_bal as  stmt_over_due_amt,"
            + "d.dc_temp_unbill_interest as  temp_unbill_interest,"
            + "d.dc_ttl_amt_bal as ttl_amt_bal,"
            + "d.dc_min_pay as min_pay,"
            + "d.dc_min_pay_bal as min_pay_bal,"
            + "d.dc_ttl_amt_bal as waive_ttl_bal,"
            + "d.delaypay_ok_flag";
  daoTable  = "cyc_acmm_curr_"+ hWdayStmtCycle +" b,act_acct_curr d,cyc_acmm_"+ hWdayStmtCycle +" c";
  whereStr  = "WHERE  b.p_seqno = c.p_seqno "
            + "AND    d.p_seqno = b.p_seqno "
            + "AND    d.curr_code =  b.curr_code ";

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    if (getValue("last_interest_date").length()==0) 
       setValue("last_interest_date", hWdayLastCloseDate);

    if (!getValue("curr_code").equals("901")) 
       setValue("stmt_auto_pay_date", hWdayThisLastpayDate);

    initDefaultValue();

    setValue("debt.p_seqno"   , getValue("p_seqno"));
    setValue("debt.curr_code" , getValue("curr_code"));
    int cnt1 = getLoadData("debt.p_seqno,debt.curr_code");

    for (int inti=0;inti<cnt1;inti++) 
      {
       setValue("debt.beg_bal" , getValue("debt.dc_beg_bal",inti) , inti);
       setValue("debt.end_bal" , getValue("debt.dc_end_bal",inti) , inti);
       procActDebt(inti);
      }

    insertActCurrHst();
    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void initDefaultValue() throws Exception
 {
  for (int inti=0;inti<pcodCnt;inti++)
    {
     billStr[0] = "unbill_beg_bal_"+getValue("pcod.acct_code",inti);
     billStr[1] = "unbill_end_bal_"+getValue("pcod.acct_code",inti);
     billStr[2] = "billed_beg_bal_"+getValue("pcod.acct_code",inti);
     billStr[3] = "billed_end_bal_"+getValue("pcod.acct_code",inti);
 
     for (int intk=0;intk<4;intk++) 
        {
         setValue(billStr[intk] , "0");
          if (getValue("pcod.acct_code",inti).toLowerCase().equals("db"))
             {
              setValue(billStr[intk]+"_b" , "0");
              setValue(billStr[intk]+"_c" , "0");
              setValue(billStr[intk]+"_i" , "0");
             }
        }
    }
 }
// ************************************************************************
 void procActDebt(int inti) throws Exception
 {
  int amtValue=0;
  if (getValue("debt.post_date",inti).compareTo(getValue("last_interest_date"))>0)
     {
      setValueDouble("unbill_beg_bal_"+getValue("debt.acct_code",inti), 
                  getValueDouble("unbill_beg_bal_"+getValue("debt.acct_code",inti)) +
                  getValueDouble("debt.beg_bal",inti) + 0.000001);

      setValueDouble("unbill_end_bal_"+getValue("debt.acct_code",inti), 
                  getValueDouble("unbill_end_bal_"+getValue("debt.acct_code",inti)) +
                  getValueDouble("debt.end_bal",inti)+ 0.000001);

      if (getValue("debt.acct_code_type",inti).length()!=0)
         {
          setValueDouble("unbill_beg_bal_"+getValue("debt.acct_code",inti)+"_"+getValue("debt.acct_code_type",inti),
                      getValueDouble("unbill_beg_bal_"+getValue("debt.acct_code",inti)+"_"+getValue("debt.acct_code_type",inti)) +
                      getValueDouble("debt.beg_bal",inti)+ 0.000001);

          setValueDouble("unbill_end_bal_"+getValue("debt.acct_code",inti)+"_"+getValue("debt.acct_code_type",inti),
                      getValueDouble("unbill_end_bal_"+getValue("debt.acct_code",inti)+"_"+getValue("debt.acct_code_type",inti)) +
                      getValueDouble("debt.end_bal",inti)+ 0.000001);
         }
     }
   else
     {
      setValueDouble("billed_beg_bal_"+getValue("debt.acct_code",inti), 
                  getValueDouble("billed_beg_bal_"+getValue("debt.acct_code",inti)) +
                  getValueDouble("debt.beg_bal",inti)+ 0.000001);
      setValueDouble("billed_end_bal_"+getValue("debt.acct_code",inti), 
                  getValueDouble("billed_end_bal_"+getValue("debt.acct_code",inti)) +
                  getValueDouble("debt.end_bal",inti)+ 0.000001);

      if (getValue("debt.acct_code_type",inti).length()!=0)
         {
          setValueDouble("billed_beg_bal_"+getValue("debt.acct_code",inti)+"_"+getValue("debt.acct_code_type",inti),
                      getValueDouble("billed_beg_bal_"+getValue("debt.acct_code",inti)+"_"+getValue("debt.acct_code_type",inti)) +
                      getValueDouble("debt.beg_bal",inti)+ 0.000001);

          setValueDouble("billed_end_bal_"+getValue("debt.acct_code",inti)+"_"+getValue("debt.acct_code_type",inti),
                      getValueDouble("billed_end_bal_"+getValue("debt.acct_code",inti)+"_"+getValue("debt.acct_code_type",inti)) +
                      getValueDouble("debt.end_bal",inti)+ 0.000001);
         }
     }
 }
// ************************************************************************
 int insertActAcctHst() throws Exception
 {
  dateTime();

  setValue("acct_month"           , hWdayLastAcctMonth);
  setValue("rc_min_pay"           , "0");
  setValue("rc_min_pay_bal"       , "0");
  setValue("rc_min_pay_m0"        , "0");
  setValue("run_e_month"          , getValue("run_e_month"));
  setValue("run_e_month_2"        , getValue("run_e_month_2"));
  setValue("run_e_month_cnt"      , getValue("run_e_month_cnt"));
  setValue("his_combo_cash_amt"   , getValue("his_combo_cash_amt"));
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);

  daoTable  = "act_acct_hst";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertActCurrHst() throws Exception
 {
  dateTime();

  setValue("acct_month"              , hWdayLastAcctMonth);
  setValue("stmt_cycle"              , hWdayStmtCycle);
  setValue("mod_time"                , sysDate+sysTime);
  setValue("mod_pgm"                 , javaProgram);

  daoTable  = "act_curr_hst";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteActAcctHst() throws Exception
 {
  daoTable  = "act_acct_hst a";
  whereStr  = "WHERE  acct_month = ? "
            + "AND    stmt_cycle = ? "
            + "AND    p_seqno in (select p_seqno from cyc_acmm_"+ hWdayStmtCycle +") ";

  setString(1 , hWdayLastAcctMonth);
  setString(2 , hWdayStmtCycle);

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void deleteActCurrHst() throws Exception
 {
  daoTable  = "act_curr_hst a";
  whereStr  = "WHERE  acct_month = ? "
            + "AND    stmt_cycle = ? "
            + "AND    p_seqno in (select p_seqno from cyc_acmm_"+ hWdayStmtCycle +") ";

  setString(1 , hWdayLastAcctMonth);
  setString(2 , hWdayStmtCycle);

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 public void loadActDebt() throws Exception
 {
  extendField = "debt.";
  selectSQL = "a.p_seqno, "
            + "a.curr_code,"
            + "a.acct_code,"
            + "a.acct_code_type,"
            + "a.post_date,"
            + "a.beg_bal,"
            + "a.end_bal,"
            + "a.dc_beg_bal,"
            + "a.dc_end_bal";
  daoTable  = "act_debt a,cyc_acmm_"+ hWdayStmtCycle +" b";
  whereStr  = "WHERE a.p_seqno = b.p_seqno "
            + "order by a.p_seqno,a.curr_code";

  int  n = loadTable();
  setLoadData("debt.p_seqno");
  setLoadData("debt.p_seqno,debt.curr_code");

  showLogMessage("I","","Load act_debt Count: ["+n+"]");
 }
// ************************************************************************
 void loadPtrCurrRate() throws Exception
 {
  extendField = "curr.";
  selectSQL = "curr_code";
  daoTable  = "ptr_curr_rate";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("curr.curr_code");

  showLogMessage("I","","Load ptr_curr_rate Count: ["+n+"]");
 }
// ************************************************************************
 int selectPtrActcode() throws Exception
 {
  extendField = "pcod.";
  selectSQL = "";
  daoTable  = "ptr_actcode";
  whereStr  = "";

  pcodCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample


