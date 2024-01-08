/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/04/13  V1.00.23  Allen Ho   cyc_C181                                   *
* 111/02/08  V1.01.01  Allen Ho   mantis 9156                                *
* 111/03/01  V1.02.01  Allen Ho   mantis 9288                                *
* 111-11-11  V1.00.01    Machao   sync from mega & updated for project coding standard                                                                           *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA340 extends AccessDAO
{
 private final String PROGNAME = "台幣基金-消費門檻轉基金處理程式 111-11-11  V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;

 String businessDate   = "";
 String tranSeqno = "";
 String fundCode="";
 String pSeqno = "";

 int    cycleFlag = 1;
 String feedbackType   = "2";  //2 cycle  1: Month Fixed Date
 double tempDestAmt = 0;
 long    totalCnt=0;
 int parmCnt=0;;
 int[] dInt = {0,0,0,0};
 boolean DEBUG = false;
 boolean testOld = false;
 int runCnt=0;

 int paymentAmt = 0,insertCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA340 proc = new CycA340();
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

   if ( (args.length > 5) || (args.length< 1) )
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [feedbackType  1.每月 2.帳單週期]");
       showLogMessage("I","","PARM 2 : [business_date]");
       showLogMessage("I","","PARM 3 : [fund_code/TEST]");
       showLogMessage("I","","PARM 4 : [p_seqno/TEST]");
       showLogMessage("I","","PARM 5 : [TEST]");
       return(1);
      }
   
   if (args.length >= 1 )
	  feedbackType  = args[0];
   if (args.length >= 2 )
      businessDate = args[1];
   if (args.length >= 3 )
      {
       if (args[2].equals("TEST"))
          testOld = true;
       else
          fundCode = args[2];
      }
   if (args.length >= 4 )
      {
       if (args[3].length()==10)
          pSeqno = args[3];
       else if (args[3].equals("TEST"))
          testOld = true;
      }
   if (args.length >= 5 )
      {
       if (args[4].equals("TEST"))
          testOld = true;
      }
   
   if (testOld)
       showLogMessage("I","","目前在 TEST 模式");
   
   if ((!feedbackType.equals("1"))&&
	       (!feedbackType.equals("2")))
   {
	       showLogMessage("I","","回饋方式 : 1.每月 2.帳單週期 ");
	       return(1);
   }

   if ( !connectDataBase() ) 
       return(1);
   comC = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()==0) cycleFlag=0;
   
