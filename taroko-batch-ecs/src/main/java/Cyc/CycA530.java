/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/07/31  V1.00.06  Allen Ho   cyc_A600                                   *
* 111/10/18  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class CycA530 extends AccessDAO
{
 private final String PROGNAME = "關帳-對帳單計算繳納次數首期篩選處理程式 111/10/18  V1.00.02";
 CommFunction comm = new CommFunction();

 String hBusiBusinessDate    = "";
 int    minPayCnt            = 0;
 double minPayAmt            = 0;

 long    totalCnt=0,updateCnt=0;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA530 proc = new CycA530();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int  mainProcess(String[] args) {
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

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 )
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) return(1);

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       showLogMessage("I","","=========================================");
       return(0);
      }

   loadPtrCurrcode();
   showLogMessage("I","","刪除 cyc_capital_dtl");
   deleteCycCapitalDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","篩選資料處理....");
   totalCnt=0;
   selectActCurrHst();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
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

  if (hBusiBusinessDate.length()==0)
      hBusiBusinessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ hBusiBusinessDate +"]");
 }
// ************************************************************************ 
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1, hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void selectActCurrHst() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.curr_code,"
            + "a.acct_type,"
            + "b.dc_min_pay as min_pay,"
            + "unbill_end_bal_bl+billed_end_bal_bl+ "             // 總欠本金
            + "    unbill_end_bal_it+billed_end_bal_it+ "
            + "    unbill_end_bal_id+billed_end_bal_id+ "
            + "    unbill_end_bal_ot+billed_end_bal_ot+ "
            + "    unbill_end_bal_ca+billed_end_bal_ca+ "
            + "    unbill_end_bal_ao+billed_end_bal_ao+ "
            + "    unbill_end_bal_db+billed_end_bal_db+ "
            + "    unbill_end_bal_cb+billed_end_bal_cb as billed_end_bal_bl, "
            + "unbill_end_bal_bl+unbill_end_bal_it+ "                         // 當期本金
            + "    unbill_end_bal_id+unbill_end_bal_ot+ "
            + "    unbill_end_bal_ca+unbill_end_bal_ao+ "
            + "    unbill_end_bal_db+unbill_end_bal_cb as unbill_end_bal_bl, "
            + "billed_end_bal_lf+unbill_end_bal_lf+ "                        // 總欠費用
            + "    billed_end_bal_af+unbill_end_bal_af+ "
            + "    billed_end_bal_pf+unbill_end_bal_pf+ "
            + "    billed_end_bal_sf+unbill_end_bal_sf+ "
            + "    billed_end_bal_cf+unbill_end_bal_cf+ "
            + "    billed_end_bal_cc+unbill_end_bal_cc as billed_end_bal_af, "
            + "billed_end_bal_ri+unbill_end_bal_ri+     "                           // 總欠利息
            + "    billed_end_bal_ai+unbill_end_bal_ai+ "
            + "    billed_end_bal_ci+unbill_end_bal_ci as billed_end_bal_ri, "
            + "billed_end_bal_pn+unbill_end_bal_pn as billed_end_bal_pn, "          // 總欠違約金
            + "unbill_beg_bal_af+unbill_beg_bal_cc+ "
            + "    unbill_beg_bal_cf+unbill_beg_bal_pn+ "
            + "    unbill_beg_bal_lf+ "
            + "    unbill_beg_bal_pf+unbill_beg_bal_sf as fee_amt, "                // 當期費用+違約金
            + "unbill_beg_bal_ai+unbill_beg_bal_ci+ "                               // 當期利息
            + "    unbill_beg_bal_ri as interest_amt ";
  daoTable  = "act_curr_hst a,act_acct_curr b";
  whereStr  = "where  a.acct_month   = ? "
            + "and    a.stmt_cycle   = ? "
            + "and    a.p_seqno    = b.p_seqno "
            + "and    a.curr_code  = b.curr_code "
            ;

  setString(1, getValue("wday.last_acct_month"));
  setString(2, getValue("wday.stmt_cycle"));

  openCursor();

  totalCnt=0;

  while( fetchTable() )
   {
    totalCnt++;
    minPayCnt = 0;
    minPayAmt = 0;

    if (getValueDouble("min_pay")>0)
    if (getValueDouble("min_pay") >= commCurrAmt(getValue("curr_code") 
                                    , (getValueDouble("billed_end_bal_bl")
                                    + getValueDouble("billed_end_bal_af")
                                    + getValueDouble("billed_end_bal_ri")
                                    + getValueDouble("billed_end_bal_pn")
                                    + 0.000001),0))
       {
        minPayCnt = 1;
        minPayAmt = getValueDouble("min_pay");
       }

    if (updateCycBillExt()!=0) insertCycBillExt();

    if (getValueDouble("min_pay") >= commCurrAmt(getValue("curr_code") 
                                    , (getValueDouble("billed_end_bal_bl")
                                    +  getValueDouble("billed_end_bal_af")
                                    +  getValueDouble("billed_end_bal_ri")
                                    +  getValueDouble("billed_end_bal_pn")
                                    +  0.000001),0)) continue;

    if (getValueDouble("min_pay")<=0) continue;

    insertCycCapitalDtl();
   } 
  closeCursor();
  return;    
 }           
