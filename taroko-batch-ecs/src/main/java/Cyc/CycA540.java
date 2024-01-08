/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/07/31  V1.00.10  Allen Ho   cyc_A610                                   *
* 110/10/27  V1.01.01  Allen Ho   ptr_curr_general set acct_type='01'        *
* 111/10/18  V1.01.02  Yang Bo    sync code from mega                        *
* 112/10/22  V1.01.03  Simon      帳單揭露只繳最低應繳金額剩餘期數及金額     *
*                                 tcb版需扣除當期次數及金額                  *
* 112/11/02  V1.01.04  Simon      原本以通用參數最高利率改以各帳戶之利率計算利息*
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class CycA540 extends AccessDAO
{
 private final String PROGNAME = "關帳-對帳單計算繳納次數計算處理程式 " 
                               + "112/11/02  V1.01.04";
 CommFunction comm = new CommFunction();

 String hBusiBusinessDate = "";

 String hTempBusinessDate = "";
 String hTempThisAcctMonth = "";
 String hTempThisAcctMonth1 = "";
 int hTempInterestDays = 0;
 double hThisMonthDcMp = 0;
 double hInterestRate = 0;
 double minIntRate = 0;

 int monthCnt = 0;
 final boolean DEBUG = false;
//boolean DEBUG =true;

 long    totalCnt=0,updateCnt=0;
 int cnt1;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA540 proc = new CycA540();
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
   showLogMessage("I","","篩選資料處理....");
   totalCnt=0;
   selectCycCapitalDtl();
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
 void selectCycCapitalDtl() throws Exception
 {
  selectSQL = "b.p_seqno,"
            + "b.stmt_cycle,"
            + "b.curr_code,"
            + "b.acct_type,"
            + "b.capital_amt,"
            + "b.new_capital_amt,"
            + "b.interest_amt,"
            + "b.min_pay,"
            + "a.rc_use_indicator,"
            + "a.rc_use_s_date,"
            + "a.rc_use_e_date,"
            + "a.rc_use_b_adj,"
            + "a.mp_flag,"
            + "a.min_pay_rate,"
            + "f.mp_1_amt,"
            + "f.mp_1_s_month,"
            + "f.mp_1_e_month,"
            + "a.special_stat_s_month,"
            + "a.special_stat_e_month,"
            + "a.special_stat_fee,"
            + "a.min_pay_rate_s_month,"
            + "a.min_pay_rate_e_month,"
            + "c.revolving_interest1,"
            + "decode(a.revolve_int_sign,'+',a.revolve_int_rate,"
            + "                              a.revolve_int_rate*-1) as revolve_int_rate,"
            + "a.revolve_rate_s_month,"
            + "decode(a.revolve_rate_e_month,'','999912',a.revolve_rate_e_month) as revolve_rate_e_month,"
            + "c.mp_3_rate,"
            + "d.rc_use_flag,"
            + "e.min_payment,"
            + "e.total_bal";
  daoTable  = "act_acno a,cyc_capital_dtl b,ptr_actgeneral_n c,ptr_acct_type d,ptr_curr_general e,act_acct_curr f";
  whereStr  = "where  b.stmt_cycle     = ?            "
            + "and    b.acct_month     = ?            "
            + "and    c.acct_type      = d.acct_type  "
            + "and    b.acct_type      = c.acct_type  "
            + "and    f.bill_sort_seq != ''          "
            + "and    a.p_seqno        = b.p_seqno    "
            + "and    b.curr_code      = e.curr_code  "
            + "and    b.curr_code      = f.curr_code  "
            + "and    b.p_seqno        = f.p_seqno    "
            + "and    a.p_seqno        = a.acno_p_seqno    "
            + "and    e.acct_type      = '01' "
            ;

  setString(1, getValue("wday.stmt_cycle"));
  setString(2, getValue("wday.this_acct_month"));

  openCursor();

  totalCnt=0;

  while( fetchTable() )
   {
    totalCnt++;

     if (getValue("rc_use_s_date").length()==0) setValue("rc_use_s_date","20000101");
     if (getValue("rc_use_e_date").length()==0) setValue("rc_use_e_date","30001231");

     if (getValue("mp_1_s_month").length()==0) setValue("mp_1_s_month","200001");
     if (getValue("mp_1_e_month").length()==0) setValue("mp_1_e_month","300012");

     if (getValue("mp_flag").equals("1")) 
        {
         setValue("min_pay_rate_s_month",getValue("mp_1_s_month"));
         setValue("min_pay_rate_e_month",getValue("mp_1_e_month"));
        }
     else
        {
         if (getValue("min_pay_rate_s_month").length()==0) setValue("min_pay_rate_s_month","200001");
         if (getValue("min_pay_rate_e_month").length()==0) setValue("min_pay_rate_e_month","300012");
        }

     hInterestRate = getValueDouble("revolving_interest1");

     minIntRate = 0;
     if ((getValue("wday.this_acct_month").compareTo(getValue("revolve_rate_s_month"))>=0)&&
         (getValue("wday.this_acct_month").compareTo(getValue("revolve_rate_e_month"))<= 0))
         minIntRate = getValueDouble("revolve_int_rate");

     hInterestRate = hInterestRate + minIntRate;

     if (DEBUG) showLogMessage("I","","p_seqno["+ getValue("p_seqno") 
                                                + "] curr_code ["
                                                + getValue("curr_code")   
                                                + "] acct_month ["
                                                + getValue("wday.this_acct_month")
                                                +"]");
     if (DEBUG) showLogMessage("I","","  本期["+ getValue("wday.this_acct_month")
                                               + "] 銷帳前 欠款本金["
                                                + String.format("%.0f",getValueDouble("capital_amt"))
                                                +"](含當期["
                                                + String.format("%.0f",getValueDouble("new_capital_amt"))
                                                +"]) 費用及利息["
                                                + String.format("%.0f",getValueDouble("interest_amt"))
                                                +"] 最低應繳金額["
                                                + String.format("%.0f",getValueDouble("min_pay"))
                                                +"]");

     setValue("min_pay_amt" , getValue("min_pay"));
     hThisMonthDcMp = getValueDouble("min_pay");
     if (getValueDouble("interest_amt")>0)
        {
         if (getValueDouble("min_pay") > getValueDouble("interest_amt"))
            {
             setValueDouble("min_pay" , commCurrAmt(getValue("curr_code") ,
                                        getValueDouble("min_pay") 
                                      - getValueDouble("interest_amt"),0));
             setValue("interest_amt"  , "0");
            }
         else
            {
             setValueDouble("interest_amt" , commCurrAmt(getValue("curr_code") , 
                                             getValueDouble("interest_amt") 
                                           - getValueDouble("min_pay"),0)); 
             setValue("min_pay"       , "0");
            }
        }

     if (getValueDouble("capital_amt")>0)
        {
         if (getValueDouble("min_pay") > getValueDouble("capital_amt"))
            {
             setValueDouble("min_pay" , commCurrAmt(getValue("curr_code") ,
                                        getValueDouble("min_pay") 
                                      - getValueDouble("capital_amt"),0));
             setValue("capital_amt"  , "0");
            }
         else
            {
             setValueDouble("capital_amt" , commCurrAmt(getValue("curr_code") , 
                                            getValueDouble("capital_amt") 
                                          - getValueDouble("min_pay"),0)); 
             if (getValueDouble("new_capital_amt")>getValueDouble("capital_amt"))
                setValue("new_capital_amt" , getValue("capital_amt"));

             setValue("min_pay"       , "0");
            }
        }

     int okFlag = 0;
     monthCnt =0;
     int int1a=1;
     while (okFlag==0)
       {
        if (monthCnt >=238)   /* 繳納期數最多20年(240期) */
           {
            okFlag=1;
            break;
           }
        int1a++;

        hTempBusinessDate = comm.nextMonthDate(hBusiBusinessDate, monthCnt);
        hTempThisAcctMonth = comm.nextMonth(getValue("wday.this_acct_month"), monthCnt);
        hTempThisAcctMonth1 = comm.nextMonth(getValue("wday.this_acct_month"), monthCnt +1);
        hTempInterestDays = Integer.valueOf(comm.lastdateOfmonth(hTempThisAcctMonth).substring(6,8));

        setValueDouble("new_interest_amt" , commCurrAmt("901",
                                            (hTempInterestDays 
                                          * getValueDouble("capital_amt")
                                        //* getValueDouble("revolving_interest1"))/10000 ,-1));
                                          * hInterestRate)/10000 ,-1));

        if (getValueDouble("capital_amt") < getValueDouble("total_bal"))
           setValue("new_interest_amt" , "0");

        if (DEBUG)
           {
           if (monthCnt ==0)
              {
               showLogMessage("I","","[010] 本期["+ hTempThisAcctMonth1
                                    + "]["
                                    + String.format("%02d", monthCnt +1)
                                    +"] 關帳前 欠款本金["
                                    + String.format("%.0f",getValueDouble("capital_amt"))
                                    +"](含當期["
                                    + String.format("%.0f",getValueDouble("new_capital_amt"))
                                    +"]) 費用及利息["
                                    + String.format("%.0f",getValueDouble("interest_amt"))
                                    +"]");

               setValue("new_capital_amt" , "0");
              }
           else
              {
               showLogMessage("I","","[011] 本期["+ hTempThisAcctMonth1
                                    + "]["
                                    + String.format("%02d", monthCnt +1)
                                    +"] 關帳前 欠款本金["
                                    + String.format("%.0f",getValueDouble("capital_amt"))
                                    +"]) 費用及利息["
                                    + String.format("%.0f",getValueDouble("interest_amt"))
                                    +"]");
              }
           if (getValueDouble("capital_amt") < getValueDouble("total_bal"))
              {
               showLogMessage("I","","[012]              本金("
                                    + String.format("%.0f",getValueDouble("capital_amt"))
                                    +")小於("
                                    + String.format("%.0f",getValueDouble("total_bal"))
                                    +") 免計息");
              }
           else
              {
               showLogMessage("I","","[013]              新增利息 : ("
                                    + String.format("%.0f",getValueDouble("capital_amt"))
                                    +")*("
                                    + String.format("%d", hTempInterestDays)
                                    +")*("
                                  //+ String.format("%.3f",getValueDouble("revolving_interest1"))
                                    + String.format("%.3f",hInterestRate)
                                    +")/10000 = ["
                                    + String.format("%.3f",getValueDouble("new_interest_amt"))
                                    +"]");
              }
           }

        if (getValueDouble("min_payment")>= commCurrAmt(getValue("curr_code"),
                                            getValueDouble("new_interest_amt")
                                          + getValueDouble("capital_amt")
                                          + getValueDouble("interest_amt"),0))
           {
            setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"),  
                                           getValueDouble("new_interest_amt")
                                         + getValueDouble("capital_amt")
                                         + getValueDouble("interest_amt"),0));

            setValueDouble("min_pay_amt" , commCurrAmt(getValue("curr_code"), 
                                           getValueDouble("min_pay_amt") 
                                         + getValueDouble("new_min_pay"),0));
            okFlag=1;
            break;
           }

        procMinpay();

        if (getValueDouble("new_min_pay") <= getValueDouble("min_payment"))
            setValue("new_min_pay" , getValue("min_payment"));

        if (getValueDouble("new_min_pay") > commCurrAmt(getValue("curr_code"), 
                                            getValueDouble("new_interest_amt")+
                                          + getValueDouble("capital_amt")
                                          + getValueDouble("interest_amt"),0))
           setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"),
                                          getValueDouble("new_interest_amt")+
                                        + getValueDouble("capital_amt")
                                        + getValueDouble("interest_amt"),0));

        setValueDouble("min_pay_amt" , commCurrAmt(getValue("curr_code"),
                                       getValueDouble("min_pay_amt") 
                                     + getValueDouble("new_min_pay"),0))
                                     ;
        if (DEBUG)
        showLogMessage("I","","[014]              最低應繳 : ["
                             + String.format("%.2f",getValueDouble("new_min_pay"))
                             +"] 累計["
                             + String.format("%.2f",getValueDouble("min_pay_amt"))
                             +"]");

        if (getValueDouble("new_min_pay") >= commCurrAmt(getValue("curr_code"), 
                                             getValueDouble("new_interest_amt")
                                           + getValueDouble("capital_amt")
                                           + getValueDouble("interest_amt"),0))
           {
            okFlag=1;
            break;
           }

        monthCnt++;
        if (getValueDouble("new_interest_amt")>0)
           {
            if (getValueDouble("new_min_pay") > getValueDouble("new_interest_amt"))
               {
                setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"),  
                                               getValueDouble("new_min_pay")
                                             - getValueDouble("new_interest_amt"),0));
                setValue("new_interest_amt"  , "0");
               }
            else
               {
                setValueDouble("new_interest_amt" , commCurrAmt(getValue("curr_code"),  
                                                    getValueDouble("new_interest_amt")
                                                  - getValueDouble("new_min_pay"),0));
                setValue("new_min_pay"  , "0");
               }
           }

        if (getValueDouble("interest_amt")>0)
           {
            if (getValueDouble("new_min_pay") > getValueDouble("interest_amt"))
               {
                setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"),  
                                               getValueDouble("new_min_pay")
                                             - getValueDouble("interest_amt"),0));
                setValue("interest_amt"  , "0");
               }
            else
               {
                setValueDouble("interest_amt" , commCurrAmt(getValue("curr_code"),  
                                                getValueDouble("interest_amt")
                                              - getValueDouble("new_min_pay"),0));
                setValue("new_min_pay"  , "0");
               }
           }

        setValueDouble("interest_amt" , commCurrAmt(getValue("curr_code"),  
                                        getValueDouble("interest_amt")
                                      + getValueDouble("new_interest_amt"),0));

        if (getValueDouble("capital_amt")>0)
           {
            if (getValueDouble("new_min_pay") > getValueDouble("capital_amt"))
               {
                setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"),  
                                               getValueDouble("new_min_pay")
                                             - getValueDouble("capital_amt"),0));
                setValue("capital_amt"  , "0");
               }
            else
               {
                setValueDouble("capital_amt" , commCurrAmt(getValue("curr_code"),  
                                               getValueDouble("capital_amt")
                                             - getValueDouble("new_min_pay"),0));
                setValue("new_min_pay"  , "0");
               }
           }
        if (getValueDouble("new_min_pay")>0)
           {
            showLogMessage("I","","[END] P_seqno["
                                + getValue("p_seqno")
                                + "] curr_code["
                                + getValue("curr_code")
                                + " Minpay error compute!");
            exitProgram(1);
           }
       }
     updateCycBillExt();
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
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";
  daoTable  = "cyc_bill_ext";
  whereStr  = "WHERE  p_seqno   = ? "
            + "AND    acct_year = ? "
            + "AND    curr_code = ? ";