// String feedbackType   = "2";  //2 cycle  1: Month Fixed Date
// int    cycleFlag = 0;   // 0 : cycle  1: Month Fixed Date  
   
   if ((feedbackType.equals("2")) && !(cycleFlag == 0) )   {
	       showLogMessage("I","","回饋方式 : 2.帳單週期 ,本日非關帳日,不需執行");
	       showLogMessage("I","","本日非關帳日, 不需執行");
	       return(0);
   }
   if ((feedbackType.equals("1")) &&  (cycleFlag == 0) )   {
	       showLogMessage("I","","回饋方式 : 1.每月指定日 ,本日是關帳日,不需執行 ");
	       return(0);
   }   
   
   if (selectPtrFundp()==0)
      {
       showLogMessage("I","","本日無符合參數, 不需執行");
       return(0);
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","cycle_flag ["+cycleFlag+"]");
   showLogMessage("I","","載入暫存資料");
   loadCrdCard();
   loadPtrFundData();

   if (testOld)
      loadActAcctHst();
   else
      loadActAcct();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","刪除逾期明細資料(cyc_cal_fund)...");
   deleteCycPurchFund();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理參數資料...");
   selectCycPurchFund();
   showLogMessage("I","","=========================================");

   if (pSeqno.length()==0) finalProcess();
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
  int  selectPtrWorkday() throws Exception
 {
  extendField = "work.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "";

  int recCnt = selectTable();

  String maxDate="";
  String maxCycle="";
  for (int inti=0;inti<recCnt;inti++)
    {
     if (getValue("work.this_close_date",inti).compareTo(maxDate)>0)
        {
         maxDate = getValue("work.this_close_date",inti);
         maxCycle = getValue("work.stmt_cycle",inti);
        
         if (getValue("work.this_close_date",inti).equals(businessDate))
            {
             setValue("wday.stmt_cycle"      ,  getValue("work.stmt_cycle",inti));
             setValue("wday.this_acct_month" ,  getValue("work.this_acct_month",inti));
             setValue("wday.last_acct_month" ,  getValue("work.last_acct_month",inti));
             setValue("wday.ll_acct_month"   ,  getValue("work.ll_acct_month",inti));
             return(0);
            }
        }
    }
  setValue("wday.stmt_cycle" ,  maxCycle);

  return(1);
 }
// ************************************************************************
 void  selectCycPurchFund() throws Exception
 {
  selectSQL = "b.p_seqno,"
            + "b.fund_code,"
            + "b.proc_date,"
            + "min(b.acct_type) as acct_type,"
            + "sum(decode(sign(b.dest_amt),1,1,0)) as dest_cnt,"
            + "sum(b.dest_amt) as dest_amt,"
            + "min(a.autopay_acct_bank) as autopay_acct_bank,"
            + "min(a.autopay_acct_s_date) as autopay_acct_s_date,"
            + "min(a.autopay_acct_e_date) as autopay_acct_e_date,"
            + "min(a.stmt_cycle) as stmt_cycle,"
            + "min(b.major_card_no) as major_card_no,"
            + "min(a.id_p_seqno) as id_p_seqno";
  daoTable  = "act_acno a,cyc_purch_fund b";
  whereStr  = "where  a.p_seqno      = b.p_seqno "
            + "and    b.proc_mark    = 'N' "   
            + "and    b.proc_date    = ? "           
            ;

  setString(1 , businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   b.fund_code = ? ";
      setString(2,fundCode);

       if (pSeqno.length()!=0)
          {
           whereStr  = whereStr 
                     + "and   b.p_seqno = ? ";
           setString(3, pSeqno);
          }
     }
  whereStr  = whereStr 
            + "group  by b.p_seqno,fund_code,proc_Date ";

  openCursor();

  totalCnt=0;
  double dTempTotAmt = 0;
  double tempDAmt     = 0;
  int    tempLAmt     = 0;
  int    loadCnt1       = 0; 
  double dTempDAmt   = 0;
  String resSDate = "";

  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    for (int inti=0;inti<parmCnt;inti++)
      {
    parmArr[inti][0]++;
       if (!getValue("fund_code").equals(getValue("parm.fund_code",inti))) continue;

       //if (!getValue("parm.tran_base",inti).equals("2"))
       if (!getValue("parm.tran_base",inti).equals("B"))   
          { 
           setValue("parm.group_code_sel","0",inti);
           setValueDouble("parm.rc_sub_rate",0.0,inti);
          }
          
       if (getValue("purch_tol_amt_cond").equals("Y"))   setValueInt("purch_tol_amt" ,0); 
       if (getValue("purch_tol_time_cond").equals("Y"))  setValueInt("purch_tol_time" ,0); 

       if (getValue("parm.feedback_type",inti).equals("1"))
          {
    parmArr[inti][1]++;
           resSDate = resSMonth(getValue("parm.unlimit_start_month",inti) +
                                    String.format("%02d",getValueInt("parm.card_feed_run_day",inti)),
                                    businessDate,getValueInt("parm.feedback_months",inti));

           if (!getValue("proc_date").equals(resSDate)) continue;

          }
       else if (getValue("parm.feedback_type",inti).equals("2"))
          {
    parmArr[inti][2]++;
           if (cycleFlag!=0) continue;

    parmArr[inti][3]++;
           if (getValue("parm.mp_flag",inti).equals("Y"))
              {
               setValue("acct.p_seqno",getValue("p_seqno"));
               loadCnt1 = getLoadData("acct.p_seqno");

    parmArr[inti][4]++;
               if (loadCnt1 > 0)
                  {
                   updateCycPurchFund(3);
                   continue;
                  }
               }
          }

       if (getValue("parm.ebill_flag",inti).equals("Y"))
          {
           int retInt=0;
           if (testOld)
               retInt = selectActAcno();
           else
               retInt = selectCycAcmm();

            if (retInt !=0)
              {
               updateCycPurchFund(8);
               continue;
              }
          }
    parmArr[inti][5]++;

       if (getValue("parm.purch_tol_time_cond",inti).equals("Y"))
       if (getValueInt("parm.purch_tol_time",inti)>getValueInt("dest_cnt"))
          {
           updateCycPurchFund(6);
           continue;
          }

    parmArr[inti][6]++;
       if (getValue("parm.purch_tol_amt_cond",inti).equals("Y"))
          {
           if (getValueDouble("dest_amt")>0)
           if (getValueInt("parm.purch_tol_amt",inti)>getValueInt("dest_amt"))
              {
               updateCycPurchFund(9);
               continue;
              }

    parmArr[inti][7]++;
           if (getValueDouble("dest_amt")<0)
           if (getValueInt("parm.purch_tol_amt",inti)>getValueInt("dest_amt")*-1)
              {
               updateCycPurchFund(9);
               continue;
              }
          }

    parmArr[inti][8]++;
       if (getValueDouble("dest_amt")>0)
       if ((getValue("parm.autopay_flag",inti).equals("Y"))&&
           //((!getValue("autopay_acct_bank").equals("07"))||
    		 ((!getValue("autopay_acct_bank").equals("006"))||
            (businessDate.compareTo(getValue("autopay_acct_s_date"))<0)||
            (businessDate.compareTo(getValue("autopay_acct_e_date"))>0)))
          {
           updateCycPurchFund(1);
           continue;
          }

    parmArr[inti][9]++;
       setValue("data_key" , getValue("parm.fund_code",inti));

       int okFlag=1;
       if ((!getValue("parm.group_code_sel",inti).equals("0"))&&
           (getValue("parm.valid_afi_flag",inti).equals("Y")))
          {
           setValue("card.p_seqno",getValue("p_seqno"));
           loadCnt1 = getLoadData("card.p_seqno");

           okFlag=1;
           for (int intk=0;intk<loadCnt1;intk++)
             {
              if (selectPtrFundData(getValue("card.group_code",intk),
                                       getValue("parm.group_code_sel",inti),"3",3)!=0) continue;
              okFlag = 0;
             }
           if (okFlag!=0)
             {
              updateCycPurchFund(4);
              continue;
             }
          }

    parmArr[inti][10]++;
       if ((!getValue("parm.acct_type_sel",inti).equals("0"))&&
           (getValue("parm.valid_card_flag",inti).equals("Y")))
          {
           setValue("card.p_seqno",getValue("p_seqno"));
           loadCnt1 = getLoadData("card.p_seqno");

           okFlag=1;
           for (int intk=0;intk<loadCnt1;intk++)
             {
              if (selectPtrFundData(getValue("card.acct_type",intk),
                                       getValue("parm.acct_type_sel",inti),"4",3)!=0) continue;
              okFlag = 0;
             }
           if (okFlag!=0)
             {
              updateCycPurchFund(5);
              continue;
             }
          }

    parmArr[inti][11]++;
       dTempTotAmt = 0;
       int nTempTotAmt = 0;
       int nTempLmt     = 0;

       if (getValueDouble("dest_amt")>0)
       if (getValueInt("parm.purch_feed_times",inti)>0)
          {
           selectCycFundDtl(inti);
           nTempLmt = getValueInt("parm.purch_feed_times",inti) - getValueInt("fund_cnt");
           if (nTempLmt <= 0)
              {
               updateCycPurchFund(7);
               continue;
              }
          }

    parmArr[inti][12]++;
       tempDAmt = getValueDouble("dest_amt");
       if (tempDAmt<0) tempDAmt = tempDAmt*-1;

       //if (getValue("parm.purch_feed_type",inti).equals("1"))
       if (getValue("parm.purch_type",inti).equals("1"))    	   
          {
           if (getValueDouble("dest_amt")>0)
           if (nTempLmt != 0)
           if (getValueInt("dest_cnt")>nTempLmt)  setValueInt("dest_cnt" , nTempLmt);
  
           //nTempTotAmt = (int)Math.round(getValueInt("parm.purch_feed_amt",inti) * tempDAmt);
           nTempTotAmt = (int)Math.round(getValueInt("parm.purch_feed_amt",inti));
           
          }

       //if (getValue("parm.purch_feed_type",inti).equals("2"))
       if (getValue("parm.purch_type",inti).equals("2"))    
          {
           tempLAmt = (int)Math.round(tempDAmt*getValueDouble("parm.purch_feed_rate",inti)/100.0);
           nTempTotAmt =getValueInt("parm.purch_feed_amt",inti) + tempLAmt;
           setValueInt("dest_cnt" , 1); 
          }

       if (nTempTotAmt ==0)
          {
           updateCycPurchFund(2);
           continue;
          }

    parmArr[inti][13]++;
       if ((nTempTotAmt > getValueInt("parm.feedback_lmt",inti))&&(getValueInt("parm.feedback_lmt",inti) > 0))
          nTempTotAmt = getValueInt("parm.feedback_lmt",inti);

       setValueInt("beg_tran_amt" , nTempTotAmt);

       if (!getValue("parm.valid_period",inti).equals("Y"))
          insertCycCobrandFund(inti);
       else
          {
//           if (getValueDouble("dest_amt")<0)
//              {
//               setValueInt("beg_tran_amt" , nTempTotAmt*-1);
//               insertCycFundDtl(2,inti);
//              }
//           else insertCycFundDtl(1,inti);

           insertMktCashbackDtl(inti);
          }

    parmArr[inti][14]++;
       updateCycPurchFund(0);
      }
   } 
  closeCursor();
  if (fundCode.length()!=0)
     {
      showLogMessage("I","","=========================================");
      for (int inti=0;inti<parmCnt;inti++)
        {
         showLogMessage("I","","["+inti+"] fund_code  :["+ getValue("parm.fund_code",inti) +"]");
         for (int intk=0;intk<20;intk++)
          {
           if (parmArr[inti][intk]==0) continue;
           showLogMessage("I",""," 測試絆腳石 : ["+intk+"] = ["+parmArr[inti][intk]+"]");
          }
        }    
     showLogMessage("I","","=========================================");
    }
  return;
 }