// ************************************************************************
 int updateCycBillExt() throws Exception
 {
  dateTime();
  String monthMm = getValue("wday.this_acct_month").substring(4,6);

  updateSQL = "min_pay_cnt_" + monthMm + "  = ?, "
            + "min_pay_amt_" + monthMm + "  = ?, " 
            + "capital_amt_" + monthMm + "  = ?, " 
            + "fee_amt_" + monthMm + "      = ?, " 
            + "interest_amt_" + monthMm + " = ?, "
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";
  daoTable  = "cyc_bill_ext";
  whereStr  = "WHERE  p_seqno   = ? "
            + "AND    acct_year = ? "
            + "AND    curr_code = ? ";

  setInt(1    , minPayCnt);
  setDouble(2 , minPayAmt); 
  setDouble(3 , getValueDouble("billed_end_bal_bl")); 
  setDouble(4 , getValueDouble("fee_amt")); 
  setDouble(5 , getValueDouble("interest_amt")); 
  setString(6 , javaProgram);
  setString(7 , getValue("p_seqno"));
  setString(8 , getValue("wday.this_acct_month").substring(0,4));
  setString(9 , getValue("curr_code"));

  updateTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 void deleteCycCapitalDtl() throws Exception
 {
  daoTable  = "cyc_capital_dtl";
  whereStr  = "where acct_month = ? "
              + "and   p_seqno in (select p_seqno from cyc_acmm_"+getValue("wday.stmt_cycle")+") "  
              ;

  setString(1 , getValue("wday.this_acct_month"));

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 int insertCycCapitalDtl() throws Exception
 {
  dateTime();

  setValue("cdtl.p_seqno"                   , getValue("p_seqno"));
  setValue("cdtl.acct_month"                , getValue("wday.this_acct_month"));
  setValue("cdtl.stmt_cycle"                , getValue("wday.stmt_cycle")); 
  setValue("cdtl.curr_code"                 , getValue("curr_code")); 
  setValue("cdtl.acct_type"                 , getValue("acct_type")); 
  setValue("cdtl.capital_amt"               , getValue("billed_end_bal_bl")); 
  setValue("cdtl.new_capital_amt"           , getValue("unbill_end_bal_bl")); 
  setValueDouble("cdtl.interest_amt"        , commCurrAmt(getValue("curr_code")
                                           , getValueDouble("billed_end_bal_af")
                                           + getValueDouble("billed_end_bal_ri") 
                                           + getValueDouble("billed_end_bal_pn"),0)); 
  setValueDouble("cdtl.min_pay"             , getValueDouble("min_pay")); 
  setValue("cdtl.mod_time"                 , sysDate+sysTime);
  setValue("cdtl.mod_pgm"                  , javaProgram);

  extendField = "cdtl.";
  daoTable  = "cyc_capital_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertCycBillExt() throws Exception
 {
  dateTime();

  String monthMm = getValue("wday.this_acct_month").substring(4,6);

  showLogMessage("I","","month_mm =["+ monthMm +"]");

  setValue("cbxt.p_seqno"                        , getValue("p_seqno"));
  setValue("cbxt.acct_year"                      , getValue("wday.this_acct_month").substring(0,4));
  setValue("cbxt.curr_code"                      , getValue("curr_code")); 
  setValue("cbxt.acct_type"                      , getValue("acct_type")); 
  setValue("cbxt.stmt_cycle"                     , getValue("stmt_cycle")); 
  setValueInt("cbxt.min_pay_cnt_" + monthMm     , minPayCnt);
  setValueDouble("cbxt.min_pay_amt_" + monthMm  , minPayAmt);
  setValueDouble("cbxt.capital_amt_" + monthMm  , getValueDouble("billed_end_bal_bl"));
  setValueDouble("cbxt.fee_amt_" + monthMm      , getValueDouble("fee_amt"));
  setValueDouble("cbxt.interest_amt_" + monthMm , getValueDouble("interest_amt"));
  setValue("cbxt.mod_time"                       , sysDate+sysTime);
  setValue("cbxt.mod_pgm"                        , javaProgram);

  extendField = "cbxt.";
  daoTable  = "cyc_bill_ext";

  insertTable();

  return(0);
 }
// ************************************************************************
 double commCurrAmt(String currCode, double val, int rnd) throws Exception
 {
  setValue("pcde.curr_code" , currCode);
  int cnt1 = getLoadData("pcde.curr_code");

  val = val * 10000.0;
  val = Math.round(val);

  BigDecimal currAmt = new BigDecimal(val).divide(new BigDecimal("10000"));

  if (cnt1==0) return(currAmt.doubleValue());

  double retNum = 0.0;
  if (rnd>0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_UP).doubleValue(); 
  if (rnd==0) retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_HALF_UP).doubleValue(); 
  if (rnd<0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_DOWN).doubleValue(); 

  return(retNum);
 }
// ************************************************************************
 void loadPtrCurrcode() throws Exception
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