//帳單揭露只繳最低應繳金額剩餘期數及金額，tcb版需扣除當期次數及金額 
//setInt(1    , (monthCnt +2));
  setInt(1    , (monthCnt +1));
  setDouble(2 , getValueDouble("min_pay_amt") - hThisMonthDcMp);
  setString(3 , javaProgram);
  setString(4 , getValue("p_seqno"));
  setString(5 , getValue("wday.this_acct_month").substring(0,4));
  setString(6 , getValue("curr_code"));

  updateTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
void procMinpay() throws Exception 
 {
   // *********** 特殊固定 MP *start***************
    if ((getValue("mp_flag").equals("1"))&&
        (getValue("mp_1_s_month").compareTo(hTempThisAcctMonth1)<=0)&&
        (getValue("mp_1_e_month").compareTo(hTempThisAcctMonth1)>=0))   
       {
        setValue("new_min_pay" , getValue("mp_1_amt"));
        if (DEBUG)
        showLogMessage("I","","[016]              特殊固定 MP ["
                             + getValueDouble("mp_1_amt")
                             + "]");
        return;
       }
   /************* 特殊固定 MP *end*****************/

   /************* 特殊MP百分比*start*************/
   if ((!getValue("mp_flag").equals("1"))&&
       (getValueDouble("min_pay_rate")>0)&&
        (getValue("min_pay_rate_s_month").compareTo(hTempThisAcctMonth1)<=0)&&
        (getValue("min_pay_rate_e_month").compareTo(hTempThisAcctMonth1)>=0))
      {
       if (getValue("curr_cod").equals("901"))
         {
          setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"), 
                                         getValueDouble("capital_amt") 
                                       * getValueDouble("min_pay_rate")/100.0 , 0));
         }
       else
         {
          setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"), 
                                         getValueDouble("capital_amt") 
                                       * getValueDouble("min_pay_rate")/100.0,0));
         }
        
       setValue("temp_new_min_pay" , getValue("new_min_pay"));
       if (getValueDouble("new_min_pay") <= getValueDouble("min_payment"))
          setValue("new_min_pay" , getValue("min_payment"));
       if (getValueDouble("new_min_pay")>getValueDouble("capital_amt"))
          setValue("new_min_pay" , getValue("capital_amt"));

       if (DEBUG)
       showLogMessage("I","","                     特殊MP百分比 : ("
                            + String.format("%.0f",getValueDouble("capital_amt"))
                            + ")*("
                            + String.format("%d",getValueInt("min_pay_rate"))
                            + ")/100 = ["
                            + String.format("%.0f",getValueInt("temp_new_min_pay"))
                            + "] 最低應繳["
                            + String.format("%f",getValueDouble("new_min_pay"))
                            + "]");
      }
   /************* 特殊MP百分比*end***************/
   else
      {
    /************ 不允用 RC *start****************/
       if (((getValue("rc_use_indicator").equals("2"))&&
            ((hTempBusinessDate.compareTo(getValue("rc_use_s_date"))<0)||
             (hTempBusinessDate.compareTo(getValue("rc_use_e_date"))>0)))||
           ((getValue("rc_use_indicator").equals("3"))&&
            ((hTempBusinessDate.compareTo(getValue("rc_use_s_date"))<0)||
             (hTempBusinessDate.compareTo(getValue("rc_use_e_date"))>0))))
           setValue("rc_use_indicator" , getValue("rc_use_b_adj"));

       if (!getValue("rc_use_indicator").equals("2"))
          if (getValue("rc_use_flag").equals("3")) setValue("rc_use_indicator" , "3"); 

       if (getValue("rc_use_indicator").equals("3"))
          {
           setValue("new_min_pay" ,  getValue("capital_amt"));
           if (DEBUG) showLogMessage("I","","[017]              不允用 RC, 全額繳清");

          }
     /************ 不允用 RC *end******************/
       else
          {
           setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"), 
                                          getValueDouble("capital_amt")
                                        * getValueDouble("mp_3_rate")/100.0,0));

           setValue("temp_new_min_pay" , getValue("new_min_pay"));

           if (getValueDouble("new_min_pay") <= getValueDouble("min_payment"))
               setValueDouble("new_min_pay" , getValueDouble("min_payment"));
           if (getValueDouble("new_min_pay") > getValueDouble("capital_amt"))
              setValue("new_min_pay" , getValue("capital_amt"));
           if (DEBUG)
           showLogMessage("I","","[015]              本金類MP: ("
                                + String.format("%.0f",getValueDouble("capital_amt"))
                                + ")*("
                                + String.format("%d",getValueInt("mp_3_rate"))
                                + ")/100 = ["
                                + String.format("%.0f",getValueDouble("temp_new_min_pay"))
                                + "] 最低應繳["
                                + String.format("%f",getValueDouble("new_min_pay"))
                                + "]");
          }
      }

  if (getValue("curr_code").equals("901"))
     {
      if ((getValueDouble("special_stat_fee")!=0)&&
          (getValue("special_stat_s_month").compareTo(hTempThisAcctMonth1)<=0)&&
          (getValue("special_stat_e_month").compareTo(hTempThisAcctMonth1)>=0))
          {
           setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"),  
                                          getValueDouble("new_min_pay") 
                                        + getValueDouble("special_stat_fee"),0));

           if (DEBUG)
           showLogMessage("I","","[018]              特殊對帳單費用 : ["
                                + String.format("%.0f",getValueDouble("special_stat_fee"))
                                + "]");
          }

      if (getValueDouble("interest_amt") >0)
         {
          setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"), 
                                         getValueDouble("new_min_pay") 
                                       + getValueDouble("interest_amt"),0));
           if (DEBUG)
           showLogMessage("I","","[019]              前期費用和利息 : ["
                                + String.format("%.0f",getValueDouble("new_min_pay"))
                                + "] = +["
                                + String.format("%.0f",getValueDouble("ninterest_amt"))
                                + "]");
         }
     }

  if (getValueDouble("new_interest_amt") >0)
      setValueDouble("new_min_pay" , commCurrAmt(getValue("curr_code"), 
                                     getValueDouble("new_min_pay") 
                                   + getValueDouble("new_interest_amt"),0));
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