// ************************************************************************
int selectPtrFundp() throws Exception
 {
  extendField = "parm.";
  selectSQL = "";
  daoTable  = "ptr_fundp";
  whereStr  = "WHERE apr_flag      = 'Y' "
            + "AND   (stop_flag  = 'N' "
            + " OR    (stop_flag = 'Y' "
            + "  and   ? < stop_date)) "
            + "and   feedback_type in ('1','2') "
            + "and   purch_feed_flag = 'Y' ";

  setString(1,businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2,fundCode);
     }

  parmCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  for (int inti=0;inti<parmCnt;inti++)
      {
       if (getValue("parm.feedback_type",inti).equals("1"))
          {
           if (!businessDate.substring(6,8).equals
               (String.format("%02d",getValueInt("parm.card_feed_run_day",inti)))) continue;
          }
       else
          {
           if (cycleFlag!=0) continue;
          }

       if (getValue("parm.fund_crt_date_s",inti).length()==0)
          setValue("parm.fund_crt_date_s","20000101",inti);

       if (getValue("parm.fund_crt_date_e",inti).length()==0)
          setValue("parm.fund_crt_date_e","30001231",inti);

       if ((businessDate.compareTo(getValue("parm.fund_crt_date_s",inti))<0)|| 
           (businessDate.compareTo(getValue("parm.fund_crt_date_e",inti))>0))  continue;

       runCnt++;
      }


  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt + "] 本日執行筆數[" + runCnt + "]");

  return(runCnt);
 }
// ************************************************************************
 int insertMktCashbackDtl(int inti) throws Exception
 {
  tranSeqno     = comr.getSeqno("mkt_modseq");
  dateTime();

  setValue("tran_date"            , sysDate);
  setValue("tran_time"            , sysTime);
  setValue("fund_code"            , getValue("parm.fund_code",inti));
  setValue("fund_name"            , getValue("parm.fund_name",inti));
  setValue("p_seqno"              , getValue("p_seqno"));
  setValue("acct_type"            , getValue("acct_type"));
  setValue("id_p_seqno"           , getValue("id_p_seqno"));
  if (getValueInt("beg_tran_amt")>0)
     {
      setValue("tran_code"            , "2");
      setValue("mod_desc"             , "消費門檻回饋基金");
     }
  else
     {
      setValue("tran_code"            , "2");
      setValue("mod_desc"             , "消費門檻回饋基金系統調整");
     }
  setValue("tran_pgm"             , javaProgram);
  setValueInt("beg_tran_amt"      , getValueInt("beg_tran_amt"));
  setValueInt("end_tran_amt"      , getValueInt("beg_tran_amt"));
  setValueInt("res_tran_amt"      , 0);
  setValueInt("res_total_cnt"     , 0);
  setValueInt("res_tran_cnt"      , 0);
  setValue("res_s_month"          , "");
  setValue("res_upd_date"         , "");
  setValue("effect_e_date"        , "");
  if (getValue("parm.effect_type",inti).length()==0)
     {
      if (getValueInt("parm.effect_months",inti)>0)
         setValue("parm.effect_type","1" ,inti);
      else
         setValue("parm.effect_type","0" ,inti);
     }
  if (getValue("parm.effect_type",inti).equals("1"))
     setValue("effect_e_date"    , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));
  else if (getValue("parm.effect_type",inti).equals("2"))
    {
     setValue("effect_e_date"    , comm.nextMonth( businessDate , getValueInt("parm.effect_years",inti)*12).substring(0,4)
                                 + String.format("%02d" , getValueInt("parm.effect_fix_month",inti))
                                 + businessDate.substring(6,8));
    }
  setValue("tran_seqno"           , tranSeqno);
  setValue("proc_month"           , businessDate.substring(0,6));
  setValue("acct_date"            , businessDate);
  setValue("mod_memo"             , "");
  setValue("mod_reason"           , "");
  setValue("case_list_flag"       , "N");
  setValue("crt_user"             , javaProgram);
  setValue("crt_date"             , sysDate);
  setValue("apr_date"             , sysDate);
  setValue("apr_user"             , javaProgram);
  setValue("apr_flag"             , "Y");
  setValue("mod_user"             , javaProgram); 
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);
  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteCycPurchFund() throws Exception
 {
  daoTable  = "cyc_purch_fund";
  whereStr  = "WHERE ((proc_mark = 'Y' "
            + "  and   proc_date < ?)  "
            + " or    (proc_mark != 'Y' "
            + "  and   proc_date < ?)) ";

  setString(1, comm.nextMonthDate(businessDate,-12));
  setString(2, comm.nextMonthDate(businessDate,-6));

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(3,fundCode);

       if (pSeqno.length()!=0)
          {
           whereStr  = whereStr 
                     + "and   p_seqno = ? ";
           setString(4, pSeqno);
          }
     }

  int n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete cyc_purch_fund [" + n + "] records");

  return;
 }
// ************************************************************************
 void updateCycPurchFund(int hIntb) throws Exception
 {
  dateTime();
  String procMark = "Y";
  if (hIntb<=8) procMark = String.format("%d" , hIntb); 

  updateSQL = "proc_mark  = ?,"
            + "stmt_cycle = ?,"
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";  
  daoTable  = "cyc_purch_fund";
  whereStr  = "WHERE proc_date    = ? "
            + "and   fund_code    = ? "
            + "and   p_seqno      = ? "
            + "and   proc_mark    = 'N' "
            ;

  setString(1 , procMark);
  setString(2 , getValue("stmt_cycle"));
  setString(3 , javaProgram);
  setString(4 , getValue("proc_date"));
  setString(5 , getValue("fund_code"));
  setString(6 , getValue("p_seqno"));

  updateTable();
  return;
 }
// ************************************************************************
// int insertCycFundDtl(int numType,int inti) throws Exception
// {
//  dateTime();
//  extendField = "fdtl.";
//
//  setValue("fdtl.business_date"        , businessDate);
//  setValue("fdtl.curr_code"            , "901");
//  setValue("fdtl.create_date"          , sysDate);
//  setValue("fdtl.create_time"          , sysTime);
//  setValue("fdtl.id_p_seqno"           , getValue("id_p_seqno"));
//  setValue("fdtl.p_seqno"              , getValue("p_seqno"));
//  setValue("fdtl.acct_type"            , getValue("acct_type"));
//  setValue("fdtl.card_no"              , "");
//  setValue("fdtl.fund_code"            , getValue("parm.fund_code",inti).substring(0,4));
//  setValue("fdtl.vouch_type"           , "3"); // '1':single record,'2':fund_code+id '3':fund_code */
//  if (numType==1)
//     {
//      setValue("fdtl.tran_code"            , "1");
//      setValue("fdtl.cd_kind"              , "H001");    // 新增New add
//     }
//  else
//     {
//      setValue("fdtl.tran_code"            , "7");
//      setValue("fdtl.cd_kind"              , "H003");  // 0-移轉 1-新增 2-贈與 3-調整 4-使用 5-匯入 6-移除 7-扣回
//     }
//  setValue("fdtl.memo1_type"           , "1");   /* fund_code 必須有值 */
//  setValueInt("fdtl.fund_amt"          , Math.abs(getValueInt("beg_tran_amt")));
//  setValueInt("fdtl.other_amt"         , 0);
//  setValue("fdtl.proc_flag"            , "N");
//  setValue("fdtl.proc_date"            , "");
//  setValue("fdtl.execute_date"         , businessDate);
//  setValue("fdtl.fund_cnt"             , getValue("fund_cnt"));
//  setValue("fdtl.mod_user"             , javaProgram); 
//  setValue("fdtl.mod_time"             , sysDate+sysTime);
//  setValue("fdtl.mod_pgm"              , javaProgram);
//
//  daoTable  = "cyc_fund_dtl";
//
//  insertTable();
//
//  insertCnt++;
//  return(0);
// }
// ************************************************************************
 String resSMonth(String yearSDate,String nowDate,int calMonths) throws Exception
 {
  String okDate="";
  for (int inti=0;inti<6000;inti++)
    {
     okDate = comm.nextMonthDate(yearSDate,inti*calMonths);
     if (okDate.compareTo(nowDate)>=0) break;
    }
  return okDate;
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.p_seqno as p_seqno,"
            + "max(a.acct_type) as acct_type,"
            + "decode(a.group_code,'','0000',a.group_code) as group_code";
  daoTable  = "crd_card a,(select distinct p_seqno from cyc_purch_fund "
            + "            where proc_mark = 'N' ) b ";
  whereStr  = "where a.current_code = '0' "
            + "and   a.p_seqno        = b.p_seqno "
            ;

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   b.p_seqno = ? ";
      setString(1, pSeqno);
     }
  whereStr  = whereStr 
            + "group by a.p_seqno,decode(a.group_code,'','0000',a.group_code) "
            + "order by a.p_seqno,decode(a.group_code,'','0000',a.group_code) ";

  int  n = loadTable();
  setLoadData("card.p_seqno");

  showLogMessage("I","","CARD Load Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActAcct() throws Exception
 {
  extendField = "acct.";
  selectSQL = "p_seqno,"
            + "min_pay_bal";
  daoTable  = "act_acct_"+getValue("wday.stmt_cycle");
  whereStr  = "where min_pay_bal > 0 "
            + "and   p_seqno in (select p_seqno "
            + "                  from   cyc_purch_fund "
            + "                  where proc_date = ?  "
            + "                  group by p_seqno ) "
            ;

  setString(1 , businessDate);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   p_seqno = ? ";
      setString(2, pSeqno);
     }

  int  n = loadTable();
  setLoadData("acct.p_seqno");

  showLogMessage("I","","ACCT Load Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcctHst() throws Exception
 {
  extendField = "acct.";
  selectSQL = "p_seqno,"
            + "min_pay_bal";
  daoTable  = "act_acct_hst";
  whereStr  = "where min_pay_bal > 0 "
            + "and   p_seqno in (select p_seqno "
            + "                  from   cyc_purch_fund "
            + "                  where proc_date = ?  "
            + "                  group by p_seqno ) "
            + "and   acct_month = ? " 
            ;

  setString(1 , businessDate);
  setString(2 , getValue("wday.last_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   p_seqno = ? ";
      setString(3, pSeqno);
     }

  int  n = loadTable();
  setLoadData("acct.p_seqno");

  showLogMessage("I","","Load act_acct_hst Count: ["+n+"]");
 }
// ************************************************************************
 int  selectCycFundDtl(int inti) throws Exception
 {
  selectSQL = "sum(fund_cnt) as fund_cnt";
  daoTable  = "cyc_fund_dtl";
  whereStr  = "where p_seqno   = ? "
            + "and   fund_code = ? "
            + "and   tran_code = '1' ";

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("parm.fund_code",inti));

  selectTable();

  if ( notFound.equals("Y") ) setValueDouble("dc_min_pay_bal",0.0);

  return(0);
 }
// ************************************************************************
 int selectPtrFundData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectPtrFundData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectPtrFundData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectPtrFundData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectPtrFundData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",dataType);

  int cnt1=0;
  if (dataNum==2)
     {
      cnt1 = getLoadData("data.data_key,data.data_type");
     }
  else
     {
      setValue("data.data_code",col1);
      cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
     }

  int okFlag=0;
  for (int intm=0;intm<cnt1;intm++)
    {
     if (dataNum==2)
        {
         if (getValue("data.data_code",intm).length()!=0)
            {
             if (col1.length()!=0)
                {
                 if (!getValue("data.data_code",intm).equals(col1)) continue;
                }
              else
                {
                 if (sel.equals("1")) continue;
                }
            }
        }
     if (getValue("data.data_code2",intm).length()!=0)
        {
         if (col2.length()!=0)
            {
             if (!getValue("data.data_code2",intm).equals(col2)) continue;
            }
          else
            {
             continue;
            }
        }

     if (getValue("data.data_code3",intm).length()!=0)
        {
         if (col3.length()!=0)
            {
             if (!getValue("data.data_code3",intm).equals(col3)) continue;
            }
          else
            {
             continue;
            }
        }
     

     okFlag=1;
     break;
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************
 void  loadPtrFundData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "ptr_fund_data";
  whereStr  = "WHERE TABLE_NAME = 'PTR_FUNDP' "
            + "and   data_key in "
            + "     (select fund_code from ptr_fundp "
            + "      WHERE apr_flag      = 'Y' "
            + "      AND   (stop_flag  = 'N' "
            + "       OR    (stop_flag = 'Y' "
            + "         and   ? < stop_date)) "
            + "       and   feedback_type in ('1','2') "
            + "       and   fund_feed_flag = 'Y' "
            ;

  setString(1, businessDate);
  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
     }
  whereStr  = whereStr 
            + "     ) "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();
  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");
  showLogMessage("I","","Load ptr_fund_data Count: ["+n+"]");
 }
// ************************************************************************
 void  loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,ptr_fund_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'PTR_FUNDP' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  = '6' "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();
  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  showLogMessage("I","","Load mkt_fstpgp_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktMchtgpData(String col1,String col2,String sel,String dataType) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("mcht.data_key" , getValue("data_key"));
  setValue("mcht.data_type",dataType);
  setValue("mcht.data_code",col1);

  int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  int okFlag=0;
  for (int inti=0;inti<cnt1;inti++)
    {
     if ((getValue("mcht.data_code2",inti).length()==0)||
         ((getValue("mcht.data_code2",inti).length()!=0)&&
          (getValue("mcht.data_code2",inti).equals(col2))))
        {
         okFlag=1;
         break;
        }
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************
 int insertCycCobrandFund(int inti) throws Exception
 {
  dateTime();
  extendField = "cobr.";

  setValue("cobr.proc_date"            , businessDate);
  setValue("cobr.program_code"         , getValue("fund_code").substring(0,4));
  setValue("cobr.cobrand_code"         , getValue("parm.cobrand_code",inti));
  setValue("cobr.card_code"            , "C");
  setValue("cobr.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("cobr.p_seqno"              , getValue("p_seqno"));
  setValue("cobr.acct_type"            , getValue("acct_type"));
  setValue("cobr.acct_month"           , getValue("acct_month"));
  setValue("cobr.major_card_no"        , getValue("major_card_no")); 
  setValueInt("cobr.fund_amt"          , getValueInt("beg_tran_amt"));
  setValue("cobr.mod_time"             , sysDate+sysTime);
  setValue("cobr.mod_pgm"              , javaProgram);

  daoTable  = "cyc_cobrand_fund";

  insertTable();

  return(0);
 }
// ************************************************************************
 int  selectCycAcmm() throws Exception
 {
  selectSQL = "";
  daoTable  = "cyc_acmm_"+getValue("wday.stmt_cycle"); 
  whereStr  = "where  p_seqno       = ? "
            + "and    send_paper    = 'N' "
            + "and    send_internet = 'Y' ";

  setString(1 , getValue("p_seqno"));

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int  selectActAcno() throws Exception
 {
  extendField = "acnx.";
  selectSQL = "stat_send_paper,"
            + "stat_send_s_month,"
            + "stat_send_e_month,"
            + "stat_send_internet,"
            + "stat_send_s_month2,"
            + "stat_send_e_month2";
  daoTable  = "act_acno";
  whereStr  = "where  p_seqno       = ? "
            ;

  setString(1 , getValue("p_seqno"));

  selectTable();

  String statSMonth = "";
  String statEMonth = "";
  if (getValue("acnx.stat_send_paper").equals("Y"))
     { 
      statSMonth = getValue("acnx.stat_send_s_month");
      statEMonth = getValue("acnx.stat_send_e_month");

      if (getValue("acnx.stat_send_s_month").length()==0) statSMonth = "000000";
      if (getValue("acnx.stat_send_e_month").length()==0) statEMonth = "999912";

      if ((getValue("wday.this_acct_month").compareTo(statSMonth)>=0)&&
          (getValue("wday.this_acct_month").compareTo(statEMonth)<=0))
         {
          setValue("stat_send_paper","Y");
         }
      else
         {
          setValue("stat_send_paper","N");
         }
       }
    else
       {
        setValue("stat_send_paper","N");
       }

    if (getValue("acnx.stat_send_internet").equals("Y"))
       { 
        statSMonth = getValue("acnx.stat_send_s_month2");
        statEMonth = getValue("acnx.stat_send_e_month2");
        if (getValue("acnx.stat_send_s_month2").length()==0) statSMonth = "000000";
        if (getValue("acnx.stat_send_e_month2").length()==0) statEMonth = "999912";
        if ((getValue("wday.this_acct_month").compareTo(statSMonth)>=0)&&
            (getValue("wday.this_acct_month").compareTo(statEMonth)<=0))  
           {
            setValue("stat_send_internet","Y");
           }
        else
           {
            setValue("stat_send_internet","N");
           }
       }
    else
       {
        setValue("stat_send_internet","N");
       }

  if (getValue("stat_send_paper").equals("Y")) return(1);
  if (getValue("stat_send_internet").equals("N")) return(1);
            
  return(0);
 }
// ************************************************************************


}  // End of class FetchSample
